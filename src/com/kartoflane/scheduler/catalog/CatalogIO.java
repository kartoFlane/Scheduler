package com.kartoflane.scheduler.catalog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kartoflane.scheduler.core.CourseTypes;
import com.kartoflane.scheduler.core.Days;
import com.kartoflane.scheduler.core.TimeInterval;
import com.kartoflane.scheduler.core.Weeks;
import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.util.CatalogJsonPrettyPrinter;


public class CatalogIO {

	private static final Logger log = LogManager.getLogger(CatalogIO.class);

	private static final String groupCodeRegex = "[A-Z]{1}\\d{2}-\\d{2}[a-z]+";
	private static final String courseCodeRegex = "[A-Z]{3}\\d{6}[A-Z]{1}";
	private static final String dayRegex = "pn|wt|œr|sr|cz|pt";
	private static final String weekRegex = "TP|TN";
	private static final String rangeRegex = "\\b\\d{1,2}:\\d{2}\\s*?-\\s*?\\d{1,2}:\\d{2}\\b";
	private static final String titlesRegex = "(?i)\\b(lic|doc|prof|dr|hab|in¿|inz|mgr)\\b\\.*";

	// Edukacja.CL
	private static final String typesRegex = "\\bWyk³ad|Æwiczenia|Zajêcia laboratoryjne|Seminarium|Projekt\\b";
	private static final String slotsRegex = "(\\w+)\\s*.*?\\d+/\\d+.*?"; // TODO: confirm that it's slots, I don't remember

	private static final Pattern courseDefPtrn = Pattern.compile("^(" + courseCodeRegex + ")\\s*?(.*?)$");
	private static final Pattern classDefPtrn = Pattern.compile("^(" + groupCodeRegex + ")\\s*?(" +
			courseCodeRegex + ")" + "\\s*?" + slotsRegex + "$");
	private static final Pattern timeLocPtrn = Pattern.compile("(" + dayRegex + ")/{0,1}(" + weekRegex +
			")*?(?:\\+1/2)*?\\s*?(" + rangeRegex + "),\\s*?(.*)");

	// AKZ
	private static final Pattern courseDefPtrnAKZ = Pattern.compile("^(.*)\\((" + courseCodeRegex + ")\\),\\s*\\d{1,3}\\s*ZZU$");
	private static final Pattern groupCodePtrnAKZ = Pattern.compile("^" + groupCodeRegex);
	private static final Pattern classDefPtrnAKZ = Pattern.compile("\\((" + dayRegex + ")/?(" + weekRegex + ")?\\)\\s*(" + rangeRegex + ")," +
			"\\s*?(.*\\s*?\\(.*\\))\\s*(.*)?$");
	private static final Pattern instructorPtrnAKZ = Pattern.compile("(.*?)\\s*\\d{0,3}$");

	private CatalogIO() {
		// Static class -- disallow instantiation.
	}

	public static Catalog read(File src)
			throws JsonProcessingException, IOException {

		Catalog result = new Catalog();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		JsonNode rootNode = mapper.readTree(src);

		JsonNode coursesNode = rootNode.get("courses");
		Iterator<Entry<String, JsonNode>> nodeIterator = coursesNode.fields();
		while (nodeIterator.hasNext()) {
			Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
			result.addCourse(new CourseData(entry.getKey(), entry.getValue().asText()));
		}

		JsonNode groupsNode = rootNode.get("classGroups");
		for (CourseTypes type : CourseTypes.values()) {
			JsonNode typeNode = groupsNode.get(type.name().toLowerCase());

			if (typeNode != null) {
				nodeIterator = typeNode.fields();
				while (nodeIterator.hasNext()) {
					Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
					for (JsonNode node : entry.getValue()) {
						result.addClassGroup(new ClassGroup(result.getCourse(entry.getKey()), node.asText(), type));
					}
				}
			}
		}

		JsonNode classesNode = rootNode.get("classes");
		nodeIterator = classesNode.fields();
		while (nodeIterator.hasNext()) {
			Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
			ClassGroup group = result.getClassGroup(entry.getKey());

			if (group == null) {
				throw new IllegalArgumentException("Encountered undeclared group code: " + entry.getKey());
			}
			else {
				for (JsonNode classNode : entry.getValue()) {
					Days day = Days.eval(classNode.get("day").asText());
					Weeks week = Weeks.eval(classNode.get("week").asText());
					TimeInterval time = new TimeInterval(classNode.get("time").asText());
					String instructor = classNode.get("instructor").asText();
					String location = classNode.get("location").asText();

					result.addClass(new ClassData(group, instructor, location, time, day, week));
				}
			}
		}

		return result;
	}

	public static void write(Catalog catalog, File output)
			throws JsonGenerationException, JsonMappingException, IOException {

		Map<String, CourseData> courseMap = catalog.getCourses();
		Map<String, ClassGroup> groupMap = catalog.getClassGroupMap();
		Set<ClassData> classSet = catalog.getClasses();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

		ObjectNode root = mapper.createObjectNode();

		ObjectNode coursesNode = root.putObject("courses");
		for (String courseCode : courseMap.keySet()) {
			CourseData sd = courseMap.get(courseCode);
			coursesNode.put(courseCode, sd.name);
		}

		ObjectNode groupsNode = root.putObject("classGroups");
		for (String groupCode : groupMap.keySet()) {
			ClassGroup cgd = groupMap.get(groupCode);

			ObjectNode typeNode = (ObjectNode) groupsNode.get(cgd.type.name().toLowerCase());
			if (typeNode == null) {
				typeNode = groupsNode.putObject(cgd.type.name().toLowerCase());
			}

			ArrayNode courseNode = (ArrayNode) typeNode.get(cgd.course.courseCode);
			if (courseNode == null) {
				courseNode = typeNode.putArray(cgd.course.courseCode);
			}

			courseNode.add(groupCode);
		}

		ObjectNode classesNode = root.putObject("classes");
		for (ClassData cd : classSet) {
			ArrayNode groupNode = (ArrayNode) classesNode.get(cd.group.groupCode);
			if (groupNode == null) {
				groupNode = classesNode.putArray(cd.group.groupCode);
			}

			ObjectNode node = groupNode.addObject();
			node.put("day", cd.day.name().toLowerCase());
			node.put("week", cd.week.name().toLowerCase());
			node.put("time", cd.time.toString());
			node.put("instructor", cd.instructor);
			node.put("location", cd.location);
		}

		CatalogJsonPrettyPrinter pp = new CatalogJsonPrettyPrinter();
		mapper.writer(pp).writeValue(output, root);
	}

	/**
	 * 
	 * @param catalog
	 *            catalog in which to save the data
	 * @param text
	 *            ctrl+A copy of the enrollment page
	 * @return the catalog passed in argument
	 */
	public static Catalog scrapeECL(Catalog catalog, String text, LocaleManager localem) {
		Scanner sc = new Scanner(text);

		try {
			while (sc.hasNextLine()) {
				String line = sc.nextLine();

				Matcher m = courseDefPtrn.matcher(line);
				if (m.find()) {
					String code = m.group(1).trim();
					code = code.substring(0, code.length() - 1);

					CourseData sd = new CourseData(code, m.group(2).trim());
					if (catalog.addCourse(sd)) {
						log.trace("Added new course: " + sd);
					}
				}

				m = classDefPtrn.matcher(line);
				if (m.find()) {
					String group = m.group(1);
					ClassGroup cg = catalog.getClassGroup(group);

					if (cg == null) {
						String courseCodeWhole = m.group(2).trim();
						String courseCode = courseCodeWhole.substring(0, courseCodeWhole.length() - 1);

						String subjectType = courseCodeWhole.substring(courseCodeWhole.length() - 1);
						CourseTypes type = CourseTypes.eval(subjectType);

						CourseData course = catalog.getCourse(courseCode);

						if (course == null) {
							log.error("Course not found: " + courseCode);
							continue;
						}

						cg = new ClassGroup(course, group, type);
						if (catalog.addClassGroup(cg)) {
							log.trace("Added new class group: " + cg.toString(localem));
						}
					}

					line = sc.nextLine();
					line = line.replaceAll(titlesRegex, "").replaceAll("\\.", "");
					line = line.replaceAll(typesRegex, "");
					String prof = line.trim();

					m = timeLocPtrn.matcher(line);
					if (m.find()) {
						Days day = Days.eval(m.group(1).trim());
						Weeks week = Weeks.eval(m.group(2));
						TimeInterval time = new TimeInterval(m.group(3).trim());
						String loc = m.group(4).trim();

						ClassData cd = new ClassData(cg, prof, loc, time, day, week);
						if (catalog.addClass(cd)) {
							log.trace("Added new class: " + cd.toString(localem));
						}
					}
					else {
						line = sc.nextLine();
						m = timeLocPtrn.matcher(line);
						if (m.find()) {
							Days day = Days.eval(m.group(1).trim());
							Weeks week = Weeks.eval(m.group(2));
							TimeInterval time = new TimeInterval(m.group(3).trim());
							String loc = m.group(4).trim();

							ClassData cd = new ClassData(cg, prof, loc, time, day, week);
							if (catalog.addClass(cd)) {
								log.trace("Added new class: " + cd.toString(localem));
							}
						}
						else {
							log.error("Found a class def, but could not follow up with time & location:\n" + line);
						}
					}
				}
			}
		}
		finally {
			sc.close();
		}

		return catalog;
	}

	private static void finalizeClasses(Catalog catalog, LocaleManager localem, List<IncompleteClassData> cList, Set<String> iList) {
		if (cList.size() > 0) {
			if (iList.size() == 0) {
				log.error("No instructors!");
			}
			else if (iList.size() == 1) {
				String instructor = iList.iterator().next();
				for (IncompleteClassData icd : cList) {
					ClassData cd = new ClassData(icd.group, instructor, icd.loc, icd.time, icd.day, icd.week);
					if (catalog.addClass(cd)) {
						// log.trace("Added new class: " + cd.toString(localem));
					}
				}
			}
			else {
				log.trace("Multiple instructors: =========================");
				log.trace(cList.get(0).group.toString(localem));
				for (String instr : iList) {
					log.trace(instr);
				}
				log.trace("===============================================");
				// TODO: Figure out what to do when there are multiple instructors
			}
		}

		cList.clear();
		iList.clear();
	}

	/**
	 * FIXME groups and classes sometimes are not matched correctly
	 * FIXME instructors are not matched correctly
	 * FIXME nor do hours
	 * FIXME truth be told, nothing really works
	 * 
	 * TODO figure out what to do with courses that have no 'classes' (e-learning courses, biking/hiking/camps)
	 */
	public static Catalog scrapeAKZ(Catalog catalog, String text, LocaleManager localem) {
		Scanner sc = new Scanner(text);

		try {
			CourseData currentCourse = null;
			CourseTypes currentType = null;
			ClassGroup currentGroup = null;
			List<IncompleteClassData> classList = new ArrayList<IncompleteClassData>();
			Set<String> instructorList = new HashSet<String>();

			boolean foundClass = false;

			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				foundClass = false;

				Matcher m = courseDefPtrnAKZ.matcher(line);
				if (m.find()) {
					finalizeClasses(catalog, localem, classList, instructorList);

					String name = m.group(1).trim().replaceAll("\\s{0,1}/\\s{0,1}", " - ");
					String code = m.group(2).trim();
					currentType = CourseTypes.eval(code.substring(code.length() - 1, code.length()));
					code = code.substring(0, code.length() - 1);

					currentCourse = new CourseData(code, name);
					if (catalog.addCourse(currentCourse)) {
						// log.trace("Added new course: " + currentCourse);
					}
				}

				m = groupCodePtrnAKZ.matcher(line);
				if (m.find()) {
					finalizeClasses(catalog, localem, classList, instructorList);

					currentGroup = new ClassGroup(currentCourse, m.group(), currentType);
					if (catalog.addClassGroup(currentGroup)) {
						// log.trace("Added new class group: " + currentGroup.toString(localem));
					}

					m = classDefPtrnAKZ.matcher(line);
					if (m.find()) {
						Days day = Days.eval(m.group(1));
						Weeks week = Weeks.eval(m.group(2));
						TimeInterval time = new TimeInterval(m.group(3).trim());
						String loc = m.group(4).trim();
						String instructor = m.group(5);

						if (instructor == null) {
							classList.add(new IncompleteClassData(currentGroup, day, week, time, loc));
						}
						else {
							instructor = instructor.replaceAll(titlesRegex, "").replaceAll("\\d", "").trim();
							int l = instructor.split(" ").length;
							if (l > 1 && l < 5) {
								ClassData cd = new ClassData(currentGroup, instructor, loc, time, day, week);
								if (catalog.addClass(cd)) {
									// log.trace("Added new class: " + cd.toString(localem));
								}
							}
							else {
								classList.add(new IncompleteClassData(currentGroup, day, week, time, loc));
							}
						}

						foundClass = true;
					}
					else {
						log.error("Found group def, but could not find class def: " + line);
					}
				}

				m = classDefPtrnAKZ.matcher(line);
				while (m.find()) {
					Days day = Days.eval(m.group(1));
					Weeks week = Weeks.eval(m.group(2));
					TimeInterval time = new TimeInterval(m.group(3).trim());
					String loc = m.group(4).trim();
					String instructor = m.group(5);

					if (instructor == null) {
						classList.add(new IncompleteClassData(currentGroup, day, week, time, loc));
					}
					else {
						instructor = instructor.replaceAll(titlesRegex, "").replaceAll("\\d", "").trim();
						int l = instructor.split(" ").length;
						if (l > 1 && l < 5) {
							ClassData cd = new ClassData(currentGroup, instructor, loc, time, day, week);
							if (catalog.addClass(cd)) {
								// log.trace("Added new class: " + cd.toString(localem));
							}
						}
						else {
							classList.add(new IncompleteClassData(currentGroup, day, week, time, loc));
						}
					}

					foundClass = true;

					line = sc.nextLine();
					m.reset(line);
				}

				m = instructorPtrnAKZ.matcher(line);
				if (foundClass && m.find()) {
					String instructor = m.group(1).replaceAll(titlesRegex, "").trim();
					int l = instructor.split(" ").length;
					if (l > 1 && l < 5) {
						instructorList.add(instructor);
					}
				}
			}
		}
		finally {
			sc.close();
		}

		return catalog;
	}

	private static class IncompleteClassData {
		public final ClassGroup group;
		public final Days day;
		public final Weeks week;
		public final TimeInterval time;
		public final String loc;

		public IncompleteClassData(ClassGroup g, Days d, Weeks w, TimeInterval t, String l) {
			group = g;
			day = d;
			week = w;
			time = t;
			loc = l;
		}
	}
}

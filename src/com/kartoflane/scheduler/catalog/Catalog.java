package com.kartoflane.scheduler.catalog;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class Catalog {

	private final Map<String, CourseData> courseMap;
	private final Map<String, ClassGroup> groupMap;
	private final Set<ClassData> classSet;
	private final Map<ClassGroup, Set<ClassData>> groupContentsMap;

	// Lazily instantiated views
	private Map<String, CourseData> courseMapView = null;
	private Map<String, ClassGroup> groupMapView = null;
	private Set<ClassData> classSetView = null;

	public Catalog() {
		courseMap = new TreeMap<String, CourseData>();
		groupMap = new TreeMap<String, ClassGroup>();
		classSet = new HashSet<ClassData>();
		groupContentsMap = new HashMap<ClassGroup, Set<ClassData>>();
	}

	public boolean addCourse(CourseData cd) {
		if (cd == null) {
			throw new IllegalArgumentException("Argument is null");
		}

		if (!courseMap.containsKey(cd.courseCode)) {
			courseMap.put(cd.courseCode, cd);
			return true;
		}
		return false;
	}

	public CourseData getCourse(String courseCode) {
		return courseMap.get(courseCode);
	}

	public Map<String, CourseData> getCourses() {
		if (courseMapView == null) {
			courseMapView = Collections.unmodifiableMap(courseMap);
		}
		return courseMapView;
	}

	public boolean addClassGroup(ClassGroup cg) {
		if (cg == null) {
			throw new IllegalArgumentException("Argument is null");
		}

		if (!groupMap.containsKey(cg.groupCode)) {
			groupMap.put(cg.groupCode, cg);
			Set<ClassData> classes = new HashSet<ClassData>();
			groupContentsMap.put(cg, classes);
			cg.setClasses(Collections.unmodifiableSet(classes));
			return true;
		}
		return false;
	}

	public ClassGroup getClassGroup(String group) {
		return groupMap.get(group);
	}

	public Map<String, ClassGroup> getClassGroupMap() {
		if (groupMapView == null) {
			groupMapView = Collections.unmodifiableMap(groupMap);
		}
		return groupMapView;
	}

	public Collection<ClassGroup> getClassGroups() {
		return Collections.unmodifiableCollection(groupMap.values());
	}

	public boolean addClass(ClassData cd) {
		if (cd == null) {
			throw new IllegalArgumentException("Argument is null");
		}

		groupContentsMap.get(cd.group).add(cd);
		return classSet.add(cd);
	}

	public ClassData getFirstClass(ClassGroup cg) {
		Set<ClassData> set = cg.getClasses();
		for (ClassData cd : set) {
			return cd;
		}
		return null;
	}

	public Set<ClassData> getClasses() {
		if (classSetView == null) {
			classSetView = Collections.unmodifiableSet(classSet);
		}

		return classSetView;
	}
}

package com.kartoflane.scheduler.catalog;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.kartoflane.scheduler.core.ClassAlreadyEnrolledException;
import com.kartoflane.scheduler.core.ClassOverlapException;
import com.kartoflane.scheduler.core.CourseAlreadyEnrolledException;
import com.kartoflane.scheduler.core.Days;
import com.kartoflane.scheduler.util.CatalogJsonPrettyPrinter;


public class ScheduleIO {

	private ScheduleIO() {
		// Static class -- disallow instantiation.
	}

	public static Schedule read(Catalog cat, File src)
			throws JsonProcessingException, IOException, ClassOverlapException,
			ClassAlreadyEnrolledException, CourseAlreadyEnrolledException {
		Schedule result = new Schedule();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		JsonNode root = mapper.readTree(src);

		for (JsonNode groupNode : root) {
			String groupCode = groupNode.asText();
			ClassGroup group = cat.getClassGroup(groupCode);

			if (group == null) {
				throw new IllegalArgumentException(String.format("Could not find group for code '%s' in " +
						"currently loaded catalog.", groupCode));
			}
			else {
				result.add(group);
			}
		}

		return result;
	}

	public static void write(Schedule schedule, File output)
			throws JsonGenerationException, JsonMappingException, IOException {

		Set<String> groupCodeSet = new HashSet<String>();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

		ArrayNode root = mapper.createArrayNode();

		for (Days day : Days.values()) {
			Set<ClassData> classes = schedule.listClasses(day);

			for (ClassData cd : classes) {
				if (!groupCodeSet.contains(cd.group.groupCode)) {
					root.add(cd.group.groupCode);
					groupCodeSet.add(cd.group.groupCode);
				}
			}
		}

		CatalogJsonPrettyPrinter pp = new CatalogJsonPrettyPrinter();
		mapper.writer(pp).writeValue(output, root);
	}
}

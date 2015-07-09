package com.kartoflane.scheduler.catalog;

/**
 * This class represents a single course, that comprises several subjects,
 * for example lecture and practice classes.
 * 
 * @author kartoFlane
 *
 */
public class CourseData {

	// Unmodifiable fields - safe to expose
	public final String courseCode;
	public final String name;

	public CourseData(String courseCode, String name) {
		if (courseCode == null)
			throw new IllegalArgumentException("Course code is null.");
		if (name == null)
			throw new IllegalArgumentException("Course name is null.");

		this.courseCode = courseCode;
		this.name = name;
	}

	@Override
	public int hashCode() {
		return courseCode.hashCode();
	}

	@Override
	public String toString() {
		return "CourseData { " + courseCode + " - " + name + " }";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CourseData) {
			CourseData cd = (CourseData) o;
			return courseCode.equals(cd.courseCode);
		}
		else {
			return false;
		}
	}
}

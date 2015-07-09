package com.kartoflane.scheduler.catalog;

import java.util.Set;

import com.kartoflane.scheduler.core.CourseTypes;
import com.kartoflane.scheduler.core.Days;
import com.kartoflane.scheduler.core.TimeInterval;
import com.kartoflane.scheduler.core.Weeks;
import com.kartoflane.scheduler.locale.LocaleManager;


/**
 * A ClassGroup represents a single subject that can have several classes
 * comprising it, happening at different times and places throughout the week.
 * 
 * This is mostly true for language subjects that often have two classes a week,
 * both belonging to the same class group.
 * 
 * @author kartoFlane
 *
 */
public class ClassGroup {

	public static final String MULTIPLE_VALUES = "(Multiple)";

	// Unmodifiable fields - safe to expose
	public final CourseData course;

	public final String groupCode;
	public final CourseTypes type;

	// The set is unmodifiable, but the field is, hide it
	private Set<ClassData> classes;

	public ClassGroup(CourseData subject, String groupCode, CourseTypes type) {
		if (subject == null)
			throw new IllegalArgumentException("Subject is null.");
		if (groupCode == null)
			throw new IllegalArgumentException("Group code is null.");
		if (type == null)
			throw new IllegalArgumentException("Class type is null.");

		this.course = subject;
		this.groupCode = groupCode;
		this.type = type;
	}

	public Set<ClassData> getClasses() {
		return classes;
	}

	/**
	 * Quasi-initiation method, intended to only be called once when the group is
	 * added to the catalog.
	 */
	protected void setClasses(Set<ClassData> view) {
		classes = view;
	}

	public Object[] toRowData(LocaleManager localem) {
		Object[] result = new Object[8];
		Days day = null;
		Weeks week = null;
		TimeInterval ti = null;
		String instructor = null;
		String location = null;

		for (ClassData cd : classes) {
			if (day != null && day != cd.day) {
				result[0] = MULTIPLE_VALUES;
			}
			else {
				day = cd.day;
			}

			if (ti != null && !ti.equals(cd.time)) {
				result[1] = MULTIPLE_VALUES;
			}
			else {
				ti = cd.time;
			}

			if (week != null && week != cd.week) {
				result[2] = MULTIPLE_VALUES;
			}
			else {
				week = cd.week;
			}

			if (instructor != null && !instructor.equals(cd.instructor)) {
				result[6] = MULTIPLE_VALUES;
			}
			else {
				instructor = cd.instructor;
			}

			if (location != null && !location.equals(cd.location)) {
				result[7] = MULTIPLE_VALUES;
			}
			else {
				location = cd.location;
			}
		}

		result[3] = groupCode;
		result[4] = type.toShort(localem);
		result[5] = course.name;

		if (result[0] == null)
			result[0] = day;
		if (result[1] == null)
			result[1] = ti;
		if (result[2] == null)
			result[2] = week;
		if (result[6] == null)
			result[6] = instructor;
		if (result[7] == null)
			result[7] = location;

		return result;
	}

	public String toString(LocaleManager localem) {
		return "ClassGroupData { " + course.courseCode + " " + type.toShort(localem) + "; " + groupCode + " }";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupCode == null) ? 0 : groupCode.hashCode());
		result = prime * result + ((course == null) ? 0 : course.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ClassGroup))
			return false;
		ClassGroup other = (ClassGroup) obj;
		if (groupCode == null) {
			if (other.groupCode != null)
				return false;
		}
		else if (!groupCode.equals(other.groupCode))
			return false;
		if (course == null) {
			if (other.course != null)
				return false;
		}
		else if (!course.equals(other.course))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}

package com.kartoflane.scheduler.catalog;

import com.kartoflane.scheduler.core.Days;
import com.kartoflane.scheduler.core.TimeInterval;
import com.kartoflane.scheduler.core.Weeks;
import com.kartoflane.scheduler.locale.LocaleManager;
import com.kartoflane.scheduler.ui.tables.CatalogTableModel;


/**
 * This class holds data pertaining to the time and location of
 * meetings of a particular subject.
 */
public class ClassData implements Comparable<ClassData> {

	// Unmodifiable fields - safe to expose
	public final ClassGroup group;

	public final Days day;
	public final Weeks week;

	public final String instructor;
	public final String location;

	// Modifiable fields
	protected final TimeInterval time;

	public ClassData(ClassGroup group, String instructor, String location, TimeInterval time, Days day, Weeks w) {
		if (group == null) {
			throw new IllegalArgumentException("Class group is null.");
		}
		if (instructor == null) {
			throw new IllegalArgumentException("Class instructor is null.");
		}
		if (location == null) {
			throw new IllegalArgumentException("Class location is null.");
		}
		if (time == null) {
			throw new IllegalArgumentException("Class time is null.");
		}
		if (day == null) {
			throw new IllegalArgumentException("Class day is null.");
		}
		if (w == null) {
			throw new IllegalArgumentException("Class week is null.");
		}

		this.group = group;
		this.instructor = instructor;
		this.location = location;
		this.time = time;
		this.day = day;
		this.week = w;
	}

	/**
	 * Returns a copy of the class' own TimeInterval.
	 */
	public TimeInterval getTime() {
		return new TimeInterval(time);
	}

	public Object toCellData(LocaleManager localem) {
		// It's possible to have multiline text, however doing so requires wrapping
		// it in <html> tags and using <br/> instead of newlines.
		// For whatever reason, Swing's HTML parsing speed is absolutely atrocious,
		// and makes the table react to user input extremely sluggishly
		StringBuilder buf = new StringBuilder();
		buf.append(group.type.toShort(localem));
		buf.append(" - ");
		buf.append(group.course.name);

		return buf.toString();
	}

	public Object[] toRowData(LocaleManager localem) {
		return new Object[] {
				day, time, week, group.groupCode, group.type.toShort(localem),
				group.course.name, instructor, location
		};
	}

	@Override
	public int compareTo(ClassData o) {
		int result = day.compareTo(o.day);
		if (result == 0) {
			result = time.compareTo(o.time);
			if (result == 0) {
				result = week.compareTo(o.week);
			}
		}
		return result;
	}

	public boolean collides(ClassData cd) {
		return day == cd.day && week.collides(cd.week) && time.intersects(cd.time);
	}

	public boolean intersects(TimeInterval time) {
		return this.time.intersects(time);
	}

	public String toString(LocaleManager localem) {
		return "ClassData { " +
				group.course.courseCode + " " +
				group.type.toShort(localem) + "; " +
				group.groupCode + "; " +
				instructor + "; " +
				day.toString(localem) + " " +
				week.toShort(localem) + " " +
				time.toString() + " @ " +
				location +
				" }";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((instructor == null) ? 0 : instructor.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((week == null) ? 0 : week.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ClassData)) {
			return false;
		}
		ClassData other = (ClassData) obj;
		if (day != other.day) {
			return false;
		}
		if (group == null) {
			if (other.group != null) {
				return false;
			}
		}
		else if (!group.equals(other.group))
			return false;
		if (location == null) {
			if (other.location != null) {
				return false;
			}
		}
		else if (!location.equals(other.location)) {
			return false;
		}
		if (instructor == null) {
			if (other.instructor != null) {
				return false;
			}
		}
		else if (!instructor.equals(other.instructor)) {
			return false;
		}
		if (time == null) {
			if (other.time != null) {
				return false;
			}
		}
		else if (!time.equals(other.time)) {
			return false;
		}
		if (week != other.week) {
			return false;
		}
		return true;
	}

	public Object getValueByColumn(LocaleManager localem, int col) {
		switch (col) {
			case CatalogTableModel.COLUMN_DAY:
				return day.toString(localem);
			case CatalogTableModel.COLUMN_TIME:
				return time.toString();
			case CatalogTableModel.COLUMN_WEEK:
				return week.toString(localem);
			case CatalogTableModel.COLUMN_GROUP:
				return group.groupCode;
			case CatalogTableModel.COLUMN_TYPE:
				return group.type.toString(localem);
			case CatalogTableModel.COLUMN_COURSE:
				return group.course.name;
			case CatalogTableModel.COLUMN_INSTR:
				return instructor;
			case CatalogTableModel.COLUMN_LOC:
				return location;

			default:
				throw new IllegalArgumentException("" + col);
		}
	}
}

package com.kartoflane.scheduler.core;

import java.util.Set;

import com.kartoflane.scheduler.catalog.ClassData;
import com.kartoflane.scheduler.catalog.ClassGroup;
import com.kartoflane.scheduler.catalog.Schedule;


public class GroupFilter implements IPredicate<ClassGroup> {

	private final Schedule schedule;
	private final Days day;
	private final Weeks week;
	private final TimeInterval ti;

	/**
	 * Any of the arguments can be null to not check that property.
	 */
	public GroupFilter(Schedule sch, Days day, Weeks week, TimeInterval ti) {
		this.schedule = sch;
		this.day = day;
		this.week = week;
		this.ti = ti;
	}

	public boolean accept(ClassGroup cg) {
		boolean result = false;

		Set<ClassData> classes = cg.getClasses();

		for (ClassData cd : classes) {
			boolean matched = true;
			if (day != null) {
				matched &= cd.day == day;
			}
			if (week != null) {
				matched &= cd.week == week || cd.week == Weeks.EACH;
			}

			if (matched && ti != null) {
				TimeInterval time = cd.getTime();
				if (day != null && ti.equals(time)) {
					// Immediately match if the selected time matches the class' time
					// exactly, and the the user is enrolled in the class.
					// Means that the cell selected in the schedule table is exactly
					// the cell in which this class is located.
					return schedule.contains(cd);
				}
				else {
					matched &= ti.intersects(time);
				}
			}

			// Include the group if any of its member classes has been matched
			result |= matched;
		}

		// Filter out subjects that the user's schedule already covers
		result &= !schedule.contains(cg);

		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + ((schedule == null) ? 0 : schedule.hashCode());
		result = prime * result + ((ti == null) ? 0 : ti.hashCode());
		result = prime * result + ((week == null) ? 0 : week.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GroupFilter))
			return false;
		GroupFilter other = (GroupFilter) obj;
		if (day != other.day)
			return false;
		if (schedule == null) {
			if (other.schedule != null)
				return false;
		}
		else if (!schedule.equals(other.schedule))
			return false;
		if (ti == null) {
			if (other.ti != null)
				return false;
		}
		else if (!ti.equals(other.ti))
			return false;
		if (week != other.week)
			return false;
		return true;
	}
}

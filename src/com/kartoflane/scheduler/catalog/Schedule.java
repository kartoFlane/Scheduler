package com.kartoflane.scheduler.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kartoflane.scheduler.core.ClassAlreadyEnrolledException;
import com.kartoflane.scheduler.core.ClassOverlapException;
import com.kartoflane.scheduler.core.CourseAlreadyEnrolledException;
import com.kartoflane.scheduler.core.Days;
import com.kartoflane.scheduler.core.TimeInterval;
import com.kartoflane.scheduler.core.Weeks;


/**
 * A container for the classes selected by the user.
 * 
 * @author kartoFlane
 * 
 */
public class Schedule {

	private Map<Days, Map<Weeks, List<ClassData>>> dayMap;

	public Schedule() {
		dayMap = new HashMap<Days, Map<Weeks, List<ClassData>>>();

		for (Days day : Days.values()) {
			Map<Weeks, List<ClassData>> weekMap = new HashMap<Weeks, List<ClassData>>();
			dayMap.put(day, weekMap);

			weekMap.put(Weeks.EVEN, new ArrayList<ClassData>());
			weekMap.put(Weeks.ODD, new ArrayList<ClassData>());
		}
	}

	public boolean add(ClassGroup cg)
			throws ClassOverlapException, ClassAlreadyEnrolledException, CourseAlreadyEnrolledException {
		if (cg == null) {
			throw new IllegalArgumentException("Argument is null");
		}

		// Check prerequisites
		if (contains(cg)) {
			throw new CourseAlreadyEnrolledException(cg);
		}

		for (ClassData cd : cg.getClasses()) {
			if (!isFree(cd.day, cd.week, cd.time)) {
				throw new ClassOverlapException(cd);
			}
			if (cd.week == Weeks.EACH) {
				if (dayMap.get(cd.day).get(Weeks.EVEN).contains(cd) ||
						dayMap.get(cd.day).get(Weeks.ODD).contains(cd)) {
					throw new ClassAlreadyEnrolledException(cd);
				}
			}
			else {
				if (dayMap.get(cd.day).get(cd.week).contains(cd)) {
					throw new ClassAlreadyEnrolledException(cd);
				}
			}
		}

		boolean result = true;
		for (ClassData cd : cg.getClasses()) {
			boolean temp = false;

			if (cd.week == Weeks.EACH) {
				temp = dayMap.get(cd.day).get(Weeks.EVEN).add(cd);
				temp |= dayMap.get(cd.day).get(Weeks.ODD).add(cd);
			}
			else {
				temp = dayMap.get(cd.day).get(cd.week).add(cd);
			}

			if (temp && !result) {
				throw new IllegalStateException();
			}

			result &= temp;
		}

		return result;
	}

	public boolean remove(ClassGroup cg) {
		if (cg == null) {
			return false;
		}

		boolean result = true;
		for (ClassData cd : cg.getClasses()) {
			boolean temp = false;

			if (cd.week == Weeks.EACH) {
				temp = dayMap.get(cd.day).get(Weeks.EVEN).remove(cd);
				temp |= dayMap.get(cd.day).get(Weeks.ODD).remove(cd);
			}
			else {
				temp = dayMap.get(cd.day).get(cd.week).remove(cd);
			}

			if (temp && !result) {
				throw new IllegalStateException();
			}

			result &= temp;
		}

		return result;
	}

	public boolean contains(ClassData cd) {
		if (cd == null) {
			return false;
		}

		if (cd.week == Weeks.EACH) {
			List<ClassData> even = dayMap.get(cd.day).get(Weeks.EVEN);
			List<ClassData> odd = dayMap.get(cd.day).get(Weeks.ODD);

			return even.contains(cd) && odd.contains(cd);
		}
		else {
			List<ClassData> classes = dayMap.get(cd.day).get(cd.week);

			return classes.contains(cd);
		}
	}

	public boolean contains(ClassGroup cg) {
		if (cg == null) {
			return false;
		}

		final Weeks[] weeks = { Weeks.ODD, Weeks.EVEN };

		for (Days day : Days.values()) {
			for (Weeks week : weeks) {
				List<ClassData> classes = dayMap.get(day).get(week);
				for (ClassData cd : classes) {
					if (cd.group.course.equals(cg.course) && cd.group.type == cg.type)
						return true;
				}
			}
		}

		return false;
	}

	public boolean isFree(Days day, Weeks week, TimeInterval time) {
		if (day == null) {
			throw new IllegalArgumentException("Day is null");
		}
		if (week == null) {
			throw new IllegalArgumentException("Week is null");
		}
		if (time == null) {
			throw new IllegalArgumentException("Time is null");
		}

		if (week == Weeks.EACH) {
			return isFree(day, Weeks.EVEN, time) && isFree(day, Weeks.ODD, time);
		}
		else {
			List<ClassData> classes = dayMap.get(day).get(week);

			for (ClassData cd : classes) {
				if (cd.intersects(time)) {
					return false;
				}
			}

			return true;
		}
	}

	public Set<ClassData> listClasses(Days day, Weeks week) {
		Set<ClassData> result = new HashSet<ClassData>();
		List<ClassData> even = dayMap.get(day).get(Weeks.EVEN);
		List<ClassData> odd = dayMap.get(day).get(Weeks.ODD);

		if (week == Weeks.EACH) {
			if (even != null && odd != null) {
				for (ClassData cd : even) {
					if (odd.contains(cd)) {
						result.add(cd);
					}
				}
			}
		}
		else {
			if (even != null && odd != null) {
				List<ClassData> thiz = week == Weeks.EVEN ? even : odd;
				List<ClassData> other = week == Weeks.EVEN ? odd : even;
				for (ClassData cd : thiz) {
					if (!other.contains(cd)) {
						result.add(cd);
					}
				}
			}
			else if (dayMap.get(day).get(week) != null) {
				result.addAll(dayMap.get(day).get(week));
			}
		}
		return result;
	}

	public Set<ClassData> listClasses(Days day) {
		Set<ClassData> result = new HashSet<ClassData>();

		for (Weeks week : Weeks.values()) {
			List<ClassData> list = dayMap.get(day).get(week);

			if (list != null) {
				result.addAll(list);
			}
		}

		return result;
	}

	public ClassData getClass(Days day, Weeks week, TimeInterval time) {
		if (day == null) {
			throw new IllegalArgumentException("Day is null");
		}
		if (week == null) {
			throw new IllegalArgumentException("Week is null");
		}
		if (time == null) {
			throw new IllegalArgumentException("Time is null");
		}

		if (week == Weeks.EACH) {
			List<ClassData> even = dayMap.get(day).get(Weeks.EVEN);
			List<ClassData> odd = dayMap.get(day).get(Weeks.ODD);

			for (ClassData cd : even) {
				if (odd.contains(cd) && cd.intersects(time)) {
					return cd;
				}
			}

			return null;
		}
		else {
			List<ClassData> classes = dayMap.get(day).get(week);

			for (ClassData cd : classes) {
				if (cd.intersects(time)) {
					return cd;
				}
			}

			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dayMap == null) ? 0 : dayMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Schedule))
			return false;
		Schedule other = (Schedule) obj;
		if (dayMap == null) {
			if (other.dayMap != null)
				return false;
		}
		else if (!dayMap.equals(other.dayMap))
			return false;
		return true;
	}
}

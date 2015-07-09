package com.kartoflane.scheduler.core;

import com.kartoflane.scheduler.catalog.ClassGroup;


@SuppressWarnings("serial")
public class CourseAlreadyEnrolledException extends Exception {

	private final ClassGroup cg;

	public CourseAlreadyEnrolledException(ClassGroup cg) {
		super();
		this.cg = cg;
	}

	public CourseAlreadyEnrolledException(ClassGroup cg, String msg) {
		super(msg);
		this.cg = cg;
	}

	public CourseAlreadyEnrolledException(ClassGroup cg, String msg, Throwable cause) {
		super(msg, cause);
		this.cg = cg;
	}

	public CourseAlreadyEnrolledException(ClassGroup cg, Throwable cause) {
		super(cause);
		this.cg = cg;
	}

	public ClassGroup getClassGroup() {
		return cg;
	}
}

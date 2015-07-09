package com.kartoflane.scheduler.core;

import com.kartoflane.scheduler.catalog.ClassData;


@SuppressWarnings("serial")
public class ClassAlreadyEnrolledException extends Exception {

	private final ClassData cd;

	public ClassAlreadyEnrolledException(ClassData cd) {
		super();
		this.cd = cd;
	}

	public ClassAlreadyEnrolledException(ClassData cd, String msg) {
		super(msg);
		this.cd = cd;
	}

	public ClassAlreadyEnrolledException(ClassData cd, String msg, Throwable cause) {
		super(msg, cause);
		this.cd = cd;
	}

	public ClassAlreadyEnrolledException(ClassData cd, Throwable cause) {
		super(cause);
		this.cd = cd;
	}

	public ClassData getClassData() {
		return cd;
	}
}

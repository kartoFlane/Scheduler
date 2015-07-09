package com.kartoflane.scheduler.core;

import com.kartoflane.scheduler.catalog.ClassData;


@SuppressWarnings("serial")
public class ClassOverlapException extends Exception {

	private final ClassData cd;

	public ClassOverlapException(ClassData cd) {
		super();
		this.cd = cd;
	}

	public ClassOverlapException(ClassData cd, String msg) {
		super(msg);
		this.cd = cd;
	}

	public ClassOverlapException(ClassData cd, String msg, Throwable cause) {
		super(msg, cause);
		this.cd = cd;
	}

	public ClassOverlapException(ClassData cd, Throwable cause) {
		super(cause);
		this.cd = cd;
	}

	public ClassData getClassData() {
		return cd;
	}
}

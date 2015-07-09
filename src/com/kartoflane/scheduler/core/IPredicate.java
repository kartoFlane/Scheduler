package com.kartoflane.scheduler.core;

public interface IPredicate<T> {
	public boolean accept(T o);
}

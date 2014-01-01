package com.upcrob.jotp;

public enum TokenstoreType {
	LOCAL("local"), JDBC("jdbc"), REDIS("redis");
	
	private String name;
	
	private TokenstoreType(String s) {
		name = s;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

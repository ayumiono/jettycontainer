package com.gb.pos.jettycontainer;

public class DubboBootException extends Exception {

	private static final long serialVersionUID = 1477516761984857263L;

	public DubboBootException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public DubboBootException(String msg) {
		super(msg);
	}
}

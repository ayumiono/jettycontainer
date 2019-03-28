package com.gb.pos.jettycontainer.logback;

import org.apache.commons.lang3.StringUtils;

public class Assert {
	public static void notNull(Object obj, String msg) {
		if(obj == null)
			throw new RuntimeException(msg);
	}
	public static void hasLength(String text, String msg) {
		if(StringUtils.isEmpty(text))
			throw new RuntimeException(msg);
	}
}

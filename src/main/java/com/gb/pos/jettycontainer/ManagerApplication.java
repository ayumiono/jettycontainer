package com.gb.pos.jettycontainer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class ManagerApplication extends Application {
	
	public ManagerApplication() {
		HashSet<Object> objects = new HashSet<Object>();
		objects.add(InjectorSingleton.getInstance().getInjector().getInstance(AppRuntimeInfoInterface.class));
		singletons = Collections.unmodifiableSet(objects);
	}

	private final Set<Object> singletons;

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}

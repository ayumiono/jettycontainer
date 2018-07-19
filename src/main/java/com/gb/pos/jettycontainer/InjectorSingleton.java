package com.gb.pos.jettycontainer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class InjectorSingleton {

	private Injector injector = null;

	private InjectorSingleton() {
	}

	private static class InstanceHolder {
		private static InjectorSingleton instance = new InjectorSingleton();
	}

	public static InjectorSingleton getInstance() {
		return InstanceHolder.instance;
	}

	public Injector getInjector(Module module) {
		if (this.injector != null) {
			return injector;
		}
		this.injector = Guice.createInjector(module);
		return injector;
	}

	public Injector getInjector() {
		return this.injector;
	}
}

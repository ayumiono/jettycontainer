package com.gb.pos.jettycontainer;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class AppModule extends AbstractModule {
	
	@Override
	protected void configure() {
		try {
			String appLibsDir = System.getProperty("applibs.dir");
			String rootDir = System.getProperty("root.dir");
			if(appLibsDir == null) throw new RuntimeException("请配置-Dapplibs.dir参数,指定应用目录");
			binder().bindConstant().annotatedWith(Names.named("applibs.dir")).to(appLibsDir);
			if(rootDir == null) throw new RuntimeException("请配置-Droot.dir参数，指定项目根目录");
			binder().bindConstant().annotatedWith(Names.named("root.dir")).to(rootDir);
			bind(MultipleDubboBootContainer.class).toInstance(new MultipleDubboBootContainer());
			bind(AppRuntimeInfoInterface.class).to(AppRuntimeInfoRestImpl.class).in(Scopes.SINGLETON);
			bind(PidLockFileHandle.class).toInstance(new PidLockFileHandle());
		} catch (Exception e) {
			throw new RuntimeException("AppModule configure failed!");
		}
	}

}

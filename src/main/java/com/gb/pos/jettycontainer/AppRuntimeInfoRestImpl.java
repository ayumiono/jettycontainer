package com.gb.pos.jettycontainer;

import java.util.List;

import com.google.inject.Inject;

public class AppRuntimeInfoRestImpl implements AppRuntimeInfoInterface{
	
	@Inject
	MultipleDubboBootContainer container;

	@Override
	public List<AppRuntimeContext> appRuntimeInfos(){
		return container.getRuntimeContexts();
	}

	@Override
	public AppRuntimeContext restartApp(String appName) {
		return container.retryLoadApp(appName);
	}
}

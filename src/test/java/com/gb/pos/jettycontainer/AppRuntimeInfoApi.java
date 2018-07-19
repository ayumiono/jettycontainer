package com.gb.pos.jettycontainer;

import java.util.List;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.gb.pos.jettycontainer.AppRuntimeContext;
import com.gb.pos.jettycontainer.AppRuntimeInfoInterface;

public class AppRuntimeInfoApi {
	
	private ResteasyWebTarget baseTarget;
	private String baseUri = "http://localhost:8080";
	
	public AppRuntimeInfoApi() {
		init();
	}
	
	public void init() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		baseTarget = client.target(baseUri);
	}
	
	public List<AppRuntimeContext> appRuntimeInfos(){
		return baseTarget.proxy(AppRuntimeInfoInterface.class).appRuntimeInfos();
	}
	
	public static void main(String[] args) {
		AppRuntimeInfoApi api = new AppRuntimeInfoApi();
		List<AppRuntimeContext> infos = api.appRuntimeInfos();
//		System.out.println("test");
//		System.out.flush();
//		System.out.close();
		System.out.println(infos);
	}
}

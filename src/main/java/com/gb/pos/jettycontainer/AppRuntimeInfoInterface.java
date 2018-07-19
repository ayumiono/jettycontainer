package com.gb.pos.jettycontainer;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface AppRuntimeInfoInterface {
	@GET
	@Path("/appinfo")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AppRuntimeContext> appRuntimeInfos();
	
	@GET
	@Path("/restart/{appName}")
	@Produces(MediaType.APPLICATION_JSON)
	public AppRuntimeContext restartApp(@PathParam("appName") String appName);
}

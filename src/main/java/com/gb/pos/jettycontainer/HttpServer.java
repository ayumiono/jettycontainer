package com.gb.pos.jettycontainer;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;

import com.google.inject.Inject;


public class HttpServer {
	
	protected final Server webServer;
	
	private MultipleDubboBootContainer container;
	
	@Inject
	public HttpServer(MultipleDubboBootContainer container) throws IOException {
		this.container = container;
		this.webServer = new Server(80);
		startInternalManagerApp();
	}
	
	private void startInternalManagerApp() {
		ServletContextHandler contextHandler = new ServletContextHandler();
		contextHandler.setContextPath("/");
		contextHandler.addEventListener(new ResteasyBootstrap());
		contextHandler.setAttribute("resteasy.scan", true);
//		contextHandler.setInitParameter("javax.ws.rs.Application", ManagerApplication.class.getName());
		ServletHolder holder = new ServletHolder(HttpServletDispatcher.class);
		holder.setInitOrder(1);
		holder.setAsyncSupported(true);
		contextHandler.addServlet(holder, "/*");
		this.webServer.setHandler(contextHandler);
	}
	
	
	protected void pureWebAppWithoutWebxml(ClassLoader individualClassLoader) {
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.addEventListener(new ServletContextListener() {
			
			@Override
			public void contextInitialized(ServletContextEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void contextDestroyed(ServletContextEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	
	private void loadDubboApp(){
		container.loadApplication();
	}
	
	protected String getWebAppsPath() throws IOException {
		URL url = getClass().getClassLoader().getResource("webapps");
		if (url == null)
			throw new IOException("webapps not found in CLASSPATH");
		return url.toString();
	}

	public void start() throws Exception {
//		loadDubboApp();
		this.webServer.start();
	}

	public void stop() throws Exception {
		this.webServer.stop();
	}
	
	public static void main(String[] args) throws Exception {
		try {
			HttpServer server = new HttpServer(null);
			server.start();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}

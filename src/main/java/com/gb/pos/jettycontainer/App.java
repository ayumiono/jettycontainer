package com.gb.pos.jettycontainer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App {
	
	private static final Logger log = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) throws IOException {
		HttpServer server = InjectorSingleton.getInstance().getInjector(new AppModule()).getInstance(HttpServer.class);
		try {
			server.start();
		} catch (Exception e) {
			log.error("", e);
			System.exit(9999);
		}
		PidLockFileHandle pidLock = InjectorSingleton.getInstance().getInjector().getInstance(PidLockFileHandle.class);
		pidLock.writePid();
		pidLock.lock();
		System.out.println("jetty container setup done!");//方便监听容器是否启动完毕
//		System.out.flush();
//		System.out.close();
	}
}

package com.gb.pos.jettycontainer;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
	
	public static final String IS_FINISH = "jetty container setup done!";
	
	public static void main(String[] args) throws IOException {
		
		//在程序启动一开始，即加锁，防止连续启动多个报错
		PidLockFileHandle pidLock = InjectorSingleton.getInstance().getInjector(new AppModule()).getInstance(PidLockFileHandle.class);
		pidLock.writePid();
		pidLock.lock();
		
		try {
			HttpServer server = InjectorSingleton.getInstance().getInjector().getInstance(HttpServer.class);
			server.start();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(9999);
		}
		System.out.println(IS_FINISH);//方便监听容器是否启动完毕
	}
}

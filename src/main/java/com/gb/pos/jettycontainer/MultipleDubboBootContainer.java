package com.gb.pos.jettycontainer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleDubboBootContainer {

	private static final Logger log = LoggerFactory.getLogger("MultipleDubboBootContainer");

	private ConcurrentHashMap<String, AppRuntimeContext> runtimeContexts = new ConcurrentHashMap<>();
	
	private ConcurrentHashMap<String, ApplicationJarInfo> innerInfoHolder = new ConcurrentHashMap<>();
	
	public List<AppRuntimeContext> getRuntimeContexts() {
		return new ArrayList<>(runtimeContexts.values());
	}

	private String appLibsDir;
	
	public MultipleDubboBootContainer(String appLibsDir) {
		this.appLibsDir = appLibsDir;
		System.setProperties(new MultipleDubboBootProperties());
	}
	
	public void loadApplication() {
		long startTime = System.currentTimeMillis();
		log.info("applicaions dir path:"+appLibsDir);
		File dir = new File(appLibsDir);
		if(!dir.exists() || !dir.isDirectory()) {
			log.error("invalid applicatioins dir path");
		}
		List<ApplicationJarInfo> appWaitForLoad = new ArrayList<>();
		File[] applications = dir.listFiles();
		for(File application : applications) {
			File[] children = application.listFiles();
			File appLib = null;
			File dependencies = null;
			int priority = 0;
			String mainClass = null;
			String appName = null;
			for(File child : children) {
				if(child.isDirectory() && child.getName().equals("lib")) {
					dependencies = child;
				}else if(!child.isDirectory() && child.getName().endsWith("jar")) {
					appLib = child;
					try {
						JarFile jarFile = new JarFile(appLib);
						Manifest manifest = null;
						manifest = jarFile.getManifest();
						Attributes attributes = manifest.getMainAttributes();
						priority = attributes.getValue("PRIORITY") == null ? 0 : Integer.parseInt(attributes.getValue("PRIORITY"));
						mainClass = attributes.getValue("Main-Class");
						appName = attributes.getValue("APP-NAME") == null ? appLib.getName() : attributes.getValue("APP-NAME");
						appWaitForLoad.add(new ApplicationJarInfo(appLib, dependencies, priority, mainClass,appName));
						jarFile.close();
					} catch (Exception e) {
					}
				}
			}
		}
		
		Collections.sort(appWaitForLoad, new Comparator<ApplicationJarInfo>() {
			@Override
			public int compare(ApplicationJarInfo o1, ApplicationJarInfo o2) {
				return o2.priority - o1.priority;
			}
		});
		
		for(ApplicationJarInfo app : appWaitForLoad) {
			innerInfoHolder.put(app.appName, app);
			app.load();
		}
		log.info("applications setup finished, spent {} millsends",System.currentTimeMillis() - startTime);
	}
	
	class ApplicationJarInfo {
		File appLib;
		File dependencies;
		int priority;
		String mainClass;
		String appName;
		public ApplicationJarInfo(File appLib,File dependencies,int priority,String mainClass,String appName) {
			this.appLib = appLib;
			this.dependencies = dependencies;
			this.priority = priority;
			this.mainClass = mainClass;
			this.appName = appName;
		}
		
		public AppRuntimeContext load() {
			if(runtimeContexts.get(appName) != null && runtimeContexts.get(appName).getCode() == 0) {
				//already loaded, do nothing
				return MultipleDubboBootContainer.this.runtimeContexts.get(this.appName);
			}else {
				if(this.mainClass == null) {
					return startDubbo(this);
				}else {
					return startSpringBoot(this);
				}
			}
		}
	}

	private AppRuntimeContext startSpringBoot(ApplicationJarInfo app) {
		log.info("launch SpringBoot app:{}...",app.appName);
		long startTime = System.currentTimeMillis();
		final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			List<URL> dependencies = new ArrayList<>();
			dependencies.add(app.appLib.toURI().toURL());
			for(File dependency : app.dependencies.listFiles()) {
				dependencies.add(dependency.toURI().toURL());
			}
//			dependencies.add(this.getClass().getResource("./logback/JettyContainerLoggerContextSelector.class").toURI().toURL());
			final PluginLibClassLoader currentClassLoader = new PluginLibClassLoader(app.appName, dependencies.toArray(new URL[] {}),app.appLib.getParentFile().toURI().toURL(), oldClassLoader);
//			final PluginLibClassLoader currentClassLoader = new PluginLibClassLoader(app.appName, dependencies.toArray(new URL[] {}), oldClassLoader);
			Thread.currentThread().setContextClassLoader(currentClassLoader);
			Class<?> bootClass = currentClassLoader.loadClass(app.mainClass);
			System.setProperty("system.name", app.appName);
			System.setProperty("print.full.message","true");
			Class<?> _springApplicationBuilderClazz = currentClassLoader.loadClass("org.springframework.boot.builder.SpringApplicationBuilder");
			Method _source = _springApplicationBuilderClazz.getMethod("sources", Class[].class);
			Method _profiles = _springApplicationBuilderClazz.getMethod("profiles", String[].class);
			Method _run = _springApplicationBuilderClazz.getMethod("run", String[].class);
			Constructor<?> constructor = _springApplicationBuilderClazz.getConstructor(Object[].class);
			Object springApplicationBuilder = constructor.newInstance(new Object[] {new Object[] {}});
			_run.invoke(_profiles.invoke(_source.invoke(springApplicationBuilder, new Object[] {new Class[] {bootClass}}), new Object[] {new String[] {"offline"}}), new Object[] {new String[] {}});
			
//			Method mainEnter = bootClass.getDeclaredMethod("main", String[].class);
//			mainEnter.invoke(null, new Object[] {new String[] {}});
			AppRuntimeContext runtimeContext = new AppRuntimeContext();
			runtimeContext.setCode(0);
			runtimeContext.setAppPath(app.appLib.getAbsolutePath());
			runtimeContext.setArtifactId(app.appName);
			runtimeContext.setExceptionStackTrace(null);
			runtimeContext.setStartTime(System.currentTimeMillis());
			runtimeContext.setLaunchSpent(System.currentTimeMillis() - startTime);
			this.runtimeContexts.put(app.appName, runtimeContext);
			log.info("{} launch finish.",app.appName);
			return runtimeContext;
		} catch (Exception e) {
			log.error(app.appName+"launch failed!",e);
			AppRuntimeContext runtimeContext = new AppRuntimeContext();
			runtimeContext.setCode(9);
			runtimeContext.setAppPath(app.appLib.getAbsolutePath());
			runtimeContext.setArtifactId(app.appName);
			runtimeContext.setExceptionStackTrace(ExceptionUtils.getStackTrace(e));
			runtimeContext.setStartTime(0L);
			this.runtimeContexts.put(app.appName, runtimeContext);
			return runtimeContext;
		} finally {
			Thread.currentThread().setContextClassLoader(oldClassLoader);
		}
	}
	
	private AppRuntimeContext startDubbo(ApplicationJarInfo app) {
		log.info("launch Dubbo service:{}...",app.appName);
		long startTime = System.currentTimeMillis();
		final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			List<URL> dependencies = new ArrayList<>();
			dependencies.add(app.appLib.toURI().toURL());
			for(File dependency : app.dependencies.listFiles()) {
				dependencies.add(dependency.toURI().toURL());
			}
//			dependencies.add(this.getClass().getResource("./logback/JettyContainerLoggerContextSelector.class").toURI().toURL());
			log.debug("start init pluginlibclassloader");
			final PluginLibClassLoader currentClassLoader = new PluginLibClassLoader(app.appName, dependencies.toArray(new URL[] {}),app.appLib.getParentFile().toURI().toURL(), oldClassLoader);
//			final PluginLibClassLoader currentClassLoader = new PluginLibClassLoader(app.appName,dependencies.toArray(new URL[] {}));
			log.debug("start switch thread context class loader");
			Thread.currentThread().setContextClassLoader(currentClassLoader);
			System.setProperty("system.name", app.appName);
			System.setProperty("print.full.message","true");
			log.debug("start load _classPathXmlApplicationContextClazz");
			Class<?> _classPathXmlApplicationContextClazz = currentClassLoader.loadClass("org.springframework.context.support.ClassPathXmlApplicationContext");
			log.debug("start load classpathxmlapplicationcontext construcotr");
			Constructor<?> constructor = _classPathXmlApplicationContextClazz.getConstructor(String[].class,boolean.class);
			log.debug("start load classpathxmlapplicationcontext _refresh");
			Method _refresh = _classPathXmlApplicationContextClazz.getMethod("refresh");
			log.debug("start load classpathxmlapplicationcontext _start");
			Method _start = _classPathXmlApplicationContextClazz.getMethod("start");
			log.debug("start load classpathxmlapplicationcontext _getEnvironment");
			Method _getEnvironment = _classPathXmlApplicationContextClazz.getMethod("getEnvironment");
			log.debug("start load _configurableEnvironmentClazz _getEnvironment");
			Class<?> _configurableEnvironmentClazz = currentClassLoader.loadClass("org.springframework.core.env.ConfigurableEnvironment");
			log.debug("start load configurableEnvironmentClazz _setActiveProfiles");
			Method _setActiveProfiles = _configurableEnvironmentClazz.getMethod("setActiveProfiles", String[].class);
			log.debug("start init _classPathXmlApplicationContext Object");
			Object _classPathXmlApplicationContext = constructor.newInstance(new String[] {"applicationContext.xml"}, false);
			log.debug("start invoke _classPathXmlApplicationContext _getEnvironment method");
			Object environment = _configurableEnvironmentClazz.cast(_getEnvironment.invoke(_classPathXmlApplicationContext));
			log.debug("start set up offline profile");
			_setActiveProfiles.invoke(environment, new Object[] {new String[] {"offline"}});
			log.debug("start refresh spring application context");
			_refresh.invoke(_classPathXmlApplicationContext);
			log.debug("starting spring application context");
			_start.invoke(_classPathXmlApplicationContext);
			
//			Class<?> cachemain = currentClassLoader.loadClass("com.gb.soa.omp.ccache.CacheMain");
//			Method mainEnter = cachemain.getDeclaredMethod("main", String[].class);
//			mainEnter.invoke(null, new Object[] {new String[] {}});
			
			AppRuntimeContext runtimeContext = new AppRuntimeContext();
			runtimeContext.setCode(0);
			runtimeContext.setArtifactId(app.appName);
			runtimeContext.setAppPath(app.appLib.getAbsolutePath());
			runtimeContext.setExceptionStackTrace(null);
			runtimeContext.setStartTime(System.currentTimeMillis());
			runtimeContext.setLaunchSpent(System.currentTimeMillis() - startTime);
			this.runtimeContexts.put(app.appName, runtimeContext);
			log.info("{} launch finish.",app.appName);
			return runtimeContext;
		} catch (Exception e) {
			log.error(app.appName+" launch failed!",e);
			AppRuntimeContext runtimeContext = new AppRuntimeContext();
			runtimeContext.setCode(9);
			runtimeContext.setArtifactId(app.appName);
			runtimeContext.setAppPath(app.appLib.getAbsolutePath());
			runtimeContext.setExceptionStackTrace(ExceptionUtils.getStackTrace(e));
			runtimeContext.setStartTime(0L);
			this.runtimeContexts.put(app.appName, runtimeContext);
			return runtimeContext;
		} finally {
			Thread.currentThread().setContextClassLoader(oldClassLoader);
		}
	}
	
	public AppRuntimeContext retryLoadApp(String appName) {
		return this.innerInfoHolder.get(appName).load();
	}

//	public static class PluginLibClassLoader extends URLClassLoader {
//		
//		private String appName;
//		
//		private ClassLoader parent;
//		
//		public PluginLibClassLoader(String appName, URL[] urls, ClassLoader parent) {
//			super(urls,parent);
//			this.parent = parent;
//			this.appName = appName;
//		}
//		
//		public String appName() {
//			return this.appName;
//		}
//		
//		@Override
//	    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//	        Class<?> clazz = findLoadedClass(name);
//	        if (clazz == null) {
//	            if (onLoadClass(name)) {
//	                clazz = findClass(name);
//	            } else {
//	                try {
//	                    clazz = parent.loadClass(name);
//	                } catch (ClassNotFoundException ignore) {
//	                }
//	                if (clazz == null) {
//	                    clazz = findClass(name);
//	                }
//	            }
//	        }
//	        if (resolve) {
//	            resolveClass(clazz);
//	        }
//	        return clazz;
//	    }
//		
//		private static final String[] PINPOINT_PROFILER_CLASS = new String[] {//slf4j的包重新加载，不从父加载器加载
////				"org.slf4j","ch.qos.logback",
//				"org.apache.http"
//		};
//		
//	    boolean onLoadClass(String clazzName) {
//	    	final int length = PINPOINT_PROFILER_CLASS.length;
//	        for (int i = 0; i < length; i++) {
//	            if (clazzName.startsWith(PINPOINT_PROFILER_CLASS[i])) {
//	                return true;
//	            }
//	        }
//	        return false;
//	    }
//	}
	
	public static class PluginLibClassLoader extends URLClassLoader {
		
		final public static String GROOVY_AUTOCONFIG_FILE = "logback.groovy";
	    final public static String AUTOCONFIG_FILE = "logback.xml";
	    final public static String TEST_AUTOCONFIG_FILE = "logback-test.xml";
		
		private static final String[] PINPOINT_PROFILER_CLASS = new String[] {//slf4j的包重新加载，不从父加载器加载
	            "org.slf4j","ch.qos.logback","org.apache.http"
	    };
		
		private String appName;
		
		private URL rootURL;
		
		Map<String, URI> singleClasses = new HashMap<>();
		
		private final ClassLoader parent;
		
		public PluginLibClassLoader(String appName, URL[] urls, URL rootURL) {
			super(urls);
			this.appName = appName;
			this.parent = null;
			this.rootURL = rootURL;
			appendSingleClasses();
		}
		public PluginLibClassLoader(String appName,URL[] urls, URL rootURL,ClassLoader parent) {
			super(urls, parent);
			this.appName = appName;
			this.parent = parent;
			this.rootURL = rootURL;
			appendSingleClasses();
		}
		
		private void appendSingleClasses() {
			try {
				this.addSingleClass("com.gb.pos.jettycontainer.logback.JettyContainerLoggerContextSelector", this.getClass().getResource("logback/JettyContainerLoggerContextSelector.class").toURI());
				this.addSingleClass("com.gb.pos.jettycontainer.logback.Assert", this.getClass().getResource("logback/Assert.class").toURI());
				this.addSingleClass("com.gb.pos.jettycontainer.logback.LogbackConfigurator", this.getClass().getResource("logback/LogbackConfigurator.class").toURI());
				this.addSingleClass("com.gb.pos.jettycontainer.logback.ErrLogFilter", this.getClass().getResource("logback/ErrLogFilter.class").toURI());
				this.addSingleClass("com.gb.pos.jettycontainer.logback.InfoLogFilter", this.getClass().getResource("logback/InfoLogFilter.class").toURI());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void addSingleClass(String name, URI uri) {
			this.singleClasses.put(name, uri);
		}
		
		public String appName() {
			return this.appName;
		}
		
		@Override
	    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
	        Class<?> clazz = findLoadedClass(name);
	        if (clazz == null) {
	            if (onLoadClass(name)) {
	                clazz = findClass(name);
	            } else {
	                try {
	                    clazz = parent.loadClass(name);
	                } catch (ClassNotFoundException ignore) {
	                }
	                if (clazz == null) {
	                    clazz = findClass(name);
	                }
	            }
	        }
	        if (resolve) {
	            resolveClass(clazz);
	        }
	        return clazz;
	    }
		
	    boolean onLoadClass(String clazzName) {
	    	final int length = PINPOINT_PROFILER_CLASS.length;
	        for (int i = 0; i < length; i++) {
	            if (clazzName.startsWith(PINPOINT_PROFILER_CLASS[i])) {
	                return true;
	            }
	        }
	        if(singleClasses.containsKey(clazzName)) {
	        	return true;
	        }
	        return false;
	    }
	    
	    private FileSystem initFileSystem(URI uri) throws IOException {
	    	try {
	    		return FileSystems.getFileSystem(uri);
			} catch (Exception e) {
				Map<String, String> env = new HashMap<>();
	            env.put("create", "true");
	            return FileSystems.newFileSystem(uri, env);
			}
	    }
		
		@Override
		public Class<?> findClass(String name) throws ClassNotFoundException {
			if(singleClasses.containsKey(name)) {
				try {
					FileSystem zipfs = initFileSystem(singleClasses.get(name));
					byte[] cLassBytes = Files.readAllBytes(Paths.get(singleClasses.get(name)));
					Class<?> clazz = defineClass(name, cLassBytes, 0, cLassBytes.length);
					zipfs.close();
					return clazz;
				} catch (IOException e) {
					throw new ClassNotFoundException(name);
				}
			}else {
				return super.findClass(name);
			}
		}
		
		/* 
		 * 重写findResource，让logback以为jar包中没有logback.xml,logback.grovery等配置文件
		 * @see java.net.URLClassLoader#findResource(java.lang.String)
		 */
		@Override
		public URL findResource(final String name) {
			if(name.equals(AUTOCONFIG_FILE) || name.equals(GROOVY_AUTOCONFIG_FILE) || name.equals(TEST_AUTOCONFIG_FILE)) {
				return null;
			}
			if(name.equals("")) {
				return this.rootURL;
			}
			return super.findResource(name);
		}
	}
}

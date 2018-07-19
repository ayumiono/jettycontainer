package com.gb.pos.jettycontainer;

import java.io.File;
import java.util.List;

import com.gb.pos.jettycontainer.MultipleDubboBootContainer.PluginLibClassLoader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;

/**
 * 
 * 无视应用logback.xml的配置，定制logback输出
 * 
 * 添加-Dlogback.ContextSelector=com.gb.pos.jettycontainer.JettyContainerLoggerContextSelector 
 * -Droot.dir=xxxx\xxxx
 * jvm启动参数
 * 
 * @see org.slf4j.impl.StaticLoggerBinder slf4j中的LoggerFactory.getLogger
 * @see ch.qos.logback.classic.util.ContextSelectorStaticBinder
 * @see LogbackConfigurator
 * 
 * @author xuelong.chen
 *
 */
public class JettyContainerLoggerContextSelector implements ContextSelector {
	
	private String logsDirPath;
	
	private String artifactId;
	
	private LoggerContext defaultLoggerContext;
	
	private LoggerContext customContext;
	
	private static final String LAYOUT_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";;
	private static final String MAX_LOG_FILE_SIZE = "256MB";
	private static final String TOTAL_FILE_SIZE_CAP = "2GB";
	private static final int MAX_HISTORY = 2;
	
	public JettyContainerLoggerContextSelector(LoggerContext defaultLoggerContext) {//这里传入的是logback自动从配置文件中解析出的default LoggerContext
		Assert.notNull(System.getProperty("root.dir"), "请添加-Droot.dir 参数");
		this.logsDirPath = System.getProperty("root.dir") + File.separator + "logs";
		this.defaultLoggerContext = defaultLoggerContext;
		if(this.getClass().getClassLoader() instanceof PluginLibClassLoader) {
			this.artifactId = ((PluginLibClassLoader) this.getClass().getClassLoader()).appName();
		}else {
			this.artifactId = "jetty";
		}
		init();
	}
	
	private void init() {
		this.customContext = new LoggerContext();
		LogbackConfigurator configurator = new LogbackConfigurator(customContext);
		RollingFileAppender<ILoggingEvent> rollingFile = initRollingFileAppender();
		ConsoleAppender<ILoggingEvent> console = initConsoleAppender();
		configurator.appender("rollingFile", rollingFile);
		configurator.appender("console", console);
		configurator.root(Level.INFO, rollingFile,console);
	}
	
	/**
	 * 将应用错误日志通过netty实时通知服务器,考虑到是离线时才会运行此系统，这个功能暂时不做 TODO 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected Appender initNettyAppender() {
		return null;
	}
	
	/**
	 * 将应用错误日志输出到 logsDirPath/appName.err 文件下。暂时不用 TODO
	 * @return
	 */
	protected RollingFileAppender<ILoggingEvent> initErrorRollingFileAppender() {
		RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
		appender.setContext(customContext);
		appender.setName("");
		appender.setFile(logsDirPath + File.separator + artifactId + ".err");
		appender.setAppend(true);
		TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
		rollingPolicy.setContext(customContext);
		rollingPolicy.setFileNamePattern(logsDirPath + File.separator + artifactId + ".%d{yyyy-MM-dd}_%i" + ".err");
		rollingPolicy.setMaxHistory(MAX_HISTORY);
		rollingPolicy.setTotalSizeCap(FileSize.valueOf(TOTAL_FILE_SIZE_CAP));
		rollingPolicy.setCleanHistoryOnStart(true);
		rollingPolicy.setParent(appender);
		SizeAndTimeBasedFNATP<ILoggingEvent> timeBasedTriggering = new SizeAndTimeBasedFNATP<>();
		timeBasedTriggering.setMaxFileSize(FileSize.valueOf(MAX_LOG_FILE_SIZE));
		rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(timeBasedTriggering);
		rollingPolicy.start();
		timeBasedTriggering.start();
		appender.setRollingPolicy(rollingPolicy);
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setPattern(LAYOUT_PATTERN);
		encoder.setContext(customContext);
		encoder.start();
		appender.setEncoder(encoder);
		return appender;
	}
	
	private ConsoleAppender<ILoggingEvent> initConsoleAppender(){
		ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setPattern(LAYOUT_PATTERN);
		encoder.setContext(customContext);
		encoder.start();
		consoleAppender.setEncoder(encoder);
		consoleAppender.setContext(customContext);
		return consoleAppender;
	}
	
	/**
	 * 将应用日志输出到logsDirPath/appName.err 文件下
	 * @return
	 */
	private RollingFileAppender<ILoggingEvent> initRollingFileAppender() {
		RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
		appender.setContext(customContext);
		appender.setName("");
		appender.setFile(logsDirPath + File.separator + artifactId + ".log");
		appender.setAppend(true);
		TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
		rollingPolicy.setContext(customContext);
		rollingPolicy.setFileNamePattern(logsDirPath + File.separator + artifactId + ".%d{yyyy-MM-dd}_%i" + ".log");
		rollingPolicy.setMaxHistory(MAX_HISTORY);
		rollingPolicy.setTotalSizeCap(FileSize.valueOf(TOTAL_FILE_SIZE_CAP));
		rollingPolicy.setCleanHistoryOnStart(true);
		rollingPolicy.setParent(appender);
		SizeAndTimeBasedFNATP<ILoggingEvent> timeBasedTriggering = new SizeAndTimeBasedFNATP<>();
		timeBasedTriggering.setMaxFileSize(FileSize.valueOf(MAX_LOG_FILE_SIZE));
		rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(timeBasedTriggering);
		rollingPolicy.start();
		timeBasedTriggering.start();
		appender.setRollingPolicy(rollingPolicy);
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setPattern(LAYOUT_PATTERN);
		encoder.setContext(customContext);
		encoder.start();
		appender.setEncoder(encoder);
		return appender;
	}

	@Override
	public LoggerContext getLoggerContext() {
		return this.customContext;
	}

	@Override
	public LoggerContext getLoggerContext(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerContext getDefaultLoggerContext() {
		return this.defaultLoggerContext;
	}

	@Override
	public LoggerContext detachLoggerContext(String loggerContextName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getContextNames() {
		return null;
	}

}

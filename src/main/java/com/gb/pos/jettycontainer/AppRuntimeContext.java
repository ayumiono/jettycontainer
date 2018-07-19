package com.gb.pos.jettycontainer;

public class AppRuntimeContext {
	private int code;
	private String exceptionStackTrace;
	private long startTime;
	private String artifactId;
	private long launchSpent;
	private String appPath;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getExceptionStackTrace() {
		return exceptionStackTrace;
	}
	public void setExceptionStackTrace(String exceptionStackTrace) {
		this.exceptionStackTrace = exceptionStackTrace;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public String getArtifactId() {
		return artifactId;
	}
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	public long getLaunchSpent() {
		return launchSpent;
	}
	public void setLaunchSpent(long launchSpent) {
		this.launchSpent = launchSpent;
	}
	public String getAppPath() {
		return appPath;
	}
	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}
}

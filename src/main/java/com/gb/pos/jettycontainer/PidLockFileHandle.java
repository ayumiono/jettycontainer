package com.gb.pos.jettycontainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PidLockFileHandle {
	
	@Inject
	@Named("root.dir")
	private String rootDir;
	
	protected FileLock lock;
	
	
	public void writePid() throws IOException {
		File _pid = new File(rootDir,"pid");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_pid), "UTF-8"));
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.split("@")[0];
		bw.write(pid);
		bw.flush();
		bw.close();
	}
	
	public void lock() throws IOException {
		File lock = new File(rootDir,".lock");
		FileChannel channel = FileChannel.open(lock.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
		this.lock = channel.lock();
//		this.lock = channel.tryLock();
	}
}

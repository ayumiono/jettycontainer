package com.gb.pos.jettycontainer.netty;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * 将应用的ERROR级别的日志通过netty发送到服务端
 * @author xuelong.chen
 *
 */
public class NettyAppender extends AppenderBase<ILoggingEvent>{

	@Override
	protected void append(ILoggingEvent eventObject) {
		if(eventObject.getLevel() != Level.ERROR) {
			//send error level log to server only
		} else {
			//TODO
		}
	}

}

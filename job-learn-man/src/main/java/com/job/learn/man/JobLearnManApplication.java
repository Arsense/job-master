package com.job.learn.man;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class JobLearnManApplication {
	private final static Logger LOG = LoggerFactory.getLogger(JobLearnManApplication.class);

	public static void main(String[] args) {

		ApplicationContext context = SpringApplication.run(JobLearnManApplication.class,args);
		String serverPort = context.getEnvironment().getProperty("server.port");
		//输入访问链接
		LOG.info("Clay started at http://localhost:" + serverPort);
	}

}


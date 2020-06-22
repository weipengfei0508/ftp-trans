package com.ztxy.dwps.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ztxy.dwps.file.service.FtpService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScheduledTask {

	
	@Value("${ftp.remote.dir}")
	private String remoteDir;
	
	@Value("${ftp.localdir.dir}")
	private String localDir;
	
	

	
	@Autowired
	private FtpService ftpService;
	
	@Scheduled(fixedRateString = "${ftp.scheduled.rate}")
	public void getTask1() {
		ftpService.downloadFiles(remoteDir, localDir);
	}
	
}

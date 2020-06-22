package com.ztxy.dwps.file.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("FileGenService")
@Slf4j
public class FileGenService {

	@Autowired
	private FtpService ftpService;

	public void downFiles() {

	}

}

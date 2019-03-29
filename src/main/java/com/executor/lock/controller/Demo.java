package com.executor.lock.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.executor.lock.annotation.DistributedLockAnno;
import com.executor.lock.annotation.LockKeyParam;

@RestController
public class Demo {

	@DistributedLockAnno(prefix="aaa",expire=100,needSureOwn=true)
	@RequestMapping("aaa")
	public String aa(@LockKeyParam String id) {
		return "a";
	}
}

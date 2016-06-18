package com.bj58.emc.study.curator.demo.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateZKDir {
	
	static Logger LOG = LoggerFactory.getLogger(CreateZKDir.class);
	
	public static void main(String[] args) throws Exception {
		
		String connectionStr = "10.9.19.123:2181,10.9.19.124:2181,10.9.19.125:2181";
		CuratorFramework c = CuratorFrameworkFactory.newClient(connectionStr, 120000, 120000,new RetryNTimes(5, 1000));
		c.start();
		String fullPath = "/zk/test" ;
		System.out.println(c.create().creatingParentsIfNeeded().forPath(fullPath));
		
		
    }

}

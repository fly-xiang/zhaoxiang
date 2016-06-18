package com.bj58.emc.study.curator.demo.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

public class ClientFactory {

	public static CuratorFramework newClient() {
		String connectionString = "10.9.19.123:2181,10.9.19.124:2181,10.9.19.125:2181";
//		ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 5);
		return CuratorFrameworkFactory.newClient(connectionString, 120000, 120000,new RetryNTimes(5, 1000));
	}
}

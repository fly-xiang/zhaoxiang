package com.bj58.emc.study.curator.demo.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**删除ZK下面的kafka目录  用于系统长时间中断 导致kafka堆积大量数据  无法正常消耗完从而使spark项目无法启动
 * @author huangliang
 *
 */
public class DeleteZKDir {
	
	public static int slideInterval = 10000;
	
	static Logger LOG = LoggerFactory.getLogger(DeleteZKDir.class);
	
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws Exception {
		
		String connectionStr = "10.9.19.123:2181,10.9.19.124:2181,10.9.19.125:2181";
		CuratorFramework c = CuratorFrameworkFactory.newClient(connectionStr, 120000, 120000,new RetryNTimes(5, 1000));
		c.start();
		System.out.println(c.delete().deletingChildrenIfNeeded().forPath("/zk/test"));
		
		
    }

}

package com.bj58.emc.study.curator.demo.watch;

import java.nio.charset.Charset;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKClassLoaderWatcher implements CuratorWatcher{
	private CuratorFramework zkTools;
	private static Logger log = LoggerFactory.getLogger(ZKClassLoaderWatcher.class);
	private String path;
	private String className;
	private volatile Class<?> loadClass = null;
	public ZKClassLoaderWatcher(CuratorFramework zkTools,String path,String className){
		this.zkTools = zkTools;
		this.path = path;
		this.className = className;
	}
	public void start(){
		try {
			refreshClass();
		} catch (Exception e) {
			log.error("",e);
			System.exit(1);
		}
	}
	public Class<?> getLoadClass() {
		return loadClass;
	}

	@Override
	public void process(WatchedEvent event) {
		System.out.println(event.getType());
		if(event.getType() == EventType.NodeDataChanged){
			try {
				refreshClass();
			} catch (Exception e) {
				log.error("refreshClass error ",e);
			}
		}
	}
	   
	public void refreshClass () throws Exception{
		 //watcher是一次性的，必须每次添加.usingWatcher(this)，否则下次变化不会再通知了
		byte[] data = zkTools.getData().usingWatcher(this).forPath(path);
		if(data != null){
			ZkClassLoader.putClassCodeToCache(className, data);
		}
		loadClass = new ZkClassLoader().loadClass(className);
		System.out.println(path+":"+new String(data,Charset.forName("utf-8")));
//		MrKafkaTrack.setToolClass(loadClass);
	}
	
	public static ZKClassLoaderWatcher build(){
		String zkAddress = "10.9.19.123:2181,10.9.19.124:2181,10.9.19.125:2181";
		CuratorFramework zkTools = CuratorFrameworkFactory.builder()
			.connectString(zkAddress)
			.sessionTimeoutMs(120000)
			.connectionTimeoutMs(120000)
			.retryPolicy(new RetryNTimes(5,20000))	//.namespace("zk/test")
			.build();
		zkTools.start();
		String classPath = "/zk/test";
		String className = "test.Sample";
		ZKClassLoaderWatcher zkClassWatcher= new ZKClassLoaderWatcher(zkTools, classPath, className);
		return zkClassWatcher;
	}
	/**
	 * @param args
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws Exception {
		String zkAddress = "10.9.19.123:2181,10.9.19.124:2181,10.9.19.125:2181";
		CuratorFramework zkTools = CuratorFrameworkFactory.builder()
			.connectString(zkAddress)
			.sessionTimeoutMs(120000)
			.connectionTimeoutMs(120000)
			.retryPolicy(new RetryNTimes(5,20000))	//.namespace("zk/test")
			.build();
		zkTools.start();
		String classPath = "/zk/test";
		String className = "test.Sample";
		ZKClassLoaderWatcher zkClassWatcher= new ZKClassLoaderWatcher(zkTools, classPath, className);
//		Thread.sleep(30000);
		Object loadedObject = zkClassWatcher.getLoadClass().newInstance();
		System.out.println(loadedObject);
	}

}

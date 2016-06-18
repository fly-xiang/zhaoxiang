package com.bj58.emc.study.curator.demo.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

public class ZKBaseUtils {
	public static String zkAddress = "10.9.19.123:2181,10.9.19.124:2181,10.9.19.125:2181";
	private static  CuratorFramework curator ;
	public static synchronized void init() {
		if(curator == null){
			curator = CuratorFrameworkFactory.newClient(zkAddress, 120000, 120000,new RetryNTimes(5, 1000));
		}
		if(!(curator.getState() == CuratorFrameworkState.STARTED)){
			curator.start();
		}
	}
	
	public static synchronized void destory() {
		if(curator == null || curator.getState() == CuratorFrameworkState.STOPPED){
			return;
		}
		curator.close();
	}
	
	/* 创建 */
	public static void  createZKDir(String fullPath, byte[] data){
		try {
			curator.create().creatingParentsIfNeeded().forPath(fullPath,data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* 删除 */
	public static void  delZKDir(String fullPath){
		try {
			curator.delete().deletingChildrenIfNeeded().forPath(fullPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void createEphemeral(CuratorFramework client, String path, byte[] payload) throws Exception {
        // this will create the given EPHEMERAL ZNode with the given data
        client.create().withMode(CreateMode.EPHEMERAL).forPath(path, payload);
    }

    public static String createEphemeralSequential(CuratorFramework client, String path, byte[] payload) throws Exception {
        // this will create the given EPHEMERAL-SEQUENTIAL ZNode with the given
        // data using Curator protection.
        return client.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
    }

    public static void setData(CuratorFramework client, String path, byte[] payload) throws Exception {
        // set data for the given node
        client.setData().forPath(path, payload);
    }

    public static void setDataAsync(CuratorFramework client, String path, byte[] payload) throws Exception {
        // this is one method of getting event/async notifications
        CuratorListener listener = new CuratorListener() {
            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                // examine event for details
            }
        };
        client.getCuratorListenable().addListener(listener);
        // set data for the given node asynchronously. The completion
        // notification
        // is done via the CuratorListener.
        client.setData().inBackground().forPath(path, payload);
    }

    public static void setDataAsyncWithCallback(CuratorFramework client, BackgroundCallback callback, String path, byte[] payload) throws Exception {
        // this is another method of getting notification of an async completion
        client.setData().inBackground(callback).forPath(path, payload);
    }

    public static void guaranteedDelete(CuratorFramework client, String path) throws Exception {
        // delete the given node and guarantee that it completes
        client.delete().guaranteed().forPath(path);
    }

    public static List<String> watchedGetChildren(CuratorFramework client, String path) throws Exception {
        /**
         * Get children and set a watcher on the node. The watcher notification
         * will come through the CuratorListener (see setDataAsync() above).
         */
        return client.getChildren().watched().forPath(path);
    }

    public static List<String> watchedGetChildren(CuratorFramework client, String path, Watcher watcher) throws Exception {
        /**
         * Get children and set the given watcher on the node.
         */
        return client.getChildren().usingWatcher(watcher).forPath(path);
    }
	
	public static byte[] getLocalFileData(String localPath) {
		String path = localPath;
		try {
			InputStream ins = new FileInputStream(path);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int bufferSize = 4096;
			byte[] buffer = new byte[bufferSize];
			int bytesNumRead = 0;
			while ((bytesNumRead = ins.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesNumRead);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public static void main(String[] args) throws Exception {
		init();
		String fullPath = "/counter";
//		createZKDir(fullPath,"test Data".getBytes());
//		byte[] classData = getLocalFileData("E:\\tmp\\classloader\\test\\Sample.class");
//		setData(curator, fullPath, "drdftfgy Data1".getBytes());
		delZKDir(fullPath);
		destory();
	}

}

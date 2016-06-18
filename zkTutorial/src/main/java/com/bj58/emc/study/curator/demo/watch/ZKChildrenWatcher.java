package com.bj58.emc.study.curator.demo.watch;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

public class ZKChildrenWatcher  implements CuratorWatcher{
	   private final String path;
	   private CuratorFramework zkTools;
	   public String getPath() {
	       return path;
	   }
	   public ZKChildrenWatcher(CuratorFramework zkTools,String path) {
		   this.zkTools = zkTools;
	       this.path = path;
	   }
	   
	   @Override
	   public void process(WatchedEvent event) throws Exception {
	       System.out.println(event.getType());
	       List<String> childrenList = null;
	       if(zkTools.checkExists().forPath(path) != null){
	    	  childrenList = zkTools.getChildren().usingWatcher(this).forPath(path);
//	    	  System.out.println(childrenList);
	       }
	       
	       if(event.getType() == EventType.NodeDataChanged || event.getType() == EventType.NodeCreated){
	    	   //watcher是一次性的，必须每次添加.usingWatcher(this)，否则下次变化不会再通知了
	          byte[] data = zkTools.getData().usingWatcher(this).forPath(path);
	          System.out.println(path+":"+new String(data,Charset.forName("utf-8")));
	       }else if(event.getType() == EventType.NodeDeleted){
	          //节点被删除了，需要创建新的节点
	          Stat stat = zkTools.checkExists().usingWatcher(this).forPath(path);
	          if(stat == null){
	        	  System.out.println(path + ":" + path +" has been deleted.");
	              zkTools.create()
	              .creatingParentsIfNeeded()
	              .withMode(CreateMode.EPHEMERAL)
	              .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
	              .forPath(path);
	          }
	       }else if(event.getType() == EventType.NodeChildrenChanged){
	          //节点被创建时，需要添加监听事件（创建可能是由于session过期后，curator的状态监听部分触发的）
		       System.out.println(path+" ChildrenChanged :"+childrenList);
	       }
		   zkTools.checkExists().usingWatcher(this).forPath(path);
	   }
	  
	}
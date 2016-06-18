package com.bj58.emc.study.curator.demo.watch;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;


public class ZKWatch implements CuratorWatcher{
	//监听某一类事件
   private final WatcherType watcherType;
   private final String path;
   private CuratorFramework zkTools;
   public String getPath() {
       return path;
   }
   public ZKWatch(WatcherType watcherType, CuratorFramework zkTools,String path) {
	   this.zkTools = zkTools;
	   this.watcherType = watcherType;
       this.path = path;
   }
   
   @Override
   public void process(WatchedEvent event) throws Exception {
       System.out.println(watcherType+" watcher recieved event :"+event.getType());
       if(event.getType() == EventType.NodeDataChanged && watcherType == WatcherType.getData){
    	   //watcher是一次性的，必须每次添加.usingWatcher(this)，否则下次变化不会再通知了
          byte[] data = zkTools.getData().usingWatcher(this).forPath(path);
          System.out.println(path+":"+new String(data,Charset.forName("utf-8")));
       }else if(event.getType() == EventType.NodeDeleted && watcherType == WatcherType.checkExists){
          //节点被删除了，需要创建新的节点
          System.out.println(path + ":" + path +" has been deleted.");
          Stat stat = zkTools.checkExists().usingWatcher(this).forPath(path);
//          if(stat == null){
//              zkTools.create()
//              .creatingParentsIfNeeded()
//              .withMode(CreateMode.EPHEMERAL)
//              .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
//              .forPath(path);
//          }
       }else if(event.getType() == EventType.NodeCreated && watcherType == WatcherType.checkExists){
          //节点被创建时，需要添加监听事件（创建可能是由于session过期后，curator的状态监听部分触发的）
    	   byte[] value = zkTools.getData().forPath(path);
    	   System.out.println(path + ":" +" has been created!" + "the current data is " + new String(value));
//    	   zkTools.getData().usingWatcher(this).forPath(path);
	  	    Stat stat = zkTools.checkExists().usingWatcher(this).forPath(path);
	  	    System.out.println(stat);
       }else if(event.getType() == EventType.NodeChildrenChanged && watcherType == WatcherType.getChildren){
	          //节点被创建时，需要添加监听事件（创建可能是由于session过期后，curator的状态监听部分触发的）
    	   List<String> childrenList = null;
    	   if(zkTools.checkExists().forPath(path) != null){
    		   childrenList = zkTools.getChildren().usingWatcher(this).forPath(path);
    	   }
    	   System.out.println(path+" ChildrenChanged :"+childrenList);
       }
   }
  
  public static  enum WatcherType{
		getData,checkExists,getChildren;
   }
}


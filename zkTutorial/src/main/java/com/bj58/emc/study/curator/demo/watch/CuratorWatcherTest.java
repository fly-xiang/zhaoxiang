package com.bj58.emc.study.curator.demo.watch;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import com.bj58.emc.study.curator.demo.watch.ZKWatch.WatcherType;

public class CuratorWatcherTest {
	    private CuratorFramework zkTools;
	    @SuppressWarnings("rawtypes")
		private ConcurrentSkipListSet watchers = new ConcurrentSkipListSet();
	    public static final Charset charset = Charset.forName("utf-8");
	   
	    public enum ZookeeperWatcherType {
	    	EXITS,GET_CHILDREN,GET_DATA,CREATE_ON_NO_EXITS;
	    }   
	    
	    public CuratorWatcherTest() {     
	       zkTools = CuratorFrameworkFactory
	              .builder()
	              .connectString("10.9.19.123:2181,10.9.19.124:2181,10.9.19.125:2181")
//	              .namespace("zk/test")
	              .retryPolicy(new RetryNTimes(2000,20000))
	              .build();
	       zkTools.start();
	      
	        }  
	   
	   
	    @SuppressWarnings("unchecked")
		public void addReconnectionWatcher(final String path,final ZookeeperWatcherType watcherType,final CuratorWatcher watcher){
	       synchronized (this) {
	           if(!watchers.contains(watcher.toString()))//不要添加重复的监听事件
	           {
	              watchers.add(watcher.toString());
	              System.out.println("add new watcher " + watcher);
	              zkTools.getConnectionStateListenable().addListener(new ConnectionStateListener() {  
	                  @Override
	                  public void stateChanged(CuratorFramework client, ConnectionState newState) {
	                     System.out.println(newState);
	                     if(newState == ConnectionState.LOST){//处理session过期
	                         try{
	                            if(watcherType == ZookeeperWatcherType.EXITS){
	                                zkTools.checkExists().usingWatcher(watcher).forPath(path);
	                            }else if(watcherType == ZookeeperWatcherType.GET_CHILDREN){
	                                zkTools.getChildren().usingWatcher(watcher).forPath(path);
	                            }else if(watcherType == ZookeeperWatcherType.GET_DATA){
	                                zkTools.getData().usingWatcher(watcher).forPath(path);
	                            }else if(watcherType == ZookeeperWatcherType.CREATE_ON_NO_EXITS){
	                                //ephemeral类型的节点session过期了，需要重新创建节点，并且注册监听事件，之后监听事件中，
	                                //会处理create事件，将路径值恢复到先前状态
	                                Stat stat = zkTools.checkExists().usingWatcher(watcher).forPath(path);                             
	                                if(stat == null){
	                                   System.err.println("to create");
	                                   zkTools.create()
	                                   .creatingParentsIfNeeded()
	                                   .withMode(CreateMode.EPHEMERAL)
	                                   .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
	                                   .forPath(path);                                     
	                                }
	                            }
	                         }catch (Exception e) {
	                            e.printStackTrace();
	                         }
	                     }
	                  }
	              });          
	           }
	       }
	    }
	 
	    public void create() throws Exception{
	       zkTools.create()//创建一个路径
	       .creatingParentsIfNeeded()//如果指定的节点的父节点不存在，递归创建父节点
	       .withMode(CreateMode.PERSISTENT)//存储类型（临时的还是持久的）
	       .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)//访问权限
	       .forPath("zk/test");//创建的路径
	    }
	   
	    public void put() throws Exception{
	       zkTools.//对路径节点赋值
	       setData().
	       forPath("zk/test","hello world".getBytes(Charset.forName("utf-8")));
	    }
	   
	    public void get() throws Exception{
	       String path = "/zk/test";
	       ZKWatch watch = new ZKWatch(WatcherType.getData, zkTools,path);
	       byte[] buffer = zkTools.getData().usingWatcher(watch).forPath(path);
	       System.out.println(new String(buffer,charset));
	       //添加session过期的监控
//	       addReconnectionWatcher(path, ZookeeperWatcherType.GET_DATA, watch);
	    }  
	    
	    public void checkExists() throws Exception{
	    	String path = "/zk/test";
	    	ZKWatch watch = new ZKWatch(WatcherType.checkExists, zkTools,path);
	    	Stat stat = zkTools.checkExists().usingWatcher(watch).forPath(path);
	    	System.out.println(stat);
	    }
	    
	    public void getChildren() throws Exception{
	    	String path = "/zk/test";
	    	ZKWatch watch = new ZKWatch(WatcherType.getChildren,zkTools,path);
	    	List<String> childrenList = zkTools.getChildren().usingWatcher(watch).forPath(path);
	    	System.out.println(childrenList);
	    }
	   
	   
	    public void register() throws Exception{
	      
	       String ip = InetAddress.getLocalHost().getHostAddress();
	       String registeNode = "/zk/register/"+ip;//节点路径
	      
	       byte[] data = "disable".getBytes(charset);//节点值
	 
	       CuratorWatcher watcher = new ZKWatchRegister(zkTools,registeNode,data);    //创建一个register watcher
	      
	       Stat stat = zkTools.checkExists().forPath(registeNode);
	       if(stat != null){
	           zkTools.delete().forPath(registeNode);
	       }
	       zkTools.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
	       .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
	       .forPath(registeNode,data);//创建的路径和值
	      
	       //添加到session过期监控事件中
	       addReconnectionWatcher(registeNode, ZookeeperWatcherType.CREATE_ON_NO_EXITS,watcher);               
	       data = zkTools.getData().usingWatcher(watcher).forPath(registeNode);
	       System.out.println("get path form zk : "+registeNode+":"+new String(data,charset));
	    }
	   
	    public static void main(String[] args) throws Exception {
	    	CuratorWatcherTest test = new CuratorWatcherTest();
	       test.get();
	       test.getChildren();
	       test.checkExists();
//	       test.get();
//	       test.register();
	       Thread.sleep(10000000000L);
	 
	    }
	
	   
	   
	 

	
}

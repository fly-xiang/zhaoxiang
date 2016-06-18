package com.bj58.emc.study.curator.demo.watch;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.EnsurePath;

/**
 * Cache对ZooKeeper事件监听进行了封装，能够自动处理反复注册监听
 * 使用Watcher需要每次添加watch供下次使用getData().usingWatcher(watch).forPath(path)
 * @author zhaoxiang
 */
public class NodeCacheWatcher {

	  public static PathChildrenCache pathChildrenCache(CuratorFramework client, String path, Boolean cacheData) throws Exception {
	    final PathChildrenCache cached = new PathChildrenCache(client, path, cacheData);
	    cached.getListenable().addListener(new PathChildrenCacheListener() { 
	      @Override
	      public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
	        PathChildrenCacheEvent.Type eventType = event.getType();
	        switch (eventType) {
	          case CONNECTION_RECONNECTED:
	            cached.rebuild();
	            break;
	          case CONNECTION_SUSPENDED:
	          case CONNECTION_LOST:
	            System.out.println("Connection error,waiting...");
	            break;
	          default:
	            System.out.println("PathChildrenCache changed : {path:" + event.getData().getPath() + " data:" +
	                new String(event.getData().getData()) + "}");
	        }
	      }
	    });
	    return cached;
	  }


	  public static NodeCache nodeCache(CuratorFramework client, final String path) {
	    final NodeCache cache = new NodeCache(client, path);
	    cache.getListenable().addListener(new NodeCacheListener() {
	      @Override
	      public void nodeChanged() {
	    	  try{
	    		 ChildData data =  cache.getCurrentData();
	    		  if(data !=null){
	    			  System.out.println("NodeCache changed, data is: " + new String(cache.getCurrentData().getData()));
	    		  }else{
	    			  System.out.println(path + " has been deleted!");
	    		  }
	    	  }catch (Exception e) {
	    		  e.printStackTrace();
	    	  }
	      }
	    });

	    return cache;
	  }


	  public static void main(String[] args) throws Exception {
	    ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

	    CuratorFramework client = CuratorFrameworkFactory.newClient("10.9.19.123:2181,10.9.19.124:2181,10.9.19.125:2181", retryPolicy);
	    client.start();

	    EnsurePath ensurePath = client.newNamespaceAwareEnsurePath("/zk/test");
	    ensurePath.ensure(client.getZookeeperClient());

	    /**
	     * pathChildrenCache
	     */
	    PathChildrenCache cache = pathChildrenCache(client, "/zk/test", true);
	    cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
	    List<ChildData> datas = cache.getCurrentData();

	    for (ChildData data : datas) {
	      System.out.println("pathcache:{" + data.getPath() + ":" + new String(data.getData())+"}");
	    }


	    /**
	     *	NodeCache
	     */
	    NodeCache nodeCache = nodeCache(client, "/zk/test");
	    nodeCache.start(true);

//	    client.setData().forPath("/zk/test", "1111".getBytes());
//	    System.out.println(new String(nodeCache.getCurrentData().getData()));

	    Thread.sleep(10 * 60 * 1000);
	    CloseableUtils.closeQuietly(cache);
	    CloseableUtils.closeQuietly(client);
	  }
}

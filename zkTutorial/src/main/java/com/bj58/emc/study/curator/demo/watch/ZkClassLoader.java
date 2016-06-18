package com.bj58.emc.study.curator.demo.watch;

import java.util.concurrent.ConcurrentHashMap;

public class ZkClassLoader extends ClassLoader{
	private static ConcurrentHashMap<String, byte[]> classCodeCash = new ConcurrentHashMap<String, byte[]>();
	
    public ZkClassLoader() {
		super();
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException { 
        byte[] classData = getClassData(name);
        if (classData == null) {
            throw new ClassNotFoundException();
        }else {
            return defineClass(name, classData, 0, classData.length);
        }
    }

    private byte[] getClassData(String className) {
    	return classCodeCash.get(className);
    } 
    
    public static void putClassCodeToCache(String className, byte[] code){
    	classCodeCash.put(className, code);
    }
    
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException {
//		String classDataRootPath = "E:\\tmp\\classloader";
        ClassLoader loader1 =new ZkClassLoader();
        ClassLoader loader2 =new ZkClassLoader();
        
		Class<?> c1 = loader1.loadClass("test.Sample");
        Class<?> c2 = loader2.loadClass("test.Sample");
        System.out.println(c1 == c2);
        while (loader1 != null) {
        	System.out.println(loader1.toString());
        	loader1 = loader1.getParent();
        }
	}

}

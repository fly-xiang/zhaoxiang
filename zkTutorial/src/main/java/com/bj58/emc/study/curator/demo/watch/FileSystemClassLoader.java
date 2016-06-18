package com.bj58.emc.study.curator.demo.watch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileSystemClassLoader extends ClassLoader {

	private String rootDir;

	public FileSystemClassLoader(String rootDir) {
		this.rootDir = rootDir;
	}
	
	/**
	 * 不应该重写此方法，重写会出现很多问题，
	 * 比如第二次调用此方法加载相同的类时可能会出现 duplicate class definition for name:错误
	 * 因为同一个类加载器加载两次同样的类
	 * 为了防止loadClass返回系统类加载器加载的类不要 把要加载的类放在当前项目中，
	 * 会自动调用findClass，可以不覆盖此方法。
	 */
	@Override
	public java.lang.Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
        		File path = new File(classNameToPath(name));
	       		 /*
	       		  * 这部分一定要写，因为当加载的类依赖当前classpath中的其他类如java.lang.Object时，
	       		  * 也会调用该loadClass方法去加载依赖的类，此时应委托给系统类加载器。
	       		  */
	       		 if(!path.exists()){
	                    return super.loadClass(name);
	       		 }
	       		 return findClass(name);
            }
            return c;
        }
        
	};
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] classData = getClassData(name);
		if (classData == null) {
			throw new ClassNotFoundException();
		}
		else {
			return defineClass(name, classData, 0, classData.length);
		}
	}

	private byte[] getClassData(String className) {
		String path = classNameToPath(className);
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

	private String classNameToPath(String className) {
		return rootDir + File.separatorChar
				+ className.replace('.', File.separatorChar) + ".class";
	}
}

package net.paintless;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import com.lapissea.util.LogUtil;

import net.paintless.load.LoadRequest;

public class Paintless{
	
	public static final Paintless INSTANCE=new Paintless();
	
	public static long start=System.currentTimeMillis();
	
	public static final ForkJoinPool LOADING_POOL=new ForkJoinPool(1, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
	
	public static void main(String[] args){
		LogUtil.__.INJECT_DEBUG_PRINT(true);
//		LogUtil.__.INJECT_FILE_LOG(file);
//		System.setProperty("sun.java2d.opengl", "true");
//		System.setProperty("sun.java2d.noddraw", "true");
//		System.setProperty("awt.useSystemAAFontSettings", "on");
		INSTANCE.start();
	}
	
	private static final List<LoadRequest>	loadQ	=new ArrayList<>();
	private static final Object				LOCK	=new Object();
	
	private static void run(){
		try{
			while(true){
				synchronized(LOCK){
					if(loadQ.isEmpty()){
						LOCK.wait(5000);
						continue;
					}
					load(loadQ.remove(loadQ.size()-1));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void load(LoadRequest request){
		try{//start
			Class<?> c=Class.forName(request.classPath);
			Method start=c.getDeclaredMethod("start");
			start.setAccessible(true);
			start.invoke(null);
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}catch(Exception e){}
	}
	
	public static void request(LoadRequest request){
		synchronized(LOCK){
			loadQ.add(request);
			loadQ.sort(null);
			try{
				LOCK.notifyAll();
			}catch(Exception e){}
		}
	}
	
	public void start(){
		request(new LoadRequest("gui.MainWindow", Integer.MAX_VALUE, false));
		request(new LoadRequest("editing.OpenCL", 0, true));
		run();
	}
}

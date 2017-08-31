package net.paintless.load;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LoadManager{
	
	private final List<LoadRequest> loadQ=new ArrayList<>();
	
	public void start(){
		try{
			while(true){
				synchronized(LoadManager.this){
					if(loadQ.isEmpty()){
						wait(5000);
						continue;
					}
					load(loadQ.remove(loadQ.size()-1));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void load(LoadRequest request){
		try{//start
			Class<?> c=Class.forName(request.classPath);
			Method start=c.getDeclaredMethod("start");
			start.setAccessible(true);
			start.invoke(null);
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}catch(IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e){}
	}
	
	public synchronized void request(LoadRequest request){
		loadQ.add(request);
		loadQ.sort(null);
		try{
			notifyAll();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}

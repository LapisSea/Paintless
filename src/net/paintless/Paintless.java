package net.paintless;

import com.lapissea.util.LogUtil;

import net.paintless.load.LoadManager;
import net.paintless.load.LoadRequest;

public class Paintless{
	
	public static final Paintless INSTANCE=new Paintless();
	public static long start=System.currentTimeMillis();
	
	public static void main(String[] args){
		LogUtil.__.INJECT_DEBUG_PRINT(true);
		LogUtil.__.INJECT_FILE_LOG("log.txt");
		System.setProperty("sun.java2d.opengl", "false");
//		System.setProperty("sun.java2d.noddraw", "true");
		System.setProperty("awt.useSystemAAFontSettings", "on");
		INSTANCE.start();
	}
	
	public final LoadManager loadManager=new LoadManager();
	
	public void start(){
		loadManager.request(new LoadRequest("gui.MainWindow", Integer.MAX_VALUE, false));
		loadManager.request(new LoadRequest("editing.OpenCL", 0, true));
		loadManager.start();
	}
}

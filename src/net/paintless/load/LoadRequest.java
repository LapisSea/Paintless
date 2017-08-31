package net.paintless.load;

public class LoadRequest implements Comparable<LoadRequest>{
	
	public final boolean	requiresOpenCL;
	public final String		classPath;
	public final int		priority;
	
	public LoadRequest(String classPath, int priority, boolean requiresOpenCL){
		this.requiresOpenCL=requiresOpenCL;
		this.classPath="net.paintless.program."+classPath;
		this.priority=priority;
	}
	
	@Override
	public int compareTo(LoadRequest o){
		if(requiresOpenCL!=o.requiresOpenCL) return requiresOpenCL?-1:1;
		return Integer.compare(priority, o.priority);
	}
}

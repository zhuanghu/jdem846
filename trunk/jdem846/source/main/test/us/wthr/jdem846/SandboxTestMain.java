package us.wthr.jdem846;

import us.wthr.jdem846.cli.ProjectExecutor;
import us.wthr.jdem846.cli.ProjectRunPlan;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.math.Plane;
import us.wthr.jdem846.math.Spheres;
import us.wthr.jdem846.math.Vector;

public class SandboxTestMain extends AbstractTestMain
{
	private static Log log = null;
	

	
	public static void main(String[] args)
	{
		try {
			AbstractTestMain.initialize(true);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		SandboxTestMain main = new SandboxTestMain();
		main.run();
		
		
		//int frameNum = 1000000;
		//for (double e = start; e <= stop; e+=1) {
		//	System.setProperty("seaLevel.elevation", "" + e);
			
			/*
			ProjectRunPlan runPlan = new ProjectRunPlan("C:\\Users\\GillFamily\\Google Drive\\jDem Visuals\\Earth Flooding"
														, "C:\\Users\\GillFamily\\Google Drive\\jDem Visuals\\Earth Flooding\\test-output.jpg");
			
			runPlan.addOptionOverride("us.wthr.jdem846.model.GlobalOptionModel.eyeDistance", ""+2500);
			ProjectExecutor exec = new ProjectExecutor();
			try {
				exec.executeProject(runPlan);
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			*/
		//	frameNum++;
	//	}
	}
	
	public void run()
	{
		int start = -10900;
		int stop = 8900;
		int step = 10;
		int threads = 1;
		int framesPerThread = (stop - start) / threads;

		System.err.println("Frames per thread: " + framesPerThread);
		String projectPath = "C:\\Users\\GillFamily\\Google Drive\\jDem Visuals\\Earth Flooding";
		
		int frameIndexStart = 0;
		for (int threadStart = start; threadStart < stop; threadStart+=framesPerThread) {
			int threadEnd = threadStart + framesPerThread;
			
			RenderThread thread = new RenderThread(projectPath, (double)threadStart, (double)threadEnd, (double)step, frameIndexStart);
			thread.start();
			
			frameIndexStart += framesPerThread;
		}
	}
	
	
	class RenderThread extends Thread
	{
		private String projectPath;
		private double start = 0;
		private double stop = 0;
		private double step = 0;
		private int frameIndexStart;
		
		public RenderThread(String projectPath, double start, double stop, double step, int frameIndexStart)
		{
			this.projectPath = projectPath;
			this.start = start;
			this.stop = stop;
			this.step = step;
			this.frameIndexStart = frameIndexStart;
		}
		
		public void run()
		{
			
			System.err.println("Starting thread from " + start + " to " + stop);
			int frameNum = 1000000 + frameIndexStart;
			for (double e = start; e <= stop; e+=step) {
				//System.setProperty("seaLevel.elevation", "" + e);
				
				System.err.println("Starting render for elevation " + e);
				
				ProjectRunPlan runPlan = new ProjectRunPlan(projectPath
															, "E:\\frames\\frame-" + frameNum + ".jpg");
				
				runPlan.addOptionOverride("us.wthr.jdem846.model.GlobalOptionModel.eyeDistance", ""+e);
				ProjectExecutor exec = new ProjectExecutor();
				try {
					exec.executeProject(runPlan);
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				frameNum++;
			}
		}
		
	}
	
	
	public SandboxTestMain() 
	{

	}
	
	
	public static boolean test(double latitude, double longitude, double radius, Plane plane,  double innerRingRadius, double outterRingRadius)
	{
		Vector point = new Vector();
		Spheres.getPoint3D(longitude, latitude, radius, point);
		
		//Vector point = new Vector(118258.6343130089, -42615747.405093856, -42615747.403190926);
		Vector sun = new Vector(-54289586911.33092, -18629619855.001747, -135611588101.05907);
		
		
		
		Vector direction = point.getDirectionTo(sun);
		double intersectDistance = point.intersectDistance(plane, direction) * radius;

		//System.err.println("Intersect Distance: " + intersectDistance);
		
		Vector intersect = point.intersectPoint(direction, intersectDistance);
		
		if (intersect != null && intersectDistance >= 0) {

			//System.err.println("Intersect Point: " + intersect.x + "/" + intersect.y + "/" + intersect.z);
			
			double intersectRadius = intersect.getLength();
			System.err.println("Intersect Radius: " + intersectRadius + ", Intersect Distance: " + intersectDistance);
			if (intersectRadius >= innerRingRadius && intersectRadius <= outterRingRadius) {
				return true;
			}
			
			
		
		} 
		
		
		return false;
	}
	
	public static Vector findPlane(Vector pt0, Vector pt1, Vector pt2)
	{
		
		Vector vec0 = new Vector();
		Vector vec1 = new Vector();
		Vector plane = new Vector();
		
		vec0.x = pt1.x - pt0.x;
	    vec0.y = pt1.y - pt0.y;
	    vec0.z = pt1.z - pt0.z;
	    
	    vec1.x = pt2.x - pt0.x;
	    vec1.y = pt2.y - pt0.y;
	    vec1.z = pt2.z - pt0.z;
	    
	    plane.x = vec0.y * vec1.z - vec0.z * vec1.y;
	    plane.y = -(vec0.x * vec1.z - vec0.z * vec1.x);
	    plane.z = vec0.x * vec1.y - vec0.y * vec1.x;
	    plane.w = -(plane.x * pt0.x + plane.y * pt0.y + plane.z * pt0.z);
	    
		return plane;
		
	}
	
	
	 public static double intersectDistance(Vector plane, Vector origin, Vector direction)
	 {
		 double ldotv = plane.dotProduct(direction);
		 if (ldotv == 0) {
			 return 0;
		 }
		 return -plane.dotProduct4(origin) / ldotv;
	 }
	
	
	

}

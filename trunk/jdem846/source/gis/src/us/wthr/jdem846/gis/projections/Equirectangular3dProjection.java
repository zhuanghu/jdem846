package us.wthr.jdem846.gis.projections;

import us.wthr.jdem846.ModelContext;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.render.gfx.Vector;

public class Equirectangular3dProjection extends EquirectangularProjection
{
	private static Log log = Logging.getLog(Equirectangular3dProjection.class);
	
	double[] eyeVector = {0, 0, 0};
	double[] nearVector = {0, 0, 0};
	
	double rotateX = 30;
	double rotateY = 0;
	
	double[] pointVector = new double[3];
	
	private ModelContext modelContext;
	
	public Equirectangular3dProjection()
	{
		
	}
	
	public Equirectangular3dProjection(double north, double south, double east, double west, double width, double height)
	{
		setUp(north, south, east, west, width, height);
	}
	
	public void setUp(ModelContext modelContext)
	{
		this.modelContext = modelContext;

		setUp(modelContext.getNorth(), 
				modelContext.getSouth(),
				modelContext.getEast(),
				modelContext.getWest(),
				modelContext.getModelDimensions().getOutputWidth(),
				modelContext.getModelDimensions().getOutputHeight());
		
		
		rotateX = modelContext.getModelOptions().getProjection().getRotateX();
		rotateY = modelContext.getModelOptions().getProjection().getRotateY();

	}
	
	public void setUp(double north, double south, double east, double west, double width, double height)
	{
		super.setUp(north, south, east, west, width, height);
		
		rotateX = 30;
		rotateY = 0;
		
		eyeVector[2] = width;
		nearVector[2] = (width/2.0f);
		
	}
	
	
	public void getPoint(double latitude, double longitude, double elevation, MapPoint point)
	{
		super.getPoint(latitude, longitude, elevation, point);
		
		double min = 0;
		double max = 0;
		double elev = 0;
		double resolution = 0;
		
		if (this.modelContext != null) {
			min = modelContext.getRasterDataContext().getDataMinimumValue();
			max = modelContext.getRasterDataContext().getDataMaximumValue();
			resolution = modelContext.getRasterDataContext().getMetersResolution();
			elev = ((elevation - max) / resolution) + Math.abs(min);
		}

		pointVector[0] = point.column - (getWidth() / 2.0);
		pointVector[1] = elev;
		pointVector[2] = point.row - (getHeight() / 2.0);
		
		Vector.rotate(0, rotateY, 0, pointVector);
		Vector.rotate(rotateX, 0, 0, pointVector);
		
		projectTo(pointVector, eyeVector, nearVector);
		
		point.column = -pointVector[0] + (getWidth()/2.0);
		point.row = pointVector[1] + (getHeight()/2.0);
		
	}
	
	
	public void projectTo(double[] vector, double[] eye, double[] near) //Vector eye, Vector near)
	{
		//double thetaX = 0; // Orientation of the camera
		//double thetaY = 0;
		//double thetaZ = 0;
		
		double[] a = vector;   // 3D position of points being projected
		double[] e = near;     // Viewer's position relative to the display surface
		double[] c = eye;      // Camera position
		
		
		
		/*
		double sinTX = sin(thetaX);
		double sinTY = sin(thetaY);
		double sinTZ = sin(thetaZ);
		
		double cosTX = cos(thetaX);
		double cosTY = cos(thetaY);
		double cosTZ = cos(thetaZ);
		
		double dX = cosTY * (sinTZ * (a[1] - c[1]) + cosTZ * (a[0] - c[0])) - sinTY * (a[2] - c[2]);
		double dY = sinTX * (cosTY * (a[2] - c[2]) + sinTY * (sinTZ * (a[1] - c[1]) + cosTZ * (a[0] - c[0]))) + cosTX * (cosTZ * (a[1] - c[1]) - sinTZ * (a[0] - c[0]));
		double dZ = cosTX * (cosTY * (a[2] - c[2]) + sinTY * (sinTZ * (a[1] - c[1]) + cosTZ * (a[0] - c[0]))) - sinTX * (cosTZ * (a[1] - c[1]) - sinTZ * (a[0] - c[0]));
		*/
		
		vector[0] = ((a[0] - c[0]) - e[0]) * (e[2] / (a[2] - c[2]));
		vector[1] = ((a[1] - c[1]) - e[1]) * (e[2] / (a[2] - c[2]));
		
		//vector[0] = bX;
		///vector[1] = bY;
	}
}
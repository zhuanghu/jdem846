package us.wthr.jdem846.render;

import us.wthr.jdem846.DemConstants;
import us.wthr.jdem846.ModelContext;
import us.wthr.jdem846.gis.exceptions.MapProjectionException;
import us.wthr.jdem846.gis.projections.MapPoint;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.math.Spheres;
import us.wthr.jdem846.render.gfx.Vector;

public class CanvasProjectionGlobe extends CanvasProjection3d
{
	
	private static Log log = Logging.getLog(CanvasProjectionGlobe.class);
	
	
	public CanvasProjectionGlobe(ModelContext modelContext)
	{
		super(modelContext);
	}
	
	
	
	@Override
	public void getPoint(double latitude, double longitude, double elevation, MapPoint point) throws MapProjectionException
	{
		
		
		double radius = (getWidth() / 3);
		
		double elev = 0;
		elevation -= ((max + min) / 2.0);
		elev = (elevation / resolution) * elevationMultiple;
		
		double earthMeanRadiusMeters = DemConstants.EARTH_MEAN_RADIUS * 1000;
		
		radius = (radius / earthMeanRadiusMeters) * (earthMeanRadiusMeters + elevation);
		
		//double[] points = new double[3];
		Spheres.getPoint3D(longitude+180, latitude, radius, pointVector);

		//double globeLat = points[2];
		//double globeLon = points[0];
		//double globeElev = points[1];

		//super.getPoint(globeLat, globeLon, globeElev, point);
		
		//pointVector[0] = pointVector[0] - (getWidth() / 2.0);
		//pointVector[1] = pointVector[1];
		//pointVector[2] = pointVector[2] - (getHeight() / 2.0);
		
		
		Vector.rotate(0, rotateY, 0, pointVector);
		Vector.rotate(rotateX, 0, 0, pointVector);
		Vector.translate(shiftX, shiftY, shiftZ, pointVector);

		projectTo(pointVector);
		
		point.column = -pointVector[0] + (getWidth()/2.0);
		point.row = pointVector[1] + (getHeight()/2.0);
		point.z = pointVector[2];
		
		
	}
	
}

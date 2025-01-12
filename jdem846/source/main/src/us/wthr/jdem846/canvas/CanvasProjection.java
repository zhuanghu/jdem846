package us.wthr.jdem846.canvas;

import us.wthr.jdem846.gis.exceptions.MapProjectionException;
import us.wthr.jdem846.gis.projections.MapPoint;
import us.wthr.jdem846.gis.projections.MapProjection;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.math.MathExt;

public class CanvasProjection
{
	@SuppressWarnings("unused")
	private static Log log = Logging.getLog(CanvasProjection.class);
	
	private MapProjection mapProjection;
	
	private double north;
	private double south;
	private double east;
	private double west;
	
	private double width; 
	private double height;
	
	private boolean usePointAdjustments = false;
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;


	
	public CanvasProjection(MapProjection mapProjection,
			double north,
			double south,
			double east,
			double west,
			double width,
			double height)
	{
		setUp(mapProjection,
				north, 
				south,
				east,
				west,
				width,
				height);
	}
	
	public void setUp(MapProjection mapProjection,
					double north,
					double south,
					double east,
					double west,
					double width,
					double height)
	{
		this.mapProjection = mapProjection;
		this.north = north;
		this.south = south;
		this.east = east;
		this.west = west;
		this.width = width;
		this.height = height;
		
		determineXYAdjustments();
	}
	
	private void determineXYAdjustments()
	{
		if (mapProjection != null) {
			usePointAdjustments = true;
			
			/*
			minX = west;
			maxX = east;
			minY = south;
			maxY = north;
			*/
			
			
			minX = 180;
			maxX = -180;
			
			minY = Double.MAX_VALUE;
			maxY = Double.MIN_VALUE;
			
			MapPoint point = new MapPoint();
			try {
				mapProjection.getPoint(north, west, 0.0, point);
				checkXYMinMax(point);
				
				mapProjection.getPoint(north, east, 0.0, point);
				checkXYMinMax(point);
				
				mapProjection.getPoint(north, (west + east) / 2.0, 0.0, point);
				checkXYMinMax(point);
				
				mapProjection.getPoint(south, west, 0.0, point);
				checkXYMinMax(point);
				
				mapProjection.getPoint(south, east, 0.0, point);
				checkXYMinMax(point);

				mapProjection.getPoint(south, (west + east) / 2.0, 0.0, point);
				checkXYMinMax(point);
				
				mapProjection.getPoint((north + south) / 2.0, west, 0.0, point);
				checkXYMinMax(point);
				
				mapProjection.getPoint((north + south) / 2.0, east, 0.0, point);
				checkXYMinMax(point);
				
			} catch (MapProjectionException ex) {
				ex.printStackTrace();
			}
			
		}
	}
	
	private void checkXYMinMax(MapPoint point)
	{
		minX = MathExt.min(minX, point.column);
		maxX = MathExt.max(maxX, point.column);
		minY = MathExt.min(minY, point.row);
		maxY = MathExt.max(maxY, point.row);

	}

	
	public void getPoint(double latitude, double longitude, double elevation, MapPoint point) throws MapProjectionException
	{
		if (mapProjection != null) {
			mapProjection.getPoint(latitude, longitude, elevation, point);
			
			point.row = latitudeToRow(point.row);
			point.column = longitudeToColumn(point.column);

			point.z = 0.0;
		} else {
			point.row = latitudeToRow(latitude);
			point.column = longitudeToColumn(longitude);
			point.z = elevation;
		}
	}
	
	
	
	
	public double latitudeToRow(double latitude)
	{
		if (usePointAdjustments) {
			return ((double) height) * ((maxY - latitude) / (maxY - minY));
		} else {
			return ((double) height) * ((getNorth() - latitude) / (getNorth() - getSouth()));
		}
	}
	
	public double longitudeToColumn(double longitude)
	{
		if (usePointAdjustments) {
			return ((double)width) * ((longitude - minX)) / (maxX - minX);
		} else {
			return ((double)width) * ((longitude - getWest()) / (getEast() - getWest()));
		}
	}

	public MapProjection getMapProjection()
	{
		return mapProjection;
	}

	public void setMapProjection(MapProjection mapProjection)
	{
		this.mapProjection = mapProjection;
	}

	public double getNorth()
	{
		return north;
	}

	public void setNorth(double north)
	{
		this.north = north;
	}

	public double getSouth()
	{
		return south;
	}

	public void setSouth(double south)
	{
		this.south = south;
	}

	public double getEast()
	{
		return east;
	}

	public void setEast(double east)
	{
		this.east = east;
	}

	public double getWest()
	{
		return west;
	}

	public void setWest(double west)
	{
		this.west = west;
	}

	public double getWidth()
	{
		return width;
	}

	public void setWidth(double width)
	{
		this.width = width;
	}

	public double getHeight()
	{
		return height;
	}

	public void setHeight(double height)
	{
		this.height = height;
	}
	
	
	public static LatLonResolution calculateOutputResolutions(double outputWidth,
													double outputHeight,
													double dataColumns,
													double dataRows,
													double latitudeResolution,
													double longitudeResolution,
													double scaleFactor)
	{
		
		double xdimRatio = (double)outputWidth / (double)dataColumns;
		double ydimRatio = (double)outputHeight / (double)dataRows;
		
		double outputLongitudeResolution = longitudeResolution / xdimRatio;
		double outputLatitudeResolution = latitudeResolution / ydimRatio;
		
		LatLonResolution latLonRes = new LatLonResolution(outputLatitudeResolution, outputLongitudeResolution);
		return latLonRes;
	}
}

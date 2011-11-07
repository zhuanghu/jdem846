package us.wthr.jdem846.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;

import us.wthr.jdem846.ModelContext;
import us.wthr.jdem846.ModelOptionNamesEnum;
import us.wthr.jdem846.exception.CanvasException;
import us.wthr.jdem846.exception.ImageException;
import us.wthr.jdem846.image.ImageUtilities;
import us.wthr.jdem846.image.ImageWriter;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.render.mapprojection.AitoffProjection;
import us.wthr.jdem846.render.mapprojection.EquirectangularProjection;
import us.wthr.jdem846.render.mapprojection.MapPoint;
import us.wthr.jdem846.render.mapprojection.MapProjection;
import us.wthr.jdem846.render.mapprojection.WinkelTripelProjection;
import us.wthr.jdem846.util.ColorSerializationUtil;

public class ModelCanvas
{
	private static Log log = Logging.getLog(ModelCanvas.class);
	
	private MapProjection mapProjection;
	private ModelContext modelContext;
	private Color backgroundColor;
	private ModelDimensions2D modelDimensions;
	
	private BufferedImage image;
	private Graphics2D graphics;
	
	private boolean isDisposed = false;
	
	private Path2D.Double pathBuffer = new Path2D.Double();
	private MapPoint mapPoint = new MapPoint();
	
	public ModelCanvas(ModelContext modelContext)
	{
		this.modelContext = modelContext;
		backgroundColor = ColorSerializationUtil.stringToColor(modelContext.getModelOptions().getBackgroundColor());
		modelDimensions = ModelDimensions2D.getModelDimensions(modelContext);
		
		image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		graphics = (Graphics2D) image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		
		if (modelContext.getModelOptions().getBooleanOption(ModelOptionNamesEnum.ANTIALIASED)) {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		mapProjection = new EquirectangularProjection(getNorth(), getSouth(), getEast(), getWest(), getWidth(), getHeight());
		//mapProjection = new AitoffProjection(getNorth(), getSouth(), getEast(), getWest(), getWidth(), getHeight());
		//graphics.setColor(backgroundColor);
		//graphics.fillRect(0, 0, getWidth(), getHeight());
	}

	protected MapProjection getMapProjection()
	{
		return mapProjection;
	}

	protected void setMapProjection(MapProjection mapProjection)
	{
		this.mapProjection = mapProjection;
	}

	public void fillTriangle(int[] color, 
								double latitude0, double longitude0,
								double latitude1, double longitude1,
								double latitude2, double longitude2)
	{
		pathBuffer.reset();
		Color fillColor = new Color(color[0], color[1], color[2], 0xFF);
		
		mapProjection.getPoint(latitude0, longitude0, 0, mapPoint);
		double row0 = mapPoint.row;
		double column0 = mapPoint.column;
		
		mapProjection.getPoint(latitude1, longitude1, 0, mapPoint);
		double row1 = mapPoint.row;
		double column1 = mapPoint.column;
		
		mapProjection.getPoint(latitude2, longitude2, 0, mapPoint);
		double row2 = mapPoint.row;
		double column2 = mapPoint.column;
		

		pathBuffer.moveTo(column0, row0);
		pathBuffer.lineTo(column1, row1);
		pathBuffer.lineTo(column2, row2);
		pathBuffer.closePath();
		
		graphics.setColor(fillColor);
		graphics.fill(pathBuffer);
		
	}
	
	public void fillRectangle(int[] color, 
			double latitude0, double longitude0,
			double latitude1, double longitude1,
			double latitude2, double longitude2,
			double latitude3, double longitude3)
	{
		pathBuffer.reset();
		Color fillColor = new Color(color[0], color[1], color[2], color[3]);
		
		mapProjection.getPoint(latitude0, longitude0, 0, mapPoint);
		double row0 = mapPoint.row;
		double column0 = mapPoint.column;
		
		mapProjection.getPoint(latitude1, longitude1, 0, mapPoint);
		double row1 = mapPoint.row;
		double column1 = mapPoint.column;
		
		mapProjection.getPoint(latitude2, longitude2, 0, mapPoint);
		double row2 = mapPoint.row;
		double column2 = mapPoint.column;
		
		mapProjection.getPoint(latitude3, longitude3, 0, mapPoint);
		double row3 = mapPoint.row;
		double column3 = mapPoint.column;
	
		pathBuffer.moveTo(column0, row0);
		pathBuffer.lineTo(column1, row1);
		pathBuffer.lineTo(column2, row2);
		pathBuffer.lineTo(column3, row3);
		pathBuffer.closePath();
		
		fillShape(fillColor, pathBuffer);
	
	}
	
	public void fillShape(Color color, Shape shape)
	{
		graphics.setColor(color);
		graphics.fill(shape);
	}
	
	public void drawShape(Color color, Shape shape)
	{
		graphics.setColor(color);
		graphics.draw(shape);
	}
	
	public int getColor(int x, int y)
	{
		if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight())
			return 0;
		
		return image.getRGB(x, y);
	}

	
	public double getNorth()
	{
		return modelContext.getNorth();
	}
	
	public double getSouth()
	{
		return modelContext.getSouth();
	}
	
	public double getEast()
	{
		return modelContext.getEast();
	}
	
	public double getWest()
	{
		return modelContext.getWest();
	}
	
	
	public int getWidth()
	{
		return modelDimensions.getOutputWidth();
	}
	
	public int getHeight()
	{
		return modelDimensions.getOutputHeight();
	}
	
	public boolean isDisposed()
	{
		return isDisposed;
	}
	
	
	public Image getImage()
	{
		return image;
	}
	
	public Image getFinalizedImage()
	{
		BufferedImage finalImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) finalImage.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		if (modelContext.getModelOptions().getBooleanOption(ModelOptionNamesEnum.ANTIALIASED)) {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		graphics.drawImage(image, 0, 0, null);
		stripAlphaChannel(finalImage);
		applyBackgroundColor(finalImage);
		graphics.dispose();
		return finalImage;
	}
	
	protected void applyBackgroundColor(BufferedImage image)
	{
		WritableRaster raster = image.getRaster();
		int[] rasterPixel = new int[4];
		
		for (int row = 0; row < raster.getHeight(); row++) {
			for (int column = 0; column < raster.getWidth(); column++) {
				raster.getPixel(column, row, rasterPixel);
				
				if (rasterPixel[0] == 0 && rasterPixel[1] == 0 && rasterPixel[2] == 0 && rasterPixel[3] == 0) {
					// Apply background color
					
					rasterPixel[0] = backgroundColor.getRed();
					rasterPixel[1] = backgroundColor.getGreen();
					rasterPixel[2] = backgroundColor.getBlue();
					rasterPixel[3] = backgroundColor.getAlpha();
					
					raster.setPixel(column, row, rasterPixel);
				} 
			}
			
		}
	}
	
	protected void stripAlphaChannel(BufferedImage image)
	{
		WritableRaster raster = image.getRaster();
		int[] rasterPixel = new int[4];
		
		for (int row = 0; row < raster.getHeight(); row++) {
			for (int column = 0; column < raster.getWidth(); column++) {
				raster.getPixel(column, row, rasterPixel);
				
				if (!(rasterPixel[0] == 0 && rasterPixel[1] == 0 && rasterPixel[2] == 0 && rasterPixel[3] == 0)) {
					// Remove alpha channel
					
					rasterPixel[3] = 255;
					raster.setPixel(column, row, rasterPixel);
				}
			}
			
		}
	}
	
	
	public void save(String saveTo) throws CanvasException
	{
		try {
			ImageWriter.saveImage((BufferedImage)getFinalizedImage(), saveTo);
		} catch (ImageException ex) {
			throw new CanvasException("Failed to save image to disk: " + ex.getMessage(), ex);
		}

	}
	
	public void dispose()
	{
		if (!isDisposed) {
			graphics.dispose();
			graphics = null;
			image = null;
		}
	}
}
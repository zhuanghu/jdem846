package us.wthr.jdem846.graphics;

import us.wthr.jdem846.ModelContext;
import us.wthr.jdem846.ModelDimensions;
import us.wthr.jdem846.gis.projections.MapProjection;
import us.wthr.jdem846.math.Vector;
import us.wthr.jdem846.model.GlobalOptionModel;
import us.wthr.jdem846.model.ModelGrid;
import us.wthr.jdem846.model.ModelPointGrid;
import us.wthr.jdem846.scripting.ScriptProxy;

public interface View {
	
	public void setModelContext(ModelContext arg);
	public void setGlobalOptionModel(GlobalOptionModel arg);
	public void setModelDimensions(ModelDimensions arg);
	public void setMapProjection(MapProjection arg);

	public void setScript(ScriptProxy arg);
	public void setModelGrid(ModelPointGrid arg);
	
	public double radiusTrue();
	public double radius();
	public double horizFieldOfView();
	public double elevationFromSurface();
	public double nearClipDistance();
	public double farClipDistance();
	public double eyeZ();
	
	public void project(double latitude, double longitude, double elevation, Vector point);
}

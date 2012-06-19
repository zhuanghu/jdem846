package us.wthr.jdem846.model.processing.shading;

import us.wthr.jdem846.DemConstants;
import us.wthr.jdem846.ModelContext;
import us.wthr.jdem846.color.ColorAdjustments;
import us.wthr.jdem846.exception.RayTracingException;
import us.wthr.jdem846.exception.RenderEngineException;
import us.wthr.jdem846.gis.planets.Planet;
import us.wthr.jdem846.gis.planets.PlanetsRegistry;
import us.wthr.jdem846.lighting.LightSourceSpecifyTypeEnum;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.math.MathExt;
import us.wthr.jdem846.math.Vectors;
import us.wthr.jdem846.model.ModelGrid;
import us.wthr.jdem846.model.ModelPointHandler;
import us.wthr.jdem846.model.ViewPerspective;
import us.wthr.jdem846.model.annotations.GridProcessing;
import us.wthr.jdem846.model.processing.AbstractGridProcessor;
import us.wthr.jdem846.model.processing.GridProcessingTypesEnum;
import us.wthr.jdem846.model.processing.GridProcessor;
import us.wthr.jdem846.model.processing.shading.RayTracing.RasterDataFetchHandler;
import us.wthr.jdem846.model.processing.util.LightingCalculator;
import us.wthr.jdem846.model.processing.util.SunlightPositioning;
import us.wthr.jdem846.model.processing.util.SurfaceNormalCalculator;
import us.wthr.jdem846.scripting.ScriptProxy;
import us.wthr.jdem846.scripting.ScriptingContext;


@GridProcessing(id="us.wthr.jdem846.model.processing.coloring.HillshadingProcessor",
				name="Hillshading Process",
				type=GridProcessingTypesEnum.SHADING,
				optionModel=HillshadingOptionModel.class,
				enabled=true
				)
public class HillshadingProcessor extends AbstractGridProcessor implements GridProcessor, ModelPointHandler
{
	private static Log log = Logging.getLog(HillshadingProcessor.class);

	protected boolean lightingEnabled = true;
	
	protected double relativeLightIntensity;
	protected double relativeDarkIntensity;
	protected int spotExponent;
	
	private double latitudeResolution;
	private double longitudeResolution;
	
	
	
	protected double sunsource[] = new double[3];
	protected double solarElevation;
	protected double solarAzimuth;
	protected double solarZenith;
	
	protected int[] rgbaBuffer = new int[4];

	protected double lightZenith;
	protected double darkZenith;


	protected boolean recalcLightOnEachPoint;

	
	private boolean advancedLightingControl = false;
	private LightingCalculator advancedLightingCalculator;
	private double modelRadius;
	
	protected RayTracing lightSourceRayTracer;
	protected boolean rayTraceShadows;
	protected double shadowIntensity;
	
	
	private double[] normal = new double[3];
	private SurfaceNormalCalculator normalsCalculator;
	
	private SunlightPositioning sunlightPosition;
	private ViewPerspective viewPerspective;
	
	private Planet planet;
	
	public HillshadingProcessor()
	{
		
	}
	
	public HillshadingProcessor(ModelContext modelContext, ModelGrid modelGrid)
	{
		super(modelContext, modelGrid);
	}
	
	@Override
	public void prepare() throws RenderEngineException
	{
		
		HillshadingOptionModel optionModel = (HillshadingOptionModel) this.getProcessOptionModel();
		
		lightingEnabled = optionModel.isLightingEnabled();
		
		relativeLightIntensity = optionModel.getLightIntensity();
		relativeDarkIntensity = optionModel.getDarkIntensity();
		spotExponent = optionModel.getSpotExponent();
		
		viewPerspective = this.getGlobalOptionModel().getViewAngle();
		
		
		ScriptingContext scriptingContext = modelContext.getScriptingContext();
		ScriptProxy scriptProxy = null;
		if (getGlobalOptionModel().getUseScripting() && scriptingContext != null) {
			scriptProxy = scriptingContext.getScriptProxy();
		}
		
		advancedLightingControl = optionModel.getAdvancedLightingControl();
		advancedLightingCalculator = new LightingCalculator(optionModel.getEmmisive(), optionModel.getAmbient(), optionModel.getDiffuse(), optionModel.getSpecular(), optionModel.getShadowIntensity(), viewPerspective, scriptProxy);
		
		advancedLightingCalculator.setUseDistanceAttenuation(optionModel.getUseDistanceAttenuation());
		advancedLightingCalculator.setAttenuationRadius(optionModel.getAttenuationRadius());
		
		planet = PlanetsRegistry.getPlanet(getGlobalOptionModel().getPlanet());
		if (planet != null) {
			modelRadius = planet.getMeanRadius() * 1000;
		} else {
			modelRadius = DemConstants.EARTH_MEAN_RADIUS * 1000;
		}


		latitudeResolution = getModelDimensions().getOutputLatitudeResolution();
		longitudeResolution = getModelDimensions().getOutputLongitudeResolution();
		

		
		
		

		LightSourceSpecifyTypeEnum lightSourceType = LightSourceSpecifyTypeEnum.getByOptionValue(optionModel.getSourceType());
		
		long lightOnTime = optionModel.getSunlightTime().getTime();
		long lightOnDate = optionModel.getSunlightDate().getDate();
		lightOnDate += lightOnTime;
		
		recalcLightOnEachPoint = optionModel.isRecalcLightForEachPoint();
		lightZenith = optionModel.getLightZenith();
		darkZenith = optionModel.getDarkZenith();

		
		sunlightPosition = new SunlightPositioning(modelContext, modelGrid, lightOnDate, viewPerspective);
		if (lightSourceType == LightSourceSpecifyTypeEnum.BY_AZIMUTH_AND_ELEVATION) {
			
			solarAzimuth = optionModel.getSourceLocation().getAzimuthAngle();
			solarElevation = optionModel.getSourceLocation().getElevationAngle();
			
			sunlightPosition.getLightPositionByAngles(solarElevation, solarAzimuth, sunsource);
			
			recalcLightOnEachPoint = false;
		}
		
		if (lightSourceType == LightSourceSpecifyTypeEnum.BY_DATE_AND_TIME && !recalcLightOnEachPoint) {
			
			double north = getGlobalOptionModel().getNorthLimit();
			double south = getGlobalOptionModel().getSouthLimit();
			double east = getGlobalOptionModel().getEastLimit();
			double west = getGlobalOptionModel().getWestLimit();
			
			double latitude = (north + south) / 2.0;
			double longitude = (east + west) / 2.0;
				
			sunlightPosition.getLightPositionByCoordinates(latitude, longitude, sunsource);

		}

		
		rayTraceShadows = optionModel.isRayTraceShadows();
		shadowIntensity = optionModel.getShadowIntensity();
		if (rayTraceShadows) {
			lightSourceRayTracer = new RayTracing(
					latitudeResolution, 
					longitudeResolution, 
					modelRadius,
					modelContext.getNorth(),
					modelContext.getSouth(),
					modelContext.getEast(),
					modelContext.getWest(),
					modelContext.getRasterDataContext().getDataMaximumValue(),
					modelContext.getRasterDataContext().getElevationScaler(),
					new RasterDataFetchHandler() {
						public double getRasterData(double latitude, double longitude) throws Exception {
							return getElevationAtPoint(latitude, longitude);
						}
			});
		} else {
			lightSourceRayTracer = null;
		}
		

		normalsCalculator = new SurfaceNormalCalculator(modelGrid, 
				planet, 
				getModelDimensions().getOutputLatitudeResolution(), 
				getModelDimensions().getOutputLongitudeResolution(),
				viewPerspective);

	}

	@Override
	public void process() throws RenderEngineException
	{
		super.process();
	}
	
	@Override
	public void onCycleStart() throws RenderEngineException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onModelLatitudeStart(double latitude)
			throws RenderEngineException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onModelPoint(double latitude, double longitude)
			throws RenderEngineException
	{
		processPointColor(latitude, longitude);
	}

	@Override
	public void onModelLatitudeEnd(double latitude)
			throws RenderEngineException
	{
		
		
	}

	@Override
	public void onCycleEnd() throws RenderEngineException
	{
		
		
	}


	

	
	protected double calculateDotProduct(double[] normal) throws RenderEngineException
	{
		double dot = Vectors.dotProduct(normal, sunsource);
		
		double lower = lightZenith;
		double upper = darkZenith;
		
		if (solarZenith > lower && solarZenith <= upper) {
			double range = (solarZenith - lower) / (upper - lower);
			dot = dot - (2 * range);
		} else if (solarZenith > upper) {
			dot = dot - (2 * 1.0);
		}
		if (dot < -1.0) {
			dot = -1.0;
		}
		
		return dot;

		
	}
	
	protected double calculateRayTracedShadow(double elevation, double latitude, double longitude) throws RayTracingException
	{
		if (this.rayTraceShadows) {
			double blockAmt = lightSourceRayTracer.isRayBlocked(this.solarElevation, this.solarAzimuth, latitude, longitude, elevation);
			return blockAmt;
		} else {
			return 0.0;
		}
	}
	

	
	protected void processPointColor(double latitude, double longitude) throws RenderEngineException
	{
		if (!lightingEnabled) {
			return;
		}
		
		if (recalcLightOnEachPoint) {
			sunlightPosition.getLightPositionByCoordinates(latitude, longitude, sunsource);
		}
		
		modelGrid.getRgba(latitude, longitude, rgbaBuffer);
		normalsCalculator.calculateNormal(latitude, longitude, normal);
		
		
		double blockAmt = 0;
		try {
			blockAmt = calculateRayTracedShadow(modelGrid.getElevation(latitude, longitude), latitude, longitude);
		} catch (RayTracingException ex) {
			throw new RenderEngineException("Error running ray tracing: " + ex.getMessage(), ex);
		}
		

		
		
		if (advancedLightingControl) {
			advancedLightingCalculator.calculateColor(normal, 
													latitude, 
													longitude, 
													modelRadius, 
													spotExponent,
													blockAmt,
													sunsource,
													rgbaBuffer);
			
			
		} else {
			double dot = calculateDotProduct(normal);
			
			
			if (dot > 0) {
				dot *= relativeLightIntensity;
			} else if (dot < 0) {
				dot *= relativeDarkIntensity;
			}
			
			if (blockAmt > 0) {
				dot = dot - (2 * shadowIntensity * blockAmt);
				if (dot < -1.0) {
					dot = -1.0;
				}
			}
			
			if (spotExponent != 1) {
				dot = MathExt.pow(dot, spotExponent);
			}
			ColorAdjustments.adjustBrightness(rgbaBuffer, dot);
		}

		modelGrid.setRgba(latitude, longitude, rgbaBuffer);
	}
	



	public boolean rayTraceShadows()
	{
		return rayTraceShadows;
	}

	public void setRayTraceShadows(boolean rayTraceShadows)
	{
		this.rayTraceShadows = rayTraceShadows;
	}

	public boolean recalcLightOnEachPoint()
	{
		return recalcLightOnEachPoint;
	}

	public void setRecalcLightOnEachPoint(boolean recalcLightOnEachPoint)
	{
		this.recalcLightOnEachPoint = recalcLightOnEachPoint;
	}
	
	
	
	
	
}

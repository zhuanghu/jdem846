package us.wthr.jdem846.model.processing.shading;

import us.wthr.jdem846.DemConstants;
import us.wthr.jdem846.canvas.CanvasProjectionTypeEnum;
import us.wthr.jdem846.exception.RayTracingException;
import us.wthr.jdem846.exception.RenderEngineException;
import us.wthr.jdem846.gis.planets.Planet;
import us.wthr.jdem846.gis.planets.PlanetsRegistry;
import us.wthr.jdem846.graphics.ElevationFetchCallback;
import us.wthr.jdem846.graphics.INormalsCalculator;
import us.wthr.jdem846.graphics.SphericalNormalsCalculator;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.math.MathExt;
import us.wthr.jdem846.math.Spheres;
import us.wthr.jdem846.math.Vector;
import us.wthr.jdem846.math.Vectors;
import us.wthr.jdem846.model.GlobalOptionModel;
import us.wthr.jdem846.model.ViewPerspective;
import us.wthr.jdem846.model.annotations.GridProcessing;
import us.wthr.jdem846.model.processing.GridProcessingTypesEnum;
import us.wthr.jdem846.model.processing.GridProcessor;
import us.wthr.jdem846.model.processing.shading.RayTracing.RasterDataFetchHandler;
import us.wthr.jdem846.model.processing.util.LightingCalculator;
import us.wthr.jdem846.model.processing.util.SunlightPositioning;
import us.wthr.jdem846.scripting.ScriptProxy;
import us.wthr.jdem846.scripting.ScriptingContext;
import us.wthr.jdem846.util.ColorUtil;

@GridProcessing(id = "us.wthr.jdem846.model.processing.coloring.HillshadingProcessor"
				, name = "Hillshading Process"
				, type = GridProcessingTypesEnum.SHADING
				, optionModel = HillshadingOptionModel.class
				, enabled = true)
public class HillshadingProcessor extends GridProcessor
{
	private static Log log = Logging.getLog(HillshadingProcessor.class);

	protected boolean lightingEnabled = true;

	protected double relativeLightIntensity;
	protected double relativeDarkIntensity;
	protected int spotExponent;

	private double latitudeResolution;
	private double longitudeResolution;

	protected Vector sunsource = new Vector();
	protected double solarElevation;
	protected double solarAzimuth;
	protected double solarZenith;

	protected int[] rgbaBuffer = new int[4];

	private boolean advancedLightingControl = false;
	private LightingCalculator advancedLightingCalculator;
	private double modelRadius;

	protected RayTracing lightSourceRayTracer;
	protected boolean rayTraceShadows;
	protected double shadowIntensity;

	private Vector normal = new Vector();
	private INormalsCalculator normalsCalculator;

	private SunlightPositioning sunlightPosition;
	private ViewPerspective viewPerspective;

	private Planet planet;

	public HillshadingProcessor()
	{

	}

	@Override
	public void prepare() throws RenderEngineException
	{

		HillshadingOptionModel optionModel = (HillshadingOptionModel) this.getOptionModel();
		GlobalOptionModel globalOptionModel = this.getGlobalOptionModel();

		lightingEnabled = false;//optionModel.isLightingEnabled();

		relativeLightIntensity = optionModel.getLightIntensity();
		relativeDarkIntensity = optionModel.getDarkIntensity();
		spotExponent = optionModel.getSpotExponent();

		viewPerspective = this.getGlobalOptionModel().getViewAngle();

		if (CanvasProjectionTypeEnum.PROJECT_FLAT.identifier().equals(getGlobalOptionModel().getRenderProjection())) {
			viewPerspective.setRotateX(0.0);
			viewPerspective.setRotateY(0.0);
			viewPerspective.setRotateZ(0.0);
			viewPerspective.setShiftX(0.0);
			viewPerspective.setShiftY(0.0);
			viewPerspective.setShiftZ(0.0);
			viewPerspective.setZoom(1.0);
		}

		planet = PlanetsRegistry.getPlanet(getGlobalOptionModel().getPlanet());
		if (planet != null) {
			modelRadius = planet.getMeanRadius();
		} else {
			modelRadius = DemConstants.EARTH_MEAN_RADIUS;
		}

		ScriptingContext scriptingContext = modelContext.getScriptingContext();
		ScriptProxy scriptProxy = null;
		if (getGlobalOptionModel().getUseScripting() && scriptingContext != null) {
			scriptProxy = scriptingContext.getScriptProxy();
		}

		advancedLightingControl = optionModel.getAdvancedLightingControl();
		advancedLightingCalculator = new LightingCalculator(optionModel.getEmmisive(), optionModel.getAmbient(), optionModel.getDiffuse(), optionModel.getSpecular(), optionModel.getShadowIntensity(),
				viewPerspective, scriptProxy);

		// double minSideLength = MathExt.min(globalOptionModel.getWidth(),
		// globalOptionModel.getHeight()) - 20;
		// double fov = 18.0;
		// double a = (fov / 2.0);
		// double R = modelRadius;

		// double D = R / MathExt.tan(MathExt.radians(a));
		// double d = (minSideLength / 2.0) / MathExt.tan(MathExt.radians(a));

		Vector eye = new Vector();
		Spheres.getPoint3D(0, 0, globalOptionModel.getEyeDistance(), eye);
		Vectors.rotate(-viewPerspective.getRotateX(), -viewPerspective.getRotateY(), -viewPerspective.getRotateZ(), eye, Vectors.YXZ);
		advancedLightingCalculator.setEye(eye);

		advancedLightingCalculator.setUseDistanceAttenuation(optionModel.getUseDistanceAttenuation());
		advancedLightingCalculator.setAttenuationRadius(optionModel.getAttenuationRadius());

		latitudeResolution = getModelDimensions().getTextureLatitudeResolution();
		longitudeResolution = getModelDimensions().getTextureLongitudeResolution();

		// LightSourceSpecifyTypeEnum lightSourceType =
		// LightSourceSpecifyTypeEnum.getByOptionValue(optionModel.getSourceType());

		long lightOnTime = optionModel.getSunlightTime().getTime();
		long lightOnDate = optionModel.getSunlightDate().getDate();
		lightOnDate += lightOnTime;

		sunlightPosition = new SunlightPositioning(lightOnDate);
		sunlightPosition.getLightPosition(sunsource);

		rayTraceShadows = optionModel.isRayTraceShadows();
		shadowIntensity = optionModel.getShadowIntensity();
		if (rayTraceShadows) {
			lightSourceRayTracer = new RayTracing(latitudeResolution, longitudeResolution, modelRadius, modelContext.getNorth(), modelContext.getSouth(), modelContext.getEast(),
					modelContext.getWest(), modelContext.getRasterDataContext().getDataMaximumValue(), null/*modelContext.getRasterDataContext().getElevationScaler()*/, new RasterDataFetchHandler()
					{
						public double getRasterData(double latitude, double longitude) throws Exception
						{
							return 0x0;// getElevationAtPoint(latitude,
										// longitude);
						}
					});
		} else {
			lightSourceRayTracer = null;
		}

		normalsCalculator = new SphericalNormalsCalculator(planet, getModelDimensions().getTextureLatitudeResolution(), getModelDimensions().getTextureLongitudeResolution(), new ElevationFetchCallback() {

			@Override
			public double getElevation(double latitude, double longitude)
			{
				return modelGrid.getElevation(latitude, longitude, true);
			}
			
		});

	}

	@Override
	public void onLatitudeStart(double latitude) throws RenderEngineException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onModelPoint(double latitude, double longitude) throws RenderEngineException
	{
		processPointColor(latitude, longitude);
		// double elevation = modelGrid.getElevation(latitude, longitude,
		// false);
	}

	@Override
	public void onLatitudeEnd(double latitude) throws RenderEngineException
	{

	}

	protected double calculateDotProduct(Vector normal) throws RenderEngineException
	{
		return Vectors.dotProduct(normal, sunsource);
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

		double blockAmt = 0;
		double elevation = 0;
		try {
			elevation = modelGrid.getElevation(latitude, longitude, false);

			blockAmt = calculateRayTracedShadow(elevation, latitude, longitude);
		} catch (RayTracingException ex) {
			throw new RenderEngineException("Error running ray tracing: " + ex.getMessage(), ex);
		}

		modelGrid.getRgba(latitude, longitude, rgbaBuffer);

		if (lightingEnabled) {
			normalsCalculator.calculateNormal(latitude, longitude, normal);

			if (advancedLightingControl) {
				advancedLightingCalculator.calculateColor(normal, latitude, longitude, elevation, modelRadius, spotExponent, blockAmt, sunsource, rgbaBuffer);

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
				ColorUtil.adjustBrightness(rgbaBuffer, dot);
			}
		}

		modelGrid.setRgba(latitude, longitude, rgbaBuffer);
	}

	@Override
	public void onProcessBefore() throws RenderEngineException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onProcessAfter() throws RenderEngineException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() throws RenderEngineException
	{
		// TODO Auto-generated method stub

	}

}

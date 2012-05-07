package us.wthr.jdem846.model.processing.shading;

import us.wthr.jdem846.model.OptionModel;
import us.wthr.jdem846.model.annotations.Order;
import us.wthr.jdem846.model.annotations.ProcessOption;

public class SlopeShadingOptionModel implements OptionModel
{
	
	private double lightMultiple = 1.0;
	private double lightIntensity = 0.75;
	private double darkIntensity = 1.0;
	private int spotExponent = 1;
	
	public SlopeShadingOptionModel()
	{
		
	}
	
	
	@ProcessOption(id="us.wthr.jdem846.model.SlopeShadingOptionModel.lightMultiple",
			label="Light Multiple",
			tooltip="",
			enabled=true)
	@Order(80)
	public double getLightMultiple()
	{
		return lightMultiple;
	}

	public void setLightMultiple(double lightMultiple)
	{
		this.lightMultiple = lightMultiple;
	}

	@ProcessOption(id="us.wthr.jdem846.model.SlopeShadingOptionModel.lightIntensity",
			label="Light Intensity",
			tooltip="",
			enabled=true)
	@Order(90)
	public double getLightIntensity()
	{
		return lightIntensity;
	}

	public void setLightIntensity(double lightIntensity)
	{
		this.lightIntensity = lightIntensity;
	}
	
	@ProcessOption(id="us.wthr.jdem846.model.SlopeShadingOptionModel.darkIntensity",
			label="Dark Intensity",
			tooltip="",
			enabled=true)
	@Order(100)
	public double getDarkIntensity()
	{
		return darkIntensity;
	}

	public void setDarkIntensity(double darkIntensity)
	{
		this.darkIntensity = darkIntensity;
	}

	@ProcessOption(id="us.wthr.jdem846.model.SlopeShadingOptionModel.spotExponent",
			label="Spot Exponent",
			tooltip="",
			enabled=true)
	@Order(110)
	public int getSpotExponent()
	{
		return spotExponent;
	}

	public void setSpotExponent(int spotExponent)
	{
		this.spotExponent = spotExponent;
	}
	
	
	
	public SlopeShadingOptionModel copy()
	{
		SlopeShadingOptionModel copy = new SlopeShadingOptionModel();
		copy.lightMultiple = this.lightMultiple;
		copy.lightIntensity = this.lightIntensity;
		copy.darkIntensity = this.darkIntensity;
		copy.spotExponent = this.spotExponent;
		return copy;
	}
	
}

/*
 * Copyright (C) 2011 Kevin M. Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.wthr.jdem846;

import java.util.UUID;

import us.wthr.jdem846.input.DataPackage;
import us.wthr.jdem846.rasterdata.RasterDataProxy;
import us.wthr.jdem846.scripting.ScriptProxy;
import us.wthr.jdem846.util.UniqueIdentifierUtil;

/** Provides a unique context environment for each modeling task
 * 
 * @author Kevin M. Gill
 *
 */
public class ModelContext
{
	
	
	
	@Deprecated
	private DataPackage dataPackage;
	
	private RasterDataProxy rasterDataProxy;
	private ModelOptions modelOptions;
	private ScriptProxy scriptProxy;
	private String contextId;
	
	@Deprecated
	protected ModelContext(DataPackage dataPackage, ModelOptions modelOptions, ScriptProxy scriptProxy, String contextId)
	{
		this.dataPackage = dataPackage;
		this.modelOptions = modelOptions;
		this.scriptProxy = scriptProxy;
	}
	
	protected ModelContext(RasterDataProxy rasterDataProxy, ModelOptions modelOptions, ScriptProxy scriptProxy, String contextId)
	{
		this.rasterDataProxy = rasterDataProxy;
		this.modelOptions = modelOptions;
		this.scriptProxy = scriptProxy;
	}

	public RasterDataProxy getRasterDataProxy()
	{
		return rasterDataProxy;
	}
	
	@Deprecated
	public DataPackage getDataPackage()
	{
		return dataPackage;
	}
	
	public ModelOptions getModelOptions()
	{
		return modelOptions;
	}

	public ScriptProxy getScriptProxy()
	{
		return scriptProxy;
	}

	public String getContextId()
	{
		return contextId;
	}
	
	@Deprecated
	public static ModelContext createInstance(DataPackage dataPackage, ModelOptions modelOptions)
	{
		return ModelContext.createInstance(dataPackage, modelOptions, null);
	}
	
	@Deprecated
	public static ModelContext createInstance(DataPackage dataPackage, ModelOptions modelOptions, ScriptProxy scriptProxy)
	{
		String contextId = ModelContext.generateContextId();
		ModelContext modelContext = new ModelContext(dataPackage, modelOptions, scriptProxy, contextId);
		
		return modelContext;
	}
	
	public static ModelContext createInstance(RasterDataProxy rasterDataProxy, ModelOptions modelOptions)
	{
		return ModelContext.createInstance(rasterDataProxy, modelOptions, null);
	}
	
	public static ModelContext createInstance(RasterDataProxy rasterDataProxy, ModelOptions modelOptions, ScriptProxy scriptProxy)
	{
		String contextId = ModelContext.generateContextId();
		ModelContext modelContext = new ModelContext(rasterDataProxy, modelOptions, scriptProxy, contextId);
		
		return modelContext;
	}
	
	
	protected static String generateContextId()
	{
		return UniqueIdentifierUtil.getNewIdentifier();
	}
}

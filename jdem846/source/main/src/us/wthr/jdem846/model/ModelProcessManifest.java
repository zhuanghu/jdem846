package us.wthr.jdem846.model;

import java.util.LinkedList;
import java.util.List;

import us.wthr.jdem846.graphics.View;
import us.wthr.jdem846.graphics.framebuffer.FrameBuffer;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.model.exceptions.InvalidProcessOptionException;
import us.wthr.jdem846.model.exceptions.ModelContainerException;
import us.wthr.jdem846.model.exceptions.ProcessContainerException;
import us.wthr.jdem846.model.processing.GridWorker;
import us.wthr.jdem846.model.processing.ModelProcessRegistry;
import us.wthr.jdem846.model.processing.ProcessInstance;
import us.wthr.jdem846.model.processing.RenderProcessor;

public class ModelProcessManifest
{
	private static Log log = Logging.getLog(ModelProcessManifest.class);
	
	
	private OptionModelContainer globalOptionModelContainer;
	
	private List<ModelProcessContainer> workerList = new LinkedList<ModelProcessContainer>();
	
	public ModelProcessManifest() throws ProcessContainerException
	{
		this(new GlobalOptionModel());
	}
	
	public ModelProcessManifest(GlobalOptionModel globalOptionModel) throws ProcessContainerException
	{
		if (globalOptionModel != null) {
			setGlobalOptionModel(globalOptionModel);
		}
	}
	
	public ModelProcessManifest(OptionModelContainer globalOptionModelContainer)
	{
		this.globalOptionModelContainer = globalOptionModelContainer;
	}
	
	public ModelProgram createModelProgram(FrameBuffer frameBuffer, View view) throws Exception
	{
		ModelProgram modelProgram = new ModelProgram();
		
		for (ModelProcessContainer container : workerList) {
			
			GridWorker worker = container.getGridWorker().getClass().newInstance();
			OptionModel optionModel = container.getOptionModel();

			if (worker instanceof RenderProcessor) {
				RenderProcessor r = (RenderProcessor) worker;
				r.setFrameBuffer(frameBuffer);
				r.setView(view);
			}
			
			worker.setOptionModel(optionModel);
			modelProgram.addWorker(worker);

			
		}
		
		return modelProgram;
	}
	
	
	
	public void removeAll()
	{
		workerList.clear();
	}
	
	public void addWorker(String processId) throws ProcessContainerException
	{
		addWorker(processId, (OptionModel) null);
	}
	
	public void addWorker(String processId, OptionModel optionModel) throws ProcessContainerException
	{
		ProcessInstance processInstance = ModelProcessRegistry.getInstance(processId);
		Class<?> clazz = (Class<?>) processInstance.getProcessorClass();
		
		
		GridWorker gridWorker = null;
		try {
			gridWorker = (GridWorker) clazz.newInstance();
		} catch (Exception ex) {
			// TODO: Throw up
			log.error("Error creating grid worker instance: " + ex.getMessage(), ex);
			return;
		}
		
		addWorker(gridWorker, optionModel);
		
	}
	
	public void addWorker(GridWorker gridWorker, OptionModel optionModel) throws ProcessContainerException
	{
		addProcessContainer(new ModelProcessContainer(gridWorker, optionModel));
	}
	
	public void addProcessContainer(ModelProcessContainer processContainer)
	{
		workerList.add(processContainer);
	}
	
	public List<ModelProcessContainer> getProcessList()
	{
		return workerList;
	}
	
	public int getProcessListSize()
	{
		return workerList.size();
	}
	
	public ModelProcessContainer getProcessContainerByIndex(int index)
	{
		return workerList.get(index);
	}

	public GlobalOptionModel getGlobalOptionModel()
	{
		return (GlobalOptionModel) this.globalOptionModelContainer.getOptionModel();
	}

	public OptionModelContainer getGlobalOptionModelContainer()
	{
		return this.globalOptionModelContainer;
	}
	
	public void setGlobalOptionModel(GlobalOptionModel globalOptionModel) throws ProcessContainerException
	{
		try {
			globalOptionModelContainer = new OptionModelContainer(globalOptionModel);
		} catch (InvalidProcessOptionException ex) {
			throw new ProcessContainerException("Error creating default container for global option model: " + ex.getMessage(), ex);
		}
	}
	
	
	protected OptionModelContainer getOptionModelContainerThatContainsPropertyName(String name)
	{
		if (globalOptionModelContainer != null && globalOptionModelContainer.hasPropertyByName(name)) {
			return globalOptionModelContainer;
		}
		
		for (ModelProcessContainer processContainer : this.workerList) {
			OptionModelContainer optionModelContainer = processContainer.getOptionModelContainer();
			if (optionModelContainer != null && optionModelContainer.hasPropertyByName(name)) {
				return optionModelContainer;
			}
			
		}
		
		return null;
	}
	
	protected OptionModelContainer getOptionModelContainerThatContainsPropertyId(String id)
	{
		if (globalOptionModelContainer != null && globalOptionModelContainer.hasPropertyById(id)) {
			return globalOptionModelContainer;
		}
		
		for (ModelProcessContainer processContainer : this.workerList) {
			OptionModelContainer optionModelContainer = processContainer.getOptionModelContainer();
			if (optionModelContainer != null && optionModelContainer.hasPropertyById(id)) {
				return optionModelContainer;
			}
			
		}
		
		return null;
	}
	
	public boolean setPropertyById(String id, Object value) throws ModelContainerException
	{
		OptionModelContainer optionModelContainer = getOptionModelContainerThatContainsPropertyId(id);
		if (optionModelContainer != null && optionModelContainer.hasPropertyById(id)) {
			optionModelContainer.setPropertyValueById(id, value);
			return true;
		} else {
			return false;
		}
	}
	
	
	public Object getPropertyById(String id) throws ModelContainerException
	{
		OptionModelContainer optionModelContainer = getOptionModelContainerThatContainsPropertyId(id);
		if (optionModelContainer != null && optionModelContainer.hasPropertyById(id)) {
			return optionModelContainer.getPropertyValueById(id);
		} else {
			return null;
		}
	}
	
	public boolean setPropertyByName(String name, Object value) throws ModelContainerException
	{
		OptionModelContainer optionModelContainer = getOptionModelContainerThatContainsPropertyName(name);
		if (optionModelContainer != null && optionModelContainer.hasPropertyByName(name)) {
			optionModelContainer.setPropertyValueByName(name, value);
			return true;
		} else {
			return false;
		}
	}
	
	
	public Object getPropertyByName(String name) throws ModelContainerException
	{
		OptionModelContainer optionModelContainer = getOptionModelContainerThatContainsPropertyName(name);
		if (optionModelContainer != null && optionModelContainer.hasPropertyByName(name)) {
			return optionModelContainer.getPropertyValueByName(name);
		} else {
			return null;
		}
	}
	
	
	public ModelProcessManifest copy() throws ProcessContainerException
	{

		ModelProcessManifest copy = null;
		
		
		copy = new ModelProcessManifest((GlobalOptionModel)null);
		try {
			copy.globalOptionModelContainer = new OptionModelContainer(this.globalOptionModelContainer.getOptionModel().copy());
		} catch (InvalidProcessOptionException ex) {
			throw new ProcessContainerException("Error creating copy of option model container: " + ex.getMessage(), ex);
		}
		
		
		for (ModelProcessContainer processContainer : this.workerList) {
			copy.addProcessContainer(new ModelProcessContainer(processContainer.getGridWorker(), processContainer.getOptionModel()));
		}
		
		return copy;
		
	}
	
	
}
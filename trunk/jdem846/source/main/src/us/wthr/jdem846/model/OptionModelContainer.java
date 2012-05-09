package us.wthr.jdem846.model;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.model.annotations.ProcessOption;
import us.wthr.jdem846.model.exceptions.InvalidProcessOptionException;
import us.wthr.jdem846.model.exceptions.MethodContainerInvokeException;
import us.wthr.jdem846.model.exceptions.ModelContainerException;

public class OptionModelContainer
{
	private static Log log = Logging.getLog(OptionModelContainer.class);
	
	
	private OptionModel optionModel;
	
	private OptionModelChangeListener internalPropertyChangeListener;
	private List<OptionModelChangeListener> propertyChangeListeners = new LinkedList<OptionModelChangeListener>();
	
	private Map<String, Method> allMethods = new HashMap<String, Method>();
	private Map<String, Method> annotatedMethods = new HashMap<String, Method>();
	private Map<String, OptionModelMethodContainer> getterSetterMethods = new HashMap<String, OptionModelMethodContainer>();
	
	private Map<String, OptionModelPropertyContainer> propertyContainerMap = new HashMap<String, OptionModelPropertyContainer>();
	private List<OptionModelPropertyContainer> propertyContainerList = new LinkedList<OptionModelPropertyContainer>();
	
	public OptionModelContainer(OptionModel optionModel) throws InvalidProcessOptionException
	{
		this.optionModel = optionModel;
		
		log.info("Loading option model for " + optionModel.getClass().getName());
		
		internalPropertyChangeListener = new OptionModelChangeListener() {
			public void onPropertyChanged(OptionModelChangeEvent e)
			{
				fireOptionModelChangeListeners(e);
			}
		};
		
		getAllMethods();
		getAnnotatedMethods();
		getGetterSetterMethods();
		getPropertyContainers();
	}
	
	
	public OptionModel getOptionModel()
	{
		return optionModel;
	}
	
	private void getAllMethods()
	{

		for (Method method : optionModel.getClass().getMethods()) {
			allMethods.put(method.getName(), method);
		}
		
	}
	
	private void getAnnotatedMethods()
	{

		for (Method method : optionModel.getClass().getMethods()) {
			if (method.isAnnotationPresent(ProcessOption.class)) {
				annotatedMethods.put(method.getName(), method);
			}
		}

	}
	
	
	private void getGetterSetterMethods() throws InvalidProcessOptionException
	{

		for (Method method : optionModel.getClass().getMethods()) {
			
			boolean isValid = false;
			try {
				isValid = OptionModelMethodContainer.validateAsGetterOrSetter(method);
			} catch (Exception ex) {
				
			}

			if (isValid) {
				OptionModelMethodContainer container = new OptionModelMethodContainer(optionModel, method);
				getterSetterMethods.put(method.getName(), container);
			}

		}

	}
	
	private void getPropertyContainers() throws InvalidProcessOptionException
	{

		for (Method method : optionModel.getClass().getMethods()) {
			if (method.isAnnotationPresent(ProcessOption.class)) {
				OptionModelMethodContainer m0 = new OptionModelMethodContainer(optionModel, method);
				String propertyName = OptionModelMethodContainer.determinePropertyName(method.getName());
				
				Method other = getMethodByPropertyName(propertyName, method);
				OptionModelMethodContainer m1 = new OptionModelMethodContainer(optionModel, other);

				OptionModelPropertyContainer propertyContainer = new OptionModelPropertyContainer(m0, m1);
				propertyContainer.addOptionModelChangeListener(internalPropertyChangeListener);
				
				propertyContainerMap.put(propertyName, propertyContainer);
				propertyContainerList.add(propertyContainer);
				
			}
		}
		
		sortPropertyContainerList(propertyContainerList);

	}
	
	private void sortPropertyContainerList(List<OptionModelPropertyContainer> propertyContainerList)
	{
		
		OptionModelPropertyContainer[] containerArray = new OptionModelPropertyContainer[propertyContainerList.size()];
		propertyContainerList.toArray(containerArray);
		Arrays.sort(containerArray, new Comparator<OptionModelPropertyContainer>() {
			public int compare(OptionModelPropertyContainer o1, OptionModelPropertyContainer o2)
			{
				if (o1.getOrder() < o2.getOrder()) {
					return -1;
				} else if (o1.getOrder() == o2.getOrder()) {
					return 0;
				} else {
					return 1;
				}
			}
			
		});
		
		propertyContainerList.clear();
		
		for (OptionModelPropertyContainer propertyContainer : containerArray) {
			propertyContainerList.add(propertyContainer);
		}
		
	}
	
	
	private Method getMethodByPropertyName(String findPropertyName, Method doesntEqual)
	{
		Method findMethod = null;
		
		for (Method method : optionModel.getClass().getMethods()) {
			String methodPropertyName = OptionModelMethodContainer.determinePropertyName(method.getName());
			
			if (findPropertyName.equals(methodPropertyName) && !method.equals(doesntEqual)) {
				findMethod = method;
				break;
			}
			
		}
		
		return findMethod;
	}
	
	
	public OptionModelMethodContainer getMethodByName(String methodName)
	{
		return this.getterSetterMethods.get(methodName);
	}
	
	public OptionModelMethodContainer getGetMethodByPropertyName(String propertyName)
	{
		for (OptionModelMethodContainer method : this.getterSetterMethods.values()) {
			if (method.isGetter() && method.getPropertyName().equals(propertyName)) {
				return method;
			}
		}
		
		return null;
	}
	
	public OptionModelMethodContainer getSetMethodByPropertyName(String propertyName)
	{
		for (OptionModelMethodContainer method : this.getterSetterMethods.values()) {
			if (method.isSetter() && method.getPropertyName().equals(propertyName)) {
				return method;
			}
		}
		
		return null;
	}
	
	public String getPropertyNameById(String id)
	{
		for (OptionModelMethodContainer method : this.getterSetterMethods.values()) {
			if (method.hasAnnotation() && method.getId().equals(id)) {
				return method.getPropertyName();
			}
		}
		
		return null;
	}
	
	public OptionModelMethodContainer getGetMethodById(String id)
	{
		String propertyName = getPropertyNameById(id);
		if (propertyName == null) {
			return null;
		}
		
		return getGetMethodByPropertyName(propertyName);
	}
	
	public OptionModelMethodContainer getSetMethodById(String id)
	{
		String propertyName = getPropertyNameById(id);
		if (propertyName == null) {
			return null;
		}
		
		return getSetMethodByPropertyName(propertyName);
	}
	
	
	public OptionModelPropertyContainer getPropertyByName(String propertyName)
	{
		return this.propertyContainerMap.get(propertyName);
	}
	
	public OptionModelPropertyContainer getPropertyById(String id)
	{
		String propertyName = getPropertyNameById(id);
		
		if (propertyName == null) {
			return null;
		}
		
		return getPropertyByName(propertyName);
	}
	
	
	public int getPropertyCount()
	{
		return this.propertyContainerList.size();
	}
	
	public List<String> getPropertyNames()
	{
		List<String> propertyNames = new LinkedList<String>();
		for (String propertyName : propertyContainerMap.keySet()) {
			propertyNames.add(propertyName);
		}

		return propertyNames;
	}
	
	public List<OptionModelPropertyContainer> getProperties()
	{
		List<OptionModelPropertyContainer> propertyList = new LinkedList<OptionModelPropertyContainer>();
		
		for (OptionModelPropertyContainer property : this.propertyContainerList) {
			propertyList.add(property);
		}
		
		return propertyList;
	}
	
	public boolean hasPropertyByName(String propertyName)
	{
		if (getPropertyByName(propertyName) != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean hasPropertyById(String propertyId)
	{
		if (getPropertyById(propertyId) != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setPropertyValueByName(String propertyName, Object value) throws ModelContainerException
	{
		OptionModelPropertyContainer propertyContainer = getPropertyByName(propertyName);
		if (propertyContainer != null) {
			try {
				propertyContainer.setValue(value);
			} catch (MethodContainerInvokeException ex) {
				throw new ModelContainerException("Exception setting property value: " + ex.getMessage(), ex);
			}
		} else {
			throw new ModelContainerException("Property not found with name '" + propertyName + "'");
		}
	
	}
	
	public Object getPropertyValueByName(String propertyName) throws ModelContainerException
	{
		OptionModelPropertyContainer propertyContainer = getPropertyByName(propertyName);
		if (propertyContainer != null) {
			try {
				return propertyContainer.getValue();
			} catch (MethodContainerInvokeException ex) {
				throw new ModelContainerException("Exception getting property value: " + ex.getMessage(), ex);
			}
		} else {
			throw new ModelContainerException("Property not found with name '" + propertyName + "'");
		}
	
	}
	
	
	public void setPropertyValueById(String propertyId, Object value) throws ModelContainerException
	{
		OptionModelPropertyContainer propertyContainer = getPropertyById(propertyId);
		if (propertyContainer != null) {
			try {
				propertyContainer.setValue(value);
			} catch (MethodContainerInvokeException ex) {
				throw new ModelContainerException("Exception setting property value: " + ex.getMessage(), ex);
			}
		} else {
			throw new ModelContainerException("Property not found with id '" + propertyId + "'");
		}
	
	}
	
	public Object getPropertyValueById(String propertyId) throws ModelContainerException
	{
		OptionModelPropertyContainer propertyContainer = getPropertyById(propertyId);
		if (propertyContainer != null) {
			try {
				return propertyContainer.getValue();
			} catch (MethodContainerInvokeException ex) {
				throw new ModelContainerException("Exception getting property value: " + ex.getMessage(), ex);
			}
		} else {
			throw new ModelContainerException("Property not found with id '" + propertyId + "'");
		}
	
	}
	
	
	public void fireOptionModelChangeListeners(OptionModelChangeEvent e)
	{
		
		for (OptionModelChangeListener listener : this.propertyChangeListeners) {
			listener.onPropertyChanged(e);
		}
		
	}
	
	public void addOptionModelChangeListener(OptionModelChangeListener listener)
	{
		this.propertyChangeListeners.add(listener);
	}
	
	public boolean removeOptionModelChangeListener(OptionModelChangeListener listener)
	{
		return this.propertyChangeListeners.remove(listener);
	}
	
}
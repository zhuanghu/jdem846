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

package us.wthr.jdem846.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTabbedPane;

import us.wthr.jdem846.exception.ComponentException;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;


/** Implements a tabbed pane with the first tab being the project form and cannot be closed. The
 * other tabs are output images within a OutputImageViewer component panel.
 * 
 * @author Gill Family
 *
 */
@SuppressWarnings("serial")
public class TabPane extends JTabbedPane implements Disposable
{
	private static Log log = Logging.getLog(TabPane.class);
	
	public TabPane()
	{

	}
	
	@Override
	public void addTab(String title, Component component)
	{
		addTab(title, component, false);
	}
	
	public void addTab(String title, Component component, boolean closable)
	{
		super.addTab(title, component);
		ClosableTab tab = new ClosableTab(title, closable);

		tab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Component comp = (Component) e.getSource();
				int index = indexOfTabComponent(comp);
				removeTabAt(index);
				
				
			}
		});
		
		this.setTabComponentAt(this.getTabCount() - 1, tab);

	}
	
	
	
	
	@Override
	public void removeTabAt(int index)
	{
		if (index >= 0) {
			Component tabComponent = getComponentAt(index);
			
			log.info("Closing tab of type " + tabComponent.getClass().getCanonicalName());
			
			if (tabComponent instanceof Disposable) {
				Disposable disposable = (Disposable) tabComponent;
				try {
					disposable.dispose();
				} catch (ComponentException e1) {
					log.warn("Failed to dispose Tab pane: " + e1.getMessage(), e1);
					e1.printStackTrace();
				}
			}
		}
		super.removeTabAt(index);
	}

	@Override
	public void setTitleAt(int index, String title)
	{
		ClosableTab tab = (ClosableTab) this.getTabComponentAt(index);
		tab.setTitle(title);
	}

	@Override
	public void dispose() throws ComponentException
	{
		log.info("TabPane.dispose()");
		
		while(this.getTabCount() > 0) {
			removeTabAt(0);
		}
		
		
		/*
		Component components[] = getComponents();
		for (Component component : components) {
			if (component instanceof Disposable) {
				Disposable disposableComponent = (Disposable) component;
				try {
					disposableComponent.dispose();
				} catch (ComponentException ex) {
					log.warn("Failed to dispose of component", ex);
					ex.printStackTrace();
				}
			}
		}
		*/
	}
	

	
	
}

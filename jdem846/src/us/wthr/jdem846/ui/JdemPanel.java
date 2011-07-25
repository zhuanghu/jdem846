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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class JdemPanel extends JPanel
{
	private String title;
	
	public JdemPanel()
	{
		this.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent e) { }
			public void componentMoved(ComponentEvent e) { }
			public void componentResized(ComponentEvent e) { }
			public void componentShown(ComponentEvent e)
			{
				JdemFrame.getInstance().setTitle(title);
			}
		});
	}
	
	public JdemPanel(String title)
	{
		this();
		this.title = title;
		
	}
	
	public abstract void cleanUp();

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}
	
	
}

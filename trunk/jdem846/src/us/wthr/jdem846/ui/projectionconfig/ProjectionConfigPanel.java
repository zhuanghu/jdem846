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

package us.wthr.jdem846.ui.projectionconfig;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import us.wthr.jdem846.ModelOptions;
import us.wthr.jdem846.input.DataPackage;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;

@SuppressWarnings("serial")
public class ProjectionConfigPanel extends JPanel
{
	private static Log log = Logging.getLog(ProjectionConfigPanel.class);
	
	private ProjectionPreview projectionPreview;
	private JLabel jlblRotateAngles;
	
	private JSlider jsldRotateX;
	private JSlider jsldRotateY;
	
	private List<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	
	int lastX = -1;
	int lastY = -1;
	
	private boolean ignoreChanges = false;
	
	public ProjectionConfigPanel()
	{
		jlblRotateAngles = new JLabel("");
		projectionPreview = new ProjectionPreview(new Dimension(250, 250));
		
		
		jsldRotateX = new JSlider(0, 180, 30);
		jsldRotateY = new JSlider(-180, 180, 0);
		
		jsldRotateX.setOrientation(JSlider.VERTICAL);
		
		MouseAdapter mouseAdapter = new MouseAdapter() {
			public void mouseDragged(MouseEvent e)
			{
				onMouseDragged(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				lastX = -1;
				lastY = -1;
				fireChangeListeners();
			}
		};
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
		
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (ignoreChanges)
					return;
				
				JSlider source = (JSlider)e.getSource();
				syncPreviewToInputs();
				if (!source.getValueIsAdjusting()) {
					
					fireChangeListeners();
				}
			}
		};
		jsldRotateX.addChangeListener(changeListener);
		jsldRotateY.addChangeListener(changeListener);
		
		setLayout(new BorderLayout());
		add(projectionPreview, BorderLayout.CENTER);
		add(jlblRotateAngles, BorderLayout.SOUTH);
		add(jsldRotateX, BorderLayout.EAST);
		add(jsldRotateY, BorderLayout.NORTH);
		
		setRotation(0, 0, 0);
	}
	
	
	protected void syncPreviewToInputs()
	{
		setRotation(jsldRotateX.getValue(), jsldRotateY.getValue(), 0, false);
	}
	
	protected void onMouseDragged(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();
		
		if (lastX != -1 && lastY != -1) {
			
			int deltaX = x - lastX;
			int deltaY = y - lastY;
			
			//log.info("X: " + x + ", Y: " + y + ", deltaX: " + deltaX + ", deltaY: " + deltaY);
		
			double rotateX = projectionPreview.getRotateX();
			double rotateY = projectionPreview.getRotateY();
			double rotateZ = projectionPreview.getRotateZ();
			
			rotateX += (deltaY * 2);
			if (rotateX < 0)
				rotateX = 0;
			if (rotateX > 180)
				rotateX = 180;
			
			rotateY += (deltaX * 2);
			if (rotateY < -180)
				rotateY = -180;
			if (rotateY > 180)
				rotateY = 180;
			
			this.setRotation(rotateX, rotateY, rotateZ);
		}
		
		lastX = x;
		lastY = y;
		
	}
	
	
	
	public void setDimensions(Dimension d)
	{
		projectionPreview.setDimension(d);
	}
	
	public void setRotation(double x, double y, double z)
	{
		setRotation(x, y, z, true);
	}
	
	/**
	 * Set protected as other components have no business calling this
	 * with updateControls as false.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param updateControls
	 */
	protected void setRotation(double x, double y, double z, boolean updateControls)
	{
		if (updateControls) {
			jsldRotateX.setValue((int)x);
			jsldRotateY.setValue((int)y);
		}
		jlblRotateAngles.setText("Rotation X/Y: " + x + ", " + y);
		projectionPreview.setRotateX(x);
		projectionPreview.setRotateY(y);
		projectionPreview.setRotateZ(z);
		projectionPreview.repaint();
	}
	
	
	public double getRotateX()
	{
		return projectionPreview.getRotateX();
	}
	
	public double getRotateY()
	{
		return projectionPreview.getRotateY();
	}
	
	public double getRotateZ()
	{
		return projectionPreview.getRotateZ();
	}
	
	public void fireChangeListeners()
	{
		ChangeEvent event = new ChangeEvent(this);
		for (ChangeListener listener : changeListeners) {
			listener.stateChanged(event);
		}
	}
	
	public void addChangeListener(ChangeListener listener)
	{
		changeListeners.add(listener);
	}
	
	public boolean removeChangeListener(ChangeListener listener)
	{
		return changeListeners.remove(listener);
	}
	
	
}

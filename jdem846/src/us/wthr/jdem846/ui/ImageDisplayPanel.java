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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class ImageDisplayPanel extends JPanel
{
	
	
	private JLabel displayLabel;
	private JScrollPane scrollPane;
	
	private Image trueImage;
	private int imageTrueWidth = -1;
	private int imageTrueHeight = -1;
	private double scalePercent = 1.0;
	
	private int scaleQuality = Image.SCALE_DEFAULT;
	
	private int lastDragMouseX = -1;
	private int lastDragMouseY = -1;
	
	private List<MousePositionListener> mousePositionListeners = new LinkedList<MousePositionListener>();
	
	
	public ImageDisplayPanel()
	{
		// Set Properties
		setLayout(new BorderLayout());
		
		// Create components
		displayLabel = new JLabel();
		scrollPane = new JScrollPane(displayLabel);
		
		// Add listeners
		displayLabel.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				onMouseDragged(e.getX(), e.getY());
			}
			public void mouseMoved(MouseEvent e) {
				onMouseMoved(e.getX(), e.getY());
			}
		});
		
		displayLabel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				
			}
			public void mouseEntered(MouseEvent e) {
				
			}
			public void mouseExited(MouseEvent e) {
				
			}
			public void mousePressed(MouseEvent e) {
				lastDragMouseX = e.getX();
				lastDragMouseY = e.getY();
			}
			public void mouseReleased(MouseEvent e) {
				lastDragMouseX = -1;
				lastDragMouseY = -1;
			}
		});
		
		displayLabel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				onMouseWheelMoved(e.getUnitsToScroll(), e.getScrollAmount(), e.getScrollType(), e.getX(), e.getY());
			}
		});
		
		
		// Set Layout
		add(scrollPane, BorderLayout.CENTER);
	}
	
	
	
	public int getScaleQuality()
	{
		return scaleQuality;
	}



	public void setScaleQuality(int scaleQuality) 
	{
		this.scaleQuality = scaleQuality;
	}



	public double getScalePercent() 
	{
		return scalePercent;
	}



	public void setScalePercent(double scalePercent)
	{
		this.scalePercent = scalePercent;
	}



	public void setImage(Image image)
	{
		trueImage = image;
		imageTrueWidth = image.getWidth(this);
		imageTrueHeight = image.getHeight(this);
	}
	
	protected void onMouseWheelMoved(int units, int amount, int type, int x, int y)
	{
		zoom(units);
		/*
		scalePercent += (((double)units / 100.0) * -1);
		if (scalePercent > 1.0)
			scalePercent = 1.0;
		if (scalePercent < 0)
			scalePercent = 0;
		
		repaint();
		*/
	}
	
	protected void onMouseDragged(int x, int y)
	{
		int horizMin = scrollPane.getHorizontalScrollBar().getMinimum();
		int horizMax = scrollPane.getHorizontalScrollBar().getMaximum();
		
		int vertMin = scrollPane.getVerticalScrollBar().getMinimum();
		int vertMax = scrollPane.getVerticalScrollBar().getMaximum();
		
		int horizStep = -1 * (x - lastDragMouseX);
		int vertStep = -1 * (y - lastDragMouseY);
		
		int horizPos = scrollPane.getHorizontalScrollBar().getValue();
		int vertPos = scrollPane.getVerticalScrollBar().getValue();
		
		horizPos += horizStep;
		if (horizPos < horizMin)
			horizPos = horizMin;
		if (horizPos > horizMax)
			horizPos = horizMax;
		
		vertPos += vertStep;
		if (vertPos < vertMin)
			vertPos = vertMin;
		if (vertPos > vertMax)
			vertPos = vertMax;

		scrollPane.getHorizontalScrollBar().setValue(horizPos);
		scrollPane.getVerticalScrollBar().setValue(vertPos);

		lastDragMouseX = x + horizStep;
		lastDragMouseY = y + vertStep;
	}
	
	protected void onMouseMoved(int x, int y)
	{
		fireMousePositionListeners(x, y);
	}
	
	@Override
	public void paint(Graphics g)
	{
		if (trueImage != null) {
		
			if (scalePercent > 1.0)
				scalePercent = 1.0;
			
			// Not perfect
			Dimension viewSize = scrollPane.getViewport().getExtentSize();

			double minimumScalePercent = this.getZoomToFitScalePercentage();

			if (scalePercent < minimumScalePercent)
				scalePercent = minimumScalePercent;
			
			
			int scaleToWidth = (int) Math.floor((double)imageTrueWidth * (double) scalePercent);
			int scaleToHeight = (int) Math.floor((double)imageTrueHeight * (double) scalePercent);
			
			Image scaled = trueImage.getScaledInstance(scaleToWidth, scaleToHeight, scaleQuality);
			
			displayLabel.setIcon(new ImageIcon(scaled));
			displayLabel.setSize(scaleToWidth, scaleToHeight);
			displayLabel.setAlignmentX(CENTER_ALIGNMENT);
			displayLabel.setAlignmentY(CENTER_ALIGNMENT);
			displayLabel.setHorizontalAlignment(JLabel.CENTER);
			displayLabel.setVerticalAlignment(JLabel.CENTER);
			super.paint(g);
		} else {
			g.setColor(Color.black);
			g.fillRect(0, 0, getWidth(), getHeight());
			
		}
		
		
	}
	
	protected double getZoomToFitScalePercentage()
	{
		double imageWidth = trueImage.getWidth(this);
		double imageHeight = trueImage.getHeight(this);
		
		double panelWidth = getWidth();
		double panelHeight = getHeight();
		
		double scaleWidth = 0;
		double scaleHeight = 0;
		
		double scale = Math.max(panelHeight/imageHeight, panelWidth/imageWidth);
		scaleHeight = imageHeight * scale;
		scaleWidth = imageWidth * scale;
		
		
		if (scaleHeight > panelHeight) {
			scale = panelHeight/scaleHeight;
		    scaleHeight = scaleHeight * scale;
			scaleWidth = scaleWidth * scale;
		}
		if (scaleWidth > panelWidth) {
		    scale = panelWidth/scaleWidth;
		    scaleHeight = scaleHeight * scale;
			scaleWidth = scaleWidth * scale;
		}
		
		
		return (scaleWidth / imageWidth);
	}
	
	public void zoom(double units)
	{
		scalePercent += ((units / 100.0) * -1);
		if (scalePercent > 1.0)
			scalePercent = 1.0;
		if (scalePercent < 0)
			scalePercent = 0;
		
		repaint();
	}
	
	public void zoomIn()
	{
		zoom(-3);
	}
	
	public void zoomOut()
	{
		zoom(3);
	}
	
	public void zoomFit()
	{
		zoom(100);
		//scalePercent = getZoomToFitScalePercentage();
		//repaint();
	}
	
	public void zoomActual()
	{
		zoom(-100);
	}
	
	public void addMousePositionListener(MousePositionListener listener)
	{
		mousePositionListeners.add(listener);
	}
	
	public void removeMousePositionListener(MousePositionListener listener)
	{
		mousePositionListeners.remove(listener);
	}
	
	protected void fireMousePositionListeners(int x, int y)
	{
		for (MousePositionListener listener : mousePositionListeners) {
			listener.onMousePositionChanged(x, y, scalePercent);
		}
	}
	
	
	public interface MousePositionListener
	{
		public void onMousePositionChanged(int x, int y, double scaledPercent);
	}
	
}

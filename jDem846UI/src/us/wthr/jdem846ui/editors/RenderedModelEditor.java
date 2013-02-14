package us.wthr.jdem846ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import us.wthr.jdem846.ElevationModel;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846ui.View;
import us.wthr.jdem846ui.actions.ExportModelAction;
import us.wthr.jdem846ui.observers.ModelPreviewChangeObserver;

public class RenderedModelEditor extends EditorPart
{
	public static final String ID = "us.wthr.jdem846ui.editors.RenderedModelEditor";
	
	private static Log log = Logging.getLog(RenderedModelEditor.class);

	private ElevationModel elevationModel;
	private Canvas canvas;
	
	private Composite parent;
	
	private Long imageMutex = new Long(0);
	private Image image;
	
	private ExportModelAction exportModelAction;
	
	private TabFolder tabFolder;
	private RenderedModelPropertiesContainer propertiesContainer;
	
	@Override
	public void doSave(IProgressMonitor arg0)
	{
		
	}

	@Override
	public void doSaveAs()
	{
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		setSite(site);
		setInput(input);
		
		this.setTitle(input.getName());
	}

	@Override
	public boolean isDirty()
	{
		return false;
	}

	@Override
	public boolean isSaveAsAllowed()
	{

		return false;
	}

	@Override
	public void createPartControl(Composite parent)
	{
		this.parent = parent;
		
		tabFolder = new TabFolder (parent, SWT.TOP);
		
		TabItem displayTabItem = new TabItem(tabFolder, SWT.NONE);
		displayTabItem.setText("Model");
		
		createDisplayControls(tabFolder);
		displayTabItem.setControl(canvas);
		
		TabItem propertiesTabItem = new TabItem(tabFolder, SWT.NONE);
		propertiesTabItem.setText("Properties");
		this.propertiesContainer = new RenderedModelPropertiesContainer(tabFolder, SWT.NONE);
		propertiesTabItem.setControl(propertiesContainer);
		
		ElevationModelEditorInput editorInput = (ElevationModelEditorInput) this.getEditorInput();
		if (editorInput.getElevationModel() != null) {
			this.setElevationModel(editorInput.getElevationModel());
		}
	}
	
	protected void createDisplayControls(Composite parent)
	{
		
		log.info("Creating parts for elevation model display editor");
		
		exportModelAction = new ExportModelAction("Export...", View.ID);
		IActionBars actionBars = getEditorSite().getActionBars();
		IMenuManager dropDownMenu = actionBars.getMenuManager();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		dropDownMenu.add(exportModelAction);
		toolBar.add(exportModelAction);
		
		canvas = new Canvas(parent, SWT.NONE);
		canvas.setBackground(new Color(parent.getDisplay(), 0xFF, 0xFF, 0xFF));
		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				
				synchronized(imageMutex) {
					if (image != null) {
						
						double scalePercent = getZoomToFitScalePercentage();
						
						int scaleToWidth = (int) Math.floor((double)image.getImageData().width * (double) scalePercent);
						int scaleToHeight = (int) Math.floor((double)image.getImageData().height * (double) scalePercent);
						
						int x = (int) ((canvas.getClientArea().width / 2.0) - (scaleToWidth / 2.0)) + 0;
						int y = (int) ((canvas.getClientArea().height / 2.0) - (scaleToHeight / 2.0)) + 0;
						
						Image scaledImage = getScaledImage(image, scalePercent);
						
						e.gc.drawImage(scaledImage, x, y);
					}
				}
				
			}
			
		});
		
		
		
		canvas.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(ControlEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void controlResized(ControlEvent e) {

				int previewHeight = canvas.getClientArea().height;
				int previewWidth = canvas.getClientArea().width;
				ModelPreviewChangeObserver.getInstance().setPreviewHeight(previewHeight);
				ModelPreviewChangeObserver.getInstance().setPreviewWidth(previewWidth);
				
				if (previewHeight > 0 && previewWidth > 0) {
					ModelPreviewChangeObserver.getInstance().update(false, false);
				}
			}
			
		});
		
		
		
	
	}
	
	

	protected double getZoomToFitScalePercentage()
	{
		if (image == null) {
			return 0.0;
		}
		
		double imageWidth = image.getImageData().width;
		double imageHeight = image.getImageData().height;
		
		double panelWidth = canvas.getClientArea().width;
		double panelHeight = canvas.getClientArea().height;
		
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
	
	
	protected Image getScaledImage(Image image, double scalePercent)
	{
		int width = image.getBounds().width;
	    int height = image.getBounds().height;
	    
	    Image scaled = new Image(canvas.getDisplay(), image.getImageData().scaledTo((int)(width*scalePercent),(int)(height*scalePercent)));
	    return scaled;
	}

	
	public void setElevationModel(ElevationModel elevationModel)
	{
		this.elevationModel = elevationModel;
		if (elevationModel == null) {
			return;
		}
		
		if (!elevationModel.isLoaded()) {
			try {
				elevationModel.load();
			} catch (Exception ex) {
				log.error("Error loading elevation model: " + ex.getMessage(), ex);
				return;
			} 
		}
		
		this.propertiesContainer.setElevationModel(elevationModel);
		
		synchronized(imageMutex) {
			
			int width = (elevationModel != null) ? elevationModel.getWidth() : canvas.getClientArea().width;
			int height = (elevationModel != null) ? elevationModel.getHeight() : canvas.getClientArea().height;
			
			if (width <= 0 || height <= 0)
				return;
			
			PaletteData palette = new PaletteData(0xFF000000, 0xFF0000 , 0xFF00);
			ImageData imageData = new ImageData(width, height, 32, palette);
			
			if (elevationModel != null) {

				for (int y = 0; y < elevationModel.getHeight(); y++) {
					for (int x = 0; x < elevationModel.getWidth(); x++) {
						int rgbaInt = elevationModel.getRgba(x, y);
						imageData.setPixel(x, y, rgbaInt);
					}
				}
				
			}
			
			image = new Image(canvas.getDisplay(), imageData);

			canvas.redraw();
		}
	}
	
	@Override
	public void setFocus()
	{
		if (canvas != null) {
			canvas.setFocus();
		}
	}

}
package us.wthr.jdem846ui.actions;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;

import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846ui.ICommandIds;
import us.wthr.jdem846ui.project.ProjectContext;
import us.wthr.jdem846ui.project.ProjectException;

public class AddDataAction extends BasicAction
{
	private static Log log = Logging.getLog(AddDataAction.class);
	
	public static final String[] GENERIC_FILE_TYPES = {"Data Files", "All Files (*)"};
	public static final String[] GENERIC_FILE_TYPES_WIN = {"Data Files", "All Files (*.*)"};
	
	public static final String[] GENERIC_FILE_EXTENSIONS = {"*.flt;*bil;*.png;*.jpg;*.shp", "*"};
	public static final String[] GENERIC_FILE_EXTENSIONS_WIN = {"*.flt;*bil;*.png;*.jpg;*.shp", "*.*"};
	
	public AddDataAction(IWorkbenchWindow window, String label, String viewId)
	{
		super(window, ICommandIds.CMD_ADD_DATA, viewId, label, "/icons/eclipse/data_add.gif");
	}

	@Override
	public void run() {
		
		openInputData();
		
		super.run();
	}
	
	
	protected void openInputData()
	{
		
		String dataFilePath = promptForFilePath();
		
		if (dataFilePath != null) {
			log.info("Opening file '" + dataFilePath + "'");
			loadFile(dataFilePath);
		} else {
			log.info("Not opening a file.");
		}
		
	}
	
	protected void loadFile(String filePath)
	{
		File file = new File(filePath);
		if (file.exists()) {
			
			String extension = file.getName().substring(file.getName().lastIndexOf(".")+1);
			
			if (extension != null && extension.equalsIgnoreCase("shp")) {
				
				try {
					ProjectContext.getInstance().addShapeDataset(file.getAbsolutePath(), null);
				} catch (ProjectException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				
			} else if (extension != null && (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png"))) {
				
				try {
					ProjectContext.getInstance().addImageryData(file.getAbsolutePath());
				} catch (ProjectException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				
			} else {
				
				try {
					ProjectContext.getInstance().addElevationDataset(file.getAbsolutePath());
				} catch (ProjectException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				
			}

		} else {
			// THROW!!!
		}
	}
	
	
	protected String promptForFilePath()
	{
		FileDialog dialog = new FileDialog (this.getWindow().getShell(), SWT.OPEN);
		String [] filterNames = StandardFileTypes.GENERIC_DATA_FILE_TYPES;
		String [] filterExtensions = StandardFileTypes.GENERIC_DATA_FILE_EXTENSIONS;
		String filterPath = "/";
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterNames = StandardFileTypes.GENERIC_DATA_FILE_TYPES_WIN;
			filterExtensions = StandardFileTypes.GENERIC_DATA_FILE_EXTENSIONS_WIN;
			filterPath = "c:\\";
		}
		dialog.setFilterNames (filterNames);
		dialog.setFilterExtensions (filterExtensions);
		dialog.setFilterPath (filterPath);
		return dialog.open();
	}
	
}
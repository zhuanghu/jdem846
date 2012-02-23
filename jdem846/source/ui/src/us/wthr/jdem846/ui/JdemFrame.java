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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import us.wthr.jdem846.JDem846Properties;
import us.wthr.jdem846.ModelContext;
import us.wthr.jdem846.ModelOptions;
import us.wthr.jdem846.dbase.ClassLoadException;
import us.wthr.jdem846.exception.ProjectParseException;
import us.wthr.jdem846.i18n.I18N;
import us.wthr.jdem846.image.ImageIcons;
//import us.wthr.jdem846.input.DataPackage;
//import us.wthr.jdem846.input.ElevationDataLoaderInstance;
import us.wthr.jdem846.rasterdata.RasterDataContext;
import us.wthr.jdem846.rasterdata.RasterDataProviderFactory;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.project.ProjectFiles;
import us.wthr.jdem846.project.ProjectTypeEnum;
import us.wthr.jdem846.project.XmlProjectFileReader;
import us.wthr.jdem846.project.XmlProjectFileWriter;
import us.wthr.jdem846.project.ProjectModel;
import us.wthr.jdem846.render.EngineInstance;
import us.wthr.jdem846.render.EngineRegistry;
import us.wthr.jdem846.render.RenderEngine;
import us.wthr.jdem846.ui.TopButtonBar.ButtonClickedListener;
import us.wthr.jdem846.ui.base.FileChooser;
import us.wthr.jdem846.ui.base.Frame;
import us.wthr.jdem846.ui.base.Label;
import us.wthr.jdem846.ui.base.Menu;
import us.wthr.jdem846.ui.base.MenuItem;
import us.wthr.jdem846.ui.base.TabPane;
import us.wthr.jdem846.ui.base.ToolBar;
import us.wthr.jdem846.ui.preferences.PreferencesDialog;
import us.wthr.jdem846.util.TempFiles;

@SuppressWarnings("serial")
public class JdemFrame extends Frame
{
	private static Log log = Logging.getLog(JdemFrame.class);

	private TabPane tabPane;
	private TopButtonBar topButtonBar;
	private MainMenuBar menuBar;
	private MainButtonBar mainButtonBar;
	private SharedStatusBar statusBar;
	
	private static JdemFrame instance = null;
	
	private JdemFrame()
	{
		

		// Set Properties
		this.setTitle(null);
		this.setSize(JDem846Properties.getIntProperty("us.wthr.jdem846.general.ui.windowWidth"), JDem846Properties.getIntProperty("us.wthr.jdem846.general.ui.windowHeight"));
		this.setLocationRelativeTo(null);
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		try {
			Image icon = ImageIcons.loadImage(JDem846Properties.getProperty("us.wthr.jdem846.icon"));
			this.setIconImage(icon);
		} catch (IOException e) {
			e.printStackTrace();
			log.warn("Failed to load icon: " + e.getMessage(), e);
		}
		
		// Create components
		statusBar = new SharedStatusBar();
		
		buildJMenuBar();
		
		mainButtonBar = MainButtonBar.getInstance();
		
		topButtonBar = new TopButtonBar();
		MainButtonBar.addToolBar(topButtonBar);
		
		
		tabPane = new TabPane();
		
		topButtonBar.add(Box.createHorizontalGlue());
		
		if (JDem846Properties.getBooleanProperty("us.wthr.jdem846.general.ui.jdemFrame.displayMemoryMonitor")) {
			MemoryMonitor memoryCurrentState = new MemoryMonitor(1000, true, true);
			memoryCurrentState.start();
			SharedStatusBar.addControl(memoryCurrentState);
			
			/*
			MemoryMonitor memoryTrend = new MemoryMonitor(1000, false, true);
			memoryTrend.start();
			SharedStatusBar.addControl(memoryTrend);
			*/
		}
		
		// Add listeners
		topButtonBar.addButtonClickedListener(new ButtonClickedListener() {
			public void onExitClicked() {
				exitApplication();
			}
			public void onNewProjectClicked(ProjectTypeEnum projectType) {
				newProject(projectType);
				//createNewStandardProject(null);
			}
			public void onSaveProjectClicked() {
				saveTab();
			}
			public void onSaveProjectAsClicked() {
				saveTabAs();
			}
			public void onOpenProjectClicked() {
				openProject();
			}
		});
		
		addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent event) { }
			public void windowClosing(WindowEvent event) 
			{ 
				log.info("JdemFrame.windowClosing()");
				exitApplication();
			}
			public void windowDeactivated(WindowEvent event) { }
			public void windowDeiconified(WindowEvent event) { }
			public void windowIconified(WindowEvent event) { }
			public void windowOpened(WindowEvent event) { }
			
			public void windowClosed(WindowEvent event)
			{ 
				log.info("JdemFrame.windowClosed()");

			}
		});
		this.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent e)
			{
				
			}
			public void componentMoved(ComponentEvent e)
			{
				
			}
			public void componentResized(ComponentEvent e)
			{
				JDem846Properties.setProperty("us.wthr.jdem846.general.ui.windowHeight", ""+getHeight());
				JDem846Properties.setProperty("us.wthr.jdem846.general.ui.windowWidth", ""+getWidth());
			}
			public void componentShown(ComponentEvent e)
			{
				
			}
		});
		
		
		this.setJMenuBar(menuBar);
		
		
		
		
		this.setLayout(new BorderLayout());
		this.add(mainButtonBar, BorderLayout.NORTH);
		//this.add(topButtonBar, BorderLayout.NORTH);
		this.add(tabPane, BorderLayout.CENTER);
		this.add(statusBar, BorderLayout.SOUTH);
		
		if (JDem846Properties.getBooleanProperty("us.wthr.jdem846.general.ui.displayLogViewPanel")) {
			log.info("Log viewer panel is enabled");
			LogViewerDialog logViewer = new LogViewerDialog();
			logViewer.setVisible(true);
		}
		
		this.setGlassPane(new WorkingGlassPane());
		
		
		
		TimerTask task = new TimerTask() {
			public void run()
			{
				String openFiles = System.getProperty("us.wthr.jdem846.ui.openFiles");
				if (openFiles != null) {
					String[] paths = openFiles.split(";");
					openFileList(paths);
				}
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, 500);
		
		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.ready"));
		
	}
	
	protected void openFileList(String[] paths)
	{
		for (String path : paths) {
			if (path != null && path.length() > 0) {
				openProject(path);
				//createNewStandardProject(path);
			}
		}
	}
	
	protected void buildJMenuBar()
	{
		Menu menu;
		MenuItem menuItem;
		
		menuBar = MainMenuBar.getInstance();

		// File menu
		ComponentMenu fileMenu = new ComponentMenu(this, I18N.get("us.wthr.jdem846.ui.menu.file"), KeyEvent.VK_F);
		menuBar.add(fileMenu);
		fileMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.file.new"), JDem846Properties.getProperty("us.wthr.jdem846.ui.project.new"), KeyEvent.VK_N, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				newProject(ProjectTypeEnum.STANDARD_PROJECT);
				//createNewStandardProject(null);
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)));
		
		fileMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.file.open"), JDem846Properties.getProperty("us.wthr.jdem846.ui.project.open"), KeyEvent.VK_O, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				openProject();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)));
		
		fileMenu.addSeparator();
		
		fileMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.file.save"), JDem846Properties.getProperty("us.wthr.jdem846.ui.project.save"), KeyEvent.VK_S, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				saveTab();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)));
		
		fileMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.file.saveAs"), JDem846Properties.getProperty("us.wthr.jdem846.ui.project.saveAs"), new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				saveTabAs();
			}
		}));
		
		fileMenu.addSeparator();
		
		fileMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.file.exit"), JDem846Properties.getProperty("us.wthr.jdem846.ui.exit"), KeyEvent.VK_X, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				exitApplication();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK)));

		// Edit Menu
		
		ComponentMenu editMenu = new ComponentMenu(this, I18N.get("us.wthr.jdem846.ui.menu.edit"), KeyEvent.VK_E);
		menuBar.add(editMenu);
		editMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.edit.preferences"), JDem846Properties.getProperty("us.wthr.jdem846.ui.edit.preferences"), KeyEvent.VK_P, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				onPreferences();
			}
		}));
		
		// Help Menu
		menuBar.add(Box.createHorizontalGlue());
		ComponentMenu helpMenu = new ComponentMenu(this, I18N.get("us.wthr.jdem846.ui.menu.help"), KeyEvent.VK_H);
		menuBar.add(helpMenu);
		helpMenu.add(new MenuItem(I18N.get("us.wthr.jdem846.ui.menu.help.about"), JDem846Properties.getProperty("us.wthr.jdem846.ui.help"), KeyEvent.VK_A, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				onAbout();
			}
		}));

		
		MainMenuBar.setInsertIndex(2);
	}
	
	public void onAbout()
	{
		AboutDialog about = new AboutDialog(this);
		about.setVisible(true);
	}
	
	public boolean exitApplication()
	{
		int response = JOptionPane.showConfirmDialog(this,
				I18N.get("us.wthr.jdem846.ui.jdemFrame.exitConfirm.message"), 
				I18N.get("us.wthr.jdem846.ui.jdemFrame.exitConfirm.title"), 
				JOptionPane.YES_NO_OPTION);
		
		// 0 = Yes
		// 1 = No
		
		if (response == JOptionPane.OK_OPTION) {
			
			log.info("Shutting down application");
			
			close();
			
			return true;
		} else {
			return false;
		}
	}
	
	public void saveTab()
	{
		Object tabObj = tabPane.getSelectedComponent();
		
		if (tabObj != null && tabObj instanceof Savable) {
			Savable savable = (Savable) tabObj;
			savable.save();
		} else {
			JOptionPane.showMessageDialog(getRootPane(),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.saveError.invalidTab.message"),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.saveError.invalidTab.title"),
				    JOptionPane.WARNING_MESSAGE);
		}
		
	}
	
	public void saveTabAs()
	{
		Object tabObj = tabPane.getSelectedComponent();
		
		if (tabObj != null && tabObj instanceof Savable) {
			Savable savable = (Savable) tabObj;
			savable.saveAs();
		} else {
			JOptionPane.showMessageDialog(getRootPane(),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.saveError.invalidTab.message"),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.saveError.invalidTab.title"),
				    JOptionPane.WARNING_MESSAGE);
		}
		
	}
	
	public void onPreferences()
	{
		
		PreferencesDialog preferencesDialog = new PreferencesDialog();
		preferencesDialog.setVisible(true);
		
	}
	
	
	public void setComponentTabTitle(int index, String title)
	{
		tabPane.setTitleAt(index, title);
		((JdemPanel)tabPane.getComponentAt(index)).setTitle(title);
	}
	
	public void openProject()
	{
		log.info("Displaying open project dialog");
		FileChooser chooser = new FileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(I18N.get("us.wthr.jdem846.ui.projectFormat.generic.name"), "zdem", "xdem");
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(false);
	    int returnVal = chooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	File selectedFile = chooser.getSelectedFile();
	    	openProject(selectedFile.getAbsolutePath());
	    	//createNewStandardProject(selectedFile.getAbsolutePath());
	    }
	}
	
	
	
	public void newProject(ProjectTypeEnum projectType)
	{
		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.loadingNew"));
		
		ProjectModel projectModel = new ProjectModel();
		projectModel.setProjectType(projectType);
		buildProjectUI(projectModel);
	}
	
	public void openProject(String filePath)
	{
		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.loadingPath") + " " + filePath);
		
		ProjectModel projectModel = null;

		try {
			projectModel = ProjectFiles.read(filePath);
		} catch (FileNotFoundException ex) {
			log.warn("Project file not found: " + ex.getMessage(), ex);
			JOptionPane.showMessageDialog(getRootPane(),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.fileNotFound.message"),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.fileNotFound.title"),
				    JOptionPane.ERROR_MESSAGE);
			return;
		} catch (IOException ex) {
			log.warn("IO error reading from disk: " + ex.getMessage(), ex);
			JOptionPane.showMessageDialog(getRootPane(),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.ioError.message"),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.ioError.title"),
				    JOptionPane.ERROR_MESSAGE);
			return;
		} catch (ProjectParseException ex) {
			log.warn("Error parsing project: " + ex.getMessage(), ex);
			JOptionPane.showMessageDialog(getRootPane(),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.parseError.message"),
				    I18N.get("us.wthr.jdem846.ui.jdemFrame.projectLoadError.parseError.title"),
				    JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		projectModel.setLoadedFrom(filePath);
		
		buildProjectUI(projectModel);
	}
	
	
	protected void buildProjectUI(ProjectModel projectModel)
	{
		if (projectModel.getProjectType() == ProjectTypeEnum.STANDARD_PROJECT) {
			buildStandardProjectUI(projectModel);
		} else if (projectModel.getProjectType() == ProjectTypeEnum.SCRIPT_PROJECT) {
			buildScriptProjectUI(projectModel);
		} else {
			log.warn("Invalid project type: " + projectModel.getProjectType());
			// TODO: Message Dialog
		}
	}
	
	
	protected void buildStandardProjectUI(ProjectModel projectModel)
	{
		
		DemProjectPane projectPane = new DemProjectPane(projectModel);
		
		projectPane.addCreateModelListener(new CreateModelListener() {
			public void onCreateModel(ModelContext modelContext) {
				onCreateModelTab(modelContext);
			}
		});
		
		String title = I18N.get("us.wthr.jdem846.ui.defaultProjectTitle");
		if (projectModel != null && projectModel.getLoadedFrom() != null) {
			File f = new File(projectModel.getLoadedFrom());
			title = f.getName();
			projectPane.setSavedPath(projectModel.getLoadedFrom());
		}
		
		tabPane.addTab(title, projectPane, true);
		tabPane.setSelectedComponent(projectPane);
		projectPane.setTitle(title);

		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.ready"));
		
	}
	
	protected void buildScriptProjectUI(ProjectModel projectModel)
	{
		/*
		JOptionPane.showMessageDialog(getRootPane(),
				I18N.get("us.wthr.jdem846.ui.notYetImplemented.message"),
			    I18N.get("us.wthr.jdem846.ui.notYetImplemented.title"),
			    JOptionPane.INFORMATION_MESSAGE);
		*/
		
		ScriptProjectPane projectPane = new ScriptProjectPane(projectModel);
		
		/*
		projectPane.addCreateModelListener(new CreateModelListener() {
			public void onCreateModel(ModelContext modelContext) {
				onCreateModelTab(modelContext);
			}
		});
		*/
		
		String title = I18N.get("us.wthr.jdem846.ui.defaultProjectTitle");
		if (projectModel != null && projectModel.getLoadedFrom() != null) {
			File f = new File(projectModel.getLoadedFrom());
			title = f.getName();
			projectPane.setSavedPath(projectModel.getLoadedFrom());
		}
		
		tabPane.addTab(title, projectPane, true);
		tabPane.setSelectedComponent(projectPane);
		projectPane.setTitle(title);

		SharedStatusBar.setStatus(I18N.get("us.wthr.jdem846.ui.jdemFrame.status.ready"));
		
	}

	
	@Override
	public void setTitle(String title)
	{
		String appTitle = JDem846Properties.getProperty("us.wthr.jdem846.ui.windowTitle");
		String wndTitle = "";
		
		if (title != null)
			wndTitle = title + " | " + appTitle;
		else
			wndTitle = appTitle;
		
		super.setTitle(wndTitle);
		
	}
	
	protected void onCreateModelTab(ModelContext modelContext)
	{
		
		
		
		String engineIdentifier = modelContext.getModelOptions().getEngine();
		EngineInstance engineInstance = EngineRegistry.getInstance(engineIdentifier);
		
		// TODO: Add scripting proxy
		//ModelContext modelContext = ModelContext.createInstance(dataPackage, modelOptions);
		
		RenderEngine engine;
		try {
			engine = engineInstance.getImpl();
			engine.initialize(modelContext);
		} catch (ClassLoadException ex) {
			ex.printStackTrace();
			log.error("Failed to load engine class '" + ex.getClassName() + "': " + ex.getMessage(), ex);
			
			JOptionPane.showMessageDialog(getRootPane(),
					I18N.get("us.wthr.jdem846.ui.jdemFrame.createModelTab.modelInstanceError.message") + " " + engineInstance.getName(),
					I18N.get("us.wthr.jdem846.ui.jdemFrame.createModelTab.modelInstanceError.title"),
				    JOptionPane.ERROR_MESSAGE);
			return;
		}
		//engine.setDataPackage(dataPackage);
		
		// TODO: Restore this functionality
		/*
		ElevationDataLoaderInstance dataLoaderInstance = engine.needsOutputFileOfType();
		if (dataLoaderInstance != null) {
			
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(dataLoaderInstance.getName(), dataLoaderInstance.getExtension());
			chooser.setFileFilter(filter);
			chooser.setMultiSelectionEnabled(false);
		    int returnVal = chooser.showSaveDialog(this);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	File selectedFile = chooser.getSelectedFile();
		    	modelContext.getModelOptions().setWriteTo(selectedFile.getAbsolutePath());
		    } else {
		    	JOptionPane.showMessageDialog(getRootPane(),
					    I18N.get("us.wthr.jdem846.ui.jdemFrame.createModelTab.fileNotSelected.message"),
					    I18N.get("us.wthr.jdem846.ui.jdemFrame.createModelTab.fileNotSelected.title"),
					    JOptionPane.ERROR_MESSAGE);
		    	return;
		    }
			

		}
		*/
		
		if (engine.generatesImage()) {
			OutputImageViewPanel outputPanel = new OutputImageViewPanel(engine);
			tabPane.addTab(I18N.get("us.wthr.jdem846.ui.jdemFrame.createModelTab.modelTabTitle"), outputPanel, true);
			tabPane.setSelectedComponent(outputPanel);
			outputPanel.setTitle(I18N.get("us.wthr.jdem846.ui.jdemFrame.createModelTab.modelTabTitle"));
			outputPanel.startWorker();
		} else {
			
			DataGenerationViewPanel outputPanel = new DataGenerationViewPanel(engine);
			tabPane.addTab(I18N.get("us.wthr.jdem846.ui.jdemFrame.createModelTab.dataTabTitle"), outputPanel, true);
			tabPane.setSelectedComponent(outputPanel);
			outputPanel.setTitle(I18N.get("us.wthr.jdem846.ui.jdemFrame.createModelTab.modelTabTitle"));
			outputPanel.startWorker();
		}

	}
	
	public void setGlassVisible(boolean visible)
	{
		WorkingGlassPane glassPane = (WorkingGlassPane) this.getGlassPane();
		glassPane.setVisible(visible);
	}
	
	public void setGlassVisible(String text, boolean visible)
	{
		WorkingGlassPane glassPane = (WorkingGlassPane) this.getGlassPane();
		glassPane.setVisible(visible);
		glassPane.setShadeComponent(null);
		glassPane.setText(text);
	}
	
	public void setGlassVisible(String text, Component component, boolean visible)
	{
		WorkingGlassPane glassPane = (WorkingGlassPane) this.getGlassPane();
		glassPane.setVisible(visible);
		glassPane.setShadeComponent(component);
		glassPane.setText(text);
	}
	
	
	public static JdemFrame getInstance()
	{
		if (JdemFrame.instance == null) {
			JdemFrame.instance = new JdemFrame();
		}
		return JdemFrame.instance;
	}

}

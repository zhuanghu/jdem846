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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import us.wthr.jdem846.DataSetTypes;
import us.wthr.jdem846.JDem846Properties;
import us.wthr.jdem846.ModelContext;
import us.wthr.jdem846.i18n.I18N;
import us.wthr.jdem846.image.ImageIcons;
import us.wthr.jdem846.image.SimpleGeoImage;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.rasterdata.RasterData;
import us.wthr.jdem846.shapefile.ShapeBase;
import us.wthr.jdem846.shapefile.exception.ShapeFileException;
import us.wthr.jdem846.ui.base.Panel;
import us.wthr.jdem846.ui.base.ScrollPane;
import us.wthr.jdem846.ui.base.Tree;

@SuppressWarnings("serial")
public class DataSetTree extends Panel
{
	private static Log log = Logging.getLog(DataSetTree.class);
	
	private Tree tree;
	private ScrollPane scrollPane;
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode(I18N.get("us.wthr.jdem846.ui.dataSetTree.node.datasets"));
	private DefaultTreeModel treeModel;
	
	private DefaultMutableTreeNode elevationNode;
	private DefaultMutableTreeNode shapeNode;
	private DefaultMutableTreeNode imageryNode;
	
	//private DataPackage dataPackage;
	private ModelContext modelContext;
	
	private Icon polygonIcon;
	private Icon elevationIcon;
	private Icon polylineIcon;
	private Icon orthoImageryIcon;
	private Icon noDataIcon;
	
	private Icon shapesCategoryIcon;
	private Icon elevationCategoryIcon;
	private Icon imageryCategoryIcon;
	
	private List<DatasetSelectionListener> datasetSelectionListeners = new LinkedList<DatasetSelectionListener>();
	
	public DataSetTree(ModelContext modelContext)
	{
		//this.dataPackage = dataPackage;
		this.modelContext = modelContext;
		
		// Load icons
    	try {
    		polygonIcon = ImageIcons.loadImageIcon(JDem846Properties.getProperty("us.wthr.jdem846.ui.dataSetTree.nodeIcon.polygon"));
    		elevationIcon = ImageIcons.loadImageIcon(JDem846Properties.getProperty("us.wthr.jdem846.ui.dataSetTree.nodeIcon.elevation"));
    		polylineIcon = ImageIcons.loadImageIcon(JDem846Properties.getProperty("us.wthr.jdem846.ui.dataSetTree.nodeIcon.polyline"));
    		orthoImageryIcon = ImageIcons.loadImageIcon(JDem846Properties.getProperty("us.wthr.jdem846.ui.dataSetTree.nodeIcon.imagery"));
    		
    		noDataIcon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
    		
    		elevationCategoryIcon = ImageIcons.loadImageIcon(JDem846Properties.getProperty("us.wthr.jdem846.ui.dataSetTree.categoryIcon.elevation"));
    		shapesCategoryIcon = ImageIcons.loadImageIcon(JDem846Properties.getProperty("us.wthr.jdem846.ui.dataSetTree.categoryIcon.shapes"));
    		imageryCategoryIcon = ImageIcons.loadImageIcon(JDem846Properties.getProperty("us.wthr.jdem846.ui.dataSetTree.categoryIcon.imagery"));
    	} catch (IOException ex) {
			ex.printStackTrace();
			log.warn("Failed to load image icon for tree node: " + ex.getMessage(), ex);
		}
		
		
		// Create components
    	
    	treeModel = new DefaultTreeModel(top);
    	treeModel.addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent e)
			{
				
			}
			public void treeNodesInserted(TreeModelEvent e)
			{
				
			}
			public void treeNodesRemoved(TreeModelEvent e)
			{
				
			}
			public void treeStructureChanged(TreeModelEvent e)
			{
				
			}
    	});
    	
		tree = new Tree(treeModel);
		scrollPane = new ScrollPane(tree);
		tree.setCellRenderer(new DatasetTreeCellRenderer());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		//tree.setRootVisible(false);
		
		elevationNode = new DatasetTreeNode(elevationCategoryIcon, I18N.get("us.wthr.jdem846.ui.dataSetTree.node.elevation"));
		shapeNode = new DatasetTreeNode(shapesCategoryIcon, I18N.get("us.wthr.jdem846.ui.dataSetTree.node.shapes"));
		imageryNode =  new DatasetTreeNode(imageryCategoryIcon, I18N.get("us.wthr.jdem846.ui.dataSetTree.node.imagery"));
		
		top.add(elevationNode);
		top.add(shapeNode);
		top.add(imageryNode);
		
		// Add Listeners

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			
			boolean selectionEnabled = true;
			
			public void valueChanged(TreeSelectionEvent e)
			{
				if (!selectionEnabled)
					return;
				
				if (e.getNewLeadSelectionPath() == null) {
					fireDatasetSelected(null, DataSetTypes.UNSUPPORTED, -1);
					return;
				}
				
				TreePath oldPath = e.getOldLeadSelectionPath();
				Object value = e.getPath().getLastPathComponent();
				if (value instanceof DatasetTreeNode) {
					DatasetTreeNode node = (DatasetTreeNode) value;
					if (node.getType() == DatasetTreeNode.TYPE_CATEGORY) {
						selectionEnabled = false;
						tree.setSelectionPath(oldPath);
						selectionEnabled = true;
					} else {
						fireDatasetSelected(node.getDataObject(), node.getType(), node.getIndex());
					}
					
				}
			
			}
		});

		// Set Layout
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		
		updateTreeNodes();
	}
	
	public void updateTreeNodes()
	{
		
		log.info("Updating tree nodes.");
		top.removeAllChildren();
		
		elevationNode.removeAllChildren();
		shapeNode.removeAllChildren();
		imageryNode.removeAllChildren();
		
		//if (modelContext.getRasterDataContext().getRasterDataListSize() > 0) {
			top.add(elevationNode);
		//}
		
		//if (modelContext.getShapeDataContext().getShapeDataListSize() > 0) {
			top.add(shapeNode);
		//}
		
		//if (modelContext.getImageDataContext().getImageListSize() > 0) {
			top.add(imageryNode);
		//}
		
		
		
		//elevationNode.removeFromParent();
		
		if (modelContext.getRasterDataContext() != null && modelContext.getRasterDataContext().getRasterDataListSize() > 0) {
			List<RasterData> rasterDataList = modelContext.getRasterDataContext().getRasterDataList();
			for (int i = 0; i < rasterDataList.size(); i++) {
				RasterData rasterData = rasterDataList.get(i);
				log.info("Adding elevation data: " + rasterData.getFilePath());
				elevationNode.add(new DatasetTreeNode(elevationIcon, rasterData, i));
			}
		} else {
			elevationNode.add(new DatasetTreeNode(noDataIcon, I18N.get("us.wthr.jdem846.ui.dataSetTree.node.noData")));
		}
		
		if (modelContext.getShapeDataContext() != null && modelContext.getShapeDataContext().getShapeDataListSize() > 0) {
			Set<ShapeBase> shapeBases = modelContext.getShapeDataContext().getShapeFiles();
			//Iterator<ShapeBase> iter = shapeBases.iterator();
			
			int i = 0;
			for (Iterator<ShapeBase> iter = shapeBases.iterator(); iter.hasNext(); ) {
				
				
				try {
					ShapeBase shapeBase = iter.next();
					
					Icon icon = null;
					
					if (shapeBase.getShapeType() == DataSetTypes.SHAPE_POLYGON)
						icon = polygonIcon;
					else if (shapeBase.getShapeType() == DataSetTypes.SHAPE_POLYLINE)
						icon = polylineIcon;
					
					shapeNode.add(new DatasetTreeNode(icon, shapeBase, i));
					i++;
				} catch (Exception ex) {
					// TODO Throw
					ex.printStackTrace();
				}
			}
		} else {
			shapeNode.add(new DatasetTreeNode(noDataIcon, I18N.get("us.wthr.jdem846.ui.dataSetTree.node.noData")));
		}
		
		
		if (modelContext.getImageDataContext() != null && modelContext.getImageDataContext().getImageListSize() > 0) {
			List<SimpleGeoImage> imageFiles = modelContext.getImageDataContext().getImageList();
			for (int i = 0; i < imageFiles.size(); i++) {
				SimpleGeoImage image = imageFiles.get(i);
				
				Icon icon = this.orthoImageryIcon;
				imageryNode.add(new DatasetTreeNode(icon, image, i));
				
			}
		} else {
			imageryNode.add(new DatasetTreeNode(noDataIcon, I18N.get("us.wthr.jdem846.ui.dataSetTree.node.noData")));
		}
		
		
		treeModel.reload();
		
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		tree.setRootVisible(false);
		
		
	}

	protected DatasetTreeNode getSelectedTreeNode()
	{
		if (tree.getSelectionPath() == null)
			return null;
		
		Object value = tree.getSelectionPath().getLastPathComponent();
		if (value instanceof DatasetTreeNode) {
			return (DatasetTreeNode) value;
		} else {
			return null;
		}
	}
	
	public int getSelectedDatasetIndex()
	{
		DatasetTreeNode node = getSelectedTreeNode();
		if (node == null || node.getType() == DatasetTreeNode.TYPE_CATEGORY) {
			return -1;
		} else {
			return node.getIndex();
		}
	}
	
	public int getSelectedDatasetType()
	{
		DatasetTreeNode node = getSelectedTreeNode();
		if (node == null || node.getType() == DatasetTreeNode.TYPE_CATEGORY) {
			return DataSetTypes.UNSUPPORTED;
		} else {
			return node.getType();
		}
	}
	
	class DatasetTreeNode extends DefaultMutableTreeNode
	{
		public static final int TYPE_ELEVATION = DataSetTypes.ELEVATION;
		public static final int TYPE_SHAPES_POLYGON = DataSetTypes.SHAPE_POLYGON;
		public static final int TYPE_SHAPES_POLYLINE = DataSetTypes.SHAPE_POLYLINE;
		public static final int TYPE_IMAGERY = DataSetTypes.IMAGERY;
		public static final int TYPE_CATEGORY = DataSetTypes.UNSUPPORTED;
		public static final int TYPE_NODATA = DataSetTypes.UNSUPPORTED;
		
		
		private int type = -1;
		private int index = -1;
		private Object dataObject;
		private Icon icon;
		
		public DatasetTreeNode(String label)
		{
			super(label);
			this.icon = null;
			this.type = TYPE_NODATA;
		}
		
		public DatasetTreeNode(Icon icon, String label)
		{
			super(label);
			this.icon = icon;
			this.type = TYPE_CATEGORY;
		}
		
		public DatasetTreeNode(Icon icon, RasterData rasterData, int index)
		{
			super((new File(rasterData.getFilePath())).getName());
			this.index = index;
			this.icon = icon;
			this.type = TYPE_ELEVATION;
		}
		
		
		
		public DatasetTreeNode(Icon icon, ShapeBase shapeBase, int index)
		{
			super((new File(shapeBase.getShapeFileReference().getPath()).getName()));
			this.index = index;
			this.icon = icon;
			try {
				this.type = shapeBase.getShapeType();
			} catch (ShapeFileException ex) {
				// TODO Throw
				ex.printStackTrace();
			}
		}
		
		public DatasetTreeNode(Icon icon, SimpleGeoImage image, int index)
		{
			super((new File(image.getImageFile()).getName()));
			this.index = index;
			this.icon = icon;
			this.type = TYPE_IMAGERY;
		}
		
		// TODO: Add imagery constructor
		
		
		public int getIndex()
		{
			return index;
		}
		
		public int getType()
		{
			return type;
		}
		
		public Object getDataObject()
		{
			return dataObject;
		}
		
		public Icon getIcon()
		{
			return icon;
		}
	}
	
	class DatasetTreeCellRenderer extends DefaultTreeCellRenderer 
	{

	    public DatasetTreeCellRenderer() {

			
			
	    }
	    
	    
	    public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

	    	super.getTreeCellRendererComponent(
		                tree, value, sel,
		                expanded, leaf, row,
		                hasFocus);
	    	
	    	
	    	if (value instanceof DatasetTreeNode) {
	    		DatasetTreeNode node = (DatasetTreeNode) value;
	    		if (node.getIcon() != null)
	    			this.setIcon(node.getIcon());
	    	}

	    	
	    	return this;
		}
	    
	}
	
	
	public void addDatasetSelectionListener(DatasetSelectionListener listener)
	{
		datasetSelectionListeners.add(listener);
	}
	
	public boolean removeDatasetSelectionListener(DatasetSelectionListener listener)
	{
		return datasetSelectionListeners.remove(listener);
	}
	
	protected void fireDatasetSelected(Object dataObject, int type, int index)
	{
		for (DatasetSelectionListener listener : datasetSelectionListeners) {
			listener.onDatasetSelected(dataObject, type, index);
		}
	}
	

	
	public interface DatasetSelectionListener
	{
		public void onDatasetSelected(Object dataObject, int type, int index);
	}
}

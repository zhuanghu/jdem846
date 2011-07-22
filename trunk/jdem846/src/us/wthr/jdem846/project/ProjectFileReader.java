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

package us.wthr.jdem846.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;


import us.wthr.jdem846.exception.ProjectParseException;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.shapefile.ShapeFileRequest;
import us.wthr.jdem846.shapefile.exception.ShapeFileException;

public class ProjectFileReader 
{
	private static Log log = Logging.getLog(ProjectFileReader.class);
	
	protected ProjectFileReader()
	{
		
	}
	
	
	/*
	<jdem846>
		<project>
			<options>
				<width>3000</width>
				<height>3000</height>
				<hill-shading>true</hill-shading>
				<hill-shading-type>1002</hill-shading-type>
				<coloring-type>1050</coloring-type>
				<tile-size>1000</tile-size>
				<lighting-multiple>0.5</lighting-multiple>
			</options>
			<input-files>
				<input>C:/Documents and Settings/a345926/My Documents/testdata/ned_64087130.flt</input>
			</input-files>
		</project>
	</jdem846>
		 */
	
	public static boolean fileExists(String path) throws FileNotFoundException
	{
		File f = new File(path);
		if (!f.exists()) {
			throw new FileNotFoundException(path);
		}
		return true;
	}
	
	public static Document loadProject(String path) throws DocumentException 
	{
		File file = new File(path);
		SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        return document;
	}
	
	@SuppressWarnings("unchecked")
	public static ProjectModel parseProject(Document doc) throws ProjectParseException
	{
		ProjectModel projectModel = new ProjectModel();
		Node node = null;
		
		List optionsList = doc.selectNodes( "//jdem846/project/options/option" );
		for (Iterator iter = optionsList.iterator(); iter.hasNext(); ) {
			Node inputNode = (Node) iter.next();
			Node keyAttribute = inputNode.selectSingleNode("@key");
			log.info("Key: " + keyAttribute.getText() + ", value: " + inputNode.getText());
			
			String key = keyAttribute.getText();
			String value = inputNode.getText();
			
			if (key != null && value != null) {
				projectModel.setOption(key, value);
			} else if (key == null) {
				log.warn("Option has no key!");
			}
			//projectModel.getInputFiles().add(inputNode.getText());
		}
		
		List elevationList = doc.selectNodes( "//jdem846/project/input-files/input" );
		for (Iterator iter = elevationList.iterator(); iter.hasNext(); ) {
			Node inputNode = (Node) iter.next();
			projectModel.getInputFiles().add(inputNode.getText());
		}

		List shapeList = doc.selectNodes( "//jdem846/project/input-files/shapefile" );
		for (Iterator iter = shapeList.iterator(); iter.hasNext(); ) {
			Node shapeNode = (Node) iter.next();
			
			Node pathNode = (Node) shapeNode.selectSingleNode("path");
			Node shapeDataDefinitionIdNode = (Node) shapeNode.selectSingleNode("data-definition-id");
			
			String path = pathNode.getText();
			String shapeDataDefinitionId = null;
			if (shapeDataDefinitionIdNode != null) {
				shapeDataDefinitionId = shapeDataDefinitionIdNode.getText();
			}
			
			try {
				projectModel.getShapeFiles().add(new ShapeFileRequest(path, shapeDataDefinitionId, false));
			} catch (ShapeFileException ex) {
				throw new ProjectParseException("Failed to load shapefile data when parsing project file", ex);
			}
			
		}
		
		return projectModel;
	}
	
	public static ProjectModel readProject(String path) throws IOException, FileNotFoundException, ProjectParseException
	{
		log.info("Opening project file: " + path);
		
		ProjectFileReader.fileExists(path);
		
		Document doc = null;
		try {
			doc = ProjectFileReader.loadProject(path);
		} catch (DocumentException ex) {
			throw new ProjectParseException("Failed to load project from " + path, ex);
		}

		ProjectModel projectModel = ProjectFileReader.parseProject(doc);
		
		projectModel.setLoadedFrom(path);
		
		return projectModel;
	}
	
}
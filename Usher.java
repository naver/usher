// 
// Copyright 2015 Naver
// Author : Dongseok Hyun <dustin.hyun@navercorp.com>
//
// This file is part of Usher.
//
// Usher is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Usher is distrubuted in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Usher. If not, see <http://www.gnu.org/licenses/>.

// Usher.java
//
// Returns x, y coordinates after running Force Atlas algorithm for given
// nodes and edges data.

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.Random;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openide.util.Lookup;

import static java.lang.Math.*; // for min max

/**
 * Usher runs force directed layout algorithm and returns 2d coordinates.
 * <p>
 * It takes node and edge data as JSON format. Converting them into gml,
 * it runs Force Atlas layout algorithm with fixed amount of time. After all
 * iterations end in a given time, it responses with coordinate data as
 * JSON format. It can also returns result as pdf or json file.
 *
 * Request data example,
 * 	{
 * 		"nodes":["n1", "n2", "n3", "n4"],
 * 		"edges":[ ["n1", "n2"], ["n1", "n3"], ["n3", "n4"] ]
 * 	}
 *
 * Response data example,
 * 	{
 * 		"status": "ok",
 * 		"message": "",
 * 		"data": {
 * 			"n1": [-63.589378, 309.603668],
 * 			"n2": [42.014568, -66.732544],
 * 			"n3": [170.108322, -20.717154],
 * 			"n4": [-121.449570, 192.134644],
 * 			"n4": [-10.2, 100.3234]
 * 		}
 * 	}
 *
 * 'message' has md5 hash value when 'status' is 'ok'. Hash value is
 * used as file name when coords data is cached as a file.
 *
 * Presets can also be given if some nodes need to be placed at specific
 * coordinates. Use when you want to tune the layout by putting a group of
 * nodes at a certain spot so that they would be placed close each other.
 *
 * @author Dongseok Hyun
 */
public class Usher {

	private JSONParser jsonParser;

	private Boolean exportToPdf = false;

	public Usher() {
		jsonParser = new JSONParser();
	}

	public void setPdfExport() {
		exportToPdf = true;
	}

	public void unsetPdfExport() {
		exportToPdf = false;
	}

	protected JSONObject getJson(String jsonString) throws ParseException {
		JSONObject jsonObj = (JSONObject)jsonParser.parse(jsonString);
		return jsonObj;
	}

	public String getLayoutData(String dataString) throws ParseException {

        // Workspace
        ProjectController pc =
			Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        // Graph
        GraphModel graphModel =
			Lookup.getDefault().lookup(GraphController.class).getModel();
        DirectedGraph graph = graphModel.getDirectedGraph();

		// Parse request data
		JSONObject data = getJson(dataString);

		// Nodes
		JSONArray nodeIds = (JSONArray) data.get("nodes");

		for (Object each : nodeIds) {
			String id = (String) each;
			Node n = graphModel.factory().newNode(id);
			graph.addNode(n);
		}

		// Edges
		JSONArray edges = (JSONArray) data.get("edges");

		for (Object each : edges) {
			JSONArray edge = (JSONArray) each;
			String source = (String) edge.get(0);
			String target = (String) edge.get(1);
			graph.addEdge(graph.getNode(source), graph.getNode(target));
		}

		// Presets
		// Note: Receives preset as Long value but put them as float.
		JSONObject presets = (JSONObject) data.get("presets");
		Random random = new Random();
		if (presets != null) {
			Iterator iterator = presets.keySet().iterator();
			while (iterator.hasNext()) {
				String id = (String) iterator.next();
				Node n = graph.getNode(id);

				if (n != null) {
					JSONArray preset = (JSONArray) presets.get(id);
					NodeData nodeData = n.getNodeData();
					Long x = (Long) preset.get(0);
					Long y = (Long) preset.get(1);
					nodeData.setX(x.floatValue() + random.nextFloat());
					nodeData.setY(y.floatValue() + random.nextFloat());
				}
				else {
					// Put your debug message here if needed.
				}
			}
		}

		////////////////////////////////////////////////////////////////////////
		// AUTOMATIC LAYOUT
		
		// Set layout execution time as 4 minutes
		//AutoLayout autoLayout = new AutoLayout(2, TimeUnit.SECONDS);
		AutoLayout autoLayout = new AutoLayout(4, TimeUnit.MINUTES);

		// Attach graph to layout
		autoLayout.setGraphModel(graphModel);

		// Setting up force atlas as layout algorithm.
		ForceAtlasLayout forceAtlasLayout = new ForceAtlasLayout(null);

		// Size may be set as number of edge each node has.
		AutoLayout.DynamicProperty adjustBySizeProperty =
			AutoLayout.createDynamicProperty(
				"forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f
			);

		// Bigger repulsion value makes nodes more dispersed.
		AutoLayout.DynamicProperty repulsionProperty =
			AutoLayout.createDynamicProperty(
				"forceAtlas.repulsionStrength.name", new Double(500.), 0.7f
			);

		// Hope that this would accelerate layout process.
		AutoLayout.DynamicProperty speedProperty =
			AutoLayout.createDynamicProperty(
				"forceAtlas.speed.name", new Double(10.0), 0f
			);

		// Adding all properties.
		autoLayout.addLayout(
			forceAtlasLayout,
			1.0f, // Ratio for forceAtlasLayout (1.0f == 100%)
			new AutoLayout.DynamicProperty[] {
				adjustBySizeProperty,
				repulsionProperty,
				speedProperty
			}
		);

		autoLayout.execute();

		// Export coordination
		StringBuffer layoutData = new StringBuffer();
		layoutData.append("{");
		Boolean isFirstLine = true;

		for (Node node : graph.getNodes().toArray()) {
			String id = node.getNodeData().getId();
			float x = node.getNodeData().x();
			float y = node.getNodeData().y();

			if (!isFirstLine) {
				layoutData.append(",");
			}

			layoutData.append(String.format("\"%s\":[%f,%f]", id, x, y));
			isFirstLine = false;
		}

		layoutData.append("}");

		if (exportToPdf) {

			//Export as a pdf
			ExportController ec =
				Lookup.getDefault().lookup(ExportController.class);

			try {
				ec.exportFile(new File("autolayout.pdf"));
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return layoutData.toString();
	}
}

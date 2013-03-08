/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV Institute as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
 * of Ecology and Sustainable Development.
 *
 * GDMS-Topology is distributed under GPL 3 license. It is produced by the
 * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
 * 2488.
 *
 * Copyright (C) 2009-2013 IRSTV (FR CNRS 2488)
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://wwwc.orbisgis.org/> or contact
 * directly: info_at_orbisgis.org
 */
package org.gdms.gdmstopology.graphcreator;

import com.graphhopper.sna.model.Edge;
import org.jgrapht.Graph;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gdms.data.indexes.IndexException;
import org.gdms.data.schema.Metadata;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.WeightedMultigraph;

/**
 * Creates a graph with a specified orientation from the given {@link DataSet}.
 *
 * @author Adam Gouge
 */
public abstract class GraphCreator {

    /**
     * The data set.
     */
    protected final DataSet dataSet;
    /**
     * Orientation.
     */
    protected final int orientation;
    /**
     * Specifies a directed graph.
     */
    public static final int DIRECTED = 1;
    /**
     * Specifies a directed graph with reversed edge orientation.
     */
    public static final int REVERSED = 2;
    /**
     * Specifies an undirected graph.
     */
    public static final int UNDIRECTED = 3;
    /**
     * An error message given when a user inputs an erroneous graph orientation.
     */
    public final static String GRAPH_TYPE_ERROR =
            "Please enter an appropriate graph orientation (1, 2 or 3).";
    /**
     * An error message given when there is a problem accessing the edge
     * metadata or recovering the start node, end node or weight field indices.
     */
    public final static String METADATA_ERROR =
            "Cannot load the edge metadata OR Cannot recover node "
            + "or weight field indices.";
    /**
     * An error message given when the edges cannot be loaded.
     */
    public final static String EDGE_LOADING_ERROR =
            "Cannot load the edges.";
    /**
     * An error message given when a field is missing from the input table.
     */
    public final static String MISSING_FIELD_ERROR =
            "The input table must contain the field \'";

    /**
     * Constructs a new {@link GraphCreator}.
     *
     * @param dataSet The data set.
     *
     */
    public GraphCreator(DataSet dataSet, int orientation) {
        this.dataSet = dataSet;
        this.orientation = orientation;
    }

    /**
     * Prepares a graph.
     *
     * @return The newly prepared graph.
     *
     * @throws IndexException
     */
    public Graph prepareGraph() throws IndexException {
        // DATASET INFORMATION
        // Get the weight column name.
        String weightColumnName = getWeightColumnName();
        // Initialize all the indices to -1.
        int startNodeIndex = -1;
        int endNodeIndex = -1;
        int weightFieldIndex = -1;
        // Recover the indices from the metadata.
        try {
            // Recover the edge Metadata.
            // TODO: Add a check to make sure the metadata was loaded correctly.
            Metadata edgeMetadata = dataSet.getMetadata();

            // Recover the indices of the start node and end node.
            startNodeIndex = edgeMetadata.getFieldIndex(GraphSchema.START_NODE);
            endNodeIndex = edgeMetadata.getFieldIndex(GraphSchema.END_NODE);
            verifyIndex(startNodeIndex, GraphSchema.START_NODE);
            verifyIndex(endNodeIndex, GraphSchema.END_NODE);

            // Recover the weight field index if possible.
            if (weightColumnName != null) {
                weightFieldIndex = edgeMetadata.getFieldIndex(weightColumnName);
                verifyIndex(weightFieldIndex, weightColumnName);
            }
        } catch (DriverException ex) {
            Logger.getLogger(GraphCreator.class.getName()).
                    log(Level.SEVERE, METADATA_ERROR, ex);
        }

        // GRAPH CREATION
        // Initialize the graph.
        Graph<Integer, Edge> graph = initializeGraph(weightColumnName,
                                                     orientation);
        try {
            // Add the edges according to the given graph type.
            loadEdges(graph, orientation,
                      startNodeIndex, endNodeIndex, weightFieldIndex);
        } catch (GraphException ex) {
            Logger.getLogger(GraphCreator.class.getName()).
                    log(Level.SEVERE, EDGE_LOADING_ERROR, ex);
        }
        return graph;
    }

    /**
     * Returns the weight column name, or {@code null} for unweighted graphs.
     *
     * @return The weight column name.
     */
    protected abstract String getWeightColumnName();

    /**
     * Initializes a JGraphT graph according to the given weight column name and
     * orientation.
     *
     * @param weightColumnName The weight column name.
     * @param orientation      The orientation.
     *
     * @return The newly initialized graph.
     */
    private Graph initializeGraph(
            String weightColumnName,
            int orientation) {
        // Unweighted
        if (weightColumnName == null) {
            if (orientation != UNDIRECTED) {
                return new DirectedMultigraph(DefaultEdge.class);
            } else {
                return new Multigraph(DefaultEdge.class);
            }
        } // Weighted
        else {
            if (orientation != UNDIRECTED) {
                return new DirectedWeightedMultigraph(DefaultWeightedEdge.class);
            } else {
                return new WeightedMultigraph(DefaultWeightedEdge.class);
            }
        }
    }

    /**
     * Loads the graph edges with the appropriate orientation.
     *
     * @param graph            The graph.
     * @param orientation      The orientation.
     * @param startNodeIndex   The start node index.
     * @param endNodeIndex     The end node index.
     * @param weightFieldIndex The weight field index.
     *
     * @throws GraphException
     */
    private void loadEdges(Graph graph, int orientation,
                           int startNodeIndex,
                           int endNodeIndex,
                           int weightFieldIndex) throws
            GraphException {
        if (orientation == GraphSchema.DIRECT) {
            loadDirectedEdges((DirectedGraph) graph,
                              startNodeIndex, endNodeIndex, weightFieldIndex);
        } else if (orientation == GraphSchema.DIRECT_REVERSED) {
            loadReversedEdges((DirectedGraph) graph,
                              startNodeIndex, endNodeIndex, weightFieldIndex);
        } else if (orientation == GraphSchema.UNDIRECT) {
            loadUndirectedEdges((UndirectedGraph) graph,
                                startNodeIndex, endNodeIndex, weightFieldIndex);
        } else {
            throw new GraphException(GRAPH_TYPE_ERROR);
        }
    }

    /**
     * Loads directed edges; the weight is decided by the implementation of this
     * method.
     *
     * @param graph            The graph.
     * @param startNodeIndex   The start node index.
     * @param endNodeIndex     The end node index.
     * @param weightFieldIndex The weight field index.
     */
    protected abstract void loadDirectedEdges(DirectedGraph graph,
                                              int startNodeIndex,
                                              int endNodeIndex,
                                              int weightFieldIndex);

    /**
     * Loads directed edges with orientations reversed.
     *
     * @param graph            The graph.
     * @param startNodeIndex   The start node index.
     * @param endNodeIndex     The end node index.
     * @param weightFieldIndex The weight field index.
     */
    private void loadReversedEdges(DirectedGraph graph,
                                   int startNodeIndex,
                                   int endNodeIndex,
                                   int weightFieldIndex) {
        loadDirectedEdges(graph, endNodeIndex, startNodeIndex, weightFieldIndex);
    }

    /**
     * Loads undirected edges; the weight is decided by the implementation of
     * this method.
     *
     * @param graph            The graph.
     * @param startNodeIndex   The start node index.
     * @param endNodeIndex     The end node index.
     * @param weightFieldIndex The weight field index.
     */
    protected abstract void loadUndirectedEdges(UndirectedGraph graph,
                                                int startNodeIndex,
                                                int endNodeIndex,
                                                int weightFieldIndex);

    /**
     * Verifies that the given index is not equal to -1; if it is, then throws
     * an exception saying that the given field is missing.
     *
     * @param index        The index.
     * @param missingField The field.
     *
     * @throws FunctionException
     */
    private void verifyIndex(int index, String missingField) throws
            IndexException {
        if (index == -1) {
            throw new IndexException(
                    MISSING_FIELD_ERROR + missingField + "\'.");
        }
    }
}

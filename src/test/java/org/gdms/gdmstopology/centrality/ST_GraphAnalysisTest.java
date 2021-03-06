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
package org.gdms.gdmstopology.centrality;

import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.gdmstopology.TopologySetupTest;
import org.gdms.gdmstopology.function.ST_ShortestPathLength;
import org.gdms.gdmstopology.graphcreator.GraphCreator;
import org.gdms.gdmstopology.model.GraphSchema;
import org.junit.Test;
import org.orbisgis.progress.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link ST_GraphAnalysis} on a 2D graph for all combinations of
 * (un)weighted, (un)directed/reversed.
 *
 * @author Adam Gouge
 */
public class ST_GraphAnalysisTest extends TopologySetupTest {

    private static final String LENGTH = "length";
    private static final double TOLERANCE = 0.0;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ST_GraphAnalysisTest.class);

    private void unweightedAnalysis(String orientation)
            throws Exception {
        new ST_GraphAnalysis()
                .evaluate(dsf,
                        prepareTables(),
                        new Value[]{ValueFactory.createValue(orientation)},
                        new NullProgressMonitor());
    }

    @Test
    public void unweightedDirectedTest() throws Exception {

        unweightedAnalysis(ST_ShortestPathLength.DIRECTED
                + ST_ShortestPathLength.SEPARATOR
                + GraphSchema.EDGE_ORIENTATION);

        DataSource nodes = dsf.getDataSource("node_centrality");
        nodes.open();

        for (int i = 0; i < nodes.getRowCount(); i++) {
            Value[] row = nodes.getRow(i);
            int id = row[0].getAsInt();
            double betweenness = row[1].getAsDouble();
            double closeness = row[2].getAsDouble();
            if (id == 2 || id == 4 || id == 5) {
                assertEquals(0.0, betweenness, TOLERANCE);
            } else if (id == 1 || id == 3) {
                assertEquals(1.0, betweenness, TOLERANCE);
            } else if (id == 6) {
                assertEquals(0.6666666666666666, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected vertex {}", id);
            }
            if (id == 1 || id == 3 || id == 4 || id == 5 || id == 6) {
                assertEquals(0.0, closeness, TOLERANCE);
            } else if (id == 2) {
                assertEquals(0.4166666666666667, closeness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected vertex {}", id);
            }
        }

        nodes.close();

        DataSource edges = dsf.getDataSource("edge_centrality");
        edges.open();
        assertTrue(edges.getRowCount() == 6);
        for (int i = 0; i < edges.getRowCount(); i++) {
            final Value[] row = edges.getRow(i);
            final int id = row[0].getAsInt();
            final double betweenness = row[1].getAsDouble();
            if (id == 1) {
                assertEquals(3.0 / 4, betweenness, TOLERANCE);
            } else if (id == 2) {
                assertEquals(0, betweenness, TOLERANCE);
            } else if (id == 3) {
                assertEquals(1, betweenness, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(1.0 / 4, betweenness, TOLERANCE);
            } else if (id == 6) {
                assertEquals(1.0 / 2, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected edge {}", id);
            }
        }
        edges.close();
    }

    @Test
    public void unweightedReversedTest() throws Exception {

        unweightedAnalysis(ST_ShortestPathLength.REVERSED
                + ST_ShortestPathLength.SEPARATOR
                + GraphSchema.EDGE_ORIENTATION);

        DataSource nodes = dsf.getDataSource("node_centrality");
        nodes.open();

        for (int i = 0; i < nodes.getRowCount(); i++) {
            Value[] row = nodes.getRow(i);
            int id = row[0].getAsInt();
            double betweenness = row[1].getAsDouble();
            double closeness = row[2].getAsDouble();
            if (id == 2 || id == 4 || id == 5) {
                assertEquals(0.0, betweenness, TOLERANCE);
            } else if (id == 3 || id == 6) {
                assertEquals(1.0, betweenness, TOLERANCE);
            } else if (id == 1) {
                assertEquals(0.375, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected vertex {}", id);
            }
            // All vertices have closeness zero.
            assertEquals(0.0, closeness, TOLERANCE);
        }

        nodes.close();

        DataSource edges = dsf.getDataSource("edge_centrality");
        edges.open();
        assertTrue(edges.getRowCount() == 6);
        for (int i = 0; i < edges.getRowCount(); i++) {
            final Value[] row = edges.getRow(i);
            final int id = row[0].getAsInt();
            final double betweenness = row[1].getAsDouble();
            if (id == 1) {
                assertEquals(3.0 / 4, betweenness, TOLERANCE);
            } else if (id == 2) {
                assertEquals(0, betweenness, TOLERANCE);
            } else if (id == 3) {
                assertEquals(1, betweenness, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(1.0 / 4, betweenness, TOLERANCE);
            } else if (id == 6) {
                assertEquals(1.0 / 2, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected edge {}", id);
            }
        }
    }

    @Test
    public void unweightedUndirectedTest() throws Exception {

        unweightedAnalysis(ST_ShortestPathLength.UNDIRECTED);

        DataSource nodes = dsf.getDataSource("node_centrality");
        nodes.open();

        for (int i = 0; i < nodes.getRowCount(); i++) {
            Value[] row = nodes.getRow(i);
            int id = row[0].getAsInt();
            double betweenness = row[1].getAsDouble();
            double closeness = row[2].getAsDouble();
            if (id == 2 || id == 4 || id == 5) {
                assertEquals(0.0, betweenness, TOLERANCE);
            } else if (id == 3) {
                assertEquals(1.0, betweenness, TOLERANCE);
            } else if (id == 6) {
                assertEquals(0.75, betweenness, TOLERANCE);
            } else if (id == 1) {
                assertEquals(0.5, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected vertex {}", id);
            }
            if (id == 3 || id == 6) {
                assertEquals(0.625, closeness, TOLERANCE);
            } else if (id == 2 || id == 5) {
                assertEquals(0.4166666666666667, closeness, TOLERANCE);
            } else if (id == 1) {
                assertEquals(0.5, closeness, TOLERANCE);
            } else if (id == 4) {
                assertEquals(0.35714285714285715, closeness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected vertex {}", id);
            }
        }

        nodes.close();

        DataSource edges = dsf.getDataSource("edge_centrality");
        edges.open();
        assertTrue(edges.getRowCount() == 6);
        for (int i = 0; i < edges.getRowCount(); i++) {
            final Value[] row = edges.getRow(i);
            final int id = row[0].getAsInt();
            final double betweenness = row[1].getAsDouble();
            if (id == 1 || id == 2 || id == 6) {
                assertEquals(1.0 / 5, betweenness, TOLERANCE);
            } else if (id == 3) {
                assertEquals(1, betweenness, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(0, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected edge {}", id);
            }
        }
    }

    private void weightedAnalysis(String weight, String orientation)
            throws Exception {
        new ST_GraphAnalysis()
                .evaluate(dsf,
                        prepareTables(),
                        new Value[]{ValueFactory.createValue(weight),
                                ValueFactory.createValue(orientation)},
                        new NullProgressMonitor());
    }

    @Test
    public void weightedDirectedTest() throws Exception {

        weightedAnalysis(LENGTH,
                ST_ShortestPathLength.DIRECTED
                        + ST_ShortestPathLength.SEPARATOR
                        + GraphSchema.EDGE_ORIENTATION);

        DataSource nodes = dsf.getDataSource("node_centrality");
        nodes.open();

        for (int i = 0; i < nodes.getRowCount(); i++) {
            Value[] row = nodes.getRow(i);
            int id = row[0].getAsInt();
            double betweenness = row[1].getAsDouble();
            double closeness = row[2].getAsDouble();
            if (id == 2 || id == 4 || id == 5) {
                assertEquals(0.0, betweenness, TOLERANCE);
            } else if (id == 3 || id == 6) {
                assertEquals(1.0, betweenness, TOLERANCE);
            } else if (id == 1) {
                assertEquals(0.75, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected vertex {}", id);
            }
            if (id == 1 || id == 3 || id == 4 || id == 5 || id == 6) {
                assertEquals(0.0, closeness, TOLERANCE);
            } else if (id == 2) {
                assertEquals(0.0035327735482214143, closeness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected vertex {}", id);
            }
        }

        nodes.close();

        DataSource edges = dsf.getDataSource("edge_centrality");
        edges.open();
        assertTrue(edges.getRowCount() == 6);
        for (int i = 0; i < edges.getRowCount(); i++) {
            final Value[] row = edges.getRow(i);
            final int id = row[0].getAsInt();
            final double betweenness = row[1].getAsDouble();
            if (id == 1) {
                assertEquals(5.0 / 6, betweenness, TOLERANCE);
            } else if (id == 2) {
                assertEquals(1.0 / 3, betweenness, TOLERANCE);
            } else if (id == 3 || id == 4) {
                assertEquals(1, betweenness, TOLERANCE);
            } else if (id == 5) {
                assertEquals(0, betweenness, TOLERANCE);
            } else if (id == 6) {
                assertEquals(2.0 / 3, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected edge {}", id);
            }
        }
    }

    @Test
    public void weightedReversedTest() throws Exception {

        weightedAnalysis(LENGTH,
                ST_ShortestPathLength.REVERSED
                        + ST_ShortestPathLength.SEPARATOR
                        + GraphSchema.EDGE_ORIENTATION);

        DataSource nodes = dsf.getDataSource("node_centrality");
        nodes.open();

        for (int i = 0; i < nodes.getRowCount(); i++) {
            Value[] row = nodes.getRow(i);
            int id = row[0].getAsInt();
            double betweenness = row[1].getAsDouble();
            double closeness = row[2].getAsDouble();
            if (id == 2 || id == 4 || id == 5) {
                assertEquals(0.0, betweenness, TOLERANCE);
            } else if (id == 3 || id == 6) {
                assertEquals(1.0, betweenness, TOLERANCE);
            } else if (id == 1) {
                assertEquals(0.75, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected vertex {}", id);
            }
            // All vertices have closeness zero.
            assertEquals(0.0, closeness, TOLERANCE);
        }

        nodes.close();

        DataSource edges = dsf.getDataSource("edge_centrality");
        edges.open();
        assertTrue(edges.getRowCount() == 6);
        for (int i = 0; i < edges.getRowCount(); i++) {
            final Value[] row = edges.getRow(i);
            final int id = row[0].getAsInt();
            final double betweenness = row[1].getAsDouble();
            if (id == 1) {
                assertEquals(5.0 / 6, betweenness, TOLERANCE);
            } else if (id == 2) {
                assertEquals(1.0 / 3, betweenness, TOLERANCE);
            } else if (id == 3 || id == 4) {
                assertEquals(1, betweenness, TOLERANCE);
            } else if (id == 5) {
                assertEquals(0, betweenness, TOLERANCE);
            } else if (id == 6) {
                assertEquals(2.0 / 3, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected edge {}", id);
            }
        }
    }

    @Test
    public void weightedUndirectedTest() throws Exception {

        weightedAnalysis(LENGTH,
                ST_ShortestPathLength.UNDIRECTED);

        DataSource nodes = dsf.getDataSource("node_centrality");
        nodes.open();

        // CHECK RESULTS
        for (int i = 0; i < nodes.getRowCount(); i++) {
            Value[] row = nodes.getRow(i);
            int id = row[0].getAsInt();
            double betweenness = row[1].getAsDouble();
            double closeness = row[2].getAsDouble();
            if (id == 2 || id == 4 || id == 5) {
                assertEquals(0.0, betweenness, TOLERANCE);
            } else if (id == 3) {
                assertEquals(1.0, betweenness, TOLERANCE);
            } else if (id == 1) {
                assertEquals(0.5714285714285714, betweenness, TOLERANCE);
            } else if (id == 6) {
                assertEquals(0.8571428571428571, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected vertex {}", id);
            }
            if (id == 3 || id == 6) {
                assertEquals(0.0055753940798198886, closeness, TOLERANCE);
            } else if (id == 1) {
                assertEquals(0.003787491035823884, closeness, TOLERANCE);
            } else if (id == 2) {
                assertEquals(0.0035327735482214143, closeness, TOLERANCE);
            } else if (id == 4) {
                assertEquals(0.0032353723348164448, closeness, TOLERANCE);
            } else if (id == 5) {
                assertEquals(0.003495002741097083, closeness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected vertex {}", id);
            }
        }

        nodes.close();

        DataSource edges = dsf.getDataSource("edge_centrality");
        edges.open();
        assertTrue(edges.getRowCount() == 6);
        for (int i = 0; i < edges.getRowCount(); i++) {
            final Value[] row = edges.getRow(i);
            final int id = row[0].getAsInt();
            final double betweenness = row[1].getAsDouble();
            if (id == 1 || id == 2 || id == 6) {
                assertEquals(5.0 / 9, betweenness, TOLERANCE);
            } else if (id == 3) {
                assertEquals(1, betweenness, TOLERANCE);
            } else if (id == 4) {
                assertEquals(8.0 / 9, betweenness, TOLERANCE);
            } else if (id == 5) {
                assertEquals(0, betweenness, TOLERANCE);
            } else {
                LOGGER.error("Unexpected edge {}", id);
            }
        }
    }

    private DataSet[] prepareTables() throws DataSourceCreationException,
            NoSuchTableException, DriverException {
        DataSource edges = dsf.getDataSource(GRAPH2D_EDGES);
        edges.open();
        MemoryDataSetDriver newEdges = introduceDefaultOrientations(edges);
        DataSet[] tables = new DataSet[]{newEdges};
        return tables;
    }

    private MemoryDataSetDriver introduceDefaultOrientations(DataSet edges)
            throws DriverException {
        DefaultMetadata newMetadata = new DefaultMetadata(edges.getMetadata());
        newMetadata.addField(GraphSchema.EDGE_ORIENTATION,
                TypeFactory.createType(Type.INT));
        MemoryDataSetDriver newEdges =
                new MemoryDataSetDriver(newMetadata);
        for (int i = 0; i < edges.getRowCount(); i++) {
            Value[] oldRow = edges.getRow(i);
            final Value[] newRow = new Value[newMetadata.getFieldCount()];
            System.arraycopy(oldRow, 0, newRow, 0,
                    newMetadata.getFieldCount() - 1);
            newRow[newMetadata.getFieldCount() - 1] =
                    ValueFactory.createValue(GraphCreator.DIRECTED_EDGE);
            newEdges.addValues(newRow);
        }
        return newEdges;
    }
}

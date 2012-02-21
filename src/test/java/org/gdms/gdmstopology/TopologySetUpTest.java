/**
 * GDMS-Topology  is a library dedicated to graph analysis. It's based on the JGraphT available at
 * http://www.jgrapht.org/. It ables to compute and process large graphs using spatial
 * and alphanumeric indexes.
 * This version is developed at French IRSTV institut as part of the
 * EvalPDU project, funded by the French Agence Nationale de la Recherche
 * (ANR) under contract ANR-08-VILL-0005-01 and GEBD project
 * funded by the French Ministery of Ecology and Sustainable Development .
 *
 * GDMS-Topology  is distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://trac.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.gdms.gdmstopology;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import java.io.File;
import org.gdms.data.SQLDataSourceFactory;
import org.junit.After;
import org.junit.Before;
import org.orbisgis.utils.FileUtils;

/**
 *
 * @author ebocher
 */
public class TopologySetUpTest {

        protected static final GeometryFactory gf = new GeometryFactory();
        protected SQLDataSourceFactory dsf;
        protected WKTReader wktReader;
        protected String GRAPH2D = "graph2D";
        protected String GRAPH2D_EDGES = "graph2D_edges";
        protected String GRAPH2D_NODES = "graph2D_nodes";
        public static String internalData = "src/test/resources/org/gdms/gdmstopology/";
        public static File backupDir = new File(internalData + "backup");

        @Before
        public  void setUp() throws Exception {
                //Create a folder to save all results
                FileUtils.deleteDir(backupDir);
                backupDir.mkdir();
                //Create the datasourcefactory that uses the folder
                dsf = new SQLDataSourceFactory(backupDir.getAbsolutePath(), backupDir.getAbsolutePath());

                //Create some geometries
                wktReader = new WKTReader();
                dsf.getSourceManager().register(GRAPH2D, new File(internalData + "graph2D.shp"));
                dsf.getSourceManager().register(GRAPH2D_EDGES, new File(internalData + "graph2D_edges.shp"));
                dsf.getSourceManager().register(GRAPH2D_NODES, new File(internalData + "graph2D_nodes.shp"));
        }

        @After
        public  void tearDown() throws Exception {
                //Delete the folder that contains result
                FileUtils.deleteDir(backupDir);
        }
}
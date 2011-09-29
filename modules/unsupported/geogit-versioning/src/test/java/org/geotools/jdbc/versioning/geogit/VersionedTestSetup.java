/**
 * 
 */
package org.geotools.jdbc.versioning.geogit;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.geogit.repository.Repository;
import org.geogit.storage.bdbje.EntityStoreConfig;
import org.geogit.storage.bdbje.EnvironmentBuilder;
import org.geogit.storage.bdbje.JERepositoryDatabase;
import org.geotools.data.DataUtilities;
import org.geotools.data.postgis.PostGISTestSetup;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.WKTReader2;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;

import com.sleepycat.je.Environment;
import com.vividsolutions.jts.io.ParseException;


/**
 * @author wdeane
 *
 */
public class VersionedTestSetup extends PostGISTestSetup {

    protected Repository repo;
	private JERepositoryDatabase repositoryDatabase;
	    protected static final String idL1 = "Lines.1";

	    protected static final String idL2 = "Lines.2";

	    protected static final String idL3 = "Lines.3";

	    protected static final String idP1 = "Points.1";

	    protected static final String idP2 = "Points.2";

	    protected static final String idP3 = "Points.3";

	    protected static final String pointsNs = "http://geogit.points";

	    protected static final String pointsName = "Points";

	    protected static final String pointsTypeSpec = "name:String,geometry:Point:srid=4326";

	    protected static final Name pointsTypeName = new NameImpl(pointsNs, pointsName);

	    protected SimpleFeatureType pointsType;

	    protected Feature points1;

	    protected Feature points2;

	    protected Feature points3;

	    protected static final String linesNs = "http://geogit.lines";

	    protected static final String linesName = "Lines";

	    protected static final String linesTypeSpec = "name:String,geometry:LineString:srid=4326";

	    protected static final Name linesTypeName = new NameImpl(linesNs, linesName);

	    protected SimpleFeatureType linesType;

	    protected Feature lines1;

	    protected Feature lines2;

	    protected Feature lines3;
	    
	    protected VersionedTestSetup setup;

	@Override
	protected Properties createExampleFixture() {
		// TODO Auto-generated method stub
		return super.createExampleFixture();
	}

    @Override
    public void setUpData() throws Exception {
    	super.setUpData();
    	pointsType = DataUtilities.createType(pointsNs, pointsName, pointsTypeSpec);

        points1 = feature(pointsType, idP1, "StringProp1_1", "POINT(1 1)");
        points2 = feature(pointsType, idP2, "StringProp1_2", "POINT(2 2)");
        points3 = feature(pointsType, idP3, "StringProp1_3", "POINT(3 3)");
        runSafe("DROP TABLE \"ggpoints\"");
        runSafe("DELETE FROM GEOMETRY_COLUMNS WHERE f_table_name=\"ggpoints\"");
        
        run("CREATE TABLE \"ggpoints\"(" //
                + "\"id\" serial primary key, "
                + "\"stringProperty\" varchar, "
                + "\"geometry\" geometry)");
//        run("INSERT INTO GEOMETRY_COLUMNS VALUES('', 'public', 'ggpoints', 'geometry', 2, '4326', 'POINT')");
//        run("CREATE INDEX GGPOINTS_GEOMETRY_INDEX ON \"ggpoints\" USING GIST (\"geometry\") ");
        
        
        
/*
        linesType = DataUtilities.createType(linesNs, linesName, linesTypeSpec);

        lines1 = feature(linesType, idL1, "StringProp2_1", new Integer(1000),
                "LINESTRING (1 1, 2 2)");
        lines2 = feature(linesType, idL2, "StringProp2_2", new Integer(2000),
                "LINESTRING (3 3, 4 4)");
        lines3 = feature(linesType, idL3, "StringProp2_3", new Integer(3000),
                "LINESTRING (5 5, 6 6)");
        
        
        run("CREATE TABLE \"gglines\"(" //
                + "\"id\" serial primary key, "
                + "\"stringProperty\" varchar, "
                + "\"geometry\" geometry)");
        run("INSERT INTO GEOMETRY_COLUMNS VALUES('', 'public', 'gglines', 'geometry', 2, '4326', 'LINESTRING')");
        run("CREATE INDEX GGLINES_GEOMETRY_INDEX ON \"gglines\" USING GIST (\"geometry\") ");*/
    }
    
    protected Feature feature(SimpleFeatureType type, String id, Object... values)
            throws ParseException {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (type.getDescriptor(i) instanceof GeometryDescriptor) {
                if (value instanceof String) {
                    value = new WKTReader2().read((String) value);
                }
            }
            builder.set(i, value);
        }
        return builder.buildFeature(id);
    }


	/* (non-Javadoc)
	 * @see org.geotools.data.postgis.PostGISTestSetup#createDataStoreFactory()
	 */
	@Override
	protected VersionedGeoGITDataStoreFactory createDataStoreFactory() {
		// TODO Auto-generated method stub
		return new VersionedGeoGITDataStoreFactory(super.createDataStoreFactory());
	}
	
	@Override
    public void setUp() throws Exception {
        //
		super.setUp();
		final File envHome = new File(
				(String) VersionedGeoGITDataStoreFactory.GG_ENVHOME
						.lookUp((Map) this.fixture));
		final File repositoryHome = new File(envHome,
				(String) VersionedGeoGITDataStoreFactory.GG_REPHOME
						.lookUp((Map) this.fixture));
		final File indexHome = new File(envHome,
				(String) VersionedGeoGITDataStoreFactory.GG_INDEXHOME
						.lookUp((Map) this.fixture));

	try {
            FileUtils.deleteDirectory(envHome);
            repositoryHome.mkdirs();
            indexHome.mkdirs();
            EntityStoreConfig config = new EntityStoreConfig();
            config.setCacheMemoryPercentAllowed(50);
            EnvironmentBuilder esb = new EnvironmentBuilder(config);
            Properties bdbEnvProperties = null;
            Environment environment;
            environment = esb.buildEnvironment(repositoryHome, bdbEnvProperties);

            Environment stagingEnvironment;
            stagingEnvironment = esb.buildEnvironment(indexHome, bdbEnvProperties);

            repositoryDatabase = new JERepositoryDatabase(environment,
            		stagingEnvironment);
            


            repo = new Repository(repositoryDatabase, envHome);

            repo.create();
            
        } catch (Exception e) {
        } finally{
            if (repo != null)repo.close();
        }
    }

    /* (non-Javadoc)
	 * @see org.geotools.jdbc.JDBCTestSetup#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
		if (repo != null){
		 repo.close();  
		 repo = null;

		}

        
	}

}

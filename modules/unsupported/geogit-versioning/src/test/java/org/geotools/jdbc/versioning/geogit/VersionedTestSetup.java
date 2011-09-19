/**
 * 
 */
package org.geotools.jdbc.versioning.geogit;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.geogit.repository.Repository;
import org.geogit.storage.bdbje.EntityStoreConfig;
import org.geogit.storage.bdbje.EnvironmentBuilder;
import org.geogit.storage.bdbje.JERepositoryDatabase;
import org.geotools.data.jdbc.datasource.DBCPDataSource;
import org.geotools.data.postgis.PostGISTestSetup;
import org.geotools.jdbc.IJDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;

import com.sleepycat.je.Environment;


/**
 * @author wdeane
 *
 */
public class VersionedTestSetup extends PostGISTestSetup {

    protected Repository repo;
	private JERepositoryDatabase repositoryDatabase;
	/* (non-Javadoc)
	 * @see org.geotools.data.postgis.PostGISTestSetup#setUpDataStore(org.geotools.jdbc.IJDBCDataStore)
	 */
	@Override
	protected void setUpDataStore(IJDBCDataStore dataStore) {
		// TODO Auto-generated method stub
		super.setUpDataStore(dataStore);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.postgis.PostGISTestSetup#createExampleFixture()
	 */
	@Override
	protected Properties createExampleFixture() {
		// TODO Auto-generated method stub
		return super.createExampleFixture();
	}

    @Override
    public void setUpData() throws Exception {
    	super.setUpData();
    	
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

		// repositoryDatabase = new FileSystemRepositoryDatabase(envHome);

		repo = new Repository(repositoryDatabase, envHome);

		repo.create();
    }

    /* (non-Javadoc)
	 * @see org.geotools.jdbc.JDBCTestSetup#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
        if (repo != null) {
            repo.close();
        }
        
	}

}

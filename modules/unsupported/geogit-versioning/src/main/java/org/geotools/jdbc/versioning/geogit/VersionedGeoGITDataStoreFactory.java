/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 * 
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.jdbc.versioning.geogit;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.geogit.api.GeoGIT;
import org.geogit.repository.Repository;
import org.geogit.storage.bdbje.EntityStoreConfig;
import org.geogit.storage.bdbje.EnvironmentBuilder;
import org.geogit.storage.bdbje.JERepositoryDatabase;
import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.jdbc.datasource.DataSourceUtil;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.jdbc.CompositePrimaryKeyFinder;
import org.geotools.jdbc.HeuristicPrimaryKeyFinder;
import org.geotools.jdbc.IJDBCDataStore;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.MetadataTablePrimaryKeyFinder;
import org.geotools.jdbc.SQLDialect;

import com.sleepycat.je.Environment;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Builds instances of the Versioned JDBC/GeoGIT wrapper
 * 
 *
 * @source $URL$
 */
public class VersionedGeoGITDataStoreFactory extends JDBCDataStoreFactory {
    
    /** The logger for the postgis module. */
    protected static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.jdbc.versioning.geogit");
    
    
    public static final Param GG_ENVHOME = new Param("envHome", String.class, "geogit", true);
    public static final Param GG_REPHOME = new Param("repHome", String.class, "repository", true);
    public static final Param GG_INDEXHOME = new Param("indexHome", String.class, "index", true);
    
    private JDBCDataStoreFactory dataStoreFactory;
   

    /**
     * Creates a new instance of PostgisDataStoreFactory
     */
    public VersionedGeoGITDataStoreFactory(JDBCDataStoreFactory datastoreFactory) {
    	this.dataStoreFactory = datastoreFactory;
    }
    
    /**
     * @param dataStore
     * @param params
     * @return
     * @throws IOException
     */
    protected IJDBCDataStore createDataStoreInternal(IJDBCDataStore dataStore, Map params)
            throws IOException {
    		GeoGITFacade ggit = this.createGeoGIT(params);
    		dataStore.setDataStoreFactory(this);
            return new GeoGITWrappingDataStore(ggit, dataStore);
    }
    
    private GeoGITFacade createGeoGIT(Map<String, ?> params) throws IOException {
    	
    	final File envHome = new File((String) GG_ENVHOME.lookUp(params));
        final File repositoryHome = new File(envHome, (String) GG_REPHOME.lookUp(params));
        final File indexHome = new File(envHome,  (String) GG_INDEXHOME.lookUp(params));
        EntityStoreConfig config = new EntityStoreConfig();
        config.setCacheMemoryPercentAllowed(50);
        EnvironmentBuilder esb = new EnvironmentBuilder(config);
        Properties bdbEnvProperties = null;
        Environment environment;
        environment = esb.buildEnvironment(repositoryHome, bdbEnvProperties);

        Environment stagingEnvironment;
        stagingEnvironment = esb.buildEnvironment(indexHome, bdbEnvProperties);

        JERepositoryDatabase repositoryDatabase = new JERepositoryDatabase(environment, stagingEnvironment);

        // repositoryDatabase = new FileSystemRepositoryDatabase(envHome);

        Repository repo = new Repository(repositoryDatabase, envHome);
        return new GeoGITFacade(new GeoGIT(repo));
	}



    /*
     * Construct a data store using the params.
     * 
     * @param params
     *            The full set of information needed to construct a live data
     *            source. Should have dbtype equal to postgis, as well as host,
     *            user, passwd, database, and table.
     * 
     * @return The created DataSource, this may be null if the required resource
     *         was not found or if insufficient parameters were given. Note that
     *         canProcess() should have returned false if the problem is to do
     *         with insufficient parameters.
     * 
     * @throws IOException
     *             See DataSourceException
     * @throws DataSourceException
     *             Thrown if there were any problems creating or connecting the
     *             datasource.
     
    public IJDBCDataStore createDataStore(Map params) throws IOException {
    	IJDBCDataStore dataStore = this.dataStoreFactory.createDataStore(params);
    	return  createDataStoreInternal(dataStore, params);
    }
*/


    public String getDisplayName() {
        return "DataStore with GeoGIT Versioning backing";
    }

    /**
     * Describe the nature of the datasource constructed by this factory.
     * 
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.
     */
    public String getDescription() {
        return "A IJDBCDataStore backed by a GeoGIT object for maintaining versioning.";
    }

    /**
     * Determines if the appropriate libraries are present for this datastore
     * factory to successfully produce datastores.
     * 
     * @return <tt>true</tt> if the necessary jars are on the classpath.
     */
    public boolean isAvailable() {
        return this.dataStoreFactory.isAvailable();
    }


    @Override 
    protected void setupParameters(@SuppressWarnings("rawtypes") Map parameters) {
        // remember: when adding a new parameter here that is not connection related,
        // add it to the JDBCJNDIDataStoreFactory class
        super.setupParameters(parameters);
        parameters.put(GG_ENVHOME.key, GG_ENVHOME);
        parameters.put(GG_REPHOME.key, GG_REPHOME);
        parameters.put(GG_INDEXHOME.key, GG_INDEXHOME);
    }
    
	@Override
	public String getDatabaseID() {
		return this.dataStoreFactory.getDatabaseID();
	}

	@Override
	public String getDriverClassName() {
		// TODO Auto-generated method stub
		return this.dataStoreFactory.getDriverClassName();
	}

	@Override
	public SQLDialect createSQLDialect(IJDBCDataStore dataStore) {
		// TODO Auto-generated method stub
		return this.dataStoreFactory.createSQLDialect(dataStore);
	}

	@Override
	public String getValidationQuery() {
		// TODO Auto-generated method stub
		return this.dataStoreFactory.getValidationQuery();
	}
	
    @Override
    public String getJDBCUrl(Map params) throws IOException {
        return this.dataStoreFactory.getJDBCUrl(params);
    }
}

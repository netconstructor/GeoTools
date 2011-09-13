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
import org.geotools.jdbc.IJDBCDataStore;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;

import com.sleepycat.je.Environment;

/**
 * Builds instances of the Versioned JDBC/GeoGIT wrapper
 * 
 *
 * @source $URL$
 */
public class VersionedGeoGITDataStoreFactory extends AbstractDataStoreFactory {
    
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
    		GeoGIT ggit = this.createGeoGIT(params);
            return new GeoGITWrappingDataStore(ggit, dataStore);
    }

    //public static final Param GG_ENVHOME = new Param("envHome", String.class, "geogit", true);
    //public static final Param GG_REPHOME = new Param("repHome", String.class, "repository", true);
    //public static final Param GG_INDEXHOME = new Param("indexHome", String.class, "index", true);
    
    private GeoGIT createGeoGIT(Map params) throws IOException {
		
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
        return new GeoGIT(repo);
	}

	public boolean canProcess(Map params) {
        return this.dataStoreFactory.canProcess(params);
    }

    /**
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
     */
    public IJDBCDataStore createDataStore(Map params) throws IOException {
    	IJDBCDataStore dataStore = this.dataStoreFactory.createDataStore(params);
    	return  createDataStoreInternal(dataStore, params);
    }


    /**
     * Postgis cannot create a new database.
     * 
     * @param params
     * 
     * 
     * @throws IOException
     *             See UnsupportedOperationException
     * @throws UnsupportedOperationException
     *             Cannot create new database
     */
    public DataStore createNewDataStore(Map params) throws IOException, UnsupportedOperationException {
        return (DataStore) this.dataStoreFactory.createDataSource(params);
    }

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

    /**
     * Describe parameters.
     * 
     * 
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
    	Param[] geogitparams = new Param[] {
    			GG_ENVHOME, GG_REPHOME, GG_INDEXHOME
    	};
    	Param[] dsparams = this.dataStoreFactory.getParametersInfo();
    	Param[] result = Arrays.copyOf(geogitparams, geogitparams.length + dsparams.length);
    	  System.arraycopy(dsparams, 0, result, geogitparams.length, dsparams.length);
    	  return result;
    }
}

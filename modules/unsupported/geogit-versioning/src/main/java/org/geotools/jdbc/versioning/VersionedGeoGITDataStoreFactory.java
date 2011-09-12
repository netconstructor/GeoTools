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
package org.geotools.jdbc.versioning;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.geogit.api.GeoGIT;
import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.jdbc.datasource.DataSourceUtil;
import org.geotools.jdbc.IJDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;

/**
 * Builds instances of the Versioned JDBC/GeoGIT wrapper
 * 
 *
 * @source $URL$
 */
public class VersionedGeoGITDataStoreFactory extends AbstractDataStoreFactory {
    
    /** The logger for the postgis module. */
    protected static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.jdbc.versioning.geogit");
    
    private JDBCDataStoreFactory dataStoreFactory;
    private GeoGIT geoGIT;

    /**
     * Creates a new instance of PostgisDataStoreFactory
     */
    public VersionedGeoGITDataStoreFactory(JDBCDataStoreFactory datastoreFactory) {
    	this.dataStoreFactory = datastoreFactory;
    }

    public boolean canProcess(Map params) {
        return this.canProcess(params);
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
     *         was not found or if insufficent parameters were given. Note that
     *         canProcess() should have returned false if the problem is to do
     *         with insuficent parameters.
     * 
     * @throws IOException
     *             See DataSourceException
     * @throws DataSourceException
     *             Thrown if there were any problems creating or connecting the
     *             datasource.
     */
    public IJDBCDataStore createDataStore(Map params) throws IOException {
    	IJDBCDataStore wrapped = this.dataStoreFactory.createDataStore(params);
    	
    	
    	
    	
    	
    	
    	return null;
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
        return this.dataStoreFactory.getParametersInfo();
    }
}

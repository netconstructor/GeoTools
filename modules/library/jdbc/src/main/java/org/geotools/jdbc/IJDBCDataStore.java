package org.geotools.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.geotools.data.DataStore;
import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.factory.Hints;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.GeometryFactory;

public interface IJDBCDataStore extends DataStore {
	
    /**
     * The native SRID associated to a certain descriptor
     * TODO: qualify this key with 'org.geotools.jdbc'
     */
    public static final String JDBC_NATIVE_SRID = "nativeSRID";
    
    /**
     * Boolean marker stating whether the feature type is to be considered read only
     */
    public static final String JDBC_READ_ONLY = "org.geotools.jdbc.readOnly";
    
    /**
     * The key for attribute descriptor user data which specifies the original database column data 
     * type.
     */
    public static final String JDBC_NATIVE_TYPENAME = "org.geotools.jdbc.nativeTypeName";
    

	/**
	 * The dialect the datastore uses to generate sql statements in order to
	 * communicate with the underlying database.
	 *
	 * @return The dialect, never <code>null</code>.
	 */
	public SQLDialect getSQLDialect();

	/**
	 * Sets the dialect the datastore uses to generate sql statements in order to
	 * communicate with the underlying database.
	 *
	 * @param dialect The dialect, never <code>null</code>.
	 */
	public void setSQLDialect(SQLDialect dialect);

	/**
	 * The data source the datastore uses to obtain connections to the underlying
	 * database.
	 *
	 * @return The data source, never <code>null</code>.
	 */
	public DataSource getDataSource();

	/**
	 * Sets the data source the datastore uses to obtain connections to the underlying
	 * database.
	 *
	 * @param dataSource The data source, never <code>null</code>.
	 */
	public void setDataSource(DataSource dataSource);

	public FilterFactory getFilterFactory();

	FilterToSQL createFilterToSQL(SimpleFeatureType featureType);

	public boolean isView(DatabaseMetaData metaData, String databaseSchema,
			String tableName) throws SQLException;

	public Map<String, VirtualTable> getVirtualTables();

	public void closeSafe(ResultSet uniqueIndex);
	
	public void closeSafe(Statement st);

	public void closeSafe(Connection cx);
	
	public Integer getMapping(Class<?> clazz);

	public void encodeTableName(String tableName, StringBuffer sql,
			Hints hints) throws SQLException;


	public Class getColumnType(DatabaseMetaData metaData, String schema,
			String table, String colName) throws SQLException;

	public String getDatabaseSchema();

	public Logger getLogger();

	
	 public Class<?> getMapping(int sqlType);

	public GeometryFactory getGeometryFactory();

	public PrimaryKeyFinder getPrimaryKeyFinder();



	 
	
	

}
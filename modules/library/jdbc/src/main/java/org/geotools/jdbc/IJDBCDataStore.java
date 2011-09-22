package org.geotools.jdbc;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultQuery;
import org.geotools.data.GmlObjectStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentState;
import org.geotools.factory.Hints;
import org.geotools.filter.FilterCapabilities;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.JDBCFeatureReader.ResultSetFeature;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public interface IJDBCDataStore  extends  DataStore, GmlObjectStore {
	
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
	
	public String[] getTypeNames() throws IOException;
	
	public List<Name> createTypeNames() throws IOException;

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

	public String getNamespaceURI();

	public ContentFeatureSource getFeatureSource(String tname,
			Transaction autoCommit) throws IOException;

    
    ContentFeatureSource getFeatureSource(String typeName) throws IOException;

    public void setDatabaseSchema(String databaseSchema);

	public Connection createConnection();

	public Transaction buildTransaction(Connection conn);

	public void setAssociations(boolean b);

	public FilterCapabilities getFilterCapabilities();

	public void addVirtualTable(VirtualTable vt) throws IOException;

	public PrimaryKey getPrimaryKey(SimpleFeatureType featureType) throws IOException;

	public Connection getConnection(Transaction autoCommit) throws IOException;

	public Class<?> getMapping(String sqlTypeName);

	public Connection getConnection(JDBCState state) throws IOException;

	public String selectSQL(SimpleFeatureType featureType, Query query) throws IOException, SQLException;

	public PreparedStatement selectSQLPS( SimpleFeatureType featureType, Query query, Connection cx )
        throws SQLException, IOException;

	public ReferencedEnvelope getBounds(SimpleFeatureType featureType, Query query, Connection cx)
            throws IOException;

	public void releaseConnection(Connection cx, JDBCState state);

	public int getCount(SimpleFeatureType featureType, Query query, Connection cx)
	        throws IOException;

	public String selectRelationshipSQL(String tableName, String name) throws SQLException;

	public Statement selectRelationshipSQLPS(String tableName, String name,
			Connection cx) throws SQLException;

	public boolean isAssociations();

	public void ensureAssociationTablesExist(Connection cx) throws IOException, SQLException;

	public PrimaryKey getPrimaryKey(ContentEntry entry) throws IOException;

	public void delete(SimpleFeatureType featureType, Filter preFilter,
			Connection cx) throws IOException;

	public void ensureAuthorization(SimpleFeatureType featureType,
			Filter preFilter, Transaction transaction, Connection cx) throws IOException, SQLException;

	public void update(SimpleFeatureType schema,
			AttributeDescriptor[] innerTypes, Object[] values,
			Filter preFilter, Connection cx) throws IOException, SQLException;

	public Object getAggregateValue(FeatureVisitor visitor,
			SimpleFeatureType featureType, Query query, Connection cx)
			throws IOException;

	public void setPrimaryKeyFinder(PrimaryKeyFinder keyFinder);

	public List<Object> decodeFID(PrimaryKey key, String fid, boolean b);

	public String encodeFID(PrimaryKey key, ResultSet rs) throws SQLException, IOException;

	public LinkedHashSet<String> getColumnNames(PrimaryKey pkey);

	public FeatureFactory getFeatureFactory();

	public FeatureTypeFactory getFeatureTypeFactory();

	public Statement selectAssociationSQLPS(String fid, Connection cx) throws SQLException;

	public void setGmlProperties(Geometry g, String gid, String name, String description);

	public String selectAssociationSQL(String fid) throws SQLException;

	public String selectGeometrySQL(String mgid) throws SQLException;

	public Statement selectGeometrySQLPS(String mgid, Connection cx) throws SQLException;

	public String selectMultiGeometrySQL(String gid) throws SQLException;

	public Statement selectMultiGeometrySQLPS(String gid, Connection cx) throws SQLException;

	public PreparedStatement selectGeometryAssociationSQLPS(String fid, String gid, String gname, Connection cx) throws SQLException;


	public String selectGeometryAssociationSQL(String fid, String gid, String gname) throws SQLException;

	public int getFetchSize();


	public void insert(SimpleFeature feature, SimpleFeatureType featureType, Connection cx)
	        throws IOException;

	    public void insert(Collection<SimpleFeature> features, SimpleFeatureType featureType, Connection cx)
	        throws IOException;

		public void delete(SimpleFeatureType featureType, String id,
				Connection connection) throws IOException;

	    
		public void update(SimpleFeatureType featureType, List<AttributeDescriptor> attributes,
		        List<Object> values, Filter filter, Connection cx)
		        throws IOException, SQLException;

        public void encodeGeometryColumn(GeometryDescriptor gatt, StringBuffer temp, Hints hints);

        public FilterToSQL createPreparedFilterToSQL(SimpleFeatureType ft);

        public void applyLimitOffset(StringBuffer sql, Query  query);

        public void setPreparedFilterValues(PreparedStatement ps,
                PreparedFilterToSQL preparedFilterToSQL, int i, Connection cx) throws SQLException;

        public void setDataStoreFactory( JDBCDataStoreFactory dataStoreFactory);

        public void setFeatureTypeFactory(FeatureTypeFactory typeFactory);

        public void setFeatureFactory(FeatureFactory featureFactory);

        public void setFilterFactory(FilterFactory filterFactory);

        public void setGeometryFactory(GeometryFactory geometryFactory);

        public DataStoreFactorySpi getDataStoreFactory();

        public void setDataStoreFactory(DataStoreFactorySpi dataStoreFactory);

        public void setNamespaceURI(String namespaceURI);

        public ContentEntry getEntry(Name name);

        public ContentState createContentState(ContentEntry entry);



		
		



}
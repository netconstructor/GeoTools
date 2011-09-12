/**
 * 
 */
package org.geotools.jdbc.versioning;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.geogit.api.AddOp;
import org.geogit.api.BranchCreateOp;
import org.geogit.api.BranchDeleteOp;
import org.geogit.api.CheckoutOp;
import org.geogit.api.CommitOp;
import org.geogit.api.DiffOp;
import org.geogit.api.GeoGIT;
import org.geogit.api.LogOp;
import org.geogit.api.MergeOp;
import org.geogit.api.ShowOp;
import org.geogit.repository.Repository;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.GmlObjectStore;
import org.geotools.data.Query;
import org.geotools.data.ServiceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentState;
import org.geotools.factory.Hints;
import org.geotools.jdbc.IJDBCDataStore;
//import org.geotools.jdbc.JDBCDataStore;
//import org.geotools.jdbc.JDBCDataStoreFactory;
//import org.geotools.jdbc.JDBCFeatureSource;
//import org.geotools.jdbc.JDBCFeatureStore;
import org.geotools.jdbc.JDBCState;
import org.geotools.jdbc.PrimaryKey;
import org.geotools.jdbc.PrimaryKeyFinder;
import org.geotools.jdbc.SQLDialect;
import org.geotools.jdbc.VirtualTable;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.GmlObjectId;
import org.opengis.util.ProgressListener;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.FilterCapabilities;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.data.simple.SimpleFeatureSource;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * @author wdeane
 *
 */
public final class GeoGITWrappingDataStore extends ContentDataStore implements
		IJDBCDataStore, GmlObjectStore {

	/**
	 * @param sqlTypeName
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getMapping(java.lang.String)
	 */
	public Class<?> getMapping(String sqlTypeName) {
		return datastore.getMapping(sqlTypeName);
	}

	/**
	 * @param state
	 * @return
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#getConnection(org.geotools.jdbc.JDBCState)
	 */
	public Connection getConnection(JDBCState state) throws IOException {
		return datastore.getConnection(state);
	}

	/**
	 * @param visitor
	 * @param schema
	 * @param query
	 * @param cx
	 * @return
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#getAggregateValue(org.opengis.feature.FeatureVisitor, org.opengis.feature.simple.SimpleFeatureType, org.geotools.data.Query, java.sql.Connection)
	 */
	public Object getAggregateValue(FeatureVisitor visitor,
			SimpleFeatureType schema, Query query, Connection cx)
			throws IOException {
		return datastore.getAggregateValue(visitor, schema, query, cx);
	}

	/**
	 * @param featureType
	 * @param query
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @see org.geotools.jdbc.IJDBCDataStore#selectSQL(org.opengis.feature.simple.SimpleFeatureType, org.geotools.data.Query)
	 */
	public String selectSQL(SimpleFeatureType featureType, Query query)
			throws IOException, SQLException {
		return datastore.selectSQL(featureType, query);
	}

	/**
	 * @param featureType
	 * @param query
	 * @param cx
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#selectSQLPS(org.opengis.feature.simple.SimpleFeatureType, org.geotools.data.Query, java.sql.Connection)
	 */
	public PreparedStatement selectSQLPS(SimpleFeatureType featureType,
			Query query, Connection cx) throws SQLException, IOException {
		return datastore.selectSQLPS(featureType, query, cx);
	}

	/**
	 * @param featureType
	 * @param query
	 * @param cx
	 * @return
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#getBounds(org.opengis.feature.simple.SimpleFeatureType, org.geotools.data.Query, java.sql.Connection)
	 */
	public ReferencedEnvelope getBounds(SimpleFeatureType featureType,
			Query query, Connection cx) throws IOException {
		return datastore.getBounds(featureType, query, cx);
	}

	/**
	 * @param cx
	 * @param state
	 * @see org.geotools.jdbc.IJDBCDataStore#releaseConnection(java.sql.Connection, org.geotools.jdbc.JDBCState)
	 */
	public void releaseConnection(Connection cx, JDBCState state) {
		datastore.releaseConnection(cx, state);
	}

	/**
	 * @param featureType
	 * @param query
	 * @param cx
	 * @return
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#getCount(org.opengis.feature.simple.SimpleFeatureType, org.geotools.data.Query, java.sql.Connection)
	 */
	public int getCount(SimpleFeatureType featureType, Query query,
			Connection cx) throws IOException {
		return datastore.getCount(featureType, query, cx);
	}

	/**
	 * @param tableName
	 * @param name
	 * @return
	 * @throws SQLException
	 * @see org.geotools.jdbc.IJDBCDataStore#selectRelationshipSQL(java.lang.String, java.lang.String)
	 */
	public String selectRelationshipSQL(String tableName, String name)
			throws SQLException {
		return datastore.selectRelationshipSQL(tableName, name);
	}

	/**
	 * @param tableName
	 * @param name
	 * @param cx
	 * @return
	 * @throws SQLException
	 * @see org.geotools.jdbc.IJDBCDataStore#selectRelationshipSQLPS(java.lang.String, java.lang.String, java.sql.Connection)
	 */
	public Statement selectRelationshipSQLPS(String tableName, String name,
			Connection cx) throws SQLException {
		return datastore.selectRelationshipSQLPS(tableName, name, cx);
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#isAssociations()
	 */
	public boolean isAssociations() {
		return datastore.isAssociations();
	}

	/**
	 * @param cx
	 * @throws IOException
	 * @throws SQLException
	 * @see org.geotools.jdbc.IJDBCDataStore#ensureAssociationTablesExist(java.sql.Connection)
	 */
	public void ensureAssociationTablesExist(Connection cx) throws IOException,
			SQLException {
		datastore.ensureAssociationTablesExist(cx);
	}

	/**
	 * @param entry
	 * @return
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#getPrimaryKey(org.geotools.data.store.ContentEntry)
	 */
	public PrimaryKey getPrimaryKey(ContentEntry entry) throws IOException {
		return datastore.getPrimaryKey(entry);
	}

	/**
	 * @param autoCommit
	 * @return
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#getConnection(org.geotools.data.Transaction)
	 */
	public Connection getConnection(Transaction autoCommit) throws IOException {
		return datastore.getConnection(autoCommit);
	}

	/**
	 * @param geoGIT
	 * @param datastore
	 */
	public GeoGITWrappingDataStore(GeoGIT geoGIT, IJDBCDataStore datastore) {
		super();
		this.geoGIT = geoGIT;
		this.datastore = datastore;
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getNamespaceURI()
	 */
	public String getNamespaceURI() {
		return datastore.getNamespaceURI();
	}


	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#getFeatureTypeFactory()
	 */
	@Override
	public FeatureTypeFactory getFeatureTypeFactory() {
		// TODO Auto-generated method stub
		return super.getFeatureTypeFactory();
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#setFeatureTypeFactory(org.opengis.feature.type.FeatureTypeFactory)
	 */
	@Override
	public void setFeatureTypeFactory(FeatureTypeFactory typeFactory) {
		// TODO Auto-generated method stub
		super.setFeatureTypeFactory(typeFactory);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#setFeatureFactory(org.opengis.feature.FeatureFactory)
	 */
	@Override
	public void setFeatureFactory(FeatureFactory featureFactory) {
		// TODO Auto-generated method stub
		super.setFeatureFactory(featureFactory);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#getFeatureFactory()
	 */
	@Override
	public FeatureFactory getFeatureFactory() {
		// TODO Auto-generated method stub
		return super.getFeatureFactory();
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#setFilterFactory(org.opengis.filter.FilterFactory)
	 */
	@Override
	public void setFilterFactory(FilterFactory filterFactory) {
		// TODO Auto-generated method stub
		super.setFilterFactory(filterFactory);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#setGeometryFactory(com.vividsolutions.jts.geom.GeometryFactory)
	 */
	@Override
	public void setGeometryFactory(GeometryFactory geometryFactory) {
		// TODO Auto-generated method stub
		super.setGeometryFactory(geometryFactory);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#getDataStoreFactory()
	 */
	@Override
	public DataStoreFactorySpi getDataStoreFactory() {
		// TODO Auto-generated method stub
		return super.getDataStoreFactory();
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#setDataStoreFactory(org.geotools.data.DataStoreFactorySpi)
	 */
	@Override
	public void setDataStoreFactory(DataStoreFactorySpi dataStoreFactory) {
		// TODO Auto-generated method stub
		super.setDataStoreFactory(dataStoreFactory);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#setNamespaceURI(java.lang.String)
	 */
	@Override
	public void setNamespaceURI(String namespaceURI) {
		// TODO Auto-generated method stub
		super.setNamespaceURI(namespaceURI);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#getFeatureSource(org.opengis.feature.type.Name, org.geotools.data.Transaction)
	 */
	@Override
	public ContentFeatureSource getFeatureSource(Name typeName, Transaction tx)
			throws IOException {
		// TODO Auto-generated method stub
		ContentFeatureSource fs = super.getFeatureSource(typeName, tx);
		return new VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature>(fs);
		
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#getEntry(org.opengis.feature.type.Name)
	 */
	@Override
	public ContentEntry getEntry(Name name) {
		// TODO Auto-generated method stub
		return super.getEntry(name);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.store.ContentDataStore#createContentState(org.geotools.data.store.ContentEntry)
	 */
	@Override
	protected ContentState createContentState(ContentEntry entry) {
		// TODO Auto-generated method stub
		return super.createContentState(entry);
	}

	/**
	 * @param typeName
	 * @return
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#getFeatureSource(java.lang.String)
	 */
	public ContentFeatureSource getFeatureSource(String typeName)
			throws IOException {
		return datastore.getFeatureSource(typeName);
	}

	/**
	 * @param databaseSchema
	 * @see org.geotools.jdbc.IJDBCDataStore#setDatabaseSchema(java.lang.String)
	 */
	public void setDatabaseSchema(String databaseSchema) {
		datastore.setDatabaseSchema(databaseSchema);
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#createConnection()
	 */
	public Connection createConnection() {
		return datastore.createConnection();
	}

	/**
	 * @param conn
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#buildTransaction(java.sql.Connection)
	 */
	public Transaction buildTransaction(Connection conn) {
		return datastore.buildTransaction(conn);
	}

	/**
	 * @param b
	 * @see org.geotools.jdbc.IJDBCDataStore#setAssociations(boolean)
	 */
	public void setAssociations(boolean b) {
		datastore.setAssociations(b);
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getFilterCapabilities()
	 */
	public FilterCapabilities getFilterCapabilities() {
		return datastore.getFilterCapabilities();
	}

	/**
	 * @param vt
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#addVirtualTable(org.geotools.jdbc.VirtualTable)
	 */
	public void addVirtualTable(VirtualTable vt) throws IOException {
		datastore.addVirtualTable(vt);
	}

	/**
	 * @param schema
	 * @return
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#getPrimaryKey(org.opengis.feature.simple.SimpleFeatureType)
	 */
	public PrimaryKey getPrimaryKey(SimpleFeatureType schema)
			throws IOException {
		return datastore.getPrimaryKey(schema);
	}

	private GeoGIT geoGIT;
	private IJDBCDataStore datastore;
	
	/**
	 * 
	 */
	protected void ensureSyncDataStore(){
		
	}
	
	/**
	 * 
	 */
	protected void ensureSyncGeoGIT(){
		
	}

	/**
	 * @return
	 * @see org.geogit.api.GeoGIT#getRepository()
	 */
	public Repository getRepository() {
		return geoGIT.getRepository();
	}

	/**
	 * @return
	 * @see org.geogit.api.GeoGIT#add()
	 */
	public AddOp add() {
		return geoGIT.add();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return geoGIT.hashCode();
	}

	/**
	 * @param user
	 * @param typeName
	 * @param affectedFeatures
	 * @param progressListener
	 * @return
	 * @see org.geogit.api.GeoGIT#rm(java.lang.String, org.opengis.feature.type.Name, org.geotools.feature.FeatureCollection, org.opengis.util.ProgressListener)
	 */
	public String rm(String user, Name typeName,
			FeatureCollection<SimpleFeatureType, SimpleFeature> affectedFeatures,
			ProgressListener progressListener) {
		return geoGIT.rm(user, typeName, affectedFeatures, progressListener);
	}

	/**
	 * @param user
	 * @param typeName
	 * @param changedProperties
	 * @param affectedFeatures
	 * @param progressListener
	 * @return
	 * @see org.geogit.api.GeoGIT#update(java.lang.String, org.opengis.feature.type.Name, java.util.List, org.geotools.feature.FeatureCollection, org.opengis.util.ProgressListener)
	 */
	public String update(String user, Name typeName,
			List<PropertyName> changedProperties,
			FeatureCollection<SimpleFeatureType, SimpleFeature> affectedFeatures,
			ProgressListener progressListener) {
		return geoGIT.update(user, typeName, changedProperties,
				affectedFeatures, progressListener);
	}

	/**
	 * @return
	 * @see org.geogit.api.GeoGIT#commit()
	 */
	public CommitOp commit() {
		return geoGIT.commit();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return geoGIT.equals(obj);
	}

	/**
	 * @return
	 * @see org.geogit.api.GeoGIT#branchCreate()
	 */
	public BranchCreateOp branchCreate() {
		return geoGIT.branchCreate();
	}

	/**
	 * @return
	 * @see org.geogit.api.GeoGIT#branchDelete()
	 */
	public BranchDeleteOp branchDelete() {
		return geoGIT.branchDelete();
	}

	/**
	 * @return
	 * @see org.geogit.api.GeoGIT#checkout()
	 */
	public CheckoutOp checkout() {
		return geoGIT.checkout();
	}

	/**
	 * @return
	 * @see org.geogit.api.GeoGIT#diff()
	 */
	public DiffOp diff() {
		return geoGIT.diff();
	}

	/**
	 * 
	 * @see org.geogit.api.GeoGIT#fetch()
	 */
	public void fetch() {
		geoGIT.fetch();
	}

	/**
	 * 
	 * @see org.geogit.api.GeoGIT#init()
	 */
	public void init() {
		geoGIT.init();
	}

	/**
	 * @return
	 * @see org.geogit.api.GeoGIT#log()
	 */
	public LogOp log() {
		return geoGIT.log();
	}

	/**
	 * @return
	 * @see org.geogit.api.GeoGIT#merge()
	 */
	public MergeOp merge() {
		return geoGIT.merge();
	}

	/**
	 * 
	 * @see org.geogit.api.GeoGIT#pull()
	 */
	public void pull() {
		geoGIT.pull();
	}

	/**
	 * 
	 * @see org.geogit.api.GeoGIT#push()
	 */
	public void push() {
		geoGIT.push();
	}

	/**
	 * 
	 * @see org.geogit.api.GeoGIT#rebase()
	 */
	public void rebase() {
		geoGIT.rebase();
	}

	/**
	 * 
	 * @see org.geogit.api.GeoGIT#reset()
	 */
	public void reset() {
		geoGIT.reset();
	}

	/**
	 * @return
	 * @see org.geogit.api.GeoGIT#show()
	 */
	public ShowOp show() {
		return geoGIT.show();
	}

	/**
	 * 
	 * @see org.geogit.api.GeoGIT#status()
	 */
	public void status() {
		geoGIT.status();
	}

	/**
	 * 
	 * @see org.geogit.api.GeoGIT#tag()
	 */
	public void tag() {
		geoGIT.tag();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return geoGIT.toString();
	}

	/**
	 * @param id
	 * @param hints
	 * @return
	 * @throws IOException
	 * @see org.geotools.data.GmlObjectStore#getGmlObject(org.opengis.filter.identity.GmlObjectId, org.geotools.factory.Hints)
	 */
	public Object getGmlObject(GmlObjectId id, Hints hints) throws IOException {
		return datastore.getGmlObject(id, hints);
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getSQLDialect()
	 */
	public SQLDialect getSQLDialect() {
		return datastore.getSQLDialect();
	}

	/**
	 * @param dialect
	 * @see org.geotools.jdbc.IJDBCDataStore#setSQLDialect(org.geotools.jdbc.SQLDialect)
	 */
	public void setSQLDialect(SQLDialect dialect) {
		datastore.setSQLDialect(dialect);
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getDataSource()
	 */
	public DataSource getDataSource() {
		return datastore.getDataSource();
	}

	/**
	 * @param dataSource
	 * @see org.geotools.jdbc.IJDBCDataStore#setDataSource(javax.sql.DataSource)
	 */
	public void setDataSource(DataSource dataSource) {
		datastore.setDataSource(dataSource);
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getFilterFactory()
	 */
	public FilterFactory getFilterFactory() {
		return datastore.getFilterFactory();
	}

	/**
	 * @param featureType
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#createFilterToSQL(org.opengis.feature.simple.SimpleFeatureType)
	 */
	public FilterToSQL createFilterToSQL(SimpleFeatureType featureType) {
		return datastore.createFilterToSQL(featureType);
	}

	/**
	 * @param metaData
	 * @param databaseSchema
	 * @param tableName
	 * @return
	 * @throws SQLException
	 * @see org.geotools.jdbc.IJDBCDataStore#isView(java.sql.DatabaseMetaData, java.lang.String, java.lang.String)
	 */
	public boolean isView(DatabaseMetaData metaData, String databaseSchema,
			String tableName) throws SQLException {
		return datastore.isView(metaData, databaseSchema, tableName);
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getVirtualTables()
	 */
	public Map<String, VirtualTable> getVirtualTables() {
		return datastore.getVirtualTables();
	}

	/**
	 * @param uniqueIndex
	 * @see org.geotools.jdbc.IJDBCDataStore#closeSafe(java.sql.ResultSet)
	 */
	public void closeSafe(ResultSet uniqueIndex) {
		datastore.closeSafe(uniqueIndex);
	}

	/**
	 * @param st
	 * @see org.geotools.jdbc.IJDBCDataStore#closeSafe(java.sql.Statement)
	 */
	public void closeSafe(Statement st) {
		datastore.closeSafe(st);
	}

	/**
	 * @param cx
	 * @see org.geotools.jdbc.IJDBCDataStore#closeSafe(java.sql.Connection)
	 */
	public void closeSafe(Connection cx) {
		datastore.closeSafe(cx);
	}

	/**
	 * @param clazz
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getMapping(java.lang.Class)
	 */
	public Integer getMapping(Class clazz) {
		return datastore.getMapping(clazz);
	}
	/**
	 * @param tableName
	 * @param sql
	 * @param hints
	 * @throws SQLException
	 * @see org.geotools.jdbc.IJDBCDataStore#encodeTableName(java.lang.String, java.lang.StringBuffer, org.geotools.factory.Hints)
	 */
	public void encodeTableName(String tableName, StringBuffer sql, Hints hints)
			throws SQLException {
		datastore.encodeTableName(tableName, sql, hints);
	}

	/**
	 * @param metaData
	 * @param schema
	 * @param table
	 * @param colName
	 * @return
	 * @throws SQLException
	 * @see org.geotools.jdbc.IJDBCDataStore#getColumnType(java.sql.DatabaseMetaData, java.lang.String, java.lang.String, java.lang.String)
	 */
	public Class getColumnType(DatabaseMetaData metaData, String schema,
			String table, String colName) throws SQLException {
		return datastore.getColumnType(metaData, schema, table, colName);
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getDatabaseSchema()
	 */
	public String getDatabaseSchema() {
		return datastore.getDatabaseSchema();
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getLogger()
	 */
	public Logger getLogger() {
		return datastore.getLogger();
	}

	/**
	 * @param sqlType
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getMapping(int)
	 */
	public Class<?> getMapping(int sqlType) {
		return datastore.getMapping(sqlType);
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getGeometryFactory()
	 */
	public GeometryFactory getGeometryFactory() {
		return datastore.getGeometryFactory();
	}

	/**
	 * @return
	 * @see org.geotools.jdbc.IJDBCDataStore#getPrimaryKeyFinder()
	 */
	public PrimaryKeyFinder getPrimaryKeyFinder() {
		return datastore.getPrimaryKeyFinder();
	}

	/**
	 * @param typeName
	 * @return
	 * @throws IOException
	 * @see org.geotools.data.DataStore#getFeatureSource(org.opengis.feature.type.Name)
	 */
	public SimpleFeatureSource getFeatureSource(Name typeName)
			throws IOException {
		return datastore.getFeatureSource(typeName);
	}

	/**
	 * @return
	 * @see org.geotools.data.DataAccess#getInfo()
	 */
	public ServiceInfo getInfo() {
		return datastore.getInfo();
	}

	/**
	 * @param query
	 * @param transaction
	 * @return
	 * @throws IOException
	 * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query, org.geotools.data.Transaction)
	 */
	public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(
			Query query, Transaction transaction) throws IOException {
		return datastore.getFeatureReader(query, transaction);
	}

	/**
	 * @param featureType
	 * @throws IOException
	 * @see org.geotools.data.DataAccess#createSchema(org.opengis.feature.type.FeatureType)
	 */
	public void createSchema(SimpleFeatureType featureType) throws IOException {
		datastore.createSchema(featureType);
	}

	/**
	 * @param typeName
	 * @param featureType
	 * @throws IOException
	 * @see org.geotools.data.DataAccess#updateSchema(org.opengis.feature.type.Name, org.opengis.feature.type.FeatureType)
	 */
	public void updateSchema(Name typeName, SimpleFeatureType featureType)
			throws IOException {
		datastore.updateSchema(typeName, featureType);
	}

	/**
	 * @return
	 * @throws IOException
	 * @see org.geotools.data.DataAccess#getNames()
	 */
	public List<Name> getNames() throws IOException {
		return datastore.getNames();
	}

	/**
	 * @param typeName
	 * @param filter
	 * @param transaction
	 * @return
	 * @throws IOException
	 * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, org.opengis.filter.Filter, org.geotools.data.Transaction)
	 */
	public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(
			String typeName, Filter filter, Transaction transaction)
			throws IOException {
		return datastore.getFeatureWriter(typeName, filter, transaction);
	}

	/**
	 * @param name
	 * @return
	 * @throws IOException
	 * @see org.geotools.data.DataAccess#getSchema(org.opengis.feature.type.Name)
	 */
	public SimpleFeatureType getSchema(Name name) throws IOException {
		return datastore.getSchema(name);
	}

	/**
	 * 
	 * @see org.geotools.data.DataAccess#dispose()
	 */
	public void dispose() {
		datastore.dispose();
	}


    /**
     * Creates a new instance of {@link VersionedJDBCFeatureStore}.
     *
     * @see ContentDataStore#createFeatureSource(ContentEntry)
     */
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        // grab the schema, it carries a flag telling us if the feature type is read only
        SimpleFeatureType schema = entry.getState(Transaction.AUTO_COMMIT).getFeatureType();
        if (schema == null) {
            // if the schema still haven't been computed, force its computation so
            // that we can decide if the feature type is read only
            schema = new VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature>(entry, null).buildFeatureType();
            entry.getState(Transaction.AUTO_COMMIT).setFeatureType(schema);
        }

        Object readOnlyMarker = schema.getUserData().get(JDBC_READ_ONLY);
        if (Boolean.TRUE.equals(readOnlyMarker)) {
            return new VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature>(entry, null);
        }
        return new VersionedJDBCFeatureStore<SimpleFeatureType, SimpleFeature>(entry, null);
    }

	@Override
	public List<Name> createTypeNames() throws IOException {
		return this.datastore.createTypeNames();
	}

	/**
	 * @param tname
	 * @param autoCommit
	 * @return
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#getFeatureSource(java.lang.String, org.geotools.data.Transaction)
	 */
	public ContentFeatureSource getFeatureSource(String tname,
			Transaction autoCommit) throws IOException {
		return datastore.getFeatureSource(tname, autoCommit);
	}

	/**
	 * @param featureType
	 * @param preFilter
	 * @param cx
	 * @throws IOException
	 * @see org.geotools.jdbc.IJDBCDataStore#delete(org.opengis.feature.simple.SimpleFeatureType, org.opengis.filter.Filter, java.sql.Connection)
	 */
	public void delete(SimpleFeatureType featureType, Filter preFilter,
			Connection cx) throws IOException {
		datastore.delete(featureType, preFilter, cx);
	}

	/**
	 * @param featureType
	 * @param preFilter
	 * @param transaction
	 * @param cx
	 * @throws IOException
	 * @throws SQLException
	 * @see org.geotools.jdbc.IJDBCDataStore#ensureAuthorization(org.opengis.feature.simple.SimpleFeatureType, org.opengis.filter.Filter, org.geotools.data.Transaction, java.sql.Connection)
	 */
	public void ensureAuthorization(SimpleFeatureType featureType,
			Filter preFilter, Transaction transaction, Connection cx)
			throws IOException, SQLException {
		datastore.ensureAuthorization(featureType, preFilter, transaction, cx);
	}

	/**
	 * @param schema
	 * @param innerTypes
	 * @param values
	 * @param preFilter
	 * @param cx
	 * @throws IOException
	 * @throws SQLException
	 * @see org.geotools.jdbc.IJDBCDataStore#update(org.opengis.feature.simple.SimpleFeatureType, org.opengis.feature.type.AttributeDescriptor[], java.lang.Object[], org.opengis.filter.Filter, java.sql.Connection)
	 */
	public void update(SimpleFeatureType schema,
			AttributeDescriptor[] innerTypes, Object[] values,
			Filter preFilter, Connection cx) throws IOException, SQLException {
		datastore.update(schema, innerTypes, values, preFilter, cx);
	}

}

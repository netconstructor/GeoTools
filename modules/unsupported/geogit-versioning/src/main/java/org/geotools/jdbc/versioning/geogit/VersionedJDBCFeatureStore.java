/**
 * 
 */
package org.geotools.jdbc.versioning.geogit;

import java.awt.RenderingHints;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.geogit.api.GeoGIT;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.FeatureEvent.Type;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.data.store.ContentState;
import org.geotools.factory.Hints;
import org.geotools.factory.Hints.Key;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.IJDBCDataStore;

import org.geotools.jdbc.IJDBCFeatureSource;
import org.geotools.jdbc.JDBCInsertFeatureWriter;
import org.geotools.jdbc.JDBCState;
import org.geotools.jdbc.JDBCUpdateFeatureWriter;
import org.geotools.jdbc.JDBCUpdateInsertFeatureWriter;
import org.geotools.jdbc.PreparedStatementSQLDialect;
import org.geotools.jdbc.PrimaryKey;
import org.geotools.jdbc.VersioningJDBCFeatureSource;
import org.geotools.jdbc.versioning.VersioningFeatureSource;
import org.geotools.jdbc.versioning.VersioningFeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author wdeane
 * @param <T>
 * @param <F>
 *
 */

@SuppressWarnings("unchecked")
public class VersionedJDBCFeatureStore<T extends SimpleFeatureType, F extends SimpleFeature>  extends ContentFeatureStore implements VersioningFeatureStore<SimpleFeatureType, SimpleFeature> {
    
    @SuppressWarnings("deprecation")
	private static final Query QUERY_NONE = new DefaultQuery(null, Filter.EXCLUDE); 
    private GeoGITFacade ggit;
    
    /**
     * jdbc feature source to delegate to, we do this b/c we can't inherit from
     * both ContentFeatureStore and JDBCFeatureSource at the same time
     */
    VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature> delegate;
    
    /**
     * Creates the new feature store.
     * @param entry The datastore entry.
     * @param query The defining query.
     */
    @SuppressWarnings("unchecked")
	public VersionedJDBCFeatureStore(ContentEntry entry,Query query, GeoGITFacade ggit2) throws IOException {
        super(entry,query);
        
        delegate = new VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature>( entry, query, ggit2 ) {
            @Override
            public void setTransaction(Transaction transaction) {
                super.setTransaction(transaction);
                
                //keep this feature store in sync
                VersionedJDBCFeatureStore.this.setTransaction(transaction);
            }
        };
        
    	Set jdbcHints = new HashSet();      		
    	jdbcHints.addAll(delegate.getSupportedHints());
    	getJDBCDataStore().getSQLDialect().addSupportedHints(jdbcHints);
    	hints=Collections.unmodifiableSet(jdbcHints);    	
    }

    public VersionedJDBCFeatureStore(ContentFeatureSource fs) {
		// TODO Auto-generated constructor stub
    	super(fs.getEntry(), null);
	}


	public IJDBCDataStore getJDBCDataStore() {
        return delegate.getDataStore();
    }

    @Override
    public ContentEntry getEntry() {
        return delegate.getEntry();
    }

    @Override
    public ResourceInfo getInfo() {
        return delegate.getInfo();
    }

    @Override
    public Name getName() {
        return delegate.getName();
    }

    @Override
    public QueryCapabilities getQueryCapabilities() {
        return delegate.getQueryCapabilities();
        
    }

    @Override
    public JDBCState getState() {
        return delegate.getState();
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void setTransaction(Transaction transaction) {
        //JD: note, we need to set both super and delegate transactions.
        super.setTransaction(transaction);

        //JD: this guard ensures that a recursive loop will not form
        if ( delegate.getTransaction() != transaction ) {
            delegate.setTransaction(transaction);    
        }
    }
    
    public PrimaryKey getPrimaryKey() {
        return delegate.getPrimaryKey();
    }

    /**
     * Sets the flag which will expose columns which compose a tables identifying or primary key,
     * through feature type attributes. 
     * <p>
     * Note: setting this flag which affect all feature sources created from or working against 
     * the current transaction.
     * </p>
     */
    public void setExposePrimaryKeyColumns(boolean exposePrimaryKeyColumns) {
        delegate.setExposePrimaryKeyColumns(exposePrimaryKeyColumns);
    }
    
    /**
     * The flag which will expose columns which compose a tables identifying or primary key,
     * through feature type attributes.
     */
    public boolean isExposePrimaryKeyColumns() {
        return delegate.isExposePrimaryKeyColumns();
    }
    
    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        return delegate.buildFeatureType();
    }
    
    @Override
    protected int getCountInternal(Query query) throws IOException {
        return delegate.getCount(query);
    }
    
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query)
            throws IOException {
        return delegate.getBoundsInternal(query);
    }
    
    @Override
    protected boolean canFilter() {
        return delegate.canFilter();
    }
    
    @Override
    protected boolean canSort() {
        return delegate.canSort();
    }
    
    @Override
    protected boolean canRetype() {
        return delegate.canRetype();
    }
    
    @Override
    protected boolean canLimit() {
        return delegate.canLimit();
    }
    
    @Override
    protected boolean canOffset() {
        return delegate.canOffset();
    }
    
    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
            Query query) throws IOException {
        return delegate.getReaderInternal(query);
    }
    
    @Override
    protected boolean handleVisitor(Query query, FeatureVisitor visitor) throws IOException {
        return delegate.handleVisitor(query, visitor);
    }
    
//  /**
//  * This method operates by delegating to the
//  * {@link JDBCFeatureCollection#update(AttributeDescriptor[], Object[])}
//  * method provided by the feature collection resulting from
//  * {@link #filtered(ContentState, Filter)}.
//  *
//  * @see FeatureStore#modifyFeatures(AttributeDescriptor[], Object[], Filter)
//  */
// public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
//     throws IOException {
//     if (filter == null) {
//         String msg = "Must specify a filter, must not be null.";
//         throw new IllegalArgumentException(msg);
//     }
//
//     JDBCFeatureCollection features = (JDBCFeatureCollection) filtered(getState(), filter);
//     features.update(type, value);
// }
    
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> getWriterInternal(Query query, int flags)
            throws IOException {
        
        if ( flags == 0 ) {
            throw new IllegalArgumentException( "no write flags set" );
        }
        
        //get connection from current state
        Connection cx = getJDBCDataStore().getConnection(getState());
        
        Filter postFilter;
        //check for update only case
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
        try {
            //check for insert only
            if ( (flags | WRITER_ADD) == WRITER_ADD ) {
                DefaultQuery queryNone = new DefaultQuery(query);
                queryNone.setFilter(Filter.EXCLUDE);
                if ( getJDBCDataStore().getSQLDialect() instanceof PreparedStatementSQLDialect ) {
                    PreparedStatement ps = getJDBCDataStore().selectSQLPS(getSchema(), queryNone, cx);
                    return new VersionedJDBCInsertFeatureWriter( ps, cx, delegate, query.getHints() );
                }
                else {
                    //build up a statement for the content, inserting only so we dont want
                    // the query to return any data ==> Filter.EXCLUDE
                    String sql = getJDBCDataStore().selectSQL(getSchema(), queryNone);
                    getJDBCDataStore().getLogger().fine(sql);
    
                    return new VersionedJDBCInsertFeatureWriter( sql, cx, delegate, query.getHints() );
                }
            }
            
            //split the filter
            Filter[] split = delegate.splitFilter(query.getFilter());
            Filter preFilter = split[0];
            postFilter = split[1];
            
            // build up a statement for the content
            DefaultQuery preQuery = new DefaultQuery(query);
            preQuery.setFilter(preFilter);
            if(getJDBCDataStore().getSQLDialect() instanceof PreparedStatementSQLDialect) {
                PreparedStatement ps = getJDBCDataStore().selectSQLPS(getSchema(), preQuery, cx);
                if ( (flags | WRITER_UPDATE) == WRITER_UPDATE ) {
                    writer = new JDBCUpdateFeatureWriter(ps, cx, delegate, query.getHints() );
                } else {
                    //update insert case
                    writer = new VersionedJDBCUpdateInsertFeatureWriter(ps, cx, delegate, query.getPropertyNames(), query.getHints() );
                }
            } else {
                String sql = getJDBCDataStore().selectSQL(getSchema(), preQuery);
                getJDBCDataStore().getLogger().fine(sql);
                
                if ( (flags | WRITER_UPDATE) == WRITER_UPDATE ) {
                    writer = new VersionedJDBCUpdateFeatureWriter( sql, cx, delegate, query.getHints() );
                } else {
                    //update insert case
                    writer = new VersionedJDBCUpdateInsertFeatureWriter( sql, cx, delegate, query.getHints() );
                }
            }
        } catch (Throwable e) { // NOSONAR
            // close the connection
            getJDBCDataStore().closeSafe(cx);
            // safely rethrow
            if (e instanceof Error) {
                throw (Error) e;
            } else {
                throw (IOException) new IOException().initCause(e);
            }
        }
        
        //check for post filter and wrap accordingly
        if ( postFilter != null && postFilter != Filter.INCLUDE ) {
            writer = new VersionedFilteringFeatureWriter( writer, postFilter );
        }
        return writer;
    }
    
    @Override
    public void modifyFeatures(Name[] names, Object[] values, Filter filter)
            throws IOException {
        
        // we cannot trust attribute definitions coming from outside, they might not
        // have the same metadata the inner ones have. Let's remap them
        AttributeDescriptor[] innerTypes = new AttributeDescriptor[names.length];
        for (int i = 0; i < names.length; i++) {
            innerTypes[i] = getSchema().getDescriptor(names[i].getLocalPart());
            if(innerTypes[i] == null)
                throw new IllegalArgumentException("Unknon attribute " + names[i].getLocalPart());
        }
        
        // split the filter
        Filter[] splitted = delegate.splitFilter(filter);
        Filter preFilter = splitted[0];
        Filter postFilter = splitted[1];
        
        if(postFilter != null && !Filter.INCLUDE.equals(postFilter)) {
            // we don't have a fast way to perform this update, let's do it the
            // feature by feature way then
            super.modifyFeatures(innerTypes, values, filter);
        } else {
            // let's grab the connection
            Connection cx = getJDBCDataStore().getConnection(getState());
            
            // we want to support a "batch" update, but we need to be weary of locks
            SimpleFeatureType featureType = getSchema();
             try {
                getJDBCDataStore().ensureAuthorization(featureType, preFilter, getTransaction(), cx);
             }
             catch (SQLException e) {
                 throw (IOException) new IOException().initCause( e );
            }
            ContentState state = getEntry().getState(transaction);
            ReferencedEnvelope bounds = new ReferencedEnvelope( getSchema().getCoordinateReferenceSystem() );
            if( state.hasListener() ){
                // gather bounds before modification
                ReferencedEnvelope before = getBounds( new DefaultQuery( getSchema().getTypeName(), preFilter ) );                
                if( before != null && !before.isEmpty() ){
                    bounds = before;
                }
            }
            try {
                getJDBCDataStore().update(getSchema(), innerTypes, values, preFilter, cx);
            } catch(SQLException e) {
                throw (IOException) (new IOException(e.getMessage()).initCause(e));
            }
            
            if( state.hasListener() ){
                // gather any updated bounds due to a geometry modification
                for( Object value : values ){
                    if( value instanceof Geometry ){
                        Geometry geometry = (Geometry) value;
                        bounds.expandToInclude( geometry.getEnvelopeInternal() );
                    }
                }
                // issue notificaiton
                FeatureEvent event = new FeatureEvent(this, Type.CHANGED, bounds, preFilter );
                state.fireFeatureEvent( event );
            }
        }
    }
    
    @Override
    public void removeFeatures(Filter filter) throws IOException {
        Filter[] splitted = delegate.splitFilter(filter);
        Filter preFilter = splitted[0];
        Filter postFilter = splitted[1];
        
        if(postFilter != null && !Filter.INCLUDE.equals(postFilter)) {
            // we don't have a fast way to perform this delete, let's do it the
            // feature by feature way then
            super.removeFeatures(filter);
        } else {
            // let's grab the connection
            Connection cx = getJDBCDataStore().getConnection(getState());
            
            // we want to support a "batch" delete, but we need to be weary of locks
            SimpleFeatureType featureType = getSchema();
            try {
                getJDBCDataStore().ensureAuthorization(featureType, preFilter, getTransaction(), cx);
            } catch (SQLException e) {
            	throw (IOException) new IOException().initCause( e );
				
			} 
           // catch (IOException e) {
            //    throw (IOException) new IOException().initCause( e );
            //}
            finally {}
            ContentState state = getEntry().getState(transaction);
            ReferencedEnvelope bounds = new ReferencedEnvelope( getSchema().getCoordinateReferenceSystem() );
            if( state.hasListener() ){
                // gather bounds before modification
                ReferencedEnvelope before = getBounds( new DefaultQuery( getSchema().getTypeName(), preFilter ) );                
                if( before != null && !before.isEmpty() ){
                    bounds = before;
                }
            }            
            getJDBCDataStore().delete(featureType, preFilter, cx);
            if( state.hasListener() ){
                // issue notification
                FeatureEvent event = new FeatureEvent(this, Type.REMOVED, bounds, preFilter );
                state.fireFeatureEvent( event );
            }
        }
    }

	@Override
	public void rollback(String toVersion, Filter filter, String[] users)
			throws IOException {
		// TODO Auto-generated method stub
		
		
		
	}
}
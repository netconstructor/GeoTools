/**
 * 
 */
package org.geotools.jdbc.versioning.geogit;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.factory.Hints;
import org.geotools.jdbc.JDBCUpdateInsertFeatureWriter;
import org.geotools.jdbc.PrimaryKey;
import org.geotools.jdbc.versioning.VersioningFeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Id;

/**
 * @author wdeane
 *
 */
public class VersionedJDBCUpdateInsertFeatureWriter extends
		JDBCUpdateInsertFeatureWriter implements
		VersioningFeatureWriter<SimpleFeatureType, SimpleFeature> {


	/**
	 * @param sql
	 * @param cx
	 * @param featureSource
	 * @param hints
	 * @throws SQLException
	 * @throws IOException
	 */
	public VersionedJDBCUpdateInsertFeatureWriter(String sql, Connection cx,
			VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature> featureSource, Hints hints) throws SQLException,
			IOException {
		super(sql, cx, featureSource, hints);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ps
	 * @param cx
	 * @param featureSource
	 * @param attributeNames
	 * @param hints
	 * @throws SQLException
	 * @throws IOException
	 */
	public VersionedJDBCUpdateInsertFeatureWriter(PreparedStatement ps,
			Connection cx,
			VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			String[] attributeNames, Hints hints) throws SQLException,
			IOException {
		super(ps, cx, featureSource, attributeNames, hints);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.jdbc.JDBCUpdateInsertFeatureWriter#remove()
	 */
	@Override
	public void remove() throws IOException {
        if ( inserter != null ) {
            inserter.remove();
            return;
        }
        try {
        	GeoGITFacade ggit = ((VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature>) this.featureSource).getGeoGIT();
            dataStore.delete(featureType, last.getID(), st.getConnection());
            ggit.deleteAndAdd(last);
            ggit.commit();
            // issue notification
            ContentEntry entry = featureSource.getEntry();
            ContentState state = entry.getState( this.tx );
            if( state.hasListener() ){
                state.fireFeatureRemoved( featureSource, last );
            }
        } catch (SQLException e) {
            throw (IOException) new IOException().initCause(e);
        } catch (Exception e) {
        	 throw (IOException) new IOException().initCause(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.geotools.jdbc.JDBCUpdateInsertFeatureWriter#write()
	 */
	@Override
	public void write() throws IOException {
        if ( inserter != null ) {
            inserter.write();
            return;
        }
        
        try {
        	GeoGITFacade ggit = ((VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature>) this.featureSource).getGeoGIT();
            //figure out what the fid is
            PrimaryKey key = dataStore.getPrimaryKey(featureType);
            String fid = dataStore.encodeFID(key, rs);

            Id filter = dataStore.getFilterFactory()
                                 .id(Collections.singleton(dataStore.getFilterFactory()
                                                                    .featureId(fid)));

            //figure out which attributes changed
            List<AttributeDescriptor> changed = new ArrayList<AttributeDescriptor>();
            List<Object> values = new ArrayList<Object>();

            for (AttributeDescriptor att : featureType.getAttributeDescriptors()) {
                if (last.isDirrty(att.getLocalName())) {
                    changed.add(att);
                    values.add(last.getAttribute(att.getLocalName()));
                }
            }

            // do the write
            dataStore.update(featureType, changed, values, filter, st.getConnection());
            ggit.insertAndAdd(last);
            ggit.commit();
            // issue notification
            ContentEntry entry = featureSource.getEntry();
            ContentState state = entry.getState( this.tx );
            if( state.hasListener() ){
                state.fireFeatureUpdated( featureSource, last, lastBounds );
            }
        } catch (Exception e) {
            throw (IOException) new IOException().initCause(e);
        }
	}


}

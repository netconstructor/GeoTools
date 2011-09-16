/**
 * 
 */
package org.geotools.jdbc.versioning.geogit;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.factory.Hints;
import org.geotools.jdbc.JDBCInsertFeatureWriter;
import org.geotools.jdbc.JDBCUpdateFeatureWriter;
import org.geotools.jdbc.versioning.VersioningFeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * @author wdeane
 *
 */
public class VersionedJDBCInsertFeatureWriter extends JDBCInsertFeatureWriter
		implements VersioningFeatureWriter<SimpleFeatureType, SimpleFeature> {

	/**
	 * @param sql
	 * @param cx
	 * @param delegate
	 * @param hints
	 * @throws SQLException
	 * @throws IOException
	 */
	public VersionedJDBCInsertFeatureWriter(String sql, Connection cx,
			VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature> delegate,
			Hints hints) throws SQLException, IOException {
		super(sql, cx, delegate, hints);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ps
	 * @param cx
	 * @param featureSource
	 * @param hints
	 * @throws SQLException
	 * @throws IOException
	 */
	public VersionedJDBCInsertFeatureWriter(PreparedStatement ps,
			Connection cx,
			VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			Hints hints) throws SQLException, IOException {
		super(ps, cx, featureSource, hints);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param other
	 */
	public VersionedJDBCInsertFeatureWriter(JDBCUpdateFeatureWriter other) {
		super(other);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.jdbc.JDBCInsertFeatureWriter#remove()
	 */
	@Override
	public void remove() throws IOException {

		super.remove();
	}

	/* (non-Javadoc)
	 * @see org.geotools.jdbc.JDBCInsertFeatureWriter#write()
	 */
	@Override
	public void write() throws IOException {
        try {
            //do the insert
        	GeoGITFacade ggit = ((VersionedJDBCFeatureSource<SimpleFeatureType, SimpleFeature>) this.featureSource).getGeoGIT();
            dataStore.insert(last, featureType, st.getConnection());
            
            //the datastore sets as userData, grab it and update the fid
            String fid = (String) last.getUserData().get( "fid" );
            last.setID( fid );
            ggit.insertAndAdd(last);
            ggit.commit();
            ContentEntry entry = featureSource.getEntry();
            ContentState state = entry.getState( this.tx );            
            state.fireFeatureAdded( featureSource, last );
        } catch (SQLException e) {
            throw (IOException) new IOException().initCause(e);
		} catch (Exception e) {
			throw (IOException) new IOException().initCause(e);
		}
	}
}

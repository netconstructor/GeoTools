/**
 * 
 */
package org.geotools.jdbc.versioning;

import java.io.IOException;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.jdbc.JDBCFeatureSource;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

/**
 * @author wdeane
 * @param <F>
 * @param <T>
 * @param <F>
 * @param <T>
 *
 */
@SuppressWarnings("unchecked")
public class VersionedJDBCFeatureSource<T extends SimpleFeatureType, F extends SimpleFeature> extends JDBCFeatureSource<SimpleFeatureType, SimpleFeature> implements VersioningFeatureSource<SimpleFeatureType, SimpleFeature> {

	/**
	 * @param entry
	 * @param query
	 * @throws IOException
	 */
	public VersionedJDBCFeatureSource(ContentEntry entry, Query query)
			throws IOException {
		super(entry, query);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param featureSource
	 * @throws IOException
	 */
	public VersionedJDBCFeatureSource(JDBCFeatureSource featureSource)
			throws IOException {
		super(featureSource.getEntry(), featureSource.getQuery());
		// TODO Auto-generated constructor stub
	}

	public VersionedJDBCFeatureSource(ContentFeatureSource fs) throws IOException {
		// TODO Auto-generated constructor stub
		super(fs.getEntry(), fs.getQuery());
	}

	@Override
	public SimpleFeatureCollection getLog(String fromVersion, String toVersion,
			Filter filter, String[] userIds, int maxRows) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rollback(String toVersion, Filter filter, String[] users)
			throws IOException {
		// TODO Auto-generated method stub
		
	}




}

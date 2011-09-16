/**
 * 
 */
package org.geotools.jdbc.versioning.geogit;

import java.io.IOException;

import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureWriter;
import org.geotools.jdbc.versioning.VersioningFeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

/**
 * @author wdeane
 *
 */
public class VersionedFilteringFeatureWriter extends FilteringFeatureWriter
		implements VersioningFeatureWriter<SimpleFeatureType, SimpleFeature> {

	/**
	 * @param writer
	 * @param filter
	 */
	public VersionedFilteringFeatureWriter(
			FeatureWriter<SimpleFeatureType, SimpleFeature> writer,
			Filter filter) {
		super(writer, filter);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FilteringFeatureWriter#remove()
	 */
	@Override
	public void remove() throws IOException {
		// TODO Auto-generated method stub
		super.remove();
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FilteringFeatureWriter#write()
	 */
	@Override
	public void write() throws IOException {
		// TODO Auto-generated method stub
		super.write();
	}

}

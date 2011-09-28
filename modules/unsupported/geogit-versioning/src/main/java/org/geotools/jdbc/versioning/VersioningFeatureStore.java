/**
 * 
 */
package org.geotools.jdbc.versioning;

import java.io.IOException;

import org.geotools.jdbc.IJDBCFeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

/**
 * @author wdeane
 *
 */
public interface VersioningFeatureStore  extends IJDBCFeatureStore<SimpleFeatureType, SimpleFeature> {

	    /**
	     * Rolls back features matching the filter to the state they had on the
	     * specified version.
	     * <p>
	     * For a feature to be included into the rollback it's sufficient that one
	     * of its states between <code>toVersion</code> and current matches the
	     * filter.
	     * 
	     * @param toVersion
	     *            target of the rollback
	     * @param filter
	     *            limits the feature whose history will be rolled back by an OGC
	     *            filter
	     * @param users
	     *            limits the feature whose history will be rolled back, by
	     *            catching only those that have been modified by at least one of
	     *            the specified users. May be null to avoid user filtering.
	     * @throws IOException
	     */
	    public void rollback(String toVersion, Filter filter, String[] users) throws IOException;

	    
}

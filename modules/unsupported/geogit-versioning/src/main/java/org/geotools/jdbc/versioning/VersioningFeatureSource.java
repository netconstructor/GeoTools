/**
 * 
 */
package org.geotools.jdbc.versioning;

import java.io.IOException;

import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

/**
 * @author wdeane
 *
 */
public interface VersioningFeatureSource<T extends SimpleFeatureType, F extends SimpleFeature> extends FeatureSource<SimpleFeatureType, SimpleFeature> {

    /**
     * Returns a log of changes performed between fromVersion and toVersion
     * against the features matched by the specified filter.
     * <p>
     * This is equivalent to gathering the ids of features changed between the
     * two versions and matching the filter, getting a list of revision
     * involving those feaures between fromVersion and toVersion, and then query
     * {@link VersionedPostgisDataStore#TBL_CHANGESETS} against these revision
     * numbers.
     * 
     * @param fromVersion
     *            the start revision
     * @param toVersion
     *            the end revision, may be null to imply the latest one
     * @param filter
     *            will match features whose log will be reported *
     * @param users
     *            limits the features whose log will be returned, by
     *            catching only those that have been modified by at least one of
     *            the specified users. May be null to avoid user filtering.
     * @param maxRows
     *            the maximum number of log rows returned from this call
     * @return a feature collection of the logs, sorted on revision, descending
     * @throws IOException
     */
    public SimpleFeatureCollection getLog(String fromVersion, String toVersion, Filter filter,
            String[] userIds, int maxRows) throws IOException;

	void rollback(String toVersion, Filter filter, String[] users)
			throws IOException;
    
    
}

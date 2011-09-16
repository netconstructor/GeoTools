/**
 * 
 */
package org.geotools.jdbc.versioning;

import org.geotools.data.FeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * @author wdeane
 *
 */
public interface VersioningFeatureWriter<T extends SimpleFeatureType, F extends SimpleFeature> extends FeatureWriter<SimpleFeatureType, SimpleFeature> {

}

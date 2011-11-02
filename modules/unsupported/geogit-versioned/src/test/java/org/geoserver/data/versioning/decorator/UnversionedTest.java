package org.geoserver.data.versioning.decorator;

import java.io.IOException;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;

public class UnversionedTest extends DecoratedTestCase {

    public void testFeatureAccess() throws IOException {
        assertEquals(sampleType, versioned.getSchema(sampleName));
        
        SimpleFeatureSource source = versioned.getFeatureSource(sampleName);
        SimpleFeatureIterator feats = source.getFeatures().features();
        while(feats.hasNext()) {
            SimpleFeature feat = feats.next();
            System.out.println(feat);
        }
        
        Query query = new Query(sampleName);
        query.setVersion("ALL");
        feats = source.getFeatures(query).features();
        while(feats.hasNext()) {
            SimpleFeature feat = feats.next();
            System.out.println(feat);
        }
        
        
    }

}

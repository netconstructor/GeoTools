package org.geoserver.data.versioning.decorator;

import java.io.IOException;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;

public class VersionedTest extends DecoratedTestCase {
    
    public void testNoHistory() throws Exception {
        SimpleFeatureIterator feats = null;
        try {
            SimpleFeatureSource source = versioned.getFeatureSource(sampleName);
            assertNotNull(source);

            Query query = new Query(sampleName);
            query.setVersion("ALL");
            SimpleFeatureCollection collection = source.getFeatures(query);
            assertNotNull(collection);
            List<SimpleFeature> featList = getOriginalFeatures(sampleName);
            assertEquals(featList.size(), collection.size());
            feats = collection.features();
            assertNotNull(feats);
            while(feats.hasNext()) {
                SimpleFeature feat = feats.next();
                assertNotNull(feat);
                LOGGER.info(feat.toString());
                assertTrue(containsFeature(feat, featList));
            }
            LOGGER.info("End testNoHistory");
        } finally {
            if(feats != null)
                feats.close();
        }
    }
    
    public void testWithHistory() throws Exception {
        updateTestFeatures();
        SimpleFeatureIterator feats = null;
        try {
            SimpleFeatureSource source = versioned.getFeatureSource(testName);
            assertNotNull(source);

            Query query = new Query(testName);
            query.setVersion("ALL");
            SimpleFeatureCollection collection = source.getFeatures(query);
            assertNotNull(collection);
            List<SimpleFeature> featList = getAllFeatures(testName);
            LOGGER.info(" " + featList.size());
            LOGGER.info(" " + collection.size());
            assertEquals(featList.size(), collection.size());
            feats = collection.features();
            assertNotNull(feats);
            while(feats.hasNext()) {
                SimpleFeature feat = feats.next();
                assertNotNull(feat);
                LOGGER.info(feat.toString());
                assertTrue(containsFeature(feat, featList));
            }
        } finally {
            if(feats != null)
                feats.close();
        }
    }

}

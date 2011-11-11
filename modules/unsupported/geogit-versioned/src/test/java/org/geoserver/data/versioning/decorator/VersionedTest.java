package org.geoserver.data.versioning.decorator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.FilterFactoryImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

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
    
    public void testFullHistory() throws Exception {
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
    
    public void testBBoxHistory() throws Exception {
        updateTestFeatures();
        SimpleFeatureIterator feats = null;
        try {

            List<SimpleFeature> featList = new ArrayList<SimpleFeature>(4);
            featList.add(test1);
            featList.add(test3);

            SimpleFeatureSource source = versioned.getFeatureSource(testName);
            assertNotNull(source);

            FilterFactory2 ff = new FilterFactoryImpl();
            Filter filter = ff.bbox("ln", -1.5, -1.5, 1.5, 1.5, "srid=4326");
            Query query = new Query(testName, filter);
            query.setVersion("FIRST");
            SimpleFeatureCollection collection = source.getFeatures(query);
            assertNotNull(collection);
            assertEquals(featList.size(), collection.size());
            feats = collection.features();
            assertNotNull(feats);
            while(feats.hasNext()) {
                SimpleFeature feat = feats.next();
                assertNotNull(feat);
                LOGGER.info(feat.toString());
                assertTrue(feat.toString() + " is expected.", 
                        containsFeature(feat, featList));
            }
        } finally {
            if(feats != null)
                feats.close();
        }
    }
    public void testBBoxFullHistory() throws Exception {

        SimpleFeatureIterator feats = null;
        try {
            updateTestFeatures();

            List<SimpleFeature> featList = new ArrayList<SimpleFeature>(4);
            featList.add(test1);
            featList.add(test3);
            featList.add(test1b);
            featList.add(test3b);

            SimpleFeatureSource source = versioned.getFeatureSource(testName);
            assertNotNull(source);

            FilterFactory2 ff = new FilterFactoryImpl();
            Filter filter = ff.bbox("ln", -1.5, -1.5, 1.5, 1.5, "srid=4326");
            Query query = new Query(testName, filter);
            query.setVersion("ALL");
            SimpleFeatureCollection collection = source.getFeatures(query);
            assertNotNull(collection);
            assertEquals(featList.size(), collection.size());
            feats = collection.features();
            assertNotNull(feats);
            while(feats.hasNext()) {
                SimpleFeature feat = feats.next();
                assertNotNull(feat);
                LOGGER.info(feat.toString());
                assertTrue(feat.toString() + " is expected.", 
                        containsFeature(feat, featList));
            }
        } finally {
            if(feats != null)
                feats.close();
        }
    }

}

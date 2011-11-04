package org.geoserver.data.versioning.decorator;

import java.io.IOException;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.FilterFactoryImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.Version;

public class UnversionedTest extends DecoratedTestCase {
    
    public void testGetSchema() throws Exception { 
        SimpleFeatureType type = versioned.getSchema(sampleType.getName());
        assertNotNull("Sample schema must be found.", type);
        assertEquals("Sample schema must match.", sampleType, type);
        type = versioned.getSchema(testType.getName());
        assertNotNull("Test schema must be found.", type);
        assertEquals("Test schema must match.", testType, type);
    }
    
    public void testGetNames() throws Exception {
        List<Name> names = versioned.getNames();
        
        assertEquals("Both feature types have been added.", 2, names.size());
        assertTrue("The sample feature type must be available.", 
                names.contains(sampleType.getName()));
        assertTrue("The sample feature type must be available.", 
                names.contains(testType.getName()));
    }
    
    public void testNoQuery() throws Exception {
        verifyNoQuery();
    }
    
    public void testCurrentNoQuery() throws Exception {
        updateSampleFeatures();
        verifyNoQuery();
    }
    
    private void verifyNoQuery() throws Exception {
        SimpleFeatureIterator feats = null;
        try {
            SimpleFeatureSource source = versioned.getFeatureSource(sampleName);
            assertNotNull(source);
            SimpleFeatureCollection collection = source.getFeatures();
            assertNotNull(collection);
            List<SimpleFeature> featList = getCurrentFeatures(sampleName);
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
    
    public void testVersionQuery() throws Exception {
        verifyVersionQuery();
    }
    
    public void testCurrentVersionQuery() throws Exception {
        updateSampleFeatures();
        verifyVersionQuery();
    }
    
    private void verifyVersionQuery() throws Exception {
        SimpleFeatureIterator feats = null;
        try {
            SimpleFeatureSource source = versioned.getFeatureSource(sampleName);
            assertNotNull(source);

            Query query = new Query(sampleName);
            query.setVersion("LAST");
            SimpleFeatureCollection collection = source.getFeatures(query);
            assertNotNull(collection);
            List<SimpleFeature> featList = getCurrentFeatures(sampleName);
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
    
    public void testIdQuery() throws Exception {
        verifyIdQuery(test1);
    }
    
    public void testCurrentIdQuery() throws Exception {
        updateTestFeatures();
        verifyIdQuery(test1b);
    }
    
    private void verifyIdQuery(SimpleFeature expectedFeature) throws Exception {
               SimpleFeatureIterator feats = null;
        try {
            SimpleFeatureSource source = versioned.getFeatureSource(testName);
            assertNotNull(source);

            FilterFactory2 ff = new FilterFactoryImpl();
            Filter filter = ff.id(ff.featureId(idT1)); 
            Query query = new Query(testName, filter);
            query.setVersion(Version.Action.LAST);
            SimpleFeatureCollection collection = source.getFeatures(query);
            assertNotNull(collection);
            feats = collection.features();
            assertNotNull(feats);
            assertTrue(feats.hasNext());
            SimpleFeature feat = feats.next();
            assertNotNull(feat);
            assertTrue(feat.equals(expectedFeature));
            assertFalse(feats.hasNext());
        } finally {
            if(feats != null)
                feats.close();
        }
    }

    
}

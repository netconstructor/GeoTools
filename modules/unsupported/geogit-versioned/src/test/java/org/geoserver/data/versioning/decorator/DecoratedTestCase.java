package org.geoserver.data.versioning.decorator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.geogit.test.RepositoryTestCase;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.property.PropertyDataStore;
import org.geotools.data.property.PropertyDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.Hints;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

public abstract class DecoratedTestCase extends RepositoryTestCase {

    protected static final String idS1 = "Sample.1";
    protected static final String idS2 = "Sample.2";
    protected static final String idS3 = "Sample.3";
    protected static final String sampleNs = "http://geogit.sample";
    protected static final String sampleName = "Sample";
    protected static final String sampleTypeSpec = "st:String,it:Integer,pn:Point:srid=4326,db:Double";
    private static String newString1 = "New String Value";
    private static String newString2 = "Another new string";
    protected SimpleFeatureType sampleType;
    protected SimpleFeature sample1;
    protected SimpleFeature sample2;
    protected SimpleFeature sample3;
    protected SimpleFeature sample1b;
    protected SimpleFeature sample2b;
    protected SimpleFeature sample3b;
    
    
    protected static final String idT1 = "Test.1";
    protected static final String idT2 = "Test.2";
    protected static final String idT3 = "Test.3";
    protected static final String testName = "Test";
    protected static final String testTypeSpec = "st:String,ln:LineString:srid=4326,it:Integer";
    protected SimpleFeatureType testType;
    protected SimpleFeature test1;
    protected SimpleFeature test2;
    protected SimpleFeature test3;
    protected SimpleFeature test1b;
    protected SimpleFeature test2b;
    protected SimpleFeature test3b;
    
    
    /**
     * Holds a reference to the unversioned datastore.
     */
    protected DataStore unversioned;

    /**
     * Holds a reference to the decorated datastore that will track versioning info
     * in the backing geogit repository.
     */
    protected DataStore versioned;
    private Integer newInt = new Integer(0);

    protected static final Logger LOGGER = Logging.getLogger(DecoratedTestCase.class);
    
    @Override
    protected void setUpInternal() throws Exception {
        PropertyDataStoreFactory fact = new PropertyDataStoreFactory();

        File target = new File("target");
        File directory = new File(target, "properties");
        FileUtils.deleteDirectory(directory);

        Map params = new HashedMap();
        params.put(PropertyDataStoreFactory.DIRECTORY.key, directory.getPath());
        params.put(PropertyDataStoreFactory.NAMESPACE.key, sampleNs);

        unversioned = fact.createNewDataStore(params);
        
        versioned = new DataStoreDecorator(unversioned, repo);
        
        sampleType = DataUtilities.createType(sampleNs, sampleName, sampleTypeSpec);
        
        sample1 = (SimpleFeature)feature(sampleType, idS1, "Sample String 1", new Integer(1), "POINT (0 1)", new Double(2.34));
        sample1.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE);
        sample2 = (SimpleFeature)feature(sampleType, idS2, "Sample String 2", new Integer(4), "POINT (1 0)", new Double(3380));
        sample2.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE);
        sample3 = (SimpleFeature)feature(sampleType, idS3, "Sample String 3", new Integer(81), "POINT (2 2)", new Double(78.2));
        sample3.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE);
        LOGGER.info(sample1.toString());
        LOGGER.info(sample2.toString());
        LOGGER.info(sample3.toString());
        
        testType = DataUtilities.createType(sampleNs, testName, testTypeSpec);
        
        test1 = (SimpleFeature)feature(testType, idT1, "Test String A", "LINESTRING(1 0,0 0,0 1)", new Integer(5));
        test1.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE);
        test2 = (SimpleFeature)feature(testType, idT2, "Test String B", "LINESTRING(2 6,2 8,3 18)", new Integer(-2));
        test2.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE);
        test3 = (SimpleFeature)feature(testType, idT3, "Test String C", "LINESTRING(1 0,0 1,-1 0,0 -1,1 0)", new Integer(37));
        test3.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE);
        LOGGER.info(test1.toString());
        LOGGER.info(test2.toString());
        LOGGER.info(test3.toString());

        versioned.createSchema(sampleType);
        SimpleFeatureStore store = (SimpleFeatureStore)versioned.getFeatureSource(sampleName);
        Transaction tranny = new DefaultTransaction("DecoratedTestCase.setupInternal()");
        store.setTransaction(tranny);
        
        DefaultFeatureCollection collection = new DefaultFeatureCollection(sampleName, sampleType);
        collection.add(sample1);
        collection.add(sample2);
        collection.add(sample3);
        store.addFeatures(collection);
        
        tranny.commit();
        tranny.close();
        
        versioned.createSchema(testType);
        SimpleFeatureStore store2 = (SimpleFeatureStore)versioned.getFeatureSource(testName);
        Transaction tranny2 = new DefaultTransaction("DecoratedTestCase.setupInternal().2");
        store2.setTransaction(tranny2);
        
        DefaultFeatureCollection collection2 = new DefaultFeatureCollection(testName, testType);
        collection2.add(test1);
        collection2.add(test2);
        collection2.add(test3);
        store2.addFeatures(collection2);
        
        tranny2.commit();
        tranny2.close();
    }

    protected void updateSampleFeatures() throws Exception {
        Transaction trans = null;
        try {
            SimpleFeatureStore store = (SimpleFeatureStore)versioned.getFeatureSource(sampleName);
            trans = new DefaultTransaction("DecoratedTestCase.updateSampleFeatures()");
            store.setTransaction(trans);
            FilterFactory2 ff = new FilterFactoryImpl();
            Filter filter = ff.id(ff.featureId(idS1)); 
            store.modifyFeatures("st", newString1, filter);
            
            filter = ff.id(ff.featureId(idS2), ff.featureId(idS3));
            store.modifyFeatures(new String[] {"st", "it"},
                    new Object[] {newString2, newInt}, filter);
            trans.commit();
            
            SimpleFeatureBuilder fb = new SimpleFeatureBuilder(sampleType);
            sample1b = fb.copy(sample1);
            sample1b.setAttribute("st", newString1);
            sample2b = fb.copy(sample2);
            sample2b.setAttribute("st", newString2);
            sample2b.setAttribute("it", newInt);
            sample3b = fb.copy(sample3);
            sample3b.setAttribute("st", newString2);
            sample3b.setAttribute("it", newInt);
            
        } catch(Exception ex) {
            if(trans != null)
                trans.rollback();
            throw ex;
        } finally {
            if(trans != null)
                trans.close();
        }
    }

    protected void updateTestFeatures() throws Exception {
        Transaction trans = null;
        try {
            SimpleFeatureStore store = (SimpleFeatureStore)versioned.getFeatureSource(testName);
            trans = new DefaultTransaction("DecoratedTestCase.updateTestFeatures()");
            store.setTransaction(trans);
            FilterFactory2 ff = new FilterFactoryImpl();
            Filter filter = ff.id(ff.featureId(idT1)); 
            store.modifyFeatures("st", newString1, filter);
    
            filter = ff.id(ff.featureId(idT2), ff.featureId(idT3));
            store.modifyFeatures(new String[] {"st", "it"},
                    new Object[] {newString2, newInt}, filter);
            trans.commit();
            
            SimpleFeatureBuilder fb = new SimpleFeatureBuilder(testType);
            test1b = fb.copy(test1);
            test1b.setAttribute("st", newString1);
            test2b = fb.copy(test2);
            test2b.setAttribute("st", newString2);
            test2b.setAttribute("it", newInt);
            test3b = fb.copy(test3);
            test3b.setAttribute("st", newString2);
            test3b.setAttribute("it", newInt);
        } catch(Exception ex) {
            if(trans != null)
                trans.rollback();
            throw ex;
        } finally {
            if(trans != null)
                trans.close();
        }
    }
    
    protected List<SimpleFeature> getCurrentFeatures(String typeName) {
        if(typeName.equals(sampleName)) {
            if(sample1b == null)
                return getOriginalFeatures(sampleName);
            else
                return getUpdatedFeatures(sampleName);
            
        } else if(typeName.equals(testName)) {
            if(test1b == null)
                return getOriginalFeatures(testName);
            else
                return getUpdatedFeatures(testName);
        }
        return new ArrayList<SimpleFeature>();
    }
    
    protected List<SimpleFeature> getOriginalFeatures(String typeName) {
        List<SimpleFeature> featList = new ArrayList<SimpleFeature>();
        if(typeName.equals(sampleName)) {
            featList.add(sample1);
            featList.add(sample2);
            featList.add(sample3);
        } else if(typeName.equals(testName)) {
            featList.add(test1);
            featList.add(test2);
            featList.add(test3);
        }
        return featList;
    }
    
    protected List<SimpleFeature> getUpdatedFeatures(String typeName) {
        List<SimpleFeature> featList = new ArrayList<SimpleFeature>();
        if(typeName.equals(sampleName)) {
            featList.add(sample1b);
            featList.add(sample2b);
            featList.add(sample3b);
        } else if(typeName.equals(testName)) {
            featList.add(test1b);
            featList.add(test2b);
            featList.add(test3b);
        }
        return featList;
    }
    
    protected List<SimpleFeature> getAllFeatures(String typeName) {
        List<SimpleFeature> featList = new ArrayList<SimpleFeature>();
        if(typeName.equals(sampleName)) {
            featList.add(sample1);
            featList.add(sample2);
            featList.add(sample3);
            featList.add(sample1b);
            featList.add(sample2b);
            featList.add(sample3b);
        } else if(typeName.equals(testName)) {
            featList.add(test1);
            featList.add(test2);
            featList.add(test3);
            featList.add(test1b);
            featList.add(test2b);
            featList.add(test3b);
        }
        return featList;
    }
    
    protected boolean containsFeature(SimpleFeature feat, List<SimpleFeature> list) {
        Iterator<SimpleFeature> it = list.iterator();
        while(it.hasNext()) {
            if(feat.equals(it.next()))
                return true;
        }
        LOGGER.info("Could not match feature to list: " + feat.toString());
        return false;
    }
}

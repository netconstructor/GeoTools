package org.geoserver.data.versioning.decorator;

import java.io.File;
import java.util.Map;

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
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public abstract class DecoratedTestCase extends RepositoryTestCase {

    protected static final String idS1 = "Sample.1";
    protected static final String idS2 = "Sample.2";
    protected static final String idS3 = "Sample.3";
    protected static final String sampleNs = "http://geogit.sample";
    protected static final String sampleName = "Sample";
    protected static final String sampleTypeSpec = "st:String,it:Integer,pn:Point:srid=4326,db:Double";
    protected SimpleFeatureType sampleType;
    protected SimpleFeature sample1;
    protected SimpleFeature sample2;
    protected SimpleFeature sample3;
    
    /**
     * Holds a reference to the unversioned datastore.
     */
    protected DataStore unversioned;

    /**
     * Holds a reference to the decorated datastore that will track versioning info
     * in the backing geogit repository.
     */
    protected DataStore versioned;

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
        sample2 = (SimpleFeature)feature(sampleType, idS2, "Sample String 2", new Integer(4), "POINT (1 0)", new Double(3380));
        sample3 = (SimpleFeature)feature(sampleType, idS3, "Sample String 3", new Integer(81), "POINT (2 2)", new Double(78.2));

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
        
    }

}

package org.geotools.jdbc.versioning.geogit;

import java.io.IOException;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.PostGISTestSetup;
import org.geotools.data.postgis.PostgisDataStoreTest;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;
import org.geotools.jdbc.versioning.VersioningFeatureSource;
import org.geotools.jdbc.versioning.VersioningFeatureStore;
import org.geotools.jdbc.versioning.VersioningFeatureWriter;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;

public class VersionedDataStoreTest extends PostgisDataStoreTest {

/*    @Override
    protected String getFixtureId() {
    	
        return "versioned";
    }*/
    

    @Override
    protected String getFixtureId() {
        return ((VersionedTestSetup)createTestSetup()).createDataStoreFactory().getDatabaseID() + "versioned";
    }
    

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new VersionedTestSetup();
        
    }
    

   @Override
    protected void connect() throws Exception {
        //create the test harness

        if (setup == null) {
            setup =  createTestSetup();
        }
        VersionedTestSetup vsetup = (VersionedTestSetup) setup;

        vsetup.setFixture(fixture);
        vsetup.setUp();

        //initialize the database
        vsetup.initializeDatabase();

        //initialize the data
        vsetup.setUpData();
        
        HashMap params = new HashMap();
        params.put( JDBCDataStoreFactory.NAMESPACE.key, "http://www.geotools.org/test" );
        //params.put( JDBCDataStoreFactory.SCHEMA.key, "geotools" );
        params.put( JDBCDataStoreFactory.DATASOURCE.key, setup.getDataSource() );
        params.putAll(fixture);

       VersionedGeoGITDataStoreFactory factory = vsetup.createDataStoreFactory();
        dataStore = factory.createDataStore( params );
        
        //setup.setUpDataStore(dataStore);
        dialect = dataStore.getSQLDialect();
    }
   

   @Override
   protected boolean isOnline() throws Exception {
	   VersionedTestSetup setup = (VersionedTestSetup) createTestSetup();
       setup.setFixture(fixture);
       
       try {
           DataSource dataSource = setup.getDataSource();
           Connection cx = dataSource.getConnection();
           cx.close();
           return true;
       } 
       catch (Throwable t) {
           throw new RuntimeException(t);
       } 
       finally {
           try {
               setup.tearDown();    
           } 
           catch(Exception e) {
               System.out.println("Error occurred tearing down the test setup");
           }
       }
   }
   
   public void testGetVersionedDataStore() throws Exception {
       assertTrue(dataStore instanceof GeoGITWrappingDataStore);
   }
   
   public void testGetVersionedFeatureSource() throws Exception {
       SimpleFeatureSource featureSource = dataStore.getFeatureSource(tname("ft1"));
       assertTrue(featureSource instanceof VersioningFeatureStore);
   }
   
   public void testVersionLog() throws IOException {
       FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore.getFeatureWriter(tname("ft1"), Transaction.AUTO_COMMIT);

       while (writer.hasNext()) {
           SimpleFeature feature = writer.next();
           feature.setAttribute(aname("stringProperty"), "foobar");
           writer.write();
           

       }


       writer.close();

       DefaultQuery query = new DefaultQuery(tname("ft1"));
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
       assertTrue(reader.hasNext());
       FilterFactory2 ff = (FilterFactory2) CommonFactoryFinder.getFilterFactory(null);

       while (reader.hasNext()) {
           SimpleFeature feature = reader.next();
           assertEquals("foobar", feature.getAttribute(aname("stringProperty")));
           
           try {
               FeatureId fid = feature.getIdentifier();
               Id fidFilter = ff.id(Collections.singleton(fid));
            SimpleFeatureCollection logs = ((GeoGITWrappingDataStore)dataStore).getGeoGIT().getLog(null, null, feature.getFeatureType(), fidFilter, null);
            assertNotNull(logs);
            Iterator logit = logs.iterator();
            while (logit.hasNext()){
                SimpleFeature sf = (SimpleFeature) logit.next();
                assertNotNull(sf);
            }
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       }

       reader.close();


   }

}

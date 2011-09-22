package org.geotools.jdbc.versioning.geogit;

import java.sql.Connection;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.geotools.data.postgis.PostGISTestSetup;
import org.geotools.data.postgis.PostgisDataStoreTest;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;

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
            setup = createTestSetup();
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
}

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

    @Override
    protected String getFixtureId() {
        return "versioned";
    }

    VersionedTestSetup setup;

    @Override
    protected VersionedTestSetup createTestSetup() {
        return new VersionedTestSetup();
    }
    

   @Override
    protected void connect() throws Exception {
        //create the test harness
        if (setup == null) {
            setup = createTestSetup();
        }

        setup.setFixture(fixture);
        setup.setUp();

        //initialize the database
        setup.initializeDatabase();

        //initialize the data
       setup.setUpData();

       VersionedGeoGITDataStoreFactory factory = setup.createDataStoreFactory();
        dataStore = factory.createDataStore( fixture );
        
        //setup.setUpDataStore(dataStore);
        dialect = dataStore.getSQLDialect();
    }

   @Override
   protected boolean isOnline() throws Exception {
	   VersionedTestSetup setup = createTestSetup();
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

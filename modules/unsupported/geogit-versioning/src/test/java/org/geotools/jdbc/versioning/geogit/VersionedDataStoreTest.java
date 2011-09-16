package org.geotools.jdbc.versioning.geogit;

import org.geotools.data.postgis.PostGISTestSetup;
import org.geotools.data.postgis.PostgisDataStoreTest;
import org.geotools.jdbc.JDBCTestSetup;

public class VersionedDataStoreTest extends PostgisDataStoreTest {

    @Override
    protected String getFixtureId() {
        return "versioned";
    }
    
    @Override
    protected JDBCTestSetup createTestSetup() {
        return new VersionedTestSetup();
    }
}

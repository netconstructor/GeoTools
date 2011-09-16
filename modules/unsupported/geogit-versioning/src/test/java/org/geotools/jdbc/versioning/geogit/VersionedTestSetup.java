/**
 * 
 */
package org.geotools.jdbc.versioning.geogit;

import java.util.Properties;

import org.geotools.data.postgis.PostGISTestSetup;
import org.geotools.jdbc.IJDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;

/**
 * @author wdeane
 *
 */
public class VersionedTestSetup extends PostGISTestSetup {

	/* (non-Javadoc)
	 * @see org.geotools.data.postgis.PostGISTestSetup#setUpDataStore(org.geotools.jdbc.IJDBCDataStore)
	 */
	@Override
	protected void setUpDataStore(IJDBCDataStore dataStore) {
		// TODO Auto-generated method stub
		super.setUpDataStore(dataStore);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.postgis.PostGISTestSetup#createExampleFixture()
	 */
	@Override
	protected Properties createExampleFixture() {
		// TODO Auto-generated method stub
		return super.createExampleFixture();
	}

    @Override
    protected void setUpData() throws Exception {
    	assert(true);
/*        runSafe("DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'ft1'");
        runSafe("DROP TABLE \"ft1\"");
        runSafe("DROP TABLE \"ft2\"");
        
        run("CREATE TABLE \"ft1\"(" //
                + "\"id\" serial primary key, " //
                + "\"geometry\" geometry, " //
                + "\"intProperty\" int," //
                + "\"doubleProperty\" double precision, " // 
                + "\"stringProperty\" varchar)");
        run("INSERT INTO GEOMETRY_COLUMNS VALUES('', 'public', 'ft1', 'geometry', 2, '4326', 'POINT')");
        run("CREATE INDEX FT1_GEOMETRY_INDEX ON \"ft1\" USING GIST (\"geometry\") ");
        
        run("INSERT INTO \"ft1\" VALUES(0, GeometryFromText('POINT(0 0)', 4326), 0, 0.0, 'zero')"); 
        run("INSERT INTO \"ft1\" VALUES(1, GeometryFromText('POINT(1 1)', 4326), 1, 1.1, 'one')");
        run("INSERT INTO \"ft1\" VALUES(2, GeometryFromText('POINT(2 2)', 4326), 2, 2.2, 'two')");
        // advance the sequence to 2
        run("SELECT nextval(pg_get_serial_sequence('ft1','id'))");
        run("SELECT nextval(pg_get_serial_sequence('ft1','id'))");
        // analyze so that the stats will be up to date
        run("ANALYZE \"ft1\"");*/
    	
    }


	/* (non-Javadoc)
	 * @see org.geotools.data.postgis.PostGISTestSetup#createDataStoreFactory()
	 */
	@Override
	protected JDBCDataStoreFactory createDataStoreFactory() {
		// TODO Auto-generated method stub
		return new VersionedGeoGITDataStoreFactory(super.createDataStoreFactory());
	}

}

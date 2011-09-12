package org.geotools.data.oracle;

import org.geotools.jdbc.JDBCDataStoreAPITestSetup;
import org.geotools.jdbc.JDBCVirtualTableTest;
import org.geotools.data.oracle.OracleTestSetup;

public class OracleVirtualTableTest extends JDBCVirtualTableTest {

    @Override
    protected JDBCDataStoreAPITestSetup createTestSetup() {
        return new OracleDataStoreAPITestSetup(new OracleTestSetup());
    }

}

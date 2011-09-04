/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.teradata;

import org.geotools.jdbc.JDBCPrimaryKeyTestSetup;
import org.geotools.jdbc.JDBCTestSetup;

public class TeradataPrimaryKeyTestSetup extends JDBCPrimaryKeyTestSetup {

    public TeradataPrimaryKeyTestSetup(JDBCTestSetup delegate) {
        super(delegate);
    }

    public TeradataTestSetup getDelegate() {
        return (TeradataTestSetup) delegate;
    }
    
    protected void createAutoGeneratedPrimaryKeyTable() throws Exception {
        run("CREATE TABLE \"auto\"(\"key\" PRIMARY KEY not null generated always as identity (start with 1) integer, \"name\" varchar(200), geom ST_Geometry)");
        run("INSERT INTO SYSSPATIAL.GEOMETRY_COLUMNS (F_TABLE_CATALOG, F_TABLE_SCHEMA, F_TABLE_NAME, " +
            "F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, GEOM_TYPE) VALUES ('','test','auto', 'geom', " +
            "2, " + getDelegate().getSrid4326() + ", 'ST_Geometry')");
        run("INSERT INTO \"auto\" (\"name\",\"geom\" ) VALUES ('one',NULL)");
        run("INSERT INTO \"auto\" (\"name\",\"geom\" ) VALUES ('two',NULL)");
        run("INSERT INTO \"auto\" (\"name\",\"geom\" ) VALUES ('three',NULL)");
    }


    protected void createMultiColumnPrimaryKeyTable() throws Exception {
        run("CREATE TABLE \"multi\" ( \"key1\" int NOT NULL, \"key2\" varchar(20) NOT NULL, "
                + "\"name\" varchar(20), geom ST_Geometry)");
        run("INSERT INTO SYSSPATIAL.GEOMETRY_COLUMNS (F_TABLE_CATALOG, F_TABLE_SCHEMA, F_TABLE_NAME, " +
            "F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, GEOM_TYPE) VALUES ('','test','multi', 'geom', " +
            "2, " + getDelegate().getSrid4326() + ", 'ST_Geometry')");
        run("ALTER TABLE \"multi\" ADD PRIMARY KEY (\"key1\",\"key2\")");

        run("INSERT INTO \"multi\" VALUES (1, 'x', 'one', NULL)");
        run("INSERT INTO \"multi\" VALUES (2, 'y', 'two', NULL)");
        run("INSERT INTO \"multi\" VALUES (3, 'z', 'three', NULL)");
    }


    protected void createNonIncrementingPrimaryKeyTable() throws Exception {
        run("CREATE TABLE \"noninc\"(\"key\" PRIMARY KEY NOT NULL integer, \"name\" varchar(200), geom ST_Geometry)");
        run("INSERT INTO SYSSPATIAL.GEOMETRY_COLUMNS (F_TABLE_CATALOG, F_TABLE_SCHEMA, F_TABLE_NAME, " +
            "F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, GEOM_TYPE) VALUES ('','test','noninc', 'geom'," +
            "2, " + getDelegate().getSrid4326() + ", 'ST_Geometry')");

        run("INSERT INTO \"noninc\" VALUES (1, 'one', NULL)");
        run("INSERT INTO \"noninc\" VALUES (2, 'two', NULL)");
        run("INSERT INTO \"noninc\" VALUES (3, 'three', NULL)");
    }


    protected void createSequencedPrimaryKeyTable() throws Exception {
        run("CREATE TABLE \"seq\" ( \"key\" generated always as identity (start with 1)  integer, \"name\" varchar(200), geom ST_Geometry)");
        run("INSERT INTO SYSSPATIAL.GEOMETRY_COLUMNS (F_TABLE_CATALOG, F_TABLE_SCHEMA, F_TABLE_NAME, " +
        "F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, GEOM_TYPE) VALUES ('','test','seq', 'geom', 2, " +
        getDelegate().getSrid4326() + ", 'ST_Geometry')");
//        run("CREATE SEQUENCE SEQ_KEY_SEQUENCE START WITH 1 OWNED BY \"seq\".\"key\"");

/*        run("INSERT INTO \"seq\" (\"key\", \"name\",\"geom\" ) VALUES (" +
"(SELECT NEXTVAL('SEQ_KEY_SEQUENCE')),'one',NULL)");
run("INSERT INTO \"seq\" (\"key\", \"name\",\"geom\" ) VALUES (" +
"(SELECT NEXTVAL('SEQ_KEY_SEQUENCE')),'two',NULL)");
run("INSERT INTO \"seq\" (\"key\", \"name\",\"geom\" ) VALUES (" +
"(SELECT NEXTVAL('SEQ_KEY_SEQUENCE')),'three',NULL)");*/

        run("INSERT INTO \"seq\" (\"name\",\"geom\") VALUES ('one',NULL)");
        run("INSERT INTO \"seq\" (\"name\",\"geom\") VALUES ('two',NULL)");
        run("INSERT INTO \"seq\" (\"name\",\"geom\") VALUES ('three',NULL)");
    }


    protected void createNullPrimaryKeyTable() throws Exception {
        run("CREATE TABLE \"nokey\" ( \"name\" varchar(200))");
        run("INSERT INTO \"nokey\" VALUES ('one')");
        run("INSERT INTO \"nokey\" VALUES ('two')");
        run("INSERT INTO \"nokey\" VALUES ('three')");
    }


    protected void createUniqueIndexTable() throws Exception {
        run("CREATE TABLE \"uniq\"(\"key\" UNIQUE NOT NULL int, \"name\" varchar(200), geom ST_Geometry)");
        run("INSERT INTO SYSSPATIAL.GEOMETRY_COLUMNS (F_TABLE_CATALOG, F_TABLE_SCHEMA, F_TABLE_NAME, " +
        "F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, GEOM_TYPE) VALUES ('','test','uniq', 'geom', 2, " +
        getDelegate().getSrid4326() + ", 'ST_Geometry')");

//        run("CREATE UNIQUE INDEX \"uniq_key_index\" ON \"uniq\"(\"key\")");
        run("INSERT INTO \"uniq\" VALUES (1,'one',NULL)");
        run("INSERT INTO \"uniq\" VALUES (2,'two',NULL)");
        run("INSERT INTO \"uniq\" VALUES (3,'three',NULL)");
        createExtraTables();
    }

    protected void dropAutoGeneratedPrimaryKeyTable() throws Exception {
        runSafe("DELETE FROM SYSSPATIAL.GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'auto'");
        runSafe("DROP TABLE \"auto\"");
    }


    protected void dropMultiColumnPrimaryKeyTable() throws Exception {
        runSafe("DELETE FROM SYSSPATIAL.GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'multi'");
        runSafe("DROP TABLE \"multi\"");
    }


    protected void dropNonIncrementingPrimaryKeyTable() throws Exception {
        runSafe("DELETE FROM SYSSPATIAL.GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'noninc'");
        runSafe("DROP TABLE \"noninc\"");
    }


    protected void dropSequencedPrimaryKeyTable() throws Exception {
        runSafe("DELETE FROM SYSSPATIAL.GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'seq'");
        runSafe("DROP SEQUENCE SEQ_KEY_SEQUENCE");
        runSafe("DROP TABLE \"seq\"");
    }


    protected void dropNullPrimaryKeyTable() throws Exception {
        runSafe("DROP TABLE \"nokey\"");
    }


    protected void dropUniqueIndexTable() throws Exception {
        runSafe("DELETE FROM SYSSPATIAL.GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'uniq'");
        runSafe("DROP TABLE \"uniq\"");
        dropExtraTables();
    }

    private void createExtraTables() throws Exception {
        run("CREATE TABLE \"uniquetablenotgenerated\" ( \"key\" UNIQUE not null integer, \"name\" VARCHAR(256), \"geom\" ST_GEOMETRY)");
        run("INSERT INTO SYSSPATIAL.GEOMETRY_COLUMNS (F_TABLE_CATALOG, F_TABLE_SCHEMA, F_TABLE_NAME, " +
            "F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, GEOM_TYPE) VALUES ('','" + fixture.getProperty("schema") 
            + "','uniquetablenotgenerated', 'geom', 2, " + getDelegate().getSrid4326() + ", 'GEOMETRY')");
        insertFeatures("uniquetablenotgenerated");

        run("CREATE TABLE \"uniquetable\" ( \"key\" UNIQUE generated always as identity (start with 1) integer not null, \"name\" VARCHAR(256), \"geom\" ST_GEOMETRY)");
        run("INSERT INTO SYSSPATIAL.GEOMETRY_COLUMNS (F_TABLE_CATALOG, F_TABLE_SCHEMA, F_TABLE_NAME, " +
            "F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, GEOM_TYPE) VALUES ('','" + fixture.getProperty("schema") 
            + "','uniquetable', 'geom', 2, " + getDelegate().getSrid4326() + ", 'GEOMETRY')");
        insertFeatures("uniquetable");
    }

    private void dropExtraTables() throws Exception {
        runSafe("DROP TABLE \"uniquetablenotgenerated\"");
        runSafe("delete from SYSSPATIAL.GEOMETRY_COLUMNS where f_table_name='uniquetablenotgenerated'");
        runSafe("DROP TABLE \"uniquetable\"");
        runSafe("delete from SYSSPATIAL.GEOMETRY_COLUMNS where f_table_name='uniquetable'");
    }

    private void insertFeatures(String tableName) throws Exception {
        run("INSERT INTO \""+tableName+"\" (\"key\", \"name\",\"geom\" ) VALUES (1,'one',NULL)");
        run("INSERT INTO \""+tableName+"\" (\"key\", \"name\",\"geom\" ) VALUES (2,'two',NULL)");
        run("INSERT INTO \""+tableName+"\" (\"key\", \"name\",\"geom\" ) VALUES (3,'three',NULL)");
    }

}

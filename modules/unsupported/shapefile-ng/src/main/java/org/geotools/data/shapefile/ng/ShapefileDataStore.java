package org.geotools.data.shapefile.ng;

import static org.geotools.data.shapefile.ng.files.ShpFileType.*;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ng.dbf.DbaseFileException;
import org.geotools.data.shapefile.ng.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.ng.files.ShpFiles;
import org.geotools.data.shapefile.ng.files.StorageFile;
import org.geotools.data.shapefile.ng.shp.ShapeType;
import org.geotools.data.shapefile.ng.shp.ShapefileWriter;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ShapefileDataStore extends ContentDataStore {

    // User-data keyword names for storing the original field name and its
    // instance number when the field name is replaced with a new name built
    // using the original+count convention.
    public static final String ORIGINAL_FIELD_NAME = "original";

    public static final String ORIGINAL_FIELD_DUPLICITY_COUNT = "count";

    public static final Charset DEFAULT_STRING_CHARSET = (Charset) ShapefileDataStoreFactory.DBFCHARSET.getDefaultValue();
    
    public static final TimeZone DEFAULT_TIMEZONE = (TimeZone) ShapefileDataStoreFactory.DBFTIMEZONE.getDefaultValue();

    ShpFiles shpFiles;

    Charset charset = DEFAULT_STRING_CHARSET;

    TimeZone timeZone = DEFAULT_TIMEZONE;

    boolean memoryMapped = false;

    boolean bufferCachingEnabled = true;

    boolean indexed = true;

    public ShapefileDataStore(URL url) {
        shpFiles = new ShpFiles(url);
    }

    @Override
    protected List<Name> createTypeNames() throws IOException {
        return Collections.singletonList(getTypeName());
    }

    private Name getTypeName() {
        return new NameImpl(namespaceURI, shpFiles.getTypeName());
    }

    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return getFeatureSource();
    }

    public ContentFeatureSource getFeatureSource() throws IOException {
        ContentEntry entry = ensureEntry(getTypeName());
        if(shpFiles.isWritable()) {
            return new ShapefileFeatureStore(entry, shpFiles);
        } else {
            return new ShapefileFeatureSource(entry, shpFiles);
        }
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isMemoryMapped() {
        return memoryMapped;
    }

    public void setMemoryMapped(boolean memoryMapped) {
        this.memoryMapped = memoryMapped;
    }

    public boolean isBufferCachingEnabled() {
        return bufferCachingEnabled;
    }

    public void setBufferCachingEnabled(boolean bufferCachingEnabled) {
        this.bufferCachingEnabled = bufferCachingEnabled;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public SimpleFeatureType getSchema() throws IOException {
        return getSchema(getTypeName());
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader() throws IOException {
        return super.getFeatureReader(new Query(getTypeName().getLocalPart()), Transaction.AUTO_COMMIT);
    }

    public long getCount(Query query) throws IOException {
        return getFeatureSource().getCount(query);
    }
    
    /**
     * Set the FeatureType of this DataStore. This method will delete any
     * existing local resources or throw an IOException if the DataStore is
     * remote.
     * 
     * @param featureType
     *                The desired FeatureType.
     * 
     * @throws IOException
     *                 If the DataStore is remote.
     */
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        if (!shpFiles.isLocal()) {
            throw new IOException(
                    "Cannot create FeatureType on remote or in-classpath shapefile");
        }

        shpFiles.delete();

        CoordinateReferenceSystem crs = featureType.getGeometryDescriptor()
                .getCoordinateReferenceSystem();
        final Class<?> geomType = featureType.getGeometryDescriptor().getType()
                .getBinding();
        final ShapeType shapeType;

        if (Point.class.isAssignableFrom(geomType)) {
            shapeType = ShapeType.POINT;
        } else if (MultiPoint.class.isAssignableFrom(geomType)) {
            shapeType = ShapeType.MULTIPOINT;
        } else if (LineString.class.isAssignableFrom(geomType)
                || MultiLineString.class.isAssignableFrom(geomType)) {
            shapeType = ShapeType.ARC;
        } else if (Polygon.class.isAssignableFrom(geomType)
                || MultiPolygon.class.isAssignableFrom(geomType)) {
            shapeType = ShapeType.POLYGON;
        } else {
            throw new DataSourceException(
                    "Cannot create a shapefile whose geometry type is "
                            + geomType);
        }

        StorageFile shpStoragefile = shpFiles.getStorageFile(SHP);
        StorageFile shxStoragefile = shpFiles.getStorageFile(SHX);
        StorageFile dbfStoragefile = shpFiles.getStorageFile(DBF);
        StorageFile prjStoragefile = shpFiles.getStorageFile(PRJ);

        FileChannel shpChannel = shpStoragefile.getWriteChannel();
        FileChannel shxChannel = shxStoragefile.getWriteChannel();

        ShapefileWriter writer = new ShapefileWriter(shpChannel, shxChannel);
        try {
            // by spec, if the file is empty, the shape envelope should be ignored
            writer.writeHeaders(new Envelope(), shapeType, 0, 100);
        } finally {
            writer.close();
            assert !shpChannel.isOpen();
            assert !shxChannel.isOpen();
        }

        DbaseFileHeader dbfheader = createDbaseHeader(featureType);

        dbfheader.setNumRecords(0);

        WritableByteChannel dbfChannel = dbfStoragefile.getWriteChannel();

        try {
            dbfheader.writeHeader(dbfChannel);
        } finally {
            dbfChannel.close();
        }

        if (crs != null) {
            String s = crs.toWKT();
            // .prj files should have no carriage returns in them, this
            // messes up
            // ESRI's ArcXXX software, so we'll be compatible
            s = s.replaceAll("\n", "").replaceAll("  ", "");

            FileWriter prjWriter = new FileWriter(prjStoragefile.getFile());
            try {
                prjWriter.write(s);
            } finally {
                prjWriter.close();
            }
        }
        else {
            LOGGER.warning("PRJ file not generated for null CoordinateReferenceSystem");
        }
        StorageFile.replaceOriginals(shpStoragefile, shxStoragefile,
                dbfStoragefile, prjStoragefile);
    }
    
    /**
     * Attempt to create a DbaseFileHeader for the FeatureType. Note, we cannot
     * set the number of records until the write has completed.
     * 
     * @param featureType
     *                DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *                 DOCUMENT ME!
     * @throws DbaseFileException
     *                 DOCUMENT ME!
     */
    protected static DbaseFileHeader createDbaseHeader(
            SimpleFeatureType featureType) throws IOException,
            DbaseFileException {

        DbaseFileHeader header = new DbaseFileHeader();

        for (int i = 0, ii = featureType.getAttributeCount(); i < ii; i++) {
            AttributeDescriptor type = featureType.getDescriptor(i);

            Class<?> colType = type.getType().getBinding();
            String colName = type.getLocalName();

            int fieldLen = FeatureTypes.getFieldLength(type);
            if (fieldLen == FeatureTypes.ANY_LENGTH)
                fieldLen = 255;
            if ((colType == Integer.class) || (colType == Short.class)
                    || (colType == Byte.class)) {
                header.addColumn(colName, 'N', Math.min(fieldLen, 9), 0);
            } else if (colType == Long.class) {
                header.addColumn(colName, 'N', Math.min(fieldLen, 19), 0);
            } else if (colType == BigInteger.class) {
                header.addColumn(colName, 'N', Math.min(fieldLen, 33), 0);
            } else if (Number.class.isAssignableFrom(colType)) {
                int l = Math.min(fieldLen, 33);
                int d = Math.max(l - 2, 0);
                header.addColumn(colName, 'N', l, d);
            // This check has to come before the Date one or it is never reached
            // also, this field is only activated with the following system property:
            // org.geotools.shapefile.datetime=true
            } else if (java.util.Date.class.isAssignableFrom(colType)
                       && Boolean.getBoolean("org.geotools.shapefile.datetime"))
            {
                header.addColumn(colName, '@', fieldLen, 0);
            } else if (java.util.Date.class.isAssignableFrom(colType) ||
                    Calendar.class.isAssignableFrom(colType)) {
                header.addColumn(colName, 'D', fieldLen, 0);
            } else if (colType == Boolean.class) {
                header.addColumn(colName, 'L', 1, 0);
            } else if (CharSequence.class.isAssignableFrom(colType)) {
                // Possible fix for GEOT-42 : ArcExplorer doesn't like 0 length
                // ensure that maxLength is at least 1
                header.addColumn(colName, 'C', Math.min(254, fieldLen), 0);
            } else if (Geometry.class.isAssignableFrom(colType)) {
                continue;
            } else {
                throw new IOException("Unable to write : " + colType.getName());
            }
        }

        return header;
    }
    
    /**
     * This method is used to force the creation of a .prj file.
     * <p>
     * The internally cached FeatureType will be removed, so the next call to
     * getSchema() will read in the created file. This method is not thread safe
     * and will have dire consequences for any other thread making use of the
     * shapefile.
     * <p>
     * 
     * @param crs
     */
    public void forceSchemaCRS(CoordinateReferenceSystem crs)
            throws IOException {
        if (crs == null)
            throw new NullPointerException("CRS required for .prj file");

        String s = crs.toWKT();
        s = s.replaceAll("\n", "").replaceAll("  ", "");
        StorageFile storageFile = shpFiles.getStorageFile(PRJ);
        FileWriter out = new FileWriter(storageFile.getFile());

        try {
            out.write(s);
        } finally {
            out.close();
        }
        storageFile.replaceOriginal();
        entries.clear();
    }

}

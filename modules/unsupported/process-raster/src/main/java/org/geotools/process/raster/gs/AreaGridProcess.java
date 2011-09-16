/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008-2011 TOPP - www.openplans.org.
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
package org.geotools.process.raster.gs;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.ProcessException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.opengis.referencing.operation.MathTransform;

/**
 * A process that build a regular cell grid where each pixel represents its effective area in the
 * envelope in square meters.
 * <p>
 * Internally the process uses a reprojection to EckertIV to ensure proper area computation. Current
 * limitations:
 * <ul>
 * <li>won't work for very large rasters since it allocates the entire grid in memory</li>
 * <li>area accuracy increases as the cell size shrinks, avoid having cells that occupy sizeable
 * chunks of the world</li>
 * </ul>
 * 
 * @author Luca Paolino - GeoSolutions
 */
@DescribeProcess(title = "areaGrid", description = "Builds a regular cell grid where each pixel represents its effective area in the envelope using the EPSG:54012")
public class AreaGridProcess implements GSProcess {
    private static final String targetCRSWKT = "PROJCS[\"World_Eckert_IV\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Eckert_IV\"],PARAMETER[\"Central_Meridian\",0.0],UNIT[\"Meter\",1.0]]";

    @DescribeResult(name = "result", description = "The grid")
    public GridCoverage2D execute(
            @DescribeParameter(name = "envelope", description = "The envelope. The envelope must be in WGS84") ReferencedEnvelope bounds,
            @DescribeParameter(name = "width", description = "image width ") int width,
            @DescribeParameter(name = "height", description = "image height ") int height)
            throws ProcessException {
        // basic checks
        if (height <= 0 || width <= 0) {
            throw new ProcessException("height and width parameters must be greater than 0");
        }
        if (bounds.getCoordinateReferenceSystem() == null) {
            throw new ProcessException("Envelope CRS must not be null");
        }

        // build the grid
        GeometryFactory geomFactory = new GeometryFactory();
        try {
            CoordinateReferenceSystem sourceCRS = org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
            CoordinateReferenceSystem targetCRS = CRS.parseWKT(targetCRSWKT);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
            double pX = bounds.getMinX();
            double pY = bounds.getMinY();
            double stepX = (bounds.getMaxX() - bounds.getMinX()) / width;
            double stepY = (bounds.getMaxY() - bounds.getMinY()) / height;
            float[][] matrix = new float[width][height];
            Coordinate[] tempCoordinates = new Coordinate[5];
            
            // scroll thrhough every cell
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    double nX = pX + stepX;
                    double nY = pY + stepY;
                    
                    // build the cell in the original srs
                    tempCoordinates[0] = new Coordinate(pX, pY);
                    tempCoordinates[1] = new Coordinate(nX, pY);
                    tempCoordinates[2] = new Coordinate(nX, nY);
                    tempCoordinates[3] = new Coordinate(pX, nY);
                    tempCoordinates[4] = tempCoordinates[0];
                    LinearRing linearRing = geomFactory.createLinearRing(tempCoordinates);
                    Polygon polygon = geomFactory.createPolygon(linearRing, null);
                    
                    // transform to EckertIV and compute area
                    Geometry targetGeometry = JTS.transform(polygon, transform);
                    matrix[j][i] = (float) targetGeometry.getArea();
                    
                    // move on
                    pX = pX + stepX;
                }
                pY = pY + stepY;
            }
            
            // build the grid coverage
            GridCoverageFactory coverageFactory = new GridCoverageFactory();
            GridCoverage2D grid = coverageFactory
                    .create("AreaGridCoverage", matrix, bounds);
            return grid;

        } catch (org.opengis.referencing.FactoryException ef) {
            throw new ProcessException("Unable to create the target CRS", ef);
        } catch (org.opengis.referencing.operation.TransformException et) {
            throw new ProcessException("Unable to tranform the coordinate system", et);
        }

    }
}
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
package org.geotools.process.feature.gs;

import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.geotools.data.simple.SimpleFeatureCollection;

/**
 * Counts the elements in the collection (useful as a WFS sidekick)
 * 
 * @author Andrea Aime
 */
@DescribeProcess(title = "countFeatures", description = "Counts the number of features in the specified collection")
public class CountProcess implements GSProcess {
    /** The functions this process can handle */
    public enum AggregationFunction {
        Average, Max, Median, Min, StdDev, Sum;
    }

    @DescribeResult(name = "result", description = "The reprojected features")
    public Number execute(
            @DescribeParameter(name = "features", description = "The feature collection that will be aggregate") SimpleFeatureCollection features)
            throws Exception {

        return features.size();
    }

}

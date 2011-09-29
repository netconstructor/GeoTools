/**
 * 
 */
package org.geotools.jdbc.versioning.geogit;

import java.io.IOException;

import org.geogit.api.GeoGIT;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.jdbc.JDBCFeatureSource;
import org.geotools.jdbc.versioning.VersioningFeatureSource;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;


/**
 * @author wdeane
 *
 * @param <T extends SimpleFeatureType>
 * @param <F extends SimpleFeature>
 */
@SuppressWarnings("unchecked")
public class VersionedJDBCFeatureSource<T extends SimpleFeatureType, F extends SimpleFeature> extends JDBCFeatureSource<SimpleFeatureType, SimpleFeature> implements VersioningFeatureSource<SimpleFeatureType, SimpleFeature> {

	protected final GeoGITFacade ggit;
	
	/**
	 * @return the ggit
	 */
	public GeoGITFacade getGeoGIT() {
		return ggit;
	}


	/**
	 * @param entry
	 * @param query
	 * @param geogit
	 * @throws IOException
	 */
	public VersionedJDBCFeatureSource(ContentEntry entry, Query query, GeoGITFacade geogit)
			throws IOException {
		super(entry, query);
		ggit = geogit;
	}


	public VersionedJDBCFeatureSource(ContentFeatureSource fs, GeoGITFacade geogit) throws IOException {
		super(fs.getEntry(), fs.getQuery());
		ggit = geogit;
		
		
		
	}



    @Override
	public SimpleFeatureCollection getLog(String fromVersion, String toVersion,
			Filter filter, String[] userIds, int maxRows) throws Exception {
		// TODO Sanitise Filter
		SimpleFeatureCollection sfc = ggit.getLog(fromVersion, toVersion, this.buildFeatureType(), filter, maxRows);
		if (userIds != null && userIds.length>0) {
			SimpleFeatureCollection sfc2 = new ListFeatureCollection(this.schema);
			SimpleFeatureIterator iterator=sfc.features();
			 try {
			     while( iterator.hasNext()  ){
			          SimpleFeature feature = iterator.next();
			          for (String userid : userIds){
			        	  if (userid.equals(feature.getAttribute("author"))){
			        		  sfc2.add(feature);
			        		  break;
			        	  }
			          }
			     }
			 }
			 finally {
				 iterator.close();
			 }
			 return sfc2;
		}

		return sfc;
	}

/*	@Override
	public void rollback(String toVersion, Filter filter, String[] users)
			throws IOException {
		// TODO Auto-generated method stub
		
	}*/




}

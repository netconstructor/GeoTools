/**
 * 
 */
package org.geotools.jdbc.versioning.geogit;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.OperationNotSupportedException;

import org.geogit.api.GeoGIT;
import org.geogit.api.LogOp;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.repository.StagingArea;
import org.geogit.repository.Triplet;
import org.geogit.storage.ObjectWriter;
import org.geogit.storage.WrappedSerialisingFactory;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.geometry.BoundingBox;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.vividsolutions.jts.io.ParseException;

/**
 * @author wdeane
 * 
 */
public class GeoGITFacade {

	private GeoGIT ggit;
	private boolean hasFid;
    protected static final String commitLogNs = "http://geogit.log";
    protected static final String commitLogName = "CommitLog";
    protected static final String commitLogTypeSpec = "author:String,message:String,timestamp:Long";
    
    
    /**
	 * @param ggit
	 */
	public GeoGITFacade(GeoGIT ggit) {
		super();
		this.ggit = ggit;
	}

	/**
	 * Inserts the Feature to the index and stages it to be committed.
	 */
	protected ObjectId insertAndAdd(Feature f) throws Exception {
		ObjectId objectId = insert(f);

		ggit.add().call();
		return objectId;
	}

	/**
	 * Inserts the feature to the index but does not stages it to be committed
	 */
	protected ObjectId insert(Feature f) throws Exception {
        final StagingArea index = ggit.getRepository().getIndex();
        Name name = f.getType().getName();
        String namespaceURI = name.getNamespaceURI();
        String localPart = name.getLocalPart();
        String id = f.getIdentifier().getID();

        Ref ref = index.inserted(
        		WrappedSerialisingFactory.getInstance().createFeatureWriter(f), f.getBounds(), namespaceURI, localPart, id);
        ObjectId objectId = ref.getObjectId();
        return objectId;
	}

	protected void insertAndAdd(Feature... features) throws Exception {
		insert(features);
		ggit.add().call();
	}
	
	protected void close() throws Exception {
		//ggit.getRepository().close();
	}

	protected void insert(Feature... features) throws Exception {

		 final StagingArea index = ggit.getRepository().getIndex();

	        Iterator<Triplet<ObjectWriter<?>, BoundingBox, List<String>>> iterator;
	        Function<Feature, Triplet<ObjectWriter<?>, BoundingBox, List<String>>> function = new Function<Feature, Triplet<ObjectWriter<?>, BoundingBox, List<String>>>() {

	            @Override
	            public Triplet<ObjectWriter<?>, BoundingBox, List<String>> apply(final Feature f) {
	                Name name = f.getType().getName();
	                String namespaceURI = name.getNamespaceURI();
	                String localPart = name.getLocalPart();
	                String id = f.getIdentifier().getID();

	                Triplet<ObjectWriter<?>, BoundingBox, List<String>> tuple;
	                ObjectWriter<?> writer = WrappedSerialisingFactory.getInstance().createFeatureWriter(f);
	                BoundingBox bounds = f.getBounds();
	                List<String> path = Arrays.asList(namespaceURI, localPart, id);
	                tuple = new Triplet<ObjectWriter<?>, BoundingBox, List<String>>(writer, bounds,
	                        path);
	                return tuple;
	            }
	        };

	        iterator = Iterators.transform(Iterators.forArray(features), function);

	        index.inserted(iterator, new NullProgressListener(), null);

	}

	/**
	 * Deletes a feature from the index
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	protected boolean deleteAndAdd(Feature f) throws Exception {
		boolean existed = delete(f);
		if (existed) {
			ggit.add().call();
		}

		return existed;
	}

	protected boolean delete(Feature f) throws Exception {
		final StagingArea index = ggit.getRepository().getIndex();
		Name name = f.getType().getName();
		String namespaceURI = name.getNamespaceURI();
		String localPart = name.getLocalPart();
		String id = f.getIdentifier().getID();
		boolean existed = index.deleted(namespaceURI, localPart, id);
		return existed;
	}

    @Override
    public String toString() {
		// TODO Auto-generated method stub
		return ggit.toString();
	}
    
    public SimpleFeatureCollection getLog(String fromVersion, String toVersion, FeatureType featureType, Filter filter, Integer maxRows) throws Exception{
    	 SimpleFeatureType logType = DataUtilities.createType(commitLogNs, commitLogName, commitLogTypeSpec);
    	SimpleFeatureCollection commits = new ListFeatureCollection(logType);
    	
    	this.hasFid = false;
    	String[] path = { featureType.getName().getNamespaceURI(), featureType.getName().getLocalPart()};
    	ObjectId from = null;
    	ObjectId until = null;
    	if (fromVersion != null) {
    		from = ObjectId.valueOf(fromVersion);
    	}
    	if (fromVersion != null) {
    		until = ObjectId.valueOf(toVersion);
    	}

    	LogOp logOp = ggit.log();
    	logOp.setSince(from).setUntil(until);
    	processFilter(filter, featureType, logOp);
    	if (!this.hasFid){
    		logOp.addPath(path);
    	}
    	if (maxRows != null){
    		logOp.setLimit(maxRows.intValue());
    	}
    	Iterator<RevCommit> logs = logOp.call();
        for (; logs.hasNext();) {
        	RevCommit rc = logs.next();
        	
        	SimpleFeature sf = feature(logType, rc.getId().printSmallId(), rc.getAuthor(), rc.getMessage(), rc.getTimestamp());
        	commits.add(sf);
        }
		return commits;
       
    }
    
    private SimpleFeature feature(SimpleFeatureType type, String id, Object... values)
            throws ParseException {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (type.getDescriptor(i) instanceof GeometryDescriptor) {
                if (value instanceof String) {
                    value = new WKTReader2().read((String) value);
                }
            }
            builder.set(i, value);
        }
        return builder.buildFeature(id);
    }
    
	private void processFilter(Filter filter, FeatureType featureType, LogOp logOp) throws OperationNotSupportedException {
		if (filter instanceof And){
			for (Filter child: ((And) filter).getChildren()){
				processFilter(child, featureType, logOp);
			}
		} else if (filter instanceof Id){
			this.hasFid=true;
			Id id = (Id) filter;
			Set<Object> ids =  id.getIDs();
			for (Object idVal : ids){
				String[] path = { featureType.getName().getNamespaceURI(), featureType.getName().getLocalPart(), idVal.toString()};
		    	logOp.addPath(path);
			}
				
		} else {
			throw new OperationNotSupportedException(Filter.class.getSimpleName() + " filter is unsupported for logging.");
		}
		
	}

	public void commit() throws Exception {
		ggit.commit().call();
	}
}

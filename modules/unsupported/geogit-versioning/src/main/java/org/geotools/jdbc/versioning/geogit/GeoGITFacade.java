/**
 * 
 */
package org.geotools.jdbc.versioning.geogit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.geogit.api.GeoGIT;
import org.geogit.api.LogOp;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.repository.StagingArea;
import org.geogit.repository.Triplet;
import org.geogit.storage.FeatureWriter;
import org.geogit.storage.ObjectWriter;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.geometry.BoundingBox;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

/**
 * @author wdeane
 *
 */
public class GeoGITFacade {

	private GeoGIT ggit;
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

        Ref ref = index.inserted(new FeatureWriter(f), f.getBounds(), namespaceURI, localPart, id);
        ObjectId objectId = ref.getObjectId();
        return objectId;
    }

    protected void insertAndAdd(Feature... features) throws Exception {
        insert(features);
        ggit.add().call();
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
                ObjectWriter<?> writer = new FeatureWriter(f);
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
    
    public void getLog(String fromVersion, String toVersion, FeatureType featureType, Filter filter, int maxRows) throws Exception{
    	
    	String[] path = { featureType.getName().getNamespaceURI(), featureType.getName().getLocalPart()};
    	ObjectId from = null;
    	ObjectId since = null;
    	if (fromVersion != null) {
    		from = ObjectId.valueOf(fromVersion);
    	}
    	if (fromVersion != null) {
    		since = ObjectId.valueOf(toVersion);
    	}

    	LogOp logOp = ggit.log();
    	
    	processFilter(filter, logOp);
    	Iterator<RevCommit> logs = logOp.setSince(from).setUntil(since).setLimit(maxRows).addPath(path).call();
    	List<RevCommit> logged = new ArrayList<RevCommit>();
        for (; logs.hasNext();) {
            logged.add(logs.next());
        }
        
        
    }

	private void processFilter(Filter filter, LogOp logOp) {
		
		
	}
    
    //public SimpleFeatureCollection getLog(String fromVersion, String toVersion, Filter filter, String[] userIds, int maxRows)

}

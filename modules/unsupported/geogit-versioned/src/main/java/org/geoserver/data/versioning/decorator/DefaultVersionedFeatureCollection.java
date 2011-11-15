package org.geoserver.data.versioning.decorator;

import java.util.Collection;
import java.util.Iterator;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.identity.FeatureId;

public class DefaultVersionedFeatureCollection extends DefaultFeatureCollection {

    public DefaultVersionedFeatureCollection( 
            FeatureCollection<SimpleFeatureType,SimpleFeature> collection ) {
        super(collection);
    }
    
    public DefaultVersionedFeatureCollection(String id, SimpleFeatureType memberType) {
        super(id, memberType);
    }
    
    /**
     * Returns <tt>true</tt> if this collection contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this collection
     * contains at least one element <tt>e</tt> such that <tt>(o==null ?
     * e==null : o.equals(e))</tt>.
     *
     * @param o element whose presence in this collection is to be tested.
     *
     * @return <tt>true</tt> if this collection contains the specified element
     */
    public boolean contains(Object o) {
        // The contract of Set doesn't say we have to cast here, but I think its
        // useful for client sanity to get a ClassCastException and not just a
        // false.
        if( !(o instanceof SimpleFeature) ) return false;
        
        SimpleFeature feature = (SimpleFeature) o;
        String ID = getKey(feature.getIdentifier());
            
        return contents.containsKey( ID ); // || contents.containsValue( feature );        
    }
    
    @Override
    public boolean containsAll( Collection collection ) {
        Iterator iterator = collection.iterator();
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = (SimpleFeature) iterator.next();
                String ID = getKey(feature.getIdentifier());
                if( !contents.containsKey( ID )){
                    return false;
                }                
            }
            return true;
        }
        finally {
            if( collection instanceof FeatureCollection ){
                ((SimpleFeatureCollection)collection).close( iterator );
            }
        }
    }
    
    @Override
    protected boolean add(SimpleFeature feature, boolean fire) {
        // This cast is necessary to keep with the contract of Set!
        if( feature == null ) return false; // cannot add null!
        final String ID = getKey(feature.getIdentifier());
        if( ID == null ) return false; // ID is required!
        if( contents.containsKey( ID ) ) return false; // feature all ready present
        
        if( this.schema == null ) {
                this.schema = feature.getFeatureType(); 
        }
        SimpleFeatureType childType = (SimpleFeatureType) getSchema();
//        if ( childType==null ){
//              //this.childType=
//        }else{
        if( !feature.getFeatureType().equals(childType) )
                LOGGER.warning("Feature Collection contains a heterogeneous" +
                        " mix of features");
                        
//        }
        //TODO check inheritance with FeatureType here!!!
        contents.put( ID, feature );
        if(fire) {
                fireChange(feature, CollectionEvent.FEATURES_ADDED);
        }
        return true;   
    }
    
    private String getKey(FeatureId id) {
        if(id.getFeatureVersion() == null) {
            return id.getID();
        } else {
            return id.getID() + FeatureId.VERSION_SEPARATOR + id.getFeatureVersion();
        }
    }
}

package org.geotools.jdbc;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.identity.FeatureId;

public interface IJDBCFeatureStore<T extends SimpleFeatureType, F extends SimpleFeature> extends FeatureStore<SimpleFeatureType, SimpleFeature> {

	public abstract DataStore getDataStore();

	public abstract ContentEntry getEntry();

	public abstract ResourceInfo getInfo();

	public abstract Name getName();

	public abstract QueryCapabilities getQueryCapabilities();

	public abstract JDBCState getState();

	public abstract Transaction getTransaction();

	public abstract void setTransaction(Transaction transaction);

	public abstract PrimaryKey getPrimaryKey();

	/**
	 * Sets the flag which will expose columns which compose a tables identifying or primary key,
	 * through feature type attributes. 
	 * <p>
	 * Note: setting this flag which affect all feature sources created from or working against 
	 * the current transaction.
	 * </p>
	 */
	public abstract void setExposePrimaryKeyColumns(
			boolean exposePrimaryKeyColumns);

	/**
	 * The flag which will expose columns which compose a tables identifying or primary key,
	 * through feature type attributes.
	 */
	public abstract boolean isExposePrimaryKeyColumns();

	public abstract void modifyFeatures(Name[] names, Object[] values,
			Filter filter) throws IOException;

	public abstract void removeFeatures(Filter filter) throws IOException;
	
	SimpleFeatureCollection getFeatures() throws IOException;
	
	SimpleFeatureType getSchema();
	
	public abstract void modifyFeatures(String[] names, Object[] values,
                Filter filter) throws IOException;

	public List<FeatureId> addFeatures(Collection collection)
                throws IOException;

	
	//List<FeatureId> addFeatures(List<SimpleFeature> list) throws IOException;


}
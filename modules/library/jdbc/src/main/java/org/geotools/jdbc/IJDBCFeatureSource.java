package org.geotools.jdbc;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentEntry;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public interface IJDBCFeatureSource<T extends SimpleFeatureType, F extends SimpleFeature> extends FeatureSource< SimpleFeatureType, SimpleFeature> {

	/**
	 * Type narrow to {@link JDBCDataStore}.
	 */
	public abstract IJDBCDataStore getDataStore();

	/**
	 * Type narrow to {@link JDBCState}.
	 */
	public abstract JDBCState getState();

	/**
	 * Returns the primary key of the table backed by feature store.
	 */
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

	/**
	 * Builds the feature type from database metadata.
	 */
	public abstract SimpleFeatureType buildFeatureType() throws IOException;

	/**
	 * Helper method for splitting a filter.
	 */
	public abstract Filter[] splitFilter(Filter original);

	public abstract ReferencedEnvelope getBoundsInternal(Query query)
			throws IOException;

	public abstract boolean canFilter();

	public abstract boolean canSort();

	public abstract boolean canRetype();

	public abstract boolean canLimit();

	public abstract boolean canOffset();

	public abstract FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
			Query query) throws IOException;

	public abstract boolean handleVisitor(Query query, FeatureVisitor visitor)
			throws IOException;

	public abstract SimpleFeatureType getSchema();

	public abstract Transaction getTransaction();

	public abstract ContentEntry getEntry();

	public abstract void setTransaction(Transaction transaction);

}
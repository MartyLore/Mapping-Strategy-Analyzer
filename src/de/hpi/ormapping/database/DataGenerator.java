package de.hpi.ormapping.database;

import de.hpi.ormapping.structures.Hierarchy;

public interface DataGenerator {

	public void populateDataST(Hierarchy hierarchy) throws Exception;
	public void populateDataTPC(Hierarchy hierarchy) throws Exception;
	public void populateDataTPCC(Hierarchy hierarchy) throws Exception;
	
}

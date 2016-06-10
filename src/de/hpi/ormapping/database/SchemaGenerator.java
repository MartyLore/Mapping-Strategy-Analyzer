package de.hpi.ormapping.database;

import de.hpi.ormapping.structures.Hierarchy;

public interface SchemaGenerator {

	public static String SCHEMA_NAME = "ORM";
	
	public void dropSchema() throws Exception;
	public void generateSTSchema(Hierarchy hierarchy) throws Exception;
	public void generateTPCSchema(Hierarchy hierarchy) throws Exception;
	public void generateTPCCSchema(Hierarchy hierarchy) throws Exception;
	
	public String getFullSchemaName();
	
}

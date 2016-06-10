package de.hpi.ormapping.database;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DataGeneratorFactory {

	public DataGeneratorFactory() {
		// TODO Auto-generated constructor stub
	}

	public static DataGenerator getGeneratorForDB(String db, String schemaName) {

		if (db.equals("hana-row")) {
			return new DefaultDataGenerator(schemaName, db);
		} else if (db.equals("hana-column")) {
			return new DefaultDataGenerator(schemaName, db);
		} else if (db.equals("mysql")) {
			return new DefaultDataGenerator(schemaName, db);
		} else if (db.equals("infinidb")) {
			return new DefaultDataGenerator(schemaName, db);
		} else {
			Logger.getLogger("DataGeneratorFacotry").log(Level.WARNING, "Unsupported Database selected: " + db);
			return null;
		}
	}
}

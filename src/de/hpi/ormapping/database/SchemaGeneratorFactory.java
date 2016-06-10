package de.hpi.ormapping.database;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.hpi.ormapping.database.hana.HANAColumnSchemaGenerator;
import de.hpi.ormapping.database.hana.HANARowSchemaGenerator;
import de.hpi.ormapping.database.infinidb.InfiniDBSchemaGenerator;
import de.hpi.ormapping.database.mysql.MySQLSchemaGenerator;

public class SchemaGeneratorFactory {

	public SchemaGeneratorFactory() {
		// TODO Auto-generated constructor stub
	}

	public static SchemaGenerator getGeneratorForDB(String db) {

		if (db.equals("hana-row")) {
			return new HANARowSchemaGenerator(db);
		} else if (db.equals("hana-column")) {
			return new HANAColumnSchemaGenerator(db);
		} else if (db.equals("mysql")) {
			return new MySQLSchemaGenerator(db);
		} else if (db.equals("infinidb")) {
			return new InfiniDBSchemaGenerator(db);
		} else {
			Logger.getLogger("SchemaGeneratorFactory").log(Level.WARNING, "Unsupported Database selected: " + db);
			return null;
		}
	}
}

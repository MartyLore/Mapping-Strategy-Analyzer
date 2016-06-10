package de.hpi.ormapping.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.hpi.ormapping.structures.ClassAttribute;
import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;

public class DefaultDataGenerator implements DataGenerator {

	public static String connection_identifier = "infinidb";
	private String schemaName;
	
	private static final int BATCH_SIZE = 2500;
	
	public DefaultDataGenerator(String schemaName, String db) {
		if (db != null) {
			connection_identifier = db;
		}
		this.schemaName = schemaName;
	}

	@Override
	public void populateDataST(Hierarchy hierarchy) throws Exception {

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Populate ST Table for " + connection_identifier);

		// get connection
		Connection cn = ConnectionFactory.getConnection(connection_identifier);
		PreparedStatement prepStmt = null;

		int idCounter = 0; // counter to generate consecutive IDs

		// for each class
		for (ClassNode node : hierarchy.classList) {

			// is class is not an abstract class
			if (!node.type.equals("{abstract}")) {

				// generate SQL to insert class-specific attributes
				String sql = generatePreparedStmtForSt(node, hierarchy);
				prepStmt = cn.prepareStatement(sql);

				// generate objects for this class
				for (int i = 1; i <= node.instanceCount; i++) {

					idCounter++;
					prepStmt.setInt(1, idCounter); // ID Column

					int attributeIndex = 2;
					// iterate over class specific attributes and add values
					for (ClassAttribute att : node.collectAllInheritedAttributes(hierarchy)) {
						prepStmt.setObject(attributeIndex, generateRandomValue(att, i));
						attributeIndex++;
					}
					
					// TYPE Column
					prepStmt.setString(attributeIndex, node.className.toUpperCase());
					prepStmt.addBatch();

					if (idCounter % BATCH_SIZE == 0) {
						prepStmt.executeBatch();
					}
				}

				// insert batch of leftover records
				prepStmt.executeBatch();
				prepStmt.close();
			}

		}

		cn.close();
	}

	@Override
	public void populateDataTPC(Hierarchy hierarchy) throws Exception {

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Populate TPC Tables for " + connection_identifier);

		// get connection
		Connection cn = ConnectionFactory.getConnection(connection_identifier);

		int idCounter = 0; // counter to generate consecutive IDs

		// for each class
		for (ClassNode node : hierarchy.classList) {

			// if class is not an abstract class
			if (!node.type.equals("{abstract}")) {

				List<ClassNode> parents = node.getAllParentClasses(hierarchy);
				parents.add(node);

				// prepare statements for class and all parent classes
				Map<String, String> statements = generatePreparedStmtsForTPC(parents, cn);
				Map<String, PreparedStatement> prepStatements = new HashMap<>();
				for(String key : statements.keySet()) {
					prepStatements.put(key, cn.prepareStatement(statements.get(key)));
				}

				// generate objects for this class
				for (int i = 1; i <= node.instanceCount; i++) {

					idCounter++;

					for (ClassNode parent : parents) {
						PreparedStatement prepStmt = prepStatements.get(parent.className);

						prepStmt.setInt(1, idCounter); // ID Column

						int attributeIndex = 2;
						// iterate over class specific attributes and add values
						for (ClassAttribute att : parent.attributes) {
							prepStmt.setObject(attributeIndex, generateRandomValue(att, i));
							attributeIndex++;
						}
						 
						// TYPE COLUMN
						prepStmt.setString(attributeIndex, node.className.toUpperCase());
						prepStmt.addBatch();

						if (idCounter % BATCH_SIZE == 0) {
							for (PreparedStatement stmt : prepStatements.values()) {
								stmt.executeBatch();
							}
						}
					}

				}

				// insert batch of leftover records
				for (PreparedStatement stmt : prepStatements.values()) {
					stmt.executeBatch();
					stmt.close();
				}
			}

		}

		cn.close();
	}

	@Override
	public void populateDataTPCC(Hierarchy hierarchy) throws Exception {

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Populate TPCC Tables for " + connection_identifier);

		// get connection
		Connection cn = ConnectionFactory.getConnection(connection_identifier);
		PreparedStatement prepStmt = null;

		// for each class
		for (ClassNode node : hierarchy.classList) {

			// is class is not an abstract class
			if (!node.type.equals("{abstract}")) {

				// generate SQL to insert class-specific attributes
				String sql = generatePreparedStmtForTPCC(node, hierarchy);
				prepStmt = cn.prepareStatement(sql);

				// generate objects for this class
				for (int i = 1; i <= node.instanceCount; i++) {

					prepStmt.setInt(1, i); // ID

					int attributeIndex = 2;
					// iterate over class specific attributes and add values
					for (ClassAttribute att : node.collectAllInheritedAttributes(hierarchy)) {
						prepStmt.setObject(attributeIndex, generateRandomValue(att, i));
						attributeIndex++;
					}

					prepStmt.addBatch();

					if (i % BATCH_SIZE == 0) {
						prepStmt.executeBatch();
					}
				}

				// insert batch of leftover records
				prepStmt.executeBatch();
				prepStmt.close();
			}

		}

		cn.close();
	}
	
	/**
	 * 
	 * SQL generator methods
	 * 
	 */

	public String generatePreparedStmtForSt(ClassNode node, Hierarchy hierarchy) {

		StringBuilder buffer = new StringBuilder(String.format("INSERT INTO %1s.ST_%2s (ID, ", schemaName, hierarchy.classList.get(0).className.toUpperCase()));

		for (ClassAttribute att : node.collectAllInheritedAttributes(hierarchy)) {
			buffer.append(att.attName.toUpperCase()).append(", ");
		}
		buffer.append(" TYPE) VALUES (?,");

		for (int i = 0; i < node.collectAllInheritedAttributes(hierarchy).size(); i++) {
			buffer.append("?,");
		}
		buffer.append("?)");

		return buffer.toString();
	}

	public Map<String, String> generatePreparedStmtsForTPC(List<ClassNode> parents, Connection cn) throws SQLException {

		Map<String, String> stmts = new HashMap<>();

		for (ClassNode node : parents) {

			StringBuilder buffer = new StringBuilder(String.format("INSERT INTO %1s.TPC_%2s (ID, ", schemaName, node.className.toUpperCase()));

			for (ClassAttribute att : node.attributes) {
				buffer.append(att.attName.toUpperCase()).append(", ");
			}
			buffer.append("TYPE) VALUES (?,");

			for (int i = 0; i < node.attributes.size(); i++) {
				buffer.append("?,");
			}
			buffer.append("?)");

			stmts.put(node.className, buffer.toString());
		}

		return stmts;
	}

	public String generatePreparedStmtForTPCC(ClassNode node, Hierarchy hierarchy) {

		StringBuilder buffer = new StringBuilder(String.format("INSERT INTO %1s.TPCC_%2s (ID, ", schemaName, node.className.toUpperCase()));

		for (ClassAttribute att : node.collectAllInheritedAttributes(hierarchy)) {
			buffer.append(att.attName.toUpperCase()).append(", ");
		}
		buffer.setLength(buffer.length() - 2);

		buffer.append(") VALUES (?,");

		for (int i = 0; i < node.collectAllInheritedAttributes(hierarchy).size(); i++) {
			buffer.append("?,");
		}
		buffer.setLength(buffer.length() - 1);

		buffer.append(")");

		return buffer.toString();
	}

	/**
	 * 
	 * SQL randomize functions
	 * 
	 */
	
	public static Object generateRandomValue(ClassAttribute att, int recordId) {
		Object randomValue = null;
		
		if (att.attType.equals("STRING")) {
			randomValue = String.format("%1s_%2d", att.attName, recordId % att.getAttDistinctCount());
		} else if (att.attType.equals("INTEGER")) {
			randomValue = recordId % att.getAttDistinctCount();
		} else if (att.attType.equals("DOUBLE")) {
			randomValue = recordId % att.getAttDistinctCount();
		} else if (att.attType.equals("DATE")) {
			Calendar c = Calendar.getInstance();
			c.set(2000, 1, 1);

			long startDate = c.getTimeInMillis();
			long offset = 1000 * (att.getAttDistinctCount() % recordId);

			randomValue =  new Date(startDate + offset);
		}
		
		return randomValue;
	}

}

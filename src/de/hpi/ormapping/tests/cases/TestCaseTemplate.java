package de.hpi.ormapping.tests.cases;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import de.hpi.ormapping.database.ConnectionFactory;
import de.hpi.ormapping.database.SchemaGeneratorFactory;
import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;
import de.hpi.ormapping.tests.Measurement;

/**
 * 
 * @author Martin Lorenz
 * 
 *         All classes that inherit directly from TestCaseTemplate should
 *         implement test functionality that conforms to standard SQL.
 *         Database-specific extensions or optimization have to be implemented
 *         in more specialized classes.
 *
 */

public abstract class TestCaseTemplate implements Comparable<TestCaseTemplate> {

	private static final String testPackage = "de.hpi.ormapping.tests.cases";

	public static final int ST_Strategy = 0;
	public static final int TPC_Strategy = 1;
	public static final int TPCC_Strategy = 2;

	protected Connection cn;
	protected Hierarchy hierarchy;
	protected String schemaName;

	public TestCaseTemplate() {
	}

	public TestCaseTemplate(String db, Hierarchy hierarchy) throws Exception {
		cn = ConnectionFactory.getConnection(db);
		schemaName = SchemaGeneratorFactory.getGeneratorForDB(db).getFullSchemaName();
		this.hierarchy = hierarchy;
	}

	/**
	 * 
	 * measurement methods
	 * 
	 */

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public abstract String getDescription();

	public abstract String getDatabse();

	public abstract String getQueryTemplate(int strategy, ClassNode node);

	public abstract void prepare() throws Exception;

	public abstract Map<String, Measurement> executeForST(int repetitions) throws Exception;

	public abstract Map<String, Measurement> executeForTPC(int repetitions) throws Exception;

	public abstract Map<String, Measurement> executeForTPCC(int repetitions) throws Exception;

	public abstract void tearDown() throws Exception;

	public void cleanUp() throws Exception {
		cn.close();
	}

	/**
	 * 
	 * helper methods
	 * 
	 */

	@Override
	public int compareTo(TestCaseTemplate other) {
		return getName().compareTo(other.getName());
	}

	protected double measure(Statement stmt, String query) throws Exception {
		double start = System.nanoTime();
		stmt.execute(query);
		double time = System.nanoTime() - start;
		return time / 1000000;
	}

	protected double measurePreparedStmt(PreparedStatement stmt) throws Exception {
		double start = System.nanoTime();
		stmt.execute();
		double time = System.nanoTime() - start;
		return time / 1000000;
	}

	protected double measureBatchExecution(Statement stmt) throws Exception {
		double start = System.nanoTime();
		stmt.executeBatch();
		double time = System.nanoTime() - start;
		return time / 1000000;
	}
	
	public static TestCaseTemplate getDatabaseSepcificTestCaseImplementation(String testCaseName, String db, Hierarchy hierarchy) throws Exception {

		// get default TestImplementation (standard SQL)
		TestCaseTemplate template = (TestCaseTemplate) Class.forName(testPackage + "." + testCaseName).newInstance();

		// check if a special test implementation for this type of database
		// exists
		Reflections reflections = new Reflections(testPackage);
		Set<?> subTypes = reflections.getSubTypesOf(template.getClass());

		Iterator typeIt = subTypes.iterator();
		while (typeIt.hasNext()) {
			Class subType = (Class) typeIt.next();
			TestCaseTemplate specializedTemplate = (TestCaseTemplate) Class.forName(subType.getName()).newInstance();

			if (specializedTemplate.getDatabse().equals(db)) {
				template = specializedTemplate;
			}
		}

		// create instance of the found test implementation
		Constructor<?> constructor = template.getClass().getConstructor(String.class, Hierarchy.class);
		TestCaseTemplate test = (TestCaseTemplate) constructor.newInstance(db, hierarchy);

		return test;
	}

	protected Measurement calculateMeasures(List<Double> measurements) {

		Measurement result = new Measurement();

		double low = measurements.get(0);
		double high = 0;
		Double sum = .0;

		for (double data : measurements) {
			sum += data;

			if (low > data) {
				low = data;
			}

			if (high < data) {
				high = data;
			}
		}

		result.avgValue = sum / measurements.size();
		result.maxValue = high;
		result.minValue = low;

		return result;
	}
}

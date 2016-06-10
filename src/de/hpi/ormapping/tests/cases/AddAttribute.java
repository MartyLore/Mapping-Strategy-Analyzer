package de.hpi.ormapping.tests.cases;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;
import de.hpi.ormapping.tests.Measurement;

public class AddAttribute extends TestCaseTemplate {

	protected final String COL_NAME = "NEW_COLUMN";

	public AddAttribute() {
		super();
	}

	public AddAttribute(String db, Hierarchy hierarchy) throws Exception {
		super(db, hierarchy);
	}

	@Override
	public String getDescription() {
		return "Single SELECT on a key attribute and full materialization of the object";
	}

	@Override
	public String getDatabse() {
		return "ansi-sql";
	}

	@Override
	public String getQueryTemplate(int strategy, ClassNode node) {

		StringBuilder builder = new StringBuilder();

		String queryTemplate = null;

		if (strategy == ST_Strategy) {

			builder.append(String.format("ALTER TABLE %1$s.ST_%2$s ADD %3$s INTEGER", schemaName, hierarchy.classList.get(0).className.toUpperCase(), COL_NAME));

		} else if (strategy == TPC_Strategy) {

			builder.append(String.format("ALTER TABLE %1$s.TPC_%2$s ADD %3$s INTEGER", schemaName, node.className.toUpperCase(), COL_NAME));

		} else if (strategy == TPCC_Strategy) {

			if (!node.type.equals("{abstract}")) {
				builder.append(String.format("ALTER TABLE %1$s.TPCC_%2$s ADD %3$s INTEGER;", schemaName, node.className.toUpperCase(), COL_NAME));
			}

			List<ClassNode> children = node.getAllChildClasses(hierarchy);
			for (ClassNode child : children) {
				if (!child.type.equals("{abstract}")) {
					builder.append(String.format("ALTER TABLE %1$s.TPCC_%2$s ADD %3$s INTEGER;", schemaName, child.className.toUpperCase(), COL_NAME));
				}
			}

		}

		queryTemplate = builder.toString();
		return queryTemplate;
	}

	private Map<String, Measurement> execute(int repetitions, int strategy) throws Exception {

		Map<String, Measurement> result = new HashMap<>();

		for (ClassNode node : hierarchy.classList) {

			String queryTemplate = getQueryTemplate(strategy, node);
			String[] subQueries = queryTemplate.split(";");
			Statement stmt = cn.createStatement();

			for (String query : subQueries) {
				stmt.addBatch(query);
			}

			List<Double> queryTimes = new ArrayList<>();
			double executionTime = measureBatchExecution(stmt);
			queryTimes.add(executionTime);

			result.put(node.className, calculateMeasures(queryTimes));
			stmt.close();

			removeAllNewColumns();
		}

		return result;
	}

	@Override
	public void prepare() throws Exception {
		removeAllNewColumns();
	}

	@Override
	public Map<String, Measurement> executeForST(int repetitions) throws Exception {
		return execute(repetitions, ST_Strategy);
	}

	@Override
	public Map<String, Measurement> executeForTPC(int repetitions) throws Exception {
		return execute(repetitions, TPC_Strategy);
	}

	@Override
	public Map<String, Measurement> executeForTPCC(int repetitions) throws Exception {
		// TODO Auto-generated method stub
		return execute(repetitions, TPCC_Strategy);
	}

	@Override
	public void tearDown() throws Exception {

	}

	private void removeAllNewColumns() throws Exception {

		Statement stmt = cn.createStatement();

		String query = String.format("ALTER TABLE %1$s.ST_%2$s DROP COLUMN %3$s", schemaName, hierarchy.classList.get(0).className.toUpperCase(), COL_NAME);

		try {
			stmt.executeUpdate(query);
		} catch (Exception e) {}

		for (ClassNode node : hierarchy.classList) {
			query = String.format("ALTER TABLE %1$s.TPC_%2$s DROP COLUMN %3$s", schemaName, node.className.toUpperCase(), COL_NAME);

			try {
				stmt.executeUpdate(query);
			} catch (Exception e) {}

			if (node.type.equals("{concrete}")) {
				query = String.format("ALTER TABLE %1$s.TPCC_%2$s DROP COLUMN %3$s", schemaName, node.className.toUpperCase(), COL_NAME);
			}

			try {
				stmt.executeUpdate(query);
			} catch (Exception e) {}

		}

		stmt.close();

	}

}

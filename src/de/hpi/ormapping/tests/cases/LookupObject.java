package de.hpi.ormapping.tests.cases;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;
import de.hpi.ormapping.tests.Measurement;

public class LookupObject extends TestCaseTemplate {

	public LookupObject() {
		super();
	}

	public LookupObject(String db, Hierarchy hierarchy) throws Exception {
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
	public Map<String, Measurement> executeForST(int repetitions) throws Exception {
		return execute(repetitions, ST_Strategy);
	}

	@Override
	public Map<String, Measurement> executeForTPC(int repetitions) throws Exception {
		return execute(repetitions, TPC_Strategy);
	}

	@Override
	public Map<String, Measurement> executeForTPCC(int repetitions) throws Exception {
		return execute(repetitions, TPCC_Strategy);
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
	}

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getQueryTemplate(int strategy, ClassNode node) {

		StringBuilder builder = new StringBuilder();
		
		String queryTemplate = null;
		if (strategy == ST_Strategy) {

			builder.append(String.format("SELECT * FROM %1$s.ST_%2$s WHERE ID=?", schemaName, hierarchy.classList.get(0).className.toUpperCase()));

		} else if (strategy == TPC_Strategy) {

			List<ClassNode> parents = node.getAllParentClasses(hierarchy);
			builder.append("SELECT * FROM ").append(schemaName).append(".TPC_").append(node.className.toUpperCase()).append(" AS ").append(node.className.toUpperCase());

			for (ClassNode parent : parents) {
				builder.append(", ").append(schemaName).append(".").append("TPC_").append(parent.className.toUpperCase()).append(" AS ").append(parent.className.toUpperCase());
			}

			builder.append(" WHERE ").append(node.className.toUpperCase()).append(".ID=?");

			for (ClassNode parent : parents) {
				builder.append(" AND ").append(parent.className.toUpperCase()).append(".ID=").append(node.className.toUpperCase()).append(".ID");
			}

		} else if (strategy == TPCC_Strategy) {

			builder.append(String.format("SELECT * FROM %1$s.TPCC_%2$s WHERE ID=?", schemaName, node.className.toUpperCase()));

		}

		queryTemplate = builder.toString();
		return queryTemplate;
	}

	private Map<String, Measurement> execute(int repetitions, int strategy) throws Exception {

		Map<String, Measurement> result = new HashMap<>();

		for (ClassNode node : hierarchy.classList) {

			if (!node.type.equals("{abstract}")) {

				List<Long> probes = getRandomIds(strategy, repetitions, node);

				String queryTemplate = getQueryTemplate(strategy, node);
				PreparedStatement stmt = cn.prepareStatement(queryTemplate);

				List<Double> queryTimes = new ArrayList<>();
				for (Long probe : probes) {

					stmt.setLong(1, probe);
					double executionTime = measurePreparedStmt(stmt);
					queryTimes.add(executionTime);

				}

				result.put(node.className, calculateMeasures(queryTimes));
				stmt.close();
			}
		}

		return result;
	}

	private List<Long> getRandomIds(int strategy, int numberOfRepetitions, ClassNode node) throws Exception {

		List<Long> probes = new ArrayList<>();
		Random random = new Random();
		
		long lower_bound = 1;
		
		if(strategy == TPC_Strategy) {
			Statement stmt = cn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT ID FROM "+schemaName.toUpperCase()+".TPC_"+node.className.toUpperCase()+" ORDER BY ID ASC LIMIT 1");
			rs.next();
			lower_bound = rs.getLong(1);
			rs.close();
			stmt.close();
		}
		
		long upper_bound = lower_bound + node.instanceCount;
		
		for (int i = 0; i < numberOfRepetitions; i++) {
			long randomValue = lower_bound + (long) (random.nextDouble() * (upper_bound - lower_bound));
			probes.add(randomValue);
		}

		return probes;
	}
}

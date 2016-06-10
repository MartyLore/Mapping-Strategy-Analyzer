package de.hpi.ormapping.tests.cases;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hpi.ormapping.database.DefaultDataGenerator;
import de.hpi.ormapping.structures.ClassAttribute;
import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;
import de.hpi.ormapping.tests.Measurement;

public class TableScanNonPolymorph extends TestCaseTemplate {

	public TableScanNonPolymorph() {
	}

	public TableScanNonPolymorph(String db, Hierarchy hierarchy) throws Exception {
		super(db, hierarchy);
	}

	@Override
	public String getDescription() {
		return "";
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

		String rootClassName = hierarchy.classList.get(0).className.toUpperCase();
		String nodeClassName = node.className.toUpperCase();
		String queryAttributeName = hierarchy.classList.get(0).attributes.get(0).attName.toUpperCase();

		StringBuilder builder = new StringBuilder();

		String queryTemplate = null;
		if (strategy == ST_Strategy) {

			builder.append(String.format("SELECT %1$s FROM %2$s.ST_%3$s WHERE TYPE='%4$s' AND %1$s=?", queryAttributeName, schemaName, rootClassName, nodeClassName));

		} else if (strategy == TPC_Strategy) {

			List<ClassNode> parents = node.getAllParentClasses(hierarchy);
			builder.append("SELECT ").append(queryAttributeName).append(" FROM ").append(schemaName).append(".TPC_").append(nodeClassName).append(" AS ").append(nodeClassName);

			for (ClassNode parent : parents) {
				builder.append(", ").append(schemaName).append(".").append("TPC_").append(parent.className.toUpperCase()).append(" AS ").append(parent.className.toUpperCase());
			}

			builder.append(" WHERE ").append(nodeClassName).append(".type='").append(node.className).append("' AND ").append(nodeClassName).append(".").append(node.attributes.get(0).attName.toUpperCase()).append("=?");

			for (ClassNode parent : parents) {
				builder.append(" AND ").append(parent.className.toUpperCase()).append(".ID=").append(nodeClassName).append(".ID");
			}

		} else if (strategy == TPCC_Strategy) {

			builder.append(String.format("SELECT %1$s FROM %2$s.TPCC_%3$s WHERE %1$s=?", queryAttributeName, schemaName, nodeClassName));

		}

		queryTemplate = builder.toString();
		return queryTemplate;
	}

	private Map<String, Measurement> execute(int repetitions, int strategy) throws Exception {

		Map<String, Measurement> result = new HashMap<>();

		for (ClassNode node : hierarchy.classList) {

			if (!node.type.equals("{abstract}")) {

				String queryTemplate = getQueryTemplate(strategy, node);
				PreparedStatement stmt = cn.prepareStatement(queryTemplate);

				List<Double> queryTimes = new ArrayList<>();
				List<Object> probes = getRandomValues(repetitions, node.attributes.get(0));
				for (Object probe : probes) {

					for (int i = 1; i <= stmt.getParameterMetaData().getParameterCount(); i++) {
						stmt.setObject(i, probe);
					}

					double executionTime = measurePreparedStmt(stmt);
					queryTimes.add(executionTime);

				}

				stmt.close();
				result.put(node.className, calculateMeasures(queryTimes));
			}
		}

		return result;
	}

	private List<Object> getRandomValues(int numberOfRepetitions, ClassAttribute att) {

		List<Object> probes = new ArrayList<>();

		for (int i = 0; i < numberOfRepetitions; i++) {
			probes.add(DefaultDataGenerator.generateRandomValue(att, i));
		}

		return probes;
	}

}

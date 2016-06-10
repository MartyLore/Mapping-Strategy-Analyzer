package de.hpi.ormapping.tests.cases.hana;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.hpi.ormapping.database.DefaultDataGenerator;
import de.hpi.ormapping.structures.ClassAttribute;
import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;
import de.hpi.ormapping.tests.Measurement;
import de.hpi.ormapping.tests.cases.RangeSelectPolymorph;

public class RangeSelectPolymorphHANARS extends RangeSelectPolymorph {

	public RangeSelectPolymorphHANARS() {
	}

	public RangeSelectPolymorphHANARS(String db, Hierarchy hierarchy) throws Exception {
		super(db, hierarchy);
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getDatabse() {
		return "hana-row";
	}

	@Override
	public String getQueryTemplate(int strategy, ClassNode node) {

		String rootClassName = hierarchy.classList.get(0).className.toUpperCase();
		String queryAttributeName = hierarchy.classList.get(0).attributes.get(0).attName.toUpperCase();

		List<ClassNode> polyMorphClasses = new ArrayList<>();
		polyMorphClasses.addAll(node.getAllConcreteChildClasses(hierarchy));
		if (node.type.equals("{concrete}")) {
			polyMorphClasses.add(node);
		}

		List<String> polyMorphClassNames = new ArrayList<>();
		for (ClassNode clazz : polyMorphClasses) {
			polyMorphClassNames.add("'" + clazz.className + "' ");
		}

		String inFilter = StringUtils.join(polyMorphClassNames, ",");

		StringBuilder builder = new StringBuilder();

		String queryTemplate = null;
		if (strategy == ST_Strategy) {

			builder.append(String.format("SELECT %1$s FROM %2$s.ST_%3$s WHERE TYPE IN (%4$s) AND %1$s=?", queryAttributeName, schemaName, rootClassName, inFilter));

		} else if (strategy == TPC_Strategy) {

			builder.append("SELECT ").append(queryAttributeName).append(" FROM ").append(schemaName).append(".TPC_").append(rootClassName);
			builder.append(" WHERE ").append(queryAttributeName).append("=? AND TYPE IN (").append(inFilter).append(")");

		} else if (strategy == TPCC_Strategy) {

			Iterator<ClassNode> childIt = polyMorphClasses.iterator();

			while (childIt.hasNext()) {
				ClassNode child = childIt.next();
				builder.append(String.format("SELECT %1$s FROM %2$s.TPCC_%3$s WHERE %1$s=?", queryAttributeName, schemaName, child.className.toUpperCase()));
				if (childIt.hasNext()) {
					builder.append(" UNION ALL ");
				}
			}

		}

		queryTemplate = builder.toString();
		return queryTemplate;
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
	public void prepare() throws Exception {

		Statement stmt = cn.createStatement();

		String className = hierarchy.classList.get(0).className.toUpperCase();
		String attributeName = hierarchy.classList.get(0).attributes.get(0).attName.toUpperCase();

		// prepare ST
		stmt.executeUpdate(String.format("CREATE INDEX IDX_ST_%1$s_%2$s ON %3$s.ST_%4$s (%5$s)", className, attributeName, schemaName, className, attributeName));

		// prepare TPC & TPCC
		for (ClassNode node : hierarchy.classList) {
			stmt.executeUpdate(String.format("CREATE INDEX IDX_TPC_%1$s_%2$s ON %3$s.TPC_%4$s (%5$s)", node.className.toUpperCase(), node.attributes.get(0).attName, schemaName, node.className.toUpperCase(), node.attributes.get(0).attName));
			if (node.type.equals("{concrete}")) {
				stmt.executeUpdate(String.format("CREATE INDEX IDX_TPCC_%1$s_%2$s ON %3$s.TPCC_%4$s (%5$s)", node.className.toUpperCase(), attributeName, schemaName, node.className.toUpperCase(), attributeName));
			}
		}

		stmt.close();
	}

	@Override
	public void tearDown() throws Exception {

		Statement stmt = cn.createStatement();

		String className = hierarchy.classList.get(0).className.toUpperCase();
		String attributeName = hierarchy.classList.get(0).attributes.get(0).attName.toUpperCase();

		// prepare ST
		stmt.executeUpdate(String.format("DROP INDEX %1$s.IDX_ST_%2$s_%3$s", schemaName, className, attributeName));

		// prepare TPC & TPCC
		for (ClassNode node : hierarchy.classList) {
			stmt.executeUpdate(String.format("DROP INDEX %1$s.IDX_TPC_%2$s_%3$s", schemaName, node.className.toUpperCase(), node.attributes.get(0).attName));
			if (node.type.equals("{concrete}")) {
				stmt.executeUpdate(String.format("DROP INDEX %1$s.IDX_TPCC_%2$s_%3$s", schemaName, node.className.toUpperCase(), attributeName));
			}
		}

		stmt.close();
	}

	private Map<String, Measurement> execute(int repetitions, int strategy) throws Exception {

		Map<String, Measurement> result = new HashMap<>();

		for (ClassNode node : hierarchy.classList) {

			List<Object> probes = getRandomValues(repetitions, node.attributes.get(0));

			String queryTemplate = getQueryTemplate(strategy, node);
			PreparedStatement stmt = cn.prepareStatement(queryTemplate);

			List<Double> queryTimes = new ArrayList<>();
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

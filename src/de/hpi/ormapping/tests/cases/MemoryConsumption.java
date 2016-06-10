package de.hpi.ormapping.tests.cases;

import java.util.Map;

import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;
import de.hpi.ormapping.tests.Measurement;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class MemoryConsumption extends TestCaseTemplate {

	public MemoryConsumption() {
		super();
	}

	public MemoryConsumption(String db, Hierarchy hierarchy) throws Exception {
		super(db, hierarchy);
	}

	@Override
	public String getDescription() {
		return "Determine the memory footprint of a particular strategy";
	}

	@Override
	public String getDatabse() {
		return "ansi-sql";
	}

	@Override
	public String getQueryTemplate(int strategy, ClassNode node) {
		throw new NotImplementedException();
	}

	@Override
	public Map<String, Measurement> executeForST(int repetitions) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public Map<String, Measurement> executeForTPC(int repetitions) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public Map<String, Measurement> executeForTPCC(int repetitions) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
	}

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub
	}

}

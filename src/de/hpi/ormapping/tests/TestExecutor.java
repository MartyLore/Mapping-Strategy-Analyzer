package de.hpi.ormapping.tests;

import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.hpi.ormapping.structures.Hierarchy;
import de.hpi.ormapping.tests.cases.TestCaseTemplate;

public class TestExecutor extends Thread {

	Queue<ResultTemplate> resultQueue;
	List<String> selectedTests;
	List<String> selectedDbs;
	Hierarchy hierarchy;
	int testRuns;

	public TestExecutor(Queue<ResultTemplate> resultQueue, List<String> selectedTests, List<String> selectedDbs, Hierarchy hierarchy, int testRuns) {
		this.resultQueue = resultQueue;
		this.selectedTests = selectedTests;
		this.selectedDbs = selectedDbs;
		this.hierarchy = hierarchy;
		this.testRuns = testRuns;
	}

	@Override
	public void run() {

		// for every test
		for (String testCase : selectedTests) {

			// for every db
			for (String db : selectedDbs) {

				try {
					TestCaseTemplate test = TestCaseTemplate.getDatabaseSepcificTestCaseImplementation(testCase, db, hierarchy);

					Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Execute Test " + test.getClass().getSimpleName() + " on DB (" + db + ")");

					ResultTemplate template = new ResultTemplate();
					template.testCase = testCase;
					template.database = db;
					
					test.prepare();
					template.setSTMeasurements(test.executeForST(testRuns));
					template.setTPCMeasurements(test.executeForTPC(testRuns));
					template.setTPCCMeasurements(test.executeForTPCC(testRuns));
					test.tearDown();
					
					if(testCase.contains("MemoryConsumption")) {
						template.aggregateSumST();
						template.aggregateSumTPC();
						template.aggregateSumTPCC();
					} else {
						template.aggregateAverageST();
						template.aggregateAverageTPC();
						template.aggregateAverageTPCC();
					}
					
					template.calculateFastestStrategyPerClass();
					template.calculateFastestStrategyOnAvg();
					
					resultQueue.add(template);

					test.cleanUp();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}

}

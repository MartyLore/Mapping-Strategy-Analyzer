package de.hpi.ormapping.servlets.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.hpi.ormapping.database.DataGenerator;
import de.hpi.ormapping.structures.Hierarchy;

public class DataGenerationWorker extends Thread {

	public static final int ST_STRATEGY = 0;
	public static final int TPC_STRATEGY = 1;
	public static final int TPCC_STRATEGY = 2;

	public int resultCode = 0;
	public String taskName = "";
	public String resultMessage = "Success";
	
	private DataGenerator generator = null;
	private int selectedStrategy = -1;
	private Hierarchy hierarchy = null;

	public DataGenerationWorker(DataGenerator generator, int strategy, Hierarchy hierarchy) {
		this.generator = generator;
		this.selectedStrategy = strategy;
		this.hierarchy = hierarchy;
	}

	public DataGenerationWorker() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {

		try {
			if (selectedStrategy == ST_STRATEGY) {
				generator.populateDataST(hierarchy);
			} else if (selectedStrategy == TPC_STRATEGY) {
				generator.populateDataTPC(hierarchy);
			} else if (selectedStrategy == TPCC_STRATEGY) {
				generator.populateDataTPCC(hierarchy);
			} else {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "No matching strategy found. Selected startegy was " + selectedStrategy);
			}
		} catch (Exception e) {
			resultCode = 1;
			resultMessage = e.getMessage();
			e.printStackTrace();
		}
	}

}

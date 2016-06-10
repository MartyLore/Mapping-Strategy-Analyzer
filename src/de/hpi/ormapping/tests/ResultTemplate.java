package de.hpi.ormapping.tests;

import java.util.Map;

public class ResultTemplate {

	public String testCase;
	public String database;
	public Map<String, Measurement> ST;
	public Map<String, Measurement> TPC;
	public Map<String, Measurement> TPCC;
	
	public double ST_AGG;
	public double TPC_AGG;
	public double TPCC_AGG;
	
	public String fastestStrategyOnAvg = null;
	
	public ResultTemplate() {
		// TODO Auto-generated constructor stub
	}

	// ST Measurements
	
	public void setSTMeasurements(Map<String, Measurement> measurements) {
		ST = measurements;
	}
	
	public void aggregateAverageST() {
		double sum = 0;
		for(Measurement m : ST.values()) {
			sum += m.avgValue;
		}
		
		ST_AGG = sum/ST.size();
	}
	
	public void aggregateSumST() {
		double sum = 0;
		for(Measurement m : ST.values()) {
			sum += m.avgValue;
		}
		
		ST_AGG = sum;
	}
	
	// TPC Measurements

	public void setTPCMeasurements(Map<String, Measurement> measurements) {
		TPC = measurements;
	}
	
	public void aggregateAverageTPC() {
		double sum = 0;
		for(Measurement m : TPC.values()) {
			sum += m.avgValue;
		}
		
		TPC_AGG = sum/TPC.size();
	}
	
	public void aggregateSumTPC() {
		double sum = 0;
		for(Measurement m : TPC.values()) {
			sum += m.avgValue;
		}
		
		TPC_AGG = sum;
	}
	
	// TPCC Measurements

	public void setTPCCMeasurements(Map<String, Measurement> measurements) {
		TPCC = measurements;
	}
	
	public void aggregateAverageTPCC() {
		double sum = 0;
		for(Measurement m : TPCC.values()) {
			sum += m.avgValue;
		}
		
		TPCC_AGG = sum/TPCC.size();
	}
	
	public void aggregateSumTPCC() {
		double sum = 0;
		for(Measurement m : TPCC.values()) {
			sum += m.avgValue;
		}
		
		TPCC_AGG = sum;
	}
	
	// Calculations
		
	public void calculateFastestStrategyPerClass() {
		for(String className : ST.keySet()) {
			Measurement st_m = ST.get(className);
			Measurement tpc_m = TPC.get(className);
			Measurement tpcc_m = TPCC.get(className);
			
			if(st_m.avgValue < tpc_m.avgValue && st_m.avgValue < tpcc_m.avgValue) {
				st_m.fastest = true;
			} else if (tpc_m.avgValue < st_m.avgValue && tpc_m.avgValue < tpcc_m.avgValue) {
				tpc_m.fastest = true;
			} else {
				tpcc_m.fastest = true;
			}
		}
	}
	
	public void calculateFastestStrategyOnAvg() {

		if(ST_AGG < TPC_AGG && ST_AGG < TPCC_AGG) {
			fastestStrategyOnAvg = "ST";
		} else if (TPC_AGG < ST_AGG && TPC_AGG < TPCC_AGG) {
			fastestStrategyOnAvg = "TPC";
		} else {
			fastestStrategyOnAvg = "TPCC";
		}
		
	}
}

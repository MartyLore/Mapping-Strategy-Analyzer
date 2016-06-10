package de.hpi.ormapping.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hpi.ormapping.database.DataGenerator;
import de.hpi.ormapping.database.DataGeneratorFactory;
import de.hpi.ormapping.database.SchemaGenerator;
import de.hpi.ormapping.database.SchemaGeneratorFactory;
import de.hpi.ormapping.servlets.util.DataGenerationWorker;
import de.hpi.ormapping.structures.Hierarchy;

/**
 * Servlet implementation class ModelConsumer
 */
@WebServlet("/ModelConsumer")
public class ModelConsumer extends HttpServlet {

	private static final long serialVersionUID = 1L;

	List<DataGenerationWorker> workers = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ModelConsumer() {
		super();
		workers = new ArrayList<>();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		List<WorkerState> workerStates = new ArrayList<>();

		for (DataGenerationWorker worker : workers) {

			WorkerState state = new WorkerState();
			state.workerName = worker.taskName;
			if (worker.isAlive()) {
				state.statusCode = WorkerState.RUNNING;
			} else {
				state.statusCode = WorkerState.FINISHED;
				state.resultMessage = worker.resultMessage;
			}

			workerStates.add(state);
		}

		response.setContentType("application/json");
		response.getWriter().append(mapper.writeValueAsString(workerStates));
		response.getWriter().flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		List<String> selectedDBs = mapper.readValue(request.getParameter("dbs"), List.class);
		Hierarchy hierarchy = mapper.readValue(request.getParameter("classModel"), Hierarchy.class);

		//System.out.println(request.getParameter("classModel"));
		
		String resultMessage = "{message: \"ok\"}";

		workers.clear();
		for (String selectedDB : selectedDBs) {
			try {
				setup(hierarchy, selectedDB);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage());
				response.setStatus(500);
				resultMessage = "{message: \"" + e.getMessage() + "\"}";

			}
		}

		response.getWriter().append(resultMessage);
		response.getWriter().flush();

	}

	private void setup(Hierarchy hierarchy, String selectedDB) throws Exception {

		// client needs to send info what databases should be analyzed
		SchemaGenerator schemaGen = SchemaGeneratorFactory.getGeneratorForDB(selectedDB);
		schemaGen.dropSchema();

		schemaGen.generateSTSchema(hierarchy);
		schemaGen.generateTPCSchema(hierarchy);
		schemaGen.generateTPCCSchema(hierarchy);

		DataGenerator dataGen = DataGeneratorFactory.getGeneratorForDB(selectedDB, schemaGen.getFullSchemaName());

		String nameFormat = "%1$s for " + selectedDB;
		
		// initiate data generation for ST
		DataGenerationWorker stWorker = new DataGenerationWorker(dataGen, DataGenerationWorker.ST_STRATEGY, hierarchy);
		stWorker.taskName = String.format(nameFormat, "ST");
		workers.add(stWorker);
		stWorker.start();

		// initiate data generation for TPC
		DataGenerationWorker tpcWorker = new DataGenerationWorker(dataGen, DataGenerationWorker.TPC_STRATEGY, hierarchy);
		tpcWorker.taskName = String.format(nameFormat, "TPC");
		workers.add(tpcWorker);
		tpcWorker.start();

		// initiate data generation for TPCC
		DataGenerationWorker tpccWorker = new DataGenerationWorker(dataGen, DataGenerationWorker.TPCC_STRATEGY, hierarchy);
		tpccWorker.taskName = String.format(nameFormat, "TPCC");
		workers.add(tpccWorker);
		tpccWorker.start();

	}

	private class WorkerState {
		public static final int FINISHED = 0;
		public static final int RUNNING = 1;

		public int statusCode = 0;
		public String resultMessage = "";
		public String workerName = "";
	}
}

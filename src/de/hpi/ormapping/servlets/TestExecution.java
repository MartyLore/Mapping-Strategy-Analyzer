package de.hpi.ormapping.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;
import de.hpi.ormapping.tests.ResultTemplate;
import de.hpi.ormapping.tests.TestExecutor;

/**
 * Servlet implementation class TestExecution
 */
@WebServlet("/TestExecution")
public class TestExecution extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final int TEST_REPETITIONS = 50;

	Queue<ResultTemplate> resultQueue;

	TestExecutor executor = null;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public TestExecution() {
		super();
		resultQueue = new LinkedBlockingQueue<>();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		ServerResponse res = new ServerResponse();

		while (!resultQueue.isEmpty()) {
			res.results.add(resultQueue.poll());
		}
		
		if(executor.isAlive()) {
			res.status = 1;
		} else {
			res.status = 0;
		}

		String result = mapper.writeValueAsString(res);

		response.setContentType("application/json");
		response.getWriter().append(result);
		response.getWriter().flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		List<String> selectedDbs = mapper.readValue(request.getParameter("dbs"), List.class);
		List<String> selectedTests = mapper.readValue(request.getParameter("tests"), List.class);
		Hierarchy hierarchy = mapper.readValue(request.getParameter("classModel"), Hierarchy.class);

		ExperimentTemplate template = constructTemplate(selectedDbs, selectedTests, hierarchy);

		String result = mapper.writeValueAsString(template);

		response.setContentType("application/json");
		response.getWriter().append(result);
		response.getWriter().flush();

		resultQueue.clear();

		executor = new TestExecutor(resultQueue, selectedTests, selectedDbs, hierarchy, TEST_REPETITIONS);
		executor.start();

	}

	private ExperimentTemplate constructTemplate(List<String> selectedDbs, List<String> selectedTests, Hierarchy hierarchy) {

		ExperimentTemplate template = new ExperimentTemplate();

		template.testCases.addAll(selectedTests);
		template.selectedDbs.addAll(selectedDbs);
		template.strategies.add("ST");
		template.strategies.add("TPC");
		template.strategies.add("TPCC");

		for (ClassNode node : hierarchy.classList) {
			template.classes.add(node.className);
		}

		return template;
	}

	private class ExperimentTemplate {
		public List<String> testCases = new ArrayList<>();
		public List<String> selectedDbs = new ArrayList<>();
		public List<String> classes = new ArrayList<>();
		public List<String> strategies = new ArrayList<>();
	}

	private class ServerResponse {
		public List<ResultTemplate> results = new ArrayList<>();
		public int status = 0;
	}
}

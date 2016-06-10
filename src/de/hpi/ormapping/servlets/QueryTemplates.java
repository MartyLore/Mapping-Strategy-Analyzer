package de.hpi.ormapping.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hpi.ormapping.database.ConnectionFactory;
import de.hpi.ormapping.structures.ClassNode;
import de.hpi.ormapping.structures.Hierarchy;
import de.hpi.ormapping.tests.cases.TestCaseTemplate;

/**
 * Servlet implementation class QueryTemplates
 */
@WebServlet("/QueryTemplates")
public class QueryTemplates extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public QueryTemplates() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		String testCase = request.getParameter("testCase");
		Hierarchy hierarchy = mapper.readValue(request.getParameter("classModel"), Hierarchy.class);

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Fetch Query Templates");

		Map<String, Map<String, Map<String, String>>> templates = new HashMap<>();

		try {
			templates = fetchQueryTemplatesForTestCase(testCase, hierarchy);
			response.getWriter().append(mapper.writeValueAsString(templates));
		} catch (Exception e) {
			response.setStatus(500);
			response.getWriter().append("{error: \"" + e.getMessage() + "\"}");
			e.printStackTrace();
		}

		response.setContentType("application/json");
		response.getWriter().flush();

	}

	private Map<String, Map<String, Map<String, String>>> fetchQueryTemplatesForTestCase(String testCaseName, Hierarchy hierarchy) throws Exception {

		Map<String, Map<String, Map<String, String>>> templates = new HashMap<>();
		List<String> availableDbs = ConnectionFactory.getRegisteredDatabases();

		for (String db : availableDbs) {

			TestCaseTemplate testCase = TestCaseTemplate.getDatabaseSepcificTestCaseImplementation(testCaseName, db, hierarchy);

			Map<String, Map<String, String>> templatesForClass = new HashMap<>();
			for (ClassNode node : hierarchy.classList) {

				Map<String, String> queryTemplates = new HashMap<>();
				queryTemplates.put("ST", testCase.getQueryTemplate(TestCaseTemplate.ST_Strategy, node));
				queryTemplates.put("TPC", testCase.getQueryTemplate(TestCaseTemplate.TPC_Strategy, node));
				queryTemplates.put("TPCC", testCase.getQueryTemplate(TestCaseTemplate.TPCC_Strategy, node));

				templatesForClass.put(node.className, queryTemplates);
			}

			templates.put(db, templatesForClass);
		}

		return templates;
	}

}

package de.hpi.ormapping.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.reflections.Reflections;

import de.hpi.ormapping.tests.cases.TestCaseTemplate;

/**
 * Servlet implementation class TestManager
 */
@WebServlet("/AvailableTests")
public class AvailableTests extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AvailableTests() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		List<TestCaseTemplate> tests = new ArrayList<>();
		String resultMessage = "[]";

		try {
			tests.addAll(fetchAvailableTests());
			resultMessage = generateJSON(tests);
		} catch (Exception e) {
			response.setStatus(500);
			resultMessage = String.format("{message: %1s}", e.getMessage());
		}

		response.setContentType("application/json");
		response.getWriter().append(resultMessage);
		response.getWriter().flush();
	}

	public static List<TestCaseTemplate> fetchAvailableTests() throws Exception {

		List<TestCaseTemplate> tests = new ArrayList<>();

		Reflections reflections = new Reflections("de.hpi.ormapping.tests.cases");
		Set<Class<? extends TestCaseTemplate>> subTypes = reflections.getSubTypesOf(TestCaseTemplate.class);

		for (Class<?> clazz : subTypes) {
			if(clazz.getSuperclass().getSimpleName().equals(TestCaseTemplate.class.getSimpleName())) {
				TestCaseTemplate t = (TestCaseTemplate) Class.forName(clazz.getName()).newInstance();
				tests.add(t);
			}
		}
		
		Collections.sort(tests);
		
		return tests;
	}

	private String generateJSON(List<TestCaseTemplate> tests) {
		
		StringBuilder builder = new StringBuilder("[");
		Iterator<TestCaseTemplate> testIt = tests.iterator();
		while (testIt.hasNext()) {

			TestCaseTemplate test = testIt.next();
			builder.append("{ name: \"").append(test.getName()).append("\", class: \"").append(test.getName()).append("\"}");

			if (testIt.hasNext()) {
				builder.append(",");
			}
		}
		builder.append("]");

		return builder.toString();
	}

}

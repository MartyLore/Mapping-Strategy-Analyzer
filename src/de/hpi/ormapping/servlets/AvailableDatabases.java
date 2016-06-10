package de.hpi.ormapping.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.hpi.ormapping.database.ConnectionFactory;

/**
 * Servlet implementation class DatabaseManager
 */
@WebServlet("/AvailableDatabases")
public class AvailableDatabases extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AvailableDatabases() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		List<String> dbs = fetchDatabaseResources();

		StringBuilder builder = new StringBuilder("[");
		Iterator<String> dbIt = dbs.iterator();
		while (dbIt.hasNext()) {
			builder.append(dbIt.next());
			if (dbIt.hasNext()) {
				builder.append(",");
			}
		}
		builder.append("]");

		response.getWriter().append(builder.toString());
		response.getWriter().flush();
	}

	public static List<String> fetchDatabaseResources() {

		List<String> dbs = null;

		try {
			dbs = ConnectionFactory.getRegisteredDatabases();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dbs;
	}

}

package de.hpi.ormapping.servlets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet implementation class Model
 */
@WebServlet("/Model")
public class Model extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Model() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		response.setContentType("application/text");
		
		if(request.getParameterMap().containsKey("action")) {
			if(request.getParameter("action").equals("load")) {
				try {
					result = loadFile(request.getParameter("fileName"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (request.getParameter("action").equals("delete")) {
				deleteFile(request.getParameter("fileName"));
			}
		} else {
			List<String> storedModels = getAvailableModels();
			result = mapper.writeValueAsString(storedModels);
			response.setContentType("application/json");
		}
		
		response.getWriter().append(result);
		response.getWriter().flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String model = request.getParameter("classModel");
		String modelName = request.getParameter("fileName");
		
		List<String> lines = Arrays.asList(model);
		
		String fileName = modelName;
		if(!fileName.endsWith(".json")) {
			fileName += ".json";
		}
		
		Path file = Paths.get(getServletContext().getRealPath("./models/")+fileName);
		Files.write(file, lines, Charset.forName("UTF-8"));
	}
	
	private List<String> getAvailableModels() {
		
		List<String> jsonFiles = new ArrayList<>();
		
		File folder = new File(getServletContext().getRealPath("./models"));

		for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.getName().endsWith(".json")) {
	        	jsonFiles.add(fileEntry.getName());
	        }
	    }
		
		return jsonFiles;
	}

	private String loadFile(String fileName) throws Exception {
		String json ="";
		
		byte[] encoded = Files.readAllBytes(Paths.get(getServletContext().getRealPath("./models/")+fileName));
		json = new String(encoded, "UTF-8");
		
		return json;
	}
	
	private void deleteFile(String fileName) {
		(new File(fileName)).delete();
	}
}

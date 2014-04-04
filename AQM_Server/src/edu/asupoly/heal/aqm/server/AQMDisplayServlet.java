package edu.asupoly.heal.aqm.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import edu.asupoly.heal.aqm.dmp.AQMDAOFactory;
import edu.asupoly.heal.aqm.dmp.IAQMDAO;

@SuppressWarnings("serial")
public class AQMDisplayServlet extends HttpServlet {
	public final void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = null;
		IAQMDAO dao = AQMDAOFactory.getDAO();
		try {
			response.setContentType("text/plain");
			out = response.getWriter();
			
			JSONArray Dylosrd = dao.findDylosReadingsByGroup(null, 10, true);
			JSONArray Sensordronerd = dao.findSensordroneReadingsByGroup(null, 10, true);
			Dylosrd.addAll(Sensordronerd);
			
			String search_callback = getJSONPObject("search_callback", Dylosrd);
			out.print(search_callback);
			
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
        }
	}

	private String getJSONPObject(String callback, JSONArray rd) throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("type", "FeatureCollection");
		obj.put("features", rd);
		
		return callback + "(" + obj.toString() + ");";
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
	}
	
	
}

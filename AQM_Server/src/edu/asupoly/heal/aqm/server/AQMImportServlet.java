/**
 * 
 */
package edu.asupoly.heal.aqm.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import edu.asupoly.heal.aqm.dmp.AQMDAOFactory;
import edu.asupoly.heal.aqm.dmp.IAQMDAO;
import edu.asupoly.heal.aqm.model.ServerPushEvent;

@SuppressWarnings("serial")
public class AQMImportServlet extends HttpServlet {
	private static Date lastImportTime = new Date();
	public static final int AIR_QUALITY_READINGS_TYPE = 1;
	public static final int SENSORDRONE_READINGS_TYPE = 0;
	private static final String[] __TYPES = { "Sensordrone",
			"AirQualityReadings" };

	public final void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * Handle upload of JSON objects
	 * 
	 * @param request
	 *            HTTP Request object
	 * @param response
	 *            HTTP Response object
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ServletInputStream sis = null;
		int appReturnValue = ServerPushEvent.PUSH_UNSET;
		String jsonString = "";
		lastImportTime = new Date();

		try {
			String objectType = (String) request.getParameter("type");
			System.out
					.println("Server received push request for " + objectType);

			sis = request.getInputStream();
			if (sis != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						sis));
				if (br != null)
					jsonString = br.readLine(); // get received JSON data from
												// request
				System.out.println(jsonString);
				if (objectType != null && jsonString != null) {
					IAQMDAO dao = AQMDAOFactory.getDAO();
					if (objectType.startsWith("dylos")) {
						JSONArray jsonary = new JSONArray();
						JSONParser parser = new JSONParser();
						jsonary = (JSONArray) parser.parse(jsonString);

						appReturnValue = (dao.importDylosReading(jsonString) ? jsonary
								.size()	: ServerPushEvent.SERVER_DYLOS_IMPORT_FAILED);
						System.out.println("Server imported Dylos Readings: "
								+ appReturnValue);
						__recordResult(dao, lastImportTime, appReturnValue, AIR_QUALITY_READINGS_TYPE, "DylosReadings");
					} else if (objectType.startsWith("sensordrone")) {
						appReturnValue = (dao.importSensordroneReading(jsonString) ? 1
								: ServerPushEvent.SERVER_SENSORDRONE_IMPORT_FAILED);
						System.out.println("Server imported Sensordrone Readings: " + appReturnValue);
						__recordResult(dao, lastImportTime, appReturnValue, SENSORDRONE_READINGS_TYPE, "SensordroneReadings");
					}
				} else
					appReturnValue = ServerPushEvent.SERVER_BAD_OBJECT_TYPE;
			} else
				appReturnValue = ServerPushEvent.SERVER_STREAM_ERROR;
		} catch (StreamCorruptedException sce) {
			sce.printStackTrace();
			appReturnValue = ServerPushEvent.SERVER_STREAM_CORRUPTED_EXCEPTION;
		} catch (IOException ie) {
			ie.printStackTrace();
			appReturnValue = ServerPushEvent.SERVER_IO_EXCEPTION;
		} catch (SecurityException se) {
			se.printStackTrace();
			appReturnValue = ServerPushEvent.SERVER_SECURITY_EXCEPTION;
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			appReturnValue = ServerPushEvent.SERVER_NULL_POINTER_EXCEPTION;
		} catch (Throwable t) {
			t.printStackTrace();
			appReturnValue = ServerPushEvent.SERVER_UNKNOWN_ERROR;
		}

		PrintWriter pw = null;
		try {
			System.out.println("Server returning value: " + appReturnValue);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			pw = response.getWriter();
			pw.println("" + appReturnValue);
		} catch (Throwable t3) {
			System.out.println("Server pushed stacktrace on response: "
					+ t3.getMessage());
			t3.printStackTrace();
		} finally {
			try {
				if (pw != null) {
					pw.close();
				}
				if (sis != null)
					sis.close();
			} catch (Throwable t2) {
				t2.printStackTrace();
			}
		}
	}
	
	private void __recordResult(IAQMDAO dao, Date d, int rval, int type, String label) {
        String msg = "";
        if (rval >= 0) {
            msg = "Pushed " + rval + " " + label + " to the server";            
        } else {
            msg = "Unable to push " + label + " to the server";
        }
        System.out.println(msg);

        try {
            dao.addPushEvent(new ServerPushEvent(d.toString(), rval, type, msg));
        } catch (Throwable ts) {
        	ts.printStackTrace();
        	System.out.println("Unable to record " + label + " push event");
        }
    }
}

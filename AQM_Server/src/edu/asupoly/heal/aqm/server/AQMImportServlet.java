/**
 * 
 */
package edu.asupoly.heal.aqm.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import edu.asupoly.heal.aqm.dmp.AQMDAOFactory;
import edu.asupoly.heal.aqm.dmp.IAQMDAO;
import edu.asupoly.heal.aqm.model.ServerPushEvent;

@SuppressWarnings("serial")
public class AQMImportServlet extends HttpServlet {
//	private static Date lastImportTime = new Date();
//	public static final int DYLOS_READINGS_TYPE = 1;
//	public static final int SENSORDRONE_READINGS_TYPE = 0;
	//private static final String[] __TYPES = { "Sensordrone", "Dylos" };

	public final void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = null;
		IAQMDAO dao = AQMDAOFactory.getDAO();
		try {
			response.setContentType("text/plain");
			out = response.getWriter();
			Map<String, String[]> requestParams = request.getParameterMap();
			Set set = requestParams.entrySet();
			Iterator it = set.iterator();
			if (! it.hasNext()) {
				out.println("Dylos Reading: ");
				dao.findDeviceIdinDylos(out);
				out.println("\n\nSensordrone Reading:");
				dao.findDeviceIdinSensordrone(out);
			} else {
				String deviceid = null;
				int tail = Integer.MAX_VALUE;
				String type = null;
				
				 while (it.hasNext()) {
					 Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>) it.next();
					 String paramName = entry.getKey();
					 if (!paramName.isEmpty())
						 System.out.println("paramName = " + paramName);
					 String[] paramValues = entry.getValue();
					 
					 if (paramValues.length == 1) {
						 String paramValue = paramValues[0];
						 System.out.println("     , paramValue = " + paramValue);
						 if (paramName.equals("Dylos") && !paramValue.isEmpty()) {
							 type = "Dylos";
							 tail = Integer.parseInt(paramValue);
						 }
						 if (paramName.equals("Sensordrone") && !paramValue.isEmpty()) {
							 type = "Sensordrone";
							 tail = Integer.parseInt(paramValue);
						 }
						 if (paramName.equals("deviceid") && !paramValue.isEmpty()) {
							 deviceid = paramValue;
						 }
					 }
				 }
				 if (type != null) {
					 out.println(type + " Readings:\n ");
					 JSONArray rd = new JSONArray();
					 if (type.equals("Dylos")) rd = dao.findDylosReadingsByGroup(deviceid, tail);
					 else if (type.equals("Sensordrone")) rd = dao.findSensordroneReadingsByGroup(deviceid, tail);
					 printString(rd, out);
				 } else if (type == null && deviceid != null) {
					 out.println("request sample:\n ?Dylos=100&deviceid=aqm0\n ?Sensordrone=20&deviceid=SensorDrone00:00:00:00:00:00"
					 		+ "\n ?Dylos=50");
				 }
			}

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
	
	private void printString(JSONArray rd, PrintWriter out) throws Exception {
		StringWriter json = new StringWriter();
		 rd.writeJSONString(json);
		 //out.println(json.toString() + "\n");
		 for (int i = 0; i < rd.size(); i++) {
			 rd = (JSONArray)JSONValue.parse(json.toString());
			 JSONObject jval= (JSONObject) rd.get(i);
			 out.println((i+1) + ". "+ jval.toJSONString() + "\n");
		 }
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ServletInputStream sis = null;
		int appReturnValue = ServerPushEvent.PUSH_UNSET;
		String jsonString = "";
		//lastImportTime = new Date();

		try {
			sis = request.getInputStream();
			if (sis != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(sis));
				if (br != null)
					jsonString = br.readLine(); // get received JSON data from request
				System.out.println("Received data: "+ jsonString);
				if (jsonString != null) {
					IAQMDAO dao = AQMDAOFactory.getDAO();
					appReturnValue = (dao.importReadings(jsonString)) ? 1 : ServerPushEvent.SERVER_IMPORT_FAILED;
					
					
//						appReturnValue = (dao.importDylosReading(jsonString) ? jsonary
//								.size()	: ServerPushEvent.SERVER_DYLOS_IMPORT_FAILED);
//						System.out.println("Server imported Dylos Readings: "
//								+ appReturnValue);
						//__recordResult(dao, lastImportTime, appReturnValue, AIR_QUALITY_READINGS_TYPE, "DylosReadings");
					
//						appReturnValue = (dao.importSensordroneReading(jsonString) ? 1
//								: ServerPushEvent.SERVER_SENSORDRONE_IMPORT_FAILED);
//						System.out.println("Server imported Sensordrone Readings: " + appReturnValue);
						//__recordResult(dao, lastImportTime, appReturnValue, SENSORDRONE_READINGS_TYPE, "SensordroneReadings");
				} 
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
	
/*	private void __recordResult(IAQMDAO dao, Date d, int rval, int type, String label) {
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
    }*/
}

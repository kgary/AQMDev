package edu.asupoly.aspira.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import edu.asupoly.aspira.dmp.AspiraDAO;
import edu.asupoly.aspira.dmp.IAspiraDAO;
import edu.asupoly.aspira.model.AirQualityReadings;
import edu.asupoly.aspira.model.ParticleReading;
import edu.asupoly.aspira.model.ServerPushEvent;
import edu.asupoly.aspira.monitorservice.ServerPushTask;

@SuppressWarnings("serial")
public class AspiraImportServlet extends HttpServlet {
    private static Date lastImportTime = new Date();

    public static final int AIR_QUALITY_READINGS_TYPE = 1;
    //private static final String[] __TYPES = { "SpirometerReadings", "AirQualityReadings", "UIEvents" };
    private static final String[] __TYPES = {"Sensordrone",  "AirQualityReadings"};
    
    
    /**
     * doGet returns the time of the last successful import for patient patientid
     */
    public final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	JSONObject getjson = new JSONObject();
    	String s = request.getRequestURI();
    	System.out.println("s = "+s);
    	
    	String objectType = request.getPathInfo();
    	System.out.println(" objectType  = " + objectType);

 	
        PrintWriter out = null;
        try {
            //response.setContentType("text/plain");
        	response.setContentType("text/html;charset=utf-8");
        	
            out = response.getWriter();
            
/*            //****************test
        	JSONObject jsonObj=new JSONObject();
        	JSONArray jsonArr = new JSONArray();
        	JSONObject jsonObj2 = new JSONObject();
        	jsonObj2.put("largeparticle", 23);
        	jsonObj2.put("patientid", "patient8");
        	jsonObj2.put("readingtime", "Mon Feb 17 19:38:53 MST 2014");
        	jsonObj2.put("smallparticle", 45);
        	jsonObj2.put("deviceid", "aqm7");
        	jsonArr.add(jsonObj2);
        	jsonObj.put("info", jsonArr);
        	System.out.println("A test json in doget(): "+jsonObj);
        	//out.print(jsonObj);
            //***************************************
*/            
            
            Map<String, String[]> requestParams = request.getParameterMap();
            IAspiraDAO dao = AspiraDAO.getDAO();            
            //for (int i = 0; i < ALL_TYPES; i++) {
                //printByType(requestParams, dao, 1, out);//i
                getjson = printByTypeJson(requestParams, dao, 1, out);
                out.print(getjson);
            //}        
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Throwable t2) {
                System.out.println("\nCould not flush and close output stream on doGet");
            }
        }
    }


    private JSONObject printByTypeJson(Map<String, String[]> requestParams, IAspiraDAO dao, int type, PrintWriter out) {
		JSONObject spe = null;
		JSONObject spe2 = null;
        try {
            spe = dao.getLastServerPushJson(type);
        } catch (Exception dme) {
        	dme.printStackTrace();
        	System.out.println("\nCould not retrieve last server push for type " + __TYPES[type]);
        }		

        if (spe == null) {
            out.println("\nNo server push records for type " + __TYPES[type]);
          } else {
        	//************ 
            //out.println("\nLast server push for type " + __TYPES[type] + " (json) :\n" + spe.toString());
        	//**************
            String[] tail = requestParams.get(__TYPES[type]);
            if (tail != null && tail.length > 0) {
          	  System.out.println("Requesting " + tail[0] + " records for type " + __TYPES[type]);
                switch (type) {
                case AIR_QUALITY_READINGS_TYPE : 
                	spe2 = printAirQualityReadingsJson(dao, out, tail[0]);
                    break;
                default:
                    out.println("\nUnknown event type passed to print: " + type);
                    break;
                }
            }
          }
		return spe2;

	}



	private void printByType(Map<String, String[]> requestParams, IAspiraDAO dao, int type, PrintWriter out) {
        ServerPushEvent spe = null;
        try {
            spe = dao.getLastServerPush(type);
        } catch (Exception dme) {
        	dme.printStackTrace();
        	System.out.println("\nCould not retrieve last server push for type " + __TYPES[type]);
        }
        
        if (spe == null) {
          out.println("\nNo server push records for type " + __TYPES[type]);
        } else {
          out.println("\nLast server push for type " + __TYPES[type] + ":\n" + spe.toString());
          String[] tail = requestParams.get(__TYPES[type]);
          if (tail != null && tail.length > 0) {
        	  System.out.println("Requesting " + tail[0] + " records for type " + __TYPES[type]);
              switch (type) {
              case AIR_QUALITY_READINGS_TYPE : 
                  printAirQualityReadings(dao, out, tail[0]);
                  break;
              default:
                  out.println("\nUnknown event type passed to print: " + type);
                  break;
              }
          }
        }
    }



	private JSONObject printAirQualityReadingsJson(IAspiraDAO dao, PrintWriter out, String tail) {
		JSONObject sprs = null;
		try {
            
            int tailNum = Integer.parseInt(tail);
            if (tailNum > 0) {
                sprs = dao.findAirQualityReadingsForPatientTailJson(null, tailNum);
            } else {
                sprs = dao.findAirQualityReadingsForPatientJson(null);
            }
            if (sprs == null) {
                out.println("No Air Quality Readings available");
            } else {
                	//out.println(sprs.toString());
            		System.out.println("sprs(json) =  " + sprs.toString());
                	//out.print(sprs);//****************************
                	
                	//return out;
            }
        } catch (Throwable ts) {
        	ts.printStackTrace();
            out.println("\nUnable to retrieve air quality readings\n");
        }
		return sprs;

		
	}
	
    private void printAirQualityReadings(IAspiraDAO dao, PrintWriter out, String tail) {
        try {
            AirQualityReadings sprs = null;
            int tailNum = Integer.parseInt(tail);
            if (tailNum > 0) {
                sprs = dao.findAirQualityReadingsForPatientTail(null, tailNum);
            } else {
                sprs = dao.findAirQualityReadingsForPatient(null);
            }
            if (sprs == null) {
                out.println("No Air Quality Readings available");
            } else {
                Iterator<ParticleReading> iter = sprs.iterator();
                if (iter == null) {
                    out.println("No Air Quality Readings are available");
                } else {
                    while (iter.hasNext()) {
                        out.println(iter.next().toString());
                    }
                }
            }
        } catch (Throwable ts) {
        	ts.printStackTrace();
            out.println("\nUnable to retrieve air quality readings\n");
        }
    }
    
    /**
     * Handle upload of serialized objects
     *
     * @param request HTTP Request object
     * @param response HTTP Response object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectInputStream  ois = null;
        ServletInputStream sis = null;        
        int appReturnValue = ServerPushTask.PUSH_UNSET;
        String jsonString = "";
        JSONObject json = new JSONObject();
        
        lastImportTime = new Date();
        try {
            //String objectType = request.getPathInfo();
            String objectType = (String)request.getParameter("type");
                       
            System.out.println("Server received push request for " + objectType);
            
            if (objectType != null && objectType.length() > 0) {
                //if (objectType.startsWith("/")) objectType = objectType.substring(1);
            	sis = request.getInputStream();
            	IAspiraDAO dao = AspiraDAO.getDAO();
            	
            	if (objectType.startsWith("jairqualityreadings")) {
            		BufferedReader br = new BufferedReader(new InputStreamReader(sis));
            		if (br != null) jsonString = br.readLine(); //get received JSON data from request
            		//if (br != null) jsonString = readAll(br);
            		//jsonString = "{\"largeparticle\":[19,19],\"patientid\":[\"patient3\",\"patient3\"],\"readingtime\":[\"Tue Feb 25 12:03:10 MST 2014\",\"Tue Feb 25 12:03:20 MST 2014\"],\"smallparticle\":[86,86],\"deviceid\":[\"aqm3\",\"aqm3\"]}";
            		
            		JSONParser parser = new JSONParser();
					json = (JSONObject)parser.parse(jsonString);
            		System.out.println(json.toString());
            		JSONArray jarray = new JSONArray();
            		jarray = (JSONArray)json.get("deviceid");
                	if (!jarray.isEmpty() && jarray.size() > 0) {
                		appReturnValue = (dao.importAirQualityReadingsJson(json, false) ? jarray.size() : ServerPushTask.SERVER_AQ_IMPORT_JSON_FAILED);
                		System.out.println("Server imported AQ Readings (json): " + appReturnValue);
                		//System.out.println(json.toString());
                		
                	} else {
                		appReturnValue = ServerPushTask.SERVER_NO_AQ_JSON_READINGS;
                	}
                	__recordResult(dao, appReturnValue, "jairqualityreadings", lastImportTime, AIR_QUALITY_READINGS_TYPE);            		

            	}
            } else appReturnValue = ServerPushTask.SERVER_BAD_OBJECT_TYPE;
        } catch (StreamCorruptedException sce) {
            sce.printStackTrace();
            appReturnValue = ServerPushTask.SERVER_STREAM_CORRUPTED_EXCEPTION;
        } catch (IOException ie) {
            ie.printStackTrace();
            appReturnValue = ServerPushTask.SERVER_IO_EXCEPTION;
        } catch (SecurityException se) {
            se.printStackTrace();
            appReturnValue = ServerPushTask.SERVER_SECURITY_EXCEPTION;
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            appReturnValue = ServerPushTask.SERVER_NULL_POINTER_EXCEPTION;
        } catch (Throwable t) {
            t.printStackTrace();
            appReturnValue = ServerPushTask.SERVER_UNKNOWN_ERROR;
        } 
        
        
        PrintWriter pw = null;
        try {
        	System.out.println("Server returning value: " + appReturnValue);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");
            pw = response.getWriter();
            pw.println(""+appReturnValue);//return to __pushToServerJson()
        } catch (Throwable t3) {
        	System.out.println("Server pushed stacktrace on response: " + t3.getMessage());
            t3.printStackTrace();
        } finally {        
            try {
                if (pw != null) {
                    pw.close();            
                }
                if (sis != null) sis.close();              
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
        }
    }
    
/*    private String readAll(Reader br) throws IOException {
    	StringBuilder sb = new StringBuilder();
    	int cp;
    	while ((cp = br.read()) != -1) {
    		sb.append((char) cp);
    		
    	}
		return sb.toString();
	}*/


/*	public void doPost2(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the input stream and read the object off of it
        ObjectInputStream  ois = null;
        ServletInputStream sis = null;        
        int appReturnValue = ServerPushTask.PUSH_UNSET;
        lastImportTime = new Date();
        try {
            //String objectType = request.getPathInfo();//*****************************test
            String objectType = (String)request.getParameter("type");
            
            
            System.out.println("Server received push request for " + objectType);
            if (objectType != null && objectType.length() > 0) {
                //if (objectType.startsWith("/")) objectType = objectType.substring(1);
               
                sis = request.getInputStream();
                if (sis != null) {
                    ois = new ObjectInputStream(sis);                    
                    IAspiraDAO dao = AspiraDAO.getDAO();
                    if (objectType.startsWith("airqualityreadings")) {                        
                        AirQualityReadings aqrs = (AirQualityReadings)ois.readObject();                        
                        if (aqrs != null && aqrs.size() > 0) {
                            appReturnValue = (dao.importAirQualityReadings(aqrs, false) ? aqrs.size() : ServerPushTask.SERVER_AQ_IMPORT_FAILED);                            
                            System.out.println("Server imported AQ Readings: " + appReturnValue);
                        } else {
                            appReturnValue = ServerPushTask.SERVER_NO_AQ_READINGS;
                        }
                        __recordResult(dao, appReturnValue, "airqualityreadings", lastImportTime, AIR_QUALITY_READINGS_TYPE);
                    } 
                    //*******************************json
                    else if (objectType.startsWith("jairqualityreadings")) {
                    	JSONObject jaqrs = (JSONObject)ois.readObject();
                    	JSONArray jarray = new JSONArray();
                    	jarray = (JSONArray)jaqrs.get("deviceid");
                    	if (!jarray.isEmpty() && jarray.size() > 0) {
                    		appReturnValue = (dao.importAirQualityReadingsJson(jaqrs, false) ? jarray.size() : ServerPushTask.SERVER_AQ_IMPORT_JSON_FAILED);
                    		System.out.println("Server imported AQ Readings (json): " + appReturnValue);
                    		System.out.println(jaqrs.toString());
                    		
                    	} else {
                    		appReturnValue = ServerPushTask.SERVER_NO_AQ_JSON_READINGS;
                    	}
                    	__recordResult(dao, appReturnValue, "jairqualityreadings", lastImportTime, AIR_QUALITY_READINGS_TYPE);

                    }

                                    
                    //***********************************
                } else appReturnValue = ServerPushTask.SERVER_STREAM_ERROR;
            } else appReturnValue = ServerPushTask.SERVER_BAD_OBJECT_TYPE;
        } catch (StreamCorruptedException sce) {
            sce.printStackTrace();
            appReturnValue = ServerPushTask.SERVER_STREAM_CORRUPTED_EXCEPTION;
        } catch (IOException ie) {
            ie.printStackTrace();
            appReturnValue = ServerPushTask.SERVER_IO_EXCEPTION;
        } catch (SecurityException se) {
            se.printStackTrace();
            appReturnValue = ServerPushTask.SERVER_SECURITY_EXCEPTION;
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            appReturnValue = ServerPushTask.SERVER_NULL_POINTER_EXCEPTION;
        } catch (Throwable t) {
            t.printStackTrace();
            appReturnValue = ServerPushTask.SERVER_UNKNOWN_ERROR;
        } 
        
        
        PrintWriter pw = null;
        try {
        	System.out.println("Server returning value: " + appReturnValue);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");
            pw = response.getWriter();
            pw.println(""+appReturnValue);//return to __pushToServerJson()
        } catch (Throwable t3) {
        	System.out.println("Server pushed stacktrace on response: " + t3.getMessage());
            t3.printStackTrace();
        } finally {        
            try {
                if (pw != null) {
                    pw.close();            
                }
                if (sis != null) sis.close();              
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
        }
    }*/



	private void __recordResult(IAspiraDAO dao, int rval, String label, Date d, int type) {
        String msg = "";
        if (rval >= 0) {
            msg = "Pushed " + rval + " " + label + " to the server";            
        } else {
            msg = "Unable to push " + label + " to the server";
        }
        System.out.println(msg);

        try {
            dao.addPushEvent(new ServerPushEvent(d, rval, type, msg));
        } catch (Throwable ts) {
        	ts.printStackTrace();
        	System.out.println("Unable to record " + label + " push event");
        }
    }
    

}

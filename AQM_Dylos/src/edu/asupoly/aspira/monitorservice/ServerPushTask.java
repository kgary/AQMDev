package edu.asupoly.aspira.monitorservice;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import org.json.simple.JSONObject;

import edu.asupoly.aspira.AspiraSettings;
import edu.asupoly.aspira.dmp.AspiraDAO;
import edu.asupoly.aspira.dmp.IAspiraDAO;
import edu.asupoly.aspira.model.AirQualityReadings;
import edu.asupoly.aspira.model.ServerPushEvent;

public class ServerPushTask extends AspiraTimerTask {
    public static final int PUSH_UNSET = 0;    
    public static final int PUSH_BAD_RESPONSE_CODE = -101;
    public static final int PUSH_MALFORMED_URL = -102;
    public static final int PUSH_UNABLE_TO_CONNECT = -100;
    public static final int SERVER_AQ_IMPORT_FAILED = -20;
    public static final int SERVER_NO_AQ_READINGS = -21;
    public static final int SERVER_SPIROMETER_IMPORT_FAILED = -30;
    public static final int SERVER_NO_SPIROMETER_READINGS = -31;
    public static final int SERVER_UIEVENT_IMPORT_FAILED = -40;
    public static final int SERVER_NO_UIEVENTS = -41;
    public static final int SERVER_STREAM_ERROR = -1;
    public static final int SERVER_BAD_OBJECT_TYPE = -2;
    public static final int SERVER_STREAM_CORRUPTED_EXCEPTION = -10;
    public static final int SERVER_IO_EXCEPTION = -11;
    public static final int SERVER_SECURITY_EXCEPTION = -12;
    public static final int SERVER_NULL_POINTER_EXCEPTION = -13;
    public static final int SERVER_UNKNOWN_ERROR = -99;
    
    public static final int SERVER_AQ_IMPORT_JSON_FAILED = -25;
    public static final int SERVER_NO_AQ_JSON_READINGS = -26;
    
    public static final int AIR_QUALITY_READINGS_TYPE = 1;
    
    private Properties __props;
    private String __pushURL;
    
    protected Date[] _lastRead;  // override of parent
    
    public ServerPushTask() {
        super();
    }   
    
    /**
     * verifies URL form
     */
/*     private String __setURL(String url) {
        // figure the shortest possible valid URL is http://X.YYY
        String pushURL = null;
        if (url != null && url.trim().length() > 12) {
            pushURL = url;
            if (!pushURL.endsWith("/")) {  
                pushURL = url + "/";
            }
        }
        return pushURL;
    } */
    
	@Override
	public boolean init(Properties p) {
        boolean rval = true;
        
        __props = new Properties();  // need this even if not using here
        String patientId = AspiraSettings.getPatientId();
        //__pushURL = __setURL(p.getProperty("push.url"));
		__pushURL = p.getProperty("push.url");
		
        if (patientId != null && __pushURL != null || __pushURL.length() >= 12) { // must be at least http://x.yyy
            __props.setProperty("patientid", patientId);
        } else {
            rval = false;
        }
        _isInitialized = rval;
        
        // This section tries to initialize the last reading date
        Date lastRead = new Date(0L);  // Jan 1 1970, 00:00:00
        _lastRead = new Date[3];
        try {
            IAspiraDAO dao = AspiraDAO.getDAO();
            ServerPushEvent spe = dao.getLastServerPush(AIR_QUALITY_READINGS_TYPE);
            if (spe != null) {                
                _lastRead[AIR_QUALITY_READINGS_TYPE] = spe.getEventDate();
                System.out.println("Last server push " + _lastRead[AIR_QUALITY_READINGS_TYPE].toString());
            } else {
                _lastRead[AIR_QUALITY_READINGS_TYPE] = lastRead;
                System.out.println("Last server push unknown, using " + lastRead.toString());
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("Unable to get last server push time, using " + lastRead.toString());
        }
        
        return _isInitialized;
	}
	
	@Override
	public void run() {
        if (_isInitialized) {
            System.out.println("MonitoringService: ServerPushTask executing");
            Date d = new Date(System.currentTimeMillis());

            try {
                IAspiraDAO dao = AspiraDAO.getDAO();
                System.out.println("Checking for air quality readings between " + 
                		_lastRead[AIR_QUALITY_READINGS_TYPE].toString() + " and " + d.toString());
                AirQualityReadings atoImport = dao.findAirQualityReadingsForPatient(__props.getProperty("patientid"),
                        _lastRead[AIR_QUALITY_READINGS_TYPE], d);

                
                int rval = 0;
                if (__pushURL != null) {
                    if (atoImport != null && atoImport.size() > 0) {                                
                        //rval = __pushToServer(atoImport, "airqualityreadings");
                        
                        //***********convert serializable object to json
                        JSONObject ajsontoImport = atoImport.toJson(); 
                        rval = __pushToServerJson(ajsontoImport, "jairqualityreadings");
                        //****************************
                        
                        __recordResult(dao, rval, "air quality readings", d, AIR_QUALITY_READINGS_TYPE);
                    } else {
                    	System.out.println(" No Air Quality Readings to push");
                    }
                }
            } catch (Throwable t) {
            	System.out.println("Error pushing to the server " + t.getMessage());
            }
        }
		
	}

	private void __recordResult(IAspiraDAO dao, int rval, String label, Date d, int type) {
        String msg = "";
        if (rval >= 0) {
            msg = "Pushed " + rval + " " + label + " to the server";            
        } else {
            msg = "Unable to push " + label + " to the server";
        }
        System.out.println(msg);

        _lastRead[type] = d;    // whether we are successful or not we update the date.
                                // otherwise this could happen over and over (say, UNIQUE constraint)
        try {
            dao.addPushEvent(new ServerPushEvent(d, rval, type, msg));
        } catch (Throwable ts) {
        	ts.printStackTrace();
            System.out.println("Unable to record " + label + " push event");
        }
	}
	
	private int __pushToServerJson(JSONObject jobjects, String type) throws Exception {
        HttpURLConnection urlConn = null;
        DataOutputStream output = null;
        BufferedReader br = null;
		
		String url = __pushURL+"?type="+type;
		//String url = "http://localhost:8081/AQMServer/AspiraImportServlet?type=jairqualityreadings";// for TCP/TP Monitor test
		
        int rval = 0;
        try {
            System.out.println("Pushing (json) to server" + url);
            urlConn = (HttpURLConnection) new URL(url).openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestMethod("POST");
			urlConn.setRequestProperty("Content-Type", "application/json;charset=utf-8");//tell the server to expect a JSON Object
            urlConn.connect();
			
            output = new DataOutputStream(urlConn.getOutputStream());
            
            output.writeBytes(jobjects.toString());
            output.flush();
            output.close();
            System.out.println("Push complete " + url);
            
            // Process the response
            if (urlConn.getResponseCode() != 200) {
                throw new Exception("Did not receive OK from server for request");
            } else {
                // Get the return value, the response of doPost()
                br = new BufferedReader(new InputStreamReader(new DataInputStream (urlConn.getInputStream())));
                String str = br.readLine();
                try {
                    rval = Integer.parseInt(str);
                } catch (NumberFormatException nfe) {
                	nfe.printStackTrace();
                	System.out.println("Unable to convert server response to return code");
                    rval = PUSH_BAD_RESPONSE_CODE;
                }
            }
        } catch (MalformedURLException mue) {
            System.out.println("Malformed URL " + url);
            mue.printStackTrace();
            rval = PUSH_MALFORMED_URL;
        } catch (Throwable t) {
            System.out.println("Error trying to connect to push server");
            t.printStackTrace();
            rval = PUSH_UNABLE_TO_CONNECT;
        } finally {
            try {
                if (br != null) br.close();
                if (output != null) output.close();
            } catch (Throwable t2) {
            	t2.printStackTrace();
                System.out.println("Unable to close Object Output Stream");
            }
        }
        __logReturnValue(rval);
        return rval;   
	}



	private int __pushToServer(java.io.Serializable objects, String type) throws Exception {
        HttpURLConnection urlConn = null;
        ObjectOutputStream oos = null;
        BufferedReader br = null;
        int rval = 0;
        try {
            System.out.println("Pushing to server " + __pushURL+type);
            urlConn = (HttpURLConnection) new URL(__pushURL+type).openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestMethod("POST");
            urlConn.connect();
            oos = new ObjectOutputStream(urlConn.getOutputStream());
            
            oos.writeObject(objects);
            oos.flush();
            oos.close();
            System.out.println("Push complete " + __pushURL+type);
            
            // Process the response
            if (urlConn.getResponseCode() != 200) {
                throw new Exception("Did not receive OK from server for request");
            } else {
                // Get the return value
                br = new BufferedReader(new InputStreamReader(new DataInputStream (urlConn.getInputStream())));
                String str = br.readLine();
                try {
                    rval = Integer.parseInt(str);
                } catch (NumberFormatException nfe) {
                	nfe.printStackTrace();
                	System.out.println("Unable to convert server response to return code");
                    rval = PUSH_BAD_RESPONSE_CODE;
                }
            }
        } catch (MalformedURLException mue) {
            System.out.println("Malformed URL " + __pushURL+type);
            mue.printStackTrace();
            rval = PUSH_MALFORMED_URL;
        } catch (Throwable t) {
            System.out.println("Error trying to connect to push server");
            t.printStackTrace();
            rval = PUSH_UNABLE_TO_CONNECT;
        } finally {
            try {
                if (br != null) br.close();
                if (oos != null) oos.close();
            } catch (Throwable t2) {
            	t2.printStackTrace();
                System.out.println("Unable to close Object Output Stream");
            }
        }
        __logReturnValue(rval);
        return rval;    
	}

	private void __logReturnValue(int rval) {
        // This is a total hack right now
		System.out.println("Return code from server push: " + rval);
        if (rval > 0) System.out.println("This is the number of elements pushed successfully");
        else if (rval == 0) System.out.println("Server did not think there was anything to push");
        else if (rval <= -100) System.out.println("Some error on the client prevented server push round trip");
        else if (rval <= -90) System.out.println("Server side servlet error");
        else if (rval <= -40) System.out.println("Could not push UI Events");
        else if (rval <= -30) System.out.println("Could not push Spirometer Readings");
        else if (rval <= -20) System.out.println("Could not push Air Quality Readings");
        else if (rval <= -10) System.out.println("Server encountered an exception");
        else if (rval < 0) System.out.println("Server encountered parameters it did not understand");
	}
}

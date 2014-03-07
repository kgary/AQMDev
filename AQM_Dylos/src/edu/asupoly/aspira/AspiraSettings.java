package edu.asupoly.aspira;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

public final class AspiraSettings {
	private static final String PROPERTY_FILENAME = "properties/aspira.properties";
	public static final Calendar ASPIRA_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	
	private String ASPIRA_HOME = null;
	private Properties __globalProperties;
	
	private static AspiraSettings __appSettings = null;
	
	private AspiraSettings() throws Exception {
		InputStreamReader isr = null;
		try{
			isr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILENAME));
			__globalProperties = new Properties();
			__globalProperties.load(isr);
			
			// check for environment variables ASPIRA_HOME and ASPIRA DATA
			ASPIRA_HOME = System.getenv("ASPIRA_HOME");
			if(ASPIRA_HOME == null) {
				ASPIRA_HOME = __globalProperties.getProperty("aspira.home");
				if (ASPIRA_HOME == null || ASPIRA_HOME.isEmpty()) {
					ASPIRA_HOME = "D:/projects/AQM_server/";
				}
			}
			if (!ASPIRA_HOME.endsWith(File.separator)) {
				ASPIRA_HOME = ASPIRA_HOME + File.separator;
			}
			// We have a value for ASPIRA_HOME but is it valid and can I write to it?
			File f = new File(ASPIRA_HOME);
			if (!f.canWrite()) {
				 System.out.println("No write access to ASPIRA_HOME: " + ASPIRA_HOME);
				 throw new Exception("No write access to ASPIRA_HOME: " + ASPIRA_HOME);
			}
			//Tell the User
			System.out.println("Aspira system starting with values:");
			System.out.println("\tASPIRA_HOME =\t" + ASPIRA_HOME);		
		} catch (Throwable t1) {
			System.out.println("Unable to initialize Aspira Monitoring Service, exiting");
			t1.printStackTrace();
			System.exit(0);
		} finally {
			try{
				isr.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	
	public static AspiraSettings getAspiraSettings() {
		if(__appSettings == null) {
			try {
				__appSettings = new AspiraSettings();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return __appSettings;
	}

	public static String getAirQualityMonitorId() {
		return getAspiraProperty("aqmonitor.id");
	}

	private static String getAspiraProperty(String key) {
		return AspiraSettings.getAspiraSettings().getGlobalProperty(key);
	}

	private String getGlobalProperty(String key) {
		return __globalProperties.getProperty(key);
	}

	public static String getPatientId() {
		return getAspiraProperty("patient.id");
	}
	
    public static String getAspiraHome() {
        return AspiraSettings.getAspiraSettings().getHome();
    }

	private String getHome() {
        return ASPIRA_HOME;
	}
	
	public static String getGeoLatitude() {
		return getAspiraProperty("geocoordinates.latitude");
	}	
	
	public static String getGeoLongitude() {
		return getAspiraProperty("geocoordinates.longitude");
	}
	
	public static String getGeoMethod() {
		return getAspiraProperty("geocoordinates.method");
	}
}

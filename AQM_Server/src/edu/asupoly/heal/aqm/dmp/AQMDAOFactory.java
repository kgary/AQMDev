package edu.asupoly.heal.aqm.dmp;

import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

public final class AQMDAOFactory {
	private static final String PROPERTY_FILENAME = "properties/dao.properties";
	public static final Calendar AQM_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	
    private static IAQMDAO __dao = null;
    private static Properties __daoProperties = null;
    
    private AQMDAOFactory() {
    	// We do not want this factory instantiated
    }
    
	public static IAQMDAO getDAO() {
        if (__dao != null) {
            return __dao;
        }
        __daoProperties = new Properties();
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(AQMDAOFactory.class.getClassLoader().getResourceAsStream(PROPERTY_FILENAME));
            __daoProperties.load(isr);
            // let's create a DAO based on a known property
            String daoClassName = __daoProperties.getProperty("daoClassName");
            Class<?> daoClass = Class.forName(daoClassName);
            __dao = (IAQMDAO)daoClass.newInstance();
            __dao.init(__daoProperties);
        } catch (Throwable t1) {
            System.out.println("Throwable in constructor for AspiraDAO");
            t1.printStackTrace();
        } try {
            if (isr != null) isr.close();
        } catch (Throwable t) {
        	System.out.println("Unable to close input dao property stream");
        	t.printStackTrace();
        }      
     
        return __dao;
	}
}

package edu.asupoly.aspira.dmp;

import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;

import edu.asupoly.aspira.model.AirQualityReadings;
import edu.asupoly.aspira.model.ServerPushEvent;

public final class AspiraDAO implements IAspiraDAO{
	private static String PROPERTY_FILENAME = "properties/dao.properties";
	
    private static AspiraDAO  __singletonDAOWrapper;
    private AspiraDAOBaseImpl __dao = null;
    private Properties        __daoProperties = null;
    
    /**
     * The wrapper constructor determines whether to push to a server URL,
     * and in the future can add any other interceptor-style behavior we wish
     * @throws DMPException if the DAO cannot be initialized
     */
    private AspiraDAO() {
        __daoProperties = new Properties();
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILENAME));
            __daoProperties.load(isr);
            // let's create a DAO based on a known property
            String daoClassName = __daoProperties.getProperty("daoClassName");
            Class<?> daoClass = Class.forName(daoClassName);
            __dao = (AspiraDAOBaseImpl)daoClass.newInstance();
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
    }
    
    
	public static IAspiraDAO getDAO() {
        if (__singletonDAOWrapper == null) {
            __singletonDAOWrapper = new AspiraDAO();
        }
        return __singletonDAOWrapper;
	}


	@Override
	public AirQualityReadings findAirQualityReadingsForPatientTail(String patientId, int tail) throws Exception {
        return __dao.findAirQualityReadingsForPatientTail(patientId, tail);
	}


	@Override
	public boolean importAirQualityReadings(AirQualityReadings toImport, boolean overwrite) throws Exception {
		return __dao.importAirQualityReadings(toImport, overwrite);
	}


	@Override
	public ServerPushEvent getLastServerPush(int type) throws Exception {
		return __dao.getLastServerPush(type);
	}


	@Override
	public ServerPushEvent getLastServerPush() throws Exception {
		return __dao.getLastServerPush();
	}


	@Override
	public AirQualityReadings findAirQualityReadingsForPatient(
			String patientId, Date start, Date end) throws Exception {
		return __dao.findAirQualityReadingsForPatient(patientId, start, end);
	}


	@Override
	public boolean addPushEvent(ServerPushEvent s) throws Exception {
		return __dao.addPushEvent(s);
	}


	@Override
	public AirQualityReadings findAirQualityReadingsForPatient(String patientId)
			throws Exception {
		return __dao.findAirQualityReadingsForPatient(patientId);
	}
	
	
	
	
	
}

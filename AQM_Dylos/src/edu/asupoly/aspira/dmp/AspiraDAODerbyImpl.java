package edu.asupoly.aspira.dmp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.asupoly.aspira.AspiraSettings;
import edu.asupoly.aspira.model.AirQualityReadings;
import edu.asupoly.aspira.model.ParticleReading;
import edu.asupoly.aspira.model.ServerPushEvent;


/**
 * @author kevinagary
 *
 */
public class AspiraDAODerbyImpl extends AspiraDAOBaseImpl {
    private static final int NO_GROUP_IDENTIFIER = -2;
    private static final long MS_ONE_YEAR_FROM_NOW = 1000L * 60L * 60 * 24L * 365L;
    
	private String __jdbcURL;
    private Properties __derbyProperties;

    public AspiraDAODerbyImpl() {
        super();  // true initialization goes in the init method
    }
	
	
		
	@Override
	public void init(Properties p) throws Exception {
        __derbyProperties = new Properties();
        String jdbcDriver = p.getProperty("jdbc.driver");
        String jdbcURL    = p.getProperty("jdbc.url");
        
        if (jdbcDriver == null || jdbcURL == null) {
            throw new Exception("JDBC not configured");         
        }
        
        // load the driver, test the URL
        try {
            // In case we need to modify system properties for Derby
            Properties sysProps = System.getProperties();
            
            // read in all the derby properties and SQL queries we need  
            Enumeration<?> keys = p.keys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                if (key.startsWith("sql")) {            
                    __derbyProperties.setProperty(key, p.getProperty(key));
                } else if (key.startsWith("derby")) {
                    sysProps.setProperty(key, p.getProperty(key));
                }
            }
            
            Class.forName(jdbcDriver);
            // test the connection
            if (!__testConnection(jdbcURL, p.getProperty("sql.checkConnectionQuery"))) {
                throw new Exception("Unable to connect to database");
            } else {
                System.out.println("Testing DAO Connection -- OK");
            }
            __jdbcURL = jdbcURL;
            __derbyProperties.setProperty("jdbc.driver", jdbcDriver);
            __derbyProperties.setProperty("jdbc.url", jdbcURL);
        } catch (Throwable t) {
            throw new Exception(t);
        }
	}



	private static boolean __testConnection(String url, String query) {
        Connection c = null;
        Statement s = null;
        try {
            c = DriverManager.getConnection(url);
            s = c.createStatement();
            return s.execute(query);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        } finally {
            try {
                if (s != null) s.close();
                if (c != null) c.close();
            } catch (SQLException se) {
            	se.printStackTrace();
            }
        }
	}



	@Override
	public AirQualityReadings findAirQualityReadingsForPatientTail(String patientId, int tail) throws Exception {
        if (tail <= 0) return null;
        return __findAirQualityReadingsForPatientByQuery(patientId, NO_GROUP_IDENTIFIER, tail, 
                __derbyProperties.getProperty("sql.findAirQualityReadingsForPatientTail"),
                null, null);
	}


    /**
     * Used by findAirQualityReadings methods, parameterized behavior
     * @param patientId
     * @param count - pass in MAXINT if not seeking the tail/head
     * @param query - pass in the query from the properties
     * @param begin - null if no start date
     * @param end   - null if no end date
     * @return
     * @throws Exception 
     */
	private AirQualityReadings __findAirQualityReadingsForPatientByQuery(
			String patientId, int groupId, int count, String query, Date begin, Date end) throws Exception {
        if (query == null || query.trim().length() == 0) return null;

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        AirQualityReadings rval = new AirQualityReadings();
        try {
            c = DriverManager.getConnection(__jdbcURL);
            ps = c.prepareStatement(query);
            if (patientId == null || patientId.length() == 0) {                
                ps.setString(1,  "%");
            } else {                
                ps.setString(1, patientId);
            }
            if (begin != null) {
                ps.setTimestamp(2, new java.sql.Timestamp(begin.getTime()), AspiraSettings.ASPIRA_CALENDAR);
                if (end != null) {
                    ps.setTimestamp(3, new java.sql.Timestamp(end.getTime()), AspiraSettings.ASPIRA_CALENDAR);
                } else {  // if we have no end but we have a begin we set end to the way future
                    ps.setTimestamp(3, new java.sql.Timestamp(begin.getTime()+MS_ONE_YEAR_FROM_NOW), AspiraSettings.ASPIRA_CALENDAR);
                }
            } else if (groupId != NO_GROUP_IDENTIFIER) {
                ps.setInt(2,  groupId);
            }
            rs = ps.executeQuery();
            while (rs.next() && count > 0) {
               rval.addReading(new ParticleReading(rs.getString("deviceid"), rs.getString("patientid"),
                       new Date(rs.getTimestamp("readingtime", AspiraSettings.ASPIRA_CALENDAR).getTime()), 
                       rs.getInt("smallparticle"), rs.getInt("largeparticle"), rs.getString("geolatitude"), rs.getString("geolongitude"), rs.getString("geomethod"), rs.getInt("groupid")));
               count--;
            }
            return rval;
        } catch (SQLException se) {
            se.printStackTrace();
            throw new Exception(se);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
	}



	@Override
	public boolean importAirQualityReadings(AirQualityReadings toImport, boolean overwrite) throws Exception {
        Connection c = null;
        PreparedStatement ps = null;
        PreparedStatement psgroup = null;
        ParticleReading next = null;
        ResultSet rs = null;
        int id = -1; // in case get unique fails
        
        if (toImport == null || toImport.size() == 0) return false;
        
        try {
            c = DriverManager.getConnection(__jdbcURL);
            ps = c.prepareStatement(__derbyProperties.getProperty("sql.importAirQualityReadingsWithGeo"));
            psgroup = c.prepareStatement(__derbyProperties.getProperty("sql.getUniqueId"));
            
            // every import is a batch insert of particle readings, so we track those imports in
            // a particular table
            rs = psgroup.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            
            Iterator<ParticleReading> iter = toImport.iterator();
            while (iter.hasNext()) {
                next = iter.next();
                ps.setString(1, next.getDeviceId());
                ps.setString(2, next.getPatientId());

                ps.setTimestamp(3, new java.sql.Timestamp(next.getDateTime().getTime()), AspiraSettings.ASPIRA_CALENDAR);
                ps.setInt(4, next.getSmallParticleCount());
                ps.setInt(5, next.getLargeParticleCount());
                ps.setString(6, next.getGeoLatitude());
                ps.setString(7, next.getGeoLongitude());
                ps.setString(8, next.getGeoMethod());
                
                ps.setInt(9, id);
                ps.executeUpdate();
                ps.clearParameters();
            }
            c.commit();
        } catch (SQLException se) {
        	se.printStackTrace();
            throw new Exception(se);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (psgroup != null) psgroup.close();
                if (c != null) {
                    c.rollback();
                    c.close();
                }
            } catch (SQLException se2) {
               se2.printStackTrace();
            }
        }
        return true;
	}
	
	@Override
	public boolean importAirQualityReadingsJson(JSONObject toImport, boolean overwrite) throws Exception {
        Connection c = null;
        PreparedStatement ps = null;
        PreparedStatement psgroup = null;
        ResultSet rs = null;
        int id = -1; // in case get unique fails
        
    	JSONArray jDeviceid =  (JSONArray)toImport.get("deviceid");
    	JSONArray jPatientid = (JSONArray)toImport.get("patientid");
    	JSONArray jDate = (JSONArray)toImport.get("readingtime");
    	JSONArray jSmall = (JSONArray)toImport.get("smallparticle");
    	JSONArray jLarge = (JSONArray)toImport.get("largeparticle");

        
        if (jDeviceid.isEmpty() || jDeviceid.size() == 0) return false;
        
        try {
            c = DriverManager.getConnection(__jdbcURL);
            ps = c.prepareStatement(__derbyProperties.getProperty("sql.importAirQualityReadings"));
            psgroup = c.prepareStatement(__derbyProperties.getProperty("sql.getUniqueId"));
            
            // every import is a batch insert of particle readings, so we track those imports in
            // a particular table
            rs = psgroup.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            
            for(int i = 0; i < jDeviceid.size(); i++) {
                ps.setString(1, (String)jDeviceid.get(i));
                ps.setString(2, (String)jPatientid.get(i));

                Date d;
                d = (Date)jDate.get(i);
                
                ps.setTimestamp(3,new java.sql.Timestamp(d.getTime()), AspiraSettings.ASPIRA_CALENDAR);
                
                
                ps.setInt(4, (Integer) jSmall.get(i));
                ps.setInt(5, (Integer) jLarge.get(i));
                
                ps.setInt(6, id);
                ps.executeUpdate();
                ps.clearParameters();
            }
            

            c.commit();
        } catch (SQLException se) {
        	se.printStackTrace();
            throw new Exception(se);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (psgroup != null) psgroup.close();
                if (c != null) {
                    c.rollback();
                    c.close();
                }
            } catch (SQLException se2) {
               se2.printStackTrace();
            }
        }
		return true;
	}



	@Override
	public ServerPushEvent getLastServerPush(int type) throws Exception {
        if (type < 0) return getLastServerPush();
        return __findLastServerPushEventByQuery(__derbyProperties.getProperty("sql.getServerPushEventsForType"), type, true);
	}


	@Override
	public ServerPushEvent getLastServerPush() throws Exception {
        return __findLastServerPushEventByQuery(__derbyProperties.getProperty("sql.getServerPushEvents"), -1, true);
	}
	
	@Override
	public JSONObject getLastServerPushJson(int type) throws Exception {
		if (type < 0) return getLastServerPushJson();
		return __findLastServerPushEventByQueryJson(__derbyProperties.getProperty("sql.getServerPushEventsForType"), type, true);
	}


	@Override
	public JSONObject getLastServerPushJson() throws Exception {
		return __findLastServerPushEventByQueryJson(__derbyProperties.getProperty("sql.getServerPushEvents"), -1, true);
	}
	

	private JSONObject __findLastServerPushEventByQueryJson(String query, int type, boolean includeErrors) throws Exception {
		if (query == null || query.trim().length() == 0) return null;  
		
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONObject rval = new JSONObject();
        try {
            c = DriverManager.getConnection(__jdbcURL);
            ps = c.prepareStatement(query);
            int minCode = 0;
            if (includeErrors) {
                minCode = -9999;
            }
            ps.setInt(1,  minCode);
            if (type >= 0) {
                ps.setInt(2, type);
            }
 
            rs = ps.executeQuery();
            if (rs.next()) {
            	//Timestamp t = rs.getTimestamp("eventtime", AspiraSettings.ASPIRA_CALENDAR);
            	//Date d = new Date(t.getTime());
            	//rval.put("eventtime", d);
            	rval.put("eventtime", new Date(rs.getTimestamp("eventtime", AspiraSettings.ASPIRA_CALENDAR).getTime()));
            	rval.put("responsecode", rs.getInt("responsecode"));
            	rval.put("objecttype", rs.getInt("objecttype"));
            	rval.put("message", rs.getString("message"));
            	
            }            
            return rval;
        } catch (SQLException se) {
        	se.printStackTrace();
            throw new Exception(se);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }		

	}


	private ServerPushEvent __findLastServerPushEventByQuery(String query, int type, boolean includeErrors) throws Exception{
        if (query == null || query.trim().length() == 0) return null;        
        
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ServerPushEvent rval = null;
        try {
            c = DriverManager.getConnection(__jdbcURL);
            ps = c.prepareStatement(query);
            int minCode = 0;
            if (includeErrors) {
                minCode = -9999;
            }
            ps.setInt(1,  minCode);
            if (type >= 0) {
                ps.setInt(2, type);
            }
 
            rs = ps.executeQuery();
            if (rs.next()) {
               rval = new ServerPushEvent(new Date(rs.getTimestamp("eventtime", AspiraSettings.ASPIRA_CALENDAR).getTime()),
                       rs.getInt("responsecode"), rs.getInt("objecttype"), rs.getString("message"));
            }            
            return rval;
        } catch (SQLException se) {
        	se.printStackTrace();
            throw new Exception(se);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
	}



	@Override
	public AirQualityReadings findAirQualityReadingsForPatient(
			String patientId, Date start, Date end) throws Exception {
        if (start == null || end == null) return null;
        return __findAirQualityReadingsForPatientByQuery(patientId, NO_GROUP_IDENTIFIER, Integer.MAX_VALUE, 
                __derbyProperties.getProperty("sql.findAirQualityReadingsForPatientBetween"),
                start, end);
	}



	@Override
	public boolean addPushEvent(ServerPushEvent s) throws Exception {
        if (s == null) return false;

        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = DriverManager.getConnection(__jdbcURL);
            ps = c.prepareStatement(__derbyProperties.getProperty("sql.addServerPushEvent"));
            ps.setTimestamp(1, new java.sql.Timestamp(s.getEventDate().getTime()), AspiraSettings.ASPIRA_CALENDAR);
            ps.setInt(2,  s.getResponseCode());
            ps.setInt(3, s.getImportType());
            ps.setString(4, s.getMessage());
            return (ps.executeUpdate() == 1);
        } catch (SQLException se) {
            System.out.println("addPushEvent SQL Error");
            se.printStackTrace();
            throw new Exception(se);
        } catch (Throwable t) {
            System.out.println("addPushEvent Throwable");
            t.printStackTrace();
            throw new Exception(t);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
	}



	@Override
	public AirQualityReadings findAirQualityReadingsForPatient(String patientId)
			throws Exception {
        return __findAirQualityReadingsForPatientByQuery(patientId, NO_GROUP_IDENTIFIER, Integer.MAX_VALUE, 
                __derbyProperties.getProperty("sql.findAirQualityReadingsForPatient"),
                null, null);
	}



	@Override
	public JSONObject findAirQualityReadingsForPatientTailJson(String patientId, int tail) throws Exception {
        if (tail <= 0) return null;
        return __findAirQualityReadingsForPatientByQueryJson(patientId, NO_GROUP_IDENTIFIER, tail, 
                __derbyProperties.getProperty("sql.findAirQualityReadingsForPatientTail"),
                null, null);
	}



	@Override
	public JSONObject findAirQualityReadingsForPatientJson(String patientId) throws Exception {
	        return __findAirQualityReadingsForPatientByQueryJson(patientId, NO_GROUP_IDENTIFIER, Integer.MAX_VALUE, 
	                __derbyProperties.getProperty("sql.findAirQualityReadingsForPatient"),
	                null, null);
	}




	private JSONObject __findAirQualityReadingsForPatientByQueryJson(
			String patientId, int groupId, int count, String query, Date begin, Date end) throws Exception {
        if (query == null || query.trim().length() == 0) return null;

        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONObject rval = new JSONObject();
        try {
            c = DriverManager.getConnection(__jdbcURL);
            ps = c.prepareStatement(query);
            if (patientId == null || patientId.length() == 0) {                
                ps.setString(1,  "%");
            } else {                
                ps.setString(1, patientId);
            }
            if (begin != null) {
                ps.setTimestamp(2, new java.sql.Timestamp(begin.getTime()), AspiraSettings.ASPIRA_CALENDAR);
                if (end != null) {
                    ps.setTimestamp(3, new java.sql.Timestamp(end.getTime()), AspiraSettings.ASPIRA_CALENDAR);
                } else {  // if we have no end but we have a begin we set end to the way future
                    ps.setTimestamp(3, new java.sql.Timestamp(begin.getTime()+MS_ONE_YEAR_FROM_NOW), AspiraSettings.ASPIRA_CALENDAR);
                }
            } else if (groupId != NO_GROUP_IDENTIFIER) {
                ps.setInt(2,  groupId);
            }
            rs = ps.executeQuery();
            
        	JSONArray jDeviceid = new JSONArray();
        	JSONArray jPatientid = new JSONArray();
        	JSONArray jDate = new JSONArray();
        	JSONArray jSmall = new JSONArray();
        	JSONArray jLarge = new JSONArray();  
        	
        	int i = 0;
        	
            while (rs.next() && count > 0 ) {
            	Timestamp t = rs.getTimestamp("readingtime", AspiraSettings.ASPIRA_CALENDAR);
            	Date d = new Date(t.getTime());
            	            	
        		jDeviceid.add(i, rs.getString("deviceid"));
        		jPatientid.add(i, rs.getString("patientid"));
        		jDate.add(i, d);
        		jSmall.add(i, rs.getInt("smallparticle"));
        		jLarge.add(i, rs.getInt("largeparticle"));
            	
        		count--;
        		i++;
            }
        	
        	rval.put("deviceid", jDeviceid);
        	rval.put("patientid", jPatientid);
        	rval.put("readingtime", jDate);
        	rval.put("smallparticle", jSmall);
        	rval.put("largeparticle", jLarge);
        	
        	//System.out.println(rval.toString());
        	
            return rval;
        } catch (SQLException se) {
            se.printStackTrace();
            throw new Exception(se);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
	}


	









}

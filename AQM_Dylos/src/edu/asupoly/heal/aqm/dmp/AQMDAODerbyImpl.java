package edu.asupoly.heal.aqm.dmp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.asupoly.heal.aqm.model.DylosReading;
import edu.asupoly.heal.aqm.model.ServerPushEvent;

public class AQMDAODerbyImpl implements IAQMDAO {
	private static final long MS_ONE_YEAR_FROM_NOW = 1000L * 60L * 60 * 24L
			* 365L;

	private String __jdbcURL;
	private Properties __derbyProperties;

	public AQMDAODerbyImpl() {
	}

	@Override
	public void init(Properties p) throws Exception {
		__derbyProperties = new Properties();
		String jdbcDriver = p.getProperty("jdbc.driver");
		String jdbcURL = p.getProperty("jdbc.url");
		// do we need user and password?

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
				String key = (String) keys.nextElement();
				if (key.startsWith("sql")) {
					__derbyProperties.setProperty(key, p.getProperty(key));
				} else if (key.startsWith("derby")) {
					sysProps.setProperty(key, p.getProperty(key));
				}
			}

			Class.forName(jdbcDriver);
			// test the connection
			if (!__testConnection(jdbcURL,
					p.getProperty("sql.checkConnectionQuery"))) {
				System.out.println("Testing Connection Failed "
						+ p.getProperty("sql.checkConnectionQuery"));
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
				if (s != null)
					s.close();
				if (c != null)
					c.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

	// received Dylos json string sample:
	// [{"deviceId":"aqm1","userId":"patient1","dateTime":"Sat Mar 08 22:24:10 MST 2014",
	// "smallParticle":76,"largeParticle":16,
	// "geoLatitude":33.3099177,"geoLongitude":-111.6726974,"geoMethod":"manual"},{...},...]
	@Override
	public boolean importDylosReading(String toImport) throws Exception {
		Connection c = null;
		PreparedStatement ps = null;

		JSONArray jsonary = new JSONArray();
		JSONParser parser = new JSONParser();
		JSONObject jsonobj = new JSONObject();
		jsonary = (JSONArray) parser.parse(toImport);
		if (jsonary.isEmpty())
			return false;

		try {
			c = DriverManager.getConnection(__jdbcURL);
			ps = c.prepareStatement(__derbyProperties
					.getProperty("sql.importDylosReadingWithGeo"));

			for (int i = 0; i < jsonary.size(); i++) {
				jsonobj = (JSONObject) jsonary.get(i);
				ps.setString(1, (String) jsonobj.get("deviceId"));
				ps.setString(2, (String) jsonobj.get("userId"));

				String dateTime = (String) jsonobj.get("dateTime");
				Date d = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
						Locale.US).parse(dateTime);
				ps.setTimestamp(3, new java.sql.Timestamp(d.getTime()),
						AQMDAOFactory.AQM_CALENDAR);
				
				//ps.setInt(4, (Integer) jsonobj.get("smallParticle")); java.lang.ClassCastException: java.lang.Long cannot be cast to java.lang.Integer
				ps.setInt(4, ((Long) jsonobj.get("smallParticle")).intValue());
				ps.setInt(5, ((Long) jsonobj.get("largeParticle")).intValue());
				ps.setDouble(6, (Double) jsonobj.get("geoLatitude"));
				ps.setDouble(7, (Double) jsonobj.get("geoLongitude"));
				ps.setString(8, (String) jsonobj.get("geoMethod"));

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
				if (ps != null)
					ps.close();
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
	public ServerPushEvent getLastServerPush() throws Exception {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ServerPushEvent rval = null;
		boolean includeErrors = true;
		int minCode = 0;

		try {
			c = DriverManager.getConnection(__jdbcURL);
			ps = c.prepareStatement(__derbyProperties
					.getProperty("sql.getServerPushEvents"));

			if (includeErrors) {
				minCode = -9999;
			}
			ps.setInt(1, minCode);
			rs = ps.executeQuery();
			if (rs.next()) {
				Timestamp t = rs.getTimestamp("eventtime", AQMDAOFactory.AQM_CALENDAR);
				Date d = new Date(t.getTime());

				rval = new ServerPushEvent(d.toString(), rs.getInt("responsecode"), rs.getInt("devicetype"),
						rs.getString("message"));
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
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (c != null)
					c.close();
			} catch (SQLException se2) {
				se2.printStackTrace();
			}
		}
	}

	@Override
	public JSONArray findDylosReadingsForUserBetween(String userId, Date start, Date end) throws Exception {
		if (start == null || end == null) return null;
		//
		
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JSONArray rval = new JSONArray();
        try {
            c = DriverManager.getConnection(__jdbcURL);
            ps = c.prepareStatement(__derbyProperties.getProperty("sql.findDylosReadingsForUserBetween"));
            
            if (userId == null) {                
                ps.setString(1,  "%");
            } else {                
                ps.setString(1, userId);
            }
            if (start != null) {
                ps.setTimestamp(2, new java.sql.Timestamp(start.getTime()), AQMDAOFactory.AQM_CALENDAR);
                if (end != null) {
                    ps.setTimestamp(3, new java.sql.Timestamp(end.getTime()), AQMDAOFactory.AQM_CALENDAR);
                } else {  // if we have no end but we have a begin we set end to the way future
                    ps.setTimestamp(3, new java.sql.Timestamp(start.getTime()+MS_ONE_YEAR_FROM_NOW), AQMDAOFactory.AQM_CALENDAR);
                }
            }
            rs = ps.executeQuery();
            while (rs.next()) {
            	Timestamp t = rs.getTimestamp("datetime", AQMDAOFactory.AQM_CALENDAR);
            	Date d = new Date(t.getTime());

            	DylosReading dr = new DylosReading(rs.getString("deviceid"), rs.getString("userid"), d.toString(), rs.getInt("smallparticle"), rs.getInt("largeparticle"), rs.getDouble("geolatitude"), rs.getDouble("geolongitude"), rs.getString("geomethod"));
            	rval.add(dr);
            }
            
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
        return rval;
	}

	@Override
	public boolean addPushEvent(ServerPushEvent s) throws Exception {
        if (s == null) return false;

        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = DriverManager.getConnection(__jdbcURL);
            ps = c.prepareStatement(__derbyProperties.getProperty("sql.addServerPushEvent"));
            
            String dateTime = s.getEventTime();
			Date d = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
					.parse(dateTime);
			ps.setTimestamp(1, new java.sql.Timestamp(d.getTime()), AQMDAOFactory.AQM_CALENDAR);
		
            ps.setInt(2,  s.getResponseCode());
            ps.setInt(3, s.getDeviceType());
            ps.setString(4, s.getMessage());
            ps.executeUpdate();
			ps.clearParameters();
			c.commit();

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
		return true;
	}
}

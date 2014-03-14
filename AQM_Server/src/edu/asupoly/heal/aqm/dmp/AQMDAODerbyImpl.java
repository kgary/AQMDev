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
import edu.asupoly.heal.aqm.model.SensordroneReading;
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

	// received Sensordrone json string sample
	// {"deviceId":"SensorDroneB8:FF:FE:B9:D9:A0","dateTime":"20140313_195444",
	// "co2DeviceID":"UNKNOWN","coData":-2,"co2Data":-1,
	// "presureData":96128,"tempData":27,"humidityData":42,
	// "geoLatitude":33.2830173,"geoLongitude":-111.7627723,"geoMethod":"Network"}
	@Override
	public boolean importSensordroneReading(String toImport) throws Exception {
		Connection c = null;
		PreparedStatement ps = null;

		JSONParser parser = new JSONParser();
		JSONObject jsonobj = new JSONObject();
		jsonobj = (JSONObject) parser.parse(toImport);
		if (jsonobj.isEmpty())
			return false;

		try {
			c = DriverManager.getConnection(__jdbcURL);
			ps = c.prepareStatement(__derbyProperties
					.getProperty("sql.importSensordroneReadingWithGeo"));

			ps.setString(1, (String) jsonobj.get("deviceId"));

			String dateTime = (String) jsonobj.get("dateTime");
			Date d = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
					.parse(dateTime);
			ps.setTimestamp(2, new java.sql.Timestamp(d.getTime()),
					AQMDAOFactory.AQM_CALENDAR);

			ps.setString(3, (String) jsonobj.get("co2DeviceID"));
			ps.setInt(4, ((Long) jsonobj.get("coData")).intValue());
			ps.setInt(5, ((Long) jsonobj.get("co2Data")).intValue());
			ps.setInt(6, ((Long) jsonobj.get("presureData")).intValue());
			ps.setInt(7, ((Long) jsonobj.get("tempData")).intValue());
			ps.setInt(8, ((Long) jsonobj.get("humidityData")).intValue());
			ps.setDouble(9, (Double) jsonobj.get("geoLatitude"));
			ps.setDouble(10, (Double) jsonobj.get("geoLongitude"));
			ps.setString(11, (String) jsonobj.get("geoMethod"));

			ps.executeUpdate();
			ps.clearParameters();
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

	// [{"deviceId":"aqm1","userId":"patient1","dateTime":"Sat Mar 08 22:24:10 MST 2014",
	// "smallParticle":76,"largeParticle":16,
	// "geoLatitude":33.3099177,"geoLongitude":-111.6726974,"geoMethod":"manual"},{...},...]
	@Override
	public JSONArray findDylosReadingsTest() throws Exception {
		Connection c = null;
		ResultSet rs = null;
		JSONArray prtrdArray = new JSONArray();
		try {
			c = DriverManager.getConnection(__jdbcURL);
			Statement statement = c.createStatement();
			statement.setMaxRows(10);
			rs = statement
					.executeQuery("select * from particle_reading order by dateTime desc");
			while (rs.next()) {
				Timestamp t = rs.getTimestamp("dateTime",
						AQMDAOFactory.AQM_CALENDAR);
				Date d = new Date(t.getTime());

				String deviceId = rs.getString("deviceId");
				String userId = rs.getString("userId");
				String dateTime = d.toString();
				int smallParticle = rs.getInt("smallParticle");
				int largeParticle = rs.getInt("largeParticle");
				double geoLatitude = rs.getDouble("geoLatitude");
				double geoLongitude = rs.getDouble("geoLongitude");
				String geoMethod = rs.getString("geoMethod");

				DylosReading prd = new DylosReading(deviceId, userId, dateTime,
						smallParticle, largeParticle, geoLatitude,
						geoLongitude, geoMethod);

				prtrdArray.add(prd);
			}
		} catch (SQLException se) {
			se.printStackTrace();
			throw new Exception(se);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new Exception(t);
		} finally {
			if (c != null)
				c.close();
			if (rs != null)
				rs.close();
		}

		return prtrdArray;
	}

	// [{"deviceId":"SensorDroneB8:FF:FE:B9:D9:A0","dateTime":"20140313_195444",
	// "co2DeviceID":"UNKNOWN","coData":-2,"co2Data":-1,
	// "presureData":96128,"tempData":27,"humidityData":42,
	// "geoLatitude":33.2830173,"geoLongitude":-111.7627723,"geoMethod":"Network"},{...},...]
	@Override
	public JSONArray findSensordroneReadingsTest() throws Exception {
		Connection c = null;
		ResultSet rs = null;
		JSONArray senrdArray = new JSONArray();
		try {
			c = DriverManager.getConnection(__jdbcURL);
			Statement statement = c.createStatement();
			statement.setMaxRows(10);
			rs = statement
					.executeQuery("select * from sensordrone_reading order by datetime desc");
			while (rs.next()) {
				Timestamp t = rs.getTimestamp("dateTime",
						AQMDAOFactory.AQM_CALENDAR);
				Date d = new Date(t.getTime());

				String deviceId = rs.getString("deviceId");
				String dateTime = d.toString();
				String co2DeviceID = rs.getString("co2DeviceID");
				int coData = rs.getInt("coData");
				int co2Data = rs.getInt("co2Data");
				int presureData = rs.getInt("presureData");
				int tempData = rs.getInt("tempData");
				int humidityData = rs.getInt("humidityData");
				double geoLatitude = rs.getDouble("geoLatitude");
				double geoLongitude = rs.getDouble("geoLongitude");
				String geoMethod = rs.getString("geoMethod");

				SensordroneReading ssr = new SensordroneReading(deviceId,
						dateTime, co2DeviceID, coData, co2Data, presureData,
						tempData, humidityData, geoLatitude, geoLongitude,
						geoMethod);

				senrdArray.add(ssr);
			}
		} catch (SQLException se) {
			se.printStackTrace();
			throw new Exception(se);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new Exception(t);
		} finally {
			if (c != null)
				c.close();
			if (rs != null)
				rs.close();
		}

		return senrdArray;
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

	@Override
	public JSONArray findServerPushEventTest() throws Exception {
		Connection c = null;
		ResultSet rs = null;
		JSONArray eventArray = new JSONArray();
		try {
			c = DriverManager.getConnection(__jdbcURL);
			Statement statement = c.createStatement();
			statement.setMaxRows(10);
			rs = statement
					.executeQuery("select * from server_push_event where responsecode > 0 order by eventtime desc");
			while (rs.next()) {
				Timestamp t = rs.getTimestamp("eventTime",
						AQMDAOFactory.AQM_CALENDAR);
				Date d = new Date(t.getTime());
				String eventTime = d.toString();

				int responseCode = rs.getInt("responseCode");
				int deviceType = rs.getInt("deviceType");
				String message = rs.getString("message");
				
				ServerPushEvent spe = new ServerPushEvent(eventTime,
						responseCode, deviceType, message);

				eventArray.add(spe);
			}
		} catch (SQLException se) {
			se.printStackTrace();
			throw new Exception(se);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new Exception(t);
		} finally {
			if (c != null)
				c.close();
			if (rs != null)
				rs.close();
		}

		return eventArray;
	}
}

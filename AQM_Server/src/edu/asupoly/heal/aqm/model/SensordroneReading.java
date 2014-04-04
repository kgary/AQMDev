package edu.asupoly.heal.aqm.model;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;

public class SensordroneReading implements JSONStreamAware {
	private String deviceId;
	private String dateTime;
	private String co2deviceId;
	private int coData;
	private int co2Data;
	private int presureData;
	private int tempData;
	private int humidityData;
	private double geoLatitude;
	private double geoLongitude;
	private String geoMethod;
	
	public SensordroneReading(String deviceId, String dateTime,
			String co2deviceId, int coData, int co2Data, int presureData,
			int tempData, int humidityData, double geoLatitude,
			double geoLongitude, String geoMethod) {
		this.deviceId = deviceId;
		this.co2deviceId = co2deviceId;
		this.dateTime = dateTime;
		this.coData = coData;
		this.co2Data = co2Data;
		this.presureData = presureData;
		this.tempData = tempData;
		this.humidityData = humidityData;
		this.geoLatitude = geoLatitude;
		this.geoLongitude = geoLongitude;
		this.geoMethod = geoMethod;
	}

	@Override
	public void writeJSONString(Writer out) throws IOException {
		LinkedHashMap obj = new LinkedHashMap();
		obj.put("deviceId", deviceId);
		obj.put("co2deviceId", co2deviceId);
		obj.put("dateTime", dateTime);
		obj.put("coData", coData);
		obj.put("co2Data", co2Data);
		obj.put("presureData", presureData);
		obj.put("tempData", tempData);
		obj.put("humidityData", humidityData);
		obj.put("geoLatitude", geoLatitude);
		obj.put("geoLongitude", geoLongitude);
		obj.put("geoMethod", geoMethod);
		JSONValue.writeJSONString(obj, out);
	}

	/*	{
	  "type": "Feature",
	  "properties": {...},
	  "geometry": {
		"type": "Point",
		"coordinates": [
		  63.017578125,
		  48.10743118848039
		]
	  }
}*/

/*	   "properties":{
     "type":"Sensordrone",
	 "deviceId":"SensorDrone00:00:00:00:00:00",
	 "dateTime":"Fri Mar 21 07:28:55 MST 2014",
	 "method":"Network",
	 "co2SensorId":"unknown",
	 "coData":1,
	 "co2Data":-1,
	 "pressureData":96264,
	 "tempData":24,
	 "humidityData":34
 }*/
public JSONObject getGeoJSONFeature() {
	JSONObject obj = new JSONObject();
	JSONObject prop = new JSONObject();
	JSONObject geo = new JSONObject();
	JSONArray coord = new JSONArray();
	obj.put("type", "Feature");
	obj.put("properties", prop);
	obj.put("geometry", geo);
	
	geo.put("type", "Point");
	geo.put("coordinates", coord);
	coord.add(new Double(geoLongitude));
	coord.add(new Double(geoLatitude));
	
	prop.put("type", "Sensordrone");
	prop.put("deviceId", deviceId);
	prop.put("dateTime", dateTime);
	prop.put("method", geoMethod);
	prop.put("co2SensorId", co2deviceId);
	prop.put("coData", new Integer(coData));
	prop.put("co2Data", new Integer(co2Data));
	prop.put("presureData", new Integer(presureData));
	prop.put("tempData", new Integer(tempData));
	prop.put("humidityData", new Integer(humidityData));

	return obj;
}
	
	public String getDeviceId() {
		return deviceId;
	}

	public String getCo2deviceId() {
		return co2deviceId;
	}

	public String getDateTime() {
		return dateTime;
	}

	public int getCoData() {
		return coData;
	}

	public int getCo2Data() {
		return co2Data;
	}

	public int getPresureData() {
		return presureData;
	}

	public int getTempData() {
		return tempData;
	}

	public int getHumidityData() {
		return humidityData;
	}

	public double getGeoLatitude() {
		return geoLatitude;
	}

	public double getGeoLongitude() {
		return geoLongitude;
	}

	public String getGeoMethod() {
		return geoMethod;
	}

}

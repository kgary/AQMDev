package edu.asupoly.heal.aqm.model;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

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

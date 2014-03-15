package edu.asupoly.heal.aqm.model;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;

public class DylosReading implements JSONStreamAware {
    
    private String deviceId;
    private String userId;
    private String dateTime;
    private int smallParticleCount;
    private int largeParticleCount;
    private double geoLatitude;
    private double geoLongitude;
    private String geoMethod;
    
    public DylosReading(String deviceId, String userId, String d, int s, int l, double geoLatitude, double geoLongitude, String geoMethod)  {
        this.deviceId  = deviceId;
        this.userId = userId;
        this.dateTime = d;
        this.smallParticleCount = s;
        this.largeParticleCount = l;
        this.geoLatitude = geoLatitude;
        this.geoLongitude = geoLongitude;
        this.geoMethod = geoMethod;
    }
    
	@Override
	public void writeJSONString(Writer out) throws IOException {
        LinkedHashMap obj = new LinkedHashMap();
        obj.put("deviceId", deviceId);
        obj.put("userId", userId);
        obj.put("dateTime", dateTime);
        obj.put("smallParticle", new Integer(smallParticleCount));
        obj.put("largeParticle", new Integer(largeParticleCount));
        obj.put("geoLatitude", new Double(geoLatitude));
        obj.put("geoLongitude", new Double(geoLongitude));
        obj.put("geoMethod", geoMethod);
        JSONValue.writeJSONString(obj, out);			
	}
	
    @Override
    public String toString() {
        return dateTime + " " + getUserId() + " " + getDeviceId() + " " + 
                getSmallParticleCount() + " " + getLargeParticleCount();
    }

	public int getLargeParticleCount() {
        return largeParticleCount;
	}

	public int getSmallParticleCount() {
        return smallParticleCount;
	}

	public String getDeviceId() {
        return deviceId;
	}

	public String getUserId() {
        return userId;
	}


	public String getDate() {
		return dateTime;
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

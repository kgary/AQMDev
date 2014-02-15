package edu.asupoly.aspira.model;

import java.util.Iterator;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/*
 * AirQualityReadings represents the air quality readings
 * taken by a given device for a given Patient.
 * The implementation is a SortedMap where the key is a
 * tuple <DeviceId, PatientId, Date> and the stored object
 * is a ParticleReading.
 */
public class AirQualityReadings implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1349373435898119224L;
	private TreeSet<ParticleReading> __readings;
	
    /*
     * To create one you have to know the device taking the readings
     * and the id of the patient to which the device is assigned
     */
    public AirQualityReadings() {
        __readings = new TreeSet<ParticleReading>();
    }

    
    public boolean addReading(ParticleReading pr) {
        if (__readings == null) {
            __readings = new TreeSet<ParticleReading>();
        }
        return __readings.add(pr);
    }

	public ParticleReading getFirstReading() {
        ParticleReading rval = null;
        if (__readings != null && !__readings.isEmpty()) {
            rval = __readings.first();
        }
        return rval;
	}

	public int size() {
        if (__readings == null)  return 0;
        return __readings.size();
	}

    /**
     * Gets all values as a iterator in ascending order
     * @return an Iterator with all values in ascending order or null
     */
	public Iterator<ParticleReading> iterator() {
        if (__readings != null && !__readings.isEmpty()) {
            return __readings.iterator();
        }
        return null;
	}

	public ParticleReading getLastReading() {
        ParticleReading rval = null;
        if (__readings != null && !__readings.isEmpty()) {
            rval = __readings.last();
        }
        return rval;
	}
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((__readings == null) ? 0 : __readings.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        AirQualityReadings other = (AirQualityReadings) obj;
        if (__readings == null) {
            if (other.__readings != null)
                return false;
        } else {
            Iterator<ParticleReading> thisIter  = __readings.iterator();
            Iterator<ParticleReading> otherIter = other.__readings.iterator();
            boolean equals = true;
            for (;
                 equals && thisIter.hasNext() && otherIter.hasNext(); 
                ) 
            {
                equals = equals && (thisIter.next().equals(otherIter.next()));
            }
            if (!equals) return false;
        }
        return true;
    }
    
    public JSONObject toJson(){
    	JSONObject obj = new JSONObject();
    	System.out.println("convert Serialized to JSON, contain: " + __readings.size() + " airqualityreadings");
    	
    	JSONArray jDeviceid = new JSONArray();
    	JSONArray jPatientid = new JSONArray();
    	JSONArray jDate = new JSONArray();
    	JSONArray jSmall = new JSONArray();
    	JSONArray jLarge = new JSONArray();
    	
    	ParticleReading next = null;
    	
    	Iterator<ParticleReading> iter = __readings.iterator();
    	int i = 0;
    	while(iter.hasNext()){
    		next = iter.next();
    		jDeviceid.add(i, next.getDeviceId());
    		jPatientid.add(i, next.getPatientId());
    		jDate.add(i, next.getDateTime());
    		jSmall.add(i, next.getSmallParticleCount());
    		jLarge.add(i, next.getLargeParticleCount());
    		i++;
    	}
    	obj.put("deviceid", jDeviceid);
    	obj.put("patientid", jPatientid);
    	obj.put("readingtime", jDate);
    	obj.put("smallparticle", jSmall);
    	obj.put("largeparticle", jLarge);
    	
    	System.out.println(obj.toString());
    	
		return obj;
    	
    }
    
}
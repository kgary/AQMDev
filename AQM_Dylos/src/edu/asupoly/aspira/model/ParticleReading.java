package edu.asupoly.aspira.model;

import java.util.Date;

public class ParticleReading implements java.io.Serializable, Comparable<ParticleReading> {

	private static final long serialVersionUID = 9002395112333017198L;

	public static final int DEFAULT_NO_GROUP_ASSIGNED = -1;

    private Date dateTime;
    private int smallParticleCount;
    private int largeParticleCount;

    private String deviceId;
    private String patientId;
    
    public ParticleReading(String deviceId, String patientId, Date d, int s, int l) {
		this(deviceId, patientId, d, s, l, DEFAULT_NO_GROUP_ASSIGNED);
	}

    public ParticleReading(String deviceId, String patientId, Date d, int s, int l, int groupid)  {
        this.deviceId  = deviceId;
        this.patientId = patientId;
        dateTime = d;
        smallParticleCount = s;
        largeParticleCount = l;
    }


	@Override
	public int compareTo(ParticleReading other) {
		 return dateTime.compareTo(other.dateTime);
	}
	
    @Override
    public String toString() {
        return dateTime.toString() + " " + getPatientId() + " " + getDeviceId() + " " + 
                getSmallParticleCount() + " " + getLargeParticleCount();
    }

    @Override
    public int hashCode() {
        return dateTime.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ParticleReading && dateTime.equals(((ParticleReading)obj).dateTime);
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

	public String getPatientId() {
        return patientId;
	}

	public Date getDateTime() {
        return dateTime;
	}
	
	
	
	
	
	
	
	
	
	
}

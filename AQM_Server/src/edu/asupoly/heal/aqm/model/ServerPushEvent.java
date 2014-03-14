package edu.asupoly.heal.aqm.model;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;

public class ServerPushEvent implements JSONStreamAware {
	public static final int PUSH_UNSET = 0;
	public static final int SERVER_DYLOS_IMPORT_FAILED = -20;
	public static final int SERVER_SENSORDRONE_IMPORT_FAILED = -21;
	public static final int SERVER_BAD_OBJECT_TYPE = -2;
	public static final int SERVER_STREAM_ERROR = -1;
	public static final int SERVER_STREAM_CORRUPTED_EXCEPTION = -10;
	public static final int SERVER_IO_EXCEPTION = -11;
	public static final int SERVER_SECURITY_EXCEPTION = -12;
	public static final int SERVER_NULL_POINTER_EXCEPTION = -13;
	public static final int SERVER_UNKNOWN_ERROR = -99;
	
	private String eventTime;
    private int responseCode;
    private int deviceType;
    private String message;
    

    public ServerPushEvent(String eventTime, int c, int t, String m) {
        this.eventTime = eventTime;
        this.responseCode = c;
        this.deviceType = t;
        this.message = m;        
    }

	@Override
	public void writeJSONString(Writer out) throws IOException {
        LinkedHashMap obj = new LinkedHashMap();
        obj.put("eventTime", eventTime);
        obj.put("responseCode", new Integer(responseCode));
        obj.put("deviceType", new Integer(deviceType));
        obj.put("message", message);
        JSONValue.writeJSONString(obj, out);	
	}
    
	public String getEventTime() {
        return eventTime;
	}

	public String getMessage() {
        return message;
	}
	
	public int getResponseCode() {
        return responseCode;
	}

	public int getDeviceType() {
        return deviceType;
	}
}

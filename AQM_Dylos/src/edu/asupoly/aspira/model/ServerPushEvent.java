package edu.asupoly.aspira.model;

import java.util.Date;

public class ServerPushEvent implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6862032958238175093L;
	
	private Date __eventDate;
    private int  __responseCode;
    private int  __importType;
    private String __msg;
    
    public ServerPushEvent(Date d, int c, int t, String m) {
        __eventDate = d;
        __responseCode = c;
        __importType = t;
        __msg = m;        
    }

    public String toString() {
        return "Server Push Event on " + __eventDate + " for type " + __importType + " returned " +
                __responseCode + " with message " + __msg;
    }

	public Date getEventDate() {
        return __eventDate;
	}

	public int getResponseCode() {
		return __responseCode;
	}
	
    public int getImportType() {
        return __importType;
    }

    public String getMessage() {
        return __msg;
    }
}

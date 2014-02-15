package edu.asupoly.aspira.monitorservice;

import java.util.Date;
import java.util.Properties;
import java.util.TimerTask;

public abstract class AspiraTimerTask extends TimerTask{
    protected boolean _isInitialized;
    protected Date _lastRead;
    
    protected AspiraTimerTask() {
        super();
        _isInitialized = false;
    }
    
    public Date getLastReadingTime()  { return _lastRead; }
    
    // Init should read whatever properties it expects in the subclass
    public abstract boolean init(Properties props);
}

package edu.asupoly.aspira.monitorservice;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public final class MonitoringService {
	
	private static final int DEFAULT_MAX_TIMER_TASKS = 10;
	private static final int DEFAULT_INTERVAL = 10;  // in seconds
	private static final String TASK_KEY_PREFIX = "MonitorTask";
	private static final String TASKINTERVAL_KEY_PREFIX = "TaskInterval";
	
	private static final String PROPERTY_FILENAME = "properties/monitoringservice.properties";
	
	private Timer __timer;
	private HashMap<String, TimerTask> __tasks;
	private Properties __props;

	private static MonitoringService __theMonitoringService = null;

	private MonitoringService() throws Exception {
		__timer = new Timer();
		__tasks = new HashMap<String, TimerTask>();
		int maxTimerTasks   = DEFAULT_MAX_TIMER_TASKS;
		int defaultInterval = DEFAULT_INTERVAL; // all intervals in seconds
		__props = new Properties();
		InputStreamReader  isr = null;
		try {
			isr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILENAME));
			__props.load(isr);
			maxTimerTasks = Integer.parseInt(__props.getProperty("MaxTimerTasks"));
			defaultInterval = Integer.parseInt(__props.getProperty("DefaultTaskInterval"));			
		} catch (NumberFormatException nfe) {
			defaultInterval = DEFAULT_INTERVAL;
			maxTimerTasks   = DEFAULT_MAX_TIMER_TASKS;
		} catch (NullPointerException npe) {
			if (__props == null) {
				npe.printStackTrace();
			}
			defaultInterval = DEFAULT_INTERVAL;
			maxTimerTasks   = DEFAULT_MAX_TIMER_TASKS;
		} catch (Throwable t1) {
			t1.printStackTrace();
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		// for each TimerTask indicated in the property, schedule it
		int i = 1;
		int interval = defaultInterval;
		String intervalProp = null;
		String taskClassName = __props.getProperty(TASK_KEY_PREFIX+i);
		AspiraTimerTask nextTask = null;
		while (i <= maxTimerTasks && taskClassName != null) {
			try {
				intervalProp = __props.getProperty(TASKINTERVAL_KEY_PREFIX+i);
				if (intervalProp != null) {
					try {
						interval = Integer.parseInt(intervalProp);
					} catch (NumberFormatException nfe) {
						interval = defaultInterval;
					}
				} else {// no interval specified, use default
					interval = defaultInterval;
				}
				
				// let's create a TimerTask of that class and start it
				 Class<?> taskClass = Class.forName(taskClassName);
				 nextTask = (AspiraTimerTask)taskClass.newInstance();
				 
	             if (nextTask.init(__props)) {
	            	 __tasks.put(TASK_KEY_PREFIX+i, nextTask);
	            	 // fire up the task 1 second from now and execute at fixed delay
	                 __timer.schedule(nextTask, 1000L, interval*1000L); // repeat task in seconds
	            	 System.out.println("Created timer task " + (TASK_KEY_PREFIX+i) + " for task class " + taskClassName);
	                } else {
	                System.out.println("1. Unable to initialize MonitorService task from properties, skipping " + taskClassName);
	                }
			} catch (Throwable t) {
				t.printStackTrace();
			}
			i++;
			taskClassName = __props.getProperty(TASK_KEY_PREFIX+i);
		}
		
	}
	
	public static MonitoringService getMonitoringService() {
		if (__theMonitoringService == null) {
			try{
				__theMonitoringService = new MonitoringService();
			} catch (Throwable t) {
				t.printStackTrace();
			}			
		}
		return __theMonitoringService;
	}

	public void shutdownService() throws Exception{
        // Canceling the Timer gets rid of all tasks, allowing
        // the current one to complete.
        __timer.cancel();
        // If the singleton accessor is called again it will fire up another Timer
        MonitoringService.__theMonitoringService = null;
        System.out.println("Shutting down MonitorService");
	}

	public String[] listTasks() {
        if (__tasks == null || __tasks.isEmpty()) return null;
        String[] rval = new String[0];
        if (__tasks != null) {
            Set<Map.Entry<String,TimerTask>> t = __tasks.entrySet();
            if (t != null) {
                Iterator<Map.Entry<String,TimerTask>> iter = t.iterator();
                rval = new String[t.size()];
                int index = 0;
                while (iter.hasNext() && index < t.size()) {
                    Map.Entry<String,TimerTask> next = iter.next();
                    rval[index++] = next.getKey() + " : " + next.getValue().toString();
                }
            }
        }
        return rval;
	}

	public boolean cancelTask(String taskName) {
        boolean rval = false;
        TimerTask tt = __tasks.get(taskName);
        if (tt != null && tt.cancel()) {
            // clear it out of our Map and the Timer task Queue
            __tasks.remove(taskName);
            __timer.purge();
            rval = true;
        }
        return rval;
	}

}
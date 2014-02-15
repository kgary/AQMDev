package edu.asupoly.aspira.monitorservice;

import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import edu.asupoly.aspira.AspiraSettings;
import edu.asupoly.aspira.dmp.AspiraDAO;
import edu.asupoly.aspira.dmp.IAspiraDAO;
import edu.asupoly.aspira.model.AirQualityReadings;
import edu.asupoly.aspira.model.ParticleReading;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class AirQualitySerialMonitorTask extends AspiraTimerTask{
    private static final int BAUD_RATE = SerialPort.BAUDRATE_9600;
    private static final int DATA_BITS = SerialPort.DATABITS_8;
    private static final int STOP_BITS = SerialPort.STOPBITS_1;
    private static final int PARITY    = SerialPort.PARITY_NONE;
    private static final int MASK      = 
            SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
    private static final int TIMEOUT   = 500; // 1/2 sec timeout on serial port read
    
    private SerialPort    __serialPort;
    private Properties    __props;
    private AirQualityReadings __aqReadings = new AirQualityReadings();
    private String        __deviceId;
    private String        __patientId;
    
    public AirQualitySerialMonitorTask() {
        super();
    }

	@Override
	public boolean init(Properties p) {
		boolean rval = true;
		__props = new Properties();
		
        String deviceId     = AspiraSettings.getAirQualityMonitorId();
        String patientId    = AspiraSettings.getPatientId();
        String serialPort   = p.getProperty("aqmSerialPort");
        if (deviceId != null && patientId != null && serialPort != null) {
            __deviceId  = deviceId;
            __patientId = patientId;
            __props.setProperty("deviceid", deviceId);
            __props.setProperty("patientid", patientId);
            __props.setProperty("aqmSerialPort", serialPort);
            
            try {
            	// this section initializes the serialPort
            	__serialPort = new SerialPort(serialPort);
            	rval = __serialPort.openPort();
            	if(rval) {
                    __serialPort.setParams(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
                    __serialPort.setEventsMask(MASK);
                    // we purge initially to try and clear buffers
                    __serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
                    __serialPort.addEventListener(new AspiraDylosSerialEventListener());
            	}
            } catch (SerialPortException spe) {
            	spe.printStackTrace();
            	rval = false;
            }

        } else {
        	rval = false;
        }
        _isInitialized = rval;

        // This section tries to initialize the last reading date
        _lastRead = new Date(0L);  // Jan 1 1970, 00:00:00
        try {
            IAspiraDAO dao = AspiraDAO.getDAO();
            AirQualityReadings aqr = dao.findAirQualityReadingsForPatientTail(__props.getProperty("patientid"), 1);
            if (aqr != null) {
                ParticleReading e = aqr.getFirstReading();
                if (e != null) {
                    _lastRead = e.getDateTime();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("Unable to get last reading time");
        }		
        
		return rval;
	}

	@Override
	public void run() {
        if (_isInitialized) {
            System.out.println("MonitorService: executing AQMonitor Serial Port Reading Task");
            synchronized(__aqReadings) {
                if (__aqReadings.size() > 0) {
                    IAspiraDAO dao;
                    try {
                        dao = AspiraDAO.getDAO();
                        if (dao.importAirQualityReadings(__aqReadings, true)) { // return a boolean if we need it
                            ParticleReading pr = __aqReadings.getLastReading();
                            _lastRead = pr.getDateTime();  // for completeness even though we don't use it
                            
                            // Log how many we imported here
                            System.out.println("Imported AQ Readings " + __aqReadings.size());
                            // clear out __aqReadings by creating a new one
                            __aqReadings = new AirQualityReadings();
                        } else { // If the DAO didn't work we keep the air quality readings for the next try
                        	System.out.println("Failed to Import AQ Readings in DAO");
                        }                     
                    } catch (Exception e) {
                    	System.out.println("Exception writing serial ParticleReadings to database");
                    }                    
                }
            }
        } else {
        	System.out.println("MonitorService: trying to execute uninitialized AQMonitor Serial Port Reading Task");
        }
	}
    
    @Override
    public void finalize() {
        try {
            if (__serialPort.isOpened()) {
                if (__serialPort.closePort()) System.out.println("CLOSED SERIAL PORT");
                else System.out.println("Serial port already closed!");
            }
        } catch (Throwable t) {
            // silently discard
        }
    }
    
    // nested class
    class AspiraDylosSerialEventListener implements SerialPortEventListener {
        private StringBuffer __serialBuffer = new StringBuffer();
        @Override
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){//If data is available
                if(event.getEventValue() > 0){//Check bytes count in the input buffer
                    //Read data 
                    try {
                        byte buffer[] = __serialPort.readBytes(event.getEventValue(), TIMEOUT);
                        
                        // Need to convert what is in our buffer to a ParticleReading
                        // each reading format is <small>,<large>CRLF  (CRLF = \r\n)
                        // 4 bytes = 1 int in Java, char is 2 bytes, so we'll scan 2 bytes at a time

                        // This is not the most efficient conversion but it is the simplest
                        // We convert the byte buffer to a StringBuffer
                        String toProcess = null;
                        __serialBuffer.append(new String(buffer));
                        int lastIndexOf = __serialBuffer.lastIndexOf("\r\n");
                        if (lastIndexOf != -1) {
                            toProcess = __serialBuffer.substring(0, lastIndexOf);
                            __serialBuffer.delete(0, lastIndexOf+1);
                        }
                        // then we process the StringBuffer up to the last CRLF
                        if (toProcess != null) {
                            StringTokenizer st  = new StringTokenizer(toProcess, "\r\n");
                            StringTokenizer st2 = null;
                            synchronized (__aqReadings) {
                                while (st.hasMoreTokens()) {
                                    st2 = new StringTokenizer(st.nextToken(), ",");
                                    if (st2.countTokens() == 2) { // should always be the case
                                        ParticleReading pr = new ParticleReading(__deviceId, __patientId, new Date(), 
                                                Integer.parseInt(st2.nextToken()), Integer.parseInt(st2.nextToken()));
                                        __aqReadings.addReading(pr);
                                        System.out.println("Importing ParticleReading " + pr);
                                    } else {
                                        System.out.println("Unable to process serial port event in AQSM Timer Task");
                                    }
                                }
                            } //synch on __aqReadings
                        }
                    }
                    catch (SerialPortException ex) {
                        ex.printStackTrace();
                    }
                    catch (SerialPortTimeoutException spte) {
                    	spte.printStackTrace();
                    }
                    catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }  // end nested class
}
    


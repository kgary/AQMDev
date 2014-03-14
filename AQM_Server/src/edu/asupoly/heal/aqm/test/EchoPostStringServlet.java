package edu.asupoly.heal.aqm.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

@SuppressWarnings("serial")
public class EchoPostStringServlet extends HttpServlet {
    private static BoundedSynchList __jsonList;
    private static final int __MAX_CAPACITY = 999;
    
    //private static String _filename = null;
    
    // an inner class. I am so lazy
    private class BoundedSynchList {
        private Vector<String> __list;
        
        BoundedSynchList() {
            __list = new Vector<String>(__MAX_CAPACITY);
        }
        
        public void add(String s) {
            if (__list.size() == __list.capacity()) {
                __list.removeElementAt(__list.capacity()-1);
            }
            __list.insertElementAt(s, 0);
            ListIterator<String> li = __jsonList.listIterator();
        }     
        public ListIterator<String> listIterator() {
            return __list.listIterator();
        }
    }
    
    public void init(ServletConfig config) throws ServletException {
        __jsonList = new BoundedSynchList();
    }
    
    /**
     * doGet returns the time of the last successful import for patient patientid
     */
    public final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = null;
        try {
            String numStr = request.getParameter("num");
            int num = __jsonList.__list.size();
            try {
                if (numStr != null) {
                    num = Integer.parseInt(numStr);
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            response.setContentType("text/plain");
            out = response.getWriter();
            // now echo 
            ListIterator<String> li = __jsonList.listIterator();
            while (li.hasNext() && num-- > 0) {
                out.println(num + ". " + li.next());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Throwable t2) {
                System.out.println("Could not flush and close output stream on doGet");
            }
        }
    }
    
    /**
     * Handle upload of JSON objects
     *
     * @param request HTTP Request object
     * @param response HTTP Response object
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletInputStream sis = null;        
        String theString = "Not the real string here!";
        
        try {
            sis = request.getInputStream();
            if (sis != null) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(sis, writer);
                theString = writer.toString();
                // Now write it to the list at the beginning
                __jsonList.add(theString);
            }
        } catch (StreamCorruptedException sce) {
            sce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        } 
        PrintWriter pw = null;
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");
            pw = response.getWriter();
            pw.println("OK Sent "+ theString);
        } catch (Throwable t3) {
            System.out.println("Server pushed stacktrace on response: " + t3.getMessage());
            t3.printStackTrace();
        } finally {        
            try {
                if (pw != null) {
                    pw.close();            
                }
                if (sis != null) sis.close();              
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
        }
    }
    
}

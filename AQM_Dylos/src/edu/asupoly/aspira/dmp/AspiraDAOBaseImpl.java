package edu.asupoly.aspira.dmp;

import java.util.Properties;

/**
 * @author kevinagary
 * This abstract class enables the wrapper pattern for the DAO by
 * enforcing a contract on all concrete DAO implementations that they
 * must 1) implement the IAspiraDAO interface, and 2) implement an
 * init method so they can do their own init from a set of props.
 */
public abstract class AspiraDAOBaseImpl implements IAspiraDAO {

    protected AspiraDAOBaseImpl() {
    }

    public abstract void init(Properties p) throws Exception;
}
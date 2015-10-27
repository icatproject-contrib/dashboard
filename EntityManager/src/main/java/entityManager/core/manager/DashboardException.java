/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entityManager.core.manager;




@SuppressWarnings("serial")
public class DashboardException extends Exception {
    
    public enum DashboardExceptionType{
        /** An input parameter appears to be incorrect */
		BAD_PARAMETER,
		/** Some internal error has occurred */
		INTERNAL,
		/** This is normally an authorization problem */
		INSUFFICIENT_PRIVILEGES,
		/** The requested object does not exist */
		NO_SUCH_OBJECT_FOUND,
		/** An object already exists with the same key fields */
		OBJECT_ALREADY_EXISTS,
		/**
		 * This is normally an authentication problem or the session has expired
		 */
		SESSION,
		/** If the call is not appropriate for the system in the current state */
		VALIDATION,
		/** If no implementation is provided by the server */
		NOT_IMPLEMENTED
	}
    
    private DashboardExceptionType type;
    private int offset = -1;

    @Override
    public String toString() {
            return type + " " + super.toString();
    }

    public DashboardException(DashboardExceptionType type, String msg){
        super(msg);
        this.type = type;
    }
    
    public DashboardException(DashboardExceptionType type, String msg, int offset){
        this(type, msg);
        this.offset = offset;
    }
    
    

        
}

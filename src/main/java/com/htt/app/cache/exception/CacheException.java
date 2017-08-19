package com.htt.app.cache.exception;
 
public class CacheException extends RuntimeException {
	//~ Static fields/initializers -----------------------------------------------------------------

    private static final long serialVersionUID = -6751840717046373725L;

    protected String message;

    //~ Constructors -------------------------------------------------------------------------------

    /**
     * Creates a new ServiceException object.
     *
     * @param message
     */
    public CacheException(String message) {
    	this.message = message;
    }

    /**
     * Creates a new ServiceException object.
     *
     * @param message
     * @param cause
     */
    public CacheException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

	public CacheException(Exception re) {
		super(re);
	}

	@Override
	public String toString() {
		return this.message;
	}

	/**
	 * @see Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return this.message;
	}
    
}

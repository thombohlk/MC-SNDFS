package ndfs;

import helperClasses.logger.Logger;



public class Result extends Throwable {



    private static final long serialVersionUID = 1L;
    protected Logger logger;



    public Result(String message) {
        super(message);
    }
    
    public boolean isEqualTo(Result r) {
    	Class<?> a = this.getClass();
    	Class<?> b = r.getClass();
    	if (a == null || b == null) {
    		return true;
    	}
		return a.getName().equals(b.getName());
    }

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public Logger getLogger() {
		return this.logger;
	}
}

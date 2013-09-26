package ndfs;



public class Result extends Throwable {



    private static final long serialVersionUID = 1L;



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
}

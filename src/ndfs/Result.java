package ndfs;



public class Result extends Throwable {



    private static final long serialVersionUID = 1L;



    public Result(String message) {
        super(message);
    }
    
    public boolean compare(Result r) {
		return this.getMessage().equals(r.getMessage());
    }
}

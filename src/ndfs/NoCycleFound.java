package ndfs;



public class NoCycleFound extends Result {



    private static final long serialVersionUID = 1L;



    public NoCycleFound() {
        super("Did not find a cycle");
    }


    public NoCycleFound(int id) {
        super("Thread " + id + ": did not find a cycle");
    }


    public NoCycleFound(String message) {
        super("Did not find a cycle: " + message);
    }


    public NoCycleFound(int id, String message) {
        super("Thread " + id + ": did not find a cycle. " + message);
    }
}

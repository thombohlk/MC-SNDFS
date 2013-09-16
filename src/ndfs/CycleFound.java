package ndfs;



public class CycleFound extends Result {



    private static final long serialVersionUID = 1L;



    public CycleFound() {
        super("Found a cycle");
    }


    public CycleFound(int id) {
        super("Thread " + id + ": found a cycle");
    }


    public CycleFound(String message) {
        super("Found a cycle: " + message);
    }


    public CycleFound(int id, String message) {
        super("Thread " + id + ": found a cycle. " + message);
    }
}

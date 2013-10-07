package ndfs;



public interface NDFS {


    public void ndfs() throws Result;

    public void init(int nrOfThreads);
    
    public void tearDown();
    
}

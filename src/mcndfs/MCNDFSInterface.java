package mcndfs;

import ndfs.Result;

public interface MCNDFSInterface {

    public void ndfs() throws Result;

    public void init(int nrOfThreads);
    
    public void tearDown();
	
}

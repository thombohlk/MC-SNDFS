package mcndfs;

import graph.State;
import helperClasses.BooleanHashMap;
import helperClasses.IntegerHashMap;
import helperClasses.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ndfs.CycleFound;
import ndfs.NoCycleFound;
import ndfs.Result;

public abstract class MCNDFS implements MCNDFSInterface {

	protected File file;
	protected BooleanHashMap<State> stateRed;
	protected IntegerHashMap<State> stateCount;
	protected ArrayList<GeneralBird> swarm;
	protected Logger logger;

	public MCNDFS(File file) {
        this.file = file;
        this.stateRed = new BooleanHashMap<State>(new Boolean(false));
        this.stateCount = new IntegerHashMap<State>(new Integer(0));
    	this.swarm = new ArrayList<GeneralBird>();
    }

    protected void nndfs() throws Result {
        boolean foundCycle = false;
        int foundBy = 0;
        Result r;

        ExecutorService ex = Executors.newFixedThreadPool(swarm.size());
        CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(ex);
        
        // setup threads for each of the callables 
        for (int i = 0; i < this.swarm.size(); i++) {
            cs.submit(swarm.get(i));
        }

        // Wait for the first thread to return. If an exception is thrown the 
        // completion service is shut down and a CycleFound will be thrown.
        try {
			int result = cs.take().get();
			if (result > 0) {
				foundBy = result;
			} else {
				foundBy = -result;
				foundCycle = true;
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
        ex.shutdownNow();

        if (foundCycle) {
            r = new CycleFound(foundBy);
        } else {
            r = new NoCycleFound();
        }
        r.setLogger(logger);
        throw r;
        
    }


	public void ndfs() throws Result {
        nndfs();
    }

	public void tearDown() {
		if (logger != null) {
			logger.parseData();
			logger.stop();
		}
	}
	
}

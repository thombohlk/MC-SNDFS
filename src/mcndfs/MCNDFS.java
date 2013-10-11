package mcndfs;

import graph.State;
import helperClasses.BooleanHashMap;
import helperClasses.IntegerHashMap;
import helperClasses.logger.AlgorithmLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import ndfs.CycleFound;
import ndfs.NoCycleFound;
import ndfs.Result;

public abstract class MCNDFS implements MCNDFSInterface {
	
	protected AtomicLong totalWaitingTime = new AtomicLong(0);

	protected File file;
	protected BooleanHashMap<State> stateRed;
	protected IntegerHashMap<State> stateCount;
	protected ArrayList<GeneralBird> swarm;
	protected AlgorithmLogger logger;
	protected ExecutorService executorService;

	public MCNDFS(File file) {
        this.file = file;
        this.stateRed = new BooleanHashMap<State>(new Boolean(false));
        this.stateCount = new IntegerHashMap<State>(new Integer(0));
    	this.swarm = new ArrayList<GeneralBird>();
    }

    protected void nndfs() throws Result {
        boolean foundCycle = false;
        int foundBy = 0;
        Result result;

        long start = System.currentTimeMillis();
        executorService = Executors.newFixedThreadPool(swarm.size());
        CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(executorService);
        System.out.print("setup: " + (System.currentTimeMillis() - start));
        // setup threads for each of the callables 
        for (int i = 0; i < this.swarm.size(); i++) {
            cs.submit(swarm.get(i));
        }
        System.out.print(", adding: " + (System.currentTimeMillis() - start) + "\n");

        // Wait for the first thread to return. If an exception is thrown the 
        // completion service is shut down and a CycleFound will be thrown.
        try {
			int id = cs.take().get();
			if (id > 0) {
				foundBy = id;
			} else {
				foundBy = -id;
				foundCycle = true;
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
        executorService.shutdownNow();

        if (foundCycle) {
            result = new CycleFound(foundBy);
        } else {
            result = new NoCycleFound();
        }
        if (logger != null)
        	result.setAnalysisData(logger.getAnalysisData());
        throw result;
        
    }


	public void ndfs() throws Result {
        nndfs();
    }

	public void tearDown() {
		try {
			executorService.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (logger != null) {
			logger.parseData();
			logger.stop();
		}
	}
	
}

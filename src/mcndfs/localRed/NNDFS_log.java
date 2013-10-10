package mcndfs.localRed;

import graph.State;
import helperClasses.logger.AlgorithmLogger;

import java.io.File;

import ndfs.Result;

public class NNDFS_log extends NNDFS {

    class Bird extends NNDFS.Bird {

        Bird(int id) {
        	super(id);
        }
        
        @Override
        protected Integer shutDownAndReturn(boolean foundCycle) {
        	logger.logWaitingTime(this.id, this.waitingTime);
        	return super.shutDownAndReturn(foundCycle);
        }

        @Override
		protected void dfsRed(State s) throws Result, InterruptedException {
        	logger.logDfsRedStart(id, s);
        	super.dfsRed(s);
            logger.logDfsRedDone();
        }

        @Override
        protected void dfsBlue(State s) throws Result, InterruptedException {
        	logger.logDfsBlueStart(id, s);
        	super.dfsBlue(s);
            logger.logDfsBlueDone();
        }

    }


    public NNDFS_log(File file) {
    	super(file);
    	logger = new AlgorithmLogger(file);
    }

    @Override
    public void init(int nrOfThreads) {
    	this.nrOfThreads = nrOfThreads;
    	for (int i = 1; i <= nrOfThreads; i++) {
    		super.swarm.add(new Bird(i));
    	}
    }

    @Override
    protected void nndfs() throws Result {
        logger.start();
        super.nndfs();
    }

}

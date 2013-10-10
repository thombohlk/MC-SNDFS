package ndfs.nndfs;


import graph.Graph;
import graph.State;
import helperClasses.Color;
import helperClasses.logger.AlgorithmLogger;

import java.util.Map;

import ndfs.Result;


public class NNDFS_log extends NNDFS {

    public NNDFS_log(Graph graph, Map<State, Color> colorStore) {
    	super(graph, colorStore);
    	logger = new AlgorithmLogger(graph);
    }


    @Override
    protected void dfsRed(State s) throws Result {
    	logger.logDfsRedStart(1, s);
    	super.dfsRed(s);
        logger.logDfsRedDone();
    }

    @Override
    protected void dfsBlue(State s) throws Result {
    	logger.logDfsBlueStart(1, s);
    	super.dfsBlue(s);
        logger.logDfsBlueDone();
    }
    

    @Override
    protected void nndfs(State s) throws Result {
        super.nndfs(s);
    }


    public void ndfs() throws Result {
    	logger.start();
        super.ndfs();
    }
    
}

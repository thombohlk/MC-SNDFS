package mcndfs;

import graph.Graph;
import graph.GraphFactory;
import graph.State;
import helperClasses.BooleanHashMap;
import helperClasses.Color;
import helperClasses.Colors;
import helperClasses.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;

import ndfs.NoCycleFound;
import ndfs.Result;

/**
 * Abstract version of a callable object Bird that is able to look for
 * accepting cycles in a graph.
 * 
 * @author thomas
 *
 */
public abstract class GeneralBird implements Callable<Integer> {

	protected int id;
	protected Graph graph;
	protected State initialState;
	protected BooleanHashMap<State> localStatePink;
	protected Colors localColors;
	protected Random rand;

	protected GeneralBird(int id, File file) {
        try {
            this.graph = GraphFactory.createGraph(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.id = id;
        this.initialState = graph.getInitialState();
        this.localStatePink = new BooleanHashMap<State>(new Boolean(false));
        this.localColors = new Colors(new HashMap<State, Color>());
        this.rand = new Random(Global.SEED);
    }

    /**
     * Start algorithm.
     * 
     * @return Integer Returns -id if cycle has been found, otherwise id.
     */
    public Integer call() throws Exception {
        try {
            dfsBlue(initialState);
        } catch (Result e) {
            return -(this.id);
        }

        return this.id;
    }
    
    /**
     * Method to perform dfsBlue on state s.
     * 
     * @param s
     * @throws Result
     * @throws InterruptedException
     */
    protected void dfsBlue(State s) throws Result, InterruptedException {
    	if (Thread.currentThread().isInterrupted())
            terminate();
    }

    /**
     * Method to perform dfsRed on state s.
     * 
     * @param s
     * @throws Result
     * @throws InterruptedException
     */
	protected void dfsRed(State s) throws Result, InterruptedException {
    	if (Thread.currentThread().isInterrupted())
            terminate();
    }
    
	/**
	 * Method to savely terminate the callable.
	 * @throws Result
	 */
    protected void terminate() throws Result {
    	throw new NoCycleFound(id);
	}
    
}

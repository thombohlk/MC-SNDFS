package ndfs.mcndfs_nosync;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import graph.GraphFactory;
import graph.State;
import graph.Graph;
import helperClasses.BooleanHashMap;
import helperClasses.Colors;
import helperClasses.Color;
import helperClasses.IntegerHashMap;
import helperClasses.RandomSeed;
import mcndfs.GeneralBird;
import mcndfs.MCNDFS;
import ndfs.NDFS;
import ndfs.Result;
import ndfs.CycleFound;
import ndfs.NoCycleFound;

public class NNDFS extends MCNDFS {

	protected ConcurrentBooleanHashMap<State> stateRed;
	protected ConcurrentIntegerHashMap<State> stateCount;

    class Bird extends GeneralBird {

        Bird(int id) {
        	super(id, file);
        	stateRed = new ConcurrentBooleanHashMap<>(new AtomicBoolean(false));
        	stateCount = new ConcurrentIntegerHashMap<>(new AtomicInteger(0));
        }

        @Override
        protected void dfsRed(State s) throws Result, InterruptedException {
            List<State> post;

            localStatePink.put(s, true);

            post = graph.post(s);
            Collections.shuffle(post, this.rand);

            for (State t : post) {
                if (localColors.hasColor(t, Color.CYAN)) {
                    throw new CycleFound(id);
                }

                if (! localStatePink.get(t).booleanValue() && ! stateRed.get(t).get()) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
                stateCount.get(s).decrementAndGet();

                synchronized(stateCount) {
	                while (stateCount.get(s).intValue() > 0) {
	                	stateCount.wait();
	                }
	                stateCount.notifyAll();
                }
            }

            stateRed.put(s, new AtomicBoolean(true));
            localStatePink.put(s, false);
        }

        @Override
        protected void dfsBlue(State s) throws Result, InterruptedException {
            boolean allRed = true;
            List<State> post;

            localColors.color(s, Color.CYAN);

            post = graph.post(s);
            Collections.shuffle(post, this.rand);

            for (State t : post) {
            	// early cycle detection
            	if ( localColors.hasColor(t, Color.CYAN) && s.isAccepting() && t.isAccepting() ) {
            		throw new CycleFound(id);
            	}

            	if (localColors.hasColor(t, Color.WHITE) && ! stateRed.get(t).get()) {
                    dfsBlue(t);
                }
                
                // allred
                if (! stateRed.get(t).get()) {
                	allRed = false;
                }
            }

            if (allRed) {
                stateRed.put(s, new AtomicBoolean(true));
            } else if (s.isAccepting()) {
                stateCount.get(s).incrementAndGet();

                dfsRed(s);
            }

            localColors.color(s, Color.BLUE);
        }

    }

    public NNDFS(File file) {
    	super(file);
    }

    @Override
    public void init(int nrOfThreads) {
    	for (int i = 1; i <= nrOfThreads; i++) {
    		this.swarm.add(new Bird(i));
    	}
    }

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub
		
	}

}

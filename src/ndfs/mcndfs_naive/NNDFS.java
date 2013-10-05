package ndfs.mcndfs_naive;

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

import graph.GraphFactory;
import graph.State;
import graph.Graph;
import helperClasses.BooleanHashMap;
import helperClasses.Color;
import helperClasses.Colors;
import helperClasses.IntegerHashMap;
import helperClasses.RandomSeed;
import mcndfs.GeneralBird;
import mcndfs.MCNDFS;
import ndfs.NDFS;
import ndfs.Result;
import ndfs.CycleFound;
import ndfs.NoCycleFound;

public class NNDFS extends MCNDFS {

    class Bird extends GeneralBird {

        Bird(int id) {
        	super(id, file);
        }

        @Override
        protected void dfsRed(State s) throws Result, InterruptedException {
            boolean tRed;
            List<State> post;

            localStatePink.put(s, true);

            post = graph.post(s);
            Collections.shuffle(post, this.rand);

            for (State t : post) {
                if (localColors.hasColor(t, Color.CYAN)) {
                    throw new CycleFound();
                }

                synchronized (stateRed) {
                    tRed = stateRed.get(t);
                }
                if (! localStatePink.get(t).booleanValue() && ! tRed) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
                synchronized(stateCount) {
                    int count = stateCount.get(s).intValue();
                    stateCount.put(s, count - 1);
                }

                synchronized(stateCount) {
	                while (stateCount.get(s).intValue() > 0) {
	                	stateCount.wait();
	                }
	                stateCount.notifyAll();
                }
            }

            synchronized (stateRed) {
                stateRed.put(s, true);
            }
            localStatePink.put(s, false);
        }

        @Override
        protected void dfsBlue(State s) throws Result, InterruptedException {
            boolean tRed;
            List<State> post;

            localColors.color(s, Color.CYAN);

            post = graph.post(s);
            Collections.shuffle(post, this.rand);

            for (State t : post) {
                synchronized (stateRed) {
                    tRed = stateRed.get(t);
                }
                if (localColors.hasColor(t, Color.WHITE) && ! tRed) {
                    dfsBlue(t);
                }
            }

            if (s.isAccepting()) {
                synchronized (stateCount) {
                    int count = stateCount.get(s);
                    stateCount.put(s, count + 1);
                }

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

}

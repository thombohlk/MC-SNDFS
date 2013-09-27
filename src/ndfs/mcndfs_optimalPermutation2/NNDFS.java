package ndfs.mcndfs_optimalPermutation2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import helperClasses.Colors;
import helperClasses.Color;
import helperClasses.IntegerHashMap;
import ndfs.NDFS;
import ndfs.Result;
import ndfs.CycleFound;
import ndfs.NoCycleFound;

public class NNDFS implements NDFS {

    volatile private BooleanHashMap<State> stateRed;
    volatile private Map<State, Integer> stateCount;
    volatile private Map<State, Integer> permutationNr;

    private ArrayList<Bird> swarm;
    private File file;
    
    class Bird implements Callable<Integer> {

        int id;
        private Graph graph;
        private State initialState;
        private Colors localColors;
        private Map<State, Boolean> localStatePink;


        Bird(int id) {
            try {
                this.graph = GraphFactory.createGraph(file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            this.id = id;
            this.initialState = graph.getInitialState();
            this.localStatePink = new BooleanHashMap<State>(new Boolean(false));
            this.localColors = new Colors(new HashMap<State, Color>());
        }


        public Integer call() throws Exception {
            try {
                dfsBlue(initialState);
            } catch (Result e) {
                return -(this.id);
            }

            return this.id;
        }


        private void dfsRed(State s) throws Result, InterruptedException {
            boolean tRed;
            List<State> post;

            localStatePink.put(s, true);

            post = getPermutation(s);

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
	                stateCount.notify();
                }
            }

            synchronized (stateRed) {
                stateRed.put(s, true);
            }
            localStatePink.put(s, false);
        }


        private void dfsBlue(State s) throws Result, InterruptedException {
            boolean tRed;
            boolean allRed = true;
            List<State> post;

            localColors.color(s, Color.CYAN);

            post = getPermutation(s);

            for (State t : post) {
            	// early cycle detection
            	if ( localColors.hasColor(t, Color.CYAN) && s.isAccepting() && t.isAccepting() ) {
            		throw new CycleFound();
            	}
            	
                synchronized (stateRed) {
                    tRed = stateRed.get(t);
                }
                if (localColors.hasColor(t, Color.WHITE) && ! tRed) {
                    dfsBlue(t);
                }
                
                // allred
                synchronized (stateRed) {
                    if (! stateRed.get(t)) {
                    	allRed = false;
                    }
                }
            }

            if (allRed) {
                synchronized (stateRed) {
                    stateRed.put(s, true);
                }
            } else if (s.isAccepting()) {
                synchronized (stateCount) {
                    int count = stateCount.get(s);
                    stateCount.put(s, count + 1);
                }

                dfsRed(s);
            }

            localColors.color(s, Color.BLUE);
        }
        
        private List<State> getPermutation(State s) {
			// Get post states
			List<State> postStates = graph.post(s);
			
			// Determine amount of post states to permute
			int nrOfSuccessors = postStates.size();
			
			// Permute for amount of post states > 1
			if (nrOfSuccessors > 1) {
				// Create array to store rotations for the different sub-parts
				// Amount of rotations always 1 smaller than number of elements
				// (rotating 1 element is sort of pointless)
				int[] rotates = new int[(nrOfSuccessors-1)];
				
				// initialize local permutation id variable
				int permNumber = id;
				
				// Acquire permutation id and increase by one (synchronized)
//				synchronized (permutationNr) {
//					permNumber = permutationNr.get(s);
//					permutationNr.put(s, permNumber + 1);
//				}
				
				// Calculate initial rotation (ergo on all elements)
				// (to determine which state to visit first)
				rotates[(rotates.length-1)] = permNumber % nrOfSuccessors;
				
				// Determine remaining rotations (if any)
				for (int i=(nrOfSuccessors-3); i>=0; i--) {
					rotates[i] = (permNumber/nrOfSuccessors) % i+2;
				}
				
				// Execute rotations
				for (int i=1; i<=rotates.length; i++) {
					Collections.rotate(postStates.subList(i-1,rotates.length+1), -1*rotates[rotates.length-i]);
				}
				// return post states in permuted order
				return postStates;
			} else {
				return postStates;
			}
		}

    }

    public NNDFS(File file) {
        this.file = file;
        this.stateRed = new BooleanHashMap<State>(new Boolean(false));
        this.stateCount = new IntegerHashMap<State>(new Integer(0));
        this.permutationNr = new IntegerHashMap<State>(new Integer(1));
    }


    public void init(int nrOfThreads) {
    	this.swarm = new ArrayList<Bird>();
    	for (int i = 1; i <= nrOfThreads; i++) {
    		this.swarm.add(new Bird(i));
    	}
    }

    private void nndfs() throws Result {
        boolean foundCycle = false;
        int foundBy = 0;
        
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ex.shutdownNow();

        if (foundCycle) {
            throw new CycleFound(foundBy);
        } else {
            throw new NoCycleFound();
        }
    }


    public void ndfs() throws Result {
        nndfs();
    }

}

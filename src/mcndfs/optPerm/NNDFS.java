package mcndfs.optPerm;

import graph.State;
import helperClasses.Color;
import helperClasses.IntegerHashMap;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mcndfs.GeneralBird;
import mcndfs.MCNDFS;
import ndfs.CycleFound;
import ndfs.Result;

public class NNDFS extends MCNDFS {

    volatile private Map<State, Integer> permutationNr;
    
    class Bird extends GeneralBird {

        Bird(int id) {
        	super(id, file);
        }

        @Override
        protected void dfsRed(State s) throws Result, InterruptedException {
        	super.dfsRed(s);
        	
            boolean tRed;
            List<State> post;

            localStatePink.put(s, true);

            post = getPermutation(s);

            for (State t : post) {
                if (localColors.hasColor(t, Color.CYAN)) {
                    throw new CycleFound();
                }

                start = System.currentTimeMillis();
                synchronized (stateRed) {
                	waitingTime += System.currentTimeMillis() - start;
                    tRed = stateRed.get(t);
                }
                if (! localStatePink.get(t).booleanValue() && ! tRed) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
                start = System.currentTimeMillis();
                synchronized(stateCount) {
                	waitingTime += System.currentTimeMillis() - start;
                    int count = stateCount.get(s).intValue();
                    stateCount.put(s, count - 1);
                }

                start = System.currentTimeMillis();
                synchronized(stateCount) {
                	waitingTime += System.currentTimeMillis() - start;
	                while (stateCount.get(s).intValue() > 0) {
	                	stateCount.wait();
	                }
	                stateCount.notifyAll();
                }
            }

            start = System.currentTimeMillis();
            synchronized (stateRed) {
            	waitingTime += System.currentTimeMillis() - start;
                stateRed.put(s, true);
            }
            localStatePink.put(s, false);
        }

        @Override
        protected void dfsBlue(State s) throws Result, InterruptedException {
        	super.dfsBlue(s);
        	
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

                start = System.currentTimeMillis();
                synchronized (stateRed) {
                	waitingTime += System.currentTimeMillis() - start;
                    tRed = stateRed.get(t);
                }
                if (localColors.hasColor(t, Color.WHITE) && ! tRed) {
                    dfsBlue(t);
                }
                
                // allred
                start = System.currentTimeMillis();
                synchronized (stateRed) {
                	waitingTime += System.currentTimeMillis() - start;
                    if (! stateRed.get(t)) {
                    	allRed = false;
                    }
                }
            }

            if (allRed) {
                start = System.currentTimeMillis();
                synchronized (stateRed) {
                	waitingTime += System.currentTimeMillis() - start;
                    stateRed.put(s, true);
                }
            } else if (s.isAccepting()) {
                start = System.currentTimeMillis();
                synchronized (stateCount) {
                	waitingTime += System.currentTimeMillis() - start;
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
				int permNumber;
				
				// Acquire permutation id and increase by one (synchronized)
                start = System.currentTimeMillis();
				synchronized (permutationNr) {
                	waitingTime += System.currentTimeMillis() - start;
					permNumber = permutationNr.get(s);
					permutationNr.put(s, permNumber + 1);
				}
				
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
        
        @Override
        protected void terminate() throws Result {
        	synchronized(stateCount) {
                stateCount.notifyAll();
            }
        	super.terminate();
        }

    }

    public NNDFS(File file) {
    	super(file);
        this.permutationNr = new IntegerHashMap<State>(new Integer(1));
    }

    @Override
    public void init(int nrOfThreads) {
    	for (int i = 1; i <= nrOfThreads; i++) {
    		super.swarm.add(new Bird(i));
    	}
    }

}

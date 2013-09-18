package ndfs.mcndfs_1_naive;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import graph.State;
import graph.Graph;
import ndfs.NDFS;
import ndfs.Result;
import ndfs.CycleFound;
import ndfs.NoCycleFound;

public class NNDFS implements NDFS {


    volatile private Graph graph;
    volatile private Colors colors;
    volatile private Map<State, Integer> counts;
    volatile boolean busy = false;
    

    class Bird implements Callable<Integer> {

    	int id;
    	private State initialState;
    	private Colors localColors;
        private Map<State, Boolean> localPinks;


    	Bird(State s, int i) {
    		this.id = i;
    		this.initialState = s;
    		this.localPinks = new BooleanHashMap<State>(new Boolean(false));

            Map<State, ndfs.mcndfs_1_naive.Color> map = new HashMap<State, ndfs.mcndfs_1_naive.Color>();
            this.localColors = new Colors(map);
    	}
    	

    	public Integer call() throws Exception {
			try {
				dfsBlue(initialState);
			} catch (Result e) {
				throw new Exception(e);
			}

			return id;
    	}


        private void dfsRed(State s) throws Result {
        	localPinks.put(s, new Boolean(true));
            for (State t : graph.post(s)) {
                if (localColors.hasColor(t, Color.CYAN)) {
                	throw new CycleFound();
                }
                if (! localPinks.get(t).booleanValue() && ! colors.hasColor(t, Color.RED)) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
            	synchronized(counts) { 
	            	int currentVal = counts.get(s).intValue();
	            	counts.put(s, new Integer(currentVal - 1));
            	}
            	while (counts.get(s).intValue() > 0 && busy) {
            		try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
//						e.printStackTrace();
					}
            	}
            }

            synchronized (colors) {
            	colors.color(s, Color.RED);
            }
        	localPinks.put(s, new Boolean(false));
        }


        private void dfsBlue(State s) throws Result {
            localColors.color(s, Color.CYAN);
            
            for (State t : graph.post(s)) {
                if (localColors.hasColor(t, Color.WHITE) && ! colors.hasColor(t, Color.RED)) {
                    dfsBlue(t);
                }
            }
            
            if (s.isAccepting()) {
            	synchronized (counts) {
	            	Integer c = counts.get(s);
	           		counts.put(s, new Integer(c.intValue() + 1));
            	}
            	
                dfsRed(s);
            }
            
            localColors.color(s, Color.BLUE);
        }
    	
    }


    public NNDFS(Graph graph, Map<State, Color> colorStore) {
        this.graph = graph;
        this.colors = new Colors(colorStore);
        this.counts = new IntegerHashMap<State>(new Integer(0));
    }


    public void init() {}


    private void nndfs(State s, int nrOfThreads) throws Result {
    	ExecutorService ex = Executors.newFixedThreadPool(nrOfThreads);
    	CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(ex);
    	
    	boolean foundCycle = false;
    	busy = true;
    	
    	for (int i = 0; i < nrOfThreads; i++) {
    		cs.submit(new Bird(s, i));
    	}
    	
    	for (int i = 0; i < nrOfThreads; i++) {
			try {
				cs.take().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				foundCycle = true;
				busy = false;
				break;
			}
    	}
    	
    	ex.shutdownNow();
		
        if (foundCycle) {
        	throw new CycleFound();
        } else {
        	throw new NoCycleFound();
        }
    }


    public void ndfs() throws Result {
        nndfs(graph.getInitialState(), 10);
    }

}

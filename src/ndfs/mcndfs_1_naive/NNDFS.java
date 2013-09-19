package ndfs.mcndfs_1_naive;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    
    private int nrOfThreads = 1;
    

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

            boolean tRed;
            List<State> post;
            
        	synchronized (graph) {
        		post = graph.post(s);
        	}
    		Collections.shuffle(post);
    		
            for (State t : post) {
                if (localColors.hasColor(t, Color.CYAN)) {
                	throw new CycleFound();
                }

            	synchronized (colors) {
            		tRed = colors.hasColor(t, Color.RED);
            	}
                if (! localPinks.get(t).booleanValue() && ! tRed) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
            	synchronized(counts) {
	            	int currentVal = counts.get(s).intValue();
	            	counts.put(s, new Integer(currentVal - 1));
            	}
            	while (counts.get(s).intValue() > 0) {
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
            
            boolean tRed;
            List<State> post;
            
        	synchronized (graph) {
        		post = graph.post(s);
        	}
    		Collections.shuffle(post);
    		
            for (State t : post) {
            	synchronized (colors) {
            		tRed = colors.hasColor(t, Color.RED);
            	}
                if (localColors.hasColor(t, Color.WHITE) && ! tRed) {
                    dfsBlue(t);
                }
            }
            
            if (s.isAccepting()) {
            	synchronized (counts) {
	            	Integer c = counts.get(s);
	           		counts.put(s, c.intValue() + 1);
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


    public void init(int nrOfThreads) {
    	this.nrOfThreads = nrOfThreads;
    }

    private void nndfs(State s) throws Result {
    	ExecutorService ex = Executors.newFixedThreadPool(this.nrOfThreads);
    	CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(ex);
    	
    	boolean foundCycle = false;
    	
    	for (int i = 0; i < this.nrOfThreads; i++) {
    		cs.submit(new Bird(s, i));
    	}
    	
    	for (int i = 0; i < this.nrOfThreads; i++) {
			try {
				cs.take().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				foundCycle = true;
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
        nndfs(graph.getInitialState());
    }

}

package ndfs.mcndfs_1_naive;

import java.util.HashMap;
import java.util.Map;

import graph.State;
import graph.Graph;
import ndfs.NDFS;
import ndfs.Result;
import ndfs.CycleFound;
import ndfs.NoCycleFound;

public class NNDFS implements NDFS {


    private Graph graph;
    private Colors colors;
    private Map<State, Integer> counts;
    
    int threadsBusy;
    boolean foundCycle;
    Result result;


    class Bird implements Runnable {

    	int id;
    	private Thread th;
    	private State initialState;
    	private Colors localColors;
        private Map<State, Boolean> localPinks;


    	Bird(State s, int i) {
    		this.id = i;
    		this.initialState = s;
    		this.localPinks = new BooleanHashTable<State, Boolean>(new Boolean(false));

            Map<State, ndfs.mcndfs_1_naive.Color> map = new HashMap<State, ndfs.mcndfs_1_naive.Color>();
            this.localColors = new Colors(map);
    		
            this.th = new Thread(this, "Bird thread");
            this.th.start();
    	}
    	

    	@Override
    	public void run() {
			try {
				dfsBlue(initialState);
			} catch (Result e) {
				//
			} finally {
				decreaseCounter();
			}
    	}


        private void dfsRed(State s) throws Result {
    		System.out.println("red " + id);
        	localPinks.put(s, new Boolean(true));
            for (State t : graph.post(s)) {
                if (localColors.hasColor(t, Color.CYAN)) {
                	if (!foundCycle) {
                		foundCycle = true;
                    	reportResult(new CycleFound(id));
                	}
                    throw new CycleFound(id);
                }
                if (! localPinks.get(t).booleanValue() && ! colors.hasColor(t, Color.RED)) {
                    dfsRed(t);
                }
            }

            if (s.isAccepting()) {
            	// todo: convert to custom hashmap
            	Integer c = counts.get(s);
            	int v = 0;
            	if (c != null) {
            		v = Math.max(0, c.intValue());
            	}
            	counts.put(s, new Integer(v));
            	while (counts.get(s).intValue() > 0) {
            		try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
            		System.out.println(counts.get(s).intValue());
            	}
            }

            colors.color(s, Color.RED);
        	localPinks.put(s, new Boolean(false));
            
        }


        private void dfsBlue(State s) throws Result {
    		System.out.println("blue " + id);
            localColors.color(s, Color.CYAN);
            
            for (State t : graph.post(s)) {
                if (localColors.hasColor(t, Color.WHITE) && ! colors.hasColor(t, Color.RED)) {
                    dfsBlue(t);
                }
            }
            
            if (s.isAccepting()) {
            	Integer c = counts.get(s);
            	if (c != null) {
            		counts.put(s, new Integer(c.intValue() + 1));
            	} else {
            		counts.put(s, new Integer(1));
            	}
                dfsRed(s);
            }
            
            localColors.color(s, Color.BLUE);
        }
    	
    }
    
    public void reportResult(Result e) {
    	result = e;
    }
    
    public void decreaseCounter() {
    	threadsBusy--;
    }  


    public NNDFS(Graph graph, Map<State, Color> colorStore) {
        this.graph = graph;
        this.colors = new Colors(colorStore);
        this.counts = new HashMap<State, Integer>();
    }


    public void init() {}


    private void nndfs(State s, int nrOfThreads) throws Result {
    	threadsBusy = 0;
    	foundCycle = false;
    	
    	result = new NoCycleFound();
    	
    	for (int i = 0; i < nrOfThreads; i++) {
    		threadsBusy++;
    		new Bird(s, i);
    		
    	}
    	while (threadsBusy != 0) {
    		if (foundCycle) {
    			throw result;
    		}
	    	try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	
        throw result;
    }


    public void ndfs() throws Result {
        nndfs(graph.getInitialState(), 10);
    }

}

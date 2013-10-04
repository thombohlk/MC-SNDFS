package ndfs.nndfs;



import java.util.Map;

import graph.State;
import graph.Graph;
import helperClasses.Color;
import helperClasses.Colors;
import helperClasses.logger.Logger;
import ndfs.NDFS;
import ndfs.Result;
import ndfs.CycleFound;
import ndfs.NoCycleFound;



public class NNDFS implements NDFS {



    protected Graph graph;
    protected Colors colors; 
    protected Logger logger;



    public NNDFS(Graph graph, Map<State, Color> colorStore) {
        this.graph = graph;
        this.colors = new Colors(colorStore);
    }



    public void init(int nrOfTreads) {
    	// do nothing
    }



    protected void dfsRed(State s) throws Result {
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.CYAN)) {
            	Result result = new CycleFound();
            	result.setLogger(logger);
                throw result;
            }
            else if (colors.hasColor(t, Color.BLUE)) {
                colors.color(t, Color.RED);
                dfsRed(t);
            }
        }
    }


    protected void dfsBlue(State s) throws Result {
        colors.color(s, Color.CYAN);
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.WHITE)) {
                dfsBlue(t);
            }
        }
        if (s.isAccepting()) {
            dfsRed(s);
            colors.color(s, Color.RED);
        }
        else {
            colors.color(s, Color.BLUE);
        }
    }
    

    protected void nndfs(State s) throws Result {
        dfsBlue(s);

    	Result result = new NoCycleFound();
    	result.setLogger(logger);
        throw result;
    }


    public void ndfs() throws Result {
        nndfs(graph.getInitialState());
    }



	@Override
	public void tearDown() {
		// TODO Auto-generated method stub
		
	}
}

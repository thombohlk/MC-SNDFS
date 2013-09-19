package ndfs.nndfs;



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



    public NNDFS(Graph graph, Map<State, Color> colorStore) {
        this.graph = graph;
        this.colors = new Colors(colorStore);
    }



    public void init(int nrOfTreads) {
    	// do nothing
    }



    private void dfsRed(State s) throws Result {
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.CYAN)) {
                throw new CycleFound();
            }
            else if (colors.hasColor(t, Color.BLUE)) {
                colors.color(t, Color.RED);
                dfsRed(t);
            }
        }
    }


    private void dfsBlue(State s) throws Result {
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
    

    private void nndfs(State s) throws Result {
        dfsBlue(s);
        throw new NoCycleFound();
    }


    public void ndfs() throws Result {
        nndfs(graph.getInitialState());
    }
}

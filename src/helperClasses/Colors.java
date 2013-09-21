package helperClasses;



import java.util.Map;

import graph.State;



public class Colors {



    private Map<State, Color> map;
    


    public Colors(Map<State, Color> map) {
        this.map = map;
    }


    public boolean hasColor(State state, Color color) {
        if (color == Color.WHITE) {
            return map.get(state) == null;
        }
        else {
            return map.get(state) == color;
        }
    }


    public void color(State state, Color color) {
        map.put(state, color);
    }
}

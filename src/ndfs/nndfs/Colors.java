package ndfs.nndfs;



import java.util.Map;

import graph.State;



class Colors {



    private Map<State, Color> map;
    


    Colors(Map<State, Color> map) {
        this.map = map;
    }


    boolean hasColor(State state, Color color) {
        if (color == Color.WHITE) {
            return map.get(state) == null;
        }
        else {
            return map.get(state) == color;
        }
    }


    void color(State state, Color color) {
        map.put(state, color);
    }
}

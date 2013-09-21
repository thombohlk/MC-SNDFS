package ndfs;



import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import graph.Graph;
import graph.State;



public class NDFSFactory {



    public static NDFS createNNDFS(Graph graph, 
            Map<State, helperClasses.Color> map) {
        return new ndfs.nndfs.NNDFS(graph, map);
    }


    public static NDFS createMCNDFS(String version, File file) throws InstantiationException {
        try {
        	  String name = "ndfs.mcndfs_" + version + ".NNDFS";
        	  Class<?> cl = Class.forName(name);
        	  Constructor<?> co = cl.getConstructor(new Class<?>[] { File.class });
        	  return (NDFS) co.newInstance(new Object[] { file });
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw new InstantiationException("Unkown version: " + version);
		}
    }
}
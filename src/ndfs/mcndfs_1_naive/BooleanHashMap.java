package ndfs.mcndfs_1_naive;

import java.util.HashMap;
import java.util.Hashtable;

public class BooleanHashMap<State> extends HashMap<State, Boolean> {
	protected Boolean defaultValue;
	
	public BooleanHashMap(Boolean b) {
		this.defaultValue = b;
	}

	@Override
	public Boolean get(Object k) {
		Boolean b = super.get(k);
		return ((b == null) && !this.containsKey(k)) ? this.defaultValue : b;
	}
}

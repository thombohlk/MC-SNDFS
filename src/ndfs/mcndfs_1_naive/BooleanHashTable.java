package ndfs.mcndfs_1_naive;

import java.util.Hashtable;

public class BooleanHashTable<State, Boolean> extends Hashtable<State, Boolean> {
	protected Boolean defaultValue;
	
	public BooleanHashTable(Boolean b) {
		this.defaultValue = b;
	}

	@Override
	public Boolean get(Object k) {
		Boolean b = super.get(k);
		return ((b == null) && !this.containsKey(k)) ? this.defaultValue : b;
	}
}

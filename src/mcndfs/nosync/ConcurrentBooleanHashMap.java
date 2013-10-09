package mcndfs.nosync;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentBooleanHashMap<State> extends ConcurrentHashMap<State, AtomicBoolean> {
	protected AtomicBoolean defaultValue;
	
	public ConcurrentBooleanHashMap(AtomicBoolean b) {
		this.defaultValue = b;
	}

	@Override
	public AtomicBoolean get(Object k) {
		AtomicBoolean b = super.get(k);
		return ((b == null) || !this.containsKey(k)) ? this.defaultValue : b;
	}
}

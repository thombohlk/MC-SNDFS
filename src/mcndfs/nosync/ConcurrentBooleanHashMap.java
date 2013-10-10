package mcndfs.nosync;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentBooleanHashMap<State> extends ConcurrentHashMap<State, AtomicBoolean> {
	
	private static final long serialVersionUID = 1L;
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

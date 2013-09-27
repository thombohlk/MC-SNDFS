package ndfs.mcndfs_nosync;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentIntegerHashMap<State> extends ConcurrentHashMap<State, AtomicInteger> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected AtomicInteger defaultValue;
	
	public ConcurrentIntegerHashMap(AtomicInteger i) {
		this.defaultValue = i;
	}

	@Override
	public AtomicInteger get(Object k) {
		AtomicInteger i = super.get(k);
		return ((i == null) && !this.containsKey(k)) ? this.defaultValue : i;
	}

	@Override
	public AtomicInteger put(State key, AtomicInteger i) {
		return super.put(key, new AtomicInteger(Math.max(0, i.get())));
	}
}

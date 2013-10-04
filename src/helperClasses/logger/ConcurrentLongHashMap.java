package helperClasses.logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrentLongHashMap<State> extends ConcurrentHashMap<Long, AtomicLong> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected long defaultValue;
	
	public ConcurrentLongHashMap(long i) {
		this.defaultValue = i;
	}

	public AtomicLong get(Long k) {
		AtomicLong i = super.get(k);
		if ((i == null) || !this.containsKey(k)) {
			i = new AtomicLong(this.defaultValue);
			this.put(k, i);
		}
		return i;
	}

	@Override
	public AtomicLong put(Long key, AtomicLong i) {
		return super.put(key, new AtomicLong(Math.max(0, i.get())));
	}
}

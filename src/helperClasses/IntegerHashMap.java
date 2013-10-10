package helperClasses;

import java.util.HashMap;

public class IntegerHashMap<State> extends HashMap<State, Integer> {
	
	private static final long serialVersionUID = 1L;
	protected Integer defaultValue;
	
	public IntegerHashMap(Integer i) {
		this.defaultValue = i;
	}

	@Override
	public Integer get(Object k) {
		Integer i = super.get(k);
		return ((i == null) && !this.containsKey(k)) ? this.defaultValue : i;
	}

	@Override
	public Integer put(State key, Integer value) {
		return super.put(key, Math.max(0, value));
	}
}

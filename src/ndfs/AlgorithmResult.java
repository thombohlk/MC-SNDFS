package ndfs;

public class AlgorithmResult extends Result {

	private long duration;
	private Result result;
	
	public AlgorithmResult(String message, long duration, Result result) {
		super(message);
		
		this.duration = duration;
		this.result = result;
	}
	
	public long getDuration() {
		return this.duration;
	}
	
	public Result getResult() {
		return this.result;
	}

}

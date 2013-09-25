package ndfs;

public class AlgorithmResult extends Result {

	private long duration;
	private Result result;
	private String version;
	
	public AlgorithmResult(String message, long duration, Result result, String version) {
		super(message);
		
		this.duration = duration;
		this.result = result;
		this.version = version;
	}
	
	public long getDuration() {
		return this.duration;
	}
	
	public Result getResult() {
		return this.result;
	}
	
	public String getVersion() {
		return this.version;
	}

}

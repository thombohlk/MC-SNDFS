package ndfs;

public class AlgorithmResult extends Result implements Comparable {

	private long duration;
	private Result result;
	private String version;
	
	public AlgorithmResult(Result result, long duration, String version) {
		super(result.getMessage());
		
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

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return (int) (this.duration - ((AlgorithmResult) o).duration);
	}

}

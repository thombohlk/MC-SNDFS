package ndfs;

import helperClasses.logger.Logger;

public class AlgorithmResult extends Result implements Comparable<AlgorithmResult> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public Result getResult() {
		return this.result;
	}
	
	public String getVersion() {
		return this.version;
	}

	@Override
	public int compareTo(AlgorithmResult o) {
		// TODO Auto-generated method stub
		return (int) (this.duration - ((AlgorithmResult) o).duration);
	}

	@Override
	public Logger getLogger() {
		return (this.logger == null ? this.result.getLogger() : this.logger);
	}
	
	public void printUserFriendly() {
		System.out.println(this.version + " took " + this.duration + "ms");
		System.out.println(this.result.getMessage());
		if (super.logger != null) {
//			super.logger.printUserFriendly();
		}
	}

}

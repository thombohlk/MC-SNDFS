package ndfs;

import helperClasses.logger.GraphAnalysisDataObject;

import java.io.File;

public class AlgorithmResult extends Result implements Comparable<AlgorithmResult> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long duration;
	private Result result;
	private String version;
	private File file;
	private int nrOfThreads;
	
	public AlgorithmResult(Result result, long duration, String version) {
		super(result.getMessage());
		
		this.duration = duration;
		this.result = result;
		this.version = version;
	}
	
	public void printUserFriendly() {
		System.out.println(this.version + " took " + this.duration + "ms");
		System.out.println(this.result.getMessage());
	}

	@Override
	public int compareTo(AlgorithmResult o) {
		return (int) (this.duration - ((AlgorithmResult) o).duration);
	}

	@Override
	public GraphAnalysisDataObject getAnalysisData() {
		return (this.analysisData == null ? this.result.getAnalysisData() : this.analysisData);
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

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public int getNrOfThreads() {
		return nrOfThreads;
	}

	public void setNrOfThreads(int nrOfThreads) {
		this.nrOfThreads = nrOfThreads;
	}

	public String getFileName() {
		return this.file.getName();
	}

}

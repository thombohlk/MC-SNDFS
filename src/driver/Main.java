package driver;

import helperClasses.Global;
import helperClasses.StringArray;

import java.io.File;
import java.io.FileNotFoundException;

import ndfs.AlgorithmResult;

public class Main {

	private static class ArgumentException extends Exception {
		private static final long serialVersionUID = 1L;

		ArgumentException(String message) {
			super(message);
		}
	};

	private static void printUsage() {
		System.out.println("Usage: bin/ndfs <file> <version> <nrWorkers>");
		System.out.println("  where");
		System.out.println("    <file> is a Promela file (.prom)");
		System.out.println("    <version> is one of {seq}");
	}

	private static void dispatch(File file, String version, int nrOfThreads)
			throws ArgumentException, FileNotFoundException,
			InstantiationException {

		try {
			if (version.equals("seq")) {
				if (nrOfThreads != 1) {
					throw new ArgumentException(
							"seq can only run with 1 worker");
				}
				Executor.runNDFS("seq", file, "none");
			} else if (version.matches(StringArray.implodeArray(
					Executor.availableVersions, "|"))) {
				Executor.runMCNDFS(version, file, nrOfThreads, "none");
			} else {
				throw new ArgumentException("Unkown version: " + version);
			}
		} catch (AlgorithmResult r) {
			// default output
			System.out.println(r.getMessage());
			System.out.printf("%s took %d ms\n", r.getVersion(),
					r.getDuration());
		}

	}

	private static void dispatchAnalysis(String fileArg, String versionArg,
			String nrOfThreadsArg) throws ArgumentException,
			FileNotFoundException, InstantiationException {
		Analyser analyser = new Analyser(fileArg, versionArg, nrOfThreadsArg);
		analyser.executeAnalysis();
	}

	public static void main(String[] argv) {
		try {
			if (argv.length < 3 || argv.length > 4)
				throw new ArgumentException("Wrong number of arguments");
			if (argv.length == 4) {
				if (argv[3].matches(StringArray.implodeArray(Analyser.MODES,
						"|"))) {
					String fileArg = argv[0];
					String versionArg = argv[1];
					String nrOfThreadsArg = argv[2];
					Global.MODE = argv[3];

					dispatchAnalysis(fileArg, versionArg, nrOfThreadsArg);
				} else {
					throw new ArgumentException(
							"The fourth argument should be either 'csv', 'csv_performance', 'user', 'user_performance' or 'heartbeat'.");
				}
			}
			if (argv.length == 3) {
				File file = new File(argv[0]);
				String version = argv[1];
				int nrOfThreads = new Integer(argv[2]);

				dispatch(file, version, nrOfThreads);
			}
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (ArgumentException e) {
			System.err.println(e.getMessage());
			printUsage();
		} catch (NumberFormatException e) {
			System.err.println(e.getMessage());
			printUsage();
		} catch (InstantiationException e) {
			System.err.println(e.getMessage());
			printUsage();
		}
	}
}

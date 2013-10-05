package driver;

import graph.State;
import helperClasses.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

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

	private static void dispatch(File file, String version, int nrOfThreads,
			String mode) throws ArgumentException, FileNotFoundException,
			InstantiationException {

		try {
			if (mode.matches("CSV|user")) {
				Analyser analyser = new Analyser();
				analyser.init(file, version, nrOfThreads, mode);
				analyser.makeComparison(3);
			} else if (version.equals("seq")) {
				if (nrOfThreads != 1) {
					throw new ArgumentException(
							"seq can only run with 1 worker");
				}
				Map<State, Color> map = new HashMap<State, Color>();
				Executor.runNDFS("seq", map, file, mode);
			} else if (version
					.matches("naive|extended|optimalPermutation|optimalPermutation2|optimalPermutation3|lock|nosync")) {
				Executor.runMCNDFS(version, file, nrOfThreads, mode);
			} else {
				throw new ArgumentException("Unkown version: " + version + (mode.equals("log") ? "_log" : "?"));
			}
		} catch (AlgorithmResult r) {
			if (mode.equals("none")) {
				// default output
				System.out.println(r.getMessage());
				System.out.printf("%s took %d ms\n", r.getVersion(),
						r.getDuration());
			} else if (mode.equals("performance")) {
				// print performance
				System.out.println(r.getDuration());
			} else {
				// print logs
				System.out.print(r.getLogger().getResultsCSV());
			}
		}

	}

	public static void main(String[] argv) {
		try {
			String mode = "none";
			if (argv.length < 3 || argv.length > 4)
				throw new ArgumentException("Wrong number of arguments");
			if (argv.length == 4) {
				if (argv[3].matches("CSV|user")) {
					mode = argv[3];
				} else {
					throw new ArgumentException("The fourth argument should be either 'log' or 'performance'.");
				}
			}
			File file = new File(argv[0]);
			String version = argv[1];
			int nrOfThreads = new Integer(argv[2]);

			dispatch(file, version, nrOfThreads, mode);
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

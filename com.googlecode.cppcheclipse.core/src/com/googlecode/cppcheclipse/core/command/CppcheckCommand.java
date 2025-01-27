package com.googlecode.cppcheclipse.core.command;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.googlecode.cppcheclipse.core.Checker;
import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.IConsole;
import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.core.IProgressReporter;
import com.googlecode.cppcheclipse.core.Problem;
import com.googlecode.cppcheclipse.core.Symbol;
import com.googlecode.cppcheclipse.core.Symbols;
import com.googlecode.cppcheclipse.core.utils.FileUtils;

public class CppcheckCommand extends AbstractCppcheckCommand {

	private final static String DELIMITER = ";";
	private final static String ERROR_FORMAT = "{file}" + DELIMITER + "{line}"
			+ DELIMITER + "{severity}" + DELIMITER + "{id}" + DELIMITER
			+ "{message}";
	private final static String[] DEFAULT_ARGUMENTS = { "--template="
			+ ERROR_FORMAT };

	/**
	 * pattern recognizes "2/2 files checked 100% done"
	 */
	private final static Pattern PROGRESS_PATTERN = Pattern
			.compile("^((\\d)*)/(\\d)* files checked (\\d)*% done");

	/**
	 * pattern recognizes "Checking src/test.1.cpp..."
	 */
	private final static Pattern FILE_PATTERN = Pattern
			.compile("^Checking (.*)...");

	private static final String CPPCHECK_PROJ_STRING = ".cppcheck";

	private final Collection<String> arguments;
	private String advancedArguments;


	private void addPremiumChecks(IPreferenceStore settingsStore) {
		if (settingsStore.getBoolean(IPreferenceConstants.P_PREMIUM_BUG_HUNTING)) {
			arguments.add("--premium=bughunting");
		}
		if (settingsStore.getBoolean(IPreferenceConstants.P_PREMIUM_MISRA_C_12)) {
			arguments.add("--premium=misra-c-2012");
		}
		if (settingsStore.getBoolean(IPreferenceConstants.P_PREMIUM_MISRA_C_23)) {
			arguments.add("--premium=misra-c-2023");
		}
		if (settingsStore.getBoolean(IPreferenceConstants.P_PREMIUM_MISRA_CPP_08)) {
			arguments.add("--premium=misra-c++-2008");
		}
		if (settingsStore.getBoolean(IPreferenceConstants.P_PREMIUM_MISRA_CPP_23)) {
			arguments.add("--premium=misra-c++-2023");
		}
		if (settingsStore.getBoolean(IPreferenceConstants.P_PREMIUM_CERT_C)) {
			arguments.add("--premium=cert-c-2016");
		}
		if (settingsStore.getBoolean(IPreferenceConstants.P_PREMIUM_CERT_CPP)) {
			arguments.add("--premium=cert-c++-2016");
		}
		if (settingsStore.getBoolean(IPreferenceConstants.P_PREMIUM_AUTOSAR)) {
			arguments.add("--premium=autosar");
		}
	}

	/**
	 * For testing purposes either use interfaces or simple types as parameters.
	 * No dependency to Eclipse classes allowed.
	 * 
	 * @param console
	 * @param settingsStore
	 *            either workspace or project settings
	 * @param advancedSettingsStore
	 *            always project settings
	 * @param userIncludePaths
	 * @param systemIncludePaths
	 * @param symbols
	 */
	public CppcheckCommand(IConsole console, String binaryPath,
			IPreferenceStore settingsStore,
			IPreferenceStore advancedSettingsStore,
			Collection<File> userIncludePaths,
			Collection<File> systemIncludePaths, Symbols symbols) {
		super(console, DEFAULT_ARGUMENTS, binaryPath);
		arguments = new LinkedList<String>();

		if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_ALL)) {
			arguments.add("--enable=all");
		} else {

			List<String> enableFlags = new LinkedList<String>();
			if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_STYLE)) {
				enableFlags.add("style");
			}

			if (settingsStore
					.getBoolean(IPreferenceConstants.P_CHECK_INFORMATION)) {
				enableFlags.add("information");
			}

			if (settingsStore
					.getBoolean(IPreferenceConstants.P_CHECK_PERFORMANCE)) {
				enableFlags.add("performance");
			}

			if (settingsStore
					.getBoolean(IPreferenceConstants.P_CHECK_PORTABILITY)) {
				enableFlags.add("portability");
			}

			if (settingsStore
					.getBoolean(IPreferenceConstants.P_CHECK_MISSING_INCLUDE)) {
				enableFlags.add("missingInclude");
			}

			// when unused-function check is on, -j is not available!
			boolean checkUnusedFunctions = settingsStore
					.getBoolean(IPreferenceConstants.P_CHECK_UNUSED_FUNCTIONS);
			if (checkUnusedFunctions) {
				enableFlags.add("unusedFunction");
			} else {
				arguments.add("-j");
				arguments.add(String.valueOf(settingsStore
						.getInt(IPreferenceConstants.P_NUMBER_OF_THREADS)));
			}

			if (!enableFlags.isEmpty()) {
				arguments.add("--enable=" + Joiner.on(",").join(enableFlags));
			}
		}

		String projectFile = settingsStore.getString(IPreferenceConstants.P_PROJECT_FILE);
		if (!projectFile.isEmpty()) {
			arguments.add("--project=" + projectFile);
			arguments.add("--file-filter=-");
		} else {
			arguments.add("--file-list=-");
		}

		if (projectFile.isEmpty() || !projectFile.endsWith(CPPCHECK_PROJ_STRING)) {
			addPremiumChecks(settingsStore);
		}

		if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_VERBOSE)) {
			arguments.add("--verbose");
		}

		if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_INCONCLUSIVE)) {
			arguments.add("--inconclusive");
		}

		if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_FORCE)) {
			arguments.add("--force");
		}

		if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_DEBUG)) {
			arguments.add("--debug");
		}

		if (settingsStore
				.getBoolean(IPreferenceConstants.P_USE_INLINE_SUPPRESSIONS)) {
			arguments.add("--inline-suppr");
		}

		// which target platform is used?
		String targetPlatform = settingsStore
				.getString(IPreferenceConstants.P_TARGET_PLATFORM);
		if (!targetPlatform.isEmpty()) {
			arguments.add("--platform=" + targetPlatform);
		}

		// which C language standard is used?
		String languageStandardC = settingsStore
				.getString(IPreferenceConstants.P_LANGUAGE_STANDARD_C);
		if (!languageStandardC.isEmpty()) {
			arguments.add("--std=" + languageStandardC);
		}

		// which C++ language standard is used?
		String languageStandardCpp = settingsStore
				.getString(IPreferenceConstants.P_LANGUAGE_STANDARD_CPP);
		if (!languageStandardCpp.isEmpty()) {
			arguments.add("--std=" + languageStandardCpp);
		}

		if (settingsStore
				.getBoolean(IPreferenceConstants.P_LANGUAGE_STANDARD_POSIX)) {
			arguments.add("--std=posix");
		}

		if (settingsStore
				.getBoolean(IPreferenceConstants.P_FOLLOW_SYSTEM_INCLUDES)) {
			for (File path : systemIncludePaths) {
				arguments.add("-I");
				arguments.add(path.toString());
			}
		}

		if (settingsStore
				.getBoolean(IPreferenceConstants.P_FOLLOW_USER_INCLUDES)) {
			for (File path : userIncludePaths) {
				arguments.add("-I");
				arguments.add(path.toString());
			}
		}

		// the symbols already contain all necessary symbols
		for (Symbol symbol : symbols) {
			arguments.add(symbol.toString());
		}

		// use advanced arguments
		advancedArguments = advancedSettingsStore.getString(
				IPreferenceConstants.P_ADVANCED_ARGUMENTS).trim();
		if (advancedArguments.length() == 0) {
			advancedArguments = null;
		}
	}

	public void run(Checker checker, IProgressReporter progressReporter,
			IProject project, List<IFile> files, IProgressMonitor monitor)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException, InterruptedException,
			ProcessExecutionException, CoreException, URISyntaxException {

		// convert list of files to filenames
		List<String> filenames = new LinkedList<String>();
		File projectFolder = project.getLocation().toFile();
		for (IFile file : files) {
			// make this also work with linked resources
			File absoluteFile = file.getLocation().toFile();

			// make file relative to project
			filenames.add(FileUtils.relativizeFile(projectFolder, absoluteFile)
					.toString());
		}
		// read filenames from stdin (separated by "\n")
		String input = Joiner.on("\n").join(filenames);
		console.println("=== Input stream for following process ===");
		console.println(input);
		console.println("=== End of input stream for process ==");

		setProcessInputStream(new ByteArrayInputStream(input.getBytes(DEFAULT_CHARSET)));

		setWorkingDirectory(projectFolder);
		CppcheckProcessResultHandler resultHandler = runInternal(
				arguments.toArray(new String[0]), advancedArguments,
				null);

		List<Problem> problems = new LinkedList<Problem>();
		try {
			while (resultHandler.isRunning()) {
				Thread.sleep(SLEEP_TIME_MS);
				if (monitor.isCanceled()) {
					watchdog.destroyProcess();
					throw new InterruptedException("Process manually killed");
				}

				// parse output
				parseResultLines(project, getErrorReader(), problems);

				if (!problems.isEmpty()) {
					// give out problems
					checker.reportProblems(problems);
					problems.clear();
				}

				// parse progress
				parseProgressLines(getOutputReader(), progressReporter);

				// don't use parsed lines twice
			}
			waitForExit(resultHandler, monitor);
		} finally {
			if (resultHandler.isRunning()) {
				// always destroy process if it is still running here
				watchdog.destroyProcess();
			}
		}
	}

	public static void parseResultLines(IProject project,
			BufferedReader reader, List<Problem> problems) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			try {
				problems.add(parseResult(line, project));
			} catch (IllegalArgumentException e) {
				CppcheclipsePlugin.logWarning("Problems parsing a result line",
						e);
			}
		}
	}

	public static Problem parseResult(String line, IProject project) {
		String[] lineParts = line.split(DELIMITER, 5);
		if (lineParts.length < 5) {
			throw new IllegalArgumentException("Not enough tokens in line '"
					+ line + "'. Expected 5 tokens but got " + lineParts.length);
		}

		/**
		 * line should have the following format
		 * <file>;<line>;<severity>;<id>;<message>
		 * 
		 * TODO: <file> and <line> might be empty!
		 */
		try {

			File filename;
			if (Strings.isNullOrEmpty(lineParts[0]) || "nofile".equals(lineParts[0])) {
				filename = null;
			} else {
				filename = new File(lineParts[0]);
			}
			// if line is empty set it to -1
			int lineNumber;
			if (Strings.isNullOrEmpty(lineParts[1]) || "0".equals(lineParts[1])) {
				lineNumber = -1;
			} else {
				lineNumber = Integer.parseInt(lineParts[1]);
			}
			String severity = lineParts[2];
			String id = lineParts[3];
			String message = lineParts[4];
			return new Problem(id, message, severity, filename, project,
					lineNumber);

		} catch (NumberFormatException e2) {
			throw new IllegalArgumentException(
					"Could not parse the second token in line: '" + line
							+ "' into an Integer", e2);
		}
	}

	public static void parseProgressLines(BufferedReader reader,
			IProgressReporter progressReporter) throws IOException {
		String line;
		String fileName = null;
		Integer fileNumber = null;

		while ((line = reader.readLine()) != null) {
			fileName = parseFilename(line);
			fileNumber = parseProgress(line);
			progressReporter.reportProgress(fileName, fileNumber);
		}
	}

	public static String parseFilename(String line) {
		Matcher fileMatcher = FILE_PATTERN.matcher(line);
		if (fileMatcher.matches()) {
			return fileMatcher.group(1);
		}
		return null;
	}

	public static Integer parseProgress(String line) {
		Matcher progressMatcher = PROGRESS_PATTERN.matcher(line);
		if (progressMatcher.matches()) {
			String fileNumber = progressMatcher.group(1);
			if (fileNumber != null) {
				return new Integer(fileNumber);
			}
		}
		return null;
	}
}

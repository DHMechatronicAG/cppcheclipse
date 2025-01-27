package com.googlecode.cppcheclipse.core;

/**
 * Constant definitions for plug-in preferences
 */
public interface IPreferenceConstants {
	public static final String P_PREMIUM = "premium";
	public static final String P_RUN_ON_BUILD = "runOnBuild";
	public static final String P_PROBLEMS = "problems";
	public static final String P_USE_PARENT_SUFFIX = "_useParentScope";
	public static final String P_BINARY_PATH = "binaryPath";
	public static final String P_CHECK_STYLE = "checkStyle";
	public static final String P_CHECK_ALL = "checkAllIssues";
	public static final String P_CHECK_PERFORMANCE = "checkPerformance";
	public static final String P_CHECK_PORTABILITY = "checkPortability";
	public static final String P_CHECK_INFORMATION = "checkInformation";
	public static final String P_CHECK_MISSING_INCLUDE = "checkMissingInclude";
	public static final String P_CHECK_VERBOSE = "checkVerbose";
	public static final String P_CHECK_INCONCLUSIVE = "checkInconclusive";
	public static final String P_CHECK_FORCE = "checkForce";
	public static final String P_CHECK_DEBUG = "checkDebug";
	public static final String P_PREMIUM_BUG_HUNTING = "bughunting";
	public static final String P_PREMIUM_MISRA_C_12 = "misrac12";
	public static final String P_PREMIUM_MISRA_C_23 = "misrac23";
	public static final String P_PREMIUM_MISRA_CPP_08 = "misracpp08";
	public static final String P_PREMIUM_MISRA_CPP_23 = "misracpp23";
	public static final String P_PREMIUM_CERT_C = "certc";
	public static final String P_PREMIUM_CERT_CPP = "certcpp";
	public static final String P_PREMIUM_AUTOSAR = "autosar";
	public static final String P_USE_INLINE_SUPPRESSIONS = "useInlineSuppressions";
	public static final String P_CHECK_UNUSED_FUNCTIONS = "checkUnusedFunctions";
	public static final String P_FOLLOW_SYSTEM_INCLUDES = "followSystemIncludes";
	public static final String P_FOLLOW_USER_INCLUDES = "followUserIncludes";
	public static final String P_NUMBER_OF_THREADS = "numberOfThreads";
	public static final String P_PROJECT_FILE = "projectFile";
	public static final String P_AUTOMATIC_UPDATE_CHECK_INTERVAL = "automaticUpdateCheckInterval";
	public static final String P_USE_AUTOMATIC_UPDATE_CHECK = "automaticUpdateCheck";
	public static final String P_LAST_UPDATE_CHECK = "lastUpdateCheck";
	public static final String P_SUPPRESSIONS = "suppressions";
	public static final String P_ADVANCED_ARGUMENTS = "advancedArgument";
	public static final String P_SYMBOLS = "symbols";
	public static final String P_RESTRICT_CONFIGURATION_CHECK = "restrictConfigurations";
	public static final String P_INCLUDE_CDT_SYSTEM_SYMBOLS = "includeCDTSystemSymbols";
	public static final String P_INCLUDE_CDT_USER_SYMBOLS = "includeCDTUserSymbols";
	
	// these are the page id's of the preferences but also the prefixes for the use parent properties
	public static final String PROBLEMS_PAGE_ID = "com.googlecode.cppcheclipse.ui.ProblemsPreferencePage";
	public static final String SETTINGS_PAGE_ID = "com.googlecode.cppcheclipse.ui.SettingsPreferencePage";
	public static final String P_LANGUAGE_STANDARD = "languageStandard";
	public static final String P_TARGET_PLATFORM = "targetPlatform";
	public static final String P_LANGUAGE_STANDARD_POSIX = "languageStandardPosix";
	public static final String P_LANGUAGE_STANDARD_C = "languageStandardC";
	public static final String P_LANGUAGE_STANDARD_CPP = "languageStandardCpp";
}

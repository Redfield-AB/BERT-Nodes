/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.bert.prefs;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.knime.conda.Conda;
import org.knime.conda.prefs.CondaPreferences;
import org.knime.core.node.NodeLogger;
import org.knime.python2.config.CondaEnvironmentsConfig;
import org.knime.python2.config.ManualEnvironmentsConfig;
import org.knime.python2.config.PythonConfig;
import org.knime.python2.config.PythonEnvironmentType;
import org.knime.python2.config.PythonEnvironmentTypeConfig;
import org.knime.python2.prefs.PythonPreferences;

/**
 * Preferences initializer.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class BertPreferenceInitializer extends AbstractPreferenceInitializer {
	@SuppressWarnings("unused")
	private static final NodeLogger LOGGER = NodeLogger.getLogger(BertPreferenceInitializer.class);

	/**
	 * The cache directory setting key
	 */
	static final String PREF_CACHE_DIR = "redfield.bert.cachedir";

	@Override
	public void initializeDefaultPreferences() {
		saveToDefault(//
				BertPreferences.createBundledEnvConfig(), //
				BertPreferences.createEnvTypeConfig(), //
				BertPreferences.createCondaEnvConfig(), //
				BertPreferences.createManualEnvConfig(), //
				BertPreferences.createCacheDirConfig()//
		);
	}

	private static void saveToDefault(final PythonConfig... configs) {
		for (var config : configs) {
			config.saveConfigTo(BertPreferences.DEFAULT);
		}
	}

	static PythonEnvironmentTypeConfig getDefaultPythonEnvironmentTypeConfig() {
		return new PythonEnvironmentTypeConfig(PythonPreferences.getEnvironmentTypePreference());
	}

	static CondaEnvironmentsConfig getDefaultCondaEnvironmentsConfig() {
		return (CondaEnvironmentsConfig) PythonPreferences.getPythonEnvironmentsConfig(PythonEnvironmentType.CONDA);
	}

	static ManualEnvironmentsConfig getDefaultManualEnvironmentsConfig() {
		return (ManualEnvironmentsConfig) PythonPreferences.getPythonEnvironmentsConfig(PythonEnvironmentType.MANUAL);
	}

	static boolean isCondaConfigured() {
		try {
			final var condaDir = CondaPreferences.getCondaInstallationDirectory();
			final var conda = new Conda(condaDir);
			conda.testInstallation();
			return true;
		} catch (IOException ex) { // NOSONAR: we handle the exception by returning false, no need to rethrow
			return false;
		}
	}
}

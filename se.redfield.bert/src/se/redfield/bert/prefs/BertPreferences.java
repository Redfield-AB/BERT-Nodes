/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.bert.prefs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.knime.core.node.NodeLogger;
import org.knime.python2.PythonCommand;
import org.knime.python2.config.BundledCondaEnvironmentConfig;
import org.knime.python2.config.CondaEnvironmentsConfig;
import org.knime.python2.config.ManualEnvironmentsConfig;
import org.knime.python2.config.PythonConfig;
import org.knime.python2.config.PythonConfigStorage;
import org.knime.python2.config.PythonEnvironmentType;
import org.knime.python2.config.PythonEnvironmentTypeConfig;
import org.knime.python2.config.PythonEnvironmentsConfig;
import org.knime.python2.prefs.PreferenceStorage;
import org.knime.python2.prefs.PreferenceWrappingConfigStorage;

/**
 * Convenience front-end of the BERT preferences.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class BertPreferences {

	private static final String PLUGIN_ID = "se.redfield.bert";

	private static final PreferenceStorage DEFAULT_SCOPE_PREFS = new PreferenceStorage(PLUGIN_ID,
			DefaultScope.INSTANCE);

	private static final PreferenceStorage CURRENT_SCOPE_PREFS = new PreferenceStorage(PLUGIN_ID,
			InstanceScope.INSTANCE, DefaultScope.INSTANCE);

	private static final String DEFAULT_CACHE_DIR = System.getProperty("java.io.tmpdir") + File.separator
			+ "bert-cache";

	/**
	 * Accessed by preference page.
	 */
	static final PythonConfigStorage CURRENT = new PreferenceWrappingConfigStorage(CURRENT_SCOPE_PREFS);

	/**
	 * Accessed by preference page and preferences initializer.
	 */
	static final PythonConfigStorage DEFAULT = new PreferenceWrappingConfigStorage(DEFAULT_SCOPE_PREFS);

	static final String BUNDLED_ENV_ID = "se_redfield_bert";

	private static PythonEnvironmentType getEnvironmentTypePreference() {
		var config = createAndLoadCurrent(PythonEnvironmentTypeConfig::new);
		return PythonEnvironmentType.fromId(config.getEnvironmentType().getStringValue());
	}

	private static <T extends PythonConfig> T createAndLoadCurrent(final Supplier<T> supplier) {
		var config = supplier.get();
		config.loadConfigFrom(CURRENT);
		return config;
	}

	static BundledCondaEnvironmentConfig createBundledEnvConfig() {
		return new BundledCondaEnvironmentConfig(BUNDLED_ENV_ID);
	}

	static PythonEnvironmentTypeConfig createEnvTypeConfig() {
		return new PythonEnvironmentTypeConfig(PythonEnvironmentType.BUNDLED);
	}

	static CondaEnvironmentsConfig createCondaEnvConfig() {
		return new CondaEnvironmentsConfig();
	}

	static ManualEnvironmentsConfig createManualEnvConfig() {
		return new ManualEnvironmentsConfig();
	}

	static StringPythonConfig createCacheDirConfig() {
		return new StringPythonConfig("cachedir", DEFAULT_CACHE_DIR);
	}

	private static PythonEnvironmentsConfig getCurrentEnvironmentConfig() {
		var envType = getEnvironmentTypePreference();
		switch (envType) {
		case BUNDLED:
			return createAndLoadCurrent(BertPreferences::createBundledEnvConfig);
		case CONDA:
			return createAndLoadCurrent(BertPreferences::createCondaEnvConfig);
		case MANUAL:
			return createAndLoadCurrent(BertPreferences::createManualEnvConfig);
		default:
			throw new IllegalStateException("Unknown environment type encountered: " + envType);
		}
	}

	/**
	 * @return the PythonCommand configured on the preference page
	 */
	public static PythonCommand getPythonCommandPreference() {
		return getCurrentEnvironmentConfig()//
				.getPython3Config()//
				.getPythonCommand();
	}

	/**
	 * @return the absolute path to the cache directory
	 */
	public static String getCacheDir() {
		var cacheDir = createAndLoadCurrent(BertPreferences::createCacheDirConfig).getValue();
		try {
			Files.createDirectories(Path.of(cacheDir));
		} catch (IOException e) {
			// it's fine if it can't be created, then the user will just have to choose a different one
			NodeLogger.getLogger(BertPreferences.class).error("Failed to create BERT cache directory.", e);
		}
		return cacheDir;
	}

	private BertPreferences() {
	}
}

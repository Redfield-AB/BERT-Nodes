/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.bert.prefs;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.knime.core.util.Version;
import org.knime.python2.PythonModuleSpec;
import org.knime.python2.config.PythonConfig;
import org.knime.python2.prefs.AbstractPythonPreferencePage;
import org.knime.python3.PythonSourceDirectoryLocator;
import org.knime.python3.scripting.nodes.prefs.BundledCondaEnvironmentConfig;

import se.redfield.bert.prefs.MultiOptionEnvironmentCreator.CondaEnvironmentCreationOption;

/**
 * The preference page.
 * 
 * @author Alexander Bondaletov
 *
 */
public final class BertPreferencePage extends AbstractPythonPreferencePage {

	private static final String FEATURE_NAME = "Redfield BERT Nodes";

	private final MultiOptionEnvironmentCreator m_condaEnvironmentCreator = new MultiOptionEnvironmentCreator(
			FEATURE_NAME, "redfield_bert", getCondaEnvCreationOptions());
	
	private static CondaEnvironmentCreationOption[] getCondaEnvCreationOptions() {
		var options = Stream.of(new CondaEnvironmentCreationOption("CPU", true, getEnvPath("cpu")));
		if (Platform.OS_LINUX.equals(Platform.getOS()) || Platform.OS_WIN32.equals(Platform.getOS())) {
			options = Stream.concat(options,
					Stream.of(new CondaEnvironmentCreationOption("GPU", true, getEnvPath("gpu"))));
		}
		return options.toArray(CondaEnvironmentCreationOption[]::new);
	}

	private final PythonEnvironmentSelectionConfig m_pyEnvSelectConfig = new PythonEnvironmentSelectionConfig(
			BertPreferenceInitializer.getDefaultPythonEnvironmentTypeConfig(),
			BertPreferenceInitializer.getDefaultCondaEnvironmentsConfig(),
			BertPreferenceInitializer.getDefaultManualEnvironmentsConfig(),
			new BundledCondaEnvironmentConfig(BertPreferences.BUNDLED_ENV_ID));

	private PythonEnvironmentSelectionPanel m_pyEnvSelectPanel;
	
	private StringPythonConfig m_cacheDirConfig = BertPreferences.createCacheDirConfig();

	/**
	 * Creates new instance.
	 */
	public BertPreferencePage() {
		super(BertPreferences.CURRENT, BertPreferences.DEFAULT);
	}

	@Override
	protected void populatePageBody(Composite container, List<PythonConfig> configs) {
		configs.add(m_pyEnvSelectConfig);
		var configObserver = new BundledEnvironmentConfigsObserver(m_pyEnvSelectConfig, m_condaEnvironmentCreator,
				FEATURE_NAME, List.of(//
						// TODO double check requirements
						new PythonModuleSpec("py4j"), //
						new PythonModuleSpec("pyarrow", new Version(6, 0, 0), true), //
						new PythonModuleSpec("pandas"),
						new PythonModuleSpec("transformers"),
						new PythonModuleSpec("tensorflow"),
						new PythonModuleSpec("bert.tokenization"),
						new PythonModuleSpec("tensorflow_hub"))//
		);
		m_pyEnvSelectPanel = new PythonEnvironmentSelectionPanel(container, m_pyEnvSelectConfig,
				m_condaEnvironmentCreator, configObserver);
		
		configs.add(m_cacheDirConfig);
		addCacheDirChooser(container);
	}
	
	private void addCacheDirChooser(Composite container) {
		var cacheDirGroup = PreferenceUtils.createGroup(container, "Cache Directory");
		var cacheDirModel = m_cacheDirConfig.getModel();
		new DirectoryChooser(BertPreferenceInitializer.PREF_CACHE_DIR, "", cacheDirGroup, cacheDirModel);
	}

	private static String getEnvPath(final String tag) {
		return PythonSourceDirectoryLocator
				.getPathFor(BertPreferencePage.class, String.format("config/bert_%s_%s.yml", getPlatformTag(), tag))//
				.toAbsolutePath()//
				.toString();
	}
	
	private static String getPlatformTag() {
		final var os = Platform.getOS();
		if (Platform.OS_WIN32.equals(os)) {
			return "win";
		} else if (Platform.OS_LINUX.equals(os)) {
			return "linux";
		} else if (Platform.OS_MACOSX.equals(os)) {
			return "osx";
		} else {
			throw new IllegalStateException("Unsupported platform: " + os);
		}
	}

	@Override
	protected void reflectLoadedConfigurations() {
		String warning = m_pyEnvSelectPanel.reflectLoadedConfigurations();
		setMessage(FEATURE_NAME, NONE);
		updateDisplayMinSize();
		if (warning != null) {
			setMessage(warning, WARNING);
		}
	}

	@Override
	protected void setupHooks() {
		m_pyEnvSelectConfig.getEnvironmentTypeConfig().getEnvironmentType()
				.addChangeListener(e -> setMessage(FEATURE_NAME, NONE));
		m_pyEnvSelectPanel.setupHooksAfterInitialization(this::updateDisplayMinSize);
	}
}

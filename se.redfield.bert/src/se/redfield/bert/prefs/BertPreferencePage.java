/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.bert.prefs;

import java.util.List;

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

	// TODO add GPU
	private final MultiOptionEnvironmentCreator m_condaEnvironmentCreator = new MultiOptionEnvironmentCreator(
			FEATURE_NAME, "redfield_bert", new CondaEnvironmentCreationOption("CPU", true, getEnvPath("cpu")));

	private final PythonEnvironmentSelectionConfig m_pyEnvSelectConfig = new PythonEnvironmentSelectionConfig(
			BertPreferenceInitializer.getDefaultPythonEnvironmentTypeConfig(),
			BertPreferenceInitializer.getDefaultCondaEnvironmentsConfig(),
			BertPreferenceInitializer.getDefaultManualEnvironmentsConfig(),
			new BundledCondaEnvironmentConfig(BertPreferences.BUNDLED_ENV_ID));

	private PythonEnvironmentSelectionPanel m_pyEnvSelectPanel;

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

	}

	private static String getEnvPath(final String tag) {
		return PythonSourceDirectoryLocator.getPathFor(BertPreferencePage.class, "config/bert_" + tag + ".yml")//
				.toAbsolutePath()//
				.toString();
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

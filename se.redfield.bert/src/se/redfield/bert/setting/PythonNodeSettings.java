/*
 * Copyright (c) 2020 Redfield AB.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *  
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package se.redfield.bert.setting;

import org.knime.conda.prefs.CondaPreferences;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.python2.PythonVersion;
import org.knime.python2.config.PythonCommandConfig;
import org.knime.python2.prefs.PythonPreferences;

public class PythonNodeSettings {

	private static final String KEY_PYTHON_COMMAND = "pythonCommand";

	private final PythonCommandConfig pythonCommand;

	public PythonNodeSettings() {
		// TODO use bundled environment or environment coming from the extension's preference page
		pythonCommand = new PythonCommandConfig(KEY_PYTHON_COMMAND, PythonVersion.PYTHON3,
				CondaPreferences::getCondaInstallationDirectory, PythonPreferences::getPython3CommandPreference);
	}

	public PythonCommandConfig getPythonCommand() {
		return pythonCommand;
	}

	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		pythonCommand.loadSettingsFrom(settings);
	}

	public void saveSettingsTo(NodeSettingsWO settings) {
		pythonCommand.saveSettingsTo(settings);
	}
}

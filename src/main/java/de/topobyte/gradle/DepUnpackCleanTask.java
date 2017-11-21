// Copyright 2017 Sebastian Kuerten
//
// This file is part of gradle-dependency-unpack-plugin.
//
// gradle-dependency-unpack-plugin is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// gradle-dependency-unpack-plugin is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with gradle-dependency-unpack-plugin. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.gradle;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;

public class DepUnpackCleanTask extends AbstractDepUnpackTask
{

	public DepUnpackCleanTask()
	{
		setGroup("info");
	}

	@TaskAction
	protected void clean()
	{
		File buildDir = getProject().getBuildDir();
		Path unpack = buildDir.toPath()
				.resolve(Constants.DIR_NAME_UNPACKED_JARS);
		clean(unpack);
	}

	private void clean(Path directory)
	{
		logger.lifecycle("cleaning: " + directory);
		FileUtils.deleteQuietly(directory.toFile());
	}

}

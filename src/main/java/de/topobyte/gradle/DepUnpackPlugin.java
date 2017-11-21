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

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPlugin;

public class DepUnpackPlugin implements Plugin<Project>
{

	@Override
	public void apply(final Project project)
	{
		Logger logger = project.getLogger();
		logger.info("applying dependency unpack plugin");

		if (!project.getPlugins().hasPlugin(JavaPlugin.class)
				&& !project.getPlugins().hasPlugin(WarPlugin.class)) {
			logger.error("Please enable java or war plugin.");
			throw new IllegalStateException("No java or war plugin detected.");
		}

		DepUnpackPluginExtension extension = project.getExtensions()
				.create("depunpack", DepUnpackPluginExtension.class);

		DepUnpackTask task = project.getTasks().create("depunpack",
				DepUnpackTask.class);
		task.setConfiguration(extension);

		DepUnpackCleanTask cleanTask = project.getTasks()
				.create("depunpackClean", DepUnpackCleanTask.class);
		cleanTask.setConfiguration(extension);

		// register the directory with unpacked jars as a source set
		project.getConvention().getPlugin(JavaPluginConvention.class)
				.getSourceSets().findByName("main")
				.java(new Action<SourceDirectorySet>() {

					@Override
					public void execute(SourceDirectorySet sourceSet)
					{
						File buildDir = project.getBuildDir();
						File output = new File(buildDir,
								Constants.DIR_NAME_UNPACKED_JARS);

						logger.lifecycle("source set: " + sourceSet.getName());
						sourceSet.srcDir(output);
					}

				});

		// TODO: also register task dependencies, i.e. that our plugin needs to
		// be run before the compile task.
	}

}

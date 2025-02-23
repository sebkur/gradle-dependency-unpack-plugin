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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.query.ArtifactResolutionQuery;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.component.Artifact;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier;
import org.gradle.jvm.JvmLibrary;
import org.gradle.language.base.artifact.SourcesArtifact;

public class DepUnpackTask extends AbstractDepUnpackTask
{

	public DepUnpackTask()
	{
		setGroup("build");
	}

	@TaskAction
	protected void unpack()
	{
		Project project = getProject();

		if (configuration.isDebug()) {
			SortedSet<String> names = project.getConfigurations().getNames();
			for (String name : names) {
				logger.info("configuration: " + name);
			}
		}

		String configName = configuration.getConfiguration();

		Configuration configuration = project.getConfigurations()
				.getByName(configName);
		DependencySet dependencies = configuration.getAllDependencies();
		dependencies.all(d -> {
			String group = d.getGroup();
			String module = d.getName();
			String version = d.getVersion();
			logger.info(String.format("%s:%s:%s", group, module, version));
		});

		File buildDir = project.getBuildDir();
		File output = new File(buildDir, Constants.DIR_NAME_UNPACKED_JARS);
		logger.lifecycle("unpacking to: " + output);
		output.mkdirs();

		Set<ResolvedArtifact> artifacts = configuration
				.getResolvedConfiguration().getResolvedArtifacts();
		for (ResolvedArtifact artifact : artifacts) {
			ComponentArtifactIdentifier id = artifact.getId();
			if (id instanceof ModuleComponentArtifactIdentifier) {
				ModuleComponentArtifactIdentifier mcai = (ModuleComponentArtifactIdentifier) id;
				ModuleComponentIdentifier mci = mcai.getComponentIdentifier();
				String group = mci.getGroup();
				String module = mci.getModule();
				String version = mci.getVersion();
				if (group.equals("org.jsweet")
						&& module.equals("jsweet-core")) {
					logger.lifecycle(String.format("JSweet core: %s:%s:%s",
							group, module, version));
					continue;
				}
				if (isCandyJar(artifact)) {
					logger.lifecycle(String.format("Candy: %s:%s:%s", group,
							module, version));
					continue;
				}
				logger.lifecycle(String.format("Normal: %s:%s:%s", group,
						module, version));

				List<ResolvedArtifactResult> sources = resolveSource(group,
						module, version);
				if (sources.isEmpty()) {
					logger.warn("no source found");
				}
				for (ResolvedArtifactResult resolvedSourceResult : sources) {
					printInfo(group, module, version, resolvedSourceResult);
					try {
						unpack(resolvedSourceResult.getFile(), output);
					} catch (IOException e) {
						logger.warn("Error while unpacking "
								+ resolvedSourceResult.getFile());
						e.printStackTrace();
					}
				}
			}
		}
	}

	private boolean isCandyJar(ResolvedArtifact artifact)
	{
		boolean candyMetadataFound = false;
		File file = artifact.getFile();
		try {
			ZipFile zip = new ZipFile(file);
			ZipEntry entry = zip.getEntry("META-INF/candy-metadata.json");
			candyMetadataFound = entry != null;
			zip.close();
		} catch (IOException e) {
			return false;
		}
		return candyMetadataFound;
	}

	private void printInfo(String group, String module, String version,
			ResolvedArtifactResult resolvedSourceResult)
	{
		logger.info(String.format("%s:%s:%s %s", group, module, version,
				resolvedSourceResult.getFile()));
	}

	private List<ResolvedArtifactResult> resolveSource(String group,
			String module, String version)
	{
		Project project = getProject();
		DependencyHandler handler = project.getDependencies();

		DefaultModuleComponentIdentifier id = new DefaultModuleComponentIdentifier(
				new MavenGroup(group, module), version);

		Class<? extends Artifact>[] classes = new Class[] {
				SourcesArtifact.class };

		ArtifactResolutionQuery query = handler.createArtifactResolutionQuery()
				.forComponents(id).withArtifacts(JvmLibrary.class, classes);
		ArtifactResolutionResult result = query.execute();

		List<ResolvedArtifactResult> all = new ArrayList<>();
		for (ComponentArtifactsResult resolved : result
				.getResolvedComponents()) {
			for (ArtifactResult sourceResult : resolved
					.getArtifacts(SourcesArtifact.class)) {
				if (sourceResult instanceof ResolvedArtifactResult) {
					ResolvedArtifactResult resolvedSourceResult = (ResolvedArtifactResult) sourceResult;
					all.add(resolvedSourceResult);
				}
			}
		}

		return all;
	}

	private void unpack(File zip, File output) throws ZipException, IOException
	{
		InputStream fis = new FileInputStream(zip);
		ZipInputStream zis = new ZipInputStream(fis);

		ZipEntry entry = zis.getNextEntry();
		while (entry != null) {
			String fileName = entry.getName();
			File file = new File(output, fileName);

			logger.info("unzip: " + file.getAbsoluteFile());
			if (entry.isDirectory()) {
				file.mkdirs();
			} else {
				file.getParentFile().mkdirs();
				unpack(zis, file);
			}

			entry = zis.getNextEntry();
		}

		zis.close();
		fis.close();
	}

	private void unpack(ZipInputStream zis, File file) throws IOException
	{
		OutputStream fos = new FileOutputStream(file);
		IOUtils.copyLarge(zis, fos);
		fos.close();
	}

}

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
		dependsOn(JavaPlugin.CLASSES_TASK_NAME);
		setGroup("info");
	}

	@TaskAction
	protected void unpack()
	{
		Project project = getProject();

		if (configuration.isDebug()) {
			SortedSet<String> names = project.getConfigurations().getNames();
			for (String name : names) {
				System.out.println(name);
			}
		}

		String configName = getConfiguration().getConfiguration();

		Configuration configuration = project.getConfigurations()
				.getByName(configName);
		DependencySet dependencies = configuration.getAllDependencies();
		dependencies.all(d -> {
			String group = d.getGroup();
			String module = d.getName();
			String version = d.getVersion();
			System.out
					.println(String.format("%s:%s:%s", group, module, version));
		});

		File buildDir = project.getBuildDir();
		File output = new File(buildDir, "unpackedJars");
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
					System.out.println(String.format("JSweet core: %s:%s:%s",
							group, module, version));
					continue;
				}
				if (isCandyJar(artifact)) {
					System.out.println(String.format("Candy: %s:%s:%s", group,
							module, version));
					continue;
				}
				System.out.println(String.format("Normal: %s:%s:%s", group,
						module, version));

				List<ResolvedArtifactResult> sources = resolveSource(group,
						module, version);
				if (sources.isEmpty()) {
					System.out.println("no source found");
				}
				for (ResolvedArtifactResult resolvedSourceResult : sources) {
					printInfo(group, module, version, resolvedSourceResult);
					try {
						unpack(resolvedSourceResult.getFile(), output);
					} catch (IOException e) {
						System.out.println("Error while unpacking "
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
		System.out.println(String.format("%s:%s:%s %s", group, module, version,
				resolvedSourceResult.getFile()));
	}

	private List<ResolvedArtifactResult> resolveSource(String group,
			String module, String version)
	{
		Project project = getProject();
		DependencyHandler handler = project.getDependencies();

		DefaultModuleComponentIdentifier id = new DefaultModuleComponentIdentifier(
				group, module, version);

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

			System.out.println("unzip: " + file.getAbsoluteFile());
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
		byte[] buffer = new byte[1024];
		OutputStream fos = new FileOutputStream(file);
		int len;
		while ((len = zis.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}
		fos.close();
	}

}

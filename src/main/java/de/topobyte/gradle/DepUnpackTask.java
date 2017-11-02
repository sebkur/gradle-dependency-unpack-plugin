package de.topobyte.gradle;

import java.util.Set;
import java.util.SortedSet;

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
				String extension = artifact.getExtension();
				printInfo(group, module, version, extension, artifact);
				resolveSource(group, module, version);
			}
		}
	}

	private void printInfo(String group, String module, String version,
			String extension, ResolvedArtifact artifact)
	{
		System.out.println(String.format("%s:%s:%s %s %s", group, module,
				version, extension, artifact.getFile()));
	}

	private void printInfo(String group, String module, String version,
			ResolvedArtifactResult resolvedSourceResult)
	{
		System.out.println(String.format("%s:%s:%s %s", group, module, version,
				resolvedSourceResult.getFile()));
	}

	private void resolveSource(String group, String module, String version)
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

		for (ComponentArtifactsResult resolved : result
				.getResolvedComponents()) {
			for (ArtifactResult sourceResult : resolved
					.getArtifacts(SourcesArtifact.class)) {
				if (sourceResult instanceof ResolvedArtifactResult) {
					ResolvedArtifactResult resolvedSourceResult = (ResolvedArtifactResult) sourceResult;
					printInfo(group, module, version, resolvedSourceResult);
				}
			}
		}
	}

}

package de.topobyte.gradle;

import java.util.Set;
import java.util.SortedSet;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier;

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

	private void resolveSource(String group, String module, String version)
	{
		Project project = getProject();
		// TODO: somehow request the path of the respective GAV source artifact
	}

}

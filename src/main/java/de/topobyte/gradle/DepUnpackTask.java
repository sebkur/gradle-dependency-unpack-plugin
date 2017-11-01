package de.topobyte.gradle;

import java.util.SortedSet;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskAction;

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
			String artifact = d.getName();
			String version = d.getVersion();
			System.out.println(
					String.format("%s:%s:%s", group, artifact, version));
		});
	}

}

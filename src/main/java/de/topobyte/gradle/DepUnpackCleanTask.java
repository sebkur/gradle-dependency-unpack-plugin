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
		Path unpack = buildDir.toPath().resolve("unpack");
		clean(unpack);
	}

	private void clean(Path directory)
	{
		logger.info("cleaning: " + directory);
		FileUtils.deleteQuietly(directory.toFile());
	}

}

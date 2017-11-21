package de.topobyte.gradle;

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.logging.Logger;

public abstract class AbstractDepUnpackTask extends ConventionTask
{

	protected final Logger logger = getLogger();

	protected DepUnpackPluginExtension configuration;

	public DepUnpackPluginExtension getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration(DepUnpackPluginExtension configuration)
	{
		this.configuration = configuration;
	}

}
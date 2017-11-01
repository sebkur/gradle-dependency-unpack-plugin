package de.topobyte.gradle;

import org.apache.log4j.Logger;
import org.gradle.api.internal.ConventionTask;

public abstract class AbstractDepUnpackTask extends ConventionTask
{

	protected final Logger logger = Logger.getLogger(getClass());

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
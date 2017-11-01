package de.topobyte.gradle;

public class DepUnpackPluginExtension
{

	private String configuration = "compile";

	private boolean debug = false;

	public String getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration(String configuration)
	{
		this.configuration = configuration;
	}

	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

}

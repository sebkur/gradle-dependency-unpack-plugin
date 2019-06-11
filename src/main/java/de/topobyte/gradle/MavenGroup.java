// Copyright 2019 Sebastian Kuerten
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

import org.gradle.api.artifacts.ModuleIdentifier;

public class MavenGroup implements ModuleIdentifier
{

	private static final long serialVersionUID = 5729788223340222369L;

	private String group;
	private String name;

	public MavenGroup(String group, String name)
	{
		this.group = group;
		this.name = name;
	}

	@Override
	public String getGroup()
	{
		return group;
	}

	@Override
	public String getName()
	{
		return name;
	}

}

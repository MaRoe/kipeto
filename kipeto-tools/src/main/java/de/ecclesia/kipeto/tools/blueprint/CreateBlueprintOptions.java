/*
 * #%L
 * Kipeto Tools
 * %%
 * Copyright (C) 2010 - 2011 Ecclesia Versicherungsdienst GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.ecclesia.kipeto.tools.blueprint;

import java.io.IOException;

import org.kohsuke.args4j.Option;

import de.ecclesia.kipeto.common.util.AbstractOption;

public class CreateBlueprintOptions extends AbstractOption {

	@Option(name = "-d", aliases = { "--data" }, required = true, usage = "Local data directory like 'C:/temp/kipeto", metaVar = "DIR")
	private String data;

	@Option(name = "-s", aliases = { "--source" }, required = true, usage = "Local directory to make Blueprint from 'C:/Programme/Anwendung'", metaVar = "DIR")
	private String source;

	@Option(name = "-b", aliases = { "--blueprint" }, required = true, usage = "Blueprint reference name", metaVar = "REF")
	private String blueprint;

	@Option(name = "-n", aliases = { "--description" }, required = true, usage = "Blueprint description")
	private String description;

	@Option(name = "-i", aliases = { "--icon" }, required = false, usage = "Blueprint icon")
	private String icon;

	@Option(name = "-l", aliases = { "--log-level" }, required = false, usage = "Log Level ")
	private String logLevel;

	public CreateBlueprintOptions(String[] args) throws IOException {
		parse(args);
	}

	public CreateBlueprintOptions() {
	}

	public String getDataDir() {
		return data;
	}

	public String getSource() {
		return source;
	}

	public String getBlueprint() {
		return blueprint;
	}

	public String getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setDataDir(String data) {
		this.data = data;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setBlueprint(String blueprint) {
		this.blueprint = blueprint;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

}

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
package de.ecclesia.kipeto.tools.rmref;

import org.kohsuke.args4j.Option;

import de.ecclesia.kipeto.tools.ToolOptions;

public class RemoveReferenceOptions extends ToolOptions {

	@Option(name = "-b", aliases = { "--reference" }, required = true, usage = "Reference to remove", metaVar = "VAR")
	private String reference;

	public RemoveReferenceOptions() {
	}

	public RemoveReferenceOptions(String[] args) {
		parse(args);
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

}

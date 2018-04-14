/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;

/**
 *
 * @author Hoofdgebruiker
 */
public class PackageDefinitions {
	private final List<HighLevelDefinition> definitions = new ArrayList<>();
	private final Map<String, HighLevelDefinition> definitionsByName = new HashMap<>();
	
	public List<HighLevelDefinition> getAll() {
		return definitions;
	}
	
	public void add(HighLevelDefinition definition) {
		definitions.add(definition);
		definitionsByName.put(definition.name, definition);
	}
	
	public HighLevelDefinition getDefinition(String name) {
		return definitionsByName.get(name);
	}
	
	public void registerTo(ZSPackage pkg) {
		for (HighLevelDefinition definition : definitions) {
			pkg.register(definition);
		}
	}
	
	public void registerExpansionsTo(List<ExpansionDefinition> expansions) {
		for (HighLevelDefinition definition : definitions) {
			if (definition instanceof ExpansionDefinition)
				expansions.add((ExpansionDefinition) definition);
		}
	}
}

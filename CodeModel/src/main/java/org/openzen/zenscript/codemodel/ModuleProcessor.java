/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ModuleProcessor {
	public ScriptBlock process(ScriptBlock block);
	
	public void process(HighLevelDefinition definition);
}

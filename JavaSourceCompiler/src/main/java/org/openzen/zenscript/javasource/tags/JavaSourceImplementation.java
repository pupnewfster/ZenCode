/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.tags;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceImplementation {
	public final boolean inline;
	public final JavaSourceClass implementationClass;
	
	public JavaSourceImplementation(boolean inline, JavaSourceClass implementationClass) {
		this.inline = inline;
		this.implementationClass = implementationClass;
	}
}
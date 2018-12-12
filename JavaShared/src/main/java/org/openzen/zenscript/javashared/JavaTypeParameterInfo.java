/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaTypeParameterInfo {
	public final int parameterIndex;
	public final JavaField field;
	
	public JavaTypeParameterInfo(int parameterIndex) {
		this.parameterIndex = parameterIndex;
		this.field = null;
	}
	
	public JavaTypeParameterInfo(JavaField field) {
		this.parameterIndex = -1;
		this.field = field;
	}
}

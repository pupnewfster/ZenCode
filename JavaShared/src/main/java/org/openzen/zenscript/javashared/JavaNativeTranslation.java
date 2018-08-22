/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.Expression;

/**
 *
 * @author Hoofdgebruiker
 */
public interface JavaNativeTranslation<T> {
	public T translate(Expression expression, JavaNativeTranslator<T> translator);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public interface StatementAnnotation {
	StatementAnnotation[] NONE = new StatementAnnotation[0];
	
	AnnotationDefinition getDefinition();
	
	Statement apply(Statement statement, StatementScope scope);
}

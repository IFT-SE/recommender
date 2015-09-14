package recommendationsystem.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import recommendationsystem.model.CurrentMethodList;

public class CurrentMethodsVisitor extends ASTVisitor
{
    public boolean visit(MethodDeclaration node)
    {
        CurrentMethodList.getInstance().addMethod(node);
        return true;
    }
}

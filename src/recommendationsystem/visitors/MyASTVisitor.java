package recommendationsystem.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import recommendationsystem.preload.DatabaseLoader;
import recommendationsystem.preload.MethodLocations;

public class MyASTVisitor extends ASTVisitor
{

    public boolean visit(MethodDeclaration node)
    {
        MethodLocations.getInstance().addMethod(node.resolveBinding().getKey(),
                node.getStartPosition(), node.getLength());
        DatabaseLoader.getInstance().addMethodToDb(node);
        return true;
    }
}

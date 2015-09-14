package recommendationsystem.visitors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;

import recommendationsystem.preload.DatabaseLoader;
import recommendationsystem.preload.MethodLocations;

public class ResourceVisitor implements IResourceVisitor
{
    private MethodLocations methodLocations;

    @Override
    public boolean visit(IResource resource) throws CoreException
    {
        System.out.println("Visit called on " + resource.getName());
        if (resource.getType() == IResource.FILE
                && JavaCore.isJavaLikeFileName(resource.getName()))
        {
            methodLocations = MethodLocations.getInstance();
            methodLocations.clearLocations();
            ICompilationUnit icu = JavaCore
                    .createCompilationUnitFrom((IFile) resource);
            CompilationUnit cu = parse(icu);
            cu.accept(new MyASTVisitor());
            processComments(cu);
            DatabaseLoader.getInstance().workedFile();
        }
        return true;
    }

    private CompilationUnit parse(ICompilationUnit icu)
    {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(icu);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null);
    }

    private void processComments(CompilationUnit cu)
    {
        System.out.println("Processing comments...");
        for (Object o : cu.getCommentList())
        {
            ((Comment) o).accept(new CommentVisitor());
        }
    }

}

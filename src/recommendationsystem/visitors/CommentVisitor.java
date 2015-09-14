package recommendationsystem.visitors;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;

import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.preload.CommentData;
import recommendationsystem.preload.DatabaseLoader;
import recommendationsystem.preload.MethodLocations;

public class CommentVisitor extends ASTVisitor
{
    public boolean visit(LineComment node)
    {
        addCommentToDb(node);
        return true;
    }

    public boolean visit(BlockComment node)
    {
        addCommentToDb(node);
        return true;
    }
    
    private String getCommentText(Comment node)
    {
        CompilationUnit cu = (CompilationUnit) node.getAlternateRoot();
        IFile file = (IFile) cu.getJavaElement().getResource();
        String rv = null;
        try
        {
            byte[] buffer = new byte[node.getLength()];
            InputStream is = file.getContents();
            is.skip(node.getStartPosition());
            is.read(buffer);
            is.close();

            rv = new String(buffer);
        }
        catch (CoreException e)
        {
            ErrorLogger.getInstance().logException(e);
        }
        catch (IOException e)
        {
            ErrorLogger.getInstance().logException(e);
        }
        return rv;
    }
    
    private void addCommentToDb(Comment node)
    {
        String commentText = getCommentText(node);
        String methodKey = MethodLocations.getInstance().getKeyForPosition(
                node.getStartPosition());
        if (methodKey != null)
        {
            CommentData commentData = new CommentData(commentText, methodKey, node.getStartPosition(), node.getLength());
            DatabaseLoader.getInstance().addCommentToDb(commentData);
        }        
    }
}

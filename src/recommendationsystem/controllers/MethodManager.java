package recommendationsystem.controllers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import recommendationsystem.model.CurrentMethodList;

/**
 * MethodManager tries to locate a MethodDeclaration node from the AST given a
 * method's key.
 * <p>
 * MethodManager is a singleton class. Call <code>getInstance()</code> to use
 * it.
 * 
 * @author David Piorkowski
 * 
 */
public class MethodManager
{
    private static MethodManager instance;
    private DbManager dbManager;

    /**
     * The singleton constructor
     */
    private MethodManager()
    {
        // This class is singleton, don't put anything here
    }

    /**
     * Get the instance of MethodManager.
     * 
     * @return The singleton instance of MethodManager
     */
    public static synchronized MethodManager getInstance()
    {
        if (instance == null)
        {
            instance = new MethodManager();
            instance.init();
        }
        return instance;
    }

    /**
     * MethodManager is singleton. Don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    /**
     * Initializes the private class variables used.
     */
    private void init()
    {
        dbManager = DbManager.getInstance();
    }

    /**
     * Returns a MethodDeclaration node for the given method's key. The method
     * tries to search for the node intelligently by seeing if it exists in the
     * currently opened file first. If it isn't, it looks up the source code's
     * file path for the method in the database, walks the file and gets the
     * MethodDeclaration node that way.
     * <p>
     * Unfortunately, despite the fact that Eclipse has methods to make this
     * translation trivial, they are not visible to plug-ins, so we're stuck
     * with this slower approach.
     * 
     * @param methodKey
     *            The method's key to look for
     * @return <ul>
     *         <li>A MethodDeclaration node for the given key <li>null if the
     *         key does not exist in the database or the source code file does
     *         not compile cleanly
     *         </ul>
     */
    public MethodDeclaration getMethodDeclarationFromMethodKey(String methodKey)
    {
        MethodDeclaration rv = null;

        // Check the current method list first
        rv = CurrentMethodList.getInstance().getMethodFromKey(methodKey);
        if (rv != null)
            return rv;

        // Now do the heavy lifting
        String path = dbManager.getPathFromMethodKey(methodKey);
        if (path == null)
            return null;

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IPath location = Path.fromOSString(path);
        IFile sourceFile = workspace.getRoot().getFileForLocation(location);

        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(sourceFile);
        CompilationUnit cu = parse(unit);
        rv = (MethodDeclaration) cu.findDeclaringNode(methodKey);

        return rv;
    }

    /**
     * Builds an AST from the given source file and returns it as a compilation
     * unit. This is used to find a MethodDeclaration node from a given key.
     * 
     * @param unit
     *            The source code file to parse
     * @return CompilationUnit representing the passed in file
     */
    private CompilationUnit parse(ICompilationUnit unit)
    {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null);
    }
}

package recommendationsystem.model;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * CurrentMethodList keeps track of the methods that exist in the currently open
 * source code file. It provides quick access to the current file's
 * MethodDeclaration nodes. It is primarily used by the {@link ModelManager} to
 * determine the what method the text cursor is currently in. To a lesser
 * degree, it is also used by the {@link MethodManager} when locating a
 * MethodDeclaration node from a method key.
 * <p>
 * CurrentMethodList is a singleton class. Call <code>getInstance()</code> to
 * use it.
 * 
 * @author David Piorkowski
 * 
 */
public class CurrentMethodList
{
    private static CurrentMethodList instance;
    private HashMap<String, MethodDeclaration> list;
    private IFile currentSourceFile;

    /**
     * The singleton constructor.
     */
    private CurrentMethodList()
    {
        // This class is singleton. Do not put anything here.
    }

    /**
     * Get the instance of CurrentMethodList.
     * 
     * @return A singleton instance of CurrentMethodList
     */
    public static synchronized CurrentMethodList getInstance()
    {
        if (instance == null)
        {
            instance = new CurrentMethodList();
            instance.init();
        }
        return instance;
    }

    /**
     * CurrentMethodList is singleton. Don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    /**
     * Initializes the private class variables used. This method should not be
     * called externally.
     */
    public void init()
    {
        list = new HashMap<String, MethodDeclaration>();
    }

    /**
     * Adds the given method declaration to the list of current methods.
     * Declarations whose keys already exist in the list will not be added. The
     * given MethodDeclaration must have had its bindings resolved prior to
     * inclusion in the list.
     * 
     * @param node
     *            The MethodDeclaration node to include in the list
     */
    public void addMethod(MethodDeclaration node)
    {
        list.put(node.resolveBinding().getKey(), node);
    }

    /**
     * Returns the method's name for the given method's key.
     * 
     * @param key
     *            The method's key to search for
     * @return <ul>
     *         <li>The name of the method for the given key
     *         <li>null if the key is not found
     *         </ul>
     */
    @Deprecated
    public String getMethodName(String key)
    {
        MethodDeclaration method = list.get(key);

        if (method != null)
            return list.get(key).getName().toString();

        return null;
    }

    /**
     * Returns the text offset for the given method's key. Note there is no
     * check to see if the method's key exists.
     * 
     * @param key
     *            The method's key to search for
     * @return The text offset in number of characters for the given key
     */
    private int getMethodOffset(String key)
    {
        return list.get(key).getStartPosition();
    }

    /**
     * Returns the length of the given method's key. Note there is no check to
     * see if the method's key exists.
     * 
     * @param key
     *            The method's key to search for
     * @return The length in number of characters for the method
     */
    private int getMethodLength(String key)
    {
        return list.get(key).getLength();
    }

    /**
     * Finds and returns the MethodDeclaration node in this list at the given
     * offset.
     * 
     * @param offset
     *            The text offset in number of characters
     * @return <ul>
     *         <li>The MethodDeclaration node at the given offset
     *         <li>null if the offset is not in a method
     *         </ul>
     */
    public MethodDeclaration getMethodFromOffset(int offset)
    {
        MethodDeclaration rv = null;
        int innermostOffset = 0;
        int foundOffset = 0;

        for (String key : list.keySet())
        {
            // This is so we get the innermost method
            foundOffset = getMethodOffset(key);

            if (offset >= foundOffset
                    && offset < (foundOffset + getMethodLength(key))
                    && innermostOffset < foundOffset)
            {
                innermostOffset = foundOffset;
                rv = list.get(key);
            }
        }
        return rv;
    }

    /**
     * Returns a MethodDeclaration node for the given method's key.
     * 
     * @param key
     *            The method's key to search for
     * @return <ul>
     *         <li>The MethodDeclaration node for the given key
     *         <li>null if the offset is not in a method
     *         </ul>
     */
    public MethodDeclaration getMethodFromKey(String key)
    {
        return list.get(key);
    }

    /**
     * Returns the source code file associated with this list. Note that this
     * must be first set using the <code>setCurrentSourceFile</code> method.
     * 
     * @return <ul>
     *         <li>The source file that contains the methods in this list
     *         <li>null if the source file has not been set
     *         </ul>
     */
    public IFile getCurrentSourceFile()
    {
        return currentSourceFile;
    }

    /**
     * Sets the source code file associated with this list.
     * 
     * @param sourceFile
     *            The source file that contains the methods in this list
     */
    public void setCurrentSourceFile(IFile sourceFile)
    {
        currentSourceFile = sourceFile;
    }

    /**
     * Clears the list and sets the source file to null.
     */
    public void clearData()
    {
        list.clear();
        currentSourceFile = null;
    }
}

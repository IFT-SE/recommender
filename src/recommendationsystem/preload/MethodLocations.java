package recommendationsystem.preload;

import java.util.LinkedList;
import java.util.List;

import recommendationsystem.visitors.CommentVisitor;
import recommendationsystem.visitors.MyASTVisitor;
import recommendationsystem.visitors.ResourceVisitor;

/**
 * MethodLocations is used by the {@link ResourceVisitor}, {@link MyASTVisitor}
 * and {@link CommentVisitor} mainly to determine if a comment exists within a
 * method or not. During pre-loading, each visit of the Resource visitor to a
 * new source code file clears this list. The walk through the AST then
 * populates this list with the method declarations of that AST. Since
 * CommentVisitor is run after MyASTVisitor, but before the next file of
 * ResourceVisitor, we can determine if a comment is part of a method or not.
 * <p>
 * MethodLocations is a singleton class. Call <code>getInstance()</code> to use
 * it.
 * 
 * @author David Piorkowski
 * 
 */
public class MethodLocations
{
    /**
     * MethodRange encapsulates the key and within-file position data for
     * location comparisons.
     * 
     */
    private class MethodRange
    {
        public int startPos;
        public int endPos;
        public String methodKey;

        /**
         * The constructor.
         * 
         * @param methodKey
         *            The method's key
         * @param startPos
         *            The starting position of this method declaration counted
         *            in number of characters from the beginning of the file.
         * @param endPos
         *            The end position of this method declaration counted in
         *            number of characters from the beginning of the file.
         */
        public MethodRange(String methodKey, int startPos, int endPos)
        {
            this.methodKey = methodKey;
            this.startPos = startPos;
            this.endPos = endPos;
        }
    }

    private List<MethodRange> methodPositions;
    private static MethodLocations instance;

    /**
     * The singleton constructor.
     */
    private MethodLocations()
    {
        // This class is singleton, do not put anything here.
    }

    /**
     * Gets an instance of MethodLocations.
     * 
     * @return Returns the singleton instance of MethodLocations
     */
    public static synchronized MethodLocations getInstance()
    {
        if (instance == null)
        {
            instance = new MethodLocations();
        }
        return instance;
    }

    /**
     * MethodLocations is singleton, don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    /**
     * Empties the list of known methods and locations.
     */
    public void clearLocations()
    {
        if (methodPositions == null)
            methodPositions = new LinkedList<MethodRange>();
        else
            methodPositions.clear();
    }

    /**
     * Adds a method to the list of methods and locations.
     * 
     * @param methodKey
     *            The method's key
     * @param startPos
     *            The starting position of this method declaration counted in
     *            number of characters from the beginning of the file.
     * @param length
     *            The length of the method in number of characters
     */
    public void addMethod(String methodKey, int startPos, int length)
    {
        methodPositions.add(new MethodRange(methodKey, startPos, startPos
                + length));
    }

    /**
     * Returns the innermost method's key for a given starting position.
     * 
     * @param commentStartPos
     *            The position in number of characters to search for
     * @return <ul>
     *         <li>The method's key if the position exists within a method's
     *         range
     *         <li>null otherwise
     *         </ul>
     */
    public String getKeyForPosition(int commentStartPos)
    {
        String rv = null;
        int innermostOffset = 0;

        for (MethodRange r : methodPositions)
        {
            if (commentStartPos >= r.startPos && commentStartPos < r.endPos
                    && r.startPos > innermostOffset)
            {
                // This is so we get the innermost method
                innermostOffset = r.startPos;
                rv = r.methodKey;
            }
        }
        return rv;
    }

}

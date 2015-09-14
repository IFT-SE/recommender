package recommendationsystem.preload;

/**
 * CommentData is an object used by {@link DatabaseLoader} to encapsulate
 * comment data before inserting it into the database. It contains basic
 * information such as the comment text, the method the comment exists in, the
 * start position of the comment and its length.
 * 
 * @author David Piorkowski
 * 
 */
public class CommentData
{
    private String comment, methodKey;
    private int startPos, length;

    /**
     * The constructor.
     * 
     * @param comment
     *            The comment string
     * @param methodKey
     *            The method's key that this comment belongs to
     * @param startPos
     *            The starting position of the comment in number of characters
     *            from the start of the file
     * @param length
     *            The length of this comment in number of characters
     */
    public CommentData(String comment, String methodKey, int startPos,
            int length)
    {
        this.comment = comment;
        this.methodKey = methodKey;
        this.startPos = startPos;
        this.length = length;
    }

    /**
     * Returns the comment data.
     * 
     * @return A string of the comment's content
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * Returns the method this comment belongs to.
     * 
     * @return The key of the method this comment is contained in
     */
    public String getMethodKey()
    {
        return methodKey;
    }

    /**
     * Returns the starting location of this comment.
     * 
     * @return The character offset from the beginning of the file where this
     *         comment starts
     */
    public int getStartPos()
    {
        return startPos;
    }

    /**
     * Returns the end location of this comment.
     * 
     * @return The character offset from the beginning of the file where this
     *         comment ends
     */
    public int getEndPos()
    {
        return startPos + length;
    }
}

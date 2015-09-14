package recommendationsystem.preload;

/**
 * MethodData is an object used by {@link DatabaseLoader} to encapsulate method
 * declaration data before inserting it into the database. It contains basic
 * information such as the method's AST node key, the method's name and the path
 * to the source file containing the method.
 * 
 * @author David Piorkowski
 * 
 */
public class MethodData
{
    private String key, name, path;

    /**
     * The constructor.
     * 
     * @param key
     *            The method's key
     * @param name
     *            The name of the method
     * @param path
     *            The path to the source file containing the method
     */
    public MethodData(String key, String name, String path)
    {
        this.key = key;
        this.name = name;
        this.path = path;
    }

    /**
     * Returns the method's AST node's key.
     * 
     * @return The key of this method
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Returns the name of the method.
     * 
     * @return The name of the method
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the path to the source file containing the method.
     * 
     * @return The path to the source file containing the method
     */
    public String getPath()
    {
        return path;
    }
}

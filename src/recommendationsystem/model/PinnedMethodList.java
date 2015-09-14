package recommendationsystem.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import recommendationsystem.controllers.MethodManager;
import recommendationsystem.dnd.MethodDeclarationDragAdapter;
import recommendationsystem.dnd.MethodDeclarationDropAdapter;
import recommendationsystem.logging.MyLogger;

/**
 * PinnedMethodList is the data source for the viewer used to display the
 * methods that were pinned by the user. {@link PinnedContentProvider} reads
 * this class to display the recommendations to the user in the plug-in's view.
 * This data store is updated solely through user actions, specifically those
 * contained in {@link MethodDeclarationDragAdapter} and
 * {@link MethodDeclarationDropAdapter}.
 * <p>
 * This class also provides methods for acting on the data it stores such as
 * retrievals and additions.
 * <p>
 * PinnedMethodList is a singleton class. Call <code>getInstance()</code> to use
 * it.
 * 
 * @author David Piorkowski
 * 
 */
public class PinnedMethodList
{
    private static PinnedMethodList instance;
    private List<MethodDeclaration> currentPinned;
    private List<String[]> cuesList;
    private MethodManager methodManager;

    /**
     * The singleton constructor.
     */
    private PinnedMethodList()
    {
        // This class is singleton, do not put anything here
    }

    /**
     * Gets the instance of PinnedMethodList.
     * 
     * @return Returns a singleton instance of PinnedMethodList
     */
    public static synchronized PinnedMethodList getInstance()
    {
        if (instance == null)
        {
            instance = new PinnedMethodList();
            instance.init();
        }
        return instance;
    }

    /**
     * PinnedMethodList is singleton, don't allow clones.
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
        methodManager = MethodManager.getInstance();
        currentPinned = new ArrayList<MethodDeclaration>();
        cuesList = new ArrayList<String[]>();
    }

    /**
     * Adds a method to the list of pinned methods. This method will verify that
     * this is a valid method key before committing it to the list. Currently,
     * there is no check for duplicates and entries are always added to the end
     * of the list.
     * 
     * @param pinnedMethodKey
     *            The method's key to add
     * @param cues
     *            The cues to add for that method.
     */
    public void addPinnedMethod(String pinnedMethodKey, String[] cues)
    {
        MethodDeclaration method = methodManager
                .getMethodDeclarationFromMethodKey(pinnedMethodKey);
        if (method != null)
        {
            currentPinned.add(methodManager
                    .getMethodDeclarationFromMethodKey(pinnedMethodKey));
            MyLogger.getInstance().logMethodPinned(pinnedMethodKey);

            cuesList.add(cues);
        }
    }

    /**
     * Removes the first instance of a given method from the list of pinned
     * methods and its cues.
     * 
     * @param method
     *            The method's key to remove
     */
    public void removePinnedMethod(MethodDeclaration method)
    {
        int index = currentPinned.indexOf(method);
        currentPinned.remove(method);

        if (index > -1)
            cuesList.remove(index);
    }

    /**
     * Returns true if the method's key exist in the list of currently pinned
     * methods.
     * 
     * @param methodKey
     *            The method's key to seek
     * @return <ul>
     *         <li>True if the method's key exists in the pinned method's list
     *         <li>False otherwise
     *         </ul>
     */
    public boolean contains(String methodKey)
    {
        for (MethodDeclaration method : currentPinned)
        {
            if (method.resolveBinding().getKey().equals(methodKey))
                return true;
        }
        return false;
    }

    /**
     * Returns an array of methods in the order they are stored in this object.
     * 
     * @return An array of MethodDeclaration nodes.
     */
    public MethodDeclaration[] getPinnedMethods()
    {
        int i = 0;
        MethodDeclaration[] rv = new MethodDeclaration[currentPinned.size()];
        for (MethodDeclaration method : currentPinned)
        {
            rv[i++] = method;
        }
        return rv;
    }

    /**
     * Returns the ordered array of ordered cues.
     * 
     * @param cues
     *            An array of string arrays where each string array is a list of
     *            cues that corresponds to a pinned method. The list of cues in
     *            the second array are ordered where the 0 index is the highest
     *            weighted cue. Each cell in each string array can contain:
     *            <ul>
     *            <li>A word representing the cue
     *            <li>null for no cue
     *            </ul>
     */
    public String[][] getCues()
    {
        int i = 0;
        String[][] rv = new String[cuesList.size()][];
        for (String[] cues : cuesList)
        {
            rv[i++] = cues;
        }
        return rv;
    }

    /**
     * Returns the number of methods in the pinned methods list.
     * 
     * @return The length of the list.
     */
    public int getLength()
    {
        return currentPinned.size();
    }
}

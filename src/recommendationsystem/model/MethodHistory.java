package recommendationsystem.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import recommendationsystem.controllers.threads.PFISThreadDispatcher;
import recommendationsystem.controllers.threads.TFIDFThreadDispatcher;

/**
 * MethodHistory keeps track of user's navigation choices as a list of
 * method-to-method navigations stored as methods' keys. This class is mainly
 * used to color the list of recommendations, and it is not used by
 * {@link PFISThreadDispatcher} and {@link TFIDFThreadDispatcher} in their
 * calculations.
 * <p>
 * The history's order is maintained as a simple list with n+1 is more recent
 * than n. The oldest method navigation is stored in index 0 and the newest will
 * always be [size of list] - 1.
 * <p>
 * MethodHistory is a singleton class. Call <code>getInstance()</code> to use
 * it.
 * 
 * @author David Piorkowski
 * 
 */
public class MethodHistory
{
    private List<String> visitedMethodList;
    private Set<String> uniqueVisitedMethodList;
    private HashMap<Integer, Integer> stepToMethodKeyMap;
    private int step = 0;
    private static MethodHistory instance = null;

    /**
     * The singleton constructor.
     */
    private MethodHistory()
    {
        // This class is singleton, do not put anything here
    }

    /**
     * Gets the instance of MethodHistory.
     * 
     * @return A singleton instance of MethodHistory
     */
    public static synchronized MethodHistory getInstance()
    {
        if (instance == null)
        {
            instance = new MethodHistory();
            instance.init();
        }
        return instance;
    }

    /**
     * MethodHistory is singleton, don't allow clones.
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
        visitedMethodList = new ArrayList<String>();
        uniqueVisitedMethodList = new TreeSet<String>();
        stepToMethodKeyMap = new HashMap<Integer, Integer>();
    }

    /**
     * Adds a recent navigation to the history list. It will always append to
     * the end of the list.
     * 
     * @param key
     *            The method's key to add
     */
    public void addMethodKey(String key)
    {
        if (visitedMethodList.size() == 0
                || !visitedMethodList.get(visitedMethodList.size() - 1).equals(
                        key))
        {
            stepToMethodKeyMap.put(step, visitedMethodList.size());
            step++;
            visitedMethodList.add(key);
            uniqueVisitedMethodList.add(key);
        }
    }

    /**
     * Returns the navigation history as a string array.
     * 
     * @return A string array of method keys where index 0 is the oldest
     *         navigation.
     */
    public String[] getMethodHistory()
    {
        String[] output = new String[visitedMethodList.size()];
        return visitedMethodList.toArray(output);
    }

    /**
     * Returns a navigation at a particular step.
     * 
     * @param step
     *            The step to return. Step 1 is the oldest navigation in the
     *            history
     * @return <ul>
     *         <li>A method's key representing that step of navigation
     *         <li>null if there is no history or a future step is requested
     *         </ul>
     */
    public String getMethodKeyAtStep(int step)
    {
        if (this.step == 0 || step + 1 > this.step)
            return null;

        return visitedMethodList.get(stepToMethodKeyMap.get(step));
    }

    /**
     * Returns the most recent step of navigation.
     * 
     * @return <ul>
     *         <li>A method's key representing the most current step of
     *         navigation
     *         <li>null if there is no history
     *         </ul>
     */
    public String getMostRecentMethodKey()
    {
        return getMethodKeyAtStep(step - 1);
    }

    /**
     * Returns true if the method exists anywhere in the history.
     * 
     * @param key
     *            The method's key to search for
     * @return <ul>
     *         <li>True if the method exists in the history
     *         <li>False if it does not
     *         </ul>
     */
    public boolean contains(String key)
    {
        return uniqueVisitedMethodList.contains(key);
    }

    /**
     * Returns true if the MethodDeclaration has a resolved key that matches one
     * in the history.
     * 
     * @param method
     *            The MethodDeclaration node to search for
     * @return<ul> <li>True if the method exists in the history <li>False if it
     *             does not </ul>
     */
    public boolean contains(MethodDeclaration method)
    {
        return contains(method.resolveBinding().getKey());
    }

    /**
     * Returns true if the list is empty.
     * 
     * @return <ul>
     *         <li>True if there is no history
     *         <li>False if there is at least one method in the history
     *         </ul>
     */
    public boolean isEmpty()
    {
        return visitedMethodList.isEmpty();
    }

    /**
     * Returns the number of methods in the list.
     * 
     * @return The number of methods in the navigation history
     */
    public int length()
    {
        return visitedMethodList.size();
    }

    /**
     * Prints the list of navigations to the console.
     */
    public void printHistory()
    {
        int i = 0;

        for (String key : visitedMethodList)
        {
            System.out.println(i++ + ": " + key);
        }
    }

    public Set<String> getUniqueVisitedMethods()
    {
        return uniqueVisitedMethodList;
    }
}

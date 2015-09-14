package recommendationsystem.model.pfis;

import java.util.ArrayList;
import java.util.List;

/**
 * PFISNodeList is a collection of {@link PFISNode} types that is extended by
 * {@link PFISHistoryList}, {@link PFISWordList} and {@link PFISMethodList}. It
 * is a collection of PFISNodes. This class is not intended to be used directly,
 * it is meant to be extended by other classes. Extending classes are meant to
 * implement the singleton paradigm.
 * <p>
 * This class provides general methods for adding and manipulating nodes to the
 * collection.
 * 
 * @author David Piorkowski
 * 
 */
public class PFISNodeList
{
    protected List<PFISNode> list;

    /**
     * Initializes the private class variables used.
     */
    protected void init()
    {
        list = new ArrayList<PFISNode>();
    }

    /**
     * PFISNodeList is singleton. Don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    /**
     * Empties the list of all nodes.
     */
    protected void clear()
    {
        list.clear();
    }

    /**
     * Adds the node to the list. This method does not check for duplicates
     * before adding to the list.
     * 
     * @param node
     *            The node to add to the list
     * @return A pointer to the node that was just added
     */
    protected PFISNode addNode(PFISNode node)
    {
        list.add(node);
        return node;
    }

    /**
     * Adds a node to the list. This method prevents duplicates from being added
     * to the list. A duplicate is a node that has the same name. It returns a
     * pointer to the node added if it is new or a pointer to the existing
     * equivalent node.
     * 
     * @param node
     *            The node to add to the list
     * @return <ul>
     *         <li>A pointer to the given node if it does not exist in the list
     *         <li>A pointer to the pre-existing node in the list if the given
     *         node is a duplicate
     *         </ul>
     */
    protected PFISNode addNodeNoDuplicate(PFISNode node)
    {
        if (list.contains(node))
        {
            return list.get(list.indexOf(node));
        }

        list.add(node);
        return node;
    }

    /**
     * Returns the node that has the given name in this list. The name should be
     * a method's key for method type nodes or a word for word type nodes.
     * 
     * @param name
     *            The name to search for
     * @return <ul>
     *         <li>A pointer to the PFISNode with that name
     *         <li>null otherwise
     *         </ul>
     */
    protected PFISNode getNode(String name)
    {
        for (PFISNode node : list)
        {
            if (node.getName().equals(name))
                return node;
        }
        return null;
    }

    /**
     * Returns a pointer to the list underlying this collection of PFISNode
     * types.
     * 
     * @return A pointer to the list
     */
    public List<PFISNode> getList()
    {
        return list;
    }

    /**
     * Returns the current number of nodes in this collection.
     * 
     * @return The size of this list
     */
    public int getSize()
    {
        return list.size();
    }

    /**
     * Sets the weight of all the nodes in this list to weight 1.
     */
    public void resetWeights()
    {
        for (PFISNode node : list)
        {
            node.setWeight(1F);
        }
    }

    /**
     * Spread activation from all nodes in this list to its children with the
     * given decay factor.
     * 
     * @param decay
     *            The decay factor
     */
    public void spreadActivation(float decay)
    {
        for (PFISNode node : list)
        {
            for (PFISNode child : node.getChildren())
            {
                child.spreadActivation(node.getWeight(), decay);
            }
        }
    }

}

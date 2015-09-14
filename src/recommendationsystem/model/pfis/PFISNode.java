package recommendationsystem.model.pfis;

import java.util.LinkedList;
import java.util.List;

/**
 * PFISNode is the most general node type in the PFIS topology. It stores four
 * pieces of information: the name of the node, the weight of the node, the type
 * of the node and nodes this node connects to. This class is used by
 * {@link PFISNodeList}.
 * 
 * @author David Piorkowski
 * 
 */
public class PFISNode implements Comparable<PFISNode>
{
    /**
     * Enum for the two types of nodes. History nodes are stored as method
     * nodes.
     * 
     */
    public static enum NodeType
    {
        WORD, METHOD;
    }

    private NodeType type;
    private String name;
    private List<PFISNode> children;
    private float nodeWeight;

    /**
     * This constructor creates a PFISNode with the given name and type,
     * initializes the list of children and sets the weight to zero. For method
     * nodes, the name will be the method's key. For word nodes, the name will
     * be the word.
     * 
     * @param name
     *            The name of this node.
     * @param type
     *            The type of this node.
     */
    public PFISNode(String name, NodeType type)
    {
        this.name = name;
        this.type = type;
        children = new LinkedList<PFISNode>();
        nodeWeight = 0;
    }

    /**
     * Adds a child to the list of children in this node. This will check if the
     * passed in node already exists among the children and replace the passed
     * in node with an existing one among children if the node already exists.
     * 
     * @param node
     *            The node to add to the list of children
     */
    public void addChild(PFISNode node)
    {
        // TODO: Check if this can be removed
        if (children.contains(node))
        {
            // if (node.isWord())
            // {
            node = children.get(children.indexOf(node));
            // }
        }
        else
        {
            children.add(node);
        }
    }

    /**
     * Returns true if this node contains a word.
     * 
     * @return <ul>
     *         <li>True if this node's NodeType is WORD
     *         <li>False otherwise
     *         </ul>
     */
    public boolean isWord()
    {
        return type.equals(NodeType.WORD);
    }

    /**
     * Returns true if this mode contains a method.
     * 
     * @return <ul>
     *         <li>True if this node's NodeType is METHOD
     *         <li>False otherwise
     *         </ul>
     */
    public boolean isMethod()
    {
        return type.equals(NodeType.METHOD);
    }

    /**
     * Returns the node's name. If the node is a method, this will return the
     * method's key. If the node is a word, this will return the word.
     * 
     * @return The node's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the list of children for this node. The children represent
     * outgoing links from this node in the spreading activation.
     * 
     * @return A list of children in this node.
     */
    public List<PFISNode> getChildren()
    {
        return children;
    }

    /**
     * Returns the current weight of the node.
     * 
     * @return The weight of the current node.
     */
    public float getWeight()
    {
        return nodeWeight;
    }

    /**
     * Sets the current weight of the node.
     * 
     * @param weight
     *            The weight to set
     */
    public void setWeight(float weight)
    {
        this.nodeWeight = weight;
    }

    /**
     * Decay the current node with the given decay factor. Ex: If the node's
     * weight is 2 and the decay is 0.9, the resulting weight will be 1.8.
     * 
     * @param alpha
     *            The decay factor to decay by.
     */
    public void decayWeight(float alpha)
    {
        nodeWeight = alpha * nodeWeight;
    }

    /**
     * Spread activation into this node given the incoming weight and decay
     * factor. Specifically,
     * <p>
     * nodeWeight = nodeWeight + (incomingWeight * decay)
     * 
     * @param incomingWeight
     *            The weight of the node on the other side of the incoming link
     * @param decay
     *            The decay factor used for spreading
     */
    public void spreadActivation(float incomingWeight, float decay)
    {
        // A[j] = A[j] + (A[i] * W[i, j] * D)
        // Linked node's weight = linked node's weight + (incoming node's weight
        // [1] * incoming link's weight * decay)
        nodeWeight = nodeWeight + (incomingWeight * decay);
    }

    /**
     * Equals is overridden for PFISNode. A node is equal to another PFISNode if
     * they have the same name (case-sensitive). If the object passed in is not
     * of PFISNode type, then the overridden equals is called.
     */
    @Override
    public boolean equals(Object node)
    {
        if (node instanceof PFISNode)
        {
            if (((PFISNode) node).name != null
                    && this.name.equals(((PFISNode) node).name))
                return true;
            return false;
        }
        return super.equals(node);
    }

    /**
     * ToString is overridden for PFISNode. This is just for readability. The
     * format is &lt;node type&gt; &lt;node name&gt; &lt;node weight&gt;
     * separated by tabs.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.type);
        sb.append('\t');
        sb.append(this.name);
        sb.append('\t');
        sb.append(this.nodeWeight);

        return sb.toString();
    }

    /**
     * CompareTo is overridden. Collections of PFISNode can be sorted according
     * to their weight.
     */
    @Override
    public int compareTo(PFISNode o)
    {
        if (o.nodeWeight < nodeWeight)
            return 1;
        if (o.nodeWeight > nodeWeight)
            return -1;
        return 0;
    }
}

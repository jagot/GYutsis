/* Graph.java
   ----------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

import java.io.PrintStream;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/** 
 * Interface of a general graph of which the nodes are labeled with
 * positive integers in an arbitrary way.
 * @see NodeIterator
 * @author Dries.VanDyck@rug.ac.be
 **/
public interface Graph extends Cloneable{

    /** 
     * Returns the number of nodes of the underlying graph.
     * @return the number of nodes
     **/
    public int nrOfNodes();

    /** 
     * Returns the number of edges of the underlying graph.
     * @return the number of edges
     **/
    public int nrOfEdges();
    
    /** 
     * Returns the original order of this graph; there will be
     * <code>nrOfNodes()</code> nodes with labels between
     * <coede>0</code> and <code>Order()</code>, the latter not
     * included.
     * @return the original order of the graph
     * @see #nrOfNodes
     **/
    public int order();

    /** 
     * Returns the neighbors of the node with label <code>node</code>.
     * @param node the node of which the neighbors have to be returned
     * @return the neihbors of the specified node as an array
     **/
    public int [] neighbors(int node);

    /** 
     * True if nodes <code>i</code> and <code>j</code> are connected,
     * false otherwise.
     * @param i a node of the Graph
     * @param j a node of the Graph
     * @return true if <code>i</code> and <code>j</code> are
     *         connected, false otherwise 
     **/
    public boolean connected(int i, int j);

    /** 
     * Returns true if the specified node is removed or not apparent
     * in the graph, false otherwise.
     * @param node the node to be checked
     * @return true if the node is removed, false otherwise
     * @see Yutsis 
     **/
    boolean isRemoved(int node);

    /**
     * Checks of the nodes in the nodeset form a vertex induced tree.
     * Returns also false when the nodeset is not connected or one of
     * the specified nodes is removed.
     * @param nodeset the nodes to be checked if they induce a tree
     * @return true, if the specified nodeset induces a tree, false otherwise
     **/
    public boolean isTree(int [] nodeset);

    /** 
     * Returns an iterator over the nodeset starting at the node (by
     * calling the next() method) with lowest label in some
     * ordening.
     * @return a NodeIterator pointing to the first node
     * @see NodeIterator
     **/
    public NodeIterator first();

    /** 
     * Returns an iterator over the nodeset starting at the node with
     * highest label (by calling the previous() method) in some
     * ordening.
     * @return a NodeIterator pointing to the last node
     * @see NodeIterator
     **/
    public NodeIterator last();

    /** 
     * Returns an iterator which will return <code>node</code> 
     * by a NodeIterator#next() call.
     * @param node the node to be returned by the first 
     *             NodeIterator#next() of the returned 
     * 	           NodeIterator
     * @return a NodeIterator returning <code>node</code> by the
     *         first NodeIterator#next() call.
     * @see NodeIterator
     * @see NodeIterator#next
     **/
    public NodeIterator nodeIterator(int node);

    
    /** 
     * Adds a ChangeListener to the YutsisGraph. Only structural changes are
     * notified, when a node or edge is inverted no listeners are notified. 
     * @param l the ChangeListener to be added
     **/
    public void addChangeListener(ChangeListener l);

    /** 
     * Removes the ChangeListener l from the YutsisGraph.
     * @param l the ChangeListener to be removed
     **/
    public void removeChangeListener(ChangeListener l);

    /**
     * Prints the graph in its current state to the PrintStream
     * in gml-format (graphlet).
     * @param out the PrintStream to which output will be written.
     **/
    public void toGml(PrintStream out);
}

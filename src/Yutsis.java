/* Yutsis.java
   ---------
   2002 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

/** 
 * Interface of a (cubic) graph allowing the graph transformations
 * used to reduce a Yutsis graph.
 * @see Graph
 * @see YutsisGraph
 * @author Dries.VanDyck@rug.ac.be
 **/
public interface Yutsis extends Graph{

    /** 
     * Returns the first bubble encoutered, or {-1,-1} if no bubbles
     * are present.
     * @return the nodes of the bubble as an array or {-1,-1} if no bubbles
     * are present
     **/
    public int [] bubble();

    /** 
     * Interchange on the edge <code>(nodes[0], nodes[1])</code>
     * interchanging the edges <code>(nodes[0], icnodes[0])</code>
     * and <code>(nodes[1].
     * @param nodes array containing the endpoints of the edge on
     *              which the interchange is performed
     * @param icnodes array containing the neighbors of the corresponding
     *                nodes of nodes to be interchanged 
     **/
    public void interchange(int [] nodes, int [] icnodes);

    /** 
     * Interchange on the edge <code>(node1, node2)</code> interchanging 
     * the edges <code>(node1, icnode1)</code> and 
     * <code>(node2, icnode2)</code>.
     * @param node1 first node of the edge on which the interchange 
     *              is performed
     * @param node2 second node of the edge on which the interchange 
     *              is performed
     * @param icnode1 neighbor of node1 to be interchanged with icnode2
     * @param icnode2 neighbor of node2 to be interchanged with icnode1
     **/
    public void interchange(int node1, int node2, int icnode1, int icnode2);

    /** 
     * Removes the bubble.
     * @param bubble the bubble to be removed
     **/
    public void removeBubble(int [] bubble);

    /** 
     * Removes the triangle <code>triangle[0], triangle[1],
     * triangle[2]</code>.
     * @param triangle array containing the nodes of the triangle to 
     *                 be removed 
     **/
    public void removeTriangle(int [] triangle);

    /** 
     * Removes the triangle <code>node1, node2, node3</code>.
     * @param node1 first node of the triangle to be removed
     * @param node2 second node of the triangle to be removed
     * @param node3 third node of the triangle to be removed 
     **/
    public void removeTriangle(int node1, int node2, int node3);

    /** 
     * Returns the first triangle encoutered, or {-1,-1,-1} if no
     * bubbles are present. 
     * @return the nodes of the triangle as an array or {-1,-1,-1} if
     * no triangle are present.
     **/
    public int [] triangle();

    /** 
     * Returns all triangles as an array of int [3] objects.
     * @return all triangles as an array of int [3] objects.
     **/
    public int [][] triangles();

    /** 
     * True if the graph represents a triangular delta, false
     * otherwise.
     * @return true if this graph represents a triangular delta 
     **/
    public boolean triangularDelta();

    /** 
     * True if the graph represents a triangular delta between node
     * <code>td[0]</code> and <code>td[1]</code>, false otherwise.
     * @param td the nodes of the triangular delta as an array
     * @return true if this Graph is a triangular delta between
     *         the specified nodes 
     **/
    public boolean triangularDelta(int [] td);

    /** 
     * True if the graph represents a triangular delta between node
     * <code>node1</code> and <code>node2</code>, false otherwise.
     * @param node1 first node of the triangular delta to be formatted
     * @param node2 second node of the triangular delta to be formatted
     * @return true if this Graph is a triangular delta between
     *         the specified nodes 
     **/
    public boolean triangularDelta(int node1, int node2);

    /** 
     * Returns an exact copy of the object.
     * @return a clone of this object
     **/
    public Object clone();
}

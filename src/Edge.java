/* Edge.java
   ---------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

/** 
 * This class represents a directed, labeled edge.
 * @author Dries.VanDyck@rug.ac.be
 **/
public class Edge implements Cloneable{
    /** 
     * The label from this edge. 
     **/
    private String label;
    
    /** 
     * The head from this edge.
     **/
    private int head;
    /** 
     * The tail from this edge.
     **/
    private int tail;
    
    /** 
     * Constructs an Edge with given <code> label</code> going
     * from <code>tail</code> to <code>head</code>.
     * @param label the label of the Edge
     * @param tail the tail of the Edge
     * @param head the head of the Edge
     **/
    public Edge(String label, int tail, int head){
	this.label = label;
	this.tail = tail;
	this.head = head;
    }

    /** 
     * Copy Constructor.
     * @param e the Edge to be duplicated
     **/
    public Edge(Edge e){
	this(e.label,e.tail,e.head);
    }

    /** 
     * Returns the label of this Edge.
     * @return the label of this Edge
     **/
    public String label() { return label;}

    /** 
     * Sets the label to <code>newlabel</code>.
     * @param newlabel the new label of this Edge
     **/
    public void setLabel(String newlabel){ label = newlabel;}

    /** 
     * Returns the head of this Edge.
     * @return the head of this Edge
     **/
    public int head(){ return head;}
    
    /** 
     * Sets the head to <code>newhead</code>.
     * @param newhead the new head of this Edge
     **/
    public void setHead(int newhead) { head = newhead;}

    /** 
     * Returns the tail of this Edge.
     * @return the tail of this Edge
     **/
    public int tail(){ return tail;}

    /** 
     * Sets the tail to <code>newtail</code>.
     * @param newtail the new tail of this Edge
     **/
    public void setTail(int newtail){ tail = newtail;}

    /** 
     * Returns the nodes from this Edge as an array 
     * containing <code>{tail,head}</code>.
     * @return {tail,head}
     **/
    public int [] nodes() {
	int [] nodes = {tail,head}; 
	return nodes;
    }
    
    /** 
     * Sets the tail from this Edge to <code>newtail</code>
     * and the head to <code>newhead</code>. 
     * @param newtail the new tail of this Edge
     * @param newhead the new head of this Edge
     **/
    public void setNodes(int newtail,int newhead){
	tail = newtail;
	head = newhead;
    }
    
    /** 
     * Sets the tail from this Edge to <code>newnodes[0]</code>
     * and the head to <code>newnodes[1]</code>.
     * @param newnodes the new nodes of this Edge
     **/
    public void setNodes(int [] newnodes){ setNodes(newnodes[0],newnodes[1]);}

    
    /** 
     * True if <code>node</code> is one of the nodes of this Edge. 
     * @param node the node to be checked
     * @return true if the specified node is one of the endnodes of this Edge
     **/
    public boolean connects(int node){
	return tail == node || head == node;
    }

    /** 
     * Returns the node at the other side of <code>node</code> or -1
     * if the given node is no endpoint of this Edge.
     * @param node the node for which the other endpoint has to be returned
     * @return the other endpoint of the specified node or -1 if the specified
     *         node is no endpoint of this Edge
     **/
    public int otherNode(int node){
	return tail == node ? head : head == node ? tail : -1;
    }

    /** 
     * Reverses the direction of this Edge.
     **/
    public void invert(){ 
	int tmp = tail;
	tail = head;
	head = tmp;
    }
    
    /** 
     * Returns a string representation of this Edge in the format
     * <code>label = (tail,head)</code>.
     * @return a String representation of this Edge
     **/
    public String toString(){
	return label + "=("+tail+","+head+")";
    }

    /** 
     * Two Edges are equal iff they have the same label and the 
     * same endpoints.
     * @param obj the object to be checke for equality
     * @return true if the specified object equals this Edge, false otherwise
     **/
    public boolean equals(Object obj){
	if (!(obj instanceof Edge))
	    return false;
	Edge e = (Edge) obj;
	return label.equals(e.label) && head == e.head && tail == e.tail;
    }

    /** 
     * Returns a clone of this Edge.
     * @return a clone of this Edge 
     **/
    public Object clone(){
	Edge e = null;
	try { e = (Edge) super.clone();}
	catch(CloneNotSupportedException ce){}//should not be possible
	return e;
    }
}

/* Cycle.java
   ----------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/
import java.util.ArrayList;

/** 
 * Class representing relevant cycles, which are by definition
 * composed of two cyclic canonical shortest {@link Path}s (for even
 * cycles) or two cyclic canonical shortest {@link Path}s and an edge
 * (for odd cycles). We call a path <em> cyclic canonical </em> if it
 * starts with the node with lowest label.
 * @see NodeIterator 
 * @see Path
 * @author Dries.VanDyck@rug.ac.be 
 **/
public class Cycle{

    /** 
     * The cycle is represented by the {@link Path} that results when all
     * edges are followed except the last edge going back to the
     * start node.
     * @see Path
     **/
    Path cycle;
    
    /** 
     * Constructs an odd relevant cycle made of a shortest path from
     * r->y (<code>ry</code>), a shortest path from r->z
     *  (<code>rz</code>) and the edge (y,z).
     * @param ry a cyclic canonical shortest path from r to y
     * @param rz a cyclic canonical shortest path from r to z
     * @see Path
     **/
    public Cycle(Path ry, Path rz){
	ArrayList nodes = new ArrayList(ry.length()+ rz.length()+1);
	NodeIterator ni = ry.first();
	while (ni.hasNext())
	    nodes.add(new Integer(ni.next()));
	ni = rz.last();
	while (ni.hasPrevious()){
	    Integer node = new Integer(ni.previous());
	    if (ni.hasPrevious())
		nodes.add(node);
	}
	cycle = new Path(nodes);
    }

    /** 
     * Constructs an even cycle made of a shortest path from r->p
     * (<code>rp</code>), a shortest path from r->q (<code>rq</code>)
     * and the edges (p,<code>y</code>) and (q,<code>y</code>).
     * @param rp a cyclic canonical shortest path from r to p
     * @param rq a cyclic canonical shortest path from r to q
     * @param y a node connected with p and q
     * @see Path
     **/
    public Cycle(Path rp, Path rq, int y){
	ArrayList nodes = new ArrayList(rp.length()+ rq.length()+2);
	NodeIterator ni = rp.first();
	while (ni.hasNext())
	    nodes.add(new Integer(ni.next()));
	nodes.add(new Integer(y));
	ni = rq.last();
	while (ni.hasPrevious()){
	    Integer node = new Integer(ni.previous());
	    if (ni.hasPrevious())
		nodes.add(node);
	}
	cycle = new Path(nodes);
    }

    /** 
     * Returns the length of this cycle (nr of edges).
     * @return the length of this cycle
     **/
    public int length(){
	return cycle.length()+1;
    }   

    /** 
     * Returns the node with index <code>index</code> if the cycle
     * was iterated by a {@link NodeIterator}.
     * @param index the index of the node to be returned
     * @return the node at the specified index
     **/
    public int nodeAt(int index){
	return cycle.nodeAt(index);
    } 

    /** 
     * Returns an {@link NodeIterator} over the nodes of this path
     * following the edges of this path starting at the first node (by
     * a call to the next() method).
     * @return a {@link NodeIterator} pointing to the first node
     * @see NodeIterator
     **/
    public NodeIterator first(){
	return cycle.first();
    }

    /** 
     * Returns an {@link NodeIterator} over the nodes of this cycle
     * following the edges of this path ending at the node with lowest
     * label (by a call to the previous() method).
     * @return a {@link NodeIterator} pointing to the last node
     * @see NodeIterator
     **/
    public NodeIterator last(){
	return cycle.last();
    }

    /** 
     * Returns an {@link NodeIterator} over the nodes of this cycle
     * following the edges of this cycle starting at the specified node
     * (by a call to the next() method).
     * @return a {@link NodeIterator} pointing to the last node
     * @see NodeIterator
     **/
    public NodeIterator nodeIterator(int node){
	return cycle.nodeIterator(node);
    }
   
    /** 
     * A String representation of this cycle.
     * @return a String representation of this cycle
     **/
    public String toString(){
	return new StringBuffer(cycle.toString()).
	    append("->"+cycle.first().next()).toString();
    }
}

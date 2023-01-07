/* Path.java
   ---------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.HashSet;
import java.util.NoSuchElementException;

/** 
 * Class representing a path in a Graph as a sequence of connected
 * nodes.
 * @see NodeIterator
 * @author Dries.VanDyck@rug.ac.be
 **/
public class Path{
    ArrayList nodes;
  
    /** 
     * Creates a new empty Path.
     **/
    public Path(){ nodes = new ArrayList();}
  
    /** 
     * Creates a new path being the edge (<code>i</code>,<code>j</code>).
     * @param i first node of the Path
     * @param j second node of the Path
     **/
    public Path(int i,int j) {
	nodes = new ArrayList();
	nodes.add(new Integer(i));
	nodes.add(new Integer(j));
    }

    /** 
     * Creates a new Path representing
     * <code>apath[0]->..->apath[apath.length-1]</code>.
     * @param apath the nodes of the Path as an array
     **/
    public Path(int [] apath){
	nodes = new ArrayList(apath.length);
	for (int i = 0; i < apath.length; i++)
	    nodes.add(new Integer(apath[i]));
    }

    /** 
     * Wraps a new Path object around this vector.
     * @param nodes the ArrayList around which a Path has to be wrapped
     **/
    protected Path(ArrayList nodes){
	this.nodes = nodes;
    }
    
    /** 
     * Returns the length of this Path (nr of edges).
     * @return the length of this Path
     **/
    public int length(){ return nodes.size()-1;}

    /** 
     * Returns the node at distance <code>d</code> from the startnode
     * of this Path.
     * @param d the distance (within the Path)
     * @return the node at the specified distance from the startnode
     **/
    public int nodeAt(int d){ 
	return ((Integer) nodes.get(d)).intValue();
    }

    /** 
     * Adds node <code>node</code> to the end of this Path.
     * @param node the node to be added to the back
     **/
    public void addBack(int node){ nodes.add(new Integer(node));}

    /** 
     * Adds node <code>node</code> to the end of this Path (O(length)).
     * @param node the node to be added to the front
     **/ 
    public void addFront(int node) { nodes.add(0, new Integer(node));}

    /** 
     * Removes the last node from this Path.
     **/
    public void removeBack() { nodes.remove(nodes.size()-1);}
  
    /** 
     * Removes the first node from this Path (O(length)).
     **/
    public void removeFront() {nodes.remove(0);}

    /** 
     * Appends <code>p</code> to this path where appropriate.
     * Appending to the back is more efficient.
     * @param p the Path to be appended
     **/
    public void append(Path p){
	if (p.nodes.size() <= 1)
	    return;
	else if (nodes.size() <= 1)
	    nodes = (ArrayList) p.nodes.clone();

	if (nodes.get(nodes.size()-1).equals(p.nodes.get(0))){
	    nodes.remove(nodes.size()-1);
	    nodes.addAll(p.nodes);
	}
	else if (nodes.get(nodes.size()-1).equals(p.nodes.get(p.nodes.size()-1))){
	    nodes.remove(nodes.size()-1);
	    ListIterator li = p.nodes.listIterator(p.nodes.size());
	    while (li.hasPrevious()){
		nodes.add(li.previous());
	    }
	}
	else if (nodes.get(0).equals(p.nodes.get(p.nodes.size()-1))){
	    ArrayList newnodes = (ArrayList) p.nodes.clone();
	    newnodes.remove(p.nodes.size()-1);
	    newnodes.addAll(nodes);
	    nodes = newnodes;
	}
	else if (nodes.get(0).equals(p.nodes.get(0))){
	    ArrayList newnodes = new ArrayList(nodes.size()+p.nodes.size());
	    ListIterator li = p.nodes.listIterator(p.nodes.size());
	    while (li.hasPrevious()){
		newnodes.add(li.previous());
	    }
	    li = nodes.listIterator();li.next();
	    while (li.hasNext()){
		newnodes.add(li.next());
	    }
	    nodes = newnodes;
	}
    }

    /** 
     * Returns the concatenation of this path with the path <code>p</code>
     * as a new Path object.
     * @param p the Path to be concatenated with this Path
     * @return the result of the concatatenation as a new Path object
     **/
    public Path concat(Path p){
	Path pt;
	if (this.nodes.get(0).equals(p.nodes.get(0))){
	    pt = (Path) p.clone();
	    pt.append(this);
	}
	else{
	    pt = (Path) this.clone();
	    pt.append(p);
	}
	return pt;
    }

    /** 
     * True if this path is cylic canonical. We call a path i->j cyclic
     * canonical if if it only passes nodes bigger than i, with other words
     * the startnode must be the lowest node of the path.
     * @return true if this Path is cyclic canonical, false otherwise
     **/
    public boolean isCanonical(){
	ListIterator li = nodes.listIterator();
	int firstnode = ((Integer) li.next()).intValue();
	while (li.hasNext()){
	    int node = ((Integer) li.next()).intValue();
	    if (li.hasNext() && firstnode > node)
		return false;
	}
	return true;
    }

    /** 
     * Returns the nodes common in both paths.
     * @param p the path to be intersected with this Path
     * @return the nodes common to this and the specified Path
     **/
    public int [] intersection(Path p){
	ArrayList intersection = new ArrayList(Math.min(length(),p.length()));
	HashSet hs = new HashSet(nodes.size()*4/3);
	ListIterator li = nodes.listIterator();    
	while (li.hasNext())
	    hs.add(li.next());
	li = p.nodes.listIterator();
	while (li.hasNext()){
	    Integer node = (Integer) li.next();
	    if (hs.remove(node))
		intersection.add(node);
	}
	li = intersection.listIterator();
	int [] toreturn = new int[intersection.size()];
	int index = 0;
	while (li.hasNext())
	    toreturn[index++] = ((Integer) li.next()).intValue();
	return toreturn;
    }
  
    /**
     * Class implementing the NodeIterator interface for
     * iterating over the nodes of this Path.
     * @see NodeIterator
     **/
    class PNodeIterator implements NodeIterator{
	ListIterator li;
      
	/**
	 * Constructs a new PNodeIterator.
	 **/
	PNodeIterator(){
	    li = nodes.listIterator();
	}
      
	/**
	 * Constructs a new PNodeIterator starting the iteration
	 * at the node at the specified index.
	 * @param index the index of the node where the iteration has
	 *              to be started
	 **/
	PNodeIterator(int index){
	    li = nodes.listIterator(index);
	}

	/**
	 * Implementation of the NodeIterator interface.
	 * @return true if this is not the last node in the iteration,
	 *         false otherwise.  
	 **/
	public boolean hasNext(){
	    return li.hasNext();
	}

	/**
	 * Implementation of the NodeIterator interface.
	 * @return the next node in this iteration or -1 if this is the
	 *         last.  
	 * @see NodeIterator
	 **/
	public int next(){
	    return ((Integer) li.next()).intValue();
	}

	/**
	 * Implementation of the NodeIterator interface.
	 * @return true if this is not the first node in the iteration,
	 *         false otherwise.
	 * @see NodeIterator
	 **/
	public boolean hasPrevious(){
	    return li.hasPrevious();
	}
      
	/**
	 * Implementation of the NodeIterator interface.
	 * @return the previous node in this iteration or -1 if this is
	 *         the first.
	 * @see NodeIterator 
	 **/
	public int previous(){
	    return ((Integer) li.previous()).intValue();
	} 
    }  

    /** 
     * Returns an NodeIterator over the nodes of this Path
     * following the edges of this Path starting at the first node (by
     * a call to the next() method).
     * @return a NodeIterator pointing to the last node
     * @see NodeIterator
     * @see NodeIterator#next
     **/
    public NodeIterator first(){
	return new PNodeIterator();
    }

    /** 
     * Returns an NodeIterator over the nodes of this Path
     * following the edges of this Path starting at the last node (by a
     * call to the previous() method)
     * @return a NodeIterator pointing to the last node.
     * @see NodeIterator
     * @see NodeIterator#previous
     **/
    public NodeIterator last(){
	return new PNodeIterator(nodes.size());
    }

    /** 
     * Returns an NodeIterator over the nodes of this Path
     * following the edges of this path starting at the specified node
     * (by a call to the next() method).
     * @param node the node to be returned by the first 
     *             NodeIterator.next() of the returned 
     *   	         NodeIterator
     * @return a NodeIterator returning <code>node</code> by the
     *         first NodeIterator.next() call.      
     * @see NodeIterator
     * @see NodeIterator.next()
     **/
    public NodeIterator nodeIterator(int node){
	int index = nodes.indexOf(new Integer(node));
	if (index >= 0)
	    return new PNodeIterator(index);
	else 
	    throw new NoSuchElementException("Node " + node + " not in path.");
    }

    /** 
     * Implementation of the Cloneable interface.
     * @return a clone of this Path
     **/
    public Object clone(){
	return new Path(new ArrayList(nodes));
    }

    /** 
     * Returns a String representation of this path.
     * @return a String representation of this path.
     **/
    public String toString(){
	StringBuffer sb = new StringBuffer();
	ListIterator li = nodes.listIterator();
	sb.append(li.next());
	while (li.hasNext()){
	    sb.append("->"+li.next());
	}
	return sb.toString();
    }
}

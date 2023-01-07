/* AbstractGraph.java
   ------------------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

import java.io.PrintStream;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


/** 
 * This abstract class provides a skeleton implementation of the
 * {@link Graph} interface to minimize the effort to implement this
 * interface.
 * @see Graph
 * @see NodeIterator
 * @author Dries.VanDyck@rug.ac.be 
 **/
public abstract class AbstractGraph 
    implements Graph{

    /**
     * ArrayList of ChangeListener's to be notified of changes.
     **/
    private ArrayList changelisteners = new ArrayList();

    /** 
     * True if nodes <code>i</code> and <code>j</code> are connected,
     * false otherwise.
     * @param i a node of the Graph
     * @param j a node of the Graph
     * @return true if <code>i</code> and <code>j</code> are
     *         connected, false otherwise 
     **/
    public boolean connected(int i, int j){
	if (isRemoved(i) || isRemoved(j))
	    return false;
	int [] nghbi = neighbors(i);
	for(int k = 0; k < 3; k++)
	    if (nghbi[k] == j)
		return true;
	return false;
    }

    /** 
     * Adds a ChangeListener to the Graph to be notified of structural
     * changes.
     * @param l the ChangeListener to be added
     **/
    public void addChangeListener(ChangeListener l){ changelisteners.add(l); }

    /** 
     * Removes the ChangeListener l from the Graph.
     * @param l the ChangeListener to be removed
     **/
    public void removeChangeListener(ChangeListener l){
	changelisteners.remove(l);
    }
    
    /** 
     * Send a ChangeEvent, whose source is this Graph, to each
     * listener.  
     **/
    protected void fireStateChanged(){
	ChangeEvent ce = new ChangeEvent(this);
	for (int i = 0; i < changelisteners.size(); i++)
	    ((ChangeListener) changelisteners.get(i)).stateChanged(ce);
    }

    /**
     * Checks of the nodes in the nodeset form a vertex induced tree.
     * Returns also false when the nodeset is not connected or one of
     * the specified nodes is removed.
     * @param nodeset the nodes to be checked if they induce a tree
     * @return true, if the specified nodeset induces a tree, false otherwise
     **/
    public boolean isTree(int [] nodeset){
	int n = order();
	boolean [] intree = new boolean[n]; // nodes in tree
	boolean [] inset = new boolean[n]; // nodes in the set
	for (int i = 0; i < nodeset.length; ++i)
	    inset[nodeset[i]] = true;
	int nrofnodesintree = 1;
	if (isRemoved(nodeset[0]))
	    return false;
	intree[nodeset[0]] = true;
	LinkedList ll = new LinkedList();
	ll.add(new Integer(nodeset[0]));
	while(!ll.isEmpty() && nrofnodesintree < n/2){
	    int currentnode = ((Integer) ll.removeFirst()).intValue();
	    int [] nghb = neighbors(currentnode);
	    for (int i = 0; i < 3; ++i)
		if (!intree[nghb[i]] && inset[nghb[i]]){
		    int [] newnghb = neighbors(nghb[i]);
		    for (int j = 0; j < 3; ++j)
			if (newnghb[j] != currentnode && intree[newnghb[j]])
			    return false; // Cycle!
		    ll.add(new Integer(nghb[i]));
		    intree[nghb[i]] = true;
		    nrofnodesintree++;
		}
	}
	if (nrofnodesintree == nodeset.length)
	    return true; // Is a vertex induced tree!
	else
	    return false; // disconnected nodeset
    }


    /**
     * Returns a string representation of the graph in adjacencylist
     * format: <br><pre>
     * 0: nghb1 nghb2 nghb3
     * ... 
     * i: nghbi1 nghbi2 nghbi3
     * ... </pre>
     * @return a String representation of this CubicGraph
     **/
    public String toString(){
	String s = nrOfNodes() + "\n";
	for(int i = 0; i < order(); i++)
	    if (!isRemoved(i)){
		int [] nghb = neighbors(i);
		s+=i+": " + nghb[0] + "\t" + 
		    nghb[1] + "\t" + nghb[2] +"\n";
	    }
	return s;
    }
    
    /** 
     * Returns an exact copy of the object.
     * @return a clone of this object
     **/
    public Object clone(){
	AbstractGraph g = null;
	try{ g = (AbstractGraph) super.clone();}
	catch(CloneNotSupportedException ce){}//should not be possible
	g.changelisteners = new ArrayList();
	return g;
    }
    
  /**
   * Prints the graph in its current state to the PrintStream
   * in gml-format (graphlet).
   * @param out the PrintStream to which output will be written.
   **/
  public void toGml(PrintStream out){
    out.println("graph [\nversion 2\ndirected 0\nnode_style " 
		+ "[\nname \"default_node_style\"\nstyle "
		+ "[\ngraphics [\nw 16.0\nh 16.0\n]\n]\n]\n"
		+ "edge_style [\nname \"default_edge_style\""
		+ "\nstyle [\ngraphics [\n]\n]\n]");
  
    float diagstep = 250 / (nrOfNodes()/2); 
    float diag = 0;
    for (int i=0; i < order(); i++)
      if (!isRemoved(i)){
	diag+=diagstep;
	out.println("node [\nid " + (20+i) + "\nlabel \"" + i 
		    + "\"\ngraphics [\nx " + diag + "\ny " + diag 
		    + "\nw 15\n]\nLabelGraphics [\ntype \"text\"\n]\n]");
      }
    for (int i = 0; i < order()-1; i++)
      if (!isRemoved(i)){
	int [] nghb = neighbors(i);
	String [] labels = gmlEdgeLabels(i);
	for (int j = 0; j < 3; j++){
	  if (nghb[j] > i){      
	      String label = labels == null || labels[j] == null ? 
		  "" : "\nlabel \""+labels[j]+"\"";
	      out.println("edge [\nsource " + (20+i) 
			+ "\ntarget " + (20+nghb[j]) 
			+ label
			+ "\ngraphics [\n]\nLabelGraphics [\n]\n]");
	  }
	}
      }
    out.print("]");
  }

    /**
     * Returns the edge labels of node i to be used for saving the Graph in 
     * GML-format; this is a default implementation returning null,
     * indicating that no edge label is used. 
     * @param i node of which the edge labels must be returned
     * @return a array of edge labels of node i in the same order the
     *         neighbors are returned.
     **/
    public String [] gmlEdgeLabels(int i){
	return null;
    }
}

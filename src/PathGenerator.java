/* PathGenarator.java
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
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/** 
 * Class that generates all shortest paths for a given
 * Graph. The shortest paths can be made cyclic canonical for the
 * purpose of cycle generation. The PathGenerator object is always
 * in sync with the Graph object by using the ChangeListener to 
 * regenerate Paths on demand.
 * @see Graph 
 * @see Path 
 * @see Cycle
 * @author Dries.VanDyck@rug.ac.be
 **/
public class PathGenerator
    implements ChangeListener{
    
    /** 
     * The Graph for which shortest pahts are generated.
     * @see Graph
     **/
    private Graph g;
    
    /** 
     * Distancetable for this graph as a symmetric matrix with
     * null on the diagonal.
     **/
    private ArrayList [] pathTable;
    
    /** 
     * Will be set true when the g notifies a change.
     **/
    private boolean stateChanged = true;
  
    /** 
     * Constructs a new PathGenerator for the graph <code>g</code>.
     * @param g the Graph for which shortest paths have to be
     *          generated 
     * @see Graph
     **/
    public PathGenerator(Graph g){ 
	this.g = g;
	g.addChangeListener(this);
	pathTable = new ArrayList[g.order()*(g.order()-1)/2];
    }
   
    /** 
     * Returns a vector of shortest paths between <code>i</code> and
     * <code>j</code>
     * @param i first node for which all Path's have to be returned 
     * @param j second node for which all Path's have to be returned 
     * @see Path
     **/
    public ArrayList paths(int i,int j){
	if (stateChanged)
	    regenerate();
	if (i > j)
	    return paths(j,i);
	else if (i == j)
	    return null;
	return pathTable[j*(j-1)/2+i];
    }

    private void setPaths(int i, int j, ArrayList d){
	if (i > j){
	    setPaths(j,i,d);
	    return;
	}
	else if (i == j)
	    throw new IllegalArgumentException("i == j");
	pathTable[j*(j-1)/2+i] = d;
    }
    
    /** 
     * Returns the distance between the nodes <code>i</code> and
     * <code>j</code>. A distance equal to the Graph's order 
     * (original nr of nodes) has to be regarded as infinity.
     * @param i first node for which the distance has to be returned
     * @param j second node for which the distance has to be returned
     * @return the distance between the specified nodes
     * @see Graph
     **/
    public int distance(int i, int j){
	if (stateChanged)
	    regenerate();
	if (i > j)
	    return distance(j,i);
	else if (i == j)
	    return 0;
	if (paths(i,j) == null)
	    return g.order(); //infinity
	return ((Path)paths(i,j).get(0)).length();
    }

    private void clearPaths(){
	for (int i = 0; i < pathTable.length; i++)
	    pathTable[i] = null;
    }

    /** 
     *Implementation of the ChangeListener interface.
     **/
    public void stateChanged(ChangeEvent e){ stateChanged = true; }

    /** 
     * Regenerates all paths. To be used when the graph is altered.
     **/
    private void regenerate(){
	stateChanged = false;
	clearPaths();
	floyd();
    }

    void floyd(){
	NodeIterator ii = g.first();
	while (ii.hasNext()){
	    int i = ii.next();
	    int [] nghb = g.neighbors(i);
	    for (int k = 0; k < 3; k++)
		if (i < nghb[k]){
		    ArrayList paths = new ArrayList();
		    paths.add(new Path(i,nghb[k]));
		    setPaths(i,nghb[k],paths);
		}
	}
	NodeIterator ki = g.first();
	while (ki.hasNext()){
	    int k = ki.next();
	    ii = g.first();
	    while (ii.hasNext()){
		int i = ii.next();
		if (ii.hasNext()){
		    NodeIterator ji = g.nodeIterator(i); 
		    if (ji != null){
			ji.next();//skip i
			while (ji.hasNext()){// Always i < j
			    int j = ji.next();
			    if (i != k && k != j && distance(i,j) >= 
				distance(i,k) + distance(k,j)){
				ArrayList ikpaths = paths(i,k);
				ArrayList kjpaths = paths(k,j);
				ArrayList ijpaths;
				if (distance(i,j) == distance(i,k) 
				    + distance(k,j))
				    ijpaths = paths(i,j);
				else
				    ijpaths = new ArrayList(ikpaths.size()
							    *kjpaths.size());
				for (int ik = 0; ik < ikpaths.size(); ik++)
				    for (int kj = 0; kj < kjpaths.size(); kj++)
					ijpaths.add(((Path) 
						     ikpaths.get(ik))
						    .concat((Path) kjpaths.
							    get(kj)));
				setPaths(i,j,ijpaths);
			    }
			}
		    }
		    else
			System.err.println("ij == null !!");
		}
	    }
	}
    }

    /** 
     * Only the Path's wich passes nodes bigger than the start
     * node will remain. A combination of such two paths uniquely
     * defines a Cycle: P(i,k) + P'(i,k) for an even cycle or
     * P(i,x) + P(i,y) + (x,y).
     * @see Path
     * @see Cycle 
     **/
    public void makeCanonical(){
	if (stateChanged)
	    regenerate();
	for (int i=0; i < pathTable.length; i++){
	    if (pathTable[i] != null){
		ListIterator li = pathTable[i].listIterator();
		ArrayList paths = new ArrayList();
		while (li.hasNext()){
		    Path p = (Path) li.next();
		    if (p.isCanonical())
			paths.add(p);
		}
		pathTable[i] = paths.size() > 0 ? paths : null;
	    }
	}
    }

    /** 
     * Returns the diameter of the Graph.
     * @return the diameter of the Graph
     * @see Graph
     **/  
    public int diameter(){
	if (stateChanged)
	    regenerate();
	int diam = 0;
	for (int i = 0; i < pathTable.length; i++)
	    if (pathTable[i] != null){
		int d = ((Path) pathTable[i].get(0)).length();
		if (d > diam)
		    diam = d;
	    }
	return diam;
    }
    
    /** 
     * Returns a String representation of this PathGenerator.
     * @return a String representation of this PathGenerator
     **/
    public String toString(){
	if (stateChanged)
	    regenerate();
	StringBuffer sb = new StringBuffer();
	NodeIterator ii = g.first();
	while (ii.hasNext()){
	    int i = ii.next();
	    NodeIterator ji = g.nodeIterator(i); ji.next();
	    while (ji.hasNext()){// Always i < j
		int j = ji.next();
		ArrayList ijpaths = paths(i,j);
		if (ijpaths != null){
		    sb.append("P("+i+','+j+"):");
		    sb.append(ijpaths.get(0));
		    for (int k = 1; k < ijpaths.size(); k++)
			sb.append("; "+ijpaths.get(k));
		    sb.append('\n');
		}
	    }
	}
	return sb.toString();
    }
    
    /** 
     * Program which prints all shortest paths (not cyclic canonical)
     * to the screen of the YutsisGraph specified by the first
     * argument.  When ran without arguments it prints a "usage" to
     * System.err.  However this class can be used with each object
     * implementing the Graph interface, this main function
     * (and thus the program) only works on YutsisGraph's.
     * @see Graph
     * @see YutsisGraph
     **/
    public static void main(String [] args) throws IOException{
	String usage = new String("Usage: java PathGenerator <YutsisGraph>\n <YutsisGraph>:= \"<Bra|Ket>\" | <filename>\n <filename> must be in BRAKET of YTS format\n  (see documentation of Class YutsisGraph for more info)");
	if (args.length != 1){
	    System.err.println(usage);
	    return;
	}
	YutsisGraph y;
	try {
	    if (args[0].charAt(0) == '<')
		y = new YutsisGraph(args[0]);
	    else {
		int format = AbstractYutsis.guessFileFormat(args[0]);
		if (format == -1){
		    System.err.println(usage);
		    return;
		}
		y = new YutsisGraph(new BufferedReader(new 
		    FileReader(args[0])), format);
	    }
	}
	catch (FileNotFoundException e){
	    System.err.println("File " + args[0] + " not found.");
	    return;
	}
	catch (IllegalArgumentException e){
	    System.err.println(e.getMessage());
	    return;
	}
	System.out.println(new PathGenerator(y));
    }
}

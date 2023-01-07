/* CycleGenerator.java
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
 * Class that generates all relevant cycles of length 3 or bigger for
 * a given Graph. A relevant cycle is a cycle that cannot be
 * created as a sum of two other cycles in the cycle vector space. In fact
 * this algorithm constructs all cycles composed of two shortest paths
 * (even cycles) or two shortest paths and an edge, of which the relevant
 * cycles are a subset.
 * @see Graph
 * @see Cycle 
 * @see Path 
 * @see PathGenerator
 * @author Dries.VanDyck@rug.ac.be 
 **/
public class CycleGenerator
    implements ChangeListener{ 
    /**
     * The Graph for which cycles will be generated.
     **/
    Graph g;
    
    /**
     * The PathGenerator used to collect cyclic canonical Paths 
     * used to generate the relevant cycles.
     **/
    PathGenerator pg;
    
    /** 
     * Array of list of cycles; index i contains all relevant cycles of
     * length i+3.
     **/
    ArrayList [] cycles;
  
    /** 
     * Will be set true when the g notifies a change.
     **/
    private boolean stateChanged = true;

    /** 
     * Constructs a new CycleGenerator object for the graph g and registers
     * itself as ChangeListener for g.
     * @param g the graph for which all relevant cycles have to be generated
     * @see Graph
     **/
    public CycleGenerator(Graph g){
	if (g.order() < 3)
	    return; // no (simple) cycles possible..
	this.g = g;
	g.addChangeListener(this);
	pg = new PathGenerator(g);
	cycles = new ArrayList[g.order()-3];
    }
  
    /**
     * Implementation of the ChangeListener interface.
     **/
    public void stateChanged(ChangeEvent e){ stateChanged = true; }

    /** 
     * Regenerates all relevant cycles. To be used if the graph is altered.
     **/
    private void regenerate(){
	stateChanged = false;
	pg.makeCanonical();
	clearCycles();
	vismara();
    }

    /** 
     * The algorithm from Philipe Vismara to generate ALL relevant cycles
     * of a given graph.
     **/
    void vismara(){
	// This variant returns ALL relevant cycles, which is potentially
	// an exponential sized set. 
	ArrayList s = new ArrayList(3);
	NodeIterator ri = g.first();
	while (ri.hasNext()){ //O(n^5)
	    int r = ri.next();
	    NodeIterator yi = g.nodeIterator(r);
	    if (yi != null){
		yi.next(); //skip r
		while (yi.hasNext()){ // O(n^4)
		    int y = yi.next();
		    s.clear();
		    if (pg.distance(r,y) < g.order()){ // y in V_r
			int [] z = g.neighbors(y);
			for (int i = 0; i < z.length; i++)
			    //O(n^3) (z.length == 3)
			    if (r < z[i] && pg.distance(r,z[i]) < g.order()){
				//z in V_r
				if (pg.distance(r,z[i]) + 1
				    == pg.distance(r,y))
				    s.add(new Integer(z[i]));
				else if (pg.distance(r,z[i]) 
					 != pg.distance(r,y) + 1 && z[i] > y){
				    ArrayList rypaths = pg.paths(r,y);
				    ArrayList rzpaths = pg.paths(r,z[i]);
				    ListIterator ryi = rypaths.listIterator();
				    ListIterator rzi = rzpaths.listIterator();
				    while (ryi.hasNext()){ //O(n^3)
					Path ry = (Path) ryi.next();
					while (rzi.hasNext()){ // O(n^2)
					    Path rz = (Path) rzi.next();
					    if (onlyStartNodeInCommon(rz,ry))
						//O(n)
						addCycle(new Cycle(ry,rz)); 
					    // O(n)
					}
				    }
				}
			    }
		    }
	
		    for (int pi = 0; pi < s.size()-1; pi++) 
			//O(n^3) (s.size() <= 3)
			for (int qi = pi+1; qi < s.size(); qi++){
			    ArrayList rppaths = 
				pg.paths(r,((Integer) s.get(pi)).intValue());
			    ArrayList rqpaths = 
				pg.paths(r,((Integer) s.get(qi)).intValue());
			    ListIterator rpi = rppaths.listIterator();
			    while (rpi.hasNext()){//O(n^3)
				Path rp = (Path) rpi.next();
				ListIterator rqi = rqpaths.listIterator();
				while (rqi.hasNext()){ //O(n^2)
				    Path rq = (Path) rqi.next();
				    if (onlyStartNodeInCommon(rp,rq))//O(n)
					addCycle(new Cycle(rp,rq,y)); //O(n)
				}
			    }
			} 
		}
	    }
	}
    }
  
    /** 
     * Adds a cycle to the set of relevant Cycle's.
     * @param c the cycle to be added to the set of relevant Cycle's
     * @see Cycle
     **/
    void addCycle(Cycle c){
	if (cycles[index(c)] == null)
	    cycles[index(c)] = new ArrayList();
	cycles[index(c)].add(c);
    }

    /**
     * Returns the index of the list where c should be added/found.
     * @return the index of the list where c should be added/found.
     **/
    private int index(Cycle c) { return c.length()-3;}

    /** 
     * True if both Path's have only the start node in common, false
     * otherwise.
     * @param vx first Path to be checked
     * @param vy second Path to be checked
     * @return true if both paths have only the startnode in common
     * @see Path
     **/
    boolean onlyStartNodeInCommon(Path vx, Path vy){
	int [] is = vx.intersection(vy);
	NodeIterator ni = vx.first();
	if (is.length == 1 && is[0] == ni.next())
	    return true;
	else
	    return false;
    }

    /** 
     * Returns the girth of the Graph.
     * @return the girth of the Graph.
     * @see Graph
     **/   
    public int girth(){
	if (stateChanged)
	    regenerate();
	for( int i = 0; i < cycles.length; i++)
	    if (cycles[i] != null)
		return i+3;
	return -1;//impossible, but needed to make javac happy
    }  

    /**
     * Returns all girth cycles as a ArrayList of Cycle's.
     * cycles of length <var>l</var> at index <var>l</var>-3.
     * @return all girth cycles as an array of ArrayLists
     * @see Cycle 
     **/
    public ArrayList girthCycles(){ return cycles(girth()); }
    
    /** 
     * Returns all cycles as a ArrayList of Cycle's with all
     * cycles of length <var>l</var> at index <var>l</var>-3.
     * @return all relevant cycles as an array of ArrayLists
     * @see Cycle 
     **/
    public ArrayList [] cycles(){ 
	if (stateChanged)
	    regenerate();
	return cycles; 
    }    

    /** 
     * Returns all cycles of length <code>l</code> as a ArrayList 
     * of Cycle's.
     * @param l the length of the relevant Cycle's to be returned
     * @return the relevant Cycle's of the specified length
     * @see Cycle
     **/
    public ArrayList cycles(int l) { 
	if (stateChanged)
	    regenerate();
	return cycles[l-3];
    }  
  
    private void clearCycles(){
	for(int i=0; i < cycles.length; i++)
	    cycles[i] = null;
    }

    /** 
     * A String representation of this object.
     * @return a String representation of this CycleGenerator object
     **/
    public String toString(){
	if (stateChanged)
	    regenerate();
	StringBuffer sb = new StringBuffer();    
	for (int i = 0; i < g.order()-3; i++)
	    if (cycles[i] != null){
		ListIterator li = cycles[i].listIterator();
		while (li.hasNext())
		    sb.append(li.next()+"\n");
		sb.append("\n");
	    }
	return sb.toString();
    }

    /** 
     * Program which prints all relevant cycles to the screen of the
     * {@link YutsisGraph} specified by the first argument.  When ran
     * without arguments it prints a "usage" to System.err.  However
     * this class can be used with each object implementing the {@link
     * Graph} interface, this main function (and thus the program) only
     * works on {@link YutsisGraph}s.
     **/
    public static void main(String [] args) throws IOException{
	String usage = new String("Usage: java CycleGenerator <YutsisGraph>\n <YutsisGraph>:= \"<Bra|Ket>\" | <filename>\n <filename> must be in BRAKET of YTS format\n  (see documentation of Class YutsisGraph for more info)");
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
		y = new YutsisGraph(new BufferedReader(new FileReader(args[0])),
				    format);
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
	System.out.println(new CycleGenerator(y));
    }
}

/* EdgeCostHeuristic.java
   ---------
   2003 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/
import java.util.ArrayList;
import java.util.ListIterator;

import java.io.PrintStream;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * This class implements an heuristic bases on edge costs for selecting
 * operations to reduce a Yutsis object.
 * <p>
 * With every edge a cost is associated equal to the difference of the
 * length of the two smallest cycles the edge is part of. The cost of
 * a cycle is defined to be the minimum of the cost of the edges in the
 * cycle. If two girth cycles have the same cost, the cycle for
 * which the minimum cost is most reached is chosen. It this is also equal
 * the cycle with lowest total edgecost is picked. 
 * @see AbstractCCAHeuristic
 * @see CCAHeuristic
 * @author Dries.VanDyck@rug.ac.be
 **/
public class EdgeCostHeuristic 
    extends AbstractCCAHeuristic 
    implements ChangeListener{

    boolean stateChanged = true;

    /** 
     * Class representing the smallest cycle an {@link Edge} is part of and
     * the cost of the {@link Edge}. 
     **/
    class EdgeCost{
	int shortestCycle = y.order();
	int cost = y.order();

	EdgeCost(int cyclelength){
	    shortestCycle = cyclelength;
	}

    }
    
    /** 
     * A table with all the {@link Edge} costs (direct acces).
     **/
    EdgeCost [] edgeCostTable;

    /**
     * Default constructor: constructs a new CycleCountHeuristic object
     **/ 
    public EdgeCostHeuristic(){}

    /**
     * Constructs a new CycleCountHeuristic object and sets the problem.
     * This equivalent with calling the default constructor and setProblem
     * afterwards.
     * @param y the Yutsis object for which an operation has to be chosen
     * @param cg the cycle generator containing the relevant cycles of y
     * @see Yutsis
     */
    public EdgeCostHeuristic(Yutsis y){
	super(y);
    }
    
    /**
     * Constructs a new CycleCountHeuristic object and sets the problem.
     * This equivalent with calling the default constructor and setProblem
     * afterwards.
     * @param y the Yutsis object for which an operation has to be chosen
     * @param cg the cycle generator containing the relevant cycles of y
     * @see Yutsis
     * @see CycleGenerator
     */
    public EdgeCostHeuristic(Yutsis y, CycleGenerator cg){
	super(y,cg);
    }

    /**
     * Sets the Yutsis object defining for which operations must be choosen, 
     * together with the CycleGenerator delivering the relevant cycles.
     * @param y the Yutsis object for which an operation has to be chosen
     * @param cg the cycle generator containing the relevant cycles of y
     * @see Yutsis
     * @see CycleGenerator
     **/
    public void setProblem(Yutsis y, CycleGenerator cg){
	super.setProblem(y,cg);
	edgeCostTable = new EdgeCost[y.order()*(y.order()-1)/2];
	y.addChangeListener(this);
    }

    /** 
     * This function returns the best Cycle to reduce and fills
     * the best edge to interchange out of the cycle (min length 4). It
     * will use a CycleGenerator to generate all relevant
     * cycles. Afterwards it calculates all edgecosts and searches for
     * the smallest cycle with the edge with minimal cost.  When there
     * are multiple cycles of this kind the one with lowest cost is
     * returned.
     * @param bestcycleedge array where the best edge to be interchanged 
     *                      out of the Cycle will be filled in
     * @param besticnodes array where the best icnodes will be filled in
     * @param candidates ArrayList which will be filled with equivalent 
     *                   operations, the first corresponds to the returned
     *                   best Cycle.
     * @return the best Cycle
     * @see Cycle
     * @see CCAHeuristic
     **/
    public Cycle bestCycle(int [] bestcycleedge,int [] besticnodes, 
			   ArrayList candidates){
	if (cg.girth() == 3){
	    return (Cycle) cg.cycles(3).get(0);
	}
	if (stateChanged)
	    regenerate();
	Cycle bestcycle = null;
	int mincyclecost = y.order()*y.order();
	int bestcycleedgecost = y.order();
	int maxnrofbestedges = 0;
	ArrayList girthCycles = cg.cycles(cg.girth());
	ListIterator li = girthCycles.listIterator();
	ArrayList bestoperations = new ArrayList(); //per cycle
	while (li.hasNext()){//O(n^5) assuming #cycles = O(n^4)
	    bestoperations.clear();
	    Cycle c = (Cycle) li.next();
	    NodeIterator ni = c.first();
	    int first = ni.next();
	    int previous = first;
	    int cyclecost = 0;
	    int [] bestedge = {-1,-1};
	    int minedgecost = y.order();
	    int nrofbestedges = 0;
	    while (ni.hasNext()){ //O(n)
		int current = ni.next();
		int edgecost = edgeCost(previous,current).cost;
		cyclecost += edgecost;
		if (edgecost < minedgecost){
		    minedgecost = edgecost;
		    bestedge[0] = previous;
		    bestedge[1] = current;
		    nrofbestedges = 1;
		} 
		else if (edgecost == minedgecost){
		    nrofbestedges++;
		    int [] icnodes = 
			interchangeNodes(c,new int [] {previous,current});
		    bestoperations.add("IC " + previous + " " + current + 
				       " " + icnodes[0] + " " + icnodes[1]);
		}
		previous = current;
	    }
	    int edgecost = edgeCost(previous,first).cost;
	    cyclecost += edgecost;
	    if (edgecost < minedgecost){
		minedgecost = edgecost;
		bestedge[0] = previous;
		bestedge[1] = first;
	    } else if (edgecost == minedgecost){
		nrofbestedges++;
		int [] icnodes = 
		    interchangeNodes(c,new int [] {previous,first});
		bestoperations.add("IC " + previous + " " + first + 
				   " " + icnodes[0] + " " + icnodes[1]);
	    }
	    if (minedgecost < bestcycleedgecost ||
		(minedgecost == bestcycleedgecost 
		 && nrofbestedges < maxnrofbestedges) ||
		(minedgecost == bestcycleedgecost 
		 && nrofbestedges == maxnrofbestedges 
		 && cyclecost < mincyclecost)){
		bestcycleedgecost = minedgecost;
		bestcycleedge[0] = bestedge[0];
		bestcycleedge[1] = bestedge[1];
		int icnodes [] = interchangeNodes(c,bestcycleedge);
		besticnodes[0] = icnodes[0]; //Call by reference!!
		besticnodes[1] = icnodes[1];
		maxnrofbestedges = nrofbestedges;
		mincyclecost = cyclecost;
		bestcycle = c;
		candidates.clear();
		candidates.addAll(bestoperations);
	    }
	    else if (minedgecost == bestcycleedgecost 
		     && nrofbestedges == maxnrofbestedges 
		     && cyclecost == mincyclecost){
		candidates.addAll(bestoperations);
	    }
	}
	return bestcycle;
    }  

    /** 
     * Returns an EdgeCost object for the {@link Edge} <code>(i,j)</code>.
     * @param i endpoint of the Edge
     * @param j endpoint of the Edge
     * @return the cost of the Edge specified by its endpoints
     **/
    EdgeCost edgeCost(int i, int j){
	if (i > j)
	    return edgeCost(j,i);
	else if (i == j)
	    return null;
	return edgeCostTable[j*(j-1)/2+i];
    }
  
    /** 
     * Calculates the edge costs for the graph. The cost of an edge
     * is the difference in length of it's two shortest cycles in which
     * it is involved.
     **/
    void calculateEdgeCosts(){
	clearEdgeCosts();
	for (int i=cg.girth(); i < y.order(); i++){//O(n^5)
	    ArrayList c_i = cg.cycles(i);
	    if (c_i != null){
		ListIterator li = c_i.listIterator();
		while (li.hasNext()){
		    Cycle c = (Cycle) li.next();
		    NodeIterator ni = c.first();
		    int first = ni.next();
		    int previous = first;
		    while (ni.hasNext()){//O(n)
			int current = ni.next();
			updateEdgeCost(previous,current,c);
			previous = current;
		    }
		    updateEdgeCost(previous,first,c);
		}
	    }
	}
    }    
    
    /**
     * Prints all considered Cycle with the costs of their edges on
     * System.out.
     * @see Cycle
     **/  
    public void printCycleCosts(){
	printCycleCosts(System.out);
    }  

    /** 
     * Prints all considered Cycle with the costs of their edges on
     * the given PrintStream.
     * @param out the PrintStream to print to
     * @see Cycle
     **/
    public void printCycleCosts(PrintStream out){
	if (out == null)
	    return;
	for (int i=cg.girth(); i < y.order(); i++){
	    ArrayList c_i = cg.cycles(i);
	    if (c_i != null){
		out.println(c_i.size()+" cycles of length " + i);
		ListIterator li = c_i.listIterator();
		while (li.hasNext()){
		    Cycle c = (Cycle) li.next();
		    printCycleCost(c,out);
		    out.println();
		}
	    }
	}
    }
  
    /** 
     * Prints the given Cycle with the costs of his edges on
     * the given PrintStream.
     * @param c the Cycle to printed
     * @param out the PrintStream to print to
     * @see Cycle
     **/
    public void printCycleCost(Cycle c, PrintStream out){
	if (stateChanged)
	    regenerate();
	int previous, current, first;
	NodeIterator ni = c.first(); 
	first = current = ni.next();
	out.print(current);
	while (ni.hasNext()){
	    previous = current;
	    current = ni.next();
	    if (edgeCost(previous,current).cost != y.order())
		out.print("-"+edgeCost(previous,current).cost+"->"+current);
	    else
		out.print("-!!"+edgeCost(previous,current).cost+"!!->"+current);
	}
	if (edgeCost(current,first).cost != y.order())
	    out.print("-"+edgeCost(current,first).cost+"->"+first);
	else
	    out.print("-!!"+edgeCost(current,first).cost+"!!->"+first);
    }

    private void setEdgeCost(int i, int j, EdgeCost ec){
	if (i > j){
	    setEdgeCost(j,i,ec);
	    return;
	}
	else if (i == j)
	    throw new IllegalArgumentException("i == j");
	edgeCostTable[j*(j-1)/2+i] = ec;
    }
    
    private void updateEdgeCost(int previous, int current, Cycle c){
	EdgeCost ec = edgeCost(previous,current);
	if (ec == null)
	    setEdgeCost(previous,current,new EdgeCost(c.length()));
	else if (ec.shortestCycle > c.length()){
	    ec.cost = ec.shortestCycle - c.length();
	    ec.shortestCycle = c.length();
	}
	else if (ec.cost > c.length() - ec.shortestCycle) // added condition 
	    ec.cost =  c.length() - ec.shortestCycle;
    }

    private void clearEdgeCosts(){
	for (int i=0; i < y.order()*(y.order()-1)/2; i++)
	    edgeCostTable[i] = null;
    }

    /**
     * Implementation of the ChangeListener interface.
     **/
    public void stateChanged(ChangeEvent e){ 
	stateChanged = true; 
    }

    /**
     * Recalculates all edge costs, to be used when the graph is altered.
     **/
    private void regenerate(){
	stateChanged = false;
	calculateEdgeCosts();
    }
}

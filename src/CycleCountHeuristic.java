/* CycleCountHeuristic.java
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
import java.util.HashSet;

import java.io.PrintStream;

/**
 * This class implements two heuristics, MORE_SMALLER_LESS_BIGGER and
 * CYCLE_COUNT, for selecting operations for reducing a Yutsis object
 * bases on the difference in the number of cycles of each length
 * created by the different interchanges.  
 * 
 * <p> The MORE_SMALLER_LESS_BIGGER heuristic works as follows: For each
 * interchange reducing a girth cycle in length the number of cycles
 * becoming smaller and bigger are calculated. Let <var>g</var> be the
 * girth of the graph. The operation which reduces the maximum number
 * of <var>g</var>-cycles is preferred.  If this is equal the
 * operation which increases the minimum number of <var>g</var>-cycles
 * is preferred. If this is also equal, we repeat the comparison for
 * (<var>g</var>+1)-cycles, ..., (<var>g</var>+k)-cycles until a
 * difference is found.
 *
 * <p> The CYCLE_COUNT heuristic works as follows: For each
 * interchange the number of cycles of each length in the graph
 * resulting after applying the interchange are calculated. Let
 * <var>g</var> be the girth of the graph after applying the
 * considered interchange. The operation which yields the maximum
 * number of <var>g</var>-cycles is preferred.If this is equal, we
 * repeat the comparison for (<var>g</var>+1)-cycles, ...,
 * (<var>g</var>+k)-cycles until a difference is found.
 * @see AbstractCCAHeuristic
 * @see CCAHeuristic
 * @author Dries.VanDyck@rug.ac.be
 **/
public class CycleCountHeuristic extends AbstractCCAHeuristic{
    
    /** 
     * Strategy comparing the effect of interchanges by preferring 
     * interchanges making a maximum of small cycles smaller and if
     * equal a minimum number of making small cycles bigger is preferred.
     * This strategy is applied for each cycle length starting with
     * the girth and going up until a difference is found.
     **/
    static final int MORE_SMALLER_LESS_BIGGER = 1;

    /**
     * Strategy comparing the effect of interchanges by preferring
     * interchanges which result in a graph with a maximum number
     * of small cycles. This strategy is applied for each cycle length 
     * starting with the girth and going up until a difference is found.
     **/
    static final int CYCLE_COUNT = 2;

    /**
     * Attribute holding the current strategy for comparing the effect
     * of differen interchanges, possible values are MORE_SMALLER_LESS_BIGGER
     * and CYCLE_COUNT.
     * @see #MORE_SMALLER_LESS_BIGGER
     * @see #CYCLE_COUNT
     **/
    int strategy = MORE_SMALLER_LESS_BIGGER;

    /**
     * HashSet keeping track of isomorphic operations such that
     * for each set of isomorphic operations only one representative
     * is considered.
     **/
    HashSet forbidden;

    /**
     * Default constructor: constructs a new CycleCountHeuristic object
     **/ 
    public CycleCountHeuristic(){}

    /**
     * Constructs a new CycleCountHeuristic object and sets the problem.
     * This equivalent with calling the default constructor and setProblem
     * afterwards.
     * @param y the Yutsis object for which an operation has to be chosen
     * @param cg the cycle generator containing the relevant cycles of y
     * @see Yutsis
     */
    public CycleCountHeuristic(Yutsis y){
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
    public CycleCountHeuristic(Yutsis y, CycleGenerator cg){
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
	forbidden = new HashSet(y.nrOfEdges());
    }
    
    /** 
     * This function returns the best {@link Cycle} to reduce and fills
     * the best edge to interchange out of the cycle (min length 4) 
     * together with the best interchange nodes in a canonical way. It
     * will use a {@link CycleGenerator} to generate all relevant
     * cycles. 
     * @param bestcycleedge array where the best edge to be interchanged 
     *                      out of the {@link Cycle} will be filled in
     * @param besticnodes array where the best icnodes will be filled in
     * @param candidates ArrayList which will be filled with equivalent 
     *                   operations, the first corresponds to the returned
     *                   best Cycle.
     * @return the best {@link Cycle}
     **/
    public Cycle bestCycle(int [] bestcycleedge, int [] besticnodes, 
			   ArrayList candidates){//O(n^9)
	if (cg.girth() == 3){
	    System.err.println("RT " + cg.cycles(3).get(0));
	    return (Cycle) cg.cycles(3).get(0);
	}
	forbidden.clear();
	Cycle bestcycle = null;
	// Assumption: the best operation reduces a girth cycle
	ArrayList girthCycles = cg.cycles(cg.girth());
	ListIterator li = girthCycles.listIterator();
	ArrayList [] bestsmaller = null;
	ArrayList [] bestbigger = null;
	while (li.hasNext()){//O(n^9)
	    Cycle c = (Cycle) li.next();
	    NodeIterator ni = c.first();
	    int [] edge = new int[2];
	    int first = ni.next();
	    int previous = first;
	    while (ni.hasNext()){
		int current = ni.next();
		edge[0] = previous;
		edge[1] = current;
		int [] icnodes = 
		    interchangeNodes(c, edge);
		String operation = "IC " + edge[0] + " " + edge[1] + " " 
		    + icnodes[0] + " " + icnodes[1];
		if (forbidden.add(operation)){
		    ArrayList [] [] smallerbigger = 
			effect(edge[0],edge[1],icnodes[0],icnodes[1]);//O(n^5)
		    ArrayList [] smaller = smallerbigger[0];
		    ArrayList [] bigger = smallerbigger[1];
		    int result = 0;
		    result = bestsmaller == null ? 1 : //O(n)
			betterEffect(smaller,bigger,bestsmaller,bestbigger);
		    if (result == 1){
			candidates.clear();
			candidates.add(operation);
			bestcycle = c;
			bestsmaller = smaller;
			bestbigger = bigger;
			bestcycleedge[0] = edge[0];
			bestcycleedge[1] = edge[1];
			besticnodes[0] = icnodes[0];
			besticnodes[1] = icnodes[1];
		    }
		    else if (result == 0){
			candidates.add(operation);
		    }		
		}
		previous = current;
	    }
	    edge[0] = previous;
	    edge[1] = first;
	    int [] icnodes = 
		    interchangeNodes(c, edge);
	    String operation = "IC " + edge[0] + " " + edge[1] + " " 
		    + icnodes[0] + " " + icnodes[1];
	    if (forbidden.add(operation)){
		ArrayList [] [] smallerbigger = 
		    effect(edge[0],edge[1],icnodes[0],icnodes[1]);
		ArrayList [] smaller = smallerbigger[0];
		ArrayList [] bigger = smallerbigger[1];
		int result = 
		    betterEffect(smaller,bigger,bestsmaller,bestbigger);
		if (result == 1){
		    candidates.clear();
		    candidates.add(operation);
		    bestcycle = c;
		    bestsmaller = smaller;
		    bestbigger = bigger;
		    bestcycleedge[0] = edge[0];
		    bestcycleedge[1] = edge[1];
		    besticnodes[0] = icnodes[0];
		    besticnodes[1] = icnodes[1];
		}
		else if (result == 0){
		    candidates.add(operation);
		}
	    }
	}
	return bestcycle;
    }

    /**
     * Returns the current strategy to compare the effect of
     * interchanges. Possible values are BIGGERSMALLER and CYCLECOUNT.
     * @see #MORE_SMALLER_LESS_BIGGER
     * @see #CYCLE_COUNT
     **/
    public int strategy(){ return strategy;}

    /**
     * Sets the strategy to compare the effect of interchanges. 
     * Possible values are BIGGERSMALLER (default) and CYCLECOUNT.
     * @param strategy the chosen strategy
     * @see #MORE_SMALLER_LESS_BIGGER
     * @see #CYCLE_COUNT
     **/
    public void setStrategy(int strategy){ this.strategy = strategy;}

    /**
     * Calculates the effect of the interchange on the edge 
     * <code>(e1,e2)</code> interchanging the edges 
     * <code>(e1,a)</code> and <code>(e2,b)</code>, assuming that
     * the girth of <code>y</code> is at least 4.
     * @param e1 endpoint of the base edge of the interchange
     * @param e2 endpoint of the base edge of the interchange 
     * @param a endpoint of the edge (e1, a) to be interchanged 
     * @param b endpoint of the edge (e2, b) to be interchanged
     * @return an array of array of ArrayLists; the first contains
     *         all cycles which become smaller, the second all cycles
     *         become bigger; both have at index l cycles of length l+4
     **/
    public ArrayList[][] effect(int e1, int e2, int a, int b){ //O(n^5)
	ArrayList [] smaller = new ArrayList[y.order()-4];
	ArrayList [] bigger = new ArrayList[y.order()-4];
	for (int i=cg.girth(); i < y.order(); i++){//O(n^5)
	    ArrayList c_i = cg.cycles(i);
	    if (c_i != null){ // assuming #cycles O(n^4)
		ListIterator li = c_i.listIterator();
		while (li.hasNext()){
		    Cycle c = (Cycle) li.next();
		    int previous, current, next, nextnext;
		    previous = c.nodeAt(c.length()-2);
		    current = c.nodeAt(c.length()-1);
		    next = c.nodeAt(0);
		    nextnext = c.nodeAt(1);
		    if (current == e1 && next == e2){
			if (previous == a){
			    if (nextnext != b)
				add(smaller,c);
			}
			else if (nextnext == b)
			    add(smaller,c);
		    }
		    else if (current == e2 && next == e1){
			if (previous == b){
			    if (nextnext != a)
				add(smaller,c);
			}
			else if (nextnext == a)
			    add(smaller,c);
		    }
		    else
			for (int j = 0; j < c.length(); j++){//O(n)
			    previous = current;
			    current = next;
			    next = nextnext;
			    nextnext = c.nodeAt((j+2) % c.length());
			    // Edge in Cycle?
			    if (current == e1){
				if (next == e2){//Yes
				    if (previous == a){
					if (nextnext != b)
					    add(smaller,c);
				    }
				    else if (nextnext == b)
					add(smaller,c);
				}
				else
				    add(bigger,c);
				break;
			    }
			    else if (current == e2){
				if (next == e1){//Yes
				    if (previous == b){
					if (nextnext != a)
					    add(smaller,c);
				    }
				    else if (nextnext == a)
					add(smaller,c);
				}
				else
				    add(bigger,c);
				break;
			    }
			}   
		}
	    }
	}
	return new ArrayList [] [] {smaller,bigger};
    }

        /**
     * Prints the effect of the interchange on the edge 
     * <code>(e1,e2)</code> interchanging the edges 
     * <code>(e1,a)</code> and <code>(e2,b)</code>, assuming that
     * the girth of <code>y</code> is at least 4 to the given PrinStream.
     * @param e1 endpoint of the base edge of the interchange
     * @param e2 endpoint of the base edge of the interchange 
     * @param a endpoint of the edge (e1, a) to be interchanged 
     * @param b endpoint of the edge (e2, b) to be interchanged
     * @param out the PrintStream where the output wil be written
     **/
    public void printEffect(int e1, int e2, int a, int b, PrintStream out){
	ArrayList [] [] result = effect(e1,e2,a,b);
	ArrayList [] smaller = result[0];
	ArrayList [] bigger = result[1];
	for (int i = 0; i < y.order()-4; i++){
	    int nrsmaller = 0;
	    int nrbigger = 0;
	    if (smaller[i] != null){
		nrsmaller = smaller[i].size();
		out.println(nrsmaller
			    + " decreasing cycles of length " + (i+4) + ":");
		for (int j = 0; j < nrsmaller; j++)
		    out.println(smaller[i].get(j));
	    }
	    if (bigger[i] != null){
		nrbigger = bigger[i].size();
		out.println(nrbigger
			    + " increasing cycles of length " + (i+4) + ":");
		for (int j = 0; j < nrbigger; j++)
		    out.println(bigger[i].get(j));
	    }
	    if (nrbigger+nrsmaller > 0)
		out.println("#(increasing) - #(decreasing) = " 
			    + (nrbigger-nrsmaller));
	}
    }

    /**
     * Prints the effect of the interchange on the edge 
     * <code>(e1,e2)</code> interchanging the edges 
     * <code>(e1,a)</code> and <code>(e2,b)</code>, assuming that
     * the girth of <code>y</code> is at least 4 to System.out.
     * @param e1 endpoint of the base edge of the interchange
     * @param e2 endpoint of the base edge of the interchange 
     * @param a endpoint of the edge (e1, a) to be interchanged 
     * @param b endpoint of the edge (e2, b) to be interchanged
     **/
    public void printEffect(int e1, int e2, int a, int b){
	printEffect(e1,e2,a,b,System.out);
    }
   
    private void add(ArrayList [] al, Cycle c){
	if (al[c.length()-4] == null)
	    al[c.length()-4] = new ArrayList();
	al[c.length()-4].add(c);
    }
    
    private int betterEffect(ArrayList [] smaller, ArrayList [] bigger, 
				 ArrayList [] bestsmaller, 
			     ArrayList [] bestbigger){//O(n)
	switch (strategy){
	case MORE_SMALLER_LESS_BIGGER:
	
	    int nrsmaller, nrbigger, nrbestsmaller, nrbestbigger;
	    // Prefere IC's making more smaller cycles or if equal less bigger
	    // Note: Preferring IC's making more smaller than bigger 
	    //       performs badly
	    for (int i = 0; i < y.order()-4; i++){//O(n)
		nrsmaller = smaller[i] != null ? smaller[i].size() : 0; 
		nrbigger = bigger[i] != null ? bigger[i].size() : 0;
		nrbestsmaller = bestsmaller[i] != null ? bestsmaller[i].size() : 0;
		nrbestbigger = bestbigger[i] != null ? bestbigger[i].size() : 0;
		
		if (nrsmaller > nrbestsmaller)
		    return 1;
		else if (nrsmaller < nrbestsmaller)
		    return -1;
		else if (nrbigger > nrbestbigger)
		    return -1;
		else if (nrbigger < nrbestbigger)
		    return 1;	
	    }
	    return 0;
	case CYCLE_COUNT:
	    int nrcurrent, nrbestcurrent;
	
	    // Prefere IC's making more smaller cycles, taking into account
	    // cycles which are unit longer/shorter
	    nrcurrent = smaller[0] != null ? smaller[0].size() : 0; 
	    nrbestcurrent = bestsmaller[0] != null ? bestsmaller[0].size() : 0;
	    if (nrcurrent > nrbestcurrent)
		return 1;
	    else if (nrcurrent < nrbestcurrent)
		return -1;
	    // smaller[0] && bestsmaller[0] don't matter -> equal
	    nrcurrent = //- (smaller[0] != null ? smaller[0].size() : 0)
		- (bigger[0] != null ? bigger[0].size() : 0)
		+ (smaller[1] != null ? smaller[1].size() : 0); 
	    nrbestcurrent =//- (bestsmaller[0] != null ? bestsmaller[0].size() : 0)
		- (bestbigger[0] != null ? bestbigger[0].size() : 0)
		+ (bestsmaller[1] != null ? bestsmaller[1].size() : 0);
	    if (nrcurrent > nrbestcurrent)
		return 1;
	    else if (nrcurrent < nrbestcurrent)
		return -1;
	    for (int i = 1; i < y.order()-5; i++){//O(n)
		nrcurrent = (bigger[i-1] != null ? bigger[i-1].size() : 0) 
		    - (smaller[i] != null ? smaller[i].size() : 0)
		    - (bigger[i] != null ? bigger[i].size() : 0)
		    + (smaller[i+1] != null ? smaller[i+1].size() : 0); 
		nrbestcurrent = (bestbigger[i-1] != null ? 
				 bestbigger[i-1].size() : 0)
		    - (bestsmaller[i] != null ? bestsmaller[i].size() : 0)
		    - (bestbigger[i] != null ? bestbigger[i].size() : 0)
		    + (bestsmaller[i+1] != null ? bestsmaller[i+1].size() : 0);
	    
		if (nrcurrent > nrbestcurrent)
		    return 1;
		else if (nrcurrent < nrbestcurrent)
		    return -1;
	    }
	    return 0;
	default: 
	    return 0; //Better to throw an exception?
	}
    }
}

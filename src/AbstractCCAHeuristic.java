/* AbstractCCAHeuristic.java
   -------------------------
   2003 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

import java.util.ArrayList;

/**
 * This abstract class provides a skeleton implementation of the
 * CCAHeuristic interface to minimize the effort to implement this
 * interface.
 * @see CCAHeuristic
 * @author Dries.VanDyck@rug.ac.be
 **/
public abstract class AbstractCCAHeuristic implements CCAHeuristic{
    /**
     * The Yutsis object to reduce.
     * @see Yutsis
     **/
    Yutsis y;

    /**
     * The CycleGenerator delivering the relevant cycles for
     * the Yutsis object
     **/
    CycleGenerator cg;

    /**
     * Constructs a new AbstractCCAHeuristic without a defined problem,
     * a problem must be defined later by means of the setProblem method.
     * @see #setProblem
     **/ 
    public AbstractCCAHeuristic(){}

    /** 
     * Constructs a new AbstractCCAHeuristic with given Yutsis object
     * and corresponding CycleGenerator. Equivalent with the default
     * constructor followed by a call to setProblem.
     * @param y the Yutsis object for which an operation has to be chosen
     * @see Yutsis
     * @see CycleGenerator
     **/
    public AbstractCCAHeuristic(Yutsis y){ 
	setProblem(y);
    }

    /** 
     * Constructs a new AbstractCCAHeuristic with given Yutsis object
     * and corresponding CycleGenerator. Equivalent with the default
     * constructor followed by a call to setProblem.
     * @param y the Yutsis object for which an operation has to be chosen
     * @param cg the cycle generator containing the relevant cycles of y
     * @see Yutsis
     * @see CycleGenerator
     **/
    public AbstractCCAHeuristic(Yutsis y, CycleGenerator cg){ 
	setProblem(y,cg) ;
    }

    /**
     * Sets the Yutsis object for which operations must be choosen, a
     * CycleGenerator will be constructed for the given Yutsis object.
     * @param y the Yutsis object for an operation has to be chosen
     * @see Yutsis
     * @see CycleGenerator
     **/
    public void setProblem(Yutsis y){
	setProblem(y, new CycleGenerator(y));
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
	this.y = y; this.cg = cg;
    }

    /**
     * Returns the problem considered.
     * @return the Yutsis object defining the problem or null if no
     *         problem is defined.
     * @see Yutsis
     **/
    public Yutsis problem(){ return y; }

    /**
     * Returns the CycleGenerator used by this CCAHeuristic.
     * @return the CycleGenerator used by the heuristic or null if
     *         no CycleGenerator is used.
     * @see CycleGenerator
     */
    public CycleGenerator cycleGenerator(){ return cg; }

    /**
     * This method returns the nodes to be interchanged from the
     * <code>bestcycledge</code> in order to reduce the
     * <code>bestcycle</code> one unit in length and makes it
     * canonical, without applying the interchange itself.  The
     * canonical representation: take an edge (a.b)
     * with adjacent edges (a,c), (a,d), (b,e), (b,f) then an 
     * IC a b c e is valid iff a < b && c < d.
     * 
     * @param bestcycle the {@link Cycle} to be reduced
     * @param bestcycleedge the edge on which to apply the interchange 
     * @return the nodes to interchange of the edge <code>bestcycleedge</code>
     *         in order to reduce the cycle one unit in length.
     * @see Cycle 
     **/
    public int [] interchangeNodes(Cycle bestcycle, int [] bestcycleedge){
	// Best cycle and his best edge found -> reduce it's size together with
	// his neighbour cycle sharing the edge
	// Make Edge canonical
	if (bestcycleedge[0] > bestcycleedge[1]){
	    int temp;
	    temp = bestcycleedge[0];
	    bestcycleedge[0] = bestcycleedge[1];
	    bestcycleedge[1] = temp;
	}
	// Get neighbours in cycle
	int [] nghbcycle = {-1,-1};
	int index = 0;
	while(bestcycle.nodeAt(index) != bestcycleedge[0] && 
	      bestcycle.nodeAt(index) != bestcycleedge[1])
	    index++;
	if (index == 0 && 
	    (bestcycle.nodeAt(bestcycle.length()-1) == bestcycleedge[0]
	     || bestcycle.nodeAt(bestcycle.length()-1) == bestcycleedge[1]))
	    index = bestcycle.length()-1;
	if (bestcycle.nodeAt(index)==bestcycleedge[0]){
	    nghbcycle[0] = bestcycle.nodeAt((index-1+bestcycle.length()) 
					    % bestcycle.length());
	    nghbcycle[1] = bestcycle.nodeAt((index+2) % bestcycle.length());
	}
	else {
	    nghbcycle[1] = bestcycle.nodeAt((index-1+bestcycle.length())
					    % bestcycle.length());
	    nghbcycle[0] = bestcycle.nodeAt((index+2) % bestcycle.length());
	}	
	// Get neighbours out cycle
	int [] nghboutcycle = {-1,-1};
	int [] nghb0 = y.neighbors(bestcycleedge[0]);
	for (int i = 0; i < 3; i++)
	    if (nghb0[i] != nghbcycle[0] && nghb0[i] != bestcycleedge[1])
		nghboutcycle[0] = nghb0[i];
	int [] nghb1 = y.neighbors(bestcycleedge[1]);
	for (int i = 0; i < 3; i++)
	    if (nghb1[i] != nghbcycle[1] && nghb1[i] != bestcycleedge[0])
		nghboutcycle[1] = nghb1[i];
	// Make IC canonical
	return canonicalIC(bestcycleedge, 
			   new int [] {nghbcycle[0],nghboutcycle[1]},
			   new int [] {nghboutcycle[0],nghbcycle[1]});
    }

    /**
     * This method returns a canonical representation of the given 
     * interchange, without applying it.  The used
     * canonical representation: take an edge (a.b)
     * with adjacent edges (a,c), (a,d), (b,e), (b,f) then an 
     * IC a b c e is valid iff a < b && c < d.
     * 
     * @param icedge the nodes of the edge on which the interchange applies
     * @param icnodes the nodes to be interchanged
     * @return the nodes to interchange making the IC canonical
     **/
    public int [] canonicalIC(int [] icedge, int [] icnodes){
	int [] alticnodes = new int[2];
	int [] nghb0 = y.neighbors(icedge[0]);
	int [] nghb1 = y.neighbors(icedge[1]);
	for (int i = 0; i < 3; i++){
	    if (nghb0[i] != icedge[1] && nghb0[i] != icnodes[0])
		alticnodes[0] = nghb0[i];
	    if (nghb1[i] != icedge[0] && nghb1[i] != icnodes[1])
		alticnodes[1] = nghb1[i];
	}
	return canonicalIC(icedge,icnodes,alticnodes);
    }

    /**
     * This method returns a canonical representation of the given 
     * interchange, without applying it.  The used
     * canonical representation: take an edge (a.b)
     * with adjacent edges (a,c), (a,d), (b,e), (b,f) then an 
     * IC a b c e is valid iff a < b && c < d.
     * 
     * @param icedge the nodes of the edge on which the interchange applies
     * @param icnodes the nodes to be interchanged
     * @param alticnodes the alternative nodes to be interchanged yielding
     *                   the same effect.
     * @return the nodes to interchange making the IC canonical
     **/
    public int [] canonicalIC(int [] icedge, 
				   int [] icnodes, int [] alticnodes){
	return icnodes[0] < alticnodes[0] ? icnodes : alticnodes;
    }

    /** 
     * This function returns the best {@link Cycle} to reduce and
     * fills the best edge to interchange out of the cycle (min length
     * 4). It will use a {@link CycleGenerator} to generate all
     * relevant cycles.
     * @param bestcycleedge array where the best edge to be interchanged 
     *                      out of the {@link Cycle} will be filled in
     * @return the best {@link Cycle}
     **/
    public Cycle bestCycle(int [] bestcycleedge){
	int [] icnodes = new int[2];
	return bestCycle(bestcycleedge,icnodes);
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
     * @return the best {@link Cycle}
     * @see CycleCostAlgorithm
     **/
    public Cycle bestCycle(int [] bestcycleedge, int [] besticnodes){
	return bestCycle(bestcycleedge,besticnodes,new ArrayList());
    }
}



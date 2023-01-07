/* CCAHeuristic.java
   -----------------
   2003 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

import java.util.ArrayList;

/**
 * Interface of an heuristic to be used with the CycleCostAlgorithm
 * class to reduce a Yutsis object with at least operations possible.
 * @see CycleCostAlgorithm
 * @see Yutsis
 * @author Dries.VanDyck@rug.ac.be
 **/
public interface CCAHeuristic{

    /**
     * Sets the Yutsis object for which operations must be choosen, a
     * CycleGenerator will be constructed for the given Yutsis object.
     * @see Yutsis
     * @see CycleGenerator
     **/
    public void setProblem(Yutsis y);

    /**
     * Sets the Yutsis object defining for which operations must be choosen, 
     * together with the CycleGenerator delivering the relevant cycles.
     * @see Yutsis
     * @see CycleGenerator
     **/
    public void setProblem(Yutsis y, CycleGenerator cg);

    /**
     * Returns the problem considered.
     * @return the Yutsis object defining the problem or null if no
     *         problem is defined.
     * @see Yutsis
     **/
    public Yutsis problem();

    /**
     * Returns the CycleGenerator used by this CCAHeuristic.
     * @return the CycleGenerator used by the heuristic or null if
     *         no CycleGenerator is used.
     * @see CycleGenerator
     */
    public CycleGenerator cycleGenerator();

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
    public int [] interchangeNodes(Cycle bestcycle, int [] bestcycleedge);    

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
    public int [] canonicalIC(int [] icedge, int [] icnodes);
    
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
			      int [] icnodes, int [] alticnodes);

    /** 
     * This function returns the best {@link Cycle} to reduce and
     * fills the best edge to interchange out of the cycle (min length
     * 4). It will use a {@link CycleGenerator} to generate all
     * relevant cycles.
     * @param bestcycleedge array where the best edge to be interchanged 
     *                      out of the {@link Cycle} will be filled in
     * @return the best {@link Cycle}
     **/
    public Cycle bestCycle(int [] bestcycleedge);

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
    public Cycle bestCycle(int [] bestcycleedge, int [] besticnodes);

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
			   ArrayList candidates);
}

/* NodeIterator.java
   -----------------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

/** 
 * Iterator for iterating over the nodes of a Graph, Path or Cycle.
 * @see Path
 * @see Cycle
 * @see Graph
 * @author Dries.VanDyck@rug.ac.be
 **/
public interface NodeIterator{

    /** 
     * Returns true if this is not the last node in the iteration,
     * false otherwise.  
     * @return true if this is not the last node in the iteration,
     *         false otherwise.  
     **/
    public boolean hasNext();

    /** 
     * Returns the next node in this iteration or -1 if this is the
     * last.
     * @return the next node in this iteration or -1 if this is the
     *         last.
     **/
    public int next();

    /** 
     * Returns true if this is not the first node in the iteration,
     * false otherwise.  
     * @return true if this is not the first node in the iteration,
     *         false otherwise.  
     **/
    public boolean hasPrevious();

    /** 
     * Returns the previous node in this iteration or -1 if this is
     * the first.
     * @return the previous node in this iteration or -1 if this is
     *         the first.
     **/
    public int previous();
}

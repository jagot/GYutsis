/* GRVisitor.java
   --------------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

/** 
 * Interface following the Visitor pattern for visiting objects
 * of the GR-family.
 * @see GenRecoupCoeff 
 * @see GRCompoundFactor 
 * @see GRSummation 
 * @see GRFactor 
 * @see GRPreFactor 
 * @see GRKrDelta 
 * @see GR6jSymbol
 * @author Dries.VanDyck@rug.ac.be
 **/
public interface GRVisitor{
    /** 
     * Specific method for visiting a GenRecoupCoeff object.
     * @param grc the GenRecoupCoeff to be visited
     * @see GenRecoupCoeff
     **/
    public void visitGenRecoupCoeff(GenRecoupCoeff grc);

    /** 
     * Specific method for visiting a GRCompoundFactor object.
     * @param grcf the GRCompoundFactor to be visited
     * @see GRCompoundFactor
     **/
    public void visitGRCompoundFactor(GRCompoundFactor grcf);

    /** 
     * Specific method for visiting a GRSummation object.
     * @param grs the GRSummation to be visited
     * @see GRSummation
     **/
    public void visitGRSummation(GRSummation grs);

    /** 
     * Specific method for visiting a GRPreFactor object.
     * @param grpf the GRPreFactor to be visited 
     * @see GRPreFactor
     **/
    public void visitGRPreFactor(GRPreFactor grpf);

    /** 
     * Specific method for visiting a GRKrDelta object.
     * @param grcd the GRKrDelta to be visited
     * @see GRKrDelta
     **/
    public void visitGRKrDelta(GRKrDelta grcd);

    /** 
     * Specific method for visiting a GR6jSymbol object.
     * @param grpf the GR6jSymbol to be visited
     * @see GR6jSymbol
     **/
    public void visitGR6jSymbol(GR6jSymbol gr6j);
    
    /**
     * The resulting object representing the visited object(s).
     **/
    public Object result();
}

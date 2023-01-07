/* GRFactor.java
   ---------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

/** 
 * Abstract class representing an elementary factor in the formula
 * of a General Recoupling Coefficient of angular momenta in terms of
 * 6j-coefficients.
 * @see GenRecoupCoeff 
 * @see Edge 
 * @see GRVisitor
 * @author Dries.VanDyck@rug.ac.be
 **/
abstract public class GRFactor implements Cloneable{
    
    /** 
     * Returns a String representation of this elementary factor.
     * @return a String representation of this GRFactor
     **/
    abstract public String toString();

    /** 
     * Returns an exact copy of this GRFactor
     * @return a clone of this GRFactor.
     **/
    public Object clone(){
	GRFactor grf = null;
	try{ grf = (GRFactor) super.clone();}
	catch(CloneNotSupportedException e){}//should not be possible
	return grf;
    }

    /** 
     * True if this elemtary factor contains the label <code>label</code>.
     * @param label the label to be checked
     * @return true if the label appears in this GRFactor, false otherwise
     **/
    abstract public boolean containsLabel(String label);

    /** 
     * Implementation of the Visitor pattern.
     * @param v the GRVisitor to be accepted
     * @see GRVisitor
     **/
    abstract public void accept(GRVisitor v);
}

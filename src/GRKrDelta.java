/* GRKrDeltaFactor.java
   --------------------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

/** 
 * Class representing a factor KrDelta(l1,l2) in a
 * General Recoupling Coefficient. Objects of this class are immutable.
 * @see GenRecoupCoeff 
 * @see Edge 
 * @see GRFactor 
 * @see GRVisitor
 * @author Dries.VanDyck@rug.ac.be
 **/
public class GRKrDelta extends GRFactor{
    String l1, l2;
    
    /** 
     * Constructs a new GRKrDelta ChDelta(l1,l2).
     * @param l1 first argument of the Chronicle Delta 
     * @param l2 second argument of the Chronicle Delta 
     **/
    public GRKrDelta(String l1, String l2){
	this.l1 = l1;
	this.l2 = l2;
    }
    
    /** 
     * True if label appears in this GRKrDelta, false otherwise.
     * @param label the label to be checked
     * @return true if the label appears in this GRPreFactor, false otherwise
     **/
    public boolean containsLabel(String label){
	return l1.equals(label) || l2.equals(label);
    }
    
    /**
     * Returns the arguments of the GRKrDelta object.
     * @return the arguments of the GRKrDelta as a String array.
     **/
    public String[] args(){
	return new String [] {l1,l2};
    }

    /** 
     * A String representation of this GRKrDelta object.
     * @return a String representation of this GRKrDelta
     **/
    public String toString(){
	return "delta("+l1+","+l2+")";
    }

    /** 
     * Implementation of the Visitor pattern.
     * @param v the GRVisitor to be accepted
     * @see GRVisitor
     **/
    public void accept(GRVisitor v){
	v.visitGRKrDelta(this);
    }
}

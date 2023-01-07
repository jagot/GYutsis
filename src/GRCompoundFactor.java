/* GRCompoundFactor.java
   ---------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/
import java.util.ArrayList;
import java.util.Iterator;

/** 
 * Class represting a product of elementary GRFactor's.
 * @see GenRecoupCoeff 
 * @see GRFactor 
 * @see GRVisitor
 * @author Dries.VanDyck@rug.ac.be 
 **/
public class GRCompoundFactor implements Cloneable{ 
    /** 
     * The GRFactor's in this product. First element is
     * always a GRPreFactor or null.
     **/
    ArrayList grfactors;

    /** 
     * Constructs a new (empty) product of GRFactor's.
     * @see GRFactor
     **/
    public GRCompoundFactor(){
	grfactors = new ArrayList();
	grfactors.add(null);
    }

    /** 
     * Constructs a new (empty) product of GRFactor's with
     * <code>nroffactors</code> expected number of factors.
     * @param nroffactors the expected number of factors
     * @see GRFactor
     **/
    public GRCompoundFactor(int nroffactors){
	grfactors = new ArrayList(nroffactors);
	grfactors.add(null);
    }

    /** 
     * Constructs a new product of GRFactor's with initial
     * GRFactor <code>f</code>.
     * @param f the intial {@link GRFactor}
     * @see GRFactor
     **/
    public GRCompoundFactor(GRFactor f){
	grfactors = new ArrayList();
	if (!(f instanceof GRPreFactor))
	    grfactors.add(null);
	grfactors.add(f);
    }

    /** 
     * Constructs a new product of GRFactor's with initial
     * GRFactor <code>f</code> and <code>nroffactors</code> expected
     * number of factors.
     * @param nroffactors the expected number of factors
     * @param f the intial GRFactor
     * @see GRFactor
     **/
    public GRCompoundFactor(int nroffactors, GRFactor f){
	grfactors = new ArrayList(nroffactors);
	if (!(f instanceof GRPreFactor))
	    grfactors.add(null);
	grfactors.add(f);
    }

    /** 
     * Copy constructor.
     * @param grcf the GRCompoundFactor to be duplicated
     **/
    GRCompoundFactor(GRCompoundFactor grcrf){
	grfactors = new ArrayList(grcrf.grfactors);
	if (grfactors.get(0) != null)
	    grfactors.set(0,new GRPreFactor((GRPreFactor) grfactors.get(0)));
    }
    
    /** 
     * A String representation of this GRCompoundFactor.
     * @return a String representation of this GRCompoundFactor
     **/
    public String toString(){
	StringBuffer bf = new StringBuffer();
	if (grfactors.get(0) != null)
	    bf.append(grfactors.get(0).toString()); 
	for (int i=1; i < grfactors.size(); i++)
	    bf.append(grfactors.get(i).toString());
	return bf.toString();
    }

    /** 
     * Returns a clone of this GRCompoundFactor object.
     * @return a clone of this GRCompoundFactor
     **/
    public Object clone(){
	GRCompoundFactor grcf = null;
	try{ grcf = (GRCompoundFactor) super.clone(); }
	catch(CloneNotSupportedException e){} //should not be possible
	grcf.grfactors = (ArrayList) grfactors.clone();
	// All but first are immutable
	if (grfactors.get(0) != null)
	    grfactors.set(0,((GRPreFactor) grfactors.get(0)).clone());
	return grcf;
    }

    /** 
     * Implementation of the Visitor pattern. Visits first itself and
     * than calls accept on its composites.
     * @param v the GRVisitor to be accepted
     * @see GRVisitor
     **/
    public void accept(GRVisitor v){
	v.visitGRCompoundFactor(this);
	if (grfactors.get(0) != null)
	    ((GRPreFactor) grfactors.get(0)).accept(v);
	for (int i=1; i < grfactors.size(); i++)
	    ((GRFactor) grfactors.get(i)).accept(v);
    }

    /** 
     * Appends a factor <code>(-1)^(j[0] + ... +j[j.length-1])</code> 
     * to this GRCompoundFactor.
     * @param j array of labels to be appended
     **/
    public void appendExp(String [] j){
	if (grfactors.get(0) == null)
	    grfactors.set(0, new GRPreFactor(j));
	else
	    ((GRPreFactor) grfactors.get(0)).appendExp(j);
    }

    /** 
     * Appends a factor <code>(-1)^(factor*j)</code> to this
     * GRCompoundFactor.
     * @param factor the factor of the label to be appended
     * @param j the label to be appended
     **/
    public void appendExp(int factor,String j){ 
	if (grfactors.get(0) == null) {
	    grfactors.set(0, new GRPreFactor(factor,j));
	} 
	else 
	    ((GRPreFactor)
	     grfactors.get(0)).appendExp(factor,j); 
    }

    /** 
     * Appends the factor <code>grf</code>.
     * @param grf the GRFactor to be appended
     * @see GRFactor
     **/
    public void append(GRFactor grf){
	if (grf instanceof GRPreFactor)
	    if (grfactors.get(0) == null)
		grfactors.set(0,grf);
	    else
		((GRPreFactor)
		 grfactors.get(0)).append((GRPreFactor) grf);
	else
	    grfactors.add(grf);
    }

    /** 
     * Appends a factor <code>(2*a+1)^(exp/2)</code> to this
     * GRCompoundFactor.
     * @param a the label of the weight to be appended
     * @param exp the 2*exponent of the weight
     **/
    public void appendFactor(String a, int exp){ 
	if (grfactors.get(0) == null) {
	    grfactors.set(0,new GRPreFactor(a,exp));
	} 
	else 
	    ((GRPreFactor)
	     grfactors.get(0)).appendFactor(a,exp); 
    }

    /** 
     * Appends a factor
     * <code>((2*a[0]+1)*...*(2*a[a.length-1]+1))^(exp/2)</code> to
     * this GRCompoundFactor.
     * @param a array of labels of weights to be appended
     * @param exp the 2*exponent of all the weights to be appended
     **/
    public void appendFactor(String [] a, int exp){ 
	if (grfactors.get(0) == null) {
	    grfactors.set(0, new GRPreFactor(a,exp));
	} 
	else 
	    ((GRPreFactor)
	     grfactors.get(0)).appendFactor(a,exp); 
    }

    /** 
     * True if this GRCompoundFactor contains a factor with label
     * <code>label</code>, false otherwise.
     * @param label the label to be checked
     * @return true if the label appears in this GRCompoundFactor,
     *         false otherwise
     **/
    public boolean containsLabel(String label){
	for (int i = 0; i < grfactors.size(); i++)
	    if (((GRFactor) grfactors.get(i)).containsLabel(label))
		return true;
    return false;
    }

    /** 
     * Returns an Iterator of the factors in this GRCompoundFactor
     * object.
     * @return an Iterator over the {@link GRFactor}s
     * @see GRFactor
     **/
    public Iterator factors(){ return grfactors.iterator();}
}

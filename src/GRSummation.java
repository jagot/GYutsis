/* GRSummation.java
   ----------------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

/** 
 * Class representing a summation factor 
 * <code> sum(summvar) </code> * 
 * {@link GRCompoundFactor} appearing in the expression for a 
 * General Recoupling Coefficient for angular momenta.
 * @see GenRecoupCoeff 
 * @see Edge 
 * @see GRCompoundFactor 
 * @see GRPreFactor 
 * @see GRFactor
 * @see GRVisitor
 * @author Dries.VanDyck@rug.ac.be
 **/
public class GRSummation extends GRCompoundFactor{
    /** 
     * The label of the summation variable of this summation factor.
     **/
    String summvar;
    // (a,b,summvar) are coupled in a node
    String a,b;
    // (c,d, summvar) are coupled in a node 
    String c,d;

    /** 
     * Constructs a new GRSummation object with summation variable
     * <code>summvar</code> with couplings <code>(a,b,summvar)</code>
     * and <code>(c,d,summvar)</code>.
     * @param summvar the label of the summation variable
     * @param a first neighbor at first side 
     * @param b second neighbor at firs side
     * @param c first neighbor at second side
     * @param d second neighbor at second side
     **/
    public GRSummation(String summvar, String a, String b, String c, String d){
	super();
	this.summvar = summvar;
	this.a = a; this.b = b;
	this.c = c; this.d = d;
    }

    /** 
     * Constructs a new GRSummation object with summation variable
     * <code>summvar</code> and <code>nroffactors</code> expected
     * number of factors.
     * @param summvar the label of the summation variable
     * @param a first neighbor at first side 
     * @param b second neighbor at firs side
     * @param c first neighbor at second side
     * @param d second neighbor at second side
     * @param nroffactors expected number of factors
     **/
    public GRSummation(String summvar, String a, String b, String c, String d,
		       int nroffactors){
	super(nroffactors);
	this.summvar = summvar;
	this.a = a; this.b = b;
	this.c = c; this.d = d;
    }

    /** 
     * Constructs a new GRSummation object with summation variable
     * <code>summvar</code> and factor <code>f</code>.
     * @param summvar the label of the summation variable
     * @param a first neighbor at first side 
     * @param b second neighbor at firs side
     * @param c first neighbor at second side
     * @param d second neighbor at second side
     * @param f initial factor {@link GRFactor}
     * @see GRFactor
     **/
    public GRSummation(String summvar,String a, String b, String c, String d,
		       GRFactor f){
	super(f);
	this.summvar = summvar;
	this.a = a; this.b = b;
	this.c = c; this.d = d;
    }

    /** 
     * Constructs a new GRSummation object with summation variable
     * <code>summvar</code>, initial factor <code>f</code> and 
     * <code>nroffactors</code> expected number of factors.
     * @param summvar the label of the summation variable
     * @param a first neighbor at first side 
     * @param b second neighbor at firs side
     * @param c first neighbor at second side
     * @param d second neighbor at second side
     * @param f initial factor {@link GRFactor}
     * @param nroffactors expected number of factors
     * @see GRFactor
     **/
    public GRSummation(String summvar,String a, String b, String c, String d,
		       int nroffactors, GRFactor f){
	super(nroffactors,f);
	this.summvar = summvar;
	this.a = a; this.b = b;
	this.c = c; this.d = d;
    }

    /** 
     * Copy constructor.
     * @param grs the GRSummation object to be duplicated
     **/
    GRSummation(GRSummation grs){
	super(grs);
	summvar = grs.summvar;
	a = grs.a; b = grs.b;
	c = grs.c; d = grs.d;
    }

    /** 
     * Returns the label of the summation variable of this summation
     * factor.
     * @return the label of the summation variable
     **/
    public String summVar() { return summvar;}

    /** 
     * Returns the labels of the edges coupled with the summatian
     * variable s; suppose s was coupled as (a,b,s) and (c,d,s)
     * than {a,b,c,d} is returned.
     * @return array containing the labels of the neighbors at each
     *         side of the summation variable
     **/
    public String [] couplings() {
	String [] couplings = {a,b,c,d};
	return couplings;
    }

    /** 
     * Returns a String representation of this GRSummation object.
     * @return a String representation of this GRSummation object
     **/
    public String toString(){
	StringBuffer bf = new StringBuffer();
	bf.append("sum("+summvar+ 
		  ",max(|"+a+"-"+b+"|,|"+c+"-"+d+ //lower bound
		  "|)..min("+a+"+"+b+","+c+"+"+d+"))") //upperbound
	    .append(super.toString());
	return bf.toString();
    }

    /** 
     * Returns a clone of this GRSummation object.
     * @return a clone of this GRSummation object
     **/
    public Object clone(){
	return (GRSummation) super.clone();
    }

    /** 
     * Implementation of the Visitor pattern. Visits first itself and then
     * calls accept on it's composites.
     * @param v the GRVisitor to be accepted
     * @see GRVisitor
     **/
    public void accept(GRVisitor v){
	v.visitGRSummation(this);
	if (grfactors.get(0) != null)
	    ((GRPreFactor) grfactors.get(0)).accept(v);
	for (int i=1; i < grfactors.size(); i++)
	    ((GRFactor) grfactors.get(i)).accept(v);
    }
}

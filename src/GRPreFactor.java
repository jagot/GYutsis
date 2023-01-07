/* GRPreFactor.java
   ---------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/
import java.util.HashMap;
import java.util.Iterator;

/** 
 * Class representing a factor
 * <code>(-1)^exp*(2*f_0+1)^(exp_0/2)+...+(2*f_k+1)^(exp_k/2)</code>
 * with <code>exp</code> a lineair combination of labels with
 * positive, integer coefficients, f_0...f_k labels and exp_0..exp_k
 * integer powers.
 * @see GenRecoupCoeff 
 * @see Edge
 * @see GRFactor 
 * @see GRVisitor
 * @author Dries.VanDyck@rug.ac.be
 **/
public class GRPreFactor extends GRFactor{
    /** 
     * HashMap with the labels as keys and the coefficients of the
     * labels as values.
     **/
    HashMap exp;
    /** 
     * HashMap with the labels as keys and their exponents*2 (to avoid
     * non-integers) as values.
     **/
    HashMap factors; 

    /** 
     * Constructs a new GRPreFactor.
     **/
    public GRPreFactor(){
	exp = new HashMap();
	factors = new HashMap();
    }

    /** 
     * Constructs a GRPreFactor with <code>nroflabels</code> expected
     * total number of labels
     * @param nroflabels the expected number of labels.
     **/
    public GRPreFactor(int nroflabels){
	exp = new HashMap(nroflabels*4/3);
	factors = new HashMap(nroflabels*4/3);
    }

    /** 
     * Constructs a new GRPreFactor representing 
     * <code>(-1)^(j[0]+..+j[j.length-1])</code>.
     * @param j array of labels of the exponent of the phase factor
     **/
    public GRPreFactor(String [] j){
	this();
	appendExp(j);
    }

    /** 
     * Constructs a new GRPreFactor representing 
     * <code>(-1)^(factor*j)</code>.
     * @param factor the factor of the specified label
     * @param j the label of the exponent of the phase factor
     **/
    public GRPreFactor(int factor, String j){
	this();
	appendExp(factor,j);
    }

    /** 
     * Constructs a new GRPreFactor representing
     * <code>(-1)^(j[0]+..+j[j.length-1])</code> with
     * <code>nroflabels</code> expected number of labels..
     * @param nroflabels the expected number of labels
     * @param j array of labels of the exponent of the phase factor
     **/
    public GRPreFactor(int nroflabels, String [] j){
	this(nroflabels);
	appendExp(j);
    }

    /** 
     * Constructs a new GRPreFactor representing
     * <code>(-1)^(factor*j)</code> with <code>nroflabels</code>
     * expected number of labels.
     * @param nroflabels the expected number of labels
     * @param factor the factor of the specified label
     * @param j the label of the exponent of the phase factor
     **/
    public GRPreFactor(int nroflabels, int factor, String j){
	this(nroflabels);
	appendExp(factor,j);
    }

    /** 
     * Constructs a new GRPreFactor representing
     * <code>(2*a+1)^(exp/2)</code>.
     * @param a label of the weight
     * @param exp 2*exponent of the weight
     **/
    public GRPreFactor(String a, int exp){
	this();
	appendFactor(a,exp);
    }

    /** 
     * Constructs a new GRPreFactor representing
     * <code>(2*a+1)^(exp/2)</code> with <code>nroflabels</code>
     * expected number of labels.
     * @param nroflabels the expected number of labels
     * @param a label of the weight
     * @param exp 2*exponent of the weight
     **/
    public GRPreFactor(int nroflabels, String a, int exp){
	this(nroflabels);
	appendFactor(a,exp);
    }

    /** 
     * Constructs a new GRPreFactor representing <code>((2*a[0]+1)* ... *
     * (2a[a.length-1]+1))^(exp/2)</code>.
     * @param a array of labels of the weights
     * @param exp 2*exponent of the weights
     **/
    public GRPreFactor(String [] a, int exp){
	this();
	appendFactor(a,exp);
    }

    /** 
     * Constructs a new GRPreFactor representing <code>((2*a[0]+1)* ... *
     * (2a[a.length-1]+1))^(exp/2)</code> with <code>nroflabels</code>
     * expected number of labels.
     * @param nroflabels the expected number of labels
     * @param a arrary of labels of the weights
     * @param exp 2*exponent of the weights
     **/
    public GRPreFactor(int nroflabels, String [] a, int exp){
	this(nroflabels);
	appendFactor(a,exp);
    }

    /** 
     * Constructs a new GRPreFactor representing
     * <code>(2*a[0]+1)^(exp[0]/2)* ... *
     * (2a[a.length-1]+1)^(exp[a,length-1]/2)</code>.
     * @param a array of labels of the weights
     * @param exp array of 2*exponents of the weights
     **/
    public GRPreFactor(String [] a, int [] exp){
	this();
	appendFactor(a,exp);
    }

    /** 
     * Constructs a new GRPreFactor representing
     * <code>(2*a[0]+1)^exp[0]* ... *
     * (2a[a.length-1]+1))^(exp[a.length-1]/2)</code> with
     * <code>nroflabels</code> expected number of labels.
     * @param nroflabels the expected number of labels
     * @param a array of labels of the weights
     * @param exp array of 2*exponents of the weights
     **/
    public GRPreFactor(int nroflabels, String [] a, int [] exp){
	this(nroflabels);
	appendFactor(a,exp);
    }

    /** 
     * Constructs a new GRPreFactor representing
     * <code>(-1)^(factor*j)*(2*a+1)^(exp/2)</code> with
     * <code>nroflabels</code> expected number of labels.
     * @param nroflabels the expected number of labels
     * @param factor factor of the label of the exponent of the phase factor
     * @param j label of the exponent of the phase factor
     * @param a label of the weight
     * @param exp 2*exponent of the weight
     **/
    public GRPreFactor(int nroflabels, int factor, String j, String a, 
		       int exp){
	this(nroflabels);
	appendExp(factor,j);
	appendFactor(a,exp);
    }

    /** 
     * Copy constructor.
     * @param gpf the GRPreFactor to be duplicated
     **/
    GRPreFactor(GRPreFactor gpf){
	exp = new HashMap(gpf.exp);
	factors = new HashMap(gpf.factors);
    }
    
    /** 
     * True if this GRPreFactor contains this label, false
     * otherwise.
     * @param label the label to be checked
     * @return true if the label appears in this GRPreFactor, false otherwise
     **/
    public boolean containsLabel(String label){
	return exp.containsKey(label);
    }

    /** 
     * Appends a factor <code>(-1)^(j[0]+..+j[j.length-1])</code> to this
     * GRPreFactor.
     * @param j array of labels to be appended to the exponent of the phase
     *        factor
     **/
    public void appendExp(String [] j){
	Integer coeff;
	for (int i = 0; i < j.length; i++){
	    coeff = (Integer) exp.remove(j[i]);
	    if (coeff == null)
		exp.put(j[i],new Integer(1));
	    else {
		Integer newcoeff = new Integer((coeff.intValue()+1) % 4);
		if (newcoeff.intValue() != 0)
		    exp.put(j[i],newcoeff);
	    }
	}
    }

    /** 
     * Appends a factor <code>(-1)^(factor*j)</code> to this
     * GRPreFactor.
     * @param factor the factor of the label to be appended to the exponent
     *               of the phase factor
     * @param j label to be appended to the exponent of the phase factor
     **/
    public void appendExp(int factor, String j){
	if (factor % 4 == 0)
	    return;
	Integer coeff;
	coeff = (Integer) exp.remove(j);
	if (coeff == null)
	    exp.put(j,new Integer(factor));
	else{
	    Integer newcoeff = new Integer((coeff.intValue()+factor) % 4);
	    if (newcoeff.intValue() != 0)
		exp.put(j,newcoeff);
	}
    }

    /** 
     * Appends a factor
     * <code>(-1)^(factor*j[0]+...+factor*j[j.length-1])</code> to
     * this GRPreFactor.
     * @param factor the factor of the labels to be appended to the exponent
     *               of the phase factors
     * @param j array of labels to be appended to the exponent of the phase 
     *          factor
     **/
    public void appendExp(int factor, String [] j){
	for (int i = 0; i < j.length; i++)
	    appendExp(factor,j[i]);
    }

    /** 
     * Appends the GRPreFactor object <cod>gpf</code> to this GRPreFactor.
     * @param gpf the {@link GRPreFactor} to be appended
     **/
    public void append(GRPreFactor gpf){
	Iterator labels = gpf.expLabels();
	Iterator coeffs = gpf.expCoeffs();
	while (labels.hasNext()){
	    Integer coeff = (Integer) coeffs.next();
	    String label = (String) labels.next();
	    Integer mycoeff = (Integer) exp.remove(label);
	    if (mycoeff == null)
		exp.put(label,coeff);
	    else {
		int newcoeff = (mycoeff.intValue() + coeff.intValue()) % 4;
		if (newcoeff != 0)
		    exp.put(label, new Integer(newcoeff));
	    }
	}
    }

    /** 
     * Returns true if this GRPreFactor represents (-1)^0*factors.
     * @return true, if the phase factor equals 1, false otherwise
     **/
    public boolean expEmpty(){ return exp.isEmpty();}

    /**
     * Returns the number of labels present in the exponent of (-1).
     * @return the number of labels in the exponent of (-1)
     **/
    public int expSize() { return exp.size(); }

    /** 
     * Appends a factor <code>(2*a+1)^(exp/2)</code> to this GRPreFactor.
     * @param a the label of the weight to be appended
     * @param exp 2*exponent of the weight to be appended
     **/
    public void appendFactor(String a, int exp){
	Integer currentexp;
	currentexp = (Integer) factors.remove(a);
	if (currentexp == null)
	    factors.put(a,new Integer(exp));
	else if (currentexp.intValue()+exp != 0)
	    factors.put(a,new Integer(currentexp.intValue()+exp));
    }

    /** 
     * Appends a factor <code>(2*a+1)</code> to this GRPreFactor.
     * @param a the label of the weight to be appended
     **/
    public void appendFactor(String a){ appendFactor(a,2); }

    /** 
     * Appends a factor <code>((2*a[0]+1)* ... *
     * (2a[a.length-1]+1))^(exp/2)</code> to this GRPreFactor.
     * @param a array of labels of the weights to be appended
     * @param exp 2*exponent of the weights to be appended
     **/
    public void appendFactor(String [] a, int exp){
	for (int i = 0; i < a.length; i++)
	    appendFactor(a[i],exp);
    }

    /** 
     * Appends a factor <code>(2*a[0]+1)^(exp[0]/2)* ... *
     * (2a[a.length-1]+1)^(exp[a.length-1]/2)</code> to this GRPreFactor.
     * @param a array of labels of the weights to be appended
     * @param exp array of 2*exponents of the weights to be appended
     **/
    public void appendFactor(String [] a, int [] exp){
	for (int i = 0; i < a.length; i++)
	    appendFactor(a[i],exp[i]);
    }

    /** 
     * Appends a factor <code>(2*a[0]+1)* ... *
     * (2a[a.length-1]+1)</code> to this GRPreFactor.
     * @param a array of labels of the weights to be appended
     **/
    public void appendFactor(String [] a){ appendFactor(a,2);}

    /** 
     * Returns true if this GRPreFactor represents (-1)^exp*1.
     * @return true if the weights equals 1, false otherwise
     **/
    public boolean factorsEmpty(){ return factors.isEmpty();}

    /**
     * Returns the number of factors in the product.
     * @return the number of factors in the product
     **/
    public int factorsSize(){ return factors.size();}

    /** 
     * Returns true if this GRPreFactor represents 1, i.e. (-1)^0*1.
     * @return true if this GRPreFactor equals 1, false otherwise
     **/
    public boolean empty(){ return expEmpty() && factorsEmpty();}

    /** 
     * Returns an Iterator over the labels in the exponent of (-1).
     * To be used in collaboration with {@link GRPreFactor#expCoeffs}.
     * @return an Iterator over the labels of the exponent of the 
     *         phase factor
     * @see #expCoeffs
     **/
    public Iterator expLabels(){ return exp.keySet().iterator(); }

    /** 
     * Returns an Iterator of the coeffs in the exponent of (-1).
     * To be used in collaboration with {@link GRPreFactor#expLabels}.
     * @return an Iterator over the coefficients of the labels of 
     *         the exponent of the phase factor
     * @see #expLabels
     **/
    public Iterator expCoeffs(){ return exp.values().iterator(); }

    /** 
     * Returns an Iterator of the labels in the factors
     * (2*label+1)^(exp/2). To be used in collaboration with {@link
     * GRPreFactor#factorsExps}.
     * @return an Iterator over the labels of the weights
     * @see #factorsExps 
     **/
    public Iterator factorsLabels(){ return factors.keySet().iterator();}

    /** 
     * Returns an Iterator over the exponents in the factors
     * (2*label+1)^(exp/2). To be used in collaboration with {@link
     * GRPreFactor#factorsLabels}.
     * @return an Iterator over the exponents of the weights 
     * @see #factorsLabels
     **/
    public Iterator factorsExps(){ return factors.values().iterator();}

    /** 
     * A String representation of this GRPreFactor.
     * @return a String representation of this GRPreFactor
     **/
    public String toString(){
	StringBuffer bf = new StringBuffer();
	if (!expEmpty()){
	    bf.append("(-1)^(");
	    Iterator labels = expLabels();
	    Iterator coeffs = expCoeffs();
	    int coeff = ((Integer) coeffs.next()).intValue();
	    String label = (String) labels.next();
	    switch (coeff){
	    case 1:
		bf.append(label); break;
	    case 2:
		bf.append("2*").append(label); break;
	    case 3:
		bf.append('-').append(label); break;
	    default:
		System.err.println("Bug in GRPreFactor");
	    }
	    while (labels.hasNext()){
		coeff = ((Integer) coeffs.next()).intValue();
		label = (String) labels.next();
		switch (coeff){
		case 1:
		    bf.append('+').append(label); break;
		case 2:
		    bf.append("+2*").append(label); break;
		case 3:
		    bf.append('-').append(label); break;
		}
	    }
	    bf.append(')');
	}
	if (!factorsEmpty()){
	    if (!expEmpty()){
		bf.append('*');
	    }
	    Iterator labels = factorsLabels();
	    Iterator exps = factorsExps();
	    int exp = ((Integer) exps.next()).intValue();
	    String label = (String) labels.next();
	    bf.append("(2*"+label+"+1)" + (exp == 2 ? "" : 
					 exp % 2 == 0 ?  
					 exp > 0 ? "^" + exp/2 
					 : "^(" + exp/2 + ")"
					 : "^(" + exp + "/2)"));
	    while (labels.hasNext()){
		exp = ((Integer) exps.next()).intValue();
		label = (String) labels.next();
		bf.append("*(2*"+label+"+1)" + (exp == 2 ? "" : 
					 exp % 2 == 0 ?  
					 exp > 0 ? "^" + exp/2 
					 : "^(" + exp/2 + ")"
					 : "^(" + exp + "/2)"));
	    }
	}
	return bf.toString();
    }

    /** 
     * Returns a clone of this object, immutable objects are not cloned.
     * @return a clone of this GRPreFactor.
     **/
    public Object clone(){
	GRPreFactor gpf = null;
	gpf = (GRPreFactor) super.clone();
	gpf.exp = (HashMap) exp.clone();
	gpf.factors = (HashMap) factors.clone();
	return gpf;
    }

    /** 
     * Implementation of the Visitor pattern.
     * @param v the {@link GRVisitor} to be accepted
     * @see GRVisitor
     **/
    public void accept(GRVisitor v){
	v.visitGRPreFactor(this);
    }
}

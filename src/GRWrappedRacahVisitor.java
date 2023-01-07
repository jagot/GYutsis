/* GRWrappedRacahVisitor.java
   ---------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Stack;
import java.util.Iterator;
import java.util.Collections;

import java.awt.FontMetrics;

/** 
 * A GRVisitor which constructs a wrapped String representation of an
 * object from the GR-family in Racah format, including 
 * GenRecoupCoeff.  When a FontMetrics Class is specified in the
 * constructor, the wrapsize is in pixels and takes in account the
 * used font. 
 * @see GRVisitor 
 * @see GenRecoupCoeff 
 * @see GRCompoundFactor 
 * @see GRSummation 
 * @see GRFactor 
 * @see GRPreFactor 
 * @see GRKrDelta 
 * @see GR6jSymbol
 * @author Dries.VanDyck@rug.ac.be 
 **/
public class GRWrappedRacahVisitor extends AbstractGRWrappedTextVisitor{
    
    private boolean addcomma = false;
    private Stack summations = new Stack();
    
    /** 
     * Constructs a new GRWrappedRacahVisitor. Default wrapsize is 80
     * characters.
     **/
    public GRWrappedRacahVisitor(){
	append("Racah_set(");
    }

    /** 
     * Constructs a new GRWrappedRacahVisitor, with given wrapsize 
     * in pixels and the FontMetrics class for the used font.
     * @param wrapsize number of allowed pixels on a line
     * @param FontMetrics the FontMetrics for the used font
     **/
    public GRWrappedRacahVisitor(int wrapsize, FontMetrics fm){
	super(wrapsize,fm);
	append("Racah_set(");
    }

    /** 
     * Constructs a new GRRacahMapleVisitor which wraps at
     * <code>wrapsize</code> characters.
     * @param wrapsize number of allowed chars on one line
     **/
    public GRWrappedRacahVisitor(int wrapsize){ 
	super(wrapsize); 
    	append("Racah_set(");
    }

    /** 
     * Specific method for visiting a GenRecoupCoeff object.
     * @param grc the GenRecoupCoeff to be visited 
     * @see GenRecoupCoeff
     **/
    public void visitGenRecoupCoeff(GenRecoupCoeff grc){}

    /** 
     * Specific method for visiting a GRCompoundFactor object.
     * @param grcf the GRCompoundFactor to be visited 
     **/
    public void visitGRCompoundFactor(GRCompoundFactor grcf){}

    /** 
     * Specific method for visiting a GRSummation object.
     * @param grs the GRSummation to be visited 
     * @see GRSummation
     **/
    public void visitGRSummation(GRSummation grs){
	if (addcomma)
	    append(",");
	else 
	    addcomma = true;
	append("summation("+grs.summVar()+")");
    }

    /** 
     * Specific method for visiting a GRPreFactor object.
     * @param grpf the GRPreFactorto be visited 
     * @see GRPreFactor
     **/
    public void visitGRPreFactor(GRPreFactor grpf){
	if (!grpf.expEmpty())
	    wrapExp(grpf);
	if (!grpf.factorsEmpty())
	    wrapFactors(grpf);
    }

    /** 
     * Specific method for visiting a GRKrDelta object.
     * @param grcd the GRKrDelta to be visited 
     * @see GRKrDelta
     **/
    public void visitGRKrDelta(GRKrDelta grkd){
	if (addcomma)
	    append(",");
	else 
	    addcomma = true;
	String [] args = grkd.args();
	String delta = "delta("+args[0]+","+args[1]+")"; 
	append(delta);
    }

    /** 
     * Specific method for visiting a GR6jSymbol object.
     * @param gr6j the GR6jSymbol to be visited 
     * @see GR6jSymbol
     **/
    public void visitGR6jSymbol(GR6jSymbol gr6j){
	if (addcomma)
	    append(",");
	else
	    addcomma = true;
	String [] args = gr6j.args(); 
	String w6j = "w6j("
	    + args[0] + "," + args[1] + "," + args[2] + ","
	    + args[3] + "," + args[4] + "," + args[5] 
	    + ")";
	append(w6j);
    }

    /** 
     * Returns the wrapped String representing the object visited.
     * @return the resulting wrapped String as an Object
     **/
    public Object result(){
	append(")");
	return wrappedstring.toString();
    }

    private void wrapFactors(GRPreFactor grpf){
	if (addcomma)
	    append(",");
	else 
	    addcomma = true;
	append("(factor,");
	Iterator labels = grpf.factorsLabels();
	Iterator exps = grpf.factorsExps();
	// Collect the terms with equal power in a Hashmap
	// where they are held in a LinkedList to be sorted
	HashMap hm = new HashMap(grpf.factorsSize());
	// and add the available powers in two Arraylists (positive
	// and negative) which will be sorted afterwards
	ArrayList pospowers = new ArrayList();
	ArrayList negpowers = new ArrayList();
	while (labels.hasNext()){
	    String label = (String) labels.next();
	    Integer exp = (Integer) exps.next();
	    if (hm.containsKey(exp)){
		LinkedList factors = (LinkedList) hm.remove(exp);
		factors.add(label);
		hm.put(exp,factors);
	    }
	    else{
		if (exp.intValue() < 0)
		    negpowers.add(new Integer(-exp.intValue()));
		else
		    pospowers.add(exp);
		LinkedList ll = new LinkedList();
		ll.add(label);
		hm.put(exp,ll);
	    }
	}
	Collections.sort(pospowers);
	wrapProductFactors(pospowers,hm);
	if (negpowers.size() > 0){
	    Collections.sort(negpowers);
	    append("/");
	    if (negpowers.size() > 1)
		append("(");
	    wrapProductFactors(negpowers,hm);
	    if (negpowers.size() > 1)
		append(")");
	}
	append(")");
    }

    private void wrapProductFactors(ArrayList powers, HashMap hm){
	Iterator itpowers = powers.iterator();
	while(itpowers.hasNext()){
	    Integer power = (Integer) itpowers.next();
	    LinkedList factors = (LinkedList) hm.get(power);
	    Collections.sort(factors, new VarComparator());
	    Iterator itfactor = factors.iterator();
	    String suffix="";
	    String factor;
	    if (power.intValue() == 2){//use no power
		factor = "(2*"+(String) itfactor.next()+"+1)";
	    }
	    else {
		factor = "(2*"+(String) itfactor.next()+"+1)";
		if (itfactor.hasNext()){
		    factor = "("+factor;
		    suffix = ")";
		}
		suffix += power.intValue()%2 != 0 ? 
		    "^("+power.intValue()+"/2)" :
		    "^("+(power.intValue()/2)+")";
	    }
	    append(factor);
	    while (itfactor.hasNext()){
		    append("*(2*"+(String) itfactor.next()+"+1)");
	    }
	    append(suffix);
	}
    }

    private void wrapExp(GRPreFactor grpf){
	if (addcomma)
	    append(",");
	else 
	    addcomma = true;
	Iterator itlabels = grpf.expLabels();
	ArrayList labels= new ArrayList(grpf.expSize());
	while (itlabels.hasNext())
	    labels.add(itlabels.next());
	Collections.sort(labels, new VarComparator());
	int coeff = ((Integer) grpf.exp.get(labels.get(0))).intValue();
	String scoeff="";
	switch (coeff){
	    case 2:
		scoeff = "2*";
		break;
	    case 3:
		scoeff = "-";
		break;
	}
	String term = "phase(" + scoeff + (String) labels.get(0);
	append(term);
	for (int i = 1; i < labels.size(); i++){
	    String label = (String) labels.get(i);
	    term = expCoeff(((Integer) grpf.exp.get(label)).intValue())
		+ label;
	    append(term);
	}
	append(")");
    }
    
    private String expCoeff(int coeff){
	switch (coeff){
	    case 1:
		return "+";
	    case 2:
		return "+2*";
	    case 3:
		return "-";
	default:
	    return "Error"; // should not be possible
	}
    }
}

/* GRWrappedLaTeXVisitor.java
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
 * object from the GR-family in Maple format, including
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
public class GRWrappedMapleVisitor extends AbstractGRWrappedTextVisitor{
    /**
     * Macro, as a Maple function, for the Kronecker Delta symbol.
     **/
    public static final String KR_DELTA_MACRO =
	"delta:=(i,j)->if (i = j) then 1 else 0";
    /**
     * Macro, as a Maple function, for the triangular symbol.
     **/
    public static final String DELTA_MACRO = 
	"del:=(a,b,c)->((-a+b+c)!*(a-b+c)!*(a+b-c)!/(a+b+c+1)!)^(1/2)";
    /**
     * Macro, as a Maple function, for the Wigner 6-j symbol.
     **/
    public static final String SIXJ_MACRO = 
	"sixj:=proc(a,b,c,d,e,f)\n" +
	"local s,n:\n" +
	"s := add( (-1)^n*(n+1)!/( (n-a-b-c)!*(n-c-d-e)!*(n-a-e-f)!*(n-b-d-f)* (a+b+d+e-n)!*(a+c+d+f-n)!*(b+c+e+f-n)!),n=max(-1,a+b+c,c+d+e,a+e+f,b+d+f)..min(a+b+d+e,a+c+d+f,b+c+e+f) ):\n" + 
	"RETURN( del(a,b,c)*del(c,d,e)*del(a,e,f)*del(b,d,f)*s);\n" + 
	"end";

    private boolean multiply = false;
    private Stack summations = new Stack();
    
    /** 
     * Constructs a new GRWrappedMapleVisitor. Default wrapsize is 80
     * characters.
     **/
    public GRWrappedMapleVisitor(){}

    /** 
     * Constructs a new GRWrappedMapleVisitor, with given wrapsize 
     * in pixels and the FontMetrics class for the used font.
     * @param wrapsize number of allowed pixels on a line
     * @param FontMetrics the FontMetrics for the used font
     **/
    public GRWrappedMapleVisitor(int wrapsize, FontMetrics fm){
	super(wrapsize,fm);
    }

    /** 
     * Constructs a new GRWrappedMapleVisitor which wraps at
     * <code>wrapsize</code> characters.
     * @param wrapsize number of allowed chars on one line
     **/
    public GRWrappedMapleVisitor(int wrapsize){ super(wrapsize); }

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
	if (multiply)
	    append("*");
	append("add(");
	String [] couplings = grs.couplings();
	String suffix = "," + grs.summVar() +
	    "=max(abs("+ couplings[0]+"-"+couplings[1]+"),abs("+
	    couplings[2]+"-"+couplings[3]+"))..min("+
	    couplings[0]+"+"+couplings[1]+","+
	    couplings[2]+"+"+couplings[3]+"))";
	summations.push(suffix);
	multiply = false;
    }

    /** 
     * Specific method for visiting a GRPreFactor object.
     * @param grpf the GRPreFactorto be visited 
     * @see GRPreFactor
     **/
    public void visitGRPreFactor(GRPreFactor grpf){
	if (!grpf.expEmpty())
	    wrapExp(grpf);
	if (!grpf.factorsEmpty()){
	    if (!grpf.expEmpty())
		append("*");
	    wrapFactors(grpf);
	}
	multiply = true;
    }

    /** 
     * Specific method for visiting a GRKrDelta object.
     * @param grcd the GRKrDelta to be visited 
     * @see GRKrDelta
     **/
    public void visitGRKrDelta(GRKrDelta grkd){
	if (multiply)
	    append("*");
	String [] args = grkd.args();
	String delta = "delta("+args[0]+","+args[1]+")"; 
	append(delta);
	multiply = true;
    }

    /** 
     * Specific method for visiting a GR6jSymbol object.
     * @param gr6j the GR6jSymbol to be visited 
     * @see GR6jSymbol
     **/
    public void visitGR6jSymbol(GR6jSymbol gr6j){
	if (multiply)
	    append("*");
	String [] args = gr6j.args(); 
	String w6j = "sixj("
	    + args[0] + "," + args[1] + "," + args[2] + ","
	    + args[3] + "," + args[4] + "," + args[5] 
	    + ")";
	append(w6j);
	multiply = true;
    }

    /** 
     * Returns the wrapped String representing the object visited.
     * @return the resulting wrapped String as an Object
     **/
    public Object result(){
	while(!summations.empty()){
	    append((String) summations.pop());
	}
	return wrappedstring.toString();
    }

    /**
     * Returns some Maple procedures to be used with the generated formula.
     * @return the needed procedures as a String.
     **/
    public static String macros(){ 
	return KR_DELTA_MACRO + ":\n" 
	    + DELTA_MACRO + ":\n" 
	    + SIXJ_MACRO + ":\n";
    }

    private void wrapFactors(GRPreFactor grpf){
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
		factor = "(2"+(String) itfactor.next()+"+1)";
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
	String term = "(-1)^(" + scoeff + (String) labels.get(0);
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

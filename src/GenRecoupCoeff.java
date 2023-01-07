/* GenRecoupCoeff.java
   -------------------
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
 * Class representing a General Recoupling Coefficient of Angular Momenta
 * in terms of 6j-coefficients, which is gradually build by the graphical
 * methods developed by Yutsis and Vanagas by performing reduction 
 * operations on the corresponding YutsisGraph.
 * @see YutsisGraph 
 * @see Edge 
 * @see GRCompoundFactor 
 * @see GRSummation 
 * @see GRFactor 
 * @see GRVisitor
 * @author Dries.VanDyck@rug.ac.be
 **/
public class GenRecoupCoeff implements Cloneable{
    int order;
    int nrof6j = 0;
    GRCompoundFactor precf;
    ArrayList summations;
    
    /** 
     * Constructs a new General Recoupling Coefficient with rootnode
     * <code>root</code>, left intermediate angular momenta
     * <code>a[0]...  a[a.length-1]</code>, right intermediate
     * angular momenta <code>b[0]...  b[b.length-1]</code> and s all
     * first coupled momenta (with doubles if a angular momenta is
     * first coupled in both trees) with expected number of
     * summationvariables specified by <code>order</code>.
     * @param order the order of the corresponding YutsisGraph
     * @param root the label of the root Edge of the 
     *             YutsisGraph
     * @param a array containing the labels of the intermediate angular
     *          momenta of the BRA of the general recoupling coefficient
     * @param b array containing the labels of the intermediate angular
     *          momenta of the KET of the general recoupling coefficient
     * @param s array containing the labels of all first coupled angular
     *          momenta 
     **/
    public GenRecoupCoeff(int order, String root, 
			  String [] a, String [] b, String [] s){
	this.order = order;
	summations = new ArrayList(order);
	GRPreFactor pf = new GRPreFactor(3*order);
	// Construct initial factors (transformation trees->graph)
	pf.appendExp(2,root);
	pf.appendExp(2,b);
	pf.appendExp(2,s);
	pf.appendFactor(a,1);
	pf.appendFactor(b,1);
	precf = new GRCompoundFactor(3*order,pf);
    }

    /** 
     * Constructs an exact copy of the given GenRecoupCoeff.
     * @param grc the GenRecoupCoeff object to be duplicated
     **/
    public GenRecoupCoeff(GenRecoupCoeff grc){
	order = grc.order;
	nrof6j = grc.nrof6j;
	precf = new GRCompoundFactor(grc.precf);
	summations = new ArrayList();
	Iterator i = grc.summations.iterator();
	while (i.hasNext()){
	    summations.add(new GRSummation ((GRSummation) i.next()));
	}
    }

    /** 
     * Returns the number of Wigner 6j-symbols in this General
     * Recoupling Coefficient (in its current state).
     * @return the number of Wigner 6j-symbols 
     **/
    public int nrOf6js() { return nrof6j; }

    /** 
     * Returns the number of summationindices in this General
     * Recoupling Coefficient.
     * @return the number of summationinices
     **/
    public int nrOfSummations() { return summations.size(); }
    
    /** 
     * Returns a String representation of this General Recoupling 
     * Coefficient.
     * @return a String representation of this GenRecoupCoeff
     **/
    public String toString(){
	StringBuffer bf = new StringBuffer();
	bf.append(precf);
	for (int i = 0; i < summations.size(); i++)
	    bf.append((GRSummation) summations.get(i));
	if (bf.length() == 0)
	    return "1";
	else
	    return bf.toString();
    }

    /** 
     * Implementation of the Visitor pattern. Visits first itself and then
     * calls accept on it's composites.
     * @param v the GRVisitor to be accepted
     * @see GRVisitor
     **/
    public void accept(GRVisitor v){
	v.visitGenRecoupCoeff(this);
	precf.accept(v);
	for (int i = 0; i < summations.size(); i++)
	    ((GRSummation) summations.get(i)).accept(v);
    }

    /** 
     * Appends a factor <code>(-1)^(j[0] + j[1] + j[2])</code> to this
     * General Recoupling Coefficient as a consequence of the invertion
     * of the node sign in wich <code>j[0],j[1],j[2]</code> are coupled
     * in the corresponding YutsisGraph.
     * @param j the labels of the Edges coupled in the inverted node
     * @see YutsisGraph
     **/
    public void invertNode(String [] j){
	boolean [] notyetappended = {true, true, true};
	for (int k = 0; k < 3; k++)
	    for (int i = summations.size()-1; i >= 0; i--){
		GRSummation summ = (GRSummation) summations.get(i);
		if (notyetappended[k] && summ.summVar().equals(j[k])){
		    summ.appendExp(1,j[k]);
		    notyetappended[k] = false;
		}
	    }
	for (int k = 0; k < 3; k++)
	    if (notyetappended[k])
		precf.appendExp(1,j[k]);
    }

    /** 
     * Appends a factor <code>(-1)^(2*j)</code> to this General
     * Recoupling Coefficient as a consequence of the invertion of
     * the Edge with label <code>j</code> in the corresponding
     * YutsisGraph.
     * @param j the label of the inverted Edge
     * @see Edge
     * @see YutsisGraph
     **/
    public void invertEdge(String j){
	for (int i = summations.size()-1; i >= 0; i--){
		GRSummation summ = (GRSummation) summations.get(i);
		if (summ.summVar().equals(j)){
		    summ.appendExp(2,j);
		    return;
		}
	}
	precf.appendExp(2,j);
    }

    /** 
     * Appends a factor <code>(2*l1+1)^(-1)*delta(l1,l2)</code> to this
     * General Recoupling Coefficient as a consequence of the removal of
     * a bubble between the edges with labels <code>l1, l2</code> in the
     * corresponding YutsisGraph.
     * @param l1 the label of the Edge coming out the bubble
     * @param l2 the label of the Edge entering the bubble
     * @see Edge
     * @see YutsisGraph
     **/
    public void bubble(String l1, String l2){
	GRKrDelta cd = new GRKrDelta(l1,l2);
	for (int i = summations.size()-1; i >= 0; i--){
		GRSummation summ = (GRSummation) summations.get(i);
		if (summ.summVar().equals(l1) 
		    || summ.summVar().equals(l2)){
		    summ.appendFactor(l1,-2);
		    summ.append(cd);
		    return;
		}
	}
	precf.appendFactor(l1,-2);
	precf.append(cd);
    }

    /** 
     * Appends a factor <code>{j[0], j[1], j[2];l[0], l[1], l[2]}</code>
     * (Wigner 6j-symbol) to this Genereal Recoupling Coefficient as 
     * a consequence of the removal of a the triangle <code>l[0], l[1],
     * l[2]</code> with neighboredges <code>j[0], j[1], j[2]</code> such
     * that <code>j[i]<code> is not coupled with <code>l[i]</code> in
     * the corresponding YutsisGraph.
     * @param l array containing the labels of the Edges of the
     *          triangle
     * @param j array containg the labels of the Edges coming out
     *          the triangle
     * @see Edge	 
     * @see YutsisGraph
     **/
    public void triangle(String [] l, String [] j){
	GR6jSymbol a6j = new GR6jSymbol(j,l);
	nrof6j++;
	for (int i = summations.size()-1; i >= 0; i--){
		GRSummation summ = (GRSummation) summations.get(i);
		for (int k = 0; k < 3; k++)
		    if (summ.summVar().equals(l[k]) 
			|| summ.summVar().equals(j[k])){
		    summ.append(a6j);
		    return;
		}
	}
	precf.append(a6j);
    }
    
    /** 
     * Appends the summation factor <code>sum(f)(2f+1){a, b, f; d, c,
     * e}</code> (Wigner 6j-symbol) to this Genereal Recoupling
     * Coefficient as a consequence of an interchange on the edge
     * with label <code>e</code> interchanging the edges 
     * <code>b,c</code> which (respectively) neighboredges <code>a, d</code>
     * in the corresponding YutsisGraph. <code>f</code> is
     * the new label of the edge with label <code>e</code>.
     * @param e the label of the Edge on which the interchange is applied
     * @param b label of an Edge to be interchanged
     * @param c label of an Edge to be interchanged
     * @param a label of the Edge coupled with e and b
     * @param d label of the Edge coupled with e and c
     * @param f new label of the Edge e
     * @see Edge
     * @see YutsisGraph
     **/
    public void interchange(String e, String b, String c, 
			    String a, String d, String f){
	GRSummation sumf = new GRSummation(f,a,b,c,d);
	sumf.append(new GRPreFactor(3*(nrOfSummations()+1)/4,1,f,f,2));
	String [] top = {a,b,f};
	String [] bottom = {d,c,e};
	nrof6j++;
	sumf.append(new GR6jSymbol(top,bottom));
	summations.add(sumf);
	String [] labels = {b,c,e};
	invertNode(labels); //same effect... 
    }

    /** 
     * Implementation of the Cloneable interface. 
     * @return a clone of this GenRecoupCoeff.
     **/
    public Object clone(){
	GenRecoupCoeff grc = null;
	try { grc = (GenRecoupCoeff) super.clone(); }
	catch(CloneNotSupportedException e){}//should not be possible
	grc.precf = (GRCompoundFactor) precf.clone();
	summations = new ArrayList();
	Iterator i = grc.summations.iterator();
	while (i.hasNext()){
	    summations.add(((GRSummation) i.next()).clone());
	}
	return grc;
    }
}

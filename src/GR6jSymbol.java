/* GR6jSymbol.java
   ---------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

/** 
 * Class representing a Wigner 6j-symbol {a,b,c;d,e,f}. Objects of 
 * this class are immutable.
 * @see GenRecoupCoeff
 * @see Edge
 * @see GRFactor
 * @see GRVisitor
 * @author Dries.VanDyck@rug.ac.be
 **/
public class GR6jSymbol extends GRFactor{
    /** 
     * The labels in this 6j-symbol when read from left to right, top
     * to bottom.
     **/
    String [] labels = new String[6]; 
    
    /** 
     * Constructs a Wigner 6j-symbol with toprow <code>toprow[0],
     * toprow[1], toprow[2]</code> and bottomrow 
     * <code>bottomrow[0], bottomrow[1], bottomrow[2]</code>.
     * @param toprow array of labels for the toprow 
     * @param bottomrow array of labels for the bottomrow 
     **/
    public GR6jSymbol(String [] toprow, String [] bottomrow){
	for (int i =0 ; i < 3; i++){
	    labels[i] = toprow[i];
	    labels[3+i] = bottomrow[i];
	}
    }

    /** 
     * True if this 6j-symbol contains the label <code>label</code>,
     * false otherwise.
     * @param label the label to be checked
     * @return true if the label appears in this GRPreFactor, false otherwise
     **/
    public boolean containsLabel(String label){
	for (int i = 0; i < 6; i++)
	    if (labels[i].equals(label))
		return true;
	return false;
    }

    /**
     * Returns the arguments of the GR6jSymbol object.
     * @return the arguments of the GR6jSymbol as a String array; the 
     *         first/last three make up the bottom/top row.
     **/
    public String[] args(){
	String [] args = new String[6];
	System.arraycopy(labels,0,args,0,6);
	return args;
    }

    /** 
     * A String representation of this 6j-symbol.
     * @return a String representation of this GR6jSymbol
     **/
    public String toString(){
	StringBuffer bs = new StringBuffer();
	bs.append('{');
	for(int i = 0; i < 3; i++)
	    bs.append(labels[i]).append(',');
	bs.setCharAt(bs.length()-1,';');
	for(int i = 3; i <6; i++)
	    bs.append(labels[i]).append(',');
	bs.setCharAt(bs.length()-1,'}');
	return bs.toString();
    }
    
    /** 
     * Implementation of the Visitor pattern.
     * @param v the GRVisitor to be accepted
     * @see GRVisitor
     **/
    public void accept(GRVisitor v){
	v.visitGR6jSymbol(this);
    }

}

/* AbstractGRWrappedTextVisitor.java
   ---------------------------------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
*/

import java.util.Iterator;
import java.util.Comparator;

import java.awt.FontMetrics;

/**
 * Abstract class implementing common functionality between visitors
 * creating a wrapped text representation of a {@link GenRecoupCoeff}
 * in some format. This class implements some basic wrapping functionality
 * based on the length in characters if no font is specified, or in pixels
 * if a font is specified. The generated text is held internally as a 
 * StringBuffer.
 * @see GRVisitor
 * @see GenRecoupCoeff
 **/
public abstract class AbstractGRWrappedTextVisitor implements GRVisitor{

    /**
     * Stores the wrapped String representation under construction.
     **/
    protected StringBuffer wrappedstring = new StringBuffer();
    FontMetrics fm = null;
    int wrapsize = 80;
    int wrapcount = 0;

    /** 
     * Constructs a new AbstractGRWrappedTextVisitor. Default wrapsize
     * is 80 characters.  
     **/
    public AbstractGRWrappedTextVisitor(){}

    /** 
     * Constructs a new AbstractGRWrappedTextVisitor which wraps at
     * <code>wrapsize</code> characters.
     * @param wrapsize number of allowed chars on one line
     **/
    public AbstractGRWrappedTextVisitor(int wrapsize){ 
	this.wrapsize = wrapsize; 
    }

    /** 
     * Constructs a new AbstarctGRWrappedTextVisitor, with given
     * wrapsize in pixels and the FontMetrics class for the used font.
     * @param wrapsize number of allowed pixels on a line
     * @param FontMetrics the FontMetrics for the used font 
     **/
    public AbstractGRWrappedTextVisitor(int wrapsize, FontMetrics fm){
    	this.wrapsize = wrapsize;
	this.fm = fm;
    }

    /**
     * Puts a newline in the generated String.
     **/
    protected void wrap(){
	wrappedstring.append('\n');
	wrapcount = 0;
    }

    /** 
     * Appends the given String to the generated String as one 
     * entity on a line, wrapping on demand.
     * @param s the String to be appended as one entity
     **/
    protected void append(String s){
	if (wrapcount+size(s) >= wrapsize)
	    wrap();
	wrappedstring.append(s);
	wrapcount += size(s);
    }

    /** 
     * Returns the size of the given string in pixels when a
     * font is specified, otherwise the length is returned.
     * @param s string of which the size needs to be calculated
     * @return the size in pixels if a font is specified, otherwise 
     *         the length of the given string
     **/
    protected int size(String s){
	return fm == null ? s.length() : fm.stringWidth(s);
    }

    /**
     * Returns the starting index of a suffix number if present.
     * @param var the string for which a suffix number has to be found
     * @return the index of the number suffix if present, -1 otherwise 
     **/
    static int subscriptIndex(String var){
	int index = var.length();
	while (Character.isDigit(var.charAt(index-1)))
	    index--;
	return index == var.length() ? -1 : index; 
    }

    /**
     * Static inner class implementing a Comparator comparing Strings
     * interpreting there number suffix as a number: 
     * <code> compare("j2","j11") == -1 </code>
     * This Comparator is consistent with Object.equals.
     **/
    static class VarComparator implements Comparator{
	
	/** 
	 * Implementation of the Comparator interface.
	 * @param o1 first Object of the comparison
	 * @param o2 second Object of the comparison
	 * @return a negative value if <code>o1 < o2</code>, positive
	 *         value if <code>o1 > o2</code> and 0 if <code>o1 == o2</code>
	 *         in the considered ordening.
	 **/
	public int compare(Object o1, Object o2){
	    String label1 = (String) o1;
	    String label2 = (String) o2;
	    int index1= subscriptIndex(label1);
	    int index2= subscriptIndex(label2);
	    String var1 = label1.substring(0, index1 == -1 ? 
					   label1.length() : index1);
	    String var2 = label1.substring(0, index2 == -1 ? 
					   label2.length() : index2);
	    int result = var1.compareTo(var2); 
	    if (result != 0)
		return result;
	    Integer subscript1 = index1 == -1 ? 
		new Integer(Integer.MIN_VALUE) : 
		new Integer(label1.substring(index1));
	    Integer subscript2 = index2 == -1 ? 
		new Integer(Integer.MIN_VALUE) : 
		new Integer(label2.substring(index2));
	    return subscript1.compareTo(subscript2);
	}
    }
}

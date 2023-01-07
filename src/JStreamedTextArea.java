/* JStreamedTextArea.java
   ---------
   2001 (c) Dries Van Dyck
   Combinatorial Algorithms Research Group
   Department of Applied Mathematics and Computer Science
   University of Ghent
   Krijgslaan 281--S9
   B-9000 GENT Belgium
   
   Freely taken from the example in 
     "Java I/O", Elliote Rusty Harold, O'Reilly (1999)
   and adapted for Java Swing
*/

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/** 
 * Class representing TextArea objects behaving as OutputStreams.
 * @author Dries.VanDyck@rug.ac.be
 **/
public class JStreamedTextArea extends JTextArea {

    OutputStream theOutput = new TextAreaOutputStream();

    /** 
     * Constructs a new JStreamedTextArea.
     **/
    public JStreamedTextArea() {
	this("", 0, 0);
    }

    /** 
     * Constructs a new JStreamedTextArea.
     * @param text
     **/
    public JStreamedTextArea(String text) {
	this(text, 0, 0);
    } 

    /** 
     * Constructs a new JStreamedTextArea with given number of rows
     * and columns.
     * @param rows the number of rows 
     * @param columns the number of columns
     **/
    public JStreamedTextArea(int rows, int columns) {
	this("", rows, columns);
    }

    /** 
     * Constructs a new JStreamedTextArea with given number of rows
     * and columns and intial text.
     * @param text the intial text
     * @param rows the number of rows 
     * @param columns the number of columns.
     **/
    public JStreamedTextArea(String text, int rows, int columns){
	super(text, rows, columns);
	super.setEditable(false);
    }

    /** 
     * A do-nothing implementation overriding the method of JTextArea.
     * @param dummy dummy argument
     * @param pos dummy argument
     **/
    public void insert(String dummy, int pos){}

    /** 
     * A do-nothing implementation overriding the method of JTextArea.
     * @param dummy dummy argument
     **/
    public void setEditable(boolean dummy){}

    /** 
     * Clears the TextArea.
     **/
    public void clear() { setText(""); }
   
    /** 
     * Returns the associated OutputStream of this JStreamedTextArea.
     * @return the associated OutputStream
     **/
    public OutputStream getOutputStream() {
	return theOutput;
    }

    /**
     * Class implementing an OutputStream behaviour of
     * JStreamedTextArea.  
     **/
    class TextAreaOutputStream extends OutputStream {

	/** 
	 * Overrides write(int) from OutputStream, which is mandatory.
	 * @param b the byte to be written to the OutputStream
	 **/
	public synchronized void write(int b) {
	    // recall that the int should really just be a byte
	    b &= 0x000000FF;
	    // must convert byte to a char in order to append it
	    char c = (char) b;
	    append(String.valueOf(c));
	}

	/**
	 * Writes len bytes from the specified byte array starting at
	 * offset off to this output stream. The general contract for
	 * <code>write(b, off, len)</code> is that some of the bytes
	 * in the array b are written to the output stream in order;
	 * element <code>b[off]</code> is the first byte written and
	 * <code>b[off+len-1]</code> is the last byte written by this
	 * operation.  
	 * @param b the data.off 
	 * @param offset the start offset in the data.len 
	 * @param length the number of bytes to write.
	 **/
	public synchronized void write(byte[] b, int offset, int length) {
	    
	    append(new String(b, offset, length));
	    
	}
    }
}

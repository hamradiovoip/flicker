/* File: Windump.java */

import java.awt.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

/** 
 * Class Windump is used debug print methods for image neighbohood,
 * windows, etc. 
 *<P>
 * This file is derived from GELLAB-II sg2gii file sg2rlm.c.
 *<P>
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government.  As a work of
 * the United States Government there is no associated copyright.  It is
 * offered as open source software under the Mozilla Public License
 * (version 1.1) subject to the limitations noted in the accompanying
 * LEGAL file. This notice must be included with the code. The Open2Dprot 
 * Mozilla and Legal files are available on http://Open2Dprot.sourceforge.net/.
 *<P>
 * @author P. Lemkin, NCI-Frederick, Frederick, MD, 21702
 * @version $Date$   $Revision$
*/

public class Windump
{         
  /** Window dump OCTAL data mode */
  final public static int
    SHOW_DECIMAL= 0;
  /** Window dump OCTAL data mode */
  final public static int
    SHOW_OCTAL= 1;
  /** Window dump OCTAL data mode */
  final public static int
    SHOW_HEX= 2;
  /** Window dump OD data mode */
  final public static int
    SHOW_OD= 3;
  
  /** instance of Flicker */
  private static Flicker
    flk; 
  /** FileIO utilities */
  public static FileIO
    fio;
  /** misc utility methods */
  private static Util
    util;
  
  
  /**
   * Windump() - constructor
   */
  public Windump(Flicker flkS)
  {
    flk= flkS;
    fio=  flk.fio;
    util= flk.util;
  }
  
  
  /**
   * winDump() - Draw a window from picture frlo the lower 8-bit/pixel
   * data in an int[] 'pix' at
   *<PRE>
   * x1,y1 x2,y1
   * ...
   * x1,y2 x2,y2.
   *
   * if printMode is SHOW_OCTAL, SHOW_DECIMAL or SHOW_HEX.
   *
   * If width is < 0, then put the data in OCTAL else DECIMAL.
   *</PRE>
   * @param pix is image data but we only use 8 least siginificant bits
   * @param x1 is left column
   * @param x2 is right column
   * @param y1 is top row
   * @param y2 is bottom row
   * @param pixWidth is the number of columns in the image
   * @param pixHeight is the number of rows in the image
   * @param title is title for printout
   * @param printMode is HOW_OCTAL, SHOW_DECIMAL, SHOW_HEX or SHOW_OD
   * @param maxColsToPrint is the number of columns to print. 
   *        For 80 column terminal width is 18 and 30 for a 132 column
   *        terminal. 
   * @param stepSize is pixel step size for sample/average in [1:32],
   *        1 default
   * @param iData is the image data instance if left or right image.
   *        This is used to lookup the iData.mapGrayToOD[] map.
   * @return string of window data
  */
  final public static String winDump(int pix[], int x1, int x2, 
                                     int y1, int y2, 
                                     int pixWidth, int pixHeight,
                                     String title, int printMode,
                                     int maxColsToPrint,
                                     int stepSize, ImageData iData)
  { /* winDump */
    String 
      dRadix= "Decimal",
      dashes=" -------------------------------------------",
      sVal,
      s= "",
      s1= "",
      sout= "";
    int
      x,
      y,
      fWidth= 4,  /* for hex, octal and decimal. 6 for OD */
      xCenter= (x1+x2)/2,
      yCenter= (y1+y2)/2,
      xx1= x1,
      xx2= x2,
      yy1= y1,
      yy2= y2,
      maxrows= 0,
      maxcols= 0;
        
    stepSize= Math.max(1,Math.min(stepSize,32));    /* for safty */
    
    maxcols= (maxColsToPrint-1);  /* for 80 column terminal width is 18 and 30
                                   * for a 132 column terminal. Note the -1 is
                                   * because we lable the row # on the left. */
    maxcols= Math.min((x2-x1),maxcols);
    maxrows= (y2-y1);
    
    if(stepSize>1)
    { /* redefine the window */
      xx1= xCenter -(maxcols*stepSize)/2;
      xx2= xCenter +(maxcols*stepSize)/2 -1;
      yy1= yCenter -(maxrows*stepSize)/2;
      yy2= yCenter +(maxrows*stepSize)/2 -1;
    } /* redefine the window */
   
    /* Define the data radix */
    switch(printMode)
    {
      case SHOW_OCTAL:
        fWidth= 4;
        dRadix= "Octal";
        break;
      case SHOW_HEX:
        fWidth= 4;
        dRadix= "Hex";
        break;
      case SHOW_OD:
        fWidth= 6;
        dRadix= "Optical-density";
        break;
      case SHOW_DECIMAL:
      default:
        fWidth= 4;
        dRadix= "Decimal";
        break;
    }
    
    s= "\nWindmp ["+xx1+":"+xx2+", "+yy1+":"+yy2+"] Center ("+
       xCenter+","+ yCenter+") "+maxColsToPrint+"X"+maxColsToPrint+
       " size,\nsampled: "+stepSize+" pixels, data-radix: "+dRadix+
       "\nImage: "+title+
       "\n    X";
    
    for (x=xx1; x<=xx2; x += stepSize)
    { /* print column index */
      s += util.leftFillWithSpaces((""+x),fWidth); 
    } /* print column index */
    
    s += "\n  Y  ";
    for (x=xx1; x<=xx2; x += stepSize)
      s += dashes.substring(0,fWidth);
      //s += " ---";
    s += "\n";
    sout= s;
    
    int
      xV,
      yV,
      rgbPixel,
      val; 
    float fVal;
    for (y=yy1; y<=yy2; y += stepSize)
    { /* Do a line */
      s= util.leftFillWithSpaces((""+y),4)+" ";
      
      for (x=xx1; x<=xx2; x += stepSize)
      { /* print data */
        xV= Math.max(0,Math.min((pixWidth-1),x));
        yV= Math.max(0,Math.min((pixHeight-1),y));
        rgbPixel= pix[(yV*pixWidth)+xV];
        val= 255 - (rgbPixel & 0377);         /* take low 8-bits */
        
        switch(printMode)
        {
          case SHOW_OCTAL:                    /* dRadix= "%4o"  OCTAL */
            sVal= util.cvi2os(val);
            break;
          case SHOW_HEX:                      /* dRadix= "%4x" HEX */
            sVal= util.cvi2hexs(val);
            break;
          case SHOW_OD:                       /* map to float map to OD */
            fVal= iData.mapGrayToOD[val];
            sVal= util.cvf2s(fVal,3);
            break;
          case SHOW_DECIMAL:                  /* dRadix= "%4d" DECIMAL */
          default:
            sVal= (""+val);
            break;
        }        
        s1= util.leftFillWithSpaces(sVal,fWidth);
        s += s1;
      } /* print data */
      s += "\n";
      sout += s;                              /* build the big picture */
    } /* Do a line */
    
    return(sout);
  } /* winDump */


} /* End of class Windump */


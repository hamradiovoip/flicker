/* File: Info.java */

import java.awt.Color;
import java.awt.Label;
import java.awt.Font;
import java.awt.Point;
import java.util.*;
import java.lang.*;
import javax.swing.JComponent.*;
import javax.swing.*;

/**
 * Info class supports updating the "Info" area on the Flicker GUI
 * displaying messages on the status of Flicker.
 *
 *<P>
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government.  As a work of
 * the United States Government there is no associated copyright.  It is
 * offered as open source software under the Mozilla Public License
 * (version 1.1) subject to the limitations noted in the accompanying
 * LEGAL file. This notice must be included with the code. The Flicker 
 * Mozilla and Legal files are available on 
 * http://open2dprot.sourceforge.net/Flicker
 *<P>
 * @author P. Lemkin (LECB/NCI), G. Thornwall (SAIC), Frederick, MD
 * @version $Date$   $Revision$
 * @see <A HREF="http://open2dprot.sourceforge.net/Flicker">Flicker Home</A>
 */

public class Info 
{
  
  Flicker
    flk;
  Affine
    aff;
  BuildGUI
    bGui;
  Landmark
    lms;
  Util
    util;
  
  private JPanel
    msgPanel;			
  private Label
    info1Label;
  private Label
    info2Label;
  private Label
    info3Label;
  private Label   
    info4Label;
  private Label 
    info5Label;
  private Label
    info6Label;
  
  
  /**
   * Info() - constructor
   * @param lms is landmark set
   * @param p is panel where we stuff it in the BuildGUI
   */
  public Info(Flicker flk, JPanel msgPanel)
  { /* Info */
    this.flk= flk;
    this.aff= flk.aff;
    this.bGui= flk.bGui;
    this.lms= flk.lms;
    this.util= flk.util;
    
   this.msgPanel= msgPanel;	
        
    info1Label= new Label("",Label.LEFT);
    info2Label= new Label("",Label.LEFT);
    info3Label= new Label("",Label.LEFT);
    info4Label= new Label("",Label.LEFT);
    info5Label= new Label("",Label.LEFT);
    info6Label= new Label("",Label.LEFT);
    
    info1Label.setFont(flk.textFont);
    info2Label.setFont(flk.textFont);
    info3Label.setFont(flk.textFont);
    info4Label.setFont(flk.textFont);
    info5Label.setFont(flk.textFont);
    info6Label.setFont(flk.textFont);
   
    msgPanel.add(info1Label);
    msgPanel.add(info2Label);
    msgPanel.add(info3Label);
    msgPanel.add(info4Label);
    msgPanel.add(info5Label);
    msgPanel.add(info6Label);
  } /* Info */

  
  /**
   * updateInfoString() - update Flicker Info string messages.
   * This is currently done with a set of labels.
   * NOTE: call repaint() separately - not repainted from here!
   */
  public void updateInfoString()
  { /* updateInfoString */
    String
      imgToXform,		/* image to transform */
      s1,		        /* #LMS: n, transform:<left><right><both>*/
      s2= "",		  	/* L[nLM] x1:y1,x2:y2 ... */
      s3= "",			/* x'= ... (affine) */
      s4= "",			/* y'= ... (affine) */
      s5= "",			/* LSQ1=..., LSQ2=... (affine)*/
      s6;		      	/* Mem T:... F:...  */
    
    /* Set text indicating active image It will be "left", "right"
     * or "both".
     */
    if(flk.activeImage=="left")
      imgToXform= "Active image: left";
    else if(flk.activeImage=="right")
      imgToXform= "Active image: right";
    else
      imgToXform= "Active image: both";
    
    s1= imgToXform;
    
    if(flk.lms!=null)
    { /* show landmark */
      int
        j;
      
      if(lms.nLM>=1)
      {
        j= lms.nLM-1;
        s2= "LM"+(j+1) + " " +
             lms.x1[j]+ "," + lms.y1[j]+
             ":" + lms.x2[j] + "," + lms.y2[j];
      }
      else
        s2="No landmarks are defined";
    } /* show landmark */
    
    if(flk.validAffineFlag)
    { /* show affine related data */
      int  p= 1;		/* precision */
      /* x'=.94x-0.15y+40.18
       * y'=.2x+1.03y-77.96]
       */
      s3= ("x'=" + util.cvf2s(aff.a,p) +
           "*x" + ((aff.b>=0) ? "+" : "") +
           util.cvf2s(aff.b,p) +
           "*y" + ((aff.c>=0) ? "+" : "") +
           util.cvf2s(aff.c,p));
      s4= ("y'=" + util.cvf2s(aff.d,p) +
           "*x" + ((aff.e>=0) ? "+" : "") +
           util.cvf2s(aff.e,p) +
           "*y" + ((aff.f>=0) ? "+" : "") +
           util.cvf2s(aff.f,p));
           
      /*  LSQerr=6.2 */
      float maxLSQerr= Math.max(aff.minLSQcolinearity1,
                                aff.minLSQcolinearity2);
      s5= "LSQerr=" + util.cvf2s(maxLSQerr,p);
    } /* show affine related data */
    
    /* Always show current memory usage */
    Runtime rt= Runtime.getRuntime();
    long
      freeMem= rt.freeMemory(),     /*Estimate is not always accurate */
      totalMem= rt.totalMemory();
    s6= "T:"+ totalMem + " F:" + freeMem;
    
    /* Draw labels */
    info1Label.setText(s1);
    info2Label.setText(s2);
    info3Label.setText(s3);
    info4Label.setText(s4);
    info5Label.setText(s5);
    info6Label.setText(s6);
    
    /* Update Message Label */    
    bGui= flk.bGui;
    Color lastMsgColor= util.getLastMsgColor();
    if(bGui!=null && bGui.textMsgLabel1!=null && bGui.textMsgLabel2!=null)
    {
      bGui.textMsgLabel1.setForeground(flk.lastMsgColor);
      bGui.textMsgLabel1.setText(util.getLastMsgStr());
    
    /* Force label to reside to fit the text.
     * See "The Java Class Libraries" page 862.
     */
      bGui.textMsgLabel1.invalidate();
      bGui.textMsgLabel1.getParent().validate(); 
      bGui.textMsgLabel2.invalidate();
      bGui.textMsgLabel2.getParent().validate();   
    }
  } /* updateInfoString */
   
  
  /**
   * setInfo1Text() - set text for info1 field (used for last landmark)
   * @param msg to display
   */
  public void setInfo1Text(String msg)
  {
    info1Label.setText(msg);
  }
  
  
  /**
   * showScrollCoords() - show the scroll bar coordinates
   * @param showMaxLimitFlag to modify the display
   */
  public void showScrollCoords(boolean showMaxLimitFlag)
  { /* showScrollCoords */
    int
      x1= flk.i1IS.getImgCursor().x,
      y1= flk.i1IS.getImgCursor().y,
      x2= flk.i2IS.getImgCursor().x,
      y2= flk.i2IS.getImgCursor().y;
    Point
      obj1= flk.i1IS.getObjPosition(),
      obj2= flk.i2IS.getObjPosition(),
      img1= flk.i1IS.getImgPosition(),
      img2= flk.i2IS.getImgPosition();
    
    if(showMaxLimitFlag)
    {
      int
        x1m= flk.i1IS.getImgCursorMax().x,
        y1m= flk.i1IS.getImgCursorMax().y,
        x2m= flk.i2IS.getImgCursorMax().x,
        y2m= flk.i2IS.getImgCursorMax().y;
      
      util.showMsg(" cursor[x1,y1 : x2,y2]=[" + x1 + "," + y1 + " : " +
                   x2 + "," + y2 + "]" +
                   " cursorMax[x1,y1 : x2,y2]=[" + x1m + "," + y1m +
                   " : " + x2m + "," + y2m + "]",
                   Color.blue);
    }
    else
      util.showMsg(" cursor[x1,y1 : x2,y2]=[" + x1 + "," + y1 + " : " +
                   x2 + "," + y2 + "]" +
                   " obj[]=[" + obj1.x + "," + obj1.y + " : " +
                   obj2.x + "," + obj2.y + "]" +
                   " img[]=[" + img1.x + "," + img1.y + " : " +
                   img2.x + "," + img2.y + "]",
                   Color.blue );
  } /* showScrollCoords */
  
  
  
} /*  End of class Info */

/* File: EventKbd.java */

import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.TextEvent;
import java.util.*;
import java.util.EventListener;
import java.lang.*;

/**
 * EventKbd class supports keyboard event handling for Flicker 
 * top level GUI.
 * NOTE: MenuItem's and CheckboxMenuItem's are serviced by EventMenu.
 * This is meant for keyboard commands that are NOT in the menus.
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

public class EventKbd implements KeyListener, TextListener
{ /*  EventKbd */
  private Flicker
    flk;
  private Util
    util;
  private Info
    info;
  private Landmark
    lms;
  
  /**
   * EventKbd() - constructor
   * @param flk instance
   */
  public EventKbd(Flicker flk)
  {
    this.flk= flk;
    this.util= flk.util;
    lms= flk.lms;
  }
    
  
  /**
   * keyPressed() - handle key down events
   * @param e is KeyEvent
   */
  public void keyPressed(KeyEvent e)
  { /* keyPressed */
    String keyT= e.getKeyText(e.getKeyCode());    
    int
      key= e.getKeyChar(),
      modifiers= e.getModifiers(),
      val;
    boolean
      ctrlMod= ((modifiers & Event.CTRL_MASK) != 0),
      shiftMod= ((modifiers & Event.SHIFT_MASK) != 0),
      bVal;    
    ImageData iData= null;      /* will be null if not left or right */
    ImageScroller lastIS= null;
    
    if(flk.activeImage.equals("left"))
    {
      iData= flk.iData1;
      lastIS= flk.i1IS;
    }
    else if(flk.activeImage.equals("right")) 
    {
      iData= flk.iData2;
      lastIS= flk.i2IS;
    }
      
    if(key==65535 /* Control key */)
      return;           /* only handle Control/key presses */
    
    if(flk.dbugFlag)
      util.showMsg("key=" + key + "='" + (char)key + "' CTL=" + ctrlMod,
                   Color.blue);
    
    switch(key)
    { /* process keys */       
      case 1:  /* KeyEvent.VK_A CTRL/A (1) add a landmark
                * Add a new landmark handled in EventMenu by short-cut
                */
        break;
        
      case 2:	 /* KeyEvent.VK_B CTRL/B (2) capture background gray value
                * handled in EventMenu handler
                */
        break;
            
      case 4:	 /* KeyEvent.VK_D CTRL/D (4) delete last landmark 
                * delete landmark by Undoing the last landmark
                *  handled in EventMenu by short-cut 
                */
        break;
            
      case 5:	 /* KeyEvent.VK_E CTRL/E (5) edit selected spot if it exists 
                *  handled in EventMenu by short-cut 
                */
        break;
            
      case 6:	 /* KeyEvent.VK_F CTRL/F (6) toggle Flickering state 
                *  handled in EventMenu by short-cut 
                */
        break;
        
      case 7:	 /* KeyEvent.VK_G CTRL/G (7) toggle Display gray value 
                * display (x,y) coords else gray values
                * handled in EventMenu by short-cut.
                */
        break;
            
      case 8:	 /* KeyEvent.VK_H CTRL/H (8) popup computing window ROI
                * histogram -  handled in EventMenu by short-cut 
                */
        break;
            
      case 9:	 /* KeyEvent.VK_I CTRL/I (9) deletes selected spot if it exists 
                *  handled in EventMenu by short-cut 
                */
        break;
                         
      case 12: /* KeyEvent.VK_L CTRL/L (12) define LRHC of 
                * computing window handled in EventMenu by short-cut.
                */
        break; 
        
      case 13:  /* KeyEvent.VK_M CTRL/M (13) capture measurement gray value
                 * handled in EventMenu handler
                 */
        break;
        
      case 16:  /* KeyEvent.VK_P CTRL/P (16) toggle dbugFlag */
        flk.dbugFlag= !flk.dbugFlag;
        flk.util.showMsg("Debug reporting is "+
                         (String)((flk.dbugFlag) ? "ON" : "OFF"),
                         Color.black);
        break;
        
      case 17:  /* KeyEvent.VK_Q CTRL/Q (17) C-Q Stop lookup of annotation
                 * data from from proteomic Web server
                 */
        flk.stopAnnotationUpdateFlag= true;
                     
      case 21: /* KeyEvent.VK_U CTRL/U (21) define ULHC of computing window
                * handled in EventMenu by short-cut.
                */
        break;
        
      case 23:  /* KeyEvent.VK_W CTRL/W (23) */
                /* Clear the ROI in current image
                * handled in EventMenu by short-cut.
                */
        break;
      
      case 25:  /* KeyEvent.VK_Y CTRL/Y (25) - only for demo gels 3 
                 * landmarks for affine warping 
                 * handled in EventMenu by short-cut.
                 */
        break;
        
      case 26:  /* KeyEvent.VK_Z CTRL/Z (26) - only for demo gels 6 
                 * landmarks for polynomial warping
                 * handled in EventMenu by short-cut.
                */
        break;
        
        /* ... [TODO]... add whatever we need if any...*/
      default:
        break;
    } /* process keys */ 
    
  } /* keyPressed */
  
    
  public void keyReleased(KeyEvent e) { }
  public void keyTyped(KeyEvent e) { }
  
  
  /**
   * textValueChanged() - handle text value changed events
   * @param e is TextEvent
   */
  public void textValueChanged(TextEvent e)
  { /* textValueChanged */
  } /* textValueChanged */
  
  
} /* end of EventKbd  */

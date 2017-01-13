/* File: SliderState.java */

import java.util.*;
import java.lang.*;

/**
 * Class State contains the sliders state for an ImageScroller instance.
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

public class SliderState
{ /* SliderState */
  
  /* Global links */
  private static Flicker
    flk;
  /* Global links */
  private static Util
    util;
    
  /* ---- max and min values ---- */	
  /** max zScale percent */
  final public static int
    MAX_ZSCALE= 50;	
  /** min zScale percent */
  final public static int
    MIN_ZSCALE= 0;
  /** max degrees */
  final public static int
    MAX_ANGLE= 45;		
  /** min degrees */
  final public static int
    MIN_ANGLE= -45;		
  /** max eScale percent */
  final public static int
    MAX_ESCALE= 50;		
  /** min eScale percent */
  final public static int
    MIN_ESCALE= 0;		
  /** max brightness baseline */
  final public static int
    MAX_BRIGHTNESS= 100;	
  /** min brightness baseline */
  final public static int
    MIN_BRIGHTNESS= 0;		
  /** max constrast x 0.10 slope */
  final public static int
    MAX_CONTRAST= 100;		
  /** min constrast x 0.10 slope */
  final public static int
    MIN_CONTRAST= 1;		
  /** max GRAY threshold 1 */
  final public static int
    MAX_THRESHOLD1= 255;	
  /** min GRAY threshold 1 */
  final public static int
    MIN_THRESHOLD1= 0;	
  /** max GRAY threshold 2 */
  final public static int
    MAX_THRESHOLD2= 255;	
  /** min GRAY threshold 2 */
  final public static int
    MIN_THRESHOLD2= 0;			
  /** max X magnificationAWT */
  final public static int
    MAX_ZOOM= 5;				
  /** min X magnificationAWT */
  final public static int
    MIN_ZOOM= 1;
  /** max zoomMag (zoom/dezoom) X magnification mapped to [-1/10X : 10X] */
  final public static int
    MAX_ZOOM_MAG_SCR= 200;				
  /** min zoomMag (zoom/dezoom) X magnification mapped to [-1/10X : 10X] */
  final public static int
    MIN_ZOOM_MAG_SCR= 0;		
  
  /** max zoomMag value (zoom/dezoom) X magnification */
  final public static float
    MAX_ZOOM_MAG_VAL= 5.0F;				
  /** min zoomMag value (zoom/dezoom) X magnification*/
  final public static float
    MIN_ZOOM_MAG_VAL= 0.10000F;		
  
  /* ---- default values ---- */	
  /** rotation angle 10 degrees */
  final public static int
    DEF_ANGLE= 15;		
  /** default zScale 5 percent */
  final public static int
    DEF_ZSCALE= 10;		
  /** default eScale 20 percent */
  final public static int
    DEF_ESCALE= 20;		
  /** default contrast */
  final public static int
    DEF_CONTRAST= 50;	
  /** default brightness */
  final public static int
    DEF_BRIGHTNESS= 50;	
  /** MAX gray */
  final public static int
    DEF_THRESHOLD2= 255;	
  /** MIN gray */
  final public static int
    DEF_THRESHOLD1= 0;			
  /** default zoom X magnificationAWT*/
  final public static int
    DEF_ZOOM= 1;					
  /** default zoomMag X magnification scroller value */
  final public static int
    DEF_ZOOM_MAG_SCR= (MAX_ZOOM_MAG_SCR - MIN_ZOOM_MAG_SCR)/2;
  
  /* -------------------- INSTANCE ------------- */
  /* Name of the state */
  public String
    name;  
    
  /** GUI state: lower bound threshold for grayscale slicing */
  public int
    threshold1;		
  /** GUI state: upper bound threshold for grayscale slicing */
  public int
    threshold2;		
  /** GUI state: degrees 3D perspective xform */
  public int
    angle;		
  /** GUI state: contrast in [MIN_BRIGHTNESS:MAX_BRIGHTNESS] */
  public int
    brightness;	
  /** GUI state: contrast in [MIN_CONTRAST:MAX_CONTRAST] */
  public int
    contrast;		
  /** opt. image delay if flickering */
  public int 
    delay;		      	
  /** GUI state: scale factor for image sharpening xform */
  public int
    eScale;			      
  /** GUI state: zoom mangification in range [1X:MAX_ZOOM] */
  public int
    magnificationAWT;	
  /** GUI state: % grayscale factor 3D xform */
  public int
    zScale;			      
  /** GUI scroller state: dezoom and zoom magnification in
   * range [MIN_ZOOM_MAG_SCR : MAX_ZOOM_MAG_SCR] 
   */
  public int	  
    zoomMagScr;
  /** GUI state radius of circle mask size used for flk.measCircleRadius */
  public int
    measCircleRadius;
  /** NOT a GUI slider. This is the last (C-B) measurement take using
   * the current flk.measCircleRadius at the time. We keep this here since
   * the value is dependent on the image - NOT the global value.
   */
  public int
    bkgrdCircleRadius;
     		      
  /** GUI magnification state: dezoom and zoom magnification in
   * range [MIN_ZOOM_MAG_VAL : MAX_ZOOM_MAG_VAL] 
   */
  public float	  
    zoomMagVal;
  
   
 /**
  * SliderState() - constructor
  */ 
  public SliderState()
  { /* SliderState */           
  } /* SliderState */
  
  
 /**
  * init() - set state to default values.
  * @param flicker is instance of Flicker
  * @param name of the SliderState
  */ 
  public void init(Flicker flicker, String name)
  { /* init */       
    flk= flicker;
    util= flk.util;  
    this.name= name;  
    
    reset();
  } /* init */
   
  
 /**
  * reset() - reset state to default values.
  */ 
  private void reset()
  { /* reset */ 		  
    this.angle= DEF_ANGLE;		  
    this.brightness= DEF_BRIGHTNESS;	  
    this.contrast= DEF_CONTRAST;	  
    this.delay= BuildGUI.DEFAULT_DELAY;		      	  
    this.eScale= DEF_ESCALE;			        
    this.magnificationAWT= DEF_ZOOM;
    this.threshold1= DEF_THRESHOLD1;    
    this.threshold2= DEF_THRESHOLD2;	  
    this.zScale= DEF_ZSCALE;	
    this.zoomMagScr= DEF_ZOOM_MAG_SCR; /* actual value */
    this.zoomMagVal= cvtZoomMagScr2ZoomMagVal(this.zoomMagScr);
    this.measCircleRadius= flk.measCircleRadius;
    this.bkgrdCircleRadius= flk.bkgrdCircleRadius;
  } /* reset */
    
  
 /**
  * clone() - clone a copy of the src SliderState
  * @param src SliderState to clone
  * @return the cloned SliderState instance
  */ 
  public SliderState clone(SliderState src)
  { /* clone */
    SliderState dst= new SliderState();
    copy(src,dst);
    return(dst);
  } /* clone */
  
  
  /**
   * copy() - copy src SliderState to dst SliderState
   * @param src SliderState to copy
   * @param dst SliderState that we copy to
   * @return true if succeed
   */
  private boolean copy(SliderState src, SliderState dst)
  { /* copy */ 
    if(src==null || dst==null)
      return(false);
    
    dst.angle= src.angle;	    		  
    dst.brightness= src.brightness;
    dst.contrast= src.contrast;	  
    dst.delay= src.delay;		      	  
    dst.eScale= src.eScale;  		        
    dst.magnificationAWT= src.magnificationAWT;
    dst.threshold1= src.threshold1; 
    dst.threshold2= src.threshold2;
    dst.zScale= src.zScale;	
    dst.zoomMagScr= src.zoomMagScr;
    dst.zoomMagVal= cvtZoomMagScr2ZoomMagVal(src.zoomMagScr);
    dst.measCircleRadius= src.measCircleRadius;
    dst.bkgrdCircleRadius= src.bkgrdCircleRadius;
     		      
    return(true);
  } /* copy */  
  
    
 /**
  * readState() - Read scroller state from .flk startup state file.
  * @param iName of the image to read (e.g., "I1", or "I2")
  */ 
  public void readState(String iName)
  { /* readState */
     angle= util.getStateValue(iName+"-angle", DEF_ANGLE);
     brightness= util.getStateValue(iName+"-brightness", DEF_BRIGHTNESS);
     contrast= util.getStateValue(iName+"-contrast", DEF_CONTRAST);
     delay= util.getStateValue(iName+"-delay", BuildGUI.DEFAULT_DELAY);
     eScale= util.getStateValue(iName+"-eScale", DEF_ESCALE);
     magnificationAWT= util.getStateValue(iName+"-magnificationAWT", DEF_ZOOM);
     threshold1= util.getStateValue(iName+"-threshold1", DEF_THRESHOLD1);
     threshold2= util.getStateValue(iName+"-threshold2", DEF_THRESHOLD2);
     zScale= util.getStateValue(iName+"-zScale", DEF_ZSCALE); 
     zoomMagScr= util.getStateValue(iName+"-zoomMagScr", DEF_ZOOM_MAG_SCR); 
     zoomMagVal= cvtZoomMagScr2ZoomMagVal(zoomMagScr); /* map it */
     measCircleRadius= util.getStateValue(iName+"-measCircleRadius",
                                          flk.measCircleRadius); 
     bkgrdCircleRadius= util.getStateValue(iName+"-bkgrdCircleRadius",
                                           flk.bkgrdCircleRadius); 
  } /* readState */
  
    
 /**
  * writeState() - Write this scroller state to string buffer sBuf
  * @param iName of the image to read (e.g., "I1", or "I2")
  * @param sBuf is the string buffer to write to.
  */ 
  public void writeState(String iName, StringBuffer sBuf)
  { /* writeState */ 
    sBuf.append(iName+"-angle\t"+angle+"\n");
    sBuf.append(iName+"-brightness\t"+brightness+"\n");
    sBuf.append(iName+"-contrast\t"+contrast+"\n");
    sBuf.append(iName+"-delay\t"+delay+"\n");
    sBuf.append(iName+"-eScale\t"+eScale+"\n");
    sBuf.append(iName+"-magnificationAWT\t"+magnificationAWT+"\n");
    sBuf.append(iName+"-threshold1\t"+threshold1+"\n");
    sBuf.append(iName+"-threshold2\t"+threshold2+"\n");
    sBuf.append(iName+"-zScale\t"+zScale+"\n");
    sBuf.append(iName+"-zoomMagScr\t"+zoomMagScr+"\n");
    sBuf.append(iName+"-measCircleRadius\t"+measCircleRadius+"\n");
    sBuf.append(iName+"-bkgrdCircleRadius\t"+bkgrdCircleRadius+"\n");
  } /* writeState */
  
    
 /**
  * getStateStr() - get this scroller state as a string
  * @param iData is the image data to use
  * @param fileName associated with the iData
  * @return state string
  */ 
  public String getStateStr(ImageData iData, String fileName)
  { /* getStateStr */ 
    boolean isGrayScaleFlag= (iData.calib.unitsAbbrev.equals("gray"));
    String sR= "State scroller values for ["+name+"] image '"+fileName+"'\n";
    sR += "   angle = "+angle+" degrees\n";
    sR += "   brightness = "+(brightness - MAX_BRIGHTNESS)+" %\n";
    sR += "   contrast = "+(contrast - MAX_CONTRAST)+" %\n";
    sR += "   delay = "+delay+" mSec\n";
    sR += "   e-scale = "+eScale+" %\n";
    sR += "   magnificationAWT = "+magnificationAWT+" X\n";
    sR += "   threshold 1 = "+((isGrayScaleFlag)
                                 ? (threshold1+" gray value")
                                 : (iData.mapGrayToOD[threshold1]+
                                    " "+iData.calib.unitsAbbrev))+"\n"; 
    sR += "   threshold 2 = "+((isGrayScaleFlag)
                                ? (threshold2+" gray value")
                                : (iData.mapGrayToOD[threshold2]+
                                   " "+iData.calib.unitsAbbrev))+"\n"; 
    sR += "   z-scale = "+ zScale+" %\n";
    sR += "   zoomMag = "+ cvtZoomMagScr2Str(zoomMagScr)+" X";
    sR += "   measurement circle = "+(2*measCircleRadius+1)+
                                        " (diameter)\n";
     
    return(sR);
  } /* getStateStr */
    
  
  /**
   * cvtZoomMagScr2Str() - convert a zoomMag scroller value to a 
   * prettyprint string with 2 digits.
   * @param zmScr is zoomMag scroller value to convert to 2 digit mag string
   * @return converted string
   */
  public static String cvtZoomMagScr2Str(int zmScr)
  { /* cvtZoomMagScr2Str */
    float zmVal= cvtZoomMagScr2ZoomMagVal(zmScr);
    String sR= util.cvf2s(zmVal,2);
    if(flk.NEVER && flk.dbugFlag)
      System.out.println("SS-CZM2S zmScr="+zmScr+" zmVal="+zmVal+" sR="+sR);
    
    return(sR);
  } /* cvtZoomMagScr2Str*/ 
  
  
  /**
   * cvtZoomMagScr2ZoomMagVal() - convert a zoomMag scroller value to 
   * a zoom mag value.
   * Compute this by mapping the scroller range [MIN_ZOOM_MAG : MAX_ZOOM_MAG]
   * to the value range [MIN_ZOOM_MAG_VAL : MAX_ZOOM_MAG_VAL]
   * by solving the linear equation and then interpolating the value.
   *</PRE>
   * @param zmScr is the zoomMag integer scroller value  in the
   *        range of [MIN_ZOOM_MAG : MAX_ZOOM_MAG] to convert
   * @return converted value in the magnification domain
   */
  public static float cvtZoomMagScr2ZoomMagVal(int zmScr)
  { /* cvtZoomMagScr2ZoomMagVal */    
    int zm = (zmScr - DEF_ZOOM_MAG_SCR);
    float
      zmVal= 0.0F,   /* corresponding magnification value */
      m= 1.0F,       /* slope */
      b= 0.0F;       /* intercept */
    
    if(zm>=0)
    { /* magnify */
      m= (MAX_ZOOM_MAG_VAL - 1.0F) / (MAX_ZOOM_MAG_SCR - DEF_ZOOM_MAG_SCR);
      b= MAX_ZOOM_MAG_VAL - (m * MAX_ZOOM_MAG_SCR);
      zmVal= (m * zmScr) + b; 
    }
    else
    { /* demagnify */
      m= (1.0F - MIN_ZOOM_MAG_VAL) / (DEF_ZOOM_MAG_SCR - MIN_ZOOM_MAG_SCR);
      b= 1.0F - (m * DEF_ZOOM_MAG_SCR);
      zmVal= (m * zmScr) + b; 
    }
    
    if(flk.NEVER && flk.dbugFlag)
      System.out.println("SS-CZMS2ZMV zm="+zm+" zmVal="+zmVal+
                         " = fct(zmScr="+zmScr+")"+ " m="+m+" b="+b);
    
    /* limit the zoom */
    float magVal= zmVal;
    magVal= Math.max(magVal, MIN_ZOOM_MAG_VAL);
    magVal= Math.min(magVal, MAX_ZOOM_MAG_VAL);
    
    return(magVal);
  } /* cvtZoomMagScr2ZoomMagVal */
  
      
}  /* end of class SliderState */
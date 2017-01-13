/* File: Landmark.java */

import java.awt.*;
import java.util.*;
import java.lang.*;
import java.io.*;
 
/**
 * Landmark class supports landmark data structures and operations.
 * Landmarks are numbered internally [0:51] and vlsually as overlays
 * by the ImageScroller.drawLandmarksTextInImage() method
 * as [A:Z,a:z] and are display as "+letter" in the selected landmark
 * color. They are used by the warping transforms (AffineWarp, etc).
 *
 *<P>
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government. As a work of
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

public class Landmark
{ /* class Landmark */
  
  private static Flicker
    flk;
  private static Util
    util;
  /** 3 for affine, but 10x10 for warp */
  final static int
    MAXLMS= 100;		
  
  /* Preset  <STATE> landmarks for LM[image 1 or 2][a,b,c] */
  public static int
    xLM1a,			/** from <STATE> else -1  */
    yLM1a,			/** from <STATE> else -1  */
    xLM2a,			/** from <STATE> else -1  */
    yLM2a,			/** from <STATE> else -1  */
    xLM1b,			/** from <STATE> else -1  */
    yLM1b,			/** from <STATE> else -1  */
    xLM2b,			/** from <STATE> else -1  */
    yLM2b,			/** from <STATE> else -1  */
    xLM1c,			/** from <STATE> else -1  */
    yLM1c,			/** from <STATE> else -1  */
    xLM2c,			/** from <STATE> else -1  */
    yLM2c;			/** from <STATE> else -1  */
  
  /** font for drawing LMS overlay */
  static Font
    lmsFont= new Font("Helvetica",Font.PLAIN,10); 
  
  /** max # of landmarks allowed */
  public static int
    maxLMS= MAXLMS; 
  /** highest landmark # */
  public static int
    nLM= 0;	
  
  /** X coordinates in left input image 1 */
  public static int
    x1[]= null;
  /** Y coordinates in left input image 1 */
  public static int
    y1[]= null;
  /** X coordinates in right input image 2 */
  public static int
    x2[]= null;
  /** Y coordinates in right input image 2 */
  public static int
    y2[]= null;
  
  /** X coordinates in left output image 1 */
  public static int
    ox1[]= null;
  /** Y coordinates in left output image 1 */
  public static int
    oy1[]= null;
  /** X coordinates in right output image 2 */
  public static int
    ox2[]= null;
  /** Y coordinates in right output image 2 */
  public static int
    oy2[]= null;
  
  public static Color
    colorLM[]= null;		/** color of the landmark */
  public static Font
    fontLM[]= null;		/** font of the landmark */
  public static String
    nameLM[]= null;		/** name of landmark '+[A-Z]' */
  
 
 /**
  * Landmark() - Constructor.
  * @param flk is main class 
  * @param lmsFont is font to use for all landmarks
  * @param maxLMS is max # of landmarks will ever use.
  *            if maxLMS==0, use MAXLMS
  */
 public Landmark(Flicker flkS, Font lmsFontUse, int maxLMSuse)
 { /* Landmark */
   flk= flkS;
   util= flk.util;  
   
   if(lmsFont!=null)
     lmsFont= lmsFontUse;
   if(maxLMSuse>0)
     maxLMS= maxLMSuse;
   
   clean(); /* reinitialize the landmarks data structures */
 } /* Landmark */
 
 
 /**
  * clean() - reinitialize the landmarks data structures
  */
 public static void clean()
 { /* cleanup */   
   nLM= 0;
   
   x1= new int[maxLMS];    /* input images landmarks */
   y1= new int[maxLMS];
   x2= new int[maxLMS];
   y2= new int[maxLMS];
   
   ox1= new int[maxLMS];   /* output images landmarks */
   oy1= new int[maxLMS];
   ox2= new int[maxLMS];
   oy2= new int[maxLMS];
   
   colorLM= new Color[maxLMS];
   fontLM= new Font[maxLMS];
   nameLM= new String[maxLMS];

   for(int i=0; i<maxLMS; i++)
   { /* Setup the colors and landmark names*/
     colorLM[i]= flk.lmsColor;
     fontLM[i]= lmsFont;	/* specified in message */
     /* [BUG] if maxLMS> |A:Za:z| then bogus */
     /* define landmark name "+[A:Za:z]" with '+' prefix */
     nameLM[i]=  "+" + ((i<=26) ? ((char)(i+'A')) : ((char)(i+'a')));
   }
 } /* cleanup */
 
 
 /**
  * setColorAll() - set the landmark color for all landmarks
  */
 public static void setColorAll(Color color)
 { /* setColorAll */
   if(color==null)
     return;
   for(int i=0; i<maxLMS; i++)
     colorLM[i]= flk.lmsColor;
 } /* setColorAll */
 
 
 /**
  * updateScrollers() - update LM in both siCanvases and repaint.
  */
 public static void updateScrollers()
 { /* updateScrollers */
     
    /* Indicate in images by drawing the landmark "+#"s in RED */
    flk.repaint();
    flk.i1IS.paintSiCanvas();
    flk.i2IS.paintSiCanvas();
    
    flk.i1IS.siCanvas.setLandmarksTextListToDraw(nLM,colorLM,nameLM,fontLM,x1,y1);                               
    flk.i2IS.siCanvas.setLandmarksTextListToDraw(nLM,colorLM,nameLM,fontLM,x2,y2);
 } /* updateScrollers */ 
 
 
 /**
  * pushLandmark() - push (x1,y1,x2,y2) and return nLM.
  * if list is full, return -1.
  * if new LM is already in list, return -2.
  * @param x1d coordinate image 1
  * @param y1d coordinate image 1
  * @param x2d coordinate image 2
  * @param y2d coordinate image 2
  * @return if new LM is already in list, return -2, if list is full, return -1.
  *         otherwise nLM
  */
 public static int pushLandmark(int x1d, int y1d, int x2d, int y2d)
 { /* pushLandmark */
   int val;
   
   if(nLM>=maxLMS)
   { /* failed - too lamn landmarks */
     val= -1;		
   }   
   else if(nLM>0 &&
           x1[nLM-1]==x1d && y1[nLM-1]==y1d &&
           x2[nLM-1]==x2d && y2[nLM-1]==y2d)
   { /* duplicate landmark to last one pushed */
     val= -2;	     
   }
   else
   { /* push it */
     x1[nLM]= x1d;   /* push into input images landmarks */
     y1[nLM]= y1d;
     x2[nLM]= x2d;
     y2[nLM]= y2d;
     
     ox1[nLM]= x1d;   /* push into output images landmarks */
     oy1[nLM]= y1d;
     ox2[nLM]= x2d;
     oy2[nLM]= y2d;
     
     nLM++;
     val= nLM;
   }
   
   return(val);
 } /* pushLandmark */
 
 
 /**
  * deleteLastLandmark() - delete last landmark
  * @return new nLM
  */
 public int deleteLastLandmark()
 { /* deleteLastLandmark */
   if(nLM > 0)
     nLM--;   
   return(nLM);
 } /* deleteLastLandmark */
 
 
 /**
  * landmarkSimilarity() - compute landmark similarity for >=3 LMs.
  *<PRE>
  * Similarity is defined as 
  *    Sqrt( |sum(LSQerr(x1,y1))-sum(LSQerr(x2,y2))| )
  *</PRE>
  * @return -1.0F if not enough landmarks, else the RMS differences
  * within each landmark set/gel.
  */
 public float landmarkSimilarity()
 { /* landmarkSimilarity */
   double
     sumDiffSq1= 0.0,
     sumDiffSq2= 0.0;
   float similarity= -1.0F;	/* failed */
   
   if(nLM >= 3)
   { /* do it */
     int
       xM1= 0,
       yM1= 0,
       xM2= 0,
       yM2= 0;
     for(int i= 0; i<nLM; i++)
     {
       xM1 += x1[i];
       yM1 += y1[i];
       xM2 += x2[i];
       yM2 += y2[i];
     }
     xM1 /= nLM;            /* Compute the means */
     yM1 /= nLM;
     xM2 /= nLM;
     yM2 /= nLM;
     
     for(int i= 0; i<nLM; i++)
     {
       sumDiffSq1 += ((xM1- x1[i])*(xM1- x1[i]) +
                      (yM1- y1[i])*(yM1- y1[i]));
       sumDiffSq2 += ((xM2- x2[i])*(xM2- x2[i]) +
                      (yM2- y2[i])*(yM2- y2[i]));
     }
     similarity= (float)Math.sqrt(Math.abs(sumDiffSq1-sumDiffSq2)/nLM);
   }
   
   return(similarity);
 } /* landmarkSimilarity */

 
 /**
  * showLandmarkSimilarity() - show landmark similarity for >=3 LMs
  */
 public void showLandmarkSimilarity()
 { /* showLandmarkSimilarity */
   float similarity= landmarkSimilarity();
   
   if(similarity<0.0F)
   {
     String msg= "Need 3 or more LMs to show similarity.";
     util.popupAlertMsg(msg, flk.alertColor);
   }
   else
   {
     String curLmsStr= getStateStr();
     util.showMsg(curLmsStr, Color.black);
     util.showMsg("Landmarks LSQ err similarity = " + 
                  util.cvf2s(similarity,3), Color.black);
     util.showMsg2("", Color.black);
   }
 } /* showLandmarkSimilarity */
 
 
  /**
   * addLandmark() - "Add LM" add temporary landmarks to landmark list
   * If exist in both I1 and I2 images into  permanent landmarks
   * and indicate in images push landmark.
   * Clear the obj coordinates.
   * @return true if succeed.
   */
  public boolean addLandmark()
  { /* addLandmark */
    int rVal;
    Point
      i1Obj,
      i2Obj;
    
    if(! flk.i1IS.img_selectedFlag || ! flk.i2IS.img_selectedFlag)
    {
      String msg= "Def points in BOTH images before Mark Landmark.";
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    
    i1Obj= flk.i1IS.getObjPosition();
    i2Obj= flk.i2IS.getObjPosition();
    Point 
      pt1,
      pt2;
        
    if(flk.useGuardRegionImageFlag)
    {
      pt1= flk.i1IS.iData.mapZoomToState(i1Obj,(int)(flk.i1IS.guardWidth/2),
                                        (int)(flk.i1IS.guardHeight/2));
      pt2= flk.i2IS.iData.mapZoomToState(i2Obj,(int)(flk.i2IS.guardWidth/2),
                                        (int)(flk.i2IS.guardHeight/2));   
    }
    else
    {
      pt1= flk.i1IS.iData.mapZoomToState(i1Obj);
      pt2= flk.i2IS.iData.mapZoomToState(i2Obj);      
    }
    
    rVal= pushLandmark(pt1.x, pt1.y,
                       pt2.x, pt2.y);    
    if(rVal == -1)
    { /* check failed */
      String msg= "Max # of landmarks reached, delete some before add more.";
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    
    if(rVal == -2)
    {
      String msg= "Can't enter same landmark twice - pick another.";
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    
    /* Update the State messages */
    flk.info.updateInfoString(); /* update Flicker info string. */
    
    
    /* Reset the obj select flags */
    flk.i1IS.img_selectedFlag= false;
    flk.i2IS.img_selectedFlag= false;
    
    util.showMsg("Added new landmark", Color.black); 
    updateScrollers();
         
    return(true);
  } /* addLandmark */
  
    
  /**
   * deleteLandmark() - "Delete LM" undo the last landmark if any
   */
  public void deleteLandmark()
  { /* deleteLandmark */
    if(nLM == 0)
    { /* check failed */
      flk.i1IS.img_selectedFlag= false;
      flk.i2IS.img_selectedFlag= false;
      String msg= "No more landmarks to delete.";
      util.popupAlertMsg(msg, flk.alertColor);
    }
    else
    { /* delete last landmark */
      flk.info.setInfo1Text(nLM + " Landmarks");
      util.showMsg("Deleting Landmark #" + nLM, Color.black);
      deleteLastLandmark();
      
      flk.info.updateInfoString(); /* update state string. */
      
      /* Reset the obj select flags */
      flk.i1IS.img_selectedFlag= false;
      flk.i2IS.img_selectedFlag= false;      
      
      updateScrollers();  
       
    } /* delete last landmark */    
   
  } /* deleteLandmark */
  

  /**
   * readState() - Read state from .flk startup state file.
  */ 
  void readState()
  { /* readState */
    nLM= 0;         /* reset it */
    int nRead= util.getStateValue("LMS-nLM", 0);
    if(nRead<=0 || nRead>=maxLMS)
      return;
    int 
      x1,y1,
      x2,y2; 
    String name;
    for(int i=0;i<nRead;i++)
    {
      name= util.getStateValue("LMS-nameLM["+i+"]", "");
      x1= util.getStateValue("LMS-x1["+i+"]", 0);
      y1= util.getStateValue("LMS-y1["+i+"]", 0);
      x2= util.getStateValue("LMS-x2["+i+"]", 0);
      y2= util.getStateValue("LMS-y2["+i+"]", 0);
      pushLandmark(x1,y1,x2,y2);            /* create a new one */
    }    
     updateScrollers();
  } /* readState */
  
    
 /**
  * writeState() - Write this landmark state to string buffer sBuf
  * @param iName of the image to read (e.g., "I1", or "I2")
  * @param sBuf is the string buffer to write to.
  */ 
  void writeState(StringBuffer sBuf)
  { /* writeState */ 
    sBuf.append("LMS-nLM\t"+nLM+"\n");
    for(int i=0;i<nLM;i++)
    {
      sBuf.append("LMS-nameLM["+i+"]\t"+nameLM[i]+"\n");
      sBuf.append("LMS-x1["+i+"]\t"+x1[i]+"\n");
      sBuf.append("LMS-y1["+i+"]\t"+y1[i]+"\n");
      sBuf.append("LMS-x2["+i+"]\t"+x2[i]+"\n");
      sBuf.append("LMS-y2["+i+"]\t"+y2[i]+"\n");
    }    
  } /* writeState */
  
    
 /**
  * getStateStr() - get this landmark state as a string
  * @param iData is the image data to use
  * @param fileName associated with the iData
  * @return state string
  */ 
  String getStateStr()
  { /* getStateStr */ 
    String sR= "Current landmarks\n";
    if(nLM>0)
      for(int i=0;i<nLM;i++)
      {
        sR += "   LM["+nameLM[i].charAt(1)+"] (x1,y1)=("+x1[i]+","+y1[i]+
              "), (x2,y2)=("+x2[i]+","+y2[i]+")\n"; 
      }
    sR += "   # landmarks = "+nLM+"\n";
    return(sR);
  } /* getStateStr */
    
    
 /**
  * addDemoLandmarks() - add 3 or 6 demo landmark pairs but only for
  * @param nPairs # of landmark pairs to add if the I1,I2 images are
  *        the demo plasmaH/L gels 3 landmarks 
  */ 
  public static boolean addDemoLandmarks(int nPairs)
  { /* addDemoLandmarks */
    String
      msg1= null,
      demoName= null,
      name1= flk.iData1.imageFile,
      name2= flk.iData2.imageFile,
      sn1= flk.util.getFileNameFromPath(name1),
      sn2= flk.util.getFileNameFromPath(name2);
    
    if(sn1.equals("plasmaH.gif") && sn2.equals("plasmaL.gif")) 
      demoName= "plasmaDemo";
    else if(sn1.equals("testA.gif") && sn2.equals("testB.gif")) 
      demoName= "testABdemo";
    
    if(demoName==null)
    { /* only works for the plasmaH/L demo gels */
      String msg= "Can only use the preset demo landmarks for demo gels";
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
          
    if(demoName.equals("testABdemo"))
    { /* Special landmarks for testA.gif and testB.gif*/
      nLM= 0;		/* force it to reset */
      pushLandmark(228,118, 211,79);     /* LM 'A' */
      pushLandmark(163,258, 163,260);    /* LM 'B' */
      pushLandmark(303,261, 265,322);    /* LM 'C' */
      msg1= "Set 3 pre-defined landmarks for testA/B Affine transform";
    }
              
    if(demoName.equals("plasmaDemo"))
    { /* add plasmaH/L landmarks */
      /* NOTE: these are for demo PlasmaH.gif and PlasmaL.gif gels only! */
      /* add 3 landmarks */
      nLM= 0;		/* force it to reset */
      pushLandmark(174,381, 160,324);     /* LM 'A' */
      pushLandmark(230,357, 207,300);     /* LM 'B' */
      pushLandmark(152,327, 134,266);     /* LM 'C' */
      msg1= "Set 3 pre-defined plasmaDemo landmarks for Affine transform";
      
      /* In addition, if it is 6 pairs, then add 3 more */
      if(nPairs==6)
      { /* for polywarp needs 6 landmarks */
        pushLandmark(219,307, 204,244);    /* LM 'D' */
        pushLandmark(178,410, 163,362);    /* LM 'E' */
        pushLandmark(147,277, 125,211);    /* LM 'F' */
        msg1= "Set 6 pre-defined plasmaDemo landmarks for Polywarp transform";
      }
    } /* add plasmaH/L landmarks */
  
    flk.info.updateInfoString();	    /* update Flicker info string */
    flk.i1IS.img_selectedFlag= false; /* Reset obj selected */
    flk.i2IS.img_selectedFlag= false;    
    
    util.showMsg(msg1, Color.black);
    util.showMsg2("for plasmaH and plasmaL demo gels.", Color.black);
    
    updateScrollers();

    return(true);
  } /* addDemoLandmarks */  
} /* end of class Landmark */

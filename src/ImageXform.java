/* File: ImageXform.java */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.*;
import java.io.*; 
import  javax.swing.SwingUtilities;

/** 
 * ImageXform class supports image transforms.
 *<P>
 * ImageXform is the class that does the actual transforms
 * These transforms include affine and polynomial spatial warping, 
 * pseudo 3D, sharpening (grad or Laplacian + grayscale),
 * gradient, Laplacian, avg8, complement, etc..
 * It runs as a background thread.
 * It uses the input image pixel buffer iPix[] from the original image.
 * It creates the output image oPix[] and the output image oImg.
 * The oPix[] buffer is G.C.ed after oImg is created.
 * When done, it calls flk.chkDoneWithTransform to let you know when it's 
 * finished.
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
   
public class ImageXform extends Thread
{ /* class ImageXform */
    
  /** main class instance */
  private Flicker
    flk;
  /** Affine xform instance */
  private Affine
    aff;
  /** Build GUI instance */
  private BuildGUI
    bGui;
  /** landmark data */
  private Landmark
    lms;	
  /** poly warp spatial transform */
  private SpatialXform
    sxf;
  /** Utility instance */
  private Util
    util;
  
  /** Image name: "left" or "right" image */
  String
    nameLR;		
  /** Image data instance to use  we get data from either iImg or oImg */
  ImageData
    iData;			  
  
  /** Corresponding builtin transform command names used in
   * the menu action commands.
   */
  public static String
    xfCmd[]= {"SharpenGrad",
              "SharpenLapl",
              "Gradient",
              "Laplacian",
              "Average",
              "Complement",
              "ContrastEnhance",
              "HistEqualize",
              "Median",
              "Max 3x3",
              "Min 3x3",
              "Threshold",
              "Pseudo3D",
              "AffineWarp",
              "PolyWarp",
              "FlipHoriz",
              "FlipVert",
              "NormColor",
              "PseudoColor",
              "Color2Gray",
              "DeZoom"
             };
  /** # of transform commands */
  public static int
    nxfCmds= xfCmd.length;   
  /** Transform number corresponding to the xfCmd[] table entry.
   * !!!! KEEP THISE NUMBERS SYNCED!!!!! 
   */
  final static int
    SHARPENGRAD= 0,
    SHARPENLAPL= 1,
    GRAD= 2,
    LAPLACIAN= 3,
    AVG8= 4,
    COMPLEMENT= 5,
    CONTR_ENHANCE= 6,
    HIST_EQUAL= 7,
    MEDIAN= 8,
    MAX8= 9,
    MIN8= 10,
    THRESHOLD= 11,
    PSEUDO3D= 12,
    AFFINEWARP= 13,
    POLYWARP= 14,
    FLIPHORIZ= 15,
    FLIPVERT= 16,
    NORMCOLOR= 17,    
    PSEUDOCOLOR= 18,
    COLOR2GRAY= 19,
    DE_ZOOM= 20;
  /** generic plugin transform number is 1 past the last builtin transform */
  public static int
    PLUGIN_FCT= nxfCmds+1;
  
  /** pixel definitions of white and black */
  final static int
    pWhite= 255;
  final static int
    pBlack= 0;
  
  /** 8-neighbor pixels */
  public int
    i0, i1, i2, i3, i4, i5, i6, i7, i8; 
  
  /** transform to use */
  public String
    useXform= "-none-";
  /** external fct number if > 0 */
  public int
    externFctNbr;	
  /** table lookup[256] grayvalue to RGB pixel*/
  public int
    gToRGB[];			
  
  /** state copy: angle factor for pseudo3D */
  public int
    angle;			  
  /** state copy: Z axis scale factor for pseudo3D */
  public int
    zScale;			  
  /** state copy: scale factor for sharpening xform */
  public int
    eScale;
  /** fast (width * sin(thetaRad) */
  public int
    dX_P3D;	
  /** state copy: lower bound threshold for grayscale slicing */
  public int
    threshold1;		
  /** state copy: upper bound threshold for grayscale slicing */
  public int
    threshold2;		
  /** state remapped copy: the zoomMagScr magnification scroller value is
   * mapped using SliderState.cvtZoomMagScr2ZoomMagVal() to this value.
   */
  public float
    zoomMagVal;	  		
  /** state: measurement circle radius */
  public int
    measCircleRadius;

  /* Local copies for speedup */
  /** histogram of iPix[] if needed */
  public int
    hist[];			
  /** equalized histogram of iPix[] if needed */
  public int
    histEq[];		
  /** mean of grayscale histogram */
  public int
    gMean;		
  /** mode of grayscale histogram */
  public int
    gMode;			  
  /** compute max grayscale of iPix */
  public int
    gMax;			 
  /** compute min grayscale of iPix */
  public int
    gMin;			 
   
  /** compute slope in g'=mCE+bCE */
  public float
    mCE;	
  /** compute intercept in g'=mCE+bCE */
  public float
    bCE;			    
   
  /** computed  y*width for speedup */
  public int
    yw;
  /** computed yw-width for speedup */
  public int
    ywMw;
  /** computed yw+width for speedup */
  public int
    ywPw;		
  /** original size of image, for zoom */
  public int
    origWidth;
  /** original size of image, for zoom */
  public int
    origHeight;
  /** new magnification size of image for IS */
  public int
    newWidth;
  /** new magnification size of image for IS */
  public int
    newHeight;
  /** iData.iWidth for speedup */
  public int
    width;		
  /* iData.iHeight for speedup  */
  public int
    height;	
  /* # of pixels in the image = height*width */
  public int
    nPixels;
  /* X result of mapXY...() */
  public int
    xPrime;		
  /* Y result of mapXY...() */
  public int
    yPrime;			
  /* local pointer copy of onput image */
  public int
    oPix[]= null;		
  /* local pointer copy of intput image */
  public int
    iPix[]= null;			
  
  /** scale factor for pseudo3D */
  public double
    scaleFactor= 1.0;
  /** colinearity threshold */
  public double
    thrColinearity; 
  /** pseudo3D angle in radians */
  public double
    thetaRad;		   		  
   
  /** Coefficient for u(x,y) poly warp */
  private double
     aU[][];
  /** Coefficient for v(x,y) poly warp */
  private double
     bV[][];			
  /** current ImageScroller right or left */
  public ImageScroller
    curIS;
  /** magnification */
  public double
    mag;

  /**
   * ImageXform() - dummy Construct ImageXform object.
   */
   public ImageXform()
   {
   }
   
   
  /**
   * ImageXform() -  Construct ImageXform object.
   * @param flk instance
   * @param nameLR is the image "left" or "right" 
   * @param iData image data input/output images
   * @param useXform is name of transform to perform
   * @param externFctNbr perform extern function if not zero
  */
  public ImageXform(Flicker flk, String nameLR, ImageData iData,
                    String useXform, int externFctNbr)
  { /* ImageXform */
    this.flk= flk;
    this.nameLR= nameLR;
    this.iData= iData;
    this.useXform= useXform; 
    this.externFctNbr= externFctNbr;
    
    this.aff= flk.aff;
    this.bGui= flk.bGui;
    this.lms= flk.lms;
    this.util= flk.util;
    
    this.angle= flk.curState.angle;
    this.zScale= flk.curState.zScale;
    this.eScale= flk.curState.eScale;
    this.thrColinearity= flk.thrColinearity;
    this.threshold1= flk.curState.threshold1;
    this.threshold2= flk.curState.threshold2;
    this.zoomMagVal= flk.curState.zoomMagVal;   
    this.measCircleRadius= flk.curState.measCircleRadius;
    
    this.thetaRad= ((double)(3.14159/180) *
                   Math.max(-flk.curState.MAX_ANGLE,
                            Math.min(flk.curState.MAX_ANGLE, angle)));
    this.dX_P3D= (int)(iData.iWidth * Math.sin(thetaRad));    
    
    this.scaleFactor= 1.0;
    
    /* [1] Setup map of gray value to RGB pixel */
    gToRGB= new int[256];	/* table lookup[256] grayvalue to RGB pixel*/
    for(int g=0; g<256; g++)
      gToRGB[g]= (0xff000000 | (g<<16) |  (g<<8) | g);
    
    width= iData.iWidth;	/* local copy for speedup */
    height= iData.iHeight;
    nPixels= height*width;
    
    mag= Math.max(this.zoomMagVal, SliderState.MIN_ZOOM_MAG_VAL);
    mag= Math.min(mag, SliderState.MAX_ZOOM_MAG_VAL);
    
    if(iData==flk.iData1)
      curIS= flk.i1IS;    
    else
      curIS= flk.i2IS;
     
    origWidth= iData.iWidth;  
    origHeight= iData.iHeight;
    newWidth= (int) (origWidth * mag);
    newHeight= (int)(origHeight * mag);
     
    /* Prevent repainting while doing a transform */
    setSyncLockFlag(true);
    
    if(!useXform.equals("DeZoom"))
    { /* Do not use iPix or oPix for zoom */      
      /* [2] Set iPix to iData.iPix  */ 
      if(iData.checkAndMakeIpix(useXform, true))
      { /* make local copy for speedup */
        iPix= iData.iPix;
      }
      else
      { /* out of memory */
        String 
          msg= "Problem making iPix from image - may be running low of memory";
        util.popupAlertMsg(msg, flk.alertColor);
        iPix= null;
        util.gcAndMemoryStats("Problem making iPix from image");
        abortTransform();
        return;	                      	/* out of memory!!! */
      } /* out of memory */

       /* [3] Create output pixel array oPix[] */
       try 
       { /* try to allocate oPix */
         oPix= null;         
         oPix= new int[width*height];
       }
       catch (Exception e3) 
       {
         oPix= null;
         String msg= "Out of memory allocating fctCalc in ImageXform";
         util.popupAlertMsg(msg, flk.alertColor);
         abortTransform();
         return;
       }
    } /* Do not use iPix or oPix for zoom */
  } /* ImageXform */
  
  
  /**
   * setSyncLockFlag() - set the repaintLockFlag to lock
   * Flicker from repainting while this the flag is true
   * @return boolean
  */
  private synchronized void setSyncLockFlag(boolean flag)
  { /* setSyncLockFlag */
    flk.repaintLockFlag= flag;         
  } /* setSyncLockFlag */
  
  
  /**
   * cleanup() - destroy data structures
   */
  public synchronized void cleanup()
  { /* cleanup */
    iData= null;
    lms= null;
    gToRGB= null;
    useXform= null;
    hist= null;
    histEq= null;
    oPix= null;
    iPix= null;
    aU= null;
    bV= null;
    flk.util.gcAndMemoryStats("Image X form cleanup()"); 
    /* Don't destroy polywarp if used in reporting for Info window */
    /* Don't destroy affine if used in reporting for Info window */
  } /* cleanup */
  
   
  /**
   * run() - Transform the pixels in iPix[] into oPix[] image data by
   * legal transform 'useXform'. Then create oImg to be displayed.
   */
  public void run()
  { /* run */
   if(flk.NEVER)
     System.out.println("run.1 xform thread started");
    /* [1] Transform iPix[] into oPix[] image data by legal transform. */
   doXform(iData);	/* do the transform */   
    
    if(flk.NEVER)
    {
     final Runnable doRun= new Runnable()
      {
        public void run()
        {
          if(flk.NEVER)
          System.out.println("doRun called from invokeAndWait");
          doXform(iData);	
        }
      };                
      SwingUtilities.invokeLater(doRun);        
    }
   if(flk.NEVER)
    {                    
      final Runnable doRun = new Runnable()
      {
        public void run()
        {
          if(flk.NEVER)
          System.out.println("doXform(iData); called from invokeAndWait");
          doXform(iData);
        }
      };
      
      Thread appThread = new Thread()
      {
        public void run()
        {
          try
          {
            SwingUtilities.invokeAndWait(doRun);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          
          //System.out.println("'doXform(iData);' Finished on " + Thread.currentThread());
        }
      };
      appThread.start();          
    } 
  
   if(flk.NEVER)
     System.out.println("run.2 xform thread done");
  } /* run */
  
  
  /**
   * abortTransform() - abort all image transforms in progress.
   * Handle the case where there is another transform in progress.
   * Clear flags and adjust counters to reflect killing this
   * transform.
   */
  public synchronized void abortTransform()
  { /* abortTransform */    
    flk.errStr= iData.errStr;                /* copy any error message */
    String
      msg1= "Aborting transform ["+useXform+"] ",
      msg2= flk.errStr;
    util.showMsg(msg1, Color.red);
    util.showMsg2(msg2, Color.red);
    util.popupAlertMsg(msg1+msg2, flk.alertColor);
    
    if(flk.ixf1!=null)
    { /* kill transform 1 */
      flk.imagesToProcess--;
      flk.finished1= true;
      flk.ixf1.iData.doingTransform= false;  /* finished */
      flk.ixf1.iData.errStr= null;	         /* reset it */   
    }
    if(flk.ixf2!=null)
    { /* kill transform 2 */
      flk.imagesToProcess--;
      flk.finished2= true;
      flk.ixf2.iData.doingTransform= false;   /* finished */
      flk.ixf2.iData.errStr= null;	          /* reset it */  
    }
    
    /* This adjusts the counter so that can finish the transform
     * processing in the flk.run() loop OUTSIDE OF THIS THREAD!
     */
    flk.testAndSetThreadCounter();
     
    /* G.C. the image transform variables */
    gcImageXform("Aborting transform");
  } /* abortTransform */
  
  
  /**
   * softDoneWithTransform() - ImageXform thread calls when xform completed.
   * ImageXform sets iData to either iData1 or iData2.
   * At that point we can create an image from the output oPix
   * data and repaint it.
   * If flk.composeXformFlag is set, then using original image which may
   * have changed.
   * @param iData is the image data
   * @param name of transform
   * @see Flicker#chkDoneWithTransform
  */
  public synchronized void softDoneWithTransform(ImageData iData, 
                                                 String name,
                                                 String useXformName)
  { /* softDoneWithTransform */
    /* [1] Save the data structure into this. object */
    /* Get total CPU time */
    Date date= new Date();
    
    iData.endTime= date.getTime();      /* milliseconds since 1970 */
    iData.runTimeMsec= (int)((iData.endTime - iData.startTime)/1.0F);
    
    flk.errStr= iData.errStr;          /* copy any error message */
    iData.errStr= null;	               /* reset it */   
    
    /* [2] All done, swap the image to be displayed. */
    if(flk.errStr!=null && ! flk.errStr.equals(""))
    {
      util.popupAlertMsg(flk.errStr, flk.alertColor);
    }
    else
    { /* Setup images to flicker if turn on flickering. */      
      if("left".equals(name))
      { /* If BC has been done */
        if(iData.bcImg!=null) 
        { /* process BC */ 
          if(flk.NEVER)
          {
            final ImageData id= iData;
            final Runnable processBCimage = new Runnable()
            {
              public void run()
              {
                //if(flk.NEVER)
                System.out.println("processBCimage called from invokeAndWait");
                flk.i1IS.processBCimage(id);
              }
            };
            
            Thread appThread = new Thread()
            {
              public void run()
              {
                try
                {
                  SwingUtilities.invokeAndWait(processBCimage);
                }
                catch (Exception e)
                {
                  e.printStackTrace();
                }
                //if(flk.NEVER)
                System.out.println("'processBCimage' Finished on " + Thread.currentThread());
              }
            };
            appThread.start();         
         }
         else
         {
           flk.i1IS.processBCimage(iData);           
         }
        } /* process BC */   
        
        flk.i1IS.paintSiCanvas();
        flk.finished1= true;
      }
      else if("right".equals(name))
      { /* If BC has been done */
        if(iData.bcImg!=null) 
        { /* process BC */   
          //Old non thread safe way : flk.i2IS.processBCimage(iData);           
          if(flk.NEVER)
          {
            final ImageData  id= iData;
            final Runnable processBCimage = new Runnable()
            {
              public void run()
              {
                if(flk.NEVER)
                System.out.println("processBCimage called from invokeAndWait");
                flk.i2IS.processBCimage(id);
              }
            };
            
            Thread appThread = new Thread()
            {
              public void run()
              {
                try
                {
                  SwingUtilities.invokeAndWait(processBCimage);
                }
                catch (Exception e)
                {
                  e.printStackTrace();
                }
                if(flk.NEVER)
                  System.out.println("'processBCimage' Finished on " + Thread.currentThread());
              }
            };
            appThread.start();     
          }
          else
            flk.i2IS.processBCimage(iData);         
        } /* process BC */       
        
        flk.i2IS.paintSiCanvas();
        flk.finished2= true;
      }      
      if(flk.imagesToProcess > 1)
        util.showMsg("Finished transform[" + useXformName + "] "+
                     Math.abs(iData.runTimeMsec) +  " Msec, " +
                     (flk.imagesToProcess-1) +
                     " more transform in progress...",
                     Color.black);
    } /* Setup images to flicker if turn on flickering. */    
    
    iData.doingTransform= false;    /* finished */  
    flk.testAndSetThreadCounter();
  } /* softDoneWithTransform */
  
    
  /**
   * imax(a,b) - compute fast MAX of two ints
   * @param a is arg
   * @param b is arg
   * @return MAX(a,b)
  */
  final synchronized private int imax(int a, int b)
  { return((a>=b) ? a : b); }
  
    
  /**
   * imin(a,b) - compute fast MIN of two ints
   * @param a is arg
   * @param b is arg
   * @return MIN(a,b)
  */
  final synchronized private int imin(int a, int b)
  {  return((a<=b) ? a : b); }
    
  
  /**
   * iabs(a) - compute fastABS of two ints
   * @param a is arg
   * @param b is arg
   * @return ABS(a)
  */
  final synchronized private int iabs(int a)
  { return((a>=0) ? a : -a); }
  
    
  /**
   * remapLMS() - remap LMS if transform changed their positions.
   * Note: if ever Restore I1 & I2 images, then wipe out landmarks so
   * don't need to remap in that case.
   *<PRE>
   * [NOTE] there are two sets of landmarks in the lms object:
   *  lms.{xy}{12}[lm#] for the iImg
   * and
   *  lms.o{xy}{12}[lm#] for the oImg.
   * The ImageScroller displays the landmarks from oImg if there is 
   * valid oImg object and otherwise uses the landmarks for iImg.
   *</PRE>
   * @param imgNameLR is "left", "right" or "both" to remap
   * @param iOpr affine operator: AFFINEWARP, POLYWARP or PSEUDO3D
   * @return true if succeed
  */
  private synchronized boolean remapLMS(String imgNameLR, int iOpr)
  { /* remapLMS */
    if(iOpr!=AFFINEWARP && iOpr!=POLYWARP && iOpr!=PSEUDO3D)
      return(false);		/* don't bother */
    
    boolean bothFlag= ("both".equals(nameLR));
    int
      h= height,
      w= width,
      x1,y1,
      x2,y2,
      x1Prime= -1,      /* mapped landmarks computed here */
      y1Prime= -1,  
      x2Prime= -1, 
      y2Prime= -1;
    
    for(int lm= 0;lm<lms.nLM; lm++)
    { /* process landmark lm */  
        x1= lms.x1[lm];             /* I1 */
        y1= lms.y1[lm];
        x2= lms.x2[lm];             /* I2 */
        y2= lms.y2[lm];  
        /* set defaults to the opposite landmarks */
        lms.ox1[lm]= x1;            /* I1 oImg is iImg */
        lms.oy1[lm]= y1;
        lms.ox2[lm]= x2;            /* I2 oImg is iImg */
        lms.oy2[lm]= y2;
        
      if("left".equals(nameLR) || bothFlag)
      { /* Map I1 */
        switch(iOpr)
        {
          case AFFINEWARP: 
            x1Prime= x2;                 /* mapped to other landmark */
            y1Prime= y2;
            break;
            
          case POLYWARP:
            mapXYtoPolyPoint(x1,y1);     /* map(x1,y1) to (xPrime,yPrime) */
            x1Prime= xPrime;
            y1Prime= yPrime;
            break;
            
          case PSEUDO3D:
            mapXYtoPseudo3DPoint(x1,y1); /* map(x1,y1) to (xPrime,yPrime) */
            x1Prime= xPrime;
            y1Prime= yPrime;
            break;
        } /* Map I1 */
        
        /* store it in OUTPUT image landmark set for repainting */
        lms.ox1[lm]= x1Prime;            /* I1 oImg is mapped */
        lms.oy1[lm]= y1Prime;
      } /* Map I1 */
      
      if("right".equals(nameLR) || bothFlag)
      { /* Map I2 */
        switch(iOpr)
        {
          case AFFINEWARP: 
            x2Prime= x1;                  /* mapped to other landmark */
            y2Prime= y1;
            break;
            
          case POLYWARP:
            mapXYtoPolyPoint(x2,y2);      /* map(x1,y1) to (xPrime,yPrime)*/
            x2Prime= xPrime;
            y2Prime= yPrime;
            break;
            
          case PSEUDO3D:
            mapXYtoPseudo3DPoint(x2,y2); /* map(x1,y1) to xPrime,yPrime*/
            x2Prime= xPrime;
            y2Prime= yPrime;
            break;
        }
        
        /* store it in OUTPUT image landmark set for repainting */
        lms.ox2[lm]= x2Prime;            /* I2 oImg is mapped */
        lms.oy2[lm]= y2Prime;
      } /* Map I2 */
    } /* process landmark lm */
    
   return(true);
  } /* remapLMS */
      
    
  /**
   * remapLMSafterAffine() - remap LMS since Affine transform changed 
   * their positions.
   * [DEPRICATE] after get remapLMS() working 100%...
   * @param iOpr affine operator: AFFINEWARP, POLYWARP or PSEUDO3D
   * @return true if succeed
  */
  private synchronized boolean remapLMSafterAffine()
  { /* remapLMSafterAffine */
    int
      h= height,
      w= width,
      x1,y1,
      x2,y2,
      x1Prime, y1Prime,
      x2Prime, y2Prime;
    
    if(flk.dbugFlag)
      aff.showAffineLM("IXF-RLMSAA Before remap");
    
    for(int lm= 0; lm<lms.nLM; lm++)
    { /* process landmark lm */
      /* fetch it */      
      if("left".equals(nameLR))        
      { /* Map I1 */       
        /* leave left LM alone, remap right since it was
         * transformed */
        lms.x2[lm]= lms.x1[lm];
        lms.y2[lm]= lms.y1[lm];
      } /* Map I1 */
      
      else if("right".equals(nameLR))
      { /* Map I2 */         
        /* leave right LM alone, remap left since it was
         * transformed */
        lms.x1[lm]= lms.x2[lm];
        lms.y1[lm]= lms.y2[lm];
      } /* Map I2 */
      
    } /* process landmark lm */
    if(flk.dbugFlag)
      aff.showAffineLM("IXF-RLMSAA after remap");
    
   return(true);
  } /* remapLMSafterAffine */
    
  
  /**
   * getFastNgh() - get 3x3 8-bit neighborhood at (x,y)
   *<PRE>
   *   i3 i2 i1
   *   i4 i8 i0
   *   i5 i6 i7
   *</PRE>
   * Note: if the neighborhood is on the edge, then return 0 ngh.
   * [NOTE] It is assumed that the image is 8-bit gray scale, so r==b==g.
   * [TODO] If not, we should map it to gray somehow.
   * [TODO] Need to ***DEBUG*** - does not work.
   * @param x1 center of neighborhood
   * @param y1 center of neighborhood
  */
  final synchronized public void getFastNgh(int x1, int y1)
  { /* getFastNgh */
    int
      widthM1=(width-1),
      heightM1= (height-1),
      xP1= x1+1;    
    
    /* [1] Check if x is at left edge */
    if(x1==0)
    { /* get fresh neighborhood in legal range */
      i3= 0;
      i4= 0;
      i5= 0;
      
      if(y1==0)
      { /* off pf image */
        i1= 0;
        i2= 0;
        i6= (iPix[ywPw] & 0Xff);
        i7= (iPix[ywPw+1] & 0Xff);
      }
      else if(y1==height)
      { /* off  image */
        i1= (iPix[ywMw-1] & 0Xff);
        i2= (iPix[ywMw] & 0Xff);
        i6= 0;
        i7= 0;
      }
      
      i8= (iPix[yw] & 0Xff); /* always is there */
      i0= (iPix[yw+1] & 0Xff);
    } /* get fresh neighborhood in legal range */    
    
    /* [2] get just pixels need for shifted ngh */
    else
    { /* shift ngh along the row */
      i3= i2;		/* 1st row, shift pixel right to left */
      i2= i1;
      
      i4= i8;		/* 2nd row */
      i8= i0;
      
      i5= i6;		/* 3rd row */
      i6= i7;
      
      if(x1==width)
      {
        i1= 0;
        i0= 0;
        i7= 0;
      }
      else if(y1==0)
      { /* off of image */
        i1= 0;
        i0= (iPix[yw+1] & 0Xff); /* always is there */
        i7= (iPix[ywPw+1] & 0Xff);
      }
      else if(y1==height)
      { /* off of image */
        i1= (iPix[ywMw-1] & 0Xff);
        i0= (iPix[yw+1] & 0Xff); /* always is there */
        i7= 0;
      }
    } /* shift ngh along the row */
    
    return;
  } /* getFastNgh */
  
    
  /**
   * getNgh3x3() - get 3x3 least significant 8-bit neighborhood data at (x,y)
   *<PRE>
   *   i3 i2 i1
   *   i4 i8 i0
   *   i5 i6 i7
   * [NOTES]
   * 1. If the neighborhood is on the edge, then return 0 ngh.
   * 2. It is assumed that the image is gray scale, so r==b==g.
   * 3. If not, we could map it to gray using NTSC RGB to gray transform.
   * 4. Assume that state vars (yw, ywMw, ywPw) are precomputed BEFORE
   *    calling this method.
   *</PRE>
   * @param x center of neighborhood
   * @param y center of neighborhood
  */
  final synchronized public void getNgh3x3(int x, int y)
  { /* getNgh3x3 */
    yw=  y*width;		/* Prep it here for speedup */
    ywMw= yw-width;
    ywPw= yw+width;
    getNgh(x, y);
  } /* getNgh3x3 */
  
    
  /**
   * getNgh() - get 3x3 least significant 8-bit neighborhood data at (x,y)
   *<PRE>
   *   i3 i2 i1
   *   i4 i8 i0
   *   i5 i6 i7
   * [NOTES]
   * 1. If the neighborhood is on the edge, then return 0 ngh.
   * 2. It is assumed that the image is gray scale, so r==b==g.
   * 3. If not, we could map it to gray using NTSC RGB to gray transform.
   * 4. Assume that state vars (yw, ywMw, ywPw) are precomputed BEFORE
   *    calling this method.
   *</PRE>
   * @param x1 center of neighborhood
   * @param y1 center of neighborhood
  */
  final synchronized public void getNgh(int x1, int y1)
  { /* getNgh */
    int
      xP1= x1+1,
      xM1= x1-1;
    
    if(x1>1 && x1<(width-1) && y1>1 && y1<(height-1))
    { /* in range */
      i1= (iPix[ywMw+xP1] & 0Xff);
      i2= (iPix[ywMw+x1] & 0Xff);
      i3= (iPix[ywMw+xM1] & 0Xff);
      
      i4= (iPix[yw+xM1] & 0Xff);
      i8= (iPix[yw+x1] & 0Xff);
      i0= (iPix[yw+xP1] & 0Xff);
      
      i5= (iPix[ywPw+xM1] & 0Xff);
      i6= (iPix[ywPw+x1] & 0Xff);
      i7= (iPix[ywPw+xP1] & 0Xff);
    }
    else
    { /* out of range */
      i1= 0;
      i2= 0;
      i3= 0;
      
      i4= 0;
      i8= 0;
      i0= 0;
      
      i5= 0;
      i6= 0;
      i7= 0;
    }
    
    return;
  } /* getNgh */
  
    
  /**
   * getNghInt3x3() - get 3x3 32-bit neighborhood at (x,y)
   *<PRE>
   *   i3 i2 i1
   *   i4 i8 i0
   *   i5 i6 i7
   * [NOTES]
   * 1. If the neighborhood is on the edge, then return 0 ngh.
   * 2. Assume that state vars (yw, ywMw, ywPw) are precomputed BEFORE
   *    calling this method.
   *</PRE>
   * @param x center of neighborhood
   * @param y center of neighborhood
  */
  final synchronized public void getNghInt3x3(int x, int y)
  { /* getNghInt3x3 */
    yw=  y*width;		/* Prep it here for speedup */
    ywMw= yw-width;
    ywPw= yw+width;
    getNghInt(x, y);
  } /* getNghInt3x3 */
  
  
  /**
   * getNghInt() - get 3x3 32-bit neighborhood at (x,y)
   *<PRE>
   *   i3 i2 i1
   *   i4 i8 i0
   *   i5 i6 i7
   * [NOTES]
   * 1. If the neighborhood is on the edge, then return 0 ngh.
   * 2. Assume that state vars (yw, ywMw, ywPw) are precomputed BEFORE
   *    calling this method.
   *</PRE>
   * @param x1 center of neighborhood
   * @param y1 center of neighborhood
  */
  final synchronized public void getNghInt(int x1, int y1)
  { /* getNghInt */
    int
      xP1= x1+1,
      xM1= x1-1;
    
    if(x1>1 && x1<(width-1) && y1>1 && y1<(height-1))
    { /* in range */
      i1= iPix[ywMw+xP1];
      i2= iPix[ywMw+x1];
      i3= iPix[ywMw+xM1];
      
      i4= iPix[yw+xM1];
      i8= iPix[yw+x1];
      i0= iPix[yw+xP1];
      
      i5= iPix[ywPw+xM1];
      i6= iPix[ywPw+x1];
      i7= iPix[ywPw+xP1];
    }
    else
    { /* out of range */
      i1= 0;
      i2= 0;
      i3= 0;
      
      i4= 0;
      i8= 0;
      i0= 0;
      
      i5= 0;
      i6= 0;
      i7= 0;
    }
    
    return;
  } /* getNghInt */
    
  
  /**
   * sharpenGrad() - Compute add 4-neighbor gradient to grayscale of 
   * pixel data
   *<PRE>
   *  3 2 1       -1  0 +1      +1 +2 +1        0 +1 +2        +2 +1  0
   *  4 8 0   dO= -2  0 +2, d90= 0  0  0, d45= -1  0 +1, d135= +1  0 -1
   *  5 6 7       -1  0 +1      -1 -2 -1       -2 -1  0         0 -1 -2
   *
   *    grad= max(d0, d45, d90, d135)
   *    sharpenGrad= (i8 + (eScale*grad)/100).
   *    sharpenGrad= ((100-eScale)*i8 + eScale*grad)/100.
   * Compute pixels in range of [0:255].
   *</PRE>
  */
  final synchronized private void sharpenGrad()
  { /* sharpenGrad */
    int
      x, y, p,
      d0, d45, d90, d135,
      m1, m2, m3, grad,
      gO;		       /* output pixel */
    
    for (y= 0, p= 0; y < height; y++)
    { /* process rows */
      yw=  y*width;		/* Do it here for speedup */
      ywMw= yw-width;
      ywPw= yw+width;
      for (x= 0; x < width; x++, p++)
      { /* process cols */       
        getNgh(x,y);
        d0= (i3+i2+i2+i1 -i5-i6-i6-i7);
        if(d0<0)
          d0= -d0;
        
        d45= (i1+i1+i0 -i4-i5-i5-i6);
        if(d45<0)
          d45=-d45;
        
        d90= (i0+i0+i7 -i3-i4-i4-i5);
        if(d90<0)
          d90= -d90;
        
        d135= (i4+i3+i3+i2 -i0-i7-i7-i6);
        if(d135<0)
          d135= -d135;
        
        m1= (d90>d135) ? d90 : d135;
        m2= (d45>m1) ? d45 : m1;
        m3= (d0>m2) ? d0 : m2;
        grad= m3;
        
        /* sharpenGrad= (i8 + (eScale*grad)/100); */
        gO= ((100-eScale)*i8 + eScale*grad)/100;
        
        /* Clip it */
        if(gO<0)
          gO= 0;
        else if(gO>255)
          gO= 255;
        oPix[p]= gToRGB[gO]; /* save output pixel */
      } /* process cols */
    } /* process rows */
  } /* sharpenGrad */
  
  
  /**
   * sharpenLaplacian() - Compute add 8-neighbor Laplacian to grayscale of
   * pixel data.
   *<PRE>
   *   3 2 1     -1 -1 -1
   *   4 8 0     -1 +8 -1
   *   5 6 7     -1 -1 -1
   *
   *    laplacian= (255 - abs((8*i8 - (i0+i1+i2+i3+i4+i5+i6+i7)), 0))
   *    sharpenLaplacian= (i8 + (eScale*laplacian)/100)
   *    sharpenLaplacian= ((100-eScale)*i8 + eScale*laplacian)/100
   * Compute pixels in range of [0:255].
   *</PRE>
  */
  final synchronized private void sharpenLaplacian()
  { /* sharpenLaplacian */
    int
      x, y, p,
      diff,
      laplacian,
      gO;		       /* output pixel */        
        
    for (y= 0, p= 0; y < height; y++)
    { /* process row */
      yw=  y*width;	/* Do it here for speedup */
      ywMw= yw-width;
      ywPw= yw+width;
      for (x= 0; x < width; x++, p++)
      { /* process column */
        getNgh(x,y);    /* get 3x3 least signif. 8-bit NGH data at (x,y) */
        diff= (8*i8 - (i0+i1+i2+i3+i4+i5+i6+i7));
        if(diff<0)
          diff= -diff;
        laplacian= (255 - diff);
        
        /* sharpenLaplacian= (i8 + (eScale*laplacian)/100); */
        gO= ((100-eScale)*i8 + eScale*laplacian)/100;
        
        /* Clip it */
        if(gO<0)
          gO= 0;
        else if(gO>255)
          gO= 255;
        oPix[p]= gToRGB[gO]; /* save output pixel */
      } /* process column */
    } /* process row */
  } /* sharpenLaplacian */
  
  
  /**
   * grad() - compute 4-neighbor gradient
   *<PRE>
   *  3 2 1       -1  0 +1      +1 +2 +1        0 +1 +2        +2 +1  0
   *  4 8 0   dO= -2  0 +2, d90= 0  0  0, d45= -1  0 +1, d135= +1  0 -1
   *  5 6 7       -1  0 +1      -1 -2 -1       -2 -1  0         0 -1 -2
   *
   *     grad= max(d0, d45, d90, d135)
   * Compute pixels in range of [0:255].
   *</PRE>
  */
  final synchronized private void grad()
  { /* grad */
    int
      x, y, p,
      d0, d45, d90, d135,
      m1, m2, m3, grad,
      gO;		       /* output pixel */
    
    for (y= 0, p= 0; y < height; y++)
    { /* process row */
      yw=  y*width;	/* Do it here for speedup */
      ywMw= yw-width;
      ywPw= yw+width;
      for (x= 0; x < width; x++, p++)
      { /* process column */  
        getNgh(x,y);    /* get 3x3 least signif. 8-bit NGH data at (x,y) */
        d0= (i3+i2+i2+i1 -i5-i6-i6-i7);
        if(d0<0)
          d0= -d0;
        
        d45= (i1+i1+i0 -i4-i5-i5-i6);
        if(d45<0)
          d45=-d45;
        
        d90= (i0+i0+i7 -i3-i4-i4-i5);
        if(d90<0)
          d90= -d90;
        
        d135= (i4+i3+i3+i2 -i0-i7-i7-i6);
        if(d135<0)
          d135= -d135;
        
        m1= (d90>d135) ? d90 : d135;
        m2= (d45>m1) ? d45 : m1;
        m3= (d0>m2) ? d0 : m2;
        grad= m3;
        gO= grad;
        
        /* Clip it */
        if(gO<0)
          gO= 0;
        else if(gO>255)
          gO= 255;
        oPix[p]= gToRGB[gO]; /* save output pixel */
      } /* process column */
    } /* process row */
  } /* grad */
  
  
  /**
   * laplacian() - compute 8-neighbor laplacian
   *<PRE>
   *   3 2 1     -1 -1 -1
   *   4 8 0     -1 +8 -1
   *   5 6 7     -1 -1 -1
   *
   *    laplacian= (255 - abs((8*i8 - (i0+i1+i2+i3+i4+i5+i6+i7)), 0))
   * Compute pixels in range of [0:255].
   *</PRE>
  */
  final synchronized private void laplacian()
  { /* laplacian */
    int
      x, y, p,
      diff,
      laplacian,
      gO;		       /* output pixel */
        
    for (y= 0, p= 0; y < height; y++)
    { /* process row */
      yw=   y*width;	/* Do it here for speedup */
      ywMw= yw-width;
      ywPw= yw+width;
      for (x= 0; x < width; x++, p++)
      { /* process column */
        getNgh(x,y);    /* get 3x3 least signif. 8-bit NGH data at (x,y) */
        diff= (8*i8 - (i0+i1+i2+i3+i4+i5+i6+i7));
        if(diff<0)
          diff= -diff;
        laplacian= (255 - diff);
        gO= laplacian;
        
        /* Clip it */
        if(gO<0)
          gO= 0;
        else if(gO>255)
          gO= 255;
        oPix[p]= gToRGB[gO]; /* save output pixel */
      } /* process column */
    } /* process row */
  } /* laplacian */
  
  
  /**
   * avg8() - compute 8-neighbor average
   *<PRE>
   *   3 2 1
   *   4 8 0
   *   5 6 7
   *
   *     avg8= (i0+i1+i2+i3+i4+i5+i6+i7+i8)/9);
   * compute pixels in range of [0:255].
   *</PRE>
  */
  final synchronized private void avg8()
  { /* avg8 */
    int
      x, y, p,
      avg8,
      gO;		              /* output pixel */        
    
    for (y= 0, p= 0; y < height; y++)
    { /* process row */
      yw=   y*width;	    /* Do it here for speedup */
      ywMw= yw-width;
      ywPw= yw+width;
      for (x= 0; x < width; x++, p++)
      { /* process column */
        getNgh(x,y);
        avg8= (i0+i1+i2+i3+i4+i5+i6+i7+i8)/9;
        gO= avg8;
        
        /* Clip it */
        if(gO<0)
          gO= 0;
        else if(gO>255)
          gO= 255;
        oPix[p]= gToRGB[gO]; /* save output pixel */
      } /* process column */
    } /* process row */
  } /* avg8 */
  
  
  /**
   * findMedian() - compute median of 9 values.
   * Finds the 5th largest of 9 values.
   * @param values to compute median [0:8]
   * @return median value.
  */
  final synchronized private int findMedian(int values[])
  { /* findMedian */
    int
      j,
      max1,
      mj;
    
    /* Finds the 5th largest of 9 values */
    for (int i= 1; i <= 4; i++)
    { /* find the median pixel value */
      max1= 0;
      mj= 1;
      for (j= 0; j<=8; j++)
        if (values[j] > max1)
        {
          max1= values[j];
          mj= j;
        }
      values[mj]= 0;
    } /* find the median pixel value */
    
    int
      k,
      max2= 0;
    for (k= 0; k <= 8; k++)
      if (values[k] > max2)
        max2= values[k];
    
    return(max2);
  } /* findMedian */
  
  
  /**
   * median8() - compute median of 8-neighbors
   * compute pixels in range of [0:255].
  */
  final synchronized private void median8()
  { /* median8 */
    int
      values[]= new int[10],
      x, y, p,
      gO;		                 /* output pixel */
    
    for (y= 0, p= 0; y < height; y++)
    { /* process row */
      yw=   y*width;	       /* Do it here for speedup */
      ywMw= yw-width;
      ywPw= yw+width;
      for (x= 0; x < width; x++, p++)
      { /* process column */
        getNgh(x,y);    /* get 3x3 least signif. 8-bit NGH data at (x,y) */
        values[0]= i1;
        values[1]= i2;
        values[2]= i3;
        values[3]= i4;
        values[4]= i5;
        values[5]= i6;
        values[6]= i7;
        values[7]= i8;
        values[8]= i0;
        gO= findMedian(values);
        
        /* Clip it */
        if(gO<0)
          gO= 0;
        else if(gO>255)
          gO= 255;
        oPix[p]= gToRGB[gO]; /* save output pixel */
      } /* process column */
    } /* process row */
  } /* median8 */
  
  
  /**
   * max8() - compute max of 8-neighbor pixels for 8-bit data
   *<PRE>
   *   3 2 1
   *   4 8 0
   *   5 6 7
   *
   *     max8= max(i0,i1,i2,i3,i4,i5,i6,i7,i8));
   * compute pixels in range of [0:255].
   *</PRE>
  */
  final synchronized private void max8()
  { /* max8 */
    int
      x, y, p,
      gO;		              /* output pixel */        
    
    for (y= 0, p= 0; y < height; y++)
    { /* process row */
      yw=   y*width;	    /* Do it here for speedup */
      ywMw= yw-width;
      ywPw= yw+width;
      for (x= 0; x < width; x++, p++)
      { /* process column */
        getNgh(x,y);
        gO= i0;
        if(gO<i1)
          gO= i1;
        else if(gO<i2)
          gO= i2;
        else if(gO<i3)
          gO= i3;
        else if(gO<i4)
          gO= i4;
        else if(gO<i5)
          gO= i5;
        else if(gO<i6)
          gO= i6;
        else if(gO<i7)
          gO= i7;
        else if(gO<i8)
          gO= i8;
        
        oPix[p]= gToRGB[gO]; /* save output pixel */
      } /* process column */
    } /* process row */
  } /* max8 */
  
  
  /**
   * min8() - compute min of 8-neighbor pixels for 8-bit data
   *<PRE>
   *   3 2 1
   *   4 8 0
   *   5 6 7
   *
   *     min8= min(i0,i1,i2,i3,i4,i5,i6,i7,i8));
   * compute pixels in range of [0:255].
   *</PRE>
  */
  final synchronized private void min8()
  { /* max8 */
    int
      x, y, p,
      gO;		              /* output pixel */        
    
    for (y= 0, p= 0; y < height; y++)
    { /* process row */
      yw=   y*width;	    /* Do it here for speedup */
      ywMw= yw-width;
      ywPw= yw+width;
      for (x= 0; x < width; x++, p++)
      { /* process column */
        getNgh(x,y);
        gO= i0;
        if(gO>i1)
          gO= i1;
        else if(gO>i2)
          gO= i2;
        else if(gO>i3)
          gO= i3;
        else if(gO>i4)
          gO= i4;
        else if(gO>i5)
          gO= i5;
        else if(gO>i6)
          gO= i6;
        else if(gO>i7)
          gO= i7;
        else if(gO>i8)
          gO= i8;
        
        /* Clip it */
        if(gO<0)
          gO= 0;
        else if(gO>255)
          gO= 255;
        oPix[p]= gToRGB[gO]; /* save output pixel */
      } /* process column */
    } /* process row */
  } /* min8 */
    
  
  /**
   * complement() - compute grayscale complement of gI
   *<PRE>
   *     gO= (255 - gI)
   * compute complemented 8-bit pixel
   *</PRE>
  */
  final synchronized private void complement()
  { /* complement */
    int
      gI,
      gO;
    for (int p= nPixels-1; p>=0; p--)
    {
      gI= (iPix[p] & 0Xff);   /* input pixel as 8-bit */
      gO= (255 - gI);     /* complement */
      oPix[p]= gToRGB[gO];    /* save output pixel as RGB */
    }
  } /* complement */
   
  
  /**
   * threshold() - compute grayscale threshold of gI
   *     gO= (gI<=t2 && gI>=t1) ? gI : pWhite;
   * compute thresholded 8-bit pixel
  */
  final synchronized private void threshold()
  { /* threshold */
    int
      gI,
      gC,
      gO;     
    
     /* Use threshold insize filter [t1:t2] else outside of [t1:t2] */
    boolean useThresholdInsideFlag= flk.useThresholdInsideFlag;
    
    for (int p= nPixels-1; p>=0; p--)
    {
      gI= (iPix[p] & 0Xff);   /* input pixel as 8-bit */
      gC= (255-gI);           /* set white is zero */
      if(useThresholdInsideFlag)
        gO= (gC>=threshold1 && gC<=threshold2) ? gI : pWhite;
      else
        gO= (gC<=threshold1 || gC>=threshold2) ? gI : pWhite;
      oPix[p]= gToRGB[gO];    /* save output pixel as RGB */
    }
  } /* threshold */
    
  
  /**
   * contrastEnhance() - compute grayscale contrast pixel enhancement
   * compute grayscale contrast enhanced image.
  */
  final synchronized private void contrastEnhance()
  { /* contrastEnhance */
    int 
      gI,
      gO;
    
    computeGrayHistStatistics(); /* which computes mCE and bCE */
    
    for (int p= nPixels-1; p>=0; p--)
    {
      gI= (iPix[p] & 0Xff);       /* input pixel as 8-bit */
      gO= (int)(mCE*gI + bCE);
      /* Clip it */
      if(gO<0)
        gO= 0;
      else if(gO>255)
        gO= 255;
      oPix[p]= gToRGB[gO];        /* save output pixel */
    }
  } /* contrastEnhance */
    
   
  /**
   * histogramEqualization() - compute grayscale histogram equalization xform
   * for 8-bit pixels
  */
  final synchronized private void histogramEqualization()
  { /* histogramEqualization*/
    int gI;
    
    computeGrayHistStatistics(); /* make histogram equilization histEq[] */    
    for (int p= nPixels-1; p>=0; p--)
    {
      gI= (iPix[p] & 0Xff);      /* input pixel as 8-bit */      
      oPix[p]= histEq[gI];
    }
  } /* histogramEqualization*/
    
  
  /**
   * normalColor() - restore output image to original input image
   * 
   */
  final synchronized private void normalColor()
  { /* normalColor */
    int
      origIpix[]= iData.iPix,       /* force it to use the original pix &*/
      gI;
    
    for (int p= (nPixels)-1; p>=0; p--)
    {
      gI= origIpix[p]; 
      oPix[p]= gI; 
    }
  } /* normalColor */
    
  
  /**
   * pseudoColor() - compute pseudocolor from grayscale
   * Map 255 to blue and green at 1/2 way, and red 0
   * for 32-bit pixels
  */
  final synchronized private void pseudoColor()
  { /* pseudoColor */
    int
      gI,
      b,
      g,
      r,
      gO;
    
    for (int p= (nPixels)-1; p>=0; p--)
    {
      gI= (iPix[p] & 0Xff); /* input pixel as 8-bit */
      b= gI;
      g= imax(0,(gI-128));
      r= (255-gI);
      gO= (0Xff000000 | (r<<16) | (g<<8) | b);
      oPix[p]= gO; 
    }
  } /* pseudoColor */
  
  
  /**
   * rgb2gray() - convert color image to NTSC gray value image
   * using NTSC transform gray= red*0.33 + green*0.50 + blue*0.17
   * NOTE: don't apply this transform more than once or it will
   * have bogus results. Could overide iPix that could be oPix
   * so it uses the iData.iPix data.
   */
  final synchronized private void rgb2gray()
  { /* rgb2gray */
    int
      origIpix[]= iData.iPix,       /* force it to use the original pix &*/
      gI,
      r, g, b,
      rG, gG, bG,
      gray,
      gO;
    
    for (int p= (nPixels)-1; p>=0; p--)
    {
      gI= origIpix[p];
      r= (gI>>16) & 0Xff;
      g= (gI>>8) & 0Xff;
      b= gI & 0Xff;
      
      rG= (int)(0.33F*r);
      gG= (int)(0.50F*r);
      bG= (int)(0.17F*r);
      
      gray= (rG+gG+bG);
      if(gray>255)
        gray= 255;
      
      gO= (0Xff000000 | (gray<<16) | (gray<<8) | gray);
      oPix[p]= gO; 
    }
  } /* rgb2gray */
  
  
  /**
   * createZoomedImage() - create zoomed image using getScaledInstance. 
   * The image used is the one used for zoomed input using the
   * getImageForZoomInput() that could be either (iImg, oImg).
   *
   * @return zoomed image
   */  
  public synchronized Image createZoomedImage()
  { /* createZoomedImage */    
    /* get image to be dezoomed/zoomed */  
    Image inputImage= iData.getImageForZoomInput();   
    
    /* Zoom image based on newWidth & newHeight */    
    final Image newImage= inputImage.getScaledInstance(newWidth, newHeight,
                                                       Image.SCALE_FAST);
    
    /* Rebuild ImageScroller since we need different sized canvas. */      
    /* Note: Swing needs the below code to be thread safe, needs to be 
     * resovled, does not work when ImageScroller is a JPanel. */
    // Old non thread safe way : curIS.resizeImageCanvas(newImage, newWidth, newHeight, mag);
    if(flk.NEVER)
    {
      final int w= newWidth;
      final int h= newHeight;
      final double m= mag;
      final ImageScroller is= curIS;
      final Runnable callResizeImageCanvas = new Runnable()
      {
        public void run()
        {
          if(flk.NEVER)
            System.out.println("resizeImageCanvas called from invokeAndWait");
          is.resizeImageCanvas(newImage, w, h, m);
        }
      };
      
      Thread appThread = new Thread()
      {
        public void run()
        {
          try 
          {
            SwingUtilities.invokeAndWait(callResizeImageCanvas);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          if(flk.NEVER)
            System.out.println("'callResizeImageCanvas' Finished on " + Thread.currentThread());
        }
      };
      appThread.start();
    }
    else
      if(flk.useGuardRegionImageFlag)
      {
        /* guard region already exists so change its size */
        curIS.setGuardRegion(newWidth,newHeight,mag);
      }
      else
        curIS.resizeImageCanvas(newImage, newWidth, newHeight, mag);    
  
    curIS.repackFlag= true;
    
    if(flk.NEVER)
    System.out.println("createZoomedImage1(): newWidth="+newWidth+
                        " curIS w= "+curIS.isWidth+
                        " origWidth="+origWidth+
                        " inputImage.w="+inputImage.getWidth(flk)+
                        " newImage.w="+newImage.getWidth(flk));   
     
    util.gcAndMemoryStats("createZoomedImage():Clean up");
    return(newImage);
  } /* createZoomedImage */   
          
        
  /**
   * createZoomedImage() - convert pixel array argument into zoomed image 
   * using getScaledInstance.
   * @param pixels 
   * @return zoomed image 
         */
  public synchronized Image createZoomedImage(int pixels[])
  { /* createZoomedImage */ 
          
    Toolkit dtk= Toolkit.getDefaultToolkit();
    MemoryImageSource mis= new MemoryImageSource(origWidth, origHeight, oPix,
                                                 0, origWidth); 
    /* Create and wait for image to be created */
    Image oImgToZoom= dtk.createImage(mis);
    MediaTracker tracker= new MediaTracker(curIS);
    tracker.addImage(oImgToZoom,0);
    try
    {
      tracker.waitForID(0);
    }
    catch(InterruptedException e){}
        
    /* Zoom image based on newWidth & newHeight. */
    Image newImage= oImgToZoom.getScaledInstance(newWidth, newHeight,
                                          Image.SCALE_FAST);
    MediaTracker tracker2= new MediaTracker(curIS);
    tracker2.addImage(newImage,0);
    try
    {
      tracker2.waitForID(0);
    }
    catch(InterruptedException e){}   
    
    if(flk.NEVER)
    System.out.println("createZoomedImage2b(): newWidth="+newWidth+
                        " curIS w= "+curIS.isWidth+
                        " origWidth="+origWidth+
                        " newImage.w="+newImage.getWidth(flk));   
                  
   if(flk.NEVER && flk.CONSOLE_FLAG)
    {      
      String str= "ImageXform.createZoomedImage().1 img= newImage";    
      iData.printProperties(str,newImage,oImgToZoom);
    }
    
    if(flk.NEVER)
    {
      String str= "ImageXform.createZoomedImage().2 img=newImage";    
      iData.printProperties(str,newImage, null);
    }
    
    /* GC */
    dtk= null;
    mis= null;
    util.gcAndMemoryStats("Clean up after createZoomedImage.");    
    return(newImage);    
  } /* createZoomedImage */
   
  
  /**
   * affineWarp() - compute Affine Warp of left I1 into geometry of
   * right I2. Or right I2 into geometry of left I1 image.
   * The image to transform is determined by the Affine Xform of iData.
   * This assumes the transform coefficients were set up using the
   * Affine class.
   *<P>
   * This is a reverse transform so there are no gaps in output image
   * unless it maps to outside of the input image.
   * compute 8-bit pixels mapped from iPix data.
  */
  final synchronized private boolean affineWarp()
  { /* affineWarp */
    int
      x, y, p;
    
    /* [1] Determine if flip landmarks during computation */
    boolean needToFlipFlag= (nameLR.equals("left"));
    
    /* [2] Setup new affine transform data */
    aff.initAffine((float)thrColinearity, width, height);
    
    /* [3] Set default is to use first 3 landmarks for now.
     * Could specify different landmarks from list of all landmarks
     * in the future. Flip landmarks if doing LEFT image.
     */
    iData.errStr= aff.setLMSindexes(0,1,2, needToFlipFlag);    
    if(! "".equals(iData.errStr))
      return(false);
       
    /* [4] Solve affine transformation of the 3 landmarks and return a
     * dynamically allocated affine object.
     * This solves the 2 simultaneous equations for (a,b,c,d,e,f):
     *        x2= a*x1 + b*y1 +c
     *        y2= d*x1 + e*y1 +f
     * given three landmarks in lms.
     *
     * This saves the dynamic the transform values in the
     * static Affine class variables so can do mappings AFTER
     * remove the Affine instance aff.
     */
    iData.errStr= aff.solveAffineXform();
    if(! "".equals(iData.errStr))
      return(false); 
      
    
    if(flk.CONSOLE_FLAG)
      util.showMsg(aff.affine_calcsString, Color.blue);       
        
    /* [5] Map pixels from the iPix[] image to the mapped
     * coordinates into the oPix[] image.
     */
    int
      idxA,	                  /* index of input pixel to use */
      gO;	                    /* output pixel */
    for (y= 0, p= 0; y < height; y++)
    { /* process rows */
      for (x= 0; x < width; x++, p++)
      { /* remap pixels from iPix to oPix */
        /* Eval 3 point affine transform mapping (x,y) in oPix[p]
         * to (xP,yP). Then the data is in iPix[idxA];
         */
        idxA= aff.mapXYtoAffineIdx(x, y);
        gO= iPix[idxA];
        oPix[p]= gO;           /* save output RGBA pixel */
      } /* remap pixels from iPix to oPix */
    } /* process rows */
    
    /* [6] Remap LMS if spatial transform changed their positions. */
    boolean ok= remapLMS(nameLR, AFFINEWARP);
    
    return(ok);
  } /* affineWarp */
  
    
  /**
   * polyWarp() - compute Polynomial warp of I1 into I2
   * as determined by the Polynomial transform warping Xform of iData.
   * This assumes that the PolyWarp class was setup previously.
   * This is a reverse transform so there are no gaps in output image
   * unless it maps to outside of the input image.
   * Return 32-bit RGBA pixels mapped from iPix data in oPix[]
  */
  final synchronized private boolean polyWarp()
  { /* polyWarp */
    int
      x, y, p;
    
    /* Make sure have enough landmarks */
    if(lms.nLM<6)
    { /* error */
      iData.errStr= "Poly Warp needs 6 landmarks.";
      return(false);
    }
    
    /* Setup local polynomials from landmarks */
    aU= new double[sxf.MXTERMS][sxf.MXTERMS];
    bV= new double[sxf.MXTERMS][sxf.MXTERMS];
    if(! sxf.solvePolyXformCoef())
    { /* error */
      aU= null;
      bV= null;
      iData.errStr= "Error solving Poly Warp coefficients from landmarks";
      return(false);
    }
    else
    { /* get the coeffient matrices */
      this.aU= sxf.get_aU();
      this.bV= sxf.get_bV();
    }
    
    int
      xI,	  /* mapped loc to get iPix[] data */
      yI,
      idxI,
      gO;	 /* output pixel */
        
    for (y= 0, p= 0; y < height; y++)
    { /* process rows */
      for (x= 0; x < width; x++, p++)
      {  /* process cols */      
        /* Eval the poly warp point transform (xI,yI)= f(x,y)
         * xI= u=  SUM     SUM    aU_ij * (x**i) * (y**j)
         *         i=0:n  i=0:n-1
         *
         * yI= v=  SUM     SUM    bV_ij * (x**i) * (y**j)
         *         i=0:n  i=0:n-1
        */
        xI= sxf.evalPoly(3,x,y,aU);
        yI= sxf.evalPoly(3,x,y,bV);
        
        /* Test and clip to real space, if needed */
        if(xI<0)
          xI= 0;
        else if(xI>=width)
          xI= width-1;
        if(yI<0)
          yI= 0;
        else if(yI>=height)
          yI= height-1;
        
        /* Map data */
        idxI= (yI*width + xI);  /* input image pixel address */
        gO= iPix[idxI];
        oPix[p]= gO;            /* save output pixel */
      } /* process columns */
    } /* process rows */
    
    /* Remap LMS if spatial transform changed their positions. */
    return( remapLMS(nameLR, POLYWARP) );
  } /* polyWarp */
  
    
  /**
   * mapXYtoPolyPoint() - map PolyWarp xform (xPrime,yPrime) from f(x1,y1)
   * Verifies that (xPrime,yPrime) in width * height else returns (0,0).
   *  (xPrime,yPrime) mapped loc to get iPix[] data.
   * @param x1 to map
   * @param y1 to map
  */
  final synchronized private void mapXYtoPolyPoint(int x1, int y1)
  { /* mapXYtoPolyPoint */
    /* Eval the poly warp point transform (xI,yI)= f(x1,y1)
     * xI= u=  SUM     SUM    aU_ij * (x1**i) * (y1**j)
     *         i=0:n  i=0:n-1
     *
     * yI= v=  SUM     SUM    bV_ij * (x1**i) * (y1**j)
     *         i=0:n  i=0:n-1
    */
    xPrime= sxf.evalPoly(3,x1,y1,aU);
    yPrime= sxf.evalPoly(3,x1,y1,bV);
    
    /* Test and clip to real space, if needed */
    if(xPrime<0)
      xPrime= 0;
    else if(xPrime>=width)
      xPrime= width-1;
    if(yPrime<0)
      yPrime= 0;
    else if(yPrime>=height)
      yPrime= height-1;
  } /* mapXYtoPolyPoint */
    
  
  /**
   * flipHoriz() - compute horizontal flip image
   * Compute 32-bit RGBA pixels from iPix[(x2*height + x2)] in oPix[]
  */
  final synchronized private void flipHoriz()
  { /* flipHoriz */
    int
      x, y, p,
      xFlipped,
      idx;
    
    for (y= 0, p= 0; y < height; y++)
      for (x= 0; x < width; x++, p++)
      {
        xFlipped= (width -x -1);
        idx= (y * width + xFlipped);
        oPix[p]= iPix[idx];      /* save output RGBA pixel */
      }
  } /* flipHoriz */
    
  
  /**
   * flipVert() - compute Vertical flip image
   * Compute 32-bit RGBA pixels from iPix[(x2*height + x2)] in oPix[]
  */
  final synchronized void flipVert()
  { /* flipVert */
    int
      x, y, p,
      yFlipped,
      idx;
    
    for (y= 0, p= 0; y < height; y++)
      for (x= 0; x < width; x++, p++)
      {
        yFlipped= (height -y-1);
        idx= (yFlipped * width + x);
        oPix[p]= iPix[idx];      /* save output RGBA pixel */
      }
  } /* flipVert */
  
   
  /**
   * pseudo3D() - compute Pseudo 3D by scaling (x',y') by g and angle.
   * Verifies that (x2,y2) in width * height else returns 0.
   * This is a forward transform which means there may be gaps in
   * the output image.
   * Computes 32-bit RGBA pixels from iPix[(x2*height + x2)] in oPix[]
   * @return true if succeed
  */
  final synchronized boolean pseudo3D()
  { /* pseudo3D */
    int
      x, y, p,
      gI,                      /* input pixel as 8-bit */
      gO,                      /* output pixel */
      dX= dX_P3D,	             /* width*sin(theataRad) */
      dX2,
      idx1,	                   /* note: yw= (y  *width) */
      x2,		                   /* mapped x */
      yScale,
      y2,     	               /* mapped y */
      idx2;		                 /* mapped pixel index */
    
    /* Make white background in output image */
    for (y= 0, p= 0; y < height; y++)
      for (x= 0; x < width; x++, p++)
      {
        gI= (iPix[p] & 0Xff);   /* input pixel as 8-bit */
        gO= 0xffffffff;         /* output pixel */
        
        dX2= (dX * (height - y))/height;
        idx1= (yw + x);	        /* note: yw= (y1  *width) */
        x2= (dX2 + x);	        /* mapped x1 */
        yScale= ((zScale * (gI & 0Xff))/100);
        y2= y - yScale;	        /* mapped y1 */
        
        /* #ifdef  USE_3D_SCALING
         scaleFactor= Math.min((((double)width-dX) /
                                        (double)width),
                               ((double)(height - 255*2.55) /
                                 (double)height));
        */
        
        /* Clip  pixel values outside of the image to white */
        if(x2 >= width || x2 < 0)
          gO= 0xffffffff;
        else if(y2 >= height || y2 < 0)
          gO= 0xffffffff;
        else
        { /* map it */
          /*
           if(scaleFactor!=1.0)
           {
             x2= (int)(scaleFactor * x2);
             y2= (int)(scaleFactor * y2);
           }
        */          
          idx2= (y2 * width + x2);
          gO= iPix[idx2];
        } /*map it */
        
        oPix[p]= gO;           /* save output RGBA pixel */
      }
    
    /* Remap LMS if spatial transform changed their positions. */
   return( remapLMS("both",PSEUDO3D) );    
  } /* pseudo3D */
    
  
  /**
   * mapXYtoPseudo3DPoint() - map Pseud3D transform Point(x',y')= f(x,y)
   * Verifies that (x2,y2) in width * height else returns (0,0).
   * Compute Point(x2,y2) in (xPrime,yPrime)
  */
  final synchronized private void mapXYtoPseudo3DPoint(int x1, int y1)
  { /* mapXYtoPseudo3DPoint */
    int
      dX= dX_P3D,	              /* width*sin(theataRad) */
      dX2= (dX * (height - y1))/height,
      idx1= (yw + x1),	        /* note: yw= (y1  *width) */
      x2= (dX2 + x1),	          /* mapped x */
      gI= iPix[y1*width+x1],	  /* get grayscale of input pix */
      yScale= ((zScale * (gI & 0Xff))/100),
      y2= y1 - yScale;	        /* mapped y */

    xPrime= x2;
    yPrime= y2;
    
    /* Clip */
    if(x2 >= width)
      xPrime= width-1;
    if(x2 < 0)
      xPrime= 0;
    if(y2 >= height)
      yPrime= height-1;
    if(y2 < 0)
      yPrime= 0;
  } /* mapXYtoPseudo3DPoint */
  
    
  /**
   * computeGrayHistStatistics() - compute grayscale histogram statistics
  */
  final synchronized private void computeGrayHistStatistics()
  { /* computeGrayHistStatistics */
    gMax= 0;
    gMin= 255;
    gMode= 0;
    hist= new int[256];   /* ASSUMES!!!! 8-bit data */
    histEq= new int[256];
    int
      g,
      size= (width*height),
      halfSize= size/2;
    
    for(int i=(size-1); i>=0; i--)
    { /* compute gMax and gMin  of pPix[] */
      g= (iPix[i] & 0Xff);      
      if(gMax<g)
        gMax= g;
      if(gMin>g)
        gMin= g;
      hist[g]++;
    }
    
    for(int i=gMin; i<=gMax; i++)
      if((gMode+hist[i]) < halfSize)
        gMode += hist[i];
    
    /* Solve:  g'= ((g - gMin)*255) / (gMax-gMin)
     *           = mCE*g + bCE
    */
    mCE= 255 / (gMax - gMin);
    bCE= - 255*gMin / (gMax - gMin);
    
    /* Compute equalized histogram */
    int sum= 0;
    for(int i=0; i<256; i++)
    {
      sum += hist[i];
      histEq[i]= (255*sum)/size;
    }
  } /* computeGrayHistStatistics */
    
  
  /**
   * doXform() - transform iPix[] into oPix[] image data by legal transform
   * Then create the oImg. Then GC whatever we don't need.
   * @param iData the image to transform
  */
  public synchronized void doXform(ImageData iData)
  { /* doXform */
    /* [1] Set status that we have a transform in progress */  
    if(iData==null)
    {
      
      System.out.println("doXform: Error iData is null");
      return;//(false);
    }
    iData.doingTransform= true;
    
    /* [1.1] Set variables */
    int
      p,
      iOpr= 1;		/* mapping of useXform to a number */
    Image
      tmpZoomImg= null;
    ImageScroller is= null; /* for resetting scroller position */
    
    if(flk.iData1==iData)
      is= flk.i1IS;
    else if(flk.iData2==iData)
      is= flk.i2IS;
    
        
    /* [2] Do any prep we need to do. */
    if(externFctNbr>0 && flk.piMgr!=null)
    { /* Perform external plugin Prologue function if not zero */
      /* Pass down additional parameters from flk state since the 
       * Plugin's may not know about the Flicker internal state.
      */
      flk.piMgr.fctCalc(flk, externFctNbr, iData, oPix);
      iOpr= PLUGIN_FCT;               /* protection */
    }
    
    else if ("SharpenGrad".equals(useXform))
    {
      iOpr= SHARPENGRAD;
      sharpenGrad();
    }
    
    else if ("SharpenLapl".equals(useXform))
    {
      iOpr= SHARPENLAPL;
      sharpenLaplacian();
    }
    
    else if ("Gradient".equals(useXform))
    {
      iOpr= GRAD;
      grad();
    }
    
    else if("Laplacian".equals(useXform))
    {
      iOpr= LAPLACIAN;
      laplacian();
    }
    
    else if("Average".equals(useXform))
    {
      iOpr= AVG8;
      avg8();
    }
    
    else if("Median".equals(useXform))
    {
      iOpr= MEDIAN;
      median8();
    } 
    
    else if("Max 3x3".equals(useXform))
    {
      iOpr= MAX8;
      max8();
    }
    else if("Min 3x3".equals(useXform))
    {
      iOpr= MIN8;
      min8();
    }
    
    else if("Complement".equals(useXform))
    {
      iOpr= COMPLEMENT;
      complement();
    }
    
    else if("Threshold".equals(useXform))
    {
      iOpr= THRESHOLD;
      threshold();
    }
    
    else if("ContrastEnhance".equals(useXform))
    { /* Setup contrast enhance by solving for (mCE, bCE) */
      iOpr= CONTR_ENHANCE;
      contrastEnhance();
    }
    
    else if("HistEqualize".equals(useXform))
    { /* make histogram equilization histEq[] */
      iOpr= HIST_EQUAL;
      histogramEqualization();
    }
    
    else if("NormColor".equals(useXform))
    {
      iOpr= NORMCOLOR;
      normalColor();
    }
    
    else if("PseudoColor".equals(useXform))
    {
      iOpr= PSEUDOCOLOR;
      pseudoColor();
    }
    
    else if("Color2Gray".equals(useXform))
    {
      iOpr= COLOR2GRAY;
      rgb2gray();
    }
    
    else if("DeZoom".equals(useXform))
    { /* if magnfication is >1, then magnify, if <1 then dezoom */
      iOpr= DE_ZOOM;
      ImageIO.flushImageResources(iData.zImg); /* if it exists */
       
      /* G.C. zImg and oPix */
      iData.zImg= null;
      oPix= null;
      
      /* Compute new zImg */      
      iData.zImg= createZoomedImage();     
      iData.checkAndMakeIpix(useXform,true); /* need copy of iPix for other stuff */
          
      flk.util.gcAndMemoryStats("dezoom.1"); 
    
    } /* if magnfication is >1, then magnify, if <1 then dezoom */
    
    else if("AffineWarp".equals(useXform))
    { /* Setup affine warp */
      iOpr= AFFINEWARP;
      if(!affineWarp())
      {
        abortTransform();
      }
    } /* Setup affine warp */
    
    else if("PolyWarp".equals(useXform))
    { /* Setup polynomial transform warp */
      iOpr= POLYWARP;
      if(!polyWarp())
      {
        abortTransform();
      }
    } /* Setup polynomial transform warp */
    
    else if("Pseudo3D".equals(useXform))
    {
      iOpr= PSEUDO3D;
      if(!pseudo3D())
      {
        abortTransform();
      }
    }
    
    else if("FlipHoriz".equals(useXform))
    {
      iOpr= FLIPHORIZ;
      flipHoriz();
    }
    
    else if("FlipVert".equals(useXform))
    {
      iOpr= FLIPVERT;
      flipVert();
    }
    
    else
    { /* BOGUS event */
      iData.errStr= "Illegal transform[" + useXform + "]";
      this.oPix= null;        /* Clean up local pointers */
      this.iPix= null;
      flk.util.gcAndMemoryStats("Illegal transform"); 
      abortTransform();
      return;//(false);
    }    
            
    /* [3] The new picture is now in oPix Pixels - create a new Image
     * from it called oImg or zImg depending on whether we are 
     * doing zooming of the oImg..
     * NOTE: if we were doing "DeZoom" function explictly, we do NOT 
     * map oPix of oImg. Instead, we create a new iData.zImg when 
     * by dezooming the oImg into a iData.zImg.
    */
    if(! useXform.equals("DeZoom"))
    { /* Convert oPix to oImg & cur. xForm may still need zooming */ 
      Toolkit dtk= Toolkit.getDefaultToolkit();
      double mag= Math.max(zoomMagVal, SliderState.MIN_ZOOM_MAG_VAL);
      mag= Math.min(mag, SliderState.MAX_ZOOM_MAG_VAL);    
      int
        x= 0,
        y= 0;
    
      //Old non thread safe way : Dimension d= is.sp.getSize();
      Dimension d;
      if(flk.NEVER)
      {
        final ImageScroller isf= curIS;
        
        final Runnable getSize = new Runnable()
        {
          public void run()
          {
            if(flk.NEVER)
              System.out.println("getSize called from invokeAndWait");
            isf.sp.getSize();
          }
        };
        Thread appThread = new Thread()
        {
          public void run()
          {
            try
            {
              SwingUtilities.invokeAndWait(getSize);
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
            if(flk.NEVER)
              System.out.println("'getSize' Finished on " + Thread.currentThread());
          }
        };
        appThread.start();
      }
      else
        d= is.sp.getSize();
      
      ImageIO.flushImageResources(iData.oImg); /* if it exists */ 
      /* G.C. oImg */
      iData.oImg= null;
      flk.util.gcAndMemoryStats("xform-oImg.1"); 
      
      /* Note: always make the oImg since this prevents a race condition
       * if we later need the oImg when doing sequential transforms.
       */
      /* make the oImg */
       MemoryImageSource mis= new MemoryImageSource(width, height, oPix,
                                                    0, width);
       iData.oImg= dtk.createImage(mis);
       mis= null;   /* Setup so can G.C. */
       dtk= null;
       flk.util.gcAndMemoryStats("xform-oImg.2"); 
        
       if(iData.zImg!=null)
       { /* zoom oPix output */
         ImageIO.flushImageResources(iData.zImg);      /* if it exists */
         iData.zImg= null;
         flk.util.gcAndMemoryStats("xform-oImg.1");
         iData.zImg= createZoomedImage(oPix);
         
         if(flk.NEVER && flk.CONSOLE_FLAG)
         {
           String str= "ImageXform.doXform()[3] img is zImg";
           iData.printProperties(str, iData.zImg, null);
         }
       } /* zoom oPix output */
       
       flk.util.gcAndMemoryStats("xform-oImg.3");
    } /* Convert oPix to oImg & cur. xForm may still need zooming */    
    
    gcImageXform("Finished ImageXform ["+useXform+"]"); /* try to G.C. */
                        
    /* [4] Notify main run thread loop that finished this transform. */
    softDoneWithTransform(this.iData, this.nameLR, this.useXform); 
    
    /* Enable repainting after doing a transform */
    setSyncLockFlag(false);         
    flk.doFullRepaint();
    //return(true);
  } /* doXform */
  
  
  /**
   * gcImageXform() - G.C. the image transform variables
   */
  private synchronized void gcImageXform(String msg)
  { /* gcImageXform */
    this.oPix= null;            /* Clean up local pointers */
    this.iPix= null;       
    this.gToRGB= null;
    this.hist= null;			
    this.histEq= null;
    util.gcAndMemoryStats(msg); /* try to G.C. */
  } /* gcImageXform */
    
} /*  -----> end of Class ImageXform <----- */

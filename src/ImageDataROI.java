/* File: ImageDataROI.java */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.*;
import java.io.*;

/**
 * ImageDataROI class supports image data ROI object access. This is
 * the top level data structure for a basic image object.  It
 * contains the input and output Image objects and input and output 
 * int[] pix arrays.
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

public class ImageDataROI
{ /* class ImageData */
      
  /** Instance of ImageData for extended classes */
  public ImageData
    id;
  
  /* ---------------------------------------------------- */
  /*        Region(s) Of Interest (ROI)                   */
  /* ---------------------------------------------------- */
  /* Note that the ND step wedge ROI is kept in and maintained by
   * calib.{ndcwx1,ndcwx2,ndcwy1,ndcwy2}
   */
  /** ULHC ROI window set by (C-U). Note (C-W) clears this. */
  public int
    roiX1, 
    roiY1;   
  /** LRHC ROI window set by (C-L). Note (C-W) clears this. */
  public int
    roiX2, 
    roiY2;
  
  /** generic ULHC computing window set when both (C-U) and (C-L) 
   * were performed. Note (C-W) clears this.
   */
  public int
    cwx1, 
    cwy1;   
  /** generic LRHC computing window when both (C-U) and (C-L) 
   * were performed. Note (C-W) clears this. */
  public int
    cwx2, 
    cwy2;
  
  
  /**
   * ImageDataROI() - Constructor. Obj keeps all image ROI data together.
   * Setup up a 1:1 mapGrayToOD[0:maxGray] if mapGrayToOD is null.
   */
  public ImageDataROI(ImageData id)
  { /* ImageDataROI */
    this.id= id;
    
    roiX1= -1;                            /* disable the ROI */
    roiY1= -1;
    roiX2= -1;
    roiY2= -1;
    setROI2CW(roiX1, roiY1, roiX2, roiY2);
  } /* ImageDataROI */
    

  /**
   * setROI2CW() - set region of interest (ROI) to Computing Window (CW)
   * @param rX1 - ULHC x coordinate
   * @param rY1 - ULHC y coordinate
   * @param rX2 - LRHC x coordinate
   * @param rY2 - LRHC y coordinate
   */
  public void setROI2CW(int rX1, int rY1, int rX2, int rY2)
  {
    this.cwx1= rX1;
    this.cwy1= rY1;
    this.cwx2= rX2;
    this.cwy2= rY2;
  }
    

  /**
   * copyROI2CW() - copy C-U/C-L ROI to Computing Window (CW)
   */
  public void copyROI2CW()
  {
    cwx1= roiX1;
    cwy1= roiY1;
    cwx2= roiX2;
    cwy2= roiY2;
  }
  
  
  /**
   * setROI() - set computing window region of interest (ROI)
   * @param rX1 - ULHC x coordinate
   * @param rY1 - ULHC y coordinate
   * @param rX2 - LRHC x coordinate
   * @param rY2 - LRHC y coordinate
   */
  public void setROI(int rX1, int rY1, int rX2, int rY2)
  {
    this.roiX1= rX1;
    this.roiY1= rY1;
    this.roiX2= rX2;
    this.roiY2= rY2;
  }
  
  
  /**
   * setROI2CALCW() - set ND wedge computing window region of interest (ROI)
   * @param rX1 - ULHC x coordinate
   * @param rY1 - ULHC y coordinate
   * @param rX2 - LRHC x coordinate
   * @param rY2 - LRHC y coordinate
   */
  public boolean setROI2CALCW(int rX1, int rY1, int rX2, int rY2)
  {
    CalibrateOD calib= id.calib;
    if(calib==null)
      return(false);
    calib.ndcwx1= rX1;
    calib.ndcwx1= rY1;
    calib.ndcwx1= rX2;
    calib.ndcwx1= rY2;
    return(true);
  }
    
  
  /**
   * forceROIUpperAndLowerCorners() - reorder the ROI so that
   * (roiX1,roiY1) < (roiX2,roiY2)
   * @return true if successful, false if ROI is not fully defined.
   */
  public boolean forceROIUpperAndLowerCorners()
  { /* forceROIUpperAndLowerCorners */
    if(roiX1==-1 || roiX2==-1 || roiY1==-1 || roiY2==-1)
     return(false);
    
    int
      x1= Math.min(roiX1,roiX2),
      x2= Math.max(roiX1,roiX2),
      y1= Math.min(roiY1,roiY2),
      y2= Math.max(roiY1,roiY2);
    
    roiX1= x1;
    roiX2= x2;
    roiY1= y1;
    roiY2= y2;
    
    return(!isValidROI());
  } /* forceROIUpperAndLowerCorners */
    
  
  /**
   * isValidROI() - check if current region of interest (ROI) is valid
   * @return true if valid
   */
  public boolean isValidROI()
  {
    boolean flag= (roiX1>=0 && roiX1<roiX2 &&roiY1>=0 && roiY1<roiY2);
    return(flag);
  }
    
  
  /**
   * isValidCW() - check if current Computing Window (CW) is valid
   * @return true if valid
   */
  public boolean isValidCW()
  {
    boolean flag= (cwx1>=0 && cwx1<cwx2 && cwy1>=0 && cwy1<cwy2);
    return(flag);
  }
  
  
  /**
   * isValidNDcalibCW() - check if current Calibration Computing Window
   * is valid
   * @return true if valid
   */
  public boolean isValidNDcalibCW()
  {
    CalibrateOD calib= id.calib;
    if(calib==null)
      return(false);
    boolean flag= (calib.ndcwx1>=0 && calib.ndcwx1<calib.ndcwx2 &&
                   calib.ndcwy1>=0 && calib.ndcwy1<calib.ndcwy2);
    return(flag);
  }
    
       
  /**
   * calcHistogramROI() - compute grayscale histogram under the current ROI.
   * The result is in this.hist[] which is created here.
   */
  public boolean calcHistogramROI()
  {  return (id.idM.calcHistogram(cwx1, cwy1, cwx2, cwy2)); }
  
    
} /* class ImageDataROI */

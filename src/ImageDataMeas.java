/* File: ImageDataMeas.java */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.*;
import java.io.*;

/**
 * ImageDataMeas class supports image data spot measurement object access.
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

public class ImageDataMeas 
{ /* class ImageDataMeas*/
              
  /** Flicker global classes */
  public static Flicker
    flk; 
  /** extended Flicker state variable class */
  public static Util
    util; 
        
  /** Instance of ImageData for extended classes */
  public ImageData
    id;
    
  /* ---------------------------------------------------------- */
  /*    Current measured circular mask spot for this image      */
  /* ---------------------------------------------------------- */
  /* Circle masks are used for computing integrated density 
   * under the mask. They are generated by createMaskDatabase().
   */
                     
  /** Array of masks [0:nMasks-1][0:Nr][0:Nr] where Nr is (2*radius+1) */          
  public static int
    maskNxN[][][]; 
  /** number of masks [0:nMasks-1][0:Nr][0:Nr] */          
  public static int
    nMasks= 0;      
  /** Array of mask radius size where Nr is (2*radius+1) */          
  public static int
    maskRadius[];               
  /** List of areas under each mask index by the mask radius */
  public static int
    maskArea[];  
  /** List of total weights under each mask index by the mask radius */
  public static int
    maskTotWeight[];
  
  /** Capture max pixel gray or OD value within mask */
  public float
    maxGrayValue;
  /** Capture min pixel gray or OD value within mask */
  public float
    minGrayValue;
  
  /* ----  Current Spot ---- */
  /** Current spot set with showMeasValue if being measured. */
  public Spot
    curSpot= null;
  
  /** Capture total pixel gray or OD value within mask */
  public float
    totGrayValue= -1.0F;
  /** Capture mean pixel gray or OD value within mask */
  public float
    meanGrayValue= -1.0F;
    
  /** Capture mean "background" pixel gray or OD value. */
  public float 
    meanBkgrdGrayValue= -1.0F;
  /** Capture mean "measurement" pixel gray or OD value. */
  public float 
    meanMeasGrayValue= -1.0F;
  /** Capture total "measurement" pixel gray or OD value. */
  public float 
    totMeasGrayValue= -1.0F;
  
  /** Capture "background" pixel gray or OD value. 
   * This is either mean or tottal integrated (sum over mask area)
   * density.
   */
  public float 
    bkgrdGrayValue= -1.0F;
  /** Capture "measurement" pixel gray or OD value.
   * This is either mean or tottal integrated (sum over mask area)
   * density.
   */
  public float 
    measGrayValue= -1.0F;
  
  /** Capture max "background" pixel gray or OD value */
  public float 
    maxBkgrdGrayValue= -1.0F;
  /** Capture max "measurement" pixel gray or OD value */
  public float 
    maxMeasGrayValue= -1.0F;
  /** Capture min "background" pixel gray or OD value */
  public float 
    minBkgrdGrayValue= -1.0F;
  /** Capture min "measurement" pixel gray or OD value */
  public float 
    minMeasGrayValue= -1.0F;
    
  /** Capture "background" X coordinate */
  public int 
    bkgrdObjX= 0;
  /** Capture "background" Y coordinate */
  public int 
    bkgrdObjY= 0;	
  /** Capture "background" X coordinate */
  public int 
    measObjX= 0;
  /** Capture "background" Y coordinate */
  public int 
    measObjY= 0;
      
  
  /**
   * ImageData() - Constructor for spot measurement extension
   * @param iData is the ImageData instance 
   */
  public ImageDataMeas(ImageData iData)
  { /* ImageDataMeas*/
    this.id= iData;
    flk= id.flk;
    util= flk.util;  
    
    createMaskDatabase(flk.MAX_CIRCLE_RADIUS);
    clean();
  } /* ImageDataMeas*/
  
  
  /**
   * ImageData() - Constructor for creating spot masks
   * Defaults to Flicker.MAX_CIRCLE_RADIUS # of masks
   */
  public ImageDataMeas()
  { /* ImageDataMeas*/    
    createMaskDatabase(Flicker.MAX_CIRCLE_RADIUS); 
  } /* ImageDataMeas*/
  
  
  /**
   * ImageData() - Constructor for creating spot masks of 
   * size 1x1 to (maxRadiusToCreate X maxRadiusToCreate)
   * @param maxRadiusToCreate 
   */
  public ImageDataMeas(int maxRadiusToCreate)
  { /* ImageDataMeas*/    
    createMaskDatabase(maxRadiusToCreate); 
  } /* ImageDataMeas*/
    
  
  /**
   * clean() - init the image data measurement state
   */
  public void clean()
  { /* clean */    
    meanBkgrdGrayValue= -1;
    meanMeasGrayValue= -1;
    bkgrdGrayValue= -1;
    measGrayValue= -1;	
    maxBkgrdGrayValue= -1.0F;
    maxMeasGrayValue= -1.0F;
    minBkgrdGrayValue= -1.0F;
    minMeasGrayValue= -1.0F;
  } /* clean */
  
  
  /**
   * createMaskDatabase() - create the set of circular masks of size NrxNr
   * stored in a 3D array [0:nMasks-1][0:Nr][0:Nr] where Nr is (2*radius+1) 
   * @param maxCircleRadius is the maximum circle size radius
   * @return the # of masks generated.
   */
  public static int createMaskDatabase(int maxCircleRadius)
  { /* createMaskDatabase */ 
    if(nMasks>0 && nMasks==maxCircleRadius)
      return(nMasks);        /* already exists */
    
    nMasks= maxCircleRadius; /* # of masks [0:nMasks-1][0:Nr][0:Nr] */  
    
    /* [1] Allocate masks [0:nMasks-1][0:Nr][0:Nr] where Nr is (2*radius+1) */          
    maskNxN= new int[nMasks+1][][];
    maskRadius= new int[nMasks+1];  
    maskArea= new int[nMasks+1]; 
    maskTotWeight= new int[nMasks+1]; 
    
    int
      nR,
      nRminus1,
      g, x, y,
      xC,                   /* Cartesian coordinates center of 2r+1 circle */
      yC,
      xR,                   /* Raster coordinates center of 2r+1 circle */
      yR,
      x1, x2, y1, y2,       /* Raster coordinates */
      mask[][],
      firstX,
      lastX;
    double 
      sinX,
      radian;
    
    /* [2] Create a set of masks */
    for(int r=0;r<=nMasks; r++)
    { /* make the r'th mask */             
      nR= 2*r+1;
      mask= new int[nR][nR];
      maskNxN[r]= mask;
      maskRadius[r]= r;
            
      /* Create the circles in the mask */
      nRminus1= nR-1;     
      xC= r;                  /* cartesian coords center of 2r+1 circle */
      yC= r;
      
      /* [2.1] Draw the circle */
      /* NOTE: the resolution is determined by the maximum mask radius! */
      /* Note:  delta= 0.05 is ok for max radius of 15 */
      double delta= 0.03;
      for(double deg=0.0; deg<=90.0; deg += delta)
      { /* process row by row */
        radian= deg*Math.PI/2;
        /* map to cartesian coordinates */
        x= (int)(r*Math.sin(radian));    
        y= (int)(r*Math.cos(radian));
        
        /* Map to raster coordinates */
        x1= xC-x;
        x2= x+xC;
        y1= yC-y;
        y2= yC+y;
        /* Set the bits on the edge of the circle */
        mask[x2][y1]= 1;            /* URHC */
        mask[x1][y1]= 1;            /* ULHC */
        mask[x2][y2]= 1;            /* LRHC */        
        mask[x1][y2]= 1;            /* LLHC */
      } /* process row by row */      
      
      //if(flk.NEVER) System.out.println(cvMaskToStr(mask,"\n1.1 Create circle"));
      
      /* [2.2] Create the actual circular mask bits between left and right
       * pixels on each line.
       */
      maskArea[r]= 0;               /* compute the sum of 1's */ 
      for(y=0;y<nR;y++)
      { /* process row by row */
        firstX= -1;
        lastX= -1;
        
        for(x=0;x<nR;x++)
        { /* find first and last column for row y */
          g= mask[x][y];
          if(g==1 && firstX==-1)
            firstX= x;              /* save first instance */
          if(g==1)
            lastX= x;               /* save last instance */
        } /* find first and last column for row y */
                
        for(x=firstX;x<=lastX;x++)
        { /* set pixels between first and last column for row y */
          mask[x][y]= 1;   
          maskArea[r]++; 
        } /* find first and last column for row y */
      } /* set pixels between first and last column for row y */
      
      /* Note: this is the weighted area which is same as area
       * for now
       */
      maskTotWeight[r]= calcMaskTotWeight(mask);
      
      //if(flk.NEVER) System.out.println(cvMaskToStr(mask,"1.2 After fill"));
    } /* make the r'th mask */
    
    return(nMasks);
  } /* createMaskDatabase */
  
     
  /**
   * cvMaskToStr() - convert a mask to a printable string
   * @param mask is [nR][nR] to print
   * @param msg is optional message to prefix out if not null
   * @return string representing the matrix
   */
  public static String cvMaskToStr(int mask[][], String msg)
  { /* cvMaskToStr */
    int 
      nR= mask.length,
      r= (nR-1)/2,
      mBit;
    if(msg==null)
      msg= "";
    String
      sVal,
      s,
      sR= msg+"\n Mask["+nR+"x"+nR+"] maskArea[r]="+maskArea[r]+"\n";
    for (int y=0; y<nR; y++)
    { /* Do a line */
      s= "";
      for (int x=0; x<nR; x++)
      { /* print data */
        mBit= mask[x][y];
        sVal= (mBit+" ");
        s += sVal;
      } /* print data */
      sR += s+"\n";
    } /* Do a line */
    
    return(sR);
  } /* cvMaskToStr */
  
  
  /**
   * setupCircularMaskStatistics()- compute the mask area and total
   * weights for each mask. Also set the current mask
   */
  public void setupCircularMaskStatistics()
  { /* setupCircularMaskStatistics */
    createMaskDatabase(flk.MAX_CIRCLE_RADIUS);    
    setCircleMaskRadius(flk.nCirMask,id);
  } /* setupCircularMaskStatistics */
  
  
  /**
   * calcMaskArea() - compute the area under the mask
   * @param mask is 2D mask
   * @return total non-zero area under the mask
   */
  private static int calcMaskArea(int mask[][])
  { /* calcMaskArea */
    int
      area= 0,
      tmp[]= mask[0],
      n= tmp.length;
    for(int r= 0; r<n;r++)
      for(int c= 0;c<n;c++)
        if(mask[r][c]!=0)
          area++;
    
    return(area);
  } /* calcMaskArea */
  
  
  /**
   * calcMaskTotWeight() - compute the total weights under the mask
   * @param mask is 2D mask
   * @return total non-zero total weights under the mask
   */
  private static int calcMaskTotWeight(int mask[][])
  { /* calcMaskTotWeight */
    int
      totWeights= 0,
      tmp[]= mask[0],
      n= tmp.length;
    for(int r= 0; r<n;r++)
      for(int c= 0;c<n;c++)
        if(mask[r][c]!=0)
          totWeights += mask[r][c];
    
    return(totWeights);
  } /* calcMaskTotWeight */
  
  
  /**
   * setCircleMaskRadius() - set the circle radius for measurement and
   * background, as well as the circle mask given the radius.
   * @param radius - of the circle mask
   * @param iData - if set to I1 or I2, null otherwise
   */
  public static void setCircleMaskRadius(int radius, ImageData iData)
  { /* setCircleMaskRadius */    
    radius= Math.max(0,Math.min(radius,flk.MAX_CIRCLE_RADIUS));
    flk.measCircleRadius= radius;
    flk.bkgrdCircleRadius= flk.measCircleRadius;
    
    /* Save these values in the current selected image state */
    if(iData!=null)
    {
      iData.state.measCircleRadius= flk.measCircleRadius;
      iData.state.bkgrdCircleRadius= flk.bkgrdCircleRadius;
    }
        
    /* Lookup "measurement" circle mask of size nCirMask = (2*radius+1) */
    flk.circleMask= maskNxN[radius];
    flk.nCirMask= radius;
  } /* setCircleMaskRadius */
  
       
  /**
   * calcSumValuesUnderMask() - compute sum of pixel values under the mask
   * corresponding to the mask set by the current radius.
   * If the gray to OD map exists, then map it and return integrated OD.
   * eturn -1 if mask intersects the edge of the image.
   * @param radius is mask radius to use (2*radius+1)
   * @param x0 center of the mask'ed data
   * @param y0 center of the mask'ed data 
   * @param useTotFlag to return total gray values else mean gray values.
   * @return sum of pixels values, mapped to OD if map exists, else
   *         return -1.0 if therre is a problem.
   */
  public float calcSumValuesUnderMask(int radius,
                                      int x0, int y0,
                                      boolean useTotDensityFlag)
  { /* calcSumValuesUnderMask */
    int
      nTot= 1,                    /* # of pixels in sum */
      g= getPixelValue(x0,y0);
    /* [TODO]: g is -1 then crashes. Only when viewDispGrayValuesFlag is set 
     * to true in the .flk file. Need to add more code through out to check
     * for -1 in indexes and other bad stuff. */
    float
      gMapped= (g>=0) ? id.mapGrayToOD[g] : 0, /* protect it */
      tot= gMapped;    
    
    maxGrayValue= gMapped;        /* default max and min */
    minGrayValue= gMapped;
      
    /* Compute the integrated intensity under the mask.
     * May need to revise this if doing non-linear OD
     * gray to OD mapping.
     */
    if(radius>=1)
    { /* compute tot intensity in the area under the circle */
      setCircleMaskRadius(radius, id);
      
      int
        x= 0, 
        y= 0,
        xP, yP,
        xM, yM,        
        pixHeight,
        pixWidth,        
        nCirMask= flk.nCirMask,
        nCirMaskSq= nCirMask*nCirMask,
        circleMask[][]= flk.circleMask,
        circleMaskRow[];     
      
      /* new w & h if zoomed otherwise orig img size */
      if(id.mag!=1.0)
      {
        pixHeight= id.zoomedHeight;
        pixWidth= id.zoomedWidth;
      }
      else
      {
        pixHeight= id.iWidth;
        pixWidth= id.iHeight;
      }
      
      /* return -1.0 if mask intersects the edge of the image */
      if(x0-radius<0 || y0-radius<0 || 
         (x0+radius)>=pixWidth|| (y0+radius)>=pixHeight)
        return(-1.0F);
      
      nTot= 0;
      tot= 0;
      for(y=-radius;y<radius;y++)
      { /* process a row */
        yP= y+y0;
        yM= y+radius;
        circleMaskRow= circleMask[yM];
        for(x= -radius; x<radius; x++)
        { /* process a pixel */
          xP= x+x0;
          xM= x+radius;
          if(circleMaskRow[xM]==0)
            continue;                  /* ignore this pixel */
          g= getPixelValue(xP, yP);    /* get gray(xP,yP) */
          gMapped= id.mapGrayToOD[g];
          
          maxGrayValue= Math.max(gMapped, maxGrayValue);
          minGrayValue= Math.min(gMapped, minGrayValue);
          
          tot += gMapped;              /* sum gray values */
          nTot++;                      /* only count valid pixels */
        } /* process a pixel */
      } /* process a row */      
    } /* compute tot intensity in the area under the circle */
    
    totGrayValue= tot;                 /* Always compute mean value */    
    meanGrayValue= totGrayValue/nTot;  /* Always compute mean value */
    if(! useTotDensityFlag)
      tot= meanGrayValue;              /* return mean gray values */
        
    return(tot);
  } /* calcSumValuesUnderMask */
  
  
  /**
   * getPixelValue() - get the pixel value for the image at (x,y). 
   * Do not map grayscale to OD. That should be done by the caller.
   *<PRE>
   * Notes: 
   * 1) because we only keep 24-bit (R,G,B) pixel image data, we only 
   *    report the least significant 8-bits (blue channel) for the gray value.
   * 2) If the original image data had > 8-bits, we still report it (for
   *    now) as 8-bits.
   *</PRE>
   * @return grayscale value of pixel, -1 if a problem.
   */
  final public int getPixelValue(int x, int y)
  { /* getPixelValue */ 
    int
      remappedX= x,/* if zoomed, must be mapped back to orig coords*/
      remappedY= y;
      
    if(id.mag !=0)
    {      
      Point pt= id.mapZoomToState(new Point(x,y));  
      remappedX= pt.x;
      remappedY= pt.y;    
    }
    
    int
      size= (id.iWidth * id.iHeight),
      idx= remappedY * id.iWidth + remappedX;
    if(id.iPix==null)
      return(-1); 
    if(id.iPix.length< size)
      return(-1);    
    int
      g= ((idx>=0 && idx<size)
            ? (id.iPix[idx] & id.pixelMask)
            : 0);      /* Note This already is masked and complemented */
              
    if(id.blackIsZeroFlag)
      g= (id.pixelMask - g);
                     
    return(g);
  } /* getPixelValue */
  
  
  /**
   * cvtGrayValueToODstr() - convert gray value to OD string if
   * the map has been defined for the ImageData instance
   * @param g is the gray value to convert
   * @return converted gray value
   */
  public String cvtGrayValueToODstr(int g)
  { /* cvtGrayValueToODstr */
     String sT;
     if(id.hasODmapFlag && g>=0)
     { /* OD value */
       float od= id.mapGrayToOD[g];
       sT= util.cvf2s(od,3)+" "+id.calib.unitsAbbrev;
     }
     else
     { /* default to gray value */
       sT= "" + g +" gray-value";
     }
     
     return(sT);
  } /* cvtGrayValueToODstr */
  
   
  /**
   * getPixelValueStr() - get the pixel value string for the image where
   * the trial object (xObj,yObj) is located. If the gray to OD map exists,
   * then compute it as OD else grayscale.
   * If we are measuring spots in the spot list and they are viewable,
   * then we can append the spot# and the spot.id (if not null).
   *<PRE>
   * Notes: 
   * 1) because we only keep 24-bit (R,G,B) pixel image data, we only 
   *    report the least significant 8-bits (blue channel) for the gray value.
   * 2) If the original image data had > 8-bits, we still report it (for
   *    now) as 8-bits.
   * 3) [TODO] could remap RGB via NTSC mapping...
   *</PRE>
   * @param xObj pixel coordinate
   * @param yObj pixel coordinate
   * @return string value of pixel 
   */
  public String getPixelValueStr(int xObj, int yObj)
  { /* getPixelValueStr */
    String 
      sumMod= (flk.useTotDensityFlag) ? " tot " : " mn ",
      shortImageName= util.getFileNameFromPath(id.imageFile),
      sT;
    
    if(flk.viewDispGrayValuesFlag && !flk.firstTimeThruFlag)
    { /* allow display if calibrated grayToODmap[] */
      float 
        odVal= calcSumValuesUnderMask(flk.measCircleRadius,
                                      xObj, yObj, 
                                      flk.useTotDensityFlag);        
       sT= shortImageName+"  ";
       if(xObj!=-1 && yObj!=-1)
         sT += "(" + xObj + ", " + yObj + ")  "; 
       
       if(!id.hasODmapFlag && odVal!=-1.0F)
       { /* default to gray value*/
         sT += ((int)odVal)+sumMod+"gray-value";
       }
       else if(odVal!=-1.0F)
       { /* OD value */
         sT += util.cvf2s(odVal,3)+sumMod+id.calib.unitsAbbrev; 
       }
    } /* allow display if calibrated grayToODmap[] */
      
    else if(xObj!=-1 && yObj!=-1)
      sT= (shortImageName+ "  (" + xObj + ", " + yObj + ")");  
      
    else
      sT= shortImageName;      /* No data was selected. Object not defined */
           
    if(flk.spotsListModeFlag)
    { /* if the spot is in the spot list, append 'spot# and id if any' */
      Spot s= id.idSL.lookupSpotInSpotListByXY(xObj, yObj);
      if(s!=null)
      {
         sT += ", spot #"+s.nbr;
         if(s.id!=null)
           sT += " id:"+s.id;
      }
    } /* if the spot is in the spot list, append 'spot# and id if any' */
      
    return(sT);
  } /* getPixelValueStr */
      
  
  /**
   * captureBackgroundValue() - capture the values under the mask of the
   * selected image if it is left or right and assign it to the
   * background pixel value for the current image.
   * If calibration is in effect, set the calibrated value.
   * @return current pixel value, -1 if no image selected
   */
  public float captureBackgroundValue()
  { /* captureBackgroundValue */ 
    ImageScroller is;
    
    if(flk.activeImage.equals("left"))
      is= flk.i1IS;
    else if(flk.activeImage.equals("right")) 
      is= flk.i2IS;      
    else
      return(-1);
    
    ImageData id2= is.iData; 
    boolean useTotDensityFlag= true;   
    int
      x= is.getXObjPosition(),
      y= is.getYObjPosition();
    
    float
      tot= calcSumValuesUnderMask(flk.bkgrdCircleRadius, x, y,
                                  flk.useTotDensityFlag);   
    ImageDataMeas idM2= id2.idM;
    idM2.bkgrdGrayValue= tot;
    double
      mag= Math.max(flk.curState.zoomMagVal, 
                    SliderState.MIN_ZOOM_MAG_VAL);
      mag= Math.min(mag, SliderState.MAX_ZOOM_MAG_VAL);
    if(mag != 1.0) 
    {
      double 
        xDb= x/mag,
        yDb= y/mag;
      
     idM2.bkgrdObjX= (int) Math.round(xDb);
     idM2.bkgrdObjY= (int) Math.round(yDb);
    }
    else 
    {
      idM2.bkgrdObjX= x;
      idM2.bkgrdObjY= y;
    }

    idM2.maxBkgrdGrayValue= maxGrayValue;
    idM2.minBkgrdGrayValue= minGrayValue;    
    idM2.meanBkgrdGrayValue= meanGrayValue;  /* in case needed */
    
    return(idM2.bkgrdGrayValue);
  } /* captureBackgroundValue */
  
     
  /**
   * showBkgrdValue() - print the background measurement value.
   * These were added using the CTRL-B for background. 
   * It sends the string to the output.
   * @return current background measurement string, null if no data
   */
  public String showBkgrdValue()
  { /* showBkgrdValue */ 
    ImageScroller is;
    String sT= null;
    
    if(flk.activeImage.equals("left"))
      is= flk.i1IS;
    else if(flk.activeImage.equals("right"))
      is= flk.i2IS;
    else
      return(null);
    
    ImageDataMeas idM= id.idM;
    float bkgrdVal= idM.captureBackgroundValue();
    int area= idM.maskArea[flk.measCircleRadius];
    
    if(bkgrdVal>=0)
    { /* background value exists */
      String
        shortImageName= util.getFileNameFromPath(is.title);
      sT= shortImageName+ " (" + idM.bkgrdObjX + "," + 
          idM.bkgrdObjY + ")";
      if(id.hasODmapFlag)
      {
        sT += " totBkgrd: "+ util.cvf2s(idM.totGrayValue,3);
        sT += " "+id.calib.unitsAbbrev+
              ", meanBkgrd: "+ util.cvf2s(idM.meanBkgrdGrayValue,3);
      }
      else
      { /* gray value */
        sT += " totBkgrd: "+ (int)idM.totGrayValue;
        sT += id.calib.unitsAbbrev+
              ", meanBkgrd: "+ (int)idM.meanBkgrdGrayValue;
      }
      
      /* Add on circle mask info */
      sT += " "+id.calib.unitsAbbrev+"\n";
      sT += "  CircleMask: "+flk.nCirMask+"X"+flk.nCirMask+
            " area: "+area;
      sT += " pixels";
      util.showMsg(sT,Color.black);
    } /* background value exists */
    
    return(sT);
  } /* showBkgrdValue */ 
  
       
  /**
   * captureMeasValue() - capture the pixel values under the mask of the
   * selected image if it is left or right and assign it to the
   * measurement pixel value for the current image.
   * If calibration is in effect, set the calibrated value.
   * @return current pixel value, -1 if no image selected
   */
  public float captureMeasValue()
  { /* captureMeasValue */
    ImageScroller is;
    
    if(flk.activeImage.equals("left"))
      is= flk.i1IS;
    else if(flk.activeImage.equals("right")) 
      is= flk.i2IS;      
    else
      return(-1);
    
    ImageData id2= is.iData;    
    float
      tot= calcSumValuesUnderMask(flk.measCircleRadius,
                                  is.getXObjPosition(), is.getYObjPosition(),
                                  flk.useTotDensityFlag);  
    ImageDataMeas idM2= id2.idM; 
    idM2.measGrayValue= tot;
    int
      x= is.getXObjPosition(),
      y= is.getYObjPosition();
    
    double
      mag= Math.max(flk.curState.zoomMagVal, SliderState.MIN_ZOOM_MAG_VAL);
      mag= Math.min(mag, SliderState.MAX_ZOOM_MAG_VAL);
    if(mag != 1.0)
     {
       double
         xDb= x/mag,
         yDb= y/mag;
       idM2.measObjX= (int) Math.round(xDb);
       idM2.measObjY= (int) Math.round(yDb);       
      }
    else
    {
       idM2.measObjX= x;
       idM2.measObjY= y;
    }
    
    idM2.maxMeasGrayValue= maxGrayValue;
    idM2.minMeasGrayValue= minGrayValue; 
    idM2.meanMeasGrayValue= meanGrayValue;  /* in case needed */
    idM2.totMeasGrayValue= totGrayValue;  /* in case needed */
    
    return(idM2.measGrayValue);
  } /* captureMeasValue */
  
  
  /**
   * captureMeasCWvalue() - capture the pixel values under the 
   * computing window of the selected image if it is left or right and 
   * assign it to the measurement pixel value for the current image.
   * If calibration is in effect, set the calibrated value.
   * @return current pixel value, -1 if no image selected
   */
  public float captureMeasCWvalue()
  { /* captureMeasCWvalue */
    ImageScroller is;
    
    if(flk.activeImage.equals("left"))
      is= flk.i1IS;
    else if(flk.activeImage.equals("right")) 
      is= flk.i2IS;      
    else
      return(-1);
    
    ImageDataROI idROI= id.idROI;
    if(!idROI.isValidCW())
    {
      String
        msg1= "You must assign the Region Of Interest (ROI) to the ",
        msg2= "Computing Window before making a measurement.";
      util.showMsg(msg1, Color.red);
      util.showMsg2(msg2, Color.red);
      util.popupAlertMsg(msg1+msg2, flk.alertColor);
      return(-1);
    }
    
    boolean
      oldUseTotDensityFlag= flk.useTotDensityFlag;
    float
      mapGrayToOD[]= id.mapGrayToOD,
      gMapped= 0.0F,
      tot= 0.0F; 
    int
      nTot= 0,
      x= 0,
      y= 0,
      cwx1= idROI.cwx1,
      cwy1= idROI.cwy1,
      cwx2= idROI.cwx2,
      cwy2= idROI.cwy2,
      g; 
    
    flk.useTotDensityFlag= true;  /* force it to get total density */
    tot= 0;
    maxGrayValue= 0.0F;           /* default max and min */
    minGrayValue= 1000000000.0F; 
    
    /* compute tot intensity in the area under the computing window */      
    for(y= cwy1;y<=cwy2;y++)
    { /* process a row */
      for(x= cwx1; x<=cwx2; x++)
      { /* process a pixel */
        g= getPixelValue(x, y);   /* get gray(xP,yP) */
        gMapped= mapGrayToOD[g];
        
        maxGrayValue= Math.max(gMapped, maxGrayValue);
        minGrayValue= Math.min(gMapped, minGrayValue);
        
        tot += gMapped;                    /* sum gray values */
        nTot++;                            /* only count valid pixels */
      } /* process a pixel */
    } /* process a row */
    
    if(! flk.useTotDensityFlag)
      tot= tot/nTot;                      /* return mean gray values */

    measGrayValue= tot;
    
    int
      xObjPos= is.getXObjPosition(),
      yObjPos= is.getYObjPosition();
    
    double
      mag= Math.max(flk.curState.zoomMagVal, SliderState.MIN_ZOOM_MAG_VAL);
      mag= Math.min(mag, SliderState.MAX_ZOOM_MAG_VAL);
    if(mag != 1.0) 
    {
      double 
        xDb= xObjPos/mag,
        yDb= yObjPos/mag;      
      measObjX= (int) Math.round(xDb);
      measObjY= (int) Math.round(yDb);      
    }
    else 
    {
      measObjX= xObjPos;
      measObjY= yObjPos;
    }
    
    maxMeasGrayValue= maxGrayValue;
    minMeasGrayValue= minGrayValue;
    
    flk.useTotDensityFlag= oldUseTotDensityFlag;  /* restore state */
        
    /* Compute the histogram under the ROI in case want to use it */
    idROI.calcHistogramROI();    
    
    /* Display the histogram under the ROI if it exists */
    if(id.dwHist!=null)
    { /* Update ROI histogram popup */
      id.dwHist.setTitleHist("Histogram of ROI ["+
                          cwx1+":"+cwx2+","+cwy1+":"+cwy2+"] "+
                          id.imageFile);
      if(! id.dwHist.isVisibleFlag)
        id.dwHist.setVisible(true,false);
      else
        id.dwHist.updateHistogramPlot(false);
    }  /*  Update ROI histogram popup */     
    
    return(measGrayValue);
  } /* captureMeasCWvalue */
  
       
  /**
   * showMeasValue() - print the measurement value subtracting the 
   * background value. These were added using the CTRL-B for background 
   * and CTRL-M for measure. It sends the string to the output.
   * @param measMode is "circleMask", "compROI", "boundary"
   * @return current pixel measurement string, null if not data. If
   *       measuring spots in the spot list, then set curSpot to
   *       the current spot being measured, else set it to null.
   */
  public String showMeasValue(String measMode)
  { /* showMeasValue */ 
    ImageScroller is;
    int 
      idxLR= 0;
    
    if(flk.activeImage.equals("left"))
    {
      idxLR= 1;
      is= flk.i1IS;
    }
    else if(flk.activeImage.equals("right")) 
    {
      idxLR= 2;
      is= flk.i2IS;
    }
    else
      return(null);
    
    ImageData id2= is.iData;
    ImageDataMeas idM2= id2.idM; 
    ImageDataSpotList idSL= id2.idSL; 
    ImageDataROI idROI= id.idROI;
    /* If calibrated, then return in OD calibrated units else grayscale */ 
    float
      bkgrdI= idM2.bkgrdGrayValue,  
      measI= idM2.measGrayValue,
      bkgrd= 0,
      meas= 0,
      measPrime= 0;    
    int area= maskArea[flk.measCircleRadius];
    
    boolean 
     oldUseTotDensityFlag= flk.useTotDensityFlag,
     hasValidODmap= (id.hasODmapFlag && bkgrdI>=0 && measI>=0);
    
    if(measMode.equals("compROI"))
    { /* Compute total background density */
      int size= (idROI.roiX2-idROI.roiX1+1)*(idROI.roiY2-idROI.roiY1+1);
      bkgrdI= idM2.meanBkgrdGrayValue*size;     
      flk.useTotDensityFlag= true;
    }
    
    /* Compute the measurement corrected for background or measPrime */
    if(hasValidODmap)
    { /* Calibrated units */    
      bkgrd= bkgrdI;
      meas= measI;
      measPrime= (meas - bkgrd); 
    }
    else
    { /* Grayscale - just subtract */
      bkgrd= (float)Math.max(bkgrdI,0.0);
      meas= (float)Math.max(measI,0.0);
      measPrime= (float)(meas - bkgrd);    
    }
       
    String
      sQ= (flk.useTotDensityFlag) ? "Tot" : "Mean",
      sMeasRange= sQ+"Meas: " + util.cvf2s(meas,3)+
                  " ["+util.cvf2s(idM2.minMeasGrayValue,3) +":"+
                       util.cvf2s(idM2.maxMeasGrayValue,3) +"]",
      sBkgrdRange= (bkgrdI>=0.0F) 
                     ? sQ+"Bkgrd: " + util.cvf2s(bkgrd,3)+        
                     " at (" + idM2.bkgrdObjX + "," + idM2.bkgrdObjY + ")"+
                     " [" + util.cvf2s(idM2.minBkgrdGrayValue,3) + ":"+
                       util.cvf2s(idM2.maxBkgrdGrayValue,3) + "]"
                     : "",
      sMeasCounter= "";
    if(flk.useMeasCtrFlag)
    {
      idSL.measCtr++;      
      sMeasCounter= "[" + idSL.measCtr + "] ";
    }
    String
      unitsAbbrev= id.calib.unitsAbbrev,
      shortImageName= flk.util.getFileNameFromPath(is.title),
      sT= sMeasCounter + shortImageName+ 
          " (" + idM2.measObjX + "," + idM2.measObjY + ") ";
    
    if(unitsAbbrev==null || unitsAbbrev.length()==0)
      unitsAbbrev= "gray-value";
    
    if(bkgrdI>=0)
      sT += sQ +"(Meas-Bkgrd): " + util.cvf2s(measPrime,3);
    else
      sT += sQ + "Meas: " + util.cvf2s(meas,3);
    sT += " " + unitsAbbrev + "\n";
    
    sT += "   " + sMeasRange;
    if(bkgrdI>=0)
      sT += "\n   " + sBkgrdRange;
    sT += " " + unitsAbbrev + "\n";     
        
    float mnDensPrime= idM2.meanMeasGrayValue - idM2.meanBkgrdGrayValue;
    if(measMode.equals("circleMask"))
    {      
      sT += "   mnDens: " + util.cvf2s(idM2.meanMeasGrayValue,3) + 
                  ", mn(Dens-Bkgrd): " + util.cvf2s(mnDensPrime,3) +
                  ((idM2.bkgrdObjX<=0) ? "" : ", mnBkgrd: " +
                  util.cvf2s(idM2.meanBkgrdGrayValue,3)) + " "+
                  unitsAbbrev+"\n";
      sT += "   CircleMask: " + flk.nCirMask + "X" + flk.nCirMask +
            " area: "+area+" pixels\n";
    }
    
    else if(measMode.equals("compROI"))
      sT += "   ROI: [" + idROI.roiX1 + ":" + idROI.roiX2 + ", " +
                idROI.roiY1 + ":" + idROI.roiY2 + "]\n";
    
    if(measMode.equals("boundary"))
      sT += "   Boundary: " + "\n";
    
    /* Save the spot in the spot list if enabled. Use the Spot string
     * converter.
     */
    curSpot= null;  /* Only use current spot if measuring spots */
    if(flk.useMeasCtrFlag && 
       (flk.doMeasureProtIDlookupAndPopupFlag ||
        measMode.equals("circleMask")))
    { /* save spot in spotList[] or update existing spot in the spotList */
      curSpot= idSL.addUniqueSpotToSpotList(idxLR, idSL.measCtr, 
                                            flk.nCirMask,
                                            flk.measCircleRadius,
                                            idM2.measObjX, idM2.measObjY,
                                            idM2.bkgrdObjX, idM2.bkgrdObjY,
                                            (float)area, hasValidODmap,
                                            flk.useTotDensityFlag,
                                            meas, measPrime, bkgrd,
                                            idM2.meanMeasGrayValue,
                                            idM2.totMeasGrayValue, 
                                            mnDensPrime,
                                            idM2.meanBkgrdGrayValue,
                                            idM2.maxMeasGrayValue,
                                            idM2.minMeasGrayValue,
                                            idM2.maxBkgrdGrayValue,
                                            idM2.minBkgrdGrayValue);
      if(curSpot!=null)
      { /* use the spot string converter */
         sT= curSpot.cvSpot2Str(shortImageName, id.calib.unitsAbbrev);
      }
    } /* save spot in spotList[] or update existing spot in the spotList */
    
    util.showMsg(sT, Color.black);
    
    flk.useTotDensityFlag= oldUseTotDensityFlag;  /* restore state */
    
    return(sT);
  } /* showMeasValue */
     
       
  /**
   * calcHistogram() - compute grayscale histogram under the specified 
   * computing window [x1:x2, y1:y2].
   * The result is in this.hist[] which is created here.
   * @param x1 - ULHC x coordinate
   * @param y1 - ULHC y coordinate
   * @param x2 - LRHC x coordinate
   * @param y2 - LRHC y coordinate
   */
  public boolean calcHistogram(int x1, int y1, int x2, int y2)
  { /* calcHistogram */
    /* Test if this computing window [x1:x2, y1:y2] is valid */
    boolean validCW= (x1>=0 && x1<x2 && y1>=0 && y1<y2); 
    if(! validCW)
      return(false);
    
    id.hist= new int[id.pixelMask+1];    /* Create new zeroed histogram */
    int 
     x, 
     g;
    
    for(int y=y1;y<y2;y++)
      for(x=x1;x<x2;x++)
      {
        g= getPixelValue(x,y);
        id.hist[g]++;
      }

    return(true);
  } /* calcHistogram */
    
       
  /**
   * main() - createMaskDatabase function for debugging...
   */
  public static void main(String [] args)
  { /* main */
    int n= 25;
    ImageDataMeas idM= new ImageDataMeas(n);
    
    for(int r=0;r<n;r++)
    { /* print each mask r */
      int
        mask[][]= maskNxN[r],
        nR= 2*r+1;
      String s= cvMaskToStr(mask,"2. In main");
      System.out.println(s);
    } /* print each mask r */
  } /* main */
  
  
} /* class ImageDataMeas */

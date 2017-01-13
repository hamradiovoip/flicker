/* File: CalibrateOD.java  */

import java.awt.*;

/**
 * CalibrateOD handles grayscale to OD calibrations for images.
 * Some of this code is derived from GELLAb-II GELLAB-II.
 * Gellab was first described in Lipkin L.E, Lemkin P.F. (1980) 
 * Database techniques for multiple two-dimensional polyacrylamide gel 
 * electrophoresis analyses. Clinical Chemistry 26, 1403-1412.
 * See http://www.lecb.ncifcrf.gov/gellab for more info.
 *<P> 
 * This work was produced by Peter Lemkin of the National Can cer
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

public class CalibrateOD
{ /* CalibrateOD */
  /** Global instances */
  private Flicker
    flk;
  private Util
    util;
  
  /** Local debugging flag set in CalibrateOD.main() test code */
  private static boolean
    DBUG_CALIB= false;
  
  /** Master Calibration counter */
  public static int
    calibCtr= 0;
  
  /** Maximum # of OD and peak values in the peak table */
  final static int
    MAX_ND_STEPS= 20;
  /** Maximum gray value possible in 8-bits */
  final static int
    MAX_GRAY= 255;
  
  /** Calibration instance counter. Note: Not copied in the clone. */
  public int
    nbr= ++calibCtr;
  
  /** Gray value for corresponding wedge point [0:maxPeaks-1].
   * It is allocated as [0:MAX_ND_STEPS-1]. 
   */
  public int 
    ndWedgeGrayValues[];	
  /** od value for corresponding wedge point [0:maxNDsteps-1].
   * It is allocated as [0:MAX_ND_STEPS-1]. 
   */
  public float 
    ndWedgeODvalues[];	
  /** eg. # of values in the Neutral density step wedge  */
  public int
    maxNDsteps; 	
  /** eg. # of gray value peaks corresponding to steps in the 
   * Neutral density step wedge. Note you can have 0 peaks
   * but a non-zero # of OD steps in an uncalibrated sample.
   */
  public int
    maxPeaks;
  /** the value of maxPeaks BEFORE running the peak finder */
  public int
    initialMaxPeaks;
  
  /** full name units used in the step wedge.
   * E.g., "Optical density"
   */
  public String
    units; 
  /** abbreviation of units name used in the step wedge
   * E.g., "od"
   */
  public String
    unitsAbbrev; 
  /** optional manufacturer part number for the step wedge */
  public String
    manufacturerPartNbr; 
  
  /** histogram of pixel data in ND computing window of [0:255] */
  public int
    hist[]= null;
  /** Had a valid ND wedge ROI when came into the wizard */
  public boolean
    hasPrevCALflag;
  /** ULHC Neutral Density calibration strip computing window */
  public int
    ndcwx1, 
    ndcwy1;   
  /** LRHC Neutral Density calibration strip computing window */
  public int
    ndcwx2, 
    ndcwy2;
  
  /* Piecewise linear map of Gray to OD [0:255] if the calibration
   * exists. If it does not, then it is just a 1:1 mapping.
   */
  public float
    mapGrayToOD[]; 
  /** Indicates that there is an valid mapGrayToOD map
   * else if it was set to 1:1 the flag is set false.
   */
  public boolean
    hasODmapFlag;
  /** max gray value found in the image and it is <= 255 */
  public int 
    maxGrayValue;
  
  /** Image file associated with the .cal file. This extra info that 
   * we may not use, but that documents the .cal file.
   * The cal file key is "imageFile".
   */
  public String
     calImageFile;
  /** Date associated with the .cal file. This extra info that 
   * we may not use, but that documents the .cal file.
   * The cal file key is "date".
   */
  public String
     calFileDate;
				    

  /**
   * CalibrateOD() - constructor to set up the initial map.
   * Note: use setMapGrayToOD() to force a particular calibration map.
   * Use setNDwedgeTable() to force the calibration input data prior
   * to extrapolation. Use extrapolateNDwedgeMap() to extrapolate the
   * calibration into the map.
   * @param flk is instance of Flicker
   * @param maxGray is maximum number of gray values possible in image
   * @see #clean
   * @see #setMapGrayToOD
   * @see #setNDwedgeTable
   */
  public CalibrateOD(Flicker flk, int maxGray)
  { /* CalibrateOD */
    this.flk= flk;
    util= flk.util;
    
    clean();                         /* clean up the state */   
       
    /* The map does not exist, so make a (MAX_GRAY+1) level 1:1 map. */
    setMapGrayToOD(null,maxGray);
  } /* CalibrateOD */
  
  
  /**
   * CalibrateOD() - constructor clone a CalibrateOD instance when you want
   * to save the clone state. Use restoreFromClone() to restore it.
   * @param clone is the CalibrateOD instance to clone in the new instance
   * @see #restoreFromClone
   */
  public CalibrateOD(CalibrateOD clone)
  { /* CalibrateOD */
    if(clone!=null)
      this.restoreFromClone(clone);
  } /* CalibrateOD */ 
  
  
  /**
   * CalibrateOD() - constructor to set up the initial map.
   * Note: use setMapGrayToOD() to force a particular calibration map.
   * Use setNDwedgeTable() to force the calibration input data prior
   * to extrapolation. Use extrapolateNDwedgeMap() to extrapolate the
   * calibration into the map.
   * @param maxGray is the maximum number of gray values possible in image
   * @see #clean
   * @see #setMapGrayToOD
   * @see #setNDwedgeTable
   */
  public CalibrateOD(int maxGray)
  { /* CalibrateOD */    
    clean();                         /* clean up the state */
    
    /* default to (MAX_GRAY+1) gray levels if not defined as a place holder */
    maxGrayValue= (maxGray>0) ? maxGray : 255;  
    mapGrayToOD= new float[MAX_GRAY+1];
    
    /* Setup default 1:1 piecewise linear map of Gray to OD
     * over the range [0:MAX_GRAY]. 
     * Note, we do not deal with the black is zero problem here.  We 
     * assume the data has been mapped so white is 0 and black is 255.
     */    
     for(int g= 0; g<=MAX_GRAY; g++)
       this.mapGrayToOD[g]= g;      /* else (255-i) if !blackIsZeroFlag */   
  } /* CalibrateOD */
  
  
  /**
   * clean() - clean up before init or when kill instance before GC
   */
  public void clean()
  { /* clean */
    ndcwx1= -1;
    ndcwy1= -1;
    ndcwx2= -1;
    ndcwy2= -1;
    hasPrevCALflag= false;
  
    mapGrayToOD= null;
    hasODmapFlag= false;
    maxGrayValue= 0;
        
    /* Set default */
    units= "gray-value";
    unitsAbbrev= "gray";
    manufacturerPartNbr= "none";    
    
    ndWedgeGrayValues= new int[MAX_ND_STEPS];
    ndWedgeODvalues= new float[MAX_ND_STEPS];
    maxNDsteps= 0;
    maxPeaks= 0;
    initialMaxPeaks= 0;
    
    hist= null;
  } /* clean */
    
  
  /**
   * restoreFromClone() - restore this. instance from a CalibrateOD clone
   * @param clone is the CalibrateOD clone
   */
  public boolean restoreFromClone(CalibrateOD clone)
  { /* restoreFromClone */  
    if(clone==null)
      return(false);
    
    ndWedgeGrayValues= new int[MAX_ND_STEPS];
    ndWedgeODvalues= new float[MAX_ND_STEPS];
    for(int i=0;i<MAX_ND_STEPS;i++)
    { /* Copy by VALUE! */
      ndWedgeGrayValues[i]= clone.ndWedgeGrayValues[i];
      ndWedgeODvalues[i]= clone.ndWedgeODvalues[i];
    }
    
    maxNDsteps= clone.maxNDsteps;
    maxPeaks= clone.maxPeaks;
    initialMaxPeaks= clone.initialMaxPeaks;
    
    units= new String(clone.units);
    unitsAbbrev= new String(clone.unitsAbbrev); 
    manufacturerPartNbr= new String(clone.manufacturerPartNbr); 
    
    if(clone.hist!=null)
    {      
      hist= new int[MAX_GRAY+1];
      for(int i=0;i<MAX_GRAY;i++)
        hist[i]= clone.hist[i];
    }
    else
      hist= null;
    
    ndcwx1= clone.ndcwx1; 
    ndcwy1= clone.ndcwy1;   
    ndcwx2= clone.ndcwx2; 
    ndcwy2= clone.ndcwy2;
    hasPrevCALflag= clone.hasPrevCALflag;
    mapGrayToOD= new float[MAX_GRAY+1];
    for(int i=0;i<=MAX_GRAY;i++)
    { /* Copy by VALUE! */
      mapGrayToOD[i]= clone.mapGrayToOD[i]; 
    }
    hasODmapFlag= clone.hasODmapFlag;
    maxGrayValue= clone.maxGrayValue;
    
    return(true);
  } /* restoreFromClone */ 
  
  
  /**
   * setWedgeROI() - set ND step Wedge region of interest
   * @param roiX1 - ULHC x coordinate
   * @param roiY1 - ULHC y coordinate
   * @param roiX2 - LRHC x coordinate
   * @param roiY2 - LRHC y coordinate
   */
  public void setWedgeROI(int roiX1, int roiY1, int roiX2, int roiY2)
  {
    this.ndcwx1= roiX1;
    this.ndcwy1= roiY1;
    this.ndcwx2= roiX2;
    this.ndcwy2= roiY2;
  }
    
  
  /**
   * isValidWedgeROI() - check if Wedge ROI is valid
   * @return true if valid
   */
  public boolean isValidWedgeROI()
  {
    boolean flag= (ndcwx1>=0 && ndcwx1<ndcwx2 && ndcwy1>=0 && ndcwy1<ndcwy2);
    return(flag);
  }
  
  
  /**
   * setDefaultUnits() - set the default to units to "Optical density",
   * unitsAbbrev to "od" and unitsManufacturerPartNbr to "< opt. part # >".
   */
  public void setDefaultUnits() 
  { /* setDefaultUnits */
    setUnits("Optical density");
    setUnitsAbbrev("od");      
    setUnitsManufacturerPartNbr("<opt. part #>");
  } /* setDefaultUnits */
  
  
  /**
   * setUnits() - set Wedge units, e.g. "Optical density", 
   * "Counts Per Minute", etc.
   * @param units to use. It is "gray-value" if never defined.
   */
  public void setUnits(String units)
  { this.units= units; }
    
  
  /**
   * getUnits() - get Wedge units, e.g. "Optical density", 
   * "Counts Per Minute", etc.
   * @return units, it is "gray-value" if never defined.
   */
  public String getUnits()
  { return(units); } 
  
  
  /**
   * setUnitsAbbrev() - set Wedge units abbreviation, e.g. "od",
   * "CPM", etc.
   * @param units to use. It is "gray-value" if never defined.
   */
  public void setUnitsAbbrev(String unitsAbbrev)
  { this.unitsAbbrev= unitsAbbrev; }
    
  
  /**
   * getUnitsAbbrev() - get Wedge units abbreviation, e.g. "od",
   * "CPM", etc.
   * @return unitsAbbrev, it is "gray" if never defined.
   */
  public String getUnitsAbbrev()
  { return(unitsAbbrev); }
    
  
  /**
   * setUnitsManufacturerPartNbr() - set Wedge manufacturerPartNbr
   * @param units to use. It is "gray-value" if never defined.
   */
  public void setUnitsManufacturerPartNbr(String manufacturerPartNbr)
  { this.manufacturerPartNbr= manufacturerPartNbr; }
    
  
  /**
   * getUnitsManufacturerPartNbr() - get Wedge manufacturerPartNbr
   * @return unitsAbbrev, it is "gray" if never defined.
   */
  public String getUnitsManufacturerPartNbr()
  { return(manufacturerPartNbr); }
  
  
  /**
   * setMapGrayToOD() - change the mapGrayToOD to the specified
   * Gray to OD map.  However, if the map does not exist then
   * make a (MAX_GRAY+1) level 1:1 map.
   * @param newGrayToODMap is the new map [0:maxGray] or null
   * @param maxGray is the number of gray values in the 1:1 map to
   *        create if the newGrayToODMap is null.
   */
  public void setMapGrayToOD(float newGrayToODMap[], int maxGray)
  { /* setMapGrayToOD */
     mapGrayToOD= newGrayToODMap; 
     maxGrayValue= (newGrayToODMap==null) ? 0 : newGrayToODMap.length-1;
     
     if(mapGrayToOD==null)
     { /* Map does not exist - make a (MAX_GRAY+1) level 1:1 map */
       maxGrayValue= (maxGray>0) ? maxGray : 255;
       mapGrayToOD= new float[MAX_GRAY+1];
       
       /* Setup default 1:1 piecewise linear map of Gray to OD
        * over the range [0:MAX_GRAY].
        * Note, we do not deal with the black is zero problem here.  We
        * assume the data has been mapped so white is 0 and black is
        * MAX_GRAY.
       */
       for(int g= 0; g<=MAX_GRAY; g++)
         this.mapGrayToOD[g]= g;      /* else (255-i) if !blackIsZeroFlag */
       hasODmapFlag= false;
     }
     else 
       hasODmapFlag= true;
  } /* setMapGrayToOD */
    
  
  /**
   * getMapGrayToOD() - return the mapGrayToOD[0:maxGray-1].
   * @return mapGrayToOD map.
   */
  public float[] getMapGrayToOD()
  { return(mapGrayToOD);  }
  
  
  /**
   * getHasODmapFlag() - return the status of the Gray to OD map.
   * @return true if it exists
   */
  public boolean getHasODmapFlag()
  { return(hasODmapFlag); }
  
    
  /**
   * getGrayValueTable() - get Wedge calibration grayscale[0:maxPeaks-1]
   * @return gray value list from gray to OD calibration
   */
  public int[] getGrayValueTable()
  { return(ndWedgeGrayValues); }
  
  
  /**
   * getODtable() - get Wedge calibration odTable[0:maxNDsteps-1] OD values
   * @return OD list values
   */
  public float[] getODtable()
  { return(ndWedgeODvalues); }
    
  
  /**
   * setNDwedgeTable() - set ND step wedge calibration set of 
   * (grayscale,OD) wedge values, # of steps and max grayvalue
   * [CHECK] we may want to copy the data to fixed size
   * arrays [0:MAX_ND_STEPS-1].
   * @param ndWedgeGrayValues of calibration step wedge [0:maxNDsteps-1]
   * @param ndWedgeODvalues of calibration step wedge [0:maxNDsteps-1]
   * @param maxNDsteps of Step wedge calibration
   * @param maxGrayValue is max grayvalue of calibration
   */
  public void setNDwedgeTable(int ndWedgeGrayValues[], 
                              float ndWedgeODvalues[],
                              int maxNDsteps, 
                              int maxPeaks, 
                              int maxGrayValue)
  { /* setNDwedgeTable */
    if(ndWedgeGrayValues==null)
      this.ndWedgeGrayValues= new int[MAX_ND_STEPS];
    else if(MAX_ND_STEPS==ndWedgeODvalues.length)
      this.ndWedgeGrayValues= ndWedgeGrayValues;
    else
    { /* values in a new array */
      this.ndWedgeGrayValues= new int[MAX_ND_STEPS];
      int n= Math.min(MAX_ND_STEPS, ndWedgeGrayValues.length);
      for(int i= 0;i<n;i++)
        this.ndWedgeGrayValues[i]= ndWedgeGrayValues[i];
    }
    
    if(ndWedgeODvalues==null)
      this.ndWedgeODvalues= new float[MAX_ND_STEPS];
    else if(MAX_ND_STEPS==ndWedgeODvalues.length)
      this.ndWedgeODvalues= ndWedgeODvalues;
    else
    { /* values in a new array */
      this.ndWedgeODvalues= new float[MAX_ND_STEPS];
      int n= Math.min(MAX_ND_STEPS, ndWedgeODvalues.length);
      for(int i= 0;i<n;i++)
        this.ndWedgeODvalues[i]= ndWedgeODvalues[i];
    }
    
    this.maxNDsteps= maxNDsteps;
    this.maxPeaks= maxPeaks;
    this.maxGrayValue= maxGrayValue;
  } /* setNDwedgeTable */
  
      
  /**
   * findPeakTableSizes() - find the current maxPeaks and maxNDvalues
   * from non-zero values in the peaks table.
   * @return true if maxNDvalues and maxPeaks are > 0.
   */
  public boolean findPeakTableSizes()
  { /* findPeakTableSizes */
    //int
      //oldMaxPeaks= maxPeaks,
      //oldMmaxNDsteps= maxNDsteps;
      
     maxPeaks= 0;
     maxNDsteps= 0;
     for(int i= 0;i<MAX_ND_STEPS;i++)
     {
       if(ndWedgeODvalues[i] > 0.0F)
         maxNDsteps++;
       if(ndWedgeGrayValues[i] >0)
         maxPeaks++;
     }
     
     if(Flicker.NEVER)
     { /* allow 0 data in first entry for each array */
       /* Handle special case where first peak is at 0 */
       if(maxPeaks>0 && ndWedgeGrayValues[0]==0)
         maxPeaks++;
       /* Handle special case where first OD value is at 0.0 */
       if(maxNDsteps>0 && ndWedgeODvalues[0]==0.0F)
         maxNDsteps++;
     }
     boolean flag= (maxNDsteps>0 && maxPeaks>0);
     
     return(flag);
  } /* findPeakTableSizes */
  
  
  /**
   * calcCalib() - continuous piecewise linear calibration od(gray)
   * @return piecewise linear OD calibration, null if failed.
   * [TODO]
   */
  public float[] calcCalib()
  { /* calcCalib */
    if(maxNDsteps==0 || maxPeaks==0 || maxGrayValue==0)
      return(null);
    
    if(mapGrayToOD==null)
      mapGrayToOD= new float[MAX_GRAY+1];
        
    return(mapGrayToOD);
  } /* calcCalib */
  
    
  /**
   * extrapolateNDwedgeMap() - extrapolate the maxNDsteps ND wedge peaks
   * in ndWedgeODvalues[0:maxNDsteps-1] with corresponding gray values
   * ndWedgeGrayValues[0:maxNDsteps-1] into a piecewise linear
   * mapGrayToOD[0:maxGray] array.  If the Wedge data is not well formed,
   * the mapGrayToOD[0:maxGray] contains a 1:1 mapping of grayscale data.
   * Otherwise, it computes the PWL interpolation of Grayvalue to OD between
   * successive peaks.
   *<P>
   * NOTE: Use setNDwedgeTable() to set the calibration input data prior
   * to doing the extrapolation.
   *<P>
   * This code was drived from GELLAB-II, Lemkin etal., NCI.
   * @return null if succeed and setup the mapGrayToOD [0:maxGray] 
   *        lookup table in the class instance. If not null, it is
   *        the error message. 
   * @see #setNDwedgeTable
   */
  public String extrapolateNDwedgeMap()
  { /* extrapolateNDwedgeMap */
     String sOK= extrapolateNDwedgeMap(maxGrayValue, maxNDsteps,
                                       maxPeaks,
                                       ndWedgeGrayValues,
                                       ndWedgeODvalues);
     return(sOK);
  } /* extrapolateNDwedgeMap */
  
    
  /**
   * extrapolateNDwedgeMap() - extrapolate the maxNDsteps ND wedge peaks
   * in ndWedgeODvalues[0:maxNDsteps-1] with corresponding gray values
   * ndWedgeGrayValues[0:maxNDsteps-1] into a piecewise linear
   * mapGrayToOD[0:maxGray] array.  If the Wedge data is not well formed,
   * the mapGrayToOD[0:maxGray] contains a 1:1 mapping of grayscale data.
   * Otherwise, it computes the PWL interpolation of Grayvalue to OD between
   * successive peaks.
   *<P>
   * This code was drived from GELLAB-II, Lemkin etal., NCI.
   * @param maxGray is max # of gray values (i.e. MAX_GRAY)
   * @param maxNDsteps is number of OD steps
   * @param maxPeaks is current number of peaks
   * @param ndWedgeGrayValues is the gray value peak [0:maxPeaks -1]
   * @param ndWedgeODvalues is the OD wedge value [0:maxNDsteps -1]
   * @return null if succeed and setup the mapGrayToOD [0:maxGray] 
   *        lookup table in the class instance. If not null, it is
   *        the error message. 
   */
  public String extrapolateNDwedgeMap(int maxGray, int maxNDsteps,
                                      int maxPeaks,
                                      int ndWedgeGrayValues[],
                                      float ndWedgeODvalues[])
  { /* extrapolateNDwedgeMap */
    if(this.mapGrayToOD==null)
      this.mapGrayToOD= new float[MAX_GRAY+1];   /* reallocate */
    
    /* use values passed via the arg list */
    if(ndWedgeGrayValues==null)
    {
      maxNDsteps= this.maxNDsteps;
      ndWedgeGrayValues= this.ndWedgeGrayValues; /* use previous value */
    }
    
    if(ndWedgeODvalues==null)
    {
      maxPeaks= this.maxPeaks;
      ndWedgeODvalues= this.ndWedgeODvalues;     /* use previous value */
    }
    
    /* Test for ill-formed data.
     * [NOTE] DO NOT change the prefixes in the following messages
     * "Bad xxx Data -" since this is used by the calling method for
     * error analysis.
     */
    if(maxNDsteps==0)
    { /* failed */
      hasODmapFlag= false;      
      return("Bad OD Data - # calibration steps is 0.");
    }
    
    if(maxPeaks==0)
    { /* failed */
      hasODmapFlag= false;      
      return("Bad gray-peak Data - # gray scale values is 0.");
    }
        
    if(maxNDsteps>ndWedgeODvalues.length)
    { /* failed */
      hasODmapFlag= false;      
      return("FATAL error - maxNDsteps("+maxNDsteps+
             ") > | ndWedgeODvalues | "+ ndWedgeODvalues.length);
    }
    
    /* [CHECK] we should not be checking the size of the GrayValue list
     * since it may change when we add / delete peaks.
     */
    if(maxPeaks>ndWedgeGrayValues.length)
    { /* failed */
      hasODmapFlag= false;      
      return("FATAL error - maxPeaks("+maxPeaks+
             ") > | ndWedgeGrayValues | "+ ndWedgeGrayValues.length);
    }
    
    /* Make sure the OD data is monotonic */
    for(int i= 1;i<maxNDsteps;i++)
      if(ndWedgeODvalues[i-1] > ndWedgeODvalues[i])
      { /* failed */
        hasODmapFlag= false;
        return("Bad OD data - all calibration data must be monotonically increasing");
      }
    
    /* Make sure the Grayscale data is monotonic */
    for(int i= 1;i<maxPeaks;i++)
      if(ndWedgeGrayValues[i-1] > ndWedgeGrayValues[i])
      { /* failed */
        hasODmapFlag= false;
        return("Bad gray-peak data - all peak data must be monotonically increasing");      
      }
    
    int
      g,
      peaka, 
      peakb;
    double
      m,              /* slope */
      b,              /* intercept */
      bOld= 0.0,      /* intercept for last actual wedge interp segment DBUG */
      mOld= 0.0,      /* slope for the last actual wedge interp segment */
      od,
      oda,
      odb= 0.0;    
    
    /* Compute the gray to OD map segment for each pair of ND steps and 
     * handle the two end points. 
     * [TODO] could improve on the first segment from peaka==0, if
     * we went back and used the slope for segment 2 to extrapolate 
     * the actual od intercept rather than forcing it to 0.0.
     */
    int nSegments= Math.min(maxNDsteps,maxPeaks);
    for (int i=0;i<=nSegments;i++)
    { /* Do a piecwise linear section */
      /* Get Grayscale peak values handling the 0 & maxGray endpoints */
      peaka= (i==0) ? 0 : ndWedgeGrayValues[i-1];
      peakb= (i==nSegments) ? maxGray : ndWedgeGrayValues[i];
      
      /* Get the corresponding OD calibration values */
      oda= (i==0) ? 0.0 : ndWedgeODvalues[i-1];
      odb= (i==nSegments) ? -1.0F : ndWedgeODvalues[i];
            
      /* Solve for (m,b) for 'od= m*g+b' */
      if (i==nSegments)	/* handle special end point case */
      { /* Just use the slope from the last segment */
        m= mOld;
      } 
      else
      { /* Solve for the initial PWL segments */
        m= (odb-oda)/(peakb-peaka);
        mOld= m;                /* save slope for the last PWL segment */        
      }
      
      /* Solve for b */
      b= oda-(m*peaka); 
      if (i==nSegments) 	/* handle special end point cases */
        bOld= b;                /* save slope for the last PWL segment [CHECK] */
      
      /* Extrapolate values between peaks */
      for (g=peaka; g<=peakb; g++)
      { /* Calc (od = g*m+b) as P.W.L. function of gray value */
        /* Handle saturated data by just mapping to the same OD */
        if(peakb==peaka && i>0)       
          od= mapGrayToOD[peaka]; /* since saturated, used previous value */
        else
          od= (m*g)+b;            /* use P.W.L. extrapolation */
        
        od= Math.max(od, 0.0);    /* clip to positive value */
        this.mapGrayToOD[g]= (float)od;        
      } /* Calc ND as P.W. linear fn of gray value */
    } /* do a piecwise linear section */
    
    hasODmapFlag= true;
    return(null);	           /* return null if no errors */
  } /* extrapolateNDwedgeMap */
  
    
  /**
   * findPeaks() - Find the peaks in the histogram sHist[]. 
   * This is used  for finding the gray scale values for the ND wedge peaks.
   * Set the peaks (indices of the sHist[]) into histPeaksFound[0:#peaks-1].
   *<BR>
   * NOTE this assumes a decreasing distance between the peaks for 
   * higher gray values indices.
   *<BR>
   * This code was drived from GELLAB-II, Lemkin et al., NCI.
   * NOTE: This was optimized for scans from the RTPP and probably
   * needs to be retuned when looking at different data.
   *<PRE>
   * Suggested parameter values:
   *   startRange                  = 15   
   *   avgDist                     = 3
   *   minDist                     = 5
   *   lookBackWidth=              = 2
   *   freqStoN                    = 10.0
   *   minHistFreqPeakValue = 100 (for CCD camera, 20 for VIDICON)
   *   smoothpeakIdxListFlag     = true
   *   useShrinkingMinDistanceFlag = true
   *</PRE>
   *<P>
   * @param maxGray is max gray value in the histogram
   * @param maxPeaksAllowed is max # steps allowed 
   * @param sHist is the smoothed histogram of the data [0:maxGray-1].
   *        For example, you could use the smoothHistogram() method
   *        to smooth them.
   * @param startRange is 1st gray value to search from. If was 0, 
   *        then we might pick up false noise peaks
   * @param avgDist is expected distance between peaks in sHist[]
   * @param minDist is minimum distance allowed between peaks in sHist[]
   * @param lookBackWidth is the number of bins to look back in sHist[] 
   *        when tracking a peak so as to ignore a few noisy data when 
   *        "climbing" a peak. The min is 1, max is minDist-1.
   * @param freqStoN is the frequency signalToNoise (S/N) used in computing 
   *        the minHistFreqPeakValue if the user specified it as > 0.0.
   *        If the user specifies 1.0, it defaults freqStoN is 10.0. 
   *        I.e., if maxFreq is 400, then the default estimated 
   *        minHistFreqPeakValue is computed as 40.
   *        The actual value of minHistFreqPeakValue used is the max of
   *        the S/N estimated value and the value specified by the user
   *        in the next parameter.
   * @param minHistFreqPeakValue is minimum histogram peak value for it to
   *        be considered as a peak
   * @param smoothpeakIdxListFlag is do local smoothing optimization
   * @param useShrinkingMinDistanceFlag to shrink the minimum distance
   *        between peaks as we find more peaks in going from min gray
   *        to max gray values. We may need this for some scanner data
   *        (e.g., CCD scanners since they a log response).
   * @return list of gray value peaks in the histogram histPeaksFound[]  
   */
  public int[] findPeaks(int maxGray, int maxPeaksAllowed, 
                         int sHist[], int startRange,
                         int avgDist, int minDist, int lookBackWidth,
                         float freqStoN, int minHistFreqPeakValue,
                         boolean smoothpeakIdxListFlag,
                         boolean useShrinkingMinDistanceFlag )
  { /* findPeaks */
    int
      i,
      freq;                           /* frequency entry for 
                                       * histogram gray value hist[i] */
         
    if(maxPeaksAllowed<=0)
      maxPeaksAllowed= 50;
    startRange= Math.max(1, startRange);
    int peakIdx[]= new int[maxPeaksAllowed];
    
    peakIdx[0]= 0;                    /* set to save the first peak */
    
    float
      sum,
      weightedSum,
      freqRange;    
    
    /* Compute the max signal/noise */
    int
      minFreq= 100000000,
      maxFreq= -1;
    
    /* find min and max frequencies */
    for (i=startRange; i<=maxGray; i++)
    {
      freq= sHist[i];
      if(freq==0)
        continue;
      minFreq= Math.min(freq,minFreq);
      maxFreq= Math.max(freq,maxFreq);
    }
    freqRange= (float)maxFreq/(float)minFreq;
       
   /* Try to use the S/N value if S/N > 0.0.
    * Use the maximum minValue. The minValueStoN is the
    * estimated minValue computed from the maxFreq and the S/N.
    */
    if(freqStoN==1.0F)
      freqStoN= 10.0F;       /* default S/N if it is 1.0 */
   int 
     minValueStoN= (freqStoN>0.0F) ? (int)(maxFreq/freqStoN) : 1,
     minValue= Math.max(minHistFreqPeakValue, minValueStoN);
    
    /* The look back distance MUST be < the minDist */
    lookBackWidth= Math.max(1, Math.min((minDist-1), lookBackWidth));
    
    if(DBUG_CALIB)
      System.out.println("FP.0 minHistFreqPeakValue="+minHistFreqPeakValue+
                         " freqStoN="+(int)freqStoN+"/1 lookBackWidth="+
                         lookBackWidth+"\n   minFreq="+minFreq+
                         " maxFreq="+maxFreq+
                         " freqRange="+(int)freqRange+
                         "/1 minValue="+minValue+" minDist="+minDist+
                         "\n\n");

    int
      k= 0,                            /* peak counter */
      n,                               /* look back index */
      lastVal= 0,
      lastIdx= -(1+minDist);     
    boolean
      isHigher,                       /* i.e. (freq >= lastVal) */
      insideLBW;                      /* inside Look Back Window */
    
    /* Process all histogram values within the range. */
    for (i=startRange; i<=maxGray; i++)
    { /* scan */
      /* [1] Process histogram gray values where the frequency 
       * is > minVal allowed.
       */
      freq= sHist[i];
      if(DBUG_CALIB)
        System.out.println("FP.1 sHist["+i+"]="+freq+" k="+k);
      if (freq<minValue)
        continue;                      /* it is too small - probably noise*/
      
       /* [2] Test if start looking for a new k'th peak maximum */
      if (((i-lastIdx) > minDist) && (freq > sHist[i-1]))
      { /* start looking for a new k'th peak maximum */
        if (k>=maxPeaksAllowed)
          break;                       /* Found maximum # peaks desired */
        ++k;
        lastIdx= i;
        lastVal= freq;
        
      if(DBUG_CALIB)
        System.out.println("FP.2 k="+k+" lastIdx="+lastIdx+
                           " lastVal="+lastVal);
      } /* start looking for a new k'th peak maximum */
      
      /* [3] Test if we are still in the peak lookback window. */
      insideLBW= false;
      for(n=(i-1); ((n>=0) && (n>=(i-lookBackWidth))); n--)
        if(peakIdx[k-1]==n || lastIdx==i)
        {
          insideLBW= true;
          break;
        }     
      isHigher= (freq >= lastVal);
      if(DBUG_CALIB)
        System.out.println("FP.3 lastIdx="+lastIdx+" lastVal="+lastVal+
                           " peakIdx[k-1]="+peakIdx[k-1]+
                           " isHigher="+isHigher+" insideLBW="+insideLBW);
      
      /* [4] Test if track the k'th peak if we are higher than
       * the previous sHist[] value and if we are close enough.
       */
      if ((freq >= lastVal) && k>0 && (lastIdx==i || insideLBW))
      { /* track the new peak maximum since larger than last value */
        lastIdx= i;
        lastVal= freq;    
        peakIdx[k-1]= i;       
        
      if(DBUG_CALIB)
        System.out.println("FP.4 *** lastIdx="+lastIdx+" lastVal="+lastVal+
                           " updated peakIdx[k-1]="+peakIdx[k-1]+
                           " sHist["+i+"]="+freq);
      }  /* track the new peak maximum since larger than last value */
      
      /* [4] Heuristic: Change minDist based on the # of peaks 
       * found so far depending on the non-linearity of the distance
       * between peaks.
       * [REDO] - this was designed for old GELLAB-II CCD camera...
       */
      if(useShrinkingMinDistanceFlag)
      { /* Change minDist based on the # of peaks found so far */
        if(peakIdx[k-1] == 5)
          minDist= 4;
        if(peakIdx[k-1] == 7)
          minDist= 3;
        if(peakIdx[k-1] == 9)
          minDist= 2;
      } /* Change minDist based on the # of peaks found so far */
    } /* scan */
    
    /* [5] Heuristic: Optimize the peaks by taking weighted values 
     * if they are too close.
     * [CHECK] and redo algorithm if needed....
     */
    int histPeaksFound[]= new int[k];  /* return array of exact size k */
    if (smoothpeakIdxListFlag)
    { /* smooth peak list */
      for (i=1;i<=k;i++)
      { /* smooth peak i */
        int 
          j= peakIdx[i],
          j1= j-avgDist,
          j2= j+avgDist;
        if (j1<0)
          j1= 0;
        
        weightedSum= 0;
        sum= 0;
        for (j=j1;j<=j2;j++)
        { /* compute local sum */
          freq= sHist[j];
          sum += freq;
          weightedSum += freq*j;
        } /* compute local sum */
        
        histPeaksFound[i]= (int)(weightedSum/sum);
      } /* smooth peak i */
    } /* smooth peak list */
    
    else
    { /* just return the peak list */
      histPeaksFound= new int[k];
      for(i=0;i<k;i++)
        histPeaksFound[i]= peakIdx[i];
    }
    
    return(histPeaksFound);
  } /* findPeaks */
  
    
  /**
   * smoothHistogram() - Compute smoothed histogram of hist[0:maxGray]
   * into hData[0:maxGray] using 'Disaster Analysis'.
   *<P>
   * This code was drived from GELLAB-II, Lemkin etal., NCI.
   * @param hDataOrig histogram  of size [0:maxGray] to be smoothed. 
   * @param nTimes is number of times to iterate
   * @param windowWidth smoothing window width
   * @param noiseThreshold noise threshold to ensure just find the peaks
   * @return the smoothed histogram [0:maxGray]
   */
  public int[] smoothHistogram(int hDataOrig[], int nTimes,
                               int windowWidth, int noiseThreshold)
  { /* smoothHistogram */
    if(hDataOrig==null)
      return(null);
    
    int
      wSize= 40,
      wSize2= wSize/2,
      histGuard= 2*wSize+1,
      maxGray= hDataOrig.length, 
      i,
      j,
      g,
      w1,
      w2;
    float
      delta,
      sum,
      noise,
      sumW,
      avg;
    int
      hData[]= new int[maxGray+histGuard],   /* [0:maxGray] */
      sHist[]= new int[maxGray+histGuard],   /* [0:maxGray+histGuard] */
      hist[]= new int[maxGray+histGuard];    /* [0:wSize] */
    
    /* [1] Initialize */
    nTimes= Math.max(1,nTimes);
    windowWidth= Math.max(1,windowWidth);
    delta= (int)(0.001F*noiseThreshold);    /* set noise threshold */
    
    /* Copy the data into local working arrays */
    for (i=0;i<maxGray;i++)
    { /* copy the data */
      hData[i]= hDataOrig[i];
      sHist[i]= hDataOrig[i];	                   
    }    
    
    /* [2] Smooth hData[] nTimes */
    for (int n=1;n<=nTimes;n++)
    { /* Smooth iteration n */
      sum= 0;
      for(i=0;i<maxGray;i++)
        sum += hData[i+wSize];             /* Sum all histogram */      
      
      /* [2.1] Set up endpoint approximations */
      for (i=0;i<maxGray;i++)
      {
        sHist[i]= hData[i];
        hist[i+wSize]= hData[i];
      }
      for (i= -wSize2;i>=1;i++)
        hist[i+wSize]= hData[0];
      for (i=maxGray+1;i<maxGray+wSize2;i++)
        hist[i+wSize]= hData[maxGray];
      
      /* [2.2] Compute smoothed histogram and find greatest peak */
      for (i=0;i<=maxGray;i++)
      { /* smooth point */
        sumW= 0;                           /* Compute average width w */
        w1= i-windowWidth/2;
        w2= i+windowWidth/2;
        for (j=w1;j<=w2;j++)
          sumW +=  hist[j+wSize];
        avg= sumW/windowWidth;
        
        noise= (int)((100.0*Math.abs(hist[i+wSize]-avg))/sum);
        
        if (noise>delta)
          g= (int)((sumW-hist[i+wSize])/(windowWidth-1));
        else 
          g= hist[i+wSize];
        
        sHist[i]= g;
        hData[i]= g;                       /* restore histogram value */
      } /* smooth point */
    } /* Smooth iteration n */
    
    /* Return array of exact size k */
    int tmp[]= new int[maxGray];
    for(i=0;i<maxGray;i++)
      tmp[i]= sHist[i];
    sHist= tmp;
    
    return(sHist);
  } /* smoothHistogram */

        
 /**
  * readState() - Read calibration state from .cal state file for this image.
  */ 
  void readState()
  { /* readState */
    /* read extra stuff that we may not use, but that docs the .cal */
     calImageFile= util.getStateValue("imageFile", "");
     calFileDate= util.getStateValue("date", "");
    
     units= util.getStateValue("units", "");
     unitsAbbrev= util.getStateValue("unitsAbbrev", "");
     manufacturerPartNbr= util.getStateValue("manufacturerPartNbr", "");
     ndcwx1= util.getStateValue("ndcwx1", 0);
     ndcwy1= util.getStateValue("ndcwy1", 0);
     ndcwx2= util.getStateValue("ndcwx2", 0);
     ndcwy2= util.getStateValue("ndcwy2", 0);
     
     maxNDsteps= util.getStateValue("maxNDsteps", 0);
     maxPeaks= util.getStateValue("maxPeaks", 0);
     maxGrayValue= util.getStateValue("maxGrayValue", 0);
     hasODmapFlag= util.getStateValue("hasODmapFlag", false);
     
     ndWedgeODvalues= new float[MAX_ND_STEPS];
     for(int i=0;i<MAX_ND_STEPS;i++)
     {
       ndWedgeODvalues[i]=
           util.getStateValue("ndWedgeODvalues["+i+"]", 0.0F);
     } 
     
     ndWedgeGrayValues= new int[MAX_ND_STEPS];
     for(int i=0;i<MAX_ND_STEPS;i++)
     {
       ndWedgeGrayValues[i]=
              util.getStateValue("ndWedgeGrayValues["+i+"]", 0);
     } 
     
     mapGrayToOD= new float[MAX_GRAY+1];
     for(int i=0;i<=MAX_GRAY;i++)
     {
       mapGrayToOD[i]= util.getStateValue("mapGrayToOD["+i+"]", 0.0F);
     }
  } /* readState */

    
 /**
  * writeState() - Write this calibration state to string buffer sBuf
  * @param imageFile is the name of the image file
  * @param sBuf is the string buffer to write to.
  */ 
  void writeState(String imageFile, StringBuffer sBuf)
  { /* writeState */ 
    /* Write extra stuff that we may not read back, but that docs the .cal */
    sBuf.append("imageFile\t"+imageFile+"\n");
    sBuf.append("date\t"+util.dateStr()+"\n");
    
    sBuf.append("units\t"+units+"\n");
    sBuf.append("unitsAbbrev\t"+unitsAbbrev+"\n");
    sBuf.append("manufacturerPartNbr\t"+manufacturerPartNbr+"\n");
    sBuf.append("ndcwx1\t"+ndcwx1+"\n");
    sBuf.append("ndcwy1\t"+ndcwy1+"\n");
    sBuf.append("ndcwx2\t"+ndcwx2+"\n");
    sBuf.append("ndcwy2\t"+ndcwy2+"\n");
    
    sBuf.append("maxNDsteps\t"+maxNDsteps+"\n");
    sBuf.append("maxPeaks\t"+maxPeaks+"\n");
    sBuf.append("maxGrayValue\t"+maxGrayValue+"\n");
    sBuf.append("hasODmapFlag\t"+hasODmapFlag+"\n");
    
    for(int i=0;i<MAX_ND_STEPS;i++)
    {
      sBuf.append("ndWedgeODvalues["+i+"]\t"+ndWedgeODvalues[i]+"\n");
    }  
    for(int i=0;i<MAX_ND_STEPS;i++)
    {
      sBuf.append("ndWedgeGrayValues["+i+"]\t"+ndWedgeGrayValues[i]+"\n");
    }  
    for(int i=0;i<=MAX_GRAY;i++)
    {
      sBuf.append("mapGrayToOD["+i+"]\t"+mapGrayToOD[i]+"\n");
    }      
   
  } /* writeState */
  
    
 /**
  * getStateStr() - get this calibration state as a string
  * @param iData is the image data to use
  * @param fileName associated with the iData
  * @return state string
  */ 
  String getStateStr(ImageData iData, String fileName)
  { /* getStateStr */ 
    String sR= "State calibration values for image '"+fileName+"'\n";
    sR += "   # calibration data in step wedge = "+maxNDsteps+"\n";
    sR += "   # gray scale peaks in histogram = "+maxNDsteps+"\n";
    sR += "   max gray value = "+maxGrayValue+"\n";
    sR += "   units = '"+units+"'\n";
    sR += "   unitsAbbrev = '"+unitsAbbrev+"'\n";
    sR += "   manufacturerPartNbr = '"+manufacturerPartNbr+"'\n";
    sR += "   ndcwx1 = "+ndcwx1+"\n";
    sR += "   ndcwy1 = "+ndcwy1+"\n";
    sR += "   ndcwx2 = "+ndcwx2+"\n";
    sR += "   ndcwy2 = "+ndcwy2+"\n";
    for(int i=0;i<maxNDsteps;i++)
    {
      sR += "   od value["+i+"] = "+ndWedgeODvalues[i]+"\n";
    }
    
    for(int i=0;i<maxPeaks;i++)
    {
      sR += "   gray value["+i+"] = "+ndWedgeGrayValues[i]+"\n";
    }
    for(int i=0;i<maxGrayValue;i++)
    {
      sR += "   mapGrayToOD["+i+"] = "+mapGrayToOD[i]+"\n";
    }    
    
    return(sR);
  } /* getStateStr */
  

  /**
   * calcHistFindPeaksAndExtrapolate() - set up histogram, peaks and extrpolate
   * map for the current ROI. Only extrapolate the # of OD values > 0.0.
   * There may be more gray peaks than OD values, but leave them there
   * since we may want to delete false-peaks in the middle which would move
   * valid-peaks down (to the proper place) in the gray peaks list.
   * @return true if success, false if ND CW ROI does not exist.
   */
  public boolean calcHistFindPeaksAndExtrapolate(ImageData iData)
  { /* calcHistFindPeaksAndExtrapolate */  
    ImageDataROI idROI= iData.idROI;
    initialMaxPeaks= maxPeaks;
    
    /* [1] Enable ROI display */    
    DBUG_CALIB= false;
    hasPrevCALflag= idROI.isValidNDcalibCW();
    boolean
      hasCWflag= idROI.isValidCW();
    if(! hasPrevCALflag && !hasCWflag)
    { /* "Please assign Wedge calibration ROI in image first." */
      return(false);
    }
    
    /* [2] Make the ROI visible in the computing window */
    if(hasPrevCALflag)
    { /* Use the ND CW */
      idROI.setROI(ndcwx1, ndcwy1, ndcwx2, ndcwy2);   
      idROI.copyROI2CW(); 
      flk.viewRoiFlag= true;               /* force it to be true */
      flk.repaint();
    }
    else if (hasCWflag)
    { /* no ND CW, but has CW - set ND CW to the CW */
      setWedgeROI(idROI.cwx1, idROI.cwy1, idROI.cwx2, idROI.cwy2);
      flk.viewRoiFlag= true;               /* force it to be true */
      flk.repaint();
    }
                    
    /* [3] Get fresh data histogram into hist[] for the ND wedge
     * ROI region.
     */
    boolean ok= iData.idM.calcHistogram(ndcwx1, ndcwy1, ndcwx2, ndcwy2);      
    if(!ok)
      return(false);
    hist= iData.hist;
                    
    /* [4] Copy hist[] into sHist[] and calculate the 
     * max/min freq and gray values.
     */
    int sHist[]= new int[MAX_GRAY+1];       /* May smooth the copy */
    int
      totFreq= 0,
      minFreq= 100000000,
      maxFreq= 0,
      minG= MAX_GRAY+1,
      maxG= -1,
      v,
      g;
    for(g= 0;g<=MAX_GRAY;g++)
    {
      v= hist[g];
      sHist[g]= v;
      if(v!=0)
      {
        totFreq += v;
        minFreq= Math.min(minFreq,v);
        maxFreq= Math.max(maxFreq,v);
        minG= Math.min(minG,g);
        maxG= Math.max(maxG,g);
      }
    }    
    maxGrayValue= maxG;
    
    /* [5] Define default peak analyser parameters */
    if(!hasPrevCALflag && hasCWflag)
    { /* find the peaks and stuff them into the peak table */
      int
        maxGray= (MAX_GRAY+1),
        maxPeaksAllowed= 50,          /* should be 15 */
        startRange= 5,
        avgDist= 3,
        minDist= 5,                   /* 4? */
        lookBackWidth= 3,
        minHistFreqPeakValue= 30;
      float freqStoN= 10.0F;
      boolean
      smoothpeakIdxListFlag= false,
      useShrinkingMinDistanceFlag= false;
      int gelHist[]= sHist;
      /*
      int
        nTimes= 1,
        windowWidth= 2,
        noiseThreshold= 10;
      gelHist= smoothHistogram(sHist, nTimes, windowWidth,noiseThreshold);
      */
      
      /* [5.1] Find the peaks in the histogram */
      int peakList[]= findPeaks(MAX_GRAY, maxPeaksAllowed,
                                gelHist, startRange,
                                avgDist, minDist, lookBackWidth,
                                freqStoN, minHistFreqPeakValue,
                                smoothpeakIdxListFlag,
                                useShrinkingMinDistanceFlag );
      int nPeaksFound= peakList.length;
      
      /* [5.2] Copy peak data to calib state. */
      maxPeaks= Math.min(MAX_ND_STEPS,nPeaksFound);
      for(int i=0;i<MAX_ND_STEPS;i++)
      { /* copy peaks into working peak table and 0 terminate */
        ndWedgeGrayValues[i]= (i<nPeaksFound) ? peakList[i] : 0;
      }      
    } /* find the peaks and stuff them into the peak table */
           
    /* [6] Compute the number of OD values for the step wedge that
     * are > 0.0. Note: can not have an OD value <= 0.0.
     * Find the current maxPeaks and maxNDvalues from non-zero
     * values in the peaks table
     */
    findPeakTableSizes();
         
    /* [7] Extrapolate the wedge peak table to a mapGrayToOD[] map.
     * if(maxNDsteps===0) then we can not extrapolate since no OD values.
     */
    String sOK= extrapolateNDwedgeMap(MAX_GRAY, maxNDsteps, maxPeaks,
                                      ndWedgeGrayValues, ndWedgeODvalues);
     /* Change the units if successful */
    if(sOK!=null)
    {    
      String msg= sOK;
      if(sOK.startsWith("Bad OD data"))
        msg += ", re-edit the OD values in the table and try again.";
      else if(sOK.equals("Bad gray-peak data "))
        msg += ", re-edit the gray-peak values in the table and try again.";
      else if(sOK.equals("FATAL error"))
        msg += ", Contact us with this error message.";
      
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    
    return(true); 
  } /* calcHistFindPeaksAndExtrapolate */
  
     
  /**
   * inheritNDwedgeODvalues() - if current calibration does NOT have ND wedge
   * OD values, but the paired gel DOES, then inherit its OD wedge values
   * and units to avoid having to type it.
   * @param iData is the current gel
   * @return true if we are using the inherited OD data
   */
  public boolean inheritNDwedgeODvalues(ImageData iData)
  { /* inheritNDwedgeODvalues */  
    boolean inheritODflag= false;
    
    /* Check for valid calibration data in paired ImageData */
    ImageData iDataOther= (iData==flk.iData2) 
                             ? flk.iData1 : flk.iData2;
             
    /* If the other gel has a calibration peak table  AND this gel
     * does NOT, then copy the OD values to this gel since they will 
     * probably be scanned from the same scanner and wedge. This will
     * save time in re-typing it in.
     */
    if(!iData.hasODmapFlag && iDataOther.hasODmapFlag && 
       iDataOther.calib.maxNDsteps>0)
    { /* use wedge calibration values of other gel copy it */
      CalibrateOD otherCalib= iDataOther.calib;
      maxNDsteps= iDataOther.calib.maxNDsteps;
      ndWedgeODvalues= new float[MAX_ND_STEPS];
      for(int i= 0;i<MAX_ND_STEPS;i++)
        ndWedgeODvalues[i]= iDataOther.calib.ndWedgeODvalues[i];
      
      units= new String(otherCalib.units);
      unitsAbbrev= new String(otherCalib.unitsAbbrev);
      manufacturerPartNbr= new String(otherCalib.manufacturerPartNbr);
      inheritODflag= true;
    } /* use wedge calibration values of other gel copy it */

    return(inheritODflag);
  } /* inheritNDwedgeODvalues */
     

  /**
   * demo_setDefaultWedgeData_LeukemiaGels() - setup the ND wedge data 
   * for the 4 demo GELLAB-II PPX leukemia gels release with Flicker.
   * Setup the ROI for the ND wedge for the gel.
   * Only extrapolate the # of OD values > 0.0.
   * There may be more gray peaks than OD values, but leave them there
   * since we may want to delete false-peaks in the middle which would move
   * valid-peaks down (to the proper place) in the gray peaks list.
   * @return true if it is a valid leukemia gel
   */
  public boolean demo_setDefaultWedgeData_LeukemiaGels(ImageData iData)
  { /* demo_setDefaultWedgeData_LeukemiaGels */    
    ImageDataROI idROI= iData.idROI;
    
    boolean DBUG_DEF_DATA= false;
    String baseFile= util.getFileNameFromPath(iData.imageFile);
    
    /* Set ND wedge to debug ROIs for the following images else error */
    if(baseFile.endsWith("HUMAN-AML.ppx"))
    {
      ndcwx1= 36;
      ndcwy1= 7;
      ndcwx2= 507;
      ndcwy2= 51;
    }
    else if(baseFile.endsWith("HUMAN-ALL.ppx"))
    {
      ndcwx1= 36;
      ndcwy1= 7;
      ndcwx2= 507;
      ndcwy2= 51;
    }
    else if(baseFile.endsWith("HUMAN-CLL.ppx"))
    {
      ndcwx1= 36;
      ndcwy1= 7;
      ndcwx2= 507;
      ndcwy2= 51;
    }
    else if(baseFile.endsWith("HUMAN-HCL.ppx"))
    {
      ndcwx1= 36;
      ndcwy1= 7;
      ndcwx2= 507;
      ndcwy2= 51;
    }
    else
      return(false);                /* ignore non-leukemia demo gels */
    
    /* Enable ROI display */
    if(! idROI.isValidNDcalibCW())
    {
      String msg= "Please assign Wedge calibration ROI in image first.";
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    
    if(DBUG_DEF_DATA)
    {
      util.showMsg("DBUG: assigning Wedge calibration ROI", Color.black);
      util.showMsg("DBUG: ND Wedge ROI ["+ndcwx1+":"+ndcwx2+","+
                   ndcwy1+":"+ndcwy2+"]", 
                   Color.black);
    }
    idROI.setROI(ndcwx1, ndcwy1, ndcwx2, ndcwy2);   
    idROI.copyROI2CW();        
    flk.viewRoiFlag= true;               /* force it to be true */
    flk.repaint();
          
    /* Define the OD values for each of the ND Wedge steps */
    int
      maxGray= MAX_GRAY,
      maxPeaksAllowed= 25,          /* should be 15 */
      startRange= 5,
      avgDist= 3,
      minDist= 5,                   /* 4? */
      lookBackWidth= 3,
      minHistFreqPeakValue= 30;
    float freqStoN= 10.0F;
    boolean
      smoothpeakIdxListFlag= false,
      useShrinkingMinDistanceFlag= false;
    
    /* Force the ndWedgeODvalues[] values.
     * Later we will read this from .cal file
     * of have the user enter it into the TextField grid GUI.
     */
    float odWedgeList[]= {0.06F, 0.20F, 0.35F, 0.51F, 0.64F, 0.80F,
                          0.95F, 1.09F, 1.24F, 1.38F, 1.54F, 1.67F,
                          1.80F, 1.96F, 2.11F, 2.25F};
    maxNDsteps= odWedgeList.length;
    ndWedgeODvalues= new float[MAX_ND_STEPS];    
    ndWedgeGrayValues= new int[MAX_ND_STEPS];
    for(int i=0;i<maxNDsteps;i++)
      ndWedgeODvalues[i]= odWedgeList[i];
      
    /* Get fresh data histogram image */
    boolean ok= iData.idM.calcHistogram(ndcwx1, ndcwy1, ndcwx2, ndcwy2);      
    if(!ok)
      return(false);
    hist= iData.hist;
    maxGrayValue= hist.length-1;  /* value actually found */
    
    int sHist[]= new int[MAX_GRAY+1];       /* May smooth the copy */
    int
      totFreq= 0,
      minFreq= 100000000,
      maxFreq= 0,
      minG= MAX_GRAY+1,
      maxG= -1,
      v,
      g;
    for(g= 0;g<=MAX_GRAY;g++)
    {
      v= hist[g];
      sHist[g]= v;
      if(v!=0)
      {
        totFreq += v;
        minFreq= Math.min(minFreq,v);
        maxFreq= Math.max(maxFreq,v);
        minG= Math.min(minG,g);
        maxG= Math.max(maxG,g);
      }
    }
     
    if(DBUG_DEF_DATA)
    {
      util.showMsg("DBUG: ND Wedge hist [minG:maxG]=["+minG+":"+maxG+"]",
                   Color.black);
      util.showMsg("DBUG: ND Wedge hist [minFreq:maxFreq]=["+
                   minFreq+":"+maxFreq+"] totFreq="+totFreq,
                   Color.black);
    }
    
    int gelHist[]= sHist;
    /*
    int
      nTimes= 1,
      windowWidth= 2,
      noiseThreshold= 10;
    gelHist= smoothHistogram(sHist, nTimes, windowWidth,
                                 noiseThreshold);
    */
    
    /* Find the peaks in the histogram */
    DBUG_CALIB= false;
    int peakList[]= findPeaks(maxGray, maxPeaksAllowed, 
                              gelHist, startRange,
                              avgDist, minDist, lookBackWidth,
                              freqStoN, minHistFreqPeakValue,
                              smoothpeakIdxListFlag,
                              useShrinkingMinDistanceFlag );
    int nPeaksFound= peakList.length;
    if(DBUG_DEF_DATA)
      util.showMsg("\nDBUG: Summary - found nPeaks="+nPeaksFound,
                   Color.black); 
         
    /* Copy data to calib state. */
    maxPeaks= Math.min(maxNDsteps,nPeaksFound);
    for(int i=0;i<maxPeaks;i++)
    {
      ndWedgeGrayValues[i]= peakList[i];
    }
    
    if(DBUG_DEF_DATA)
    {
      util.showMsg("DBUG: maxNDsteps="+maxNDsteps+" maxPeaks="+maxPeaks,
                   Color.black);
      for(int i=0;i<maxNDsteps;i++)
      {
        int idx= Math.max(Math.min(maxGrayValue,ndWedgeGrayValues[i]),0);
        util.showMsg("DBUG: step #"+(i+1)+" OD["+ndWedgeODvalues[i]+
                     " od] peakList["+i+"]="+ndWedgeGrayValues[i]+
                     " sHist[peakList[i]]="+sHist[idx],
                     Color.black);
      }
    }
    
    /* extrapolate the wedge */
    String sOK= extrapolateNDwedgeMap(maxGrayValue, maxNDsteps, maxPeaks,
                                      ndWedgeGrayValues, ndWedgeODvalues);
    /* Change the units if successful */
    if(sOK!=null)
    {
      System.out.println(sOK);
      return(false);
    }
    else
    { /* use defaults */
      setUnits("Optical density");
      setUnitsAbbrev("od");
      setUnitsManufacturerPartNbr("---");
    }
    
    return(true);  
  } /* demo_setDefaultWedgeData_LeukemiaGels */
 
  
  /**
   * main() - for testing and optimizing parameters for findPeak method 
   * using preset data from GELLAb-II Leukemia-AML gel image (gel 0324.1). 
   * @param args is the command line arg list
   */
  public static void main(String args[])
  { /* main */    
    /* [1] Make new structures */
    CalibrateOD cal= new  CalibrateOD(255);    
    
    int
      maxGray= MAX_GRAY,
      maxPeaksAllowed= 25,          /* should be 15 */
      startRange= 5,
      avgDist= 3,
      minDist= 5,                   /* 4? */
      lookBackWidth= 3,
      minHistFreqPeakValue= 30;
    float freqStoN= 10.0F;
    boolean
      smoothpeakIdxListFlag= false,
      useShrinkingMinDistanceFlag= false;
    
    /* Compute the histogram from actual peaks from  GELLAb-II Leukemia-AML
     * gel image (gel 0324.1) vidicon data
     */
    double
      odWedgeCal[]= {0.06,.20,.35,.51,.64,.80,.95,1.09,1.24,1.38,1.54,1.67,
                     1.80,1.96,2.11,2.25};
    /* Actual peaks in gel 0234.1 data from old GELLAB-II database */
    int
      jPeaks[]= {27, 49, 72, 95, 117, 136, 153, 168, 181, 
                 192, 200, 208, 213, 220, 225}; 
    /* Previous ROI in gel 0234.1 data from old GELLAB-II database */
    int    
      ndcwx1= 36, 
      ndcwy1= 497,
      ndcwx2= 74,
      ndcwy2= 509;
  
    /* New ROI measured with Flicker */
    ndcwx1= 17;
    ndcwy1= 505;
    ndcwx2= 15;
    ndcwy2= 55;
    
    /* New grayscale histogram measured with Flicker from calcHistogramROI() */
    int
      hData[]= {
                 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 4, 9, 12,

                 30, 48, 62, 93, 86, 107,                 /* peak 1 */
                 105, 83, 56, 52, 28,
                 
                 19, 18, 10, 7, 5, 6, 3, 12, 19, 16, 

                 21, 24, 42, 54, 72, 112, 130, 145,       /* peak 2 */
                 140, 131, 110, 70, 58, 35, 24, 

                 15, 13, 9, 9, 4, 7, 16, 6, 11, 

                 21, 48, 52, 91, 89, 137, 192,            /* peak 3 */
                 186, 140, 102, 74, 45, 29, 

                 17, 13, 9, 11, 5, 5, 8, 6, 4, 9, 13, 18,

                 40, 59, 84, 107, 171,                    /* peak 4 */
                 136, 150, 147, 106, 70, 46, 35, 

                 15, 16, 11, 6, 9, 16, 8, 14,

                 31, 70, 111, 132, 150, 172, 180,         /* peak 5 */
                 126, 116, 58, 42,

                 12, 9, 6, 2, 11, 3, 5, 10, 12,

                 23, 51, 77, 127, 163, 234,               /* peak 6 */
                 185, 148, 99, 54, 35,

                 18, 14, 12, 6, 2, 14, 18, 

                 29, 49, 88, 179, 214,                   /* peak 7 */
                 211, 198, 118, 71, 36, 

                 16, 14, 14, 15, 

                 21, 26, 73, 144, 183, 258,              /* peak 7 */
                 214, 147, 103,

                 21, 23, 11, 11, 19, 

                 28, 65, 136, 191, 236,                  /* peak 8 */
                 221, 181, 100, 36, 32, 

                 29, 63, 106, 153, 287, 309,             /* peak 9 */
                 141, 90, 

                 55, 62, 104, 190, 244, 280,             /* peak 10 */
                 182, 107, 73, 

                 65, 137, 197, 266, 317,                 /* peak 11 */

                 194, 126, 146, 209, 229,                /* peak 12 */
                 219, 220, 

                 140, 179, 229, 297,                     /* peak 13 */
                 288, 238, 

                 213, 320, 269, 406,                     /* peak 14 */
                 331, 314, 222, 165, 66, 35, 10, 2, 

                 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0
                 };
                  
    System.out.println("CalibrateOD:main() hData.length="+hData.length);
    System.out.println("  maxGray="+maxGray+"\n");
    maxGray= hData.length-1;
    int 
      sHist[]= new int[MAX_GRAY+1];
    int
      minG= MAX_GRAY+1,
      maxG= -1,
      v,
      g;
    for(g= 0;g<=maxGray;g++)
    {
      v= hData[g];
      sHist[g]= v;
      if(v!=0)
      {
        minG = Math.min(minG,g);
        maxG = Math.min(maxG,g);
      }
    }
    
    int gelHist[]= sHist;
    /*
    int
      nTimes= 1,
      windowWidth= 2,
      noiseThreshold= 10;
    gelHist= cal.smoothHistogram(sHist, nTimes, windowWidth,
                                 noiseThreshold);
    */
    DBUG_CALIB= true;
    int peakList[]= cal.findPeaks(maxGray, maxPeaksAllowed, 
                                  gelHist, startRange,
                                  avgDist, minDist, lookBackWidth,
                                  freqStoN, minHistFreqPeakValue,
                                  smoothpeakIdxListFlag,
                                  useShrinkingMinDistanceFlag );
    int nPeaks= peakList.length;
    
    System.out.println("\n\nSummary\nFound nPeaks="+nPeaks); 
    for(int i=0;i<nPeaks;i++)
      System.out.println("peakList["+i+"]="+peakList[i]+ 
                         " sHist[peakList[i]]="+sHist[peakList[i]]);
    
    System.out.println(
          "\n\nDifference between new method and actual edited peaks"); 
    for(int i=0;i<nPeaks;i++)
      System.out.println("(peakList["+i+"]="+peakList[i]+ 
                         ") - (jPeaks["+i+"]="+jPeaks[i]+") = diff="+
                         (peakList[i]-jPeaks[i]));  
   } /* main */

    
    
} /* End of CalibrateOD */


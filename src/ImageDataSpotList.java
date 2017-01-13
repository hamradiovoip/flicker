/* File: ImageDataSpotList.java */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.*;
import java.io.*;

/**
 * ImageDataSpotList class supports image data spot list object access.
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

public class ImageDataSpotList
{ /* class ImageDataSpotList */
              
  /** Flicker global classes */
  public static Flicker
    flk; 
  /** extended Flicker state variable class */
  public static Util
    util; 
  
  /** Instance of ImageData for extended classes */
  public ImageData
    id;
      
  /* ---------------------------------------------------- */
  /*   Spot list (measured spots) for this image          */
  /* ---------------------------------------------------- */
  public boolean
    changeSpotList= false;
  /** List of measured spots data if not null [0:nSpots-1].
   * Preallocate to [0:flk.MAX_SPOTS-1]
   */
  public Spot
    spotList[];  
  /** # of measured spots used in SpotList[] */
  public int
    nSpots= 0;  
  /** "Measurement counter" in measurements for this image */
  public int
    measCtr= 0; 
  
  /** Backup list of measured spots data if not null [0:nSpotsBkup-1].
   * Preallocate to [0:flk.MAX_SPOTS-1]. This lets us push the
   * current spot list while we temporarily use a new set of spots
   * for some task (e.g., calibrate by a set of spot measurements)
   * and then restore it later. Currently, we only do this to
   * one level and this list is NOT saved when you exit the program.
   */
  public Spot
    spotListBkup[];  
  /** # of measured spots used in SpotListBkup[] */
  public int
    nSpotsBackup= 0;  
     
   
  /**
   * ImageDataSpotList() - Constructor for Spot Lists
   */
  public ImageDataSpotList(ImageData id)
  { /* ImageDataSpotList*/
    this.id= id;
    flk= id.flk;
    util= flk.util;
    
    clean();             /* create new  empty spot list */
  } /* ImageDataSpotList*/  
  
  
  /**
   * clean() - cleanup the instance.
   * Clear idSL.spotList[0:nSpots-1].
   * Prompt "are you sure" and clear spot list if 'yes'.
   * Clean up spotlist and backup spot list.
   */
  public void clean()
  { /* clean */    
    if(nSpots>0)
    { /* Clear spotList[0:nSpots-1] */
      /* Prompt "are you sure" and clear spot list if 'yes' */
      clearSpotList();
    }
    spotListBkup= null;                /* will be created when do backup */
    nSpotsBackup= 0;    
  } /* clean */
  
     
  /**
   * clearSpotList() - prompt "are you sure" and clear spot list if 'yes'.
   * @return true if cleared the spot list
   */
  public boolean clearSpotList()
  { /* clearSpotList */
    String
      msg= "Clear spot list (has "+nSpots+
           " spots). Are you sure you want to clear list [yes/no]?";
    PopupTextFieldDialog ptfd= new PopupTextFieldDialog(flk,msg,"yes");
    if(ptfd.okFlag && ptfd.answer.equalsIgnoreCase("yes"))
    { /* clear spot list */
      spotList= new Spot[flk.MAX_SPOTS];
      nSpots= 0;     
      measCtr= 0;
      
      /* Also clear it in the spt/ directory by moving the spt/<imageFile>.spt 
       * file into spt/<imageFile>.spt.bkup clearing the .bkup file it it
       * previously existed first.
       */
      if(id==flk.iData1)
        Spot.rmvSpotListFile(flk.imageFile1, "I1");
      else if(id==flk.iData2)
        Spot.rmvSpotListFile(flk.imageFile2, "I2");
      
      return(true);
    }
    else
      return(false);
  } /* clearSpotList */
  
  
  /**
   * forceClearSpotList() - clear spot list and do NOT prompt if not empty.
   */
  public void forceClearSpotList()
  { /* forceClearSpotList */
    spotList= new Spot[flk.MAX_SPOTS];
    nSpots= 0;
    measCtr= 0;
  } /* forceClearSpotList */

  
  /** 
   * backupSpotList() - backup spotList[0:nSpots-1] to
   * spotListBkup[] and nSpotBkup.
   * @param clearSpotListFlag to also clear the spotlist
   * @return true if succeed, false if the list is currently
   *         already backed up. You can only backup one time.
   * @see restoreSpotList
   **/
  public boolean backupSpotList(boolean clearSpotListFlag)
  { /* backupSpotList */
     if(spotListBkup!=null)
       return(false);         /* already backed up */
     
     spotListBkup= spotList;  /* copy the current list to backup list */
     nSpotsBackup= nSpots;
     
     if(clearSpotListFlag)
     { /* Create a new empty current list of measurement spots */
       spotList= new Spot[flk.MAX_SPOTS]; 
       nSpots= 0;   
     }
     
     return(true);
  } /* backupSpotList */
  
  
  /** 
   * restoreSpotList() - retore spotList[0:nSpots-1] from
   * spotListBkup[] and nSpotBkup.
   * @return true if succeed, false if the list was not previously
   *         backed up. You can only restore a backed up spot list.
   * @see backupSpotList
   **/
  public boolean restoreSpotList()
  { /* restoreSpotList */
     if(spotListBkup==null)
       return(false);         /* already backed up */
     
     spotList= spotListBkup;  /* Restore the backup list */
     nSpots= nSpotsBackup;     
     
     spotListBkup= null;      /* G.C. the backup list */
     nSpotsBackup= 0;
     
     return(true);
  } /* restoreSpotList */
    
  
  /**
   * editSpotFromSpotList() - edit the spot from the spot list if found 
   * @param s is spot to edit
   * @param editOnlyIDflag if only want to edit/assign the spot id
   * @return true if edited it, false if failed
   */
  public boolean editSpotFromSpotList(Spot s, boolean editOnlyIDflag)
  { /* editSpotFromSpotList */   
    changeSpotList= s.popupSpotEdit(editOnlyIDflag); 
    
    return(changeSpotList);
  } /* editSpotFromSpotList */
    
  
  /**
   * editSpotFromSpotList() - edit the spot from the spot list if found 
   * @param s1 is spot to edit in left image
   * @param s2 is spot to edit in right image
   * @param editOnlyIDflag if only want to edit/assign the spot id
   * @return true if edited it, false if failed
   */
  public boolean editSpotFromSpotList(Spot s1, Spot s2,
                                      boolean editOnlyIDflag)
  { /* editSpotFromSpotList */   
    changeSpotList= s1.popupSpotEditBoth(s1,s2,editOnlyIDflag); 
    
    return(changeSpotList);
  } /* editSpotFromSpotList */
      
    
  /**
   * rmvSpotFromSpotList() - remove the spot from the spot list if found 
   * If the spot is at the end of the list, the reuse the spot number.
   * @param s is spot to remove
   * @return true if deleted it, false if failed
   */
  public boolean rmvSpotFromSpotList(Spot s)
  { /* rmvSpotFromSpotList */
    if(spotList==null)
      return(false);            /* there is no list */
    
    /* Find the slot */
    int idx= -1;
    for(int i=0;i<nSpots;i++)
      if(s==spotList[i])
      { /* found it */
        idx= i;
        break;
      }
    if(idx==-1)
      return(false);
    
    /* Remove the spot. Decrement the master counter only if
     * you are at the end of the list
     */ 
    if(idx==(nSpots-1))
      measCtr--;
    
    /* move spots down in the list */
    for(int i=idx;i<(nSpots-1);i++)
      spotList[i]= spotList[i+1];
   
    /* take it out of the list */
    spotList[--nSpots]= null;
    
    changeSpotList= true;           /* we changed the spot list */
    
    return(true);
  } /* rmvSpotFromSpotList */
        
  
  /**
   * mapSPIDtoPIRURL() - if using a PIR server database AND we
   * have either looked up the Swiss-Prot ID for the spot, then
   * map the dataURL to the PRI site by adding the SP ID to the
   * currentPRIbaseURL. The URL is null if no PIR server was selected.
   * @param defaultURL to use if PIR server access is not enabled.
   * @param clickCGIbaseURL is the Swiss-Prot ID if not null.
   * @return PIR URL or default URL if failed.
   */
   public String mapSPIDtoPIRURL(String defaultURL, String clickCGIbaseURL,
                                 int x, int y)
   { /* mapSPIDtoPIRURL */
     if(clickCGIbaseURL==null)
       return(defaultURL);     
     Spot s= lookupSpotInSpotListByXY(x,y);
     if(s==null)
       return(defaultURL);
     
     String dataURL= defaultURL;
     if((flk.usePIRUniprotServerFlag || flk.usePIRiProClassServerFlag ||
         flk.usePIRiProLinkFlag) && s!=null)
     { /* Map Swiss-Prot ID or name to PIR URL */
       if(s.name==null || s.name.length()==0)
       {
         String pData[]= util.getProteinIDdataByXYurl(clickCGIbaseURL,
                                                      s.xC,s.yC,null);
         s.id= pData[0];
         s.name= pData[1];
       }
       dataURL= flk.currentPRIbaseURL+s.id;
     } /* Map Swiss-Prot ID or name to PIR URL */
     
     return(dataURL);
   } /* mapSPIDtoPIRURL */ 
            
    
  /**
   * lookupProtIDandNameToSpotList() - if the gel is clickable and the user
   * enabled the clickable DB checkbox, then search the clickableCGIbaseURL 
   * protein database for all spots in the spot list and for those
   * that have matches, update the Spot.id and Spot.name. If no
   * match,then no change.
   * @param iData is the gel image to update
   * @return true if updated it, false if failed
   */
  public boolean lookupProtIDandNameToSpotList(ImageData iData)
  { /* lookupProtIDandNameToSpotList */    
    /* [1] Clear flag to stop spot list annotation lookup from
     * proteomics Web server. The flag is set by typing C-Q and 
     * tested in the lookup loop.
     */
    flk.stopAnnotationUpdateFlag= false;
    if(!flk.isClickableDBflag )
    {
      util.showMsg1("There is no active image map Web server",
                    Color.red);
      util.showMsg2("associated with this image.",Color.black);
      return(false);            /* there is no URL */
    }
    
    if(!flk.userClickableImageDBflag)
    {
      util.showMsg1("First set the 'Click to access DB' checkbox.",
                    Color.red);
      util.showMsg2("Then try again.",Color.black);
      return(false);            /* The switch was not set */
    }
    
    /* [2] Check that there is a valid spot list. */
    if(spotList==null)
    {
      util.showMsg1("There is no spot list to process for this image.",
                    Color.red);
      return(false);            /* there is no list */
    }
   
    /* [3] Check that the gel has a valid associated active map Web server */
    String clickCGIbaseURL= null;
    /* Use casecade of tests to compute the URL to use */
    if(iData==flk.iData1 && flk.clickableCGIbaseURL1!=null &&
       flk.clickableCGIbaseURL1.length()>0)
      clickCGIbaseURL= flk.clickableCGIbaseURL1;
    else if(iData==flk.iData2 && flk.clickableCGIbaseURL2!=null &&
            flk.clickableCGIbaseURL2.length()>0)
      clickCGIbaseURL= flk.clickableCGIbaseURL2;    
    else 
    {
      util.showMsg1("There is no active image map Web server",
                    Color.red);
      util.showMsg2("associated with this image.",Color.black);
      return(false);            /* there is no URL */
    }
       
    util.showMsg1("Type Control/Q to stop lookup of spot",
                   Color.black);
    util.showMsg2("annotations from proteomics Web server.",
                   Color.black);
    
    /* [4] Process all of the spots in the spot list */
    Spot s;
    flk.stopAnnotationUpdateFlag= false;
    for(int i=0;i<nSpots;i++)
    { /* get data from protein server */
      if(flk.stopAnnotationUpdateFlag)
      {
        util.showMsg1(
           "Stopping lookup of spot annotations from proteomics Web server.",
                    Color.red);
        util.showMsg2("",Color.black);
        break;
      }
      
      s= spotList[i];
      util.showMsg2("Processing spot #"+s.nbr+" at ("+s.xC+","+s.yC+")",
                    Color.black);
      String pData[]= util.getProteinIDdataByXYurl(clickCGIbaseURL,
                                                   s.xC,s.yC,null);
      if(pData!=null)
      { /* found it */
        s.name= pData[1];
        s.id= pData[0];
        util.showMsg2(" Spot #"+s.nbr+" Swiss-Prot ID="+s.id+" ["+s.name+"]", 
                      Color.black);
      }
      else
        util.showMsg2(" Spot #"+s.nbr+" was NOT found in active protein DB.",
                      Color.red);
    } /* get data from protein server */
  
    return(true);
  } /* lookupProtIDandNameToSpotList */
      
  
  /**
   * addUniqueSpotToSpotList() - add spot if it is unique to the spot list 
   * else just update the spot measurement but keep the spot number if
   * it is an existing spot if +- 1 pixel in xC and/or yC 
   * @param nGel is the Gel# (1 or 2) rof "left" or "right" image.
   * @param nbr is measurement number of spot unique to the associated gel 
   * @param nCirMask is the circular mask diameter
   * @param circleRadius is the radius of the circlular mask
   * @param xC is the spot X centroid
   * @param yC is the spot Y centroid 
   * @param xB is background x centroid 
   * @param yB is background Y centroid
   * @param area is spot area in pixels (float so possibly scaled)
   * @param isCalibFlag is measurements are calibrrated in OD rather
   *        than grayscale
   * @param useTotDensityFlag measurements total density else mean values
   * @param density is total density uncorrected for background computed as 
   *        the sum of the gray value or calibrated gray value (i.e. OD?)
   * @param densPrime is the total density corrected for background by 
   *        computed as: densPrime =  (density - mnBkgrd*area)
   * @param bkgrd is the total spot mean background density
   * @param mnDens is the mean density
   * @param totDens is the total density
   * @param mnDensPrime is mean density corrected for background
   *        computed as (mnDensPrime - mnBkgrd)
   * @param mnBkgrd is the mean spot mean background density
   * @param dMax is spot MAX OD
   * @param dMin is spot MIN OD
   * @param dMaxBkgrd background for spot MAX OD or gray value
   * @param dMinBkgrd background for spot MIN OD or gray value 
   * @return the Spot instance if added it or updated the spotList and return
   *        spot, else null if problem
   */
  public Spot addUniqueSpotToSpotList(int nGel, int nbr, int nCirMask,
                                      int circleRadius,
                                      int xC, int yC, int xB, int yB,
                                      float area, boolean isCalibFlag,
                                      boolean useTotDensityFlag,
                                      float density, float densPrime,
                                      float bkgrd,
                                      float mnDens, float totDens,
                                      float mnDensPrime, float mnBkgrd,
                                      float dMax, float dMin,
                                      float dMaxBkgrd, float dMinBkgrd)
  { /* addUniqueSpotToSpotList */ 
    Spot s= null;               /* assume problem */
        
    if(spotList==null)
    { /* create initial spot list */
      spotList= new Spot[flk.MAX_SPOTS];
      nSpots= 0;
    }
    else
    { /* search the spot list to see if spot is in the list */   
      s= lookupSpotInSpotListByXY(xC, yC);
      if(s!=null)
      { /* update an existing spot if +- 1 pixel in x and/or y */
        s.set(nGel, s.nbr, null, null, nCirMask, circleRadius, xC, yC, xB, yB, 
              area, isCalibFlag, useTotDensityFlag,
              density, densPrime, bkgrd,
              mnDens, totDens, mnDensPrime, mnBkgrd,
              dMax, dMin, dMaxBkgrd, dMinBkgrd);        
        changeSpotList= true;
        return(s);
      }
    } /* search the spot list to see if spot is in the list */

    if(s==null)
    { /* add new spot since it does not exist */
      s= new Spot(nGel, nbr, null, null,nCirMask, circleRadius, xC, yC, xB, yB,
                  area, isCalibFlag, useTotDensityFlag,
                  density, densPrime, bkgrd,
                  mnDens, totDens, mnDensPrime, mnBkgrd,
                  dMax, dMin, dMaxBkgrd, dMinBkgrd);        
      changeSpotList= true;
    }
    
    /* Save spot in spotList. Create or grow the list if need be. */    
    if(spotList==null)
      spotList= new Spot[flk.MAX_SPOTS];
    if(spotList.length>=(nSpots+1))
    { /* regrow list */
      Spot tmp[]= new Spot[nSpots+flk.MAX_SPOTS];
      for(int i=0;i<nSpots;i++)
        tmp[i]= spotList[i];
      spotList= tmp;
    } /* regrow list */
    
    /* Add new spot to the list */
    spotList[nSpots++]= s;
    
    return(s);
  } /* addUniqueSpotToSpotList */
  
  
  /**
   * lookupSpotInSpotListByXY() - lookup the spot in the spotList[0:nSpots-1]
   * if the (xC,yC) are +- 1 pixel in x and/or y.
   * @param xC to test
   * @param yC to test
   * @return Spot if found, else null
   */
  public Spot lookupSpotInSpotListByXY(int xC, int yC)
  { /* lookupSpotInSpotListByXY */
    Spot sTmp;
    int 
      minPixelDist= 2,
      tmpX= xC,
      tmpY= yC;
    
    /* map 1.0x state from mag position */
    double
      mag= Math.max(flk.curState.zoomMagVal, SliderState.MIN_ZOOM_MAG_VAL);
      mag= Math.min(mag, SliderState.MAX_ZOOM_MAG_VAL);
    if(mag != 1.0) 
    {
      double 
        xDb= xC/mag,
        yDb= yC/mag;       
       tmpX= (int) Math.round(xDb);
       tmpY= (int) Math.round(yDb);
    }      
    
    for(int i=0;i<nSpots;i++)
    { /* see if in current DB */
      sTmp= spotList[i];
      if(Math.abs(sTmp.xC-tmpX)<=minPixelDist && 
         Math.abs(sTmp.yC-tmpY)<=minPixelDist)
        return(sTmp);  /* found it */
    }
    
    return(null);     /* not found */
  }  /* lookupSpotInSpotListByXY */

  
} /* class ImageDataSpotList */

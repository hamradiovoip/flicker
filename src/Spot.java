/* File: Spot.java */

import java.awt.*;
import java.awt.Color;
import java.awt.image.*;
import java.awt.event.*;
import java.util.EventListener;
import java.awt.event.WindowEvent;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;
import java.text.*;

/**
 * Class Spot contains the spot structure for measured spots.
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

public class Spot implements ActionListener, WindowListener
{
  /** instance of Flicker*/
  private static Flicker
    flk; 
  /** FileIO utilities */
  public static FileIO
    fio;
  /** misc utility methods */
  private static Util
    util;
  
               
  /** default frame size width of popup */
  final static int
    POPUP_WIDTH= 300;      
  /** default frame size height of popup */
  final static int
    POPUP_HEIGHT= 515;
  /** preferred canvas height */
  public int
    preferredHeight= POPUP_HEIGHT;
  /** preferred canvas width */
  public int
    preferredWidth= POPUP_WIDTH;
  	
  /** Gel# (1 or 2) for "left" or "right" image. */
  int
    nGel;
  /** measurement number of spot unique to the associated gel */
  int 
    nbr;
  /** Optional associated identifier string if not null. */
  String 
    id;
  /** Optional associated protein string if not null. */
  String 
    name;
  /** circular mask diameter = 2*circleRadius+1*/
  int
    nCirMask;
  /** he radius of the circlular mask */
  int
    circleRadius;

  /** spot X centroid */
  int
    xC;	
  /** spot Y centroid */
  int
    yC;
  /** background x centroid */
  int
    xB;	
  /** background Y centroid */
  int
    yB;
	  
  /** spot area in pixels */
  float
    area;
		  
  /** measurements are in calibrated OD rather than grayscale */
  boolean
    isCalibFlag;  
  /** measurements total density else mean values */
  boolean
    useTotDensityFlag;
		  
  /** total density uncorrected for background computed as the sum of the 
   * gray value or calibrated gray value (i.e. OD?) 
   */		      
  float 
    density;		  
  /** total density corrected for background by computed as:
   * densPrime =  (density - mnBkgrd*area)
  */		      
  float 
    densPrime;
  /** total spot mean background density */
  float 
    bkgrd;		   
  
  /** mean density */		      
  float 
    mnDens; 
  /** total density over the measurement area */		      
  float 
    totDens; 
  /** mean density corrected for background (mnDensPrime - mnBkgrd) */		      
  float 
    mnDensPrime; 	
  /** mean spot mean background density */
  float 
    mnBkgrd;

  /** spot MAX OD */
  float 
    dMax;
  /** spot MIN OD */
  float
    dMin;	
	    
  /** background for spot MAX OD or gray value */
  float 
    dMaxBkgrd;
  /** background for spot MIN OD or gray value  */
  float
    dMinBkgrd;
  
  /* ---- GUI stuff -------- */
  /** Frame to create and popup when you edit */
  private static Frame
    fOne= null;
  /** Frame to create and popup when you edit */
  private static Frame
    fBoth= null;
    
  /** names of the spot fields */
  public static String
    fNames[]= {"nGel", "nbr", "id", "name", "nCirMask", "circleRadius",
               "xC", "yC", "xB", "yB", "area", "isCalibFlag",
               "useTotDensityFlag", "density", "densPrime",
               "bkgrd", "mnDens", "totDens", "mnDensPrime",
               "mnBkgrd", "dMax", "dMin", "dMaxBkgrd",
               "dMinBkgrd"
              };
  /** index of "id" field in fNames table. Edit if fName[] list changes*/
  public static int
    idxID= 2;
              
  /** # of names for the spot fields */
   public static int
     nRows= fNames.length;
   
  /** spot Editable table [nRows][2] */
  private static TextField
    spotTable[][]= null;
  
  /** spot Editable table [nRows][3] that handles spots in s1 [1]
   * and spots in s2 [2].
   */
  private static TextField
    spotTableBoth[][]= null;
  
  /** Left Spot 1 if editing both spots */
  private Spot
    s1;  
  /** Right Spot 2 if editing both spots */
  private Spot
    s2;  
  
  /** status Label */
  private Label
    statusLabel;
  
  /** edited the spot flag */
  private boolean 
    changedFlag= false;
	 
  
  /**
   * Spot() - setup Spot part of the database
   */
  public Spot(Flicker flkS)
  {
    flk= flkS;
    fio=  flk.fio;
    util= flk.util;
  }
  
  
  /**
   * Spot() - Constructor for new Spot
   */
  public Spot()
  {
  }


  /**
   * Spot() - Constructor for new Spot with data
   * @param nGel is the Gel# (1 or 2) rof "left" or "right" image.
   * @param nbr is measurement number of spot unique to the associated gel 
   * @param id is the optional associated identifier string if not null.
   * @param name is the optional associated protein name string if not null.
   * @param nCirMask is the circular mask diameter
   * @param circleRadius is the radius of the circlular mask
   * @param xC is the spot X centroid
   * @param yC is the spot Y centroid 
   * @param xB is background x centroid 
   * @param yB is background Y centroid
   * @param area is spot area in pixels (float so possibly scaled)
   * @param isCalibFlag is measurements are in calibrated OD rather than grayscale
   * @param useTotDensityFlag measurements total density else mean values
   * @param density is the total density uncorrected for background computed as 
   *        the sum of the gray value or calibrated gray value (i.e. OD?)
   * @param densPrime is the total density corrected for background by 
   *        computed as: densPrime =  (density - mnBkgrd*area)
   * @param bkgrd is the total spot mean background density
   * @param mnDens is the mean density
   * @param totDens is the total density over the measurement area
   * @param mnDensPrime is mean density corrected for background
   *        computed as (mnDensPrime - mnBkgrd)
   * @param mnBkgrd is the mean spot mean background density
   * @param dMax is spot MAX OD
   * @param dMin is spot MIN OD
   * @param dMaxBkgrd background for spot MAX OD or gray value
   * @param dMinBkgrd background for spot MIN OD or gray value 
   */
  public Spot(int nGel, int nbr, String id, String name,
              int nCirMask, int circleRadius,
              int xC, int yC, int xB, int yB, float area,
              boolean isCalibFlag, boolean useTotDensityFlag, 
              float density, float densPrime, float bkgrd, 
              float mnDens, float totDens,
              float mnDensPrime, float mnBkgrd,
              float dMax, float dMin, 
              float dMaxBkgrd, float dMinBkgrd)
  { /* Spot */
    this.nGel= nGel;
    this.nbr= nbr;
    this.id= id;
    this.name= name;
    this.nCirMask= nCirMask;
    this.circleRadius= circleRadius;
    
    this.xC= xC;
    this.yC= yC;
    this.xB= xB;
    this.yB= yB;    
    this.area= area;
    
    this.isCalibFlag= isCalibFlag;
    this.useTotDensityFlag= useTotDensityFlag;
    
    this.density= density;
    this.densPrime= densPrime;
    this.bkgrd= bkgrd;
    
    this.mnDens= mnDens;
    this.totDens= totDens;
    this.mnDensPrime= mnDensPrime;
    this.mnBkgrd= mnBkgrd;
    
    this.dMax= dMax;
    this.dMin= dMin;
    this.dMaxBkgrd= dMaxBkgrd;
    this.dMinBkgrd= dMinBkgrd;
  } /* Spot */
  

  /**
   * set() - set the data for a spot with new data
   * @param nGel is the Gel# (1 or 2) rof "left" or "right" image.
   * @param nbr is measurement number of spot unique to the associated gel
   * @param id is the optional associated identifier string if not null.
   * @param name is the optional associated protein name string if not null.
   * @param nCirMask is the circular mask diameter
   * @param circleRadius is the radius of the circlular mask
   * @param xC is the spot X centroid
   * @param yC is the spot Y centroid 
   * @param xB is background x centroid 
   * @param yB is background Y centroid
   * @param area is spot area in pixels (float so possibly scaled)
   * @param isCalibFlag is measurements are in calibrated OD rather than grayscale
   * @param useTotDensityFlag measurements total density else mean values
   * @param density is the total density uncorrected for background computed as 
   *        the sum of the gray value or calibrated gray value (i.e. OD?)
   * @param densPrime is the total density corrected for background by 
   *        computed as: densPrime =  (density - mnBkgrd*area)
   * @param bkgrd is the total spot mean background density
   * @param mnDens is the mean density
   * @param totDens is the total density over the measurement area
   * @param mnDensPrime is mean density corrected for background
   *        computed as (mnDensPrime - mnBkgrd)
   * @param mnBkgrd is the mean spot mean background density
   * @param dMax is spot MAX OD
   * @param dMin is spot MIN OD
   * @param dMaxBkgrd background for spot MAX OD or gray value
   * @param dMinBkgrd background for spot MIN OD or gray value 
   */
  public void set(int nGel, int nbr, String id, String name,
                  int nCirMask, int circleRadius,
                  int xC, int yC, int xB, int yB, float area,
                  boolean isCalibFlag, boolean useTotDensityFlag,
                  float density, float densPrime, float bkgrd,
                  float mnDens, float totDens, 
                  float mnDensPrime, float mnBkgrd,
                  float dMax, float dMin,
                  float dMaxBkgrd, float dMinBkgrd)
  { /* set */
    this.nGel= nGel;
    this.nbr= nbr;
    this.id= id;
    this.name= name;
    this.nCirMask= nCirMask;
    this.circleRadius= circleRadius;
    
    this.xC= xC;
    this.yC= yC;
    this.xB= xB;
    this.yB= yB;    
    this.area= area;
    
    this.isCalibFlag= isCalibFlag;
    this.useTotDensityFlag= useTotDensityFlag;
    
    this.density= density;
    this.densPrime= densPrime;
    this.bkgrd= bkgrd;
    
    this.mnDens= mnDens;
    this.mnDensPrime= mnDensPrime;
    this.mnBkgrd= mnBkgrd;
    
    this.dMax= dMax;
    this.dMin= dMin;
    this.dMaxBkgrd= dMaxBkgrd;
    this.dMinBkgrd= dMinBkgrd;
  } /* set */
   
    
 /**
  * getID() - get the spot ID
  * @return the spot id
  */
  public String getID()
  { return(this.id); }
   
    
 /**
  * setID() - set the spot ID
  * @param the spot id
  */
  public void setID(String id)
  { this.id= id; }  
  
    
 /**
  * readState() - Read spotList[] "spt/<gelFile>.spt" file
  * @param gelFile is the file base name. We strip off the image type
  *        and add ".spt" 
  * @param iName of the image to read (e.g., "I1", or "I2")
  * @return spot list array if succeed, else null
  */ 
  static Spot[] readState(String gelFile, String iName)
  { /* readState */   
    String baseName= util.getFileNameFromPath(gelFile);
    baseName= util.rmvFileExtension(baseName);
    String 
      sptFile= flk.userDir + flk.fileSeparator + 
               "spt"+flk.fileSeparator+ baseName+".spt",
      sMsg= "Reading Flicker ["+sptFile+"] Spot file";
    File fd= new File(sptFile);
    if(!fd.isFile())
      return(null);                       /* avoid error message */
    Hashtable 
      ht= util.readNameValuesHashTableFromFile(sptFile,sMsg,100101);
    if(ht==null)
      return(null);    
    else
      util.setStateHashtableForGetValue(ht,util.nEntries);
          
    baseName= util.getStateValue(iName+"-baseName", (String)null);    
    int nSpots= util.getStateValue(iName+"-nSpots", 0); 
    Spot spotList[]= new Spot[nSpots];    /* list maximum allowed size */
    
    for(int i=0;i<nSpots;i++)
    { /* read spot i from the hash table */
      Spot s= new Spot();
      s.nGel= util.getStateValue(iName+"-nGel-"+i, 0);
      s.nbr= util.getStateValue(iName+"-nbr-"+i, 0);
      s.id= util.getStateValue(iName+"-id-"+i, null);
      s.name= util.getStateValue(iName+"-name-"+i, null);
      s.nCirMask= util.getStateValue(iName+"-nCirMask-"+i, 0);
      s.circleRadius= util.getStateValue(iName+"-circleRadius-"+i, 0);
      s.xC= util.getStateValue(iName+"-xC-"+i, 0);
      s.yC= util.getStateValue(iName+"-yC-"+i, 0);
      s.xB= util.getStateValue(iName+"-xB-"+i, 0);
      s.yB= util.getStateValue(iName+"-yB-"+i, 0);
      s.area= util.getStateValue(iName+"-area-"+i, 0.0F);
      s.isCalibFlag= util.getStateValue(iName+"-isCalibFlag-"+i,false);
      s.useTotDensityFlag= util.getStateValue(iName+"-useTotDensityFlag-"+i,false);
      s.density= util.getStateValue(iName+"-density-"+i, 0.0F);
      s.densPrime= util.getStateValue(iName+"-densPrime-"+i, 0.0F);
      s.bkgrd= util.getStateValue(iName+"-bkgrd-"+i, 0.0F);
      s.mnDens= util.getStateValue(iName+"-mnDens-"+i, 0.0F);
      s.totDens= util.getStateValue(iName+"-totDens-"+i, 0.0F);
      s.mnDensPrime= util.getStateValue(iName+"-mnDensPrime-"+i, 0.0F);
      s.mnBkgrd= util.getStateValue(iName+"-mnBkgrd-"+i, 0.0F);
      s.dMax= util.getStateValue(iName+"-dMax-"+i, 0.0F);
      s.dMin= util.getStateValue(iName+"-dMin-"+i, 0.0F);
      s.dMaxBkgrd= util.getStateValue(iName+"-dMaxBkgrd-"+i, 0.0F);
      s.dMinBkgrd= util.getStateValue(iName+"-dMinBkgrd-"+i, 0.0F);
      if(s.area==0)
      { /* backwards compatible */
        /* Test if data was acquired before added s.area & s.circleRadius
         * and then compute it from s.nCirMask.
         */
        if( s.circleRadius==0)    
          s.circleRadius= (s.nCirMask/2)-1;
        s.area= ImageDataMeas.maskArea[s.circleRadius];
      }
      
      spotList[i]= s;        /* push the spot into the spot list */
    } /* read spot i from the hash table */
    
    return(spotList);
  } /* readState */
    
  
 /**
  * writeState() - Write the spotlist[] to "spt/<gelFile>.spt" file
  * @param gelFile is the file base name. We strip off the image type
  *        and add ".spt" 
  * @param iName of the image to read (e.g., "I1", or "I2")
  * @param spotList is list of spots to write out
  * @param sBuf is the string buffer to write to.
  * @return true if succeed
  */ 
  static boolean writeState(String gelFile, String iName, 
                            Spot spotList[], int nSpots)
  { /* writeState */ 
    if(spotList==null || nSpots==0)
      return(false);
    
    String baseName= util.getFileNameFromPath(gelFile);
    baseName= util.rmvFileExtension(baseName);
    String sptFile= flk.userDir + flk.fileSeparator + 
                    "spt"+flk.fileSeparator+ baseName+".spt";
    
    StringBuffer sBuf= new StringBuffer(nSpots*200);
    
    sBuf.append(iName+"-baseName\t"+baseName+"\n");
    sBuf.append(iName+"-nSpots\t"+nSpots+"\n");
    for(int i=0;i<nSpots;i++)
    {
      Spot s= spotList[i];
      sBuf.append(iName+"-nGel-"+i+"\t"+s.nGel+"\n");
      sBuf.append(iName+"-nbr-"+i+"\t"+s.nbr+"\n");
      if(s.id!=null)
        sBuf.append(iName+"-id-"+i+"\t"+s.id+"\n");
      if(s.name!=null)
        sBuf.append(iName+"-name-"+i+"\t"+s.name+"\n");
      sBuf.append(iName+"-nCirMask-"+i+"\t"+s.nCirMask+"\n");
      sBuf.append(iName+"-circleRadius-"+i+"\t"+s.circleRadius+"\n");
      sBuf.append(iName+"-xC-"+i+"\t"+s.xC+"\n");
      sBuf.append(iName+"-yC-"+i+"\t"+s.yC+"\n");
      sBuf.append(iName+"-xB-"+i+"\t"+s.xB+"\n");
      sBuf.append(iName+"-yB-"+i+"\t"+s.yB+"\n");      
      sBuf.append(iName+"-area-"+i+"\t"+s.area+"\n");
      sBuf.append(iName+"-isCalibFlag-"+i+"\t"+s.isCalibFlag+"\n");
      sBuf.append(iName+"-useTotDensityFlag-"+i+"\t"+s.useTotDensityFlag+"\n");      
      sBuf.append(iName+"-density-"+i+"\t"+s.density+"\n");
      sBuf.append(iName+"-densPrime-"+i+"\t"+s.densPrime+"\n");
      sBuf.append(iName+"-bkgrd-"+i+"\t"+s.bkgrd+"\n");
      sBuf.append(iName+"-mnDens-"+i+"\t"+s.mnDens+"\n");
      sBuf.append(iName+"-totDens-"+i+"\t"+s.totDens+"\n");
      sBuf.append(iName+"-mnDensPrime-"+i+"\t"+s.mnDensPrime+"\n");
      sBuf.append(iName+"-mnBkgrd-"+i+"\t"+s.mnBkgrd+"\n");
      sBuf.append(iName+"-dMax-"+i+"\t"+s.dMax+"\n");
      sBuf.append(iName+"-dMin-"+i+"\t"+s.dMin+"\n");
      sBuf.append(iName+"-dMaxBkgrd-"+i+"\t"+s.dMaxBkgrd+"\n");
      sBuf.append(iName+"-dMinBkgrd-"+i+"\t"+s.dMinBkgrd+"\n");
    }
    
    String dataStr= new String(sBuf);
    if(!flk.fio.writeFileToDisk(sptFile,dataStr))
    {
      String msg= "Problem writing "+sptFile+" file - aborted";
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    
    return(true);
  } /* writeState */
  
      
 /**
  * rmvSpotListFile() - clear the .spt file from the spt/ directory
  * by moving the spt/<imageFile>.spt file into spt/<imageFile>.spt.bkup
  * andclearing the .bkup file if it previously existed first.
  * @param gelFile is the file base name. We strip off the image type
  *        and add ".spt" 
  * @param iName of the image to remove (e.g., "I1", or "I2")
  * @return true if succeed
  */ 
  public static boolean rmvSpotListFile(String gelFile, String iName)
  { /* rmvSpotListFile */   
    String baseName= util.getFileNameFromPath(gelFile);
    baseName= util.rmvFileExtension(baseName);
    String 
      sptFile= flk.userDir + flk.fileSeparator + 
               "spt"+flk.fileSeparator+ baseName+".spt",
      sptBkupFile= sptFile+".bkup";
    
    try
    {
      File fd= new File(sptFile);
      if(!fd.isFile())
        return(false);                   /* there is no file*/
      
      File fdBkup= new File(sptBkupFile);
      if(fdBkup.isFile())
      { /* Delete spt/<imageFile>.spt.bkup */
        fdBkup.delete();        
      }
      fdBkup= new File(sptBkupFile);     /* rename .spt to .spt.bkup file */
      fd.renameTo(fdBkup);
      return(true);
    }
    catch(Exception e)
    {
      return(false);
    }
  } /* rmvSpotListFile */
  
  
 /**
  * cvSpot2Str() - convert spot to printable string
  * @param imageName is image name
  * @param unitsAbbrev is name of units abbreviation else "gray-scale"
  * @return spot as string
  */ 
  public String cvSpot2Str(String imageName, String unitsAbbrev)
  { /* cvSpot2Str */    
    int area= ImageDataMeas.maskArea[circleRadius];
    String
      sQ= (this.useTotDensityFlag) ? "Tot" : "Mean",
      sTotMeasLine= sQ+"Meas: " + util.cvf2s(this.density,3)+
                    " ["+util.cvf2s(this.dMin,3) +":"+
                    util.cvf2s(this.dMax,3) +"] "+ unitsAbbrev,
      sBkgrdLine= (this.xB!=0) 
                     ? sQ+"Bkgrd: " + util.cvf2s(this.bkgrd,3)+        
                     " (" + this.xB + "," + this.yB + ")"+
                     " [" + util.cvf2s(this.dMinBkgrd,3) + ":"+
                       util.cvf2s(this.dMaxBkgrd,3) + "] "+ unitsAbbrev
                     : "", 
      sMeanStats= "\n  mnDens: " + util.cvf2s(this.mnDens,3) + 
                  ", mn(Dens-Bkgrd): " + util.cvf2s(this.mnDensPrime,3) +
                  ((this.xB==0) ? "" : ", mnBkgrd: " +
                  util.cvf2s(this.mnBkgrd,3)) + " "+ unitsAbbrev+"\n",
      sHeaderLine= "[" + this.nbr + "] ";
                  
    /* Finish building the header line */
    if(this.id!=null)
      sHeaderLine += this.id+", ";
    if(this.name!=null)
      sHeaderLine += "["+this.name+"] ";
                  
    String
      shortImageName= flk.util.getFileNameFromPath(imageName),
      sR= sHeaderLine + shortImageName+ 
          " (" + this.xC + "," + this.yC + ")\n";
    
    /* Setup the default measurement scale units */
    if(unitsAbbrev==null || unitsAbbrev.length()==0)
      unitsAbbrev= "gray-value";
    
    /* Make the primary measurement (tot density - bkgrd density) 
     * if there is bkgrd density defined
     */
    if(this.bkgrd!=0)
      sR += "  "+sQ +"(Meas-Bkgrd): " + util.cvf2s(this.densPrime,3)+
            " " + unitsAbbrev + "\n";
    
    /* Then add the uncorrected tot density  which becomes primary
     * if there is no background
     */
    sR += "  " + sTotMeasLine;
    
    /* Add statistics */
    if(this.bkgrd!=0)
      sR += "\n  " + sBkgrdLine;
    sR += sMeanStats;
    
    /* Add measure area features */
    sR += "  CircleMask: " + this.nCirMask + "X" + this.nCirMask +
          " area: "+area+" pixels\n";
    
    return(sR);
  } /* cvSpot2Str */
  
  
  /**
   * listSpotListData() - generate a string list of the spots in the spot list
   * @param imageName is image name
   * @param units is name of units else "gray-scale"
   * @param spotList is list of spots to convert
   * @param nspots to convert
   * @return spot as string
   */
  public static String listSpotListData(String imageName, String units,
                                        Spot spotList[], int nSpots)
  { /* listSpotListData */ 
     int
       nbr= 0,
       xLbl= 0,
       yLbl= 0;
     if(spotList==null || nSpots==0)
       return(null);
     String sR= "\nSpot list for ["+imageName+"] with "+nSpots+" spots\n"+
                "-------------------------------------------------------\n";     
     
     for(int i=0;i<nSpots;i++)
     { /* draw each spot as "+"+<s.nbr> */
       Spot s= spotList[i];
       sR += s.cvSpot2Str(imageName, units);
     } /* draw each spot as "+"+<s.nbr> */
     
     return(sR);
  } /* listSpotListData */
  
    
  /**
   * listSpotListDataTabDelim() - generate a tab-delimited string table
   * of of the spots in the spot list. This could be imported into Excel
   * @param imageName is image name
   * @param unitsAbbrev is name of units abbreviation else "gray-scale"
   * @param spotList is list of spots to convert
   * @param nspots to convert
   * @return spot as string
   */
  public static String listSpotListDataTabDelim(String imageName, 
                                                String unitsAbbrev,
                                                Spot spotList[], int nSpots)
  { /* listSpotListDataTabDelim */ 
     int
       nbr= 0,
       xLbl= 0,
       yLbl= 0;
     if(spotList==null || nSpots==0)
       return(null);
     String
       shortImageName= flk.util.getFileNameFromPath(imageName),
       sFields= "Image\tSpotNbr\tID\tname\tDensity_Mode\tDensity_meas\tUnits\t"+
                "MinDensity\tMaxDensity\tBkgrd\tMinBkgrd\tMaxBkgrd\t"+
                "xC\tyC\txB\tyB\t"+
                "Density-Bkgrd\tMeanDens\t(MeanDens-MeanBkgrd)\tMeanBkgrd\t"+
                "CircleMaskSize\tarea\n";
     String
       sR= sFields;     
     
     for(int i=0;i<nSpots;i++)
     { /* draw each spot as "+"+<s.nbr> */
       Spot s= spotList[i];
       sR += shortImageName;
       sR += "\t" + s.nbr;
       sR += "\t" + (String)((s.id==null) ? ""  : s.id);
       sR += "\t" + (String)((s.name==null) ? ""  : s.name);
       sR += "\t" + (String)((s.useTotDensityFlag) ? "Total" : "Mean");
       sR += "\t" + util.cvf2s(s.density,3);
       sR += "\t" + (String)((unitsAbbrev!=null) ? unitsAbbrev : "gray-scale");
       sR += "\t" + util.cvf2s(s.dMin,3);
       sR += "\t" + util.cvf2s(s.dMax,3);
       
       sR += "\t" + (String)((s.xB==0) ? "0" : util.cvf2s(s.bkgrd,3));
       sR += "\t" + (String)((s.xB==0) ? "0" : util.cvf2s(s.dMinBkgrd,3));
       sR += "\t" + (String)((s.xB==0) ? "0" : util.cvf2s(s.dMaxBkgrd,3));
       
       sR += "\t" + s.xC;
       sR += "\t" + s.yC;
       sR += "\t" + s.xB;
       sR += "\t" + s.yB;
       sR += "\t" + (String)((s.xB==0) 
                               ? util.cvf2s(s.density,3) 
                               : util.cvf2s(s.densPrime,3));
       sR += "\t" + util.cvf2s(s.mnDens,3);
       sR += "\t" + util.cvf2s(s.mnDensPrime,3);
       sR += "\t" + (String)((s.xB==0) 
                               ? "0" : util.cvf2s(s.mnBkgrd,3));
       sR += "\t" + s.nCirMask + "X" + s.nCirMask;
       sR += "\t" + s.area;
       sR += "\n";
     } /* draw each spot as "+"+<s.nbr> */
     
     return(sR);
  } /* listSpotListDataTabDelim */
  
    
  /**
   * listPairedSpotListDataTabDelim() - generate a tab-delimited string table
   * of the paired spots (by matching spot id's) in the spot list.
   * Note:
   *<pre>
   *  1. Both spot list must be measured with either Total or Mean densities,
   *  2. Both spot lists must have the same calibration abbreviation units
   *  3. Both spots that are paired must have the same annotation id spelling
   *     including case.
   *</pre>
   * This could be imported into Excel
   * @param normByMeanSpotListFlag normalize by the mean spot value of spots 
   *          in each of the corresponding spot lists.
   * @return the table as string, else null if no data
   */
  public static String listPairedSpotListDataTabDelim(boolean normByMeanSpotListFlag)
  { /* listPairedSpotListDataTabDelim */ 
    ImageData
      iData1= flk.iData1,
      iData2= flk.iData2;
    int
      nSpots1= iData1.idSL.nSpots,
      nSpots2= iData2.idSL.nSpots;    
    String 
      unitsAbbrev1= iData1.calib.unitsAbbrev,
      unitsAbbrev2= iData2.calib.unitsAbbrev;
    if(nSpots1==0 || nSpots2==0 || !unitsAbbrev1.equals(unitsAbbrev2))
    {
      return(null);      /* no data */
    }
    
    int foundPairsInSpotLists= 0;
    Spot
      spotList1[]= iData1.idSL.spotList,
      spotList2[]= iData2.idSL.spotList;
    String
      imageName1= flk.util.getFileNameFromPath(iData1.imageFile),
      imageName2= flk.util.getFileNameFromPath(iData2.imageFile),
      idList1[]= new String[nSpots1],
      idList2[]= new String[nSpots2];
     
     int
       nbr= 0,
       xLbl= 0,
       yLbl= 0;
     
     String
       sR= "",
       id1,
       id2,
       sFields= "Image1\tImage2\tSpotNbr1\tSpotNbr2\tID\tName"+
                "\tDensityMode\tUnits"+
                "\tD1\tD2\t(D1/D2)"+
                "\t(D1-B1)\t(D2-B2)\t(D1-B1)/(D2-B2)"+
                "\tCircleMask1\tCircleMask2\n";
     float
       sumD1= 0.0F,
       sumD2= 0.0F,
       sumDB1= 0.0F,
       sumDB2= 0.0F,
       mnD1= 1.0F,
       mnD2= 1.0F,
       mnDB1= 1.0F,
       mnDB2= 1.0F,
       v;
    
     /* Compute mean spotList spot density for all spots in each list */
     for(int i=0;i<nSpots1;i++)
     { /* Check image 1 spots */
       Spot s1= spotList1[i];
       idList1[i]= s1.id;
       sumD1 += s1.density;
       sumDB1 += s1.densPrime;
     }
     
     for(int j=0;j<nSpots2;j++)
     { /* Check image 2 spots */
       Spot s2= spotList2[j];
       idList2[j]= s2.id;
       sumD2 += s2.density;
       sumDB2 += s2.densPrime;
     }
       
     if(normByMeanSpotListFlag)
     { /* use the mean spot list normalzations */
       sFields= "Image1\tImage2\tSpotNbr1\tSpotNbr2\tID\tName"+
                "\tDensityMode\tUnits"+
                "\tDm1\tDm2\t(Dm1/Dm2)"+
                "\t(Dm1-Bm1)\t(Dm2-Bm2)\t(Dm1-Bm1)/(Dm2-Bm2)"+
                "\tCircleMask1\tCircleMask2"+
                "\tMnDspotList1\tMnDspotList2\tMnDBspotList1\tMnDBspotList2\n";
       mnD1= sumD1/nSpots1;
       mnDB1= sumDB1/nSpots1;
       mnD2= sumD2/nSpots2;
       mnDB2= sumDB2/nSpots2;
       if(mnD1==0.0F || mnD2==0.0F || mnDB1==0.0F || mnDB2==0.0F)
         return(null);                      /* Bad data */
     }  /* use the mean spot list normalzations */
       
     /* Generate the spot list */
     for(int i=0;i<nSpots1;i++)
     { /* Check image 1 spots */
       Spot s1= spotList1[i];
       id1= idList1[i];
       if(id1==null)
         continue;
       
       for(int j=0;j<nSpots2;j++)
       { /* find matching spot in spot list 2 */         
         Spot s2= spotList2[j];
         id2= idList2[j];
         if(id2==null || !id1.equals(id2))
           continue;              /* must have same spot id */
         if(s1.useTotDensityFlag!=s2.useTotDensityFlag)
           continue;              /* must both be either Total or Mean */
         
         foundPairsInSpotLists++;  /* Count spots passing the test */
         
         /* found spots with the same annotation */
         sR += imageName1;
         sR += "\t" + imageName2;
         
         sR += "\t" + s1.nbr;
         sR += "\t" + s2.nbr;
         sR += "\t" + (String)((s1.id==null) ? ""  : s1.id);
         sR += "\t" + (String)((s1.name==null) ? ""  : s1.name);
         
         sR += "\t" + (String)((s1.useTotDensityFlag) ? "Total" : "Mean");
         sR += "\t" + unitsAbbrev1;
       
         sR += "\t" + util.cvf2s(s1.density/mnD1,3);
         sR += "\t" + util.cvf2s(s2.density/mnD2,3);         
         v= (s2.density==0.0F)
               ? 0.0F : ((s1.density/mnD1)/(s2.density/mnD2));
         sR += "\t" + util.cvf2s(v,3);
               
         sR += "\t" + util.cvf2s(s1.densPrime/mnDB1,3);
         sR += "\t" + util.cvf2s(s2.densPrime/mnDB2,3); 
         v= (s2.densPrime==0.0F)
               ? 0.0F : ((s1.densPrime/mnDB1)/(s2.densPrime/mnDB2));
         sR += "\t" + util.cvf2s(v,3);
                  
         sR += "\t" + s1.nCirMask + "X" + s1.nCirMask;
         sR += "\t" + s2.nCirMask + "X" + s2.nCirMask;
                           
         if(normByMeanSpotListFlag)
         { /* use the mean spot list normalzations */
           sR += "\t" + util.cvf2s(mnD1,3);
           sR += "\t" + util.cvf2s(mnD2,3);
           sR += "\t" + util.cvf2s(mnDB1,3);
           sR += "\t" + util.cvf2s(mnDB2,3);
         } /* use the mean spot list normalzations */
         
         sR += "\n";
       } /* find matching spot in spot list 2 */
     } /* Check image 1 spots */
     
     if(foundPairsInSpotLists>0)
     { /* build output string */
       sR= sFields + sR;
     }
     else
     { /* error */
       sR= null;
     }
     
     return(sR);
  } /* listPairedSpotListDataTabDelim */
    
  
  /**
   * getPreferredSize() - get the preferred size
   * @return window size
   */
  public Dimension getPreferredSize()
  { /* getPreferredSize*/
    return(new Dimension(preferredWidth, preferredHeight));
  } /* getPreferredSize */
  
  
  /**
   * getMinimumSize() - get the minimum preferred size
   * @return window size
   */
  public Dimension getMinimumSize()
  { /* getMinimumSize */
    return(new Dimension(POPUP_WIDTH, POPUP_HEIGHT));
  } /* getMinimumSize */
  
  
  
  /**
   * positionWindow() - position window on the screen
   */
  public void positionWindow(Frame f) 
  { /* positionWindow */
    /* Center frame on the screen, PC only */
    Dimension 
      spotEditWindSize= f.getSize(),
      flkSize= flk.getSize(),
      screen= Toolkit.getDefaultToolkit().getScreenSize();
    int
      xPos= ((flkSize.width+spotEditWindSize.width) < screen.width) 
              ? (flkSize.width +10)
              : (screen.width - spotEditWindSize.width)/2,
      yPos= (flkSize.height - spotEditWindSize.height)/2;
    if(xPos<0 || yPos<0)
    { /* came up too soon */      
      xPos= (screen.width - spotEditWindSize.width)/2; 
      yPos= (screen.height - spotEditWindSize.height)/2;
    }
    Point pos= new Point(xPos,yPos);
    f.setLocation(pos);
  } /* positionWindow */
  
 
  /**
   * popupSpotEdit() - popup a spot editing function to change the spot.
   * @param editOnlyIDflag if only want to edit/assign the spot id
   * @return true if any fields changed.
   */
  public boolean popupSpotEdit(boolean editOnlyIDflag)
  { /* popupSpotEdit */    
    if(fOne!=null)
    {      
      String msg=
       "Already editing this spot - finish editing before start another edit.";
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    
    String baseName= util.getFileNameFromPath(flk.imageFile1);
    baseName= util.rmvFileExtension(baseName);
    
    if(editOnlyIDflag)
    { /* define the ID in a simple popup for this spot */
      String
        sID= (this.id==null) ? "<none>" : this.id,
        idStr= (this.id==null) ? "" : this.id,
        msg2= "Edit spot annotation id' value for spot#"+this.nbr+
             " id='"+sID+"' ["+baseName+"]";
      PopupTextFieldDialog ptfd= new PopupTextFieldDialog(flk,msg2,idStr);
      if(ptfd.okFlag)
      {
        this.id= ptfd.answer;
        flk.repaint();
      }
      return(ptfd.okFlag);
    } /* define the ID in a simple popup for this spot */
    
    fOne= new Frame("Edit spot #"+this.nbr+" ["+baseName+"]");
    Panel
      statusPanel= new Panel(),     /* North */
      editPanel= new Panel(),       /* center */
      controlPanel= new Panel();    /* South */    
   
    /* Add listener to the frame */      
    fOne.addWindowListener(this);  
    /* Add components to frame */
    fOne.setLayout(new BorderLayout());
    
    /* Add a label to the statusPanel */    
    statusPanel.setLayout(new BorderLayout());
    statusLabel=
      new Label("                                                          ");
    statusPanel.add("North", statusLabel);
    fOne.add("North", statusPanel);        
    
    /* Create the above GUI and text event handlers */
    String sVal= "";    
    TextField tf;
    editPanel.setLayout(new GridLayout(nRows,2));      
    spotTable= new TextField[nRows][2];
    for(int r= 0;r<nRows;r++)
    { /* build rows in the table */
      for(int c=0;c<2;c++)
      {
        tf= new TextField("");
        spotTable[r][c]= tf;
        editPanel.add("Center",tf);
        boolean editableFlag= (c==1);
        tf.setEditable(editableFlag);
        Color
          bg= (editableFlag) ? Color.lightGray : Color.white,
          fg= (editableFlag) ? Color.black : Color.black;
        tf.setForeground(fg);
        tf.setBackground(bg);
      }
      
      /* put data into the table */
      spotTable[r][0].setText(fNames[r]);
      sVal= getValBySpotFieldName(fNames[r]);
      spotTable[r][1].setText(sVal);
    } /* build rows in the table */
    
    fOne.add("East", editPanel);
    
    /* Configure controlPanel layout */
    fOne.add("South", controlPanel);     /* Seems to work best this way */
    
    /* Add buttons */ 
    Button b;
    b= new Button("Done");
    b.setActionCommand("DoneSingle");
    b.addActionListener(this);
    controlPanel.add(b);
    
    b= new Button("Cancel");
    b.addActionListener(this);
    controlPanel.add(b);
    
    /* Realize it but don't display it */
    fOne.pack();
    fOne.setSize(preferredWidth, preferredHeight);
    positionWindow(fOne);
         
    fOne.setVisible(true);    /* enable it later... */
    
    return(true);
  } /* popupSpotEdit */
  
 
  /**
   * popupSpotEditBoth() - popup a spot editing function to let you
   * edit two selected (putatively paired) spots together
   * Edit the data in 3 columns with the field name on the left column
   * the left image data in the middle column and the right image data in 
   * the right column.
   * @param s1 is first spot
   * @param s2 is second spot
   * @param editOnlyIDflag if only want to edit/assign the spot id
   * @return true if any fields changed.
   */
  public boolean popupSpotEditBoth(Spot s1, Spot s2,
                                   boolean editOnlyIDflag)
  { /* popupSpotEditBoth */    
    if(fBoth!=null)
    {      
      String msg=
       "Already editing two spots - finish editing before start another edit.";
      util.popupAlertMsg(msg, flk.alertColor);
      return(false);
    }
    
    this.s1= s1;            /* only set if editing two spots */
    this.s2= s2;
    
    String
      baseName1= util.getFileNameFromPath(flk.imageFile1),
      baseName2= util.getFileNameFromPath(flk.imageFile2);
    baseName1= util.rmvFileExtension(baseName1);
    baseName2= util.rmvFileExtension(baseName2);
    
    if(editOnlyIDflag)
    { /* define the ID in a simple popup for both spots */
      String
        s1ID= (s1.id==null) ? "<none>" : s1.id,
        s2ID= (s2.id==null) ? "<none>" : s2.id,
        idStr= (s1.id==null && s2.id==null) 
                  ? "" 
                  : ((s1.id!=null) 
                       ? s1.id          /* Pick s1 over s2 if both defined*/
                      : ((s2.id!=null)
                            ? s2.id
                            : "")),
        msg2= "Edit spot annotation 'id' value for left spot#"+s1.nbr+
             " id='"+s1ID+"' ["+baseName1+
             "], and right spot #"+s2.nbr+" id='"+s2ID+"' ["+baseName2+"]";
      PopupTextFieldDialog ptfd= new PopupTextFieldDialog(flk,msg2,idStr);
      if(ptfd.okFlag)
      { /* Stuff them with the SAME IDs */
        s1.id= ptfd.answer;
        s2.id= ptfd.answer;
        flk.repaint();
      }
      return(ptfd.okFlag);
    } /* define the ID in a simple popup for both spots */
    
    fBoth= new Frame("Edit left spot #"+s1.nbr+" ["+baseName1+"]"+
                     ", and right spot #"+s2.nbr+" ["+baseName2+"]");
    Panel
      statusPanel= new Panel(),     /* North */
      editPanel= new Panel(),       /* center */
      controlPanel= new Panel();    /* South */    
   
    /* Add listener to the frame */      
    fBoth.addWindowListener(this);  
    /* Add components to frame */
    fBoth.setLayout(new BorderLayout());
    
    /* Add a label to the statusPanel */    
    statusPanel.setLayout(new BorderLayout());
    statusLabel=
      new Label("                                                          ");
    statusPanel.add("North", statusLabel);
    fBoth.add("North", statusPanel);        
    
    /* Create the above GUI and text event handlers */
    String sVal1= "";    
    String sVal2= "";    
    TextField tf;
    editPanel.setLayout(new GridLayout(nRows,3));      
    spotTableBoth= new TextField[nRows][3];
    for(int r= 0;r<nRows;r++)
    { /* add a row to the table */
      for(int c=0;c<3;c++)
      {
        tf= new TextField("");
        spotTableBoth[r][c]= tf;
        editPanel.add("Center",tf);
        boolean editableFlag= (c>=1);
        tf.setEditable(editableFlag);
        Color
          bg= (editableFlag) ? Color.lightGray : Color.white,
          fg= (editableFlag) ? Color.black : Color.black;
        tf.setForeground(fg);
        tf.setBackground(bg);
      }
      
      /* put data into the table */
      spotTableBoth[r][0].setText(fNames[r]);
      sVal1= s1.getValBySpotFieldName(fNames[r]);
      spotTableBoth[r][1].setText(sVal1);
      sVal2= s2.getValBySpotFieldName(fNames[r]);
      spotTableBoth[r][2].setText(sVal2);
    } /* adda row to the table */
    
    fBoth.add("East", editPanel);
    
    /* Configure controlPanel layout */
    fBoth.add("South", controlPanel);     /* Seems to work best this way */
    
    /* Add buttons */ 
    Button b;
    b= new Button("Done");
    b.setActionCommand("DoneBoth");
    b.addActionListener(this);
    controlPanel.add(b);
    
    b= new Button("Cancel");
    b.addActionListener(this);
    controlPanel.add(b);
    
    /* Realize it but don't display it */
    fBoth.pack();
    fBoth.setSize((int)(1.5*preferredWidth), preferredHeight);
    positionWindow(fBoth);
         
    fBoth.setVisible(true);    /* enable it later... */
    
    return(true);
  } /* popupSpotEditBoth */


  /**
   * setStatusMsg() - set the status msg
   * @param msg is the status window msg
   */
  public void setStatusMsg(String msg)
  { 
    statusLabel.setForeground(Color.black);
    statusLabel.setText(msg); 
  } 
  
  
  /**
   * setStatusMsg() - set the status msg
   * @param msg is the status window msg
   * @param color to draw it 
   */
  public void setStatusMsg(String msg, Color color)
  { 
    statusLabel.setForeground(color);
    statusLabel.setText(msg); 
  } 
  
  
  /**
   * getValBySpotFieldName() - get spot value string by spot field name
   * @param name of spot field
   * @return null if not found, else string value
   */
  public String getValBySpotFieldName(String sName)
  { /* getValBySpotFieldName */
    String sVal= null;
    
    if(sName.equals("nGel"))
      sVal= (""+nGel);
    else if(sName.equals("nbr"))
      sVal= (""+nbr);
    else if(sName.equals("id"))
      sVal= id;
    else if(sName.equals("name"))
      sVal= name;
    else if(sName.equals("nCirMask"))
      sVal= (""+nCirMask);
    else if(sName.equals("circleRadius"))
      sVal= (""+circleRadius);
    else if(sName.equals("xC"))
      sVal= (""+xC);
    else if(sName.equals("yC"))
      sVal= (""+yC);
    else if(sName.equals("xB"))
      sVal= (""+xC);
    else if(sName.equals("yB"))
      sVal= (""+yB);
    else if(sName.equals("area"))
      sVal= (""+area);
    else if(sName.equals("isCalibFlag"))
      sVal= (""+isCalibFlag);
    else if(sName.equals("useTotDensityFlag"))
      sVal= (""+useTotDensityFlag);
    else if(sName.equals("density"))
      sVal= (""+density);
    else if(sName.equals("densPrime"))
      sVal= (""+densPrime);
    else if(sName.equals("bkgrd"))
      sVal= (""+bkgrd);
    else if(sName.equals("mnDens"))
      sVal= (""+mnDens);
    else if(sName.equals("totDens"))
      sVal= (""+totDens);
    else if(sName.equals("mnDensPrime"))
      sVal= (""+mnDensPrime);
    else if(sName.equals("mnBkgrd"))
      sVal= (""+mnBkgrd);
    else if(sName.equals("dMax"))
      sVal= (""+dMax);
    else if(sName.equals("dMin"))
      sVal= (""+dMin);
    else if(sName.equals("dMaxBkgrd"))
      sVal= (""+dMaxBkgrd);
    else if(sName.equals("dMinBkgrd"))
      sVal= (""+dMinBkgrd);
    
    return(sVal);           
  } /* getValBySpotFieldName */
  
  
  /**
   * updateSpotFromEditedSpotTable() - set the spot from the table data
   */
  private boolean updateSpotFromEditedSpotTable()
  { /* updateSpotFromEditedSpotTable */
    changedFlag= true;
    String
      sName,
      sVal;
    
    for(int r= 0;r<nRows;r++)
    { /* process the data */
      sName= spotTable[r][0].getText();      
      sVal= spotTable[r][1].getText();
      
      if(sName.equals("nGel"))
        nGel= util.cvs2i(sVal,0);
      else if(sName.equals("nbr"))
        nbr= util.cvs2i(sVal,0);
      else if(sName.equals("id"))
        id= sVal;
      else if(sName.equals("name"))
        name= sVal;
      else if(sName.equals("nCirMask"))
        nCirMask= util.cvs2i(sVal,0);
      else if(sName.equals("circleRadius"))
        circleRadius= util.cvs2i(sVal,0);
      else if(sName.equals("xC"))
        xC= util.cvs2i(sVal,0);
      else if(sName.equals("yC"))
        yC= util.cvs2i(sVal,0);
      else if(sName.equals("xB"))
        xC= util.cvs2i(sVal,0);
      else if(sName.equals("yB"))
        yB= util.cvs2i(sVal,0);
      else if(sName.equals("area"))
        area= util.cvs2i(sVal,0);
      else if(sName.equals("isCalibFlag"))
        isCalibFlag= sVal.equals("true");
      else if(sName.equals("useTotDensityFlag"))
        useTotDensityFlag= sVal.equals("true");
      else if(sName.equals("density"))
        density= util.cvs2f(sVal,0.0F);
      else if(sName.equals("densPrime"))
        densPrime= util.cvs2f(sVal,0.0F);
      else if(sName.equals("bkgrd"))
        bkgrd= util.cvs2f(sVal,0.0F);
      else if(sName.equals("mnDens"))
        mnDens= util.cvs2f(sVal,0.0F);
      else if(sName.equals("totDens"))
        totDens= util.cvs2f(sVal,0.0F);
      else if(sName.equals("mnDensPrime"))
        mnDensPrime= util.cvs2f(sVal,0.0F);
      else if(sName.equals("mnBkgrd"))
        mnBkgrd= util.cvs2f(sVal,0.0F);
      else if(sName.equals("dMax"))
        dMax= util.cvs2f(sVal,0.0F);
      else if(sName.equals("dMin"))
        dMin= util.cvs2f(sVal,0.0F);
      else if(sName.equals("dMaxBkgrd"))
        dMaxBkgrd= util.cvs2f(sVal,0.0F);
      else if(sName.equals("dMinBkgrd"))
        dMinBkgrd= util.cvs2f(sVal,0.0F);
    } /* process the data */
      
    return(changedFlag);
  } /* updateSpotFromEditedSpotTable */
  
  
  /**
   * updateBothSpotsFromEditedSpotTable() - set both spots from the 
   * spotTableBoth table data
   */
  private boolean updateBothSpotsFromEditedSpotTable()
  { /* updateBothSpotsFromEditedSpotTable */
    changedFlag= true;
    String
      sName,
      sVal1,
      sVal2;
    
    for(int r= 0;r<nRows;r++)
    { /* process the data */
      sName= spotTableBoth[r][0].getText();      
      sVal1= spotTableBoth[r][1].getText();      
      sVal2= spotTableBoth[r][2].getText();
      
      if(sName.equals("nGel"))
      {
        s1.nGel= util.cvs2i(sVal1,0);
        s2.nGel= util.cvs2i(sVal2,0);
      }
      else if(sName.equals("nbr"))
      {
        s1.nbr= util.cvs2i(sVal1,0);
        s2.nbr= util.cvs2i(sVal2,0);
      }
      else if(sName.equals("id"))
      {
        s1.id= sVal1;
        s2.id= sVal2;
      }
      else if(sName.equals("name"))
      {
        s1.name= sVal1;
        s2.name= sVal2;
      }
      else if(sName.equals("nCirMask"))
      {
        s1.nCirMask= util.cvs2i(sVal1,0);
        s2.nCirMask= util.cvs2i(sVal2,0);
      }
      else if(sName.equals("circleRadius"))
      {
        s1.circleRadius= util.cvs2i(sVal1,0);
        s2.circleRadius= util.cvs2i(sVal2,0);
      }
      else if(sName.equals("xC"))
      {
        s1.xC= util.cvs2i(sVal1,0);
        s2.xC= util.cvs2i(sVal2,0);
      }
      else if(sName.equals("yC"))
      {
        s1.yC= util.cvs2i(sVal1,0);
        s2.yC= util.cvs2i(sVal2,0);
      }
      else if(sName.equals("xB"))
      {
        s1.xC= util.cvs2i(sVal1,0);
        s2.xC= util.cvs2i(sVal2,0);
      }
      else if(sName.equals("yB"))
      {
        s1.yB= util.cvs2i(sVal1,0);
        s2.yB= util.cvs2i(sVal2,0);
      }
      else if(sName.equals("area"))
      {
        s1.area= util.cvs2i(sVal1,0);
        s2.area= util.cvs2i(sVal2,0);
      }
      else if(sName.equals("isCalibFlag"))
      {
        s1.isCalibFlag= sVal1.equals("true");
        s2.isCalibFlag= sVal2.equals("true");
      }
      else if(sName.equals("useTotDensityFlag"))
      {
        s1.useTotDensityFlag= sVal1.equals("true");
        s2.useTotDensityFlag= sVal2.equals("true");
      }
      else if(sName.equals("density"))
      {
        s1.density= util.cvs2f(sVal1,0.0F);
        s2.density= util.cvs2f(sVal2,0.0F);
      }
      else if(sName.equals("densPrime"))
      {
        s1.densPrime= util.cvs2f(sVal1,0.0F);
        s2.densPrime= util.cvs2f(sVal2,0.0F);
      }
      else if(sName.equals("bkgrd"))
      {
        s1.bkgrd= util.cvs2f(sVal1,0.0F);
        s2.bkgrd= util.cvs2f(sVal2,0.0F);
      }
      else if(sName.equals("mnDens"))
      {
        s1.mnDens= util.cvs2f(sVal1,0.0F);
        s2.mnDens= util.cvs2f(sVal2,0.0F);
      }
      else if(sName.equals("totDens"))
      {
        s1.totDens= util.cvs2f(sVal1,0.0F);
        s2.totDens= util.cvs2f(sVal2,0.0F);
      }
      else if(sName.equals("mnDensPrime"))
      {
        s1.mnDensPrime= util.cvs2f(sVal1,0.0F);
        s2.mnDensPrime= util.cvs2f(sVal2,0.0F);
      }
      else if(sName.equals("mnBkgrd"))
      {
        s1.mnBkgrd= util.cvs2f(sVal1,0.0F);
        s2.mnBkgrd= util.cvs2f(sVal2,0.0F);
      }
      else if(sName.equals("dMax"))
      {
        s1.dMax= util.cvs2f(sVal1,0.0F);
        s2.dMax= util.cvs2f(sVal2,0.0F);
      }
      else if(sName.equals("dMin"))
      {
        s1.dMin= util.cvs2f(sVal1,0.0F);
        s2.dMin= util.cvs2f(sVal2,0.0F);
      }
      else if(sName.equals("dMaxBkgrd"))
      {
        s1.dMaxBkgrd= util.cvs2f(sVal1,0.0F);
        s2.dMaxBkgrd= util.cvs2f(sVal2,0.0F);
      }
      else if(sName.equals("dMinBkgrd"))
      {
        s1.dMinBkgrd= util.cvs2f(sVal1,0.0F);
        s2.dMinBkgrd= util.cvs2f(sVal2,0.0F);
      }
    } /* process the data */
      
    return(changedFlag);
  } /* updateBothSpotsFromEditedSpotTable */
      
  
  /**
   * actionPerformed() - Handle Control panel button clicks
   * @param e is ActionEvent for buttons in control panel
   */
  public void actionPerformed(ActionEvent e)
  { /* actionPerformed */
    String cmd= e.getActionCommand();
    Button item= (Button)e.getSource();
        
    if(cmd.equals("DoneSingle"))
    {     
      updateSpotFromEditedSpotTable();
      close();
    }
    
     else if(cmd.equals("DoneBoth"))
    {     
      updateBothSpotsFromEditedSpotTable();
      close();
    }
    
    else if (cmd.equals("Cancel"))
    {
      close();
    }
  } /* actionPerformed */
    
  
  /**
   * close() - close this popup and reset flags if needed
   * [TODO] If they edited the calibration and peak calibration table,
   * and they did not save the calibration (i.e. calibChangedFlag is true),
   * then ask them here and save it if they say yes. If not,
   * then restore the previous calibration.
   */
  public void close()
  { /* close */  
    if(fOne!=null)
    {
      fOne.dispose();      /* kill this instance for good! */  
      fOne= null;
    }
    
    if(fBoth!=null)
    {
      fBoth.dispose();     /* kill this instance for good! */  
      fBoth= null;
    }
    
    flk.repaint();         /* repaint since may have change some values */
  } /* close */
  
  
  /**
   * windowClosing() - close down the window.
   * @param e is window closing event
   * @see #close
   */
  public void windowClosing(WindowEvent e)
  { close(); }
  
  
  /* Others not used at this time */
  public void windowOpened(WindowEvent e)  { }
  public void windowActivated(WindowEvent e)  { }
  public void windowClosed(WindowEvent e)  { }
  public void windowDeactivated(WindowEvent e)  { }
  public void windowDeiconified(WindowEvent e)  { }
  public void windowIconified(WindowEvent e)  { }
  
  
  
} /* end of Spot */


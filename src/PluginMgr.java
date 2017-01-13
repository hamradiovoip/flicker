/* File: PluginMgr.java  */

import java.lang.*;

/**
 * PluginMgr base class is used for managing user Plugin{n}'s for Flicker.
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

public class PluginMgr
{ /* PluginMgr */
  /** global instance */
  static Flicker
    flk;
  
  /** max # of external functions alloc'ed*/
  final static int 
    MAX_EXTERN_FCTS= 3; 
  /** name of external function command */
  public static String
    efName[]= new String[MAX_EXTERN_FCTS+1]; 
   /** opt. info on external functions */
  public static String 
    efInfo[]= new String[MAX_EXTERN_FCTS+1];  
  /** which external functions appears in menu */
  public static String
    efMnuName[]= new String[MAX_EXTERN_FCTS+1]; 
   /** if external function is used */
  public static boolean
    efIsActive[]= new boolean[MAX_EXTERN_FCTS+1];
  /** external function number 1 to n */
  public static int
    efNbr[]= new int[MAX_EXTERN_FCTS+1];    
  /** # of external functions */
  public static int
    nExtFcts= 0;
  /** # of active plugins found */ 
  public static int
    nPluginsFound= 0;			      
  
  /* Add more when add more plugins */
  public static Plugin1 
    ef1;
  public static Plugin2 
    ef2;
  public static Plugin3 
    ef3;
  /* ... add more ... */

  /* These are defined by the external function function */
  /** name of command */
  public String
    name;		
  /** what appears in menu */
  public String
    mnuName;	
  /** optional help message for function */
  public String
    info;	
  /** set if this instance is active */
  public static boolean
    isActive;		
  /** function number */
  public static int
    extFctNbr;			

  
  /**
   * PluginMgr() - constructor
   */
  public PluginMgr()
  {
  }
  
  
  /**
   * PluginMgr() - constructor called from main to initialize things
   */
  public PluginMgr(Flicker flkS)
  { /* PluginMgr */
    flk= flkS;
    
    /* Init the plugin database */
    nPluginsFound= 0;
    nExtFcts= 0;
    
    efName= new String[MAX_EXTERN_FCTS+1];
    efInfo= new String[MAX_EXTERN_FCTS+1];
    efMnuName= new String[MAX_EXTERN_FCTS+1];
    efIsActive= new boolean[MAX_EXTERN_FCTS+1];
    efNbr= new int[MAX_EXTERN_FCTS+1];    
    
    /* Setup the plugins in case we need to call them later...*/
    ef1= new Plugin1();
    pushEFentry(ef1.name, ef1.mnuName, ef1.info, ef1.isActive,
                ef1.extFctNbr);
    
    ef2= new Plugin2();
    pushEFentry(ef2.name, ef2.mnuName, ef2.info, ef2.isActive,
                ef2.extFctNbr);
    
    ef3= new Plugin3();
    pushEFentry(ef3.name, ef3.mnuName, ef3.info, ef3.isActive,
                ef3.extFctNbr);
    /* ... add more ... */
  } /* PluginMgr */
    
  
  /**
   * pushEFentry() - push extern fuctnion ef into list.
   * @param name of the function to push
   * @param mnuName is the menu name
   * @param info associated with the function
   * @param isActive when start up
   * @param extFctNbr is the external function number
   */
  private static void pushEFentry(String name, String mnuName, String info,
                                  boolean isActive, int extFctNbr)
  { /* pushEFentry */
    ++nExtFcts;
    efName[nExtFcts]= name;
    efMnuName[nExtFcts]= mnuName;
    efInfo[nExtFcts]= info;
    efIsActive[nExtFcts]= isActive;
    efNbr[nExtFcts]= extFctNbr;
    if(isActive)
      nPluginsFound++;
  } /* pushEFentry */
  
  
  /**
   * fctCalc() - called to perform the image function
   * @param flk is instance of flicker
   * @param efNbr is the external function number
   * @param iData image data to process
   * @param oPix is output pixel array to put results
   * @return false if failed.
   */
  public static boolean fctCalc(Flicker flk, int efNbr, 
                                ImageData iData, int oPix[])
  { /* fctCalc */
    boolean flag= false;
    switch(efNbr)
    {
      case 1:
        flag= ef1.fctCalc(flk, iData,oPix);
      case 2:
        flag= ef2.fctCalc(flk, iData,oPix);
      case 3:
        flag= ef3.fctCalc(flk, iData,oPix);
        /* ... add more ... */
      default:
        flag= false;                	/* failed */
    }
    
    return(flag);
  } /* fctCalc */
  
  
} /* End of PluginMgr */

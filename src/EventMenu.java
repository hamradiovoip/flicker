/* File: EventMenu.java */

import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.EventListener;
import java.lang.*; 
import javax.swing.JComponent.*;
import javax.swing.*;
//import javax.swing.event.*;

/**
 * EventMenu class supports menu item event handling for Flicker 
 * top level GUI.
 *<P>
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government.  As a work of
 * the United States Government there is no associated copyright.  It is
 * offered as open source software under the Mozilla Public License
 * (version 1.1) subject to the limitations noted in the accompanying
 * LEGAL file. This notice must be included with the code. The Flicker 
 * Mozilla and Legal files are available on
 * http://open2dprot.sourceforge.net/Flicker/
 *<P>
 * @author P. Lemkin (LECB/NCI), G. Thornwall (SAIC), Frederick, MD
 * @version $Date$   $Revision$
 * @see <A HREF="http://open2dprot.sourceforge.net/Flicker">Flicker Home</A>
 */

public class EventMenu implements ItemListener, ActionListener
{ /*  EventMenu */ 
  /** instances of global classes */
  private Flicker
    flk; 
  private BuildGUI
    bGui;
  private Util
    util;
  public Popup
    popup;
  private ImageIO
    imgIO;
  private Landmark
    lms;
  private Windump
    windmp;
  
  /* from constructor */
  public int 
    id;				
  /* from constructor */
  public String
    name;			
  /* last selected ImageScroller */
  private static ImageScroller
    lastIS;				
  /* last selected image file name */
  private static String
    imgFile;			
  /* for last selected ImageData instance */
  private static ImageData 
    iData= null;
  /** Flag that indicates the currently selected image
   * is either left or right. It is false for "flicker" or none.
   **/
  private static boolean
    leftOrRightSelectedFlag= false;
    
  /**
   * EventMenu() - constructor
   * @param flk is Flicker instance
   */
  public EventMenu(Flicker flk, BuildGUI bGui)
  { /* EventMenu */
    this.flk= flk;
    this.bGui= bGui;
    this.util= flk.util;
    this.popup= new Popup(flk);
    this.imgIO= flk.imgIO;
    this.lms= flk.lms;       
    this.windmp= flk.windmp;
    
    this.id= 0;		
    this.name= "*INIT*";
  } /* EventMenu */

  
  /**
   * EventMenu() - constructor assigns name and id to object.
   * @param name to assign
   * @param id to assign
   */
  public EventMenu(String name, int id)
  { /* EventMenu */
    this.id= id;
    this.name= name;
  } /* EventMenu */
  
    
  /**
   * lookupCanvasPtrAndName() - lookup current image base on activeImage.
   * Set the instance variables
   *<PRE>
   *  instance  Image         Image         Image       Image
   *  variable  "left"        "right"       "flicker"   none
   *  ========  ======        =======       =========   ====
   *  iData     fli.iData1    flk.iData2    null        null
   *  lastIS    flk.i1IS      flk.i2IS      flk.flkIS   null
   *  imgFile   iData.imgName iData.imgName "flicker"   "no image"
   *
   * Then set leftOrRightSelectedFlag if the "left" or "right" 
   * image was selected.
   *</PRE>
   * @return leftOrRightSelectedFlag 
   */
  private boolean lookupCanvasPtrAndName()
  { /* lookupCanvasPtrAndName */  
    leftOrRightSelectedFlag= false;
    if(flk.activeImage.equals("left"))
    {
      lastIS= flk.i1IS;
      iData= flk.iData1;
      imgFile= flk.iData1.imageFile;
      leftOrRightSelectedFlag= true;
    }
    else if(flk.activeImage.equals("right"))
    {
      lastIS= flk.i2IS;
      iData= flk.iData2;
      imgFile= flk.iData2.imageFile;
      leftOrRightSelectedFlag= true;
    }
    else if(flk.activeImage.equals("flicker"))
    {
      lastIS= flk.flkIS;
      iData= null;
      imgFile= "flicker";
    }
    else
    {
      lastIS= null;	/* no image */
      iData= null;
      imgFile= "no image";
    }
    
    return(leftOrRightSelectedFlag);
  } /* lookupCanvasPtrAndName */ 
    
    
  /**
   * setWindmpSizeCheckbox() - set checkbox radio button
   * and process event for windmp size checkboxes
   * @param cb checkbox item is one of bGui.mi_WinDmpSizeNxNCB
   * @param size is the size X size window in the range of 5 to 40.
   */
  public void setWindmpSizeCheckbox(int size)
  { /* setWindmpSizeCheckbox */
    flk.maxColsToPrint= size;    
    bGui.mi_WinDmpSize5x5CB.setState(size==5);   
    bGui.mi_WinDmpSize10x10CB.setState(size==10);   
    bGui.mi_WinDmpSize15x15CB.setState(size==15);   
    bGui.mi_WinDmpSize20x20CB.setState(size==20);   
    bGui.mi_WinDmpSize25x25CB.setState(size==25);   
    bGui.mi_WinDmpSize30x30CB.setState(size==30);   
    bGui.mi_WinDmpSize35x35CB.setState(size==35);   
    bGui.mi_WinDmpSize40x40CB.setState(size==40);
   } /* setWindmpSizeCheckbox */
  
  
  /**
   * setWindmpRadixCheckbox() - set checkbox radio button
   * and process event for windmp radix checkboxes
   * @param cb checkbox item is one of bGui.mi_WinDmpRadix_XXX_CB
   * @param radix is the radix to set Windump.SHOW_XXXXX
   */
  public void setWindmpRadixCheckbox(int radix)
  { /* setWindmpRadixCheckbox */
    flk.winDumpRadix= radix;    
    bGui.mi_WinDmpRadixDecCB.setState(radix==Windump.SHOW_DECIMAL);
    bGui.mi_WinDmpRadixOctCB.setState(radix==Windump.SHOW_OCTAL);
    bGui.mi_WinDmpRadixHexCB.setState(radix==Windump.SHOW_HEX);
    bGui.mi_WinDmpRadixODCB.setState(radix==Windump.SHOW_OD);
   } /* setWindmpRadixCheckbox */
  
  
  /**
   * setAccessServerDBcheckbox() - set checkbox radio button
   * afor server access DB
   * @param cmb checkbox menu item
   */
  public void setAccessServerDBcheckbox(CheckboxMenuItem cmb)
  { /* setAccessServerDBcheckbox */
    boolean flag= cmb.getState();
        
    flk.useSwiss2DpageServerFlag= 
                       (flag && (bGui.mi_useSwiss2DpageServerCB==cmb));
    bGui.mi_useSwiss2DpageServerCB.setState(flk.useSwiss2DpageServerFlag);
    
    flk.usePIRUniprotServerFlag= 
                       (flag && (bGui.mi_usePIRUniprotServerCB==cmb));
    bGui.mi_usePIRUniprotServerCB.setState(flk.usePIRUniprotServerFlag);
    
    flk.usePIRiProClassServerFlag=
                        (flag && (bGui.mi_usePIRiProClassServerCB==cmb));
    bGui.mi_usePIRiProClassServerCB.setState(flk.usePIRiProClassServerFlag);
    
    flk.usePIRiProLinkFlag=
                        (flag && (bGui.mi_usePIRiProLinkServerCB==cmb));
    bGui.mi_usePIRiProLinkServerCB.setState(flk.usePIRiProLinkFlag);
   } /* setAccessServerDBcheckbox */

  
  /**
   * itemStateChanged() - handle menu item state changed events
   * @param e is ItemEvent
   */
  public void itemStateChanged(ItemEvent e)
  { /* itemStateChanged */  
    Object obj= e.getSource();
    Checkbox bogusCB= new Checkbox();
    JCheckBox bogusJCB= new JCheckBox();
    
    CheckboxMenuItem bogusMCB= new CheckboxMenuItem();
    Checkbox cbItem= (obj instanceof Checkbox)
                        ? (Checkbox)obj : bogusCB;
                        
    JCheckBox jcbItem= (obj instanceof JCheckBox)
                        ? (JCheckBox)obj : bogusJCB;
                        
    CheckboxMenuItem cbmItem= (obj instanceof CheckboxMenuItem)
                                 ? (CheckboxMenuItem)obj : bogusMCB;

    String cbmName= cbmItem.getLabel();
    
    /* In case needed, lookup current iData, lastIS, and imgFile */
    leftOrRightSelectedFlag= lookupCanvasPtrAndName();  

    /* ............... CheckBoxeMenuItems ................ */
      
    if(bGui.mi_useLogTIFFfilesCB==cbmItem)
    { /* "Use linear else log of TIFF files > 8-bits" */
      flk.useLogInputFlag= !cbmItem.getState();
      util.showMsg(((flk.useLogInputFlag)
                     ? "Taking log of Tiff file data > 8-bits"
                     : "Scaling Tiff file data > 8 bits"),
                  Color.black);
    }
   
    else if(bGui.mi_flickerCB==cbmItem)
    { /* "Flicker images (C-F)" */
      flk.flickerFlag= cbmItem.getState();
      bGui.flickerCheckbox.setSelected(flk.flickerFlag);  /* linked GUI */
      util.showMsg(((flk.flickerFlag)
                     ? "Flickering is ON"
                     : "Flickering is OFF"),
                  Color.black);
    }
  
    else if(bGui.mi_ViewLmsCB==cbmItem)
    {
      flk.viewLMSflag= cbmItem.getState();
      util.showMsg(((flk.viewLMSflag)
                     ? "Show landmark overlays"
                     : "Don't show landmark overlays"),
                  Color.black);
    }    
    
    else if(bGui.mi_ViewTargetCB==cbmItem)
    {
      flk.viewTargetFlag= cbmItem.getState(); 
      util.showMsg(((flk.viewTargetFlag)
                     ? "Show target overlay"
                     : "Don't s show target overlay"),
                  Color.black);
    }   
    
    else if(bGui.mi_ViewTrialObjCB==cbmItem)
    {
      flk.viewTrialObjFlag= cbmItem.getState();
      util.showMsg(((flk.viewTrialObjFlag)
                    ? "Draw graphic overlays"
                    : "Don't draw graphic overlays"),
                  Color.black);
    }   
    
    else if(bGui.mi_ViewBoundaryCB==cbmItem)
    {
      flk.viewBoundaryFlag= cbmItem.getState();
      util.showMsg(((flk.viewBoundaryFlag)
                     ? "Show boundary overlay"
                     : "Don't show boundary overlay"),
                  Color.black);
    }   
    
    else if(bGui.mi_ViewRoiCB==cbmItem)
    {
      flk.viewRoiFlag= cbmItem.getState();
      util.showMsg(((flk.viewRoiFlag)
                     ? "Show Region Of Interest"
                     : "Don't show Region Of Interest"),
                  Color.black);
    }   
    
    else if(bGui.mi_ViewMeasCircleCB==cbmItem)
    { /* [DEPRICATE] */
      flk.viewMeasCircleFlag= cbmItem.getState();
      util.showMsg(((flk.viewMeasCircleFlag)
                     ? "Show measurement circle"
                     : "Don't show measurement circle"),
                  Color.black);
    }    
    
    else if(bGui.mi_QuantSpotListModeCB==cbmItem)
    { /* "Toggle between spot-list and trial-spot measurement mode" (C-J) */
      /* Toggle spot-list else trial-spot measurement mode */
      flk.spotsListModeFlag= cbmItem.getState();
      flk.useMeasCtrFlag= flk.spotsListModeFlag;
      if(!flk.spotsListModeFlag)
      { /* setup single spot measurement mode by forcing the circle display */
        flk.viewMeasCircleFlag= true;
        bGui.mi_ViewMeasCircleCB.setState(true);
      }
      util.showMsg(((flk.spotsListModeFlag)
                     ? "Show list of all spot measurements"
                     : "Show trial spot measurements"),
                   Color.black);
    }
            
    else if(bGui.mi_ViewMeasSpotLocAsCircleCB==cbmItem)
    { /* "Use measured spot location as "circle"{ann} " */
      flk.viewDrawSpotLocCircleFlag= cbmItem.getState();
      if(flk.viewDrawSpotLocCircleFlag)
      { /* implement radio button - both could be OFF! */
        bGui.mi_ViewMeasSpotLocAsPlusCB.setState(false);
        flk.viewDrawSpotLocPlusFlag= false;           
        util.showMsg("Use '+' for spot locations", Color.black);
      }
      if(!flk.viewDrawSpotLocCircleFlag && !flk.viewDrawSpotLocPlusFlag)              
        util.showMsg("Not indicating spot locations", Color.black);
    }       
            
    else if(bGui.mi_ViewMeasSpotLocAsPlusCB==cbmItem)
    { /* "Use measured spot location as "+"{ann} " */
      flk.viewDrawSpotLocPlusFlag= cbmItem.getState();
      if(flk.viewDrawSpotLocPlusFlag)
      { /* implement radio button - both could be OFF! */
        bGui.mi_ViewMeasSpotLocAsCircleCB.setState(false);
        flk.viewDrawSpotLocCircleFlag= false;     
        util.showMsg("Use 'circle' for spot locations", Color.black);
      }
      if(!flk.viewDrawSpotLocCircleFlag && !flk.viewDrawSpotLocPlusFlag)              
        util.showMsg("Not indicating spot locations", Color.black);
    }   
    
    else if(bGui.mi_ViewAnnSpotNbrCB==cbmItem)
    { /* "Use measured spot annotation as 'spot.nbr' " */
      flk.viewDrawSpotAnnNbrFlag= cbmItem.getState();
      if(flk.viewDrawSpotAnnNbrFlag)
        util.showMsg("Use 'spot measurement number' for spot annotation", 
                     Color.black);
      else                
        util.showMsg("Not showing spot measurement number for spot annotation",
                     Color.black);
    }       
            
    else if(bGui.mi_ViewAnnSpotIdCB==cbmItem)
    { /* "Use measured spot annotation as 'spot.id' " */
      flk.viewDrawSpotAnnIdFlag= cbmItem.getState();
      if(flk.viewDrawSpotAnnIdFlag)
        util.showMsg("Use 'spot identifier' for spot annotation", 
                     Color.black);
      else                
        util.showMsg("Not showing spot identifier number for spot annotation",
                     Color.black);
    }     
                                                 
    else if(bGui.mi_MultPopupsCB==cbmItem)
    {
      flk.viewMultPopups= cbmItem.getState();
      if(flk.viewMultPopups)
      {
        flk.popupWindowName= "_blank";
        util.showMsg("Use Same Popup", Color.black);
      }
      else
      {
        flk.popupWindowName= "FlkWind2";
        util.showMsg("Multiple popups", Color.black);
      }            
    }    
    
    else if(bGui.mi_GangScrollImgsCB==cbmItem)
    {
      flk.viewGangScrollFlag= cbmItem.getState();
    }    
    
    else if(bGui.mi_GangZoomImgsCB==cbmItem)
    {
      flk.viewGangZoomFlag= cbmItem.getState();
    }    
    
    else if(bGui.mi_GangBCImgsCB==cbmItem)
    {
      flk.viewGangBCFlag= cbmItem.getState();
    }
    else if(bGui.mi_useGuardImgsCB==cbmItem)
    { /* (View | "Add guard region to edges of images" */
      flk.useGuardRegionImageFlag= cbmItem.getState();
            
      flk.i1IS.addGuardRegion(flk.useGuardRegionImageFlag);
      flk.i2IS.addGuardRegion(flk.useGuardRegionImageFlag);  
      flk.flkIS.addGuardRegion(flk.useGuardRegionImageFlag);   
    }
        
    else if(bGui.mi_DispGrayValsCB==cbmItem)
    { /* display (x,y) coords else gray values */
      /* "Display gray values in left and right selected image title (C-G)" */
      /* display (x,y) coords else gray values */
      flk.viewDispGrayValuesFlag= !flk.viewDispGrayValuesFlag;
      util.showMsg(((flk.viewDispGrayValuesFlag)
                      ? "Don't show gray values"
                      : "Show gray values"),
                    Color.black);
    }   
    
    else if(bGui.mi_showReportPopupCB==cbmItem)
    { /* Show report popup */
      flk.viewReportPopupFlag= cbmItem.getState();
      bGui.pra.setShow(flk.viewReportPopupFlag);
    }    
    
    else if(bGui.mi_useNTSCrgbTograyCvtCB==cbmItem)
    { /* Convert RGB image to grayscale using NTSC transform if needed */
      flk.useNTSCrgbTograyCvtFlag= cbmItem.getState();
      /* If the iPix[] data is color RGB, then apply the NTSC rgb to 
       * grayscale  transform to the iPix[] data.
       *
       * First we recompute the iData.isColorImgFlag status. If it is true,
       * it is a color image where red!=green or red!=blue.
       * This flag is then be tested if we want to map the (R,G,B) to
       * NTSC(R,G,B) grayscale on a pixel level so we could quantify
       * the color data.
       *
       * This is only applied if the flk.useNTSCrgbTograyCvtFlag and
       * the iData.isColorImgFlag was recomputed as true.
       * It always computes (minG, maxG) for the iPix[] LSB 8-bit
       * data (AFTER the NTSC conversion if required).
       */
      if(flk.useNTSCrgbTograyCvtFlag && iData.isColorImgFlag)
      {
        iData.applyNTSCrgb2grayTransform();
      }
    } 
    
    else if(bGui.mi_autoMeasProtLookupPopupCB==cbmItem)
    { /* Auto measure, protein lookup in active server and Web page popup */
      flk.doMeasureProtIDlookupAndPopupFlag= cbmItem.getState();    
      util.showMsg("Auto measure, protein lookup and Web page popup "+ 
                   ((flk.doMeasureProtIDlookupAndPopupFlag) ? "enabled." : "disabled."),
                   Color.black);
      util.showMsg("Do spot measurement adding it to the spotList, protein\n"+
                  "lookup for that spot, and popup current access server\n"+
                  "Web page in one operation when the user clicks on a spot\n"+
                  "in an active image. The use must have enabled one of the options\n"+
                  "   (Edit | Select access to active DB server | ...).\n"+
                  "If 'Auto measure, protein lookup and Web page popup' is NOT\n"+
                  "enabled, the user must set the 'Click to access DB' checkbox\n"+
                  "to have Flicker popup the browser.\n", Color.black);
    }   
        
    else if(bGui.mi_useSwiss2DpageServerCB==cbmItem)
    { /* Use Swiss-2DPAGE DB access */   
      setAccessServerDBcheckbox(cbmItem);
      flk.currentPRIbaseURL= flk.UNIPROT;  
      util.showMsg("SWISS-2DPAGE DB access to "+ flk.currentPRIbaseURL+
                   ((flk.useSwiss2DpageServerFlag) ? "enabled." : "disabled."),
                   Color.black);
    }   
        
    else if(bGui.mi_usePIRUniprotServerCB==cbmItem)
    { /* Use PIR UniProt DB access */ 
      setAccessServerDBcheckbox(cbmItem);
      flk.currentPRIbaseURL= flk.UNIPROT;  
      util.showMsg("PIR UniProt DB access to "+ flk.currentPRIbaseURL+
                   ((flk.usePIRUniprotServerFlag) ? "enabled." : "disabled."),
                   Color.black);
    }   
        
    else if(bGui.mi_usePIRiProClassServerCB==cbmItem)
    { /* Use PIR iProClass DB access */  
      setAccessServerDBcheckbox(cbmItem);
      flk.currentPRIbaseURL= flk.IPROCLASS;  
      util.showMsg("PIR iProClass DB access to "+ flk.currentPRIbaseURL+
                   ((flk.usePIRiProClassServerFlag) ? "enabled." : "disabled."),
                   Color.black);
    }   
        
    else if(bGui.mi_usePIRiProLinkServerCB==cbmItem)
    { /* Use PIR UniProt DB access */   
      setAccessServerDBcheckbox(cbmItem);
      flk.currentPRIbaseURL= flk.IPROLINK;  
      util.showMsg("PIR iProLink DB access to "+ flk.currentPRIbaseURL+
                   ((flk.usePIRiProLinkFlag) ? "enabled." : "disabled."),
                   Color.black);
    }   
        
    else if(bGui.mi_thresholdInsideCB==cbmItem)
    { /* Use threshold insize filter [t1:t2] else outside of [t1:t2] */
      flk.useThresholdInsideFlag= cbmItem.getState();
    }
    
    else if(bGui.mi_saveOimagesWhenSaveStateCB==cbmItem)
    { /* "Enable saving transformed image when do a 'Save(As) state'" */
      flk.saveOimagesWhenSaveStateflag= cbmItem.getState();
    }
    
    else if(bGui.mi_useProteinDBbrowserCB==cbmItem)
    { /* "Use protein DB browser, else lookup ID and name on active images" */
      flk.useProteinDBbrowserFlag= cbmItem.getState();      
    } 
    
    else if(bGui.mi_dbugCB==cbmItem)
    { /* Toggle flk.dbugFlag */
      flk.dbugFlag= cbmItem.getState();      
      util.showMsg("Debugging is " + ((flk.dbugFlag) ? "ON" : "OFF"),
                   Color.black);
    }
    
    else if(bGui.mi_Quant_UseLeukemiaDemoCalibCB==cbmItem)
    { /* "Use demo leukemia gels ND wedge calibration preloads" */
      flk.useDemoLeukemiaCalPreFlag= cbmItem.getState();
      if(flk.useDemoLeukemiaCalPreFlag)
      util.showMsg("Use the default-demo ND wedge calibrations\n"+
                   "for the demo leukemia gels when you use the\n"+
                   "(Quantify | Calibrate | Calibrate OD by step wedge)\n"+
                   "command.\n",
                   Color.black);
      else
        util.showMsg("Use the previously-saved calibration files you had\n"+
                     "created for the demo leukemia gels.\n"+
                     "(Quantify | Calibrate | Calibrate OD by step wedge)\n"+
                     "command.\n",  
                     Color.black);
    }
  
    else if(bGui.mi_QuantTotDensityCB==cbmItem)
    { /* "Use sum density else mean" */
      flk.useTotDensityFlag= cbmItem.getState();      
      util.showMsg("Using " + ((flk.useTotDensityFlag) ? "sum" : "mean")+
                   " density in measurements",  Color.black);
    }
    
    else if(bGui.mi_MeasCtrCB==cbmItem)
    { /* "Use measurement counters" */
      flk.useMeasCtrFlag= cbmItem.getState();      
      util.showMsg(((flk.useMeasCtrFlag) ? "Using" : "Not using")+
                   " measurement counters",  Color.black);
    }
        
    /* Set windmp size */ 
    else if(bGui.mi_WinDmpSize5x5CB==cbmItem)
      setWindmpSizeCheckbox(5); 
    else if(bGui.mi_WinDmpSize10x10CB==cbmItem)
      setWindmpSizeCheckbox(10); 
    else if(bGui.mi_WinDmpSize15x15CB==cbmItem)
      setWindmpSizeCheckbox(15); 
    else if(bGui.mi_WinDmpSize20x20CB==cbmItem)
      setWindmpSizeCheckbox(20); 
    else if(bGui.mi_WinDmpSize25x25CB==cbmItem)
      setWindmpSizeCheckbox(25); 
    else if(bGui.mi_WinDmpSize30x30CB==cbmItem)
      setWindmpSizeCheckbox(30); 
    else if(bGui.mi_WinDmpSize35x35CB==cbmItem)
      setWindmpSizeCheckbox(35); 
    else if(bGui.mi_WinDmpSize40x40CB==cbmItem)
      setWindmpSizeCheckbox(40); 
    
    /* set the Windmp radix */
    else if(bGui.mi_WinDmpRadixDecCB==cbmItem)
      setWindmpRadixCheckbox(Windump.SHOW_DECIMAL);
    else if(bGui.mi_WinDmpRadixOctCB==cbmItem)
      setWindmpRadixCheckbox(Windump.SHOW_OCTAL);
    else if(bGui.mi_WinDmpRadixHexCB==cbmItem)
      setWindmpRadixCheckbox(Windump.SHOW_HEX);
    else if(bGui.mi_WinDmpRadixDecCB==cbmItem)
      setWindmpRadixCheckbox(Windump.SHOW_OD);        
    
    /* ............... CheckBoxes ................ */
    
    else if(bGui.flickerCheckbox==jcbItem)
    { /* Toggle flicker on/off */
      flk.flickerFlag= jcbItem.isSelected();      
      bGui.mi_flickerCB.setState(flk.flickerFlag);  /* linked GUI */
      util.setFlickerGUI(flk.flickerFlag);
    } 
    
    else if(bGui.allowXformsCheckbox==jcbItem)
    { /* Allow transforms checkbox */
      flk.allowXformFlag= jcbItem.isSelected();
      if(!flk.allowXformFlag)
      {
        flk.composeXformFlag= false;
        if(bGui.composeXformCheckbox!=null)
        bGui.composeXformCheckbox.setSelected(false);
      }
      
      /* Enable GUI Transforms menu operations */
      bGui.setTransformsEnable(flk.allowXformFlag);
      util.showMsg(((flk.allowXformFlag)
                      ? "Transforms are allowed"
                      : "No transforms are allowed"),
                  Color.black);      
    } /* Allow transforms checkbox */
    
    else if(bGui.composeXformCheckbox==jcbItem)
    { /* Compose sequential transforms */
      flk.composeXformFlag= jcbItem.isSelected();
      util.showMsg(((flk.composeXformFlag)
                      ? "Transform previous transforms"
                      : "Transform original images"),
                  Color.black);    
    }
    
    else if (e.getSource()==bGui.clickableCheckbox)
    { /* Clickable database enable */ 
      if(iData!=null)
      {     
        flk.userClickableImageDBflag= bGui.clickableCheckbox.isSelected();
        iData.userClickableImageDBflag= flk.userClickableImageDBflag;    
        flk.chkIfClickableDB(true);
      }
    } /* Clickable database enable */ 
     flk.repaint();
  } /* itemStateChanged */
  
  
  /**
   * processTransform() - process the specified transform
   * @param cmd is the transform to be performed on the current image
   * @param lastISName is the image scroller selected to be transformed
   *        it is either "left" or "right"
   * @param extFctNbr is the external plugin fuction number if NOT 0
   * @return true if succeed
   */
  public boolean processTransform(String cmd, String lastISName, int extFctNbr)
  { /* processTransform */
    if(flk.doingXformFlag)
    {
      flk.util.showMsg("Can't do new transform until "+flk.xformName +
                       " finishes.", Color.red);
      return(false);
    }
    else
      flk.doingXformFlag= true;
    
    flk.imagesToProcess= 0;
    if(extFctNbr>0)
    {
      flk.xformName= flk.efName[extFctNbr];
    }
    else
      flk.xformName= (String)cmd;   /* set the new xform name */
    util.setFlickerState(false);    /* disable flicker checkbox and
                                     * stop flickering while doing Xform */
    
    if(lastISName.equals("left") || lastISName.equals("both"))
    { /* Do 1st image */
      Date date= new Date();
      flk.iData1.startTime= date.getTime(); /* grab start time
                                             * msec since 1970*/
      flk.imagesToProcess++;
      flk.validAffineFlag= false;
      
      flk.ixf1= new ImageXform(flk, "left", flk.iData1, flk.xformName,
                               extFctNbr);
      flk.ixf1.start();
      //flk.ixf1.setPriority(Thread.MIN_PRIORITY);
      //flk.ixf1.doXform(flk.iData1); /* NOTE: only used if don't use threads! */ 
      util.showMsg("Doing [" + flk.xformName + "] on " + flk.imageFile1,
                   Color.black );
    } /* Do 1st image */
    
    if(lastISName.equals("right") || lastISName.equals("both") )
    { /* Do 2nd image */
      Date date= new Date();
      flk.iData2.startTime= date.getTime(); /* grab start time
                                             * msec since 1970 */
      flk.imagesToProcess++;
      flk.validAffineFlag= false;
      
      flk.ixf2= new ImageXform(flk, "right", flk.iData2, flk.xformName,
                               extFctNbr);
      flk.ixf2.start();
      //flk.ixf2.setPriority(Thread.MIN_PRIORITY);      
      //flk.ixf2.doXform(flk.iData2);   /* NOTE: only used if don't use threads! */   
      util.showMsg("Doing [" + flk.xformName + "] on " + flk.imageFile2,
                   Color.black );
    } /* Do 2nd image */
    return(true);
  } /* processTransform */
  
  
  /**
   * actionPerformed() - common function to handle action events
   * @param e is ActionEvent
   */
  public void actionPerformed(ActionEvent e)
  { /* actionPerformed */
    BuildGUI bGui= flk.bGui;
    String 
      lastISName= flk.activeImage,
      cmd= e.getActionCommand();      /* arg for the event */
    int extFctNbr= 0;		      /* external function number 1,2, etc. */
    
    /* In case needed, lookup current iData, lastIS, and imgFile */
    leftOrRightSelectedFlag= lookupCanvasPtrAndName();  
    
    /* Extract external function NNN from name "FctNNN"if exists */
    if(cmd.startsWith("Plugin"))
    { /* pick up the plugin # */
      try
      {
        String sPI= cmd.substring(6);
        Integer i1= new Integer(sPI);
        extFctNbr= i1.intValue();
      }
      catch(Exception epI)
      {
      }
    } /* pick up the plugin # */
    else 
      extFctNbr= 0;    
    
    /*  ............... FILE Menu menu ........... */
    if(cmd.equals("OpenFile"))
    { /* Open file to replace current image selected */
      if(!lastISName.equals("left") && !lastISName.equals("right"))
      {
        String msg= "Select left or right image to overload first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      /* Popup up FileDialog to get new imageFile */
      String imageFile= popup.popupFileDialog(null, "New image",true);
      if(imageFile==null)
        return;                     /* no file picked */
      
      if(lastISName.equals("left"))
      {
        flk.clickableCGIbaseURL1= null;  
      }     
      else if(lastISName.equals("right"))
      {
        flk.clickableCGIbaseURL2= null;  
      }
      
      shutOffGuardRegion(); 
      boolean ok= imgIO.changeImageFromSpec(imageFile,lastISName,true,true); 
      if(!ok)
        return;
      
      String
        dbMenuName= util.getFileNameFromPath(imageFile),
        timeStamp= util.getCurDateStr();
      FlkRecent fr= FlkRecent.addOrUpdate(dbMenuName, "", imageFile, 
                                          "Local", timeStamp);       
      /* Update the (File | Open recent images) menu subtree
       * by removing the old subtree and building a new one.
       */
      if(fr!=null)
        bGui.createFlkRecentMenuTree(bGui.fRecentMenuStub);
      flk.repaint();
      return;
    } /* Open file to replace current image selected */
    
    else if(cmd.equals("OpenURL"))
    { /* Open URL to replace current image selected */      
      if(!leftOrRightSelectedFlag)
      {
        String msg= "Select left or right image to overload first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      /* Popup up TextField to get new imageFileX URL */     
      String imageURL= flk.bGui.pdq.dialogQuery("New image URL",null);
      if(imageURL==null)
        return;
      if(lastISName.equals("left"))
      {
        flk.clickableCGIbaseURL1= null; 
      }
      else if(lastISName.equals("right"))
      {
        flk.clickableCGIbaseURL2= null; 
      }
      /* Go reload the image */
      
       /* turn off guard region */
      shutOffGuardRegion();
      boolean ok= imgIO.changeImageFromSpec(imageURL,lastISName,true,true);
      if(!ok)
        return; 
      
      if(lastISName.equals("left"))
      {
        flk.iData1.state.init(flk,"left");
        flk.evs.setEventScrollers(flk.iData1.state);
      }       
      else if(lastISName.equals("right"))
      {
        flk.iData2.state.init(flk,"right");
        flk.evs.setEventScrollers(flk.iData2.state);
      }
      
      /* Add new file to FlkRecent database */
      String dbMenuName= util.getFileNameFromPath(imageURL);
      FlkRecent fr= FlkRecent.addOrUpdate(dbMenuName, "", imageURL, 
                                          "URL", util.getCurDateStr());   
      /* Update the (File | Open recent images) menu subtree
       * by removing the old subtree and building a new one.
       */
      if(fr!=null)
        bGui.createFlkRecentMenuTree(bGui.fRecentMenuStub);
      flk.doFullRepaint();
      return;
    } /* Open URL to replace current image selected */
    
    else if(cmd.equals("AssignActiveMapURL"))
    { /* Assign Active Map URL to current image selected */      
      if(!leftOrRightSelectedFlag)
      {
        String msg= "Select left or right image to assign MAP URL first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      /* Popup up TextField to get new imageFileX URL */     
      String activeMapURL= flk.bGui.pdq.dialogQuery("New active map URL",
                                                    null);
      if(activeMapURL==null)
        return;  
      shutOffGuardRegion();
      String imageFile= null;
      if(lastISName.equals("left"))
      { /* left image */
        imageFile= flk.imageFile1;
        flk.clickableCGIbaseURL1= activeMapURL;
      }
      else if(lastISName.equals("right"))
      { /* right image */
        imageFile= flk.imageFile2;
        flk.clickableCGIbaseURL2= activeMapURL; 
      }
      /* Add new file to FlkRecent database */
      String 
        dbMenuName= util.getFileNameFromPath(imageFile),
        timeStamp= util.getCurDateStr();
      FlkRecent fr= FlkRecent.addOrUpdate(dbMenuName, activeMapURL,
                                          imageFile, "URL", timeStamp);
      flk.doFullRepaint();
      return;  
    } /* Assign Active Map URL to current image selected */  
    
    else if(cmd.equals("TestTiffLoader"))
    { /* DEBUGGING... "Test the TiffLoader" */      
      TiffLoader ti= new TiffLoader(flk.dbugFlag);
      String tiffFileName= flk.userDir+ "PlasmaH.tif";
      if(ti.doTiffLoad(tiffFileName, false)!=null)           
        flk.util.appendReportMsg(ti.toString());
      else                 
        flk.util.appendReportMsg("Can't read file ["+tiffFileName+"]\n"+
                                 ti.fatalMsg+"\n"+ti.toString());
      flk.doFullRepaint();
      return;
    } /* DEBUGGING... "Test the TiffLoader" */ 
    
    else if(cmd.equals("ListUserImageDirs"))
    { /* "List user's images by directory" */ 
      flk.fUser.listUserImageDirs();      
      util.forceReportWindowPopup(); /* force append Report Window to popup*/
      return;
    }
        
    else if(cmd.startsWith("FM:"))    
    { /* (Open Map Image URL "FM:<i>" to replace current image selected */     
      if(! leftOrRightSelectedFlag)
      {
        String msg= "Select left or right image to overload first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      /* Get the database entry */
      String sIdx= cmd.substring(3);
      int idx= Util.cvs2i(sIdx,-1);
      if(idx>=0 && idx<flk.fm.nMaps)
      { /* Go reload the image and setup the clickable database */
        FlkMap fMap= flk.fm.flkMaps[idx];
        String
          baseURL= fMap.baseURL,
          imageURL= baseURL+fMap.imageURL,
          clickableURL= baseURL+fMap.clickableURL;
        if(lastISName.equals("left"))
        {
          flk.clickableCGIbaseURL1= clickableURL;
        }
        else if(lastISName.equals("right"))
        {
          flk.clickableCGIbaseURL2= clickableURL;
        } 
        shutOffGuardRegion();
        imgIO.changeImageFromSpec(imageURL,lastISName,true,true);          
      } /* Go reload the image and setup the clickable database */
      else        
      {
        String msg= "Illegal FlkMapDB entry ["+cmd+"]";
        util.popupAlertMsg(msg, flk.alertColor);
      }
      
      flk.doFullRepaint();
      return;
    } /* (Open Map Image URL "FM:<i>" to replace current image selected */
     
    else if(cmd.startsWith("FD:"))    
    { /* (Open Demo images pair "FD:<i>" to replace current images selected */
      String sIdx= cmd.substring(3);
      int idx= Util.cvs2i(sIdx,-1);
      if(idx>=0 && idx<flk.fDemo.nMaps)
      { /* Go reload the image and setup the clickable database */
        FlkDemo fDemo= flk.fDemo.flkDemos[idx];
        flk.clickableCGIbaseURL1= fDemo.clickableURL1;
        flk.clickableCGIbaseURL2= fDemo.clickableURL2;
        util.showMsg("Changing to two new demos images", Color.black);
        util.showMsg2("Left="+fDemo.imageURL1+", Right="+fDemo.imageURL2, 
                      Color.black);
        imgIO.changeImageFromSpec(fDemo.imageURL1, "left", true,true);
        imgIO.changeImageFromSpec(fDemo.imageURL2, "right", true,true);
      }
      else        
        util.showMsg("Illegal FlkDemoDB entry ["+cmd+"]", Color.red);
      flk.doFullRepaint();
      return;
    } /* (Open Demo images pair "FD:<i>" to replace current images selected */
    
    else if(cmd.startsWith("FUP:"))    
    { /* (Open User images pair "FUS:<i>" to replace current images selected */
      String sIdx= cmd.substring(4);
      int idx= Util.cvs2i(sIdx,-1);
      if(idx>=0 && idx<flk.fUser.nMaps)
      { /* Go reload the image and setup the clickable database */
        FlkUser fUser= flk.fUser.flkUsers[idx];
        flk.clickableCGIbaseURL1= "";
        flk.clickableCGIbaseURL2= "";
        util.showMsg("Changing to two new user images", Color.black);
        util.showMsg2("Left="+fUser.imageURL1+", Right="+fUser.imageURL2, 
                      Color.black);
        imgIO.changeImageFromSpec(fUser.imageURL1, "left", true,true);
        imgIO.changeImageFromSpec(fUser.imageURL2, "right", true,true); 
        flk.activeImage= "both";           /* disable selected image */
        String 
          fmDbName= fUser.flkUsers[idx].subMenuName,
          fmMenuName= fUser.flkUsers[idx].subMenuEntry,
          dbMenuName= fmDbName+"-"+fmMenuName,
          timeStamp= util.getCurDateStr();
        FlkRecent fr= FlkRecent.addOrUpdatePair(dbMenuName, "", "",  
                                                fUser.imageURL1, 
                                                fUser.imageURL2, 
                                                "User-Pair", timeStamp);       
        /* Update the (File | Open recent images) menu subtree
         * by removing the old subtree and building a new one.
         */
        if(fr!=null)
          bGui.createFlkRecentMenuTree(bGui.fRecentMenuStub);        
      } /* Go reload the image and setup the clickable database */
      else        
        util.showMsg("Illegal FlkUser DB entry ["+cmd+"]", Color.red);
      flk.doFullRepaint();
      return;
    } /* (Open User images pair "FUP:<i>" to replace current images selected */
    
    else if(cmd.startsWith("FUS:"))    
    { /* (Open User image single "FUS:<i>,<j>" to replace current image */
      if(!lastISName.equals("left") && !lastISName.equals("right"))
      {
        String msg= "Select left or right image to overload first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      FlkUser fUser= flk.fUser;
      String sArg= cmd.substring(4);
      int idxComma= sArg.indexOf(",");
      String
        sIdx= sArg.substring(0,idxComma),
        sJdx= sArg.substring(idxComma+1); 
      if(sIdx==null || sJdx==null)
      { /* bad entry... */
        util.popupAlertMsg("DRYROT.1 bad 'FUS:i,j' data cmd="+cmd,
                           flk.alertColor);
        return;
      }
      int
        idx= Util.cvs2i(sIdx,-1),
        jdx= Util.cvs2i(sJdx,-1);
      if(idx==-1 || jdx==-1 ||
         idx>fUser.allImgFileList.length ||
         jdx>fUser.allImgFileList[idx].length
         )
      { /* bad entry... */        
        util.popupAlertMsg("DRYROT.2 bad 'FUS:i,j' data cmd="+cmd, 
                           flk.alertColor);
        return;
      }
      String
        imageDir= fUser.dirListPath[idx],
        imageFile= fUser.allImgFileList[idx][jdx],
        fullImagePath= imageDir+flk.fileSeparator+imageFile;
      shutOffGuardRegion();
      boolean ok= imgIO.changeImageFromSpec(fullImagePath,lastISName,true,true); 
      if(!ok)
        return;
      if(lastISName.equals("left"))
      {
        flk.iData1.state.init(flk,"left");
        flk.evs.setEventScrollers(flk.iData1.state);
        flk.clickableCGIbaseURL1= null;
      }
       
      else if(lastISName.equals("right"))
      {
        flk.iData2.state.init(flk,"right");
        flk.evs.setEventScrollers(flk.iData2.state);
        flk.clickableCGIbaseURL2= null; 
      }
      /* Check if still contains 1 or more clickable DB images when set
       * the isClickableDBflag
       */        
      String
        dbMenuName= util.getFileNameFromPath(imageFile),
        timeStamp= util.getCurDateStr();
      FlkRecent fr= FlkRecent.addOrUpdate(dbMenuName, "", imageFile, 
                                          "Local", timeStamp);       
      /* Update the (File | Open recent images) menu subtree
       * by removing the old subtree and building a new one.
       */
      if(fr!=null)
        bGui.createFlkRecentMenuTree(bGui.fRecentMenuStub);
      flk.repaint();
      return;
    } /* (Open User images single "FUS:<i>" to replace current image */
                    
    else if(cmd.startsWith("FR:"))
    { /* (Open Recent image URL "FR:<i>" to replace current image selected*/ 
      /* Popup up TextField to get new imageFileX URL */
      String sIdx= cmd.substring(3);
      int idx= Util.cvs2i(sIdx,-1);
      if(idx>=0 && idx<flk.fRecent.nMaps)
      { /* Go reload the image and setup the clickable database */
        FlkRecent fRecent= flk.fRecent.flkRecents[idx];
        String
          imageURL1= fRecent.imageURL1,
          imageURL2= fRecent.imageURL2,
          clickableURL1= (fRecent.clickableURL1==null)
                            ? "" : fRecent.clickableURL1,
          clickableURL2= (fRecent.clickableURL2==null)
                            ? "" : fRecent.clickableURL2;
        if(imageURL2.length()>0)
        { /* load two images */
          util.showMsg("Changing to two new user images", Color.black);
          util.showMsg2("Left="+imageURL1+", Right="+imageURL2,
                        Color.black); 
          shutOffGuardRegion();
          imgIO.changeImageFromSpec(imageURL1, "left",true,true);
          imgIO.changeImageFromSpec(imageURL2, "right",true,true);
          flk.activeImage= "both";           /* disable selected image */
        } /* load two images */
        else
        { /* load one image */
          if(! leftOrRightSelectedFlag)
          {
            String msg= "Select left or right image to overload first.";
            util.popupAlertMsg(msg, flk.alertColor);
            return;
          }
          if(lastISName.equals("left"))
          {      
            flk.clickableCGIbaseURL1= clickableURL1;
          }
          else if(lastISName.equals("right"))
          {      
            flk.clickableCGIbaseURL2= clickableURL2;
          }
          /* Check if still contains 1 or more clickable DB images when set
           * the isClickableDBflag
          */
          util.showMsg("Changing ["+lastISName+"] to image", Color.black);
          util.showMsg2(imageURL1, Color.black);
          shutOffGuardRegion();
          imgIO.changeImageFromSpec(imageURL1,lastISName,true,true);
        } /* load one image */        
      } /* Go reload the image and setup the clickable database */
      else        
        util.showMsg("Illegal FlkRecentDB entry ["+cmd+"]", Color.red);
      flk.doFullRepaint();
      return;
    } /* (Open Recent image URL "FR:<i>" to replace current image selected*/   
    
    
    else if(cmd.equals("FlkRecent:Clear"))
    { /* (File | Open recent image | Clear all recent entries) */
      flk.fRecent.clear();
      util.showMsg("Cleared all recent images", Color.black);
      bGui.createFlkRecentMenuTree(bGui.fRecentMenuStub);
    }
        
    else if(cmd.equals("OpenStateFile"))
    { /* Open state file (i.e. read .flk state file) */      
      String flkStateFile= popup.popupFileDialog(null,
                                                 "Read .flk state file",
                                                 true);
      if(flkStateFile==null || flkStateFile.length()==0)
        return;
      if(!flkStateFile.endsWith(".flk"))
      {
        String msg= "Flicker state file must have '.flk' file extension.";
        util.popupAlertMsg(msg, flk.alertColor);
      }
      else
        util.readFlkStateFile(flkStateFile,true);
      /* Add this to the DB/FlkRecentDB.txt database */
      String 
        flkName= util.getFileNameFromPath(flkStateFile),
        dbMenuName= flkName.substring(0,flkName.length()-4),  /* drop ".flk" */
        timeStamp= util.getCurDateStr();
      FlkRecent fr= flk.fRecent.addOrUpdatePair(dbMenuName, 
                                                flk.clickableCGIbaseURL1,
                                                flk.clickableCGIbaseURL2,
                                                flk.imageFile1, flk.imageFile2,
                                                "Flk-Pair", timeStamp);       
      /* Update the (File | Open recent images) menu subtree
       * by removing the old subtree and building a new one.
       */
      if(fr!=null)
        bGui.createFlkRecentMenuTree(bGui.fRecentMenuStub);
      flk.doFullRepaint();
      return;
    } /* Open state file (i.e. read .flk state file) */  
    
    else if(cmd.startsWith("FS:"))    
    { /* (Open FlkStartups "FS:<.flk file>" to replace cur. image selected*/ 
      String flkStateFile= cmd.substring(3);
      if(flkStateFile==null || flkStateFile.length()==0)
        return;
      if(!flkStateFile.endsWith(".flk"))
      {
        String msg= "Flicker state file must have '.flk' file extension.";
        util.popupAlertMsg(msg, flk.alertColor);
      }
      else
        util.readFlkStateFile(flkStateFile,true);
      flk.doFullRepaint();
      return;
    } /* (Open FlkStartups "FS:<.flk file>" to replace cur. image selected*/ 
    
    else if(cmd.equals("SaveStateFile"))
    { /* Save state file (i.e. write .flk state file) */ 
      if(flk.flkStateFile==null)
      {
        String msg= "No state file was previously defined";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      /* Write a .flk state file */
      if(!util.writeFlkState(flk.flkStateFile))
      {
        String msg= "Problems saving Flicker state file: "+flk.flkStateFile;
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      else     
      {
        util.showMsg1("Saved Flicker state in startup",Color.black);
        util.showMsg2("file["+flk.flkStateFile+"]", Color.black);   
      }
      return;
    } /* Save state file (i.e. write .flk state file) */      
    
    else if(cmd.equals("SaveAsStateFile"))
    { /* Save state file (i.e. write .flk state file) */ 
      String
        initialPath= flk.userDir+"FlkStartups"+flk.fileSeparator+
                     "FlkStartup.flk",
        flkStateFile= popup.popupFileDialog(initialPath,
                                           "Write .flk state file",
                                           false);
      if(flkStateFile==null || flkStateFile.length()==0)
        return;
      if(!flkStateFile.endsWith(".flk"))
      {
        String msg= "Flicker state file must have '.flk' file extension.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      /* Write a .flk state file */
      if(!util.writeFlkState(flkStateFile))
      {
        String msg= "Problem saving Flicker state file: "+flkStateFile;
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      else   
      { 
        flk.flkStateFile= flkStateFile;
        util.showMsg1("Saved Flicker state in startup",Color.black);
        util.showMsg2("file["+flk.flkStateFile+"]", Color.black);   
      }
      return;
    } /* Save state file (i.e. write .flk state file) */     
    
    else if(cmd.equals("updateFlickerJar"))
    { /* "Update Flicker Program" Flicker.jar file from server */ 
      if(!util.updateFlickerJarFile())
      {
        String msg="Problem updating Flicker.jar file - aborted";
        util.popupAlertMsg(msg, flk.alertColor);
      }
      else
      {
        util.showMsg1("Finshed updating Flicker from Web server.", 
                      Color.black);
        util.showMsg2("Restart Flicker for this to take affect.", 
                      Color.black);
      }
      return;
    }       
    
    else if(cmd.equals("updateWebMaps"))
    { /* "Update Web Maps DB" Web map database from server */       
      String dbFile[]= { "FlkMapDB.txt" };
      if(!util.updateFlkDBfiles(dbFile))
      {
        String 
          msg= "Problem updating Flicker Active Web map DB file - aborted";
        util.popupAlertMsg(msg, flk.alertColor);
      }
      else
      {
        util.showMsg1("Finished updating Web Image maps DB from server.", 
                     Color.black);
        util.showMsg2("Restart Flicker for this to take affect.", 
                     Color.black);
      }
      return;
    } /* "Update Web Maps DB" Web map database from server */       
    
    else if(cmd.equals("updateDemos"))
    { /* "Update Demo Images DB" database from server */ 
      if(!util.updateDemoDBfiles())
      {
        String 
          msg= "Problem updating Flicker Demo images database files - aborted";
        util.showMsg(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
      }
      else
      {
        util.showMsg1("Finished updating Demo images database from server.", 
                      Color.black);
        util.showMsg2("Restart Flicker for this to take affect.",Color.black);
      }
      return;
    }      
     
    else if(cmd.equals("addingUserDemoDbURL"))
    { /* "Add user's Flicker Demo Images DB by URL" */         
      String
        msg= "a valid 'Flicker Images DemoDB package' Web URL for extending the Demo DB",
        userDemoDBurl= flk.bGui.pdq.dialogQuery(msg, null);
      if(userDemoDBurl==null)
      { /* no URL */
        msg= "You must specify a valid user's Flicker DemoDB URL";
        util.showMsg(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
      }
      else if(!util.addUserDemoFilesDBbyURL(userDemoDBurl))
      { /* problem */
        msg= "Problem adding user's Flicker Images DemoDB from URL - aborted";
        util.showMsg(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
      }
      else
      { /* ok */
        util.showMsg1("Finished adding user's Flicker Images Demo DB from URL", 
                      Color.black);
        util.showMsg2("Restart Flicker for this to take affect.",Color.black);
      }
      return;
    } /* "Add user's Flicker Demo Images DB by URL" */
    
    else if(cmd.equals("SaveXform_oImg"))
    { /* "Save Transformed image" */ 
      if(!flk.allowXformFlag)
      {
        String msg= "First enable Transforms before save transformed images";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
          
      if(! leftOrRightSelectedFlag)
      {
        String msg= "First select right or left image.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      if(iData.oImg==null)
      {
        String msg= "You must transform the image first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }  
      String
        saveXformImageFile= flk.userDir+"tmp"+flk.fileSeparator+
                            "SaveXformImage.gif";  
      if(!lastIS.saveOImgAsGifFile(saveXformImageFile,true))
      {
        String msg= "Problem saving transform output image ";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      return;
    } /* "Save Transformed image" */ 
        
    else if(cmd.equals("SaveOverlayImg"))
    { /* "SaveAs overlay image" */   
      String
        initialPath= flk.userDir+"tmp"+flk.fileSeparator+
                     "SaveOverlayImage.gif";        
      /* Popup up FileDialog to save imageFile */
      String saveAsImgFile= popup.popupFileDialog(initialPath,
                                                  "Save GIF image",
                                                  false);
      lastIS.saveAsOverlayFile(saveAsImgFile);      
      return;
    } /* "SaveAs overlay image" */ 
    
    else if(cmd.equals("ResetImages"))
    { /* Restore I1 & I2 images to original data */
      flk.validAffineFlag= false;
      
      /* turn off guard region */
      shutOffGuardRegion();
           
      /* just copy image that sits in memory */
      imgIO.restoreImage(flk.iData1,true);
      imgIO.restoreImage(flk.iData2,true);
      flk.info.updateInfoString(); /* update Flicker Info string messages */
      
      /* reset state in both images */  
      flk.iData1.state.init(flk,"left");
      flk.evs.setEventScrollers(flk.iData1.state);
      flk.clickableCGIbaseURL1= null;  
      
      flk.iData2.state.init(flk,"right");
      flk.evs.setEventScrollers(flk.iData2.state);
      flk.clickableCGIbaseURL2= null;

      if(flk.flkIS.iData!=null)
      { /* update Flicker window with new data */
        flk.updateFlickerImage();  /* Update the flicker image. */       
        flk.flkIS.clearLandmarkTextListToDraw();       
      }
           
      util.showMsg( "Restored images to original data.",  Color.black);
      flk.xformName= "";	/* none... */
      return;
    } /* Restore I1 & I2 images to original data */
    
    else if(cmd.equals("AbortXform"))
    { /* Kill image transform in progress */
      if(!flk.doingXformFlag)
      {
        util.showMsg("No transform is progress to abort.", Color.black);
        return;
      }
      boolean killedIt= false;
      
      if(flk.ixf1==null && flk.ixf2==null)
        return;	                /* nothing to do */
      
      if(flk.ixf1!=null)
      {
        killedIt= true;
        flk.ixf1.abortTransform();
      }
      
      if(flk.ixf2!=null)
      {
        killedIt= true;
        flk.ixf2.abortTransform();
      }
      flk.validAffineFlag= false;      
      
      flk.errStr= "Aborted [" + flk.xformName + "]";
      
      flk.doFullRepaint();
      return;
    } /* Kill image transform in progress */
    
    else if(cmd.equals("Quit"))
    { /* Quit */
      flk.finalize();
      return;            /* in case finalize aborts... */
    }
        
    /*  ............... EDIT Menu menu ........... */ 
        
    else if(cmd.equals("CanvasSize:+") || cmd.equals("CanvasSize:-"))
    { /* Change the canvas size */ 
      /* Change the canvas size by the specified + or - increment command.
       * If no increment command is used, then just resize it to the 
       * current setting.
       */
      flk.changeCanvasSize(cmd);
      return;
    } /* Change the canvas size */       
    
    else if(cmd.startsWith("TargetColor"))
    { /* Set trial object color */ 
      if(cmd.startsWith("TargetColor-Red"))
        flk.targetColor= Color.red; 
      else if(cmd.startsWith("TargetColor-Orange"))
        flk.targetColor= Color.orange; 
      else if(cmd.startsWith("TargetColor-Yellow"))
        flk.targetColor= Color.yellow; 
      else if(cmd.startsWith("TargetColor-Green"))
        flk.targetColor= Color.green; 
      else if(cmd.startsWith("TargetColor-Blue"))
        flk.targetColor= Color.blue; 
      else if(cmd.startsWith("TargetColor-Cyan"))
        flk.targetColor= Color.cyan; 
      else if(cmd.startsWith("TargetColor-Magenta"))
        flk.targetColor= Color.magenta; 
      else if(cmd.startsWith("TargetColor-Black"))
        flk.targetColor= Color.black; 
      else if(cmd.startsWith("TargetColor-Gray"))
        flk.targetColor= Color.gray; 
      else if(cmd.startsWith("TargetColor-White"))
        flk.targetColor= Color.white; 
      
      flk.doFullRepaint();;
      return;
    } /* Set trial object color */  
    
    else if(cmd.startsWith("TrialObjColor"))
    { /* Set trial object color */ 
      if(cmd.startsWith("TrialObjColor-Red"))
        flk.trialObjColor= Color.red; 
      else if(cmd.startsWith("TrialObjColor-Orange"))
        flk.trialObjColor= Color.orange; 
      else if(cmd.startsWith("TrialObjColor-Yellow"))
        flk.trialObjColor= Color.yellow; 
      else if(cmd.startsWith("TrialObjColor-Green"))
        flk.trialObjColor= Color.green; 
      else if(cmd.startsWith("TrialObjColor-Blue"))
        flk.trialObjColor= Color.blue; 
      else if(cmd.startsWith("TrialObjColor-Cyan"))
        flk.trialObjColor= Color.cyan; 
      else if(cmd.startsWith("TrialObjColor-Magenta"))
        flk.trialObjColor= Color.magenta; 
      else if(cmd.startsWith("TrialObjColor-Black"))
        flk.trialObjColor= Color.black; 
      else if(cmd.startsWith("TrialObjColor-Gray"))
        flk.trialObjColor= Color.gray; 
      else if(cmd.startsWith("TrialObjColor-White"))
        flk.trialObjColor= Color.white; 
      
      flk.doFullRepaint();
      return;
    } /* Set trial object color */          
    
    else if(cmd.startsWith("lmsColor"))
    { /* Set landmark  object color */ 
      if(cmd.startsWith("lmsColor-Red"))
        flk.lmsColor= Color.red; 
      else if(cmd.startsWith("lmsColor-Orange"))
        flk.lmsColor= Color.orange; 
      else if(cmd.startsWith("lmsColor-Yellow"))
        flk.lmsColor= Color.yellow; 
      else if(cmd.startsWith("lmsColor-Green"))
        flk.lmsColor= Color.green; 
      else if(cmd.startsWith("lmsColor-Blue"))
        flk.lmsColor= Color.blue; 
      else if(cmd.startsWith("lmsColor-Cyan"))
        flk.lmsColor= Color.cyan;  
      else if(cmd.startsWith("lmsColor-Magenta"))
        flk.lmsColor= Color.magenta; 
      else if(cmd.startsWith("lmsColor-Black"))
        flk.lmsColor= Color.black; 
      else if(cmd.startsWith("lmsColor-Gray"))
        flk.lmsColor= Color.gray; 
      else if(cmd.startsWith("lmsColor-White"))
        flk.lmsColor= Color.white;
      else 
        return;
      /*  Set the landmark color for all landmarks */
      flk.lms.setColorAll(flk.lmsColor);      
      flk.doFullRepaint();
      return;
    } /* Set landmark object color */  
                
    else if(cmd.startsWith("measColor"))
    { /* Set measurement object color */ 
      if(cmd.startsWith("measColor-Red"))
        flk.measCircleColor= Color.red; 
      else if(cmd.startsWith("measColor-Orange"))
        flk.measCircleColor= Color.orange; 
      else if(cmd.startsWith("measColor-Yellow"))
        flk.measCircleColor= Color.yellow; 
      else if(cmd.startsWith("measColor-Green"))
        flk.measCircleColor= Color.green; 
      else if(cmd.startsWith("measColor-Blue"))
        flk.measCircleColor= Color.blue; 
      else if(cmd.startsWith("measColor-Cyan"))
        flk.measCircleColor= Color.cyan; 
      else if(cmd.startsWith("measColor-Magenta"))
        flk.measCircleColor= Color.magenta; 
      else if(cmd.startsWith("measColor-Black"))
        flk.measCircleColor= Color.black; 
      else if(cmd.startsWith("measColor-Gray"))
        flk.measCircleColor= Color.gray; 
      else if(cmd.startsWith("measColor-White"))
        flk.measCircleColor= Color.white;
      else 
        return;     
      flk.doFullRepaint();
      return;
    } /* Set measurement object color */    
             
    else if(cmd.startsWith("guardColor"))
    { /* Set guard region color */ 
      if(cmd.startsWith("guardColor-Red"))
        flk.guardRegionColor= Color.red; 
      else if(cmd.startsWith("guardColor-Orange"))
        flk.guardRegionColor= Color.orange; 
      else if(cmd.startsWith("guardColor-Yellow"))
        flk.guardRegionColor= Color.yellow; 
      else if(cmd.startsWith("guardColor-Green"))
        flk.guardRegionColor= Color.green; 
      else if(cmd.startsWith("guardColor-Blue"))
        flk.guardRegionColor= Color.blue; 
      else if(cmd.startsWith("guardColor-Cyan"))
        flk.guardRegionColor= Color.cyan; 
      else if(cmd.startsWith("guardColor-Magenta"))
        flk.guardRegionColor= Color.magenta; 
      else if(cmd.startsWith("guardColor-Black"))
        flk.guardRegionColor= Color.black; 
      else if(cmd.startsWith("guardColor-Gray"))
        flk.guardRegionColor= Color.gray; 
      else if(cmd.startsWith("guardColor-White"))
        flk.guardRegionColor= Color.white;
      else 
        return;    
      flk.doFullRepaint();
      return;
    } /* Set guard region color */
    
    
    else if(cmd.equals("ResizeMem"))
    { /* Resize LAX file data for Flicker. */
      /* This will try to adjust the startup process size.
       * The LAX file (e.g., C:/Program Files/Flicker/Flicker.lax)
       * specifies the startup sizes for both the heap and stack.
       * It determines the current size, asks the user for a new size
       * then creates a new file if it succeeds in the Q and A.
       */    
    
      int newMemSize= util.resizeLAXfileData();
      if(newMemSize>0)
      {
        util.showMsg1("Changed Flicker memory size to  "+
                      newMemSize+" bytes.", Color.blue );
        util.showMsg2("Restart Flicker to use the new memory size limits.",
                      Color.blue); 
      }
      else
      {
        String msg= "FAILED! Unable to change startup memory size";
        util.showMsg2(msg, Color.red);
        util.popupAlertMsg(msg, flk.alertColor);
      }
      return;
    } /* Resize LAX file data for Flicker. */

    else if(cmd.equals("ResetDefaultView"))
    { /* Reset default view */
      flk.resetDefaultView();
      return;
    } 
    
    /*  ............... VIEW Menu menu ........... */
        
    /*  ............... LANDMARK Menu menu  ........... */
    else if(cmd.equals("AddLM"))
    { /* Mark last landmark (C-A)  KeyEvent.VK_A */
      if(flk.viewLMSflag==false)
      {
        String msg= "'View Landmarks' was turned off, turning back on.\n";
        util.appendReportMsg(msg); 
        flk.viewLMSflag= true;
        bGui.mi_ViewLmsCB.setState(flk.viewLMSflag);
      }      
      lms.addLandmark();
      return;
    }
    
    else if(cmd.equals("DelLM"))
    { /* Unmark last landmark (C-D) KeyEvent.VK_D */
      lms.deleteLandmark();            /* Undo last landmark */
      return;
    }
    
    else if(cmd.equals("LMsim"))
    { /* show landmark similarity */
      lms.showLandmarkSimilarity();
      return;
    }
    
    else if(cmd.equals("Set3LandmarksDemoGels"))
    { /* Set 3 pre-defined landmarks for demo gels (C-Y)  KeyEvent.VK_Y */
      lms.addDemoLandmarks(3);
      return;
    }
    
    else if(cmd.equals("Set6LandmarksDemoGels"))
    { /* Set 6 pre-defined landmarks for demo gels (C-Z)  KeyEvent.VK_Z */
      lms.addDemoLandmarks(6);
      return;
    }
    
    /*  ............... TRANSFORM Menu menu ........... */
    else if(cmd.equals("Xform:repeatLast"))
    { /* Do "Repeat last transform CTRL-T" */
      if(flk.doingXformFlag)
      {
        String msg= "Can't do new transform until "+ flk.xformName +
                    " finishes.";
        util.popupAlertMsg(msg, flk.alertColor);
      }
      else
      {
        processTransform(flk.xformName, lastISName, extFctNbr);
      }
      return;
    }
    
    else if(cmd.equals("AffineWarp") || cmd.equals("PolyWarp"))
    { /* Do Warp transform */
      /*  Warp Affine or Polynomial Transform to I2 image
       * based on current set of landmarks
       */      
      if(flk.doingXformFlag)
      {
        String msg= "Can't do new transform until "+ flk.xformName +
                    " finishes.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }      
      if(! leftOrRightSelectedFlag)
      {
        String msg= "You must select image to transform first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      flk.doingXformFlag= true;   /* set flag to lock up image transforms */
      
      if(cmd.equals("AffineWarp"))
        flk.xformName= "AffineWarp";
      else if(cmd.equals("PolyWarp"))
        flk.xformName= "PolyWarp";
      
      Date date= new Date();
      flk.iData1.startTime= date.getTime();  /* grab start time
                                              * msec since 1970 */
      flk.imagesToProcess= 1;	               /* do 1 image */
      util.setFlickerState(false);   /* disable flicker while doing Xform */
      flk.validAffineFlag= false;
      //this.setCursor(Frame.WAIT_CURSOR); 
      String dbMenuName;
      /* NOTE: by clicking on the LEFT image we transform the it to the
       * geometry of the RIGHT image and vice versa.
       */
      if(lastISName.equals("left"))
      { /* Flip left to geometry of right */
        flk.ixf1= new ImageXform(flk, "left", flk.iData1, flk.xformName,
                                 0 /* not extern fct */); 
        flk.ixf1.start();
        flk.ixf1.setPriority(Thread.MIN_PRIORITY);
        dbMenuName= util.getFileNameFromPath(flk.imageFile1);
        util.showMsg("Doing ["+flk.xformName + "] on "+dbMenuName,
                     Color.black );
      }
      else if(lastISName.equals("right"))
      { /* Flip right to geometry of left */
        flk.ixf2= new ImageXform(flk, "right", flk.iData2, flk.xformName,
                                 0 /* not extern fct */); flk.ixf2.start();
        flk.ixf2.setPriority(Thread.MIN_PRIORITY);
        dbMenuName= util.getFileNameFromPath(flk.imageFile2);
        util.showMsg("Doing ["+flk.xformName + "] on "+dbMenuName,
                     Color.black );
      }      
     
      return;
    } /* Do Warp transform */    
    
    else
    { /* images exist - test if do image transform(s) */
      for(int i= 0;i<ImageXform.nxfCmds;i++)
        if(ImageXform.xfCmd[i].equals(cmd) ||
           (extFctNbr>0)	/* External function requested  */ )
        { /* process grayscale transform on I1 and I2 */ 
          if(flk.doingXformFlag)
          {
            String msg= "Can't do new transform until "+ flk.xformName +
                        " finishes.";
        util.popupAlertMsg(msg, flk.alertColor);
          }
          else
          {
            processTransform(cmd, lastISName, extFctNbr);
          }
          return;
        }
    } /* images exist - test if do image transform(s) */    
    
    /*  ......... QUANTIFY Menu | Measure by circle submenu ....... */
    if(cmd.startsWith("Circle"))
    { /* Capture background and object measurements with circle masks */       
      if(! leftOrRightSelectedFlag)
      {
        String msg= "Select 'right' or 'left' image first ";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }     
      
      if(flk.doingXformFlag)
      {
        String msg= "Can't measure until transform "+flk.xformName +
                     " finishes processing.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }       
      
      if(cmd.equals("CircleBkgrd"))
      { /* Measure by circle | Capture background (C-B) */
        if(! leftOrRightSelectedFlag)
        {
          String msg= "Select left or right image to measure first.";
          util.popupAlertMsg(msg, flk.alertColor);
          return;
        } 
        ImageDataMeas idM= iData.idM;
        float bkgrdVal= idM.captureBackgroundValue();
        if(bkgrdVal>0.0F)
        { /* print the background measurement value */
          iData.idM.showBkgrdValue();
        }
      } /* Measure by circle | Capture background */
      
      else if(cmd.equals("CircleMeas"))
      { /* Measure by circle | Capture measurement (C-M) */
        if(! leftOrRightSelectedFlag)
        {
          String msg= "Select left or right image to measure first.";
          util.popupAlertMsg(msg, flk.alertColor);
          return;
        }
        
        float measVal= iData.idM.captureMeasValue();
        if(measVal>=0)     
        { /* print the measurement value and update  measured spot */
          iData.idM.showMeasValue("circleMask");
          
          /* If enabled, update the Spot .id and .name fields from 
           * the active map .
           */
          Spot curSpot= iData.idM.curSpot;
          String clickCGIbaseURL= null;
          if(iData== flk.iData1)
            clickCGIbaseURL= flk.clickableCGIbaseURL1;   
          if(iData== flk.iData2)
            clickCGIbaseURL= flk.clickableCGIbaseURL2;  
          
          if(clickCGIbaseURL!=null && curSpot!=null && 
             (flk.useSwiss2DpageServerFlag || flk.usePIRUniprotServerFlag || 
              flk.usePIRiProClassServerFlag || flk.usePIRiProLinkFlag))
          { /* get Swiss-Prot ID, NAME and save in spot*/
            util.showMsg(
                  "Searching for spot identification on active map server",
                         Color.red);
            util.showMsg2("", Color.black);
            String pData[]= util.getProteinIDdataByXYurl(clickCGIbaseURL,
                                                         curSpot.xC,
                                                         curSpot.yC,null);
            if(pData!=null)
            { /* found it */
              curSpot.id= pData[0];
              curSpot.name= pData[1];
              util.showMsg("Found spot identification on active map server",
                           Color.black);
              util.showMsg2("Protein Name["+curSpot.id+
                            "] Id ["+curSpot.name+"]",
                            Color.black);
              flk.repaint();
            }
            else
            {
              util.showMsg("Spot not found on active map server",Color.black);
              util.showMsg2("",Color.black);
            }
          } /* get Swiss-Prot ID, NAME and save in spot*/
        } /* print the measurement value and update measured spot */
      } /* Measure by circle | Capture measurement */
      
      else if(cmd.equals("CircleClearMeas"))
      { /* Measure by circle | Clear measurement (C-W) */
        iData.idM.bkgrdGrayValue= -1;
        iData.idM.measGrayValue= -1;        
      }   
      
      else if(cmd.equals("CircleClearSpotList"))
      { /* Measure by circle | Clear spot list */
        /* Prompt "are you sure" and clear spot list if 'yes' */
        iData.idSL.clearSpotList();    
      } 
      
      else if(cmd.equals("CircleLookupSpotListProtIDs"))
      { /* "Lookup Protein IDs and Names from active map server (selected image)" */
        if(! leftOrRightSelectedFlag)
        {
          String msg= "Select left or right image to measure first.";
          util.popupAlertMsg(msg, flk.alertColor);
          return;
        } 
        boolean ok= iData.idSL.lookupProtIDandNameToSpotList(iData);
        util.showMsg2("",Color.black);
        if(ok)
          util.showMsg1(
            "Updated spot list protein ID and name from active map server.",
                         Color.black);
        else
          util.showMsg1(
            "Failed to update spot list protein ID & names from active map server.",
                         Color.black);
      }                  
             
      else if(cmd.equals("CircleListSpotsInSpotList"))
      { /* "List spots in the spot list" */
        if(! leftOrRightSelectedFlag)
        {
          String msg= "Select left or right image to measure first.";
          util.popupAlertMsg(msg, flk.alertColor);
          return;
        } 
        String
          imageName= flk.util.getFileNameFromPath(iData.imageFile),
          sR= Spot.listSpotListData(imageName, iData.calib.unitsAbbrev,
                                    iData.idSL.spotList, 
                                    iData.idSL.nSpots);
        if(sR==null)
        {
          String msg= "There are no spots to report";
          util.popupAlertMsg(msg, flk.alertColor);
        }
        else
          flk.util.appendReportMsg(sR);      
        util.forceReportWindowPopup(); /* force append Report Window to popup*/
      } /* "List spots in the spot list" */  
      
      else if(cmd.equals("CircleListSpotsInSpotList-tab-delim"))
      { /* "List spots in the spot list - tab-delim" */
        if(! leftOrRightSelectedFlag)
        {
          String msg= "Select left or right image to measure first.";
          util.popupAlertMsg(msg, flk.alertColor);
          return;
        } 
        String
          imageName= flk.util.getFileNameFromPath(iData.imageFile),
          sR= Spot.listSpotListDataTabDelim(imageName, 
                                            iData.calib.unitsAbbrev,
                                            iData.idSL.spotList, 
                                            iData.idSL.nSpots);
        if(sR==null)
        {
          String msg= "There are no spots to report";
          util.popupAlertMsg(msg, flk.alertColor);
        }
        else
          flk.util.appendReportMsg(sR);      
        util.forceReportWindowPopup(); /* force append Report Window to popup*/
      } /* "List spots in the spot list" - tab-delim" */   
      
      else if(cmd.equals("CircleRmvSpotFromSpotList"))
      { /* Measure by circle | Delete selected spot from spot list (C-K) */
        if(! leftOrRightSelectedFlag)
        {
          String msg= "Select left or right image to measure first.";
          util.popupAlertMsg(msg, flk.alertColor);
          return;
        } 
        int
          xC= lastIS.getXObjPosition(),
          yC= lastIS.getYObjPosition();
        ImageDataSpotList idSL= iData.idSL;
        Spot s= idSL.lookupSpotInSpotListByXY(xC, yC);
        if(s==null)
        {
          String
            msg= "No measured spot to delete. Click closer to the spot and try again.";
          util.popupAlertMsg(msg, flk.alertColor);
          return;
        }
        else
        { /* Delete the spot */   
          String
            msg= "Are you sure you want to delete this spot [yes/no]?";
          PopupTextFieldDialog ptfd= new PopupTextFieldDialog(flk,msg,"yes");
          if(ptfd.okFlag && ptfd.answer.equalsIgnoreCase("yes"))
          { /* finish deleting  the spot */
            if(idSL.rmvSpotFromSpotList(s))            
              util.showMsg("Deleted spot #"+s.nbr+" Id:"+s.id, 
                           Color.black);
            else
            {
              msg= "Problem deleting spot #"+s.nbr+" Id:"+s.id;
              util.popupAlertMsg(msg, flk.alertColor);
            }
          } /* finish deleting the spot */
        } /* Delete the spot */
      } /* Measure by circle | Delete selected spot from spot list */ 
      
      flk.repaint();      
    } /* Capture background and object measurements with circle masks */    
    
    else if(cmd.equals("EditMeasuredSpotsFromSpotList") || 
            cmd.equals("EditMeasuredSpotsIDfield"))
    { /* Measure by circle | Edit selected spot(s) from spot list */
      /* Edit selected spots if they exist using either 
       *  "Edit selected spot(s) from spot list (C-E)" or
       *  "Edit selected spot(s) 'id' fields (C-I)".
       * The short form (C-I) pops up a short prompt to define 'id' field 
       * for selected measured spot(s).
       * The long form (C-E) pops up all of the fields.
       */     
      if(flk.i1IS.img_selectedFlag || flk.i2IS.img_selectedFlag)
      { /* see if one or both spots are defined */
        boolean editOnlyIDflag= cmd.equals("EditMeasuredSpotsIDfield");
        int
          xC1= flk.i1IS.getXObjPosition(),
          yC1= flk.i1IS.getYObjPosition(),
          xC2= flk.i2IS.getXObjPosition(),
          yC2= flk.i2IS.getYObjPosition();
        Spot
          s1= flk.iData1.idSL.lookupSpotInSpotListByXY(xC1, yC1),
          s2= flk.iData2.idSL.lookupSpotInSpotListByXY(xC2, yC2);
        if(s1==null && s2==null)
        {
          String
      msg= "No measured spot(s) to edit. Click closer to the spot(s) and try again.";
          util.popupAlertMsg(msg, flk.alertColor);
        }
        else
        { /* yes - edit 1 or both spots */
          boolean ok= false;
          
          if(s1!=null && s2!=null)
          {            
            /* If clickable DB enabled, get data from protein web DB. */
            //if(!util.getAnnotationFromServer(flk.iData1,s1))
            //  util.getAnnotationFromServer(flk.iData2,s2);
            /* now go edit it */
            ok= flk.iData1.idSL.editSpotFromSpotList(s1,s2,editOnlyIDflag);
            util.showMsg("Editing left spot #"+s1.nbr+
                         ", and right spot #"+s2.nbr, Color.black);
          }
          
          else if(s1!=null)
          {
            /* If clickable DB enabled, get data from protein web DB. */
            //util.getAnnotationFromServer(flk.iData1,s1);
            ok= flk.iData1.idSL.editSpotFromSpotList(s1,editOnlyIDflag);
            util.showMsg("Editing left spot #"+s1.nbr, Color.black);
          }
          
          else if(s2!=null)
          {
            /* If clickable DB enabled, get data from protein web DB. */
            //util.getAnnotationFromServer(flk.iData2,s2);
            ok= flk.iData2.idSL.editSpotFromSpotList(s2,editOnlyIDflag);
            util.showMsg("Editing right spot #"+s2.nbr, Color.black);
          }
          
          if(!ok)
          {
            String msg= "No change in spot";
            util.popupAlertMsg(msg, flk.alertColor);
          }
        } /* yes - edit 1 or both spots */
      } /* see if one or both spots are defined */
    } /* Measure by circle | Edit selected spot from spot list */
       
    else if(cmd.equals("ListPairedAnnMeanNormSpotList-tab-delim"))
    { /* "List 'id'-paired annotated mean norm. spots in both spot lists (tab-delimited)" */
      /* Normalize by the mean spot value of spots in each of the
       * corresponding spot lists.
       */
      boolean normByMeanSpotListFlag= true;
      String sR= Spot.listPairedSpotListDataTabDelim(normByMeanSpotListFlag);
      if(sR==null)
      {
        String msg= "No paired spots with matching id's to report - check spot lists.";
        util.popupAlertMsg(msg, flk.alertColor);
      }
      else
        flk.util.appendReportMsg(sR);      
      util.forceReportWindowPopup(); /* force append Report Window to popup*/
    } /* "List paired annotated spots in the spot list - tab-delim" */
       
    else if(cmd.equals("ListPairedAnnSpotList-tab-delim"))
    { /* "List 'id'-paired annotated spots in both spot lists (tab-delimited)" */
      /* Do NOT normalize spot lists.  */
      boolean normByMeanSpotListFlag= false;
      String sR= Spot.listPairedSpotListDataTabDelim(normByMeanSpotListFlag);
      if(sR==null)
      {
        String msg= "No paired spots with matching id's to report - check spot lists.";
        util.popupAlertMsg(msg, flk.alertColor);
      }
      else
        flk.util.appendReportMsg(sR);      
      util.forceReportWindowPopup(); /* force append Report Window to popup*/
    } /* "List paired annotated spots in the spot list - tab-delim" */
    
    /*  ......... QUANTIFY Menu | Measure by boundary submenu ....... */
    else if(cmd.equals("MeasBkgrd") || cmd.equals("MeasObject") ||
            cmd.equals("MeasDone"))
    { /* Start drawing boundary of object to measure background */
      String sOpr= (cmd.equals("MeasBkgrd")) ? "Bgrd" : "Quant";      
      if(flk.doingXformFlag)
      {
        String msg= "Can't measure until transform "+flk.xformName +
                    " finishes processing.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      if(! leftOrRightSelectedFlag)
      {
        String msg= "Select 'right' or 'left' image first ";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }      
      
      if(cmd.equals("MeasDone"))
      {
        lastIS.finishMeasurement();
        lastIS.setBndState(false);
        if(lastIS!=null && lastIS.getValidFeaturesFlag())
        {
          String sFeatures= lastIS.cvFeatures2str(imgFile);
          int rows= 6;
          int cols= 72;
          popup.popupTextAreaViewer(sFeatures, rows, cols);
        }
      }
      else
      {
        lastIS.setBndState(true);
        lastIS.startMeasurement(sOpr);
      }
      return;
    } /* Start drawing boundary of object to measure background */
    
    /*  ......... QUANTIFY Menu | Print data-window submenu ....... */
    else if (cmd.equals("WinDump"))
    { /* "Show data-window of selected pixel (C-V) */  
      if(! leftOrRightSelectedFlag)
      {
        String msg= "First select right or left image.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }  
      int 
        xObj= lastIS.getXObjPosition(),
        yObj= lastIS.getYObjPosition();
      if(xObj==-1 || yObj==-1)
      {
        String msg= "First select a spot in the image.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      int
        halfWidth= flk.maxColsToPrint/2,
        stepSize= 1,
        x1= xObj - halfWidth, 
        x2= xObj + halfWidth, 
        y1= yObj - halfWidth, 
        y2= yObj + halfWidth;
      String title= iData.imageFile;      
      
      String sData= flk.windmp.winDump(iData.iPix, x1, x2,  y1, y2, 
                                       iData.iWidth,iData. iHeight,
                                       title, flk.winDumpRadix,
                                       flk.maxColsToPrint, stepSize,
                                       iData);
      flk.util.appendReportMsg(sData+"\n");      
      util.forceReportWindowPopup(); /* force append Report Window to popup*/
      return;
    }  /* "Show data-window of selected pixel (C-V) */
            
    /*  ......... QUANTIFY Menu | Calibrate submenu ....... */
    else if (cmd.equals("CalibODstepWedge"))
    { /* Calibrate OD step wedge */ 
      if(! leftOrRightSelectedFlag)
      {
        String msg= "Select left or right image to calibrate first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      
      /* Setup the  histogram, peaks and extrpolate map for the current ROI.
       * If you are using the leukemia demos, then preload their
       * data and calibration OD gray-peak table for histogram
       * including ND step-wedge ROI. 
       * If you are NOT using the preloads, then this assumes that the ROI
       * exists. If not, no histogram will be generated.
       */
      boolean ok= false;
      if(flk.useDemoLeukemiaCalPreFlag)
      { /* force (ROI, OD list) for leukemia demo data */        
        ok= iData.calib.demo_setDefaultWedgeData_LeukemiaGels(iData);
      }
      else
      { /* set up histogram, peaks and extrpolate map for the current ROI */
        boolean inheritODflag= iData.calib.inheritNDwedgeODvalues(iData);
        ok= iData.calib.calcHistFindPeaksAndExtrapolate(iData);
      }
      
      /* Create ND wedge ROI calibration histogram popup */ 
      iData.dwCalHist= new DrawHistogram(flk,null,null,null,
                                        DrawHistogram.MODE_CALIB_ND_ROI_HIST);      
      
      iData.dwCalHist.setTitleHist(
                             "Calibrate grayscale from step wedge, image "+
                                   util.getFileNameFromPath(iData.imageFile));
      /* Note dwCalHist.drawHistogram() gets data directly from 
       * the iData.calib instance.
       */      
      iData.dwCalHist.setVisible(true,false);
      
      /* After calibration histogram GUI pops up, then prompt them to make
       * the ROI and then press the "Analyze ROI" button
       */
      if(!ok)
      { /* ROI does not exist and Wedge calibration peak table does not exist */
        String
          msg= "Assign Wedge calibration ROI in image. "+
               "Then press 'Analyze Wedge ROI' in popup.";
        util.popupAlertMsg(msg, flk.alertColor);
      }
      
      return;
    } /* Calibrate OD step wedge*/
    
    else if (cmd.equals("CalibODspotList"))
    { /* Calibrate OD spot list */       
      if(! leftOrRightSelectedFlag)
      {
        String msg= "Select left or right image to calibrate first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      /* Create calibration from pre-existing circle mask measurements
       * spot list and pop it up. This code will create iData.mapGrayToOD[].
       * It creates a calibration pseudo-histogram where the peaks are the
       * spot measurement mean gray values and the frequencies are the
       * spot areas. 
       * It will backup the current spot list and then restore it after
       * the calibration is finished. This means that you can define
       * a new spot list used just for calibration.
       */      
      boolean inheritODflag= iData.calib.inheritNDwedgeODvalues(iData);
      iData.dwCalHist= new DrawHistogram(flk,null,null,null,
                                     DrawHistogram.MODE_CALIB_SPOTLIST_HIST);
      iData.dwCalHist.setTitleHist(
                              "Calibrate grayscale from spot list, image "+
                                   util.getFileNameFromPath(iData.imageFile));
      /* Copy spot list to the histogram and calibration peak table. */
      if(iData.idSL.nSpots>0)
      { /* backup spotList[0:nSpots-1] */
        if(!iData.dwCalHist.genPeaksFromMeasSpotList())
        { /* problem with too many spots */
          iData.dwCalHist.close();
          return;
        }
        iData.idSL.backupSpotList(false);        /* backs up current spot list
                                                  * but saves current list */
      } /* backup spotList[0:nSpots-1] */
      
      iData.dwCalHist.setVisible(true,false);
      return;
    } /* Calibrate OD spot list */
    
    else if (cmd.equals("CalibMW"))
    { /* Calibrate MW */
      util.showMsg("Not Available yet.", Color.red);
      return;
    }
    
    else if (cmd.equals("CalibpIe"))
    { /* Calibrate pIe */
      util.showMsg("Not Available yet.", Color.red);
      return;
    }
    
    /*  ......... QUANTIFY Menu | Region Of Interest submenu ....... */
    else if (cmd.equals("SetRoiULHC"))
    { /* "Set ROI ULHC (C-U)" */
      /* define ULHC of computing window KeyEvent.VK_U CTRL/U */
      if(iData==null || lastIS==null)
        return;
      int
        xU= lastIS.getXObjPosition(),
        yU= lastIS.getYObjPosition();
      Point p;
      if(flk.useGuardRegionImageFlag)       
        if(lastIS.iData.mag!=1.0)
        {
          int
            w= (int)(lastIS.siCanvas.guardWidth/2),
            h= (int)(lastIS.siCanvas.guardHeight/2);
           p= lastIS.iData.mapZoomToState(new Point(xU,yU),w,h);
        }
        else
        p= lastIS.iData.mapZoomToState(new Point(xU,yU),
                                      (int)(lastIS.siCanvas.guardWidth/2),
                                      (int)(lastIS.siCanvas.guardHeight/2)); 
      else
        p= lastIS.iData.mapZoomToState(new Point(xU,yU)); 
      
      xU= p.x;
      yU= p.y;
      
      ImageDataROI idROI= iData.idROI;
      idROI.roiX1= xU;
      idROI.roiY1= yU;
      idROI.forceROIUpperAndLowerCorners();
      idROI.copyROI2CW();
      flk.util.showMsg("Setting ULHC ("+xU+","+yU+") of ROI: "+
                       lastIS.name+" image",Color.black);
      flk.repaint();
      return;
    }
    
    else if (cmd.equals("SetRoiLRHC"))
    { /* "Set ROI LRHC (C-L)" */      
      /* NOTE: handled in EventKbd handler for KeyEvent.VK_L CTRL/L */
      /* [TODO] Set the (cwx2,cwy2) in calib for current image */
      if(iData==null || lastIS==null)
        return;
      int
        xL= lastIS.getXObjPosition(),
        yL= lastIS.getYObjPosition();
      Point p;
      if(flk.useGuardRegionImageFlag)
        p= lastIS.iData.mapZoomToState(new Point(xL,yL),
                                       (int)(lastIS.siCanvas.guardWidth/2),
                                       (int)(lastIS.siCanvas.guardHeight/2)); 
      else        
        p= lastIS.iData.mapZoomToState(new Point(xL,yL));    
      
      xL= p.x;
      yL= p.y;
          
      ImageDataROI idROI= iData.idROI;    
        
      idROI.roiX2= xL;
      idROI.roiY2= yL;
      idROI.forceROIUpperAndLowerCorners();
      idROI.copyROI2CW();
      
      flk.util.showMsg("Setting LRHC ("+xL+","+yL+") of ROI: "+
                       lastIS.name+" image",Color.black);
      flk.repaint();
      return;
    }
    
    else if (cmd.equals("ClearROI"))
    { /* "Clear Region Of Interest, ROI (C-W)" */
      if(iData==null || lastIS==null)
        return;
      ImageDataROI idROI= iData.idROI;
      idROI.setROI(-1,-1,-1,-1); /* clear C-U/C-L ROI */
      idROI.copyROI2CW();        /* clear computing window too */
      flk.util.showMsg("Clearing * Region Of Interest: "+ 
                       lastIS.name+" image",
                       Color.black);
      flk.repaint();
      return;
    }
    
    else if(cmd.equals("ShowROIhist"))
    { /* Create and display ROI histogram popup (C-H) */
      if(iData==null)
      {
        String msg= "Select left or right image to show ROI histogram first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      if(iData.dwHist==null)
      { /* create ROI calibration histogram popup */
        iData.dwHist= new DrawHistogram(flk,null,null,null,
                                        DrawHistogram.MODE_CW_ROI_HIST);
      }
      
      /* Create histogram of ROI and take a measurement */      
      ImageDataROI idROI= iData.idROI;
      if(! idROI.isValidROI())
      { /* Force the ROI to full window if not defined */
        idROI.setROI(0, 0, iData.iWidth-1, iData.iHeight-1);
        idROI.copyROI2CW();
      }
      float measVal= iData.idM.captureMeasCWvalue();
      if(measVal>=0)
      {
        iData.idM.showMeasValue("compROI");
      }
      /* Now display it */
      iData.dwHist.setTitleHist("Histogram of ROI ["+
                                idROI.cwx1+":"+idROI.cwx2+","+
                                idROI.cwy1+":"+idROI.cwy2+"] "+
                              util.getFileNameFromPath(iData.imageFile));
      if(! iData.dwHist.isVisibleFlag)
        iData.dwHist.setVisible(true,true);
      else
        iData.dwHist.updateHistogramPlot(true);
    }  /* Create and display ROI histogram popup (C-H) */
    
    else if(cmd.equals("ROIMeas"))
    { /* Quantify | Region Of Interest (ROI) | Capture measurement (C-R) */ 
      if(! leftOrRightSelectedFlag)
      {
        String msg= "Select left or right image to define RPI first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }             
      ImageDataROI idROI= iData.idROI; 
      if(! idROI.isValidROI())
      {        
        String msg= "Please assign Computing ROI in image first.";
        util.popupAlertMsg(msg, flk.alertColor);
        return;
      }
      float measVal= iData.idM.captureMeasCWvalue();
      if(measVal>=0)
        iData.idM.showMeasValue("compROI");
    } /* Quantify | Region Of Interest (ROI) | Capture measurement (C-R) */
        
    /* ............... HELP Menu menu ........... */
    
    else if (cmd.equals("Help:About"))
    {
       String msg=  "\n"+flk.title +
          "\n"+
          "Flicker was created by Peter Lemkin (NCI-Frederick, lemkin@ncifcrf.gov),\n"+
          "Greg Thornwall (SAIC-Frederick), Jai Evans (CIT/NIH)\n"+
          "\n"+
          "Open source license:\n"+
          "This work was produced by Peter Lemkin of the National Cancer\n"+
          "Institute, an agency of the United States Government.  As a work of\n"+
          "the United States Government there is no associated copyright.  It is\n"+
          "offered as open source software under the Mozilla Public License\n"+
          "(version 1.1) subject to the limitations noted in the accompanying\n"+
          "LEGAL file. This notice must be included with the code. The Flicker\n"+
          "Mozilla and Legal files are available on\n"+
          "http://open2dprot.sourceforge.net/Flicker/\n";
   
      util.showMsg(msg, Color.black);
      return;
    }
    
    else if (cmd.equals("Help:webSiteVersion"))
    {
      popup.popupViewer(flk.REF_VERSION_NBR, flk.popupWindowName);
      return;
    }    
    
    else if (cmd.equals("Help:FlickerHome"))
    {
      popup.popupViewer(flk.FLICKER_HOME, flk.popupWindowName);
      return;
    }    
    
    else if (cmd.equals("Help:RefMan"))
    {
      popup.popupViewer(flk.REF_MANUAL, flk.popupWindowName);
      return;
    }    
    
    else if (cmd.equals("Help:quickstart"))
    {
      popup.popupViewer(flk.QUICK_START, flk.popupWindowName);
      return;
    }  
    
    else if (cmd.equals("Help:RefMan-keystrokes"))
    {
      popup.popupViewer(flk.REF_MAN_KEYSTROKES, flk.popupWindowName);
      return;
    } 
    
    else if (cmd.equals("Help:RefMan-mouse"))
    {
      popup.popupViewer(flk.REF_MAN_MOUSE, flk.popupWindowName);
      return;
    } 
    
    else if (cmd.equals("Help:RefMan-sliders"))
    {
      popup.popupViewer(flk.REF_MAN_SLIDERS, flk.popupWindowName);
      return;
    } 
    
    else if (cmd.equals("Help:RefMan-checkboxes"))
    {
      popup.popupViewer(flk.REF_MAN_CHECKBOX, flk.popupWindowName);
      return;
    } 
    
    else if (cmd.equals("Help:RefMan-menus"))
    {
      popup.popupViewer(flk.REF_MAN_MENUS, flk.popupWindowName);
      return;
    }  
    
    else if (cmd.equals("Help:RefMan-transforms"))
    {
      popup.popupViewer(flk.REF_MAN_TRANSFORMS, flk.popupWindowName);
      return;
    }  
    
    else if (cmd.equals("Help:RefMan-CreateSpotList"))
    {
      popup.popupViewer(flk.REF_MAN_SPOTLISTS_CREATE, flk.popupWindowName);
      return;
    }  
    
    else if (cmd.equals("Help:RefMan-AnnotateSpots"))
    {
      popup.popupViewer(flk.REF_MAN_SPOTLISTS_ANNOTATE, flk.popupWindowName);
      return;
    }  
    
    else if (cmd.equals("Help:RefMan-PutativeIDs"))
    {
      popup.popupViewer(flk.REF_MAN_PUTATIVESPOTID, flk.popupWindowName);
      return;
    }   
    
    else if (cmd.equals("Help:RefMan-updates"))
    {
      popup.popupViewer(flk.REF_MAN_UPDATING, flk.popupWindowName);
      return;
    }   
    
    else if (cmd.equals("Help:RefMan-addUserImages"))
    {
      popup.popupViewer(flk.REF_MAN_ADDUSERIMAGES, flk.popupWindowName);
      return;
    }      
    
    else if (cmd.equals("Help:Vignettes"))
    {
      popup.popupViewer(flk.VIGNETTES, flk.popupWindowName);
      return;
    }      
    
    else if (cmd.equals("Help:WalkerBookChapter2005"))
    {
      popup.popupViewer(flk.WALKER_BOOK_CHAPTER_2005, flk.popupWindowName);
      return;
    }
       
    else if (cmd.equals("Help:oldFlickerApplet"))
    {
      popup.popupViewer(flk.OLD_FLICKER_HOME, flk.popupWindowName);
      return;
    }
    
    else if (cmd.equals("Help:EP97Paper"))
    {
      popup.popupViewer(flk.EP97PAPER_FILE, flk.popupWindowName);
      return;
    }
    
    else if (cmd.equals("Help:Poster1"))
    {
      popup.popupViewer(flk.POSTER_FILE1, flk.popupWindowName);
      return;
    }
    
    else if (cmd.equals("Help:Poster2"))
    {
      popup.popupViewer(flk.POSTER_FILE2, flk.popupWindowName);
      return;
    }
    
    else if (cmd.equals("Help:Poster3"))
    {
      popup.popupViewer(flk.POSTER_FILE3, flk.popupWindowName);
      return;
    }
    
    else if (cmd.equals("Help:Poster4"))
    {
      popup.popupViewer(flk.POSTER_FILE4, flk.popupWindowName);
      return;
    }
    
    else if (cmd.equals("Help:SunJAIlicense"))
    {
      popup.popupViewer(flk.SUN_JAI_LICENSE, flk.popupWindowName);
      return;
    }
    
    else if (cmd.equals("Help:SWISS-2DPAGE"))
    {
      popup.popupViewer(flk.SWISS_2DPAGE, flk.popupWindowName);
      return;
    }
    
    else if (cmd.equals("Help:WORLD-2DPAGE"))
    {
      popup.popupViewer(flk.WORLD_2DPAGE, flk.popupWindowName);
      return;
    }
    
    else if (cmd.equals("Help:2D-HUNT"))
    {
      popup.popupViewer(flk.TWO_D_HUNT, flk.popupWindowName);
      return;
    }
    
    else if (cmd.equals("Help:Google-2Dsearch"))
    {
      popup.popupViewer(flk.GOOGLE_2D_SEARCH, flk.popupWindowName);
      return;
    }       
   
    /* ------------- BUTTONS -------------- */
    else if (cmd.equals("Report scroller values"))
    { /* "Report scroller values" button */
      String sState= util.getScrollerValuesStateStr();      
      flk.util.appendReportMsg(sState+"\n");      
      util.forceReportWindowPopup(); /* force append Report Window to popup*/
      return;
    }
    
    else 
    { /* "Missing or misspelled cmd" */
      String msg= "Missing or misspelled cmd: '"+cmd+"'";
      util.popupAlertMsg(msg, flk.alertColor);
    }
    
  } /* actionPerformed */
  
  
  /**
   * shutOffGuardRegion() - turn off guard region
   */  
  public void shutOffGuardRegion()
  {/* shutOffGuardRegion */   
    if(flk.useGuardRegionImageFlag==false)
      return;
    
    bGui.mi_useGuardImgsCB.setState(false);
    flk.useGuardRegionImageFlag= false;
    
    /* shut off zoom */
    flk.i1IS.iData.mag= 1.0;
    flk.i2IS.iData.mag= 1.0;    
    
    flk.i1IS.addGuardRegion(flk.useGuardRegionImageFlag);
    flk.i2IS.addGuardRegion(flk.useGuardRegionImageFlag);  
    flk.flkIS.addGuardRegion(flk.useGuardRegionImageFlag);   
  } /* shutOffGuardRegion */
  
  
  /**
   * turnOnGuardRegion() - turn on guard region
   */  
  public void turnOnGuardRegion()
  {/* turnOnGuardRegion */   
    if(flk.useGuardRegionImageFlag==true)
      return;
    
    bGui.mi_useGuardImgsCB.setState(true);
    flk.useGuardRegionImageFlag= true;
    flk.i1IS.addGuardRegion(flk.useGuardRegionImageFlag);
    flk.i2IS.addGuardRegion(flk.useGuardRegionImageFlag);  
    flk.flkIS.addGuardRegion(flk.useGuardRegionImageFlag);   
  } /* turnOnGuardRegion */
  
  
} /* end of class EventMenu */

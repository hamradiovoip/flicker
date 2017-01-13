/* File: BuildGUI.java */

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent; 
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.BorderFactory; 
import javax.swing.border.Border;
//import javax.swing.border.TitledBorder;
//import javax.swing.border.EtchedBorder;


/**
 * BuildGUI class is used to build the Flicker GUI. This will create other
 * class instances in the process.
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

public class BuildGUI extends JFrame 
                      implements ActionListener, ItemListener, 
	                             WindowListener

{ /* BuildGUI */

  /** for JFrame */
  final static long serialVersionUID= 0;
  
  /** Flicker instance */
  Flicker 
    flk;
  /** Util instance */
  Util
    util;
  /** event handler */
  static EventMenu 
    evMu;
  
  /** title for frame */
  String
    title; 
  /** menu bar  */
  MenuBar
    mbar;

  /** Thickness of the scrollbar slider control */
  final public static int
    THICKNESS= 5;
  
  /** 0.30 sec default delay if not flickering */
  final public static int
    DEFAULT_DELAY= 300;
  /** minimum delay 0.10 sec */
  final public static int
    MIN_DELAY= 100;		
  /** maximum delay 10000 mSec= 10.0 Sec */
  final public static int
    MAX_DELAY= 10000;
  
  /** default flicker delay0.20 sec */
  final public static int
    DEFAULT_FLICKER_DELAY= 300;
  /** minimum flicker delay0.01 sec */
  final public static int
    MIN_FLICKER_DELAY= 10;	
  /** maximum flicker delay1000 mSec= 1.0 Sec */
  final public static int
    MAX_FLICKER_DELAY= 1000;
  
  /** threshold for LMS colinearity */
  final public static int
    THR_COLINEARITY= 2; 
  
  /** max number of menu items allowed. MUST be large since adding
   * names of Images/{directories} in paired and single items for
   * (File | Open user images | ...)
   */
  final private static  int
    MAX_CMDS= 5001;                      /* was 3001, 1001, 501 */
  /** max number of menu checkbox items allowed */
  final private static  int
    MAX_CHKBOX_CMDS= 201;  
               
  /** # of menu item commands [0:nCmds-1] */
  static int        
    nCmds= 0;	  
  /** [MAX_CMDS] list of menuItem commands labels */
  static String
    menuActionLabelList[];  	        
  /** [MAX_CMDS] list of menuItem (short-form) cmds for event handler */
  static String
    menuActionCmdList[]; 
  
  /** [0:nMImustSelect-1] list of MenuItems that must be selected else
   * they are disabled (grayed out).
   */
  static MenuItem
    menuItemMustSelect[];
  /* # of menuItems that must be selected else they are disabled */
  static int
    nMImustSelect= 0;
  
  /** # of menu CheckBoxMenuItem item cmds [0:nCBcmds-1] */
  static int
    nCBcmds= 0;
  /** [MAX_CHKBOX_CMDS] list of checkboxItem cmds menu labels */
  static String
    chkBoxMenuLabelList[];              
  /** [MAX_CHKBOX_CMDS] list of ALL checkboxItem cmds menu items 
   * corresponding to names
   */ 
  static CheckboxMenuItem  
    chkBoxMenuItemList[];    
  
  /** # of Transform menuItems */
  static int        
    nXformMenuCmds= 0;	  
  /** [MAX_CMDS] list of [0:nXformMenuCmds-1] Transform menuItems */
  static MenuItem
    xformMenuCmds[];  	  
  /** [MAX_CMDS] list of [0:nXformMenuCmds-1] disabled Transform menuItems */
  static boolean
    disableXformMenuCmds[]= null;  	        
  
  /** flag set if the user had too many images in the Images/ subdirectories */
  public static boolean
    userImagesProblemFlag= false;
  
  /** generic popup dialog */ 
  public PopupDialogQuery
    pdq= null;
  
  /** generic popup report */ 
  public ShowReportPopup
    pra= null;
  
  /** +++++++ Gui stuff +++++++ */
  	  
  /* Panel to hold image 1 and image 2 SOUTH of the main panel */
  public JPanel 
    pImages;
  
  /** from Mouse Adapters */
  public Component
    selectedComponent;		

  /** "File" pull down Menu list */
  public Menu 
    fileMenu;
  /** "Edit" pull down Menu list */
  public Menu 
    editMenu;	
  /** "View" pull down Menu list */
  public Menu 
    viewMenu;	
  /** "Landmark" pull down Menu list */
  public Menu 
    lmsMenu;	
  /** "Transform" pull down Menu list */
  public Menu 
    xformMenu;
  /** "Quantify" pull down Menu list */
  public Menu 
    quantMenu;	
  /** "Plugins" pull down Menu list */
  public Menu 
    pluginMenu= null;
  /** "Help" pull down Menu list*/
  public Menu 
    helpMenu;	
  
  /** last fDemo menu tree stub for use if rebuild it */
  public Menu 
    fDemoMenuStub;
  /** last fMap menu tree stub for use if rebuild it */
  public Menu 
    fMapMenuStub;
  /** last fRecent menu tree stub for use if rebuild it */
  public Menu 
    fRecentMenuStub;
  /** last fUser menu tree stub for use if rebuild it */
  public Menu 
    fUserMenuStub;
  /** Add directories for paired user images tree */
  public Menu 
    sUserPairTreeMnu; 
  /** Add directories for single user images tree */
  public Menu 
    sUserSingleTreeMnu; 
    
  /** View menu checkbox item linked with flickerCheckbox */
  CheckboxMenuItem
    mi_flickerCB;   
  /** View landmarks menu item checkbox */
  CheckboxMenuItem
    mi_ViewLmsCB;  
  /** View target menu item checkbox */
  CheckboxMenuItem   
    mi_ViewTargetCB;  
  /** View trial object menu item checkbox */
  CheckboxMenuItem
    mi_ViewTrialObjCB;  
  /** View boundaries menu item checkbox */
  CheckboxMenuItem 
    mi_ViewBoundaryCB;   
  /** View ROI rectangle menu item checkbox */
  CheckboxMenuItem 
    mi_ViewRoiCB; 
  
  /** View measurement circles menu item checkbox */
  CheckboxMenuItem 
    mi_ViewMeasCircleCB;
  
  /** [FUTURE] View last measured spot location as "circle"{ann} 
   * menu item checkbox */
   CheckboxMenuItem
     mi_ViewLastMeasSpotAsCircleCB;
  /** View last measured spot location as "+"{ann} menu item checkbox */
   CheckboxMenuItem
     mi_ViewLastMeasSpotAsPlusCB;
   
  /** View measured spot location as "circle"{ann} menu item checkbox.
   * Radio button with mi_ViewMeasSpotLocAsPlusCB
   */
   CheckboxMenuItem
     mi_ViewMeasSpotLocAsCircleCB;
  /** View measured spot location as "+"{ann} menu item checkbox.
   * Radio button with mi_ViewMeasSpotLocAsCircleCB
   */
   CheckboxMenuItem
     mi_ViewMeasSpotLocAsPlusCB;
   
  /** View measured spot annotation {ann} as "spot.nbr" menu item checkbox */
   CheckboxMenuItem
     mi_ViewAnnSpotNbrCB;
  /** View measured spot annotation {ann} as "spot.id" menu item checkbox */
   CheckboxMenuItem
     mi_ViewAnnSpotIdCB;
  
  /** View multiple browser popups menu item checkbox */
  CheckboxMenuItem 
    mi_MultPopupsCB;  
  /** View gang scrolling menu item checkbox */
  CheckboxMenuItem    
    mi_GangScrollImgsCB;  
  /** View gang scrolling menu item checkbox */
  CheckboxMenuItem    
    mi_GangZoomImgsCB;  
  /** View gang brightness/contrast menu item checkbox */
  CheckboxMenuItem    
    mi_GangBCImgsCB;  
  
  /* view Add guard region to edges of images */
  CheckboxMenuItem
    mi_useGuardImgsCB;
    
  /** View display gray level in image title menu item checkbox */
  CheckboxMenuItem    
    mi_DispGrayValsCB;  
  /** View Report Popup item checkbox */
  CheckboxMenuItem 
    mi_showReportPopupCB;
   
  /** Convert RGB image to grayscale using NTSC transform if needed 
   * checkbox.
   */
  CheckboxMenuItem 
    mi_useNTSCrgbTograyCvtCB;
    
  /** Auto measure, protein lookup in active server and Web page popup
   */
  CheckboxMenuItem 
    mi_autoMeasProtLookupPopupCB; 
  
  /** Use Swiss-2DPAGE DB (SWISS-2DPAGE) access checkbox.*/
  CheckboxMenuItem 
    mi_useSwiss2DpageServerCB;   
  /** Use PIR UniProt DB (UNIPROT) access checkbox.*/
  CheckboxMenuItem 
    mi_usePIRUniprotServerCB; 
  /** Use PIR iProClass DB (IPROCLASS) access checkbox.*/
  CheckboxMenuItem 
    mi_usePIRiProClassServerCB; 
  /** Use PIR uProLink DB (IPROLINK) access checkbox.*/
  CheckboxMenuItem 
    mi_usePIRiProLinkServerCB; 
  
  /** View DEBUGGING menu item checkbox */
  CheckboxMenuItem
    mi_dbugCB;	
  
  /** Use demo leukemia gels ND wedge calibration preloads 
   * item checkbox 
   */
  CheckboxMenuItem
    mi_Quant_UseLeukemiaDemoCalibCB; 
  /** Quant menu to compute total integrated density for spot
   * else the mean item checkbox 
   */
  CheckboxMenuItem
    mi_QuantTotDensityCB; 
  /* Quant menu C-J Toggle between list-of-spots else trial-spot 
   * measurement mode
   */
  CheckboxMenuItem
    mi_QuantSpotListModeCB;
  /** Use measurement counters checkbox */
  CheckboxMenuItem
    mi_MeasCtrCB;
  /** Quant threshold Inside menu item checkbox */
  CheckboxMenuItem
    mi_thresholdInsideCB; 
    
  /** Quant menu WinDmp size 5x5 checkbox */  
  CheckboxMenuItem
    mi_WinDmpSize5x5CB;
  /** Quant menu WinDmp size 10x10 checkbox */  
  CheckboxMenuItem
    mi_WinDmpSize10x10CB;
  /** Quant menu WinDmp size 15x15 checkbox */  
  CheckboxMenuItem
    mi_WinDmpSize15x15CB;
  /** Quant menu WinDmp size 20x20 checkbox */  
  CheckboxMenuItem
    mi_WinDmpSize20x20CB;
  /** Quant menu WinDmp size 25x25 checkbox */  
  CheckboxMenuItem
    mi_WinDmpSize25x25CB;
  /** Quant menu WinDmp size 30x30 checkbox */  
  CheckboxMenuItem
    mi_WinDmpSize30x30CB;
  /** Quant menu WinDmp size 35x35 checkbox */  
  CheckboxMenuItem
    mi_WinDmpSize35x35CB;
  /** Quant menu WinDmp size 40x40 checkbox */  
  CheckboxMenuItem
    mi_WinDmpSize40x40CB;
  
  /** Quant menu WinDmp radix decimal mode checkbox */
  CheckboxMenuItem
    mi_WinDmpRadixDecCB;
  /** Quant menu WinDmp radix octal mode checkbox */
  CheckboxMenuItem
    mi_WinDmpRadixOctCB;
  /** Quant menu WinDmp radix hex mode checkbox */
  CheckboxMenuItem
    mi_WinDmpRadixHexCB;
  /** Quant menu WinDmp radix optical density mode checkbox */
  CheckboxMenuItem
    mi_WinDmpRadixODCB;
  
  /** Use log of TIFF file else scale to 8-bit data */
  public CheckboxMenuItem
   mi_useLogTIFFfilesCB;
               
  /* "Enable saving transformed image when do a 'Save(As) state'" */  
  public CheckboxMenuItem
    mi_saveOimagesWhenSaveStateCB;
         
  /* "Use protein DB browser, else lookup ID and name on active images" */  
  public CheckboxMenuItem
    mi_useProteinDBbrowserCB;
   
  /** Toggle flicker on/off */
  public JCheckBox
    flickerCheckbox;  
  /** enable clickable images */
  public JCheckBox
    clickableCheckbox;	
  /** Allow transforms, so that there is no oImg. */
  public JCheckBox
    allowXformsCheckbox;
  /** xform previous oImg Image */
  public JCheckBox
    composeXformCheckbox;
  
  /** GUI text 1 msg line */
  public JLabel
    textMsgLabel1= null;		
  /** GUI text 2 msg line */
  public JLabel
    textMsgLabel2= null;		
  /** max text length that can be put into msg1 or msg2 */
  public int
    maxMsgSize;
  
  /** GUI label for zoom scroll (AWT zoom) */
  public JLabel
    zoomLabel= null;
  /** GUI label for zoom scroll de_zoom transform 
   * over 1/N to NX zoom (-N to +N)) */
  public JLabel
    zoomMagLabel= null;		
  /** GUI label for angle scroll */
  public JLabel
    angleLabel= null;			
  /** GUI label for eScale scroll*/
  public JLabel
    eScaleLabel= null;		
  /** GUI label for zScale scroll */
  public JLabel
    zScaleLabel= null;		
  /** GUI label for contrast scroll */
  public JLabel
    contrastLabel= null;	
  /** GUI label for brightness scroll */
  public JLabel
    brightnessLabel= null;		
  /** GUI label for threshold 2 scroll */
  public JLabel
    threshold2Label= null;
  /** GUI label for threshold 1 scroll */
  public JLabel
    threshold1Label= null;	
  /** GUI label for measCircleRadius scroll */
  public JLabel
    measCircleRadiusLabel= null;
  
  /** GUI label for "Canvas size: xxxx" */
  public JLabel
    canvasSizeLabel= null;		

  /** scroll bar for (AWT display zoom) */
  public Scrollbar
    zoomBar= null;	
  /** scroll bar for zoom (de_zoom transform 
   * over 1/N to NX zoom (-N to +N)) */
  public Scrollbar
    zoomMagBar= null;			
  /** scroll bar for angle */
  public Scrollbar
    angleBar;			
  /** scroll bar for eScale */
  public Scrollbar
    eScaleBar;			
  /** scroll bar for zScale */
  public Scrollbar
    zScaleBar;			
  /** scroll bar for contasrt */
  public Scrollbar
    contrastBar;		
  /** scroll bar for brightness */
  public Scrollbar
    brightnessBar;		
  /** scroll bar for threshold2 */
  public Scrollbar
    threshold2Bar;		
  /** scroll bar for threshold1 */    
  public Scrollbar
    threshold1Bar;		
  /** scroll bar for measCircleRadius */
  public Scrollbar
    measCircleRadiusBar;
  
  
  /**
   * BuildGUI() - constructor for Class BuildGUI
  */
  public BuildGUI(Flicker flk, String title)
  { /* BuildGUI */
    
    this.flk= flk;
    this.title= title;
    this.util= flk.util;
    
    /* Set it up in case need it earlier */
    flk.bGui= this;           
    
    menuActionLabelList= new String[MAX_CMDS]; /* list of menuItem labels */
    menuActionCmdList= new String[MAX_CMDS];   /* list of menuItem commands*/
    chkBoxMenuLabelList= new String[MAX_CHKBOX_CMDS]; 
                                               /* checkboxItem names */
    chkBoxMenuItemList= new CheckboxMenuItem[MAX_CHKBOX_CMDS]; 
                                               /* checkboxItem items */ 
    
    /* items that must have selected image */ 
    menuItemMustSelect= new MenuItem[MAX_CMDS]; 
    nMImustSelect= 0;
  
    nXformMenuCmds= 0;
    xformMenuCmds= new MenuItem[MAX_CMDS]; 
    disableXformMenuCmds= new boolean[MAX_CMDS]; 
    
    /* create the event handler and set int flk state */
    evMu= new EventMenu(flk, this);	
    flk.evMu= evMu;
     
    /* build the GUI */
    buildGUI(flk);
    
    /* Setup the dialog box but make it invisible */
    pdq= new PopupDialogQuery(flk, 2);    
  } /* BuildGUI */
  
  
  /**
   * makeMenuItem() - make menuItem entry in menu list.
   * Setup action command and listener call back.
   * If the command name is null, set the command name to label name.
   * if shortCut is <0, then gray-out the item. i.e. not available and
   * do not add to event handler.
   * @param pm is the menu to install it
   * @param sLabel is the visible label
   * @param sCmd is the opt Cmd name (uses sLabel if null)
   * @param shortcut is the opt short cut
   * @param mustSelectImgFlag to save the menu item in a list that will
   *        be check when an image is selected or deselected for disabling
   *        that menu item.
   * @return the menu item
   */
  private MenuItem makeMenuItem(Menu pm, String sLabel, String sCmd, 
                                int shortcut, boolean mustSelectImgFlag )
  { /*makeMenuItem*/
    MenuItem mi= new MenuItem(sLabel);
    if(sCmd==null)
      sCmd= sLabel;		                   /* use same string for both */
    mi.setActionCommand(sCmd);           /* separate string for CMD & LABEL*/
    
    if(shortcut>=0)
      mi.addActionListener(this);        /* actionListener in this class */
    else
      mi.setEnabled(false);
    
    /* Push sCmd into menuActionNameList[] in case need to look it up */
    boolean ovfFlag= true;
    if(nCmds<MAX_CMDS)
    {
      menuActionLabelList[nCmds]= sLabel; /* Save for event handler */
      menuActionCmdList[nCmds++]= sCmd;   /* Save for event handler */
      ovfFlag= false;
    }
    
    /* keep a list of items that are only enabled when select an image */
    if(mustSelectImgFlag)
      menuItemMustSelect[nMImustSelect++]= mi;
    
    if(shortcut>0)
    {
      MenuShortcut
      msc= new MenuShortcut(shortcut);
      mi.setShortcut(msc);	              /* optional shortcut */
    }
    pm.add(mi);
    
    if(ovfFlag)
      return(null);   /* overflow the MenuItem stack */
    
    return(mi);
  } /* makeMenuItem*/
  
       	  
  /**
   * makeTransformMenuItem() - make transform menuItem entry in menu list.
   * Setup action command and listener call back.
   * If the command name is null, set the command name to label name.
   * if shortCut is <0, then gray-out the item. i.e. not available and
   * do not add to event handler.
   * @param pm is the menu to install it
   * @param sLabel is the visible label
   * @param sCmd is the opt Cmd name (uses sLabel if null)
   * @param shortcut is the opt short cut
   * @param mustSelectImgFlag to save the menu item in a list that will
   *        be check when an image is selected or deselected for disabling
   *        that menu item.
   * @return the menu item
   */
  private MenuItem makeTransformMenuItem(Menu pm, String sLabel, 
                                         String sCmd, int shortcut,
                                         boolean mustSelectImgFlag )
  { /* makeTransformMenuItem */
    MenuItem mi= makeMenuItem(pm, sLabel, sCmd, shortcut, mustSelectImgFlag);
    /* Push Transform menuItems */
    xformMenuCmds[nXformMenuCmds]= mi; 
    disableXformMenuCmds[nXformMenuCmds++]= (shortcut==-1);
    return(mi);
  } /* makeTransformMenuItem */

  
  /**
   * makeSubMenu() - make submenu entry in menu list.
   * Setup action command and listener call back.
   * If the command name is null, set the command name to label name.
   * @param pm is the menu to install it
   * @param sLabel is the visible label
   * @param sCmd is the opt Cmd name (uses sLabel if null)
   * @param shortcut is the opt short cut
   */
  private Menu makeSubMenu(Menu pm, String sLabel, String sCmd, int shortcut)
  { /* makeSubMenu */
    Menu mi= new Menu(sLabel);
    if(shortcut!=0)
    {
      MenuShortcut msc= new MenuShortcut(shortcut);
      mi.setShortcut(msc);	       /* optional shortcut */
    }
    pm.add(mi);
    return(mi);
  } /* makeSubMenu */
  
  
  /**
   * makeChkBoxMenuItem() - make CheckboxMenuItem entry in popup menu list.
   * Setup action command and listener call back.
   * If the command name is null, set the command name to label name.
   * if shortCut is <0, then gray-out the item. i.e. not available and
   * do not add to event handler.
   * @param pm is the menu to install it
   * @param sLabel is the visible label
   * @param sCmd is the opt Cmd name (uses sLabel if null)
   * @param shortcut is the opt short cut
   * @param value is the initial value of the checkbox
   */
  private CheckboxMenuItem makeChkBoxMenuItem(Menu pm, String sLabel, 
                                              String sCmd, int shortcut, 
                                              boolean value )
  { /* makeChkBoxMenuItem */
    CheckboxMenuItem mi= new CheckboxMenuItem(sLabel,value);
    
    mi.setState(value);
    if(sCmd==null)
      sCmd= sLabel;		     /* use same string for both */
    mi.setActionCommand(sCmd);     /* separate string for CMD and  LABEL */
    if(shortcut>=0)
      mi.addItemListener(this);    /* actionListener in this class */
    else
      mi.setEnabled(false);
    
    if(shortcut>0)
    {
      MenuShortcut  msc= new MenuShortcut(shortcut);
      mi.setShortcut(msc);	      /* optional shortcut */
    }
    
    /* Push sCmd and item into chkBoxMenuLabelList[] in case need to
     * look it up.
     */
    if(nCBcmds<MAX_CHKBOX_CMDS)
    { /* Save for event handler */
      chkBoxMenuLabelList[nCBcmds]= sCmd;
      chkBoxMenuItemList[nCBcmds++]= mi;
    }
    pm.add(mi);
    return(mi);
  } /* makeChkBoxMenuItem*/
  
      
  /**
   * createFlkMapMenuTree() - create the FlkMap menu tree from
   * the FlkMap database "DB/FlkMapDB.txt" file
   * @param smnu to attach the tree
   */
  private void createFlkMapMenuTree(Menu smnu)
  { /* createFlkMapMenuTree */
    FlkMap flkMaps[]= FlkMap.flkMaps;
    String smnu2Name[]= new String[FlkMap.MAX_DATABASES];
    Menu 
      useSmnu2= null,
      smnu2[]= new Menu[FlkMap.MAX_DATABASES];
    int maxSubmenus= 0;
    for(int i=0;i<FlkMap.nMaps;i++)
    { /* build the sub menu list */
      String 
        fmDbName= flkMaps[i].databaseName,
        fmMenuName= flkMaps[i].menuName;
      
      boolean addNewSubmenuFlag= true;
      for(int k=0;k<maxSubmenus;k++)
      if(fmDbName.equals(smnu2Name[k]))
      { /* see if submenu exists */
        useSmnu2= smnu2[k];
        addNewSubmenuFlag= false;
        break;
      }
      
      if(addNewSubmenuFlag)
      { /* new database, use it */
        useSmnu2= makeSubMenu(smnu, fmDbName,"FMdbName:"+fmDbName, 0);        
        smnu2Name[maxSubmenus]= fmDbName;
        smnu2[maxSubmenus++]= useSmnu2;
      }
      
      makeMenuItem(useSmnu2,fmMenuName,"FM:"+i,0,true); /* add image name entry*/
    } /* build the sub menu list */    
  } /* createFlkMapMenuTree */
    
  
  /**
   * createFlkDemoMenuTree() - create the FlkDemo menu tree from
   * the FlkDemo database "FlkDemoDB.txt" file
   * @param smnu to attach the tree
   */
  private void createFlkDemoMenuTree(Menu smnu)
  { /* createFlkDemoMenuTree */
    FlkDemo flkDemos[]= FlkDemo.flkDemos;
    String smnu2Name[]= new String[FlkDemo.MAX_DATABASES];
    Menu 
      useSmnu2= null,
      smnu2[]= new Menu[FlkDemo.MAX_DATABASES];
    int maxSubmenus= 0;
    for(int i=0;i<FlkDemo.nMaps;i++)
    { /* build the sub menu list */
      String 
        fmDbName= flkDemos[i].subMenuName,
        fmMenuName= flkDemos[i].subMenuEntry;
      
      boolean addNewSubmenuFlag= true;
      for(int k=0;k<maxSubmenus;k++)
      if(fmDbName.equals(smnu2Name[k]))
      { /* see if submenu exists */
        useSmnu2= smnu2[k];
        addNewSubmenuFlag= false;
        break;
      }
      
      if(addNewSubmenuFlag)
      { /* new database, use it */
        useSmnu2= makeSubMenu(smnu, fmDbName,"FDemoDbName:"+fmDbName, 0);        
        smnu2Name[maxSubmenus]= fmDbName;
        smnu2[maxSubmenus++]= useSmnu2;
      }      
      makeMenuItem(useSmnu2,fmMenuName,"FD:"+i,0,false); /* add image name entry*/
    } /* build the sub menu list */    
  } /* createFlkDemoMenuTree */
    
  
  /**
   * createFlkUserPairsMenuTree() - create the paired FlkUser menu tree
   * from analyzing the Images/* directories and setup the database of
   * pairs of user directory images
   * @param smnu to attach the tree
   * @return true if succeed, false if any problems 
   */
  private boolean createFlkUserPairsMenuTree(Menu smnu)
  { /* createFlkUserPairsMenuTree */
    FlkUser flkUsers[]= FlkUser.flkUsers;
    String smnu2Name[]= new String[FlkUser.MAX_DATABASES];
    Menu 
      useSmnu2= null,
      smnu2[]= new Menu[FlkUser.MAX_DATABASES];    
    MenuItem mi;
    
    if(FlkUser.nMaps==0)
      return(true);
    
    int maxSubmenus= 0;
    for(int i=0;i<FlkUser.nMaps;i++)
    { /* build the sub menu list */
      String 
        fmDbName= flkUsers[i].subMenuName,
        fmMenuName= flkUsers[i].subMenuEntry,
        imageURL2= flkUsers[i].imageURL2;
      if(imageURL2==null)
        continue;   /* ignore pairs entry if 1 image/directory */
      
      boolean addNewSubmenuFlag= true;
      for(int k=0;k<maxSubmenus;k++)
      if(fmDbName.equals(smnu2Name[k]))
      { /* see if submenu exists */
        useSmnu2= smnu2[k];
        addNewSubmenuFlag= false;
        break;
      }
      
      if(addNewSubmenuFlag)
      { /* new database, use it */
        useSmnu2= makeSubMenu(smnu, fmDbName,"FUserPairDbName:"+fmDbName,0);        
        smnu2Name[maxSubmenus]= fmDbName;
        smnu2[maxSubmenus++]= useSmnu2;
      }
      
      /* add image name entry*/
      mi= makeMenuItem(useSmnu2,fmMenuName,"FUP:"+i,0,false); 
      if(mi== null)
        return(false);
    } /* build the sub menu list */ 
    
    return(true);
  } /* createFlkUserPairsMenuTree */
  
        
  /**
   * createFlkUserSingleMenuTree() - create the single image
   * FlkUser menu tree from analyzing the Images/* directories and 
   * setup the database of user directory single images
   * @param smnu to attach the tree
   * @return true if succeed, false if any problems 
   */
  private boolean createFlkUserSingleMenuTree(Menu smnu)
  { /* createFlkUserSingleMenuTree */    
    String
      sCmd,
      allImgFileList[][]= FlkUser.allImgFileList,
      fmDbName,
      fmMenuName;
    MenuItem mi;
    Menu useSmnu2= null;
    
    if(FlkUser.nDirs==0)
      return(true);
    
    for(int i=0;i<FlkUser.nDirs;i++)
    { /* build the sub menu list */
      String imageList[]= allImgFileList[i];
      if(imageList==null)
        continue;
      
      fmDbName= FlkUser.dirList[i]; 
      /* new database,make sub menu */
      useSmnu2= makeSubMenu(smnu, fmDbName,"FUserSingleDbName:"+fmDbName,0);        
     
      int nImagesInIthDir= imageList.length;       
      for(int j= 0;j<nImagesInIthDir;j++)
      {
        fmMenuName= imageList[j];
        /* add image name entry*/
        sCmd= ("FUS:"+i+","+j);
        mi= makeMenuItem(useSmnu2,fmMenuName,sCmd,0,true);
        if(mi==null)
          return(false);
      }
    } /* build the sub menu list */  
    
    return(true);
  } /* createFlkUserSingleMenuTree */
       
  
  /**
   * createFlkRecentMenuTree() - create the FlkRecent menu tree from
   * the FlkRecent database "FlkRecentDB.txt" file
   * @param smnu to attach the tree
   * @return true if succeed, false if any problems 
   */
  public boolean createFlkRecentMenuTree(Menu smnu)
  { /* createFlkRecentMenuTree */
    FlkRecent
      flkRecents[]= FlkRecent.flkRecents,
      flkRecentsSorted[]= new FlkRecent[FlkRecent.nMaps];
    String timeStampsSorted[]=  new String[FlkRecent.nMaps];
        
    smnu.removeAll();
    
    /* Sort list by timeStamp in descending order */
    if(FlkRecent.nMaps==0)
      return(true);
    for(int i=0;i<FlkRecent.nMaps;i++)
      timeStampsSorted[i]= flkRecents[i].timeStamp;
    int sortIdx[]= Util.bubbleSortIndex(timeStampsSorted, FlkRecent.nMaps,
                                        false /* ascending */);
    for(int i=0;i<FlkRecent.nMaps;i++)
      flkRecentsSorted[i]= flkRecents[sortIdx[i]];
    
    /* Build the menu(s) */   
    MenuItem mi;
    for(int i=0;i<FlkRecent.nMaps;i++)
    { /* build the sub menu list */
      String 
        fmDbName= flkRecentsSorted[i].databaseName,
        fmMenuName= flkRecentsSorted[i].dbMenuName,
        fmTimeStamp= Util.prettyPrintDateStr(flkRecentsSorted[i].timeStamp),
        entryName= "[" + fmDbName +"] "+ fmMenuName +
                   "  <"+ fmTimeStamp + ">";
      /* add image name entry */
      mi=  makeMenuItem(smnu, entryName, "FR:"+sortIdx[i],0, false); 
      if(mi==null)
        return(false);
    } /* build the sub menu list */    
    
    return(true);
  } /* createFlkRecentMenuTree */
 
   
  /**
   * syncGuiWithState() - sync GUI checkboxes and menu checkboxes
   * with the state which may have changed.
   */
  public void syncGuiWithState()
  { /* setGuiEnable */    
    flickerCheckbox.setSelected(flk.flickerFlag);
    allowXformsCheckbox.setSelected(flk.allowXformFlag);
    composeXformCheckbox.setSelected(flk.composeXformFlag);
    
    /* Set menu checkbox items */
    mi_useLogTIFFfilesCB.setState(flk.useLogInputFlag);
    
    mi_flickerCB.setState(flk.flickerFlag);
    util.setFlickerGUI(flk.flickerFlag);
    
    mi_ViewLmsCB.setState(flk.viewLMSflag);
    mi_ViewTargetCB.setState(flk.useLogInputFlag);
    mi_ViewTrialObjCB.setState(flk.viewTrialObjFlag);
    mi_ViewBoundaryCB.setState(flk.viewBoundaryFlag);
    mi_ViewRoiCB.setState(flk.viewRoiFlag);
    mi_ViewMeasCircleCB.setState(flk.viewMeasCircleFlag);
    
    mi_ViewMeasSpotLocAsCircleCB.setState(flk.viewDrawSpotLocCircleFlag);
    mi_ViewMeasSpotLocAsPlusCB.setState(flk.viewDrawSpotLocPlusFlag);
    mi_ViewAnnSpotNbrCB.setState(flk.viewDrawSpotAnnNbrFlag);
    mi_ViewAnnSpotIdCB.setState(flk.viewDrawSpotAnnIdFlag);
    
    mi_MultPopupsCB.setState(flk.viewMultPopups);
    mi_GangScrollImgsCB.setState(flk.viewGangScrollFlag);
    mi_GangZoomImgsCB.setState(flk.viewGangZoomFlag);
    if(flk.USE_GANG_BC)
      mi_GangBCImgsCB.setState(flk.viewGangBCFlag);
    if(flk.USE_GUARD)
      mi_GangZoomImgsCB.setState(flk.useGuardRegionImageFlag);
    
    mi_showReportPopupCB.setState(flk.viewReportPopupFlag);
    if(flk.DBUG_NTSC_RGB2GRAY)
      mi_useNTSCrgbTograyCvtCB.setState(flk.useNTSCrgbTograyCvtFlag);
    
    mi_autoMeasProtLookupPopupCB.setState(flk.doMeasureProtIDlookupAndPopupFlag);
    mi_useSwiss2DpageServerCB.setState(flk.useSwiss2DpageServerFlag);
    mi_usePIRUniprotServerCB.setState(flk.usePIRUniprotServerFlag);
    mi_usePIRiProClassServerCB.setState(flk.usePIRiProClassServerFlag);
    mi_usePIRiProLinkServerCB.setState(flk.usePIRiProLinkFlag);
    
    mi_thresholdInsideCB.setState(flk.useThresholdInsideFlag);
    mi_saveOimagesWhenSaveStateCB.setState(flk.saveOimagesWhenSaveStateflag);
    mi_useProteinDBbrowserCB.setState(flk.useProteinDBbrowserFlag);
      
    if(mi_dbugCB!=null)
      mi_dbugCB.setState(flk.dbugFlag);
    mi_QuantTotDensityCB.setState(flk.useTotDensityFlag);
    mi_QuantSpotListModeCB.setState(flk.spotsListModeFlag);
    
    ImageDataMeas.setCircleMaskRadius(flk.measCircleRadius,null);
    evMu.setWindmpRadixCheckbox(flk.winDumpRadix);
  } /* syncGuiWithState */
 
  
  /**
   * setGuiEnable() enable/disable GUI operations
   */
  public void setGuiEnable(boolean flag)
  { /* setGuiEnable */
    flk.flkIS.setEnabled(flag);
    flk.i1IS.setEnabled(flag);
    flk.i2IS.setEnabled(flag);    
    
    fileMenu.setEnabled(flag);
    editMenu.setEnabled(flag);
    viewMenu.setEnabled(flag);
    lmsMenu.setEnabled(flag);
    xformMenu.setEnabled(flag);
    quantMenu.setEnabled(flag);
    if(pluginMenu!=null)
      pluginMenu.setEnabled(flag);
    helpMenu.setEnabled(flag);    
    flickerCheckbox.setEnabled(flag);
    
    /* Enable clickable DB checkbox if image is selected
     * and it is clickable capable.
     */
    flk.chkIfClickableDB(false);
  } /* setGuiEnable */
   
  
  /**
   * setMustSelectImageMenuItemsEnable() - enable/disable GUI menu items that must
   * have an image selected to be enabled
   * @param flag to enable/disble the GUI
   */
  public void setMustSelectImageMenuItemsEnable(boolean flag)
  { /* setMustSelectImageMenuItemsEnable */
    /* keep a list of items that are only enabled when select an image */
    for(int i=0;i<nMImustSelect;i++)
      menuItemMustSelect[i].setEnabled(flag);
  } /* setMustSelectImageMenuItemsEnable */
  
  
  /**
   * setTransformsEnable() enable/disable GUI Transforms menu operations
   * @param flag to enable/disble the GUI
   */
  public void setTransformsEnable(boolean flag)
  { /* setTransformsEnable */
    for(int i=0;i<nXformMenuCmds;i++)
      if(xformMenuCmds[i]!=null && !disableXformMenuCmds[i])
        xformMenuCmds[i].setEnabled(flag);
  } /* setTransformsEnable */  
 
  
  /**
   * buildGUI() - Build the GUI consisting of pannels and image areas
   * <PRE>
   *     -----------------------------
   *     |  Menu buttons - menu bar  |
   *     -----------------------------
   *     |  <free>  - NORTH          |
   *     -----------------------------
   *     |  Checkboxes - WEST        |
   *     -----------------------------
   *     |  Scroll bars - EAST       |
   *     -----------------------------
   *     |  Flicker window - CENTER  |
   *     -----------------------------
   *     |  I1 + I2 images - SOUTH   |
   *     -----------------------------
   * </PRE>
   * @param f is the frame to put the GUI in
   */
  public void buildGUI(JFrame f)
  { /* buildGUI */    
    /* [1] Create new frame tree */
    JPanel pMain= new JPanel();       /* top level BorderLayout panel */   
    Border 
      //blackline = BorderFactory.createLineBorder(Color.black),
      //raisedbevel = BorderFactory.createRaisedBevelBorder(),
      //raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
      loweredbevel = BorderFactory.createLoweredBevelBorder(); 
    
    JPanel chkboxPanel= new JPanel(); /* holds the checkboxes WEST */
    JPanel pScroll= new JPanel();     /* holds the scroll boxes EAST */
    JPanel pFlicker= new JPanel();    /* holds the Flicker image CENTER */
    pImages= new JPanel();            /* MUST BE GLOBAL holds image 1 and
                                       * image 2 SOUTH */  
    
    // chkboxPanel.setBorder(raisedetched);
    // pScroll.setBorder(raisedetched);
    // pFlicker.setBorder(blackline);
    pMain.setBorder(loweredbevel); /* give some space around the edges */   
        
    mbar= new MenuBar();
    this.setMenuBar(mbar);          /* activate menu bar even if menus 
                                     * are not active*/    
    Font mbarFont= new Font("Helvetica", Font.PLAIN, 12);
    mbar.setFont(mbarFont);         /* make it a larger font */    
    
    /* [1.1] Create layout */
    pMain.setLayout(new BorderLayout());        
    f.getContentPane().add(pMain);  /* swing version to add to frame */       
    
    /* Set backgrounds to white, could use color from properties file/menu */
    f.setBackground(Color.white);
    pMain.setBackground(Color.white);
    chkboxPanel.setBackground(Color.white);
    pScroll.setBackground(Color.white);
    pFlicker.setBackground(Color.white);
    pImages.setBackground(Color.white);
    f.setBackground(Color.white);      
    
    /* 0 means use as many rows as needed (as with .add());
     * 1, is 1 col fixed.
     */
    chkboxPanel.setLayout(new GridLayout(0,1));
    pMain.add("West", chkboxPanel);
    
    int nSliders= 9;
    if(flk.USE_DE_ZOOM)
      nSliders++;
    pScroll.setLayout(new GridLayout(nSliders,2)); 
    
    pMain.add("East", pScroll);
    
    pFlicker.setLayout(new FlowLayout(FlowLayout.CENTER));
    pMain.add("Center", pFlicker);
    
    pImages.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
    pMain.add("South", pImages);    
    
    /* [2] Create pull down menus in menu bar */
    addMenuTree(f, mbar);    
    
    /* [3] Create threshold scrollers and put on right of frame */
    addImageParameterStateScrollers(pScroll);
    
    /* [4] Create Checkboxes in panel on the upper left. */
    addCheckboxes(chkboxPanel);    
    
    /* [5] Add message status line */
    String s= "                                                       ";    	
    /* max text length that can be put into msg1 or msg2 */
    maxMsgSize= s.length();
    
    textMsgLabel1= new JLabel(s,Label.LEFT);  
    
    textMsgLabel1.setBackground(new Color(220,210,200));
    textMsgLabel1.setFont(flk.textFont);
    chkboxPanel.add(textMsgLabel1);   
    
    textMsgLabel2= new JLabel(s,Label.LEFT);
    textMsgLabel2.setBackground(new Color(220,210,200));
    textMsgLabel2.setFont(flk.textFont);
    chkboxPanel.add(textMsgLabel2);   
    
    /* [6] Create "State messages" in labels */    
    flk.info= new Info(flk, chkboxPanel);       
   
    /* [7] Add image scrollers: flicker to pFlicker, and image 1 
     * and Image 2 scrollable images to pImages panels in the frame.
     */
    addImageScrollers(pFlicker, pImages); 
    
    /* [8] Disable GUI operations until images are completely read in */
    setGuiEnable(false);    
  } /* buildGUI */
  
  
  /**
   * addMenuTree() - create pull down menus in menu bar
   * @param f is frame
   * @param mbar is MenuBar
   */
  private void addMenuTree(Frame f, MenuBar mbar)
  { /* addMenuTree */
    Menu
      smnu= null,
      smnu2= null;
    
    /* [1] Create menus to put in the tree */
    fileMenu= new Menu("File");
    editMenu= new Menu("Edit");
    viewMenu= new Menu("View");
    lmsMenu= new Menu("Landmark");
    xformMenu= new Menu("Transform");
    quantMenu= new Menu("Quantify");
    if(flk.piMgr!=null)
      pluginMenu= new Menu("Plugins");
    helpMenu= new Menu("Help");
    
    /* [1.1] add submenus to menu bar frame */
    mbar.add(fileMenu);
    mbar.add(editMenu);
    mbar.add(viewMenu);
    mbar.add(lmsMenu);
    mbar.add(xformMenu);
    mbar.add(quantMenu);
    if(pluginMenu!=null)
      mbar.add(pluginMenu);
    mbar.add(helpMenu);
    
    /* [2] add mbar to the frame */
    f.setMenuBar(mbar);
    
    /* [2.1] Add "File" pulldown menu */
    makeMenuItem(fileMenu, "Open image file", "OpenFile", 0, true);
    makeMenuItem(fileMenu, "Open image URL", "OpenURL", 0, true);
    
    /* Add a set of submenues in
     * (File | Open demo images | <various demos>)
     */  
    smnu= makeSubMenu(fileMenu, "Open demo images", "OpenDemos",0);
    /* Create the FlkDemo menu tree from the FlkDemoDB.txt database */
    fDemoMenuStub= smnu;              /* save in case rebuild it */
    createFlkDemoMenuTree(smnu);
    
    /* Add a set of submenues in
     * (File | Open user images | <various demos>)
     */  
    smnu= makeSubMenu(fileMenu, "Open user images", "OpenUerImgs",0);
    /* Create the FlkUser menu tree from user's images in
     * subdirectories in the in Images/ directory
     */
    fUserMenuStub= smnu;              /* save in case rebuild it */
    /* Add paired user images menu trees */
    sUserPairTreeMnu= makeSubMenu(smnu, "Pairs of images", "OpenUsrImgPairs",0);
    
    /* Add directories for single user images tree */
    sUserSingleTreeMnu= makeSubMenu(smnu, "Single images", "OpenUsrImgSingle",0);
    
    if(FlkUser.nPairs>0)
    {
      smnu.addSeparator();      /* "__________" */
      makeMenuItem(smnu, "List user's images by directory", 
                   "ListUserImageDirs", 0, false);
    }
    
    //makeMenuItem(fileMenu, "DEBUG TiffLoader", "TestTiffLoader", 
    //             flk.NOT_AVAIL_YET);    
    
    /* Add a set of submenues in
     * (File | Open active Map Image | <databaseName> | <image name>)
     */  
    smnu= makeSubMenu(fileMenu, "Open active map image", "openActiveMap",0);
    /* Create the FlkMap menu tree from the FlkMapDB.txt database */
    fMapMenuStub= smnu;              /* save in case rebuild it */
    createFlkMapMenuTree(smnu);
    
    /* Add a set of submenues in
     * (File | Open recent images | <databaseName> | <image name>)
     */  
    smnu= makeSubMenu(fileMenu, "Open recent images", "openRecentImages",0);
    /* Create the FlkRecent menu tree from the FlkDemoDB.txt database */
    fRecentMenuStub= smnu;              /* save in case rebuild it */
    createFlkRecentMenuTree(smnu); 
    makeMenuItem(fileMenu, "Assign active URL to image", 
                 "AssignActiveMapURL", 0, true); 
        
    fileMenu.addSeparator();      /* "__________" */
    makeMenuItem(fileMenu, "Open state file", "OpenStateFile",  0, false);
    makeMenuItem(fileMenu, "Save state file", "SaveStateFile",0, false);
    makeMenuItem(fileMenu, "SaveAs state file", "SaveAsStateFile",0, false);
    
    fileMenu.addSeparator();      /* "__________" */
    smnu= makeSubMenu(fileMenu, "Update", "Update", 0);
    makeMenuItem(smnu, "Flicker Program", "updateFlickerJar", 0, false); 
    makeMenuItem(smnu, "Active Web maps image DB", "updateWebMaps", 0, false); 
    makeMenuItem(smnu, "Demo Images DB", "updateDemos", 0, false); 
    makeMenuItem(smnu, "Add user's Flicker Demo Images DB by URL",
                       "addingUserDemoDbURL", 0, false); 
    
    fileMenu.addSeparator();      /* "__________" */
    makeMenuItem(fileMenu, "Save transformed image", "SaveXform_oImg", 0, true);
    makeMenuItem(fileMenu, "SaveAs overlay image", "SaveOverlayImg", 0, true);
    makeMenuItem(fileMenu, "Reset Images", "ResetImages", 0, false);
    makeMenuItem(fileMenu, "Abort Transform", "AbortXform", 0, false);
    
    fileMenu.addSeparator();    	/* "__________" */
    makeMenuItem(fileMenu, "Quit", "Quit", 0, false);
    
    /* [2.2] Add "Edit" pulldown menu */  
    smnu= makeSubMenu(editMenu, "Canvas size", "canvasSize", 0);   
    makeMenuItem(smnu,"Increase size (-)", "CanvasSize:+", 
                 KeyEvent.VK_ADD, false);  
    makeMenuItem(smnu,"Decrease size (+)", "CanvasSize:-", 
                 KeyEvent.VK_SUBTRACT, false); 
    
    /* [TODO] use checkboxes for colors so user can see selected color
     * directly.
     */
    smnu= makeSubMenu(editMenu, "Set colors", "TargetColor", 0);    
    smnu2= makeSubMenu(smnu, "Target color", "SetTargetColor", 0);
    makeMenuItem(smnu2, "Red", "TargetColor-Red",0, false);
    makeMenuItem(smnu2, "Orange", "TargetColor-Orange", 0, false);
    makeMenuItem(smnu2, "Yellow", "TargetColor-Yellow",0, false);
    makeMenuItem(smnu2, "Green", "TargetColor-Green", 0, false);
    makeMenuItem(smnu2, "Blue", "TargetColor-Blue", 0, false);
    makeMenuItem(smnu2, "Cyan", "TargetColor-Cyan", 0, false);
    makeMenuItem(smnu2, "Magenta", "TargetColor-Magenta", 0, false);
    makeMenuItem(smnu2, "Black", "TargetColor-Black", 0, false);
    makeMenuItem(smnu2, "Gray", "TargetColor-Gray", 0, false);
    makeMenuItem(smnu2, "White", "TargetColor-White", 0, false);
        
    smnu2= makeSubMenu(smnu, "Trial object color", "TrialObjColor", 0);
    makeMenuItem(smnu2, "Red", "TrialObjColor-Red", 0, false);
    makeMenuItem(smnu2, "Orange", "TrialObjColor-Orange", 0, false);
    makeMenuItem(smnu2, "Yellow", "TrialObjColor-Yellow", 0, false);
    makeMenuItem(smnu2, "Green", "TrialObjColor-Green", 0, false);
    makeMenuItem(smnu2, "Blue", "TrialObjColor-Blue", 0, false);
    makeMenuItem(smnu2, "Cyan", "TrialObjColor-Cyan", 0, false);
    makeMenuItem(smnu2, "Magenta", "TrialObjColor-Magenta", 0, false);
    makeMenuItem(smnu2, "Black", "TrialObjColor-Black", 0, false);
    makeMenuItem(smnu2, "Gray", "TrialObjColor-Gray", 0, false);
    makeMenuItem(smnu2, "White", "TrialObjColor-White", 0, false);
       
    smnu2= makeSubMenu(smnu, "Landmarks color", "LandmarksColor", 0);
    makeMenuItem(smnu2, "Red", "lmsColor-Red", 0, false);
    makeMenuItem(smnu2, "Orange", "lmsColor-Orange", 0, false);
    makeMenuItem(smnu2, "Yellow", "lmsColor-Yellow", 0, false);
    makeMenuItem(smnu2, "Green", "lmsColor-Green", 0, false);
    makeMenuItem(smnu2, "Blue", "lmsColor-Blue", 0, false);
    makeMenuItem(smnu2, "Cyan", "lmsColor-Cyan", 0, false);
    makeMenuItem(smnu2, "Magenta", "lmsColor-Magenta", 0, false);
    makeMenuItem(smnu2, "Black", "lmsColor-Black", 0, false);
    makeMenuItem(smnu2, "Gray", "lmsColor-Gray", 0, false);
    makeMenuItem(smnu2, "White", "lmsColor-White", 0, false);
    
    smnu2= makeSubMenu(smnu, "Measurement color", "MeasurementColor", 0);
    makeMenuItem(smnu2, "Red", "measColor-Red", 0, false);
    makeMenuItem(smnu2, "Orange", "measColor-Orange", 0, false);
    makeMenuItem(smnu2, "Yellow", "measColor-Yellow", 0, false);
    makeMenuItem(smnu2, "Green", "measColor-Green", 0, false);
    makeMenuItem(smnu2, "Blue", "measColor-Blue", 0, false);
    makeMenuItem(smnu2, "Cyan", "measColor-Cyan", 0, false);
    makeMenuItem(smnu2, "Magenta", "measColor-Magenta", 0, false);
    makeMenuItem(smnu2, "Black", "measColor-Black", 0, false);
    makeMenuItem(smnu2, "Gray", "measColor-Gray", 0, false);
    makeMenuItem(smnu2, "White", "measColor-White", 0, false);    
    
    smnu2= makeSubMenu(smnu, "Guard Region color", "GuardRegionColor", 0);
    makeMenuItem(smnu2, "Red", "guardColor-Red", 0, false);
    makeMenuItem(smnu2, "Orange", "guardColor-Orange", 0, false);
    makeMenuItem(smnu2, "Yellow", "guardColor-Yellow", 0, false);
    makeMenuItem(smnu2, "Green", "guardColor-Green", 0, false);
    makeMenuItem(smnu2, "Blue", "guardColor-Blue", 0, false);
    makeMenuItem(smnu2, "Cyan", "guardColor-Cyan", 0, false);
    makeMenuItem(smnu2, "Magenta", "guardColor-Magenta", 0, false);
    makeMenuItem(smnu2, "Black", "guardColor-Black", 0, false);
    makeMenuItem(smnu2, "Gray", "guardColor-Gray", 0, false);
    makeMenuItem(smnu2, "White", "guardColor-White", 0, false);
       
    editMenu.addSeparator();
    /* [DEPRICATED
    makeMenuItem(editMenu,
                 "Resize Flicker memory limits",
                 "ResizeMem",-1, false);    // to change the lax size
   */
    
    mi_useLogTIFFfilesCB= makeChkBoxMenuItem(editMenu,
                                "Use linear else log of TIFF files > 8-bits",
                                             "useLogTiff",0, 
                                             !flk.useLogInputFlag);
    mi_saveOimagesWhenSaveStateCB= makeChkBoxMenuItem(editMenu,
               "Enable saving transformed images when do a 'Save(As) state'",
                                                      "useSaveAsOImages",0, 
                                            flk.saveOimagesWhenSaveStateflag);
    
    mi_useProteinDBbrowserCB= makeChkBoxMenuItem(editMenu,
          "Use protein DB browser, else lookup ID and name on active images",
                                             "useProteinDBbrowser",0, 
                                             flk.useProteinDBbrowserFlag);
    
    editMenu.addSeparator();
    
    mi_autoMeasProtLookupPopupCB= makeChkBoxMenuItem(editMenu,
          "Auto measure, protein lookup in active server and Web page popup",
                                               "AutoMeasProtLookupPopup", 0,
                                      flk.doMeasureProtIDlookupAndPopupFlag);
    
    smnu= makeSubMenu(editMenu, "Select access to active DB server", 
                      "accessServer", 0);
    mi_useSwiss2DpageServerCB= makeChkBoxMenuItem(smnu,
                                               "Use SWISS-2DPAGE DB access",
                                               "UseSWISS-2DPAGE", 0,
                                               flk.useSwiss2DpageServerFlag); 
    mi_usePIRUniprotServerCB= makeChkBoxMenuItem(smnu,
                                               "Use PIR UniProt DB access",
                                               "UsePIR_UNIPROT", 0,
                                               flk.usePIRUniprotServerFlag); 
    mi_usePIRiProClassServerCB= makeChkBoxMenuItem(smnu,
                                               "Use PIR iProClass DB access",
                                               "UsePIR_IPROCLASS", 0,
                                               flk.usePIRiProClassServerFlag); 
    mi_usePIRiProLinkServerCB= makeChkBoxMenuItem(smnu,
                                                "Use PIR iProLINK DB access",
                                                "UsePIR_IPROLINK", 0,
                                                flk.usePIRiProLinkFlag); 
    
    editMenu.addSeparator();
    
    makeMenuItem(editMenu,"Reset default view", "ResetDefaultView",0, false);
    makeMenuItem(editMenu, "Clear all 'Recent' image entries", 
                 "FlkRecent:Clear", 0, false);
     
    if(Flicker.NEVER)
      mi_dbugCB= makeChkBoxMenuItem(editMenu, "Debug", "Debug", 0,
                                    flk.dbugFlag);
    
    
    /* [2.3] Add "View" pulldown menu */
    mi_flickerCB= makeChkBoxMenuItem(viewMenu,"Flicker images (C-F)",
                                     "flickerImages",KeyEvent.VK_F,
                                     flk.flickerFlag);
    
    /* ------------ */    
    smnu= makeSubMenu(viewMenu, "Set view overlay options", "viewOverlays", 0);
    mi_ViewLmsCB= makeChkBoxMenuItem(smnu,"View landmarks","ViewLMS",0,
                                     flk.viewLMSflag);
    mi_ViewTargetCB= makeChkBoxMenuItem(smnu,"View target",
                                        "ViewTarget", 0,
                                        flk.viewTargetFlag);
    mi_ViewTrialObjCB= makeChkBoxMenuItem(smnu,"View trial object",
                                          "ViewTrialObj", 0,
                                          flk.viewTrialObjFlag);
    mi_ViewBoundaryCB= makeChkBoxMenuItem(smnu,"View boundary",
                                          "ViewBnd", flk.NOT_AVAIL_YET,
                                          flk.viewBoundaryFlag);
    mi_ViewRoiCB= makeChkBoxMenuItem(smnu,"View Region Of Interest (ROI)",
                                          "ViewROI", 0, flk.viewRoiFlag);
    
    /* ------------ */
    smnu= makeSubMenu(viewMenu, "Set view measurement options", "viewMeas", 0);
    mi_ViewMeasCircleCB= makeChkBoxMenuItem(smnu,"View measurement circle",
                                            "ViewMeasCir", 0, 
                                            flk.viewMeasCircleFlag);
    
    mi_ViewMeasSpotLocAsCircleCB= makeChkBoxMenuItem(smnu,
                                   "Use 'Circle' for measured spot locations",
                                                     "SpotLocCircle",0,
                                               flk.viewDrawSpotLocCircleFlag);
    mi_ViewMeasSpotLocAsPlusCB= makeChkBoxMenuItem(smnu,
                                        "Use '+' for measured spot locations",
                                                   "SpotLocPlus",0,
                                               flk.viewDrawSpotLocPlusFlag);
    
    mi_ViewAnnSpotNbrCB= makeChkBoxMenuItem(smnu,
                                 "Use 'spot number' for spot annotations",
                                            "SpotAnnCircle",0,
                                            flk.viewDrawSpotAnnNbrFlag);
    mi_ViewAnnSpotIdCB= makeChkBoxMenuItem(smnu,
                                 "Use 'spot identifier' for spot annotations",
                                           "SpotAnnPlus",0,
                                           flk.viewDrawSpotAnnIdFlag);
        
    smnu= makeSubMenu(viewMenu, "Set gang options", "GangOpts", 0);
    mi_MultPopupsCB= makeChkBoxMenuItem(smnu,"Multiple Popups",
                                        "MultPopups", 0, flk.viewMultPopups);
    mi_GangScrollImgsCB= makeChkBoxMenuItem(smnu,"Gang scroll images",
                                            "GangScrImgs", 0,
                                            flk.viewGangScrollFlag);
    mi_GangZoomImgsCB= makeChkBoxMenuItem(smnu,"Gang zoom images",
                                          "GangZoomImgs", 0,
                                          flk.viewGangZoomFlag);
    if(flk.USE_GANG_BC)
      mi_GangBCImgsCB= makeChkBoxMenuItem(smnu,
                                          "Gang brightness/contrast images",
                                          "GangBCImgs", 0,
                                          flk.viewGangBCFlag);          

    mi_useGuardImgsCB= makeChkBoxMenuItem(viewMenu,
                                          "Add guard region to edges of images", 
                                          "useGuardRegion", 0,
                                          flk.useGuardRegionImageFlag);

    
    mi_DispGrayValsCB= makeChkBoxMenuItem(viewMenu,
                                  "Display gray values in image title (C-G)",
                                          "DispGrayVals", KeyEvent.VK_G,
                                          flk.viewDispGrayValuesFlag);
    mi_showReportPopupCB= makeChkBoxMenuItem(viewMenu, "Show report popup",
                                             "ShowReportPopup", 0,
                                             flk.viewReportPopupFlag); 
    if(flk.DBUG_NTSC_RGB2GRAY)
      mi_useNTSCrgbTograyCvtCB= makeChkBoxMenuItem(viewMenu,
                     "Apply NTSC RGB to grayscale color transform if needed",
                                                  "NTSCrgb2gray", 0,
                                                 flk.useNTSCrgbTograyCvtFlag);
    
    
    /* [2.4] Add "Landmark" pulldown menu */
    makeMenuItem(lmsMenu,"Add landmark (C-A)", "AddLM", KeyEvent.VK_A, false);
    makeMenuItem(lmsMenu,"Delete landmark (C-D)", "DelLM", KeyEvent.VK_D, false);
    makeMenuItem(lmsMenu,"Show landmarks similarity", "LMsim", 0, false);
    makeMenuItem(lmsMenu, "Set 3 pre-defined landmarks for demo gels (C-Y)", 
                 "Set3LandmarksDemoGels", KeyEvent.VK_Y, false);
    makeMenuItem(lmsMenu, "Set 6 pre-defined landmarks for demo gels (C-Z)", 
                 "Set6LandmarksDemoGels", KeyEvent.VK_Z, false);    
    
    /* [2.5] Add "Transform" pulldown menu */
    ImageXform ix= flk.ix;
    makeTransformMenuItem(xformMenu,"Affine Warp",ix.xfCmd[ix.AFFINEWARP],
                          0, true);
    makeTransformMenuItem(xformMenu,"Polynomial Warp", ix.xfCmd[ix.POLYWARP],
                          flk.NOT_AVAIL_YET, true);
    makeTransformMenuItem(xformMenu,"Pseudo 3D transform",
                         ix.xfCmd[ix.PSEUDO3D],0, true);
    xformMenu.addSeparator();	/* "__________" */
    makeTransformMenuItem(xformMenu,"Sharpen Gradient",
                         ix.xfCmd[ix.SHARPENGRAD],0, false);
    makeTransformMenuItem(xformMenu,"Sharpen Laplacian",
                          ix.xfCmd[ix.SHARPENLAPL],0, false);
    makeTransformMenuItem(xformMenu,"Gradient", ix.xfCmd[ix.GRAD], 0, false);
    makeTransformMenuItem(xformMenu,"Laplacian", ix.xfCmd[ix.LAPLACIAN], 0, false);
    makeTransformMenuItem(xformMenu,"Average", ix.xfCmd[ix.AVG8], 0, false);
    makeTransformMenuItem(xformMenu,"Median", ix.xfCmd[ix.MEDIAN], 0, false);
    makeTransformMenuItem(xformMenu,"Max 3x3", ix.xfCmd[ix.MAX8], 0, false);
    makeTransformMenuItem(xformMenu,"Min 3x3", ix.xfCmd[ix.MIN8], 0, false);
    xformMenu.addSeparator();	/* "__________" */
    makeTransformMenuItem(xformMenu,"Complement",
                          ix.xfCmd[ix.COMPLEMENT], 0, false);
    makeTransformMenuItem(xformMenu,"Threshold", 
                          ix.xfCmd[ix.THRESHOLD], 0, false);
    makeTransformMenuItem(xformMenu,"Contrast Enhance",
                          ix.xfCmd[ix.CONTR_ENHANCE], 0, false);
    makeTransformMenuItem(xformMenu,"Histogram Equalize", 
                          ix.xfCmd[ix.HIST_EQUAL], 0, false);
    xformMenu.addSeparator();	/* "__________" */
    makeTransformMenuItem(xformMenu,"Original Color",
                          ix.xfCmd[ix.NORMCOLOR],0, false);
    makeTransformMenuItem(xformMenu,"Pseudocolor",
                          ix.xfCmd[ix.PSEUDOCOLOR],0, false);
    makeTransformMenuItem(xformMenu,"Color to grayscale",
                          ix.xfCmd[ix.COLOR2GRAY],0, false);
    xformMenu.addSeparator();	/* "__________" */
    makeTransformMenuItem(xformMenu,"Flip Image Horizontally", 
                          ix.xfCmd[ix.FLIPHORIZ], 0, false);
    makeTransformMenuItem(xformMenu,"Flip Image Vertically",
                          ix.xfCmd[ix.FLIPVERT], 0, false);
    
    xformMenu.addSeparator();	/* "__________" */
    makeTransformMenuItem(xformMenu,"Repeat last transform",
                          "Xform:repeatLast", KeyEvent.VK_T, false);
    
    mi_thresholdInsideCB= makeChkBoxMenuItem(xformMenu, 
                                      "Use threshold inside filter [T1:T2]",
                                             "UseT1T2insideRange", 0,
                                             flk.useThresholdInsideFlag);    
    
    /* [2.6] Add "Quantify" pulldown menu */
    smnu= makeSubMenu(quantMenu, "Measure by circle", "Q:MeasCircle", 0);
    makeMenuItem(smnu,"Capture background", "CircleBkgrd",KeyEvent.VK_B, true);
    makeMenuItem(smnu,"Capture measurement to spot list", "CircleMeas",
                 KeyEvent.VK_M, true);
    
    smnu.addSeparator();	/* "__________" */
    makeMenuItem(smnu,"Clear measurement", "CircleClearMeas", 0, true);
    
    makeMenuItem(smnu,"Edit selected spot(s) 'id' fields from spot list(s) (C-I)", 
                 "EditMeasuredSpotsIDfield", KeyEvent.VK_I, false);  
    makeMenuItem(smnu,"Edit selected spot(s) from spot list(s) (C-E)", 
                 "EditMeasuredSpotsFromSpotList", KeyEvent.VK_E, false); 
    makeMenuItem(smnu,"Delete selected spot from spot list (C-K)", 
                 "CircleRmvSpotFromSpotList", KeyEvent.VK_K, true); 
        
    smnu.addSeparator();	/* "__________" */
    
    makeMenuItem(smnu,"List spots in the spot list for selected image", 
                 "CircleListSpotsInSpotList", 0, true);    
    makeMenuItem(smnu,"List spots in the spot list (tab-delimited)", 
                 "CircleListSpotsInSpotList-tab-delim", 0, true);    
    makeMenuItem(smnu,
     "List 'id'-paired annotated mean norm. spots in both spot lists (tab-delimited)", 
                 "ListPairedAnnMeanNormSpotList-tab-delim", 0, false);      
    makeMenuItem(smnu,
         "List 'id'-paired annotated spots in both spot lists (tab-delimited)", 
                 "ListPairedAnnSpotList-tab-delim", 0, false); 
    
    smnu.addSeparator();	/* "__________" */     
    makeMenuItem(smnu,
       "Lookup Protein IDs and Names in spot list from active map server (selected image)", 
                 "CircleLookupSpotListProtIDs", 0, true);   
    
    smnu.addSeparator();	/* "__________" */  
    makeMenuItem(smnu,"Clear spot list (ask first) for selected image", 
                 "CircleClearSpotList", 0, true);
    
    /* Boundary drawing Quantification menu entries */
    smnu= makeSubMenu(quantMenu, "Measure by boundary", "Q:MeasBnd", 0);
    makeMenuItem(smnu,"Define background", "MeasBkgrd",flk.NOT_AVAIL_YET, true);
    makeMenuItem(smnu,"Define object", "MeasObj",  flk.NOT_AVAIL_YET, true);
    makeMenuItem(smnu,"Done boundary", "MeasDone", flk.NOT_AVAIL_YET, true);
    
    smnu= makeSubMenu(quantMenu, "Print data-window", "Q:DataWind", 0);
    makeMenuItem(smnu,"Show data-window of selected pixel (C-V)",
                "WinDump", KeyEvent.VK_V, true);   
    
    smnu2= makeSubMenu(smnu, "Set print window size", "SetPrintWindowSize",0);
    mi_WinDmpSize5x5CB= makeChkBoxMenuItem(smnu2,"5x5","WinDmpSize-5",0,
                                           flk.maxColsToPrint==5);
    mi_WinDmpSize10x10CB= makeChkBoxMenuItem(smnu2,"10x10","WinDmpSize-10",0,
                                             flk.maxColsToPrint==10);
    mi_WinDmpSize15x15CB= makeChkBoxMenuItem(smnu2,"15x15","WinDmpSize-15",0,
                                             flk.maxColsToPrint==15);
    mi_WinDmpSize20x20CB= makeChkBoxMenuItem(smnu2,"20x20","WinDmpSize-20",0,
                                             flk.maxColsToPrint==20);
    mi_WinDmpSize25x25CB= makeChkBoxMenuItem(smnu2,"25x25","WinDmpSize-25",0,
                                             flk.maxColsToPrint==25);
    mi_WinDmpSize30x30CB= makeChkBoxMenuItem(smnu2,"30x30","WinDmpSize-30",0,
                                             flk.maxColsToPrint==30);
    mi_WinDmpSize35x35CB= makeChkBoxMenuItem(smnu2,"35x35","WinDmpSize-35",0,
                                             flk.maxColsToPrint==35);
    mi_WinDmpSize40x40CB= makeChkBoxMenuItem(smnu2,"40x40","WinDmpSize-40",0,
                                             flk.maxColsToPrint==40);
    
    smnu2= makeSubMenu(smnu, "Set print data radix", "SetPrintDataRadix",0);
    mi_WinDmpRadixDecCB= makeChkBoxMenuItem(smnu2,"decimal",
                                            "WinDmpRadix-DEC",0,
                                     flk.winDumpRadix==Windump.SHOW_DECIMAL);
    mi_WinDmpRadixOctCB= makeChkBoxMenuItem(smnu2,"octal",
                                            "WinDmpRadix-OCT",0,
                                     flk.winDumpRadix==Windump.SHOW_OCTAL);
    mi_WinDmpRadixHexCB= makeChkBoxMenuItem(smnu2,"hex",
                                            "WinDmpRadix-HEX",0,
                                     flk.winDumpRadix==Windump.SHOW_HEX);
    mi_WinDmpRadixODCB= makeChkBoxMenuItem(smnu2,"optical density",
                                            "WinDmpRadix-OD",0,
                                     flk.winDumpRadix==Windump.SHOW_OD);
      
    smnu= makeSubMenu(quantMenu, "Calibrate", "Q:calib", 0);    
    makeMenuItem(smnu,"Optical density by step wedge","CalibODstepWedge",0, true);
    mi_Quant_UseLeukemiaDemoCalibCB= makeChkBoxMenuItem(smnu,
                      "Use demo leukemia gels ND wedge calibration preloads",
                                                        "UseDemoLeukCalPre",
                                                        0,
                                               flk.useDemoLeukemiaCalPreFlag); 
        
    smnu.addSeparator();	/* "__________" */
    makeMenuItem(smnu,"Optical density by spot list","CalibODspotList",0, true);
    
    smnu.addSeparator();	/* "__________" */
    
    makeMenuItem(smnu,"Molecular mass", "CalibMW", flk.NOT_AVAIL_YET, true);
    makeMenuItem(smnu,"pIe", "CalibpIe", flk.NOT_AVAIL_YET, true);
    
    smnu= makeSubMenu(quantMenu, "Region Of Interest (ROI)", "Q:ROI", 0);          
    makeMenuItem(smnu,"Set ROI ULHC (C-U)", "SetRoiULHC",KeyEvent.VK_U, true);
    makeMenuItem(smnu,"Set ROI LRHC (C-L)", "SetRoiLRHC",KeyEvent.VK_L, true);
    makeMenuItem(smnu,"Clear ROI (C-W)","ClearROI", KeyEvent.VK_W, true);
    
    makeMenuItem(smnu,"Show ROI grayscale histogram (C-H)", "ShowROIhist",
                 KeyEvent.VK_H, true);
    makeMenuItem(smnu,"Capture measurement by ROI (C-R)", "ROIMeas",
                 KeyEvent.VK_R, true);
    
    mi_QuantTotDensityCB= makeChkBoxMenuItem(quantMenu,
                                          "Use sum density else mean density",
                                             "useTotDensity",0,
                                             flk.useTotDensityFlag);
   
    /* I.e. C-J Toggle between list-of-spots else trial-spot measurement 
     * mode
     */
    mi_QuantSpotListModeCB= makeChkBoxMenuItem(quantMenu,
                       "List-of-spots else trial-spot measurement-mode",
                                          "SpotListMode", KeyEvent.VK_J, 
                                          flk.spotsListModeFlag);
    
    /* [2.7] Add "Plugins" pulldown menu IFF any active plugins */
    if(pluginMenu!=null)
    { /* add Plugins since at least one */
      for(int i= 1;i<=PluginMgr.nExtFcts; i++)
      { /* add Extern Function menu entry */
        makeMenuItem(pluginMenu,PluginMgr.efMnuName[i],
                     PluginMgr.efName[i], 0, false);
      }
    }
    
    /* [2.8] Add "Help" pulldown menu */
    makeMenuItem(helpMenu,"Flicker Home", "Help:FlickerHome",0, false);
    makeMenuItem(helpMenu,"Reference Manual", "Help:RefMan",0, false);    
    smnu= makeSubMenu(helpMenu, "How-To use controls", "HelpHowTo", 0);    
    makeMenuItem(smnu,"Keyboard shortcut options",
                 "Help:RefMan-keystrokes",0, false);    
    makeMenuItem(smnu,"Mouse controls",
                 "Help:RefMan-mouse",0, false);    
    makeMenuItem(smnu,"Parameter sliders",
                 "Help:RefMan-sliders",0, false);    
    makeMenuItem(smnu,"Checkbox options",
                 "Help:RefMan-checkboxes",0, false);    
    makeMenuItem(smnu,"Menu commands list",
                 "Help:RefMan-menus",0, false);    
    makeMenuItem(smnu,"Image transforms operation",
                 "Help:RefMan-transforms",0, false);        
    makeMenuItem(smnu,"Creating spot lists",
                 "Help:RefMan-CreateSpotList",0, false);         
    makeMenuItem(smnu,"Annotate spot lists",
                 "Help:RefMan-AnnotateSpots",0, false);      
    makeMenuItem(smnu,"Putative spot list IDs",
                 "Help:RefMan-PutativeIDs",0, false); 
    makeMenuItem(smnu,"Adding your images to DB",
                 "Help:RefMan-addUserImages",0, false);    
    makeMenuItem(smnu,"Updating the program and data",
                 "Help:RefMan-updates",0, false); 
    
    makeMenuItem(helpMenu,"Vignettes", "Help:Vignettes", 0, false);
    makeMenuItem(helpMenu,"Version on Web site", "Help:webSiteVersion", 0, false);
    makeMenuItem(helpMenu,"About Flicker", "Help:About", 0, false);
        
    helpMenu.addSeparator();	/* "__________" */
    makeMenuItem(helpMenu,"Book chapter on Flicker (2005)", 
                 "Help:WalkerBookChapter2005 (12.5MB)", 0, false);    
    
    smnu= makeSubMenu(helpMenu, "Old flicker applet documentation", 
                      "HelpFlkApplet", 0);
    makeMenuItem(smnu,"Flicker applet home page",
                 "Help:oldFlickerApplet",0, false);
    makeMenuItem(smnu,"EP97 Paper", "Help:EP97Paper", 0, false);
    makeMenuItem(smnu,"Poster 1", "Help:Poster1", 0, false);
    makeMenuItem(smnu,"Poster 2", "Help:Poster2", 0, false);
    makeMenuItem(smnu,"Poster 3", "Help:Poster3", 0, false);
    makeMenuItem(smnu,"Poster 4", "Help:Poster4", 0, false);
    
    smnu= makeSubMenu(helpMenu, "2D gel Web resources", "Help2DGwebRes", 0);
    makeMenuItem(smnu,"SWISS-2DPAGE", "Help:SWISS-2DPAGE", 0, false);
    makeMenuItem(smnu,"WORLD-2DPAGE", "Help:WORLD-2DPAGE", 0, false);
    makeMenuItem(smnu,"2D-Hunt", "Help:2D-HUNT", 0, false);
    makeMenuItem(smnu,"Google 2D gel search", "Help:Google-2Dsearch", 0, false);
    
    /* Now add the user images menu tree if there is room */
    userImagesProblemFlag= addUserImagesMenuTrees();
  } /* addMenuTree */
  
  
  /**
   * addUserImagesMenuTrees() - build both paired and single menu trees
   * @return true if there is a problem building any of the menus
   */
  public boolean addUserImagesMenuTrees()
  { /* addUserImagesMenuTrees */
    /* Add directories for paired and single user images trees */
    boolean 
      okPairFlag= createFlkUserPairsMenuTree(sUserPairTreeMnu),
      okSingleFlag= createFlkUserSingleMenuTree(sUserSingleTreeMnu),
      problemFlag= (!okPairFlag || !okSingleFlag);
    
    return(problemFlag);
  } /* addUserImagesMenuTrees */
    
      
  /**
   * addImageParameterStateScrollers() - create image parameter 
   * state scrollers and put on right of main frame.
   */
  void addImageParameterStateScrollers(JPanel pScroll)
  { /* addImageParameterStateScrollers */
    SliderState curState= flk.curState;
    EventScroller evS= new EventScroller(flk);  /* event handler */
    flk.evs= evS;        /* make global in case want to get to it */
    
    /* Use AWT implemented zoom only does positive integer zoom */
    if(flk.USE_AWT_ZOOM)
    {
      zoomLabel= new JLabel("zoom: "+curState.magnificationAWT+"X");
      zoomBar= new Scrollbar(Scrollbar.HORIZONTAL,
                             curState.magnificationAWT, /* initial val */
                             THICKNESS,
                             SliderState.MIN_ZOOM, /* min value 1X */
                             SliderState.MAX_ZOOM  /* max value */
                            );
      pScroll.add(zoomLabel);
      pScroll.add(zoomBar);
      zoomBar.addAdjustmentListener(evS);
    }
    	
    /* scroll bar for zoomMag (de_zoom transform over [1/N : N] X zoom */
    if(flk.USE_DE_ZOOM)
    {
      String zmValStr= SliderState.cvtZoomMagScr2Str(curState.zoomMagScr);
      zoomMagLabel= new JLabel("ZoomMag: "+ zmValStr + "X");
      zoomMagLabel.setToolTipText("Image magnification");
      zoomMagBar= new Scrollbar(Scrollbar.HORIZONTAL,
                                curState.zoomMagScr,          /* init val*/
                                THICKNESS,
                                SliderState.MIN_ZOOM_MAG_SCR, /* min value */
                                (SliderState.MAX_ZOOM_MAG_SCR+
                                THICKNESS)                   /* max value */
                               );
      pScroll.add(zoomMagLabel);
      pScroll.add(zoomMagBar);
      zoomMagBar.addAdjustmentListener(evS);
    }	
    
    /* -------- */
    angleLabel= new JLabel("(3D) angle: "+ curState.angle + "Deg");
    angleLabel.setToolTipText("Angle 'Pseudo 3D transform'");
    angleBar= new Scrollbar(Scrollbar.HORIZONTAL,
                            curState.angle,    /* initial value */
                            THICKNESS,
                            -SliderState.MAX_ANGLE, /* min value*/
                            SliderState.MAX_ANGLE   /* max value */
                            );
    pScroll.add(angleLabel);
    pScroll.add(angleBar);
    angleBar.addAdjustmentListener(evS);
    
    /* -------- */
    zScaleLabel= new JLabel("(3D) z-scale: " + curState.zScale + "%");
    zScaleLabel.setToolTipText("Height 'Pseudo 3D transform'");
    zScaleBar= new Scrollbar(Scrollbar.HORIZONTAL,
                             curState.zScale, /* initial value */
                             THICKNESS,
                             0,                   /* minimum value */
                             SliderState.MAX_ZSCALE  /* max value */
                             );
    pScroll.add(zScaleLabel);
    pScroll.add(zScaleBar);
    zScaleBar.addAdjustmentListener(evS);
    
    /* -------- */
    eScaleLabel= new JLabel("(Sharpen) e-scale: " + curState.eScale + "%");
    eScaleLabel.setToolTipText("Sharpen transforms scaling");
    eScaleBar= new Scrollbar(Scrollbar.HORIZONTAL,
                             curState.eScale, /* initial value */
                             THICKNESS,
                             0,                   /* minimum value */
                             SliderState.MAX_ESCALE  /* max value */
                             );
    pScroll.add(eScaleLabel);
    pScroll.add(eScaleBar);
    eScaleBar.addAdjustmentListener(evS);
    
    /* -------- */
    contrastLabel= new JLabel("Contrast: " + 0/*curState.contrast*/ +"%");
    contrastLabel.setToolTipText("Image contrast");
    contrastBar= new Scrollbar(Scrollbar.HORIZONTAL,
                               curState.contrast, /* scaled int*/
                               THICKNESS,
                               SliderState.MIN_CONTRAST,
                               SliderState.MAX_CONTRAST
                               );
    pScroll.add(contrastLabel);
    pScroll.add(contrastBar);
    contrastBar.addAdjustmentListener(evS);
    
    /* -------- */
    brightnessLabel= new JLabel("Brightness: "+ 0/*curState.brightness*/
                               +"%");
    brightnessLabel.setToolTipText("Image brightness");
    brightnessBar= new Scrollbar(Scrollbar.HORIZONTAL,
                                 curState.brightness,
                                 THICKNESS,
                                 SliderState.MIN_BRIGHTNESS,
                                 SliderState.MAX_BRIGHTNESS
                                 );
    pScroll.add(brightnessLabel);
    pScroll.add(brightnessBar);
    brightnessBar.addAdjustmentListener(evS);
    
    /* -------- */
    /*  The "extent" is the size of the viewable area. It is also known 
     * as the "visible amount".
     */
    threshold2Label= new JLabel("Threshold2: "+ curState.threshold2);   
    threshold2Label.setToolTipText("Upper grayscale slice");
    threshold2Bar= new Scrollbar(Scrollbar.HORIZONTAL,
                                 curState.threshold2,
                                 0,
                                 SliderState.MIN_THRESHOLD2,
                                 SliderState.MAX_THRESHOLD2
                                 );
    pScroll.add(threshold2Label);
    pScroll.add(threshold2Bar);
    threshold2Bar.addAdjustmentListener(evS);
    
    /* -------- */
    threshold1Label= new JLabel("Threshold1: "+ curState.threshold1);    
    threshold1Label.setToolTipText("Lower grayscale slice");
    threshold1Bar= new Scrollbar(Scrollbar.HORIZONTAL,
                                 curState.threshold1,
                                 THICKNESS,
                                 SliderState.MIN_THRESHOLD1,
                                 SliderState.MAX_THRESHOLD1
                                 );
    pScroll.add(threshold1Label);
    pScroll.add(threshold1Bar);
    threshold1Bar.addAdjustmentListener(evS);   
        
    /* -------- */    
    /* GUI state circle size used for curState.measCircleRadius */
    measCircleRadiusLabel= new JLabel("Meas circle: "+
                                     ((2*curState.measCircleRadius)+1) + 
                                     " (diameter)");   
    measCircleRadiusLabel.setToolTipText("Size of measurement circle");
    measCircleRadiusBar= new Scrollbar(Scrollbar.HORIZONTAL,
                                       curState.measCircleRadius,
                                       THICKNESS,
                                       flk.MIN_CIRCLE_RADIUS,
                                       flk.MAX_CIRCLE_RADIUS+THICKNESS
                                     );
    pScroll.add(measCircleRadiusLabel);
    pScroll.add(measCircleRadiusBar);
    measCircleRadiusBar.addAdjustmentListener(evS); 
    
    /* ------- special controls ----- */
    
    /* Add buttons at bottom of the slider panel */
    Font buttonFont= new Font("Helvetica", Font.BOLD, 10);   
    
    /* force size of buttons to be smaller, more space for images */
    /* FUTURE: could have these buttons change in size as "+" or "-" are
     * pressed so the sliders/buttons/labels will "scale" with the images
     */   
    JButton reportScrollerValuesButton= new JButton("Report scroller values");    
   
    Insets bInsets= new Insets(1, 1, 1, 1); 
    reportScrollerValuesButton.setMargin(bInsets);
    
    reportScrollerValuesButton.setToolTipText("Save scroller values to Report Window");
    reportScrollerValuesButton.addActionListener(this);
    reportScrollerValuesButton.setFont(buttonFont);
    pScroll.add(reportScrollerValuesButton);
    
    /* holds ["-" button] [CanvasSizeLabel] ["+" Button] */
    JPanel pCanvasSize= new JPanel();
    pCanvasSize.setBackground(Color.white);
    JButton minusCanvasSizeButton= new JButton("-");   
    JButton plusCanvasSizeButton= new JButton("+");    
        
    minusCanvasSizeButton.setMargin(bInsets);  /* for smaller buttons */
    plusCanvasSizeButton.setMargin(bInsets);   
    
    minusCanvasSizeButton.setToolTipText("Click to decrease size "+
                                         "of window.");
    plusCanvasSizeButton.setToolTipText("Click to increase size "+
                                         "of window.");
     
    minusCanvasSizeButton.setActionCommand("CanvasSize:-");   
    plusCanvasSizeButton.setActionCommand("CanvasSize:+");
    minusCanvasSizeButton.addActionListener(this);
    plusCanvasSizeButton.addActionListener(this);
    minusCanvasSizeButton.setFont(buttonFont);
    plusCanvasSizeButton.setFont(buttonFont);
    canvasSizeLabel= new JLabel("Canvas size:"+flk.canvasSize);
    canvasSizeLabel.setFont(buttonFont);
    
    pCanvasSize.add(minusCanvasSizeButton);
    pCanvasSize.add(canvasSizeLabel);
    pCanvasSize.add(plusCanvasSizeButton);
    pScroll.add(pCanvasSize);     
  } /* addImageParameterStateScrollers */
  
  
  /**
   * addCheckboxes() - add checkboxes to chkboxPanel
   */
  private void addCheckboxes(JPanel chkboxPanel)
  { /* addCheckboxes */    
    flickerCheckbox= new JCheckBox("Flicker (C-F)");
    flickerCheckbox.setToolTipText("Enable flickering");
    flickerCheckbox.addItemListener(evMu);
    flickerCheckbox.setSelected(false);
    flickerCheckbox.setBackground(Color.white);
    chkboxPanel.add(flickerCheckbox);
                
    clickableCheckbox= new JCheckBox("Click to access DB");
    clickableCheckbox.setToolTipText("Enable Web DB access for clickable spots");
    clickableCheckbox.setSelected(false); 
    //clickableCheckbox.setEnabled(flk.isClickableDBflag);
    clickableCheckbox.addItemListener(evMu);
    clickableCheckbox.setSelected(flk.userClickableImageDBflag);
    clickableCheckbox.setBackground(Color.white);
    chkboxPanel.add(clickableCheckbox);
    
    /* Make checkbox to Replace(Reuse) oI with xform images */
    allowXformsCheckbox= new JCheckBox("Allow transforms");
    allowXformsCheckbox.setToolTipText("Display transform results else original image");
    allowXformsCheckbox.addItemListener(evMu);
    allowXformsCheckbox.setSelected(flk.allowXformFlag);
    allowXformsCheckbox.setBackground(Color.white);
    chkboxPanel.add(allowXformsCheckbox);
    
    /* Make checkbox to Replace(Reuse) oImg xform as input for next xform */
    if(!flk.allowXformFlag)
      flk.composeXformFlag= false;
    composeXformCheckbox= new JCheckBox("Sequential transforms");
    composeXformCheckbox.setToolTipText("Transform input is last transform else original image");
    composeXformCheckbox.addItemListener(evMu);
    composeXformCheckbox.setSelected(flk.composeXformFlag);
    composeXformCheckbox.setBackground(Color.white);
    chkboxPanel.add(composeXformCheckbox);
  } /* addCheckboxes */
  
  
  /**
   * addImageScrollers() - add image scrollers:
   * flicker to pFlicker, and image 1 and Image 2 scrollable images
   * to pImages panels in the frame.
   */
  private void addImageScrollers(JPanel pFlicker, JPanel pImages)
  { /* addImageScrollers */    
    flk.flkIS= new ImageScroller(flk, "flicker", "Flicker is off", 
                                 flk.iData1,
                                 flk.canvasSize, flk.canvasSize,
                                 0,0,0 /* no delay scroll bar*/,
                                 false);
 
    /* [2] Add scrollable images - change image obj later */
    flk.i1IS= new ImageScroller(flk, "left", "Image I1", flk.iData1,
                                flk.canvasSize, flk.canvasSize,
                                flk.defaultFlickerDelay,
                                MIN_FLICKER_DELAY,
                                MAX_FLICKER_DELAY,
                                true);
  
    flk.i2IS= new ImageScroller(flk, "right", "Image I2", flk.iData2,
                                flk.canvasSize, flk.canvasSize,
                                flk.defaultFlickerDelay,
                                MIN_FLICKER_DELAY,
                                MAX_FLICKER_DELAY,
                                true);   
    
    flk.flkIS.setBackground(Color.white);
    flk.i1IS.setBackground(Color.white);
    flk.i2IS.setBackground(Color.white);
    
    pFlicker.add(flk.flkIS);    
    pImages.add(flk.i1IS);
    pImages.add(flk.i2IS);   
  } /* addImageScrollers */
        
    
  /**
   * actionPerformed() - handle action performed state changed events
   * @param e is action event
   */
  public void actionPerformed(ActionEvent e)
  { /* actionPerformed */
    evMu.actionPerformed(e);	/* event handling */
     } /* actionPerformed */
  
  
  /**
   * itemStateChanged() - handle item state changed events.
   * @param e is checkbox event Checkbox and menu checkbox commands
   */
  public void itemStateChanged(ItemEvent e)
  { /* itemStateChanged */    
    evMu.itemStateChanged(e);  /* event handling */
  } /* itemStateChanged */
  
  
  /**
   * windowClosing() - close the window
   * @param e is window closing event
   * @see MAExplorer#quit
   */
  public void windowClosing(WindowEvent e)
  { /* windowClosing */
    flk.finalize();
  } /* windowClosing */
  
  
  public void windowActivated(WindowEvent e) {}
  public void windowClosed(WindowEvent e) { }
  public void windowDeactivated(WindowEvent e) {}
  public void windowDeiconified(WindowEvent e) {}
  public void windowIconified(WindowEvent e) {}
  public void windowOpened(WindowEvent e) {}
    
  public void stateChanged(javax.swing.event.ChangeEvent changeEvent)
  {
  }  
   
  
  /**
   * paintComponent() - repaint scroll bars
   * @param g is graphics context
   */
  public void paint(Graphics g)   
  { /* paintComponent */      
   // super.paintComponent(g);      
  } /* paintComponent */ 
  
} /* End of class BuildGUI */

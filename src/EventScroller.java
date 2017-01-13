/* File: EventScroller.java */

import java.awt.*;
import java.awt.event.*;
import java.awt.event.AdjustmentEvent;
import java.awt.image.*;
import java.util.EventListener;
import java.lang.*;

 
/**
 * EventScroller class supports scroller event handling for Flicker top 
 * level GUI.
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

public class EventScroller implements AdjustmentListener
{ /*  EventScroller */
  Flicker
    flk;  
  Util
    util;
  
  /** event scroller name */
  public String 
    name;
  /** event scroller id */
  public int
    id;
  
  /** Image Scroller for current data sample being processed */
  private ImageScroller
    lastIS= null; 
  /** ImageData instance of current data sample being processed */
  private ImageData
    iData= null;
  /** file name of current data sample being processed */
  private String
    imgFile= null;
  /** current data sample being processed is LEFT or RIGHT image */
  private boolean
    leftOrRightSelectedFlag= false;
  /** LEFT image is selected */
  private boolean
    isLeftFlag= false;
  /** RIGHT image is selected */
  private boolean
    isRightFlag= false;
    
  
  /**
   * EventScroller() - constructor
   * @param flk is Flicker instance
   */
  public EventScroller(Flicker flk)
  {
    this.flk= flk;
    util= flk.util;
    this.id= 0;
    this.name= "*ONCE_ONLY*";
  }
  
  
  /**
   * EventScroller() - constructor
   * @param name of event
   * @param id of event
   * @param flk is Flicker instance
   */
  public EventScroller(String name, int id, Flicker flk)
  {
    this.id= id;
    this.name= name;
    this.flk= flk;
    util= flk.util;
  }
  
  
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
    isLeftFlag= false;
    isRightFlag= false;
    
    if(flk.activeImage.equals("left"))
    {
      lastIS= flk.i1IS;
      iData= flk.iData1;
      imgFile= flk.iData1.imageFile;
      isLeftFlag= true;
      leftOrRightSelectedFlag= true;
    }
    else if(flk.activeImage.equals("right"))
    {
      lastIS= flk.i2IS;
      iData= flk.iData2;
      imgFile= flk.iData2.imageFile;
      isRightFlag= true;
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
   * adjustmentValueChanged() - handle adjustment value changed events
   * @param e is AdjustmentEvent event
   */
  public void adjustmentValueChanged(AdjustmentEvent e) 
  { /* adjustmentValueChanged */
    Scrollbar evtScrBar= (Scrollbar)e.getSource();
    BuildGUI bGui= flk.bGui;
    String lastISName= flk.activeImage;
    
    /* Lookup selected data */
    lookupCanvasPtrAndName();
    
    boolean 
      isLeftFlag= flk.activeImage.equals("left"),
      isRightFlag= flk.activeImage.equals("right"),
      doRepaintFlag= false;    
       
    if(!leftOrRightSelectedFlag && 
       // (flk.viewGangBCFlag && 
       //  (evtScrBar==bGui.contrastBar || evtScrBar==bGui.brightnessBar)) ||
       !(flk.viewGangZoomFlag && evtScrBar==bGui.zoomMagBar)
       )
    { /* Alert user to select an image first! */ 
      setScrollersToDefault();
      String msg= "Select left or right image first.";
      util.popupAlertMsg(msg, flk.alertColor);
      return;
    } /* Alert user to select an image first! */ 
    
    else
    { /* service scrollers */
      /* ---------------- ScrollBars --------------- */
      if(evtScrBar==bGui.zoomBar)
      { /* zoom scrollbar */
        int magnificationAWT= evtScrBar.getValue();
        flk.curState.magnificationAWT= magnificationAWT;
        
        String msg= "zoom: " + magnificationAWT + "X";
        bGui.zoomLabel.setText(msg);
        /* NOTE: zoom is done by ImageScroller repaint() */
        doRepaintFlag= true;
      } /* zoom scrollbar */
      
      else if(evtScrBar==bGui.zoomMagBar)
      { /* zoomMag scrollbar */
        /* note remap scroller range of to magnification range [1/N : N] */
        int zoomMagScr= evtScrBar.getValue();
        float zoomMagVal= SliderState.cvtZoomMagScr2ZoomMagVal(zoomMagScr);
        flk.curState.zoomMagScr= zoomMagScr;
        flk.curState.zoomMagVal= zoomMagVal;
        int
          x= 0,
          y= 0;
         
        if(isLeftFlag)
        {
          x= flk.i1IS.siCanvas.xImg;
          y= flk.i1IS.siCanvas.yImg;
        }
        else
        {          
          x= flk.i2IS.siCanvas.xImg;
          y= flk.i2IS.siCanvas.yImg;
        }        
        
        String
          sVal= SliderState.cvtZoomMagScr2Str(zoomMagScr),
          msg= "ZoomMag: " + sVal + "X";
        bGui.zoomMagLabel.setText(msg);
        
        /* Save the state in correct IS state */
        if(isLeftFlag || flk.viewGangZoomFlag)
          flk.iData1.state.zoomMagScr= zoomMagScr;
        else if(isRightFlag || flk.viewGangZoomFlag)
          flk.iData2.state.zoomMagScr= zoomMagScr;
        if((leftOrRightSelectedFlag || flk.viewGangZoomFlag) &&
           !flk.doingXformFlag)
        { /* zoom the active image or both if gang zoom */       
          /* Note: must call zoom without using threads!! Crashes otherwise*/
          if(flk.viewGangZoomFlag)
            flk.evMu.processTransform("DeZoom", "both", 0);
          else
            flk.evMu.processTransform("DeZoom", flk.activeImage, 0);
          doRepaintFlag= true;         
            
        } /* zoom the active image or both if gang zoom */
      } /* zoomMag scrollbar */
      
      else if(evtScrBar==bGui.angleBar)
      { /* angle scrollbar */
        flk.curState.angle= evtScrBar.getValue();
        String msg= "(3D) Angle: " + flk.curState.angle + "Deg";
        bGui.angleLabel.setText(msg);
        if(leftOrRightSelectedFlag&& !flk.doingXformFlag)
        {
          flk.evMu.processTransform("Pseudo3D", flk.activeImage, 0);
          doRepaintFlag= true;
        }
      } /* angle scrollbar */
      
      else if(evtScrBar==bGui.zScaleBar)
      { /* zScale scrollbar */
        flk.curState.zScale= evtScrBar.getValue();
        String msg= "(3D) z-scale: " + flk.curState.zScale + "%";
        bGui.zScaleLabel.setText(msg);
        doRepaintFlag= true;
      } /* zScale scrollbar */
      
      else if(evtScrBar==bGui.eScaleBar)
      { /* eScale scrollbar */
        flk.curState.eScale= evtScrBar.getValue();
        String msg= "(Sharpen) e-scale: " + flk.curState.eScale + "%";
        bGui.eScaleLabel.setText(msg);
        doRepaintFlag= true;
      } /* eScale scrollbar */
      
      else if(evtScrBar==bGui.contrastBar)
      { /* contrast scrollbar */
        int scrContrast= evtScrBar.getValue();
        if(isRightFlag || flk.viewGangBCFlag)
        { /* right */
          if(flk.iData2.iImg!=null)
          { /* image not null */
            flk.iData2.state.contrast= scrContrast;
            bGui.contrastLabel.setText("Contrast: " +
                                   (scrContrast - flk.curState.DEF_CONTRAST)+
                                   "%");
            flk.i2IS.bcImgF.setBrCt(flk.i2IS,
                                    flk.iData2.state.brightness,
                                    flk.iData2.state.contrast,
                                    flk.curState.MAX_BRIGHTNESS,
                                    flk.curState.MAX_CONTRAST,
                                    true);
            doRepaintFlag= flk.i2IS.processBCimage(flk.iData2);
          } /* image not null */
        } /* right */
        
        if(isLeftFlag || flk.viewGangBCFlag)
        { /* left */
          if(flk.iData1.iImg!=null)
          { /* image not null */
            flk.iData1.state.contrast= scrContrast;
            bGui.contrastLabel.setText("Contrast: " +
                                     (scrContrast-flk.curState.DEF_CONTRAST)+
                                     "%");
            flk.i1IS.bcImgF.setBrCt(flk.i1IS,
                                    flk.iData1.state.brightness,
                                    flk.iData1.state.contrast,
                                    flk.curState.MAX_BRIGHTNESS,
                                    flk.curState.MAX_CONTRAST,
                                    true);
            doRepaintFlag= flk.i1IS.siCanvas.processBCimage(flk.iData1);
          } /* image not null */
        } /* left */
      } /* contrast scrollbar */
      
      else if(evtScrBar==bGui.brightnessBar)
      { /* brightness scrollbar */
        int scrBrightness= evtScrBar.getValue();
        if(isRightFlag || flk.viewGangBCFlag)
        { /* right */
          if(flk.iData2.iImg!=null)
          { /* image not null */
            flk.iData2.state.brightness= scrBrightness;
            bGui.brightnessLabel.setText("Brightness: "+
                                  (scrBrightness-flk.curState.DEF_BRIGHTNESS)+
                                  "%");
            flk.i2IS.bcImgF.setBrCt(flk.i2IS,
                                    flk.iData2.state.brightness,
                                    flk.iData2.state.contrast,
                                    flk.curState.MAX_BRIGHTNESS,
                                    flk.curState.MAX_CONTRAST,
                                    true);
            doRepaintFlag= flk.i2IS.siCanvas.processBCimage(flk.iData2);
          } /* image not null */
        } /* right */
        
        if(isLeftFlag || flk.viewGangBCFlag)
        { /* left */
          if(flk.iData1.iImg!=null)
          { /* image not null */
            flk.iData1.state.brightness= scrBrightness;
            bGui.brightnessLabel.setText("Brightness: " +
                                 (scrBrightness-flk.curState.DEF_BRIGHTNESS)+
                                 "%");
            flk.i1IS.bcImgF.setBrCt(flk.i1IS,
                                    flk.iData1.state.brightness,
                                    flk.iData1.state.contrast,
                                    flk.curState.MAX_BRIGHTNESS,
                                    flk.curState.MAX_CONTRAST,
                                    true);
            doRepaintFlag= flk.i1IS.processBCimage(flk.iData1);
          } /* image not null */
        } /* left */
      } /* brightness scrollbar */
      
      else if(evtScrBar==bGui.threshold1Bar)
      { /* threshold1 scrollbar */
        int
          val= evtScrBar.getValue(),
          threshold1= 0,
          threshold2= 255;
        
        if(isLeftFlag)
        {
          threshold1= flk.iData1.state.threshold1;
          threshold2= flk.iData1.state.threshold2;
        }
        else if(isRightFlag)
        {
          threshold1= flk.iData2.state.threshold1;
          threshold2= flk.iData2.state.threshold2;
        }
        
        if(val<=threshold2)
        { /* save the state in correct IS state */
          if(isLeftFlag)
            flk.iData1.state.threshold1= val;
          else if(isRightFlag)
            flk.iData2.state.threshold1= val;
          bGui.threshold1Label.setText("Threshold1: " +  val);
          if((isLeftFlag || isRightFlag) && !flk.doingXformFlag)
          {
            flk.evMu.processTransform("Threshold", flk.activeImage, 0);
            doRepaintFlag= true;
          }
        }
      } /* threshold1 scrollbar */
      
      else if(evtScrBar==bGui.threshold2Bar)
      { /* threshold2 scrollbar */
        int
          val= evtScrBar.getValue(),
          threshold1= 0,
          threshold2= 255;

        if(isLeftFlag)
        {
          threshold1= flk.iData1.state.threshold1;
          threshold2= flk.iData1.state.threshold2;
        }
        else if(isRightFlag)
        {
          threshold1= flk.iData2.state.threshold1;
          threshold2= flk.iData2.state.threshold2;
        }
        
        if(val>=threshold1)
        { /* save the state in correct IS state */
          if(isLeftFlag)
            flk.iData1.state.threshold2= val;
          else if(isRightFlag)
            flk.iData2.state.threshold2= val;
          bGui.threshold2Label.setText("Threshold2: " +  val);
          if((isLeftFlag || isRightFlag) && !flk.doingXformFlag)
          {
            flk.evMu.processTransform("Threshold", flk.activeImage, 0);
            doRepaintFlag= true;
          }
        }
      } /* threshold2 scrollbar */      
      
      else if(evtScrBar==bGui.measCircleRadiusBar)
      { /* meas Circle Radius scrollbar */
        flk.measCircleRadius= bGui.measCircleRadiusBar.getValue();
        ImageDataMeas.setCircleMaskRadius(flk.measCircleRadius,iData);
        String msg= "Meas circle: " + 
                    ((2*flk.curState.measCircleRadius)+1) +
                    " (diameter)";
        bGui.measCircleRadiusLabel.setText(msg);
        doRepaintFlag= true;
      }      
    } /* service scrollers */
    
    if(doRepaintFlag)
    { /* Now repaint using the new values */
      if(isLeftFlag)
      {      
        flk.i1IS.paintSiCanvas();
      }
      else if(isRightFlag)
      {       
        flk.i2IS.paintSiCanvas();
      }
    } /* Now repaint using the new values */  
   else
      setScrollersToDefault();      
  } /* adjustmentValueChanged */
  
  
  /** 
   * setScrollersToDefault() - set Event scrollers to default values
   * for Flicker top level GUI.
   */
  public void setScrollersToDefault()
  { /* setScrollersToDefault */
    BuildGUI bGui= flk.bGui;
    /* set default values */
    SliderState state= new SliderState();
    state.init(flk,"scroll-default");
    
    /* angle */
    String msg= "(3D) Angle: " + state.DEF_ANGLE + "Deg";
    bGui.angleLabel.setText(msg);
    bGui.angleBar.setValue(state.DEF_ANGLE);
    
    /* brightness */
    msg= "Brightness: " +  state.DEF_BRIGHTNESS+ "%";
    bGui.brightnessLabel.setText(msg);
    bGui.brightnessBar.setValue(state.DEF_BRIGHTNESS);
    
    /* contrast */
    msg= "Contrast: " + state.DEF_CONTRAST + "%";
    bGui.contrastLabel.setText(msg);
    bGui.contrastBar.setValue((int) state.DEF_CONTRAST);
    
    /* e-scale */
    msg= "(Sharpen) e-scale: " + state.DEF_ESCALE + "%";
    bGui.eScaleLabel.setText(msg);
    bGui.eScaleBar.setValue(state.DEF_ESCALE);
    
    /* threshold1 */
    msg= "Threshold1: " +  state.DEF_THRESHOLD1;
    bGui.threshold1Label.setText(msg);
    bGui.threshold1Bar.setValue(state.DEF_THRESHOLD1);
    
    /* threshold2 */
    msg= "Threshold2: " +  state.DEF_THRESHOLD2;
    bGui.threshold2Label.setText(msg);
    bGui.threshold2Bar.setValue(state.DEF_THRESHOLD2);
    
    /* z-scale */
    msg= "(3D) z-scale: " + state.DEF_ZSCALE + "%";
    bGui.zScaleLabel.setText(msg);
    bGui.zScaleBar.setValue(state.DEF_ZSCALE);
    
    /* Zoom */
    if(bGui.zoomBar!=null)
    {
      msg= "ZoomMag: " + state.DEF_ZOOM + "X";
      bGui.zoomLabel.setText(msg);
      bGui.zoomBar.setValue(state.DEF_ZOOM);
    }  
        
    /* meas circle (radias ==> diameter) */
    msg= "Meas circle: " + ((2*flk.measCircleRadius)+1) + " (diameter)";
    bGui.measCircleRadiusLabel.setText(msg);
    bGui.measCircleRadiusBar.setValue(state.DEF_ZSCALE);
  } /* setScrollersToDefault */

  
  /**
   * setEnabled() - Enable/disable for ImageXform scrollers 
   * @param flag to enable/disable the angle, eScale, zScale
   *        threshold1 and threshold2 scrollers.
   */
  public void setEnabled(boolean flag)
  { /* setEnabled */ 
    BuildGUI bGui= flk.bGui; 
    bGui.angleBar.setEnabled(flag);
    bGui.eScaleBar.setEnabled(flag);
    bGui.zScaleBar.setEnabled(flag);
    bGui.threshold2Bar.setEnabled(flag);
    bGui.threshold1Bar.setEnabled(flag);
    bGui.brightnessBar.setEnabled(flag);
    bGui.contrastBar.setEnabled(flag);   
    if(bGui.zoomBar!=null) 
      bGui.zoomBar.setEnabled(flag);
    if(bGui.zoomMagBar!=null)
      bGui.zoomMagBar.setEnabled(flag);
    bGui.measCircleRadiusBar.setEnabled(flag);
  } /* setEnabled */
  
  
   /** 
    * setEventScrollers() - set Event scrollers for Flicker top level GUI.
    * This sets new values from the specified state
    * @param state to set the scrollers. It is flk.iData1.state or flk.iData2.state
    * @return true if suceed, false if any problems.
   */
  public boolean setEventScrollers(SliderState state)
  { /* setEventScrollers */ 
    if(state==null)
      return(false);
      
    BuildGUI bGui= flk.bGui;
    
    /* threshold1 */
    String msg= "Threshold1: " +  state.threshold1;
    bGui.threshold1Label.setText(msg);
    bGui.threshold1Bar.setValue(state.threshold1);
    
    /* threshold2 */
    msg= "Threshold2: " + state.threshold2;
    bGui.threshold2Label.setText(msg);
    bGui.threshold2Bar.setValue(state.threshold2);
    
    /* Zoom */
    if(bGui.zoomBar!=null)
    {
      msg= "Zoom: " + state.magnificationAWT + "X";
      bGui.zoomLabel.setText(msg);
      bGui.zoomBar.setValue(state.magnificationAWT);
    }
    
    /* ZoomMag */
    if(bGui.zoomMagBar!=null)
    {
      msg= "ZoomMag: " + SliderState.cvtZoomMagScr2Str(state.zoomMagScr) + "X";
      bGui.zoomMagLabel.setText(msg);
      bGui.zoomMagBar.setValue(state.zoomMagScr); 
    }  
    
    /* brightness */
    msg= "Brightness: " + 
         (state.brightness - state.DEF_BRIGHTNESS) + "%";
    bGui.brightnessLabel.setText(msg);
    bGui.brightnessBar.setValue(state.brightness);
    
    /* contrast */
    msg= "Contrast: " +
          (state.contrast - state.DEF_CONTRAST) + "%";
    bGui.contrastLabel.setText(msg);
    bGui.contrastBar.setValue((int) state.contrast);
    
    /* e-scale */
    msg= "(Sharpen) e-scale: " + state.eScale + "%";
    bGui.eScaleLabel.setText(msg);
    bGui.eScaleBar.setValue( state.eScale);
    
    /* z-scale */
    msg= "(3D) z-scale: " + state.zScale + "%";
    bGui.zScaleLabel.setText(msg);
    bGui.zScaleBar.setValue( state.zScale);
    
    /* angle */
    msg= "(3D) angle: " + state.angle + "Deg";
    bGui.angleLabel.setText(msg);
    bGui.angleBar.setValue(state.angle);    
    
    /* meas circle size */
    msg= "Meas circle: " + ((2*state.measCircleRadius)+1) +
         " (diameter)";
    bGui.measCircleRadiusLabel.setText(msg);
    bGui.measCircleRadiusBar.setValue(state.measCircleRadius);    
    
    return(true);
  } /* setEventScrollers */
 
  
} /* end of class EventScroller */

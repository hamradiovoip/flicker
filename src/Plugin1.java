/* File: Plugin1.java  */

import java.lang.*;

/**
 * Plugin1 External Function object class for Flicker. This sample
 * plugin is a "Color complement plugin". Note: it must be compile
 * with either Flicker .java files or the Flicker.jar file.
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

public class Plugin1 extends PluginMgr
{ /* Plugin1 */

  /**
   * Plugin1() - constructor
   */
  public Plugin1()
  { /* Plugin1 */
    
    /* do not change! */
    name= "Plugin1";		/* name of command for event handler*/
    extFctNbr= 1;		    /* function number  if not zero*/
    
    /* Edit this */
    mnuName= "Color complement plugin";  	/* what appears in menu */
    
    /* optional help message for function */
    info= "Plugin1 implements a colo image complement function.\n"+
          "It does simple complementation (g'=255-g) for each color.";
    
    /* Edit this */
    isActive= true;	   	/* set if is active function */
  } /* Plugin1 */

  
  /**
   * fctCalc() - called to perform the image function
   * @param flk is instance of flicker
   * @param iData image data to process
   * @param oPix is output pixel array to put results
   */
  public boolean fctCalc(Flicker flkS, ImageData iData, int oPix[])
  { /* fctCalc */
    flk= flkS;
    int
      i= 0,
      gIn, 
      x, y,
      rI, gI, bI,
      gOut,
      rO, gO, bO,
      width= iData.iWidth,
      height= iData.iHeight,
      size= width * height,
      iPix[]= iData.iPix;
    
    for(y= 0;y<height;y++)
    { /* process row */
      for(x=0;x<width;x++)
      { /* process column */
        /* [NOTE] insert your plugin computation here... */ 
        gIn= iPix[i];
        rI= (gIn >>16) & 0Xff;
        gI= (gIn >>8) & 0Xff;
        bI= gIn & 0Xff;
        rO= (255 - rI);
        gO= (255 - gI);
        bO= (255 - bI);
        gOut= (0Xff000000 | rO<<16 | gO<<8 | bO);
        oPix[i]= gOut;
        i++;
      } /* process column */
    } /* process row */
    
    return(true);
  } /* fctCalc */


} /* End of Plugin1 */

/* File: Plugin3.java  */

import java.lang.*;

/**
 * Plugin3 External Function object class for Flicker. This is a
 * free slot for a sample plugin. Note: it must be compile
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

public class Plugin3 extends PluginMgr
{ /* Plugin3 *  

  /**
   * Plugin3() - constructor
   */
  public Plugin3()
  { /* Plugin3 */    
    /* do not change! */
    name= "Plugin3";		/* name of command for event handler*/
    extFctNbr= 3;		/* function number  if not zero*/
    
    /* Edit this */
    mnuName= "Opr 3";		/* what appears in menu */
    info= "";			/* optional help message for function */
    
    /* Edit this */
    isActive= false;		/* set if is active function */
  }
  
  
  /**
   * fctCalc() - called to perform the image function
   * @param flk is instance of flicker
   * @param iData image data to process
   * @param oPix is output pixel array to put results
   */
  public boolean fctCalc(Flicker flkS, ImageData iData, int oPix[])
  { /* Plugin3 */
    flk= flkS;
    int
      i= 0,
      x, y,
      width= iData.iWidth,
      height= iData.iHeight,
      size= width * height,
      iPix[]= iData.iPix;
    
    for(y= 0;y<height;y++)
    { /* process row */
      for(x=0;x<width;x++)
      { /* process column */
        /* [NOTE] insert your plugin computation here... */        
        oPix[i]= iPix[i];
        i++;
      } /* process column */
    } /* process row */
    
    return(true);
  } /* Plugin3 */

} /* End of Plugin3 */

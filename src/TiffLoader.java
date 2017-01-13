/* File TiffLoader.java - reads Tiff image. */

import java.io.File;
import java.io.IOException;

import java.awt.*;
import java.awt.image.*;
import java.awt.image.MemoryImageSource;

import javax.media.jai.NullOpImage;

import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageCodec;
import javax.media.jai.UnpackedImageData;
import javax.media.jai.PixelAccessor;

/** Class TiffLoader reads Tiff image into the class instance.
 * variables.
 *<PRE>
 * It uses class Raster when reading the image.
 * All image data is expressed as a collection of pixels. Each pixel consists
 * of a number of samples. A sample is a datum for one band of an image
 * and a band consists of all samples of a particular type in an image. For
 * example, a pixel might contain three samples representing its red, green
 * and blue components. There are three bands in the image containing this
 * pixel. One band consists of all the red samples from all pixels in the
 * image. The second band consists of all the green samples and the 
 * remaining band consists of all of the blue samples. The pixel can be 
 * stored in various formats. For example, all samples from a particular
 * band can be stored contiguously or all samples from a single pixel can
 * be stored contiguously. In the Java 2D(tm) API, all built-in image 
 * processing and display operators process samples which represent
 * unsigned integral values.
 *
 * A collection of pixels is represented as a Raster, which consists of a
 * DataBuffer and a SampleModel. The SampleModel allows access to samples 
 * in the DataBuffer and may provide low-level information that a programmer
 * can use to directly manipulate samples and pixels in the DataBuffer.
 *
 * This class is generally a fall back method for dealing with images. More
 * efficient code will cast the SampleModel to the appropriate subclass and
 * extract the information needed to directly manipulate pixels in the 
 * DataBuffer.
 *
 * There is an option useLogInputFlag to add Log(grayscale) to take scaled
 * log of grayscale data for maxBits>8-bits (e.g., 10-bits or 16-bits) for
 * TIFF images prior to reading them. Scale log((2**nBitsPerPixel)-1) to 255:
 *   f'(g) = 255*log(g+1)/log((2**nBitsPerPixel)-1).
 *
 * This requires two JAI jar files: jai_core.jar and jai_codec.jar
 * for TiffLoader.java to compile. These should be in the same class path
 * as the Flicker.jar file.
 *
 * @see http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/ 
 * @see http://java.sun.com/products/java-media/jai/current.html
 * See the SUN license file for JAI at these web sites.
 *</PRE>
 *
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government.  As a work of
 * the United States Government there is no associated copyright.  It is
 * offered as open source software under the Mozilla Public License
 * (version 1.1) subject to the limitations noted in the accompanying
 * LEGAL file. This notice must be included with the code. The Flicker 
 * Mozilla and Legal files are available on 
 * http://open2dprot.sourceforge.net/Flicker
 *<P>
 * @author  Jai Evans, CIT, NIH; Peter Lemkin, LECB/NCI, Frederick, MD
 * @version $Date$   $Revision$
 * @see <A HREF="http://open2dprot.sourceforge.net/Flicker">Flicker Home</A>
 * Original: Date: 8-13-2003, Jai Evans.
 */

public class TiffLoader
{
  
  /** Fixed value of true for DEBUGGING */
  private static boolean
    ALWAYS= true;
  /** Fixed value of false for DEBUGGING */
  private static boolean
    NEVER= false;
  
  /** debugging flag */
  private static boolean
    dbugFlag= false;
    
  /** instance # of TiffLoader */
  public static int 
    count= 0;
  
  /** filename path of image file */
  public String
    fileName= null;
  /** Tiff sample model name */
  public SampleModel
    sampleModel= null;
  /** List of Rendered images that was read in the tiff file */
  public RenderedImage
    ri[]= null;
  /** List of PixelAccessors from the RenderedImages' */
  public PixelAccessor
    pi[]= null;
  /** Raster image read */
  public Raster
    ras;
  /** Tiff Image Decoder */
  public ImageDecoder
    decoder= null;
  /* Color Model derived from the tiff Image */
  public ColorModel
    cm= null;
  
  /** # of rows in the image */
  public int
    nRows= 0;
  /** # of columns in the image */
  public int
    nCols= 0;
  /** X upper left hand corner of image origin */
  public int
    minX= 0;
  /** Y upper left hand corner of image origin */
  public int
    minY= 0;
  /** # of samples/pixel */
  public int
    nBands= 0;
  /** # of TIFF pages */
  public int
    numPages= 0;
  /** # raster DataBuffer data elements/pixel */
  public int
    numDataElements= 0;
    
  /** image of size nRows of nCols pixels.
   * This is either 8-bit, 16-bit, 24-bit RGB, etc.
   * If the data is 8-bits, 12-bits or 16-bits then the (R,G,B) channels
   * of the pixels[] array data are scaled to 8-bits and are set to be the
   * same.
   * [TODO] If the image is 24-bit (R,G,B) or an index-colormap 8-bit RGB, 
   * then set the (R,G,B) channels separately to preserve the colors.
   */
  private int
    pixels[]= null;
  
  /** Image structure required for Flicker */
  public Image
    img= null;
  
  /** data type from sampleModel.getDataType()
   * The types are defined as DataBuffer.TYPE_xxxx where xxxx
   * is (BYTE, DOUBLE, FLOAT, INT, SHORT, UNDEFINED, USHORT)
   */
  public int
    dataType= 0;
  /** bits/pixel is sample sizes [0:nBands-1] from 
   * sampleModel.getSampleSize()
   */
  public int
    sampleSizes[]= null;  
  
  /** set if pixel can be packed into a byte array */
  public boolean
    isPackedFlag= false;
  /** set if PackedColorModel */
  public boolean
    isPackedCMflag= false;
  /** set if has ComponentColorModel */
  public boolean
    isComponentCMflag= false;
  /** set if has ComponentSampleModel */
  public boolean
    isComponentSMflag= false;
  /** set if has a SinglePixelPackedSampleModel */
  public boolean
    isSinglePixelPackedSMflag= false;
  /** set if has a MultiplePixelPackedSampleModel */
  public boolean
    isMultiplePixelPackedSMflag= false;
  
    /** # bits/pixel is sampleModel.sampleSize[0] */
  public int
    nBitsPerPixel= 0;
  /** # of pixels/image (is nRows*nCols) */
  public int
    nPixels= 0;
  /** Black is Zero flag */
  public boolean
    blackIsZeroFlag= true;
  /** set if this is a RGB color image */
  public boolean
    isRGBcolorImageFlag= false;
  
  /** [FUTURE] grayscale to OD lookup table [0:maxGray-1] if supplied
   * as additional TIFF tags. Format specific....
   */
  public float
    gray2OD[]= null;
  /** maximum gray value */
  public int
    maxGray= 0;
  /** "Use log of pixels if > 8-bits grayscale image" */
  public boolean
    useLogInputFlag= false;
    
  /** Msg why failed if not null */
  public String
    fatalMsg= null;
    
  
  /**
   * TiffLoader() - constructor
   */
  public TiffLoader()
  {
    dbugFlag= false;           /* defaults to OFF */
  }
  
  
  /**
   * TiffLoader() - constructor where set the debug flag 
   */
  public TiffLoader(boolean flag)
  {
    dbugFlag= flag;
  }

  
  /**
   * setDbugFlag() - flag
   */
  public void setDbugFlag(boolean flag)
  { dbugFlag= flag; }
   
   
  /**
   * setUseLogFlag() - flag to set the log flag
   * This is used if the data has more than 8-bits of grayscale
   * so the data is transformed by the log over the range of
   * 255*log(10**nBits-1) to 0
   */
  public void setUseLogFlag(boolean flag)
  { useLogInputFlag= flag; }
  
  
  /**
   * readTiff() - read a tiff image file
   * @param fileName is the file to be read
   * @return image rendered raster instance that may be an array of images
   */
  public RenderedImage[] readTiff(String fileName) throws IOException
  { /* readTiff */
    this.fileName= fileName;
    
    try
    {
      File file= new File(fileName);
      SeekableStream ss= new FileSeekableStream(file);
      decoder= ImageCodec.createImageDecoder("tiff", ss, null);
      numPages= decoder.getNumPages();
      RenderedImage rImage[]= new RenderedImage[numPages];
      
      pi= new PixelAccessor[numPages];
      
      for(int i=0;i<numPages;i++)
      { /* get each of the rendered images */
        rImage[i]= decoder.decodeAsRenderedImage(i);
        /* Setup the pixel accessor */
        if(rImage[i]!=null)
          pi[i]= new PixelAccessor(rImage[i]);
      } /* get each of the rendered images */
      return(rImage);
    }
    
    catch(Exception e)
    {
      fatalMsg= e.toString();
      System.out.println("Problem decoding the TIFF image\n"+fatalMsg);
      return(null);
    }
  } /* readTiff */
  
  
  /**
   * getTiffPixels() - get pixels from the raster
   * @param r is the image raster instance
   * @param checkCacheFlag to see if pixels data exists, then return it
   * @return 1D array encoding a raster 2D image of size width X height.
   */
  public int[] getTiffPixels(Raster r, boolean checkCacheFlag)
  { /* getTiffPixels */        
    getTiffProperties(r);                 /* get it again */
    
    if(checkCacheFlag && pixels!=null)
      return(pixels);
   
    DataBuffer buffer= r.getDataBuffer();
     
    int
      nCols= r.getWidth(),
      nRows= r.getHeight(),
      minX= r.getMinX(),
      minY= r.getMinY(),
      pixels[]= null;
    pixels= r.getPixels(minX, minY, nCols, nRows, pixels);        
    
    return(pixels);
  } /* getTiffPixels */
     
  
  /**
   * getTiffProperties() - get properties of the Tiff image
   * @param r is the image raster instance
   */
  public void getTiffProperties(Raster r)
  { /* getTiffProperties */  
    this.nCols= r.getWidth();
    this.nRows= r.getHeight();
    this.minX= r.getMinX();
    this.minY= r.getMinY();
    this.nBands= r.getNumBands();
    this.numDataElements= r.getNumDataElements();
    
    this.sampleModel= r.getSampleModel();
    this.dataType= sampleModel.getDataType();    
    this.sampleSizes= sampleModel.getSampleSize();
    this.nBitsPerPixel= sampleSizes[0];
        
    this.isPackedFlag= pi[0].isPacked;
    this.isPackedCMflag= pi[0].isPackedCM;
    this.isComponentCMflag= pi[0].isComponentCM;
    this.isComponentSMflag= pi[0].isComponentSM;
    this.isSinglePixelPackedSMflag= pi[0].isSinglePixelPackedSM;
    this.isMultiplePixelPackedSMflag= pi[0].isMultiPixelPackedSM;
    
    this.nPixels= nRows*nCols;    
  } /* getTiffProperties */
  
    
  /**
   * getImage() - get pixels from the raster
   * @param r is the image raster instance
   * @return 1D array encoding a raster 2D image of size width X height.
   */
  public Image getImage()
  { /* getImage */
    return(img);
  } /* getImage */
      

  /**
   * doTiffLoad() - load the image and array grab of TIFF files
   * @param path of the image
   * @param checkCacheFlag to see if pixels data exists, then return it
   * @return null if succeed, else error message
   */
  public String doTiffLoad(String path, boolean checkCacheFlag)
  { /* doTiffLoad */
    count++;
    if(dbugFlag)
      System.out.println("---doTiffLoad ["+count+"]---\n");
      
    try
    {
      ri= readTiff(path);
      if(ri==null)
        return(fatalMsg);         /* problems ... */
      
      ras= ri[0].getData();
      getTiffProperties(ras);
            
      int
        g,
        alpha= (255<<24);  /* alpha is 100% on */
      boolean isGrayscaleFlag;
      
      if(ALWAYS && nBands==3)
      { /* 3 bands or 8-bits/band Blue, green, red channels */
        pixels= new int[nPixels];      
        int
          idx= 0,
          blue,
          green,
          red;
        Rectangle rect= new Rectangle(nCols,nRows);
        UnpackedImageData uid= pi[0].getComponentsRGB(ras,rect);
        int type= uid.type;
        
        /* Unpack it from byte arrays */
        byte
          rbgB[][]= uid.getByteData(),
          redB[]= rbgB[0],
          greenB[]= rbgB[1],
          blueB[]= rbgB[2],
          alphaB[]= rbgB[3];
        for(int i=0;i<nPixels;i++)
        {  /* build standard packed 32-bit ARGB pixels */
          blue= (blueB[i] & 0Xff);
          green= (greenB[i] & 0Xff);
          red= (redB[i] & 0Xff);
          //alpha= ((alphaB[i] & 0Xff) << 24);
          g= ((red<<16) | (green<<8) | blue);
          pixels[i]= (alpha | g);
        }
                        
        if(NEVER)
        {          
          int nColorPixels= getNbrRGBpixels(pixels, nPixels);
          boolean isColorImgFlag= (nColorPixels>0);
          if(dbugFlag)
            System.out.println("TL-DTL isColorImgFlag="+isColorImgFlag+
                               " nColorPixels="+nColorPixels+
                               " %colorPixels="+((100*nColorPixels)/nPixels)+
                               "\n  nPixels="+nPixels+
                               " pixels.length="+pixels.length);
        }
      } /* 3 bands or 8-bits/band Blue, green, red channels */
                  
      else if(nBands==1 && nBitsPerPixel==8)
      { /* single channel 8 bit - make a  3 channels the same*/
        ras= ri[0].getData();
        pixels= getTiffPixels(ras, checkCacheFlag); /* just one channel */ 
        for(int i=0;i<nPixels;i++)
        {
          g= (pixels[i] & 255);
          pixels[i]= (alpha | (g<<16) | (g<<8) | g);
        }
      } /* single channel 8 bit - make a  3 channels the same*/
      
      else if(nBands==1 && nBitsPerPixel>8)
      { /* single channel > 8 bits - scale copy to 8-bits/channels the same*/
        /* There is an option useLogInputFlag to add Log(grayscale) to take
         * scaled log of grayscale data for maxBits>8-bits (e.g., 10-bits or 
         * 16-bits) for TIFF  images prior to reading them. 
         * Scale log((2**maxBitesPerPixel)-1) to 255:
         *   f'(g) = 255*log(g+1)/log((2**nBitsPerPixel)-1).
         */
        int pixelMask= (1<<nBitsPerPixel)-1;
        double 
          log255= 255.0/Math.log(pixelMask),
          logG,
          fG;
        ras= ri[0].getData();
        pixels= getTiffPixels(ras, checkCacheFlag); /* just one channel */ 
        int gH, g8;
        for(int i=0;i<nPixels;i++)
        {
          gH= (pixels[i] & pixelMask);
          if(useLogInputFlag)
          { /* Scale to 8-bits linearly */
            g8= (gH >> (nBitsPerPixel-8));
            g= (g8 & 255);
          }
          else
          { /* use log transform  */
            logG= Math.log(gH+1);
            fG= log255*logG;
            g= (int)fG;
            if(g<0)
              g= 0;
            else if(g>255)
              g= 255;
          }
          pixels[i]= (alpha | (g<<16) | (g<<8) | g);
        }
      } /* single channel > 8 bits - scale copy to 8-bits/channels the same*/
            
      /* Construct the Image form a 24-bit+alpha (R,G,B) pixel array */
      Toolkit dtk= Toolkit.getDefaultToolkit();
      img= dtk.createImage(new MemoryImageSource(nCols, nRows, 
                                                 pixels, 0, nCols));
      pixels= null;                /* set this so can be G.C.ed */
      
      if(dbugFlag)
        System.out.println(this.toString());
      System.gc();
      return(null);
    }
    catch (Exception e)
    {
      fatalMsg= e.toString();
      img= null;
      if(dbugFlag)
        e.printStackTrace();
      return(fatalMsg);
    }    
  } /* doTiffLoad */
  
  
  /**
   * getNbrRGBpixels() - test if red==green==blue for all pixels
   * @return # of color pixels. 0 means gray value image
   */
  public int getNbrRGBpixels(int pixels[], int nPixels)
  { /* getNbrRGBpixels */
    int
      nColorPixels= 0,
      g,
      blue,
      green,
      red;
    for(int i=0;i<nPixels;i++)
    {
      g= pixels[i];
      blue= (g & 0Xff);
      green= (g & 0Xff00);
      red=  (g & 0Xff0000);
      if(red!=green || green!=blue)
      {
        nColorPixels++;
      }
    }
    
    return(nColorPixels);
  } /* getNbrRGBpixels */
  
  
  /**
   * toString() - return string representation of this instance
   * @return string instance
   */
  public String toString()
  { /* toString */
    String sR= "TiffLoader instance count="+count;
    sR += "\nfileName="+fileName;
    sR += "\nCols="+nCols+" Rows="+nRows;
    sR += "\nminX="+minX+" minY="+minY;
    sR += "\nnumDataElements="+numDataElements;
    
    sR += "\nisPackedFlag="+isPackedFlag;
    sR += "\nisPackedCMflag="+isPackedCMflag;
    sR += "\nisComponentCMflag="+isComponentCMflag;
    sR += "\nisComponentSMflag="+isComponentSMflag;
    sR += "\nisSinglePixelPackedSMflag="+isSinglePixelPackedSMflag;
    sR += "\nisMultiplePixelPackedSMflag="+isMultiplePixelPackedSMflag;
        
    sR += "\nisRGBcolorImageFlag="+isRGBcolorImageFlag;
    
    sR += "\nnBands="+nBands+" nBitsPerPixel="+nBitsPerPixel;
    sR += "\npixels.length="+
          ((pixels==null) ? "pixels is null" : (""+pixels.length));
    sR += "\nTiff number pages=" + numPages;
    sR += "\nsampleModel.dataType="+
           ((sampleModel==null)
                ? "sampleModel is null": (""+dataType));
    for(int i=0;i<nBands;i++)
      sR += "\nsampleModel.sampleSize["+i+"]="+
            sampleModel.getSampleSize(i);
    sR += "\n";
    
    return(sR);
  } /* toString */
  
  
  /**
   * main() - test loader.
   * Replace hard code here with local path for testing
   */
  public static void main(String [] args)
  { /* main */
    TiffLoader ti= new TiffLoader(true);  /* set debug flag to debug */
    
    if(args.length <1)
    {
      /*
      ti.doTiffLoad("C:\\fSrc\\TiffLoader\\Lym-2Dgel-bw.tif",false);
      ti.doTiffLoad("C:\\fSrc\\TiffLoader\\LEC-2Dgel-bw.tif",false);
      ti.doTiffLoad("C:\\fSrc\\TiffLoader\\pERK-bw.tif",false);
      ti.doTiffLoad("C:\\fSrc\\TiffLoader\\pMAR-bw.tif",false);
     
      ti.doTiffLoad("C:\\fSrc\\TiffLoader\\Penneyfield-lock-house.TIF",false);
      */
      ti.doTiffLoad("C:\\fSrc\\TiffLoader\\pumpkin-RGB-NotSepChan.tif",false);
      ti.doTiffLoad("C:\\fSrc\\TiffLoader\\pumpkin-RGB-SepChan.tif",false);
      ti.doTiffLoad("C:\\fSrc\\TiffLoader\\SRR-color.tif",false);
    }
    else
      for (int i=0; i<args.length; i++)
        ti.doTiffLoad(args[i],false);
  }  /* main*/
  
} /* End of class TiffLoader*/


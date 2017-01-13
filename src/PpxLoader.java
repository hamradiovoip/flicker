/* File: PpxLoader.java */

import java.awt.*;
import java.awt.image.*;
import java.awt.image.MemoryImageSource;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;
import java.io.FileReader;

/* Class PpxLoader is used to read PPX portable picture format .ppx images 
 * used by GELLAB-II. Note: you don't get the pixels from this class. 
 * Instead you get the Image instance using ppxLoader.getImage() and then
 * get the pixels from that instance.  Note that although we read all of
 * the PPX file fields, most are not used.
 *<P>
 * This file is derived from GELLAB-II file ppxfmt.h and libr/libppx.c
 * Gellab was first described in Lipkin L.E, Lemkin P.F. (1980) 
 * Database techniques for multiple two-dimensional polyacrylamide gel 
 * electrophoresis analyses. Clinical Chemistry 26, 1403-1412.
 * See http://www.lecb.ncifcrf.gov/gellab for more info.
 *<P>
 * This work was produced by Peter Lemkin of the National Cancer
 * Institute, an agency of the United States Government.  As a work of
 * the United States Government there is no associated copyright.  It is
 * offered as open source software under the Mozilla Public License
 * (version 1.1) subject to the limitations noted in the accompanying
 * LEGAL file. This notice must be included with the code. The open2dprot 
 * Mozilla and Legal files are available on http://open2dprot.sourceforge.net/.
 *<P>
 * @author P. Lemkin, NCI-Frederick, Frederick, MD, 21702
 * @version $Date$   $Revision$
*/

public class PpxLoader
{

  /* The image file header is a single block of 512 bytes [in VAX byte order],
   * which is followed by an uncompressed raster byte image data.
   */
  
  /** full path of PPX input file */
  public String
    ppxFileName;
  
  /*  ============================================================== */
  /*          PPX file header                                        */
  /*  ============================================================== */
  
  /* user setable parameters if the header changes */
  /** Code version 3.5 as 35 */
  public static int
    PPX_VRSION= 35;
  /** Max number of wedge steps if applicable */
  public static int
    WEDGE_PPX= 24;

  /** (unsigned short) Set to PPX_VRSION by program creating header */
  public int 
    fversn;
    
  /** type of file.
   *  = 0 for unknown.
   *  = 1 for raw image.
   *  = 2 for processed image.
   *  = 3 for synthetic image.
   *  = 4 for spot file.
   *  = 5 for exception list file.
   *  = ... 6 to 255 are free.
   */
  public int 
    filtyp;
  /** (unsigned short) full image size # rows in pixels */
  public int 
    nrows; 
  /** (unsigned short) full image size # columns in pixels */
  public int
    ncols; 
  
  /** name of the picture - was [16] */
  public String
    name;
  /** further identification - (sample ID) */
  public String 
    sid;             
  /** visualization method */
  public String 
    vism;            
  /** date of scan */
  public String 
    sdate;           
  /** time of scan */
  public String
    stime;            
  /** initials of person doing scan */
  public String 
    initl;            
  /** ID of the scanning system */
  public String
    scsys;           
  /** name of scanning program */
  public String
    scprog;          
  /** version of scanning program */
  public String
    scpvrs;           
  
  /** # of scanning bands (colors) */
  public int
    nbands= 0;  
  /** bits per pixel (one band) */
  public int 
    bitpp;  
  /** bytes per pixel (one band) */
  public int 
    bytpp;       
  
  /** (unsigned short) top left X corner of image in cm*1024 */
  public int
    x0;
  /** (unsigned short) top left Y corner of image in cm*1024 */
  public int 
    y0; 
  
  /** (unsigned short) s.s.d in microns*1024 */
  public int 
    isptsz;     
  
  /** (unsigned short) X step sizes in microns*1024 */
  public int
    istpx;
  /** (unsigned short) Y step sizes in microns*1024 */
  public int 
    istpy;      
  /** (unsigned int) tesselation code ??? bits??? */
  public int
    tessl;      
  
  /** 0 = optical density,
   * 1 = transmittance
   * 2 = gray value
   * ... 3 to 255 are free.
   */
  public int 
    domain;      
  
  /** (unsigned short) X top left corner of region
   * of interest (0,0) is U.L.H.C. */
  public int
    rix;   
  /** (unsigned short) X top left corner of region
   * of interest (0,0) is U.L.H.C. */
  public int 
    riy;   
  
  /** (unsigned short) X size of region of interest */
  public int 
    nx;  
  /** (unsigned short) Y size of region of interest */
  public int 
    ny;    
  /** block alignment flag 0 = aligned) */
  public boolean
    blkaln; 
  /** translation flag (0 = raw, 1 = done) */
  public boolean
    trnsl;  
  /** (unsigned int)  number of exception entries */
  public int 
    nexcpt;     
  /** Min Pixel value. (float odmn) Min optical densities */
  public int 
    odmn;
  /** Max Pixel value. (float odmn) Max optical densities  */
  public int 
    odmx;  
  
  /* ----------- NOTE: 11-12-87 PFL new fields ----------------------- */
  /** 1 if BLACK is gray value 0, else 0 if WHITE is gray value 0 */
  public boolean
    blackIsZeroFlag; 
 
  /** Type of step wedge if any in the image:
   * 0 = none
   * 1 = ND, neutral density
   * 2 = CPM, counts per minute
   * 3 = DPM, disintegrations per minute
   * 4  to 255 = <free>
   */
  public int 
    wedgeType;    
  
  /** will contain WEDGE_PPX to be set by user*/ 
  public int
    nWedgeSteps= WEDGE_PPX; 
  
  /** (24-bit unsigned) ND*1024 or CPM wedge step values*/
  public int
    wedgeVal[]= new int[WEDGE_PPX];
  
  /** (16-bit unsigned) gray scale peaks calibration corresponding to
   * step wedge values.
   */
  public int
    grayCalWedge[]= new int [WEDGE_PPX]; 
  
  /** ND (od) or CPM wedge step values*/
  public float
    genNdwvalues[]= new float[WEDGE_PPX]; 
  
  /** color map flag.
   * 0 = no color map
   * 1 = color map is the first 1024
   *     bytes of data after header
   *     where 3x256 bytes are Red,
   *     Green,Blue maps stored as
   *     sequential arrays.
   * 2 = same as 1 but image data is
   *     true-color image consisting of
   *     three sequential images (R,G,B).
   * 3 = same as 2 but pixels are 24-bits
   *     specifying 8-bits each of (R,G,B).
   * 4 = color map is first 12,298 bytes
   *     of data after the header where
   *     3x4096 (12-bit lookup table entries)
   *     are stored as sequential arrays.
   */
  public int 
    cMapMode;    
  
  /** Image orientation bits
   * where: Default 00==>011:
   * 01 = left to right
   * 02 = bottom to top
   * 04 = right to left
   * 010= top to bottom
   */
  public int 
    imOrientation;   
  
  /** Image encoding method
   * 0 = none (just raw raster data)
   * 1 = UNIX 'compress/uncompress'
   * 2 = run length
   * 3 = 1D modified Huffman
   * 4 = 2D modified Huffman
   * 5 = delta coding
   * 6 = run length with delta coding
   * ... 7 to 255 are free.
   */
  public int 
    imEncode;   
  
  /** (unsigned int) if non-zero, then byte # of file
   * where data dictionary starts. This
   * should be multiple of 512 bytes.
   */
  public int 
    startDataDict;  
  
  /** (unsigned int) if non-zero, then byte # of file
   * where data dictionary ends. This
   * should be multiple of 512 bytes -1.
   */
  public int
    endDataDict;  
  
  /** (unsigned int) if non-zero, then byte # of file
   * where image data starts. This
   * should be multiple of 512 bytes.
   */
  public int 
    startImageData; 
  
  /* ---------  new Fields 9/14/2003 ------ */
  
  /** pixels [0:nPixels-1] for the image */
  private byte
    bPixels[]= null;
  /** pixels [0:nPixels-1] for the image */
  private int
    pixels[]= null;
  
  /** # of pixels for the image is (nrows*ncols) */
  public int
    nPixels= 0;
  
  /** Image structure required for Flicker */
  public Image
    img= null;
  /* Color Model for 8-bit grayscale Image */
  public ColorModel
    cm= null;
  
  
  /**
   * PpxLoader() - constuctor()
   */
  public PpxLoader()
  {
  }
  
  
  /**
   * PpxLoader() - constuctor to parse the PPX header and set up the state
   * @param byteData is 512 byte header read from PPX file
   */
  public PpxLoader(byte[] byteData)
  {
    cvP2Ihdr(byteData);
  }  
  
  
  /* -------- Test Byte conversion ------------------  
  for(i=0;i<=255;i++)
  {
    byte b= cvInt2Byte(i);
    int j= cvByte2Int(b);
    System.out.println("cvInt2Byte("+i+")="+b+" cvByte2Int("+b+")="+j+
                        " (j==i)="+(j==i));
  }
  */
  
  /**
   * cvByte2Int() - convert unsigned byte [0:255] to int in range [0:255]
   * @param b is byte to convert
   * @return int value 
   */
  final private int cvByte2Int(byte b)
  { /* cvByte2Int */ 
    int i= (b>=0) ? b : (128-b-1);
    return(i);
    
    /* TEST the byte conversion around boundary conditions for signed byte.
    System.out.println("cvByte2Int(0)="+cvByte2Int((byte)0));
    System.out.println("cvByte2Int(1)="+cvByte2Int((byte)1));
    System.out.println("cvByte2Int(127)="+cvByte2Int((byte)127));
    System.out.println("cvByte2Int(128)="+cvByte2Int((byte)128));
    System.out.println("cvByte2Int(-1)="+cvByte2Int((byte)-1));
    System.out.println("cvByte2Int(-126)="+cvByte2Int((byte)-126));
    System.out.println("cvByte2Int(-127)="+cvByte2Int((byte)-127));
    */
  } /* cvByte2Int */  
   
    
  /**
   * cvInt2Byte() - convert unsignged int [0:255] to signed byte [-128:+127]
   * @param i is int to convert
   * @return
   */
  final private byte cvInt2Byte(int i)
  { /* cvInt2Byte */
    byte b= (i<128) ? (byte)i : (byte)(128-i-1);
    return(b);
    
    /* TEST the byte conversion around boundary conditions for signed byte.
    System.out.println("cvInt2Byte(0)="+cvInt2Byte((int)0));
    System.out.println("cvInt2Byte(1)="+cvInt2Byte((int)1));
    System.out.println("cvInt2Byte(127)="+cvInt2Byte((int)127));
    System.out.println("cvInt2Byte(128)="+cvInt2Byte((int)128));
    System.out.println("cvInt2Byte(129)="+cvInt2Byte((int)129));
    System.out.println("cvInt2Byte(254)="+cvInt2Byte((int)254));
    System.out.println("cvInt2Byte(255)="+cvInt2Byte((int)255));
    */
  } /* cvInt2Byte */
    
  
  /**
   * cvP2Ihdr() - Convert portable header byteData[0:511] into PpxLoader state.
   * @param byteData is 512 byte header read from PPX file
   * @return true if valid header
   */
  public boolean cvP2Ihdr(byte byteData[])
  { /* cvP2Ihdr */
    int 
      n= 0,
      i;    
    
    fversn = cvByte2Int(byteData[n++]) << 8;
    fversn |= cvByte2Int(byteData[n++]);         /* unsigned 16 */
    
    filtyp = cvByte2Int(byteData[n++]);          /* unsigned 8 */
    
    nrows =  cvByte2Int(byteData[n++]) << 8;     /* unsigned 16 */
    nrows |= cvByte2Int(byteData[n++]); 
    
    ncols =  cvByte2Int(byteData[n++]) << 8;     /* unsigned 16 */
    ncols |= cvByte2Int(byteData[n++]); 
    
    nPixels= (nrows*ncols);
    
    char cBuf[]= new char[33];
    for(i=0;i<32;i++)                            /* char name[32] */
      cBuf[i] = (char)byteData[n++];
    name= new String(cBuf);
    
    cBuf= new char[13];
    for(i=0;i<12;i++)                            /* char sid[12] */
      cBuf[i] = (char)byteData[n++];
    sid= new String(cBuf);
    
    cBuf= new char[13];
    for(i=0;i<12;i++)                            /* char vism[12] */
      cBuf[i] = (char)byteData[n++];
    vism= new String(cBuf);
    
    cBuf= new char[9];    
    for(i=0;i<8;i++)                             /* char sdate[8] */
      cBuf[i] = (char)byteData[n++];
    sdate= new String(cBuf);
    
    cBuf= new char[9];    
    for(i=0;i<8;i++)                             /* char stime[8] */
      cBuf[i] = (char)byteData[n++];
    stime= new String(cBuf);
    
    cBuf= new char[5];    
    for(i=0;i<4;i++)                             /* char initl[4] */
      cBuf[i] = (char)byteData[n++];
    initl= new String(cBuf);
    
    cBuf= new char[21];
    for(i=0;i<20;i++)                            /* char scsys[20] */
      cBuf[i] = (char)byteData[n++];
    scsys= new String(cBuf);
    
    cBuf= new char[13];    
    for(i=0;i<12;i++)                            /* char scprog[12] */
      cBuf[i] = (char)byteData[n++];
    scprog= new String(cBuf);
    
    cBuf= new char[4];    
    for(i=0;i<4;i++)                             /* char scpvrs[4] */
      cBuf[i] = (char)byteData[n++];
    scpvrs= new String(cBuf);
    
    nbands = cvByte2Int(byteData[n++]);          /* unsigned  8 */
    
    bitpp = cvByte2Int(byteData[n++]);           /* unsigned  8 */
    bytpp = cvByte2Int(byteData[n++]);           /* unsigned  8 */
    
    x0 =  cvByte2Int(byteData[n++]) << 8;
    x0 |= cvByte2Int(byteData[n++]);             /* unsigned 16 */
    y0 =  cvByte2Int(byteData[n++]) << 8;
    y0 |= cvByte2Int(byteData[n++]);             /* unsigned 16 */
    
    isptsz =  cvByte2Int(byteData[n++]) << 8;
    isptsz |= cvByte2Int(byteData[n++]);         /* unsigned 16 */
    istpx =  cvByte2Int(byteData[n++]) << 8;
    istpx |= cvByte2Int(byteData[n++]);          /* unsigned 16 */
    istpy =  cvByte2Int(byteData[n++]) << 8;
    istpy |= cvByte2Int(byteData[n++]);          /* unsigned 16 */
    
    tessl =  cvByte2Int(byteData[n++]) << 24;
    tessl |= cvByte2Int(byteData[n++]) << 16;
    tessl |= cvByte2Int(byteData[n++]) << 8;
    tessl |= cvByte2Int(byteData[n++]);          /* unsigned 32 */
    
    domain = cvByte2Int(byteData[n++]);          /* unsigned 8 */
    
    rix =  cvByte2Int(byteData[n++]) << 8;
    rix |= cvByte2Int(byteData[n++]);            /* unsigned 16 */
    riy =  cvByte2Int(byteData[n++]) << 8;
    riy |= cvByte2Int(byteData[n++]);            /* unsigned 16 */
    
    nx =  cvByte2Int(byteData[n++]) << 8;
    nx |= cvByte2Int(byteData[n++]);             /* unsigned 16 */
    ny =  cvByte2Int(byteData[n++]) << 8;
    ny |= cvByte2Int(byteData[n++]);             /* unsigned 16 */
    
    blkaln = (cvByte2Int(byteData[n++])==0);     /* unsigned 8 */
    trnsl = (cvByte2Int(byteData[n++])==0);      /* unsigned 8 */
    
    nexcpt =  cvByte2Int(byteData[n++]) << 24;
    nexcpt |= cvByte2Int(byteData[n++]) << 16;
    nexcpt |= cvByte2Int(byteData[n++]) << 8;
    nexcpt |= cvByte2Int(byteData[n++]);         /* unsigned 32 */
    
    odmn =  cvByte2Int(byteData[n++]) << 8;
    odmn |= cvByte2Int(byteData[n++]);           /* unsigned 16 */
    odmx =  cvByte2Int(byteData[n++]) << 8;
    odmx |= cvByte2Int(byteData[n++]);           /* unsigned 16 */
    
    blackIsZeroFlag= (cvByte2Int(byteData[n++])!=0); /* unsigned 8 */
    blackIsZeroFlag= false; /* unsigned 8 */
        
    /* NOTE: wedge calibrations in PPX files may be bogus since
     * GELLAB-II usually gets the calibrations from the accession gel.id
     * file!!!
     */    
    wedgeType = cvByte2Int(byteData[n++]);       /* unsigned 8 */
    nWedgeSteps = cvByte2Int(byteData[n++]);     /* unsigned 8 */
    nWedgeSteps= 0;                    /* recompute it */
    int wG, wCal;
    for(i=0;i<WEDGE_PPX;i++)
    { /* Do all of Wedge */
      /* Wedge value data is 3-bytes, unsigned var : 24 */
      wCal=  cvByte2Int(byteData[n++]) << 16;
      wCal |= cvByte2Int(byteData[n++]) << 8;
      wCal |= cvByte2Int(byteData[n++]);
      
      /* Grayscale calibration data is 2-bytes, unsigned var : 16 */
      wG=  cvByte2Int(byteData[n++]) << 8;
      grayCalWedge[i] |= cvByte2Int(byteData[n++]);
      
      if(i>0 && wCal==0 && wG==0)
        break;                     /* no more data */
      /* save entry in the list */
      wedgeVal[i]= wCal;
      grayCalWedge[i]= wG;
      genNdwvalues[i]= wCal*1024.0F;      
      nWedgeSteps++;
    } /* Do all of Wedge */
    
    cMapMode = cvByte2Int(byteData[n++]);         /* unsigned 8 */
    
    imOrientation = cvByte2Int(byteData[n++]);    /* unsigned 8 */
    
    imEncode = cvByte2Int(byteData[n++]);         /* unsigned 8 */
    
    startDataDict =  cvByte2Int(byteData[n++]) << 24;
    startDataDict |= cvByte2Int(byteData[n++]) << 16;
    startDataDict |= cvByte2Int(byteData[n++]) << 8;
    startDataDict |= cvByte2Int(byteData[n++]);   /* unsigned 32 */
    
    endDataDict =  cvByte2Int(byteData[n++]) << 24;
    endDataDict |= cvByte2Int(byteData[n++]) << 16;
    endDataDict |= cvByte2Int(byteData[n++]) << 8;
    endDataDict |= cvByte2Int(byteData[n++]);     /* unsigned 32 */
    
    startImageData =  cvByte2Int(byteData[n++]) << 24;
    startImageData |= cvByte2Int(byteData[n++]) << 16;
    startImageData |= cvByte2Int(byteData[n++]) << 8;
    startImageData |= cvByte2Int(byteData[n++]);  /* unsigned 32 */
    
    /* [TODO] add validation code */
    return(true);
  } /* cvP2Ihdr */
  
  
  /**
   * getImage() - get pixels from the raster
   * @param r is the image raster instance
   * @return 1D array encoding a raster 2D image of size width X height.
   */
  public Image getImage()
  { /* getImage */
    if(img==null)
      makePPXimage();  /* make img and pixels[] from byte bPixels[] data */
    return(img);
  } /* getImage */
  
    
  /**
   * readPPXfile() - read loca PPX file or PPX file URL.
   * @param ppxFileName is the full path file name to be read
   * @return true if succeed and the data is in the class instance.
   */
  boolean readPPXfile(String ppxFileName)
  { /* readPPXfile*/    
    this.ppxFileName= ppxFileName;
    
    try
    { /* read data and set up the state and the pixels[] */
      int
        bufSize= 100000,
        nBytesRead= 0,
        nBytesWritten= 0,
        nTotBytesRead= 0;
      byte
        hdrBuf[]= new byte[512],
        buf[]= new byte[bufSize];
      
      boolean isURL= (ppxFileName.indexOf("://")>0);
      if(isURL)
      { /* read the file from Web site */
        URL url= new URL(ppxFileName);
        InputStream urlIS= url.openStream();
        
        /* Read the header */
        nBytesRead= urlIS.read(hdrBuf, 0, 512);
        nTotBytesRead= nBytesRead;
        if(nBytesRead!=512)
          return(false);                       /* bad data */
        
        /* Parse the header and set the nPixels size to
         * allocate bPixels[] array 
         */
        if(!cvP2Ihdr(hdrBuf))
          return(false);
        
        /* Allocate the bPixels[] array */
        this.bPixels= new byte[this.nPixels];
        
        /* Read the bPixels[] array */
        while(true)
        { /* read-write loop */
          nBytesRead= urlIS.read(buf);
          nTotBytesRead += nBytesRead;
          if(nBytesRead==-1)
            break;                           /* end of data */
          else
          { /* append buf[] data to pixels[] */
            System.arraycopy(buf, 0, this.bPixels, nBytesWritten,
                             nBytesRead);
            nBytesWritten += nBytesRead;
          }
        } /* read-write loop */
      } /* read the file from Web site */
      
      else
      { /* read the file on the local file system */
        FileInputStream srcFIS= new FileInputStream(new File(ppxFileName));
        
        /* Read the header */
        nBytesRead= srcFIS.read(hdrBuf, 0, 512);
        nTotBytesRead= nBytesRead;
        if(nBytesRead!=512)
          return(false);                       /* bad data */
        
        /* Parse the header and set the nPixels size to
         * allocate bPixels[] array 
         */
        if(!cvP2Ihdr(hdrBuf))
          return(false);
        
        /* Allocate the pixels[] array */
        this.bPixels= new byte[this.nPixels];
        
        /* Read the bPixels[] array */
        while(true)
        { /* read-write loop */
          nBytesRead= srcFIS.read(buf);
          nTotBytesRead += nBytesRead;
          if(nBytesRead==-1)
            break;                             /* end of data */
          else
          { /* append buf[] data to bPixels[] */
            System.arraycopy(buf, 0, this.bPixels, nBytesWritten,
                             nBytesRead);
            nBytesWritten += nBytesRead;
          }
        } /* read-write loop */
        srcFIS.close();
      } /* read the file on the local file system */
    } /* read data and set up the state and the pixels[] */
    
    catch(Exception e)
    { /* just fail if any problems at all! */
      e.printStackTrace();
      return(false);
    }
    
    return(true);
  } /* readPPXfile*/
  
  
  /**
   * makePPXimage() - make img and pixels[] from the byte bPixels[] data
   * @return true if generate the Image instance and colorModel
   */
  public boolean makePPXimage()
  { /* makePPXimage */
    int
      alpha= (255<<24),
      g;
    
    /* allocate pixel array for making the image where black is 0. */
    pixels= new int[this.nPixels];
        
    for(int i=0;i<nPixels;i++)
    { /* create RGB cm with 0 being black & force bPixels to 0 being black*/
      g= (!blackIsZeroFlag)
            ? (255 - bPixels[i]) : bPixels[i]; 
      bPixels[i]= (byte)g;
      pixels[i]= (alpha | (g<<16) | (g<<8) | g);
    }
    
    bPixels= null;       /* Can Gabarge collect since no longer needed */
    
    /* Construct the color mode from Tiff image data */
    //cm= Toolkit.getDefaultToolkit().getColorModel();
    cm= ColorModel.getRGBdefault();
    
    /* Construct the Image */
    Toolkit dtk= Toolkit.getDefaultToolkit();
    img= dtk.createImage(new MemoryImageSource(ncols,nrows,cm,pixels,
                                               0,ncols));
    pixels= null;                /* no longer needed */
    
    return(true);
  } /* makePPXimage */
    
  
  /**
   * toString() - return string representation of this PpxLoader instance
   * @return string PpxLoader instance
   */
  public String toString()
  { /* toString */
    String sR= "PpxLoader instance";
    sR += "\n ppxFileName="+ppxFileName;
    sR += "\n name="+name;
    sR += "\n sid="+sid;
    sR += "\n vism="+vism;
    sR += "\n sdate="+sdate;
    sR += "\n stime="+stime;
    sR += "\n initl="+initl;
    sR += "\n scsys="+scsys;
    sR += "\n scpvrs="+scpvrs;
    sR += "\n scprog="+scprog;
  
    sR += "\n cols="+ncols+" nrows="+nrows+" nPixels="+nPixels;
    sR += "\n bPixels.length="+
          ((bPixels==null) ? "bPixels is null" : (""+bPixels.length));
    sR += "\n pixels.length="+
          ((pixels==null) ? "pixels is null" : (""+pixels.length));
          
    sR += "\n x0="+x0+" y0="+y0;
    sR += "\n filtyp=" + filtyp+" cMapMode="+cMapMode+" imEncode="+imEncode;
    sR += "\n nbands="+nbands+" bitpp="+bitpp+" bytpp="+bytpp;
    sR += "\n isptsz="+isptsz+" domain="+domain+
          " imOrientation="+imOrientation;
    sR += "\n istpx="+istpx+" istpy="+istpy;
    sR += "\n rix="+rix+" riy="+riy+" nx="+nx+" ny="+ny;
    sR += "\n blackIsZeroFlag="+blackIsZeroFlag;
    sR += "\n odmn="+odmn+" odmx="+odmx;
    /*
    sR += "\n nWedgeSteps="+nWedgeSteps+" wedgeType="+wedgeType;
    for(int i=0;i<nWedgeSteps;i++)
      sR += "\n  wedgeVal["+i+"]="+ wedgeVal[i]+"(gray)"+
             " grayCalWedge["+i+"]="+ grayCalWedge[i]+"(1024*od)"+
             " genNdwvalues["+i+"]="+ genNdwvalues[i]+"(od)";
     **/
    sR += "\n";
    
    return(sR);
  } /* toString */
  
} /* End of PpxLoader.java */

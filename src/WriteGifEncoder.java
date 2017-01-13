/* File: WriteGifEncoder */

import java.awt.*;
import java.util.*;
import java.io.*;
import java.awt.Image;
import java.awt.image.*;

/**
 * The WriteGifEncoder class writes out an Image object as a named GIF file.
 * After creating the class, use writeFile() to actually write the file.
 * <P>
 * This module was taken from NIH ImageJ and was in turn derived from 
 * GifEncoder.java and MedianCut.java. It writes out an Image as a GIF file 
 * after convering 24-bit RGB to an 8-bit IndexColorModel. It was taken from a 
 * public domain version of Wayne Rasband's ImageJ source code 
 * available at http://rsb.info.nih.gov/nih-image/
 *<PRE>
 * Transparency handling and variable bit size courtesy of Jack Palevich.
 *
 * Copyright (C) 1996 by Jef Poskanzer <jef@acme.com>.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Visit the ACME Labs Java page for up-to-date versions of this and other
 * fine Java utilities at http://www.acme.com/java/
 *</PRE>
 * This code was then modified by P. Lemkin, G. Thornwall, NCI/FCRDC.
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
 *
 *<P>
 * This work was derived from MAExplorer under the Mozilla 1.1 Open Source
 * Public License by Peter Lemkin of the National Cancer Institute, an
 * agency of the United States Government subject to the limitations noted
 * in the accompanying LEGAL file. See licence info on
 * http://maexplorer.sourceforge.org/
 */
 
public class WriteGifEncoder
  {
    private static final int
      EOF= -1,
      BITS= 12,
      HSIZE= 5003;	           	/** 80% occupancy */
  
    private boolean 
      ok= false,                /** was successful in writing file*/
      interlace= false,
      Interlace,
      clear_flg= false;
      
    private int
      htab[]= new int[HSIZE],
      codetab[]= new int[HSIZE];
      
    private int 
      a_count,                  /** # of chars so far in this 'packet' */
      hsize= HSIZE,	          	/** for dynamic table sizing */
      free_ent= 0,	            /** first unused entry */
      g_init_bits,
      ClearCode,
      EOFCode,
      width, height,
      Width, Height,
      pixelIndex,
      numPixels,
      n_bits,			              /** number of bits/code */ 
      maxbits= BITS,        		/** user settable max # bits/code */
      maxcode,		             	/** maximum code, given n_bits */
      maxmaxcode= (1 << BITS),	/** should NEVER generate this code */
      cur_accum= 0,
      cur_bits= 0;
      
    /** Image if specified */
    private Image
      img;                      
      
    private int
      masks[]= { 0x0000, 0x0001, 0x0003, 0x0007, 0x000F,
		            0x001F, 0x003F, 0x007F, 0x00FF,
		            0x01FF, 0x03FF, 0x07FF, 0x0FFF,
	            	 0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF
               };

    private byte
      accum[]= new byte[256],  	/** Define storage for packet accumulator */
      pixels[],
      r[],			                /** the color look-up table */
      g[],
      b[]; 
    
      
    /**
     * WriteGifEncoder() - Constructs a new WriteGifEncoder.
     * After creating the class, use writeFile() to actually write the file.
     * @param width	The image width.
     * @param height	The image height.
     * @param pixels	The pixel data.
     * @param r		The red look-up table.
     * @param g		The green look-up table.
     * @param b		The blue look-up table.
     * [Not used in MAExplorer]
     */
    public WriteGifEncoder(int width, int height, byte pixels[],
                           byte r[], byte g[], byte b[])
    { /* WriteGifEncoder */
      img= null;
      this.width= width;
      this.height= height;
      this.pixels= pixels;
      this.r= r;
      this.g= g;
      this.b= b;
      
      interlace= false;
      pixelIndex= 0;
      numPixels= width*height;
    } /* WriteGifEncoder */
    
    
    /**
     * WriteGifEncoder() - Constructs using 24-bit Image.
     * After creating the class, use writeFile() to actually write the file.
     * The image is assumed to be fully loaded.
     * It converts the 24-bit image into an 8-bit image with
     * (r,g,b)LUT[] data using the MedianCut algorithm.
     * @param img is the RGB image
     */
    public WriteGifEncoder(Image img /* RGB image */)
    { /* WriteGifEncoder */
      this.img= img;
      width= img.getWidth(null);
      height= img.getHeight(null);
      
      /* [1] Get the full RGB 24-bit pixel array */
      PixelGrabber pg24= new PixelGrabber(img, 0, 0, width, height, true);
      try
      {
        pg24.grabPixels();
      }
      catch (InterruptedException e)
      {
        System.err.println(e);
      };
      int iPixels[]= (int[])pg24.getPixels();
      
      /* [2] Convert the 24-bit image to 8-bit IndexColorModel
       * but keep in pixels[], r[], g[], b[] data structures.
       */
      MedianCut mc= new MedianCut(iPixels, width, height);
      iPixels= null;            /* Can G.C. now, since 8bit exists now*/
      ok= mc.cvtImg24toImg8(256 /* maxcubes  is ncolors */);
      if(!ok)
      {
        mc= null;             /* can G.C. now */
        return;
      }
      
      /* [3] Get the 8-bit pixel data and(r,g,b)LUT[]s */
      pixels= mc.pixels8;
      r= mc.rLUT;
      g= mc.gLUT;
      b= mc.bLUT;
      
      mc= null;                 /* can G.C. now */
      
      interlace= false;
      pixelIndex= 0;
      numPixels= width*height;
    } /* WriteGifEncoder */
    
    
    /**
     * writeFile() - write the Gif encoded image to output file
     * @param oGifFileName is full path of file name to be written
     * @return true if succeed
     */
    public boolean writeFile(String oGifFileName)
    { /* writeFile */
      if(!ok)
        return(false);
      
      /* Write it into GIF image */
      try
      { /* do it */
        FileOutputStream fos= new FileOutputStream(oGifFileName);
        writeGif(fos);       /* write 8-bit data to GIF file */
        fos.close();
      } /* do it */
      catch (Exception e)
      {
        return(false);
      }
      
      return(true);
    } /* writeFile */
    
    
    /**
     * writeGif() -  Saves the image as a GIF file.
     * @param out is file output stream
     */
    private void writeGif(FileOutputStream out) throws IOException
    { /* writeGif */
      /* Figure out how many bits to use. */
      int
        numColors= r.length,
        BitsPerPixel;
      if (numColors<=2)
        BitsPerPixel= 1;
      else if (numColors<=4)
        BitsPerPixel= 2;
      else if (numColors<=16)
        BitsPerPixel= 4;
      else
        BitsPerPixel= 8;
      
      int ColorMapSize= 1 << BitsPerPixel;
      byte
        reds[]= new byte[ColorMapSize],
        grns[]= new byte[ColorMapSize],
        blus[]= new byte[ColorMapSize];
      
      for (int i=0; i<numColors; i++)
      {
        reds[i]= r[i];
        grns[i]= g[i];
        blus[i]= b[i];
      }
      
      GIFEncode(out, width, height, interlace, (byte) 0, -1, BitsPerPixel,
                reds, grns, blus);
    } /* writeGif */
    
    
    /**
     * writeString() - write string to the file.
     * @param out is file output stream
     * @param str is string to write
     */
    private static void writeString(FileOutputStream out, String str)
    throws IOException
    { /* writeString */
      byte[] buf= str.getBytes();
      out.write(buf);
    } /* writeString */
    
    
    /**
     * GIFEncode() - encode the image
     * Adapted from ppmtogif, which is based on GIFENCOD by David
     * Rowley <mgardi@watdscu.waterloo.edu>.  Lempel-Zim compression
     * based on "compress".
     * @param outs is file output stream
     * @param Width of image
     * @param Height of image
     * @param Interlace flag
     * @param Background value
     * @param Transparent flag
     * @param BitsPerPixel bits per pixel
     * @param Red is byte array of data
     * @param Green byte array of data
     * @param Blue is byte array of data
     */
    private void GIFEncode(FileOutputStream outs, int Width, int Height,
                           boolean Interlace, byte Background,
                           int Transparent, int BitsPerPixel,
                           byte[] Red, byte[] Green, byte[] Blue)
    throws IOException
    { /* GIFEncode */
      byte B;
      int
        LeftOfs, TopOfs,
        ColorMapSize,
        InitCodeSize,
        i;
      
      this.Width= Width;
      this.Height= Height;
      this.Interlace= Interlace;
      ColorMapSize= 1 << BitsPerPixel;
      LeftOfs= TopOfs= 0;
      
      /* The initial code size */
      if(BitsPerPixel <= 1)
        InitCodeSize= 2;
      else
        InitCodeSize= BitsPerPixel;
      
      writeString(outs, "GIF89a");     /* Write the Magic header */
      
      Putword(Width, outs);	           /* Write out screen width & height */
      Putword(Height, outs);
      
      /* Indicate that there is a global colour map */
      B= (byte) 0x80;		                /* Yes, there is a color map */
      B |= (byte) ((8 - 1) << 4);       /* OR in the resolution */
      
      /* Not sorted */
      B |= (byte) ((BitsPerPixel - 1)); /* OR in the Bits per Pixel */
      
      /* Write it out */
      Putbyte(B, outs);
      
      /* Write out the Background colour */
      Putbyte(Background, outs);
      
      /* Pixel aspect ratio - 1:1.
       * Putbyte((byte) 49, outs);
       * Java's GIF reader currently has a bug, if the aspect ratio byte is
       * not zero it throws an ImageFormatException.  It doesn't know that
       * 49 means a 1:1 aspect ratio.  Well, whatever, zero works with all
       * the other decoders I've tried so it probably doesn't hurt.
       */
      Putbyte((byte) 0, outs);
      
      /* Write out the Global Colour Map */
      for (i= 0; i < ColorMapSize; ++i)
      {
        Putbyte(Red[i], outs);
        Putbyte(Green[i], outs);
        Putbyte(Blue[i], outs);
      }
      
      /* Write out extension for transparent colour index, if necessary. */
      if (Transparent != -1)
      {
        Putbyte((byte) '!', outs);
        Putbyte((byte) 0xf9, outs);
        Putbyte((byte) 4, outs);
        Putbyte((byte) 1, outs);
        Putbyte((byte) 0, outs);
        Putbyte((byte) 0, outs);
        Putbyte((byte) Transparent, outs);
        Putbyte((byte) 0, outs);
      }
      
      /* Write an Image separator */
      Putbyte((byte) ',', outs);
      
      /* Write the Image header */
      Putword(LeftOfs, outs);
      Putword(TopOfs, outs);
      Putword(Width, outs);
      Putword(Height, outs);
      
      /* Write out whether or not the image is interlaced */
      if(Interlace)
        Putbyte((byte) 0x40, outs);
      else
        Putbyte((byte) 0x00, outs);
      
      /* Write out the initial code size */
      Putbyte((byte) InitCodeSize, outs);
      
      /* Go and actually compress the data */
      compress(InitCodeSize+1, outs);
      
      /* Write out a Zero-length packet (to end the series) */
      Putbyte((byte) 0, outs);
      
      /* Write the GIF file terminator */
      Putbyte((byte) ';', outs);
    } /* GIFEncode */
    
    
    
    /**
     * GIFNextPixel() - Return the next pixel from the image
     */
    private int GIFNextPixel() throws IOException
    { /* GIFNextPixel */
      if(pixelIndex==numPixels)
        return(EOF);
      else
        return(((byte[])pixels)[pixelIndex++] & 0xff);
    } /* GIFNextPixel */
    
    
    /**
     * Putword() - Write out a word to the GIF file
     */
    private void Putword(int w, FileOutputStream outs) throws IOException
    { /* Putword */
      Putbyte((byte) (w & 0xff), outs);
      Putbyte((byte) ((w >> 8) & 0xff), outs);
    } /* Putword */
    
    
    /**
     * Putbyte() -  Write out a byte to the GIF file
     */
    private void Putbyte(byte b, FileOutputStream outs) throws IOException
    { /* Putbyte */
      outs.write(b);
    } /* Putbyte */
    
    
    /**
     * MAXCODE() - return n bits
     */
    final private int MAXCODE(int n_bits)
    { /* MAXCODE */
      return((1 << n_bits) - 1);
    } /* MAXCODE */
    
    
    /**
     * compress() -  GIF Image compression - modified 'compress'.
     *
     * Original code from GIFCOMPR.C GIF Image compression routines.     *
     * Lempel-Ziv compression based on 'compress'.  GIF modifications
     * by David Rowley (mgardi@watdcsu.waterloo.edu)
     *
     * This GIF Image compression - modified 'compress' was based
     * on: compress.c - File compression ala IEEE Computer, June 1984.
     *
     * By Authors:  Spencer W. Thomas    (decvax!harpo!utah-cs!utah-gr!thomas)
     *              Jim McKie            (decvax!mcvax!jim)
     *              Steve Davies         (decvax!vax135!petsd!peora!srd)
     *              Ken Turkowski        (decvax!decwrl!turtlevax!ken)
     *              James A. Woods       (decvax!ihnp4!ames!jaw)
     *              Joe Orost            (decvax!vax135!petsd!joe)
     *
     * Algorithm: use open addressing double hashing (no chaining) on
     * the prefix code / next character combination.  We do a variant
     * of Knuth's algorithm D (vol. 3, sec. 6.4) along with G. Knott's
     * relatively-prime secondary probe.  Here, the modular division
     * first probe is gives way to a faster exclusive-or manipulation.
     * Also do block compression with an adaptive reset, whereby the
     * code table is cleared when the compression ratio decreases, but
     * after the table fills.  The variable-length output codes are
     * re-sized at this point, and a special CLEAR code is generated
     * for the decompressor.  Late addition: construct the table
     * according to file size for noticeable speed improvement on
     * small files.  Please direct questions about THE OLD
     * implementation to ames!jaw.
     *
     * Block compression parameters -- after all codes are used up,
     * and compression rate changes, start over.
     */
    private void compress(int init_bits, FileOutputStream outs)
    throws IOException
    { /* compress */
      int
        fcode,
        i /*= 0 */,
        c,
        ent,
        disp,
        hsize_reg,
        hshift;
      
      /* Set up the globals:  g_init_bits - initial number of bits */
      g_init_bits= init_bits;
      
      /* Set up the necessary values */
      clear_flg= false;
      n_bits= g_init_bits;
      maxcode= MAXCODE(n_bits);
      
      ClearCode= (1 << (init_bits - 1));
      EOFCode= ClearCode + 1;
      free_ent= ClearCode + 2;
      
      char_init();
      
      ent= GIFNextPixel();
      
      hshift= 0;
      for (fcode= hsize; fcode < 65536; fcode *= 2)
        ++hshift;
      hshift= 8 - hshift;	/* set hash code range bound */
      
      hsize_reg= hsize;
      cl_hash(hsize_reg);	/* clear hash table */
      
      output(ClearCode, outs);
      
      outer_loop:
        while ((c= GIFNextPixel()) != EOF)
        { /* encode pixels */
          fcode= (c << maxbits) + ent;
          i= (c << hshift) ^ ent;     /* xor hashing */
          
          if (htab[i] == fcode)
          {
            ent= codetab[i];
            continue;
          }
          else if (htab[i] >= 0)
          { /* non-empty slot */
            disp= hsize_reg - i;      /* secondary hash (after G. Knott) */
            if (i == 0)
              disp= 1;
            do
            {
              if ((i -= disp) < 0)
                i += hsize_reg;
              
              if (htab[i] == fcode)
              {
                ent= codetab[i];
                continue outer_loop;
              }
            }
            while (htab[i] >= 0);
          } /* non-empty slot */
          
          output(ent, outs);
          ent= c;
          if (free_ent < maxmaxcode)
          {
            codetab[i]= free_ent++;	/* code -> hashtable */
            htab[i]= fcode;
          }
          else
            cl_block(outs);
        } /* encode pixels */
        
        /* Put out the final code. */
        output(ent, outs);
        output(EOFCode, outs);
    } /* compress */
    
    
    /**
     * output() - Output the given code.
     * Inputs:
     *      code:   A n_bits-bit integer.  If == -1, then EOF.  This assumes
     *              that n_bits =< wordsize - 1.
     * Outputs:
     *      Outputs code to the file.
     * Assumptions:
     *      Chars are 8 bits long.
     * Algorithm:
     *      Maintain a BITS character long buffer (so that 8 codes will
     * fit in it exactly).  Use the VAX insv instruction to insert each
     * code in turn.  When the buffer fills up empty it and start over.
     */
    
    private void output(int code, FileOutputStream outs) throws IOException
    { /* output */
      cur_accum &= masks[cur_bits];
      
      if (cur_bits > 0)
        cur_accum |= (code << cur_bits);
      else
        cur_accum= code;
      
      cur_bits += n_bits;
      
      while (cur_bits >= 8)
      {
        char_out((byte) (cur_accum & 0xff), outs);
        cur_accum >>= 8;
        cur_bits -= 8;
      }
      
     /* If the next entry is going to be too big for the code size,
      * then increase it, if possible.
      */
      if (free_ent > maxcode || clear_flg)
      {
        if (clear_flg)
        {
          maxcode= MAXCODE(n_bits= g_init_bits);
          clear_flg= false;
        }
        else
        {
          ++n_bits;
          if (n_bits == maxbits)
            maxcode= maxmaxcode;
          else
            maxcode= MAXCODE(n_bits);
        }
      }
      
      if (code == EOFCode)
      { /* At EOF, write the rest of the buffer. */
        while (cur_bits > 0)
        {
          char_out((byte) (cur_accum & 0xff), outs);
          cur_accum >>= 8;
          cur_bits -= 8;
        }
        flush_char(outs);
      }
    } /* output*/
    
    
    /**
     * cl_block() - table clear for block compress
     * Clear out the hash table.
     */
    private void cl_block(FileOutputStream outs) throws IOException
    { /* cl_block */
      cl_hash(hsize);
      free_ent= ClearCode + 2;
      clear_flg= true;
      
      output(ClearCode, outs);
    } /* cl_block */
    
    
    /**
     * cl_hash() -  reset code table
     */
    private void cl_hash(int hsize)
    { /* cl_hash */
      for (int i= 0; i < hsize; ++i)
        htab[i]= -1;
    } /* cl_hash */
    
    
    /* --- The following are GIF Specific routines --- */
    
    /**
     * char_init() - Set up the 'byte output' routine
     */
    private void char_init()
    { /* char_init */
      a_count= 0;
    } /* char_init */
    
    
    /**
     * char_out() - add char to end of current packet, and if it is 254
     * characters, flush the packet to disk.
     */
    void char_out(byte c, FileOutputStream outs) throws IOException
    { /* char_out */
      accum[a_count++]= c;
      if (a_count >= 254)
        flush_char(outs);
    } /* char_out */
    
    
    /**
     * flush_char() - flush packet to disk, and reset the accumulator
     */
    void flush_char(FileOutputStream outs) throws IOException
    { /*  flush_char */
      if (a_count > 0)
      {
        outs.write(a_count);
        outs.write(accum, 0, a_count);
        a_count= 0;
      }
    } /*  flush_char */
    
}  /* end of class WriteGifEncoder */


/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
/*           Class WriteGifEncoderHashitem                                */
/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */

class WriteGifEncoderHashitem
{ /* WriteGifEncoderHashitem  */
  int
    rgb,
    count,
    index;
  boolean
    isTransparent;
  
  /**
   * WriteGifEncoderHashitem() - constructor
   */
  WriteGifEncoderHashitem(int rgb, int count, int index, boolean isTransparent)
  { /* WriteGifEncoderHashitem  */
    this.rgb= rgb;
    this.count= count;
    this.index= index;
    this.isTransparent= isTransparent;
  } /* WriteGifEncoderHashitem  */
  
}  /* end of class WriteGifEncoderHashitem  */




/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
/*           Class MedianCut                                               */
/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */


/**
 * MedianCut converts an RGB image to 8-bit index color using
 * Heckbert's median-cut color quantization algorithm. Based on
 * median.c by Anton Kruger from the
 *   September, 1994 issue of Dr. Dobbs Journal.
 *
 * Notes on the algorithm:
 * 1. It takes the 24-bit (r,g,b) pixel, clips the bottom 3 bits
 *   of each color to make a packed 15-bit pixel (2**15=32768).
 * 2. It then builds a build 32x32x32 RGB histogram hist15[0:32768].
 * 3. Calling code invokes cvtImg24toImg8() to cvt color space defined
 *    by hist15[] into maxcubes cubes.
 *  3.1 Create initial cube from hist15[] data.
 *  3.2 Search cubeList[] for next cube to split, lowest level cube.
 *    3.2.1 Check if split cubes
 *    3.2.2 Find longest dimension of this cube
 *    3.2.3 Sort along "longdim" by:
 *         reorderColors(), quickSort(), restoreColorOrder().
 *    3.2.4 Find median
 *    3.2.5 Now split "cube" at the median and add the two new
 *          cubes to the list of cubes.
 *    3.3 Compute the color map, inverse color map.
 *  3.4 Convert 24-bit RGB image to an 8-bit image.
 * 4. The caller gets the return data from the MedianCut instance:
 *    pixels8[], rLUT[], gLUT[], and bLUT[].
 */

class MedianCut
{
  /** maximum # of output colors */
  static final int
    MAXCOLORS= 256;
  /** max size of image histogram */
  static final int
    HSIZE= 32768;
  
  /** [0:32K] 15-bit RGB hist. & reverse color LUT */
  private int
    hist15[];
  /** [0:colorMax-1] points to colors in "hist15[]" */
  private int
    histPtr[];
  /** 32-bit image[width*height] (Alpha,Red,Green,Blue) pixels */
  private int
    pixels32[];
  
  /** list of cubes */
  private MC_Cube
    cubeList[];
  
  /* --- Data to be returned --- */
  /** # of color cubes */
  int
    ncubes;
  /** # of non-zero bins in hist15[] */
  int
    colorMax;
  /** width size of image */
  int
    width;
  /** width size of image */
  int
    height;
    
  /** 8-bit image[width*height] using LUTs */
  byte
    pixels8[];
  /** [0:255] Red Color map look up table */
  byte
    rLUT[];
  /** [0:255] Green Color map look up table */
  byte
    gLUT[];
  /** [0:255] Blue Color map look up table */
  byte
    bLUT[];
  
  
  
  /**
   * MedianCut() - Constructor
   * Then call cvtImg24toImg8() to get the 8-bit image.
   * @param pixels array of 32-bit Java Color pixel
   * @param width is the image size
   * @param height is the image size
   */
  MedianCut(int pixels[], int width, int height)
  { /*  MedianCut */
    int
      size= width*height,
      color15,
      p32;
    
    pixels32= pixels;
    this.width= width;
    this.height= height;
    
    rLUT= new byte[256]; /* Color map look up tables */
    gLUT= new byte[256];
    bLUT= new byte[256];
    pixels8= null;       /* will alloc [width*height] in makeImage */
    
    /* Build 32x32x32 RGB histogram */
    hist15= new int[HSIZE];
    
    for (int i=0; i<size; i++)
    { /* compute initial hist15[] */
      p32= pixels32[i];
      color15= rgb15(p32);
      hist15[color15]++;
    } /* compute initial hist15[] */    
  } /* MedianCut */
  
  
  /**
   * rgb15() - convert from 24-bit (ignore alpha) to 15-bit color.
   * Use top 5-bits of the 8-bit color components, ignores low 3-bits
   * of each component.
   *
   *  -----------------------------
   *  |Alpha | Red  | Green | Blue| Java Color layout (8-bit pixels)
   *  -----------------------------
   *   31-17  23-16   15-8   7-0
   *
   *  -----------------------------
   *  | free | Red  | Green | Blue|  15-bit color Layout (5-bit pixels)
   *  -----------------------------
   *   31-15  14-10   9-5    4-0
   *
   * @param  c is the RGB 24-bit Java Color pixel
   * @return 15-bit color.
   */
  private final int rgb15(int c)
  { /* rgb15 */
    /* Get most signif. 5 bits shifted to 15-bit packed positions */
    int
      rr= ((c & 0xf80000) >> 9),  /* shift bit 23 to bit 14 */
      gg= ((c & 0xf800) >> 6),    /* shift bit 15 to bit 9 */
      bb= ((c & 0xf8) >> 3);      /* shift bit 7 to bit 4 */
    
    return( rr | gg | bb );
  } /* rgb15 */
  
  
  /**
   * redBits() - Get 8-bit red component of a 15-bit color
   * 8-bit pixel with low 3 bits zero.
   * @param x 15-bit color component
   * @return value
   */
  private final int redBits(int x)
  { /* redBits */
    return((x >> 7) & 0xf8);   /* shift bit 14 to bit 7 */
  } /* redBits */
  
  
  /**
   * greenBits() - Get 8-bit green component of a 15-bit color
   * 8-bit pixel with low 3 bits zero.
   * @param x 15-bit color component
   * @return value
   */
  private final int greenBits(int x)
  { /* greenBits */
    return((x >> 2) & 0xf8);  /* shift bit 9 to bit 7 */
  } /* greenBits */
  
  
  /**
   * blueBits() - Get 8-bit blue component of a 15-bit color
   * 8-bit pixel with low 3 bits zero.
   * @param x 15-bit color component
   * @return value
   */
  private final int blueBits(int x)
  { /* blueBits */
    return((x << 3) & 0xf8);   /* shift bit 4 to bit 7 */
  } /* blueBits */  
  
  
  /**
   * cvtImg24toImg8() - Uses Heckbert's median-cut algorithm to divide
   * the color space defined by "hist15[]" into "maxcubes" cubes.  The
   * centroids (average value) of each cube are are used to create a
   * color table.  "hist15[]" is then updated to function as an inverse
   * color map that is used to generate an 8-bit image.
   * @param maxcubes to use
   * @return true if succeed. Return data is in pixels8[], rLUT, gLUT[], bLUT[].
   */
  boolean cvtImg24toImg8(int maxcubes)
  { /* cvtImg24toImg8 */
    if(width==0 || height==0)
      return(false);
    
    int
      lr, lg, lb,
      i, median, color15,
      count,
      k, level, splitpos,
      num, width,
      longdim= 0;	               	/* longest dimension of cube */
    MC_Cube
      cube,
      cubeA,
      cubeB;
    
    /* [1] Create initial cube */
    cubeList= new MC_Cube[MAXCOLORS];
    histPtr= new int[HSIZE];
    ncubes= 0;
    colorMax= 0;
    cube= new MC_Cube();           /* initial cube with zero count */
    
    for (i=0; i<HSIZE; i++)
      if (hist15[i] != 0)
      {
        histPtr[colorMax++]= i;  /* lookup table for hist15[] */
        cube.cnt += hist15[i];
      }
    
    cube.low= 0;
    cube.up= colorMax-1;
    cube.lvl= 0;
    shrinkCube(cube);
    cubeList[ncubes++]= cube;
    
    /* [2] Search the cubeList[] for next cube to split,
     * the lowest level cube. If there are no cubes to split
     * then quit the loop (i.e. splitpos will still be -1).
     */
    while (ncubes < maxcubes)
    { /* Main loop to split cubes */
      level= 255;
      splitpos= -1;
      MC_Cube cubeK;
      
      /* [2.1] Check if split cubes */
      for (k=0; k<ncubes; k++)
      { /* check if split cubes */
        cubeK= cubeList[k];
        if (cubeK.low == cubeK.up)
          ;	        /* single color; cannot be split */
        else if (cubeK.lvl < level)
        {
          level= cubeK.lvl;
          splitpos= k;
        }
      } /* check if split cubes */
      
      if (splitpos == -1)	/* no more cubes to split - exit loop */
        break;
      
      /* [2.2] Find longest dimension of this cube */
      cube= cubeList[splitpos];
      lr= cube.rmax - cube.rmin;
      lg= cube.gmax - cube.gmin;
      lb= cube.bmax - cube.bmin;
      if (lr >= lg && lr >= lb)
        longdim= 0;
      if (lg >= lr && lg >= lb)
        longdim= 1;
      if (lb >= lr && lb >= lg)
        longdim= 2;
      
      /* [2.3] Sort along "longdim" */
      reorderColors(histPtr, cube.low, cube.up, longdim);
      quickSort(histPtr, cube.low, cube.up);
      restoreColorOrder(histPtr, cube.low, cube.up, longdim);
      
      /* [2.4] Find median */
      count= 0;
      for (i=cube.low;i<=cube.up-1;i++)
      {
        if (count >= cube.cnt/2)
          break;
        color15= histPtr[i];
        count += hist15[color15];
      }
      median= i;
      
    /* [2.5] Now split "cube" at the median and add the two new
     * cubes to the list of cubes.
     */
      cubeA= new MC_Cube();
      cubeA.low= cube.low;
      cubeA.up= median-1;
      cubeA.cnt= count;
      cubeA.lvl= cube.lvl + 1;
      shrinkCube(cubeA);
      cubeList[splitpos]= cubeA;		 /* add in old slot */
      
      cubeB= new MC_Cube();
      cubeB.low= median;
      cubeB.up= cube.up;
      cubeB.cnt= cube.cnt - count;
      cubeB.lvl= cube.lvl + 1;
      shrinkCube(cubeB);
      cubeList[ncubes++]= cubeB;		 /* add in new slot */
    }  /* Main loop */
    
    /* [3] We have enough cubes, or we have split all we can. Now
     * compute the color map, inverse color map.
     */
    makeInverseMap(hist15, ncubes);
    
    /* [4] Convert 24-bit RGB image to an 8-bit image. */
    make8BitImage();
    
    return(true);
  } /* cvtImg24toImg8 */
  
  
  /**
   * shrinkCube() - Encloses "cube" with a tight-fitting cube by updating
   * the (rmin,gmin,bmin) and (rmax,gmax,bmax) members of "cube".
   * @param cube is the cube to use
   */
  private void shrinkCube(MC_Cube cube)
  { /* shrinkCube */
    int
      rr, gg, bb,
      color15,
      rmin= 255,
      rmax= 0,
      gmin= 255,
      gmax= 0,
      bmin= 255,
      bmax= 0;
    
    for (int i=cube.low; i<=cube.up; i++)
    { /* find  cube limits */
      color15= histPtr[i];
      
      rr= redBits(color15);
      gg= greenBits(color15);
      bb= blueBits(color15);
      
      if (rr > rmax)
        rmax= rr;
      if (rr < rmin)
        rmin= rr;
      if (gg > gmax)
        gmax= gg;
      if (gg < gmin)
        gmin= gg;
      if (bb > bmax)
        bmax= bb;
      if (bb < bmin)
        bmin= bb;
    } /* find  cube limits */
    
    cube.rmin= rmin;  /* set cube limits */
    cube.rmax= rmax;
    cube.gmin= gmin;
    cube.gmax= gmax;
    cube.bmin= bmin;  /* NOTE:[BUG] was gmax/gmin dups. in IJ1.14 */
    cube.bmax= bmax;
  } /* shrinkCube */
  
  
  /**
   * makeInverseMap() - for all cubes, compute centroid of colors in
   * cube.  For each cube in the list of cubes, computes the centroid
   * (average value) of the colors enclosed by that cube, and then
   * loads the centroids in the color map. Next loads "hist15[]" with
   * indices into the color map.
   * @param hist15 is the histogram to compute
   * @param ncubes is the number of cubes to use
   */
  void makeInverseMap(int hist15[], int ncubes)
  { /* makeInverseMap */
    int
      rr,
      gg,
      bb,
      color15;
    float
      fcount,
      rsum,
      gsum,
      bsum,
      hc15;
    MC_Cube cube;
    
    for (int k=0; k<=ncubes-1; k++)
    { /* process cubes */
      cube= cubeList[k];
      rsum= 0.0F;
      gsum= 0.0F;
      bsum= 0.0F;
      
      for (int i=cube.low; i<=cube.up; i++)
      { /* compute sums of each of the component colors */
        color15= histPtr[i];
        hc15= (float)hist15[color15];
        
        rr= redBits(color15);
        rsum += rr*hc15;
        
        gg= greenBits(color15);
        gsum += gg*hc15;
        
        bb= blueBits(color15);
        bsum += bb*hc15;
      }
      
      /* Update the color map with mean (r,g,b) */
      fcount= (float)cube.cnt;
      rr= (int)(rsum/fcount);
      gg= (int)(gsum/fcount);
      bb= (int)(bsum/fcount);
      
      if (rr==248 && gg==248 && bb==248)
      { /* Restore white (255,255,255) */
        rr= 255;
        gg= 255;
        bb= 255;
      }
      rLUT[k]= (byte)rr;
      gLUT[k]= (byte)gg;
      bLUT[k]= (byte)bb;
    } /* process cubes */
    
    /* For each color in each cube, load the corresponding
     * slot in "hist15[]" with the centroid of the cube.
     */
    for (int k=0; k<=ncubes-1; k++)
    {
      cube= cubeList[k];
      for (int i=cube.low; i<=cube.up; i++)
      {
        color15= histPtr[i];
        hist15[color15]= k;
      }
    }
    
    return;
  } /* makeInverseMap */
  
  
  /**
   * reorderColors() - Change ordering of 5-bit colors in each word
   * of int[] so we can sort on the 'longDim' color
   */
  void reorderColors(int histP[], int lo, int hi, int longDim)
  { /* reorderColors */
    int
      c, 
      rr, 
      gg,
      bb;
    
    switch (longDim)
    {
      case 0:			/* red */
        for (int i=lo; i<=hi; i++)
        {
          c= histP[i];
          rr= c & 31;
          histP[i]= (rr << 10) | (c>>5);
        }
        break;
        
      case 1:     /* green */
        for (int i=lo; i<=hi; i++)
        {
          c= histP[i];
          rr= c & 31;
          gg= (c >> 5) & 31;
          bb= c >> 10;
          histP[i]= (gg << 10) | (bb << 5) | rr;
        }
        break;
        
      case 2:			/* blue; already in the needed order */
        break;
    }
  } /* reorderColors */
  
  
  /**
   * restoreColorOrder() -  Restore the 5-bit colors to original order
   */
  void restoreColorOrder(int[] histP, int lo, int hi, int longDim)
  { /* restoreColorOrder */
    int
      c, 
      rr, 
      gg,
      bb;
    
    switch(longDim)
    {
      case 0:			/* red */
        for (int i=lo; i<=hi; i++)
        {
          c= histP[i];
          rr= c >> 10;
          histP[i]= ((c & 1023) << 5) | rr;
        }
        break;
        
      case 1:			/* green */
        for (int i=lo; i<=hi; i++)
        {
          c= histP[i];
          rr= c & 31;
          gg= c >> 10;
          bb= (c >> 5) & 31;
          histP[i]= (bb << 10) | (gg << 5) | rr;
        }
        break;
        
      case 2:			/* blue */
        break;
    }
  } /* restoreColorOrder */
  
  
  /**
   * quickSort() - sort the map
   * Based on the QuickSort method by James Gosling from Sun's
   * SortDemo applet
   */
  void quickSort(int a[], int lo0, int hi0)
  { /* quickSort */
    int
      lo= lo0,
      hi= hi0,
      mid,
      t;
    
    if ( hi0 > lo0)
    {
      mid= a[(lo0 + hi0)/2];
      while(lo <= hi)
      {
        while((lo < hi0) && (a[lo] < mid) )
          ++lo;
        while((hi > lo0) && (a[hi] > mid) )
          --hi;
        if(lo <= hi)
        {
          t= a[lo];
          a[lo]= a[hi];
          a[hi]= t;
          ++lo;
          --hi;
        }
      }
      if(lo0 < hi)
        quickSort(a, lo0, hi);
      if(lo < hi0)
        quickSort(a, lo, hi0);
    }
  } /* quickSort */
  
  
  /**
   * make8BitImage() - Generate 8-bit image from interla 15-bit image.
   */
  void make8BitImage()
  { /* make8BitImage */
    int
      color15,
      size= width*height;
    
    pixels8= new byte[width*height];
    for (int i=0; i<size; i++)
    { /* map 15-bit pixels to 8-bit pixels */
      color15= rgb15(pixels32[i]);
      pixels8[i]= (byte)(hist15[color15] & 0Xff );
    }
  } /* make8BitImage */
  
  
  
} /* end of class MedianCut */



/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
/*           Class MC_Cube                                              */
/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
class MC_Cube
{				/* structure for a cube in color space */
  static int nextID= 0;
  int id;
  int  low;			/* one corner's index in histogram */
  int  up;			/* another corner's index in histogram */
  int  cnt;			/* cube's histogram count */
  int  lvl;			/* cube's level */
  int  rmin, rmax;
  int  gmin, gmax;
  int  bmin, bmax;
  
  /**
   * MC_Cube() - constructor
   */
  MC_Cube()
  { /* MC_Cube */
    id= ++nextID;
    cnt= 0;
  } /* MC_Cube */
  
  
  /**
   * toString() - prettyprint as string.
   */
  /*
  String toString(int j)
    {
      String s= "Cube#"+j+" id="+id+
                " RGBrng["+rmin+":"+rmax+
                ", "+gmin+":"+gmax+
                ", "+bmin+":"+bmax+"]"+
                " low="+low+" up="+up+
                " cnt="+cnt+" lvl="+lvl;
   
      return(s);
    }
   */
  
}    /* End of class Cube */




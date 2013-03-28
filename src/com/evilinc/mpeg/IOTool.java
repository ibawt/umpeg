/**********************************************************************
 *
 *  Copyright Ian Quick 2002
 *  
 *  This file is part of MicroMpeg.
 *
 *   MicroMpeg is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   MicroMpeg is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MicroMpeg; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **********************************************************************/
package com.evilinc.mpeg;

import java.io.*;

public class IOTool
{
    private InputStream	dis = null;			// THE input stream
    
    private int bit_pos = 0;				// bit- "pointer" 
    private long longword = 0L;				// THE bit shift register
    private boolean eof = false;
    
    private int rest_bytes = -1;			// the counter: How many MPEG layer II bytes
    // remain. A negative value means: This is
    // MPEG layer I
    long pts;
    // Presentation time stamp
    // Doesn't belong here-move back to scanner or player

    IOTool(InputStream stream)
    {
        dis = stream;
    }


    public final void close() throws IOException
    {
        dis.close();
    }
    
    public final boolean isEOF() { return eof; }

	// The method "skip(long n)" skips over n bytes but takes into account that
	// some of them are already in shift register.
    public final void skip(long n)
        throws IOException
    {
        int k = bit_pos / 8;  // How many bytes are already in shift register ?
        n -= k;                // substract from whole count
        bit_pos -= ( k * 8 ); // skip over the bits in shift register
        while (n > 0)
        {
            n -= dis.skip(n);
        }
    }

    // The method "get_read_bytes()" tells how many bytes are in shift register.
    public final int get_read_bytes()
        throws IOException
    {
        if ((bit_pos & 0x7) != 0)
        { // "bit_pos" sould be byte aligned
            throw new IOException("get_read_bytes: bit_pos = " + bit_pos);
        }
        return (bit_pos/ 8);
    }

	// The method "set_rest_bytes" sets the number of MPEG layer II bytes.
    public final void set_rest_bytes(int bytes)
    {
        rest_bytes = bytes;
    }
    
	// The method "get_long" grabs "bytes" from data input stream
	// into the shift register "longword".
    private final void get_long()
        throws IOException
    {
        int save_pos = 0;				// A copy of "bit_pos"
        long save_word = 0L;			// A copy of the bit shift register
        
        try {
            // If less than 4 bytes remain in layer II it must be checked
            // whether it is possible to return 32 bit.
            // System.err.println("get_long(): " + rest_bytes);
            if ((rest_bytes & 0xfffffffc) == 0 ) {
		System.out.println("do I get here?");
		// rest_bytes >= 0 && rest_bytes < 4
                if ((rest_bytes * 8) >= 32 - bit_pos) { // still 32 bit available ?
                    while (bit_pos < 32) { // Yes --> read still some bytes
                        longword = (longword << 8) | dis.read();
                        bit_pos += 8;
                        rest_bytes--;
                    }
                    return;
                }
                
                
                // There aren't still 32 bit available in layer II --> save the
                // bits on bit shift register:
                save_word = longword & ((1L << bit_pos) - 1L);
                save_pos = bit_pos;
                
                // read the remaining layer II bytes and put them in the saved
                // shift register.
                for(;rest_bytes > 0; rest_bytes--) {
                    save_word = (save_word << 8) | dis.read();
                    save_pos += 8;
                }
                
                // prepare for switch to layer I:
                
                rest_bytes = -1; // signal; we are now in layer I !!!
                bit_pos = 0;	 // clear shift register.
                
                //av_semaphor.toggle0(); // suspend layer II thread; wake up
                // layer I thread.
                
                if (save_pos + bit_pos > 63) { // ERROR ERROR ERROR
                    throw new IOException("Recovery not possible = " + save_pos +
                                       "; bit_pos = " + bit_pos);
                }
                longword &= ((1L << bit_pos) - 1L); // cut leading bits to "0"
                longword |= (save_word << bit_pos); // recover saved bits
                bit_pos += save_pos;	// correct bit pointer
                while (bit_pos < 32) {	// make 32 bit available
                    longword = (longword << 8) | dis.read();
                    bit_pos += 8; rest_bytes--;
                }
                return;
            } else { // normal processing
                
                longword = (longword << 32) | (((long) dis.read()) << 24) |
                    (((long) dis.read()) << 16) |
                    (((long) dis.read()) << 8) | dis.read();
                
                bit_pos += 32;
                rest_bytes -= 4;
            }
            // System.out.println(count++ + " long: " + );
        }
        catch (EOFException e) {
	    System.out.println("EOF?");
            eof = true;
            bit_pos += 32;
            longword <<= 32;
        }
    }
    
    // The method "get_bits" gets the next "n" bits from shift register
    // interprets them as integer and returns this integer value.

    private boolean once;
    
    public final int getBits(int n)
        throws IOException
    {
        if (bit_pos < n) get_long();

	    
        bit_pos -= n; // correct bit "pointer"
        // FIXME: array lookup shift faster??
        return (int) ((longword >>> bit_pos) & ((1L << n) - 1L));
    }
    
    // The method "skip_bits" skips the next "n" bits from shift register.
    // This method created because too much time was being wasted doing the
    // math in get_bits when the result was just going to be thrown out.

    public final void skipBits(int n)
        throws IOException
    {
        if (bit_pos < n) get_long();
        bit_pos -= n; // correct bit "pointer"
    }

    // The method "next_bits" checks whether the next "n" bits match the
    // "pattern". If so it returns "true"; otherwise "false".
    // Note: This method changes the bit "pointer" physically BUT NOT
    // logically !!!

    public final boolean nextBits(int pattern, int n)
        throws IOException
    {
        if (bit_pos < n) get_long();
        return (int)((longword >>> (bit_pos - n)) & ((1L << n) - 1L)) == pattern;
    }

    // The method "peek_bits" is like get_bits except it leaves the
    // bits in the shift register.  Use it when you want to read
    // the same bits more than once.

    public final int peekBits(int n)
        throws IOException
    {
        if (bit_pos < n) get_long();
        return (int)((longword >>> (bit_pos - n)) & ((1L << n) - 1L));
    }


    // The method "unget_bits" gives "n" bits back to the IO system. Because
    // "n" is always less than 32 this can be performed by a simple
    // correction of the bit "pointer".

    public final void ungetBits(int n)
    {
        bit_pos += n;
    }

    // The method "next_start_code" aligns the bit "pointer" to the next
    // byte and tries to find the next MPEG start code. Because (only) start
    // codes are made of a 24-bit ONE the method searches such a pattern.
    // (see also: ISO 11172-2)

    public final void nextStartCode()
        throws IOException
    {
        if ((bit_pos & 0x7) != 0) {
            bit_pos &= ~0x7;
        }
        while (!nextBits(0x1, 24) && !eof) skipBits(8);
    } 


    // Note: does not set rest_bytes, as it should only be called
    // from mpeg_scan when decoding system stream.
    public int read(byte[] b, int off, int len) throws IOException
    {
        int i = 0;

        if (bit_pos > 0) {
	    
            int k = bit_pos / 8;     // How many bytes are in shift register ?
	    
            for (; i < k; i++) {
                b[off + i] = (byte) getBits(8);
            }

            bit_pos = 0;

            if (len - k > 0) {
                return dis.read(b, off + k, len - k) + k;
            }
            return k;
        }
        return dis.read(b, off, len);
    }



}
    
       

    
    

    

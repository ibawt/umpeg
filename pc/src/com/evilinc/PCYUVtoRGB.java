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

package com.evilinc;

import com.evilinc.mpeg.video.YUVtoRGB;

public class PCYUVtoRGB extends YUVtoRGB
{

    private static final int CR_FAC = 0x166EA;
    private static final int CB_FAC = 0x1C5A2;
    private static final int CR_DIFF_FAC = 0xB6D2;
    private static final int CB_DIFF_FAC = 0x581A;
    
    
    private FrameBufferPanel m_frameBuff;
    private boolean          m_bDisplaying;
    
    
    public PCYUVtoRGB( FrameBufferPanel frameBuff )
    {
	if( frameBuff == null )
	    throw new NullPointerException();

	m_frameBuff = frameBuff;
    }

    public boolean display( byte[] ya, byte[] ua, byte[] va)
    {
	if( m_bDisplaying )
	    return false;

	m_bDisplaying = true;
	
	if( ya == null || ua == null || va == null )
	    throw new NullPointerException();

	int[] pixels = m_frameBuff.lockFrameBuffer();

	int stride = m_frameBuff.getWidth();


	int lum_idx1 = 0, lum_idx2 = stride;

	
	for( int i = 0 ; i < (pixels.length>>2 ) ; ++i )
	{
	    int cb = ( ua[i] & 0xff ) - 128;
	    int cr = ( va[i] & 0xff ) - 128;

	    int crb_g = cr * CR_DIFF_FAC + cb * CB_DIFF_FAC;
	    
	    cb *= CB_FAC;
	    cr *= CR_FAC;

	    int lum = (ya[lum_idx1] & 0xFF ) << 16;

	    int red = ( lum + cr );
	    red = (( red & 0xFF000000 ) == 0 ) ? red & 0xff0000 : ( red > 0xff0000 ) ? 0xFF0000 : 0;

	    int blue = ( lum + cb ) >> 16;
	    blue = ( (blue  & 0xFFFFFF00 ) == 0 ) ? blue : ( blue > 0xff ) ? 0xFF : 0;

	    int green = ( lum - crb_g ) >> 8;
	    green = ((green & 0xFFFF0000 ) == 0 ) ? green & 0xFF00: ( green > 0xFF00 )  ? 0xFF00 : 0 ;

	    pixels[ lum_idx1++ ] = ( 0xFF << 24 ) | red | green | blue;


	    lum = (ya[lum_idx1] & 0xFF ) << 16;

	    red = ( lum + cr );
	    red = (( red & 0xFF000000 ) == 0 ) ? red & 0xff0000 : ( red > 0xff0000 ) ? 0xFF0000 : 0;

	    blue = ( lum + cb ) >> 16;
	    blue = ( (blue  & 0xFFFFFF00 ) == 0 ) ? blue : ( blue > 0xff ) ? 0xFF : 0;

	    green = ( lum - crb_g ) >> 8;
	    green = ((green & 0xFFFF0000 ) == 0 ) ? green & 0xFF00: ( green > 0xFF00 )  ? 0xFF00 : 0 ;

	    pixels[ lum_idx1++ ] = ( 0xFF << 24 ) | red | green | blue;


	    lum = (ya[lum_idx2] & 0xFF ) << 16;

	    red = ( lum + cr );
	    red = (( red & 0xFF000000 ) == 0 ) ? red & 0xff0000 : ( red > 0xff0000 ) ? 0xFF0000 : 0;

	    blue = ( lum + cb ) >> 16;
	    blue = ( (blue  & 0xFFFFFF00 ) == 0 ) ? blue : ( blue > 0xff ) ? 0xFF : 0;

	    green = ( lum - crb_g ) >> 8;
	    green = ((green & 0xFFFF0000 ) == 0 ) ? green & 0xFF00: ( green > 0xFF00 )  ? 0xFF00 : 0 ;

	    pixels[ lum_idx2++ ] = ( 0xFF << 24 ) | red | green | blue;
	    
	    lum = (ya[lum_idx2] & 0xFF ) << 16;

	    red = ( lum + cr );
	    red = (( red & 0xFF000000 ) == 0 ) ? red & 0xff0000 : ( red > 0xff0000 ) ? 0xFF0000 : 0;

	    blue = ( lum + cb ) >> 16;
	    blue = ( (blue  & 0xFFFFFF00 ) == 0 ) ? blue : ( blue > 0xff ) ? 0xFF : 0;

	    green = ( lum - crb_g ) >> 8;
	    green = ((green & 0xFFFF0000 ) == 0 ) ? green & 0xFF00: ( green > 0xFF00 )  ? 0xFF00 : 0 ;

	    pixels[ lum_idx2++ ] = ( 0xFF << 24 ) | red | green | blue;


	    if( lum_idx2 % stride == 0 )
	    {
		lum_idx2 += stride;
		lum_idx1 += stride;
	    }
	}
	
	m_frameBuff.freeFrameBuffer( pixels );

	m_bDisplaying = false;

	return true;
    }
}


	
    

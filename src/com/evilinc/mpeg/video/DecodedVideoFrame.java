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

package com.evilinc.mpeg.video;

import com.evilinc.mpeg.*;

public class DecodedVideoFrame implements IDecodedFrame
{
    private int m_iFrameIdx;
    private int m_iFrameType;

    private byte[] m_Yvals;
    private byte[] m_Uvals;
    private byte[] m_Vvals;

    private int m_iYaddr;
    private int m_iUaddr;
    private int m_iVaddr;

    private int m_iNumPixels;

    private long m_lPts;

    private YUVtoRGB m_yuv;
    
    private static final String SET_PIXELS = "DecodedVideoFrame.setPixels";
    private static final String PLAY       = "DecodedVideoFrame.play";

    public DecodedVideoFrame(int width, int height, YUVtoRGB yuv)
    {
        if( (width <= 0 ) || (height <= 0 ) )
            throw new IllegalArgumentException();

        if( yuv == null )
            throw new NullPointerException();
        
        m_Yvals = new byte[ width * height];
        m_Uvals = new byte[ (width * height) / 2 ];
        m_Vvals = new byte[ m_Uvals.length ];

        m_iNumPixels = width * height;

        m_yuv = yuv;
    }

    public void setPixels(int[][] YUV, int frameIdx, int frameType)
    {
        Statistics.startLog(SET_PIXELS );

        m_iFrameIdx = frameIdx;
        m_iFrameType = frameType;
        
        int Y[] = YUV[0];
        int U[] = YUV[2];
        int V[] = YUV[1];

        
        byte[] yVals = m_Yvals;
        byte[] uVals = m_Uvals;
        byte[] vVals = m_Vvals;

        int numPixels = m_iNumPixels;

	int i;

	for( i = 0 ; i < (numPixels >> 1 ) ; ++i )
	{
	    int val = Y[i];
	    
            yVals[i] = ( val < 0 ) ? 16 : (byte)val;

	    uVals[i] = (byte)( ( U[i] < 0 ) ? 16 : U[i] );
	    vVals[i] = (byte)( ( V[i] < 0 ) ? 16 : V[i] );
	}

	
        for( ; i < numPixels ; ++i )
        {
            int val = Y[i];
            yVals[i] = ( val < 0 ) ? 16 : (byte)val;
        }

        Statistics.endLog(SET_PIXELS );
    }
            
    public int getFrameType()
    {
        return m_iFrameType;
    }

    public int getFrameIndex()
    {
        return m_iFrameIdx;
    }

    public void setPTS(long pts)
    {
        m_lPts = pts;
    }

    public long getPTS()
    {
        return m_lPts;
    }

    public void play()
        throws PlayerException
    {
        Statistics.startLog(PLAY);

        if( ! m_yuv.display(m_Yvals, m_Uvals, m_Vvals ) )
            throw new PlayerException("YUV player failed");

        Statistics.endLog(PLAY);
    }
}
    

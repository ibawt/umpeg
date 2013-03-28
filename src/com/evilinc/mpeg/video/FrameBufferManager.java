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

public class FrameBufferManager
{
    private DecodedFramePool m_framePool;
    private int              m_iFramePoolSize;
    private AudioVideoSync   m_avSync;
    private YUVtoRGB         m_yuv2rgb;
    
    private int              m_iHeight;
    private int              m_iWidth;
    
    public FrameBufferManager(AudioVideoSync avSync, int framePoolSize)
    {
        if( avSync == null)
            throw new NullPointerException();

        if( framePoolSize <= 0 )
           throw new IllegalArgumentException();

        m_avSync = avSync;

        m_iFramePoolSize = framePoolSize;

        //m_framePool = new DecodedFramePool(framePoolSize, new DecodedFrameFactory() );
        
    }

    public void setYUVtoRGB( YUVtoRGB yuv )
    {
	m_yuv2rgb = yuv;
    }
    
    public void init(int width, int height)
    {
        Debug.println(Debug.INFO, "FrameBufferManager.init()");
        m_iWidth = width;
        m_iHeight = height;
        
        m_framePool = new DecodedFramePool( m_iFramePoolSize, new DecodedFrameFactory() );
        m_avSync.setVideoFramePool(m_framePool);

	m_yuv2rgb.setDimensions( width, height );
    }

    public void close()
    {
    }

    public boolean isReady()
    {
	return true;
    }

    public void setPixels(int[][] yuv, int frameIndex, int frameType, long pts)
    {
        DecodedVideoFrame dec = (DecodedVideoFrame)m_framePool.getFrame();
        if( dec == null )
            Debug.println(Debug.ERROR, "FrameBufferManager.setPixels() - DecodedVideoFrame dec = null");
        dec.setPTS(pts);

        dec.setPixels(yuv,frameIndex, frameType );

        m_avSync.pushVideoFrame(dec);
    }

    
    private class DecodedFrameFactory implements IDecodedFrameFactory
    {
        public IDecodedFrame newFrame()
        {
            return new DecodedVideoFrame(m_iWidth, m_iHeight, m_yuv2rgb );
        }
    }
}

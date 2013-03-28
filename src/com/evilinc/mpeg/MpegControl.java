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

import java.io.InputStream;
import com.evilinc.mpeg.audio.*;
import com.evilinc.mpeg.video.*;

public class MpegControl
{
    private static final int AUDIO_FRAME_POOL_SIZE = 8;
    private static final int AUDIO_QUEUE_SIZE = 16;
    private static final int VIDEO_QUEUE_SIZE = 16;
    
    private AudioVideoSync m_avSync;
    private VideoDecoder   m_videoDecoder;
    private AudioDecoder   m_audioDecoder;
    private InputStream    m_inputStream;
    private MpegScan       m_mpegScan;
    private IOTool         m_ioTool;
    
    /**
     * Creates a new <code>MpegControl</code> instance.
     *
     */
    public MpegControl()
    {
        m_avSync = new AudioVideoSync(AUDIO_QUEUE_SIZE, VIDEO_QUEUE_SIZE );
        //m_ioTool = new IOTool(m_inputStream);
        //m_videoDecoder = new VideoDecoder(m_ioTool, m_avSync );
        //m_audioDecoder = new AudioDecoder(m_ioTool, m_avSync, AUDIO_FRAME_POOL_SIZE );
        //m_mpegScan = new MpegScan(m_ioTool, m_audioDecoder, m_videoDecoder,m_avSync );
    }


    public void openMpeg( InputStream is )
	throws MpegDecodeException
    {
	if( is == null )
	    throw new NullPointerException();


	m_inputStream = is;

	m_ioTool = new IOTool( m_inputStream );
	m_videoDecoder = new VideoDecoder( m_ioTool, m_avSync );
	m_audioDecoder = new AudioDecoder( m_ioTool, m_avSync, AUDIO_FRAME_POOL_SIZE );
	m_mpegScan = new MpegScan( m_ioTool, m_audioDecoder, m_videoDecoder, m_avSync );
	// start it up to get width and height
	m_mpegScan.startDecoding();

	m_mpegScan.setRunMode( true );
    }

    public void stopPlaying()
    {
	m_mpegScan.setRunMode( false );
    }
    
    
    public void setYUVtoRGB( YUVtoRGB yuv2rgb )
    {
	if( yuv2rgb == null )
	    throw new NullPointerException();

	m_videoDecoder.setYUVtoRGB( yuv2rgb );
    }

    public int getWidth()
    {
	return ( m_videoDecoder != null ) ? m_videoDecoder.getWidth() : -1;
    }

    public int getHeight()
    {
	return ( m_videoDecoder != null ) ? m_videoDecoder.getHeight() : -1;
    }
    

    public void start()
        throws MpegDecodeException
    {
        m_mpegScan.startDecoding();
    }

}
            
    

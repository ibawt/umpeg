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
package com.evilinc.mpeg.audio;

import com.evilinc.mpeg.*;

public class AudioDecoder implements IAudio
{
    private static final int SCALE_FACTOR = FP.toFP(32700);
    
    protected IOTool           m_ioTool;
    protected AudioFrameHeader m_audioHeader = new AudioFrameHeader();
    protected CRC16[]          m_crc;
    protected SynthesisFilter  m_filter1;
    protected SynthesisFilter  m_filter2;
    protected AudioVideoSync   m_avSync;
    protected DecodedFramePool m_framePool;
    protected boolean          m_bInitialized;
    protected int              m_iFramePoolSize;
    protected int              m_iChannels;
    protected int              m_iOutputFreq;
    protected Equalizer        m_EQ = new Equalizer();
    private   boolean          m_bDownConvert;
    protected IFrameDecoder    m_frameDecoder;
    
    public AudioDecoder(IOTool ioTool, AudioVideoSync avsync, int framePoolSize)
    {
        if( (ioTool == null ) || (avsync == null ) )
            throw new NullPointerException();

        if( framePoolSize <= 0 )
            throw new IllegalArgumentException();
        
        m_ioTool = ioTool;

        m_avSync = avsync;

        m_iFramePoolSize = framePoolSize;
    }

    public void decodeAudioPacket(int pktLength, long pts )
        throws MpegAudioDecodeException
    {
        m_audioHeader.read(m_ioTool, m_crc );

        if( !m_bInitialized )
            init();

        OutputBuffer out = (OutputBuffer)m_framePool.getFrame();
        m_frameDecoder.decodeFrame(out);

        out.setPTS(pts);

        m_avSync.pushAudioFrame(out);
    }

    private void init()
        throws MpegAudioDecodeException
    {
        int mode = m_audioHeader.getMode();
        int layer = m_audioHeader.getLayer();
        int channels = (mode == AudioFrameHeader.SINGLE_CHANNEL ) ? 1 : 2;

        m_framePool = new DecodedFramePool(m_iFramePoolSize,
                                           new OutputBufferFactory(m_audioHeader.getFreq(), channels ));

        m_avSync.setAudioFramePool(m_framePool);
        
        int[] factors = m_EQ.getBandFactors();

        m_filter1 = new SynthesisFilter(0, SCALE_FACTOR, factors, m_audioHeader.getSampleFreq(),
                                        m_bDownConvert);

        if( channels == 2 )
        {
            m_filter2 = new SynthesisFilter(1, SCALE_FACTOR, factors, m_audioHeader.getSampleFreq(),
                                            m_bDownConvert );
        }
        
        m_iChannels = channels;
        m_iOutputFreq = m_audioHeader.getFreq();


        switch(layer)
        {
        case 1:
            m_frameDecoder = new LayerIDecoder(m_ioTool, m_audioHeader, m_filter1, m_filter2,
                                               OutputChannels.BOTH_CHANNELS );
            break;
        case 0:
            m_frameDecoder = new LayerIIDecoder(m_ioTool, m_audioHeader, m_filter1, m_filter2,
                                                OutputChannels.BOTH_CHANNELS );
            break;
        case 2:
            throw new MpegAudioDecodeException("Layer 3 Audio not supported");
        default:
            throw new MpegAudioDecodeException("Unknown layer type: " + layer );
        }
        
        m_bInitialized = true;
    }
}

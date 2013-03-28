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

import java.io.IOException;
import com.evilinc.mpeg.audio.*;
import com.evilinc.mpeg.video.*;

/**
 * Describe class <code>MpegScan</code> here.
 *
 */
public class MpegScan
{
    /**
     * MPEG Packet Layer constants
     */
    private static final int PACK_START_CODE          = 0x1BA;
    private static final int SYSTEM_HEADER_START_CODE = 0x1BB;
    private static final int PACKET_START_CODE_PREFIX = 0x1;
    private static final int ISO_11172_END_CODE       = 0x1B9;

    /**
     * Stream ID Values
     */
    private static final int RESERVED_STREAM = 0xBC;
    private static final int PRIVATE_STREAM1 = 0xBD;
    private static final int PADDING_STREAM  = 0xBE;
    private static final int PRIVATE_STREAM2 = 0xBF;

    /**
     * System Clock Reference
     */
    private long m_lSysClkRef;
    private int  m_iMuxRate;

    /**
     * System Header
     */
    private int     m_iRateBound;
    private int     m_iAudioBound;
    private boolean m_bFixedFlag;
    private boolean m_bCSPSFlag;
    private boolean m_bSysAudioLck;
    private boolean m_bSysVideoLck;
    private boolean m_bVidBound;

    /**
     * A/V offsets
     */
    private long m_lFirstAudioPTS = -1;
    private long m_lFirstVideoPTS = -1;


    private long m_lLastPTS;
    
    /**
     * I/O Tool
     */
    private IOTool      m_ioTool;

    private AudioDecoder m_audioDecoder;
    private VideoDecoder m_videoDecoder;
    private AudioVideoSync m_avSync;

    private boolean m_bRunMode;
    
    
    /**
     * String constants for Statistics
     */
    private static final String DECODE_STRING = "MpegScan.startDecoding";
    private static final String PARSE_PACK_STRING = "MpegScan.parsePack";
    private static final String PARSE_PACKET_STRING = "MpegScan.parsePacket";
    private static final String PARSE_SYSTEM_HEADER = "MpegScan.parseSystemHeader";
    private static final String PARSE_TIME_STAMPS = "MpegScan.parseTimeStamps";
   
    public MpegScan(IOTool ioTool, AudioDecoder audio, VideoDecoder video, AudioVideoSync avSync)
    {
        if( ( ioTool == null ) || ( audio == null ) ||
            ( video == null ) || ( avSync == null ) )
            throw new NullPointerException();

        m_ioTool = ioTool;
        m_audioDecoder = audio;
        m_videoDecoder = video;
        m_avSync = avSync;
    }

    private void decodeAudio(int pktLength)
        throws MpegDecodeException
    {
        m_audioDecoder.decodeAudioPacket(pktLength, m_lLastPTS );
    }

    private void decodeVideo( int pktLength )
        throws MpegDecodeException
    {
        m_videoDecoder.decodeVideoPacket(pktLength, m_lLastPTS );
    }

    public void setRunMode( boolean state )
    {
	m_bRunMode = state;
	m_videoDecoder.setRunMode( state );
    }
    
    private boolean m_bAudioOnly;

    /**
     *  <code>startDecoding</code>
     *
     *  This method initiates the decoding of the MPEG.
     *  It will loop through the system stream looking
     *  for packs or video only streams and pass control
     *  to the apprioriate method.
     *  
     * @exception MpegDecodeException if an error occurs
     */
    public synchronized void startDecoding()
        throws MpegDecodeException
    {
        Statistics.startLog(DECODE_STRING);
        try
        {
	    if( !m_bRunMode )
		m_ioTool.nextStartCode();

            if( (m_ioTool.nextBits(PACK_START_CODE, 32 ) ) && ( !m_bAudioOnly ) )
            {
                Debug.println(Debug.INFO, "Found PACK_START_CODE");
                // System stream decode packs
                while( parsePack() )
                {
                    try
                    {
			
                        m_avSync.schedule();
                    }
                    catch(PlayerException e)
                    {
                        throw new MpegDecodeException(e.getMessage() );
                    }
                }
            }
            else
            {
                Debug.println(Debug.INFO, "Only Video Packets");
		m_bAudioOnly = true;
                // Only video packets
                m_avSync.setNoAudio(true);
                
                while( true )
                {
                    if( m_ioTool.isEOF() || m_ioTool.nextBits(ISO_11172_END_CODE, 32 ) )
                    {
                        break;
                    }

                    m_videoDecoder.decodeVideoPacket(-1,-1 );

                    try
                    {
                        m_avSync.schedule();
                    }
                    catch(PlayerException e)
                    {
                        throw new MpegDecodeException(e.getMessage() );
                    }

		    if( !m_bRunMode )
		    {
			Statistics.endLog(DECODE_STRING );
			return;
		    }
		    
                }
            }
        }
        catch(IOException e)
        {
            Debug.println(Debug.ERROR,
                          "Caught IOException in MpegScan.startDecoding(): " + e.getMessage());
            throw new MpegDecodeException( e.getMessage() );
        }
        Statistics.endLog(DECODE_STRING);
    }

    
    /**
     *  <code>parsePack</code>
     *
     *  Parses a "PACK" ( packet with headers ).
     *  Fills in system clock reference, mux rate.
     *  Will parse packets until next "PACK".
     *  
     *  
     * @return a <code>boolean</code> false if we're done
     * @exception MpegDecodeException if an error occurs
     */
    private boolean parsePack()
        throws MpegDecodeException, IOException
    {
        Statistics.startLog(PARSE_PACK_STRING);
        
        // Throw away packet start code
        m_ioTool.skipBits(32);

        if( m_ioTool.getBits(4) != 0x2 )
        {
            Debug.println(Debug.ERROR,
                          "Synchronization error in pack");

            throw new MpegDecodeException("Synchronization error in pack");
            //return false;
        }

	
        m_lSysClkRef = m_ioTool.getBits(3) << 30;
        m_ioTool.skipBits(1);
        m_lSysClkRef |= m_ioTool.getBits(15) << 15;
        m_ioTool.skipBits(1);
        m_lSysClkRef |= m_ioTool.getBits(15);
        m_ioTool.skipBits(2);

        m_iMuxRate = m_ioTool.getBits(22);
        m_ioTool.skipBits(1);

        m_lSysClkRef = ( m_lSysClkRef > 0 ) ? ( m_lSysClkRef / 90 ) : -1;

        m_iMuxRate *= 50;

        if( m_ioTool.nextBits(SYSTEM_HEADER_START_CODE, 32 ) )
        {
            parseSystemHeader();
        }

        boolean flag = true;

        while( flag )
        {
            if( m_ioTool.isEOF() || m_ioTool.nextBits(ISO_11172_END_CODE, 32 ) )
            {
                flag = false;
                break;
            }
            else if( m_ioTool.nextBits(PACK_START_CODE, 32 ) )
            {
                flag = true;
                break;
            }
            else
            {
                parsePacket();
            }
        }

        Statistics.endLog(PARSE_PACK_STRING);
        return flag;
    }

    /**
     *  <code>parsePacket</code>
     *
     *  Parses the packet and sends control
     *  to the appr. decoder.
     *
     * @exception MpegDecodeException if an error occurs
     */
    private void parsePacket()
        throws MpegDecodeException, IOException
    {
        Statistics.startLog(PARSE_PACKET_STRING);
	System.out.println("Parsing packet");
        if(m_ioTool.getBits(24) != 0x1 )
        {
            Debug.println(Debug.ERROR, "Synchronization error in packet");
            throw new MpegDecodeException("Synchronization error in packet");
        }

        int streamId = m_ioTool.getBits(8);
        int pktLength = m_ioTool.getBits(16);

        if( streamId != PRIVATE_STREAM2 )
        {
            pktLength -= parseTimeStamps();
        }
                
        if(( streamId & 0xE0 ) == 0xC0 )
        {
            // Audio Stream
            //pktLength -= m_ioTool.getReadBytes();
            decodeAudio( pktLength );
        }
        else
        {
        
            if( (0xF0 & streamId) == 0xE0 )
            {
                decodeVideo(pktLength );
            }
            else if( ( 0xF0 & streamId ) == 0xF0 )
            {
                // Reserved Stream ID
                ;
            }
            else
            {
                switch(streamId)
                {
                case RESERVED_STREAM:
                case PRIVATE_STREAM1:
                case PADDING_STREAM:
                case PRIVATE_STREAM2:
                    break;
                default:
                    Debug.println(Debug.ERROR, "Unknown Stream: " + streamId);
                    throw new MpegDecodeException("Unknown Stream: " + streamId);
                }
            }
        }
        Statistics.endLog(PARSE_PACKET_STRING);
    }

    /**
     *  <code>parseSystemHeader</code>
     *
     *  Parses the system header field.
     *  
     */
    private void parseSystemHeader()
        throws IOException
    {
        Statistics.startLog(PARSE_SYSTEM_HEADER);
        
        m_ioTool.skipBits(32); // system header
        m_ioTool.skipBits(17); // header length + marker

        m_iRateBound = m_ioTool.getBits(22);
        m_ioTool.skipBits(1);

        m_iAudioBound = m_ioTool.getBits(6);
        m_bFixedFlag = m_ioTool.getBits(1) == 1;
        m_bCSPSFlag = m_ioTool.getBits(1) == 1;
        m_bSysAudioLck = m_ioTool.getBits(1) == 1;
        m_bSysVideoLck = m_ioTool.getBits(1) == 1;

        m_ioTool.skipBits(1);
        m_bVidBound = m_ioTool.getBits(5) == 1;

        m_ioTool.skipBits(8); // reserved

        while( m_ioTool.nextBits(1,1) )
        {
            m_ioTool.skipBits(24);
        }

        Statistics.endLog(PARSE_SYSTEM_HEADER);
    }


    /**
     *  <code>parseTimeStamps</code>
     *
     *  Parses the time stamp values
     *
     *  NOTE: dts isn't used at the moment
     *
     * @return an <code>int</code> representing how
     *         much of the packet we have read.
     * @exception MpegDecodeException if an error occurs
     */
    private int parseTimeStamps()
        throws MpegDecodeException, IOException
    {
        Statistics.startLog(PARSE_TIME_STAMPS);
        
        int pktLength = 0;
        long pts = -1, dts = -1;
        
        while( m_ioTool.nextBits( 0xFF, 8 ) )
        {
            m_ioTool.skipBits(8);
                pktLength++;
        }
        
        if( m_ioTool.nextBits(0x1, 2) )
        {
            m_ioTool.skipBits(16);
            pktLength += 2;
        }
        
        if( m_ioTool.nextBits(0x2, 4) )
        {
            m_ioTool.skipBits(4);
            pts = m_ioTool.getBits(3) << 30;
            m_ioTool.skipBits(1);
            pts |= m_ioTool.getBits(15) << 15;
            m_ioTool.skipBits(1);
            pts |= m_ioTool.getBits(15);
            m_ioTool.skipBits(1);
            pktLength += 5;
        }
        else if( m_ioTool.nextBits(0x3, 4 ) )
        {
            m_ioTool.skipBits(4);
            pts = m_ioTool.getBits(3) << 30;
            m_ioTool.skipBits(1);
            pts |= m_ioTool.getBits(15) << 15;
            m_ioTool.skipBits(1);
            pts |= m_ioTool.getBits(15);
            m_ioTool.skipBits(5);
            
            dts = m_ioTool.getBits(3) << 30;
            m_ioTool.skipBits(1);
            dts |= m_ioTool.getBits(15) << 15;
            m_ioTool.skipBits(1);
            dts |= m_ioTool.getBits(15);
            m_ioTool.skipBits(1);
            
            pktLength += 10;
        }
        else if( m_ioTool.getBits(8) != 0x0F )
        {
            Debug.println(Debug.ERROR, "Synchronization Error");
            throw new MpegDecodeException("Synchronization Error");
        }
        else
        {
            pktLength++;
        }

        pts = (pts > 0 ) ? ( pts / 90 ) : -1;
        if( m_lFirstVideoPTS == -1 )
        {
            m_lFirstVideoPTS = pts;
        }

        if( m_lFirstVideoPTS == -1 )
        {
            m_lFirstVideoPTS = pts;
        }

        m_lLastPTS = pts;

        Statistics.endLog(PARSE_TIME_STAMPS);
        
        return pktLength;
    }
}                    
                

            
            
        

            
            

    
    

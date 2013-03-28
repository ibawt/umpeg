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
import java.io.*;

public class VideoDecoder implements IVideo
{
    private static final int FRAME_POOL_SIZE = 16;

    private static final int ISO_11172_END_CODE       = 0x1B9;
    protected IOTool  m_ioTool;
    protected Huffman m_huf;
    protected int[]   m_nullMatrix = new int[64];

    protected int[]   m_intraMatrix =
    {
        8, 16, 19, 22, 26, 27, 29, 34,
        16, 16, 22, 24, 27, 29, 34, 37,
        19, 22, 26, 27, 29, 34, 34, 38,
        22, 22, 26, 27, 29, 34, 37, 40,
        22, 26, 27, 29, 32, 35, 40, 48,
        26, 27, 29, 32, 35, 40, 48, 58,
        26, 27, 29, 34, 38, 46, 56, 69,
        27, 29, 35, 38, 46, 56, 69, 83
    };

    protected int[]  m_zigzag =
    {
        0,  1,  8, 16,  9,  2,  3, 10,
        17, 24, 32, 25, 18, 11,  4,  5,
        12, 19, 26, 33, 40, 48, 41, 34,
        27, 20, 13,  6,  7, 14, 21, 28,
        35, 42, 49, 56, 57, 50, 43, 36,
        29, 22, 15, 23, 30, 37, 44, 51,
        58, 59, 52, 45, 38, 31, 39, 46,
        53, 60, 61, 54, 47, 55, 62, 63
    };

    protected IDCT m_IDCT = new IDCT();

    protected int[]   m_dctRecon = new int[64];
    protected int[]   m_nonIntraMatrix = new int[64];
    protected boolean m_lumBlock;
    
    protected int       m_dctDcCrPast;
    protected int       m_dctDcCbPast;
    protected int       m_dctDcYPast;

    protected int[][][] m_PelBuffer;
    protected int       m_akIdx = 0, m_predIdx = -1, m_backIdx = -1;

    // MPEG paramters
    protected int m_iWidth;
    protected int m_iHeight;
    protected int m_iAspectRatio;
    protected int m_iPicRate;
    protected int m_iMbWidth;
    protected int m_iMbHeight;
    protected int m_iBitRate;
    protected int m_iVBVbuffer;
    protected boolean m_bConstParam;
    protected boolean m_bQuantMatrix;
    protected int m_iHour;
    protected int m_iMinute;
    protected int m_iSecond;
    protected int m_iPictCount;
    protected boolean m_bDropFlag;
    protected boolean m_bClosedGroup;
    protected boolean m_bBrokenLink;
    protected int m_iTempRef;
    protected int m_iPicType;
    protected int m_iFrameNrOffset = -1;
    protected int m_iFrameNr;
    protected int m_iVBVdelay;

    // Forward predication
    protected boolean m_bFullPelForwVector;
    protected int m_iForwFcode, m_iForwardF, m_iForwardRSize;
    protected int m_iMotionHorizForwCode, m_iMotionHorizForwR;
    protected int m_iMotionVertiForwCode, m_iMotionVertiForwR;

    // Backward predication
    protected boolean m_bFullPelBackVector;
    protected int m_iBackFcode, m_iBackF, m_iBackRSize;
    protected int m_iMotionHorizBackCode, m_iMotionHorizBackR;
    protected int m_iMotionVertiBackCode, m_iMotionVertiBackR;
    
    
    protected int m_iQuantScale;
    protected int m_iMacroBlockAddress, m_iPastIntraAddress;
    protected int m_iMbRow, m_iMbCol;

    protected boolean m_bMacroBlockPattern;
    protected boolean m_bMacroBlockMotionForw, m_bMacroBlockMotionBack;
    protected boolean m_bMacroBlockQuant, m_bMacroBlockIntra;

    protected int[] m_iPel1 = new int[16*16 + 2*8*8];
    protected int[] m_iPel2 = new int[16*16 + 2*8*8];

    protected MotionData m_forward = new MotionData();
    protected MotionData m_backward = new MotionData();

    private int m_iPixelPerLumLine, m_iPixelPerColLine;
    private int m_iLumYincr, m_iColYincr;


    private FrameBufferManager m_frameMgr;
    private AudioVideoSync     m_avSync;

    private long               m_lPTS;

    private boolean m_bRunMode;
    
    private static final String DECODE_VIDEO_STRING = "VideoDecoder.decodeVideoPacket";
    private static final String PARSE_SEQ_HEADER_STRING = "VideoDecoder.parseSequenceHeader";
    private static final String PARSE_GOP_STRING = "VideoDecoder.parseGroupOfPictures";
    private static final String PARSE_PICTURE = "VideoDecoder.parsePicture";
    private static final String PARSE_SLICE = "VideoDecoder.parseSlice";
    private static final String PARSE_MACRO_BLOCK = "VideoDecoder.parseMacroBlock";
    private static final String PARSE_BLOCK = "VideoDecoder.parseBlock";
    private static final String CORRECT_LUM_PIXEL_STRING = "VideoDecoder.correctLumPixel";
    private static final String CORRECT_COL_PIXEL_STRING = "VideoDecoder.correctColPixel";
    private static final String SET_COL_PIXEL_STRING = "VideoDecoder.setColPixel";
    private static final String SET_LUM_PIXEL_STRING = "VideoDecoder.setLumPixel";
    
    public VideoDecoder(IOTool ioTool, AudioVideoSync avSync)
    {
        Debug.println(Debug.INFO, "VideoDecoder");
        if( ( ioTool == null ) || ( avSync == null ) )
            throw new NullPointerException();

        m_ioTool = ioTool;
        
        for( int i = 0 ; i < m_nonIntraMatrix.length ; ++i)
        {
            m_nonIntraMatrix[i] = 16;
        }

        Debug.println(Debug.INFO, "Initializing matrices");
        m_IDCT.norm(m_intraMatrix);
        m_IDCT.norm(m_nonIntraMatrix);


        m_huf = new Huffman(ioTool);
        Debug.println(Debug.INFO, "Huffman");
        m_avSync = avSync;

        m_frameMgr = new FrameBufferManager(avSync, FRAME_POOL_SIZE );
        Debug.println(Debug.INFO, "FrameBufferManager");
    }

    public void setRunMode(boolean state)
    {
	m_bRunMode = state;
    }
    
    

    private void setDim(int width, int height, int oWidth, int oHeight )
    {
        m_frameMgr.init(width, height );
    }
    
    public void decodeVideoPacket(int pktLength, long pts)
        throws MpegVideoDecodeException
    {
        Statistics.startLog(DECODE_VIDEO_STRING);

        m_lPTS = pts;

	
        try
        {
	    if( !m_bRunMode )
		m_ioTool.nextStartCode();
        
            do
            {
		if( !m_bRunMode )
		{
		    parseSequenceHeader();
		    Statistics.endLog(DECODE_VIDEO_STRING );
		    return;
		}
		

                if( m_PelBuffer == null )
                {
                    setDim(m_iMbWidth*16, m_iMbHeight * 16, m_iWidth, m_iHeight);

                    m_PelBuffer = new int[3][3][m_iMbWidth*16*m_iMbHeight*16];
                
                    m_iPixelPerLumLine = m_iMbWidth << 4;
                    m_iPixelPerColLine = m_iMbWidth << 3;
                    m_iLumYincr = m_iPixelPerLumLine - 8;
                    m_iColYincr = m_iPixelPerColLine - 8;

                    m_forward.init( m_iPixelPerLumLine,
                                    m_iPixelPerColLine,
                                    m_iPixelPerLumLine - 16,
                                    m_iColYincr);

                    m_backward.init( m_iPixelPerLumLine,
                                     m_iPixelPerColLine,
                                     m_iPixelPerLumLine - 16,
                                     m_iColYincr);
                }
		

                while( (!m_ioTool.isEOF()) && m_ioTool.nextBits(GOP_START_CODE,32 ) )
                    parseGroupOfPictures();
            
            }
            while( (!m_ioTool.isEOF() ) && m_ioTool.nextBits(SEQ_START_CODE, 32 ) );

        }
        catch(IOException e)
        {
            throw new MpegVideoDecodeException(e.getMessage() );
        }
        finally
        {
            m_frameMgr.close();
        }
        Statistics.endLog(DECODE_VIDEO_STRING);
    }

    public int getWidth()
    {
	return m_iWidth;
    }

    public int getHeight()
    {
	return m_iHeight;
    }

    public void setYUVtoRGB(YUVtoRGB y)
    {
	m_frameMgr.setYUVtoRGB( y );
    }
    
    
    private void parseSequenceHeader()
        throws MpegVideoDecodeException, IOException
    {
        Statistics.startLog(PARSE_SEQ_HEADER_STRING);

	if( m_ioTool.isEOF() )
	    return;
	
        if( m_ioTool.getBits(32) != SEQ_START_CODE )
        {
            throw new MpegVideoDecodeException("SEQ_START_CODE expected");
        }

	Debug.println(Debug.INFO, "SEQ_START_CODE");

        m_iWidth = m_ioTool.getBits(12);
        m_iHeight = m_ioTool.getBits(12);

	//System.out.println(m_iWidth + "x" + m_iHeight );
	
        m_iMbWidth = (m_iWidth + 15 ) / 16;
        m_iMbHeight = (m_iHeight + 16 ) / 16;

        m_iAspectRatio = m_ioTool.getBits(4);
        m_iPicRate = m_ioTool.getBits(4);
        m_iBitRate = m_ioTool.getBits(18);

        m_ioTool.skipBits(1);

        m_iVBVbuffer = m_ioTool.getBits(10);
        m_bConstParam = m_ioTool.getBits(1) == 1;
        m_bQuantMatrix = m_ioTool.getBits(1) == 1;

        if( m_bQuantMatrix )
        {
            for(int i = 0 ; i < 64 ; ++i)
                m_intraMatrix[i] = m_ioTool.getBits(8) & 0xFF;
            m_IDCT.norm(m_intraMatrix);
        }

        m_bQuantMatrix = m_ioTool.getBits(1) == 1;

        if( m_bQuantMatrix )
        {
            for( int i = 0 ; i < 64 ; ++i)
                m_nonIntraMatrix[i] = m_ioTool.getBits(8) & 0xFF;
            m_IDCT.norm(m_nonIntraMatrix);
        }
        m_ioTool.nextStartCode();
        while( m_ioTool.nextBits(USER_START_CODE, 32 ) )
        {
            m_ioTool.skipBits(32);
            while( ! m_ioTool.nextBits(0x1, 24 ) )
            {
                m_ioTool.skipBits(8);
            }
        }

        Statistics.endLog(PARSE_SEQ_HEADER_STRING);
    }

    private void parseGroupOfPictures()
        throws MpegVideoDecodeException, IOException
    {
        Statistics.startLog(PARSE_GOP_STRING);

        if( m_ioTool.getBits(32) != GOP_START_CODE )
        {
            throw new MpegVideoDecodeException("GOP_START_CODE expected");
        }

	Debug.println(Debug.INFO, "GOP_START_CODE");

        m_bDropFlag = m_ioTool.getBits(1) == 1;
        m_iHour = m_ioTool.getBits(5);
        m_iMinute = m_ioTool.getBits(6);
        m_ioTool.skipBits(1);
        m_iSecond = m_ioTool.getBits(6);
        m_iPictCount = m_ioTool.getBits(6);
        m_bClosedGroup = m_ioTool.getBits(1) == 1;
        m_bBrokenLink = m_ioTool.getBits(1) == 1;

        m_ioTool.nextStartCode();
            
        if( m_ioTool.nextBits( EXT_START_CODE, 32 ) )
        {
            while( ! m_ioTool.nextBits(0x1, 24 ) )
            {
                m_ioTool.getBits(8);
            }
            throw new MpegVideoDecodeException("MPEG-2 not supported");
        }

        if( m_ioTool.nextBits(USER_START_CODE, 32 ) )
        {
            while( ! m_ioTool.nextBits(0x1, 24 ) )
                m_ioTool.getBits(8);
        }

        m_iFrameNrOffset = m_iFrameNr + 2;

        do
        {
            parsePicture();
            m_avSync.schedule();

        } while( (!m_ioTool.isEOF() ) && m_ioTool.nextBits(PICTURE_START_CODE, 32 ) );

        Statistics.endLog(PARSE_GOP_STRING);
    }


    private void parsePicture()
        throws MpegVideoDecodeException, IOException
    {
        Statistics.startLog(PARSE_PICTURE);
        
        if( m_ioTool.getBits(32) != PICTURE_START_CODE )
            throw new MpegVideoDecodeException("PICTURE_START_CODE expected");

        m_iTempRef = m_ioTool.getBits(10);

        m_iFrameNr = m_iFrameNrOffset + m_iTempRef;

        m_iPicType = m_ioTool.getBits(3);

        m_iVBVdelay = m_ioTool.getBits(16);

        if(m_iPicType != B_TYPE )
        {
            m_predIdx = m_backIdx;
        }

        if( m_iPicType != I_TYPE )
        {
            if( m_predIdx == -1 )
            {
                Debug.println(Debug.WARNING, "No predicative frame in P_FRAME");
                m_predIdx = ( m_akIdx + 2) % 3;
            }

            m_bFullPelForwVector = m_ioTool.getBits(1) == 1;
            m_iForwFcode = m_ioTool.getBits(3);
            m_iForwardRSize = m_iForwFcode - 1;
            m_iForwardF = 1 << m_iForwardRSize;

            m_forward.setPicData(m_iForwardF, m_bFullPelForwVector);
        }

        if( m_iPicType == B_TYPE )
        {
            if( m_backIdx == -1 )
            {
                Debug.println(Debug.WARNING, "No backward predicative frame in B_FRAME");
                m_backIdx = (m_akIdx + 1 ) % 3;
            }
            m_bFullPelBackVector = m_ioTool.getBits(1) == 1;
            m_iBackFcode = m_ioTool.getBits(3);
            m_iBackRSize = m_iBackFcode - 1;
            m_iBackF = 1 << m_iBackRSize;

            m_backward.setPicData(m_iBackF, m_bFullPelBackVector);
            
        }

        while( m_ioTool.nextBits(0x1,1) )
        {
            m_ioTool.skipBits(8);
        }

        m_ioTool.skipBits(1);
        m_ioTool.nextStartCode();

        if( m_ioTool.nextBits(EXT_START_CODE, 32 ) )
        {
            m_ioTool.skipBits(32);
            while( ! m_ioTool.nextBits(0x1, 24 ) )
            {
                m_ioTool.skipBits(8);
            }
        }

        if( m_ioTool.nextBits(USER_START_CODE, 32 ) )
        {
            m_ioTool.skipBits(32);
            while( ! m_ioTool.nextBits(0x1, 24 ) )
                m_ioTool.skipBits(8);
        }

        if( m_iPicType == 4)
        {
            throw new MpegVideoDecodeException("D Frames unsupported");
        }

        int start_c;
        
        do
        {
            parseSlice();
            start_c = m_ioTool.peekBits(32);
        }
        while( ( start_c >= SLICE_MIN_START_CODE ) && ( start_c <= SLICE_MAX_START_CODE) );

        m_frameMgr.setPixels( m_PelBuffer[m_akIdx], m_iFrameNr, m_iPicType, m_lPTS);

        if( m_iPicType != B_TYPE )
        {
            m_backIdx = m_akIdx;
            m_akIdx = ( m_akIdx + 1 ) % 3;
        }
        Statistics.endLog(PARSE_PICTURE);
    }    
                   
    private void parseSlice()
        throws MpegVideoDecodeException, IOException
    {
        Statistics.startLog(PARSE_SLICE);

        int k = m_ioTool.getBits(32);

        if( ( k < SLICE_MIN_START_CODE ) || ( k > SLICE_MAX_START_CODE ) )
            throw new MpegVideoDecodeException("SLICE_START_CODE expected");
        
        m_iPastIntraAddress = -2;

        m_dctDcYPast = m_dctDcCbPast = m_dctDcCrPast = 1024;

        m_forward.resetPrev();
        m_backward.resetPrev();

        m_iMacroBlockAddress = (( k & 0xFF ) - 1 ) * m_iMbWidth -1;

        m_iQuantScale = m_ioTool.getBits(5);

        while( m_ioTool.nextBits(1,1) )
        {
            m_ioTool.skipBits(9);
        }

        m_ioTool.skipBits(1);

        int b_nr = 0;
        do
        {
            parseMacroBlock(b_nr++);
        }
        while( ! m_ioTool.nextBits(0x0, 23 ) );

        m_ioTool.nextStartCode();

        Statistics.endLog(PARSE_SLICE );
    }

    /**
     * The method <code>parseMacroBlock</code> parses a macroblock according
     * to ISO 11172-2. It is one of the most complex methods because of
     * the great variety of the constitution of a macroblock. The
     * constitution and existence of the the most information fields
     * depends on the constitution and existence of information fields
     * before.
     * Furthermore the decoding process is controlled by this method.
     * In some situations some variables must be reset to some default
     * values or in case of skipped macroblocks implizit values must be
     * applied.
     * Bear in mind that some variables used in this method are member
     * (class) variables for later reference!
     *
     * @param an <code>int</code> value
     * @exception MpegVideoDecodeException if an error occurs
     */
    private void parseMacroBlock(int b_nr)
        throws MpegVideoDecodeException, IOException
    {
        Statistics.startLog(PARSE_MACRO_BLOCK);

        int cbp = CODED_BLOCK_PATTERN_DEFAULT;
        
        // Skip macro block escape
        while( m_ioTool.nextBits(0xF, 11 ) )
        {
            m_ioTool.skipBits(11);
        }

        // If the macro block increment is > 1 some blocks are skipped
        int inc = 0;

        // macroblock skipping
        while( m_ioTool.nextBits(0x8, 11 ) )
        {
            m_ioTool.skipBits(11);
            inc += 33;
        }

        int values[] = m_huf.decode(11, m_huf.macro_block_inc);

        inc += values[2];

        // working variables
        int inc_tmp, mb_a_tmp, mb_r_tmp, mb_c_tmp;
        
        if( inc > 1 )
        {
            // Special treatment for skipped macroblocks
            m_dctDcCrPast = m_dctDcCbPast = m_dctDcYPast = DCT_DEFAULT_VALUES;

            if( (m_iPicType == B_TYPE) && ( b_nr > 0 ) )
            {
                // Motion vectors are still valid
                for( inc_tmp = inc - 1, mb_a_tmp = m_iMacroBlockAddress + 1;
                     inc_tmp-- > 0 ; mb_a_tmp++ )
                {
                    // get macroblock row and col
                    mb_r_tmp = mb_a_tmp / m_iMbWidth;
                    mb_c_tmp = mb_a_tmp % m_iMbWidth;

                    // Forward predication
                    if( m_bMacroBlockMotionForw )
                    {
                        if( ! m_bMacroBlockMotionBack )
                        {
                            m_forward.copyArea(mb_r_tmp, mb_c_tmp, m_PelBuffer[m_predIdx],
                                               m_PelBuffer[m_akIdx] );
                        }
                        else
                        {
                            m_forward.getArea(mb_r_tmp, mb_c_tmp, m_PelBuffer[m_predIdx],
                                              m_iPel1 );
                        }
                        
                    }

                    // Backward predication
                    if( m_bMacroBlockMotionBack )
                    {
                        if( ! m_bMacroBlockMotionForw )
                        {
                            m_backward.copyArea(mb_r_tmp, mb_c_tmp, m_PelBuffer[m_backIdx],
                                                m_PelBuffer[m_akIdx] );
                        }
                        else
                        {
                            m_backward.getArea(mb_r_tmp, mb_c_tmp, m_PelBuffer[m_backIdx],
                                               m_iPel2 );

                            m_backward.putArea(mb_r_tmp, mb_c_tmp, m_iPel1, m_iPel2,
                                               m_PelBuffer[m_akIdx] );
                        }
                    }
                }
            }
            else if( m_iPicType != I_TYPE )
            {
                m_forward.resetPrev();
                if( (b_nr > 0) && (!m_bMacroBlockMotionBack ) )
                {
                    for( inc_tmp = inc - 1, mb_a_tmp = m_iMacroBlockAddress + 1 ;
                         inc_tmp-- > 0 ; mb_a_tmp++ )
                    {
                        mb_r_tmp = mb_a_tmp / m_iMbWidth;
                        mb_c_tmp = mb_a_tmp % m_iMbWidth;
                        m_forward.copyUnchanged(mb_r_tmp, mb_c_tmp, m_PelBuffer[m_predIdx],
                                                m_PelBuffer[m_akIdx] );
                    }
                }
            }
        }
        else
        { // macroblock is not skipped
            ;
        }
        
        m_iMacroBlockAddress += inc;

        m_iMbRow = m_iMacroBlockAddress / m_iMbWidth;
        m_iMbCol = m_iMacroBlockAddress % m_iMbWidth;

        switch(m_iPicType)
        {
        case I_TYPE:
            m_bMacroBlockMotionForw = false;
            m_bMacroBlockMotionBack = false;
            m_bMacroBlockPattern = false;

            m_bMacroBlockIntra = true;

            if( m_ioTool.getBits(1) == 1 )
            {
                m_bMacroBlockQuant = false;
            }
            else
            {
                m_bMacroBlockQuant = true;
                m_ioTool.skipBits(1);
            }
            break;
        case P_TYPE:
            values = m_huf.decode(6, m_huf.p_type_mb_type);

            m_bMacroBlockQuant = values[2] != 0;
            m_bMacroBlockMotionForw = (values[3] == 1);
            m_bMacroBlockMotionBack = false;
            m_bMacroBlockPattern = ( values[4] == 1 );

            if( !( m_bMacroBlockIntra = ( values[5] != 0 ) ) )
            {
                m_dctDcYPast = m_dctDcCbPast = m_dctDcCrPast = 1024;
                cbp = 0;
            }
            break;
        case B_TYPE:
            values = m_huf.decode( 6, m_huf.b_type_mb_type );
            m_bMacroBlockQuant = values[2] != 0;
            m_bMacroBlockMotionForw = (values[3] == 1 );
            m_bMacroBlockMotionBack = (values[4] == 1 );
            m_bMacroBlockPattern = (values[5] == 1 );

            if( !(m_bMacroBlockIntra = (values[6] != 0 ) ) )
            {
                m_dctDcYPast = m_dctDcCbPast = m_dctDcCrPast = 1024;
                cbp = 0;
            }
            break;
        default:
            throw new MpegVideoDecodeException("Unknown frame type: " + m_iPicType );
        }

        if( m_bMacroBlockQuant )
        {
            m_iQuantScale = m_ioTool.getBits(5);
        }

        if( m_bMacroBlockMotionForw )
        {
            values = m_huf.decode( 11, m_huf.motion_code );

            m_iMotionHorizForwCode = values[2];

            if( (m_iForwardF != 1 ) && (m_iMotionHorizForwCode != 0 ) )
            {
                m_iMotionHorizForwR = m_ioTool.getBits(m_iForwardRSize );
            }

            values = m_huf.decode(11, m_huf.motion_code );

            m_iMotionVertiForwCode = values[2];

            if( (m_iForwardF != 1) && (m_iMotionVertiForwCode != 0 ) )
            {
                m_iMotionVertiForwR = m_ioTool.getBits(m_iForwardRSize );
            }

            m_forward.computeMotionVector(m_iMotionHorizForwCode,
                                          m_iMotionVertiForwCode,
                                          m_iMotionHorizForwR,
                                          m_iMotionVertiForwR );

            if( (m_iPicType != B_TYPE) || ( !m_bMacroBlockMotionBack ) )
            {
                m_forward.copyArea(m_iMbRow, m_iMbCol, m_PelBuffer[m_predIdx],
                                   m_PelBuffer[m_akIdx] );

            }
            else
            {
                m_forward.getArea(m_iMbRow, m_iMbCol, m_PelBuffer[m_predIdx], m_iPel1 );
            }
        }
        else if( m_iPicType != B_TYPE )
        {
            m_forward.resetPrev();
        }

        if( m_bMacroBlockMotionBack )
        {
            values = m_huf.decode(11, m_huf.motion_code );

            m_iMotionHorizBackCode = values[2];

            if( (m_iBackF != 1 ) && ( m_iMotionHorizBackCode != 0 ) )
            {
                m_iMotionHorizBackR = m_ioTool.getBits( m_iBackRSize );
            }

            values = m_huf.decode(11, m_huf.motion_code );
            m_iMotionVertiBackCode = values[2];

            if( (m_iBackF != 1 ) && (m_iMotionVertiBackCode != 0 ) )
            {
                m_iMotionVertiBackR = m_ioTool.getBits( m_iBackRSize );
            }
            
            m_backward.computeMotionVector( m_iMotionHorizBackCode,
                                            m_iMotionVertiBackCode,
                                            m_iMotionHorizBackR,
                                            m_iMotionVertiBackR );

            
            if( !m_bMacroBlockMotionForw )
            {
                m_backward.copyArea(m_iMbRow, m_iMbCol, m_PelBuffer[m_backIdx],
                                    m_PelBuffer[m_akIdx] );
            }
            else
            {
                m_backward.getArea(m_iMbRow, m_iMbCol, m_PelBuffer[m_backIdx],m_iPel2 );
                m_backward.putArea(m_iMbRow, m_iMbCol, m_iPel1, m_iPel2, m_PelBuffer[m_akIdx] );
            }
            
        }

        if( m_bMacroBlockPattern )
        {
            values = m_huf.decode(9, m_huf.block_pattern );
            cbp = values[2];
        }

        if( (m_iPicType == P_TYPE) && (!m_bMacroBlockMotionBack ) && (!m_bMacroBlockMotionForw ) )
        {
            m_forward.copyUnchanged(m_iMbRow, m_iMbCol, m_PelBuffer[m_predIdx], m_PelBuffer[m_akIdx] );
        }

        // No luminance in this block
        m_lumBlock = false;

        for(int i = 0 ; i < 6 ; ++i) // all 6 blocks
        {
            if((cbp & ( 1 << ( 5 - i ) ) ) != 0 )
            {
                // Block information supplied
                parseBlock(i);

                if( m_bMacroBlockIntra )
                {
                    if( i < 4 )
                        setLumPixel(i);
                    else
                        setColPixel(i);
                }
                else
                {
                    if( i < 4 )
                        correctLumPixel(i);
                    else
                        correctColPixel(i);
                }
            }
        }

        if( (m_iPicType == B_TYPE ) && (m_bMacroBlockIntra ) )
        {
            m_forward.resetPrev();
            m_backward.resetPrev();
        }

        Statistics.endLog(PARSE_MACRO_BLOCK);
    }
                        

    private void parseBlock(int nr)
        throws MpegVideoDecodeException, IOException
    {
        int idx = 0, size, sign, idx_run = 0, level;
        int coeffCount = 0;
        int pos = 0;
        int values[];
        int pValues;

        Statistics.startLog(PARSE_BLOCK);

        
        for( int i = 64 - 1 ; i >= 0 ; i -= 8)
        {
            m_dctRecon[i] = 0;
            m_dctRecon[i-1] = 0;
            m_dctRecon[i-2] = 0;
            m_dctRecon[i-3] = 0;
            m_dctRecon[i-4] = 0;
            m_dctRecon[i-5] = 0;
            m_dctRecon[i-6] = 0;
            m_dctRecon[i-7] = 0;
        }

        if( m_bMacroBlockIntra )
        {
            if( nr < 4 )
            {
                values = m_huf.decode(7, m_huf.dct_size_luminance );
                size = values[2];

                if( size != 0 )
                {
                    setDctDiff( m_ioTool.getBits(size), size );
                }

                if( m_lumBlock )
                {
                    m_dctDcYPast = m_dctRecon[0] = (m_dctDcYPast + (m_dctRecon[0] << 3 ) );
                }
                else
                {
                    m_lumBlock = true;
                    m_dctRecon[0] <<= 3;

                    if( ( m_iMacroBlockAddress  - m_iPastIntraAddress ) > 1 )
                    {
                        m_dctDcYPast = m_dctRecon[0] += 1024;
                    }
                    else
                    {
                        m_dctDcYPast = m_dctRecon[0] += m_dctDcYPast;
                    }
                }
                m_iPastIntraAddress = m_iMacroBlockAddress;
            }
            else
            {
                values = m_huf.decode(8, m_huf.dct_size_crominance );
                size = values[2];

                if( size != 0 )
                {
                    setDctDiff(m_ioTool.getBits(size), size );
                }
                
                if( nr == 4 )
                {
                    m_dctRecon[0] <<= 3;
                    if( (m_iMacroBlockAddress - m_iPastIntraAddress ) > 1 )
                    {
                        m_dctDcCbPast = m_dctRecon[0] += 1024;
                    }
                    else
                    {
                        m_dctDcCbPast = m_dctRecon[0] += m_dctDcCbPast;
                    }
                }
                else if( nr == 5 )
                {
                    m_dctRecon[0] <<= 3;
                    if( (m_iMacroBlockAddress - m_iPastIntraAddress ) > 1 )
                    {
                        m_dctDcCrPast = m_dctRecon[0] += 1024;
                    }
                    else
                    {
                        m_dctDcCrPast = m_dctRecon[0] += m_dctDcCrPast;
                    }
                }
            }

            m_iPastIntraAddress = m_iMacroBlockAddress;

            if( m_dctRecon[0] != 0 )
                coeffCount = 1;

            // because of the IDCT:
            // DC values are not quantized so the fixed
            // point translation must be performed
            m_dctRecon[0] <<= m_IDCT.VAL_BITS - 3; 
        }
        else
        { // no intra coded block -> first AC value

            // special treatment of the VLC "1"
            if( m_ioTool.nextBits(1,1) )
            {
                idx = 0;
                m_ioTool.skipBits(1);
                sign = level = m_ioTool.getBits(1) == 0 ? 1 : -1;
            }
            else
            {
                //pValues = (m_huf.dctDecode8BtPreSearch() )[3];
                pValues = m_huf.decodeCoeff();
                idx = pValues & 0xFF;

                if( idx == m_huf.DCT_ESCAPE )
                {
                    idx = m_ioTool.getBits(6);

                    if( ( (level = m_ioTool.getBits(8) ) & 0x7F ) == 0 )
                    {
                        level = ( level << 8 ) | m_ioTool.getBits(8);

                        if( (level & 0x8000 ) != 0 )
                            level |= 0xFFFFFF00;
                    }
                    else if( ( level & 0x80 ) != 0 )
                    {
                        level |= 0xFFFFFF00;
                    }
                }
                else
                {
                    level = m_ioTool.getBits(1) == 0 ?
                        (pValues >> 8 ) :
                        -(pValues >> 8 );
                }
                sign = ( level == 0 ) ? 0 : ((level < 0 ) ? -1 : 1 );
            }
            pos = m_zigzag[idx];

            m_dctRecon[pos] = (( level + sign ) * m_iQuantScale * m_nonIntraMatrix[pos] ) >> 3;

            if( level != 0 )
                coeffCount++;
        }

        //pValues = m_huf.dctDecode8BtPreSearch())[3]
        pValues = m_huf.decodeCoeff();

        int i1;
        while( ( idx_run = ( pValues & 0xFF ) ) != m_huf.EOB )
        {
            if( idx_run == m_huf.DCT_ESCAPE )
            {
                idx_run = m_ioTool.getBits(6);
                if( ((level = m_ioTool.getBits(8) ) & 0x7F ) == 0 )
                {
                    level = ( level << 8 ) | m_ioTool.getBits(8);
                    if( ( level & 0x8000 ) != 0 )
                        level |= 0xFFFFFF00;
                }
                else if( (level & 0x80 ) != 0 )
                {
                    level |= 0xFFFFFF00;
                }
                idx += idx_run + 1;
            }
            else
            {
                idx += idx_run + 1;
                level = m_ioTool.getBits(1) == 0 ?
                    (pValues >> 8 ) :
                    -(pValues >> 8 );
            }

            if( idx > 63 )
                idx = 63;
            pos = m_zigzag[idx];
            
            if( m_bMacroBlockIntra )
            {
                m_dctRecon[pos] = ( level * m_iQuantScale * m_intraMatrix[pos] ) >> 3;
            }
            else
            {
                sign = ( level == 0 ) ? 0 : (( level < 0 ) ? -1 : 1 );
                m_dctRecon[pos] = (( level + sign ) * m_iQuantScale * m_nonIntraMatrix[pos] ) >> 3;
            }

            if( level != 0 )
                coeffCount++;

            //pValues = (m_huf.dctDecode8BtPreSearch())[3];
            pValues = m_huf.decodeCoeff();
        }

        if( coeffCount == 1 )
        {
            m_IDCT.invers_dct_special(m_dctRecon, pos );
        }
        else
        {
            m_IDCT.invers_dct(m_dctRecon );
        }

        Statistics.endLog(PARSE_BLOCK );
    }

    private void setDctDiff( int dct_diff, int dct_size )
    {
        if( (dct_diff & ( 1 << ( dct_size - 1 ) ) ) != 0 )
            m_dctRecon[0] = dct_diff;
        else
            m_dctRecon[0] = ((-1) << dct_size ) | ( dct_diff + 1 );
    }

    private void setLumPixel(int nr)
    {
        Statistics.startLog(SET_LUM_PIXEL_STRING);

        int pos = m_iPixelPerLumLine * ((m_iMbRow << 4 ) + ((nr & 0x2 ) << 2 ))  +
                 (m_iMbCol << 4 ) + ((nr & 0x1 ) << 3 );

        int[] pb0 = m_PelBuffer[m_akIdx][0];
        int[] dctRecon = m_dctRecon;

        //Debug.println(Debug.INFO, "setLumPixel: pos = " + pos );
        
        for( int j = 0 ; j < 64 ; j += 8 )
        {
            pb0[pos] = dctRecon[j];
            pb0[pos+1] = dctRecon[j+1];
            pb0[pos+2] = dctRecon[j+2];
            pb0[pos+3] = dctRecon[j+3];
            pb0[pos+4] = dctRecon[j+4];
            pb0[pos+5] = dctRecon[j+5];
            pb0[pos+6] = dctRecon[j+6];
            pb0[pos+7] = dctRecon[j+7];
            pos += m_iPixelPerLumLine;
        }
        Statistics.endLog(SET_LUM_PIXEL_STRING );
    }


    private void setColPixel(int nr )
    {
        Statistics.startLog(SET_COL_PIXEL_STRING );

        int pixelPerColLine = m_iPixelPerColLine;
        int pos = pixelPerColLine * (m_iMbRow << 3 ) + ( m_iMbCol << 3 );
        int dctRecon[] = m_dctRecon;
        int pel[] = ( nr == 4 ) ? m_PelBuffer[m_akIdx][2] : m_PelBuffer[m_akIdx][1];

        
        for( int j = 0 ; j < 64 ; j += 8 )
        {
            pel[pos] = dctRecon[j];
            pel[pos+1] = dctRecon[j+1];
            pel[pos+2] = dctRecon[j+2];
            pel[pos+3] = dctRecon[j+3];
            pel[pos+4] = dctRecon[j+4];
            pel[pos+5] = dctRecon[j+5];
            pel[pos+6] = dctRecon[j+6];
            pel[pos+7] = dctRecon[j+7];

            pos += pixelPerColLine;
        }

        Statistics.endLog(SET_COL_PIXEL_STRING );
    }
        
    
    private void correctLumPixel(int nr)
    {
        Statistics.startLog(CORRECT_LUM_PIXEL_STRING);

        int pos = m_iPixelPerLumLine * (( m_iMbRow << 4 ) + ((nr & 0x2 ) << 2 )) +
            (m_iMbCol << 4 ) + ((nr & 0x1 ) << 3 );

        int[] pb0 = m_PelBuffer[m_akIdx][0];
        int[] dctRecon = m_dctRecon;

        int l = 0;
        for( int i = 0 ; i < 8 ; ++i )
        {
            pb0[pos] += dctRecon[l];
            pb0[pos+1] += dctRecon[l+1];
            pb0[pos+2] += dctRecon[l+2];
            pb0[pos+3] += dctRecon[l+3];
            pb0[pos+4] += dctRecon[l+4];
            pb0[pos+5] += dctRecon[l+5];
            pb0[pos+6] += dctRecon[l+6];
            pb0[pos+7] += dctRecon[l+7];

            pos += 8;
            l += 8;

            pos += m_iLumYincr;
        }

        Statistics.endLog(CORRECT_LUM_PIXEL_STRING);
    }

    private void correctColPixel(int nr)
    {
        Statistics.startLog(CORRECT_COL_PIXEL_STRING );

        int pos = m_iPixelPerColLine * ( m_iMbRow << 3 ) + (m_iMbCol << 3 );

	int ctr = 0;
        if( nr == 4 )
        {
            for( int l = 0 ; l < 8 ; l++ )
            {

                for( int j = 0 ; j < 8 ; j++ )
                    m_PelBuffer[m_akIdx][2][pos++] += m_dctRecon[ctr++];
                pos += m_iColYincr;
            }
        }
        else
        {
            for( int l = 0 ; l < 8 ; l++ )
            {
                for( int k = 0 ; k < 8 ; k++ )
                {
                    m_PelBuffer[m_akIdx][1][pos++] += m_dctRecon[ctr++];
                }
                pos += m_iColYincr;
            }
        }

        Statistics.endLog(CORRECT_COL_PIXEL_STRING);
    }

}

    

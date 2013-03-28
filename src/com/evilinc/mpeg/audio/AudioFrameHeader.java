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
import java.io.*;

public class AudioFrameHeader
{
    public static final int[][] FREQUENCIES =
    {
        { 22050, 24000, 16000, 1 },
        { 44100, 48000, 32000, 1 }
    };

    private static final int BITRATES[][][] = {
        {{0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
          112000, 128000, 144000, 160000, 176000, 192000 ,224000, 256000, 0},
         {0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
          56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0},
         {0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
          56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0}},
        {{0 /*free format*/, 32000, 64000, 96000, 128000, 160000, 192000,
          224000, 256000, 288000, 320000, 352000, 384000, 416000, 448000, 0},
         {0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
          112000, 128000, 160000, 192000, 224000, 256000, 320000, 384000, 0},
         {0 /*free format*/, 32000, 40000, 48000, 56000, 64000, 80000,
          96000, 112000, 128000, 160000, 192000, 224000, 256000, 320000, 0}}
    };	

   private static final String BITRATE_STR[][][] =
   {
       {{"free format", "32 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s",
         "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s",
         "160 kbit/s", "176 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s",
         "forbidden"},
        {"free format", "8 kbit/s", "16 kbit/s", "24 kbit/s", "32 kbit/s",
         "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s",
         "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s",
         "forbidden"},
        {"free format", "8 kbit/s", "16 kbit/s", "24 kbit/s", "32 kbit/s",
         "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s",
         "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s",
         "forbidden"}},
       {{"free format", "32 kbit/s", "64 kbit/s", "96 kbit/s", "128 kbit/s",
         "160 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s", "288 kbit/s",
         "320 kbit/s", "352 kbit/s", "384 kbit/s", "416 kbit/s", "448 kbit/s",
         "forbidden"},
        {"free format", "32 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s",
         "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "160 kbit/s",
         "192 kbit/s", "224 kbit/s", "256 kbit/s", "320 kbit/s", "384 kbit/s",
         "forbidden"},
        {"free format", "32 kbit/s", "40 kbit/s", "48 kbit/s", "56 kbit/s",
         "64 kbit/s", "80 kbit/s" , "96 kbit/s", "112 kbit/s", "128 kbit/s",
         "160 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s", "320 kbit/s",
         "forbidden"}}
   };

    private static final String STR_32   = "32 kHz";
    private static final String STR_16   = "16 kHz";
    private static final String STR_441  = "44.1 kHz";
    private static final String STR_2205 = "22.05 kHz";
    private static final String STR_48   = "48 kHz";
    private static final String STR_24   = "24 kHz";

    private static final String STR_STEREO         = "Stereo";
    private static final String STR_JOINT_STEREO   = "Joint Stereo";
    private static final String STR_DUAL_CHANNEL   = "Dual Channel";
    private static final String STR_SINGLE_CHANNEL = "Single Channel";

    private static final String STR_MPEG1 = "MPEG-1";
    private static final String STR_MPEG2 = "MPEG-2 LSF";
    
    public static final int MPEG2_LSF = 0;
    public static final int MPEG1     = 1;
    public static final int STEREO    = 0;
    public static final int JOINT_STEREO = 1;
    public static final int DUAL_CHANNEL = 2;
    public static final int SINGLE_CHANNEL = 3;
    public static final int FORTYFOUR_POINT_ONE = 0;
    public static final int FORTYEIGHT = 1;
    public static final int THIRTYTWO = 2;

    private int m_iLayer;
    private int m_iProtectionBit;
    private int m_iBitRateIdx;
    private int m_iPaddingBit;
    private int m_iMode;
    private int m_iModeExt;
    private int m_iVersion;
    private int m_iSamplingFreq;
    private int m_iPrivateBit;
    private int m_iNumSubBands;
    private int m_iIntStereoBound;
    private boolean m_bCopyright;
    private boolean m_bOriginal;
    private int m_iOriginal;
    private int m_iEmphasis;
    private CRC16 m_crc = new CRC16();

    private short m_iChecksum;
    private int   m_iFrameSize;
    private int   m_inSlots;
    
    public void read(IOTool ioTool, CRC16[] crc)
        throws MpegAudioDecodeException
    {
        try
        {
            while( ! ioTool.nextBits(0xFFF, 12 ) )
            {
                ioTool.getBits(1);
            }

            m_crc.add_bits(ioTool.peekBits(16), 16 );

            int syncword = ioTool.getBits(12);

            m_iVersion = ioTool.getBits(1);

            m_iLayer = 4 - ioTool.getBits(2);
            m_iProtectionBit = ioTool.getBits(1);
            m_iBitRateIdx = ioTool.getBits(4);
            m_iPaddingBit = ioTool.getBits(1);
            m_iPrivateBit = ioTool.getBits(1);
            m_iMode = ioTool.getBits(2);
            m_iModeExt = ioTool.getBits(2);

            if( m_iMode == JOINT_STEREO )
            {
                m_iIntStereoBound = ( m_iModeExt << 2 ) + 4;
            }

            m_bCopyright = ioTool.getBits(1) == 1;
            m_bOriginal = ioTool.getBits(1) == 1;

            m_iEmphasis = ioTool.getBits(2);
        
        
            if( m_iLayer == 1 )
                m_iNumSubBands = 32;
            else
            {
                int channelBitRate = m_iBitRateIdx;

                if( m_iMode != SINGLE_CHANNEL )
                {
                    if( channelBitRate == 4 )
                        channelBitRate = 1;
                    else
                        channelBitRate -= 4;
                }

                if((channelBitRate == 1 ) || ( channelBitRate == 2 ) )
                {
                    if( m_iSamplingFreq == THIRTYTWO )
                        m_iNumSubBands = 12;
                    else
                        m_iNumSubBands = 8;
                }
                else
                {
                    if( (m_iSamplingFreq == FORTYEIGHT ) || (channelBitRate >= 3 ) &&
                        (channelBitRate <= 5 ) )
                    {
                        m_iNumSubBands = 27;
                    }
                    else
                    {
                        m_iNumSubBands = 30;
                    }
                }
            }

            if( m_iIntStereoBound > m_iNumSubBands )
            {
                m_iIntStereoBound = m_iNumSubBands;
            }

            calFrameSize();

            if( m_iProtectionBit == 0 )
            {
                m_iChecksum = (short) ioTool.getBits(16);
                crc[0] = m_crc;
            }
            else
            {
                crc[0] = null;
            }
        }
        catch(IOException e)
        {
            throw new MpegAudioDecodeException(e.getMessage() );
        }
    }

    
    private void calFrameSize()
    {
        if( m_iLayer == 1 )
        {
            m_iFrameSize = ( 12 * BITRATES[m_iVersion][0][m_iBitRateIdx] ) /
                FREQUENCIES[m_iVersion][m_iSamplingFreq];

            if( m_iPaddingBit != 0 )
                m_iFrameSize++;

            m_iFrameSize <<= 2;
            m_inSlots = 0;
        }
        else
        {
            m_iFrameSize = ( 144 * BITRATES[m_iVersion][m_iLayer - 1 ][m_iBitRateIdx]) /
                FREQUENCIES[m_iVersion][m_iSamplingFreq];

            if( m_iVersion == MPEG2_LSF )
                m_iFrameSize >>= 1;

            if( m_iPaddingBit != 0 )
                m_iFrameSize++;

            if( m_iLayer == 3 )
            {
                if( m_iVersion == MPEG1 )
                {
                    m_inSlots = m_iFrameSize - (( m_iMode == SINGLE_CHANNEL ) ? 17 : 32 )
                        - (( m_iProtectionBit != 0 ) ? 0 : 2 )
                        - 4;

                }
                else
                {
                    m_inSlots = m_iFrameSize - (( m_iMode == SINGLE_CHANNEL ) ? 8 : 17 )
                        - ((m_iProtectionBit != 0 ) ? 0 : 2 )
                        - 4;

                }
            }
            else
            {
                m_inSlots = 0;
            }
        }
        m_iFrameSize -= 4;
    }

        public int getVersion()
    {
        return m_iVersion;
    }

    public int getLayer()
    {
        return m_iLayer;
    }

    public int getBitRateIndex()
    {
        return m_iBitRateIdx;
    }

    public int getSampleFreq()
    {
        return m_iSamplingFreq;
    }

    public int getFreq()
    {
        return FREQUENCIES[m_iVersion][m_iSamplingFreq];
    }

    public int getMode()
    {
        return m_iMode;
    }

    public boolean getProtected()
    {
        return m_iProtectionBit == 0;
    }

    public boolean isCopyright()
    {
        return m_bCopyright;
    }

    public boolean isOriginal()
    {
        return m_bOriginal;
    }

    public boolean isChecksumOk()
    {
        return m_iChecksum == m_crc.checksum();
    }

    public boolean isPadded()
    {
        return m_iPaddingBit == 1;
    }

    public int getSlots()
    {
        return m_inSlots;
    }

    public int getModeExtension()
    {
        return m_iModeExt;
    }


    public String getBitRateString()
    {
        return BITRATE_STR[m_iVersion][m_iLayer - 1 ][m_iBitRateIdx];
    }


    public String getLayerString()
    {
        switch(m_iLayer )
        {
        case 1:
            return "I";
        case 2:
            return "II";
        case 3:
            return "III";
        default:
            return null;
        }
    }

    public String getSampleFreqString()
    {
        switch(m_iSamplingFreq )
        {
        case THIRTYTWO:
            return ( m_iVersion == MPEG1 ) ? STR_32 : STR_16;
        case FORTYFOUR_POINT_ONE:
            return (m_iVersion == MPEG1 ) ? STR_441 : STR_2205;
        case FORTYEIGHT:
            return (m_iVersion == MPEG1 ) ? STR_48 : STR_24;
        default:
            return null;
        }
    }

    public String getVersionString()
    {
        switch(m_iVersion )
        {
        case MPEG1:
            return STR_MPEG1;
        case MPEG2_LSF:
            return STR_MPEG2;
        default:
            return null;
        }
    }

    public int getNumSubBands()
    {
        return m_iNumSubBands;
    }

    public int getIntensityStereoBound()
    {
        return m_iIntStereoBound;
    }
    
}
    
    
    
    

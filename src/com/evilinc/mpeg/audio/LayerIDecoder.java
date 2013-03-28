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


class LayerIDecoder implements IFrameDecoder
{
    protected IOTool           m_ioTool;
    protected AudioFrameHeader m_audioHeader;
    protected SynthesisFilter  m_filter1, m_filter2;
    protected int              m_iWhichChannels;
    protected int              m_iMode;
    protected int              m_iNumSubBands;
    protected Subband[]        m_subbands = new Subband[32];
    protected CRC16[]          m_crc = new CRC16[1];
    protected OutputBuffer     m_buffer;
    

    private static final String DECODE_FRAME = "LayerIDecoder.decodeFrame";
    private static final String READ_ALLOCATION = "LayerIDecoder.readAllocation";
    private static final String READ_SCALE_FACTORS = "LayerIDecoder.readScaleFactors";
    private static final String READ_SAMPLE_DATA = "LayerIDecoder.readSampleData";

    
    public LayerIDecoder(IOTool ioTool, AudioFrameHeader header, SynthesisFilter filtera,
                     SynthesisFilter filterb, int which_ch0 )
    {
        m_ioTool = ioTool;
        m_audioHeader = header;
        m_filter1 = filtera;
        m_filter2 = filterb;
        m_iWhichChannels = which_ch0;

        m_iNumSubBands = m_audioHeader.getNumSubBands();
        m_iMode = m_audioHeader.getMode();

        createSubbands();

    }
        
    protected void createSubbands()
    {
        if( m_iMode == AudioFrameHeader.SINGLE_CHANNEL )
        {
            for(int i = 0 ; i < m_iNumSubBands ; ++i )
                m_subbands[i] = new SubbandLayer1(i);
        }
        else if( m_iMode == AudioFrameHeader.JOINT_STEREO )
        {
            int j;
            for( j = 0 ; j < m_audioHeader.getIntensityStereoBound() ; ++j )
                m_subbands[j] = new SubbandLayer1Stereo(j);

            for( ; j < m_iNumSubBands ; ++j )
                m_subbands[j] = new SubbandLayer1IntensityStereo(j);
        }
        else
        {
            for( int i = 0 ; i < m_iNumSubBands ; ++i )
                m_subbands[i] = new SubbandLayer1Stereo(i);
        }
    }
                
    protected void resetSubbands()
    {
        for( int i = 0 ; i < m_iNumSubBands ; ++i )
        {
            m_subbands[i].reset();
        }
    }

    
    public void decodeFrame(OutputBuffer out)
        throws MpegAudioDecodeException
    {
        Statistics.startLog(DECODE_FRAME );

        m_buffer = out;
        
        resetSubbands();
        readAllocation();

        readScaleFactorSelection();

        if((m_crc != null ) || m_audioHeader.isChecksumOk() )
        {
            readScaleFactors();
            readSampleData();
        }

        Statistics.endLog(DECODE_FRAME);
    }

    protected void readScaleFactorSelection()
    {
        ;
    }
    
    protected void readAllocation()
    {
        Statistics.startLog(READ_ALLOCATION);
        for( int i = 0 ; i < m_iNumSubBands ; ++i )
        {
            m_subbands[i].readAllocation(m_ioTool, m_audioHeader, m_crc[0] );
        }
        Statistics.endLog(READ_ALLOCATION);
    }

    protected void readScaleFactors()
    {
        Statistics.startLog(READ_SCALE_FACTORS);
        for(int i = 0 ; i < m_iNumSubBands ; ++i )
            m_subbands[i].readScaleFactor(m_ioTool, m_audioHeader );

        Statistics.endLog(READ_SCALE_FACTORS);
    }


    protected void readSampleData()
    {
        Statistics.startLog( READ_SAMPLE_DATA );
        
        boolean read_ready = false;
        boolean write_ready = false;

        int mode = m_audioHeader.getMode();

        do
        {
            for(int i = 0 ; i < m_iNumSubBands ; ++i )
            {
                read_ready = m_subbands[i].readSampleData(m_ioTool );
            }

            do
            {
                for( int i = 0 ; i < m_iNumSubBands ; ++i)
                {
                    write_ready = m_subbands[i].putNextSample(m_iWhichChannels,
                                                              m_filter1,
                                                              m_filter2 );
                }

                m_filter1.calculate_pcm_samples(m_buffer);

                if( (m_iWhichChannels == OutputChannels.BOTH_CHANNELS) &&
                    (mode != AudioFrameHeader.SINGLE_CHANNEL ) )
                {
                    m_filter2.calculate_pcm_samples(m_buffer);
                }
            }
            while( ! write_ready );
        } while( !read_ready );

        Statistics.endLog(READ_SAMPLE_DATA );
    }
    
    static abstract class Subband
    {
        public static final int scalefactors[] =
        {
            131072, 104032, 82570, 65536, 52016, 41285, 32768, 26008, 
            20643, 16384, 13004, 10321, 8192, 6502, 5161, 4096, 
            3251, 2580, 2048, 1625, 1290, 1024, 813, 645, 
            512, 406, 323, 256, 203, 161, 128, 102, 
            81, 64, 51, 40, 32, 25, 20, 16, 
            13, 10, 8, 6, 5, 4, 3, 3, 
            2, 2, 1, 1, 1, 1, 1, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 
        };
        
        public abstract void readAllocation (IOTool stream, AudioFrameHeader header, CRC16 crc)
            throws MpegAudioDecodeException;
        public abstract void readScaleFactor (IOTool stream, AudioFrameHeader header)
            throws MpegAudioDecodeException;
        
        public abstract boolean readSampleData (IOTool stream)
            throws MpegAudioDecodeException;
        
        public abstract boolean putNextSample (int channels,
                                               SynthesisFilter filter1,
                                               SynthesisFilter filter2)
            throws MpegAudioDecodeException;
        
        public abstract void reset();
    };

    static class SubbandLayer1 extends Subband
    {
        private static final int TABLE_FACTOR_SIZE = 15;
        private static final int TABLE_OFFSET_SIZE = 15;
        public static final int[] table_factor = new int[TABLE_FACTOR_SIZE];
        public static final int[] table_offset = new int[TABLE_OFFSET_SIZE];

        static
        {
            table_factor[0] = 0;
            for( int i = 1 ; i < TABLE_FACTOR_SIZE ; ++i )
            {
                int number = 1 << i;
                int first = FP.inv( FP.toFP(number ) );
                int second = FP.div( FP.toFP( number << 1 ) , FP.toFP( (number<<1 ) - 1 ) );
                table_factor[i] = FP.mul( first, second );
            }

            table_offset[0] = 0;
            for(int i = 1 ; i < TABLE_OFFSET_SIZE ; ++i)
            {
                int number = 1 << i;
                int first = FP.inv( FP.toFP(number) ) - FP.toFP(1);
                int second = FP.div( FP.toFP(number << 1), FP.toFP( (number << 1) - 1 ) );
                table_offset[i] = FP.mul( first, second );
            }
        }
        
        protected int		 subbandnumber;
        protected int		 samplenumber;
        protected int		 allocation;
        protected int		 scalefactor;
        protected int		 samplelength;
        protected int		 sample;
        protected int		 factor, offset;
        
        /**
         * Construtor.
         */
        public SubbandLayer1(int subbandnumber)
        {
            this.subbandnumber = subbandnumber;
            samplenumber = 0;  
        }
        
        public void reset() {
            samplenumber = 0;  
            allocation = 0;
            scalefactor = 0;
            samplelength = 0;
            sample = 0;
            factor = 0;
            offset = 0;
        }
        

        public void readAllocation(IOTool stream, AudioFrameHeader header, CRC16 crc)
            throws MpegAudioDecodeException
        {
            try
            {
                if ((allocation = stream.getBits (4)) == 15)
                {
                    throw new MpegAudioDecodeException("Stream contains an illegal allocation");
                }
            }
            catch(IOException e)
            {
                throw new MpegAudioDecodeException(e.getMessage() );
            }

            if (crc != null)
                crc.add_bits (allocation, 4);
            if (allocation != 0)
            {
                samplelength = allocation + 1;
                factor = table_factor[allocation];
                offset = table_offset[allocation];
            }
        }
        
        /**
         *
         */
        public void readScaleFactor(IOTool stream, AudioFrameHeader header)
            throws MpegAudioDecodeException
        {
            try
            {
                if (allocation != 0)
                    scalefactor = scalefactors[stream.getBits(6)];
            }
            catch(IOException e)
            {
                throw new MpegAudioDecodeException(e.getMessage() );
            }
        }
        
        /**
         *
         */
        public boolean readSampleData(IOTool stream)
            throws MpegAudioDecodeException
        {
            try
            {
                if (allocation != 0) {
                    sample = FP.toFP( (stream.getBits(samplelength)) );
                }
                if (++samplenumber == 12) {
                    samplenumber = 0;
                    return true;
                }
                return false;
            }
            catch(IOException e)
            {
                throw new MpegAudioDecodeException(e.getMessage() );
            }
        }
        
        /**
         *
         */
        public boolean putNextSample(int channels, SynthesisFilter filter1, SynthesisFilter filter2)
            throws MpegAudioDecodeException
        {
            if ((allocation !=0) && (channels != OutputChannels.RIGHT_CHANNEL)) {
                int scaled_sample = FP.mul( (FP.mul(sample, factor) + offset), scalefactor);
                filter1.input_sample (scaled_sample, subbandnumber);
            }
            return true;
        }
    };
    
    /**
     * Class for layer I subbands in joint stereo mode.
     */
    static class SubbandLayer1IntensityStereo extends SubbandLayer1
    {
        protected int channel2_scalefactor;
        
        /**
         * Constructor
         */
        public SubbandLayer1IntensityStereo(int subbandnumber)
        {
            super(subbandnumber);
        }
        
        /**
         *
         */
        public void readScaleFactor(IOTool stream, AudioFrameHeader header)
            throws MpegAudioDecodeException
        {
            try
            {
                if (allocation != 0) {
                    scalefactor = scalefactors[stream.getBits(6)];
                    channel2_scalefactor = scalefactors[stream.getBits(6)];
                }
            }
            catch(IOException e)
            {
                throw new MpegAudioDecodeException(e.getMessage() );
            }
        }

        /**
         *
         */
        public boolean putNextSample (int channels, SynthesisFilter filter1, SynthesisFilter filter2)
            throws MpegAudioDecodeException
        {
            if (allocation !=0 ) {
                sample = FP.mul(sample, factor) + offset; // requantization
                if (channels == OutputChannels.BOTH_CHANNELS)
                {
                    int sample1 = FP.mul(sample, scalefactor);
                    int sample2 = FP.mul(sample, channel2_scalefactor);
                    filter1.input_sample(sample1, subbandnumber);
                    filter2.input_sample(sample2, subbandnumber);
                }
                else if (channels == OutputChannels.LEFT_CHANNEL) {
                    int sample1 = FP.mul(sample, scalefactor);
                    filter1.input_sample(sample1, subbandnumber);
                } else {
                    int sample2 = FP.mul(sample, channel2_scalefactor);
                    filter1.input_sample(sample2, subbandnumber);
                }
            }
            return true;
        }
    };
    
    /**
     * Class for layer I subbands in stereo mode.
     */
    static class SubbandLayer1Stereo extends SubbandLayer1
    {
        protected int 		channel2_allocation;
        protected int		channel2_scalefactor;
        protected int 		channel2_samplelength;
        protected int	 	channel2_sample;
        protected int    	channel2_factor, channel2_offset;


        /**
         * Constructor
         */
        public SubbandLayer1Stereo(int subbandnumber) {
            super(subbandnumber);
        }
	  
        /**
         *
         */
        public void readAllocation (IOTool stream, AudioFrameHeader header, CRC16 crc)
            throws MpegAudioDecodeException
        {
            try
            {
                allocation = stream.getBits(4);
                channel2_allocation = stream.getBits(4);
                if (crc != null) {
                    crc.add_bits(allocation, 4);
                    crc.add_bits(channel2_allocation, 4);
                }
                if (allocation != 0) {
                    samplelength = allocation + 1;
                    factor = table_factor[allocation];
                    offset = table_offset[allocation];
                }
                if (channel2_allocation != 0) {
                    channel2_samplelength = channel2_allocation + 1;
                    channel2_factor = table_factor[channel2_allocation];
                    channel2_offset = table_offset[channel2_allocation];
                }
            }
            catch(IOException e)
            {
                throw new MpegAudioDecodeException(e.getMessage() );
            }
        }
	  
        /**
         *
         */
        public void readScaleFactor(IOTool stream, AudioFrameHeader header)
            throws MpegAudioDecodeException
        {
            try
            {
                if (allocation != 0)
                    scalefactor = scalefactors[stream.getBits(6)];
                if (channel2_allocation != 0)
                    channel2_scalefactor = scalefactors[stream.getBits(6)];
            }
            catch(IOException e)
            {
                throw new MpegAudioDecodeException(e.getMessage() );
            }
        }

        /**
         *
         */
        public boolean readSampleData(IOTool stream)
            throws MpegAudioDecodeException
        {
            try
            {
                boolean returnvalue = super.readSampleData(stream);
                if (channel2_allocation != 0) {
                    channel2_sample = FP.toFP((stream.getBits(channel2_samplelength)));
                }
                return(returnvalue);
            }
            catch(IOException e)
            {
                throw new MpegAudioDecodeException(e.getMessage() );
            }
        }
	  
        /**
         *
         */
        public boolean putNextSample(int channels, SynthesisFilter filter1, SynthesisFilter filter2)
            throws MpegAudioDecodeException
        {
            super.putNextSample (channels, filter1, filter2);
            if ((channel2_allocation != 0) && (channels != OutputChannels.LEFT_CHANNEL)) {
                int sample2 = FP.mul( (FP.mul(channel2_sample, channel2_factor) + channel2_offset) ,
                    channel2_scalefactor);
                if (channels == OutputChannels.BOTH_CHANNELS)
                    filter2.input_sample (sample2, subbandnumber);
                else
                    filter1.input_sample (sample2, subbandnumber);
            }
            return true;
        }
    };
}


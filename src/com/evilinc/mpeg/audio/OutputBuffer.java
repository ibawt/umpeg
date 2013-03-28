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

public class OutputBuffer implements IDecodedFrame
{
    public static final int BUFFER_SIZE = 2 * 1152;
    public static final int MAX_CHANNELS = 2;

    private short[] buffer = new short[BUFFER_SIZE];
    private int[]   bufferp  = new int[MAX_CHANNELS];
    private int     channels;
    private int     frequency;
    private long    pts;
    
    public OutputBuffer(int sampleFreq, int numChannels)
    {
        if( (sampleFreq < 0 ) || (numChannels < 0 ) )
            throw new IllegalArgumentException();

        channels = numChannels;
        frequency = sampleFreq;

        for( int i = 0 ; i < numChannels ; ++i )
        {
            bufferp[i] = (short) i;
        }
    }

    public void setPTS(long pts)
    {
        this.pts = pts;
    }

    public long getPTS()
    {
        return pts;
    }

    public void play()
        throws PlayerException
    {
        ;
    }
    
    public int getChannelCount()
    {
        return channels;
    }

    public int getSampleFrequency()
    {
        return frequency;
    }

    public short[] getBuffer()
    {
        return buffer;
    }

    public int getBufferLength()
    {
        return bufferp[0];
    }

    public void append(int channel, short value)
    {
        buffer[bufferp[channel]] = value;
        bufferp[channel] += channels;
    }

    public void appendSamples(int channel, int[] f, int len )
    {
        int pos = bufferp[channel];

        for( int i = 0 ; i < len ; ++i )
        {
            int fs = f[i];

            buffer[pos] = (short) FP.toInt(fs );
            pos += channels;
        }
        bufferp[channel] = pos;
    }

    public void clearBuffer()
    {
        for( int i = 0 ; i < channels ; ++i )
            bufferp[i] = (short)i;
    }
}

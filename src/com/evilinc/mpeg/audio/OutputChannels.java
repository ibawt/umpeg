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

public class OutputChannels
{
    public static final int BOTH_CHANNELS = 0;
    public static final int LEFT_CHANNEL = 1;
    public static final int RIGHT_CHANNEL = 2;
    public static final int DOWNMIX_CHANNELS = 3;

    public static final OutputChannels LEFT = new OutputChannels(LEFT_CHANNEL);
    public static final OutputChannels RIGHT = new OutputChannels(RIGHT_CHANNEL);
    public static final OutputChannels BOTH = new OutputChannels(BOTH_CHANNELS);
    public static final OutputChannels DOWNMIX = new OutputChannels(DOWNMIX_CHANNELS);

    private int m_iOutputChannels;

    public OutputChannels(int code)
    {
        m_iOutputChannels = code;
    }

    
    public static OutputChannels getOutputChannels(int code )
    {
        switch(code)
        {
        case BOTH_CHANNELS:
            return BOTH;
        case LEFT_CHANNEL:
            return LEFT;
        case RIGHT_CHANNEL:
            return RIGHT;
        case DOWNMIX_CHANNELS:
            return DOWNMIX;
        default:
            return null;
        }
    }

    public int getOutputChannels()
    {
        return m_iOutputChannels;
    }

    public int getChannelCount()
    {
        return (m_iOutputChannels == BOTH_CHANNELS ) ? 2 : 1;
    }
}
    
    

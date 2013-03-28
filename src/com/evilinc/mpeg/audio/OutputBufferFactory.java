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

public class OutputBufferFactory implements IDecodedFrameFactory
{

    private int m_iSampleFreq;
    private int m_iNumChannels;

    public OutputBufferFactory(int sampleFreq, int numChannels )
    {
        m_iSampleFreq = sampleFreq;
        m_iNumChannels = numChannels;
    }
    
    public IDecodedFrame newFrame()
    {
        return new OutputBuffer(m_iSampleFreq, m_iNumChannels);
    }
}

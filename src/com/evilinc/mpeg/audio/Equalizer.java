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

public class Equalizer
{
    public static final int BAND_NOT_PRESENT = Integer.MIN_VALUE;
    public static final Equalizer PASS_THRU_EQ = new Equalizer();

    private static final int BANDS = 32;

    private int[] m_settings = new int[BANDS];

    public Equalizer()
    {
        ;
    }

    public Equalizer(int[] settings)
    {
        setFrom(settings);
    }

     public void setFrom(int[] eq )
    {
        reset();

        int max = ( eq.length > BANDS ) ? BANDS : eq.length;

        for( int i = 0 ; i < max ; ++i)
            m_settings[i] = limit(eq[i]);
    }

     public void reset()
    {
        for( int i = 0 ; i < m_settings.length ; ++i )
            m_settings[i] = 0;
    }

    public int getBandCount()
    {
        return m_settings.length;
    }

    public int setBand(int band, int neweq)
    {
        int eq = 0;

        if((band >= 0 ) && (band < BANDS ) )
        {
            eq = m_settings[band];
            m_settings[band] = limit(neweq);
        }
        return eq;
    }

    public int getBand(int band)
    {
        if( ( band >= 0 ) && (band < BANDS ) )
            return m_settings[band];
        else
            return -1;
    }

    private int limit(int eq )
    {
        if( eq == BAND_NOT_PRESENT )
            return eq;

        if( eq > FP.toFP(1) )
            return FP.toFP(1);

        if( eq < FP.toFP(-1 ) )
            return FP.toFP(-1);

        return eq;
    }

    public int[] getBandFactors()
    {
        int[] factors = new int[BANDS];
        for( int i = 0 ; i < BANDS ; ++i )
        {
            factors[i] = getBandFactor(m_settings[i] );
        }
        return factors;
    }

    private int getBandFactor(int eq )
    {
        //TBD: fix this
        // return (float)Math.pow(2.0,eq)
        return 0;
    }

}
              
    
    

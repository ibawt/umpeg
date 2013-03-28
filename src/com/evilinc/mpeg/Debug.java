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

import java.io.*;

public abstract class Debug
{
    public static final int ERROR   = 1;
    public static final int WARNING = 2;
    public static final int INFO    = 3;

    private static PrintStream m_outStream;

    private static int m_iDebugLevel = 2;
    
    public static void setOutputStream(PrintStream ps)
    {
        m_outStream = ps;
    }

    public static void println(int level, String message)
    {
        if( m_outStream == null )
            m_outStream = System.err;

        if( level < m_iDebugLevel)
        {
            switch(level)
            {
            case ERROR:
                m_outStream.println("ERROR: " + message );
                break;
            case WARNING:
                m_outStream.println("WARNING: " + message );
                break;
            case INFO:
                m_outStream.println("INFO: " + message );
                break;
            default:
                m_outStream.println("UNKNOWN: " + message );
            }
        }
    }
}

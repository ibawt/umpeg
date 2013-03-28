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

public class BufferedInputStream extends InputStream
{
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    
    protected byte[]      m_buff;
    protected int         m_iSize;
    protected int         m_iCount;
    protected int         m_iHead;
    protected int         m_iTail;
    protected int         m_iFillThresh;
    protected InputStream m_in;
    protected boolean     m_bEOF;

    public BufferedInputStream(InputStream in)
    {
        this(in,DEFAULT_BUFFER_SIZE);
    }

    public BufferedInputStream(InputStream in, int size )
    {
        if( in == null )
            throw new NullPointerException();

        if( size <= 0 )
            throw new IllegalArgumentException();

        m_buff = allocByteBuffer(size);

        m_iSize = size;
        m_in = in;
        m_iFillThresh = size / 2;
    }

    public int read()
        throws IOException
    {
        checkThreshold();

        if( (m_iCount == 0 ) || (m_bEOF ) )
            return -1;

        int data = m_buff[m_iHead++] & 0xFF;

        if( m_iHead >= m_iSize )
            m_iHead = 0;

        m_iCount--;

        return data;
    }
       
    public void close()
        throws IOException
    {
        m_in.close();
    }

    private void checkThreshold()
        throws IOException
    {
        while( m_iCount < m_iFillThresh)
        {
            int len = ( m_iHead < m_iTail ) ?
                ( m_iSize - m_iTail ) :
                ( m_iHead - m_iTail );

            int bytesRead = m_in.read(m_buff, m_iTail, len );

            if( bytesRead == -1 )
            {
                m_bEOF = true;
                return;
            }

            m_iTail += bytesRead;
            m_iCount += bytesRead;
        }
    }

    private byte[] allocByteBuffer(int size)
    {
        return new byte[size];
    }
}
                

        
        
    

    

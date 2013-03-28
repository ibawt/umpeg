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

public class DecodedFrameQueue
{
    protected static final int DEFAULT_QUEUE_SIZE = 16;
    
    protected IDecodedFrame m_queue[];
    protected int           m_iHead;
    protected int           m_iTail = -1;
    protected int           m_iCount;

    public DecodedFrameQueue()
    {
        this(DEFAULT_QUEUE_SIZE);
    }

    public DecodedFrameQueue(int size )
    {
        if( size < 0  )
            throw new IllegalArgumentException();

        m_queue = new IDecodedFrame[size];
    }

    public int size()
    {
        return m_iCount;
    }

    public boolean isFull()
    {
        return m_iCount == m_queue.length;
    }
    
    public void push(IDecodedFrame frame)
    {
        //Debug.println(Debug.INFO, "Pushed a frame");
        if( frame == null )
            throw new NullPointerException();

        if( isFull() )
            Debug.println(Debug.ERROR, "QUEUE FULL!!!");
        
        int tail = m_iTail + 1;
        tail = ( tail < m_queue.length ) ? tail : 0;

        m_queue[tail] = frame;
        m_iCount++;
        m_iTail = tail;
        //System.out.println("Count = " + m_iCount + ", Head = " + m_iHead + ", Tail = " + m_iTail );
    }

    public IDecodedFrame pop()
    {
        int head = m_iHead;
        IDecodedFrame frame = m_queue[head++];

        m_iHead = ( head < m_queue.length ) ? head : 0;
        m_iCount--;


        //System.out.println("Count = " + m_iCount + ", Head = " + m_iHead + ", Tail = " + m_iTail );
        return frame;
    }

    public IDecodedFrame peek()
    {
        return m_queue[m_iHead];
    }
}
            
    

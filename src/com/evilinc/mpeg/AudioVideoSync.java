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

public class AudioVideoSync
{
    protected DecodedFrameQueue m_audioQueue;
    protected DecodedFrameQueue m_videoQueue;
    protected DecodedFramePool  m_audioFramePool;
    protected DecodedFramePool  m_videoFramePool;
    protected boolean           m_bNoAudio;
    
    public AudioVideoSync()
    {
        m_audioQueue = new DecodedFrameQueue();
        m_videoQueue = new DecodedFrameQueue();
    }

    public AudioVideoSync(int audioSize, int videoSize )
    {
        if( (audioSize <= 0 ) || ( videoSize <= 0 ) )
            throw new IllegalArgumentException();


        m_audioQueue = new DecodedFrameQueue(audioSize);
        m_videoQueue = new DecodedFrameQueue(videoSize);
    }


    public void pushAudioFrame(IDecodedFrame frame)
    {
        Debug.println(Debug.INFO, "Pushing an audio frame");
        m_audioQueue.push(frame);
    }

    public void pushVideoFrame(IDecodedFrame frame)
    {
        if( m_videoQueue.isFull() )
        {
            try
            {
                schedule();
            }
            catch(PlayerException e)
            {
                Debug.println(Debug.ERROR, "schedule threw exception: " + e.getMessage());
            }
        }
        
        m_videoQueue.push(frame);
    }

    public void schedule()
        throws PlayerException
    {
        //Debug.println(Debug.INFO, "A/V schedule");
        if( m_bNoAudio )
        {
            while( m_videoQueue.size() > 0 )
            {
                IDecodedFrame frame = m_videoQueue.pop();
                if( frame == null )
                    Debug.println(Debug.INFO, "videoQueue.pop() return null");
                frame.play();

                m_videoFramePool.freeFrame(frame);
            }
        }

        else
        {
            long audioPts = m_audioQueue.peek().getPTS();
            long videoPts = m_videoQueue.peek().getPTS();
            
            if( audioPts == videoPts )
            {
                IDecodedFrame frame = m_audioQueue.pop();


                
                frame.play();

                
                m_audioFramePool.freeFrame(frame);
                
                frame = m_videoQueue.pop();
                
                frame.play();
                
                m_videoFramePool.freeFrame(frame);
            }
        }
    }

    public void setAudioFramePool(DecodedFramePool pool)
    {
        if( pool == null )
            throw new NullPointerException();
        
        m_audioFramePool = pool;
    }

    public void setVideoFramePool(DecodedFramePool pool)
    {
        if( pool == null )
            throw new NullPointerException();

        m_videoFramePool = pool;
    }

    public void setNoAudio(boolean state)
    {
        m_bNoAudio = true;
    }
}
    

package com.evilinc.mpeg;

public class DecodedFramePool
{
    protected IDecodedFrame m_frames[];
    protected int           m_iTop;

    public DecodedFramePool(int size, IDecodedFrameFactory frameFac )
    {
        if( size <= 0 )
            throw new IllegalArgumentException();

        if( frameFac == null )
            throw new NullPointerException();

        m_frames = new IDecodedFrame[size];

        for( int i = 0 ; i < size ; ++i )
        {
            m_frames[i] = frameFac.newFrame();
            if( m_frames[i] == null )
                Debug.println(Debug.ERROR, "Frame factory returned null");
        }

        m_iTop = size - 1;
    }

    public IDecodedFrame getFrame()
    {
        if( m_iTop >= 0 )
        {
            IDecodedFrame frame = m_frames[m_iTop];
            
            m_frames[m_iTop--] = null;

            return frame;
        }
        Debug.println(Debug.ERROR, "m_iTop < 0");
        return null;
    }

    public void freeFrame(IDecodedFrame frame)
    {
        if( (++m_iTop) < m_frames.length )
        {
            m_frames[m_iTop] = frame;
        }
    }

    public int size()
    {
        return m_iTop + 1;
    }
}

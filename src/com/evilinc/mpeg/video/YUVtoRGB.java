package com.evilinc.mpeg.video;

public abstract class YUVtoRGB
{
    public static final int FOUR_TWO_ZERO = 0;
    public static final int FOUR_TWO_TWO  = 1;
    public static final int MINUS_128     = 2;

    private int m_iMode;
    private int m_iWidth;
    private int m_iHeight;
    
    public abstract boolean display(byte[] y, byte[] u, byte[] v);

    public void setMode( int mode )
    {
	m_iMode = mode;
    }
    
    public void setDimensions( int width, int height )
    {
	if( width <= 0 || height <= 0 )
	    throw new IllegalArgumentException();
	
	m_iWidth = width;
	m_iHeight = height;
    }
}

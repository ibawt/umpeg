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

package com.evilinc;

import javax.swing.*;
import java.awt.image.*;
import java.awt.*;

public class FrameBufferPanel extends JPanel
{
    private int[] m_pixels;
    private int   m_iWidth;
    private int   m_iHeight;

    private MemoryImageSource m_memImage;
    private Image             m_image;

    private boolean m_bFrameLocked;
    
    public FrameBufferPanel( int width, int height )
    {
	if( width <= 0 || height <= 0 )
	    throw new IllegalArgumentException();

	m_iWidth = width;
	m_iHeight = height;

	m_pixels =  new int[ width * height ];
	m_memImage = new MemoryImageSource( width, height, m_pixels, 0, width );
	m_memImage.setAnimated( true );

	m_image = createImage( m_memImage);

	setPreferredSize( new Dimension( width, height ) );
    }

    public void paintComponent( Graphics g )
    {
	if( ! m_bFrameLocked )
	{
	    synchronized( m_pixels )
	    {
		super.paintComponent( g );
		g.drawImage( m_image, 0, 0, this );
	    }
	}
    }

    public int[] lockFrameBuffer()
    {
	synchronized( m_pixels )
	{
	    while( m_bFrameLocked )
	    {
		try
		{
		    wait();
		}
		catch(InterruptedException e)
		{}
	    }


	    m_bFrameLocked = true;

	    return m_pixels;
	}
    }

    public int getWidth()
    {
	return m_iWidth;
    }

    public int getHeight()
    {
	return m_iHeight;
    }
    
    
    public void freeFrameBuffer( int[] pixels )
    {
	if( pixels != m_pixels )
	    return;

	synchronized( m_pixels )
	{
	    while( !m_bFrameLocked )
	    {
		try
		{
		    wait();
		}
		catch(InterruptedException e)
		{}
	    }

	    m_bFrameLocked = false;

	    m_memImage.newPixels( 0, 0, m_iWidth, m_iHeight );
	}
    }
}

		
	    
		
    


    
	
    

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
import java.io.*;
import java.awt.event.*;

import java.net.*;

import com.evilinc.mpeg.*;
import com.evilinc.mpeg.video.*;

/**
 * class <code>MainFrame</code>
 *
 * Swing Frame for PC output.
 *
 * @author <a href="mailto:ian@EUCLID"></a>
 * @version 1.0
 */
public class MainFrame extends JFrame
    implements ActionListener
{
    /**
     * MicroMpeg members
     */
    private YUVtoRGB         m_yuv2rgb;
    private MpegControl      m_mpegControl = new MpegControl();
    private InputStream      m_inputStream;
    private FrameBufferPanel m_frameBufferPanel;

    /**
     * Menu's
     */
    private JMenuBar  m_menuBar;
    private JMenu     m_menuFile;
    private JMenuItem m_menuItemOpen;
    private JMenuItem m_menuItemQuit;
    private JMenu     m_menuHelp;
    private JMenuItem m_menuItemAbout;

    /**
     * ActionCommand's
     */
    private static final String OPEN = "Open...";
    private static final String QUIT = "Quit";
    private static final String ABOUT= "About";

    /**
     * Thread members
     */
    private boolean  m_bKeepRunning;
    private boolean  m_bMpegPlaying;
    private MyThread m_thread;


    /**
     * <code>showDialog()</code>  constants
     */
    private static final int INFO    = 1;
    private static final int WARNING = 2;
    private static final int ERROR   = 3;
    private static final int ABOUTDIALOG   = 4;


    /**
     * Other constants
     */
    private static final String TITLE_MESSAGE = "Evil Inc. uMPEG Player";
    
    
    public MainFrame()
    {
	setTitle(TITLE_MESSAGE);

	m_menuBar = new JMenuBar();
	setJMenuBar( m_menuBar );

	m_menuFile = new JMenu("File");

	m_menuFile.setMnemonic(KeyEvent.VK_F );

	
	m_menuItemOpen = new JMenuItem(OPEN, KeyEvent.VK_O );
	m_menuItemOpen.setActionCommand( OPEN );
	m_menuItemOpen.addActionListener( this ) ;
	m_menuItemOpen.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_O,
							      ActionEvent.ALT_MASK ) );
	
	m_menuFile.add( m_menuItemOpen );

	m_menuItemQuit = new JMenuItem(QUIT, KeyEvent.VK_Q );
	m_menuItemQuit.setActionCommand(QUIT );
	m_menuItemQuit.addActionListener( this );
	m_menuItemQuit.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Q,
							       ActionEvent.ALT_MASK ) );

	m_menuFile.add( m_menuItemQuit );

	m_menuBar.add( m_menuFile );
	
	m_menuHelp = new JMenu("Help");
	m_menuHelp.setMnemonic( KeyEvent.VK_H );

	m_menuItemAbout = new JMenuItem(ABOUT, KeyEvent.VK_A );
	m_menuItemAbout.setActionCommand(ABOUT );
	m_menuItemAbout.addActionListener( this );
	m_menuItemAbout.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_A,
								ActionEvent.ALT_MASK ) );

	m_menuHelp.add( m_menuItemAbout );

	// makes the help menu at the right
	m_menuBar.add( Box.createHorizontalGlue() );
	m_menuBar.add( m_menuHelp );
	
	// clean up afterselves
	addWindowListener( new WindowAdapter() {
		public void windowClosing(WindowEvent e)
		{
		    close();
		}
	    });
	
	pack();

	validate();
    }

    public void actionPerformed(ActionEvent e )
    {
	String str = e.getActionCommand();

	if( str.equals( OPEN ) )
	{
	    openFilePane();
	}
	else if( str.equals( QUIT ) )
	{
	    close();
	}
	else if( str.equals( ABOUT ) )
	{
	    showDialog( ABOUTDIALOG, "None yet" );
	}
    }


    private void openFilePane()
    {
	JFileChooser fileChooser = new JFileChooser();

	fileChooser.addChoosableFileFilter( new javax.swing.filechooser.FileFilter() {
		public boolean accept(File f)
		{
		    if( f.isDirectory() )
			return true;

		    String ext = getExtension(f );

		    if( ext != null )
		    {
			if( ext.equals("mpg" ) ||
			    ext.equals("mpeg") ||
			    ext.equals("m1v" ))
			    return true;
			else
			    return false;
		    }
		    else
			return false;
		}

		public String getDescription()
		{
		    return "*.mpg, *.mpeg";
		}
		
		private String getExtension(File f )
		{
		    String ext = null;
		    String s = f.getName();
		    int i = s.lastIndexOf('.' );

		    if( i > 0 && i < s.length() -1 )
		    {
			ext = s.substring(i+1).toLowerCase();
		    }

		    return ext;
		}
	    } );

	int returnVal = fileChooser.showOpenDialog(this);

	if( returnVal == JFileChooser.APPROVE_OPTION )
	{
	    try
	    {
		openFile( fileChooser.getSelectedFile().getCanonicalPath() );
		startPlaying();
	    }
	    catch(IOException e)
	    {
		showDialog( ERROR, e.getMessage() );
	    }
	}
    }
    
    
    
    /**
     * <code>close</code>
     *
     * Tries to close the current playing mpeg gracefully.
     *
     */
    public void close()
    {
	if( m_bMpegPlaying )
	    m_bKeepRunning = false;

	if( m_thread != null )
	{
	    try
	    {
		m_thread.join();
	    }
	    catch(InterruptedException e )
	    {}
	}
	
	if( m_inputStream != null )
	{
	    try
	    {
		m_inputStream.close();
	    }
	    catch(IOException e)
	    {
		showDialog( WARNING, e.getMessage() );
	    }
	}

	System.exit(0);
    }
    
    

    /**
     * <code>openFile</code>
     *
     * Opens a file or URL
     *
     * @param fileName a <code>String</code> giving the filename or URL
     */
    public void openFile( String fileName )
    {
	if( m_bMpegPlaying )
	{
	    m_bKeepRunning = false;
	}

	if( m_inputStream != null )
	{
	    try
	    {
		m_inputStream.close();
	    }
	    catch(IOException e )
	    {
		showDialog( WARNING, e.getMessage() );
	    }
	}
	
	
	if( fileName != null )
	{
	    try
	    {
		try
		{
		    URL url = new URL( fileName );
		    
		    m_inputStream = new java.io.BufferedInputStream( url.openStream());
		}
		catch(MalformedURLException e )
		{
		    m_inputStream = new java.io.BufferedInputStream( new FileInputStream( fileName ) );
		}
	    }
	    catch(IOException e )
	    {
		showDialog( ERROR, e.getMessage());
	    }
	}
	else
	    showDialog( ERROR, "null FileName" );
    }

    public void startPlaying()
    {
	if( m_inputStream == null )
	{
	    showDialog( ERROR, "Please select an MPEG via the Open menu item" );
	}
	else
	{
	    try
	    {
		m_mpegControl.openMpeg( m_inputStream );
		
		int width = m_mpegControl.getWidth();
		int height = m_mpegControl.getHeight();
		
		m_frameBufferPanel = new FrameBufferPanel( width, height );
		m_yuv2rgb = new PCYUVtoRGB( m_frameBufferPanel );

		m_mpegControl.setYUVtoRGB( m_yuv2rgb );
		
		getContentPane().removeAll();
		
		getContentPane().add( m_frameBufferPanel );
		pack();
		validate();
		
		m_thread = new MyThread();
		m_bKeepRunning = true;
		
		m_thread.start();
	    }
	    catch(MpegDecodeException e )
	    {
		m_bKeepRunning = false;
		showDialog( ERROR, e.getMessage() );
	    }
	}
	
    }

    /**
     * <code>showDialog</code>
     *
     * Shows a dialog of the type requested.
     *
     * @param type an <code>int</code> value
     * @param message a <code>String</code> value
     */
    private void showDialog( int type, String message )
    {
	switch( type )
	{
	case ERROR:
	    JOptionPane.showMessageDialog( this, message,message, JOptionPane.ERROR_MESSAGE );
	    break;

	case WARNING:
	    JOptionPane.showMessageDialog( this, message,message, JOptionPane.WARNING_MESSAGE );
	    break;

	case INFO:
	    JOptionPane.showMessageDialog(this, message, message,JOptionPane.INFORMATION_MESSAGE );
	    break;

	default:
	    JOptionPane.showMessageDialog( this, message,message, JOptionPane.PLAIN_MESSAGE );
	}
    }

    public boolean isPlaying()
    {
	return m_bMpegPlaying;
    }
    
    
    /**
     * class <code>MyThread</code>
     *
     * Small innerclass to handle running the mpeg player.
     *
     */
    private class MyThread extends Thread
    {
	public void run()
	{
	    while( m_bKeepRunning )
	    {
		
		if( !m_bMpegPlaying )
		{
		    // Start the mpeg
		    try
		    {
			new Thread() 
			{
			    public void run()
			    {
				m_mpegControl.start();
			    }
			}.start();
			    
			m_bMpegPlaying = true;
		    }
		    catch(MpegDecodeException e)
		    {
			showDialog( ERROR, e.getMessage() );
		    }
		}
		else
		{
		    try
		    {
			
			sleep( 100 );
		    }
		    catch(InterruptedException e)
		    {
		    }
		}
	    }

	    // In case we exit before mpeg is done
	    try
	    {
		m_mpegControl.stopPlaying();
	    }
	    catch(MpegDecodeException e )
	    {
		showDialog( WARNING, e.getMessage() );
	    }
	}
    }


    /**
     * <code>main</code>
     *
     * New's the MainFrame and will start the first
     * file on the command line
     *
     * @param args a <code>String[]</code> value
     */
    public static void main(String[] args )
    {
	MainFrame mf = new MainFrame();
	mf.show();


	for( int i = 0 ; i < args.length ; ++i )
	{
	    mf.openFile( args[0] );
	    mf.startPlaying();

	    while( mf.isPlaying() )
	    {
		try
		{
		    Thread.sleep( 100 );
		}
		catch(InterruptedException e)
		{
		}
		
	    }
	}
    }
}


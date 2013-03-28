/* 
 *  Ian Quick 2002
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
import java.util.*;

/**
 * <code>Statistics</code>
 *
 * Class designed to keep performance statistics in
 * a static context for ease of use in other classes.
 *
 */
public abstract class Statistics
{
    /**
     * Container for <code>StatMember</code>'s
     */
    private static Hashtable m_stats = new Hashtable();


    public static void reset()
    {
        m_stats = new Hashtable();
    }

    public static void startLog(String key)
    {
        //Debug.println(Debug.INFO, "start: " + key );
        StatMember stat;

        if( m_stats.containsKey(key) )
        {
            stat = (StatMember)m_stats.get(key);
        }
        else
        {
            stat = new StatMember(key);
            m_stats.put(key, stat);
        }

        try
        {
            stat.logStart();
        }
        catch(LogException e)
        {
            Debug.println(Debug.ERROR,
                          "Caught LogException in startLog(" + key + "): " + e.getMessage() );

            // Clean it up for next time
            stat.setInSession(false);
        }
    }
    
    public static void endLog(String key)
    {
        //Debug.println(Debug.INFO, "end: " + key );
        if( m_stats.containsKey(key) )
        {
            StatMember stat = (StatMember)m_stats.get(key);

            try
            {
                stat.logStop();
            }
            catch(LogException e)
            {
                Debug.println(Debug.ERROR,
                              "Caught LogException in endLog(" + key + "): " + e.getMessage() );

            }
        }
        else
        {
            Debug.println(Debug.ERROR,
                          "No such log with key: " + key );
        }
    }

    public static long getClocks(String key)
    {
        if( m_stats.containsKey(key) )
        {
            StatMember stat = (StatMember)m_stats.get(key);

            return stat.getClockSum();
        }

        return -1;
    }

    public static long getCount(String key)
    {
        if( m_stats.containsKey(key) )
        {
            StatMember stat = (StatMember)m_stats.get(key);
            
            return stat.getCount();
        }

        return -1;
    }

    public static void dumpStatistics(OutputStream os)
    {
        PrintStream ps = new PrintStream(os);

        for( Enumeration e = m_stats.elements() ; e.hasMoreElements() ; )
        {
            StatMember stat = (StatMember) e.nextElement();

            ps.println("=- " + stat.getName() + "-=");
            ps.println("   Count = " + stat.getCount() +
                     ", ms Sum = " + stat.getClockSum() );
        }
    }
    
    /**
     * <code>StatMember</code>
     *
     * Container for Statistics gathering
     *
     */
    private static class StatMember
    {
        /**
         * Clocks when log session started
         */
        private long m_lStart;

        /**
         * Number of times log sessions has run
         */
        private long m_lCount;

        /**
         * Total number of clocks
         */
        private long m_lClkSum;

        /**
         * Have we started a log?
         */
        private boolean m_bInSession;

        /**
         * Session's name
         */
        private String  m_name;


        /**
         * Creates a new <code>StatMember</code> instance.
         *
         * @param name a <code>String</code> value
         */
        public StatMember(String name)
        {
            m_name = name;
        }

        
        /**
         *  <code>logStart</code>
         *
         *  Starts a logging session
         *
         * @exception LogException If session is already open
         */
        public void logStart()
            throws LogException
        {
            if( m_bInSession )
                throw new LogException("Member already in session");

            m_lStart = System.currentTimeMillis();

            m_lCount++;

            m_bInSession = true;
        }

        public void logStop()
            throws LogException
        {
            if( !m_bInSession )
                throw new LogException("Member not in session");

            m_lClkSum += System.currentTimeMillis() - m_lStart;

            m_bInSession = false;
        }

        public long getCount()
        {
            return m_lCount;
        }

        public boolean isInSession()
        {
            return m_bInSession;
        }

        public long getClockSum()
        {
            return m_lClkSum;
        }

        public String getName()
        {
            return m_name;
        }

        public void setInSession(boolean state)
        {
            m_bInSession = state;
        }
    }

    private static class LogException extends RuntimeException
    {
        public LogException(String s)
        {
            super(s);
        }

        public LogException()
        {
            super();
        }
    }
    
}

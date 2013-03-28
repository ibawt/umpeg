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

import com.evilinc.mpeg.*;

class SynthesisFilter
{
    private int[]    v1;
    private int[]  v2;
    private int[]  actual_v;			// v1 or v2
    private int      actual_write_pos;	// 0-15
    private int[]  samples;			// 32 new subband samples
    private int	     channel;
    private int    scalefactor;
    private int[]  eq;
	
    // ulaw conversion code (from maplay 2)
    boolean ulaw;
    private int sampleFrequency;
    private int offset1, offset2;		// number of samples to skip
    private int remaining_offset;		// number of samples still to skip when entering
    // compute_pcm_samples() next time
    private int highest_subband;		// highest subband to use, e.g. 7 for 32 kHz to 8 kHz conversion

    private static final String COMPUTE_NEW_V = "SynthesisFilter.compute_new_v";
    private static final String CAL_PCM_SAMPLES = "SynthesisFilter.calculate_pcm_samples";
    
    /**
     * Contructor.
     * The scalefactor scales the calculated float pcm samples to short values
     * (raw pcm samples are in [-1.0, 1.0], if no violations occur).
     */
    public SynthesisFilter(int channelnumber, int factor, int[] eq0, int _sampleFrequency, boolean _ulaw)
    {
        // FIXME: do we need to keep this?
        ulaw = _ulaw;
        
        if (d == null)
        {
            d = load_d();
            d16 = splitArray(d, 16);
        }

        v1 = new int[512];
        v2 = new int[512];
        samples = new int[32];
        channel = channelnumber;
        scalefactor = factor;
        setEQ(eq);
        reset();

        sampleFrequency = _sampleFrequency;
        remaining_offset = 0;

        if (ulaw)
        {
            switch (sampleFrequency)
            {    
	// The original data for d[]. This data is loaded from a file
	// to reduce the overall package size and to improve performance. 
/*  
  static final float d_data[] = {
  	0.000000000f, -0.000442505f,  0.003250122f, -0.007003784f,
  	0.031082153f, -0.078628540f,  0.100311279f, -0.572036743f,
  	1.144989014f,  0.572036743f,  0.100311279f,  0.078628540f,
  	0.031082153f,  0.007003784f,  0.003250122f,  0.000442505f,
   -0.000015259f, -0.000473022f,  0.003326416f, -0.007919312f,
  	0.030517578f, -0.084182739f,  0.090927124f, -0.600219727f,
  	1.144287109f,  0.543823242f,  0.108856201f,  0.073059082f,
  	0.031478882f,  0.006118774f,  0.003173828f,  0.000396729f,
   -0.000015259f, -0.000534058f,  0.003387451f, -0.008865356f,
  	0.029785156f, -0.089706421f,  0.080688477f, -0.628295898f,
  	1.142211914f,  0.515609741f,  0.116577148f,  0.067520142f,
    0.031738281f,  0.005294800f,  0.003082275f,  0.000366211f,
   -0.000015259f, -0.000579834f,  0.003433228f, -0.009841919f,
    0.028884888f, -0.095169067f,  0.069595337f, -0.656219482f,
  	1.138763428f,  0.487472534f,  0.123474121f,  0.061996460f,
    0.031845093f,  0.004486084f,  0.002990723f,  0.000320435f,
   -0.000015259f, -0.000625610f,  0.003463745f, -0.010848999f,
    0.027801514f, -0.100540161f,  0.057617188f, -0.683914185f,
  	1.133926392f,  0.459472656f,  0.129577637f,  0.056533813f,
  	0.031814575f,  0.003723145f,  0.002899170f,  0.000289917f,
   -0.000015259f, -0.000686646f,  0.003479004f, -0.011886597f,
  	0.026535034f, -0.105819702f,  0.044784546f, -0.711318970f,
  	1.127746582f,  0.431655884f,  0.134887695f,  0.051132202f,
  	0.031661987f,  0.003005981f,  0.002792358f,  0.000259399f,
   -0.000015259f, -0.000747681f,  0.003479004f, -0.012939453f,
  	0.025085449f, -0.110946655f,  0.031082153f, -0.738372803f,
    1.120223999f,  0.404083252f,  0.139450073f,  0.045837402f,
    0.031387329f,  0.002334595f,  0.002685547f,  0.000244141f,
   -0.000030518f, -0.000808716f,  0.003463745f, -0.014022827f,
    0.023422241f, -0.115921021f,  0.016510010f, -0.765029907f,
  	1.111373901f,  0.376800537f,  0.143264771f,  0.040634155f,
    0.031005859f,  0.001693726f,  0.002578735f,  0.000213623f,
   -0.000030518f, -0.000885010f,  0.003417969f, -0.015121460f,
  	0.021575928f, -0.120697021f,  0.001068115f, -0.791213989f,
    1.101211548f,  0.349868774f,  0.146362305f,  0.035552979f,
  	0.030532837f,  0.001098633f,  0.002456665f,  0.000198364f,
   -0.000030518f, -0.000961304f,  0.003372192f, -0.016235352f,
    0.019531250f, -0.125259399f, -0.015228271f, -0.816864014f,
  	1.089782715f,  0.323318481f,  0.148773193f,  0.030609131f,
  	0.029937744f,  0.000549316f,  0.002349854f,  0.000167847f,
   -0.000030518f, -0.001037598f,  0.003280640f, -0.017349243f,
  	0.017257690f, -0.129562378f, -0.032379150f, -0.841949463f,
    1.077117920f,  0.297210693f,  0.150497437f,  0.025817871f,
    0.029281616f,  0.000030518f,  0.002243042f,  0.000152588f,
   -0.000045776f, -0.001113892f,  0.003173828f, -0.018463135f,
  	0.014801025f, -0.133590698f, -0.050354004f, -0.866363525f,
  	1.063217163f,  0.271591187f,  0.151596069f,  0.021179199f,
  	0.028533936f, -0.000442505f,  0.002120972f,  0.000137329f,
   -0.000045776f, -0.001205444f,  0.003051758f, -0.019577026f,
  	0.012115479f, -0.137298584f, -0.069168091f, -0.890090942f,
  	1.048156738f,  0.246505737f,  0.152069092f,  0.016708374f,
  	0.027725220f, -0.000869751f,  0.002014160f,  0.000122070f,
   -0.000061035f, -0.001296997f,  0.002883911f, -0.020690918f,
    0.009231567f, -0.140670776f, -0.088775635f, -0.913055420f,
  	1.031936646f,  0.221984863f,  0.151962280f,  0.012420654f,
    0.026840210f, -0.001266479f,  0.001907349f,  0.000106812f,
   -0.000061035f, -0.001388550f,  0.002700806f, -0.021789551f,
  	0.006134033f, -0.143676758f, -0.109161377f, -0.935195923f,
    1.014617920f,  0.198059082f,  0.151306152f,  0.008316040f,
  	0.025909424f, -0.001617432f,  0.001785278f,  0.000106812f,
   -0.000076294f, -0.001480103f,  0.002487183f, -0.022857666f,
  	0.002822876f, -0.146255493f, -0.130310059f, -0.956481934f,
  	0.996246338f,  0.174789429f,  0.150115967f,  0.004394531f,
    0.024932861f, -0.001937866f,  0.001693726f,  0.000091553f,
   -0.000076294f, -0.001586914f,  0.002227783f, -0.023910522f,
   -0.000686646f, -0.148422241f, -0.152206421f, -0.976852417f,
    0.976852417f,  0.152206421f,  0.148422241f,  0.000686646f,
  	0.023910522f, -0.002227783f,  0.001586914f,  0.000076294f,
   -0.000091553f, -0.001693726f,  0.001937866f, -0.024932861f,
   -0.004394531f, -0.150115967f, -0.174789429f, -0.996246338f,
    0.956481934f,  0.130310059f,  0.146255493f, -0.002822876f,
    0.022857666f, -0.002487183f,  0.001480103f,  0.000076294f,
   -0.000106812f, -0.001785278f,  0.001617432f, -0.025909424f,
   -0.008316040f, -0.151306152f, -0.198059082f, -1.014617920f,
    0.935195923f,  0.109161377f,  0.143676758f, -0.006134033f,
    0.021789551f, -0.002700806f,  0.001388550f,  0.000061035f,
   -0.000106812f, -0.001907349f,  0.001266479f, -0.026840210f,
   -0.012420654f, -0.151962280f, -0.221984863f, -1.031936646f,
  	0.913055420f,  0.088775635f,  0.140670776f, -0.009231567f,
  	0.020690918f, -0.002883911f,  0.001296997f,  0.000061035f,
   -0.000122070f, -0.002014160f,  0.000869751f, -0.027725220f,
   -0.016708374f, -0.152069092f, -0.246505737f, -1.048156738f,
    0.890090942f,  0.069168091f,  0.137298584f, -0.012115479f,
  	0.019577026f, -0.003051758f,  0.001205444f,  0.000045776f,
   -0.000137329f, -0.002120972f,  0.000442505f, -0.028533936f,
   -0.021179199f, -0.151596069f, -0.271591187f, -1.063217163f,
    0.866363525f,  0.050354004f,  0.133590698f, -0.014801025f,
    0.018463135f, -0.003173828f,  0.001113892f,  0.000045776f,
   -0.000152588f, -0.002243042f, -0.000030518f, -0.029281616f,
   -0.025817871f, -0.150497437f, -0.297210693f, -1.077117920f,
  	0.841949463f,  0.032379150f,  0.129562378f, -0.017257690f,
  	0.017349243f, -0.003280640f,  0.001037598f,  0.000030518f,
   -0.000167847f, -0.002349854f, -0.000549316f, -0.029937744f,
   -0.030609131f, -0.148773193f, -0.323318481f, -1.089782715f,
  	0.816864014f,  0.015228271f,  0.125259399f, -0.019531250f,
    0.016235352f, -0.003372192f,  0.000961304f,  0.000030518f,
   -0.000198364f, -0.002456665f, -0.001098633f, -0.030532837f,
   -0.035552979f, -0.146362305f, -0.349868774f, -1.101211548f,
  	0.791213989f, -0.001068115f,  0.120697021f, -0.021575928f,
  	0.015121460f, -0.003417969f,  0.000885010f,  0.000030518f,
   -0.000213623f, -0.002578735f, -0.001693726f, -0.031005859f,
   -0.040634155f, -0.143264771f, -0.376800537f, -1.111373901f,
    0.765029907f, -0.016510010f,  0.115921021f, -0.023422241f,
    0.014022827f, -0.003463745f,  0.000808716f,  0.000030518f,
   -0.000244141f, -0.002685547f, -0.002334595f, -0.031387329f,
   -0.045837402f, -0.139450073f, -0.404083252f, -1.120223999f,
    0.738372803f, -0.031082153f,  0.110946655f, -0.025085449f,
  	0.012939453f, -0.003479004f,  0.000747681f,  0.000015259f,
   -0.000259399f, -0.002792358f, -0.003005981f, -0.031661987f,
   -0.051132202f, -0.134887695f, -0.431655884f, -1.127746582f,
  	0.711318970f, -0.044784546f,  0.105819702f, -0.026535034f,
    0.011886597f, -0.003479004f,  0.000686646f,  0.000015259f,
   -0.000289917f, -0.002899170f, -0.003723145f, -0.031814575f,
   -0.056533813f, -0.129577637f, -0.459472656f, -1.133926392f,
    0.683914185f, -0.057617188f,  0.100540161f, -0.027801514f,
  	0.010848999f, -0.003463745f,  0.000625610f,  0.000015259f,
   -0.000320435f, -0.002990723f, -0.004486084f, -0.031845093f,
   -0.061996460f, -0.123474121f, -0.487472534f, -1.138763428f,
  	0.656219482f, -0.069595337f,  0.095169067f, -0.028884888f,
  	0.009841919f, -0.003433228f,  0.000579834f,  0.000015259f,
   -0.000366211f, -0.003082275f, -0.005294800f, -0.031738281f,
   -0.067520142f, -0.116577148f, -0.515609741f, -1.142211914f,
  	0.628295898f, -0.080688477f,  0.089706421f, -0.029785156f,
  	0.008865356f, -0.003387451f,  0.000534058f,  0.000015259f,
   -0.000396729f, -0.003173828f, -0.006118774f, -0.031478882f,
   -0.073059082f, -0.108856201f, -0.543823242f, -1.144287109f,
  	0.600219727f, -0.090927124f,  0.084182739f, -0.030517578f,
	0.007919312f, -0.003326416f,  0.000473022f,  0.000015259f
	};
  */

            case AudioFrameHeader.THIRTYTWO:
                highest_subband = 7;			// use 8 of 32 subbands (500 Hz per subband)
                offset1 = offset2 = 3;			// take every 4. sample, forget the rest
                break;
            case AudioFrameHeader.FORTYFOUR_POINT_ONE:
                highest_subband = 5;			// use 6 of 32 subbands (some aliasing)
                offset1 = 4;					// take 2 of 11 samples, forget the rest
                offset2 = 5;
                break;
            case AudioFrameHeader.FORTYEIGHT:
                highest_subband = 4;			// use 5 of 32 subbands (750 Hz per subband)
                offset1 = offset2 = 5;			// take every 6. sample, forget the rest
            }
            
        } else
        {
            highest_subband = 31;				// not downsampling
            offset1 = offset2 = 0;
        }
    }

    public void setEQ(int[] eq0)
    {
        this.eq = eq0;

        if (eq==null)
        {
            eq = new int[32];
            for (int i=0; i<32; i++)
                eq[i] = FP.toFP(1);
        }
        if (eq.length <32 )
        {
            throw new IllegalArgumentException("eq0");
        }
    }

    /**
     * Reset the synthesis filter.
     */
    public void reset()
    {
        // initialize v1[] and v2[]:
        for (int p = 0; p < 512; p++) 
            v1[p] = v2[p] = 0;
        
        // initialize samples[]:
        for (int p2 = 0; p2 < 32; p2++) 
            samples[p2] = 0;

        actual_v = v1;
        actual_write_pos = 15;
    }


    /**
     * Inject Sample.
     */
    public void input_sample(int sample, int subbandnumber)
    {
        if (subbandnumber <= highest_subband)
        {
            samples[subbandnumber] = FP.mul(eq[subbandnumber],sample);
        }
    }

    public void input_samples(int[] s)
    {
        for (int i = highest_subband; i >= 0; i--) {
            samples[i] = FP.mul(s[i],eq[i]);
        }
    }
  
	/**
	 * Compute new values via a fast cosine transform.
	 */
    private void compute_new_v()
    {
        Statistics.startLog(COMPUTE_NEW_V);
        
        int new_v0, new_v1, new_v2, new_v3, new_v4, new_v5, new_v6, new_v7, new_v8, new_v9;
        int new_v10, new_v11, new_v12, new_v13, new_v14, new_v15, new_v16, new_v17, new_v18, new_v19;
        int new_v20, new_v21, new_v22, new_v23, new_v24, new_v25, new_v26, new_v27, new_v28, new_v29;
        int new_v30, new_v31;

        final int[] s = samples;

        int s0 = s[0];
        int s1 = s[1];
        int s2 = s[2];
        int s3 = s[3];
        int s4 = s[4];
        int s5 = s[5];
        int s6 = s[6];
        int s7 = s[7];
        int s8 = s[8];
        int s9 = s[9];
        int s10 = s[10];	
        int s11 = s[11];
        int s12 = s[12];
        int s13 = s[13];
        int s14 = s[14];
        int s15 = s[15];
        int s16 = s[16];
        int s17 = s[17];
        int s18 = s[18];
        int s19 = s[19];
        int s20 = s[20];
        int s21 = s[21];
        int s22 = s[22];
        int s23 = s[23];
        int s24 = s[24];
        int s25 = s[25];
        int s26 = s[26];
        int s27 = s[27];
        int s28 = s[28];
        int s29 = s[29];
        int s30 = s[30];
        int s31 = s[31];
		
        int p0 = s0 + s31;
        int p1 = s1 + s30;
        int p2 = s2 + s29;
        int p3 = s3 + s28;
        int p4 = s4 + s27;
        int p5 = s5 + s26;
        int p6 = s6 + s25;
        int p7 = s7 + s24;
        int p8 = s8 + s23;
        int p9 = s9 + s22;
        int p10 = s10 + s21;
        int p11 = s11 + s20;
        int p12 = s12 + s19;
        int p13 = s13 + s18;
        int p14 = s14 + s17;
        int p15 = s15 + s16;

        int pp0 = p0 + p15;
        int pp1 = p1 + p14;
        int pp2 = p2 + p13;
        int pp3 = p3 + p12;
        int pp4 = p4 + p11;
        int pp5 = p5 + p10;
        int pp6 = p6 + p9;
        int pp7 = p7 + p8;
        int pp8 = FP.mul( (p0 - p15),cos1_32);
        int pp9 = FP.mul( (p1 - p14),cos3_32 );
        int pp10 = FP.mul( (p2 - p13), cos5_32);
        int pp11 = FP.mul( (p3 - p12), cos7_32);
        int pp12 = FP.mul( (p4 - p11), cos9_32);
        int pp13 = FP.mul( (p5 - p10), cos11_32);
        int pp14 = FP.mul( (p6 - p9), cos13_32);
        int pp15 = FP.mul( (p7 - p8), cos15_32 );

        p0 = pp0 + pp7;
        p1 = pp1 + pp6;
        p2 = pp2 + pp5;
        p3 = pp3 + pp4;
        p4 = FP.mul( (pp0 - pp7), cos1_16);
        p5 = FP.mul( (pp1 - pp6), cos3_16);
        p6 = FP.mul( (pp2 - pp5), cos5_16);
        p7 = FP.mul( (pp3 - pp4), cos7_16);
        p8 = pp8 + pp15;
        p9 = pp9 + pp14;
        p10 = pp10 + pp13;
        p11 = pp11 + pp12;
        p12 = FP.mul( (pp8 - pp15), cos1_16);
        p13 = FP.mul( (pp9 - pp14), cos3_16);
        p14 = FP.mul( (pp10 - pp13), cos5_16);
        p15 = FP.mul( (pp11 - pp12), cos7_16 );

        pp0 = p0 + p3;
        pp1 = p1 + p2;
        pp2 = FP.mul( (p0 - p3), cos1_8);
        pp3 = FP.mul( (p1 - p2), cos3_8);
        pp4 = p4 + p7;
        pp5 = p5 + p6;
        pp6 = FP.mul( (p4 - p7), cos1_8 );
        pp7 = FP.mul( (p5 - p6), cos3_8 );
        pp8 = p8 + p11;
        pp9 = p9 + p10;
        pp10 = FP.mul( (p8 - p11), cos1_8);
        pp11 = FP.mul( (p9 - p10), cos3_8);
        pp12 = p12 + p15;
        pp13 = p13 + p14;
        pp14 = FP.mul( (p12 - p15), cos1_8);
        pp15 = FP.mul( (p13 - p14), cos3_8);

        p0 = pp0 + pp1;
        p1 = FP.mul( (pp0 - pp1), cos1_4);
        p2 = pp2 + pp3;
        p3 = FP.mul( (pp2 - pp3), cos1_4);
        p4 = pp4 + pp5;
        p5 = FP.mul( (pp4 - pp5), cos1_4);
        p6 = pp6 + pp7;
        p7 = FP.mul( (pp6 - pp7),cos1_4);
        p8 = pp8 + pp9;
        p9 = FP.mul( (pp8 - pp9), cos1_4);
        p10 = pp10 + pp11;
        p11 = FP.mul( (pp10 - pp11), cos1_4);
        p12 = pp12 + pp13;
        p13 = FP.mul( (pp12 - pp13), cos1_4);
        p14 = pp14 + pp15;
        p15 = FP.mul( (pp14 - pp15),cos1_4 );

        // this is pretty insane coding
        int tmp1;
        new_v19/*36-17*/ = -(new_v4 = (new_v12 = p7) + p5) - p6;
        new_v27/*44-17*/ = -p6 - p7 - p4;
        new_v6 = (new_v10 = (new_v14 = p15) + p11) + p13;
        new_v17/*34-17*/ = -(new_v2 = p15 + p13 + p9) - p14;
        new_v21/*38-17*/ = (tmp1 = -p14 - p15 - p10 - p11) - p13;
        new_v29/*46-17*/ = -p14 - p15 - p12 - p8;
        new_v25/*42-17*/ = tmp1 - p12;
        new_v31/*48-17*/ = -p0;
        new_v0 = p1;
        new_v23/*40-17*/ = -(new_v8 = p3) - p2;

        p0 = FP.mul((s0 - s31), cos1_64);
        p1 = FP.mul((s1 - s30), cos3_64);
        p2 = FP.mul((s2 - s29), cos5_64);
        p3 = FP.mul((s3 - s28), cos7_64);
        p4 = FP.mul((s4 - s27), cos9_64);
        p5 = FP.mul((s5 - s26), cos11_64);
        p6 = FP.mul((s6 - s25), cos13_64);
        p7 = FP.mul((s7 - s24), cos15_64);
        p8 = FP.mul((s8 - s23), cos17_64);
        p9 = FP.mul((s9 - s22), cos19_64);
        p10 = FP.mul((s10 - s21), cos21_64);
        p11 = FP.mul((s11 - s20), cos23_64);
        p12 = FP.mul((s12 - s19), cos25_64);
        p13 = FP.mul((s13 - s18), cos27_64);
        p14 = FP.mul((s14 - s17), cos29_64);
        p15 = FP.mul((s15 - s16), cos31_64);

        pp0 = p0 + p15;
        pp1 = p1 + p14;
        pp2 = p2 + p13;
        pp3 = p3 + p12;
        pp4 = p4 + p11;
        pp5 = p5 + p10;
        pp6 = p6 + p9;
        pp7 = p7 + p8;
        pp8 = FP.mul((p0 - p15), cos1_32);
        pp9 = FP.mul((p1 - p14), cos3_32);
        pp10 = FP.mul((p2 - p13), cos5_32);
        pp11 = FP.mul((p3 - p12), cos7_32);
        pp12 = FP.mul((p4 - p11), cos9_32);
        pp13 = FP.mul((p5 - p10), cos11_32);
        pp14 = FP.mul((p6 - p9), cos13_32);
        pp15 = FP.mul((p7 - p8), cos15_32);

        p0 = pp0 + pp7;
        p1 = pp1 + pp6;
        p2 = pp2 + pp5;
        p3 = pp3 + pp4;
        p4 = FP.mul((pp0 - pp7), cos1_16);
        p5 = FP.mul((pp1 - pp6), cos3_16);
        p6 = FP.mul((pp2 - pp5), cos5_16);
        p7 = FP.mul((pp3 - pp4), cos7_16);
        p8 = pp8 + pp15;
        p9 = pp9 + pp14;
        p10 = pp10 + pp13;
        p11 = pp11 + pp12;
        p12 = FP.mul((pp8 - pp15), cos1_16);
        p13 = FP.mul((pp9 - pp14), cos3_16);
        p14 = FP.mul((pp10 - pp13), cos5_16);
        p15 = FP.mul((pp11 - pp12), cos7_16);

        pp0 = p0 + p3;
        pp1 = p1 + p2;
        pp2 = FP.mul((p0 - p3) ,cos1_8);
        pp3 = FP.mul((p1 - p2) , cos3_8);
        pp4 = p4 + p7;
        pp5 = p5 + p6;
        pp6 = FP.mul((p4 - p7), cos1_8);
        pp7 = FP.mul((p5 - p6), cos3_8);
        pp8 = p8 + p11;
        pp9 = p9 + p10;
        pp10 = FP.mul((p8 - p11), cos1_8);
        pp11 = FP.mul((p9 - p10), cos3_8);
        pp12 = p12 + p15;
        pp13 = p13 + p14;
        pp14 = FP.mul((p12 - p15), cos1_8);
        pp15 = FP.mul((p13 - p14), cos3_8);

        p0 = pp0 + pp1;
        p1 = FP.mul((pp0 - pp1), cos1_4);
        p2 = pp2 + pp3;
        p3 = FP.mul((pp2 - pp3), cos1_4);
        p4 = pp4 + pp5;
        p5 = FP.mul((pp4 - pp5), cos1_4);
        p6 = pp6 + pp7;
        p7 = FP.mul((pp6 - pp7), cos1_4);
        p8 = pp8 + pp9;
        p9 = FP.mul((pp8 - pp9), cos1_4);
        p10 = pp10 + pp11;
        p11 = FP.mul((pp10 - pp11), cos1_4);
        p12 = pp12 + pp13;
        p13 = FP.mul((pp12 - pp13), cos1_4);
        p14 = pp14 + pp15;
        p15 = FP.mul((pp14 - pp15), cos1_4);

        // manually doing something that a compiler should handle sucks
        // coding like this is hard to read
        int tmp2;
        new_v5 = (new_v11 = (new_v13 = (new_v15 = p15) + p7) + p11)
            + p5 + p13;
        new_v7 = (new_v9 = p15 + p11 + p3) + p13;
        new_v16/*33-17*/ = -(new_v1 = (tmp1 = p13 + p15 + p9) + p1) - p14;
        new_v18/*35-17*/ = -(new_v3 = tmp1 + p5 + p7) - p6 - p14;

        new_v22/*39-17*/ = (tmp1 = -p10 - p11 - p14 - p15)
            - p13 - p2 - p3;
        new_v20/*37-17*/ = tmp1 - p13 - p5 - p6 - p7;
        new_v24/*41-17*/ = tmp1 - p12 - p2 - p3;
        new_v26/*43-17*/ = tmp1 - p12 - (tmp2 = p4 + p6 + p7);
        new_v30/*47-17*/ = (tmp1 = -p8 - p12 - p14 - p15) - p0;
        new_v28/*45-17*/ = tmp1 - tmp2;

        // insert V[0-15] (== new_v[0-15]) into actual v:	
        // float[] x2 = actual_v + actual_write_pos;
        int dest[] = actual_v;
 
        int pos = actual_write_pos;

        dest[0 + pos] = new_v0;
        dest[16 + pos] = new_v1;
        dest[32 + pos] = new_v2;
        dest[48 + pos] = new_v3;
        dest[64 + pos] = new_v4;
        dest[80 + pos] = new_v5;
        dest[96 + pos] = new_v6;
        dest[112 + pos] = new_v7;
        dest[128 + pos] = new_v8;
        dest[144 + pos] = new_v9;
        dest[160 + pos] = new_v10;
        dest[176 + pos] = new_v11;
        dest[192 + pos] = new_v12;
        dest[208 + pos] = new_v13;
        dest[224 + pos] = new_v14;
        dest[240 + pos] = new_v15;

        // V[16] is always 0.0:
        dest[256 + pos] = 0;

        // insert V[17-31] (== -new_v[15-1]) into actual v:
        dest[272 + pos] = -new_v15;
        dest[288 + pos] = -new_v14;
        dest[304 + pos] = -new_v13;
        dest[320 + pos] = -new_v12;
        dest[336 + pos] = -new_v11;
        dest[352 + pos] = -new_v10;
        dest[368 + pos] = -new_v9;
        dest[384 + pos] = -new_v8;
        dest[400 + pos] = -new_v7;
        dest[416 + pos] = -new_v6;
        dest[432 + pos] = -new_v5;
        dest[448 + pos] = -new_v4;
        dest[464 + pos] = -new_v3;
        dest[480 + pos] = -new_v2;
        dest[496 + pos] = -new_v1;

        // insert V[32] (== -new_v[0]) into other v:
        dest = (actual_v==v1) ? v2 : v1;

        dest[0 + pos] = -new_v0;
        // insert V[33-48] (== new_v[16-31]) into other v:
        dest[16 + pos] = new_v16;
        dest[32 + pos] = new_v17;
        dest[48 + pos] = new_v18;
        dest[64 + pos] = new_v19;
        dest[80 + pos] = new_v20;
        dest[96 + pos] = new_v21;
        dest[112 + pos] = new_v22;
        dest[128 + pos] = new_v23;
        dest[144 + pos] = new_v24;
        dest[160 + pos] = new_v25;
        dest[176 + pos] = new_v26;
        dest[192 + pos] = new_v27;
        dest[208 + pos] = new_v28;
        dest[224 + pos] = new_v29;
        dest[240 + pos] = new_v30;
        dest[256 + pos] = new_v31;

        // insert V[49-63] (== new_v[30-16]) into other v:
        dest[272 + pos] = new_v30;
        dest[288 + pos] = new_v29;
        dest[304 + pos] = new_v28;
        dest[320 + pos] = new_v27;
        dest[336 + pos] = new_v26;
        dest[352 + pos] = new_v25;
        dest[368 + pos] = new_v24;
        dest[384 + pos] = new_v23;
        dest[400 + pos] = new_v22;
        dest[416 + pos] = new_v21;
        dest[432 + pos] = new_v20;
        dest[448 + pos] = new_v19;
        dest[464 + pos] = new_v18;
        dest[480 + pos] = new_v17;
        dest[496 + pos] = new_v16;

        Statistics.endLog(COMPUTE_NEW_V);
    }
	
    /**
     * Compute PCM Samples.
     *
     * Note: These 16 methods include functionality to downsample 48/44.1/32khz audio
     * to 8khz.  The additional counters cost a little performance.  In the distant future
     * when browsers support the javax libraries, this code can be removed and these
     * simplified.
     *
     * This subsampling technique came from an old version of maplay (version 2).
     *
     */
    
    private int[] _tmpOut = new int[32];

    private int compute_pcm_samples0(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = remaining_offset;
        int dvp = remaining_offset << 4;

		// fat chance of having this loop unroll
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(( (FP.mul(vp[0 + dvp],  dp[0])) +
                           (FP.mul(vp[15 + dvp], dp[1])) +
                           (FP.mul(vp[14 + dvp], dp[2])) +
                           (FP.mul(vp[13 + dvp], dp[3])) +
                           (FP.mul(vp[12 + dvp], dp[4])) +
                           (FP.mul(vp[11 + dvp], dp[5])) +
                           (FP.mul(vp[10 + dvp], dp[6])) +
                           (FP.mul(vp[9 + dvp], dp[7])) +
                           (FP.mul(vp[8 + dvp], dp[8])) +
                           (FP.mul(vp[7 + dvp], dp[9])) +
                           (FP.mul(vp[6 + dvp], dp[10])) +
                           (FP.mul(vp[5 + dvp], dp[11])) +
                           (FP.mul(vp[4 + dvp], dp[12])) +
                           (FP.mul(vp[3 + dvp], dp[13])) +
                           (FP.mul(vp[2 + dvp], dp[14])) +
                           (FP.mul(vp[1 + dvp], dp[15]))
                           ),scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
  
    private int compute_pcm_samples1(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;
	
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;

            offset = offset1;
            offset1 = offset2;
            offset2 = offset;

            pcm_sample = (FP.mul(((FP.mul(vp[1 + dvp], dp[0])) +
                                  (FP.mul(vp[0 + dvp], dp[1])) +
                                  (FP.mul(vp[15 + dvp], dp[2])) +
                                  (FP.mul(vp[14 + dvp], dp[3])) +
                                  (FP.mul(vp[13 + dvp], dp[4])) +
                                  (FP.mul(vp[12 + dvp], dp[5])) +
                                  (FP.mul(vp[11 + dvp], dp[6])) +
                                  (FP.mul(vp[10 + dvp], dp[7])) +
                                  (FP.mul(vp[9 + dvp], dp[8])) +
                                  (FP.mul(vp[8 + dvp], dp[9])) +
                                  (FP.mul(vp[7 + dvp], dp[10])) +
                                  (FP.mul(vp[6 + dvp], dp[11])) +
                                  (FP.mul(vp[5 + dvp], dp[12])) +
                                  (FP.mul(vp[4 + dvp], dp[13])) +
                                  (FP.mul(vp[3 + dvp], dp[14])) +
                                  (FP.mul(vp[2 + dvp], dp[15]))
                                  ), scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }

    private int compute_pcm_samples2(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;
	
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(((FP.mul(vp[2 + dvp], dp[0])) +
                           (FP.mul(vp[1 + dvp], dp[1])) +
                           (FP.mul(vp[0 + dvp], dp[2])) +
                           (FP.mul(vp[15 + dvp], dp[3])) +
                           (FP.mul(vp[14 + dvp], dp[4])) +
                           (FP.mul(vp[13 + dvp], dp[5])) +
                           (FP.mul(vp[12 + dvp], dp[6])) +
                           (FP.mul(vp[11 + dvp], dp[7])) +
                           (FP.mul(vp[10 + dvp], dp[8])) +
                           (FP.mul(vp[9 + dvp], dp[9])) +
                           (FP.mul(vp[8 + dvp], dp[10])) +
                           (FP.mul(vp[7 + dvp], dp[11])) +
                           (FP.mul(vp[6 + dvp], dp[12])) +
                           (FP.mul(vp[5 + dvp], dp[13])) +
                           (FP.mul(vp[4 + dvp], dp[14])) +
                           (FP.mul(vp[3 + dvp], dp[15]))
                                  ), scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
    
    private int compute_pcm_samples3(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;
	
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;

            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(((FP.mul(vp[3 + dvp], dp[0])) +
                           (FP.mul(vp[2 + dvp], dp[1])) +
                           (FP.mul(vp[1 + dvp], dp[2])) +
                           (FP.mul(vp[0 + dvp], dp[3])) +
                           (FP.mul(vp[15 + dvp], dp[4])) +
                           (FP.mul(vp[14 + dvp], dp[5])) +
                           (FP.mul(vp[13 + dvp], dp[6])) +
                           (FP.mul(vp[12 + dvp], dp[7])) +
                           (FP.mul(vp[11 + dvp], dp[8])) +
                           (FP.mul(vp[10 + dvp], dp[9])) +
                           (FP.mul(vp[9 + dvp], dp[10])) +
                           (FP.mul(vp[8 + dvp], dp[11])) +
                           (FP.mul(vp[7 + dvp], dp[12])) +
                           (FP.mul(vp[6 + dvp], dp[13])) +
                           (FP.mul(vp[5 + dvp], dp[14])) +
                           (FP.mul(vp[4 + dvp], dp[15]))
                           ), scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
			
    private int compute_pcm_samples4(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;

        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4)) {
            final int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset;

            pcm_sample = (FP.mul(((FP.mul(vp[4 + dvp], dp[0])) +
                           (FP.mul(vp[3 + dvp], dp[1])) +
                           (FP.mul(vp[2 + dvp], dp[2])) +
                           (FP.mul(vp[1 + dvp], dp[3])) +
                           (FP.mul(vp[0 + dvp], dp[4])) +
                           (FP.mul(vp[15 + dvp], dp[5])) +
                           (FP.mul(vp[14 + dvp], dp[6])) +
                           (FP.mul(vp[13 + dvp], dp[7])) +
                           (FP.mul(vp[12 + dvp], dp[8])) +
                           (FP.mul(vp[11 + dvp], dp[9])) +
                           (FP.mul(vp[10 + dvp], dp[10])) +
                           (FP.mul(vp[9 + dvp], dp[11])) +
                           (FP.mul(vp[8 + dvp], dp[12])) +
                           (FP.mul(vp[7 + dvp],  dp[13])) +
                           (FP.mul(vp[6 + dvp], dp[14])) +
                           (FP.mul(vp[5 + dvp], dp[15]))
                           ), scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
  
    private int compute_pcm_samples5(OutputBuffer buffer) {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;
        
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(((FP.mul(vp[5 + dvp] , dp[0])) +
                           (FP.mul(vp[4 + dvp] , dp[1])) +
                           (FP.mul(vp[3 + dvp] , dp[2])) +
                           (FP.mul(vp[2 + dvp] , dp[3])) +
                           (FP.mul(vp[1 + dvp] , dp[4])) +
                           (FP.mul(vp[0 + dvp] , dp[5])) +
                           (FP.mul(vp[15 + dvp] , dp[6])) +
                           (FP.mul(vp[14 + dvp] , dp[7])) +
                           (FP.mul(vp[13 + dvp] , dp[8])) +
                           (FP.mul(vp[12 + dvp] , dp[9])) +
                           (FP.mul(vp[11 + dvp] , dp[10])) +
                           (FP.mul(vp[10 + dvp] , dp[11])) +
                           (FP.mul(vp[9 + dvp] , dp[12])) +
                           (FP.mul(vp[8 + dvp] , dp[13])) +
                           (FP.mul(vp[7 + dvp] , dp[14])) +
                           (FP.mul(vp[6 + dvp] , dp[15]))
                           ) , scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
    
    private int compute_pcm_samples6(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;

        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset; 
            
            pcm_sample = (FP.mul(((FP.mul(vp[6 + dvp] , dp[0])) +
                           (FP.mul(vp[5 + dvp] , dp[1])) +
                           (FP.mul(vp[4 + dvp] , dp[2])) +
                           (FP.mul(vp[3 + dvp] , dp[3])) +
                           (FP.mul(vp[2 + dvp] , dp[4])) +
                           (FP.mul(vp[1 + dvp] , dp[5])) +
                           (FP.mul(vp[0 + dvp] , dp[6])) +
                           (FP.mul(vp[15 + dvp] , dp[7])) +
                           (FP.mul(vp[14 + dvp] , dp[8])) +
                           (FP.mul(vp[13 + dvp] , dp[9])) +
                           (FP.mul(vp[12 + dvp] , dp[10])) +
                           (FP.mul(vp[11 + dvp] , dp[11])) +
                           (FP.mul(vp[10 + dvp] , dp[12])) +
                           (FP.mul(vp[9 + dvp] , dp[13])) +
                           (FP.mul(vp[8 + dvp] , dp[14])) +
                           (FP.mul(vp[7 + dvp] , dp[15]))
                           ) , scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
  
    private int compute_pcm_samples7(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;
	
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(((FP.mul(vp[7 + dvp] , dp[0])) +
                                  (FP.mul(vp[6 + dvp] , dp[1])) +
                                  (FP.mul(vp[5 + dvp] , dp[2])) +
                                  (FP.mul(vp[4 + dvp] , dp[3])) +
                                  (FP.mul(vp[3 + dvp] , dp[4])) +
                                  (FP.mul(vp[2 + dvp] , dp[5])) +
                                  (FP.mul(vp[1 + dvp] , dp[6])) +
                                  (FP.mul(vp[0 + dvp] , dp[7])) +
                                  (FP.mul(vp[15 + dvp] , dp[8])) +
                                  (FP.mul(vp[14 + dvp] , dp[9])) +
                                  (FP.mul(vp[13 + dvp] , dp[10])) +
                                  (FP.mul(vp[12 + dvp] , dp[11])) +
                                  (FP.mul(vp[11 + dvp] , dp[12])) +
                                  (FP.mul(vp[10 + dvp] , dp[13])) +
                                  (FP.mul(vp[9 + dvp] , dp[14])) +
                                  (FP.mul(vp[8 + dvp] , dp[15]))
                                  ) , scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }

    private int compute_pcm_samples8(OutputBuffer buffer) {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;

        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;

            offset = offset1;
            offset1 = offset2;
            offset2 = offset;

            pcm_sample = (FP.mul(((FP.mul(vp[8 + dvp] , dp[0])) +
                           (FP.mul(vp[7 + dvp] , dp[1])) +
                           (FP.mul(vp[6 + dvp] , dp[2])) +
                           (FP.mul(vp[5 + dvp] , dp[3])) +
                           (FP.mul(vp[4 + dvp] , dp[4])) +
                           (FP.mul(vp[3 + dvp] , dp[5])) +
                           (FP.mul(vp[2 + dvp] , dp[6])) +
                           (FP.mul(vp[1 + dvp] , dp[7])) +
                           (FP.mul(vp[0 + dvp] , dp[8])) +
                           (FP.mul(vp[15 + dvp] , dp[9])) +
                           (FP.mul(vp[14 + dvp] , dp[10])) +
                           (FP.mul(vp[13 + dvp] , dp[11])) +
                           (FP.mul(vp[12 + dvp] , dp[12])) +
                           (FP.mul(vp[11 + dvp] , dp[13])) +
                           (FP.mul(vp[10 + dvp] , dp[14])) +
                           (FP.mul(vp[9 + dvp] , dp[15]))
                                  ) , scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
    
    private int compute_pcm_samples9(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;
        
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
                                                      
            pcm_sample = (FP.mul(((FP.mul(vp[9 + dvp] , dp[0])) +
                                  (FP.mul(vp[8 + dvp] , dp[1])) +
                                  (FP.mul(vp[7 + dvp] , dp[2])) +
                                  (FP.mul(vp[6 + dvp] , dp[3])) +
                                  (FP.mul(vp[5 + dvp] , dp[4])) +
                                  (FP.mul(vp[4 + dvp] , dp[5])) +
                                  (FP.mul(vp[3 + dvp] , dp[6])) +
                                  (FP.mul(vp[2 + dvp] , dp[7])) +
                                  (FP.mul(vp[1 + dvp] , dp[8])) +
                                  (FP.mul(vp[0 + dvp] , dp[9])) +
                                  (FP.mul(vp[15 + dvp] , dp[10])) +
                                  (FP.mul(vp[14 + dvp] , dp[11])) +
                                  (FP.mul(vp[13 + dvp] , dp[12])) +
                                  (FP.mul(vp[12 + dvp] , dp[13])) +
                                  (FP.mul(vp[11 + dvp] , dp[14])) +
                                  (FP.mul(vp[10 + dvp] , dp[15]))
                                  ) , scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
  
    private int compute_pcm_samples10(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;
        
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(((FP.mul(vp[10 + dvp] , dp[0])) +
                           (FP.mul(vp[9 + dvp] , dp[1])) +
                           (FP.mul(vp[8 + dvp] , dp[2])) +
                           (FP.mul(vp[7 + dvp] , dp[3])) +
                           (FP.mul(vp[6 + dvp] , dp[4])) +
                           (FP.mul(vp[5 + dvp] , dp[5])) +
                           (FP.mul(vp[4 + dvp] , dp[6])) +
                           (FP.mul(vp[3 + dvp] , dp[7])) +
                           (FP.mul(vp[2 + dvp] , dp[8])) +
                           (FP.mul(vp[1 + dvp] , dp[9])) +
                           (FP.mul(vp[0 + dvp] , dp[10])) +
                           (FP.mul(vp[15 + dvp] , dp[11])) +
                           (FP.mul(vp[14 + dvp] , dp[12])) +
                           (FP.mul(vp[13 + dvp] , dp[13])) +
                           (FP.mul(vp[12 + dvp] , dp[14])) +
                           (FP.mul(vp[11 + dvp] , dp[15]))
                           ) , scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
    
    private int compute_pcm_samples11(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;
        
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(((FP.mul(vp[11 + dvp] , dp[0])) +
                                  (FP.mul(vp[10 + dvp] , dp[1])) +
                                  (FP.mul(vp[9 + dvp] , dp[2])) +
                                  (FP.mul(vp[8 + dvp] , dp[3])) +
                                  (FP.mul(vp[7 + dvp] , dp[4])) +
                                  (FP.mul(vp[6 + dvp] , dp[5])) +
                                  (FP.mul(vp[5 + dvp] , dp[6])) +
                                  (FP.mul(vp[4 + dvp] , dp[7])) +
                                  (FP.mul(vp[3 + dvp] , dp[8])) +
                                  (FP.mul(vp[2 + dvp] , dp[9])) +
                                  (FP.mul(vp[1 + dvp] , dp[10])) +
                                  (FP.mul(vp[0 + dvp] , dp[11])) +
                                  (FP.mul(vp[15 + dvp] , dp[12])) +
                                  (FP.mul(vp[14 + dvp] , dp[13])) +
                                  (FP.mul(vp[13 + dvp] , dp[14])) +
                                  (FP.mul(vp[12 + dvp] , dp[15]))
                                  ) , scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }

    private int compute_pcm_samples12(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;

        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4)) {
            final int[] dp = d16[i];
            int pcm_sample;

            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(((FP.mul(vp[12 + dvp] , dp[0])) +
                                  (FP.mul(vp[11 + dvp] , dp[1])) +
                                  (FP.mul(vp[10 + dvp] , dp[2])) +
                                  (FP.mul(vp[9 + dvp] , dp[3])) +
                                  (FP.mul(vp[8 + dvp] , dp[4])) +
                                  (FP.mul(vp[7 + dvp] , dp[5])) +
                                  (FP.mul(vp[6 + dvp] , dp[6])) +
                                  (FP.mul(vp[5 + dvp] , dp[7])) +
                                  (FP.mul(vp[4 + dvp] , dp[8])) +
                                  (FP.mul(vp[3 + dvp] , dp[9])) +
                                  (FP.mul(vp[2 + dvp] , dp[10])) +
                                  (FP.mul(vp[1 + dvp] , dp[11])) +
                                  (FP.mul(vp[0 + dvp] , dp[12])) +
                                  (FP.mul(vp[15 + dvp] , dp[13])) +
                                  (FP.mul(vp[14 + dvp] , dp[14])) +
                                  (FP.mul(vp[13 + dvp] , dp[15]))
                                  ) , scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
    
    private int compute_pcm_samples13(OutputBuffer buffer) {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;
        
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4)) {
            final int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(((FP.mul(vp[13 + dvp] , dp[0])) +
                           (FP.mul(vp[12 + dvp] , dp[1])) +
                           (FP.mul(vp[11 + dvp] , dp[2])) +
                           (FP.mul(vp[10 + dvp] , dp[3])) +
                           (FP.mul(vp[9 + dvp] , dp[4])) +
                           (FP.mul(vp[8 + dvp] , dp[5])) +
                           (FP.mul(vp[7 + dvp] , dp[6])) +
                           (FP.mul(vp[6 + dvp] , dp[7])) +
                           (FP.mul(vp[5 + dvp] , dp[8])) +
                           (FP.mul(vp[4 + dvp] , dp[9])) +
                           (FP.mul(vp[3 + dvp] , dp[10])) +
                           (FP.mul(vp[2 + dvp] , dp[11])) +
                           (FP.mul(vp[1 + dvp] , dp[12])) +
                           (FP.mul(vp[0 + dvp] , dp[13])) +
                           (FP.mul(vp[15 + dvp] , dp[14])) +
                           (FP.mul(vp[14 + dvp] , dp[15]))
                           ) , scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }

    private int compute_pcm_samples14(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;
        
        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;
            
            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(((FP.mul(vp[14 + dvp] , dp[0])) +
                                  (FP.mul(vp[13 + dvp] , dp[1])) +
                                  (FP.mul(vp[12 + dvp] , dp[2])) +
                                  (FP.mul(vp[11 + dvp] , dp[3])) +
                                  (FP.mul(vp[10 + dvp] , dp[4])) +
                                  (FP.mul(vp[9 + dvp] , dp[5])) +
                                  (FP.mul(vp[8 + dvp] , dp[6])) +
                                  (FP.mul(vp[7 + dvp] , dp[7])) +
                                  (FP.mul(vp[6 + dvp] , dp[8])) +
                                  (FP.mul(vp[5 + dvp] , dp[9])) +
                                  (FP.mul(vp[4 + dvp] , dp[10])) +
                                  (FP.mul(vp[3 + dvp] , dp[11])) +
                                  (FP.mul(vp[2 + dvp] , dp[12])) +
                                  (FP.mul(vp[1 + dvp] , dp[13])) +
                                  (FP.mul(vp[0 + dvp] , dp[14])) +
                                  (FP.mul(vp[15 + dvp] , dp[15]))
                                  ), scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
    
    private int compute_pcm_samples15(OutputBuffer buffer)
    {
        int i, j = 0;
        final int[] vp = actual_v;
        final int[] tmpOut = _tmpOut;
        int offset = 0;
        int dvp = remaining_offset << 4;

        for (i = remaining_offset; i < 32; i += offset + 1, dvp += 16 + (offset << 4))
        {
            final int[] dp = d16[i];
            int pcm_sample;

            offset = offset1;
            offset1 = offset2;
            offset2 = offset;
            
            pcm_sample = (FP.mul(((FP.mul(vp[15 + dvp] , dp[0])) +
                                  (FP.mul(vp[14 + dvp] , dp[1])) +
                                  (FP.mul(vp[13 + dvp] , dp[2])) +
                                  (FP.mul(vp[12 + dvp] , dp[3])) +
                                  (FP.mul(vp[11 + dvp] , dp[4])) +
                                  (FP.mul(vp[10 + dvp] , dp[5])) +
                                  (FP.mul(vp[9 + dvp] , dp[6])) +
                                  (FP.mul(vp[8 + dvp] , dp[7])) +
                                  (FP.mul(vp[7 + dvp] , dp[8])) +
                                  (FP.mul(vp[6 + dvp] , dp[9])) +
                                  (FP.mul(vp[5 + dvp] , dp[10])) +
                                  (FP.mul(vp[4 + dvp] , dp[11])) +
                                  (FP.mul(vp[3 + dvp] , dp[12])) +
                                  (FP.mul(vp[2 + dvp] , dp[13])) +
                                  (FP.mul(vp[1 + dvp] , dp[14])) +
                                  (FP.mul(vp[0 + dvp] , dp[15]))
                                  ) , scalefactor));
            tmpOut[j++] = pcm_sample;
        }
        remaining_offset = i - 32;
        return j;
    }
	 	 	 	 
    private void compute_pcm_samples(OutputBuffer buffer)
    {
        int len = 0;

        switch (actual_write_pos) {
        case 0:
            len = compute_pcm_samples0(buffer);
            break;
        case 1:
            len = compute_pcm_samples1(buffer);
            break;
        case 2:
            len = compute_pcm_samples2(buffer);
            break;
        case 3:
            len = compute_pcm_samples3(buffer);
            break;
        case 4:
            len = compute_pcm_samples4(buffer);
            break;
        case 5:
            len = compute_pcm_samples5(buffer);
            break;
        case 6:
            len = compute_pcm_samples6(buffer);
            break;
        case 7:
            len = compute_pcm_samples7(buffer);
            break;
        case 8:
            len = compute_pcm_samples8(buffer);
            break;
        case 9:
            len = compute_pcm_samples9(buffer);
            break;
        case 10:
            len = compute_pcm_samples10(buffer);
            break;
        case 11:
            len = compute_pcm_samples11(buffer);
            break;
        case 12:
            len = compute_pcm_samples12(buffer);
            break;
        case 13:
            len = compute_pcm_samples13(buffer);
            break;
        case 14:
            len = compute_pcm_samples14(buffer);
            break;
        case 15:
            len = compute_pcm_samples15(buffer);
            break;
        }

        if (buffer != null) {
            buffer.appendSamples(channel, _tmpOut, len);
        }
    }

	/**
	 * Calculate 32 PCM samples and put the into the OutputBuffer-object.
	 */
	
    public void calculate_pcm_samples(OutputBuffer buffer)
    {
        Statistics.startLog(CAL_PCM_SAMPLES);
        compute_new_v();
        compute_pcm_samples(buffer);
        
        actual_write_pos = (actual_write_pos + 1) & 0xf;
        actual_v = (actual_v == v1) ? v2 : v1;
        
        // MDM: this may not be necessary. The Layer III decoder always
        // outputs 32 subband samples, but I haven't checked layer I & II.
        for (int p = 0; p < 32; p++) 
            samples[p] = 0;

        Statistics.endLog(CAL_PCM_SAMPLES);
    }
    
    private static final int cos1_64  =  32807;
    private static final int cos3_64  =  33126;
    private static final int cos5_64  =  33780;
    private static final int cos7_64  =  34802;
    private static final int cos9_64  =  36248;
    private static final int cos11_64 =  38203;
    private static final int cos13_64 =  40796;
    private static final int cos15_64 =  44224;
    private static final int cos17_64 =  48794;
    private static final int cos19_64 =  55007;
    private static final int cos21_64 =  63738;
    private static final int cos23_64 =  76641;
    private static final int cos25_64 =  97268;
    private static final int cos27_64 =  134858;
    private static final int cos29_64 =  223324;
    private static final int cos31_64 =  667749;
    private static final int cos1_32  =  32926;
    private static final int cos3_32  =  34242;
    private static final int cos5_32  =  37154;
    private static final int cos7_32  =  42390;
    private static final int cos9_32  =  51652;
    private static final int cos11_32 =  69513;
    private static final int cos13_32 =  112882;
    private static final int cos15_32 =  334290;
    private static final int cos1_16  =  33409;
    private static final int cos3_16  =  39409;
    private static final int cos5_16  =  58980;
    private static final int cos7_16  =  167968;
    private static final int cos1_8   =  35468;
    private static final int cos3_8   =  85625;
    private static final int cos1_4   =  46340;

    // Note: These values are not in the same order
    // as in Annex 3-B.3 of the ISO/IEC DIS 11172-3 
    // private float d[] = {0.000000000, -4.000442505};
  
    private static int d[] = null;
  
    /** 
     * d[] split into subarrays of length 16. This provides for
     * more faster access by allowing a block of 16 to be addressed
     * with constant offset. 
     **/
    private static int d16[][] = null;	
  
    /**
     * Loads the data for the d[] from the resource SFd.ser. 
     * @return the loaded values for d[].
     */
    static private int[] load_d() {
        return d_data8;
    }
	
	/**
	 * Converts a 1D array into a number of smaller arrays. This is used
	 * to achieve offset + constant indexing into an array. Each sub-array
	 * represents a block of values of the original array. 
	 * @param array			The array to split up into blocks.
	 * @param blockSize		The size of the blocks to split the array
	 *						into. This must be an exact divisor of
	 *						the length of the array, or some data
	 *						will be lost from the main array.
	 * 
	 * @return	An array of arrays in which each element in the returned
	 *			array will be of length <code>blockSize</code>.
	 */
	static private int[][] splitArray(final int[] array, final int blockSize) {
		int size = array.length / blockSize;
		int[][] split = new int[size][];
		for (int i = 0; i < size; i++) {
			split[i] = subArray(array, i*blockSize, blockSize);
		}
		return split;
	}
	
	/**
	 * Returns a subarray of an existing array.
	 * 
	 * @param array	The array to retrieve a subarray from.
	 * @param offs	The offset in the array that corresponds to
	 *				the first index of the subarray.
	 * @param len	The number of indeces in the subarray.
	 * @return The subarray, which may be of length 0.
	 */
	static private int[] subArray(final int[] array, final int offs, int len) {
		if (offs+len > array.length) {
			len = array.length-offs;
		}
		
		if (len < 0) len = 0;
		
		int[] subarray = new int[len];
		for (int i=0; i<len; i++) {
			subarray[i] = array[offs+i];
		}
		
		return subarray;
	}

    private static final int[] d_data8 = {
        0, -29, 213, -459, 2037, -5153, 6574, -37489, 
        75038, 37489, 6574, 5153, 2037, 459, 213, 29, 
        -1, -31, 218, -519, 2000, -5517, 5959, -39336, 
        74992, 35640, 7134, 4788, 2063, 401, 208, 26, 
        -1, -35, 222, -581, 1952, -5879, 5288, -41176, 
        74856, 33791, 7640, 4425, 2080, 347, 202, 24, 
        -1, -38, 225, -645, 1893, -6237, 4561, -43006, 
        74630, 31947, 8092, 4063, 2087, 294, 196, 21, 
        -1, -41, 227, -711, 1822, -6589, 3776, -44821, 
        74313, 30112, 8492, 3705, 2085, 244, 190, 19, 
        -1, -45, 228, -779, 1739, -6935, 2935, -46617, 
        73908, 28289, 8840, 3351, 2075, 197, 183, 17, 
        -1, -49, 228, -848, 1644, -7271, 2037, -48390, 
        73415, 26482, 9139, 3004, 2057, 153, 176, 16, 
        -2, -53, 227, -919, 1535, -7597, 1082, -50137, 
        72835, 24694, 9389, 2663, 2032, 111, 169, 14, 
        -2, -58, 224, -991, 1414, -7910, 70, -51853, 
        72169, 22929, 9592, 2330, 2001, 72, 161, 13, 
        -2, -63, 221, -1064, 1280, -8209, -998, -53534, 
        71420, 21189, 9750, 2006, 1962, 36, 154, 11, 
        -2, -68, 215, -1137, 1131, -8491, -2122, -55178, 
        70590, 19478, 9863, 1692, 1919, 2, 147, 10, 
        -3, -73, 208, -1210, 970, -8755, -3300, -56778, 
        69679, 17799, 9935, 1388, 1870, -29, 139, 9, 
        -3, -79, 200, -1283, 794, -8998, -4533, -58333, 
        68692, 16155, 9966, 1095, 1817, -57, 132, 8, 
        -4, -85, 189, -1356, 605, -9219, -5818, -59838, 
        67629, 14548, 9959, 814, 1759, -83, 125, 7, 
        -4, -91, 177, -1428, 402, -9416, -7154, -61289, 
        66494, 12980, 9916, 545, 1698, -106, 117, 7, 
        -5, -97, 163, -1498, 185, -9585, -8540, -62684, 
        65290, 11455, 9838, 288, 1634, -127, 111, 6, 
        -5, -104, 146, -1567, -45, -9727, -9975, -64019, 
        64019, 9975, 9727, 45, 1567, -146, 104, 5, 
        -6, -111, 127, -1634, -288, -9838, -11455, -65290, 
        62684, 8540, 9585, -185, 1498, -163, 97, 5, 
        -7, -117, 106, -1698, -545, -9916, -12980, -66494, 
        61289, 7154, 9416, -402, 1428, -177, 91, 4, 
        -7, -125, 83, -1759, -814, -9959, -14548, -67629, 
        59838, 5818, 9219, -605, 1356, -189, 85, 4, 
        -8, -132, 57, -1817, -1095, -9966, -16155, -68692, 
        58333, 4533, 8998, -794, 1283, -200, 79, 3, 
        -9, -139, 29, -1870, -1388, -9935, -17799, -69679, 
        56778, 3300, 8755, -970, 1210, -208, 73, 3, 
        -10, -147, -2, -1919, -1692, -9863, -19478, -70590, 
        55178, 2122, 8491, -1131, 1137, -215, 68, 2, 
        -11, -154, -36, -1962, -2006, -9750, -21189, -71420, 
        53534, 998, 8209, -1280, 1064, -221, 63, 2, 
        -13, -161, -72, -2001, -2330, -9592, -22929, -72169, 
        51853, -70, 7910, -1414, 991, -224, 58, 2, 
        -14, -169, -111, -2032, -2663, -9389, -24694, -72835, 
        50137, -1082, 7597, -1535, 919, -227, 53, 2, 
        -16, -176, -153, -2057, -3004, -9139, -26482, -73415, 
        48390, -2037, 7271, -1644, 848, -228, 49, 1, 
        -17, -183, -197, -2075, -3351, -8840, -28289, -73908, 
        46617, -2935, 6935, -1739, 779, -228, 45, 1, 
        -19, -190, -244, -2085, -3705, -8492, -30112, -74313, 
        44821, -3776, 6589, -1822, 711, -227, 41, 1, 
        -21, -196, -294, -2087, -4063, -8092, -31947, -74630, 
        43006, -4561, 6237, -1893, 645, -225, 38, 1, 
        -24, -202, -347, -2080, -4425, -7640, -33791, -74856, 
        41176, -5288, 5879, -1952, 581, -222, 35, 1, 
        -26, -208, -401, -2063, -4788, -7134, -35640, -74992, 
        39336, -5959, 5517, -2000, 519, -218, 31, 1, 
    };
    
}

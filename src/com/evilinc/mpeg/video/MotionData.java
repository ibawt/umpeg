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

package com.evilinc.mpeg.video;

import com.evilinc.mpeg.*;

class MotionData
{
    private int recon_right_x_prev;
    private int recon_down_x_prev;
    private int recon_right_x;
    private int recon_down_x;
    private int right_x;
    private int down_x;
    private boolean right_half_x;
    private boolean down_half_x;
    private int right_x_col;
    private int down_x_col;
    private boolean right_half_x_col;
    private boolean down_half_x_col;
    private int x_ward_f;
    private int min;
    private int max;
    private int range;
    private boolean Full_pel_x_vector;
    private int pixel_per_lum_line;
    private int pixel_per_col_line;
    private int lum_y_incr;
    private int col_y_incr;

    private static final String COMPUTE_MOTION_VECTOR = "MotionData.computeMotionVector";
    private static final String GET_AREA = "MotionData.getArea";
    private static final String COPY_AREA = "MotionData.copyArea";
    private static final String COPY_UNCHANGED = "MotionData.copyUnchanged";
    private static final String PUT_AREA_1 = "MotionData.putArea(int,int,int[],int[][]";
    private static final String PUT_AREA_2 = "MotionData.putArea(int,int,int[],int[],int[][]";
    
    public void init(int i, int j, int k, int l)
    {
        pixel_per_lum_line = i;
        pixel_per_col_line = j;
        lum_y_incr = k;
        col_y_incr = l;
    }

    public void setPicData(int i, boolean flag)
    {
        x_ward_f = i;
        Full_pel_x_vector = flag;
        range = i << 5;
        max = (i << 4) - 1;
        min = -(i << 4);
    }

    public void resetPrev()
    {
        recon_right_x_prev = recon_down_x_prev = 0;
    }

    private int motionDisplacement(int i, int j, int k)
    {
        int l;
        if(x_ward_f == 1 || i == 0)
        {
            l = i;
        }
        else
        {
            // l = 1 + x_ward_f * (Math.abs(i) - 1);
            if (i<0) l = 1 + x_ward_f * ((-i) - 1);
            else  l = 1 + x_ward_f * (i - 1);
            l += k;
            if(i < 0) l = -l;
        }
        int i1 = j + l;
        if(i1 > max) i1 -= range;
        else if(i1 < min) i1 += range;
        return i1;
    }

    public void computeMotionVector(int i, int j, int k, int l)
    {
        Statistics.startLog(COMPUTE_MOTION_VECTOR);
        
        recon_right_x_prev = recon_right_x = motionDisplacement(i, recon_right_x_prev, k);
        if(Full_pel_x_vector)
            recon_right_x <<= 1;
        recon_down_x_prev = recon_down_x = motionDisplacement(j, recon_down_x_prev, l);
        if(Full_pel_x_vector)
            recon_down_x <<= 1;
        right_x = recon_right_x >> 1;
        down_x = recon_down_x >> 1;
        right_half_x = (recon_right_x & 1) != 0;
        down_half_x = (recon_down_x & 1) != 0;
        right_x_col = recon_right_x >> 2;
        down_x_col = recon_down_x >> 2;
        right_half_x_col = (recon_right_x & 2) != 0;
        down_half_x_col = (recon_down_x & 2) != 0;

        Statistics.endLog(COMPUTE_MOTION_VECTOR);

    }

    public void getArea(int i, int j, int ai[][], int ai1[])
    {
        Statistics.startLog(GET_AREA);
        int k = (i << 4) + down_x;
        int l = (j << 4) + right_x;
        int k5 = 0;
        if(!right_half_x && !down_half_x)
        {
            int i1 = pixel_per_lum_line * k + l;
            int src[] = ai[0];
            for(int j9 = 0; j9 < 16; j9++)
            {
                // System.arraycopy(ai[0], i1, ai1, k5, 16);
                ai1[k5] =    src[i1];
                ai1[k5+1] =  src[i1+1];
                ai1[k5+2] =  src[i1+2];
                ai1[k5+3] =  src[i1+3];
                ai1[k5+4] =  src[i1+4];
                ai1[k5+5] =  src[i1+5];
                ai1[k5+6] =  src[i1+6];
                ai1[k5+7] =  src[i1+7];
                ai1[k5+8] =  src[i1+8];
                ai1[k5+9] =  src[i1+9];
                ai1[k5+10] = src[i1+10];
                ai1[k5+11] = src[i1+11];
                ai1[k5+12] = src[i1+12];
                ai1[k5+13] = src[i1+13];
                ai1[k5+14] = src[i1+14];
                ai1[k5+15] = src[i1+15];

                i1 += pixel_per_lum_line;
                k5 += 16;
            }

        }
        else if(!right_half_x && down_half_x)
        {
            int j1 = pixel_per_lum_line * k + l;
            int i3 = pixel_per_lum_line * (k + 1) + l;
            int ai0[] = ai[0];
            for(int k9 = 0; k9 < 16; k9++)
            {
                // for(int j11 = 0; j11 < 16; j11++)
                //     ai1[k5++] = ai[0][j1++] + ai[0][i3++] >> 1;
                ai1[k5]    = ai0[j1]    + ai0[i3] >> 1;
                ai1[k5+1]  = ai0[j1+1]  + ai0[i3+1] >> 1;
                ai1[k5+2]  = ai0[j1+2]  + ai0[i3+2] >> 1;
                ai1[k5+3]  = ai0[j1+3]  + ai0[i3+3] >> 1;
                ai1[k5+4]  = ai0[j1+4]  + ai0[i3+4] >> 1;
                ai1[k5+5]  = ai0[j1+5]  + ai0[i3+5] >> 1;
                ai1[k5+6]  = ai0[j1+6]  + ai0[i3+6] >> 1;
                ai1[k5+7]  = ai0[j1+7]  + ai0[i3+7] >> 1;
                ai1[k5+8]  = ai0[j1+8]  + ai0[i3+8] >> 1;
                ai1[k5+9]  = ai0[j1+9]  + ai0[i3+9] >> 1;
                ai1[k5+10] = ai0[j1+10] + ai0[i3+10] >> 1;
                ai1[k5+11] = ai0[j1+11] + ai0[i3+11] >> 1;
                ai1[k5+12] = ai0[j1+12] + ai0[i3+12] >> 1;
                ai1[k5+13] = ai0[j1+13] + ai0[i3+13] >> 1;
                ai1[k5+14] = ai0[j1+14] + ai0[i3+14] >> 1;
                ai1[k5+15] = ai0[j1+15] + ai0[i3+15] >> 1;

				k5 += 16;
				j1 += 16;
				i3 += 16;
                j1 += lum_y_incr;
                i3 += lum_y_incr;
            }

        }
        else if(right_half_x && !down_half_x)
        {
            int k1 = pixel_per_lum_line * k + l;
            int j3 = pixel_per_lum_line * k + l + 1;
            int ai0[] = ai[0];
            for(int l9 = 0; l9 < 16; l9++)
            {
                // for(int k11 = 0; k11 < 16; k11++)
                //     ai1[k5++] = ai[0][k1++] + ai[0][j3++] >> 1;
                ai1[k5+0]  = ai0[k1+0]  + ai0[j3+0] >> 1;
                ai1[k5+1]  = ai0[k1+1]  + ai0[j3+1] >> 1;
                ai1[k5+2]  = ai0[k1+2]  + ai0[j3+2] >> 1;
                ai1[k5+3]  = ai0[k1+3]  + ai0[j3+3] >> 1;
                ai1[k5+4]  = ai0[k1+4]  + ai0[j3+4] >> 1;
                ai1[k5+5]  = ai0[k1+5]  + ai0[j3+5] >> 1;
                ai1[k5+6]  = ai0[k1+6]  + ai0[j3+6] >> 1;
                ai1[k5+7]  = ai0[k1+7]  + ai0[j3+7] >> 1;
                ai1[k5+8]  = ai0[k1+8]  + ai0[j3+8] >> 1;
                ai1[k5+9]  = ai0[k1+9]  + ai0[j3+9] >> 1;
                ai1[k5+10] = ai0[k1+10] + ai0[j3+10] >> 1;
                ai1[k5+11] = ai0[k1+11] + ai0[j3+11] >> 1;
                ai1[k5+12] = ai0[k1+12] + ai0[j3+12] >> 1;
                ai1[k5+13] = ai0[k1+13] + ai0[j3+13] >> 1;
                ai1[k5+14] = ai0[k1+14] + ai0[j3+14] >> 1;
                ai1[k5+15] = ai0[k1+15] + ai0[j3+15] >> 1;

				k5 += 16;
				k1 += 16;
				j3 += 16;
                k1 += lum_y_incr;
                j3 += lum_y_incr;
            }

        } else
        {
            int l1 = pixel_per_lum_line * k + l;
            int k3 = pixel_per_lum_line * (k + 1) + l;
            int k4 = pixel_per_lum_line * k + l + 1;
            int i5 = pixel_per_lum_line * (k + 1) + l + 1;
            for(int i10 = 0; i10 < 16; i10++)
            {
                for(int l11 = 0; l11 < 16; l11++)
                    ai1[k5++] = ai[0][l1++] + ai[0][k3++] + ai[0][k4++] + ai[0][i5++] >> 2;

                l1 += lum_y_incr;
                k3 += lum_y_incr;
                k4 += lum_y_incr;
                i5 += lum_y_incr;
            }

        }
        k = (i << 3) + down_x_col;
        l = (j << 3) + right_x_col;
        if(!right_half_x_col && !down_half_x_col)
        {
            int i2 = pixel_per_col_line * k + l;
            int src1[] = ai[1];
            int src2[] = ai[2];
            for(int j10 = 0; j10 < 8; j10++)
            {
                // System.arraycopy(ai[1], i2, ai1, k5, 8);
                ai1[k5] =   src1[i2];
                ai1[k5+1] = src1[i2+1];
                ai1[k5+2] = src1[i2+2];
                ai1[k5+3] = src1[i2+3];
                ai1[k5+4] = src1[i2+4];
                ai1[k5+5] = src1[i2+5];
                ai1[k5+6] = src1[i2+6];
                ai1[k5+7] = src1[i2+7];

                k5 += 8;
                // System.arraycopy(ai[2], i2, ai1, k5, 8);
                ai1[k5] =   src2[i2];
                ai1[k5+1] = src2[i2+1];
                ai1[k5+2] = src2[i2+2];
                ai1[k5+3] = src2[i2+3];
                ai1[k5+4] = src2[i2+4];
                ai1[k5+5] = src2[i2+5];
                ai1[k5+6] = src2[i2+6];
                ai1[k5+7] = src2[i2+7];

                k5 += 8;
                i2 += pixel_per_col_line;
            }
            Statistics.endLog(GET_AREA);
            return;
        }
        if(!right_half_x_col && !down_half_x_col)
        {
            int j2 = pixel_per_col_line * k + l;
            int l3 = pixel_per_col_line * (k + 1) + l;
            for(int k10 = 0; k10 < 8; k10++)
            {
                int l5 = j2;
                int i7 = l3;
                for(int i12 = 0; i12 < 8; i12++)
                    ai1[k5++] = ai[1][j2++] + ai[1][l3++] >> 1;

                for(int l12 = 0; l12 < 8; l12++)
                    ai1[k5++] = ai[2][l5++] + ai[2][i7++] >> 1;

                j2 += col_y_incr;
                l3 += col_y_incr;
            }
            Statistics.endLog(GET_AREA);
            return;
        }
        if(right_half_x_col && !down_half_x_col)
        {
            int k2;
            int i6 = k2 = pixel_per_col_line * k + l;
            int i4;
            int j7 = i4 = pixel_per_col_line * k + l + 1;
            for(int l10 = 0; l10 < 8; l10++)
            {
                int j6 = k2;
                int k7 = i4;
                for(int j12 = 0; j12 < 8; j12++)
                    ai1[k5++] = ai[1][k2++] + ai[1][i4++] >> 1;

                for(int i13 = 0; i13 < 8; i13++)
                    ai1[k5++] = ai[2][j6++] + ai[2][k7++] >> 1;

                k2 += col_y_incr;
                i4 += col_y_incr;
            }
            Statistics.endLog(GET_AREA);
            return;
        }
        int l2;
        int k6 = l2 = pixel_per_col_line * k + l;
        int j4;
        int l7 = j4 = pixel_per_col_line * (k + 1) + l;
        int l4;
        int j8 = l4 = pixel_per_col_line * k + l + 1;
        int j5;
        int l8 = j5 = pixel_per_col_line * (k + 1) + l + 1;
        for(int i11 = 0; i11 < 8; i11++)
        {
            int l6 = l2;
            int i8 = j4;
            int k8 = l4;
            int i9 = j5;
            for(int k12 = 0; k12 < 8; k12++)
                ai1[k5++] = ai[1][l2++] + ai[1][j4++] + ai[1][l4++] + ai[1][j5++] >> 2;

            for(int j13 = 0; j13 < 8; j13++)
                ai1[k5++] = ai[2][l6++] + ai[2][i8++] + ai[2][k8++] + ai[2][i9++] >> 2;

            l2 += col_y_incr;
            j4 += col_y_incr;
            l4 += col_y_incr;
            j5 += col_y_incr;
        }

        Statistics.endLog(GET_AREA);
    }

    public void copyArea(int i, int j, int ai[][], int ai1[][])
    {
        Statistics.startLog(COPY_AREA);
        int k = (i << 4) + down_x;
        int l = (j << 4) + right_x;
        int l4 = pixel_per_lum_line * (i << 4) + (j << 4);
        if(!right_half_x && !down_half_x)
        {
            int i1 = pixel_per_lum_line * k + l;
            int dst[] = ai1[0];
            int src[] = ai[0];
            for(int l8 = 0; l8 < 16; l8++)
            {
                // System.arraycopy(ai[0], i1, ai1[0], l4, 16);
                dst[l4] =    src[i1];
                dst[l4+1] =  src[i1+1];
                dst[l4+2] =  src[i1+2];
                dst[l4+3] =  src[i1+3];
                dst[l4+4] =  src[i1+4];
                dst[l4+5] =  src[i1+5];
                dst[l4+6] =  src[i1+6];
                dst[l4+7] =  src[i1+7];
                dst[l4+8] =  src[i1+8];
                dst[l4+9] =  src[i1+9];
                dst[l4+10] = src[i1+10];
                dst[l4+11] = src[i1+11];
                dst[l4+12] = src[i1+12];
                dst[l4+13] = src[i1+13];
                dst[l4+14] = src[i1+14];
                dst[l4+15] = src[i1+15];

                i1 += pixel_per_lum_line;
                l4 += pixel_per_lum_line;
            }

        }
        else if(!right_half_x && down_half_x)
        {
            int j1 = pixel_per_lum_line * k + l;
            int j2 = pixel_per_lum_line * (k + 1) + l;
            int dst[] = ai1[0];
            int src[] = ai[0];
            for(int i9 = 0; i9 < 16; i9++)
            {
                dst[l4] =    (src[j1] +    src[j2]) >> 1;
                dst[l4+1] =  (src[j1+1] +  src[j2+1]) >> 1;
                dst[l4+2] =  (src[j1+2] +  src[j2+2]) >> 1;
                dst[l4+3] =  (src[j1+3] +  src[j2+3]) >> 1;
                dst[l4+4] =  (src[j1+4] +  src[j2+4]) >> 1;
                dst[l4+5] =  (src[j1+5] +  src[j2+5]) >> 1;
                dst[l4+6] =  (src[j1+6] +  src[j2+6]) >> 1;
                dst[l4+7] =  (src[j1+7] +  src[j2+7]) >> 1;
                dst[l4+8] =  (src[j1+8] +  src[j2+8]) >> 1;
                dst[l4+9] =  (src[j1+9] +  src[j2+9]) >> 1;
                dst[l4+10] = (src[j1+10] + src[j2+10]) >> 1;
                dst[l4+11] = (src[j1+11] + src[j2+11]) >> 1;
                dst[l4+12] = (src[j1+12] + src[j2+12]) >> 1;
                dst[l4+13] = (src[j1+13] + src[j2+13]) >> 1;
                dst[l4+14] = (src[j1+14] + src[j2+14]) >> 1;
                dst[l4+15] = (src[j1+15] + src[j2+15]) >> 1;
                l4 += 16;
                j1 += 16;
                j2 += 16;

                j1 += lum_y_incr;
                j2 += lum_y_incr;
                l4 += lum_y_incr;
            }

        }
        else if(right_half_x && !down_half_x)
        {
            int k1 = pixel_per_lum_line * k + l;
            int k2 = pixel_per_lum_line * k + l + 1;
            int dst[] = ai1[0];
            int src[] = ai[0];
            for(int j9 = 0; j9 < 16; j9++)
            {
                // for(int i11 = 0; i11 < 16; i11++)ai1[0][l4++] = ai[0][k1++] + ai[0][k2++] >> 1;
                dst[l4] =    (src[k1] +    src[k2]) >> 1;
                dst[l4+1] =  (src[k1+1] +  src[k2+1]) >> 1;
                dst[l4+2] =  (src[k1+2] +  src[k2+2]) >> 1;
                dst[l4+3] =  (src[k1+3] +  src[k2+3]) >> 1;
                dst[l4+4] =  (src[k1+4] +  src[k2+4]) >> 1;
                dst[l4+5] =  (src[k1+5] +  src[k2+5]) >> 1;
                dst[l4+6] =  (src[k1+6] +  src[k2+6]) >> 1;
                dst[l4+7] =  (src[k1+7] +  src[k2+7]) >> 1;
                dst[l4+8] =  (src[k1+8] +  src[k2+8]) >> 1;
                dst[l4+9] =  (src[k1+9] +  src[k2+9]) >> 1;
                dst[l4+10] = (src[k1+10] + src[k2+10]) >> 1;
                dst[l4+11] = (src[k1+11] + src[k2+11]) >> 1;
                dst[l4+12] = (src[k1+12] + src[k2+12]) >> 1;
                dst[l4+13] = (src[k1+13] + src[k2+13]) >> 1;
                dst[l4+14] = (src[k1+14] + src[k2+14]) >> 1;
                dst[l4+15] = (src[k1+15] + src[k2+15]) >> 1;
                l4 += 16;
                k1 += 16;
                k2 += 16;

                k1 += lum_y_incr;
                k2 += lum_y_incr;
                l4 += lum_y_incr;
            }

        }
        else
        {
            int l1 = pixel_per_lum_line * k + l;
            int l2 = pixel_per_lum_line * (k + 1) + l;
            int l3 = pixel_per_lum_line * k + l + 1;
            int j4 = pixel_per_lum_line * (k + 1) + l + 1;
            int dst[] = ai1[0];
            int src[] = ai[0];
            for(int k9 = 0; k9 < 16; k9++)
            {
                // for(int j11 = 0; j11 < 16; j11++) ai1[0][l4++] = (ai[0][l1++] + ai[0][l2++] + ai[0][l3++] + ai[0][j4++]) >> 2;
                dst[l4] =    (src[l1] +    src[l2] +    src[l3] +    src[j4]) >> 2;
                dst[l4+1] =  (src[l1+1] +  src[l2+1] +  src[l3+1] +  src[j4+1]) >> 2;
                dst[l4+2] =  (src[l1+2] +  src[l2+2] +  src[l3+2] +  src[j4+2]) >> 2;
                dst[l4+3] =  (src[l1+3] +  src[l2+3] +  src[l3+3] +  src[j4+3]) >> 2;
                dst[l4+4] =  (src[l1+4] +  src[l2+4] +  src[l3+4] +  src[j4+4]) >> 2;
                dst[l4+5] =  (src[l1+5] +  src[l2+5] +  src[l3+5] +  src[j4+5]) >> 2;
                dst[l4+6] =  (src[l1+6] +  src[l2+6] +  src[l3+6] +  src[j4+6]) >> 2;
                dst[l4+7] =  (src[l1+7] +  src[l2+7] +  src[l3+7] +  src[j4+7]) >> 2;
                dst[l4+8] =  (src[l1+8] +  src[l2+8] +  src[l3+8] +  src[j4+8]) >> 2;
                dst[l4+9] =  (src[l1+9] +  src[l2+9] +  src[l3+9] +  src[j4+9]) >> 2;
                dst[l4+10] = (src[l1+10] + src[l2+10] + src[l3+10] + src[j4+10]) >> 2;
                dst[l4+11] = (src[l1+11] + src[l2+11] + src[l3+11] + src[j4+11]) >> 2;
                dst[l4+12] = (src[l1+12] + src[l2+12] + src[l3+12] + src[j4+12]) >> 2;
                dst[l4+13] = (src[l1+13] + src[l2+13] + src[l3+13] + src[j4+13]) >> 2;
                dst[l4+14] = (src[l1+14] + src[l2+14] + src[l3+14] + src[j4+14]) >> 2;
                dst[l4+15] = (src[l1+15] + src[l2+15] + src[l3+15] + src[j4+15]) >> 2;
                l4 += 16;
                l1 += 16;
                l2 += 16;
                l3 += 16;
                j4 += 16;

                l1 += lum_y_incr;
                l2 += lum_y_incr;
                l3 += lum_y_incr;
                j4 += lum_y_incr;
                l4 += lum_y_incr;
            }

        }
        k = (i << 3) + down_x_col;
        l = (j << 3) + right_x_col;
        int i5;
        l4 = i5 = pixel_per_col_line * (i << 3) + (j << 3);
        int i2 = pixel_per_col_line * k + l;
        if(!right_half_x_col && !down_half_x_col)
        {
            i2 = pixel_per_col_line * k + l;
            int dst1[] = ai1[1];
            int src1[] = ai[1];
            int dst2[] = ai1[2];
            int src2[] = ai[2];
            for(int l9 = 0; l9 < 8; l9++)
            {
                // System.arraycopy(ai[1], i2, ai1[1], l4, 8);
                dst1[l4] =   src1[i2];
                dst1[l4+1] = src1[i2+1];
                dst1[l4+2] = src1[i2+2];
                dst1[l4+3] = src1[i2+3];
                dst1[l4+4] = src1[i2+4];
                dst1[l4+5] = src1[i2+5];
                dst1[l4+6] = src1[i2+6];
                dst1[l4+7] = src1[i2+7];

                // System.arraycopy(ai[2], i2, ai1[2], l4, 8);
                dst2[l4] =   src2[i2];
                dst2[l4+1] = src2[i2+1];
                dst2[l4+2] = src2[i2+2];
                dst2[l4+3] = src2[i2+3];
                dst2[l4+4] = src2[i2+4];
                dst2[l4+5] = src2[i2+5];
                dst2[l4+6] = src2[i2+6];
                dst2[l4+7] = src2[i2+7];

                i2 += pixel_per_col_line;
                l4 += pixel_per_col_line;
            }
            Statistics.endLog(COPY_AREA);
            return;
        }
        if(!right_half_x_col && !down_half_x_col)
        {
            i2 = pixel_per_col_line * k + l;
            int i3 = pixel_per_col_line * (k + 1) + l;
            int dst1[] = ai1[1];
            int src1[] = ai[1];
            int dst2[] = ai1[2];
            int src2[] = ai[2];
            for(int i10 = 0; i10 < 8; i10++)
            {
                int j5 = i2;
                int k6 = i3;

                // for(int k11 = 0; k11 < 8; k11++) ai1[1][l4++] = (ai[1][i2++] + ai[1][i3++]) >> 1;
                dst1[l4] =   (src1[i2] +   src1[i3]) >> 1;
                dst1[l4+1] = (src1[i2+1] + src1[i3+1]) >> 1;
                dst1[l4+2] = (src1[i2+2] + src1[i3+2]) >> 1;
                dst1[l4+3] = (src1[i2+3] + src1[i3+3]) >> 1;
                dst1[l4+4] = (src1[i2+4] + src1[i3+4]) >> 1;
                dst1[l4+5] = (src1[i2+5] + src1[i3+5]) >> 1;
                dst1[l4+6] = (src1[i2+6] + src1[i3+6]) >> 1;
                dst1[l4+7] = (src1[i2+7] + src1[i3+7]) >> 1;
                l4 += 8;
                i2 += 8;
                i3 += 8;

                // for(int j12 = 0; j12 < 8; j12++) ai1[2][i5++] = (ai[2][j5++] + ai[2][k6++]) >> 1;
                dst2[i5] =   (src2[j5] +   src2[k6]) >> 1;
                dst2[i5+1] = (src2[j5+1] + src2[k6+1]) >> 1;
                dst2[i5+2] = (src2[j5+2] + src2[k6+2]) >> 1;
                dst2[i5+3] = (src2[j5+3] + src2[k6+3]) >> 1;
                dst2[i5+4] = (src2[j5+4] + src2[k6+4]) >> 1;
                dst2[i5+5] = (src2[j5+5] + src2[k6+5]) >> 1;
                dst2[i5+6] = (src2[j5+6] + src2[k6+6]) >> 1;
                dst2[i5+7] = (src2[j5+7] + src2[k6+7]) >> 1;
                i5 += 8;
                j5 += 8;
                k6 += 8;

                i2 += col_y_incr;
                i3 += col_y_incr;
                l4 += col_y_incr;
                i5 += col_y_incr;
            }
            Statistics.endLog(COPY_AREA);
            return;
        }
        if(right_half_x_col && !down_half_x_col)
        {
            int k5 = i2 = pixel_per_col_line * k + l;
            int j3;
            int l6 = j3 = pixel_per_col_line * k + l + 1;
            int dst1[] = ai1[1];
            int src1[] = ai[1];
            int dst2[] = ai1[2];
            int src2[] = ai[2];
            for(int j10 = 0; j10 < 8; j10++)
            {
                int l5 = i2;
                int i7 = j3;
                // for(int l11 = 0; l11 < 8; l11++) ai1[1][l4++] = ai[1][i2++] + ai[1][j3++] >> 1;
                dst1[l4] =   src1[i2] +   src1[j3] >> 1;
                dst1[l4+1] = src1[i2+1] + src1[j3+1] >> 1;
                dst1[l4+2] = src1[i2+2] + src1[j3+2] >> 1;
                dst1[l4+3] = src1[i2+3] + src1[j3+3] >> 1;
                dst1[l4+4] = src1[i2+4] + src1[j3+4] >> 1;
                dst1[l4+5] = src1[i2+5] + src1[j3+5] >> 1;
                dst1[l4+6] = src1[i2+6] + src1[j3+6] >> 1;
                dst1[l4+7] = src1[i2+7] + src1[j3+7] >> 1;
                l4 += 8;
                i2 += 8;
                j3 += 8;

                // for(int k12 = 0; k12 < 8; k12++) ai1[2][i5++] = ai[2][l5++] + ai[2][i7++] >> 1;
                dst2[i5] =   src2[l5] +   src2[i7] >> 1;
                dst2[i5+1] = src2[l5+1] + src2[i7+1] >> 1;
                dst2[i5+2] = src2[l5+2] + src2[i7+2] >> 1;
                dst2[i5+3] = src2[l5+3] + src2[i7+3] >> 1;
                dst2[i5+4] = src2[l5+4] + src2[i7+4] >> 1;
                dst2[i5+5] = src2[l5+5] + src2[i7+5] >> 1;
                dst2[i5+6] = src2[l5+6] + src2[i7+6] >> 1;
                dst2[i5+7] = src2[l5+7] + src2[i7+7] >> 1;
                i5 += 8;
                l5 += 8;
                i7 += 8;

                i2 += col_y_incr;
                j3 += col_y_incr;
                l4 += col_y_incr;
                i5 += col_y_incr;
            }
            Statistics.endLog(COPY_AREA);
            return;
        }
        int i6 = i2 = pixel_per_col_line * k + l;
        int k3;
        int j7 = k3 = pixel_per_col_line * (k + 1) + l;
        int i4;
        int l7 = i4 = pixel_per_col_line * k + l + 1;
        int k4;
        int j8 = k4 = pixel_per_col_line * (k + 1) + l + 1;
        int dst1[] = ai1[1];
        int src1[] = ai[1];
        int dst2[] = ai1[2];
        int src2[] = ai[2];
        for(int k10 = 0; k10 < 8; k10++)
        {
            int j6 = i2;
            int k7 = k3;
            int i8 = i4;
            int k8 = k4;

            // for(int i12 = 0; i12 < 8; i12++) ai1[1][l4++] = ai[1][i2++] + ai[1][k3++] + ai[1][i4++] + ai[1][k4++] >> 2;
            dst1[l4] =   (src1[i2] +   src1[k3] +   src1[i4] +   src1[k4]) >> 2;
            dst1[l4+1] = (src1[i2+1] + src1[k3+1] + src1[i4+1] + src1[k4+1]) >> 2;
            dst1[l4+2] = (src1[i2+2] + src1[k3+2] + src1[i4+2] + src1[k4+2]) >> 2;
            dst1[l4+3] = (src1[i2+3] + src1[k3+3] + src1[i4+3] + src1[k4+3]) >> 2;
            dst1[l4+4] = (src1[i2+4] + src1[k3+4] + src1[i4+4] + src1[k4+4]) >> 2;
            dst1[l4+5] = (src1[i2+5] + src1[k3+5] + src1[i4+5] + src1[k4+5]) >> 2;
            dst1[l4+6] = (src1[i2+6] + src1[k3+6] + src1[i4+6] + src1[k4+6]) >> 2;
            dst1[l4+7] = (src1[i2+7] + src1[k3+7] + src1[i4+7] + src1[k4+7]) >> 2;
            l4 += 8;
            i2 += 8;
            i4 += 8;
            k4 += 8;

            // for(int l12 = 0; l12 < 8; l12++) ai1[2][i5++] = ai[2][j6++] + ai[2][k7++] + ai[2][i8++] + ai[2][k8++] >> 2;
            dst2[i5] =   (src2[j6] +   src2[k7] +   src2[i8] +   src2[k8]) >> 2;
            dst2[i5+1] = (src2[j6+1] + src2[k7+1] + src2[i8+1] + src2[k8+1]) >> 2;
            dst2[i5+2] = (src2[j6+2] + src2[k7+2] + src2[i8+2] + src2[k8+2]) >> 2;
            dst2[i5+3] = (src2[j6+3] + src2[k7+3] + src2[i8+3] + src2[k8+3]) >> 2;
            dst2[i5+4] = (src2[j6+4] + src2[k7+4] + src2[i8+4] + src2[k8+4]) >> 2;
            dst2[i5+5] = (src2[j6+5] + src2[k7+5] + src2[i8+5] + src2[k8+5]) >> 2;
            dst2[i5+6] = (src2[j6+6] + src2[k7+6] + src2[i8+6] + src2[k8+6]) >> 2;
            dst2[i5+7] = (src2[j6+7] + src2[k7+7] + src2[i8+7] + src2[k8+7]) >> 2;
            i5 += 8;
            j6 += 8;
            i8 += 8;
            k8 += 8;

            i2 += col_y_incr;
            k3 += col_y_incr;
            i4 += col_y_incr;
            k4 += col_y_incr;
            l4 += col_y_incr;
            i5 += col_y_incr;
        }

        Statistics.endLog(COPY_AREA);
    }

    public void copyUnchanged(int i, int j, int ai[][], int ai1[][])
    {
        Statistics.startLog(COPY_UNCHANGED);
        int k = pixel_per_lum_line * (i << 4) + (j << 4);
        int dst0[] = ai1[0];
        int src0[] = ai[0];
        for(int l = 0; l < 16; l++)
        {
            // System.arraycopy(ai[0], k, ai1[0], k, 16);
            dst0[k] =    src0[k];
            dst0[k+1] =  src0[k+1];
            dst0[k+2] =  src0[k+2];
            dst0[k+3] =  src0[k+3];
            dst0[k+4] =  src0[k+4];
            dst0[k+5] =  src0[k+5];
            dst0[k+6] =  src0[k+6];
            dst0[k+7] =  src0[k+7];
            dst0[k+8] =  src0[k+8];
            dst0[k+9] =  src0[k+9];
            dst0[k+10] = src0[k+10];
            dst0[k+11] = src0[k+11];
            dst0[k+12] = src0[k+12];
            dst0[k+13] = src0[k+13];
            dst0[k+14] = src0[k+14];
            dst0[k+15] = src0[k+15];
            k += pixel_per_lum_line;
        }

        k = pixel_per_col_line * (i << 3) + (j << 3);
        int dst1[] = ai1[1];
        int src1[] = ai[1];
        int dst2[] = ai1[2];
        int src2[] = ai[2];
        for(int i1 = 0; i1 < 8; i1++)
        {
            // System.arraycopy(ai[1], k, ai1[1], k, 8);
            dst1[k] =   src1[k];
            dst1[k+1] = src1[k+1];
            dst1[k+2] = src1[k+2];
            dst1[k+3] = src1[k+3];
            dst1[k+4] = src1[k+4];
            dst1[k+5] = src1[k+5];
            dst1[k+6] = src1[k+6];
            dst1[k+7] = src1[k+7];

            // System.arraycopy(ai[2], k, ai1[2], k, 8);
            dst2[k] =   src2[k];
            dst2[k+1] = src2[k+1];
            dst2[k+2] = src2[k+2];
            dst2[k+3] = src2[k+3];
            dst2[k+4] = src2[k+4];
            dst2[k+5] = src2[k+5];
            dst2[k+6] = src2[k+6];
            dst2[k+7] = src2[k+7];

            k += pixel_per_col_line;
        }
        Statistics.endLog(COPY_UNCHANGED);
    }

    public void putArea(int i, int j, int ai[], int ai1[][])
    {
        Statistics.startLog(PUT_AREA_1);
        int k = pixel_per_lum_line * (i << 4) + (j << 4);
        int l = 0;
        for(int i1 = 0; i1 < 16; i1++)
        {
            System.arraycopy(ai, l, ai1[0], k, 16);
            l += 16;
            k += pixel_per_lum_line;
        }

        k = pixel_per_col_line * (i << 3) + (j << 3);
        for(int j1 = 0; j1 < 8; j1++)
        {
            System.arraycopy(ai, l, ai1[1], k, 8);
            l += 8;
            System.arraycopy(ai, l, ai1[2], k, 8);
            l += 8;
            k += pixel_per_col_line;
        }
        Statistics.endLog(PUT_AREA_1);
    }


	// /*
    public void putArea(int i, int j, int ai[], int ai1[], int ai2[][])
    {
        Statistics.startLog(PUT_AREA_2);
        int k = pixel_per_lum_line * (i << 4) + (j << 4);
        int i1 = 0;
        // int ai2_0[] = ai2[0];
        // int ai2_1[] = ai2[1];
        // int ai2_2[] = ai2[2];
        for(int j1 = 0; j1 < 16; j1++)
        {
            for(int k1 = 0; k1 < 16; k1++)
            {
                ai2[0][k++] = ai[i1] + ai1[i1] >> 1;
                // ai2_0[k++] = ai[i1] + ai1[i1] >> 1;
                i1++;
            }

            k += lum_y_incr;
        }

        k = pixel_per_col_line * (i << 3) + (j << 3);
        for(int l1 = 0; l1 < 8; l1++)
        {
            int l = k;
            for(int i2 = 0; i2 < 8; i2++)
            {
                ai2[1][k++] = ai[i1] + ai1[i1] >> 1;
                // ai2_1[k++] = ai[i1] + ai1[i1] >> 1;
                i1++;
            }

            for(int j2 = 0; j2 < 8; j2++)
            {
                ai2[2][l++] = ai[i1] + ai1[i1] >> 1;
                // ai2_2[l++] = ai[i1] + ai1[i1] >> 1;
                i1++;
            }

            k += col_y_incr;
        }
        Statistics.endLog(PUT_AREA_2);
    }

    public MotionData()
    {
        right_half_x = false;
        down_half_x = false;
        right_half_x_col = false;
        down_half_x_col = false;
        Full_pel_x_vector = true;
    }
}
    

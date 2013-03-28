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

class IDCT
{
    private static final int BLOCK_SIZE = 64;
    
    private static final int CONST_BITS = 11;
    public final int VAL_BITS = 11;
    private final int ALLBITS = 22;
    private final int TWO = 12;
    private int matr1[] = new int[BLOCK_SIZE];
    private int matr2[] = new int[BLOCK_SIZE];

    private static int IDFT_table[][] = new int[BLOCK_SIZE][BLOCK_SIZE];

    public void norm(int ai[])
    {
        // double MathCos[] = new double[8];
        long longMathCos[] = new long[8];
        int index;

        longMathCos[0]=262144;
        longMathCos[1]=257106;
        longMathCos[2]=242189;
        longMathCos[3]=217964;
        longMathCos[4]=185363;
        longMathCos[5]=145639;
        longMathCos[6]=100318;
        longMathCos[7]=51141;

        for(int j = 0; j < 8; j++)
        {
            for(int i = 0; i < 8; i++)
            {
                long l = ai[j * 8 + i];
                if(i == 0 && j == 0) l /= 8L;
                else if(i == 0 || j == 0) l = (l*262144L)/(1482910L);
                else l /= 4L;
                ai[j * 8 + i] = (int)(((l * 2048L * longMathCos[i])/262144L * longMathCos[j])/262144L);
            }
        }

    }

    public void invers_dct_special(int ai[], int i)
    {
        if(i == 0)
        {
            int j = ai[0] >> 11;
            for(int i1 = 0; i1 < 64;)
                ai[i1++] = j;
            return;
        }
        int k = ai[i];
        int ai1[] = IDFT_table[i++];
        boolean flag = false;
        for(int l = 0; l < 64; l++)
            ai[l] = (ai1[l] * k) >> 9;

    }

    public void invers_dct(int ai[])
    {
        int i19 = 0;
        int j19 = 0;
        int k19 = 0;
        int l19 = 0;
        int l21;
        int i22 = l21 = 0;


        int matr1[] = this.matr1;
        int matr2[] = this.matr2;

        for(; l21 < 64; l21 += 8)
        {
            matr1[i22++] = ai[l21];
            matr1[i22++] = ai[l21 + 4];
            int i8;
            int l11;
            matr1[i22++] = (i8 = ai[l21 + 2]) - (l11 = ai[l21 + 6]);
            matr1[i22++] = i8 + l11;
            int j9;
            int k10;
            matr1[i22++] = -(j9 = ai[l21 + 3]) + (k10 = ai[l21 + 5]);
            int l6;
            int i13;
            int j14;
            int k15;
            matr1[i22++] = (k15 = l6 = ai[l21 + 1] + (i13 = ai[l21 + 7])) - (j14 = j9 + k10);
            matr1[i22++] = l6 - i13;
            matr1[i22++] = k15 + j14;
        }

        int i21;
        i22 = i21 = 0;
        for(; i21 < 8; i21++)
            switch(i21)
            {
            case 0: // '\0'
            case 1: // '\001'
            case 3: // '\003'
            case 7: // '\007'
                int k9;
                int l10;
                int k1 = (k9 = matr1[24 + i21]) - (l10 = matr1[40 + i21]);
                int i7;
                int j13;
                int j3 = (i7 = matr1[8 + i21]) - (j13 = matr1[56 + i21]);
                int j18 = 1567 * (j3 - k1);
                matr2[i22++] = matr1[i21] << 11;
                matr2[i22++] = matr1[32 + i21] << 11;
                int j8;
                int i12;
                matr2[i22++] = ((j8 = matr1[16 + i21]) - (i12 = matr1[48 + i21])) * 2896;
                matr2[i22++] = j8 + i12 << 11;
                matr2[i22++] = 2217 * k1 - j18;
                int k14;
                int l15;
                matr2[i22++] = ((l15 = i7 + j13) - (k14 = k9 + l10)) * 2896;
                matr2[i22++] = 5352 * j3 - j18;
                matr2[i22++] = l15 + k14 << 11;
                break;

            case 2: // '\002'
            case 5: // '\005'
                int l9;
                int i11;
                int l1 = (l9 = matr1[24 + i21]) - (i11 = matr1[40 + i21]);
                int j7;
                int k13;
                int k3 = (j7 = matr1[8 + i21]) - (k13 = matr1[56 + i21]);
                int k18 = 2217 * (k3 - l1);
                matr2[i22++] = 2896 * matr1[i21];
                matr2[i22++] = 2896 * matr1[i21 + 32];
                int k8;
                int j12;
                matr2[i22++] = (k8 = matr1[16 + i21]) - (j12 = matr1[48 + i21]) << 12;
                matr2[i22++] = 2896 * (k8 + j12);
                matr2[i22++] = 3135 * l1 - k18;
                int l14;
                int i16;
                matr2[i22++] = (i16 = j7 + k13) - (l14 = l9 + i11) << 12;
                matr2[i22++] = 7568 * k3 - k18;
                matr2[i22++] = 2896 * (i16 + l14);
                break;

            case 4: // '\004'
                matr2[i22++] = matr1[i21];
                matr2[i22++] = matr1[32 + i21];
                int l8;
                int k12;
                matr2[i22++] = (l8 = matr1[16 + i21]) - (k12 = matr1[48 + i21]);
                matr2[i22] = l8 + k12;
                int i10;
                int j11;
                i19 = k19 = -(i10 = matr1[24 + i21]) + (j11 = matr1[40 + i21]);
                i22 += 2;
                int k7;
                int l13;
                int i15;
                int j16;
                matr2[i22] = (j16 = (k7 = matr1[8 + i21]) + (l13 = matr1[56 + i21])) - (i15 = i10 + j11);
                l19 = -(j19 = k7 - l13);
                i22 += 2;
                matr2[i22++] = j16 + i15;
                break;

            case 6: // '\006'
                matr2[i22++] = matr1[i21];
                matr2[i22++] = matr1[32 + i21];
                int i9;
                int l12;
                matr2[i22++] = (i9 = matr1[16 + i21]) - (l12 = matr1[48 + i21]);
                matr2[i22] = i9 + l12;
                int i2;
                int j10;
                int k11;
                j19 += i2 = -(j10 = matr1[24 + i21]) + (k11 = matr1[40 + i21]);
                l19 += i2;
                i22 += 2;
                int l7;
                int i14;
                int j15;
                int k16;
                matr2[i22] = (k16 = (l7 = matr1[8 + i21]) + (i14 = matr1[56 + i21])) - (j15 = j10 + k11);
                int l3;
                k19 += l3 = l7 - i14;
                i19 -= l3;
                i22 += 2;
                matr2[i22++] = k16 + j15;
                break;
            }

        int i20 = 2896 * (i19 + j19);
        int j20 = 2896 * (i19 - j19);
        int k20 = k19 << 12;
        int l20 = l19 << 12;
        matr2[36] = i20 + k20;
        matr2[38] = j20 + l20;
        matr2[52] = j20 - l20;
        matr2[54] = k20 - i20;
        int l18 = 1567 * (matr2[32] + matr2[48]);
        matr2[32] = -2217 * matr2[32] - l18;
        matr2[48] = 5352 * matr2[48] - l18;
        l18 = 1567 * (matr2[33] + matr2[49]);
        matr2[33] = -2217 * matr2[33] - l18;
        matr2[49] = 5352 * matr2[49] - l18;
        l18 = 2217 * (matr2[34] + matr2[50]);
        matr2[34] = -3135 * matr2[34] - l18;
        matr2[50] = 7568 * matr2[50] - l18;
        l18 = 1567 * (matr2[35] + matr2[51]);
        matr2[35] = -2217 * matr2[35] - l18;
        matr2[51] = 5352 * matr2[51] - l18;
        l18 = 2217 * (matr2[37] + matr2[53]);
        matr2[37] = -3135 * matr2[37] - l18;
        matr2[53] = 7568 * matr2[53] - l18;
        l18 = 1567 * (matr2[39] + matr2[55]);
        matr2[39] = -2217 * matr2[39] - l18;
        matr2[55] = 5352 * matr2[55] - l18;
        int j21;
        for(int j22 = j21 = 0; j21 < 8; j22 += 8)
        {
            int j2;
            int l17;
            matr1[j22] = (j2 = (l17 = matr2[j22] + matr2[j22 + 1]) + matr2[j22 + 3]) + matr2[j22 + 7];
            int i;
            int k;
            int i4;
            int k4;
            matr1[j22 + 3] = (i4 = l17 - matr2[j22 + 3]) - (k4 = matr2[j22 + 4] - (i = (k = matr2[j22 + 6] - matr2[j22 + 7]) - matr2[j22 + 5]));
            matr1[j22 + 4] = i4 + k4;
            int i1;
            int l16;
            int j17;
            matr1[j22 + 1] = (i1 = (l16 = matr2[j22] - matr2[j22 + 1]) + (j17 = matr2[j22 + 2] - matr2[j22 + 3])) + k;
            int l2;
            matr1[j22 + 2] = (l2 = l16 - j17) - i;
            matr1[j22 + 5] = l2 + i;
            matr1[j22 + 6] = i1 - k;
            matr1[j22 + 7] = j2 - matr2[j22 + 7];
            j21++;
        }

        int i5 = 8;
        int j5 = 16;
        int k5 = 24;
        int l5 = 32;
        int i6 = 40;
        int j6 = 48;
        int k6 = 56;
        int k21;
        for(int k22 = k21 = 0; k22 < 64; k22 += 8)
        {
            int k2;
            int i18;
            ai[k22] = (k2 = (i18 = matr1[k21] + matr1[i5]) + matr1[k5]) + matr1[k6] >> 22;
            int j;
            int l;
            int j4;
            int l4;
            ai[k22 + 3] = (j4 = i18 - matr1[k5]) - (l4 = matr1[l5++] - (j = (l = matr1[j6++] - matr1[k6]) - matr1[i6++])) >> 22;
            ai[k22 + 4] = j4 + l4 >> 22;
            int j1;
            int i17;
            int k17;
            ai[k22 + 1] = (j1 = (i17 = matr1[k21++] - matr1[i5++]) + (k17 = matr1[j5++] - matr1[k5++])) + l >> 22;
            int i3;
            ai[k22 + 2] = (i3 = i17 - k17) - j >> 22;
            ai[k22 + 5] = i3 + j >> 22;
            ai[k22 + 6] = j1 - l >> 22;
            ai[k22 + 7] = k2 - matr1[k6++] >> 22;
        }
    }


    

    static
    {
        for (int i=0 ; i<64 ; i++)
        {
            for(int j=0 ; j<64 ; j++)
                IDFT_table[i][j] = 0;
        }

        IDFT_table[0][0]=1; IDFT_table[1][0]=1; IDFT_table[2][0]=1; IDFT_table[3][0]=1;
        IDFT_table[3][2]=-1; IDFT_table[3][10]=-1; IDFT_table[3][18]=-1; IDFT_table[3][26]=-1;
        IDFT_table[3][34]=-1; IDFT_table[3][42]=-1; IDFT_table[4][0]=1; IDFT_table[5][0]=1;
        IDFT_table[5][1]=-1; IDFT_table[5][4]=-1; IDFT_table[5][9]=-1; IDFT_table[5][12]=-1;
        IDFT_table[5][17]=-1; IDFT_table[5][20]=-1; IDFT_table[5][25]=-1; IDFT_table[5][28]=-1;
        IDFT_table[5][33]=-1; IDFT_table[5][36]=-1; IDFT_table[5][41]=-1; IDFT_table[6][0]=1;
        IDFT_table[6][1]=-2; IDFT_table[6][2]=1; IDFT_table[6][5]=1; IDFT_table[6][6]=-1;
        IDFT_table[6][9]=-2; IDFT_table[6][10]=1; IDFT_table[6][13]=1; IDFT_table[6][14]=-1;
        IDFT_table[6][17]=-2; IDFT_table[6][18]=1; IDFT_table[6][21]=1; IDFT_table[6][22]=-1;
        IDFT_table[6][25]=-2; IDFT_table[6][26]=1; IDFT_table[6][33]=-2; IDFT_table[6][34]=1;
        IDFT_table[6][41]=-1; IDFT_table[6][42]=1; IDFT_table[6][49]=-1; IDFT_table[7][0]=1;
        IDFT_table[7][2]=1; IDFT_table[7][3]=-2; IDFT_table[7][4]=1; IDFT_table[7][5]=-1;
        IDFT_table[7][10]=1; IDFT_table[7][11]=-2; IDFT_table[7][12]=1; IDFT_table[7][13]=-1;
        IDFT_table[7][18]=1; IDFT_table[7][19]=-2; IDFT_table[7][20]=1; IDFT_table[7][21]=-1;
        IDFT_table[7][26]=1; IDFT_table[7][27]=-2; IDFT_table[7][28]=1; IDFT_table[7][29]=-1;
        IDFT_table[7][34]=1; IDFT_table[7][35]=-1; IDFT_table[7][36]=1; IDFT_table[7][37]=-1;
        IDFT_table[7][42]=1; IDFT_table[7][43]=-1; IDFT_table[8][0]=1; IDFT_table[9][0]=1;
        IDFT_table[10][0]=1; IDFT_table[11][0]=1; IDFT_table[11][2]=-1; IDFT_table[12][0]=1;
        IDFT_table[13][0]=1; IDFT_table[13][1]=-1; IDFT_table[13][4]=-1; IDFT_table[13][9]=-1;
        IDFT_table[13][12]=-1; IDFT_table[14][0]=1; IDFT_table[14][1]=-2; IDFT_table[14][2]=1;
        IDFT_table[14][5]=1; IDFT_table[14][6]=-1; IDFT_table[14][9]=-2; IDFT_table[14][10]=1;
        IDFT_table[14][13]=1; IDFT_table[14][14]=-1; IDFT_table[14][17]=-1; IDFT_table[14][42]=-1;
        IDFT_table[14][50]=-1; IDFT_table[15][0]=1; IDFT_table[15][2]=1; IDFT_table[15][3]=-2;
        IDFT_table[15][4]=1; IDFT_table[15][5]=-1; IDFT_table[15][10]=1; IDFT_table[15][11]=-2;
        IDFT_table[15][12]=1; IDFT_table[15][13]=-1; IDFT_table[15][19]=-1; IDFT_table[15][21]=-1;
        IDFT_table[15][42]=-1; IDFT_table[15][50]=-1; IDFT_table[16][0]=1; IDFT_table[17][0]=1;
        IDFT_table[18][0]=1; IDFT_table[19][0]=1; IDFT_table[19][2]=-1; IDFT_table[20][0]=1;
        IDFT_table[21][0]=1; IDFT_table[21][1]=-1; IDFT_table[21][4]=-1; IDFT_table[21][27]=-1;
        IDFT_table[21][35]=-1; IDFT_table[22][0]=1; IDFT_table[22][1]=-2; IDFT_table[22][2]=1;
        IDFT_table[22][5]=1; IDFT_table[22][6]=-1; IDFT_table[22][25]=1; IDFT_table[22][26]=-2;
        IDFT_table[22][29]=-1; IDFT_table[22][33]=1; IDFT_table[22][34]=-1; IDFT_table[22][37]=-1;
        IDFT_table[23][0]=1; IDFT_table[23][2]=1; IDFT_table[23][3]=-2; IDFT_table[23][4]=1;
        IDFT_table[23][5]=-1; IDFT_table[23][26]=-2; IDFT_table[23][27]=1; IDFT_table[23][28]=-1;
        IDFT_table[23][34]=-1; IDFT_table[23][35]=1; IDFT_table[23][36]=-1; IDFT_table[24][0]=1;
        IDFT_table[24][16]=-1; IDFT_table[24][17]=-1; IDFT_table[24][18]=-1; IDFT_table[24][19]=-1;
        IDFT_table[24][20]=-1; IDFT_table[24][21]=-1; IDFT_table[25][0]=1; IDFT_table[25][16]=-1;
        IDFT_table[26][0]=1; IDFT_table[26][16]=-1; IDFT_table[27][0]=1; IDFT_table[27][2]=-1;
        IDFT_table[27][16]=-1; IDFT_table[27][21]=-1; IDFT_table[27][42]=-1; IDFT_table[28][0]=1;
        IDFT_table[28][16]=-1; IDFT_table[28][19]=-1; IDFT_table[28][20]=-1; IDFT_table[28][41]=-1;
        IDFT_table[28][42]=-1; IDFT_table[29][0]=1; IDFT_table[29][1]=-1; IDFT_table[29][4]=-1;
        IDFT_table[29][16]=-1; IDFT_table[29][17]=1; IDFT_table[29][19]=-1; IDFT_table[29][22]=-1;
        IDFT_table[29][33]=-1; IDFT_table[29][41]=-1; IDFT_table[30][0]=1; IDFT_table[30][1]=-2;
        IDFT_table[30][2]=1; IDFT_table[30][5]=1; IDFT_table[30][6]=-1; IDFT_table[30][16]=-1;
        IDFT_table[30][17]=1; IDFT_table[30][18]=-2; IDFT_table[30][21]=-1; IDFT_table[30][26]=-1;
        IDFT_table[30][33]=-1; IDFT_table[30][41]=-1; IDFT_table[30][42]=1; IDFT_table[31][0]=1;
        IDFT_table[31][2]=1; IDFT_table[31][3]=-2; IDFT_table[31][4]=1; IDFT_table[31][5]=-1;
        IDFT_table[31][16]=-1; IDFT_table[31][18]=-2; IDFT_table[31][19]=1; IDFT_table[31][20]=-1;
        IDFT_table[31][21]=1; IDFT_table[31][26]=-1; IDFT_table[31][28]=-1; IDFT_table[31][35]=-1;
        IDFT_table[31][41]=-1; IDFT_table[31][42]=1; IDFT_table[31][43]=-1; IDFT_table[32][0]=1;
        IDFT_table[33][0]=1; IDFT_table[34][0]=1; IDFT_table[35][0]=1; IDFT_table[35][2]=-1;
        IDFT_table[35][13]=-1; IDFT_table[35][21]=-1; IDFT_table[35][26]=-1; IDFT_table[35][34]=-1;
        IDFT_table[36][0]=1; IDFT_table[37][0]=1; IDFT_table[37][1]=-1; IDFT_table[37][4]=-1;
        IDFT_table[37][11]=-1; IDFT_table[37][19]=-1; IDFT_table[37][25]=-1; IDFT_table[37][28]=-1;
        IDFT_table[37][33]=-1; IDFT_table[37][36]=-1; IDFT_table[38][0]=1; IDFT_table[38][1]=-2;
        IDFT_table[38][2]=1; IDFT_table[38][5]=1; IDFT_table[38][6]=-1; IDFT_table[38][9]=1;
        IDFT_table[38][10]=-2; IDFT_table[38][13]=-1; IDFT_table[38][17]=1; IDFT_table[38][18]=-2;
        IDFT_table[38][21]=-1; IDFT_table[38][25]=-2; IDFT_table[38][26]=1; IDFT_table[38][33]=-2;
        IDFT_table[38][34]=1; IDFT_table[38][41]=1; IDFT_table[38][42]=-1; IDFT_table[38][50]=-1;
        IDFT_table[39][0]=1; IDFT_table[39][2]=1; IDFT_table[39][3]=-2; IDFT_table[39][4]=1;
        IDFT_table[39][5]=-1; IDFT_table[39][10]=-2; IDFT_table[39][11]=1; IDFT_table[39][12]=-2;
        IDFT_table[39][13]=1; IDFT_table[39][18]=-2; IDFT_table[39][19]=1; IDFT_table[39][20]=-1;
        IDFT_table[39][21]=1; IDFT_table[39][26]=1; IDFT_table[39][27]=-2; IDFT_table[39][28]=1;
        IDFT_table[39][29]=-1; IDFT_table[39][34]=1; IDFT_table[39][35]=-1; IDFT_table[39][36]=1;
        IDFT_table[39][37]=-1; IDFT_table[39][42]=-1; IDFT_table[39][44]=-1; IDFT_table[39][50]=-1;
        IDFT_table[40][0]=1; IDFT_table[40][8]=-1; IDFT_table[40][9]=-1; IDFT_table[40][10]=-1;
        IDFT_table[40][11]=-1; IDFT_table[40][12]=-1; IDFT_table[40][13]=-1; IDFT_table[40][32]=-1;
        IDFT_table[40][33]=-1; IDFT_table[40][34]=-1; IDFT_table[40][35]=-1; IDFT_table[40][36]=-1;
        IDFT_table[41][0]=1; IDFT_table[41][8]=-1; IDFT_table[41][9]=-1; IDFT_table[41][32]=-1;
        IDFT_table[41][33]=-1; IDFT_table[42][0]=1; IDFT_table[42][8]=-1; IDFT_table[42][27]=-1;
        IDFT_table[42][28]=-1; IDFT_table[42][32]=-1; IDFT_table[43][0]=1; IDFT_table[43][2]=-1;
        IDFT_table[43][8]=-1; IDFT_table[43][10]=1; IDFT_table[43][12]=-1; IDFT_table[43][13]=-1;
        IDFT_table[43][26]=-1; IDFT_table[43][32]=-1; IDFT_table[43][50]=-1; IDFT_table[44][0]=1;
        IDFT_table[44][8]=-1; IDFT_table[44][11]=-1; IDFT_table[44][12]=-1; IDFT_table[44][25]=-1;
        IDFT_table[44][26]=-1; IDFT_table[44][32]=-1; IDFT_table[44][35]=-1; IDFT_table[44][36]=-1;
        IDFT_table[45][0]=1; IDFT_table[45][1]=-1; IDFT_table[45][4]=-1; IDFT_table[45][8]=-1;
        IDFT_table[45][9]=2; IDFT_table[45][11]=-2; IDFT_table[45][12]=1; IDFT_table[45][14]=-1;
        IDFT_table[45][25]=-2; IDFT_table[45][27]=1; IDFT_table[45][28]=-1; IDFT_table[45][32]=-1;
        IDFT_table[45][33]=1; IDFT_table[45][35]=-1; IDFT_table[45][36]=1; IDFT_table[45][49]=-1;
        IDFT_table[46][0]=1; IDFT_table[46][1]=-2; IDFT_table[46][2]=1; IDFT_table[46][5]=1;
        IDFT_table[46][6]=-1; IDFT_table[46][8]=-1; IDFT_table[46][9]=3; IDFT_table[46][10]=-4;
        IDFT_table[46][13]=-2; IDFT_table[46][14]=1; IDFT_table[46][25]=-3; IDFT_table[46][26]=2;
        IDFT_table[46][27]=-1; IDFT_table[46][28]=-1; IDFT_table[46][29]=1; IDFT_table[46][30]=-1;
        IDFT_table[46][32]=-1; IDFT_table[46][33]=2; IDFT_table[46][34]=-2; IDFT_table[46][37]=-1;
        IDFT_table[46][49]=-1; IDFT_table[46][50]=1; IDFT_table[47][0]=1; IDFT_table[47][2]=1;
        IDFT_table[47][3]=-2; IDFT_table[47][4]=1; IDFT_table[47][5]=-1; IDFT_table[47][8]=-1;
        IDFT_table[47][10]=-4; IDFT_table[47][11]=3; IDFT_table[47][12]=-3; IDFT_table[47][13]=2;
        IDFT_table[47][25]=-1; IDFT_table[47][26]=2; IDFT_table[47][27]=-2; IDFT_table[47][28]=1;
        IDFT_table[47][29]=-1; IDFT_table[47][32]=-1; IDFT_table[47][34]=-2; IDFT_table[47][35]=1;
        IDFT_table[47][36]=-2; IDFT_table[47][37]=1; IDFT_table[47][50]=1; IDFT_table[47][51]=-1;
        IDFT_table[47][52]=1; IDFT_table[47][53]=-1; IDFT_table[48][0]=1; IDFT_table[48][8]=-2;
        IDFT_table[48][9]=-2; IDFT_table[48][10]=-2; IDFT_table[48][11]=-2; IDFT_table[48][12]=-2;
        IDFT_table[48][13]=-1; IDFT_table[48][14]=-1; IDFT_table[48][16]=1; IDFT_table[48][17]=1;
        IDFT_table[48][18]=1; IDFT_table[48][19]=1; IDFT_table[48][20]=1; IDFT_table[48][21]=1;
        IDFT_table[48][40]=1; IDFT_table[48][41]=1; IDFT_table[48][42]=1; IDFT_table[48][48]=-1;
        IDFT_table[48][49]=-1; IDFT_table[48][50]=-1; IDFT_table[49][0]=1; IDFT_table[49][8]=-2;
        IDFT_table[49][9]=-2; IDFT_table[49][10]=-1; IDFT_table[49][16]=1; IDFT_table[49][17]=1;
        IDFT_table[49][21]=-1; IDFT_table[49][22]=-1; IDFT_table[49][40]=1; IDFT_table[49][41]=1;
        IDFT_table[49][48]=-1; IDFT_table[49][49]=-1; IDFT_table[50][0]=1; IDFT_table[50][8]=-2;
        IDFT_table[50][11]=1; IDFT_table[50][12]=1; IDFT_table[50][16]=1; IDFT_table[50][19]=-2;
        IDFT_table[50][20]=-1; IDFT_table[50][40]=1; IDFT_table[50][43]=-1; IDFT_table[50][44]=-1;
        IDFT_table[50][48]=-1; IDFT_table[51][0]=1; IDFT_table[51][2]=-1; IDFT_table[51][8]=-2;
        IDFT_table[51][10]=1; IDFT_table[51][12]=-1; IDFT_table[51][13]=-1; IDFT_table[51][16]=1;
        IDFT_table[51][18]=-2; IDFT_table[51][19]=-1; IDFT_table[51][21]=1; IDFT_table[51][40]=1;
        IDFT_table[51][42]=-1; IDFT_table[51][48]=-1; IDFT_table[52][0]=1; IDFT_table[52][8]=-2;
        IDFT_table[52][9]=1; IDFT_table[52][10]=1; IDFT_table[52][11]=-2; IDFT_table[52][12]=-2;
        IDFT_table[52][13]=1; IDFT_table[52][16]=1; IDFT_table[52][17]=-2; IDFT_table[52][18]=-2;
        IDFT_table[52][19]=1; IDFT_table[52][20]=1; IDFT_table[52][21]=-1; IDFT_table[52][22]=-1;
        IDFT_table[52][40]=1; IDFT_table[52][41]=-1; IDFT_table[52][42]=-1; IDFT_table[52][48]=-1;
        IDFT_table[53][0]=1; IDFT_table[53][1]=-1; IDFT_table[53][4]=-1; IDFT_table[53][8]=-2;
        IDFT_table[53][9]=3; IDFT_table[53][11]=-3; IDFT_table[53][12]=2; IDFT_table[53][14]=-1;
        IDFT_table[53][16]=1; IDFT_table[53][17]=-4; IDFT_table[53][19]=2; IDFT_table[53][20]=-2;
        IDFT_table[53][22]=1; IDFT_table[53][27]=-1; IDFT_table[53][35]=-1; IDFT_table[53][40]=1;
        IDFT_table[53][41]=-2; IDFT_table[53][43]=1; IDFT_table[53][44]=-1; IDFT_table[53][48]=-1;
        IDFT_table[53][49]=1; IDFT_table[53][51]=-1; IDFT_table[54][0]=1; IDFT_table[54][1]=-2;
        IDFT_table[54][2]=1; IDFT_table[54][5]=1; IDFT_table[54][6]=-1; IDFT_table[54][8]=-2;
        IDFT_table[54][9]=4; IDFT_table[54][10]=-5; IDFT_table[54][11]=1; IDFT_table[54][12]=1;
        IDFT_table[54][13]=-3; IDFT_table[54][14]=1; IDFT_table[54][16]=1; IDFT_table[54][17]=-5;
        IDFT_table[54][18]=4; IDFT_table[54][19]=-2; IDFT_table[54][20]=-1; IDFT_table[54][21]=2;
        IDFT_table[54][22]=-2; IDFT_table[54][25]=1; IDFT_table[54][26]=-2; IDFT_table[54][29]=-1;
        IDFT_table[54][33]=1; IDFT_table[54][34]=-1; IDFT_table[54][37]=-1; IDFT_table[54][40]=1;
        IDFT_table[54][41]=-3; IDFT_table[54][42]=2; IDFT_table[54][43]=-1; IDFT_table[54][44]=-1;
        IDFT_table[54][45]=1; IDFT_table[54][46]=-1; IDFT_table[54][48]=-1; IDFT_table[54][49]=1;
        IDFT_table[54][50]=-2; IDFT_table[54][53]=-1; IDFT_table[55][0]=1; IDFT_table[55][2]=1;
        IDFT_table[55][3]=-2; IDFT_table[55][4]=1; IDFT_table[55][5]=-1; IDFT_table[55][8]=-2;
        IDFT_table[55][9]=1; IDFT_table[55][10]=-5; IDFT_table[55][11]=4; IDFT_table[55][12]=-4;
        IDFT_table[55][13]=2; IDFT_table[55][14]=-1; IDFT_table[55][16]=1; IDFT_table[55][17]=-2;
        IDFT_table[55][18]=4; IDFT_table[55][19]=-4; IDFT_table[55][20]=3; IDFT_table[55][21]=-3;
        IDFT_table[55][26]=-2; IDFT_table[55][27]=1; IDFT_table[55][28]=-1; IDFT_table[55][34]=-1;
        IDFT_table[55][35]=1; IDFT_table[55][36]=-1; IDFT_table[55][40]=1; IDFT_table[55][41]=-1;
        IDFT_table[55][42]=2; IDFT_table[55][43]=-2; IDFT_table[55][44]=1; IDFT_table[55][45]=-1;
        IDFT_table[55][48]=-1; IDFT_table[55][50]=-2; IDFT_table[55][51]=1; IDFT_table[55][52]=-1;
        IDFT_table[55][53]=1; IDFT_table[56][0]=1; IDFT_table[56][8]=-2; IDFT_table[56][9]=-2;
        IDFT_table[56][10]=-2; IDFT_table[56][11]=-2; IDFT_table[56][12]=-2; IDFT_table[56][13]=-1;
        IDFT_table[56][14]=-1; IDFT_table[56][16]=3; IDFT_table[56][17]=3; IDFT_table[56][18]=3;
        IDFT_table[56][19]=3; IDFT_table[56][20]=2; IDFT_table[56][21]=2; IDFT_table[56][22]=1;
        IDFT_table[56][24]=-4; IDFT_table[56][25]=-4; IDFT_table[56][26]=-4; IDFT_table[56][27]=-4;
        IDFT_table[56][28]=-3; IDFT_table[56][29]=-2; IDFT_table[56][30]=-1; IDFT_table[56][32]=3;
        IDFT_table[56][33]=3; IDFT_table[56][34]=3; IDFT_table[56][35]=2; IDFT_table[56][36]=2;
        IDFT_table[56][37]=1; IDFT_table[56][38]=1; IDFT_table[56][40]=-2; IDFT_table[56][41]=-2;
        IDFT_table[56][42]=-2; IDFT_table[56][43]=-2; IDFT_table[56][44]=-1; IDFT_table[56][45]=-1;
        IDFT_table[56][46]=-1; IDFT_table[57][0]=1; IDFT_table[57][8]=-2; IDFT_table[57][9]=-2;
        IDFT_table[57][10]=-1; IDFT_table[57][16]=3; IDFT_table[57][17]=2; IDFT_table[57][18]=1;
        IDFT_table[57][21]=-1; IDFT_table[57][22]=-1; IDFT_table[57][24]=-4; IDFT_table[57][25]=-4;
        IDFT_table[57][26]=-2; IDFT_table[57][30]=1; IDFT_table[57][32]=3; IDFT_table[57][33]=2;
        IDFT_table[57][34]=1; IDFT_table[57][37]=-1; IDFT_table[57][38]=-1; IDFT_table[57][40]=-2;
        IDFT_table[57][41]=-2; IDFT_table[57][42]=-1; IDFT_table[58][0]=1; IDFT_table[58][8]=-2;
        IDFT_table[58][9]=-1; IDFT_table[58][11]=1; IDFT_table[58][12]=1; IDFT_table[58][16]=3;
        IDFT_table[58][18]=-1; IDFT_table[58][19]=-3; IDFT_table[58][20]=-3; IDFT_table[58][21]=-1;
        IDFT_table[58][24]=-4; IDFT_table[58][25]=-2; IDFT_table[58][26]=1; IDFT_table[58][27]=3;
        IDFT_table[58][28]=2; IDFT_table[58][32]=3; IDFT_table[58][33]=1; IDFT_table[58][34]=-1;
        IDFT_table[58][35]=-3; IDFT_table[58][36]=-3; IDFT_table[58][37]=-1; IDFT_table[58][40]=-2;
        IDFT_table[58][41]=-1; IDFT_table[58][43]=1; IDFT_table[58][44]=1; IDFT_table[59][0]=1;
        IDFT_table[59][2]=-1; IDFT_table[59][8]=-2; IDFT_table[59][10]=2; IDFT_table[59][12]=-1;
        IDFT_table[59][13]=-2; IDFT_table[59][16]=3; IDFT_table[59][17]=-1; IDFT_table[59][18]=-5;
        IDFT_table[59][19]=-2; IDFT_table[59][20]=1; IDFT_table[59][21]=2; IDFT_table[59][24]=-4;
        IDFT_table[59][26]=3; IDFT_table[59][27]=2; IDFT_table[59][28]=-2; IDFT_table[59][29]=-2;
        IDFT_table[59][32]=3; IDFT_table[59][33]=-1; IDFT_table[59][34]=-3; IDFT_table[59][35]=-2;
        IDFT_table[59][36]=1; IDFT_table[59][37]=1; IDFT_table[59][40]=-2; IDFT_table[59][42]=2;
        IDFT_table[59][44]=-1; IDFT_table[59][45]=-1; IDFT_table[59][50]=-1; IDFT_table[60][0]=1;
        IDFT_table[60][8]=-2; IDFT_table[60][9]=1; IDFT_table[60][10]=1; IDFT_table[60][11]=-2;
        IDFT_table[60][12]=-2; IDFT_table[60][13]=1; IDFT_table[60][16]=3; IDFT_table[60][17]=-4;
        IDFT_table[60][18]=-4; IDFT_table[60][19]=3; IDFT_table[60][20]=2; IDFT_table[60][21]=-2;
        IDFT_table[60][22]=-1; IDFT_table[60][24]=-4; IDFT_table[60][25]=4; IDFT_table[60][26]=3;
        IDFT_table[60][27]=-4; IDFT_table[60][28]=-3; IDFT_table[60][29]=2; IDFT_table[60][30]=1;
        IDFT_table[60][32]=3; IDFT_table[60][33]=-4; IDFT_table[60][34]=-3; IDFT_table[60][35]=2;
        IDFT_table[60][36]=2; IDFT_table[60][37]=-2; IDFT_table[60][38]=-1; IDFT_table[60][40]=-2;
        IDFT_table[60][41]=2; IDFT_table[60][42]=2; IDFT_table[60][43]=-2; IDFT_table[60][44]=-1;
        IDFT_table[60][45]=1; IDFT_table[60][49]=-1; IDFT_table[60][50]=-1; IDFT_table[61][0]=1;
        IDFT_table[61][1]=-1; IDFT_table[61][4]=-1; IDFT_table[61][8]=-2; IDFT_table[61][9]=4;
        IDFT_table[61][10]=-1; IDFT_table[61][11]=-4; IDFT_table[61][12]=2; IDFT_table[61][14]=-2;
        IDFT_table[61][16]=3; IDFT_table[61][17]=-7; IDFT_table[61][19]=4; IDFT_table[61][20]=-4;
        IDFT_table[61][21]=-1; IDFT_table[61][22]=2; IDFT_table[61][24]=-4; IDFT_table[61][25]=6;
        IDFT_table[61][26]=-1; IDFT_table[61][27]=-5; IDFT_table[61][28]=4; IDFT_table[61][30]=-2;
        IDFT_table[61][32]=3; IDFT_table[61][33]=-6; IDFT_table[61][35]=4; IDFT_table[61][36]=-4;
        IDFT_table[61][38]=2; IDFT_table[61][40]=-2; IDFT_table[61][41]=3; IDFT_table[61][42]=-1;
        IDFT_table[61][43]=-3; IDFT_table[61][44]=2; IDFT_table[61][46]=-1; IDFT_table[61][49]=-2;
        IDFT_table[61][51]=1; IDFT_table[61][52]=-1; IDFT_table[62][0]=1; IDFT_table[62][1]=-2;
        IDFT_table[62][2]=1; IDFT_table[62][5]=1; IDFT_table[62][6]=-1; IDFT_table[62][8]=-2;
        IDFT_table[62][9]=5; IDFT_table[62][10]=-6; IDFT_table[62][11]=1; IDFT_table[62][12]=1;
        IDFT_table[62][13]=-3; IDFT_table[62][14]=2; IDFT_table[62][16]=3; IDFT_table[62][17]=-9;
        IDFT_table[62][18]=8; IDFT_table[62][19]=-3; IDFT_table[62][20]=-3; IDFT_table[62][21]=5;
        IDFT_table[62][22]=-3; IDFT_table[62][24]=-4; IDFT_table[62][25]=9; IDFT_table[62][26]=-9;
        IDFT_table[62][27]=3; IDFT_table[62][28]=2; IDFT_table[62][29]=-6; IDFT_table[62][30]=3;
        IDFT_table[62][32]=3; IDFT_table[62][33]=-9; IDFT_table[62][34]=7; IDFT_table[62][35]=-3;
        IDFT_table[62][36]=-3; IDFT_table[62][37]=4; IDFT_table[62][38]=-3; IDFT_table[62][40]=-2;
        IDFT_table[62][41]=5; IDFT_table[62][42]=-5; IDFT_table[62][43]=1; IDFT_table[62][44]=1;
        IDFT_table[62][45]=-3; IDFT_table[62][46]=2; IDFT_table[62][49]=-2; IDFT_table[62][50]=2;
        IDFT_table[62][53]=1; IDFT_table[62][54]=-1; IDFT_table[63][0]=1; IDFT_table[63][2]=1;
        IDFT_table[63][3]=-2; IDFT_table[63][4]=1; IDFT_table[63][5]=-1; IDFT_table[63][8]=-2;
        IDFT_table[63][9]=1; IDFT_table[63][10]=-6; IDFT_table[63][11]=4; IDFT_table[63][12]=-4;
        IDFT_table[63][13]=3; IDFT_table[63][14]=-1; IDFT_table[63][16]=3; IDFT_table[63][17]=-4;
        IDFT_table[63][18]=8; IDFT_table[63][19]=-8; IDFT_table[63][20]=6; IDFT_table[63][21]=-5;
        IDFT_table[63][22]=1; IDFT_table[63][24]=-4; IDFT_table[63][25]=4; IDFT_table[63][26]=-9;
        IDFT_table[63][27]=8; IDFT_table[63][28]=-7; IDFT_table[63][29]=5; IDFT_table[63][30]=-1;
        IDFT_table[63][32]=3; IDFT_table[63][33]=-4; IDFT_table[63][34]=7; IDFT_table[63][35]=-7;
        IDFT_table[63][36]=6; IDFT_table[63][37]=-5; IDFT_table[63][38]=1; IDFT_table[63][40]=-2;
        IDFT_table[63][41]=2; IDFT_table[63][42]=-5; IDFT_table[63][43]=4; IDFT_table[63][44]=-4;
        IDFT_table[63][45]=3; IDFT_table[63][46]=-1; IDFT_table[63][49]=-1; IDFT_table[63][50]=2;
        IDFT_table[63][51]=-2; IDFT_table[63][52]=1; IDFT_table[63][53]=-1;
    }



}
    

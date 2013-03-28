package com.evilinc.mpeg.video;

import com.evilinc.mpeg.*;

public interface IVideo
{
    public static final int SEQ_END_CODE         = 0x1B7;
    public static final int SEQ_START_CODE       = 0x1B3;
    public static final int GOP_START_CODE       = 0x1B8;
    public static final int PICTURE_START_CODE   = 0x100;
    public static final int SLICE_MIN_START_CODE = 0x101;
    public static final int SLICE_MAX_START_CODE = 0x1AF;
    public static final int EXT_START_CODE       = 0x1B5;
    public static final int USER_START_CODE      = 0x1B2;
    public static final int I_TYPE               = 1;
    public static final int P_TYPE               = 2;
    public static final int B_TYPE               = 3;
    public static final int CODED_BLOCK_PATTERN_DEFAULT = 0x3F;
    public static final int DCT_DEFAULT_VALUES = 1024;

    public void setRunMode(boolean state);
    public void decodeVideoPacket(int pktLength, long pts)
        throws MpegVideoDecodeException;
}

package org.braveopendwarves.data;
/** Nominal default frame cap recovered at EXE 4238da/49ef50.
 * Original integer-millisecond pacing and optional uncapped mode vary in practice. */
public final class OriginalClock {
    private OriginalClock(){}
    public static final float HZ=75;
    public static float seconds(float ticks){return ticks/HZ;}
    public static float speed(float pixelsPerTick){return pixelsPerTick*HZ;}
}


package org.minexew.nanotracker;

import javax.microedition.lcdui.Graphics;

public interface MenuOverlay
{
    static final short load = 0;
    static final short saveTo = 1;
    static final short newChannel = 3;
    static final short channelMenu = 4;
    static final short trackProperties = 5;
    static final short trackTempo = 6;
    static final short trackTitle = 7;
    static final short transposeUp = 8;
    static final short transposeDown = 9;
    static final short repeat = 10;
    static final short changeInstrument = 11;
    static final short duplicate = 12;
    static final short truncate = 13;

    public int getResult();
    public int getType();
    public boolean isConfirmed();
    public void keyPressed( int keyCode, int gameAction );
    public void render( Graphics g, int width, int height );
}

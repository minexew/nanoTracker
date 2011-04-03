
package org.minexew.nanotracker;

import javax.microedition.lcdui.Graphics;

public class SlotSelectionDlg extends SelectionDlg
{
    String slots[][];

    SlotSelectionDlg( String title, short type )
    {
        super( title, type, false );

        try
        {
            slots = DataStorage.getTrackList();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }

        numItems = slots.length;
    }

    public void render( Graphics g, int width, int height )
    {
        super.render( g, width, height );

        int y = getY0( height ) + font.getHeight();

        for ( int i = 0; i < slots.length; i++ )
        {
            font.drawString( g, 8, y, slots[i][0], i == selection ? 0 : 1, Font.left, Font.top );
            y += font.getHeight() * 11 / 8;

            font.drawString( g, 8, y, slots[i][1], 1, Font.left, Font.top );
            y += font.getHeight() * 11 / 8;

            y += font.getHeight() / 2;
        }
    }
}

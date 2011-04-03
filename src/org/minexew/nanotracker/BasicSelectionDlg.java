
package org.minexew.nanotracker;

import javax.microedition.lcdui.Graphics;

public class BasicSelectionDlg extends SelectionDlg
{
    String labels[];
    int values[];

    BasicSelectionDlg( String title, short type, String labels[], int values[] )
    {
        super( title, type, true );

        this.labels = labels;
        this.values = values;

        if ( values != null )
            numItems = Math.min( labels.length, values.length );
        else
            numItems = labels.length;
    }

    public int getResult()
    {
        return ( selection < 0 || values == null ) ? selection : values[selection];
    }

    public void render( Graphics g, int width, int height )
    {
        super.render( g, width, height );

        int y = getY0( height ) + font.getHeight();

        for ( int i = 0; i < labels.length; i++ )
        {
            font.drawString( g, 8, y, labels[i], i == selection ? 0 : 1, Font.left, Font.top );
            y += font.getHeight() * 11 / 8;
        }
    }
}

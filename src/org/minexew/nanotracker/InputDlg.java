
package org.minexew.nanotracker;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

public class InputDlg implements MenuOverlay
{
    String title;
    short type;
    Font font;

    int value, def, dialogHeight;

    boolean confirmed = false;

    InputDlg( String title, short type, int def )
    {
        this.title = title;
        this.type = type;
        this.value = this.def = def;

        try
        {
            font = NtMidlet.getInstance().getFont();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }

        dialogHeight = font.getHeight() * 7;
    }

    public int getResult()
    {
        return value;
    }

    public int getType()
    {
        return type;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void keyPressed( int keyCode, int gameAction )
    {
        // confirm
        if ( keyCode == -5 )
        {
            confirmed = true;
            return;
        }
        // cancel
        else if ( keyCode == -7 )
        {
            value = def;
            confirmed = true;
            return;
        }
        // delete
        else if ( keyCode == -8 )
            value /= 10;
        // input
        else if ( keyCode >= Canvas.KEY_NUM0 && keyCode <= Canvas.KEY_NUM9 )
            value = value * 10 + keyCode - Canvas.KEY_NUM0;
    }

    public void render( Graphics g, int width, int height )
    {
        final int y0 = height - dialogHeight;

        g.setColor( 0x000000 );
        g.fillRect( 0, y0, width, dialogHeight );

        font.drawString( g, 4, y0 + font.getHeight() / 2, title, 1, Font.left, Font.top );
        font.drawString( g, 16, y0 + font.getHeight() * 3, value + "_", 0, Font.left, Font.top );

        font.drawString( g, width / 2, height - font.getHeight(), "ok", 1, Font.center, Font.bottom );
        font.drawString( g, width - 4, height - font.getHeight(), "cancel", 1, Font.right, Font.bottom );
    }
}


package org.minexew.nanotracker;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

public class SelectionDlg implements MenuOverlay
{
    String title;
    short type;
    Font font;

    int numItems, selection = 0;

    boolean confirmed = false, itemHeightConst;

    SelectionDlg( String title, short type, boolean itemHeightConst )
    {
        this.title = title;
        this.type = type;
        this.itemHeightConst = itemHeightConst;

        try
        {
            font = NtMidlet.getInstance().getFont();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }

    public int getResult()
    {
        return selection;
    }

    public int getType()
    {
        return type;
    }

    int getY0( int height )
    {
        if ( itemHeightConst )
            return height - numItems * font.getHeight() * 11 / 8 - 4 * font.getHeight();
        else
            return 0;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void keyPressed( int keyCode, int gameAction )
    {
        if ( keyCode == -7 )
        {
            selection = -1;
            confirmed = true;
            return;
        }

        switch ( gameAction )
        {
            case Canvas.FIRE:
                confirmed = true;
                break;

            case Canvas.UP:
                selection--;

                while ( selection < 0 )
                    selection += numItems;
                break;

            case Canvas.DOWN:
                selection++;

                while ( selection >= numItems )
                    selection -= numItems;
                break;
        }
    }

    public void render( Graphics g, int width, int height )
    {
        int y = getY0( height );

        g.setColor( 0x000000 );
        g.fillRect( 0, y, width, height - y );

        if ( title != null )
        {
            g.fillRect( 0, y - font.getHeight() * 2, width, font.getHeight() * 2 );
            font.drawString( g, 4, y - font.getHeight() * 3 / 2, title, 1, Font.left, Font.top );
        }

        font.drawString( g, width / 2, height - font.getHeight(), "ok", 1, Font.center, Font.bottom );
        font.drawString( g, width - 4, height - font.getHeight(), "cancel", 1, Font.right, Font.bottom );
    }
}

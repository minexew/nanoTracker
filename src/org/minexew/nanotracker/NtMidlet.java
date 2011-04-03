
package org.minexew.nanotracker;

import java.io.IOException;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class NtMidlet extends MIDlet
{
    private static NtMidlet instance;

    MenuCanvas menu;
    EditorCanvas editor;

    Font font;

    static NtMidlet getInstance()
    {
        return instance;
    }

    Font getFont() throws IOException
    {
        if ( font == null )
            font = new Font( new String[] { "font.png", "font_grey.png" } );

        return font;
    }

    void showEditor( int channel )
    {
        if ( editor == null )
            editor = new EditorCanvas();

        editor.edit( Track.getInstance().getChannel( channel ) );
        Display.getDisplay( this ).setCurrent( editor );
    }

    void showMenu()
    {
        if ( menu == null )
            menu = new MenuCanvas();

        Display.getDisplay( this ).setCurrent( menu );
    }

    public void startApp()
    {
        instance = this;

        try
        {
            Display.getDisplay( this ).setCurrent( new SplashScreen() );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            destroyApp( true );
        }
    }

    public void pauseApp()
    {
    }

    public void destroyApp( boolean unconditional )
    {
        notifyDestroyed();
    }
}

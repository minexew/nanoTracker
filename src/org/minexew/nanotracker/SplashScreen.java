
package org.minexew.nanotracker;

import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.*;

public class SplashScreen extends Canvas
{
    static final int fps = 50;
    float progress = 0.0f;

    Image splash;

    Timer timer;
    Task task;

    SplashScreen()
    {
        setFullScreenMode( true );

        try
        {
            splash = Image.createImage( "/title.png" );
        }
        catch ( java.io.IOException ex )
        {
            ex.printStackTrace();
        }
    }

    protected void hideNotify()
    {
        if ( timer != null )
        {
            timer.cancel();
            timer = null;
            task = null;
        }
    }

    protected void keyPressed( int keyCode )
    {
        NtMidlet.getInstance().showMenu();
    }

    protected void paint( Graphics g )
    {
        int width = getWidth(), height = getHeight();

        g.setColor( 0xFFFFFF );
        g.fillRect( 0, 0, width, height );
        g.drawImage( splash, width / 2, height / 2, Graphics.HCENTER | Graphics.VCENTER );

        g.setColor( 0x000000 );

        g.fillRect( 0, 0, width, ( int )( height * progress ) / 2 );
        g.fillRect( 0, height - ( int )( height * progress ) / 2, width, ( int )( height * progress ) / 2 );
    }

    protected void showNotify()
    {
        task = new Task();
        timer = new Timer();
        timer.schedule( task, 0L, 1000L / fps );
    }

    class Task extends TimerTask
    {
        int stepsElapsed = 0;

        public void run()
        {
            progress += 1.0f / fps;

            if ( progress >= 1.0f )
                NtMidlet.getInstance().showMenu();

            repaint();
            serviceRepaints();
        }
    }
}
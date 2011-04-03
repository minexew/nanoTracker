
package org.minexew.nanotracker;

import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.*;

public class EditorCanvas extends Canvas
{
    static int fps = 50;

    Font font;

    Track.Channel channel;

    Timer timer;
    ScrollTask task;

    int cursorX, cursorY, cursorWidth, lastKey = -1, linesVisible, scrollDir = 0;

    EditorCanvas()
    {
        setFullScreenMode( true );

        try
        {
            font = NtMidlet.getInstance().getFont();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }

        cursorWidth = font.getStringWidth( "> " );
        linesVisible = ( getHeight() - 4 ) / font.getHeight();
    }

    void beginScroll( int direction )
    {
        scrollDir = direction;

        task = new ScrollTask();
        timer = new Timer();
        timer.schedule( task, 100L, 1000L / fps );

        scroll();
    }

    void cancelScroll()
    {
        if ( timer != null )
        {
            timer.cancel();
            timer = null;
            task = null;
        }

        scrollDir = 0;
    }

    void edit( Track.Channel channel )
    {
        this.channel = channel;
        cursorX = 0;
        cursorY = 0;
    }

    public String getNoteName( int midi )
    {
        final String noteNames[] = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"  };

        if ( midi == 0 )
            return "--";

        midi -= 12;

        if ( midi < 0 )
            return "#?";

        return noteNames[midi % 12] + midi / 12;
    }

    protected void hideNotify()
    {
        cancelScroll();
    }

    protected void keyPressed( int keyCode )
    {
        // play channel
        if ( keyCode == -6 )
        {
            if ( Player.isPlaying() )
                Player.stop();
            else
                Player.previewChannel( channel );
        }
        // exit
        else if ( keyCode == -7 )
            NtMidlet.getInstance().showMenu();
        // edit
        else if ( keyCode == -5 )
        {
            if ( cursorY >= channel.notes.size() )
                channel.addNote();
            else
            {
                Track.Channel.Note note = channel.getNote( cursorY );

                note.setProperty( cursorX, 0 );
            }
        }
        // up
        else if ( keyCode == -1 && cursorY > 0 )
            beginScroll( -1 );
        // down
        else if ( keyCode == -2 && cursorY < channel.notes.size() )
            beginScroll( 1 );
        // left
        else if ( keyCode == -3 && cursorX > 0 )
            cursorX--;
        // right
        else if ( keyCode == -4 && cursorX < 2 )
            cursorX++;
        // remove
        else if ( keyCode == -8 && cursorY < channel.notes.size() )
            channel.notes.removeElementAt( cursorY );
        // play
        else if ( keyCode == KEY_STAR && cursorY < channel.notes.size() )
        {
            //if ( !Player.isPlaying() )
            //    Player.play( channel.getNote( cursorY ) );
        }
        else if ( keyCode == KEY_POUND && cursorY < channel.notes.size() )
            channel.insertNote( cursorY );
        else if ( keyCode >= 48 && keyCode <= 57 )
        {
            Track.Channel.Note note = channel.getNote( cursorY );

            note.setProperty( cursorX, note.getProperty( cursorX ) * 10 + keyCode - KEY_NUM0 );
        }

        lastKey = keyCode;

        repaint();
        serviceRepaints();
    }

    protected void keyReleased( int keyCode )
    {
        cancelScroll();
    }

    protected void paint( Graphics g )
    {
        int width = getWidth(), height = getHeight();

        g.setColor( 0x000000 );
        g.fillRect( 0, 0, width, height );

        int y = 4;

        int i = Math.max( cursorY - linesVisible / 2, 0 );

        if ( i > 0 )
        {
            font.drawString( g, 4, y, "...", 1, Font.left, Font.top );

            y += font.getHeight();
            i++;
        }

        for ( ; i < channel.notes.size() + 1; i++ )
        {
            if ( y > height )
                break;

            if ( i >= channel.notes.size() )
            {
                if ( i == cursorY )
                    font.drawString( g, 16, y, "#INSERT", 0, Font.left, Font.top );

                break;
            }

            Track.Channel.Note note = channel.getNote( i );

            String line = "[" + Integer.toHexString( i ).toUpperCase() + "] ";

            if ( cursorX == 0 && cursorY == i )
            {
                if ( note.midi != 0 )
                    line += note.midi + "_ " + getNoteName( note.midi );
                else
                    line += "_";
            }
            else
                line += note.midi + " " + getNoteName( note.midi );

            font.drawString( g, 4, y, line, ( cursorX == 0 && cursorY == i ) ? 0 : 1, Font.left, Font.top );
            font.drawString( g, width / 2, y, note.length + "/16", ( cursorX == 1 && cursorY == i ) ? 0 : 1, Font.left, Font.top );
            font.drawString( g, width - 16, y, note.velocity + " %", ( cursorX == 2 && cursorY == i ) ? 0 : 1, Font.right, Font.top );

            y += font.getHeight();
        }
    }

    void scroll()
    {
        if ( scrollDir < 0 && cursorY > 0 )
            cursorY--;
        else if ( scrollDir > 0 && cursorY < channel.notes.size() )
            cursorY++;
    }

    class ScrollTask extends TimerTask
    {
        int ticks = 0, requiredTicks = ( int )( 0.2f * fps );

        public void run()
        {
            if ( ++ticks == requiredTicks )
            {
                scroll();
                repaint();
                serviceRepaints();

                ticks = 0;

                if ( requiredTicks > 1 )
                    requiredTicks--;
            }
        }
    }
}

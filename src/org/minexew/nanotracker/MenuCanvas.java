
package org.minexew.nanotracker;

import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.*;

public class MenuCanvas extends Canvas
{
    // constants
    static final int fps = 30;

    // overlay
    MenuOverlay overlay;

    // resources
    Image logo;
    Font font;

    // timing
    Timer timer;
    Task task;

    // sides + animation
    Menu menus[];
    ChannelMenu channelMenu;

    final int sideWidth = 160;
    int frontSide = 0, transitionDir = 0, viewX = 0;
    float transitionProgress = 0;
    float animPhase = 0;

    MenuCanvas()
    {
        setFullScreenMode( true );

        try
        {
            logo = Image.createImage( "/logo.png" );
            font = NtMidlet.getInstance().getFont();

            menus = new Menu[3];
            menus[0] = new FileMenu();
            menus[1] = new TrackMenu();
            menus[2] = channelMenu = new ChannelMenu();
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

    void instrumentMenu( short type )
    {
        overlay = new BasicSelectionDlg( "Select Instrument:", type,
                new String[] { "LEAD", "BASS", "PIANO", "ACOUSTIC GTR", "ELECTRIC GTR", "TROMBONE", "Polysynth", "DrumKit" },
                new int[] { Track.Channel.lead, Track.Channel.bass, Track.Channel.piano, Track.Channel.acoustic,
                        Track.Channel.electric, Track.Channel.trombone, 90, Track.Channel.drumKit } );
    }

    protected void keyPressed( int keyCode )
    {
        int gameAction = getGameAction( keyCode );

        if ( overlay != null )
        {
            boolean autoClose = true;
            overlay.keyPressed( keyCode, gameAction );

            if ( overlay.isConfirmed() )
            {
                int result = overlay.getResult();

                if ( overlay.getType() == MenuOverlay.load && result >= 0 )
                {
                    Track.slot = result;
                    Track.getInstance().load( result );
                    channelMenu.rebuild();
                }
                else if ( overlay.getType() == MenuOverlay.saveTo && result >= 0 )
                {
                    Track.slot = result;
                    Track.getInstance().save( result );
                }
                else if ( overlay.getType() == MenuOverlay.newChannel && result >= 0 )
                {
                    Track.getInstance().addChannel( result );
                    NtMidlet.getInstance().showEditor( Track.getInstance().channels.size() - 1 );
                    channelMenu.rebuild();
                }
                else if ( overlay.getType() == MenuOverlay.channelMenu && result >= 0 )
                {
                    Track.Channel channel = Track.getInstance().getChannel( channelMenu.selection );

                    if ( result == 0 )
                        NtMidlet.getInstance().showEditor( channelMenu.selection );
                    else if ( result == 1 )
                    {
                        channel.isMuted = !channel.isMuted;
                        channelMenu.rebuild();
                    }
                    else if ( result == 2 )
                    {
                        instrumentMenu( MenuOverlay.changeInstrument );
                        autoClose = false;
                    }
                    else if ( result == 3 )
                    {
                        overlay = new InputDlg( "Semi-tones up:", MenuOverlay.transposeUp, 0 );
                        autoClose = false;
                    }
                    else if ( result == 4 )
                    {
                        overlay = new InputDlg( "Semi-tones down:", MenuOverlay.transposeDown, 0 );
                        autoClose = false;
                    }
                    else if ( result == 5 )
                    {
                        overlay = new InputDlg( "Repeat last X notes:", MenuOverlay.repeat, 0 );
                        autoClose = false;
                    }
                    else if ( result == 6 )
                    {
                        overlay = new InputDlg( "Cut off last X notes:", MenuOverlay.truncate, 0 );
                        autoClose = false;
                    }
                    else if ( result == 7 )
                    {
                        instrumentMenu( MenuOverlay.duplicate );
                        autoClose = false;
                    }
                    else if ( result == 8 )
                    {
                        Track.getInstance().removeChannel( channelMenu.selection );
                        channelMenu.rebuild();
                    }
                }
                else if ( overlay.getType() == MenuOverlay.trackProperties && result >= 0 )
                {
                    if ( result == 0 )
                    {
                        overlay = new InputDlg( "Track Tempo:", MenuOverlay.trackTempo, Track.getInstance().bpm );
                        autoClose = false;
                    }
                }
                else if ( overlay.getType() == MenuOverlay.trackTempo && result > 0 )
                    Track.getInstance().bpm = result;
                else if ( overlay.getType() == MenuOverlay.transposeUp && result > 0 )
                    Track.getInstance().getChannel( channelMenu.selection ).transpose( result );
                else if ( overlay.getType() == MenuOverlay.transposeDown && result > 0 )
                    Track.getInstance().getChannel( channelMenu.selection ).transpose( -result );
                else if ( overlay.getType() == MenuOverlay.repeat && result > 0 )
                {
                    Track.getInstance().getChannel( channelMenu.selection ).repeatTail( result );
                    channelMenu.rebuild();
                }
                else if ( overlay.getType() == MenuOverlay.changeInstrument && result >= 0 )
                {
                    Track.getInstance().getChannel( channelMenu.selection ).instrument = result;
                    channelMenu.rebuild();
                }
                else if ( overlay.getType() == MenuOverlay.duplicate && result >= 0 )
                {
                    Track.getInstance().cloneChannel( channelMenu.selection, result );
                    channelMenu.rebuild();
                }
                else if ( overlay.getType() == MenuOverlay.truncate && result > 0 )
                {
                    Track.getInstance().getChannel( channelMenu.selection ).removeTail( result );
                    channelMenu.rebuild();
                }

                if ( autoClose )
                    overlay = null;
            }
        }
        else
            switch ( gameAction )
            {
                case LEFT:
                    transitionDir = -1;
                    transitionProgress = 0f;
                    break;

                case RIGHT:
                    transitionDir = 1;
                    transitionProgress = 0f;
                    break;

                default:
                    if ( transitionDir == 0 )
                        menus[frontSide].keyPressed( keyCode, gameAction );
            }
    }

    protected void paint( Graphics g )
    {
        int width = getWidth(), height = getHeight();

        g.setColor( 0x000000 );
        g.fillRect( 0, logo.getHeight(), width, height - logo.getHeight() );
        g.drawImage( logo, width / 2, 0, Graphics.HCENTER | Graphics.TOP );

        final float step = ( float )( Math.PI * 2f / width );
        final float y0 = height - 40f;

        g.setColor( 0x404040 );

        for ( int x = 0; x < width; x += 10 )
            g.fillRect( x + 3, ( int )( y0 + Math.sin( ( Math.PI * 2 - animPhase ) * 2 + step * x ) * 20f ) + 3, 4, 4 );

        g.setColor( 0x808080 );

        for ( int x = 0; x < width; x += 10 )
        {
            int a = ( int )Math.abs( Math.sin( animPhase + step * x ) * 8 );
            g.fillRect( x + 5 - a / 2, ( int )( y0 + Math.sin( Math.PI + animPhase + step * x ) * 10f ) + 5 - a / 2, a, a );
        }

        g.setColor( 0xFFFFFF );

        for ( int x = 0; x < width; x += 10 )
            g.fillRect( x + 1, ( int )( y0 + Math.sin( animPhase + step * x ) * 10f ) + 1, 8, 8 );

        final int menuY0 = logo.getHeight() + font.getHeight() / 2;

        //int num0 = 0, num1 = 0, num2 = 0;

        for ( int i = 0; i < menus.length * 2; i++ )
        {
            int x = ( width - sideWidth ) / 2 - viewX + i * sideWidth;

            if ( x >= width )
                break;

            if ( x > -width )
            {
                /*if ( i < menus.length )
                    num0++;
                else
                    num1++;*/
                menus[i % menus.length].render( g, x, menuY0, width, height, frontSide == i );
            }
        }

        for ( int i = menus.length - 1; i >= 0; i-- )
        {
            int x = ( width - sideWidth ) / 2 - viewX + i * sideWidth - menus.length * sideWidth;

            if ( x < -width )
                break;

            //num2++;
            menus[i].render( g, x, menuY0, width, height, frontSide == i );
        }

        //font.drawString( g, 0, 0, "xV:" + viewX + "/front=" + frontSide + "/r" + num0 + ":" + num1 + ":" + num2, 0, Font.left, Font.top );

        if ( overlay != null )
        {
            g.setClip( 16, 16, width - 32, height - 32 );
            g.translate( 16, 16 );
            overlay.render( g, width - 32, height - 32 );
        }
    }

    private void saveTo()
    {
        overlay = new SlotSelectionDlg( "Save to slot:", MenuOverlay.saveTo );
    }

    protected void showNotify()
    {
        task = new Task();
        timer = new Timer();
        timer.schedule( task, 0L, 1000L / fps );

        channelMenu.rebuild();
    }

    class Task extends TimerTask
    {
        public void run()
        {
            animPhase += Math.PI / fps;

            if ( animPhase > Math.PI * 2 )
                animPhase -= Math.PI * 2;

            if ( transitionDir != 0 )
            {
                transitionProgress += 4.0 / fps;

                if ( transitionProgress >= 1.0f )
                {
                    frontSide += transitionDir;
                    transitionDir = 0;

                    if ( frontSide < 0 )
                        frontSide += menus.length;
                    else if ( frontSide >= menus.length )
                        frontSide -= menus.length;

                    viewX = frontSide * sideWidth;
                }
                else
                    viewX = ( int )( ( frontSide + transitionDir * transitionProgress ) * sideWidth );
            }

            repaint();
            serviceRepaints();
        }
    }

    interface Menu
    {
        public void keyPressed( int keyCode, int gameAction );
        public void render( Graphics g, final int x0, final int y0, final int width, final int height, boolean isFrontSide );
    }

    abstract class BasicMenu implements Menu
    {
        String title, items[];
        int selection = 0;

        BasicMenu( String title, String items[] )
        {
            this.title = title;
            this.items = items;
        }

        abstract void selected( int index );

        public void keyPressed( int keyCode, int gameAction )
        {
            if ( items == null )
                return;

            switch ( gameAction )
            {
                case FIRE:
                    selected( selection );
                    break;

                case UP:
                    selection--;

                    if ( selection < 0 )
                        selection += items.length;

                    while ( items[selection].length() == 0 )
                        selection--;
                    break;

                case DOWN:
                    selection++;

                    if ( selection >= items.length )
                        selection -= items.length;

                    while ( items[selection].length() == 0 )
                        selection++;
                    break;
            }
        }

        public void render( Graphics g, final int x0, final int y0, final int width, final int height, boolean isFrontSide )
        {
            font.drawString( g, x0, y0, title, 1, Font.left, Font.top );

            if ( items == null )
                return;

            int y = y0 + font.getHeight() * 2;

            for ( int i = 0; i < items.length; i++ )
            {
                if ( items[i].length() > 0 )
                {
                    boolean isSelected = ( isFrontSide && i == selection );

                    font.drawString( g, x0 + 8, y, items[i], isSelected ? 0 : 1, Font.left, Font.top );
                    y += font.getHeight() * 11 / 8;
                }
                else
                    y += font.getHeight();
            }
        }
    }

    class FileMenu extends BasicMenu
    {
        FileMenu()
        {
            super( "- File -", new String[] { "New", "Load...", "Save", "Save To...", "Save Midi (beta)", "Exit" } );
        }

        void selected( int index )
        {
            if ( items[index].toLowerCase().equals( "new" ) )
            {
                Track.clear();
                Track.slot = -1;
                channelMenu.rebuild();
            }
            else if ( items[index].toLowerCase().equals( "load..." ) )
                overlay = new SlotSelectionDlg( "Load Slot:", MenuOverlay.load );
            else if ( items[index].toLowerCase().equals( "save" ) )
            {
                if ( Track.slot >= 0 )
                    Track.getInstance().save( Track.slot );
                else
                    saveTo();
            }
            else if ( items[index].toLowerCase().equals( "save to..." ) )
                saveTo();
            else if ( items[index].toLowerCase().equals( "save midi (beta)" ) )
                Player.saveMidi( Track.getInstance(), "c:/music/" + Track.getInstance().name + ".mid" );
            else if( items[index].toLowerCase().equals( "exit" ) )
                NtMidlet.getInstance().destroyApp( true );
        }
    }

    class TrackMenu extends BasicMenu
    {
        TrackMenu()
        {
            super( "- Track -", new String[] { "Play", "", "Properties" } );
        }

        void selected( int index )
        {
            if ( items[index].toLowerCase().equals( "play" ) )
            {
                if ( Player.isPlaying() )
                    Player.stop();
                else
                    Player.previewTrack( Track.getInstance() );
            }
            else if ( items[index].toLowerCase().equals( "properties" ) )
            {
                overlay = new BasicSelectionDlg( "Track Properties", MenuOverlay.trackProperties,
                        new String[] { "Change Tempo", "Change Track Title" }, null );
            }
        }
    }

    class ChannelMenu extends BasicMenu
    {
        ChannelMenu()
        {
            super( "- Channels -", null );
        }

        public void rebuild()
        {
            Track track = Track.getInstance();

            items = new String [track.channels.size() + 1];

            for ( int i = 0; i < track.channels.size(); i++ )
            {
                Track.Channel channel = track.getChannel( i );
                int instrument = channel.instrument;

                if ( !channel.isMuted )
                    items[i] = "+ ";
                else
                    items[i] = "- ";

                if ( instrument == Track.Channel.acoustic )
                    items[i] += "ACOUST";
                else if ( instrument == Track.Channel.bass )
                    items[i] += "BASS";
                else if ( instrument == Track.Channel.electric )
                    items[i] += "ELECTR";
                else if ( instrument == Track.Channel.lead )
                    items[i] += "LEAD";
                else if ( instrument == Track.Channel.piano )
                    items[i] += "PIANO";
                else if ( instrument == Track.Channel.trombone )
                    items[i] += "TROMBN";
                else if ( instrument == Track.Channel.drumKit )
                    items[i] += "DRUM";
                else
                    items[i] += "MIDI:" + instrument;

                items[i] += " (" + track.getChannel( i ).notes.size() + ")";
            }

            items[track.channels.size()] = "Add New...";
            selection = 0;
        }

        void selected( int index )
        {
            if ( index >= Track.getInstance().channels.size() )
                instrumentMenu( MenuOverlay.newChannel );
            else
            {
                overlay = new BasicSelectionDlg( null, MenuOverlay.channelMenu,
                        new String[] { "Edit", "Toggle Mute", "Change Instrument", "Transpose Up", "Transpose Down",
                                "Repeat Tail", "Truncate", "Duplicate", "Delete" }, null );
            }
        }
    }
}
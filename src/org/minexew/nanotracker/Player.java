
package org.minexew.nanotracker;

import java.io.*;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.*;

public class Player
{
    static javax.microedition.media.Player player;

    static boolean isPlaying()
    {
        return player != null && player.getState() == javax.microedition.media.Player.STARTED;
    }

    static void writeMidiHeader( DataOutputStream dos, int numChannels ) throws IOException
    {
        dos.write( new byte[] { 'M', 'T', 'h', 'd' } );
        dos.writeInt( 6 );
        dos.writeShort( numChannels == 1 ? 0 : 1 );
        dos.writeShort( numChannels );
        dos.writeShort( 0x0010 );
    }

    static void writeVarLength( DataOutputStream dos, int value ) throws IOException
    {
        if ( value < 0x80 )
            dos.writeByte( value );
        else if ( value < 0x4000 )
        {
            dos.writeByte( 0x80 | ( value >> 7 ) );
            dos.writeByte( value & 0x7F );
        }
        else if ( value < 0x200000 )
        {
            dos.writeByte( 0x80 | ( value >> 14 ) );
            dos.writeByte( 0x80 | ( ( value >> 7 ) & 0x7F ) );
            dos.writeByte( value & 0x7F );
        }
        else
        {
            dos.writeByte( 0x80 | ( ( value >> 21 ) & 0x7F ) );
            dos.writeByte( 0x80 | ( ( value >> 14 ) & 0x7F ) );
            dos.writeByte( 0x80 | ( ( value >> 7 ) & 0x7F ) );
            dos.writeByte( value & 0x7F );
        }
    }

    static byte[] renderChannel( Track.Channel channel, int c ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( baos );

        int mpqn = 60000000 / Track.getInstance().bpm;

        if ( c == 0 )
        {
            // track tempo

            writeVarLength( dos, 0 );
            dos.writeByte( 0xFF );
            dos.writeByte( 0x51 );
            writeVarLength( dos, 3 );
            dos.writeByte( mpqn >> 16 );
            dos.writeShort( mpqn & 0xFFFF );
        }

        if ( channel.instrument == Track.Channel.drumKit )
            c = 0x09;
        else if ( c >= 0x09 )
            c++;

        if ( c != 0x09 )
        {
            // instrument

            writeVarLength( dos, 0 );
            dos.writeByte( 0xC0 | c );
            dos.writeByte( channel.instrument );
        }

        int delay = 0;

        for ( int i = 0; i < channel.notes.size(); i++ )
        {
            Track.Channel.Note note = channel.getNote( i );

            if ( note.midi > 0 )
            {
                writeVarLength( dos, delay );
                dos.writeByte( 0x90 | c );
                dos.writeByte( note.midi );
                dos.writeByte( note.velocity );

                writeVarLength( dos, note.length );
                dos.writeByte( 0x80 | c );
                dos.writeByte( note.midi );
                dos.writeByte( 0 );

                delay = 0;
            }
            else
                delay += note.length;
        }

        // end of track

        writeVarLength( dos, 0 );
        dos.writeByte( 0xFF );
        dos.writeByte( 0x2F );
        writeVarLength( dos, 0 );

        dos.flush();
        return baos.toByteArray();
    }

    static byte[] renderTrack( Track track )
    {
        try
        {
            stop();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream( baos );

            int numChannels = 0;

            for ( int i = 0; i < track.channels.size(); i++ )
                if ( !track.getChannel( i ).isMuted )
                    numChannels++;

            writeMidiHeader( dos, numChannels );

            int channelId = 0;

            for ( int i = 0; i < track.channels.size(); i++ )
            {
                Track.Channel channel = track.getChannel( i );

                if ( channel.isMuted )
                    continue;

                // track chunk
                dos.write( new byte[] { 'M', 'T', 'r', 'k' } );

                // build track data
                byte channelData[] = renderChannel( channel, channelId++ );
                dos.writeInt( channelData.length );
                dos.write( channelData );
            }

            dos.flush();

            return baos.toByteArray();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();

            return null;
        }
    }

    static void previewChannel( Track.Channel channel )
    {
        try
        {
            stop();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream( baos );

            writeMidiHeader( dos,  1 );

            // track chunk
            dos.write( new byte[] { 'M', 'T', 'r', 'k' } );

            // build track data
            byte channelData[] = renderChannel( channel, 0 );
            dos.writeInt( channelData.length );
            dos.write( channelData );
            dos.flush();

            ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );

            player = Manager.createPlayer( bais, "audio/midi" );
            player.start();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }

    static void previewTrack( Track track )
    {
        try
        {
            ByteArrayInputStream bais = new ByteArrayInputStream( renderTrack( track ) );

            player = Manager.createPlayer( bais, "audio/midi" );
            player.start();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
    
    static void saveMidi( Track track, String fileName )
    {
        try
        {
            FileConnection file = ( FileConnection )Connector.open( "file:///" + fileName );

            if ( !file.exists() )
                 file.create();

            OutputStream os = file.openOutputStream();
            os.write( renderTrack( track ) );
            os.close();

            file.close();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }

    static void stop()
    {
        try
        {
            if ( player != null )
            {
                player.stop();
                player.deallocate();
                player.close();
                player = null;
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}

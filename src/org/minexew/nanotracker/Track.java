
package org.minexew.nanotracker;

import java.io.*;
import java.util.Vector;

public class Track
{
    class Channel
    {
        class Note
        {
            public int midi, length, velocity;

            Note( int midi, int length, int velocity )
            {
                this.midi = midi;
                lastLength = this.length = length;
                lastVelocity = this.velocity = velocity;
            }

            int getDuration()
            {
                return length * 60000 / bpm / 16;
            }

            int getProperty( int index )
            {
                switch ( index )
                {
                    case 0: return midi;
                    case 1: return length;
                    case 2: return velocity;
                    default: return -1;
                }
            }

            void setProperty( int index, int value )
            {
                switch ( index )
                {
                    case 0: midi = value; break;
                    case 1: lastLength = length = value; break;
                    case 2: lastVelocity = velocity = value; break;
                }
            }
        }

        public static final int piano = 0;
        public static final int acoustic = 25;
        public static final int electric = 27;
        public static final int trombone = 57;
        public static final int lead = 80;
        public static final int bass = 81;

        public static final int drumKit = 128;

        int instrument;
        boolean isMuted;
        Vector notes;

        int lastLength = 4, lastVelocity = 80;

        Channel( int instrument, int numNotes, boolean isMuted )
        {
            this.instrument = instrument;
            this.isMuted = isMuted;
            notes = new Vector( numNotes );
        }

        Channel( int instrument )
        {
            this( instrument, 4, false );

            if ( instrument == lead )
                notes.addElement( new Note( 60, 4, 80 ) );
            else if ( instrument == bass )
                notes.addElement( new Note( 48, 16, 60 ) );
        }

        Note addNote()
        {
            Note note = new Note( 0, lastLength, lastVelocity );
            notes.addElement( note );
            return note;
        }

        Note addNote( int midi, int length, int velocity )
        {
            Note note = new Note( midi, length, velocity );
            notes.addElement( note );
            return note;
        }

        Note getNote( int index )
        {
            if ( index < notes.size() )
                return ( Note )notes.elementAt( index );
            else
                return null;
        }

        Note insertNote( int index )
        {
            Note note = new Note( 0, lastLength, lastVelocity );
            notes.insertElementAt( note, index );
            return note;
        }

        void removeTail( int numNotes )
        {
            numNotes = Math.min( numNotes, notes.size() );

            int begin = notes.size() - numNotes;

            for ( int i = 0; i < numNotes; i++ )
                notes.removeElementAt( begin );
        }

        void repeatTail( int numNotes )
        {
            numNotes = Math.min( numNotes, notes.size() );

            int begin = notes.size() - numNotes;

            for ( int i = begin; i < begin + numNotes; i++ )
            {
                Note note = getNote( i );

                addNote( note.midi, note.length, note.velocity );
            }
        }

        void transpose( int semiTones )
        {
            for ( int i = 0; i < notes.size(); i++ )
                if ( getNote( i ).midi > 0 )
                    getNote( i ).midi += semiTones;
        }
    }

    private static Track instance;
    static int slot = -1;

    String name;
    int bpm;

    Vector channels;

    Track()
    {
        name = "untitled";
        bpm = 120;

        channels = new Vector();
        channels.addElement( new Channel( Channel.lead ) );
    }

    Channel addChannel( int instrument )
    {
        Channel channel = new Channel( instrument );
        channels.addElement( channel );
        return channel;
    }

    Channel addChannel( int instrument, int numNotes, boolean isMuted )
    {
        Channel channel = new Channel( instrument, numNotes, isMuted );
        channels.addElement( channel );
        return channel;
    }

    static void clear()
    {
        instance = new Track();
    }

    void cloneChannel( int channel, int instrument )
    {
        Channel source = getChannel( channel );
        Channel newChannel = addChannel( instrument, source.notes.size(), source.isMuted );

        for ( int i = 0; i < source.notes.size(); i++ )
        {
            Channel.Note note = source.getNote( i );

            newChannel.addNote( note.midi, note.length, note.velocity );
        }
    }

    Channel getChannel( int channel )
    {
        return ( Channel )channels.elementAt( channel );
    }

    static Track getInstance()
    {
        if ( instance == null )
            clear();

        return instance;
    }

    void load( int slot )
    {
        try
        {
            byte record[] = DataStorage.loadTrack( slot );

            if ( record == null )
                return;

            ByteArrayInputStream buffer = new ByteArrayInputStream( record );
            DataInputStream dis = new DataInputStream( buffer );

            byte version = dis.readByte();
            name = dis.readUTF();
            bpm = dis.readShort();

            int numChannels = dis.readByte();
            channels = new Vector( numChannels );

            System.out.println( "Version " + version + " track file: " + name + " (" + numChannels + ") channels" );

            for ( int i = 0; i < numChannels; i++ )
            {
                int instrument = dis.readShort();
                int numNotes = dis.readInt();
                boolean isMuted = false;

                if ( version > 0 )
                    isMuted = dis.readBoolean();

                Channel channel = addChannel( instrument, numNotes, isMuted );
                System.out.println( "Channel " + i + ": " + numNotes + " notes" );

                for ( int j = 0; j < numNotes; j++ )
                {
                    int midi = dis.readByte();
                    int length = dis.readShort();
                    int velocity = dis.readByte();

                    channel.addNote( midi, length, velocity );
                }
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }

    void removeChannel( int channel )
    {
        channels.removeElementAt( channel );
    }

    void save( int slot )
    {
        try
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream( buffer );

            dos.writeByte( 1 );
            dos.writeUTF( name );
            dos.writeShort( bpm );
            dos.writeByte( channels.size() );

            for ( int i = 0; i < channels.size(); i++ )
            {
                Channel channel = getChannel( i );
                dos.writeShort( channel.instrument );
                dos.writeInt( channel.notes.size() );
                dos.writeBoolean( channel.isMuted );

                for ( int j = 0; j < channel.notes.size(); j++ )
                {
                    Channel.Note note = channel.getNote( j );

                    if ( note.midi <= 0 )
                        dos.writeByte( 0 );
                    else
                        dos.writeByte( note.midi );

                    dos.writeShort( note.length );
                    dos.writeByte( note.velocity );
                }
            }

            byte record[] = buffer.toByteArray();
            System.out.println( "Track size: " + record.length );

            DataStorage.saveTrack( slot, record );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}


package org.minexew.nanotracker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import javax.microedition.rms.*;

public class DataStorage
{
    static RecordStore trackRs = null;

    public static final int numTrackSlots = 8;

    private static RecordStore getTrackDb() throws RecordStoreException
    {
        if ( trackRs == null )
            //try { RecordStore.deleteRecordStore( "nt-tracks" ); } catch ( Exception ex ) {}
            trackRs = RecordStore.openRecordStore( "nt-tracks", true );

        for ( int i = trackRs.getNumRecords(); i < numTrackSlots; i++ )
            trackRs.addRecord( null, 0, 0 );

        return trackRs;
    }

    static String[][] getTrackList() throws Exception
    {
        String list[][] = new String[numTrackSlots][2];

        for ( int slot = 0; slot < numTrackSlots; slot++ )
        {
            byte record[] = DataStorage.loadTrack( slot );

            if ( record == null )
            {
                list[slot][0] = "--";
                continue;
            }

            ByteArrayInputStream buffer = new ByteArrayInputStream( record );
            DataInputStream dis = new DataInputStream( buffer );

            byte version = dis.readByte();
            String name = dis.readUTF();
            int bpm = dis.readShort();
            int numChannels = dis.readByte();

            list[slot][0] = version + ":" + name;
            list[slot][1] = "  " + bpm + " BPM, ";

            if ( record.length < 1024 )
                list[slot][1] += record.length + " B";
            else
                list[slot][1] += Float.toString( record.length / 1024f ).substring( 0, 4 ) + " KiB";

            list[slot][1] += ", " + numChannels + " CH.";
        }

        return list;
    }

    static byte[] loadTrack( int slot ) throws RecordStoreException
    {
        return getTrackDb().getRecord( slot + 1 );
    }

    static void saveTrack( int slot, byte record[] ) throws RecordStoreException
    {
        getTrackDb().setRecord( slot + 1, record, 0, record.length );
    }
}

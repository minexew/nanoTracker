
package org.minexew.nanotracker;

import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * (c) 2009, 2011 Xeatheran Minexew
 */
public class Font
{
    /*
     * Escape sequences:    "\-" - previous texture (wraps)
     *                      "\+" - next texture (wraps)
     */

    public static final short left = 0, top = 0, center = 1, right = 2, bottom = 2;
    int charWidth, charHeight;
    Image textures[];

    public Font( Image textures[] )
    {
        this.textures = textures;
        getCharSize();
    }

    public Font( String textureNames[] ) throws IOException
    {
        textures = new Image[textureNames.length];
        
        for ( int i = 0; i < textureNames.length; i++ )
            textures[i] = Image.createImage( "/" + textureNames[i] );

        getCharSize();
    }

    public Font( String textureName ) throws IOException
    {
        this( new String[] { textureName } );
    }

    /**
     * Renders the given string using a single texture.
     * @param g <strong>Graphics</strong> object to paint the texture with.
     * @param x The X-coordinate of text anchor
     * @param y The Y-coordinate of text anchor
     * @param string String to be displayed
     * @param texId ID of the texture to be used
     * @param halign horizontal
     * @param valign and vertical alignment - use one of predefined constants
     */
    public void drawString( Graphics g, int x0, int y0, String string, int texture, short halign, short valign )
    {
        if ( string == null )
            return;

        int length = string.length();
        boolean escaped = false;

        // highly-optimized code for text aligning :)
        x0 -= length * charWidth / 2 * halign;
        y0 -= charHeight / 2 * valign;

        int x = x0, y = y0;

        for ( int i = 0; i < length; i++ )
        {
            char next = string.charAt( i );

            if ( next == '\\' && !escaped )
            {
                escaped = true;
                continue;
            }
            else if ( escaped && next == '-' )
            {
                texture--;

                while ( texture < 0 )
                    texture += textures.length;
            }
            else if ( escaped && next == '+' )
            {
                texture++;

                while ( texture >= textures.length )
                    texture -= textures.length;
            }
            else if ( next == '\n' )
            {
                x = x0;
                y += charHeight;
            }

            if ( next > ' ' )
                g.drawRegion( textures[texture], ( next - ' ' ) * charWidth, 0, charWidth, charHeight, 0, x, y, Graphics.LEFT | Graphics.TOP );

            x += charWidth;
        }
    }

    private void getCharSize()
    {
        if ( textures.length > 0 )
        {
            charWidth = textures[0].getWidth() / 96;
            charHeight = textures[0].getHeight();
        }
        else
            charWidth = charHeight = 0;
    }

    int getHeight()
    {
        return charHeight;
    }

    int getStringWidth( String s )
    {
        return s.length() * charWidth;
    }
}


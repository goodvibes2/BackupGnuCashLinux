/*
 * Copyright (C) 2020 Chris Good
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.openjfx;

import java.io.*;
//import java.util.*;
//import javafx.scene.control.TextArea;

/**
 * Refer http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
 * @author cgood
 */
class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    OutputStream os;

    StreamGobbler(InputStream is, String type)
    {
        this(is, type, null);
    }

    StreamGobbler(InputStream is, String type, OutputStream redirect)
    {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }

    @Override
    public void run()
    {
        try
        {
            PrintWriter pw = null;
            if (os != null)
                pw = new PrintWriter(os);

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            //String line=null;
            String line;
            while ( (line = br.readLine()) != null)
            {
                if (pw != null)
                    pw.println(line);
                System.out.println(type + ">" + line);
                // Following line causes :
                //  IllegalStateException: Not on FX application thread;
                // You can only update gui elements from the application thread!
                //ta.appendText(type + ">" + line + "\n");
            }
            if (pw != null)
                pw.flush();
        } catch (IOException ioe)
            {
            ioe.printStackTrace();
            }
    }
}
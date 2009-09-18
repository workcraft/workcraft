package org.workcraft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

public class FileUtils{
	   public static void copyFile(File in, File out)
   	throws IOException
	{
		   FileOutputStream outStream = new FileOutputStream(out);
		   try
		   {
			   copyFileToStream(in, outStream);
		   }
		   finally
		   {
			   outStream.close();
		   }
	}

	public static void copyFileToStream(File in, OutputStream out)
   		throws IOException
	{
	    FileChannel inChannel = new
	        FileInputStream(in).getChannel();
	    WritableByteChannel outChannel = Channels.newChannel(out);
	    try {
	        inChannel.transferTo(0, inChannel.size(),
	                outChannel);
	    }
	    catch (IOException e) {
	        throw e;
	    }
	    finally {
	        if (inChannel != null) inChannel.close();
	        if (outChannel != null) outChannel.close();
	    }
	}
}
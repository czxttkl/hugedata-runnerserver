package com.czxttkl.hugedata.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class StreamTool {

	public static void save(File file, byte[] data) throws Exception {
		FileOutputStream outStream = new FileOutputStream(file);
		outStream.write(data);
		outStream.close();
	}

	/**
	 * @param in
	 * @return String represents the next line
	 * @throws IOException
	 */
	public static String readLine(PushbackInputStream in) throws IOException {
		char buf[] = new char[128];
		int room = buf.length;
		int offset = 0;
		int c;
		loop: while (true) {
			switch (c = in.read()) {
			case -1:
			case '\n':
				break loop;
			case '\r':
				int c2 = in.read();
				if ((c2 != '\n') && (c2 != -1))
					in.unread(c2);
				break loop;
			default:
				if (--room < 0) {
					char[] lineBuffer = buf;
					buf = new char[offset + 128];
					room = buf.length - offset - 1;
					System.arraycopy(lineBuffer, 0, buf, 0, offset);

				}
				buf[offset++] = (char) c;
				break;
			}
		}
		if ((c == -1) && (offset == 0))
			return null;
		return String.copyValueOf(buf, 0, offset);
	}

	/**
	 * convert Inputstream to String
	 * 
	 * @param inStream
	 * @return String
	 * @throws Exception
	 */
	public static String readStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		return outSteam.toString();
	}

	public static String byteArrayToString(byte[] array, String charsetName) {
		Charset charset = Charset.forName(charsetName);
		ByteArrayInputStream byteIn = new ByteArrayInputStream(array);
		InputStreamReader reader = new InputStreamReader(byteIn,
				charset.newDecoder());
		int b = 0;
		String res = "";
		try {
			while ((b = reader.read()) > 0) {
				res += (char) b;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public static ByteBuffer stringToByteBuffer(String msg, String charsetName) {
		Charset charset = Charset.forName(charsetName);
		CharsetEncoder encoder = charset.newEncoder();
		try {
			return encoder.encode(CharBuffer.wrap(msg));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String bytebufferToString(ByteBuffer buffer,
			String charsetName) {
		Charset charset = Charset.forName(charsetName);
		CharsetDecoder decoder = charset.newDecoder();
		String data = "";
		try {
			int old_position = buffer.position();
			data = decoder.decode(buffer).toString();
			// reset buffer's position to its original so it is not altered:
			buffer.position(old_position);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return data;
	}
	
	/**
	 * Copy src file to dst  
	 *
	 * @param srcFile 
	 * 			  source file path + file name
	 * @param dstFile 
	 *            destination file path + file name
	 */
	public static void copyFile(String srcFile, String dstFile) {
		try{
	          File f1 = new File(srcFile);
	          File f2 = new File(dstFile);
	          
	          InputStream in = new FileInputStream(f1);

	          //For Append the file.
	          //OutputStream out = new FileOutputStream(f2,true);

	          //For Overwrite the file.
	          OutputStream out = new FileOutputStream(f2);

	          byte[] buf = new byte[1024];
	          int len;
	          while ((len = in.read(buf)) > 0){
	            out.write(buf, 0, len);
	          }
	          in.close();
	          out.close();
	          System.out.println("File copied.");
	        }
	        catch(FileNotFoundException ex){
	          System.out.println(ex.getMessage() + " in the specified directory.");
	          System.exit(0);
	        }
	        catch(IOException e){
	          System.out.println(e.getMessage());      
	        }
	}

}

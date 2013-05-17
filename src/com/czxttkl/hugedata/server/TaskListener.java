package com.czxttkl.hugedata.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.czxttkl.hugedata.helper.DeviceInfo;
import com.czxttkl.hugedata.helper.StreamTool;
import com.czxttkl.hugedata.test.PacketTest;
import com.sun.xml.internal.fastinfoset.util.CharArray;

/**
 * @author Zhengxing Chen
 * @see WebSocket Reference at:http://tools.ietf.org/html/rfc6455
 * 
 */
public class TaskListener {
	private ServerSocket serverSocket;
	public static Logger logger = Logger
			.getLogger(RunnerServer.class.getName());

	public TaskListener(int port) {
		logger.info("----------------------------------------------------------------");
		logger.info("Task Listener Initialization Starts");
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			logger.info("Task Listener Initialization Failed.");
			logger.info("----------------------------------------------------------------");
		}
	}

	public void startListen() {
		Socket socket = null;
		logger.info("Task Listener Initialization Completed.");
		logger.info("----------------------------------------------------------------");
		while (true) {
			try {
				socket = serverSocket.accept();
				RunnerServer.executor.execute(new TaskListenerHandler(socket));
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public class TaskListenerHandler implements Runnable {
		private HashMap<String, ArrayList<DeviceInfo>> deviceInfoMap = RunnerServer.deviceInfoMap;
		private Socket socket;
		private boolean hasHandshaked = false;
		private InputStream socketInputStream;
		

		public TaskListenerHandler(Socket socket) {
			this.socket = socket;
		}

		private PrintWriter getWriter(Socket socket) throws IOException {
			OutputStream socketOut = socket.getOutputStream();
			return new PrintWriter(socketOut, true);
		}

		public void run() {
			try {
				logger.info("New connection accepted" + socket.getInetAddress()
						+ ":" + socket.getPort());

				socketInputStream = socket.getInputStream();
				PrintWriter pw = getWriter(socket);

				// Read Buffer
				byte[] buf = new byte[1024];
				int len = socketInputStream.read(buf, 0, 1024);
				// Copy Array
				byte[] res = new byte[len];
				System.arraycopy(buf, 0, res, 0, len);

				String key = new String(res);
				if (!hasHandshaked && key.indexOf("Key") > 0) {
					// 握手
					key = key.substring(0, key.indexOf("==") + 2);
					key = key.substring(key.indexOf("Key") + 4, key.length())
							.trim();
					key += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
					MessageDigest md = MessageDigest.getInstance("SHA-1");
					md.update(key.getBytes("utf-8"), 0, key.length());
					byte[] sha1Hash = md.digest();
					sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
					key = encoder.encode(sha1Hash);
					pw.println("HTTP/1.1 101 Switching Protocols");
					pw.println("Upgrade: websocket");
					pw.println("Connection: Upgrade");
					pw.println("Sec-WebSocket-Accept: " + key);
					pw.println();
					pw.flush();
					hasHandshaked = true;

					byte[] first = new byte[1];
					int read = socketInputStream.read(first, 0, 1);
					
					while (read > 0) {
						// opCode:
						// 1:denotes a text frame
						// 8:denotes a connection close
						// A:denotes a pong
						byte opCode = (byte) (first[0] & 0x0F);
						System.out.println("opcode:" + opCode);

						if (opCode == 8) {
							socket.getOutputStream().close();
							break;
						}

						int b = socketInputStream.read();
						int mask = (b & 0x80) >> 7;
						System.out.println("mask:" + mask);
						/*
						 * The length of the "Payload data", in bytes: if 0-125,
						 * that is the payload length. If 126, the following 2
						 * bytes interpreted as a 16-bit unsigned integer are
						 * the payload length. If 127, the following 8 bytes
						 * interpreted as a 64-bit unsigned integer (the most
						 * significant bit MUST be 0) are the payload length.
						 */
						int payloadLength = b & 0x7F;
						if (payloadLength == 126) {
							byte[] extended = new byte[2];
							socketInputStream.read(extended, 0, 2);
							int shift = 0;
							payloadLength = 0;
							for (int i = extended.length - 1; i >= 0; i--) {
								payloadLength = payloadLength
										+ ((extended[i] & 0xFF) << shift);
								shift += 8;
							}
						} else if (payloadLength == 127) {
							byte[] extended = new byte[8];
							socketInputStream.read(extended, 0, 8);
							int shift = 0;
							payloadLength = 0;
							for (int i = extended.length - 1; i >= 0; i--) {
								payloadLength = payloadLength
										+ ((extended[i] & 0xFF) << shift);
								shift += 8;
							}
						}

						/*
						 * Defines whether the "Payload data" is masked. If set
						 * to 1, a masking key is present in masking-key, and
						 * this is used to unmask the "Payload data". All frames
						 * sent from client to server have this bit set to 1.
						 */
						byte[] maskingkey = new byte[4];
						if (mask == 1)
							socketInputStream.read(maskingkey, 0, 4);
						
						if (opCode == 1) {
							ByteBuffer byteBuf = parseTestData(payloadLength, maskingkey);
							
//							int readThisBit = 1;
//							ByteBuffer byteBuf = ByteBuffer
//									.allocate(payloadLength + 10);
//							byteBuf.put("echo: ".getBytes("UTF-8"));
//							while (payloadLength > 0) {
//								int raw = socketInputStream.read();
//								byte masked = (byte) (raw ^ (maskingkey[(readThisBit - 1) % 4]));
//								byteBuf.put((byte) masked);
//								payloadLength--;
//								readThisBit++;
//							}
							byteBuf.flip();
							responseClient(byteBuf, true);
						}
						
						socketInputStream.read(first, 0, 1);
					}
				}
				socketInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private ByteBuffer parseTestData(int payloadLength, byte[] maskingkey) {
			ByteBuffer byteBuf = ByteBuffer.allocate(payloadLength);
			ByteBuffer returnByteBuf = ByteBuffer.allocate(10);
			
			try {
			//socketInputStream.read(testData, 0, payloadLength);
				int readThisBit = 1;
				//byteBuf.put("echo: ".getBytes("UTF-8"));
				while (payloadLength > 0) {
					int raw = socketInputStream.read();
					byte masked = (byte) (raw ^ (maskingkey[(readThisBit - 1) % 4]));
					byteBuf.put((byte) masked);
					payloadLength--;
					readThisBit++;
				}
			System.out.println(StreamTool.byteArrayToString(byteBuf.array(), "UTF-8"));
			
			PacketTest a = new PacketTest.Builder("com.renren.mobile.android.test",
					deviceInfoMap.get("HTCT328WUNI").get(0))
					.testInstallPath("c:/Android/mytools/RenrenTestProject1.apk")
					.appInstallPath("c:/Android/mytools/renren.apk")
					.testDurationThres(999999).taskListernerHandler(this).build();
//			Thread.sleep(5000);
//			PacketTest b = new PacketTest.Builder("com.renren.mobile.android.test",
//					deviceInfoMap.get("HTCT328WUNI"))
//					.testInstallPath("c:/Android/mytools/RenrenTestProject1.apk")
//					.appInstallPath("c:/Android/mytools/renren.apk")
//					.testDurationThres(999999).priority(5).build();
//			Thread.sleep(5000);
//			PacketTest c = new PacketTest.Builder("com.renren.mobile.android.test",
//					deviceInfoMap.get("HTCT328WUNI"))
//					.testInstallPath("c:/Android/mytools/RenrenTestProject1.apk")
//					.appInstallPath("c:/Android/mytools/renren.apk")
//					.testDurationThres(999999).priority(6).build();

			deviceInfoMap.get("HTCT328WUNI").get(0).addToTestQueue(a);
			/*deviceInfoMap.get("HTCT328WUNI").addToTestQueue(b);
			deviceInfoMap.get("HTCT328WUNI").addToTestQueue(c);			*/
			
			
			returnByteBuf.put("Successful".getBytes("UTF-8"));
			} catch (Exception e) {
				//e.printStackTrace();
				logger.info(e.getMessage());
			}

			return returnByteBuf;
		}

		public void responseClient(ByteBuffer byteBuf, boolean finalFragment)
				throws IOException {
			OutputStream out = socket.getOutputStream();
			int first = 0x00;
			// 是否是输出最后的WebSocket响应片段
			if (finalFragment) {
				first = first + 0x80;
				first = first + 0x1;
			}
			out.write(first);

			if (byteBuf.limit() < 126) {
				out.write(byteBuf.limit());
			} else if (byteBuf.limit() < 65536) {
				out.write(126);
				out.write(byteBuf.limit() >>> 8);
				out.write(byteBuf.limit() & 0xFF);
			} else {
				// Will never be more than 2^31-1
				out.write(127);
				out.write(0);
				out.write(0);
				out.write(0);
				out.write(0);
				out.write(byteBuf.limit() >>> 24);
				out.write(byteBuf.limit() >>> 16);
				out.write(byteBuf.limit() >>> 8);
				out.write(byteBuf.limit() & 0xFF);
			}

			// Write the content
			out.write(byteBuf.array(), 0, byteBuf.limit());
			out.flush();
		}
		
	}
	

}
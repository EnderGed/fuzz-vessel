package vessel.communication;

import generator.Generator;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import vessel.generation.ShadowWeaver;
import vessel.utils.VesselUtils;

public class VesselServer implements Runnable {

	private ServerSocket sSocket;
	private Socket cSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private String className;
	private boolean connected = false;
	
	/**
	 * Is the work over?
	 */
	public boolean feierband = false;

	/**
	 * The constructor open the server connection.
	 * 
	 * @param addr
	 * @param port
	 * @param className
	 * @throws Exception
	 */
	public VesselServer(String addr, int port, String className)
			throws Exception {
		try {
			sSocket = new ServerSocket();
			sSocket.setReuseAddress(true);
			sSocket.bind(new InetSocketAddress(port));
			this.className = className;
			System.out.println("Server running at port "
					+ sSocket.getLocalPort() + "; awaiting connection.");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				cSocket.close();
			} catch (Exception e2) {
			}
			throw e;
		}
	}

	/**
	 * If not connected, waits for another connection.
	 */
	@Override
	public void run() {
		while (!feierband)
			if (!connected)
				try {
					connectionAcc();
				} catch (Exception e) {
					System.out.println(VesselUtils.DISCONNECTED);
				}
	}

	/**
	 * Accepts a new Ghost connection and sends a hello message.
	 * 
	 * @throws Exception
	 */
	public void connectionAcc() throws Exception {
		try {
			cSocket = sSocket.accept();
			oos = new ObjectOutputStream(cSocket.getOutputStream());
			ois = new ObjectInputStream(cSocket.getInputStream());
			System.out.println("New connection accepted.");
			send("hello " + className);
			System.out.println(receive());
			connected = true;
		} catch (SocketException se) {
			closeClient();
		} catch (Exception e) {
			e.printStackTrace();
			closeClient();
			throw e;
		}
	}

	/**
	 * Send a message. The message format are words separated with a whitespace
	 * (" "). The message is converted to a string array and then sent via
	 * Object Output Stream.
	 * 
	 * @param message
	 * @throws Exception
	 */
	public void send(String message) throws Exception {
		String[] words = message.split(" ");
		oos.writeObject(words);
		oos.flush();
	}

	/**
	 * Perform a single test. Generate necessary values for given args and send
	 * them to the Ghost alongside with the method name. Print elapsed time.
	 * 
	 * @param generator
	 * @param weaver
	 * @param methodName
	 * @param args
	 * @throws Exception
	 */
	public void performTest(Generator generator, ShadowWeaver weaver,
			String methodName, String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		Class<?>[] types = generator.getClassesFromClassNames(args);
		Object[] values;
		values = generator.generateValuesFromRaw(weaver.weaveArray(args));
		sendMethodData(methodName, types, values);
		System.out.println("Please wait for the end of test execution.");
		System.out.println(receive());
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("Test completed in " + estimatedTime + " ms.");
	}

	/**
	 * Send a string array describing how to run a method. The array format is
	 * [method name || argument types... || false || argument values...]. The
	 * "false" position separates argument types from argument values so that
	 * the Ghost knows when to stop registering argument types for the given
	 * method and start recording the values. The correct message should have an
	 * equal number of arg types and values.
	 * 
	 * @param methodName
	 * @param argTypes
	 * @param argValues
	 * @throws Exception
	 */
	public void sendMethodData(String methodName, Class<?>[] argTypes,
			Object[] argValues) throws Exception {
		String[] mn = { methodName };
		Boolean[] separator = { false };
		Object[] objAll = VesselUtils.joinArrays(
				VesselUtils.joinArrays(mn, argTypes),
				VesselUtils.joinArrays(separator, argValues));
		for (Object o : objAll) {
			System.out.println(o);
		}
		oos.writeObject(objAll);
		oos.flush();
	}

	/**
	 * Receive an object message and return it as a string.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String receive() throws Exception {
		return VesselUtils.joinStringArray(" ", (String[]) (ois.readObject()));
	}

	/**
	 * Send a "goodbye" message to the client so that it is able to safely
	 * disconnect.
	 * 
	 * @throws Exception
	 */
	public void goodbye() throws Exception {
		String[] goodbye = { "goodbye" };
		oos.writeObject(goodbye);
		oos.flush();
	}

	/**
	 * Close the client connection. Ignore all exceptions. This method does not
	 * include sending a "goodbye" message.
	 */
	public void closeClient() {
		try {
			oos.close();
		} catch (Exception e1) {
		}
		try {
			ois.close();
		} catch (Exception e2) {
		}
		try {
			cSocket.close();
		} catch (Exception e3) {
		}
		connected = false;
	}

	/**
	 * "Safely" close the server socket. Ignore all exceptions.
	 */
	public void closeAll() {
		closeClient();
		try {
			sSocket.close();
		} catch (Exception e) {
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

}

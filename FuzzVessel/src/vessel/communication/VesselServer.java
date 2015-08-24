package vessel.communication;

import fuzzcommons.Command;
import fuzzcommons.Command.CommandType;
import fuzzcommons.HelloBye.Greeting;
import fuzzcommons.HelloBye;
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
			hello(className);
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
	
	
	public void sendQueryClassCommand() throws Exception {
		oos.writeObject(new Command(CommandType.QUERY_CLASS, null, null, null));
		oos.flush();
	}
	
	
	public void sendQueryMethodCommand(String methodName) throws Exception {
		oos.writeObject(new Command(CommandType.QUERY_METHOD, methodName, null, null));
		oos.flush();
	}
	
	public void sendInitiateClassCommand(String className) throws Exception {
		oos.writeObject(new Command(CommandType.INITIATE, className, null, null));
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
		//for(int i=0; i<values.length; ++i)
		//	System.out.println(types[i] + " " + values[i]);
		sendDoCommand(methodName, types, values);
		System.out.println("Please wait for the end of test execution.");
		System.out.println(receive());
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("Test completed in " + estimatedTime + " ms.");
	}

	/**
	 * Send a command describing how to run a method.
	 * 
	 * @param methodName
	 * @param argTypes
	 * @param argValues
	 * @throws Exception
	 */
	public void sendDoCommand(String methodName, Class<?>[] argTypes,
			Object[] argValues) throws Exception {
		oos.writeObject(new Command(CommandType.DO, methodName, argTypes, argValues));
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
		oos.writeObject(new HelloBye(Greeting.GOODBYE));
		oos.flush();
	}
	
	/**
	 * Send a "hello" message to the client and give them a the class name.
	 * 
	 * @throws Exception
	 */
	public void hello(String className) throws Exception {
		oos.writeObject(new HelloBye(Greeting.HELLO, className));
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

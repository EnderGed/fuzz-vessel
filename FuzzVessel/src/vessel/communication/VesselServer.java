package vessel.communication;

import generator.Generator;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import vessel.generation.ShadowWeaver;
import vessel.utils.VesselUtils;

public class VesselServer{

	private ServerSocket sSocket;
	private Socket cSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private String className;

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

	public void connectionAcc() throws Exception {
		try {
			cSocket = sSocket.accept();
			oos = new ObjectOutputStream(cSocket.getOutputStream());
			ois = new ObjectInputStream(cSocket.getInputStream());
			System.out.println("New connection accepted.");
			send("hello " + className);
			System.out.println(receive());
		} catch (Exception e) {
			e.printStackTrace();
			closeClient();
			throw e;
		}
	}

	public void send(String message) throws Exception {
		String[] words = message.split(" ");
		try {
			oos.writeObject(words);
			oos.flush();
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void performTest(Generator generator, ShadowWeaver weaver, String methodName, String[] args) throws Exception{
		Class<?>[] types = generator
				.getClassesFromClassNames(args);
		Object[] values;
			values = generator.generateValuesFromRaw(weaver
					.weaveArray(args));
		long startTime = System.currentTimeMillis();
		sendMethodData(methodName, types, values);
		System.out
				.println("Please wait for end of test execution.");
		System.out.println(receive());
		long estimatedTime = System.currentTimeMillis()
				- startTime;
		System.out.println("Test completed in " + estimatedTime
				+ " ms.");
	}

	public void sendMethodData(String methodName, Class<?>[] argTypes, Object[] objs)
			throws Exception {
		String[] mn = { methodName };
		Boolean[] separator = { false };
		try {
			Object[] objAll = VesselUtils.joinArrays(VesselUtils.joinArrays(mn, argTypes), VesselUtils.joinArrays(separator, objs));
			/*for(Object o : objAll){
				System.out.println(o);
			}*/
			oos.writeObject(objAll);
			oos.flush();
		} catch (Exception e) {
			throw e;
		}
	}

	public String receive() throws Exception {
		try {
			return VesselUtils.joinStringArray(" ",
					(String[]) (ois.readObject()));
		} catch (Exception e) {
			throw e;
		}
	}

	public void goodbye() throws Exception {
		try {
			String[] goodbye = { "goodbye" };
			oos.writeObject(goodbye);
			oos.flush();
		} catch (Exception e) {
			throw e;
		}
	}

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
	}

	public void closeAll() {
		closeClient();
		try {
			sSocket.close();
		} catch (Exception e) {
		}
	}

}

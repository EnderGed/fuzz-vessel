import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Vessel {

	private ServerSocket sSocket;
	private Socket cSocket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private String className;

	public Vessel(String addr, int port, String className) throws Exception {
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

	public String receive() throws Exception {
		try {
			return joinStringArray(" ", (String[]) (ois.readObject()));
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

	private String joinStringArray(String separator, String[] array) {
		if (array.length == 0)
			return "";
		StringBuilder sb = new StringBuilder(array[0]);
		for (int i = 1; i < array.length; ++i) {
			sb.append(separator);
			sb.append(array[i]);
		}
		return sb.toString();
	}
}

import java.io.EOFException;
import java.util.Scanner;

//do 1 setRemoteControlClientPlaybackPosition int long
//do 1 registerMediaButtonIntent android.app.PendingIntent android.content.ComponentName android.os.IBinder
public class VesselMain {

	private Scanner scanner;

	public static void main(String[] args) {
		if (args.length != 3) {
			printUsage();
			return;
		}
		int port;
		String className = args[2];
		String addr = args[0];
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			printUsage();
			return;
		}
		Vessel server = null;
		try {
			server = new Vessel(addr, port, className);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		VesselMain cm = new VesselMain();
		cm.scanner = new Scanner(System.in);
		String comm;
		while (true) {
			System.out.println("Type a function");
			comm = cm.scanner.nextLine();
			if (comm.toLowerCase().startsWith("help"))
				printHelp();
			else if (comm.toLowerCase().startsWith("quit")) {
				break;
			} else if (comm.toLowerCase().startsWith("do")) {
				try {
					long startTime = System.currentTimeMillis();
					server.send(comm);
					System.out
							.println("Please wait for end of test execution.");
					System.out.println(server.receive());
					long estimatedTime = System.currentTimeMillis() - startTime;
					System.out.println("Tests were completed in "
							+ estimatedTime + " ms.");
				} catch (EOFException eofe) {
					System.out
							.println("Ghost application has probably crashed. This means, the test has most likely failed.");
				} catch (Exception e) {
					e.printStackTrace();
					server.closeAll();
				}
			} else if (comm.toLowerCase().startsWith("query")) {
				try {
					server.send(comm);
					System.out.println(server.receive());
				} catch (EOFException eofe) {
					System.out
							.println("Ghost application has probably crashed. This means, the test has most likely failed.");
				} catch (Exception e) {
					e.printStackTrace();
					server.closeAll();
				}
			} else {
				System.out
						.println("Incorrect command. Type \"help\" for hints.");
			}
		}
		try {
			server.goodbye();
		} catch (Exception e) {
			e.printStackTrace();
		}
		server.closeAll();
		cm.scanner.close();
		System.out.println("Exiting.");
	}

	public static void printHelp() {
		System.out.println("== HELP ==");
		// System.out.println("do generate [number of tests] [method name] [arg types ... ]\t\tExecute method with generated args of specified types.");
		System.out
				.println("do [number of tests] [method name] [arg types ... ]\t\tExecute method with generated args of specified types.");
		// System.out.println("do fixed [number of tests] [method name] [[arg type] [arg val] ... ]\t\tExecute method with specified args.");
		System.out
				.println("query [method name]\t\tQuery a method to get its arguments.");
		System.out
				.println("query-class\t\tQuery the class to get its methods.");
		System.out.println("help\t\tPrint this message.");
		System.out.println("quit\t\tExit the program.");
	}

	public static void printUsage() {
		System.out.println("== USAGE ==");
		System.out
				.println("== VesselMain [address] [port number] [class name] ==");
	}

}

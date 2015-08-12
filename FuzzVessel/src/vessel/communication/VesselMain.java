package vessel.communication;

import java.io.EOFException;
import java.util.Arrays;
import java.util.Scanner;

import vessel.generator.Generator;

//do 1 setRemoteControlClientPlaybackPosition int long
//do 1 registerMediaButtonIntent android.app.PendingIntent android.content.ComponentName android.os.IBinder
//do setRemoteControlClientPlaybackPosition int long
public class VesselMain {

	private Scanner scanner;
	private VesselServer server = null;
	public boolean feierband = false;
	private String dcMsg = "Ghost application has disconnected.";

	public static void main(String[] args) {
		if (args.length != 2 && args.length != 3) {
			printUsage();
			return;
		}
		int port;
		String addr = args[0];
		String className = "";
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			printUsage();
			return;
		}
		if (args.length == 3)
			className = args[2];
		VesselMain vm = new VesselMain();
		try {
			vm.server = new VesselServer(addr, port, className);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		vm.scanner = new Scanner(System.in);
		while (!vm.feierband) {
			vm.handleClient();
		}
		vm.server.closeAll();
		vm.scanner.close();
		System.out.println("Bye.");
	}

	public void handleClient() {
		try {
			server.connectionAcc();
		} catch (Exception e) {
			System.out.println(dcMsg);
			return;
		}
		String comm;
		Generator generator = new Generator();
		while (true) {
			System.out.println("Type a function.");
			comm = scanner.nextLine();
			if (comm.toLowerCase().startsWith("help"))
				printHelp();
			else if (comm.toLowerCase().startsWith("quit")) {
				feierband = true;
				break;
			} else if (comm.toLowerCase().startsWith("do")) {
				try {
					long startTime = System.currentTimeMillis();
					String[] commSplit = comm.split(" ");
					String methodName = commSplit[1];
					String[] args = Arrays.copyOfRange(commSplit, 2,
							commSplit.length);
					server.sendMethodData(methodName,
							generator.getClassesFromClassNames(args),
							generator.getRandomArgsFromClassNames(args)); // haha
					// server.send(comm);
					System.out
							.println("Please wait for end of test execution.");
					System.out.println(server.receive());
					long estimatedTime = System.currentTimeMillis() - startTime;
					System.out.println("Tests were completed in "
							+ estimatedTime + " ms.");
				} catch (EOFException eofe) {
					System.out.println(dcMsg);
					break;
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			} else if (comm.toLowerCase().startsWith("query")
					|| comm.toLowerCase().startsWith("initiate")) {
				try {
					server.send(comm);
					System.out.println(server.receive());
				} catch (EOFException eofe) {
					System.out.println(dcMsg);
					break;
				} catch (Exception e) {
					e.printStackTrace();
					break;
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
		server.closeClient();
	}

	public static void printHelp() {
		System.out.println("== HELP ==");
		System.out
				.println("initiate [class name]\t\tInitiate tests for class [class name].");
		System.out
				.println("do [method name] [arg types ... ]\t\tExecute method with generated args of specified types.");
		System.out
				.println("query method [method name]\t\tQuery a method to get its arguments.");
		System.out
				.println("query class\t\tQuery the class to get its methods.");
		System.out.println("help\t\tPrint this message.");
		System.out.println("quit\t\tExit the program.");
	}

	public static void printUsage() {
		System.out.println("== USAGE ==");
		System.out
				.println("== VesselMain [address] [port number] (optionally)[class name] ==");
	}

}

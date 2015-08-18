package vessel.communication;

import java.io.EOFException;
import java.io.InvalidClassException;
import java.util.Arrays;
import java.util.Scanner;

import vessel.generation.ShadowWeaver;

import generator.Generator;

//do setRemoteControlClientPlaybackPosition int long
//do registerMediaButtonIntent android.app.PendingIntent android.content.ComponentName android.os.IBinder
//do setRemoteControlClientPlaybackPosition int long
//do setStreamSolo int boolean android.os.IBinder
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
		ShadowWeaver weaver = new ShadowWeaver();
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
					String[] commSplit = comm.split(" ");
					String methodName = commSplit[1];
					String[] args = Arrays.copyOfRange(commSplit, 2,
							commSplit.length);
					Class<?>[] types = generator.getClassesFromClassNames(args);
					Object[] values;
					try {
						values = generator.generateValuesFromRaw(weaver
								.weaveArray(args));
					} catch (ClassNotFoundException cnfe) {
						System.out
								.println("Incorrect class detected. (Class not found exception)");
						continue;
					} catch (InvalidClassException ice) {
						System.out
								.println("Incorrect class detected. (Invalid class exception)");
						continue;
					}
					long startTime = System.currentTimeMillis();
					server.sendMethodData(methodName, types, values);
					System.out
							.println("Please wait for end of test execution.");
					System.out.println(server.receive());
					long estimatedTime = System.currentTimeMillis() - startTime;
					System.out.println("Tests were completed in "
							+ estimatedTime + " ms.");
				} catch (EOFException eofe) {
					System.out.println(dcMsg);
					server.closeClient();
					return;
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
					server.closeClient();
					return;
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
		System.out.println("=== HELP ===");
		System.out
				.println("do [method name] [arg types ... ]\tExecute method with generated args of specified types.");
		System.out.println("help\t\t\t\t\tPrint this message.");
		System.out
				.println("initiate [class name]\t\t\tInitiate tests for class [class name].");
		System.out
				.println("query class\t\t\t\tQuery the class to get its methods.");
		System.out
				.println("query method [method name]\t\tQuery a method to get its arguments.");
		System.out.println("quit\t\t\t\t\tExit the program.");
	}

	public static void printUsage() {
		System.out
				.println("USAGE: VesselMain [address] [port number] (optionally)[class name]");
	}

}

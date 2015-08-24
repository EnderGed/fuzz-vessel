package vessel.communication;

import java.io.EOFException;
import java.io.InvalidClassException;
import java.util.Arrays;
import java.util.Scanner;

import vessel.generation.ShadowWeaver;
import vessel.generation.SourceReader;
import vessel.utils.VesselUtils;

import generator.Generator;

//EXAMPLE COMMANDS:
//do 1 setRemoteControlClientPlaybackPosition int long
//do 1 registerMediaButtonIntent android.app.PendingIntent android.content.ComponentName android.os.IBinder
//do 1 setRemoteControlClientPlaybackPosition int long
//do 1 setStreamSolo int boolean android.os.IBinder

/**
 * Main vessel class. It is responsible mostly for command line communication.
 * 
 */
public class VesselMain {

	private Scanner scanner;
	private VesselServer server;
	private Generator generator;
	private ShadowWeaver weaver;

	public VesselMain(String addr, int port, String className) throws Exception {
		server = new VesselServer(addr, port, className);
		scanner = new Scanner(System.in);
		generator = new Generator();
		weaver = new ShadowWeaver();
		new Thread(server).start();
	}

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
		VesselMain vm;
		try {
			vm = new VesselMain(addr, port, className);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		vm.mainScannerLoop();
		vm.server.closeAll();
		vm.scanner.close();
		System.out.println("Bye.");
	}

	/**
	 * Main communication loop. Accepts input from the scanner and reacts
	 * adequately.
	 */
	private void mainScannerLoop() {
		String comm;
		while (true) {
			System.out.println("Type a function.");
			comm = scanner.nextLine();
			if (comm.toLowerCase().startsWith("help"))
				printHelp();
			else if (comm.toLowerCase().startsWith("quit")) {
				server.feierband = true;
				break;
			} else if (comm.toLowerCase().startsWith("do")) {
				if (server.isConnected())
					try {
						String[] commSplit = comm.split(" ");
						performTests(Integer.parseInt(commSplit[1]),
								commSplit[2], Arrays.copyOfRange(commSplit, 3,
										commSplit.length));
					} catch (NumberFormatException nfe) {
						System.out.println("Incorrect trials number.");
						continue;
					} catch (EOFException eofe) {
						System.out.println(VesselUtils.DISCONNECTED);
						server.closeClient();
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				else
					System.out.println(VesselUtils.NOT_CONNECTED);
			} else if (comm.toLowerCase().startsWith("query")
					|| comm.toLowerCase().startsWith("initiate")) {
				if (server.isConnected())
					try {
						if (sendSimpleCommand(comm.toLowerCase()))
							System.out.println(server.receive());
					} catch (EOFException eofe) {
						System.out.println(VesselUtils.DISCONNECTED);
						server.closeClient();
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				else
					System.out.println(VesselUtils.NOT_CONNECTED);
			} else {
				System.out.println(VesselUtils.INCORRECT_COMMAND);
			}
		}
	}

	private boolean sendSimpleCommand(String command) throws Exception {
		try {
			String[] details = command.split(" ");
			if (details.length == 3 && details[0].equals("query")
					&& details[1].equals("method"))
				server.sendQueryMethodCommand(details[2]);
			else if (details.length == 2 && details[0].equals("query") && details[1].equals("class"))
				server.sendQueryClassCommand();
			else if (details.length == 2 && details[0].equals("initiate"))
				server.sendInitiateClassCommand(details[1]);
			else {
				System.out.println(VesselUtils.INCORRECT_COMMAND);
				return false;
			}
			return true;
		} catch (EOFException eofe) {
			System.out.println(VesselUtils.DISCONNECTED);
			server.closeClient();
			return false;
		}
	}

	/**
	 * Run a loop in which the server performs a series of tests.
	 * 
	 * @param trials
	 * @param methodName
	 * @param args
	 * @throws Exception
	 */
	private void performTests(int trials, String methodName, String[] args)
			throws Exception {
		long overallStartTime = System.currentTimeMillis();
		for (int i = 0; i < trials; ++i) {
			try {
				server.performTest(generator, weaver, methodName, args);
			} catch (ClassNotFoundException cnfe) {
				System.out
						.println("Incorrect class detected. (Class not found exception)");
				continue;
			} catch (InvalidClassException ice) {
				System.out
						.println("Incorrect class detected. (Invalid class exception)");
				continue;
			}
		}
		long overallEstimatedTime = System.currentTimeMillis()
				- overallStartTime;
		System.out.println("All tests were completed in "
				+ overallEstimatedTime + " ms.");
	}

	/**
	 * Does exactly what you'd expect.
	 */
	private static void printHelp() {
		System.out.println("=== HELP ===");
		System.out
				.println("do [number] [method name] [arg types ... ]\tExecute method with generated args of specified types.");
		System.out.println("help\t\t\t\t\tPrint this message.");
		System.out
				.println("initiate [class name]\t\t\tInitiate tests for class [class name].");
		System.out
				.println("query class\t\t\t\tQuery the class to get its methods.");
		System.out
				.println("query method [method name]\t\tQuery a method to get its arguments.");
		System.out.println("quit\t\t\t\t\tExit the program.");
	}

	/**
	 * Prints usage. This is normally called when invalid arguments are given
	 * when starting the program.
	 */
	private static void printUsage() {
		System.out
				.println("USAGE: VesselMain [address] [port number] (optionally)[class name]");
	}

}

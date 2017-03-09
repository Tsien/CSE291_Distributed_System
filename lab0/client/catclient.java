/*
You should write a client called, "catclient" that 
queries a "catserver" every 3 seconds for 30 seconds. 
Upon receiving each response, it should check to see if the response is in a text file. 
If it is, it should echo "OK". 
If it isn't, it should echo "MISSING", in either case followed by a newline. 
The path to the text file should be the first argument. 
The server's port number should be the second.
 */ 

import java.net.*;
import java.io.*;
import java.util.*;

public class catclient {
    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println("Usage: java catserver <path to the text file> <port number>");
            System.exit(1);
        }
        
        //"--link catserver" specify the hostNmae when building the client
        String hostName = "catserver";
        String filePath = args[0];
        int portNumber = Integer.parseInt(args[1]);
        //read lines into the ArrayList
        Scanner sc = new Scanner(new File(filePath));
		ArrayList<String> lines = new ArrayList<String>();
		while (sc.hasNextLine()){
		    lines.add(sc.nextLine().toUpperCase());
		}
		sc.close();
		int len = lines.size();

        try (
            Socket catSocket = new Socket(hostName, portNumber);
            PrintWriter out =
                new PrintWriter(catSocket.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(catSocket.getInputStream()));
        ) {
            String msg;

            for (int i = 0; i < 10; ++i) {// for 30s
                try {
                    //every 3 seconds
                    Thread.sleep(3000);
                } catch(Exception e) {
                    System.out.println("Exception : " + e.getMessage());
                }
                out.println("LINE");
                msg = in.readLine();
                System.out.println(msg);
                //check
                int idx = 0;
                for (idx = 0; idx < len; ++idx) {
                    if (lines.get(idx).equals(msg)) {
                        System.out.println("OK");
                        break;
                    }
                }
                if (idx == len) {
                    System.out.println("MISSING");
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
    }
}


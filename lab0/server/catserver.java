/*
When it receives "LINE" followed by a new line, it should reply with the next line of the file, 
followed by a new line - except that the reply should be in all UPPERCASE. 
With each call, it should work its way down the file. 
If it gets to the bottom, it should start over at the top. 
The path to the text file should be the first command-line argument. 
The port number should be the second, e.g. "catserver /var/datavol/sample.txt 2000"
 */ 

import java.net.*;
import java.io.*;
import java.util.*;

public class catserver {
    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println("Usage: java catserver <path to the text file> <port number>");
            System.exit(1);
        }
        
        String filePath = args[0];
        int portNumber = Integer.parseInt(args[1]);
        //read lines into the ArrayList
        Scanner sc = new Scanner(new File(filePath));
		ArrayList<String> lines = new ArrayList<String>();
		while (sc.hasNextLine()){
		    lines.add(sc.nextLine().toUpperCase());
		}
		sc.close();
		int len = lines.size(), idx = 0;

        try (
        	//
            ServerSocket serverSocket =
                new ServerSocket(portNumber);
            //accepting a connection from a client 
            Socket clientSocket = serverSocket.accept();     
            //Gets the socket's input and output stream and opens readers and writers on them.
            PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);                   
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        ) {

            String inputLine, line;
            
            while ((inputLine = in.readLine()).equals("LINE")) {
            	if (idx == len) {
            		idx = 0;
            	}
				line = lines.get(idx);
				idx++;
                out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
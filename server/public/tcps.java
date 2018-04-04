
/*---------------------------------------------------------------------------------------
--	Source File:		tcps.java - A simple (multi-threaded) Java TCP echo server
--
--	Classes:		tcps - public class
--				ServerSocket - java.net
--				Socket	     - java.net
--
--	Methods:
--				getRemoteSocketAddress 	(Socket Class)
--				getLocalSocketAddress  	(Socket Class)
--				getInputStream		(Socket Class)
--				getOutputStream		(Socket Class)
--				getLocalPort		(ServerSocket Class)
--				setSoTimeout		(ServerSocket Class)
--				accept			(ServerSocket Class)
--
--
--	Date:			April 3, 2018
--
--	Revisions:		(Date and Description)
--                April 3, 2018
--                Initialize and Set up Project
--
--	Designer:		  Anthony Vu, Li-Yan Tong, Morgan Ariss, John Tee
--                Source: tcps.java Aman Abdulla (February 8, 2014)
--
--	Programmer:		Anthony Vu & Li-Yan Tong
--
--	Notes:
--	The program utilizes the java.net package to implement a basic web server.
--  The server is multi-threaded so every new client connection is handled by a
--  separate thread.
--
--	The application receives a string from an echo client and appends this data
--  to a .csv file to be later formatted by a website.
--
--	Generate the class file and run it as follows:
--			javac tcps.java
--			java tcps <server port>
---------------------------------------------------------------------------------------*/

import java.net.*;
import java.io.*;
import java.util.HashMap;

public class tcps extends Thread
{
  String ServerString;
  private ServerSocket ListeningSocket;
  private HashMap<String, String> clients;
  public tcps (int port) throws IOException
  {
    ListeningSocket = new ServerSocket(port);
    clients = new HashMap<>();
  }

  public void run()
  {
    while(true)
    {
      try
      {
        // Listen for connections and accept
        System.out.println ("Listening on port: " + ListeningSocket.getLocalPort());
        Socket NewClientSocket = ListeningSocket.accept();
        System.out.println ("Connection from: "+ NewClientSocket.getRemoteSocketAddress());
        //create a thread for each client
        Thread r = new ReadThread(NewClientSocket);
        r.start();
      }

      catch(IOException e)
      {
        e.printStackTrace();
        break;
      }
    }

  }

  class ReadThread extends Thread {

    private Socket s;
    public ReadThread(Socket s) {
      this.s = s;
    }
    public void run() {

      while(true) {
        DataInputStream in = null;
        try {
          in = new DataInputStream(s.getInputStream());
          ServerString = in.readLine();
        } catch (IOException e) {
          e.printStackTrace();
        }
        if(ServerString.toLowerCase().equals("quit")) {
          System.out.println(s.getRemoteSocketAddress() + " has disconnected.");
          try {
            s.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        }
        if(ServerString.length() > 0) {
          //print out coordinates of phone later on
          try {
            // Create .csv file to test values on Webserver
            System.out.println ("Message: " + ServerString + " from " + s.getRemoteSocketAddress());
            if(!clients.contains(s.getRemoteSocketAddress())) {
              clients.add(s.getRemoteSocketAddress(), ServerString);
              FileWriter pw = new FileWriter("geo.csv",true);
              StringBuilder sb = new StringBuilder();
              String[] addressString = s.getRemoteSocketAddress().toString().split(":");
              String[] serverStringArray = ServerString.split(" ");
              sb.append(serverStringArray[2]);
              sb.append(',');
              sb.append(serverStringArray[0]);
              sb.append(',');
              sb.append(serverStringArray[1]);
              sb.append(',');
              sb.append(addressString[0]);
              sb.append('\n');
              pw.write(sb.toString());
              pw.close();
            } else {

            }
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }

        }
      }
    }

  }
  public static void main (String [] args)
  {

    if(args.length != 1)
    {
      System.out.println("Usage Error : java jserver <port>");
      System.exit(0);
    }
    int port = Integer.parseInt(args[0]);

    try
    {
      Thread t = new tcps (port);
      t.start();
    }

    catch(IOException e)
    {
      e.printStackTrace();
    }
  }
}

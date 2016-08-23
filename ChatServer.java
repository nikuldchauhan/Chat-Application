 import java.util.*;
import java.net.*;
import java.io.*;

public class ChatServer
{
	private ServerSocket serverSock;
	private Socket fromClient;
	
	private static Vector loginNames;
	private static Vector loginSocks;
	
	public ChatServer()
	{
		try
		{
			loginNames = new Vector();
			loginSocks = new Vector();
			
			serverSock = new ServerSocket(4444);
		}
		catch(Exception e)
		{
			System.out.println("Server Start Problem" + e);
		}
		System.out.println("Server Started");
		while(true)
		{
			try
			{
				fromClient = serverSock.accept();
				System.out.println("Server : Client Connected");
				Connect con = new Connect(fromClient);
			}
			catch(Exception e)
			{
				System.out.println("Accept Problem : "+e);
			}
		}
	}

	public static void main(String args[])
	{
		new ChatServer();
	}
	
	class Connect extends Thread
	{
		boolean threadOk = true;
		int vecIndex,curIndex;
		String cmdStr;
		String currentLogin;
		String delLogin;
		String mesg;
		String receiver;
		String sender;
		String stringFromClient;
		String FileFromClient;
		String tmpMesg;
		String tmpName;
		String tmpNameList;
	
		String fine;
		StringTokenizer readTokenizer;

		Socket fromClient;
		Socket listClientSocket;
		Socket sendToSocket;

		Iterator myIterator;
		PrintStream outPrintStream;

		private PrintStream out;
		private BufferedReader in;

		public Connect(Socket fromClient)
		{
			this.fromClient = fromClient;
			try
			{
				out = new PrintStream(fromClient.getOutputStream());
				in = new BufferedReader(new InputStreamReader(fromClient.getInputStream()));
			}
			catch(Exception e)
			{
				System.out.println("Stream Error");
			}
			this.start();
		}		
		public void run()
		{
			System.out.println("Server : Thread started for "+fromClient);
			while(threadOk)
			{
				try
				{
					stringFromClient = in.readLine();
					System.out.println("ServerStringFromClient:" + stringFromClient);
					readTokenizer = new StringTokenizer(stringFromClient,"%");

					cmdStr = readTokenizer.nextToken();
					System.out.println("CMDSTR : " + cmdStr);
					if(cmdStr.equals("Login"))
					{
						currentLogin = readTokenizer.nextToken();
							
						loginNames.add(currentLogin);
						loginSocks.add(fromClient);

						myIterator = loginNames.iterator();
						
						tmpName = "NewLogin";
						tmpMesg ="NewLoginMess%"+currentLogin;
						while(myIterator.hasNext())
						{
							tmpName += "%";
							tmpName +=(String)myIterator.next();
						}
						
						myIterator = loginSocks.iterator();
						while(myIterator.hasNext())
						{
							listClientSocket = (Socket)myIterator.next();
							outPrintStream = new PrintStream(listClientSocket.getOutputStream());
							outPrintStream.println(tmpName);
							outPrintStream.println(tmpMesg);
						}
					}
					if(cmdStr.equals("Logout"))
					{
						delLogin = readTokenizer.nextToken();
						vecIndex = loginNames.indexOf((Object)delLogin);
						curIndex = loginSocks.indexOf((Object)fromClient);
						if(vecIndex == curIndex)
							threadOk = false;
						loginNames.removeElementAt(vecIndex);
						loginSocks.removeElementAt(vecIndex);
						myIterator = loginNames.iterator();
						tmpName = "DelLogin";
						tmpMesg = "DelLoginMess%"+delLogin;
						
						while(myIterator.hasNext())
						{
							tmpName+="%";
							tmpName+=(String)myIterator.next();
						}
						myIterator = loginSocks.iterator();
						while(myIterator.hasNext())
						{
							listClientSocket = (Socket)myIterator.next();
							outPrintStream = new PrintStream(listClientSocket.getOutputStream());
							outPrintStream.println(tmpName);
							outPrintStream.println(tmpMesg);
						}
					}
					if(cmdStr.equals("SendMessage"))
					{
						System.out.println("SendMessage is executed");
						mesg=readTokenizer.nextToken();
						System.out.println("Message is : "+mesg);
						sender = readTokenizer.nextToken();
						System.out.println("SENDER is : " +sender);
						while(readTokenizer.hasMoreTokens())
						{
							receiver = readTokenizer.nextToken();
							vecIndex = loginNames.indexOf((Object)receiver);
							sendToSocket = (Socket)loginSocks.elementAt(vecIndex);
							outPrintStream = new PrintStream(sendToSocket.getOutputStream());
							outPrintStream.println("ChatMess%"+mesg+"%"+sender);
							System.out.println("While is executed");
						}
					}
					if(cmdStr.equals("SendFile"))
					{
						System.out.println("SendFile is executed");
						mesg=readTokenizer.nextToken();
						System.out.println("File Name is : "+mesg);
						sender = readTokenizer.nextToken();
						System.out.println("SENDER is : " +sender);
						receiver = readTokenizer.nextToken();
						vecIndex = loginNames.indexOf((Object)receiver);
						sendToSocket = (Socket)loginSocks.elementAt(vecIndex);
						outPrintStream = new PrintStream(sendToSocket.getOutputStream());
						outPrintStream.println("CommingFile%"+mesg+"%"+sender);
						FileFromClient = in.readLine();
						System.out.println(FileFromClient);
						outPrintStream.println(FileFromClient);
						System.out.println("File Sucessfully Send to Client: \n");
						fine="D:/";
						fine=fine+mesg;
						try
						{
						System.out.println("Writting is called");
						PrintWriter pw= new PrintWriter(new FileWriter(fine),true);
						pw.println(FileFromClient);
						pw.close();
						}catch(Exception e){ System.out.println(e);}	
					}
				}
				catch(Exception e)
				{
					System.out.println("InputStream Problem " + e);
				}
			}
		}

	}
}
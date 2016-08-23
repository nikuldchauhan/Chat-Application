import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
/*
<applet code="ChatClient" width=500 height=500>
</applet>
*/

public class ChatClient extends Applet implements ActionListener
{
	String cmdStr;
	String currLogin;
	String logMesg;
	String FilePrint;
	String serverMess;
	StringTokenizer readTokenizer;
	
	Label loginLabel;
	Label currLoginLabel;
	
	TextField loginText;
	TextField chatMessage;
	
	Button logonButton;
	Button sendButton;
	Button loginButton;
	Button sendFileButton;

		
	TextArea chatTextArea;
	java.awt.List loginList;
	
	Panel loginPanel;
	Panel mainPanel;
	Panel chatPanel;
	Panel messagePanel;
	
	CardLayout c1;
	
	BufferedReader in;
	PrintStream out;
	
	Socket servSocket;
	Thread listUpdateThread;
	ListMesgUpdate listMesgObj;
	SendMessage sendMessageObj;

	public void init()
	{
		try
		{
			servSocket = new Socket("127.0.0.1",4444);
			in = new BufferedReader(new InputStreamReader(servSocket.getInputStream()));
			out = new PrintStream(servSocket.getOutputStream());

		}
		catch(Exception e)
		{
			System.out.println("Socket error on client : "+e);
		}
		
		c1 =new CardLayout();
		setLayout(new BorderLayout());
		
		mainPanel = new Panel();
		add(mainPanel,BorderLayout.CENTER);
		
		loginPanel = new Panel();
		chatPanel = new Panel();
		messagePanel = new Panel();
		
		mainPanel.setLayout(c1);
		
		mainPanel.add("card1",loginPanel);
		mainPanel.add("card2",chatPanel);
	
		loginLabel = new Label("Login");
		loginText = new TextField(30);
		logonButton = new Button("LOGON");
		loginList = new java.awt.List(10,true);
		chatMessage  = new TextField(30);
		sendButton = new Button("SEND");
		sendFileButton=new Button("Send File");
		sendMessageObj = new SendMessage();
		sendButton.addActionListener(sendMessageObj);
		sendFileButton.addActionListener(sendMessageObj);

		loginPanel.add(loginLabel);
		loginPanel.add(loginText);
		loginPanel.add(logonButton);
		
		logonButton.addActionListener(this);
		c1.show(mainPanel,"card1");

		chatPanel.setLayout(new BorderLayout());
		chatTextArea = new TextArea(20,20);
		chatPanel.add(chatTextArea,BorderLayout.CENTER);
		chatPanel.add(loginList,BorderLayout.EAST);
		messagePanel.add(chatMessage);
		messagePanel.add(sendButton);
		messagePanel.add(sendFileButton);



		chatPanel.add(messagePanel,BorderLayout.SOUTH);

	}
	public void actionPerformed(ActionEvent ae)
	{
		currLogin = loginText.getText();
		System.out.println("Currlogin is : " + currLogin);
		logMesg = "Login%"+currLogin;
		out.println(logMesg);
		System.out.println("logMesg is:"+logMesg);
		
		listMesgObj = new ListMesgUpdate();
		listMesgObj.start();
	}
	public void destroy()
	{
		logMesg = "Logout%"+currLogin;
		out.println(logMesg);
	}

	class ListMesgUpdate extends Thread
	{
		public void run()
		{
			String chatMessage;
			String loginName;
			String sender;
			
			while(true)
			{
				try
				{
					serverMess = in.readLine();
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
						
			readTokenizer = new StringTokenizer(serverMess,"%");
	
			cmdStr = readTokenizer.nextToken();
			System.out.println("CMDSTR : " + cmdStr);
			
			if(cmdStr.equals("NewLogin"))
			{
				loginList.removeAll();
				while(readTokenizer.hasMoreTokens())
				{
					loginName = readTokenizer.nextToken();
					loginList.add(loginName);
				}
			}
			if(cmdStr.equals("NewLoginMess"))
			{
				while(readTokenizer.hasMoreTokens())
				{
					loginName = readTokenizer.nextToken();
					chatTextArea.append(loginName+" has lo0gged in\n");
				}
			}
			if(cmdStr.equals("DelLogin"))
			{
				loginList.removeAll();
				while(readTokenizer.hasMoreTokens())
				{
					loginName = readTokenizer.nextToken();
					loginList.add(loginName);
				}		
			}
			if(cmdStr.equals("DelLoginMess"))
			{
				while(readTokenizer.hasMoreTokens())
				{
					loginName =readTokenizer.nextToken();
					chatTextArea.append("Good Bye " +loginName + "\n");
				}	
			}
			if(cmdStr.equals("ChatMess"))
			{
				chatMessage = readTokenizer.nextToken();
				sender = readTokenizer.nextToken();
				chatTextArea.append(sender+" "+"says : " +chatMessage+"\n");
				System.out.println("IF IS EXECUTED");
				
			}	

			if(cmdStr.equals("CommingFile"))
			{	
				String fName = readTokenizer.nextToken();
				System.out.println("Receiving File :"+fName);
				sender = readTokenizer.nextToken();
				chatTextArea.append(sender+" "+"is Sending : " +fName+"\n");
				System.out.println("FILE IF IS EXECUTED");
				try
				{

				FilePrint = in.readLine();
				System.out.println(FilePrint);
				chatTextArea.append(FilePrint);
				PrintWriter pw= new PrintWriter(new FileWriter("D:/Test1.txt"),true);
				pw.println(FilePrint);
				pw.close();
				}catch(Exception e){System.out.println("Exception : " + e);}
	
			}		
			c1.show(mainPanel,"card2");

			
		}
	}
	
	}

	class SendMessage implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{

		    try
		    {
			if(ae.getSource()==sendButton)
			{
			//System.out.println("Diku");
			String createMess;
			String selectList[]= new String[10];
			selectList = loginList.getSelectedItems();
			
				if(selectList.length>0)
				{
					createMess = "SendMessage%"+chatMessage.getText()+"%"+currLogin;
					for(int i=0;i<selectList.length;i++)
					{
						createMess +="%";
						createMess +=selectList[i];
					}
					out.println(createMess);
				}
			}

			if(ae.getSource()==sendFileButton)
			{
				System.out.println("Nikul");
				String MyMessage;
				String selected="";
				String fileName=chatMessage.getText();
				selected=loginList.getSelectedItem();
				MyMessage="SendFile%"+fileName+"%"+currLogin+"%"+selected;
				out.println(MyMessage);

				//System.out.println("File name Sent.. ");
				
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String strsend="";
				String tmp;
				while((tmp=br.readLine())!=null)
				{
					//System.out.println(strsend);
					strsend=strsend+tmp;
				}	
				out.println(strsend);
				br.close();
			}
		    }catch(Exception e){System.out.println("there is a problem");}
			
		}
	}

	

}
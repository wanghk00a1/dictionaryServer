import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

public class Server extends JFrame{
	private JTextArea jta = new JTextArea();
	
	public static void main(String[] args){
		new Server();
	}
	
	public Server(){
		setLayout(new BorderLayout());
		add(new JScrollPane(jta),BorderLayout.CENTER);
		setTitle("Server");
		setSize(500,300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		try{
			int ClientNo=1;
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(9083);
			jta.append("Server started at " + new Date() + '\n');
			while(true){
				Socket socket = serverSocket.accept();
				jta.append("Client " + ClientNo + ":");
				//DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
				//DataOutputStream ouputToClient = new DataOutputStream(socket.getOutputStream());
				HandleAClient task = new HandleAClient(socket);
				new Thread(task).start();
				ClientNo++;
				//while(true){
				//String word = inputFromClient.readUTF();
				
				//对接收到的单词进行处理
				//String result;
				//ouputToClient.writeUTF(word);
				//jta.append("success!"+'\n');
				
			}
		}
		catch(IOException ex){
			System.err.println(ex);
		}
	}
	
	class HandleAClient implements Runnable{
		private Socket socket;
		
		public HandleAClient(Socket socket){
			this.socket = socket;
		}
		
		public void run(){
			try{
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
				DataOutputStream ouputToClient = new DataOutputStream(socket.getOutputStream());
				while(true){
					String word = inputFromClient.readUTF();
					
					//对接收到的单词进行处理
					//String result;
					Spider sp= new Spider(word);
					String[] result1=sp.search();
					ouputToClient.writeUTF(result1[1]);
					System.out.println(result1);
					jta.append("success!"+'\n');
				}
			}
			catch(IOException e){
				System.err.println(e);
			}
		}
	}

}

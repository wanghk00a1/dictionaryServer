import java.awt.BorderLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Server extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea jta = new JTextArea();
	
	public Map<String, Socket> UserSocketMap = new HashMap<String, Socket>();
	public String sendname;
	
	public static void main(String[] args) 
			throws ClassNotFoundException, SQLException{
		new Server();
	}
	
	public Server() 
			throws ClassNotFoundException, SQLException{
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
				
				SQLServer sql=new SQLServer(socket);
				HandleAClient task = new HandleAClient(socket,sql);
				new Thread(task).start();
				ClientNo++;
			}
		}
		catch(IOException ex){
			System.err.println(ex);
		}
	}
	
	class HandleAClient implements Runnable{
		private Socket socket;
		private SQLServer sql;
		String username;
		public HandleAClient(Socket socket, SQLServer sql){
			this.socket = socket;
			this.sql=sql;
		}


		@SuppressWarnings("resource")
		public void run(){
			try{
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
				DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
				while(true){
					String order = inputFromClient.readUTF();
					if(order.equals("register")){
						String result;
						try {
							result = sql.add_new_user();
							if(!result.equals("fail")) System.out.println(result);
						} catch (SQLException | NoSuchAlgorithmException e) {
							e.printStackTrace();
						}
					}
					
					else if(order.equals("login")){
						try {
							String userName = sql.user_login();
							UserSocketMap.put(userName, socket);
							username=userName;
						} catch (SQLException | NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					else if(order.equals("logout")){
						try {
							sql.user_logout();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					else if(order.equals("search")){
						SearchWord search = new SearchWord(socket,username);
						try {
							search.Search(sql);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					else if(order.equals("like")){
						try {
							sql.like_word();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					else if(order.equals("HaveMessage")){
						//TODO:
						String sname=inputFromClient.readUTF();
						System.out.println(sname);
						//sname new massage 
						String online = sql.online();
						outputToClient.writeUTF(online);
						//user online username+' '+username
						if(sname.equals(sendname)){
							sendname = " ";
							System.out.println("开始发送");
							outputToClient.writeUTF("yes");
							
							int length = 0;
					        byte[] sendBytes = null;
					        
			                File file = new File("/Users/wang/Documents/JAVA/cc.jpg");
			                FileInputStream fis = new FileInputStream(file);
			                //sendBytes = new byte[1024];
			                sendBytes=new byte[1024];
			                length = fis.read(sendBytes, 0, sendBytes.length);
			                while (length > 1023) {
			                	//System.out.println(length);
			                	outputToClient.write(sendBytes, 0, length);
			                	outputToClient.flush();
			                	length = fis.read(sendBytes, 0, sendBytes.length);
			                }
			                outputToClient.write(sendBytes, 0, length);
		                	outputToClient.flush();
			                //outputToClient.writeUTF("aaa");
			                //fis.close();
			                //outputToClient.flush();
			                System.out.println("发送完成");
			                
						}
						//yes  picture
						//no
						else outputToClient.writeUTF("no");
					}
					
					else if(order.equals("card")){
						//TODO:
						//to username
						sendname = inputFromClient.readUTF();
						//picture
							byte[] inputByte = null;
					        int length = 0;
							
					        FileOutputStream fos = new 
					        		FileOutputStream(new File("/Users/wang/Documents/JAVA/cc.jpg"));
			                inputByte = new byte[1024];
					        //inputByte = new byte[64];
			                System.out.println("开始接收数据...");
			                length = inputFromClient.read(inputByte, 0, inputByte.length);
			                while (length > 1023) {
			                    //System.out.println(length);
			                    fos.write(inputByte, 0, length);
			                    fos.flush();
			                    length = inputFromClient.read(inputByte, 0, inputByte.length);
			                }
			                fos.write(inputByte, 0, length);
		                    fos.flush();
			                //fos.flush();
			                System.out.println("完成接收");
					}
				}
			}
			catch(IOException e){
				System.err.println(e+"requary");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	class SearchWord{
		private Socket socket;
		private String un;
		
		public SearchWord(Socket socket, String un){
			this.socket = socket;
			this.un=un;
		}

		public void Search(SQLServer sql) throws SQLException {
			try{
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
				DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
				//while(true){
					String word = inputFromClient.readUTF();
					System.out.println(word);
					//对接收到的单词进行处理
					//String result;
					Spider sp= new Spider(word);
					String[] result1=sp.search();
					if(!result1.equals(null) 
							&& !result1[0].equals("NoResult") 
							&& !result1[1].equals("NoResult") 
							&& !result1[2].equals("NoResult") ){
						outputToClient.writeUTF("success");
						outputToClient.writeUTF(result1[0]);
						outputToClient.writeUTF(result1[1]);
						outputToClient.writeUTF(result1[2]);
						sql.sort_word(word,un);
					}
					else
						outputToClient.writeUTF("fail");
					System.out.println(result1);
					jta.append("success!"+'\n');
				//}
			}
			catch(IOException e){
				System.err.println(e);
			}
		}
		
		
	}

}



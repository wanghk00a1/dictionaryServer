import java.awt.BorderLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Server extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea jta = new JTextArea();
	
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
				//ClientNo++;

				
			}
		}
		catch(IOException ex){
			System.err.println(ex);
		}
		
		
	}
	
	class HandleAClient implements Runnable{
		private Socket socket;
		private SQLServer sql;
		public HandleAClient(Socket socket,SQLServer sql){
			this.socket = socket;
			this.sql = sql;
		}
		

		public void run(){
			try{
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
				//DataOutputStream ouputToClient = new DataOutputStream(socket.getOutputStream());
				while(true){
					String order = inputFromClient.readUTF();
					if(order.equals("register")){
						String result;
						try {
							result = sql.add_new_user();
							if(!result.equals("fail")) System.out.println(result);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					
					else if(order.equals("login")){
						try {
							sql.user_login();
						} catch (SQLException e) {
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
						SearchWord search = new SearchWord(socket);
						try {
							search.Search(sql);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					else if(order.equals("like")){
						//TODO:
					}
					
					else if(order.equals("card")){
						//TODO:
					}
				}
			}
			catch(IOException e){
				System.err.println(e);
			}
		}
	}
	
	class SearchWord{
		private Socket socket;
		
		public SearchWord(Socket socket){
			this.socket = socket;
		}

		public void Search(SQLServer sql) throws SQLException {
			try{
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
				DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
				//while(true){
					String word = inputFromClient.readUTF();
					
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
						sql.sort_word(word);
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



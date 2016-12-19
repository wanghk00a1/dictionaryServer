import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLServer {
	Statement statement;
	static int numOfClient;
	Socket socket;
	String user_name;
	
	public SQLServer(Socket socket) 
			throws ClassNotFoundException, SQLException{
		
		Class.forName("com.mysql.jdbc.Driver");
		System.out.println("Driver loaded");
		
		Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost/dictUser?useUnicode=true&characterEncoding=utf-8&useSSL=false", "root", "wanghk00a1");
		System.out.println("Database connected");
		
		statement = connection.createStatement();
		
		numOfClient=0;
		
		this.socket=socket;
	}
	
	String sel(String password) throws NoSuchAlgorithmException{
		MessageDigest alga = MessageDigest.getInstance("MD5");
		alga.update(password.getBytes());
		byte[] digesta = null;
		digesta = alga.digest();
		String rs = byte2hex(digesta);
		return rs;
	}
	
	static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}
	
	public String add_new_user() throws SQLException, IOException, NoSuchAlgorithmException{
		
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		String name = inputFromClient.readUTF();
		String password = inputFromClient.readUTF();
		
		String new_password = sel(password);
		
		String sql_order = "select UNAME from DictUsers where UNAME='" + name +"'";
		ResultSet resultset = statement.executeQuery(sql_order);
		if(resultset.next()) {		//
			outputToClient.writeUTF("fail");
			return "fail";
		}
		
		else{
			
			int UID=numOfClient+1000;
			
			//insert into user table for new user
			sql_order = "insert into DictUsers values ("+ UID +", '"+name+"', '"+new_password+"', 0)";
			System.out.println(sql_order);
					//may not correct for ''
			int result = statement.executeUpdate(sql_order);
			if(result!=-1) 
				System.out.println(name+" datbase inserted successfully");
			
			//create new table for new user
			sql_order = "create table " + name +" (WORD char(50), YOUDAO int, BAIDU int, HAICI int)";
			result = statement.executeUpdate(sql_order);
			if(result!=-1) 
				System.out.println(name+" datbase created successfully");
			
			
			numOfClient++;
			outputToClient.writeUTF("success");
			return name;
		}
	}
	
	public String user_login() 
			throws SQLException, IOException, NoSuchAlgorithmException{
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		String name = inputFromClient.readUTF();
		String password = inputFromClient.readUTF();
		
		String new_password = sel(password);
		
		
		this.user_name=name;
		System.out.println(user_name);
		
		String sql_order = "select UPASSWORD from DictUsers where UNAME='" + name +"'";
		ResultSet resultset = statement.executeQuery(sql_order);
		
		if(!resultset.next())
			outputToClient.writeUTF("fail1");
		else if(resultset.getString(1).equals(new_password)) {
			sql_order = "update DictUsers set UONLINE=1 where UNAME='" + name +"'"; 
			int result = statement.executeUpdate(sql_order);
			if(result>0) {
				//TODO:增加向所有用户发送在线信息。
				outputToClient.writeUTF("success");

				System.out.println("log in successful");
			}
		}
		else outputToClient.writeUTF("fail2");
		return name;
	}
	
	public void user_logout() 
			throws IOException, SQLException{
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		String name = inputFromClient.readUTF();
		
		
		String sql_order = "update DictUsers set UONLINE=0 where UNAME='" + name +"'"; 
		System.out.println("log out success");
		int result = statement.executeUpdate(sql_order);
		if(result>0) {
			//TODO:增加向所有用户发送离线信息。
			outputToClient.writeUTF("success");
		}
		else outputToClient.writeUTF("fail");
	}
	
	public void sort_word(String word, String un) 
			throws IOException, SQLException{
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		
		String sql_order = "select * from "+un+" where WORD='"+word+"'";
		ResultSet resultset = statement.executeQuery(sql_order);
		
		if(!resultset.next())
			outputToClient.writeUTF("123");
		else{
			StringBuffer pre = new StringBuffer();
			int a1,a2,a3;
			a1=Integer.parseInt(resultset.getString(2));
			a2=Integer.parseInt(resultset.getString(3));
			a3=Integer.parseInt(resultset.getString(4));
			if(a1>a2)
				if(a1>a3)
					if(a2>a3) pre.append("123");
					else pre.append("132");
				else pre.append("231");
			else
				if(a1>a3) pre.append("213");
				else 
					if(a2>a3) pre.append("312");
					else pre.append("321");
			String pre_str = pre.toString();
			outputToClient.writeUTF(pre_str);
		}
	}
	
	public void like_word() throws IOException, SQLException{
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		//new DataOutputStream(socket.getOutputStream());
		String word = inputFromClient.readUTF();
		String pre = inputFromClient.readUTF();
		
		String sql_order = "select * from "+user_name+" where WORD='"+word+"'";
		ResultSet resultset = statement.executeQuery(sql_order);
		if(resultset.next()){
			sql_order = "delete from "+user_name+" where WORD='"+word+"'";
			statement.executeUpdate(sql_order);
		}
		
		sql_order = "insert into "+user_name+" values ('" + word + "', " + 
				pre.charAt(0) + ", " + pre.charAt(1) + ", " + pre.charAt(2) + ")";
		statement.executeUpdate(sql_order);
		
		outputToClient.writeUTF("success");
	}
	
	public void send_card() throws IOException{
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		FileOutputStream fos = new FileOutputStream(new File("/Users/wang/Documents/JAVA"));
		File file = new File("/Users/wang/Documents/JAVA");
		FileInputStream fis = new FileInputStream(file);
	} 
	
	@SuppressWarnings("null")public String online() throws IOException, SQLException{
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		
		String sql_order = "select UNAME from DictUsers where UONLINE=1";
		ResultSet resultset = statement.executeQuery(sql_order);
		
		StringBuffer online = new StringBuffer();
		int i=1;
		while(resultset.next()){
			System.out.println(resultset.getString(1));
			String uname = resultset.getString(1) + " ";
			System.out.println("uname  "+uname);
			online.append(uname);
			//i++;
		}
		System.out.println("online" + online);
		return online.toString();	
	}
}















import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
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
	
	public String add_new_user() throws SQLException, IOException{
		
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		String name = inputFromClient.readUTF();
		String password = inputFromClient.readUTF();
		
		String sql_order = "select UNAME from DictUser where UNAME='" + name +"'";
		ResultSet resultset = statement.executeQuery(sql_order);
		if(resultset.next()) {		//
			outputToClient.writeUTF("fail");
			return "fail";
		}
		
		else{
			
			int UID=numOfClient+1000;
			
			//insert into user table for new user
			sql_order = "insert into DictUser values ("+ UID +", '"+name+"', '"+password+"', 0)";
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
			throws SQLException, IOException{
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		String name = inputFromClient.readUTF();
		String password = inputFromClient.readUTF();
		
		
		this.user_name=name;
		System.out.println(user_name);
		
		String sql_order = "select UPASSWORD from DictUser where UNAME='" + name +"'";
		ResultSet resultset = statement.executeQuery(sql_order);
		
		if(!resultset.next())
			outputToClient.writeUTF("fail1");
		else if(resultset.getString(1).equals(password)) {
			sql_order = "update DictUser set UONLINE=1 where UNAME='" + name +"'"; 
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
		
		
		String sql_order = "update DictUser set UONLINE=0 where UNAME='" + name +"'"; 
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
	
	public String online() throws IOException, SQLException{
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		
		String sql_order = "select UNAME from DictUser where UONLINE=1";
		ResultSet resultset = statement.executeQuery(sql_order);
		
		StringBuffer online = null;
		int i=1;
		while(resultset.next()){
			online.append(resultset.getString(i)+" ");
			i++;
		}
		
		return online.toString();
		
		
	}
}















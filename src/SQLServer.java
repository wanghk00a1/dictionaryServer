import java.io.DataInputStream;
import java.io.DataOutputStream;
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
			sql_order = "insert into DictUser valuse ("+ UID +", '"+name+"', '"+password+"', 0)"; 
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
	
	public void user_login() 
			throws IOException, SQLException{
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		String name = inputFromClient.readUTF();
		String password = inputFromClient.readUTF();
		
		this.user_name=name;
		
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
			}
		}
		else outputToClient.writeUTF("fail2");
		return;
	}
	
	public void user_logout() 
			throws IOException, SQLException{
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		String name = inputFromClient.readUTF();
		
		String sql_order = "update DictUser set UONLINE=0 where UNAME='" + name +"'"; 
		int result = statement.executeUpdate(sql_order);
		if(result>0) {
			//TODO:增加向所有用户发送离线信息。
			outputToClient.writeUTF("success");
		}
		else outputToClient.writeUTF("fail");
	}
	
	public void sort_word(String word) 
			throws IOException, SQLException{
		DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
		
		String sql_order = "select * from "+user_name+" where WORD='"+word+"'";
		ResultSet resultset = statement.executeQuery(sql_order);
		
		if(!resultset.next())
			outputToClient.writeUTF("000");
		else{
			StringBuffer pre = new StringBuffer();
			pre.append(resultset.getString(2));
			pre.append(resultset.getString(3));
			pre.append(resultset.getString(4));
			String pre_str = pre.toString();
			outputToClient.writeUTF(pre_str);
		}
	}
	
	public void like_word() throws IOException, SQLException{
		DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
		new DataOutputStream(socket.getOutputStream());
		String word = inputFromClient.readUTF();
		String pre = inputFromClient.readUTF();
		
		String sql_order = "select * from "+user_name+" where WORD='"+word+"'";
		ResultSet resultset = statement.executeQuery(sql_order);
		if(resultset.next()){
			sql_order = "delete from "+user_name+" where WORD='"+word+"'";
			statement.executeUpdate(sql_order);
		}
		
		sql_order = "insert into "+user_name+" values ('" + word + "', " + pre.charAt(0) + ", " + pre.charAt(1) + ", " + pre.charAt(2) + ")";
		statement.executeUpdate(sql_order);
		
	}
}















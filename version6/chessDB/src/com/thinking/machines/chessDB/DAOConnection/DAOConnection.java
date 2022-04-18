package com.thinking.machines.chessDB.DAOConnection;
import java.sql.*;

public class DAOConnection
{
private static Connection connection;
private DAOConnection(){};
public static Connection getConnection()
{
try
{
Class.forName("com.mysql.cj.jdbc.Driver");
connection=DriverManager.getConnection("jdbc:mysql://localhost:3306/chessdb","chessUser","chessUser");
return connection;
}catch(Exception e)
{
System.out.println(e.getMessage());
return connection;
}
}

}
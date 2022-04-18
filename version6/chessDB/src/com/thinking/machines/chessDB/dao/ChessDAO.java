package com.thinking.machines.chessDB.dao;
import com.thinking.machines.chessDB.dto.*;
import com.thinking.machines.chessDB.DAOConnection.*;
import java.sql.*;

public class ChessDAO
{
public ChessDTO getByUserName(String userName)
{
try
{
ChessDTO chessDTO=null;
Connection c=DAOConnection.getConnection();
PreparedStatement preparedStatement=c.prepareStatement("select * from chess where user_name=?");
preparedStatement.setString(1,userName);
ResultSet rs=preparedStatement.executeQuery();
boolean m=rs.next();
if(m==false) return chessDTO;
chessDTO=new ChessDTO();
chessDTO.setUserName(rs.getString("user_name").trim());
chessDTO.setPassword(rs.getString("password").trim());
rs.close();
preparedStatement.close();
c.close();
return chessDTO;
}catch(Exception e)
{
System.out.println(e.getMessage());
return null;
}

}
}
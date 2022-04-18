import com.thinking.machines.chessUtil.*;
import java.net.*;
import java.io.*;
import java.util.*;


class RequestProcessor extends Thread
{
private Socket socket;
private ChessServer chessServer;
RequestProcessor(Socket socket,ChessServer chessServer)
{
this.socket=socket;
this.chessServer=chessServer;
start();
}

public void run()
{
try
{
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
int bytesToReceive=1024;
byte header[]=new byte[1024];
byte tmp[]=new byte[1024];
int i,j,k;
j=0;
i=0;
int bytesReadCount;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
header[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}
int requestLength=0;
i=1;
j=1023;
while(j>=0)
{
requestLength=requestLength+(header[j]*i);
j--;
i=i*10;
}
byte ack[]=new byte[1];
ack[0]=1;
os.write(ack,0,1);
os.flush();
bytesToReceive=requestLength;
byte request[]=new byte[requestLength];
i=0;
j=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
request[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}
ByteArrayInputStream bais=new ByteArrayInputStream(request);
ObjectInputStream ois=new ObjectInputStream(bais);
ChessData s=(ChessData) ois.readObject();

ChessData ch=new ChessData();

if(s.operation.equals("set")) // for setting detail array vv imp
{
this.chessServer.setServerDS(s.details);
this.chessServer.DSPlayer=s.userName;
}
else if(s.operation.equals("get"))
{
if(s.userName.equals(this.chessServer.DSPlayer)==false)
{
ch.details=this.chessServer.getServerDS();
this.chessServer.setServerDS(null);
this.chessServer.DSPlayer="";
}
}
else if(s.operation.equals("setUser"))
{
for(String g:s.list) 
{
ch.isMalformed=this.chessServer.setUser(g);
}
}
else if(s.operation.equals("getUser"))
{
ch.list=this.chessServer.getUserList();
}
else if(s.operation.equals("setInvitation"))
{
System.out.println("set invi");
this.chessServer.setInvitation(s.userName,s.invitationFor);
}
else if(s.operation.equals("getInvitation"))
{
String m=this.chessServer.getInvitation(s.userName);
if(m.equals("")==false) ch.invitationFor=m;
}
else if(s.operation.equals("removeInvitation"))
{
System.out.println("remove invi");
this.chessServer.removeInvitation(s.userName);
}
else if(s.operation.equals("confirmInvitation"))
{
System.out.println("confirm invi");
this.chessServer.confirmInvitation(s.userName);
}
else if(s.operation.equals("isInvitationAccepted"))
{
System.out.println("is invi accepted");
ch.isInvitationAccepted=this.chessServer.isInvitationAccepted(s.userName);
ch.invitationDataExists=this.chessServer.invitationDataExists(s.userName);
}
ByteArrayOutputStream baos=new ByteArrayOutputStream();
ObjectOutputStream oos=new ObjectOutputStream(baos);
oos.writeObject(ch);
oos.flush();
byte objectBytes[]=baos.toByteArray();
int responseLength=objectBytes.length;
header=new byte[1024];
int x=responseLength;
j=1023;
while(x>0)
{
header[j]=(byte)(x%10);
x=x/10;
j--;
}
os.write(header,0,1024);
os.flush();
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}
int bytesToSend=responseLength;
int chunkSize=1024;
j=0;
while(j<bytesToSend)
{
if((bytesToSend-j)<chunkSize) chunkSize=bytesToSend-j;
os.write(objectBytes,j,chunkSize);
os.flush();
j=j+chunkSize;
}
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}
socket.close();

}catch(Exception e)
{
System.out.println(e);
}
}
}

class Pair<T1,T2>
{
public T1 first;
public T2 second;
public Pair(T1 first,T2 second)
{
this.first=first;
this.second=second;
}
}

class ChessServer
{
private ServerSocket serverSocket;
private Details details[][];
private List<String> list=new ArrayList<>();
private Map<String,Pair<String,Boolean>> invitations=new HashMap<>();
private String player1="";
private String player2="";
public String DSPlayer="";

public ChessServer()
{
try
{
serverSocket=new ServerSocket(5500);
startListening();
}catch(Exception e)
{
System.out.println(e);
}
}

synchronized public void setInvitation(String userName,String invitationFor)
{
if(this.invitations.containsKey(invitationFor)) return;
this.invitations.put(invitationFor,new Pair<String,Boolean>(userName,false));
}

synchronized public String getInvitation(String userName)
{
if(this.invitations.containsKey(userName)==false) return "";
Pair<String,Boolean> p=this.invitations.get(userName);
if(p.second==true) return "";
return p.first;
}

synchronized public void confirmInvitation(String userName)
{
Pair<String,Boolean> p=this.invitations.get(userName);
if(p==null) return;
p.second=true;
this.invitations.put(userName,p);
}

synchronized public void removeInvitation(String userName)
{
this.invitations.remove(userName);
}

synchronized public boolean setUser(String user)
{
for(String m:this.list) if(m.equalsIgnoreCase(user)) return true;
this.list.add(user);
return false;
}

synchronized public boolean isInvitationAccepted(String userName)
{
String g;
for(Map.Entry<String,Pair<String,Boolean>> et:this.invitations.entrySet())
{
g=et.getKey();
Pair<String,Boolean> p=et.getValue();
if(p.first.equalsIgnoreCase(userName) && p.second==true) 
{
this.invitations.remove(g);
this.player1=p.first;
this.player2=g;
list.remove(this.player1);
list.remove(this.player2);
System.out.println(player1+"-----"+player2);
return true;
}
}
return false;
}

synchronized public boolean invitationDataExists(String userName)
{
for(Map.Entry<String,Pair<String,Boolean>> et:this.invitations.entrySet())
{
Pair<String,Boolean> p=et.getValue();
if(p.first.equalsIgnoreCase(userName)) return true;
}
return false;
}
synchronized public List getUserList()
{
return this.list;
}

synchronized public void setServerDS(Details details[][])
{
this.details=details;
}

synchronized public Details[][] getServerDS()
{
return this.details;
}

public void startListening()
{
System.out.println("server is ready to accept request at port No. 5500 :");
try
{
Socket socket;
RequestProcessor requestProcessor;
while(true)
{
socket=serverSocket.accept();
requestProcessor=new RequestProcessor(socket,this);
}

}catch(Exception e)
{
System.out.println(e);
}
}

public static void main(String gg[])
{
ChessServer chessServer=new ChessServer();
}
}
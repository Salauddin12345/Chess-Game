import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;
import com.thinking.machines.chessUtil.*;
import com.thinking.machines.chessDB.dao.*;
import com.thinking.machines.chessDB.dto.*;


class Chess extends JFrame implements ActionListener
{
private Container container;
private Details engine[][]=new Details[8][8];
private JButton buttons[][]=new JButton[8][8];
private JButton sourceAddress;
private JButton targetAddress;
private Color color;
private JPanel leftPanel;
private JLayeredPane rightPanel;
private JPanel activeUserPanel;
private JList list;
private javax.swing.Timer timer;
private boolean turn;
private int flag;
private String userName;
private String mainPlayer;
private javax.swing.Timer timer1=null;
private javax.swing.Timer timer2=null;
private javax.swing.Timer timer3=null;
private javax.swing.Timer mainTimer=null;
public Chess(String userName,String password)
{
super(userName);
this.mainPlayer="";
this.userName=userName;
ChessDAO chessDAO=new ChessDAO();
ChessDTO chessDTO=chessDAO.getByUserName(userName);
if(chessDTO==null) 
{
System.out.println("invalid user name");
return;
}
if(chessDTO.getPassword().equals(password)) System.out.println("password matched");
else
{
System.out.println("invalid password");
return;
}
this.container=getContentPane();
ChessData ch=new ChessData();
ch.operation="setUser";
java.util.List<String> l=new java.util.ArrayList<>();
l.add(userName);
ch.list=l;
ch=sendDataOverTheNetwork(ch);
if(ch.isMalformed==true) return;
generateGameFrame();
setSize(652,468);
setLocation(10,50);
setVisible(true);
setDefaultCloseOperation(EXIT_ON_CLOSE);
}


public ChessData sendDataOverTheNetwork(ChessData chessData)
{
try
{
ByteArrayOutputStream baos=new ByteArrayOutputStream();
ObjectOutputStream oos=new ObjectOutputStream(baos);
oos.writeObject(chessData);
oos.flush();
byte objectBytes[];
objectBytes=baos.toByteArray();
int requestLength=objectBytes.length;
byte header[]=new byte[1024];
int i,x;
int k;
x=requestLength;
i=1023;
while(x>0)
{
header[i]=(byte)(x%10);
x=x/10;
i--;
}
Socket socket=new Socket("localhost",5500);
OutputStream os=socket.getOutputStream();
os.write(header,0,1024);
os.flush();
InputStream is=socket.getInputStream();
byte ack[]=new byte[1];
int bytesReadCount;
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}
int bytesToSend=requestLength;
int chunkSize=1024;
int j;
j=0;
while(j<bytesToSend)
{
if((bytesToSend-j)<chunkSize) chunkSize=bytesToSend-j;
os.write(objectBytes,j,chunkSize);
os.flush();
j=j+chunkSize;
}
int bytesToReceive=1024;
j=0;
i=0;
byte tmp[]=new byte[1024];
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
int responseLength=0;
i=1;
j=1023;
while(j>=0)
{
responseLength=responseLength+(header[j]*i);
j--;
i=i*10;
}
ack[0]=1;
os.write(ack,0,1);
os.flush();
byte response[]=new byte[responseLength];
bytesToReceive=responseLength;
j=0;
i=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;

for(k=0;k<bytesReadCount;k++)
{
response[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}
ack[0]=1;
os.write(ack,0,1);
os.flush();
socket.close();
ByteArrayInputStream bais=new ByteArrayInputStream(response);
ObjectInputStream ois=new ObjectInputStream(bais);
ChessData ch=(ChessData)ois.readObject();
return ch;
}catch(Exception exception)
{
System.out.println(exception.getMessage());
return null;
}
}

public void adjustEngine()
{
int e,f;
for(e=0;e<8;e++)
{
for(f=0;f<8;f++)
{
engine[e][f].moves.clear();
if(engine[e][f].pieceName.equals("pawn")) //pawn's condition starts here
{
if(engine[e][f].pieceColor.equals("black"))
{
if(this.userName.equals(this.mainPlayer)==false)
{
if(e==6)
{
if(e-1>=0 && engine[e-1][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e-1,f));
if(e-2>=0 && engine[e-2][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e-2,f)); 
}
else
{
if(e-1>=0 && engine[e-1][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e-1,f));
}
if(e-1>=0 && f-1>=0 && engine[e-1][f-1].pieceColor.equals("white") && engine[e-1][f-1].pieceName.equals("king")==false ) engine[e][f].moves.add(new Pair(e-1,f-1));
if(e-1>=0 && f+1<=7 && engine[e-1][f+1].pieceColor.equals("white") && engine[e-1][f+1].pieceName.equals("king")==false ) engine[e][f].moves.add(new Pair(e-1,f+1));
}
else
{
if(e==1)
{
if(e+1<=7 && engine[e+1][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e+1,f));
if(e+2<=7 && engine[e+2][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e+2,f)); 
}
else
{
if(e+1<=7 && engine[e+1][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e+1,f));
}
if(e+1<=7 && f-1>=0 && engine[e+1][f-1].pieceColor.equals("white") && engine[e+1][f-1].pieceName.equals("king")==false) engine[e][f].moves.add(new Pair(e+1,f-1));
if(e+1<=7 && f+1<=7 && engine[e+1][f+1].pieceColor.equals("white") && engine[e+1][f+1].pieceName.equals("king")==false) engine[e][f].moves.add(new Pair(e+1,f+1));
}
} // black pawn
else
{
if(this.userName.equals(this.mainPlayer)==false)
{
if(e==1)
{
if(e+1<=7 && engine[e+1][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e+1,f));
if(e+2<=7 && engine[e+2][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e+2,f)); 
}
else
{
if(e+1<=7 && engine[e+1][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e+1,f));
}
if(e+1<=7 && f-1>=0 && engine[e+1][f-1].pieceColor.equals("black") && engine[e+1][f-1].pieceName.equals("king")==false) engine[e][f].moves.add(new Pair(e+1,f-1));
if(e+1<=7 && f+1<=7 && engine[e+1][f+1].pieceColor.equals("black") && engine[e+1][f+1].pieceName.equals("king")==false) engine[e][f].moves.add(new Pair(e+1,f+1));
}
else
{
if(e==6)
{
if(e-1>=0 && engine[e-1][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e-1,f));
if(e-2>=0 && engine[e-2][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e-2,f)); 
}
else
{
if(e-1>=0 && engine[e-1][f].pieceName.length()==0) engine[e][f].moves.add(new Pair(e-1,f));
}
if(e-1>=0 && f-1>=0 && engine[e-1][f-1].pieceColor.equals("black") && engine[e-1][f-1].pieceName.equals("king")==false ) engine[e][f].moves.add(new Pair(e-1,f-1));
if(e-1>=0 && f+1<=7 && engine[e-1][f+1].pieceColor.equals("black") && engine[e-1][f+1].pieceName.equals("king")==false ) engine[e][f].moves.add(new Pair(e-1,f+1));
}
} // white pawn
} // pawn's condition ends here

if(this.engine[e][f].pieceName.equals("rook"))
{
int i;
if(this.engine[e][f].pieceColor.equals("black"))
{
for(i=1;e-i>=0 && (this.engine[e-i][f].pieceName.length()==0 || this.engine[e-i][f].pieceColor.equals("white")) && this.engine[e-i][f].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e-i,f));
if(this.engine[e-i][f].pieceColor.equals("white")) break;
}
for(i=1;e+i<=7 && (this.engine[e+i][f].pieceName.length()==0 || this.engine[e+i][f].pieceColor.equals("white")) && this.engine[e+i][f].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e+i,f));
if(this.engine[e+i][f].pieceColor.equals("white")) break;
}
for(i=1;f-i>=0 && (this.engine[e][f-i].pieceName.length()==0 || this.engine[e][f-i].pieceColor.equals("white")) && this.engine[e][f-i].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e,f-i));
if(this.engine[e][f-i].pieceColor.equals("white")) break;
}
for(i=1;f+i<=7 && (this.engine[e][f+i].pieceName.length()==0 || this.engine[e][f+i].pieceColor.equals("white")) && this.engine[e][f+i].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e,f+i));
if(this.engine[e][f+i].pieceColor.equals("white")) break;
}
} // black rook
else
{
for(i=1;e-i>=0 && (this.engine[e-i][f].pieceName.length()==0 || this.engine[e-i][f].pieceColor.equals("black")) && this.engine[e-i][f].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e-i,f));
if(this.engine[e-i][f].pieceColor.equals("black")) break;
}
for(i=1;e+i<=7 && (this.engine[e+i][f].pieceName.length()==0 || this.engine[e+i][f].pieceColor.equals("black")) && this.engine[e+i][f].pieceName.equals("king")==false ;i++)
{
this.engine[e][f].moves.add(new Pair(e+i,f));
if(this.engine[e+i][f].pieceColor.equals("black")) break;
}
for(i=1;f-i>=0 && (this.engine[e][f-i].pieceName.length()==0 || this.engine[e][f-i].pieceColor.equals("black")) && this.engine[e][f-i].pieceName.equals("king")==false ;i++)
{
this.engine[e][f].moves.add(new Pair(e,f-i));
if(this.engine[e][f-i].pieceColor.equals("black")) break;
}
for(i=1;f+i<=7 && (this.engine[e][f+i].pieceName.length()==0 || this.engine[e][f+i].pieceColor.equals("black")) && this.engine[e][f+i].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e,f+i));
if(this.engine[e][f+i].pieceColor.equals("black")) break;
}
} // white rook
} // rook's condition ends here

if(this.engine[e][f].pieceName.equals("knight"))
{
if(this.engine[e][f].pieceColor.equals("black"))
{
if((e-1>=0 && f-2>=0) && (this.engine[e-1][f-2].pieceName.length()==0 || this.engine[e-1][f-2].pieceColor.equals("white")) && this.engine[e-1][f-2].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-1,f-2));
if((e-1>=0 && f+2<=7) && (this.engine[e-1][f+2].pieceName.length()==0 || this.engine[e-1][f+2].pieceColor.equals("white")) && this.engine[e-1][f+2].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-1,f+2));
if((e+1<=7 && f-2>=0) && (this.engine[e+1][f-2].pieceName.length()==0 || this.engine[e+1][f-2].pieceColor.equals("white")) && this.engine[e+1][f-2].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+1,f-2));
if((e+1<=7 && f+2<=7) && (this.engine[e+1][f+2].pieceName.length()==0 || this.engine[e+1][f+2].pieceColor.equals("white")) && this.engine[e+1][f+2].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+1,f+2));

if((e-2>=0 && f-1>=0) && (this.engine[e-2][f-1].pieceName.length()==0 || this.engine[e-2][f-1].pieceColor.equals("white")) && this.engine[e-2][f-1].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-2,f-1));
if((e+2<=7 && f-1>=0) && (this.engine[e+2][f-1].pieceName.length()==0 || this.engine[e+2][f-1].pieceColor.equals("white")) && this.engine[e+2][f-1].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+2,f-1));
if((e-2>=0 && f+1<=7) && (this.engine[e-2][f+1].pieceName.length()==0 || this.engine[e-2][f+1].pieceColor.equals("white")) && this.engine[e-2][f+1].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-2,f+1));
if((e+2<=7 && f+1<=7) && (this.engine[e+2][f+1].pieceName.length()==0 || this.engine[e+2][f+1].pieceColor.equals("white")) && this.engine[e+2][f+1].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+2,f+1));
} // black knight
else
{
if((e-1>=0 && f-2>=0) && (this.engine[e-1][f-2].pieceName.length()==0 || this.engine[e-1][f-2].pieceColor.equals("black")) && this.engine[e-1][f-2].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-1,f-2));
if((e-1>=0 && f+2<=7) && (this.engine[e-1][f+2].pieceName.length()==0 || this.engine[e-1][f+2].pieceColor.equals("black")) && this.engine[e-1][f+2].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-1,f+2));
if((e+1<=7 && f-2>=0) && (this.engine[e+1][f-2].pieceName.length()==0 || this.engine[e+1][f-2].pieceColor.equals("black")) && this.engine[e+1][f-2].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+1,f-2));
if((e+1<=7 && f+2<=7) && (this.engine[e+1][f+2].pieceName.length()==0 || this.engine[e+1][f+2].pieceColor.equals("black")) && this.engine[e+1][f+2].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+1,f+2));

if((e-2>=0 && f-1>=0) && (this.engine[e-2][f-1].pieceName.length()==0 || this.engine[e-2][f-1].pieceColor.equals("black")) && this.engine[e-2][f-1].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-2,f-1));
if((e+2<=7 && f-1>=0) && (this.engine[e+2][f-1].pieceName.length()==0 || this.engine[e+2][f-1].pieceColor.equals("black")) && this.engine[e+2][f-1].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+2,f-1));
if((e-2>=0 && f+1<=7) && (this.engine[e-2][f+1].pieceName.length()==0 || this.engine[e-2][f+1].pieceColor.equals("black")) && this.engine[e-2][f+1].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-2,f+1));
if((e+2<=7 && f+1<=7) && (this.engine[e+2][f+1].pieceName.length()==0 || this.engine[e+2][f+1].pieceColor.equals("black")) && this.engine[e+2][f+1].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+2,f+1));
} // white knight;
} // knight's condition ends here

if(this.engine[e][f].pieceName.equals("bishop"))
{
if(this.engine[e][f].pieceColor.equals("black"))
{
int i;
for(i=1;(e-i>=0 && f+i<=7) && this.engine[e-i][f+i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e-i,f+i));
if((e-i>=0 && f+i<=7) && this.engine[e-i][f+i].pieceColor.equals("white") && this.engine[e-i][f+i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-i,f+i));
for(i=1;(e+i<=7 && f-i>=0) && this.engine[e+i][f-i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e+i,f-i));
if((e+i<=7 && f-i>=0) && this.engine[e+i][f-i].pieceColor.equals("white") && this.engine[e+i][f-i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+i,f-i));
for(i=1;(e+i<=7 && f+i<=7) && this.engine[e+i][f+i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e+i,f+i));
if((e+i<=7 && f+i<=7) && this.engine[e+i][f+i].pieceColor.equals("white") && this.engine[e+i][f+i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+i,f+i));
for(i=1;(e-i>=0 && f-i>=0) && this.engine[e-i][f-i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e-i,f-i));
if((e-i>=0 && f-i>=0) && this.engine[e-i][f-i].pieceColor.equals("white") && this.engine[e-i][f-i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-i,f-i));

} // black bishop
else
{
int i;
for(i=1;(e-i>=0 && f+i<=7) && this.engine[e-i][f+i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e-i,f+i));
if((e-i>=0 && f+i<=7) && this.engine[e-i][f+i].pieceColor.equals("black") && this.engine[e-i][f+i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-i,f+i));
for(i=1;(e+i<=7 && f-i>=0) && this.engine[e+i][f-i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e+i,f-i));
if((e+i<=7 && f-i>=0) && this.engine[e+i][f-i].pieceColor.equals("black") && this.engine[e+i][f-i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+i,f-i));
for(i=1;(e+i<=7 && f+i<=7) && this.engine[e+i][f+i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e+i,f+i));
if((e+i<=7 && f+i<=7) && this.engine[e+i][f+i].pieceColor.equals("black") && this.engine[e+i][f+i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+i,f+i));
for(i=1;(e-i>=0 && f-i>=0) && this.engine[e-i][f-i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e-i,f-i));
if((e-i>=0 && f-i>=0) && this.engine[e-i][f-i].pieceColor.equals("black") && this.engine[e-i][f-i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-i,f-i));

} // white bishop
} // bishop's condition ends here

if(this.engine[e][f].pieceName.equals("queen"))
{
if(this.engine[e][f].pieceColor.equals("black"))
{
int i;
for(i=1;(e-i>=0 && f+i<=7) && this.engine[e-i][f+i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e-i,f+i));
if((e-i>=0 && f+i<=7) && this.engine[e-i][f+i].pieceColor.equals("white") && this.engine[e-i][f+i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-i,f+i));
for(i=1;(e+i<=7 && f-i>=0) && this.engine[e+i][f-i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e+i,f-i));
if((e+i<=7 && f-i>=0) && this.engine[e+i][f-i].pieceColor.equals("white") && this.engine[e+i][f-i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+i,f-i));
for(i=1;(e+i<=7 && f+i<=7) && this.engine[e+i][f+i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e+i,f+i));
if((e+i<=7 && f+i<=7) && this.engine[e+i][f+i].pieceColor.equals("white") && this.engine[e+i][f+i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+i,f+i));
for(i=1;(e-i>=0 && f-i>=0) && this.engine[e-i][f-i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e-i,f-i));
if((e-i>=0 && f-i>=0) && this.engine[e-i][f-i].pieceColor.equals("white") && this.engine[e-i][f-i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-i,f-i));

for(i=1;e-i>=0 && (this.engine[e-i][f].pieceName.length()==0 || this.engine[e-i][f].pieceColor.equals("white")) && this.engine[e-i][f].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e-i,f));
if(this.engine[e-i][f].pieceColor.equals("white")) break;
}
for(i=1;e+i<=7 && (this.engine[e+i][f].pieceName.length()==0 || this.engine[e+i][f].pieceColor.equals("white")) && this.engine[e+i][f].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e+i,f));
if(this.engine[e+i][f].pieceColor.equals("white")) break;
}
for(i=1;f-i>=0 && (this.engine[e][f-i].pieceName.length()==0 || this.engine[e][f-i].pieceColor.equals("white")) && this.engine[e][f-i].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e,f-i));
if(this.engine[e][f-i].pieceColor.equals("white")) break;
}
for(i=1;f+i<=7 && (this.engine[e][f+i].pieceName.length()==0 || this.engine[e][f+i].pieceColor.equals("white")) && this.engine[e][f+i].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e,f+i));
if(this.engine[e][f+i].pieceColor.equals("white")) break;
}
} // black queen
else
{
int i;
for(i=1;(e-i>=0 && f+i<=7) && this.engine[e-i][f+i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e-i,f+i));
if((e-i>=0 && f+i<=7) && this.engine[e-i][f+i].pieceColor.equals("black") && this.engine[e-i][f+i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-i,f+i));
for(i=1;(e+i<=7 && f-i>=0) && this.engine[e+i][f-i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e+i,f-i));
if((e+i<=7 && f-i>=0) && this.engine[e+i][f-i].pieceColor.equals("black") && this.engine[e+i][f-i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+i,f-i));
for(i=1;(e+i<=7 && f+i<=7) && this.engine[e+i][f+i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e+i,f+i));
if((e+i<=7 && f+i<=7) && this.engine[e+i][f+i].pieceColor.equals("black") && this.engine[e+i][f+i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e+i,f+i));
for(i=1;(e-i>=0 && f-i>=0) && this.engine[e-i][f-i].pieceName.length()==0;i++) this.engine[e][f].moves.add(new Pair(e-i,f-i));
if((e-i>=0 && f-i>=0) && this.engine[e-i][f-i].pieceColor.equals("black") && this.engine[e-i][f-i].pieceName.equals("king")==false) this.engine[e][f].moves.add(new Pair(e-i,f-i));

for(i=1;e-i>=0 && (this.engine[e-i][f].pieceName.length()==0 || this.engine[e-i][f].pieceColor.equals("black")) && this.engine[e-i][f].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e-i,f));
if(this.engine[e-i][f].pieceColor.equals("black")) break;
}
for(i=1;e+i<=7 && (this.engine[e+i][f].pieceName.length()==0 || this.engine[e+i][f].pieceColor.equals("black")) && this.engine[e+i][f].pieceName.equals("king")==false ;i++)
{
this.engine[e][f].moves.add(new Pair(e+i,f));
if(this.engine[e+i][f].pieceColor.equals("black")) break;
}
for(i=1;f-i>=0 && (this.engine[e][f-i].pieceName.length()==0 || this.engine[e][f-i].pieceColor.equals("black")) && this.engine[e][f-i].pieceName.equals("king")==false ;i++)
{
this.engine[e][f].moves.add(new Pair(e,f-i));
if(this.engine[e][f-i].pieceColor.equals("black")) break;
}
for(i=1;f+i<=7 && (this.engine[e][f+i].pieceName.length()==0 || this.engine[e][f+i].pieceColor.equals("black")) && this.engine[e][f+i].pieceName.equals("king")==false ;i++) 
{
this.engine[e][f].moves.add(new Pair(e,f+i));
if(this.engine[e][f+i].pieceColor.equals("black")) break;
}
} // white queen
} // queen condition ends here



}
}
}

public void drawUsingDS()
{
int i,j;
for(i=0;i<this.engine.length;i++)
{
for(j=0;j<this.engine[i].length;j++)
{
this.buttons[i][j].setIcon(null);
if(this.engine[i][j].pieceColor.equals("black"))
{
if(this.engine[i][j].pieceName.equals("pawn")) this.buttons[i][j].setIcon(new ImageIcon("icons/black_pawn.png"));
if(this.engine[i][j].pieceName.equals("rook")) this.buttons[i][j].setIcon(new ImageIcon("icons/black_rook.png"));
if(this.engine[i][j].pieceName.equals("knight")) this.buttons[i][j].setIcon(new ImageIcon("icons/black_knight.png"));
if(this.engine[i][j].pieceName.equals("bishop")) this.buttons[i][j].setIcon(new ImageIcon("icons/black_bishop.png"));
if(this.engine[i][j].pieceName.equals("queen")) this.buttons[i][j].setIcon(new ImageIcon("icons/black_queen.png"));
if(this.engine[i][j].pieceName.equals("king")) this.buttons[i][j].setIcon(new ImageIcon("icons/black_king.png"));
}
else
{
if(this.engine[i][j].pieceName.equals("pawn")) this.buttons[i][j].setIcon(new ImageIcon("icons/white_pawn.png"));
if(this.engine[i][j].pieceName.equals("rook")) this.buttons[i][j].setIcon(new ImageIcon("icons/white_rook.png"));
if(this.engine[i][j].pieceName.equals("knight")) this.buttons[i][j].setIcon(new ImageIcon("icons/white_knight.png"));
if(this.engine[i][j].pieceName.equals("bishop")) this.buttons[i][j].setIcon(new ImageIcon("icons/white_bishop.png"));
if(this.engine[i][j].pieceName.equals("queen")) this.buttons[i][j].setIcon(new ImageIcon("icons/white_queen.png"));
if(this.engine[i][j].pieceName.equals("king")) this.buttons[i][j].setIcon(new ImageIcon("icons/white_king.png"));
}


}
}



}


public void generateGameFrame()
{
this.container.setLayout(null);
this.leftPanel=new JPanel();
this.leftPanel.setBounds(0,0,430,430);
//this.leftPanel.setBorder(BorderFactory.createLineBorder(Color.black));
this.leftPanel.setLayout(new GridLayout(8,8));

// right panel starting here
this.rightPanel=new JLayeredPane();
this.rightPanel.setLayout(null);
this.rightPanel.setBounds(430,1,205,430);
//this.rightPanel.setBorder(BorderFactory.createLineBorder(Color.green));
JLabel backgroundImg=new JLabel(new ImageIcon("icons/final1.jpg"));
backgroundImg.setBounds(0,0,205,428);
this.rightPanel.add(backgroundImg,new Integer(1));


// working on right panel starts here


JLabel titleLabel=new JLabel("TM Chess");
titleLabel.setBounds(0,2,205,40);
titleLabel.setFont(new Font(Font.SERIF,Font.PLAIN,33));
titleLabel.setForeground(new Color(250, 252, 255)); 
titleLabel.setBorder(BorderFactory.createMatteBorder(0,0,2,0,new Color(250, 252, 255)));
titleLabel.setHorizontalAlignment(JLabel.CENTER);
this.rightPanel.add(titleLabel,new Integer(2));


// active user panel work

activeUserPanel=new JPanel();
activeUserPanel.setBounds(15,190,170,160);
activeUserPanel.setLayout(null);
activeUserPanel.setBackground(new Color(1,41,41));

JLabel l1=new JLabel("Online Users"); 
l1.setBounds(0,0,170,25);
l1.setFont(new Font(Font.SERIF,Font.PLAIN,20));
l1.setForeground(Color.white); 
l1.setBorder(BorderFactory.createMatteBorder(1,0,1,0,Color.white));
l1.setHorizontalAlignment(JLabel.CENTER);

Vector<String> v=new Vector();
list=new JList(v);
list.addMouseListener(new MouseAdapter(){
public void mouseClicked(MouseEvent me)
{
JList list=(JList)me.getSource();
if(me.getClickCount()==2)
{
if(list.getSelectedIndex()>=0)
{
ChessData ch=new ChessData();
ch.operation="setInvitation";
ch.userName=Chess.this.userName;
ch.invitationFor=(String)list.getSelectedValue();
sendDataOverTheNetwork(ch);
timer3.start();
}
}
}
});
list.setBackground(new Color(1,41,41));
list.setForeground(Color.white);
JScrollPane jp=new JScrollPane(list,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
jp.setBounds(0,25,171,136);
activeUserPanel.add(l1);
activeUserPanel.add(jp);
this.rightPanel.add(activeUserPanel,new Integer(2));



// timer for fectching user list from server


timer1=new javax.swing.Timer(2000,new ActionListener(){

public void actionPerformed(ActionEvent ae)
{
ChessData ch=new ChessData();
ch.operation="getUser";
ch=sendDataOverTheNetwork(ch);
Vector<String> v=new Vector<>();
for(String g:ch.list) if(Chess.this.userName.equals(g)==false)v.add(g);
Chess.this.list.setListData(v);
}

});

timer1.start();

timer2=new javax.swing.Timer(2000,new ActionListener(){

public void actionPerformed(ActionEvent ae)
{
ChessData ch=new ChessData();
ch.operation="getInvitation";
ch.userName=Chess.this.userName;
ch=sendDataOverTheNetwork(ch);
String g=ch.invitationFor;
if(g==null) return;
int selectedOption=JOptionPane.showConfirmDialog(Chess.this.activeUserPanel,"Accept Invitation\n Player Id : "+g,"Invitation Detail",JOptionPane.YES_NO_OPTION);
if(selectedOption==JOptionPane.NO_OPTION)
{
ch=new ChessData();
ch.operation="removeInvitation";
ch.userName=Chess.this.userName;
sendDataOverTheNetwork(ch);
}
else
{
ch=new ChessData();
ch.operation="confirmInvitation";
ch.userName=Chess.this.userName;
sendDataOverTheNetwork(ch);
mainTimer.start();
timer3.stop();
timer2.stop();
}
}

});

timer2.start();

timer3=new javax.swing.Timer(2000,new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
System.out.println("timer 3");

ChessData ch=new ChessData();
ch.operation="isInvitationAccepted";
ch.userName=Chess.this.userName;
ch=sendDataOverTheNetwork(ch);
if(ch.invitationDataExists==false && ch.isInvitationAccepted==false) 
{
timer3.stop();
return;
}
if(ch.isInvitationAccepted==true)
{
System.out.println("invitation accepted bhyaaa!!!");
Chess.this.mainPlayer=Chess.this.userName;
initializeDSFor1stPlayer();
drawUsingDS();
adjustEngine();
ChessData icd=new ChessData();   // sending data
icd.details=Chess.this.engine;
icd.userName=userName;
icd.operation="set";
sendDataOverTheNetwork(icd);
Chess.this.turn=true;
timer2.stop();
timer3.stop();
}
}

});




if(this.userName.equals(this.mainPlayer)==true)
{
this.mainTimer=new javax.swing.Timer(1000,new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
System.out.println("main player timer");
ChessData ch=new ChessData();   // receving data
ch.details=Chess.this.engine;
ch.userName=userName;
ch.operation="get";
ch=sendDataOverTheNetwork(ch);
if(ch.details==null) return;
reverseDS(Chess.this.engine,ch.details);
drawUsingDS();
Chess.this.turn=true;
Chess.this.mainTimer.stop();
}
});
}
else
{
this.mainTimer=new javax.swing.Timer(1000,new ActionListener(){
public void actionPerformed(ActionEvent ae)
{
System.out.println("main timer got called");
ChessData ch=new ChessData();   // receving data
ch.details=Chess.this.engine;
ch.userName=userName;
ch.operation="get";
ch=sendDataOverTheNetwork(ch);
if(ch.details==null) return;
reverseDS(Chess.this.engine,ch.details);
drawUsingDS();
if(Chess.this.flag==0) flag=1;
else 
{
Chess.this.turn=true;
Chess.this.mainTimer.stop();
}
}
});

}
// working on right panel ends here

this.container.add(this.leftPanel);
this.container.add(this.rightPanel);




JButton b;
int e,f;
Details details;
for(e=0;e<8;e++)
{
for(f=0;f<8;f++)
{
b=new JButton();
b.addActionListener(this);
this.buttons[e][f]=b;
if(e%2==0)
{
if(f%2==0) b.setBackground(new Color(253,250,227));
else b.setBackground(new Color(206,230,135));
}
else
{
if(f%2==0) b.setBackground(new Color(206,230,135));
else b.setBackground(new Color(253,250,227));
}
this.leftPanel.add(b);
this.revalidate();
this.repaint();
}
}

}


void reverseDS(Details a[][],Details b[][])
{
for(int i=0;i<b.length;i++)
{
for(int j=0;j<b[0].length;j++)
{
a[i][j]=b[7-i][j];
for(Pair p:a[i][j].moves) p.x=7-(p.x);
}
}
}

public void initializeDSFor1stPlayer()
{
System.out.println("initializing DS for player");
int e,f;
Details details;
for(e=0;e<8;e++)
{
for(f=0;f<8;f++)
{
details=new Details();
this.engine[e][f]=details;
if(e==0)
{
if(f==0 || f==7) 
{
details.pieceName="rook";
details.pieceColor="black";
}
if(f==1 || f==6) 
{
details.pieceName="knight";
details.pieceColor="black";
}
if(f==2 || f==5) 
{
details.pieceName="bishop";
details.pieceColor="black";
}
if(f==3) 
{
details.pieceName="queen";
details.pieceColor="black";
}
if(f==4) 
{
details.pieceName="king";
details.pieceColor="black";
}
}
if(e==1) 
{
details.pieceName="pawn";
details.pieceColor="black";
}
if(e==6)
{
details.pieceName="pawn";
details.pieceColor="white";
}
if(e==7)
{
if(f==0 || f==7)
{
details.pieceName="rook";
details.pieceColor="white";
}
if(f==1 || f==6) 
{
details.pieceName="knight";
details.pieceColor="white";
}
if(f==2 || f==5) 
{
details.pieceName="bishop";
details.pieceColor="white";
}
if(f==3) 
{
details.pieceName="queen";
details.pieceColor="white";
}
if(f==4) 
{
details.pieceName="king";
details.pieceColor="white";
}
}



}
}

} // intialize function ends 

public void actionPerformed(ActionEvent ae)
{
if(this.turn==false) return;
if(this.sourceAddress==null)
{
this.sourceAddress=(JButton)ae.getSource();
if(this.sourceAddress.getIcon()==null)
{
this.sourceAddress=null;
return;
}
int m,n;
for(m=0;m<8;m++)
{
for(n=0;n<8;n++)
{
if(buttons[m][n]==sourceAddress) 
{
if(this.mainPlayer.equals(this.userName)==true) 
{
if(engine[m][n].pieceColor.equals("white")==false)
{
this.sourceAddress=null;
return;
}
}
else 
{
if(engine[m][n].pieceColor.equals("black")==false) 
{
this.sourceAddress=null;
return; 
}
}
}
}
}
System.out.println("mark");
this.color=sourceAddress.getBackground();
sourceAddress.setBackground(new Color(97,112,72));
} 
else
{
System.out.println("else");
this.targetAddress=(JButton)ae.getSource();
if(this.sourceAddress==this.targetAddress)
{
this.sourceAddress.setBackground(this.color);
this.sourceAddress=null;
this.targetAddress=null;
this.color=null;
return;
}
boolean result=isMoveValid(sourceAddress,targetAddress);
System.out.println("is move valid : "+result);
if(!result) return;
ImageIcon imageIcon=(ImageIcon)sourceAddress.getIcon();
sourceAddress.setIcon(null);
targetAddress.setIcon(imageIcon);
adjustEngine();
ChessData ch=new ChessData();   // sending data
ch.details=this.engine;
ch.userName=userName;
ch.operation="set";
sendDataOverTheNetwork(ch);
if(this.flag==0)
{

System.out.println("cool");
flag=1;
this.turn=false;
this.mainTimer.start();
}
else
{
this.turn=false;
this.mainTimer.restart();
}
sourceAddress.setBackground(this.color);
this.sourceAddress=null;
this.targetAddress=null;
}

} // action performed ends here 

private boolean isMoveValid(JButton source,JButton target)
{
int e,f,x1=0,y1=0,x2=0,y2=0;
for(e=0;e<8;e++)
{
for(f=0;f<8;f++)
{
if(buttons[e][f]==source) 
{
x1=e;
y1=f;
}
if(buttons[e][f]==target)
{
x2=e;
y2=f;
}
}
}
System.out.println("("+x1+","+y1+")---("+x2+","+y2+")");
System.out.println("size : "+engine[x1][y1].moves.size());
for(Pair p:engine[x1][y1].moves) 
{
System.out.println("move : "+p.x+","+p.y);
if(p.x==x2 && p.y==y2) 
{
engine[x2][y2].pieceName=engine[x1][y1].pieceName;
engine[x2][y2].pieceColor=engine[x1][y1].pieceColor;
engine[x1][y1].pieceName="";
engine[x1][y1].pieceColor="";
return true;
}
}
return false;
} // isMoveValid function ends here

}


class ChessClient
{
public static void main(String gg[])
{
Chess chess=new Chess(gg[0],gg[1]);
}
}
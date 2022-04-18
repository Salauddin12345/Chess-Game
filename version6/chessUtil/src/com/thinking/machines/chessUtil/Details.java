package com.thinking.machines.chessUtil;
import java.util.*;

public class Details implements java.io.Serializable
{
public String pieceName;
public String pieceColor;
public LinkedList<Pair> moves=new LinkedList<>();
public Details()
{
this.pieceName="";
this.pieceColor="";
}
}


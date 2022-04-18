package com.thinking.machines.chessUtil;
import java.util.*;

public class ChessData implements java.io.Serializable
{
public Details details[][];
public String userName;
public String invitationFor;
public String operation;
public List<String> list;
public boolean isMalformed=false;
public boolean isInvitationAccepted=false;
public boolean invitationDataExists=false;
}
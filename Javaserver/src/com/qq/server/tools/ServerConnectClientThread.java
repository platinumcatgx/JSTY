/*
 * 线程类，用于服务器与客户端的持续沟通
 */

package com.qq.server.tools;

import java.net.*;
import java.util.*;
import java.io.*;

import com.qq.common.*;


public class ServerConnectClientThread extends Thread{
	Socket s;
	
	public ServerConnectClientThread(Socket s){
		//把服务器与该客户端的连接赋给s
		this.s = s;
	}
	
	//在该线程中通知其他用户
	public void NoticeOther(String iam){
		//得到所有在线的线程
		HashMap hm = ManageServerConClientThread.hm;
		Iterator it = hm.keySet().iterator();
		
		while(it.hasNext()){
			//取出在线人的QQ号
			Message ms = new Message();
			ms.setCon(iam);//说的内容是：我上线
			ms.setMesType(MessageType.message_receive_OnlineFriend);
			String OnlineUserId = it.next().toString();
			try {
				ObjectOutputStream oos = new ObjectOutputStream(ManageServerConClientThread.getClientThread(OnlineUserId).s.getOutputStream());
				ms.setGetter(OnlineUserId);//给别人说
				oos.writeObject(ms);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void run(){
		while(true){
			
			try {
				//这里接收客户端发来的信息
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				Message ms = (Message)ois.readObject();
				
				if(ms.getMesType().equals(MessageType.message_common_message)){
					System.out.println(ms.getSender()+"给"+ms.getGetter()+"说"+ms.getCon());

					//将消息转发，所以得拿到要转发的getter的socket
					ServerConnectClientThread sr = ManageServerConClientThread.getClientThread(ms.getGetter());
					ObjectOutputStream oos = new ObjectOutputStream(sr.s.getOutputStream());//注意这里得写你拿到的getter的socket
					oos.writeObject(ms);
				}else if(ms.getMesType().equals(MessageType.message_get_OnlineFriend)){
					//把在服务器的好友返回
					String res = ManageServerConClientThread.GetOnlineUserId();
					Message ms_User = new Message();
					ms_User.setMesType(MessageType.message_receive_OnlineFriend);
					ms_User.setCon(res);
					ms_User.setGetter(ms.getSender());
					ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
					oos.writeObject(ms_User);
					
				}
				

				
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
}

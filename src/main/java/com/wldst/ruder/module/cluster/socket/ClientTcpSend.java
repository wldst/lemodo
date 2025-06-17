package com.wldst.ruder.module.cluster.socket;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientTcpSend {

    public static void main(String[] args) {
    	String infoSrcIP = "127.0.0.1";
		int tcpIP = 33456;
    	
        int length = 0;
        byte[] sendByte = null;
        Socket socket = null;
        DataOutputStream dout = null;
        FileInputStream fin = null;
        try {
          try {
            socket = new Socket();
            
			socket.connect(new InetSocketAddress(infoSrcIP, tcpIP),10 * 1000);
            dout = new DataOutputStream(socket.getOutputStream());
            File file = new File("E:\\TU\\DSCF0320.JPG");
            fin = new FileInputStream(file);
            sendByte = new byte[1024];
            dout.writeUTF(file.getName());
            while((length = fin.read(sendByte, 0, sendByte.length))>0){
                dout.write(sendByte,0,length);
                dout.flush();
            }
            } catch (Exception e) {

            } finally{
                if (dout != null)
                    dout.close();
                if (fin != null)
                    fin.close();
                if (socket != null)
                    socket.close();
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

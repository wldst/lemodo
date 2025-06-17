package com.wldst.ruder.util;
import java.io.*;
import java.net.*;

public class ServerSendFile {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java ServerSendFile <server port> <filename>");
            return;
        }

        String serverPortStr = args[0];
        String filename = args[1];

        int port = Integer.parseInt(serverPortStr);
        sendFile(filename,"loaclhost", port);
    }

    public static void sendFile(String filename,String host, int port) {
        File fileToServer = new File(filename);

        try (Socket socket = new Socket(host, port)) {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(fileToServer);

            byte[] bytesToSend = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(bytesToSend)) != -1) {
                dos.write(bytesToSend, 0, bytesRead);
            }

            System.out.println("File sent successfully.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
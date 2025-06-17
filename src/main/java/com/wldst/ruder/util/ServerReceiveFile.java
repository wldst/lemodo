package com.wldst.ruder.util;
import java.io.*;
import java.net.*;

public class ServerReceiveFile {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java ServerReceiveFile <server port>");
            return;
        }

        String serverPortStr = args[0];

        int port = Integer.parseInt(serverPortStr);
        fileRecive(port);
    }

    public static void fileRecive(int port) {
	try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket clientSocket = serverSocket.accept();

            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            FileOutputStream fos = new FileOutputStream("receivedFile");

            byte[] bytesToReceive = new byte[1024];
            int bytesRead;

            while ((bytesRead = dis.read(bytesToReceive)) != -1) {
                fos.write(bytesToReceive, 0, bytesRead);
            }

            System.out.println("File received successfully.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
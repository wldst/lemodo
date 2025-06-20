package com.wldst.ruder.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
 
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
 
public class QRCodeGenerator {
    
    private static final String QR_CODE_IMAGE_PATH = "D:\\liuqiang\\download\\MyQRCode.png";
    
    private static void generateQRCodeImage(String text, int width, int height, String filePath) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        
        Path path = FileSystems.getDefault().getPath(filePath);
        
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        
    }
    
    public static void main(String[] args) {
        try {
            generateQRCodeImage("http://192.168.0.23:9000/pmis/bsp/menu.cmd", 350, 350, QR_CODE_IMAGE_PATH);
        } catch (WriterException e) {
            System.out.println("Could not generate QR Code, WriterException :: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Could not generate QR Code, IOException :: " + e.getMessage());
        }
        
    }
    
 
}
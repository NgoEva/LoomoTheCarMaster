package com.segway.loomo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.*;

/**
 * @author Vinayak Bevinakatti
 * https://stackoverflow.com/questions/2020088/sending-email-in-android-using-javamail-api-without-using-the-default-built-in-a#2033124
 */

public class ByteArrayDataSource implements DataSource {
    private byte[] data;
    private String type;


    public ByteArrayDataSource(byte[] data, String type) {
        super();
        this.data = data;
        this.type = type;
    }


    public ByteArrayDataSource(byte[] data) {
        super();
        this.data = data;
    }


    public void setType(String type) {
        this.type = type;
    }


    public String getContentType() {
        if (type == null)
            return "application/octet-stream";
        else
            return type;
    }


    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
    }


    public String getName() {
        return "ByteArrayDataSource";
    }


    public OutputStream getOutputStream() throws IOException {
        throw new IOException("Not Supported");
    }
}
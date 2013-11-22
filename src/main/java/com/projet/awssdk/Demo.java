package com.projet.awssdk;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Demo {
    public static final String BUCKET = "code.projet.com";

    public static void main(String... args) throws IOException {
        ClientsManager clientsManager = new ClientsManager();

        S3Manager s3 = clientsManager.getS3Europe();

        s3.upload(
                new ByteArrayInputStream("Hello, world!".getBytes()),
                BUCKET, "hello.txt", "text/plain");
    }
}

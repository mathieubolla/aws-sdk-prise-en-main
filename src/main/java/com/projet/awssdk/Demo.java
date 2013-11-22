package com.projet.awssdk;

import java.io.IOException;

public class Demo {
    public static void main(String... args) throws IOException {
        ClientsManager clientsManager = new ClientsManager();

        S3Manager s3 = clientsManager.getS3Europe();

        for (String bucketName : s3.getBucketsNames()) {
            System.out.println(bucketName);
        }
    }
}

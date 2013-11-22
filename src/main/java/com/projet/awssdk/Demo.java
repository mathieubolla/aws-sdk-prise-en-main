package com.projet.awssdk;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;

import java.io.IOException;

public class Demo {
    public static void main(String... args) throws IOException {
        ClientsManager clientsManager = new ClientsManager();

        AmazonS3 s3 = clientsManager.getS3Europe();

        for (Bucket bucket : s3.listBuckets()) {
            System.out.println(bucket.getName());
        }
    }
}

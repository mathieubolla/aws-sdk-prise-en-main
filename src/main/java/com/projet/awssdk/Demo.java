package com.projet.awssdk;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

import java.io.File;
import java.io.IOException;

public class Demo {
    public static void main(String... args) throws IOException {
        File credentialsFile = new File(
                new File(System.getProperty("user.home"), ".ec2"),
                "credentials.properties"
        );

        AWSCredentials credentials = new PropertiesCredentials(credentialsFile);

        AmazonS3 s3 = new AmazonS3Client(credentials);
        s3.setRegion(Region.getRegion(Regions.EU_WEST_1));

        for (Bucket bucket : s3.listBuckets()) {
            System.out.println(bucket.getName());
        }
    }
}

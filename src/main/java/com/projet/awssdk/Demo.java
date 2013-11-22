package com.projet.awssdk;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

public class Demo {
    public static final String ACCESS_KEY = "mon-id";
    public static final String SECRET_KEY = "ma-clef-SECRETE";

    public static void main(String... args) {
        AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

        AmazonS3 s3 = new AmazonS3Client(credentials);
        s3.setRegion(Region.getRegion(Regions.EU_WEST_1));

        for (Bucket bucket : s3.listBuckets()) {
            System.out.println(bucket.getName());
        }
    }
}

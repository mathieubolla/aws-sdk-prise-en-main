package com.projet.awssdk;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.IOException;

public class ClientsManager {
    private final AWSCredentials credentials;

    public ClientsManager() throws IOException {
        File credentialsFile = new File(
                new File(System.getProperty("user.home"), ".ec2"),
                "credentials.properties"
        );

        credentials = new PropertiesCredentials(credentialsFile);
    }

    public AmazonS3 getS3Europe() {
        AmazonS3 s3 = new AmazonS3Client(credentials);
        s3.setRegion(Region.getRegion(Regions.EU_WEST_1));

        return s3;
    }
}

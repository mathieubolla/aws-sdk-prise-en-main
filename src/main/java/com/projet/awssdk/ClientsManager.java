package com.projet.awssdk;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

import java.io.File;
import java.io.IOException;

public class ClientsManager {
    private final AWSCredentialsProvider credentials;

    public ClientsManager() throws IOException {
        credentials = getAwsCredentials();
    }

    private static AWSCredentialsProvider getAwsCredentials() {
        try {
            File credentialsFile = new File(
                    new File(System.getProperty("user.home"), ".ec2"),
                    "credentials.properties"
            );

            return new StaticCredentialsProvider(
                    new PropertiesCredentials(credentialsFile));
        } catch (IOException e) {
            return new DefaultAWSCredentialsProviderChain();
        }
    }

    public S3Manager getS3Europe() {
        AmazonS3 s3 = new AmazonS3Client(credentials);
        s3.setRegion(Region.getRegion(Regions.EU_WEST_1));

        return new S3Manager(s3);
    }

    public EC2Manager getEC2Europe() {
        AmazonEC2 ec2 = new AmazonEC2Client(credentials);
        ec2.setRegion(Region.getRegion(Regions.EU_WEST_1));

        return new EC2Manager(ec2);
    }

    public IAMManager getIAM() {
        AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(credentials);
        iam.setRegion(Region.getRegion(Regions.EU_WEST_1));

        return new IAMManager(iam);
    }

    public SQSManager getSQSEurope() {
        AmazonSQS sqs = new AmazonSQSClient(credentials);
        sqs.setRegion(Region.getRegion(Regions.EU_WEST_1));

        return new SQSManager(sqs);
    }
}

package com.projet.awssdk;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

public class Demo {
    public static final String BUCKET = "code.projet.com";
    public static final String JAR_SOURCE = "./target/aws-sdk-prise-en-main-1.0-SNAPSHOT-jar-with-dependencies.jar";
    private static final String JAR_NAME = "code.jar";
    private static final String JAR_TYPE = "application/java-archive";

    public static final int MACHINE_COUNT = 1;
    public static final String CLASS_NAME = Demo.class.getCanonicalName();

    public static void main(String... args) throws IOException {
        ClientsManager clientsManager = new ClientsManager();

        EC2Manager ec2Manager = clientsManager.getEC2Europe();
        S3Manager s3Manager = clientsManager.getS3Europe();
        IAMManager iamManager = clientsManager.getIAM();

        if (args.length > 0) {
            conquer(s3Manager);
        } else {
            command(ec2Manager, s3Manager, iamManager);
        }
    }

    private static void command(EC2Manager ec2Manager, S3Manager s3Manager, IAMManager iamManager) throws IOException {
        iamManager.cleanupSecurity();
        String instanceProfileName = iamManager.setupSecurity(BUCKET);

        s3Manager.upload(
                new FileInputStream(JAR_SOURCE),
                BUCKET, JAR_NAME, JAR_TYPE);

        String script = "#!/bin/sh\n" +
                // Download bootstraper
                "curl \"http://code.mathieu-bolla.com/maven/snapshot/aws-sdk-bootstraper/aws-sdk-bootstraper/1.0-SNAPSHOT/aws-sdk-bootstraper-1.0-20131018.100941-1-jar-with-dependencies.jar\" > bootstraper.jar\n" +
                // Use it to safely download private JAR_NAME from BUCKET
                "java -cp bootstraper.jar com.mathieu_bolla.bootstraper.Bootstraper " + BUCKET + " " + JAR_NAME + " " + JAR_NAME + "\n" +
                // Run from JAR_NAME
                "java -cp " + JAR_NAME + " " + CLASS_NAME + " run\n" +
                "shutdown -h now";

        System.out.println("Will run :\n" + script + "\n on "+MACHINE_COUNT + "machines");

        ec2Manager.run(
                script, MACHINE_COUNT, instanceProfileName);
    }

    private static void conquer(S3Manager s3Manager) {
        s3Manager.upload(
                new ByteArrayInputStream("Was there".getBytes()),
                BUCKET, "results/" + new Date().toString(), "text/plain");
    }
}

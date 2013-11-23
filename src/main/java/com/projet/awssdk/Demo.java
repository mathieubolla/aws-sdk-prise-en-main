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

    public static final String COMMANDS_QUEUE = "commands";
    public static final String CONQUESTS_QUEUE = "conquests";

    public static void main(String... args) throws IOException {
        ClientsManager clientsManager = new ClientsManager();

        EC2Manager ec2Manager = clientsManager.getEC2Europe();
        S3Manager s3Manager = clientsManager.getS3Europe();
        IAMManager iamManager = clientsManager.getIAM();
        SQSManager sqsManager = clientsManager.getSQSEurope();

        if (args.length > 0) {
            conquer(s3Manager, sqsManager);
        } else {
            command(ec2Manager, s3Manager, iamManager, sqsManager);
        }
    }

    private static void command(EC2Manager ec2Manager, S3Manager s3Manager,
                                IAMManager iamManager, SQSManager sqsManager) throws IOException {

        String commandsQueueArn = sqsManager.create(COMMANDS_QUEUE);
        String conquestQueueArn = sqsManager.create(CONQUESTS_QUEUE);

        iamManager.cleanupSecurity();
        String instanceProfileName =
                iamManager.setupSecurity(BUCKET, commandsQueueArn, conquestQueueArn);

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

        System.out.println("Will run :\n" + script + "\n on "+MACHINE_COUNT + " machines");

        ec2Manager.run(
                script, MACHINE_COUNT, instanceProfileName);

        sqsManager.sendMessage("Hey, zombies!", COMMANDS_QUEUE);
        if (!sqsManager.processInTransaction(new SQSManager.TransactionalProcess() {
            @Override
            public void process(String input) {
                System.out.println("All right, lets stop here");
            }}, CONQUESTS_QUEUE)) {

            System.out.println("Defeat!");
        }
    }

    private static void conquer(final S3Manager s3Manager,
                                final SQSManager sqsManager) {
        sqsManager.processInTransaction(new SQSManager.TransactionalProcess() {
            @Override
            public void process(String input) {
                s3Manager.upload(
                        new ByteArrayInputStream(input.getBytes()),
                        BUCKET, "results/" + new Date().toString(), JAR_TYPE);
                sqsManager.sendMessage("Roger!", CONQUESTS_QUEUE);
            }
        }, COMMANDS_QUEUE);
    }
}

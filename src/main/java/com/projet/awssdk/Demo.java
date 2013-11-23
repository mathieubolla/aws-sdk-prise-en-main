package com.projet.awssdk;

import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.join;

public class Demo {
    public static final String BUCKET = "code.projet.com";
    public static final String JAR_SOURCE = "./target/aws-sdk-prise-en-main-1.0-SNAPSHOT-jar-with-dependencies.jar";
    private static final String JAR_NAME = "code.jar";
    private static final String JAR_TYPE = "application/java-archive";

    public static final int MACHINE_COUNT = 5;
    public static final String CLASS_NAME = Demo.class.getCanonicalName();

    public static final String COMMANDS_QUEUE = "commands";
    public static final String CONQUESTS_QUEUE = "conquests";

    private static final int MAX_COUNT = 1000;

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
                                IAMManager iamManager,
                                final SQSManager sqsManager) throws IOException {

        prepare(ec2Manager, s3Manager, iamManager, sqsManager);
        work(sqsManager);
    }

    private static void prepare(EC2Manager ec2Manager, S3Manager s3Manager, IAMManager iamManager, SQSManager sqsManager) throws IOException {
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
    }

    private static void work(final SQSManager sqsManager) {
        final Set<String> urlDone = new HashSet<String>();

        sqsManager.sendMessage("http://www.goandcloud.it", COMMANDS_QUEUE);

        while (sqsManager.processInTransaction(new SQSManager.TransactionalProcess() {
            @Override
            public void process(String input) {
                for (String urlFound : StringUtils.split(input, '\n')) {
                    if (!urlDone.contains(urlFound) && urlDone.size() < MAX_COUNT) {
                        System.out.println("Go, process " + urlFound);
                        sqsManager.sendMessage(urlFound, COMMANDS_QUEUE);
                        urlDone.add(urlFound);
                    }
                }
            }}, CONQUESTS_QUEUE)) {

            System.out.println("Found new lands! Lets go on!");
        }
    }

    private static void conquer(final S3Manager s3Manager,
                                final SQSManager sqsManager) {
        while (proceed(s3Manager, sqsManager)) {
            System.out.println("What next, commander?");
        }
        System.out.println("Doh, we won war? Really?");
    }

    private static boolean proceed(final S3Manager s3Manager, final SQSManager sqsManager) {
        return sqsManager.processInTransaction(new SQSManager.TransactionalProcess() {
            @Override
            public void process(String input) {
                final Set<String> urlsFoundInContent = new HashSet<String>();

                InputStream urlStream = UrlUtils.download(
                        input,
                        new UrlUtils.FoundCallback<String>() {
                            @Override
                            public void found(String element) {
                                urlsFoundInContent.add(element);
                            }
                        });

                s3Manager.upload(
                        urlStream,
                        BUCKET,
                        "results/" + cleanupUrlForS3(input),
                        "application/octet-stream");

                try {
                    urlStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String content = join(urlsFoundInContent, '\n');

                if (!content.isEmpty()) {
                    sqsManager.sendMessage(content, CONQUESTS_QUEUE);
                }

            }
        }, COMMANDS_QUEUE);
    }

    private static String cleanupUrlForS3(String url) {
        return url.replace("http://", "").replace("https://", "");
    }
}

package com.projet.awssdk;

import java.io.IOException;

public class Demo {
    public static final String BUCKET = "code.projet.com";
    public static final int MACHINE_COUNT = 1;

    public static void main(String... args) throws IOException {
        ClientsManager clientsManager = new ClientsManager();

        EC2Manager ec2Manager = clientsManager.getEC2Europe();

        ec2Manager.run("#!/bin/sh\nshutdown -h now", MACHINE_COUNT);
    }
}

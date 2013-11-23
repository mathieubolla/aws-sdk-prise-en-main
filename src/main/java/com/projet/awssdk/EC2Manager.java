package com.projet.awssdk;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.ShutdownBehavior;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class EC2Manager {
    private final AmazonEC2 ec2;

    public EC2Manager(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public void run(String script, int machineCount,
                    String instanceProfileName) {
        ec2.runInstances(
                new RunInstancesRequest()
                        .withImageId("ami-c7c0d6b3")
                        .withInstanceType(InstanceType.T1Micro)
                        .withMaxCount(machineCount)
                        .withMinCount(machineCount)
                        .withInstanceInitiatedShutdownBehavior(
                                ShutdownBehavior.Terminate)
                        .withIamInstanceProfile(
                                new IamInstanceProfileSpecification()
                                        .withArn(instanceProfileName))
                        .withUserData(printBase64Binary(script.getBytes()))
        );
    }
}

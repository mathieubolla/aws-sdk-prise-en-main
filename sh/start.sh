#!/bin/sh
mvn clean package
java -cp target/aws-sdk-prise-en-main-1.0-SNAPSHOT-jar-with-dependencies.jar com.projet.awssdk.Demo
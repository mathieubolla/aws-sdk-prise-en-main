package com.projet.awssdk;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.auth.policy.*;

import java.io.IOException;

public class IAMManager {
    private final AmazonIdentityManagement iam;

    public IAMManager(AmazonIdentityManagement iam) {
        this.iam = iam;
    }

    public String setupSecurity(String bucket) throws IOException {
        iam.createRole(new CreateRoleRequest()
                .withAssumeRolePolicyDocument(
                        getAssumeRolePolicy().toJson()
                )
                .withRoleName("runner")
        );

        iam.putRolePolicy(new PutRolePolicyRequest()
                .withPolicyDocument(
                        getCodeDownloaderPolicy(
                                "arn:aws:s3:::" + bucket + "/*"
                        ).toJson()
                )
                .withRoleName("runner")
                .withPolicyName("codeDownloader")
        );

        iam.putRolePolicy(new PutRolePolicyRequest()
                .withPolicyDocument(
                        getResultUploaderPolicy(
                                "arn:aws:s3:::" + bucket + "/results/*"
                        ).toJson()
                )
                .withRoleName("runner")
                .withPolicyName("resultUploader")
        );

        iam.createInstanceProfile(new CreateInstanceProfileRequest()
                .withInstanceProfileName("runnerProfile")
        );

        iam.addRoleToInstanceProfile(new AddRoleToInstanceProfileRequest()
                .withInstanceProfileName("runnerProfile")
                .withRoleName("runner")
        );

        return iam.getInstanceProfile(
                new GetInstanceProfileRequest()
                    .withInstanceProfileName("runnerProfile"))
                .getInstanceProfile()
                .getArn();
    }

    private Policy getCodeDownloaderPolicy(String codeBaseArn) {
        return new Policy().withStatements(
                new Statement(Statement.Effect.Allow)
                        .withActions(S3Actions.GetObject)
                        .withId("Stmt1")
                        .withResources(new Resource(codeBaseArn))
        );
    }

    private Policy getResultUploaderPolicy(String resultsArn) {
        return new Policy().withStatements(
                new Statement(Statement.Effect.Allow)
                        .withActions(S3Actions.PutObject)
                        .withId("Stmt2")
                        .withResources(new Resource(resultsArn))
        );
    }

    private Policy getAssumeRolePolicy() {
        return new Policy()
                .withStatements(
                        new Statement(Statement.Effect.Allow)
                                .withActions(STSActions.AssumeRole)
                                .withId("Stmt")
                                .withPrincipals(
                                        new Principal(Principal.Services.AmazonEC2)
                                )
                );
    }
}
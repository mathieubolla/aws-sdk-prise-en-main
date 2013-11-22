package com.projet.awssdk;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.auth.policy.*;

import java.io.IOException;

public class IAMManager {
    private final AmazonIdentityManagement iam;

    public IAMManager(AmazonIdentityManagement iam) {
        this.iam = iam;
    }

    public void setupSecurity() throws IOException {
        iam.createRole(new CreateRoleRequest()
                .withAssumeRolePolicyDocument(
                        getAssumeRolePolicy().toJson()
                )
                .withRoleName("runner")
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

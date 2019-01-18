package com.wearables.ge.wearables_ble_receiver.utils;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;

import java.io.Serializable;

public class CognitoUserBundle implements Serializable {

    // The key that the cognito user will have in the bundle sent to the confirmation dialog
    public static final String COGNITO_USER_BUNDLE_KEY = "COGNITO_USER";

    public CognitoUser cognitoUser;

    public CognitoUserBundle(CognitoUser cognitoUser) {
        this.cognitoUser = cognitoUser;
    }
}

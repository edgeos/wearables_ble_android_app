package com.wearables.ge.wearables_ble_receiver.utils;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;

import java.io.Serializable;

public class ForgotPasswordContinuationBundle implements Serializable {

    // The key that the forgot password continuation will have in the bundle sent to the forgot password dialog
    public static final String FORGOT_PASSWORD_CONTINUATION_BUNDLE_KEY = "FORGOT_PASSWORD_CONTINUATION";

    public ForgotPasswordContinuation forgotPasswordContinuation;

    public ForgotPasswordContinuationBundle(ForgotPasswordContinuation forgotPasswordContinuation) {
        this.forgotPasswordContinuation = forgotPasswordContinuation;
    }
}

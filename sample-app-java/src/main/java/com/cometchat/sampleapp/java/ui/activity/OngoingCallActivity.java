package com.cometchat.sampleapp.java.ui.activity;

import android.app.PictureInPictureParams;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Rational;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.cometchat.calls.core.CometChatCalls;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.sampleapp.java.R;
import com.cometchat.sampleapp.java.databinding.ActivityOngoingCallBinding;

/**
 * This activity handles ongoing calls, setting up the UI and managing the call
 * session. It retrieves call details from the intent and initializes the call
 * interface.
 */
public class OngoingCallActivity extends AppCompatActivity {

    /**
     * Called when the activity is starting. It initializes the UI and starts the
     * call.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using View Binding
        ActivityOngoingCallBinding binding = ActivityOngoingCallBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        // Retrieve the call details from the intent
        String sessionId = getIntent().getStringExtra(getString(R.string.app_session_id));
        String callType = getIntent().getStringExtra(getString(R.string.app_call_type));

        // Configure the ongoing call UI with the received Call data
        binding.cometchatOngoingCall.setCallWorkFlow(UIKitConstants.CallWorkFlow.DEFAULT);
        binding.cometchatOngoingCall.setSessionId(sessionId);
        binding.cometchatOngoingCall.setCallType(callType);

        // Start the call session
        binding.cometchatOngoingCall.startCall();

        // Disable back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startPictureInPictureMode();
            }
        });
    }

    private void startPictureInPictureMode() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(
                    metrics.widthPixels,
                    metrics.heightPixels
            )).build());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            CometChatCalls.enterPIPMode();
        } else {
            CometChatCalls.exitPIPMode();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        startPictureInPictureMode();
    }

}
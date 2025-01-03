package com.cometchat.sampleapp.java.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.calls.CallingExtension;
import com.cometchat.chatuikit.calls.CometChatCallActivity;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.resources.soundmanager.CometChatSoundManager;
import com.cometchat.chatuikit.shared.resources.soundmanager.Sound;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.sampleapp.java.R;
import com.cometchat.sampleapp.java.data.repository.Repository;
import com.cometchat.sampleapp.java.databinding.CustomCallNotificationBinding;
import com.cometchat.sampleapp.java.ui.activity.OngoingCallActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * This is a custom Application class for managing call notifications and
 * handling CometChat events throughout the app lifecycle. It also registers
 * lifecycle callbacks to keep track of the currently active activity.
 */
public class MyApplication extends Application {
    public static String currentOpenChatId;
    private static String LISTENER_ID;
    private static boolean isAppInForeground;
    private Activity currentActivity;
    private Snackbar snackbar;
    private CometChatSoundManager soundManager;
    private Call tempCall;

    public static boolean isAppInForeground() {
        return isAppInForeground;
    }

    /**
     * Initializes the application by setting up Firebase, adding the CometChat call
     * listener, and registering activity lifecycle callbacks.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        LISTENER_ID = String.valueOf(System.currentTimeMillis());

        addCallListener();

        // Initialize the sensor manager and shake detector
        soundManager = new CometChatSoundManager(this);

        // Register activity lifecycle callbacks to keep track of the current activity
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private boolean isActivityChangingConfigurations = false;
            private int activityReferences = 0;

            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                currentActivity = activity;
                if (currentActivity instanceof CometChatCallActivity) {
                    Utils.setStatusBarColor(currentActivity, CometChatTheme.getBackgroundColor3(activity));
                } else {
                    Utils.setStatusBarColor(currentActivity, CometChatTheme.getBackgroundColor1(activity));
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                currentActivity = activity;
                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    isAppInForeground = true;
                }
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivity = activity;
                if (snackbar != null && snackbar.isShown() && tempCall != null) {
                    showTopSnackBar(tempCall);
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations();
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    isAppInForeground = false;
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }
        });
    }

    /**
     * Adds a call listener using CometChat to handle incoming and outgoing call
     * events.
     */
    public void addCallListener() {
        CometChat.addCallListener(LISTENER_ID, new CometChat.CallListener() {
            @Override
            public void onIncomingCallReceived(Call call) {
                launchIncomingCallPopup(call);
            }

            @Override
            public void onOutgoingCallAccepted(Call call) {
                dismissTopSnackBar();
            }

            @Override
            public void onOutgoingCallRejected(Call call) {
                dismissTopSnackBar();
            }

            @Override
            public void onIncomingCallCancelled(Call call) {
                dismissTopSnackBar();
            }

            @Override
            public void onCallEndedMessageReceived(Call call) {
                dismissTopSnackBar();
            }
        });
    }

    /**
     * Displays a custom SnackBar notification at the top of the screen for incoming
     * calls.
     *
     * @param call The call object representing the incoming call.
     */
    @SuppressLint("RestrictedApi")
    public void showTopSnackBar(Call call) {
        if (!isAppInForeground()) return;
        if (currentActivity != null && call != null) {
            tempCall = call;
            View rootView = currentActivity.findViewById(android.R.id.content);
            View customView = View.inflate(currentActivity, R.layout.custom_call_notification, null);
            CustomCallNotificationBinding binding = CustomCallNotificationBinding.bind(customView);

            User callUser = (User) call.getCallInitiator();
            binding.callerName.setText(callUser.getName());
            binding.callType.setText(String.format(Locale.US, getString(R.string.app_incoming_s_call), call.getType()));
            binding.callerAvatar.setAvatar(callUser.getName(), callUser.getAvatar());
            binding.callTypeIcon.setImageResource(call.getType()
                                                      .equals(CometChatConstants.CALL_TYPE_AUDIO) ? com.cometchat.chatuikit.R.drawable.cometchat_ic_call_voice : com.cometchat.chatuikit.R.drawable.cometchat_ic_call_video);

            binding.rejectButton.setOnClickListener(view -> rejectCall(call));
            binding.acceptButton.setOnClickListener(view -> acceptCall(call));

            snackbar = Snackbar.make(rootView, " ", Snackbar.LENGTH_INDEFINITE);
            Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layout.getLayoutParams();
            params.gravity = Gravity.TOP;
            layout.setLayoutParams(params);
            layout.setBackgroundColor(currentActivity.getResources().getColor(android.R.color.transparent, null));

            layout.addView(binding.getRoot(), 0);

            snackbar.show();
        }
    }

    /**
     * Dismisses the current top SnackBar if it's being shown and resets any related
     * call state.
     */
    public void dismissTopSnackBar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
            snackbar = null;
            tempCall = null;
            CallingExtension.setActiveCall(null);
            pauseSound();
        }
    }

    /**
     * Launches an incoming call popup when an incoming call is received.
     *
     * @param call The call object representing the incoming call.
     */
    public void launchIncomingCallPopup(@Nonnull Call call) {
        if (call.getCallInitiator() instanceof User) {
            User callInitiator = (User) call.getCallInitiator();
            if (CometChatUIKit.getLoggedInUser().getUid().equalsIgnoreCase(callInitiator.getUid()))
                return;
        }

        if (CometChat.getActiveCall() == null && CallingExtension.getActiveCall() == null && !CallingExtension.isActiveMeeting()) {
            CallingExtension.setActiveCall(call);
            playSound();
            showTopSnackBar(call);
        } else {
            rejectCallWithBusyStatus(call);
        }
    }

    /**
     * Rejects an incoming call with a "busy" status if another call is active.
     *
     * @param call The call object representing the call to be rejected.
     */
    private void rejectCallWithBusyStatus(Call call) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Repository.rejectCallWithBusyStatus(call, new CometChat.CallbackListener<Call>() {
                @Override
                public void onSuccess(Call call) {
                    CometChatUIKitHelper.onCallRejected(call);
                }

                @Override
                public void onError(CometChatException e) {
                }
            });
        }).start();
    }

    /**
     * Rejects an incoming call and dismisses the notification.
     *
     * @param call The call object representing the call to be rejected.
     */
    public void rejectCall(Call call) {
        Repository.rejectCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                dismissTopSnackBar();
            }

            @Override
            public void onError(CometChatException e) {
                dismissTopSnackBar();
            }
        });
    }

    /**
     * Accepts an incoming call and starts the ongoing call activity.
     *
     * @param call The call object representing the call to be accepted.
     */
    public void acceptCall(Call call) {
        Repository.acceptCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                startOngoingCallActivity(call);
            }

            @Override
            public void onError(CometChatException e) {
                dismissTopSnackBar();
            }
        });
    }

    /**
     * Starts the OngoingCallActivity to manage an accepted call.
     *
     * @param call The call object representing the accepted call.
     */
    private void startOngoingCallActivity(Call call) {
        dismissTopSnackBar();
        Intent intent = new Intent(this, OngoingCallActivity.class);
        intent.putExtra(getString(R.string.app_session_id), call.getSessionId());
        intent.putExtra(getString(R.string.app_call_type), call.getType());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Plays the sound for an incoming call notification.
     */
    public void playSound() {
        soundManager.play(Sound.incomingCall);
    }

    /**
     * Silently pauses any currently playing sound.
     */
    public void pauseSound() {
        soundManager.pauseSilently();
    }
}
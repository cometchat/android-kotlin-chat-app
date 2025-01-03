package com.cometchat.chatuikit.calls.calllogs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.calls.core.CallLogRequest;
import com.cometchat.calls.exceptions.CometChatException;
import com.cometchat.calls.model.CallLog;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatCallLogsBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.CallLogsClickListener;
import com.cometchat.chatuikit.shared.interfaces.Function2;
import com.cometchat.chatuikit.shared.interfaces.OnBackPress;
import com.cometchat.chatuikit.shared.interfaces.OnCallError;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * The CometChatCallLogs class is a custom view in Android that represents a log
 * of call records, typically used within a chat application. It extends the
 * MaterialCardView class and provides functionality for displaying a list of
 * call logs, handling various states (loading, error, empty), and customizing
 * the UI based on user-defined attributes. The class also supports setting up
 * click listeners and managing different layouts for various states, including
 * loading and error views.
 *
 * <p>
 * Created on: 22 October 2024 Modified on: 22 October 2024
 */
public class CometChatCallLogs extends MaterialCardView {
    private static final String TAG = CometChatCallLogs.class.getSimpleName();

    private CometchatCallLogsBinding binding;

    // Background, Stroke, and Corner Radius
    private @ColorInt int backgroundColor;
    private @Dimension int strokeWidth;
    private @ColorInt int strokeColor;
    private @Dimension int cornerRadius;

    // Back Icon and Tint
    private @Nullable Drawable backIcon;
    private @ColorInt int backIconTint;

    // Title Appearance and Color
    private @StyleRes int titleTextAppearance;
    private @ColorInt int titleTextColor;

    // Empty State Appearance and Colors
    private @StyleRes int emptyStateTitleTextAppearance;
    private @ColorInt int emptyStateTitleTextColor;
    private @StyleRes int emptyStateSubtitleTextAppearance;
    private @ColorInt int emptyStateSubtitleTextColor;

    // Error State Appearance and Colors
    private @StyleRes int errorTitleTextAppearance;
    private @ColorInt int errorTitleTextColor;
    private @StyleRes int errorSubtitleTextAppearance;
    private @ColorInt int errorSubtitleTextColor;

    // Call Logs Item Text Appearance and Colors
    private @StyleRes int itemTitleTextAppearance;
    private @ColorInt int itemTitleTextColor;
    private @StyleRes int itemSubtitleTextAppearance;
    private @ColorInt int itemSubtitleTextColor;

    // Incoming Call Icons and Tints
    private @Nullable Drawable itemIncomingCallIcon;
    private @ColorInt int itemIncomingCallIconTint;

    // Outgoing Call Icons and Tints
    private @Nullable Drawable itemOutgoingCallIcon;
    private @ColorInt int itemOutgoingCallIconTint;

    // Missed Call Icons and Tints
    private @ColorInt int itemMissedCallTitleColor;
    private @Nullable Drawable itemMissedCallIcon;
    private @ColorInt int itemMissedCallIconTint;

    // Audio and Video Call Icons and Tints
    private @Nullable Drawable itemAudioCallIcon;
    private @ColorInt int itemAudioCallIconTint;
    private @Nullable Drawable itemVideoCallIcon;
    private @ColorInt int itemVideoCallIconTint;

    // Avatar Style and Date Style
    private @StyleRes int avatarStyle;
    private @StyleRes int dateStyle;
    private @ColorInt int separatorColor;

    private String errorStateTitle;
    private String errorStateSubtitle;
    private String emptyStateTitle;
    private String emptyStateSubtitle;
    private boolean showShimmer = true;

    private CallLogsViewModel callLogsViewModel;
    private CallLogsAdapter callLogsAdapter;
    /**
     * Observer that updates a specific item in the recycler view when its position
     * is provided.
     */
    Observer<Integer> update = new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            callLogsAdapter.notifyItemChanged(integer);
        }
    };
    /**
     * Observer that removes an item from the recycler view based on its position.
     */
    Observer<Integer> remove = new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            callLogsAdapter.notifyItemRemoved(integer);
        }
    };
    /**
     * Observer that listens for updates in the list of call logs and sets the data
     * in the adapter.
     */
    Observer<List<CallLog>> listObserver = callList -> callLogsAdapter.setCallLogs(callList);
    private LinearLayoutManager layoutManager;
    /**
     * Observer that listens for new data and inserts the new item at the top of the
     * recycler view.
     */
    Observer<Integer> insertAtTop = new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            callLogsAdapter.notifyItemInserted(integer);
            scrollToTop();
        }
    };
    /**
     * Observer that moves a data item to the top of the recycler view and notifies
     * the adapter to update the view.
     */
    Observer<Integer> moveToTop = new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            callLogsAdapter.notifyItemMoved(integer, 0);
            callLogsAdapter.notifyItemChanged(0);
            scrollToTop();
        }
    };
    private boolean hideError;
    private View customLoadingView;
    private View customErrorView;
    private View customEmptyView;
    /**
     * Observer for handling different UI states (loading, loaded, error, empty). It
     * hides all states initially and then shows the appropriate view based on the
     * current state.
     */
    Observer<UIKitConstants.States> stateChangeObserver = states -> {
        hideAllStates(); // Hide all previous states

        switch (states) {
            case LOADING:
                // Display loading state
                if (customLoadingView != null) {
                    Utils.handleView(binding.customLayout, customLoadingView, true);
                } else {
                    if (showShimmer) {
                        setShimmerVisibility(View.VISIBLE);
                        showShimmer = false;
                    }
                }
                break;

            case LOADED:
            case NON_EMPTY:
                // Show the recycler view if data is loaded
                setRecyclerViewVisibility(View.VISIBLE);
                break;

            case ERROR:
                // Display error state if present
                if (customErrorView != null) {
                    Utils.handleView(binding.errorStateView, customErrorView, true);
                } else if (!hideError) {
                    setErrorStateVisibility(View.VISIBLE);
                }
                break;

            case EMPTY:
                // Display empty state if no data
                if (customEmptyView != null) {
                    Utils.handleView(binding.emptyStateView, customEmptyView, true);
                } else {
                    setEmptyStateVisibility(View.VISIBLE);
                }
                break;
        }
    };
    private OnCallError onError;
    /**
     * Observer for handling exceptions. If an error occurs, the OnCallError
     * listener is triggered to handle the exception.
     */
    @NonNull
    Observer<CometChatException> exceptionObserver = exception -> {
        if (onError != null) {
            onError.onError(getContext(), exception);
        }
    };    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        callLogsViewModel.fetchCalls();
    }
    private OnBackPress onBackPress;

    /**
     * Constructs a new CometChatCallLogs instance using the specified context.
     *
     * @param context The context of the view, typically the parent activity or
     *                fragment.
     */
    public CometChatCallLogs(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatCallLogs instance using the specified context and
     * attribute set.
     *
     * @param context The context of the view, typically the parent activity or
     *                fragment.
     * @param attrs   A collection of attributes specified in XML for this view.
     */
    public CometChatCallLogs(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatCallLogsStyle);
    }

    /**
     * Constructs a new CometChatCallLogs instance using the specified context,
     * attribute set, and style attribute.
     *
     * @param context      The context of the view, typically the parent activity or
     *                     fragment.
     * @param attrs        A collection of attributes specified in XML for this view.
     * @param defStyleAttr The default style attribute to apply to this view.
     */
    public CometChatCallLogs(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates the layout for this view and initializes its components. It also
     * applies custom style attributes.
     *
     * @param attrs        A collection of attributes specified in XML for this view.
     * @param defStyleAttr The default style attribute to apply to this view.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        // Inflate the layout for this view
        binding = CometchatCallLogsBinding.inflate(LayoutInflater.from(getContext()), this, true);

        // Reset the card view to default values
        Utils.initMaterialCard(this);

        init();

        // Apply style attributes
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Initializes various components of the view such as RecyclerView, ViewModel,
     * and click events.
     */
    private void init() {
        initRecyclerView();

        initViewModel();

        initClickEvents();
    }

    /**
     * Applies the custom style attributes defined in XML to this view. It extracts
     * the attributes and applies the defaults where necessary.
     *
     * @param attrs        The attribute set from the XML declaration of this view.
     * @param defStyleAttr The default style to apply to the view.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatCallLogs, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatCallLogs_cometchatCallLogsStyle, 0);
        directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatCallLogs, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Initializes the RecyclerView for displaying call logs.
     */
    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerviewList.setLayoutManager(layoutManager);
        callLogsAdapter = new CallLogsAdapter(getContext());
        binding.recyclerviewList.setAdapter(callLogsAdapter);

        binding.recyclerviewList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1)) {
                    callLogsViewModel.fetchCalls();
                }
            }
        });
    }

    /**
     * Initializes the ViewModel for managing UI-related data.
     */
    private void initViewModel() {
        callLogsViewModel = new ViewModelProvider.NewInstanceFactory().create(CallLogsViewModel.class);
        callLogsViewModel.getMutableCallsList().observe((AppCompatActivity) getContext(), listObserver);
        callLogsViewModel.getStates().observe((AppCompatActivity) getContext(), stateChangeObserver);
        callLogsViewModel.insertAtTop().observe((AppCompatActivity) getContext(), insertAtTop);
        callLogsViewModel.moveToTop().observe((AppCompatActivity) getContext(), moveToTop);
        callLogsViewModel.updateCall().observe((AppCompatActivity) getContext(), update);
        callLogsViewModel.removeCall().observe((AppCompatActivity) getContext(), remove);
        callLogsViewModel.getInitiatedCall().observe((AppCompatActivity) getContext(), (call) -> {
        });
        callLogsViewModel.getCometChatException().observe((AppCompatActivity) getContext(), exceptionObserver);
    }

    /**
     * Initializes the click events for this view.
     */
    private void initClickEvents() {
        callLogsAdapter.setCometChatCallLogClickListener(new CallLogsClickListener() {
            @Override
            public void setOnItemClickListener(Context context, CallLogsAdapter.CallLogsViewHolder holder, int position, CallLog callLog) {
            }

            @Override
            public void setOnItemLongClickListener(Context context, CallLogsAdapter.CallLogsViewHolder holder, int position, CallLog callLog) {
            }

            @Override
            public void setOnItemCallIconClickListener(Context context, CallLogsAdapter.CallLogsViewHolder holder, int position, CallLog callLog) {
                /*if (callLog.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                    String callType = callLog.getType().equals(CometChatCallsConstants.CALL_TYPE_AUDIO_VIDEO) ? CometChatCallsConstants.CALL_TYPE_VIDEO : callLog.getType();
                    String receiverId = null;
                    if (callLog.getInitiator() != null) {
                        if (CallUtils.isLoggedInUser((CallUser) callLog.getInitiator())) {
                            if (callLog.getReceiver() != null)
                                receiverId = ((CallUser) callLog.getReceiver()).getUid();
                        } else {
                            receiverId = ((CallUser) callLog.getInitiator()).getUid();
                        }
                    }
                    callLogsViewModel.startCall(receiverId, callType);
                }*/
            }
        });

        binding.toolbarBackIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onBackPress != null) {
                    onBackPress.onBack();
                }
            }
        });

        binding.btnRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callLogsViewModel.fetchCalls();
            }
        });
    }

    /**
     * Extracts style attributes from the provided TypedArray and applies them to
     * the view. If a style attribute is not provided, it falls back to the default
     * values.
     *
     * @param typedArray A TypedArray holding the extracted attributes for this view.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            backgroundColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsBackgroundColor, 0);
            strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatCallLogs_cometchatCallLogsStrokeWidth, 0);
            strokeColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsStrokeColor, 0);
            cornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatCallLogs_cometchatCallLogsCornerRadius, 0);
            backIcon = typedArray.getDrawable(R.styleable.CometChatCallLogs_cometchatCallLogsBackIcon);
            backIconTint = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsBackIconTint, 0);
            titleTextAppearance = typedArray.getResourceId(R.styleable.CometChatCallLogs_cometchatCallLogsTitleTextAppearance, 0);
            titleTextColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsTitleTextColor, 0);
            emptyStateTitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatCallLogs_cometchatCallLogsEmptyStateTitleTextAppearance, 0);
            emptyStateTitleTextColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsEmptyStateTitleTextColor, 0);
            emptyStateSubtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatCallLogs_cometchatCallLogsEmptyStateSubtitleTextAppearance,
                                                                        0
            );
            emptyStateSubtitleTextColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsEmptyStateSubtitleTextColor, 0);
            errorTitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatCallLogs_cometchatCallLogsErrorTitleTextAppearance, 0);
            errorTitleTextColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsErrorTitleTextColor, 0);
            errorSubtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatCallLogs_cometchatCallLogsErrorSubtitleTextAppearance, 0);
            errorSubtitleTextColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsErrorSubtitleTextColor, 0);
            itemTitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatCallLogs_cometchatCallLogsItemTitleTextAppearance, 0);
            itemTitleTextColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsItemTitleTextColor, 0);
            itemSubtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatCallLogs_cometchatCallLogsItemSubtitleTextAppearance, 0);
            itemSubtitleTextColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsItemSubtitleTextColor, 0);
            itemIncomingCallIcon = typedArray.getDrawable(R.styleable.CometChatCallLogs_cometchatCallLogsItemIncomingCallIcon);
            itemIncomingCallIconTint = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsItemIncomingCallIconTint, 0);
            itemOutgoingCallIcon = typedArray.getDrawable(R.styleable.CometChatCallLogs_cometchatCallLogsItemOutgoingCallIcon);
            itemOutgoingCallIconTint = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsItemOutgoingCallIconTint, 0);
            itemMissedCallTitleColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsItemMissedCallTitleColor, 0);
            itemMissedCallIcon = typedArray.getDrawable(R.styleable.CometChatCallLogs_cometchatCallLogsItemMissedCallIcon);
            itemMissedCallIconTint = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsItemMissedCallIconTint, 0);
            itemAudioCallIcon = typedArray.getDrawable(R.styleable.CometChatCallLogs_cometchatCallLogsItemAudioCallIcon);
            itemAudioCallIconTint = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsItemAudioCallIconTint, 0);
            itemVideoCallIcon = typedArray.getDrawable(R.styleable.CometChatCallLogs_cometchatCallLogsItemVideoCallIcon);
            itemVideoCallIconTint = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsItemVideoCallIconTint, 0);
            avatarStyle = typedArray.getResourceId(R.styleable.CometChatCallLogs_cometchatCallLogsAvatarStyle, 0);
            dateStyle = typedArray.getResourceId(R.styleable.CometChatCallLogs_cometchatCallLogsDateStyle, 0);
            separatorColor = typedArray.getColor(R.styleable.CometChatCallLogs_cometchatCallLogsSeparatorColor, 0);

            // Apply default styles
            updateUI();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Updates the user interface (UI) by applying the extracted and default style
     * attributes.
     */
    private void updateUI() {
        setBackgroundColor(backgroundColor);
        setStrokeWidth(strokeWidth);
        setStrokeColor(strokeColor);
        setCornerRadius(cornerRadius);
        setBackIcon(backIcon);
        setBackIconTint(backIconTint);
        setTitleTextAppearance(titleTextAppearance);
        setTitleTextColor(titleTextColor);
        setEmptyStateTitleTextAppearance(emptyStateTitleTextAppearance);
        setEmptyStateTitleTextColor(emptyStateTitleTextColor);
        setEmptyStateSubtitleTextAppearance(emptyStateSubtitleTextAppearance);
        setEmptyStateSubtitleTextColor(emptyStateSubtitleTextColor);
        setErrorTitleTextAppearance(errorTitleTextAppearance);
        setErrorTitleTextColor(errorTitleTextColor);
        setErrorSubtitleTextAppearance(errorSubtitleTextAppearance);
        setErrorSubtitleTextColor(errorSubtitleTextColor);
        setItemTitleTextAppearance(itemTitleTextAppearance);
        setItemTitleTextColor(itemTitleTextColor);
        setItemSubtitleTextAppearance(itemSubtitleTextAppearance);
        setItemSubtitleTextColor(itemSubtitleTextColor);
        setItemIncomingCallIcon(itemIncomingCallIcon);
        setItemIncomingCallIconTint(itemIncomingCallIconTint);
        setItemOutgoingCallIcon(itemOutgoingCallIcon);
        setItemOutgoingCallIconTint(itemOutgoingCallIconTint);
        setItemMissedCallTitleColor(itemMissedCallTitleColor);
        setItemMissedCallIcon(itemMissedCallIcon);
        setItemMissedCallIconTint(itemMissedCallIconTint);
        setItemAudioCallIcon(itemAudioCallIcon);
        setItemAudioCallIconTint(itemAudioCallIconTint);
        setItemVideoCallIcon(itemVideoCallIcon);
        setItemVideoCallIconTint(itemVideoCallIconTint);
        setAvatarStyle(avatarStyle);
        setDateStyle(dateStyle);
        setSeparatorColor(separatorColor);
    }    /**
     * Sets the stroke width.
     *
     * @param strokeWidth The stroke width for the call logs.
     */
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeColor);
    }

    /**
     * Sets the stroke color.
     *
     * @param strokeColor The stroke color for the call logs.
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }    /**
     * Returns the stroke width.
     *
     * @return The stroke width of the call logs.
     */
    public @Dimension int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Returns the stroke color.
     *
     * @return The stroke color of the call logs.
     */
    public ColorStateList getStrokeColorStateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        callLogsViewModel.getCallsArrayList().clear();
    }

    /**
     * Triggers a refresh of the call logs by invoking the ViewModel's `forceToRefresh` method.
     * This ensures the call logs are updated with the latest data from the server.
     */
    public void refreshCallLogs() {
        callLogsViewModel.forceToRefresh();
    }

    /**
     * Sets the style for this view programmatically using the provided style
     * resource.
     *
     * @param style The resource ID of the style to be applied.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatCallLogs);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Returns the background color.
     *
     * @return The background color of the call logs.
     */
    public @ColorInt int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color.
     *
     * @param backgroundColor The background color for the call logs.
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
    }

    /**
     * Returns the corner radius.
     *
     * @return The corner radius of the call logs.
     */
    public @Dimension int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius.
     *
     * @param cornerRadius The corner radius for the call logs.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        super.setRadius(cornerRadius);
    }

    /**
     * Returns the back icon.
     *
     * @return The drawable for the back icon.
     */
    public @Nullable Drawable getBackIcon() {
        return backIcon;
    }

    /**
     * Sets the back icon.
     *
     * @param backIcon The drawable for the back icon.
     */
    public void setBackIcon(@Nullable Drawable backIcon) {
        this.backIcon = backIcon;
        binding.toolbarBackIcon.setBackground(backIcon);
    }

    /**
     * Returns the tint for the back icon.
     *
     * @return The tint color of the back icon.
     */
    public @ColorInt int getBackIconTint() {
        return backIconTint;
    }

    /**
     * Sets the tint for the back icon.
     *
     * @param backIconTint The tint color for the back icon.
     */
    public void setBackIconTint(@ColorInt int backIconTint) {
        this.backIconTint = backIconTint;
        binding.toolbarBackIcon.setBackgroundTintList(ColorStateList.valueOf(backIconTint));
    }

    /**
     * Returns the title text appearance.
     *
     * @return The resource ID of the text appearance style.
     */
    public @StyleRes int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the title text appearance.
     *
     * @param titleTextAppearance The resource ID of the text appearance style.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.toolbarTitle.setTextAppearance(titleTextAppearance);
    }

    /**
     * Returns the title text color.
     *
     * @return The color of the title text.
     */
    public @ColorInt int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the title text color.
     *
     * @param titleTextColor The color of the title text.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        binding.toolbarTitle.setTextColor(titleTextColor);
    }

    /**
     * Returns the empty state title text appearance.
     *
     * @return The resource ID of the text appearance style for the empty state
     * title.
     */
    public @StyleRes int getEmptyStateTitleTextAppearance() {
        return emptyStateTitleTextAppearance;
    }

    /**
     * Sets the empty state title text appearance.
     *
     * @param emptyStateTitleTextAppearance The resource ID of the text appearance style for the empty state
     *                                      title.
     */
    public void setEmptyStateTitleTextAppearance(@StyleRes int emptyStateTitleTextAppearance) {
        this.emptyStateTitleTextAppearance = emptyStateTitleTextAppearance;
        binding.tvErrorStateTitle.setTextAppearance(emptyStateTitleTextAppearance);
    }

    /**
     * Returns the empty state title text color.
     *
     * @return The color of the empty state title text.
     */
    public @ColorInt int getEmptyStateTitleTextColor() {
        return emptyStateTitleTextColor;
    }

    /**
     * Sets the empty state title text color.
     *
     * @param emptyStateTitleTextColor The color of the empty state title text.
     */
    public void setEmptyStateTitleTextColor(@ColorInt int emptyStateTitleTextColor) {
        this.emptyStateTitleTextColor = emptyStateTitleTextColor;
        binding.tvEmptyStateTitle.setTextColor(emptyStateTitleTextColor);
    }

    /**
     * Returns the empty state subtitle text appearance.
     *
     * @return The resource ID of the text appearance style for the empty state
     * subtitle.
     */
    public @StyleRes int getEmptyStateSubtitleTextAppearance() {
        return emptyStateSubtitleTextAppearance;
    }

    /**
     * Sets the empty state subtitle text appearance.
     *
     * @param emptyStateSubtitleTextAppearance The resource ID of the text appearance style for the empty state
     *                                         subtitle.
     */
    public void setEmptyStateSubtitleTextAppearance(@StyleRes int emptyStateSubtitleTextAppearance) {
        this.emptyStateSubtitleTextAppearance = emptyStateSubtitleTextAppearance;
        binding.tvEmptyStateSubtitle.setTextAppearance(emptyStateSubtitleTextAppearance);
    }

    /**
     * Returns the empty state subtitle text color.
     *
     * @return The color of the empty state subtitle text.
     */
    public @ColorInt int getEmptyStateSubtitleTextColor() {
        return emptyStateSubtitleTextColor;
    }

    /**
     * Sets the empty state subtitle text color.
     *
     * @param emptyStateSubtitleTextColor The color of the empty state subtitle text.
     */
    public void setEmptyStateSubtitleTextColor(@ColorInt int emptyStateSubtitleTextColor) {
        this.emptyStateSubtitleTextColor = emptyStateSubtitleTextColor;
        binding.tvErrorStateSubtitle.setTextColor(emptyStateSubtitleTextColor);
    }

    /**
     * Returns the error title text appearance.
     *
     * @return The resource ID of the text appearance style for the error title.
     */
    public @StyleRes int getErrorTitleTextAppearance() {
        return errorTitleTextAppearance;
    }

    /**
     * Sets the error title text appearance.
     *
     * @param errorTitleTextAppearance The resource ID of the text appearance style for the error title.
     */
    public void setErrorTitleTextAppearance(@StyleRes int errorTitleTextAppearance) {
        this.errorTitleTextAppearance = errorTitleTextAppearance;
        binding.tvEmptyStateTitle.setTextAppearance(errorTitleTextAppearance);
    }

    /**
     * Returns the error title text color.
     *
     * @return The color of the error title text.
     */
    public @ColorInt int getErrorTitleTextColor() {
        return errorTitleTextColor;
    }

    /**
     * Sets the error title text color.
     *
     * @param errorTitleTextColor The color of the error title text.
     */
    public void setErrorTitleTextColor(@ColorInt int errorTitleTextColor) {
        this.errorTitleTextColor = errorTitleTextColor;
        binding.tvEmptyStateTitle.setTextColor(errorTitleTextColor);
    }

    /**
     * Returns the error subtitle text appearance.
     *
     * @return The resource ID of the text appearance style for the error subtitle.
     */
    public @StyleRes int getErrorSubtitleTextAppearance() {
        return errorSubtitleTextAppearance;
    }

    /**
     * Sets the error subtitle text appearance.
     *
     * @param errorSubtitleTextAppearance The resource ID of the text appearance style for the error
     *                                    subtitle.
     */
    public void setErrorSubtitleTextAppearance(@StyleRes int errorSubtitleTextAppearance) {
        this.errorSubtitleTextAppearance = errorSubtitleTextAppearance;
        binding.tvEmptyStateSubtitle.setTextAppearance(errorSubtitleTextAppearance);
    }

    /**
     * Returns the error subtitle text color.
     *
     * @return The color of the error subtitle text.
     */
    public @ColorInt int getErrorSubtitleTextColor() {
        return errorSubtitleTextColor;
    }

    /**
     * Sets the error subtitle text color.
     *
     * @param errorSubtitleTextColor The color of the error subtitle text.
     */
    public void setErrorSubtitleTextColor(@ColorInt int errorSubtitleTextColor) {
        this.errorSubtitleTextColor = errorSubtitleTextColor;
        binding.tvEmptyStateSubtitle.setTextColor(errorSubtitleTextColor);
    }

    /**
     * Returns the item title text appearance.
     *
     * @return The resource ID of the text appearance style for the item title.
     */
    public @StyleRes int getItemTitleTextAppearance() {
        return itemTitleTextAppearance;
    }

    /**
     * Sets the item title text appearance.
     *
     * @param itemTitleTextAppearance The resource ID of the text appearance style for the item title.
     */
    public void setItemTitleTextAppearance(@StyleRes int itemTitleTextAppearance) {
        this.itemTitleTextAppearance = itemTitleTextAppearance;
        callLogsAdapter.setItemTitleTextAppearance(itemTitleTextAppearance);
    }

    /**
     * Returns the item title text color.
     *
     * @return The color of the item title text.
     */
    public @ColorInt int getItemTitleTextColor() {
        return itemTitleTextColor;
    }

    /**
     * Sets the item title text color.
     *
     * @param itemTitleTextColor The color of the item title text.
     */
    public void setItemTitleTextColor(@ColorInt int itemTitleTextColor) {
        this.itemTitleTextColor = itemTitleTextColor;
        callLogsAdapter.setItemTitleTextColor(itemTitleTextColor);
    }

    /**
     * Returns the item subtitle text appearance.
     *
     * @return The resource ID of the text appearance style for the item subtitle.
     */
    public @StyleRes int getItemSubtitleTextAppearance() {
        return itemSubtitleTextAppearance;
    }

    /**
     * Sets the item subtitle text appearance.
     *
     * @param itemSubtitleTextAppearance The resource ID of the text appearance style for the item
     *                                   subtitle.
     */
    public void setItemSubtitleTextAppearance(@StyleRes int itemSubtitleTextAppearance) {
        this.itemSubtitleTextAppearance = itemSubtitleTextAppearance;
        callLogsAdapter.setItemSubtitleTextAppearance(itemSubtitleTextAppearance);
    }

    /**
     * Returns the item subtitle text color.
     *
     * @return The color of the item subtitle text.
     */
    public @ColorInt int getItemSubtitleTextColor() {
        return itemSubtitleTextColor;
    }

    /**
     * Sets the item subtitle text color.
     *
     * @param itemSubtitleTextColor The color of the item subtitle text.
     */
    public void setItemSubtitleTextColor(@ColorInt int itemSubtitleTextColor) {
        this.itemSubtitleTextColor = itemSubtitleTextColor;
        callLogsAdapter.setItemSubtitleTextColor(itemSubtitleTextColor);
    }

    /**
     * Returns the incoming call icon.
     *
     * @return The drawable for the incoming call icon.
     */
    public @Nullable Drawable getItemIncomingCallIcon() {
        return itemIncomingCallIcon;
    }

    /**
     * Sets the incoming call icon.
     *
     * @param itemIncomingCallIcon The drawable for the incoming call icon.
     */
    public void setItemIncomingCallIcon(@Nullable Drawable itemIncomingCallIcon) {
        this.itemIncomingCallIcon = itemIncomingCallIcon;
        callLogsAdapter.setItemIncomingCallIcon(itemIncomingCallIcon);
    }

    /**
     * Returns the tint for the incoming call icon.
     *
     * @return The tint color of the incoming call icon.
     */
    public @ColorInt int getItemIncomingCallIconTint() {
        return itemIncomingCallIconTint;
    }

    /**
     * Sets the tint for the incoming call icon.
     *
     * @param itemIncomingCallIconTint The tint color for the incoming call icon.
     */
    public void setItemIncomingCallIconTint(@ColorInt int itemIncomingCallIconTint) {
        this.itemIncomingCallIconTint = itemIncomingCallIconTint;
        callLogsAdapter.setItemIncomingCallIconTint(itemIncomingCallIconTint);
    }

    /**
     * Returns the outgoing call icon.
     *
     * @return The drawable for the outgoing call icon.
     */
    public @Nullable Drawable getItemOutgoingCallIcon() {
        return itemOutgoingCallIcon;
    }

    /**
     * Sets the outgoing call icon.
     *
     * @param itemOutgoingCallIcon The drawable for the outgoing call icon.
     */
    public void setItemOutgoingCallIcon(@Nullable Drawable itemOutgoingCallIcon) {
        this.itemOutgoingCallIcon = itemOutgoingCallIcon;
        callLogsAdapter.setItemOutgoingCallIcon(itemOutgoingCallIcon);
    }

    /**
     * Returns the tint for the outgoing call icon.
     *
     * @return The tint color of the outgoing call icon.
     */
    public @ColorInt int getItemOutgoingCallIconTint() {
        return itemOutgoingCallIconTint;
    }

    /**
     * Sets the tint for the outgoing call icon.
     *
     * @param itemOutgoingCallIconTint The tint color for the outgoing call icon.
     */
    public void setItemOutgoingCallIconTint(@ColorInt int itemOutgoingCallIconTint) {
        this.itemOutgoingCallIconTint = itemOutgoingCallIconTint;
        callLogsAdapter.setItemOutgoingCallIconTint(itemOutgoingCallIconTint);
    }

    public @ColorInt int getItemMissedCallTitleColor() {
        return itemMissedCallTitleColor;
    }

    public void setItemMissedCallTitleColor(@ColorInt int itemMissedCallTitleColor) {
        this.itemMissedCallTitleColor = itemMissedCallTitleColor;
        callLogsAdapter.setItemMissedCallTitleColor(itemMissedCallTitleColor);
    }

    /**
     * Returns the missed call icon.
     *
     * @return The drawable for the missed call icon.
     */
    public @Nullable Drawable getItemMissedCallIcon() {
        return itemMissedCallIcon;
    }

    /**
     * Sets the missed call icon.
     *
     * @param itemMissedCallIcon The drawable for the missed call icon.
     */
    public void setItemMissedCallIcon(@Nullable Drawable itemMissedCallIcon) {
        this.itemMissedCallIcon = itemMissedCallIcon;
        callLogsAdapter.setItemMissedCallIcon(itemMissedCallIcon);
    }

    /**
     * Returns the tint for the missed call icon.
     *
     * @return The tint color of the missed call icon.
     */
    public @ColorInt int getItemMissedCallIconTint() {
        return itemMissedCallIconTint;
    }

    /**
     * Sets the tint for the missed call icon.
     *
     * @param itemMissedCallIconTint The tint color for the missed call icon.
     */
    public void setItemMissedCallIconTint(@ColorInt int itemMissedCallIconTint) {
        this.itemMissedCallIconTint = itemMissedCallIconTint;
        callLogsAdapter.setItemMissedCallIconTint(itemMissedCallIconTint);
    }

    /**
     * Returns the audio call icon.
     *
     * @return The drawable for the audio call icon.
     */
    public @Nullable Drawable getItemAudioCallIcon() {
        return itemAudioCallIcon;
    }

    /**
     * Sets the audio call icon.
     *
     * @param itemAudioCallIcon The drawable for the audio call icon.
     */
    public void setItemAudioCallIcon(@Nullable Drawable itemAudioCallIcon) {
        this.itemAudioCallIcon = itemAudioCallIcon;
        Log.e(TAG, "setItemAudioCallIcon: " + itemAudioCallIcon);
        callLogsAdapter.setItemAudioCallIcon(itemAudioCallIcon);
    }

    /**
     * Returns the tint for the audio call icon.
     *
     * @return The tint color of the audio call icon.
     */
    public @ColorInt int getItemAudioCallIconTint() {
        return itemAudioCallIconTint;
    }

    /**
     * Sets the tint for the audio call icon.
     *
     * @param itemAudioCallIconTint The tint color for the audio call icon.
     */
    public void setItemAudioCallIconTint(@ColorInt int itemAudioCallIconTint) {
        this.itemAudioCallIconTint = itemAudioCallIconTint;
        callLogsAdapter.setItemAudioCallIconTint(itemAudioCallIconTint);
    }

    /**
     * Returns the video call icon.
     *
     * @return The drawable for the video call icon.
     */
    public @Nullable Drawable getItemVideoCallIcon() {
        return itemVideoCallIcon;
    }

    /**
     * Sets the video call icon.
     *
     * @param itemVideoCallIcon The drawable for the video call icon.
     */
    public void setItemVideoCallIcon(@Nullable Drawable itemVideoCallIcon) {
        this.itemVideoCallIcon = itemVideoCallIcon;
        callLogsAdapter.setItemVideoCallIcon(itemVideoCallIcon);
    }

    /**
     * Returns the tint for the video call icon.
     *
     * @return The tint color of the video call icon.
     */
    public @ColorInt int getItemVideoCallIconTint() {
        return itemVideoCallIconTint;
    }

    /**
     * Sets the tint for the video call icon.
     *
     * @param itemVideoCallIconTint The tint color for the video call icon.
     */
    public void setItemVideoCallIconTint(@ColorInt int itemVideoCallIconTint) {
        this.itemVideoCallIconTint = itemVideoCallIconTint;
        callLogsAdapter.setItemVideoCallIconTint(itemVideoCallIconTint);
    }

    /**
     * Returns the avatar style.
     *
     * @return The resource ID of the avatar style.
     */
    public @StyleRes int getAvatarStyle() {
        return avatarStyle;
    }

    /**
     * Sets the avatar style.
     *
     * @param avatarStyle The resource ID of the avatar style.
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        callLogsAdapter.setAvatarStyle(avatarStyle);
    }

    /**
     * Returns the date style.
     *
     * @return The resource ID of the date style.
     */
    public @StyleRes int getDateStyle() {
        return dateStyle;
    }

    /**
     * Sets the date style.
     *
     * @param dateStyle The resource ID of the date style.
     */
    public void setDateStyle(@StyleRes int dateStyle) {
        this.dateStyle = dateStyle;
        callLogsAdapter.setDateStyle(dateStyle);
    }

    /**
     * Sets a custom view to display when the call log is empty.
     *
     * @param id The resource ID of the layout to be used as the empty state view.
     */
    public void setEmptyStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customEmptyView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customEmptyView = null;
            }
        }
    }

    /**
     * Sets a custom view to display in the error state.
     *
     * @param id The resource ID of the layout to be used as the error state view.
     */
    public void setErrorStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customErrorView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customErrorView = null;
            }
        }
    }

    /**
     * Sets a custom view to display while the call logs are loading.
     *
     * @param id The resource ID of the layout to be used as the loading state
     *           view.
     */
    public void setLoadingStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customLoadingView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customLoadingView = null;
            }
        }
    }

    /**
     * Sets the custom view for the subtitle of each call log item.
     *
     * @param subtitle A function that provides the subtitle view based on the context
     *                 and call log.
     */
    public void setSubtitleView(Function2<Context, CallLog, View> subtitle) {
        callLogsAdapter.setSubtitleView(subtitle);
    }

    /**
     * Sets a custom tail view for each call log item.
     *
     * @param tail A function that provides the tail view based on the context and
     *             call log.
     */
    public void setTail(Function2<Context, CallLog, View> tail) {
        callLogsAdapter.setTailView(tail);
    }

    /**
     * Sets the custom layout for the entire call log item.
     *
     * @param listItemView A function that provides the item view based on the context and
     *                     call log.
     */
    public void setListItemView(Function2<Context, CallLog, View> listItemView) {
        callLogsAdapter.setCustomView(listItemView);
    }

    /**
     * Sets a custom request builder to fetch the call logs.
     *
     * @param callLogRequestBuilder The request builder that will fetch call logs.
     */
    public void setCallLogRequestBuilder(CallLogRequest.CallLogRequestBuilder callLogRequestBuilder) {
        callLogsViewModel.setCallLogRequestBuilder(callLogRequestBuilder);
    }

    /**
     * Returns the current instance of the CallLogsViewModel used by this view.
     *
     * @return The view model responsible for managing the call logs.
     */
    public CallLogsViewModel getViewModel() {
        return callLogsViewModel;
    }

    /**
     * Hides all possible UI states like recycler view, empty state, error state,
     * shimmer, and custom loader.
     */
    private void hideAllStates() {
        setRecyclerViewVisibility(View.GONE);
        setEmptyStateVisibility(View.GONE);
        setErrorStateVisibility(View.GONE);
        setShimmerVisibility(View.GONE);
        setCustomLoaderVisibility(View.GONE);
    }

    /**
     * Sets the visibility of the recycler view.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    private void setRecyclerViewVisibility(int visibility) {
        binding.recyclerviewList.setVisibility(visibility);
    }

    /**
     * Sets the visibility of the empty state view.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    private void setEmptyStateVisibility(int visibility) {
        binding.emptyStateView.setVisibility(visibility);
    }

    /**
     * Sets the visibility of the error state view.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    private void setErrorStateVisibility(int visibility) {
        binding.errorStateView.setVisibility(visibility);
    }

    /**
     * Sets the visibility of the shimmer effect, which is used to display a loading
     * state.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    private void setShimmerVisibility(int visibility) {
        if (visibility == View.GONE) {
            binding.shimmerEffectFrame.stopShimmer();
        } else {
            CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(30, R.layout.shimmer_list_base);
            binding.shimmerRecyclerviewList.setAdapter(adapter);
            binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
            binding.shimmerEffectFrame.startShimmer();
        }
        binding.shimmerParentLayout.setVisibility(visibility);
    }

    /**
     * Sets the visibility of the custom loader view.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    private void setCustomLoaderVisibility(int visibility) {
        binding.customLayout.setVisibility(visibility);
    }

    /**
     * Retry logic to fetch the call logs and show the shimmer effect while loading.
     */
    private void retry() {
        showShimmer = true;
        callLogsViewModel.fetchCalls();
    }

    /**
     * Scrolls the recycler view to the top if the first visible item is within the
     * first five positions.
     */
    private void scrollToTop() {
        if (layoutManager.findFirstVisibleItemPosition() < 5) {
            layoutManager.scrollToPosition(0);
        }
    }

    /**
     * Sets a listener for click events on the call logs.
     *
     * @param click Listener to handle call log click events.
     */
    public void setClickListener(CallLogsClickListener click) {
        callLogsAdapter.setCometChatCallLogClickListener(click);
    }

    /**
     * Returns the current OnCallError listener.
     *
     * @return The OnCallError listener.
     */
    public OnCallError getOnError() {
        return onError;
    }

    /**
     * Sets a callback for handling errors that occur during the call log process.
     *
     * @param onError The callback that will handle errors.
     */
    public void setOnError(OnCallError onError) {
        this.onError = onError;
    }

    /**
     * Returns the current OnBackPress listener.
     *
     * @return The OnBackPress listener.
     */
    public OnBackPress getOnBackPress() {
        return onBackPress;
    }

    /**
     * Sets the OnBackPress listener for handling back button press events.
     *
     * @param onBackPress Listener to handle back press events.
     */
    public void setOnBackPress(OnBackPress onBackPress) {
        this.onBackPress = onBackPress;
    }

    /**
     * Sets the visibility of the back icon in the toolbar.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    public void setBackIconVisibility(int visibility) {
        binding.toolbarBackIcon.setVisibility(visibility);
    }

    /**
     * Configures whether to hide the error state in the UI.
     *
     * @param hideError True to hide the error state, false to show it.
     */
    public void hideError(boolean hideError) {
        this.hideError = hideError;
    }

    /**
     * Hides or shows the toolbar based on the provided boolean value.
     *
     * @param hideToolbar True to hide the toolbar, false to show it.
     */
    public void hideToolbar(boolean hideToolbar) {
        binding.toolbarLayout.setVisibility(hideToolbar ? View.GONE : View.VISIBLE);
    }

    /**
     * Gets the color of the separator line.
     *
     * @return The color of the separator.
     */
    public @ColorInt int getSeparatorColor() {
        return separatorColor;
    }

    /**
     * Sets the color of the separator line.
     *
     * @param separatorColor The color to set for the separator.
     */
    public void setSeparatorColor(@ColorInt int separatorColor) {
        this.separatorColor = separatorColor;
        binding.viewSeparator.setBackgroundColor(separatorColor);
    }

    /**
     * Gets the title of the error state.
     *
     * @return The error state title.
     */
    public String getErrorStateTitle() {
        return errorStateTitle;
    }

    /**
     * Sets the title for the error state in the UI.
     *
     * @param errorStateTitle The title to display for the error state.
     */
    public void setErrorStateTitle(String errorStateTitle) {
        this.errorStateTitle = errorStateTitle;
        binding.tvErrorStateTitle.setText(errorStateTitle);
    }

    /**
     * Gets the subtitle of the error state.
     *
     * @return The error state subtitle.
     */
    public String getErrorStateSubtitle() {
        return errorStateSubtitle;
    }

    /**
     * Sets the subtitle for the error state in the UI.
     *
     * @param errorStateSubtitle The subtitle to display for the error state.
     */
    public void setErrorStateSubtitle(String errorStateSubtitle) {
        this.errorStateSubtitle = errorStateSubtitle;
        binding.tvErrorStateSubtitle.setText(errorStateSubtitle);
    }

    /**
     * Gets the title of the empty state.
     *
     * @return The empty state title.
     */
    public String getEmptyStateTitle() {
        return emptyStateTitle;
    }

    /**
     * Sets the title for the empty state in the UI.
     *
     * @param emptyStateTitle The title to display for the empty state.
     */
    public void setEmptyStateTitle(String emptyStateTitle) {
        this.emptyStateTitle = emptyStateTitle;
        binding.tvEmptyStateTitle.setText(emptyStateTitle);
    }

    /**
     * Gets the subtitle of the empty state.
     *
     * @return The empty state subtitle.
     */
    public String getEmptyStateSubtitle() {
        return emptyStateSubtitle;
    }

    /**
     * Sets the subtitle for the empty state in the UI.
     *
     * @param emptyStateSubtitle The subtitle to display for the empty state.
     */
    public void setEmptyStateSubtitle(String emptyStateSubtitle) {
        this.emptyStateSubtitle = emptyStateSubtitle;
        binding.tvEmptyStateSubtitle.setText(emptyStateSubtitle);
    }

    /**
     * Gets the binding object for CometChatCallLogs.
     *
     * @return The CometChatCallLogsBinding instance.
     */
    public CometchatCallLogsBinding getBinding() {
        return binding;
    }






}
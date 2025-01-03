package com.cometchat.chatuikit.groupmembers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.core.GroupMembersRequest;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatGroupMembersListViewBinding;
import com.cometchat.chatuikit.groupmembers.scopechange.CometChatScopeChange;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.Function3;
import com.cometchat.chatuikit.shared.interfaces.OnError;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.custom_dialog.CometChatConfirmDialog;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.ClickListener;
import com.cometchat.chatuikit.shared.resources.utils.recycler_touch.RecyclerTouchListener;
import com.cometchat.chatuikit.shared.utils.MembersUtils;
import com.cometchat.chatuikit.shared.viewholders.GroupMembersViewHolderListeners;
import com.cometchat.chatuikit.shared.views.popupmenu.CometChatPopupMenu;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * CometChatGroupMembers class handles the display and management of group
 * members in the CometChat UI.
 *
 * <p>
 * This class provides functionality to manage and interact with group members,
 * including displaying the list of members, handling clicks, and managing
 * different states (loading, error, empty). It also provides customization
 * options for different UI states and group member interactions.
 */
public class CometChatGroupMembers extends MaterialCardView {
    private static final String TAG = CometChatGroupMembers.class.getSimpleName();

    // View and Layout Binding
    private CometchatGroupMembersListViewBinding binding;
    private LinearLayoutManager layoutManager;

    // ViewModel for managing group members
    private GroupMembersViewModel groupMembersViewModel;

    // Dialogs
    private CometChatConfirmDialog deleteAlertDialog;

    // Adapter for group members
    private GroupMembersAdapter groupMembersAdapter;

    // Views for handling different states
    private View customEmptyStateView = null;
    private View customErrorStateView = null;
    private View customLoadingView = null;

    // Custom text for empty and error states
    private String customErrorStateTitleText;
    private String customErrorStateSubtitleText;
    private String customEmptyStateTitleText;
    private String customEmptyStateSubtitleText;

    // Interaction listeners
    private OnItemClick onItemClick;
    private OnItemLongClick onItemLongClick;
    private OnBackPress onBackPress;
    private OnError onError;
    private OnSelection onSelection;

    // Selection Mode and additional selection control
    private UIKitConstants.SelectionMode selectionMode = UIKitConstants.SelectionMode.NONE;
    private boolean isFurtherSelectionEnabled = true;

    // Data structures and function callbacks
    private final HashMap<GroupMember, Boolean> hashMap = new HashMap<>();
    private Function3<Context, GroupMember, Group, List<CometChatPopupMenu.MenuItem>> menuItems;

    // Group details and popup menu
    private Group group;
    private CometChatPopupMenu cometchatPopUpMenu;

    // Error visibility
    private boolean hideError;

    // Colors related to the search input
    private @ColorInt int searchInputStrokeColor;
    private @ColorInt int searchInputBackgroundColor;
    private @ColorInt int searchInputTextColor;
    private @ColorInt int searchInputPlaceHolderTextColor;

    // General color properties
    private @ColorInt int backIconTint;
    private @ColorInt int strokeColor;
    private @ColorInt int backgroundColor;
    private @ColorInt int titleTextColor;
    private @ColorInt int emptyStateTitleTextColor;
    private @ColorInt int emptyStateSubtitleTextColor;
    private @ColorInt int errorStateTitleTextColor;
    private @ColorInt int errorStateSubtitleTextColor;
    private @ColorInt int itemTitleTextColor;
    private @ColorInt int separatorColor;

    // Dimensions for UI elements
    private @Dimension int strokeWidth;
    private @Dimension int cornerRadius;
    private @Dimension int separatorHeight;
    private @Dimension int searchInputStrokeWidth;
    private @Dimension int searchInputCornerRadius;
    private @Dimension int checkBoxStrokeWidth;
    private @Dimension int checkBoxCornerRadius;

    // Text appearances for different components
    private @StyleRes int searchInputTextAppearance;
    private @StyleRes int titleTextAppearance;
    private @StyleRes int emptyStateTitleTextAppearance;
    private @StyleRes int emptyStateSubtitleTextAppearance;
    private @StyleRes int errorStateTitleTextAppearance;
    private @StyleRes int errorStateSubtitleTextAppearance;
    private @StyleRes int itemTitleTextAppearance;
    private @StyleRes int avatarStyle;
    private @StyleRes int statusIndicatorStyle;
    private @StyleRes int style;

    // Icons and their tints
    private Drawable discardSelectionIcon;
    private @ColorInt int discardSelectionIconTint;
    private Drawable submitSelectionIcon;
    private @ColorInt int submitSelectionIconTint;
    private Drawable searchInputStartIcon;
    private Drawable searchInputEndIcon;
    private @ColorInt int searchInputStartIconTint;
    private @ColorInt int searchInputEndIconTint;
    private Drawable backIcon;
    private Drawable backgroundDrawable;
    private Drawable selectIcon;
    private @ColorInt int selectIconTint;

    // Checkbox related colors
    private @ColorInt int checkBoxStrokeColor;
    private @ColorInt int checkBoxBackgroundColor;
    private @ColorInt int checkBoxCheckedBackgroundColor;

    /**
     * Interface to handle selection events.
     */
    public interface OnSelection {
        /**
         * Called when a selection is made.
         *
         * @param groupMemberList The list of selected {@link GroupMember} objects.
         */
        void onSelection(List<GroupMember> groupMemberList);
    }

    /**
     * Interface to handle back press events.
     */
    public interface OnBackPress {
        /**
         * Called when the back button is pressed.
         */
        void onBack();
    }

    /**
     * Interface for handling item click events.
     */
    public interface OnItemClick {
        /**
         * Called when an item is clicked.
         *
         * @param view     the view that was clicked.
         * @param position the position of the clicked item in the adapter.
         * @param member   the GroupMember associated with the clicked item.
         */
        void click(View view, int position, GroupMember member);
    }

    /**
     * Interface for handling item long click events.
     */
    public interface OnItemLongClick {
        /**
         * Called when an item is long-clicked.
         *
         * @param view     the view that was long-clicked.
         * @param position the position of the long-clicked item in the adapter.
         * @param member   the GroupMember associated with the long-clicked item.
         */
        void longClick(View view, int position, GroupMember member);
    }

    /**
     * Constructor for creating a CometChatGroupMembers instance.
     *
     * @param context The context of the activity or fragment.
     */
    public CometChatGroupMembers(Context context) {
        this(context, null);
    }

    /**
     * Constructor for creating a CometChatGroupMembers instance.
     *
     * @param context The context of the activity or fragment.
     * @param attrs   The attribute set for the view.
     */
    public CometChatGroupMembers(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatGroupMembersStyle);
    }

    /**
     * Constructor for creating a CometChatGroupMembers instance.
     *
     * @param context      The context of the activity or fragment.
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style attribute.
     */
    public CometChatGroupMembers(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the CometChatGroupMembers view.
     *
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style attribute.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        // Initialize the MaterialCardView and inflate the binding for the layout
        Utils.initMaterialCard(this);
        binding = CometchatGroupMembersListViewBinding.inflate(LayoutInflater.from(getContext()), this, true);

        // Initialize the popup menu for the group members
        cometchatPopUpMenu = new CometChatPopupMenu(getContext(), 0);

        // Set up the RecyclerView and its adapter
        layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerviewGroupMembersList.setLayoutManager(layoutManager);
        groupMembersAdapter = new GroupMembersAdapter(getContext());
        binding.recyclerviewGroupMembersList.setAdapter(groupMembersAdapter);

        // Add a scroll listener to the RecyclerView to detect when the user reaches the
        // bottom
        binding.recyclerviewGroupMembersList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                // If the RecyclerView cannot scroll down anymore, fetch more group members
                if (!recyclerView.canScrollVertically(1)) {
                    groupMembersViewModel.fetchGroupMember();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // Additional actions on scroll can be handled here if needed
            }
        });

        // Handle click events within the view
        clickEvents();

        // Initialize the ViewModel and observe various live data updates
        groupMembersViewModel = new ViewModelProvider.NewInstanceFactory().create(GroupMembersViewModel.class);
        groupMembersViewModel.getMutableGroupMembersList().observe((AppCompatActivity) getContext(), this::setGroupMemberList);
        groupMembersViewModel.getStates().observe((AppCompatActivity) getContext(), this::setStateChangeObserver);
        groupMembersViewModel.insertAtTop().observe((AppCompatActivity) getContext(), this::notifyInsertedAt);
        groupMembersViewModel.moveToTop().observe((AppCompatActivity) getContext(), this::notifyItemMovedToTop);
        groupMembersViewModel.updateGroupMember().observe((AppCompatActivity) getContext(), this::notifyItemChanged);
        groupMembersViewModel.removeGroupMember().observe((AppCompatActivity) getContext(), this::notifyItemRemoved);
        groupMembersViewModel.getDialogState().observe((AppCompatActivity) getContext(), this::setDialogState);
        groupMembersViewModel.getCometChatException().observe((AppCompatActivity) getContext(), exceptionObserver);

        // Set the menu items if provided
        setMenuItems(null);

        // Set up the back button click event
        binding.ivBack.setOnClickListener(view -> {
            if (onBackPress != null) {
                onBackPress.onBack();
            }
        });

        // Add a text watcher to the search EditText for filtering group members
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Show the clear icon when there is text, and filter the group members
                // based on the search input
                if (charSequence.length() != 0) {
                    binding.ivClear.setVisibility(VISIBLE);
                    groupMembersViewModel.searchGroupMembers(charSequence.toString());
                } else {
                    binding.ivClear.setVisibility(GONE);
                    groupMembersViewModel.searchGroupMembers(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action needed after text changes
            }
        });

        // Set up the clear icon click event to reset the search input
        binding.ivClear.setOnClickListener(view -> {
            binding.etSearch.setText("");
            binding.ivClear.setVisibility(GONE);
        });

        // Set up the discard selection button click event
        binding.ivDiscardSelection.setOnClickListener(v -> {
            // Clear the selection and update the UI accordingly
            hashMap.clear();
            setSelectionCount(0);
            setSelectionCountVisibility(GONE);
            groupMembersAdapter.selectGroupMember(hashMap);
        });

        // Set up the submit selection button click event
        binding.ivSubmitSelection.setOnClickListener(v -> {
            // Submit with the selection if a callback is available, and clear the selection
            if (onSelection != null) {
                onSelection.onSelection(getSelectedGroupMembers());
                hashMap.clear();
                setSelectionCount(0);
                setSelectionCountVisibility(GONE);
                groupMembersAdapter.selectGroupMember(hashMap);
            }
        });

        // Set up the retry button click event for retrying to fetch group members
        binding.retryBtn.setOnClickListener(view -> groupMembersViewModel.fetchGroupMember());

        // Apply style attributes to customize the appearance based on attributes passed
        // during
        // initialization
        applyStyleAttributes(attrs, defStyleAttr, 0);
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatGroupMembers, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatGroupMembers_cometchatGroupMembersStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatGroupMembers, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Extracts attributes from the given {@link TypedArray} and applies default
     * values.
     *
     * @param typedArray The TypedArray containing the view's attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setSearchInputStrokeColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputStrokeColor, 0));
            setSearchInputBackgroundColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputBackgroundColor, 0));
            setSearchInputTextColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputTextColor, 0));
            setSearchInputPlaceHolderTextColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputPlaceHolderTextColor, 0));
            setBackIconTint(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersBackIconTint, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersStrokeColor, 0));
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersBackgroundColor, 0));
            setTitleTextColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersTitleTextColor, 0));
            setEmptyStateTitleTextColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersEmptyStateTitleTextColor, 0));
            setEmptyStateSubtitleTextColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersEmptyStateSubtitleTextColor, 0));
            setErrorStateTitleTextColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersErrorStateTitleTextColor, 0));
            setErrorStateSubtitleTextColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersErrorStateSubtitleTextColor, 0));
            setItemTitleTextColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersItemTitleTextColor, 0));
            setSeparatorColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersSeparatorColor, 0));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatGroupMembers_cometchatGroupMembersStrokeWidth, 0));
            setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatGroupMembers_cometchatGroupMembersCornerRadius, 0));
            setSearchInputStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputStrokeWidth, 0));
            setSearchInputCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputCornerRadius, 0));
            setSearchInputTextAppearance(typedArray.getResourceId(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputTextAppearance, 0));
            setTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatGroupMembers_cometchatGroupMembersTitleTextAppearance, 0));
            setEmptyStateTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatGroupMembers_cometchatGroupMembersEmptyStateTitleTextAppearance, 0));
            setEmptyStateSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatGroupMembers_cometchatGroupMembersEmptyStateSubtitleTextAppearance, 0));
            setErrorStateTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatGroupMembers_cometchatGroupMembersErrorStateTitleTextAppearance, 0));
            setErrorStateSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatGroupMembers_cometchatGroupMembersErrorStateSubtitleTextAppearance, 0));
            setItemTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatGroupMembers_cometchatGroupMembersItemTitleTextAppearance, 0));
            setAvatarStyle(typedArray.getResourceId(R.styleable.CometChatGroupMembers_cometchatGroupMembersAvatarStyle, 0));
            setStatusIndicatorStyle(typedArray.getResourceId(R.styleable.CometChatGroupMembers_cometchatGroupMembersStatusIndicatorStyle, 0));
            setSearchInputStartIcon(typedArray.getDrawable(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputStartIcon));
            setSearchInputEndIcon(typedArray.getDrawable(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputEndIcon));
            setSearchInputStartIconTint(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputStartIconTint, 0));
            setSearchInputEndIconTint(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersSearchInputEndIconTint, 0));
            setBackIcon(typedArray.getDrawable(R.styleable.CometChatGroupMembers_cometchatGroupMembersBackIcon));
            setBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatGroupMembers_cometchatGroupMembersBackgroundDrawable));

            setCheckBoxStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxStrokeWidth, 0));
            setCheckBoxCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxCornerRadius, 0));
            setCheckBoxStrokeColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxStrokeColor, 0));
            setCheckBoxBackgroundColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxBackgroundColor, 0));
            setCheckBoxCheckedBackgroundColor(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxCheckedBackgroundColor, 0));
            setSelectIcon(typedArray.getDrawable(R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxSelectIcon));
            setSelectIconTint(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersCheckBoxSelectIconTint, 0));
            setDiscardSelectionIcon(typedArray.getDrawable(R.styleable.CometChatGroupMembers_cometchatGroupMembersDiscardSelectionIcon));
            setDiscardSelectionIconTint(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersDiscardSelectionIconTint, 0));
            setSubmitSelectionIcon(typedArray.getDrawable(R.styleable.CometChatGroupMembers_cometchatGroupMembersSubmitSelectionIcon));
            setSubmitSelectionIconTint(typedArray.getColor(R.styleable.CometChatGroupMembers_cometchatGroupMembersSubmitSelectionIconTint, 0));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the style of the text bubble from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatTextBubble);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Enables or disables further selection of group members.
     *
     * @param furtherSelectionEnabled True to enable further selection, false to disable it.
     */
    public void setFurtherSelectionEnabled(boolean furtherSelectionEnabled) {
        isFurtherSelectionEnabled = furtherSelectionEnabled;
    }

    public void setTitleText(String title) {
        binding.tvTitle.setText(title);
    }

    /**
     * Sets the custom title text for the error state.
     *
     * @param customErrorStateTitleText The custom error state title text to display.
     */
    public void setCustomErrorStateTitleText(@NonNull String customErrorStateTitleText) {
        this.customErrorStateTitleText = customErrorStateTitleText;
        binding.tvErrorGroupMembersTitle.setText(customErrorStateTitleText);
    }

    /**
     * Sets the custom subtitle text for the error state.
     *
     * @param customErrorStateSubtitleText The custom error state subtitle text to display.
     */
    public void setCustomErrorStateSubtitleText(@NonNull String customErrorStateSubtitleText) {
        this.customErrorStateSubtitleText = customErrorStateSubtitleText;
        binding.tvErrorGroupMembersSubtitle.setText(customErrorStateSubtitleText);
    }

    /**
     * Sets the custom title text for the empty state.
     *
     * @param customEmptyStateTitleText The custom empty state title text to display.
     */
    public void setCustomEmptyStateTitleText(@NonNull String customEmptyStateTitleText) {
        this.customEmptyStateTitleText = customEmptyStateTitleText;
        binding.tvEmptyGroupMembersTitle.setText(customEmptyStateTitleText);
    }

    /**
     * Sets the custom subtitle text for the empty state.
     *
     * @param customEmptyStateSubtitleText The custom empty state subtitle text to display.
     */
    public void setCustomEmptyStateSubtitleText(@NonNull String customEmptyStateSubtitleText) {
        this.customEmptyStateSubtitleText = customEmptyStateSubtitleText;
        binding.tvEmptyGroupMembersSubtitle.setText(customEmptyStateSubtitleText);
    }

    /**
     * Sets the stroke color of the search input field.
     *
     * @param searchInputStrokeColor The color to set for the stroke of the search input field.
     */
    public void setSearchInputStrokeColor(@ColorInt int searchInputStrokeColor) {
        this.searchInputStrokeColor = searchInputStrokeColor;
        binding.groupMemberSearchCard.setStrokeColor(searchInputStrokeColor);
    }

    /**
     * Sets the background color of the search input field.
     *
     * @param searchInputBackgroundColor The color to set for the background of the search input field.
     */
    public void setSearchInputBackgroundColor(@ColorInt int searchInputBackgroundColor) {
        this.searchInputBackgroundColor = searchInputBackgroundColor;
        binding.groupMemberSearchCard.setCardBackgroundColor(searchInputBackgroundColor);
    }

    /**
     * Sets the text color of the search input field.
     *
     * @param searchInputTextColor The color to set for the text of the search input field.
     */
    public void setSearchInputTextColor(@ColorInt int searchInputTextColor) {
        this.searchInputTextColor = searchInputTextColor;
        binding.etSearch.setTextColor(searchInputTextColor);
    }

    /**
     * Sets the placeholder text color of the search input field.
     *
     * @param searchInputPlaceHolderTextColor The color to set for the placeholder text of the search input
     *                                        field.
     */
    public void setSearchInputPlaceHolderTextColor(@ColorInt int searchInputPlaceHolderTextColor) {
        this.searchInputPlaceHolderTextColor = searchInputPlaceHolderTextColor;
        binding.etSearch.setHintTextColor(searchInputPlaceHolderTextColor);
    }

    /**
     * Sets the tint color for the back icon.
     *
     * @param backIconTint The tint color to set for the back icon.
     */
    public void setBackIconTint(@ColorInt int backIconTint) {
        this.backIconTint = backIconTint;
        binding.ivBack.setImageTintList(ColorStateList.valueOf(backIconTint));
    }

    /**
     * Sets the start icon for the search input field.
     *
     * @param searchInputStartIcon The drawable to use as the start icon.
     */
    public void setSearchInputStartIcon(Drawable searchInputStartIcon) {
        this.searchInputStartIcon = searchInputStartIcon;
        binding.ivSearch.setImageDrawable(searchInputStartIcon);
    }

    /**
     * Sets the end icon for the search input field.
     *
     * @param searchInputEndIcon The drawable to use as the end icon.
     */
    public void setSearchInputEndIcon(Drawable searchInputEndIcon) {
        this.searchInputEndIcon = searchInputEndIcon;
        binding.ivClear.setImageDrawable(searchInputEndIcon);
    }

    /**
     * Sets the tint color for the start icon of the search input field.
     *
     * @param searchInputStartIconTint The tint color to set for the start icon.
     */
    public void setSearchInputStartIconTint(@ColorInt int searchInputStartIconTint) {
        this.searchInputStartIconTint = searchInputStartIconTint;
        binding.ivSearch.setImageTintList(ColorStateList.valueOf(searchInputStartIconTint));
    }

    /**
     * Sets the tint color for the end icon of the search input field.
     *
     * @param searchInputEndIconTint The tint color to set for the end icon.
     */
    public void setSearchInputEndIconTint(@ColorInt int searchInputEndIconTint) {
        this.searchInputEndIconTint = searchInputEndIconTint;
        binding.ivClear.setImageTintList(ColorStateList.valueOf(searchInputEndIconTint));
    }

    /**
     * Sets the stroke color of the card.
     *
     * @param strokeColor The color to use for the card's stroke.
     */
    @Override
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }

    /**
     * Sets the background color of the card.
     *
     * @param backgroundColor The color to use for the card's background.
     */
    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
    }

    /**
     * Sets the color of the title text.
     *
     * @param titleTextColor The color to use for the title text.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        binding.tvTitle.setTextColor(titleTextColor);
    }

    /**
     * Sets the color of the empty state title text.
     *
     * @param emptyStateTitleTextColor The color to use for the empty state title text.
     */
    public void setEmptyStateTitleTextColor(@ColorInt int emptyStateTitleTextColor) {
        this.emptyStateTitleTextColor = emptyStateTitleTextColor;
        binding.tvEmptyGroupMembersTitle.setTextColor(emptyStateTitleTextColor);
    }

    /**
     * Sets the color of the empty state subtitle text.
     *
     * @param emptyStateSubtitleTextColor The color to use for the empty state subtitle text.
     */
    public void setEmptyStateSubtitleTextColor(@ColorInt int emptyStateSubtitleTextColor) {
        this.emptyStateSubtitleTextColor = emptyStateSubtitleTextColor;
        binding.tvEmptyGroupMembersSubtitle.setTextColor(emptyStateSubtitleTextColor);
    }

    /**
     * Sets the color of the error state title text.
     *
     * @param errorStateTitleTextColor The color to use for the error state title text.
     */
    public void setErrorStateTitleTextColor(@ColorInt int errorStateTitleTextColor) {
        this.errorStateTitleTextColor = errorStateTitleTextColor;
        binding.tvErrorGroupMembersTitle.setTextColor(errorStateTitleTextColor);
    }

    /**
     * Sets the color of the error state subtitle text.
     *
     * @param errorStateSubtitleTextColor The color to use for the error state subtitle text.
     */
    public void setErrorStateSubtitleTextColor(@ColorInt int errorStateSubtitleTextColor) {
        this.errorStateSubtitleTextColor = errorStateSubtitleTextColor;
        binding.tvErrorGroupMembersSubtitle.setTextColor(errorStateSubtitleTextColor);
    }

    /**
     * Sets the color of the item title text.
     *
     * @param itemTitleTextColor The color to use for the item title text.
     */
    public void setItemTitleTextColor(@ColorInt int itemTitleTextColor) {
        this.itemTitleTextColor = itemTitleTextColor;
        binding.tvSelectionCount.setTextColor(itemTitleTextColor);
        groupMembersAdapter.setItemTitleTextColor(itemTitleTextColor);
    }

    /**
     * Sets the separator color.
     *
     * @param separatorColor The color to use for the separator.
     */
    public void setSeparatorColor(@ColorInt int separatorColor) {
        this.separatorColor = separatorColor;
        binding.viewSeparator.setBackgroundColor(separatorColor);
    }

    /**
     * Sets the stroke width of the card.
     *
     * @param strokeWidth The width of the stroke to set for the card.
     */
    @Override
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    /**
     * Sets the corner radius of the card.
     *
     * @param cornerRadius The radius to set for the corners of the card.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        super.setRadius(cornerRadius);
    }

    /**
     * Sets the height of the separator.
     *
     * @param separatorHeight The height of the separator in pixels.
     */
    public void setSeparatorHeight(@Dimension int separatorHeight) {
        this.separatorHeight = separatorHeight;
        binding.viewSeparator.getLayoutParams().height = separatorHeight;
    }

    /**
     * Sets the stroke width of the search input field.
     *
     * @param searchInputStrokeWidth The width of the stroke to set for the search input field.
     */
    public void setSearchInputStrokeWidth(@Dimension int searchInputStrokeWidth) {
        this.searchInputStrokeWidth = searchInputStrokeWidth;
        binding.groupMemberSearchCard.setStrokeWidth(searchInputStrokeWidth);
    }

    /**
     * Sets the corner radius of the search input field.
     *
     * @param searchInputCornerRadius The radius to set for the corners of the search input field.
     */
    public void setSearchInputCornerRadius(@Dimension int searchInputCornerRadius) {
        this.searchInputCornerRadius = searchInputCornerRadius;
        binding.groupMemberSearchCard.setRadius(searchInputCornerRadius);
    }

    /**
     * Sets the text appearance of the search input field.
     *
     * @param searchInputTextAppearance The style resource to use for the search input field text
     *                                  appearance.
     */
    public void setSearchInputTextAppearance(@StyleRes int searchInputTextAppearance) {
        this.searchInputTextAppearance = searchInputTextAppearance;
        binding.etSearch.setTextAppearance(searchInputTextAppearance);
    }

    /**
     * Sets the text appearance of the title.
     *
     * @param titleTextAppearance The style resource to use for the title text appearance.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.tvTitle.setTextAppearance(titleTextAppearance);
    }

    /**
     * Sets the text appearance of the empty state title.
     *
     * @param emptyStateTitleTextAppearance The style resource to use for the empty state title text
     *                                      appearance.
     */
    public void setEmptyStateTitleTextAppearance(@StyleRes int emptyStateTitleTextAppearance) {
        this.emptyStateTitleTextAppearance = emptyStateTitleTextAppearance;
        binding.tvEmptyGroupMembersTitle.setTextAppearance(emptyStateTitleTextAppearance);
    }

    /**
     * Sets the text appearance of the empty state subtitle.
     *
     * @param emptyStateSubtitleTextAppearance The style resource to use for the empty state subtitle text
     *                                         appearance.
     */
    public void setEmptyStateSubtitleTextAppearance(@StyleRes int emptyStateSubtitleTextAppearance) {
        this.emptyStateSubtitleTextAppearance = emptyStateSubtitleTextAppearance;
        binding.tvEmptyGroupMembersSubtitle.setTextAppearance(emptyStateSubtitleTextAppearance);
    }

    /**
     * Sets the text appearance of the error state title.
     *
     * @param errorStateTitleTextAppearance The style resource to use for the error state title text
     *                                      appearance.
     */
    public void setErrorStateTitleTextAppearance(@StyleRes int errorStateTitleTextAppearance) {
        this.errorStateTitleTextAppearance = errorStateTitleTextAppearance;
        binding.tvErrorGroupMembersTitle.setTextAppearance(errorStateTitleTextAppearance);
    }

    /**
     * Sets the text appearance of the error state subtitle.
     *
     * @param errorStateSubtitleTextAppearance The style resource to use for the error state subtitle text
     *                                         appearance.
     */
    public void setErrorStateSubtitleTextAppearance(@StyleRes int errorStateSubtitleTextAppearance) {
        this.errorStateSubtitleTextAppearance = errorStateSubtitleTextAppearance;
        binding.tvErrorGroupMembersSubtitle.setTextAppearance(errorStateSubtitleTextAppearance);
    }

    /**
     * Sets the text appearance of the item title.
     *
     * @param itemTitleTextAppearance The style resource to use for the item title text appearance.
     */
    public void setItemTitleTextAppearance(@StyleRes int itemTitleTextAppearance) {
        this.itemTitleTextAppearance = itemTitleTextAppearance;
        binding.tvSelectionCount.setTextAppearance(itemTitleTextAppearance);
        groupMembersAdapter.setItemTitleTextAppearance(itemTitleTextAppearance);
    }

    /**
     * Sets the drawable for the back icon.
     *
     * @param backIcon The drawable to use for the back icon.
     */
    public void setBackIcon(Drawable backIcon) {
        this.backIcon = backIcon;
        binding.ivBack.setImageDrawable(backIcon);
    }

    /**
     * Sets the background drawable for the component.
     *
     * @param backgroundDrawable The drawable to use as the background.
     */
    @Override
    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        if (backgroundDrawable != null) {
            this.backgroundDrawable = backgroundDrawable;
            super.setBackgroundDrawable(backgroundDrawable);
        }
    }

    /**
     * Sets the stroke width for the checkbox.
     *
     * @param checkBoxStrokeWidth The width of the stroke to set for the checkbox.
     */
    public void setCheckBoxStrokeWidth(@Dimension int checkBoxStrokeWidth) {
        this.checkBoxStrokeWidth = checkBoxStrokeWidth;
        groupMembersAdapter.setCheckBoxStrokeWidth(checkBoxStrokeWidth);
    }

    /**
     * Sets the corner radius for the checkbox.
     *
     * @param checkBoxCornerRadius The radius to set for the checkbox corners.
     */
    public void setCheckBoxCornerRadius(@Dimension int checkBoxCornerRadius) {
        this.checkBoxCornerRadius = checkBoxCornerRadius;
        groupMembersAdapter.setCheckBoxCornerRadius(checkBoxCornerRadius);
    }

    /**
     * Sets the stroke color for the checkbox.
     *
     * @param checkBoxStrokeColor The color to use for the checkbox stroke.
     */
    public void setCheckBoxStrokeColor(@ColorInt int checkBoxStrokeColor) {
        this.checkBoxStrokeColor = checkBoxStrokeColor;
        groupMembersAdapter.setCheckBoxStrokeColor(checkBoxStrokeColor);
    }

    /**
     * Sets the background color for the checkbox.
     *
     * @param checkBoxBackgroundColor The color to use for the checkbox background.
     */
    public void setCheckBoxBackgroundColor(@ColorInt int checkBoxBackgroundColor) {
        this.checkBoxBackgroundColor = checkBoxBackgroundColor;
        groupMembersAdapter.setCheckBoxBackgroundColor(checkBoxBackgroundColor);
    }

    /**
     * Sets the background color for the checkbox when it is checked.
     *
     * @param checkBoxCheckedBackgroundColor The color to use for the checkbox background when checked.
     */
    public void setCheckBoxCheckedBackgroundColor(@ColorInt int checkBoxCheckedBackgroundColor) {
        this.checkBoxCheckedBackgroundColor = checkBoxCheckedBackgroundColor;
        groupMembersAdapter.setCheckBoxCheckedBackgroundColor(checkBoxCheckedBackgroundColor);
    }

    /**
     * Sets the drawable for the select icon.
     *
     * @param selectIcon The drawable to use for the select icon.
     */
    public void setSelectIcon(Drawable selectIcon) {
        this.selectIcon = selectIcon;
        groupMembersAdapter.setSelectIcon(selectIcon);
    }

    /**
     * Sets the tint color for the select icon.
     *
     * @param selectIconTint The color to use for the select icon tint.
     */
    public void setSelectIconTint(@ColorInt int selectIconTint) {
        this.selectIconTint = selectIconTint;
        groupMembersAdapter.setSelectIconTint(selectIconTint);
    }

    /**
     * Sets the discard selection icon drawable.
     *
     * @param discardSelectionIcon the discard selection icon drawable to set.
     */
    public void setDiscardSelectionIcon(Drawable discardSelectionIcon) {
        this.discardSelectionIcon = discardSelectionIcon;
        binding.ivDiscardSelection.setImageDrawable(discardSelectionIcon);
    }

    /**
     * Sets the discard selection icon tint color.
     *
     * @param discardSelectionIconTint the discard selection icon tint color to set.
     */
    public void setDiscardSelectionIconTint(@ColorInt int discardSelectionIconTint) {
        this.discardSelectionIconTint = discardSelectionIconTint;
        binding.ivDiscardSelection.setImageTintList(ColorStateList.valueOf(discardSelectionIconTint));
    }

    /**
     * Sets the submit selection icon drawable.
     *
     * @param submitSelectionIcon the submit selection icon drawable to set.
     */
    public void setSubmitSelectionIcon(Drawable submitSelectionIcon) {
        this.submitSelectionIcon = submitSelectionIcon;
        binding.ivSubmitSelection.setImageDrawable(submitSelectionIcon);
    }

    /**
     * Sets the submit selection icon tint color.
     *
     * @param submitSelectionIconTint the submit selection icon tint color to set.
     */
    public void setSubmitSelectionIconTint(@ColorInt int submitSelectionIconTint) {
        this.submitSelectionIconTint = submitSelectionIconTint;
        binding.ivSubmitSelection.setImageTintList(ColorStateList.valueOf(submitSelectionIconTint));
    }

    /**
     * Displays a confirmation dialog for a group member action, such as banning or
     * kicking the member.
     *
     * @param groupMember        The {@link GroupMember} on whom the action will be performed.
     * @param title              The title text for the confirmation dialog.
     * @param message            The message text for the confirmation dialog.
     * @param positiveButtonText The text for the positive button.
     * @param negativeButtonText The text for the negative button.
     * @param action             The action to be performed when the positive button is clicked,
     *                           either {@link UIKitConstants.GroupMemberOption#BAN} or
     *                           {@link UIKitConstants.GroupMemberOption#KICK}.
     */
    private void showConfirmationAlertDialog(GroupMember groupMember, String title, String message, String positiveButtonText, String negativeButtonText, String action) {
        deleteAlertDialog = new CometChatConfirmDialog(getContext(), R.style.CometChatConfirmDialogStyle);
        deleteAlertDialog.setConfirmDialogIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_ic_delete, null));
        deleteAlertDialog.setTitleText(title);
        deleteAlertDialog.setSubtitleText(message);
        deleteAlertDialog.setPositiveButtonText(positiveButtonText);
        deleteAlertDialog.setNegativeButtonText(negativeButtonText);
        deleteAlertDialog.setOnPositiveButtonClick(v -> {
            if (UIKitConstants.GroupMemberOption.BAN.equals(action)) {
                groupMembersViewModel.banGroupMember(groupMember);
            } else if (UIKitConstants.GroupMemberOption.KICK.equals(action)) {
                groupMembersViewModel.kickGroupMember(groupMember);
            }
        });
        deleteAlertDialog.setOnNegativeButtonClick(v -> deleteAlertDialog.dismiss());
        deleteAlertDialog.setConfirmDialogElevation(0);
        deleteAlertDialog.setCancelable(false);
        deleteAlertDialog.show();
    }

    /**
     * Updates the state of the confirmation dialog based on the provided
     * {@link UIKitConstants.DialogState}.
     *
     * <p>
     * This method checks the current state of the dialog and performs the
     * appropriate action:
     *
     * <ul>
     * <li>If the state is {@link UIKitConstants.DialogState#SUCCESS}, it dismisses
     * the dialog.
     * <li>If the state is {@link UIKitConstants.DialogState#INITIATED}, it hides
     * the progress bar on the positive button.
     * <li>If the state is {@link UIKitConstants.DialogState#FAILURE}, it dismisses
     * the dialog and shows a toast message indicating an error.
     * </ul>
     *
     * @param state The {@link UIKitConstants.DialogState} indicating the current
     *              state of the dialog.
     */
    private void setDialogState(UIKitConstants.DialogState state) {
        if (deleteAlertDialog != null && deleteAlertDialog.isShowing()) {
            switch (state) {
                case SUCCESS:
                    deleteAlertDialog.dismiss();
                    deleteAlertDialog = null;
                    break;
                case INITIATED:
                    deleteAlertDialog.hidePositiveButtonProgressBar(false);
                    break;
                case FAILURE:
                    deleteAlertDialog.dismiss();
                    Toast.makeText(getContext(), getContext().getString(R.string.cometchat_something_went_wrong), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * Prepares and displays a popup menu for the given group member within the
     * specified group.
     *
     * <p>
     * This method retrieves the menu items based on the provided group member and
     * group. If the {@link #menuItems} function is set, it generates a list of menu
     * items and sets up a click listener to handle user interactions with the menu
     * options. If a menu item has a defined click action, that action is executed;
     * otherwise, the method handles default click events.
     *
     * @param groupMember The {@link GroupMember} for whom the popup menu is being prepared.
     * @param group       The {@link Group} associated with the group member.
     */
    private void preparePopupMenu(GroupMember groupMember, Group group) {
        List<CometChatPopupMenu.MenuItem> optionsArrayList;
        if (menuItems != null) {
            optionsArrayList = menuItems.apply(getContext(), groupMember, group);
            cometchatPopUpMenu.setMenuItems(optionsArrayList);
            cometchatPopUpMenu.setOnMenuItemClickListener((id, name) -> {
                for (CometChatPopupMenu.MenuItem item : optionsArrayList) {
                    if (id.equalsIgnoreCase(item.getId())) {
                        if (item.getOnClick() != null) {
                            item.getOnClick().onClick();
                        } else {
                            handleDefaultClickEvents(item, groupMember);
                        }
                        break;
                    }
                }
                cometchatPopUpMenu.dismiss();
            });
        }
    }

    /**
     * Handles default click events for the given menu item associated with a group
     * member.
     *
     * <p>
     * This method performs specific actions based on the ID of the menu item. If
     * the item ID corresponds to changing the scope of the group member, it
     * displays a bottom sheet dialog to allow the user to change the member's role.
     * If the item ID indicates a ban or kick action, it shows a confirmation dialog
     * to confirm the action.
     *
     * @param item        The {@link CometChatPopupMenu.MenuItem} that was clicked.
     * @param groupMember The {@link GroupMember} associated with the clicked menu item.
     */
    private void handleDefaultClickEvents(CometChatPopupMenu.MenuItem item, GroupMember groupMember) {
        if (item.getId().equalsIgnoreCase(UIKitConstants.GroupMemberOption.CHANGE_SCOPE)) {

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

            CometChatScopeChange cometchatChangeScope = new CometChatScopeChange(getContext());
            cometchatChangeScope.setRoleData(group, groupMember);
            cometchatChangeScope.setScopeChangeCallback(new CometChat.CallbackListener<GroupMember>() {
                @Override
                public void onSuccess(GroupMember member) {
                    bottomSheetDialog.dismiss();
                }

                @Override
                public void onError(CometChatException e) {
                }
            });
            cometchatChangeScope.setOnNegativeButtonClick(bottomSheetDialog::dismiss);
            Utils.showBottomSheet(getContext(), bottomSheetDialog, true, false, cometchatChangeScope);
        }
        if (item.getId().equalsIgnoreCase(UIKitConstants.GroupMemberOption.BAN)) {
            showConfirmationAlertDialog(groupMember, getResources().getString(R.string.cometchat_ban) + " " + groupMember.getName() + " ?", "Are You sure you want to " + getResources().getString(R.string.cometchat_ban).toLowerCase() + " " + groupMember.getName() + "?", getResources().getString(R.string.cometchat_yes), getResources().getString(R.string.cometchat_no), UIKitConstants.GroupMemberOption.BAN);
        } else if (item.getId().equalsIgnoreCase(UIKitConstants.GroupMemberOption.KICK)) {
            showConfirmationAlertDialog(groupMember, getResources().getString(R.string.cometchat_remove) + " " + groupMember.getName() + " ?", "Are You sure you want to " + getResources().getString(R.string.cometchat_remove).toLowerCase() + " " + groupMember.getName() + "?", getResources().getString(R.string.cometchat_yes), getResources().getString(R.string.cometchat_no), UIKitConstants.GroupMemberOption.KICK);
        }
    }

    /**
     * Retrieves the selected group members from the list.
     *
     * @return A list of selected GroupMember objects.
     */
    @NonNull
    public List<GroupMember> getSelectedGroupMembers() {
        List<GroupMember> GroupMemberList = new ArrayList<>();
        for (HashMap.Entry<GroupMember, Boolean> entry : hashMap.entrySet()) {
            GroupMemberList.add(entry.getKey());
        }
        return GroupMemberList;
    }

    /**
     * Called when the view is attached to a window.
     *
     * <p>
     * This method is invoked when the view is attached to a window, allowing the
     * {@link GroupMembersViewModel} to add necessary listeners. This is useful for
     * setting up data binding and responding to live data changes.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        groupMembersViewModel.addListeners();
    }

    /**
     * Called when the view is detached from a window.
     *
     * <p>
     * This method is invoked when the view is detached from a window, allowing the
     * {@link GroupMembersViewModel} to remove any previously added listeners. This
     * helps prevent memory leaks by ensuring that the view model does not continue
     * to listen for changes after the view is no longer visible.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        groupMembersViewModel.removeListeners();
    }

    /**
     * Sets the group for the CometChatGroupMembers view.
     *
     * @param group The Group object to be set.
     */
    public void setGroup(Group group) {
        this.group = group;
        groupMembersAdapter.setGroup(group);
        groupMembersViewModel.setGroup(group);
        groupMembersViewModel.fetchGroupMember();
    }

    /**
     * Sets the options for the CometChatGroupMembers view.
     *
     * @param options The function that provides the options for each group member.
     */
    public void setMenuItems(Function3<Context, GroupMember, Group, List<CometChatPopupMenu.MenuItem>> options) {
        if (options != null) this.menuItems = options;
        else
            this.menuItems = (context, groupMember, group) -> MembersUtils.getDefaultGroupMemberOptions(context, groupMember, group, null);
    }

    /**
     * Disables or enables the presence status of users in the group members list.
     *
     * @param disable If true, users' presence will be disabled. If false, users'
     *                presence will be enabled.
     */
    public void disableUsersPresence(boolean disable) {
        groupMembersAdapter.disableUsersPresence(disable);
    }

    /**
     * Sets the layout resource for the empty state view.
     *
     * @param id The layout resource ID.
     */
    public void setEmptyStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customEmptyStateView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customEmptyStateView = null;
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /**
     * Sets the layout resource for the error state view.
     *
     * @param id The layout resource ID.
     */
    public void setErrorStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customErrorStateView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customErrorStateView = null;
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /**
     * Sets the layout resource for the loading state view.
     *
     * @param id The layout resource ID.
     */
    public void setLoadingStateView(@LayoutRes int id) {
        if (id != 0) {
            try {
                customLoadingView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customLoadingView = null;
                CometChatLogger.e(TAG, e.toString());
            }
        }
    }

    /**
     * Sets the background color for the group members list.
     *
     * @param colorArray  The array of colors to set as the background gradient.
     * @param orientation The orientation of the gradient.
     */
    public void setBackground(int[] colorArray, GradientDrawable.Orientation orientation) {
        GradientDrawable gd = new GradientDrawable(orientation, colorArray);
        setBackground(gd);
    }

    /**
     * Sets the custom view for each group member item in the list.
     *
     * @param listItemView The listener interface that defines callbacks for interactions
     *                     with the group member list item view.
     *                     <p>
     *                     This method allows you to specify a custom view to be used for
     *                     each group member item in the list. The provided
     *                     `GroupMembersViewHolderListeners` interface defines callbacks that
     *                     will be invoked when various interactions occur with the list item
     *                     view, such as clicking, long pressing, or triggering other
     *                     actions.
     *                     <p>
     *                     By implementing the `GroupMembersViewHolderListeners` interface
     *                     and passing an instance to this method, you can customize the
     *                     appearance and behavior of each group member item in the list
     *                     according to your specific needs.
     */
    public void setListItemView(GroupMembersViewHolderListeners listItemView) {
        groupMembersAdapter.setListItemView(listItemView);
    }

    /**
     * Sets the custom view for the subtitle area within group member items in the
     * list.
     *
     * @param subtitleView The listener interface that defines callbacks for interactions
     *                     with the group member subtitle view.
     *                     <p>
     *                     This method allows you to specify a custom view to be displayed
     *                     below the main title or name of each group member item in the
     *                     list. The provided `GroupMembersViewHolderListeners` interface
     *                     defines callbacks that will be invoked when various interactions
     *                     occur with the subtitle view.
     *                     <p>
     *                     By implementing the `GroupMembersViewHolderListeners` interface
     *                     and passing an instance to this method, you can customize the
     *                     appearance and behavior of the subtitle area within each group
     *                     member item according to your specific needs.
     */
    public void setSubtitleView(GroupMembersViewHolderListeners subtitleView) {
        groupMembersAdapter.setSubtitleView(subtitleView);
    }

    /**
     * Sets the custom view for the tail element attached at the end of each group
     * member item in the list.
     *
     * @param tailView The listener interface that defines callbacks for interactions
     *                 with the tail view.
     *                 <p>
     *                 This method allows you to specify a custom view to be displayed at
     *                 the end of each group member item in the list. The tail view can
     *                 be used for various purposes, such as:
     *                 <p>
     *                 * Displaying additional information about the group member *
     *                 Showing interactive elements like buttons or icons * Providing
     *                 visual separation between group members
     *                 <p>
     *                 The provided `GroupMembersViewHolderListeners` interface defines
     *                 callbacks that will be invoked when various interactions occur
     *                 with the tail view, allowing you to customize its behavior based
     *                 on user actions.
     */
    public void setTailView(GroupMembersViewHolderListeners tailView) {
        groupMembersAdapter.setTailView(tailView);
    }

    /**
     * Sets the avatar style for the group members.
     *
     * @param style The avatar style to set.
     */
    public void setAvatarStyle(@StyleRes int style) {
        this.avatarStyle = style;
        groupMembersAdapter.setAvatarStyle(style);
    }

    /**
     * Sets the status indicator style for the group members.
     *
     * @param style The status indicator style to set.
     */
    public void setStatusIndicatorStyle(@StyleRes int style) {
        this.statusIndicatorStyle = style;
        groupMembersAdapter.setStatusIndicatorStyle(style);
    }

    /**
     * Sets the callback for selection events in the group members list.
     *
     * @param onSelection The callback to set.
     */
    public void setOnSelection(OnSelection onSelection) {
        this.onSelection = onSelection;
    }

    /**
     * Sets the request builder for fetching group members.
     *
     * @param groupMembersRequestBuilder The request builder to set.
     */
    public void setGroupMembersRequestBuilder(GroupMembersRequest.GroupMembersRequestBuilder groupMembersRequestBuilder) {
        groupMembersViewModel.setGroupMembersRequestBuilder(groupMembersRequestBuilder);
    }

    /**
     * Sets the request builder for searching group members.
     *
     * @param groupMembersRequestBuilder The request builder to set.
     */
    public void setSearchRequestBuilder(GroupMembersRequest.GroupMembersRequestBuilder groupMembersRequestBuilder) {
        groupMembersViewModel.setSearchRequestBuilder(groupMembersRequestBuilder);
    }

    /**
     * Sets the selection mode for the group members.
     *
     * @param selectionMode The selection mode to set.
     */
    public void setSelectionMode(@NonNull UIKitConstants.SelectionMode selectionMode) {
        hashMap.clear();
        groupMembersAdapter.selectGroupMember(hashMap);
        this.selectionMode = selectionMode;
        if (UIKitConstants.SelectionMode.MULTIPLE.equals(selectionMode) || UIKitConstants.SelectionMode.SINGLE.equals(selectionMode)) {
            isFurtherSelectionEnabled = true;
            groupMembersAdapter.setSelectionEnabled(true);
            setDiscardSelectionIconVisibility(VISIBLE);
            setSubmitSelectionIconVisibility(VISIBLE);
        } else {
            isFurtherSelectionEnabled = false;
            groupMembersAdapter.setSelectionEnabled(false);
            setDiscardSelectionIconVisibility(GONE);
            setSubmitSelectionIconVisibility(GONE);
            binding.ivBack.setVisibility(VISIBLE);
            setSelectionCountVisibility(GONE);
        }
    }

    public void setBackIconVisibility(int visibility) {
        binding.ivBack.setVisibility(visibility);
    }

    /**
     * Sets the selection icon for the group members.
     *
     * @param selectionIcon The selection icon to set.
     */
    public void setSelectionIcon(Drawable selectionIcon) {
        groupMembersAdapter.setSelectionIcon(selectionIcon);
    }

    /**
     * Gets the RecyclerView for the group members list.
     *
     * @return The RecyclerView instance used to display the group members.
     */
    public RecyclerView getRecyclerView() {
        return binding.recyclerviewGroupMembersList;
    }

    /**
     * Gets the ViewModel associated with the group members.
     *
     * @return The GroupMembersViewModel instance used for managing UI-related data
     * in a lifecycle-conscious way.
     */
    public GroupMembersViewModel getViewModel() {
        return groupMembersViewModel;
    }

    /**
     * Gets the adapter used for displaying group member conversations.
     *
     * @return The GroupMembersAdapter instance currently being used for the
     * RecyclerView.
     */
    public GroupMembersAdapter getConversationsAdapter() {
        return groupMembersAdapter;
    }

    /**
     * Sets the adapter for the group members RecyclerView.
     *
     * <p>
     * If the provided adapter is not null, it updates the existing adapter for the
     * RecyclerView and sets it to the new adapter.
     *
     * @param adapter The new GroupMembersAdapter to set for the RecyclerView.
     */
    public void setAdapter(GroupMembersAdapter adapter) {
        if (adapter != null) {
            groupMembersAdapter = adapter;
            binding.recyclerviewGroupMembersList.setAdapter(adapter);
        }
    }

    /**
     * Checks if further selection of group members is enabled.
     *
     * @return {@code true} if further selection is enabled; {@code false}
     * otherwise.
     */
    public boolean isFurtherSelectionEnabled() {
        return isFurtherSelectionEnabled;
    }

    /**
     * Sets up click and long-click event listeners for the group members
     * RecyclerView.
     *
     * <p>
     * The method defines actions for single and long-click events on the group
     * members list. It handles selection of group members and displays a popup menu
     * for additional actions based on the group member's role and the group's
     * scope.
     */
    private void clickEvents() {
        binding.recyclerviewGroupMembersList.addOnItemTouchListener(new RecyclerTouchListener(getContext(), binding.recyclerviewGroupMembersList, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                GroupMember groupMember = (GroupMember) view.getTag(R.string.cometchat_member);
                if (!UIKitConstants.SelectionMode.NONE.equals(selectionMode)) {
                    selectGroupMember(groupMember, selectionMode);
                }
                if (onItemClick != null) onItemClick.click(view, position, groupMember);
            }

            @Override
            public void onLongClick(View view, int position) {
                GroupMember groupMember = (GroupMember) view.getTag(R.string.cometchat_member);
                if (onItemLongClick != null) onItemLongClick.longClick(view, position, groupMember);
                else {
                    preparePopupMenu(groupMember, group);
                    if (CometChatConstants.SCOPE_MODERATOR.equalsIgnoreCase(group.getScope()) && CometChatConstants.SCOPE_PARTICIPANT.equalsIgnoreCase(groupMember.getScope())) {
                        cometchatPopUpMenu.show(view);
                    } else if ((CometChatConstants.SCOPE_ADMIN.equalsIgnoreCase(group.getScope())) && (CometChatConstants.SCOPE_PARTICIPANT.equalsIgnoreCase(groupMember.getScope()) || CometChatConstants.SCOPE_MODERATOR.equalsIgnoreCase(groupMember.getScope()))) {
                        cometchatPopUpMenu.show(view);
                    } else if (CometChatUIKit.getLoggedInUser().getUid().equals(group.getOwner()) && groupMember.getUid() != null && !groupMember.getUid().equals(group.getOwner())) {
                        cometchatPopUpMenu.show(view);
                    }
                }
            }
        }));
    }

    /**
     * Displays the popup menu at the specified view location.
     *
     * @param view The view to which the popup menu will be attached.
     */
    public void showPopupMenus(View view) {
        cometchatPopUpMenu.show(view);
    }

    /**
     * Dismisses the currently displayed popup menu.
     */
    public void dismissPopupMenus() {
        cometchatPopUpMenu.dismiss();
    }

    /**
     * Observer for handling conversation states. Depending on the state, it
     * triggers appropriate methods to handle each state.
     */
    private void setStateChangeObserver(UIKitConstants.States states) {
        switch (states) {
            case LOADING:
                handleLoadingState();
                break;
            case LOADED:
                handleLoadedState();
                break;
            case ERROR:
                handleErrorState();
                break;
            case EMPTY:
                handleEmptyState();
                break;
            case NON_EMPTY:
                handleNonEmptyState();
                break;
            default:
                break;
        }
    }

    /**
     * Handles the loading state by displaying a loading view. If a custom loading
     * view is provided, it shows that; otherwise, it uses a shimmer effect.
     */
    private void handleLoadingState() {
        if (binding.etSearch.getText().toString().trim().isEmpty() && !binding.etSearch.isFocused()) {
            if (customLoadingView != null) {
                binding.groupMembersCustomLayout.setVisibility(View.VISIBLE);
                binding.groupMembersCustomLayout.removeAllViews();
                binding.groupMembersCustomLayout.addView(customLoadingView);
            } else {
                CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(30, R.layout.cometchat_group_member_shimmer);
                binding.shimmerRecyclerviewGroupMembersList.setAdapter(adapter);
                binding.shimmerParentLayout.setVisibility(View.VISIBLE);
                binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
                binding.shimmerEffectFrame.startShimmer();
            }
        }
    }

    /**
     * Handles the loaded state by hiding the shimmer effect and displaying the
     * conversation list.
     */
    private void handleLoadedState() {
        hideShimmer();
        hideAllStates();
        binding.recyclerviewGroupMembersList.setVisibility(View.VISIBLE);
    }

    /**
     * Handles the error state by displaying an error message or a custom error view
     * if provided. It also hides other views that are not relevant during the error
     * state.
     */
    private void handleErrorState() {
        if (groupMembersViewModel.getGroupMemberArrayList().isEmpty()) {
            if (!hideError && customErrorStateView != null) {
                binding.groupMembersCustomLayout.setVisibility(View.VISIBLE);
                binding.groupMembersCustomLayout.removeAllViews();
                binding.groupMembersCustomLayout.addView(customErrorStateView);
            } else {
                hideShimmer();
                hideAllStates();
                binding.tvErrorGroupMembersTitle.setText(customErrorStateTitleText == null ? getResources().getString(R.string.cometchat_error_conversations_title) : customErrorStateTitleText);
                binding.tvErrorGroupMembersSubtitle.setText(customErrorStateSubtitleText == null ? getResources().getString(R.string.cometchat_error_conversations_subtitle) : customErrorStateSubtitleText);
                binding.errorGroupMembersLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Handles the empty state by displaying a message indicating there are no
     * conversations. It shows a custom empty view if provided.
     */
    private void handleEmptyState() {
        if (customEmptyStateView != null) {
            binding.groupMembersCustomLayout.setVisibility(View.VISIBLE);
            binding.groupMembersCustomLayout.removeAllViews();
            binding.groupMembersCustomLayout.addView(customEmptyStateView);
        } else {
            hideShimmer();
            hideErrorState();
            binding.tvEmptyGroupMembersTitle.setText(customEmptyStateTitleText == null ? getResources().getString(R.string.cometchat_empty_conversations_title) : customEmptyStateTitleText);
            binding.tvEmptyGroupMembersSubtitle.setText(customEmptyStateSubtitleText == null ? getResources().getString(R.string.cometchat_empty_conversations_subtitle) : customEmptyStateSubtitleText);
            binding.emptyGroupMembersLayout.setVisibility(View.VISIBLE);
        }
        binding.recyclerviewGroupMembersList.setVisibility(View.GONE);
    }

    /**
     * Handles the non-empty state by ensuring the conversation list is visible.
     */
    private void handleNonEmptyState() {
        hideShimmer();
        hideAllStates();
        binding.recyclerviewGroupMembersList.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the shimmer effect and the parent layout containing it.
     */
    private void hideShimmer() {
        binding.shimmerParentLayout.setVisibility(View.GONE);
        binding.shimmerEffectFrame.stopShimmer();
    }

    /**
     * Hides all state layouts (custom, error, and empty layouts).
     */
    private void hideAllStates() {
        binding.groupMembersCustomLayout.setVisibility(View.GONE);
        binding.errorGroupMembersLayout.setVisibility(View.GONE);
        binding.emptyGroupMembersLayout.setVisibility(View.GONE);
    }

    /**
     * Hides the error state layout.
     */
    private void hideErrorState() {
        binding.errorGroupMembersLayout.setVisibility(View.GONE);
    }

    /**
     * Hides or shows the error state view.
     *
     * @param hide True to hide the error state view, false to show it.
     */
    public void hideError(boolean hide) {
        hideError = hide;
    }

    /**
     * Notifies the adapter that an item has been inserted at the specified position
     * and scrolls to the top of the list.
     *
     * @param position The position at which the item was inserted.
     */
    private void notifyInsertedAt(int position) {
        groupMembersAdapter.notifyItemInserted(position);
        scrollToTop();
    }

    /**
     * Notifies the adapter that an item has moved to the top of the list and
     * scrolls to the top.
     *
     * @param position The current position of the item before moving.
     */
    private void notifyItemMovedToTop(int position) {
        groupMembersAdapter.notifyItemMoved(position, 0);
        groupMembersAdapter.notifyItemChanged(0);
        scrollToTop();
    }

    /**
     * Scrolls the RecyclerView to the top if the first visible item position is
     * less than 5.
     */
    private void scrollToTop() {
        if (layoutManager.findFirstVisibleItemPosition() < 5) layoutManager.scrollToPosition(0);
    }

    /**
     * Notifies the adapter that the item at the specified position has changed.
     *
     * @param position The position of the item that has changed.
     */
    private void notifyItemChanged(int position) {
        groupMembersAdapter.notifyItemChanged(position);
    }

    /**
     * Notifies the adapter that an item has been removed from the specified
     * position.
     *
     * @param position The position of the item that was removed.
     */
    private void notifyItemRemoved(int position) {
        groupMembersAdapter.notifyItemRemoved(position);
    }

    /**
     * Observer for handling exceptions from CometChat operations.
     *
     * <p>
     * When an exception is observed, the onError callback is triggered.
     */
    Observer<CometChatException> exceptionObserver = exception -> {
        if (onError != null) onError.onError(getContext(), exception);
    };

    /**
     * Sets the error callback for handling errors.
     *
     * @param onError The error callback to set.
     */
    public void setOnError(OnError onError) {
        this.onError = onError;
    }

    /**
     * Retrieves the current OnError callback instance.
     *
     * @return The OnError callback instance used for handling errors.
     */
    public OnError getOnError() {
        return onError;
    }

    /**
     * Checks if the error state is hidden.
     *
     * @return {@code true} if the error state is hidden; {@code false} otherwise.
     */
    public boolean isHideError() {
        return hideError;
    }

    /**
     * Retrieves the custom error state view.
     *
     * @return The View representing the custom error state.
     */
    public View getCustomErrorStateView() {
        return customErrorStateView;
    }

    /**
     * Sets the item long click listener for the group members list.
     *
     * @param onItemLongClick The item long click listener to set.
     */
    public void setItemLongClickListener(OnItemLongClick onItemLongClick) {
        if (onItemLongClick != null) this.onItemLongClick = onItemLongClick;
    }

    /**
     * Sets the item click listener for the group members list.
     *
     * @param onItemClickListener The item click listener to set.
     */
    public void setItemClickListener(OnItemClick onItemClickListener) {
        if (onItemClickListener != null) this.onItemClick = onItemClickListener;
    }

    /**
     * Sets a listener to handle back press events.
     *
     * @param onBackPress The listener that will be notified of back press events. If
     *                    {@code null} is provided, the current listener remains unchanged.
     */
    public void setOnBackPressListener(OnBackPress onBackPress) {
        if (onBackPress != null) this.onBackPress = onBackPress;
    }

    /**
     * Selects a group member in the group members list.
     *
     * @param groupMember The group member to select.
     * @param mode        The selection mode.
     */
    public void selectGroupMember(GroupMember groupMember, UIKitConstants.SelectionMode mode) {

        if (mode != null && groupMember != null) {
            this.selectionMode = mode;
            if (UIKitConstants.SelectionMode.SINGLE.equals(selectionMode)) {
                hashMap.clear();
                hashMap.put(groupMember, true);
                groupMembersAdapter.selectGroupMember(hashMap);
            } else if (UIKitConstants.SelectionMode.MULTIPLE.equals(selectionMode)) {
                if (hashMap.containsKey(groupMember)) {
                    hashMap.remove(groupMember);
                } else {
                    if (isFurtherSelectionEnabled) {
                        hashMap.put(groupMember, true);
                    }
                }
                if (hashMap.isEmpty()) {
                    setDiscardSelectionIconVisibility(GONE);
                    setSubmitSelectionIconVisibility(GONE);
                    setSelectionCountVisibility(GONE);
                    setTitleVisibility(VISIBLE);
                } else {
                    setSelectionCount(hashMap.size());
                    setDiscardSelectionIconVisibility(VISIBLE);
                    setSubmitSelectionIconVisibility(VISIBLE);
                    setSelectionCountVisibility(VISIBLE);
                    setTitleVisibility(GONE);
                }
                groupMembersAdapter.selectGroupMember(hashMap);
            }
        }
    }

    /**
     * Sets the visibility of the selection count TextView.
     *
     * @param visibility The visibility state to set. Must be one of {@link View#VISIBLE},
     *                   {@link View#INVISIBLE}, or {@link View#GONE}.
     */
    public void setSelectionCountVisibility(int visibility) {
        binding.tvSelectionCount.setVisibility(visibility);
    }

    /**
     * Sets the visibility of the submit selection icon.
     *
     * @param visibility The visibility state to set. Must be one of {@link View#VISIBLE},
     *                   {@link View#INVISIBLE}, or {@link View#GONE}.
     */
    public void setSubmitSelectionIconVisibility(int visibility) {
        binding.ivSubmitSelection.setVisibility(visibility);
    }

    public void excludeOwner(boolean exclude) {
        groupMembersViewModel.setExcludeOwner(exclude);
    }

    /**
     * Sets the visibility of the discard selection icon. If the icon is set to
     * visible, the back button will be hidden.
     *
     * @param visibility The visibility state to set. Must be one of {@link View#VISIBLE},
     *                   {@link View#INVISIBLE}, or {@link View#GONE}.
     */
    public void setDiscardSelectionIconVisibility(int visibility) {
        binding.ivDiscardSelection.setVisibility(visibility);
        if (visibility == VISIBLE) {
            binding.ivDiscardSelection.setVisibility(View.VISIBLE);
            binding.ivBack.setVisibility(View.GONE);
        }
    }

    /**
     * Sets the visibility of the toolbar.
     *
     * @param visibility The visibility state to set. Must be one of {@link View#VISIBLE},
     *                   {@link View#INVISIBLE}, or {@link View#GONE}.
     */
    public void setToolbarVisibility(int visibility) {
        binding.toolbar.setVisibility(visibility);
    }

    /**
     * Updates the selection count displayed in the TextView.
     *
     * @param count The count to set as the text of the selection count TextView.
     */
    public void setSelectionCount(int count) {
        binding.tvSelectionCount.setText(String.valueOf(count));
    }

    /**
     * Sets the visibility of the title TextView.
     *
     * @param visibility The visibility state to set. Must be one of {@link View#VISIBLE},
     *                   {@link View#INVISIBLE}, or {@link View#GONE}.
     */
    public void setTitleVisibility(int visibility) {
        binding.tvTitle.setVisibility(visibility);
    }

    /**
     * Sets the list of group members for the adapter.
     *
     * @param list The list of {@link GroupMember} objects to be displayed.
     */
    private void setGroupMemberList(List<GroupMember> list) {
        groupMembersAdapter.setGroupMemberList(list);
    }

    /**
     * Gets the stroke color of the search input field.
     *
     * @return The stroke color as an integer.
     */
    public int getSearchInputStrokeColor() {
        return searchInputStrokeColor;
    }

    /**
     * Gets the background color of the search input field.
     *
     * @return The background color as an integer.
     */
    public int getSearchInputBackgroundColor() {
        return searchInputBackgroundColor;
    }

    /**
     * Gets the text color of the search input field.
     *
     * @return The text color as an integer.
     */
    public int getSearchInputTextColor() {
        return searchInputTextColor;
    }

    /**
     * Gets the placeholder text color of the search input field.
     *
     * @return The placeholder text color as an integer.
     */
    public int getSearchInputPlaceHolderTextColor() {
        return searchInputPlaceHolderTextColor;
    }

    /**
     * Gets the tint color applied to the back icon.
     *
     * @return The tint color as an integer.
     */
    public int getBackIconTint() {
        return backIconTint;
    }

    /**
     * Gets the stroke color.
     *
     * @return The stroke color as an integer.
     */
    @Override
    public int getStrokeColor() {
        return strokeColor;
    }

    /**
     * Gets the background color.
     *
     * @return The background color as an integer.
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Gets the text color of the title.
     *
     * @return The title text color as an integer.
     */
    public int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Gets the text color of the empty state title.
     *
     * @return The empty state title text color as an integer.
     */
    public int getEmptyStateTitleTextColor() {
        return emptyStateTitleTextColor;
    }

    /**
     * Gets the text color of the empty state subtitle.
     *
     * @return The empty state subtitle text color as an integer.
     */
    public int getEmptyStateSubtitleTextColor() {
        return emptyStateSubtitleTextColor;
    }

    /**
     * Gets the text color of the error state title.
     *
     * @return The error state title text color as an integer.
     */
    public int getErrorStateTitleTextColor() {
        return errorStateTitleTextColor;
    }

    /**
     * Gets the text color of the error state subtitle.
     *
     * @return The error state subtitle text color as an integer.
     */
    public int getErrorStateSubtitleTextColor() {
        return errorStateSubtitleTextColor;
    }

    /**
     * Gets the text color of the item title.
     *
     * @return The item title text color as an integer.
     */
    public int getItemTitleTextColor() {
        return itemTitleTextColor;
    }

    /**
     * Gets the color of the separator.
     *
     * @return The separator color as an integer.
     */
    public int getSeparatorColor() {
        return separatorColor;
    }

    /**
     * Gets the stroke width.
     *
     * @return The stroke width as an integer.
     */
    @Override
    public int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Gets the corner radius.
     *
     * @return The corner radius as an integer.
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Gets the height of the separator.
     *
     * @return The separator height as an integer.
     */
    public int getSeparatorHeight() {
        return separatorHeight;
    }

    /**
     * Gets the stroke width of the search input field.
     *
     * @return The search input stroke width as an integer.
     */
    public int getSearchInputStrokeWidth() {
        return searchInputStrokeWidth;
    }

    /**
     * Gets the corner radius of the search input field.
     *
     * @return The search input corner radius as an integer.
     */
    public int getSearchInputCornerRadius() {
        return searchInputCornerRadius;
    }

    /**
     * Gets the stroke width of the checkbox.
     *
     * @return The checkbox stroke width as an integer.
     */
    public int getCheckBoxStrokeWidth() {
        return checkBoxStrokeWidth;
    }

    /**
     * Gets the corner radius of the checkbox.
     *
     * @return The checkbox corner radius as an integer.
     */
    public int getCheckBoxCornerRadius() {
        return checkBoxCornerRadius;
    }

    /**
     * Gets the text appearance of the search input field.
     *
     * @return The search input text appearance as an integer.
     */
    public int getSearchInputTextAppearance() {
        return searchInputTextAppearance;
    }

    /**
     * Gets the text appearance of the title.
     *
     * @return The title text appearance as an integer.
     */
    public int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Gets the text appearance of the empty state title.
     *
     * @return The empty state title text appearance as an integer.
     */
    public int getEmptyStateTitleTextAppearance() {
        return emptyStateTitleTextAppearance;
    }

    /**
     * Gets the text appearance of the empty state subtitle.
     *
     * @return The empty state subtitle text appearance as an integer.
     */
    public int getEmptyStateSubtitleTextAppearance() {
        return emptyStateSubtitleTextAppearance;
    }

    /**
     * Gets the text appearance of the error state title.
     *
     * @return The error state title text appearance as an integer.
     */
    public int getErrorStateTitleTextAppearance() {
        return errorStateTitleTextAppearance;
    }

    /**
     * Gets the text appearance of the error state subtitle.
     *
     * @return The error state subtitle text appearance as an integer.
     */
    public int getErrorStateSubtitleTextAppearance() {
        return errorStateSubtitleTextAppearance;
    }

    /**
     * Gets the text appearance of the item title.
     *
     * @return The item title text appearance as an integer.
     */
    public int getItemTitleTextAppearance() {
        return itemTitleTextAppearance;
    }

    /**
     * Gets the style of the avatar.
     *
     * @return The avatar style as an integer.
     */
    public int getAvatarStyle() {
        return avatarStyle;
    }

    /**
     * Gets the style of the status indicator.
     *
     * @return The status indicator style as an integer.
     */
    public int getStatusIndicatorStyle() {
        return statusIndicatorStyle;
    }

    /**
     * Gets the style.
     *
     * @return The style as an integer.
     */
    public int getStyle() {
        return style;
    }

    /**
     * Gets the drawable for the discard selection icon.
     *
     * @return The discard selection icon as a {@link Drawable}.
     */
    public Drawable getDiscardSelectionIcon() {
        return discardSelectionIcon;
    }

    /**
     * Gets the tint color applied to the discard selection icon.
     *
     * @return The tint color for the discard selection icon as an integer.
     */
    public int getDiscardSelectionIconTint() {
        return discardSelectionIconTint;
    }

    /**
     * Gets the drawable for the submit selection icon.
     *
     * @return The submit selection icon as a {@link Drawable}.
     */
    public Drawable getSubmitSelectionIcon() {
        return submitSelectionIcon;
    }

    /**
     * Gets the tint color applied to the submit selection icon.
     *
     * @return The tint color for the submit selection icon as an integer.
     */
    public int getSubmitSelectionIconTint() {
        return submitSelectionIconTint;
    }

    /**
     * Gets the drawable for the start icon of the search input field.
     *
     * @return The start icon as a {@link Drawable}.
     */
    public Drawable getSearchInputStartIcon() {
        return searchInputStartIcon;
    }

    /**
     * Gets the drawable for the end icon of the search input field.
     *
     * @return The end icon as a {@link Drawable}.
     */
    public Drawable getSearchInputEndIcon() {
        return searchInputEndIcon;
    }

    /**
     * Gets the tint color applied to the start icon of the search input field.
     *
     * @return The tint color for the start icon as an integer.
     */
    public int getSearchInputStartIconTint() {
        return searchInputStartIconTint;
    }

    /**
     * Gets the tint color applied to the end icon of the search input field.
     *
     * @return The tint color for the end icon as an integer.
     */
    public int getSearchInputEndIconTint() {
        return searchInputEndIconTint;
    }

    /**
     * Gets the drawable for the back icon.
     *
     * @return The back icon as a {@link Drawable}.
     */
    public Drawable getBackIcon() {
        return backIcon;
    }

    /**
     * Gets the background drawable.
     *
     * @return The background drawable as a {@link Drawable}.
     */
    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * Gets the drawable for the select icon.
     *
     * @return The select icon as a {@link Drawable}.
     */
    public Drawable getSelectIcon() {
        return selectIcon;
    }

    /**
     * Gets the tint color applied to the select icon.
     *
     * @return The tint color for the select icon as an integer.
     */
    public int getSelectIconTint() {
        return selectIconTint;
    }

    /**
     * Gets the stroke color of the checkbox.
     *
     * @return The checkbox stroke color as an integer.
     */
    public int getCheckBoxStrokeColor() {
        return checkBoxStrokeColor;
    }

    /**
     * Gets the background color of the checkbox.
     *
     * @return The checkbox background color as an integer.
     */
    public int getCheckBoxBackgroundColor() {
        return checkBoxBackgroundColor;
    }

    /**
     * Gets the background color of the checkbox when checked.
     *
     * @return The checked background color of the checkbox as an integer.
     */
    public int getCheckBoxCheckedBackgroundColor() {
        return checkBoxCheckedBackgroundColor;
    }
}

package com.cometchat.chatuikit.extensions.collaborative.whiteboard;

import android.content.Context;
import android.text.SpannableString;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.extensions.Extensions;
import com.cometchat.chatuikit.extensions.collaborative.CollaborativeBoardBubbleConfiguration;
import com.cometchat.chatuikit.extensions.collaborative.CollaborativeUtils;
import com.cometchat.chatuikit.extensions.reaction.ExtensionResponseListener;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.framework.DataSource;
import com.cometchat.chatuikit.shared.framework.DataSourceDecorator;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageComposerAction;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.viewholders.MessagesViewHolderListener;
import com.cometchat.chatuikit.shared.views.cometchatmessagebubble.CometChatMessageBubble;

import java.util.HashMap;
import java.util.List;

public class CollaborativeWhiteboardExtensionDecorator extends DataSourceDecorator {
    private static final String TAG = CollaborativeWhiteboardExtensionDecorator.class.getSimpleName();


    private final String collaborativeWhiteBoardExtensionTypeConstant = ExtensionConstants.ExtensionType.WHITEBOARD;
    private CollaborativeBoardBubbleConfiguration configuration;

    public CollaborativeWhiteboardExtensionDecorator(DataSource dataSource) {
        super(dataSource);
    }

    public CollaborativeWhiteboardExtensionDecorator(DataSource dataSource, CollaborativeBoardBubbleConfiguration configuration) {
        super(dataSource);
        this.configuration = configuration;
    }

    @Override
    public List<String> getDefaultMessageTypes() {
        List<String> types = super.getDefaultMessageTypes();
        if (!types.contains(collaborativeWhiteBoardExtensionTypeConstant)) {
            types.add(collaborativeWhiteBoardExtensionTypeConstant);
        }
        return types;
    }

    @Override
    public List<String> getDefaultMessageCategories() {
        List<String> categories = super.getDefaultMessageCategories();
        if (!categories.contains(UIKitConstants.MessageCategory.CUSTOM))
            categories.add(UIKitConstants.MessageCategory.CUSTOM);
        return categories;
    }

    @Override
    public List<CometChatMessageTemplate> getMessageTemplates(AdditionParameter additionParameter) {
        List<CometChatMessageTemplate> templates = super.getMessageTemplates(additionParameter);
        templates.add(getWhiteBoardTemplate(additionParameter));
        return templates;
    }

    @Override
    public List<CometChatMessageComposerAction> getAttachmentOptions(Context context, User user, Group group, HashMap<String, String> idMap) {
        if (!idMap.containsKey(UIKitConstants.MapId.PARENT_MESSAGE_ID)) {

            List<CometChatMessageComposerAction> messageComposerActions = super.getAttachmentOptions(context, user, group, idMap);
            messageComposerActions.add(new CometChatMessageComposerAction().setId(ExtensionConstants.ExtensionType.WHITEBOARD).setTitle(context.getString(R.string.cometchat_collaborative_whiteboard)).setIcon(R.drawable.cometchat_ic_conversations_collaborative_whiteboard).setTitleColor(CometChatTheme.getTextColorPrimary(context)).setTitleAppearance(CometChatTheme.getTextAppearanceBodyRegular(context)).setIconTintColor(CometChatTheme.getIconTintHighlight(context)).setBackground(CometChatTheme.getBackgroundColor1(context)).setOnClick(() -> {
                String id, type;
                id = user != null ? user.getUid() : group.getGuid();
                type = user != null ? UIKitConstants.ReceiverType.USER : UIKitConstants.ReceiverType.GROUP;
                Extensions.callWhiteBoardExtension(id, type, new ExtensionResponseListener() {
                    @Override
                    public void OnResponseSuccess(Object var) {
                    }

                    @Override
                    public void OnResponseFailed(CometChatException e) {
                        showError(context);
                    }
                });
            }));
            return messageComposerActions;
        } else return super.getAttachmentOptions(context, user, group, idMap);
    }

    private void showError(Context context) {
        String errorMessage = context.getString(R.string.cometchat_something_went_wrong);
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
    }

    public CometChatMessageTemplate getWhiteBoardTemplate(AdditionParameter additionParameter) {
        return new CometChatMessageTemplate().setCategory(UIKitConstants.MessageCategory.CUSTOM).setType(collaborativeWhiteBoardExtensionTypeConstant).setOptions((context, baseMessage, isLeftAlign) -> ChatConfigurator.getDataSource().getCommonOptions(context, baseMessage, isLeftAlign)).setContentView(new MessagesViewHolderListener() {
            @NonNull
            @Override
            public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                return CollaborativeUtils.getCollaborativeBubbleView(context, configuration, "Collaborative Whiteboard", "Open whiteboard to draw together.", "Open Whiteboard");
            }

            @Override
            public void bindView(Context context, View createdView, BaseMessage message, UIKitConstants.MessageBubbleAlignment alignment, RecyclerView.ViewHolder holder, List<BaseMessage> messageList, int position) {
                CollaborativeUtils.bindWhiteBordCollaborativeBubble(context, createdView, message.getSender().getUid().equals(CometChatUIKit.getLoggedInUser().getUid()) ? additionParameter.getOutgoingCollaborativeBubbleStyle() : additionParameter.getIncomingCollaborativeBubbleStyle(), message, additionParameter);
            }
        }).setBottomView(new MessagesViewHolderListener() {
            @Override
            public View createView(Context context, CometChatMessageBubble messageBubble, UIKitConstants.MessageBubbleAlignment alignment) {
                return CometChatUIKit.getDataSource().getBottomView(context, messageBubble, alignment);
            }

            @Override
            public void bindView(Context context, View createdView, BaseMessage message, UIKitConstants.MessageBubbleAlignment alignment, RecyclerView.ViewHolder holder, List<BaseMessage> messageList, int position) {
                CometChatUIKit.getDataSource().bindBottomView(context, createdView, message, alignment, holder, messageList, position, additionParameter);
            }
        });
    }

    @Override
    public SpannableString getLastConversationMessage(Context context, Conversation conversation, AdditionParameter additionParameter) {
        if (conversation != null && conversation.getLastMessage() != null && (UIKitConstants.MessageCategory.CUSTOM.equals(conversation.getLastMessage().getCategory()) && ExtensionConstants.ExtensionType.WHITEBOARD.equalsIgnoreCase(conversation.getLastMessage().getType())))
            return SpannableString.valueOf(getLastConversationMessage_(context, conversation, additionParameter));
        else return super.getLastConversationMessage(context, conversation, additionParameter);
    }

    public String getLastConversationMessage_(Context context, Conversation conversation, AdditionParameter additionParameter) {
        String lastMessageText;
        BaseMessage baseMessage = conversation.getLastMessage();
        if (baseMessage != null) {
            String message = getLastMessage(context, baseMessage);
            if (message != null) {
                lastMessageText = message;
            } else
                lastMessageText = String.valueOf(super.getLastConversationMessage(context, conversation, additionParameter));
            if (baseMessage.getDeletedAt() > 0) {
                lastMessageText = context.getString(R.string.cometchat_this_message_deleted);
            }
        } else {
            lastMessageText = context.getResources().getString(R.string.cometchat_start_conv_hint);
        }
        return lastMessageText;
    }

    public String getLastMessage(Context context, BaseMessage lastMessage) {
        return Utils.getMessagePrefix(lastMessage, context) + context.getString(R.string.cometchat_custom_message_whiteboard);
    }

    @Override
    public String getId() {
        return CollaborativeWhiteboardExtensionDecorator.class.getSimpleName();
    }
}
package com.cometchat.chatuikit.messageinformation;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.MessageReceipt;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MessageInformationViewModel extends ViewModel {
    private static final String TAG = MessageInformationViewModel.class.getSimpleName();

    private String LISTENERS_TAG;
    private BaseMessage message;
    private List<MessageReceipt> messageReceipts;
    private final MutableLiveData<List<MessageReceipt>> liveListData;
    private final MutableLiveData<Integer> updateReceipt;
    private final MutableLiveData<Integer> removeReceipt;
    private final MutableLiveData<Integer> addReceipt;
    private final MutableLiveData<Void> clearList;
    private final MutableLiveData<CometChatException> exceptionMutableLiveData;
    private final MutableLiveData<UIKitConstants.States> state;

    public MessageInformationViewModel() {
        messageReceipts = new ArrayList<>();
        liveListData = new MutableLiveData<>();
        updateReceipt = new MutableLiveData<>();
        addReceipt = new MutableLiveData<>();
        removeReceipt = new MutableLiveData<>();
        exceptionMutableLiveData = new MutableLiveData<>();
        clearList = new MutableLiveData<>();
        state = new MutableLiveData<>();
    }

    public MutableLiveData<Void> clearList() {
        return clearList;
    }

    public MutableLiveData<CometChatException> exceptionMutableLiveData() {
        return exceptionMutableLiveData;
    }

    public MutableLiveData<Integer> updateReceipt() {
        return updateReceipt;
    }

    public MutableLiveData<Integer> removeReceipt() {
        return removeReceipt;
    }

    public MutableLiveData<Integer> addReceipt() {
        return addReceipt;
    }

    public List<MessageReceipt> getMessageReceipts() {
        return messageReceipts;
    }

    public MutableLiveData<List<MessageReceipt>> getLiveListData() {
        return liveListData;
    }

    public MutableLiveData<UIKitConstants.States> getState() {
        return state;
    }

    public void setMessage(BaseMessage message) {
        if (message != null) {
            this.message = message;
            if (message.getReceiverType().equalsIgnoreCase(UIKitConstants.ReceiverType.USER)) {
                messageReceipts.clear();
                MessageReceipt messageReceipt = Utils.createMessageReceipt(message);
                if (messageReceipt.getDeliveredAt() != 0) {
                    addToTop(Utils.createMessageReceipt(message));
                    liveListData.setValue(messageReceipts);
                } else state.setValue(UIKitConstants.States.EMPTY);
            } else if (message.getReceiverType().equalsIgnoreCase(UIKitConstants.ReceiverType.GROUP)) {
                fetchMessageReceipt();
            }
        }
    }

    public void addListener() {
        LISTENERS_TAG = System.currentTimeMillis() + "";
        CometChatMessageEvents.addListener(LISTENERS_TAG, new CometChatMessageEvents() {

            @Override
            public void onMessagesDelivered(MessageReceipt messageReceipt) {
                if (message != null && messageReceipt.getMessageId() == message.getId()) {
                    setOrUpdate(messageReceipt);
                }
            }

            @Override
            public void onMessagesRead(MessageReceipt messageReceipt) {
                if (message != null && messageReceipt.getMessageId() == message.getId()) {
                    setOrUpdate(messageReceipt);
                }
            }
        });
    }

    public void removeListener() {
        CometChatMessageEvents.removeListener(LISTENERS_TAG);
    }

    public void fetchMessageReceipt() {
        if (message != null) {
            state.setValue(UIKitConstants.States.LOADING);
            CometChat.getMessageReceipts(message.getId(), new CometChat.CallbackListener<List<MessageReceipt>>() {
                @Override
                public void onSuccess(List<MessageReceipt> messageReceipts) {
                    setList(messageReceipts);
                    state.setValue(messageReceipts.isEmpty() ? UIKitConstants.States.EMPTY : UIKitConstants.States.LOADED);
                }

                @Override
                public void onError(CometChatException e) {
                    Log.e(TAG, "Error: " + e);
                    exceptionMutableLiveData.setValue(e);
                    state.setValue(UIKitConstants.States.ERROR);
                }
            });
        } else {
            state.setValue(UIKitConstants.States.LOADING);
        }
    }

    public void setList(List<MessageReceipt> messageReceipts) {
        if (messageReceipts != null && !messageReceipts.isEmpty()) {
            this.messageReceipts = messageReceipts;
            liveListData.setValue(messageReceipts);
        }
    }

    public void addToTop(MessageReceipt messageReceipt) {
        if (messageReceipt != null) {
            messageReceipts.add(0, messageReceipt);
            addReceipt.setValue(0);
            if (messageReceipts.size() == 1) state.setValue(UIKitConstants.States.NON_EMPTY);
        }
    }

    public void update(MessageReceipt messageReceipt) {
        int i = isPresent(messageReceipt);
        if (i > -1) {
            MessageReceipt tempMessageReceipt = messageReceipts.get(i);
            tempMessageReceipt.setDeliveredAt(messageReceipt.getDeliveredAt() == 0 ? tempMessageReceipt.getDeliveredAt() : messageReceipt.getDeliveredAt());
            tempMessageReceipt.setReadAt(messageReceipt.getReadAt() == 0 ? tempMessageReceipt.getReadAt() : messageReceipt.getReadAt());
            messageReceipts.set(i, tempMessageReceipt);
            updateReceipt.setValue(i);
            state.setValue(UIKitConstants.States.NON_EMPTY);
        }
    }

    public int isPresent(MessageReceipt messageReceipt) {
        for (int i = 0; i < messageReceipts.size(); i++) {
            if (messageReceipts.get(i).getSender().getUid().equalsIgnoreCase(messageReceipt.getSender().getUid())) {
                return i;
            }
        }
        return -1;
    }

    public void remove(MessageReceipt messageReceipt) {
        int index = messageReceipts.indexOf(messageReceipt);
        messageReceipts.remove(messageReceipt);
        removeReceipt.setValue(index);
        if (messageReceipts.isEmpty()) state.setValue(UIKitConstants.States.EMPTY);
    }

    public void clear() {
        messageReceipts.clear();
        clearList.setValue(null);
        state.setValue(UIKitConstants.States.EMPTY);
    }

    public void setOrUpdate(MessageReceipt messageReceipt) {
        if (isPresent(messageReceipt) > -1) {
            update(messageReceipt);
        } else {
            addToTop(messageReceipt);
        }
    }

    public BaseMessage getMessage() {
        return message;
    }

    public MutableLiveData<Integer> getUpdateReceipt() {
        return updateReceipt;
    }

    public MutableLiveData<Integer> getRemoveReceipt() {
        return removeReceipt;
    }

    public MutableLiveData<Integer> getAddReceipt() {
        return addReceipt;
    }

    public MutableLiveData<Void> getClearList() {
        return clearList;
    }

    public MutableLiveData<CometChatException> getExceptionMutableLiveData() {
        return exceptionMutableLiveData;
    }
}
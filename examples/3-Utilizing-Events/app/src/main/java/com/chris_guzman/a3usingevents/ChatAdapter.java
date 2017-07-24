package com.chris_guzman.a3usingevents;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.MarkedAsSeenListener;
import com.nexmo.sdk.conversation.client.event.EventType;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final String TAG = "ChatAdapter";
    private List<Text> messages;
    private Member self;
    private MarkedAsSeenListener markedAsSeenListener = new MarkedAsSeenListener() {
        @Override
        public void onMarkedAsSeen() {
        }

        @Override
        public void onError(int errCode, String errMessage) {
            Log.d(TAG, "Error onMarkedAsSeen: " + errMessage);
        }
    };

    public ChatAdapter(List<Text> texts, Member self) {
        messages = texts;
        this.self = self;
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.chat_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChatAdapter.ViewHolder holder, int position) {
        Text textMessage = messages.get(position);
        if (!textMessage.getMember().equals(self)) {
            textMessage.markAsSeen(markedAsSeenListener);
        }
        if (textMessage.getType().equals(EventType.TEXT)) {
            holder.text.setText(textMessage.getText());
            if (!textMessage.getSeenReceipts().isEmpty()) {
                holder.seenIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView text;
        private final ImageView seenIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_chat_txt);
            seenIcon = (ImageView) itemView.findViewById(R.id.item_chat_seen_img);
        }
    }
}

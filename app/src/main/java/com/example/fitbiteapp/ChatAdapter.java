package com.example.fitbiteapp;

import android.content.Context; // Add this
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.example.fitbiteapp.R;

import java.util.List;

// Import Markwon
import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;
    private Markwon markwon; // Declare Markwon

    // Update constructor to require Context
    public ChatAdapter(List<ChatMessage> messages, Context context) {
        this.messages = messages;
        this.markwon = Markwon.create(context); // Initialize Markwon
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        // USE MARKWON INSTEAD OF setText()
        markwon.setMarkdown(holder.txtMessage, message.getText());

        Context context = holder.itemView.getContext();

        if (message.getRole().equals("user")) {
            // Align Right
            holder.messageContainer.setGravity(Gravity.END);

            // Use your app's R.attr
            int bgUser = MaterialColors.getColor(holder.itemView, io.noties.markwon.R.attr.colorPrimary);
            int textUser = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnPrimary);

            holder.messageCard.setCardBackgroundColor(bgUser);
            holder.txtMessage.setTextColor(textUser);

        } else {
            // Align Left
            holder.messageContainer.setGravity(Gravity.START);

            // Use your app's R.attr
            int bgAssistant = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorSurfaceVariant);
            int textAssistant = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnSurfaceVariant);

            holder.messageCard.setCardBackgroundColor(bgAssistant);
            holder.txtMessage.setTextColor(textAssistant);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        MaterialCardView messageCard;
        TextView txtMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            messageCard = itemView.findViewById(R.id.messageCard);
            txtMessage = itemView.findViewById(R.id.txtMessage);
        }
    }
}
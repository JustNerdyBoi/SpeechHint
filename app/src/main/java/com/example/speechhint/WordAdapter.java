package com.example.speechhint;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
    private final LinkedText linkedText;
    private final OnWordClickListener listener;
    private int bufferBeforeSize;
    private int bufferAfterSize;
    private int currentPosition = -1;

    public interface OnWordClickListener {
        void onWordClick(String word, int position);
    }

    public WordAdapter(LinkedText linkedText, OnWordClickListener listener, int bufferBeforeSize, int bufferAfterSize) {
        this.linkedText = linkedText;
        this.listener = listener;
        this.bufferBeforeSize = bufferBeforeSize;
        this.bufferAfterSize = bufferAfterSize;
    }

    public void setCurrentPosition(int position) {
        int oldPosition = currentPosition;
        currentPosition = position;
        if (oldPosition != position) {
            notifyItemRangeChanged(Math.max(0, oldPosition - bufferBeforeSize), Math.min(bufferBeforeSize + 1 + bufferAfterSize, linkedText.size() - oldPosition + bufferBeforeSize));
            notifyItemRangeChanged(Math.max(0, position - bufferBeforeSize), Math.min(bufferAfterSize + 1 + bufferAfterSize, linkedText.size() - position + bufferBeforeSize));
        } else {
            notifyItemRangeChanged(Math.max(0, position - bufferBeforeSize), Math.min(bufferAfterSize + 1 + bufferAfterSize, linkedText.size() - position + bufferBeforeSize));
        }
    }

    public void setBufferAfterSize(int bufferAfterSize) {
        this.bufferAfterSize = bufferAfterSize;
    }

    public void setBufferBeforeSize(int bufferBeforeSize) {
        this.bufferBeforeSize = bufferBeforeSize;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        String word = linkedText.getWord(position);
        if (word != null) {
            holder.bind(word, position);
        }
    }

    @Override
    public int getItemCount() {
        return linkedText.size();
    }

    class WordViewHolder extends RecyclerView.ViewHolder {
        private final TextView wordTextView;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView);
        }

        void bind(String word, int position) {
            wordTextView.setText(word);

            if (currentPosition == position) {
                wordTextView.setBackgroundResource(R.drawable.current_word_background);
            } else if (currentPosition - bufferBeforeSize <= position && position <= currentPosition + bufferAfterSize) {
                wordTextView.setBackgroundResource(R.drawable.buffer_word_background);
            } else {
                wordTextView.setBackgroundResource(R.drawable.word_background);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWordClick(word, position);
                }
            });
        }
    }
} 
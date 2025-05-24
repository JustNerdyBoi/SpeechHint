package com.example.speechhint.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.speechhint.R;
import com.example.speechhint.utils.LinkedText;
import com.example.speechhint.viewmodels.MainViewModel;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
    private final LinkedText linkedText;
    private final OnWordClickListener clickListener;
    private final OnWordLongClickListener longClickListener;
    private final MainViewModel viewModel;
    private int currentPosition = 0;
    private int bufferBeforeSize;
    private int bufferAfterSize;
    private boolean showCurrentWord;
    private boolean showBuffer;

    public interface OnWordClickListener {
        void focusOnWord(int position);
    }

    public interface OnWordLongClickListener {
        void onWordLongClick(String word, int position);
    }

    public WordAdapter(LinkedText linkedText, OnWordClickListener clickListener,
                       OnWordLongClickListener longClickListener, MainViewModel viewModel,
                       int bufferBeforeSize, int bufferAfterSize) {
        this.linkedText = linkedText;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.viewModel = viewModel;
        this.bufferBeforeSize = bufferBeforeSize;
        this.bufferAfterSize = bufferAfterSize;
        boolean useVoiceDetection = viewModel.getUseVoiceDetection().getValue();
        showCurrentWord = viewModel.getShowCurrentWord().getValue() && useVoiceDetection;
        showBuffer = viewModel.getShowBuffer().getValue() && useVoiceDetection;
    }

    public void setCurrentPosition(int position) {
        int oldPosition = currentPosition;
        currentPosition = position;
        if (oldPosition != position) {
            if (showBuffer) {
                int start = Math.max(0, Math.min(oldPosition, position) - bufferBeforeSize);
                int end = Math.min(linkedText.size(), Math.max(oldPosition, position) + bufferAfterSize + 1);
                if (end - start < (bufferBeforeSize + bufferAfterSize) * 2) {
                    notifyItemRangeChanged(start, end - start);
                } else {
                    notifyItemRangeChanged(Math.max(0, oldPosition - bufferBeforeSize), Math.min(bufferBeforeSize + 1 + bufferAfterSize, linkedText.size() - oldPosition + bufferBeforeSize));
                    notifyItemRangeChanged(Math.max(0, position - bufferBeforeSize), Math.min(bufferAfterSize + 1 + bufferAfterSize, linkedText.size() - position + bufferBeforeSize));
                }
            } else if (showCurrentWord) {
                notifyItemChanged(currentPosition);
                notifyItemChanged(oldPosition);
            }
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
            if (word.equals("\n")) {
                itemView.setTag("\n");
                wordTextView.setVisibility(View.GONE);
            } else {
                itemView.setTag(null);
                wordTextView.setVisibility(View.VISIBLE);
                wordTextView.setText(word);
                wordTextView.setTextSize(viewModel.getTextScale().getValue());
            }

            if (showCurrentWord && currentPosition == position) {
                wordTextView.setBackgroundResource(R.color.current_word_background);
            } else if (showBuffer && currentPosition - bufferBeforeSize <= position && position <= currentPosition + bufferAfterSize) {
                wordTextView.setBackgroundResource(R.color.buffer_word_background);
            } else {
                wordTextView.setBackgroundResource(R.color.word_background);
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null && !word.equals("\n")) {
                    clickListener.focusOnWord(position);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null && !word.equals("\n")) {
                    longClickListener.onWordLongClick(word, position);
                    return true;
                }
                return false;
            });
        }
    }

    public void setShowCurrentWord(boolean showCurrentWord) {
        this.showCurrentWord = showCurrentWord;
    }

    public void setShowBuffer(boolean showBuffer) {
        this.showBuffer = showBuffer;
    }
} 
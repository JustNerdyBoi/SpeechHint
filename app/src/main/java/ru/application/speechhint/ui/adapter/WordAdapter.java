package ru.application.speechhint.ui.adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.Word;
import ru.application.speechhint.R;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
    private final Document document;
    private final OnWordClickListener listener;
    private final int textSize;

    public WordAdapter(Document document, int textSize, OnWordClickListener listener) {
        this.document = document;
        this.listener = listener;
        this.textSize = textSize;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.word_item, parent, false);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        return new WordViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = document.getWords().get(position);
        holder.bind(word, position);
    }

    @Override
    public int getItemCount() {
        return document.getWords() != null ? document.getWords().size() : 0;
    }

    class WordViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }

        public void bind(final Word word, final int position) {
            if ("\n".equals(word.getText())) {
                textView.setVisibility(View.GONE); // Не отображаем перенос
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setText(word.getText());
                textView.setOnClickListener(v -> {
                    if (listener != null) listener.onWordClick(word, position);
                });
                textView.setOnLongClickListener(v -> {
                    if (listener != null) listener.onWordLongClick(word, position, v);
                    return true;
                });
            }
        }
    }

    public interface OnWordClickListener {
        void onWordClick(Word word, int position);
        void onWordLongClick(Word word, int position, View anchor);
    }
}
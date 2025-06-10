package ru.application.speechhint.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ru.application.domain.repository.SpeechRecognitionRepository;
import ru.application.domain.usecase.SpeechRecognitionUseCase;

@HiltViewModel
public class SpeechRecognitionViewModel extends ViewModel {

    private final SpeechRecognitionUseCase speechRecognitionUseCase;

    private final MutableLiveData<String> recognizedWord = new MutableLiveData<>();

    @Inject
    public SpeechRecognitionViewModel(SpeechRecognitionUseCase speechRecognitionUseCase) {
        this.speechRecognitionUseCase = speechRecognitionUseCase;
    }

    public LiveData<String> getRecognizedWord() {
        return recognizedWord;
    }

    public void startSpeechRecognition() {
        speechRecognitionUseCase.execute(new SpeechRecognitionRepository.Listener() {
            @Override
            public void onWordRecognized(String word) {
                recognizedWord.postValue(word);
            }

            @Override
            public void onError(Throwable throwable) {
            }
        });
    }

    public void stopSpeechRecognition() {
        speechRecognitionUseCase.stop();
    }
}

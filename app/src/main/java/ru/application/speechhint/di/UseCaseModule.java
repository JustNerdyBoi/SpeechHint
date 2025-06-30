package ru.application.speechhint.di;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import ru.application.domain.repository.DocumentRepository;
import ru.application.domain.repository.ServerRepository;
import ru.application.domain.repository.SettingsRepository;
import ru.application.domain.repository.SpeechRecognitionRepository;
import ru.application.domain.usecase.AddWordUseCase;
import ru.application.domain.usecase.CalculatePositionUseCase;
import ru.application.domain.usecase.EditWordUseCase;
import ru.application.domain.usecase.GetServerConnectionInfoUseCase;
import ru.application.domain.usecase.GetSettingsUseCase;
import ru.application.domain.usecase.LoadDocumentUseCase;
import ru.application.domain.usecase.RemoveWordUseCase;
import ru.application.domain.usecase.SaveSettingsUseCase;
import ru.application.domain.usecase.SetServerCurrentDocumentUseCase;
import ru.application.domain.usecase.SetServerCurrentPositionUseCase;
import ru.application.domain.usecase.SetServerCurrentSettingsUseCase;
import ru.application.domain.usecase.SpeechRecognitionUseCase;
import ru.application.domain.usecase.StartServerUseCase;
import ru.application.domain.usecase.StopServerUseCase;

@Module
@InstallIn(SingletonComponent.class)
public class UseCaseModule {

    @Provides
    public LoadDocumentUseCase provideLoadDocumentUseCase(DocumentRepository repository) {
        return new LoadDocumentUseCase(repository);
    }

    @Provides
    public SpeechRecognitionUseCase provideSpeechRecognitionUseCase(SpeechRecognitionRepository repository) {
        return new SpeechRecognitionUseCase(repository);
    }

    @Provides
    public SaveSettingsUseCase provideSaveSettingsUseCase(SettingsRepository repository){
        return new SaveSettingsUseCase(repository);
    }

    @Provides
    public GetSettingsUseCase provideGetSettingsUseCase(SettingsRepository repository){
        return new GetSettingsUseCase(repository);
    }

    @Provides
    public CalculatePositionUseCase provideCalculatePositionUseCase(){
        return new CalculatePositionUseCase();
    }

    @Provides
    public StartServerUseCase provideStartServerUseCase(ServerRepository repository){
        return new StartServerUseCase(repository);
    }

    @Provides
    public StopServerUseCase provideStopServerUsecase(ServerRepository repository){
        return new StopServerUseCase(repository);
    }

    @Provides
    public GetServerConnectionInfoUseCase provideGetServerConnectionInfoUseCase(ServerRepository repository){
        return new GetServerConnectionInfoUseCase(repository);
    }

    @Provides
    public SetServerCurrentPositionUseCase provideSetCurrentPositionUseCase(ServerRepository repository){
        return new SetServerCurrentPositionUseCase(repository);
    }

    @Provides
    public SetServerCurrentSettingsUseCase provideSetCurrentSettingsUseCase(ServerRepository repository){
        return new SetServerCurrentSettingsUseCase(repository);
    }

    @Provides
    public SetServerCurrentDocumentUseCase provideSetServerCurrentDocumentUseCase(ServerRepository repository){
        return new SetServerCurrentDocumentUseCase(repository);
    }

    @Provides
    public AddWordUseCase provideAddWordUseCase(){
        return new AddWordUseCase();
    }

    @Provides
    public EditWordUseCase provideEditWordUseCase(){
        return new EditWordUseCase();
    }

    @Provides
    public RemoveWordUseCase provideRemoveWordUseCase(){
        return new RemoveWordUseCase();
    }
}
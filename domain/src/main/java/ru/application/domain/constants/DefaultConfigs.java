package ru.application.domain.constants;

import ru.application.domain.entity.HighlightType;
import ru.application.domain.entity.Theme;

public class DefaultConfigs {
    // ui
    public static final boolean DEFAULT_CURRENT_STRING_HIGHLIGHT = false;
    public static final HighlightType DEFAULT_HIGHLIGHT_TYPE = HighlightType.LINE;
    public static final float DEFAULT_HIGHLIGHT_HEIGHT = 0.5f;
    public static final boolean DEFAULT_CURRENT_WORD_HIGHLIGHT_FOLLOW = true;
    public static final boolean DEFAULT_MIRROR_TEXT = false;
    public static final Theme DEFAULT_THEME = Theme.DARK;
    public static final int DEFAULT_TEXT_SCALE = 20;

    // stt
    public static final boolean DEFAULT_STT_ENABLED = false;
    public static final int DEFAULT_STT_BEFORE_BUFFER_SIZE = 5;
    public static final int DEFAULT_STT_AFTER_BUFFER_SIZE = 10;

    // scroll
    public static final boolean DEFAULT_ENABLE_AUTO_SCROLL = false;
    public static final float DEFAULT_SPEED = 100.0f;
}

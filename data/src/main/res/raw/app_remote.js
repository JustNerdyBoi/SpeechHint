// Translations
const translations = {
    en: {
        settings: "Settings",
        scrollSettings: "Scroll Settings",
        speedStep: "Speed Step (dp/s)",
        scrollStep: "Scroll Step (screen)",
        inverseScroll: "Inverse Scroll",
        button: "Control Panel"
    },
    ru: {
        settings: "Настройки",
        scrollSettings: "Настройки прокрутки",
        speedStep: "Шаг скорости (dp/s)",
        scrollStep: "Шаг прокрутки (экран)",
        inverseScroll: "Инвертировать прокрутку",
        button: "Панель управления"
    }
};

let currentLanguage = 'en';

let settings = {
    speedStep: 100,
    scrollStep: 0.5,
    inverseScroll: false
};

let currentSettings = {
    scrollConfig: { enableAutoScroll: true, speed: 270.0 },
    sttConfig: { sttAfterBufferSize: 16, sttBeforeBufferSize: 5, sttEnabled: true },
    uiConfig: { currentStringHighlight: false, highlightType: "LINE", highlightHeight: 0.5, currentWordHighlightFollow: true, mirrorText: false, textScale: 85, theme: "DARK" }
};

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadSettings();
    setupSliders();
    startPolling();
});

// Cookie helpers
function saveSettingsToCookie() {
    document.cookie = `scrollSettings=${encodeURIComponent(JSON.stringify(settings))}; path=/; max-age=999999999`;
}

function loadSettingsFromCookie() {
    const cookie = document.cookie.split(';').find(c => c.trim().startsWith('scrollSettings='));
    if (cookie) {
        try {
            settings = JSON.parse(decodeURIComponent(cookie.split('=')[1]));
        } catch (e) {
            console.error('Error parsing settings from cookie:', e);
        }
    }
}

// Load language
function loadLanguage() {
    return document.cookie.split(';').find(e => e.trim().startsWith('lang='))?.split('=')[1] || null;
}

// Core functions
function loadSettings() {
    loadSettingsFromCookie();

    fetch('/settings/get/')
        .then(response => response.json())
        .then(data => {
            currentSettings = data;
            document.getElementById('speedStep').value = settings.speedStep;
            document.getElementById('scrollStep').value = settings.scrollStep;
            document.getElementById('inverseScroll').checked = settings.inverseScroll;
            updateSettingsUI();
            updateControlButtons();
        })
        .catch(error => console.error('Error loading settings:', error));
}

function updateSettings() {
    settings = {
        speedStep: parseInt(document.getElementById('speedStep').value),
        scrollStep: parseFloat(document.getElementById('scrollStep').value),
        inverseScroll: document.getElementById('inverseScroll').checked
    };
    saveSettingsToCookie();
    updateSettingsUI();
}

function updateSettingsUI() {
    document.querySelectorAll('.value-display').forEach(display => {
        const input = display.previousElementSibling;
        if (input) {
            display.textContent = input.value;
        }
    });
}

function updateControlButtons() {
    const pauseButton = document.querySelector('.control-button.pause');
    const pauseIcon = pauseButton.querySelector('i');
    pauseIcon.className = currentSettings.scrollConfig.enableAutoScroll ? 'fas fa-pause' : 'fas fa-play';
    pauseButton.classList.toggle('active', currentSettings.scrollConfig.enableAutoScroll);

    const sttButton = document.querySelector('.control-button.stt');
    sttButton.classList.toggle('active', currentSettings.sttConfig.sttEnabled);
}

function setupSliders() {
    document.querySelectorAll('input[type="range"]').forEach(slider => {
        const valueDisplay = slider.nextElementSibling;
        if (valueDisplay) {
            valueDisplay.textContent = slider.value;
            slider.addEventListener('input', () => {
                valueDisplay.textContent = slider.value;
            });
        }
    });
}

// Control functions
function togglePause() {
    currentSettings.scrollConfig.enableAutoScroll = !currentSettings.scrollConfig.enableAutoScroll;
    sendSettings();
}

function toggleSTT() {
    currentSettings.sttConfig.sttEnabled = !currentSettings.sttConfig.sttEnabled;
    sendSettings();
}

function increaseSpeed() {
    currentSettings.scrollConfig.speed += settings.speedStep;
    sendSettings();
}

function decreaseSpeed() {
    currentSettings.scrollConfig.speed = Math.max(0, currentSettings.scrollConfig.speed - settings.speedStep);
    sendSettings();
}

function sendSettings() {
    fetch('/settings/set/', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(currentSettings)
    }).catch(error => console.error('Error updating settings:', error));
}

function scroll(direction) {
    const multiplier = (direction === 'up') ? 1 : -1;
    const scrollAmount = settings.inverseScroll ? -multiplier * settings.scrollStep : multiplier * settings.scrollStep;

    fetch('/scroll/', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ scroll: scrollAmount })
    }).catch(error => console.error(`Error scrolling ${direction}:`, error));
}

function scrollUp() {
    scroll('up');
}

function scrollDown() {
    scroll('down');
}

// Modal
function openSettings() {
    document.getElementById('settingsModal').style.display = 'block';
}

function closeSettings() {
    document.getElementById('settingsModal').style.display = 'none';
}

// Language
function changeLanguage(lang) {
    document.cookie = `lang=${lang};path=/;max-age=999999999`;
    currentLanguage = lang;
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        if (translations[lang][key]) {
            element.textContent = translations[lang][key];
        }
    });
}

// Polling
function startPolling() {
    setInterval(() => {
        loadSettings();
    }, 250);
}

// Event listeners
window.onclick = event => {
    const modal = document.getElementById('settingsModal');
    if (event.target === modal) {
        closeSettings();
    }
};

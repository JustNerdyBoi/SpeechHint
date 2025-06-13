// Translations
const translations = {
    en: {
        settings: "Settings",
        scrollSettings: "Scroll Settings",
        speedStep: "Speed Step (dp/s)",
        scrollStep: "Scroll Step (screen)",
        inverseScroll: "Inverse Scroll"
    },
    ru: {
        settings: "Настройки",
        scrollSettings: "Настройки прокрутки",
        speedStep: "Шаг скорости (dp/s)",
        scrollStep: "Шаг прокрутки (экран)",
        inverseScroll: "Инвертировать прокрутку"
    }
};

let currentLanguage = 'en';
let settings = {
    speedStep: 100,
    scrollStep: 0.5,
    inverseScroll: false
};

let currentSettings = {
                          scrollConfig: { autoScroll: true, speed: 270.0 },
                          sttConfig: { sttAfterBufferSize: 16, sttBeforeBufferSize: 5, sttEnabled: true },
                          uiConfig: { currentStringHighlight: false, highlightType: "LINE", highlightHeight: 0.5, currentWordHighlightFollow:true, mirrorText: false, textScale: 85, theme: "DARK" }
                      };

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    loadSettings();
    setupSliders();
    startPolling();
});

// Core functions
function loadSettings() {
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
    // Update pause button
    const pauseButton = document.querySelector('.control-button.pause');
    const pauseIcon = pauseButton.querySelector('i');
    pauseIcon.className = currentSettings.scrollConfig.autoScroll ? 'fas fa-pause' : 'fas fa-play';
    if (currentSettings.scrollConfig.autoScroll) {
        pauseButton.classList.add('active');
    } else {
        pauseButton.classList.remove('active');
    }

    // Update STT button
    const sttButton = document.querySelector('.control-button.stt');
    if (currentSettings.sttConfig.sttEnabled) {
        sttButton.classList.add('active');
    } else {
        sttButton.classList.remove('active');
    }
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
    currentSettings.scrollConfig.autoScroll = !currentSettings.scrollConfig.autoScroll;
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

function scrollUp() {
    const scrollAmount = settings.inverseScroll ? settings.scrollStep : -settings.scrollStep;
    fetch('/scroll/', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ scroll: scrollAmount })
    }).catch(error => console.error('Error scrolling up:', error));
}

function scrollDown() {
    const scrollAmount = settings.inverseScroll ? -settings.scrollStep : settings.scrollStep;
    fetch('/scroll/', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ scroll: scrollAmount })
    }).catch(error => console.error('Error scrolling down:', error));
}

// Modal functions
function openSettings() {
    document.getElementById('settingsModal').style.display = 'block';
}

function closeSettings() {
    document.getElementById('settingsModal').style.display = 'none';
}

// Language functions
function changeLanguage(lang) {
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
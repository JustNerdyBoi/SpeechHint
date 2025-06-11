// Translations
const translations = {
    en: {
        title: "Teleprompter Control Panel",
        noDocument: "No document",
        uploadDocument: "Upload Document",
        settings: "Settings",
        scrollSettings: "Scroll Settings",
        autoScroll: "Auto Scroll",
        scrollSpeed: "Scroll Speed",
        sttSettings: "Speech-to-Text Settings",
        sttEnabled: "STT Enabled",
        sttBeforeBuffer: "STT Before Buffer",
        sttAfterBuffer: "STT After Buffer",
        uiSettings: "UI Settings",
        currentStringHighlight: "Highlight Current String",
        mirrorText: "Mirror Text",
        textScale: "Text Scale",
        theme: "Theme",
        darkTheme: "Dark",
        lightTheme: "Light",
        uploadFile: "Upload File",
        yandexDisk: "Yandex.Disk",
        googleDrive: "Google Drive",
        upload: "Upload"
    },
    ru: {
        title: "Панель управления телесуфлером",
        noDocument: "Нет документа",
        uploadDocument: "Загрузить документ",
        settings: "Настройки",
        scrollSettings: "Настройки прокрутки",
        autoScroll: "Автопрокрутка",
        scrollSpeed: "Скорость прокрутки",
        sttSettings: "Настройки распознавания речи",
        sttEnabled: "Включить распознавание речи",
        sttBeforeBuffer: "Буфер до",
        sttAfterBuffer: "Буфер после",
        uiSettings: "Настройки интерфейса",
        currentStringHighlight: "Подсветка текущей строки",
        mirrorText: "Зеркальное отображение",
        textScale: "Масштаб текста",
        theme: "Тема",
        darkTheme: "Тёмная",
        lightTheme: "Светлая",
        uploadFile: "Загрузить файл",
        yandexDisk: "Яндекс.Диск",
        googleDrive: "Google Drive",
        upload: "Загрузить"
    }
};

let currentLanguage = 'en';
let currentPosition = 0;
let words = [];
let settings = {
    scrollConfig: { autoScroll: true, speed: 270.0 },
    sttConfig: { sttAfterBufferSize: 16, sttBeforeBufferSize: 5, sttEnabled: true },
    uiConfig: { currentStringHighlight: false, mirrorText: false, textScale: 85, theme: "DARK" }
};

const textDisplay = document.getElementById('textDisplay');
const uploadModal = document.getElementById('uploadModal');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    loadSettings();
    fetchDocument();
    fetchPosition();
    startPolling();
    setupSliders();
});

// Core functions
function renderWords() {
    textDisplay.innerHTML = '';
    let currentLine = document.createElement('div');
    currentLine.className = 'text-line';
    textDisplay.appendChild(currentLine);

    words.forEach((word, index) => {
        if (word.text === '\n') {
            currentLine = document.createElement('div');
            currentLine.className = 'text-line';
            textDisplay.appendChild(currentLine);
            return;
        }

        const wordElement = document.createElement('span');
        wordElement.className = 'word';
        if (index === currentPosition) {
            wordElement.classList.add('active');
        }
        wordElement.textContent = word.text;
        wordElement.onclick = () => setPosition(index);
        currentLine.appendChild(wordElement);
    });

    scrollToActiveWord();
}

function updateActiveWord() {
    document.querySelectorAll('.word.active').forEach(word => word.classList.remove('active'));
    const wordElements = document.querySelectorAll('.word');
    const newlineCount = words.slice(0, currentPosition).filter(w => w.text === '\n').length;
    const adjustedPosition = currentPosition - newlineCount;
    
    if (wordElements[adjustedPosition]) {
        wordElements[adjustedPosition].classList.add('active');
        scrollToActiveWord();
    }
}

function scrollToActiveWord() {
    const activeWord = textDisplay.querySelector('.word.active');
    if (activeWord) {
        const containerHeight = textDisplay.clientHeight;
        textDisplay.scrollTop = activeWord.offsetTop - (containerHeight / 2) + (activeWord.offsetHeight / 2);
    }
}

// API functions
async function fetchDocument() {
    try {
        const response = await fetch('/document/get/');
        const data = await response.json();
        if (data.words?.length > 0 && JSON.stringify(words) !== JSON.stringify(data.words)) {
            words = data.words;
            renderWords();
        } else if (!data.words?.length) {
            textDisplay.innerHTML = `<div class="no-document">${translations[currentLanguage].noDocument}</div>`;
        }
    } catch (error) {
        console.error('Error fetching document:', error);
    }
}

async function fetchPosition() {
    try {
        const response = await fetch('/position/get/');
        const data = await response.json();
        if (currentPosition !== data.position) {
            currentPosition = data.position;
            updateActiveWord();
        }
    } catch (error) {
        console.error('Error fetching position:', error);
    }
}

async function setPosition(position) {
    try {
        await fetch('/position/set/', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ position })
        });
        currentPosition = position;
        updateActiveWord();
    } catch (error) {
        console.error('Error setting position:', error);
    }
}

async function loadSettings() {
    try {
        const response = await fetch('/settings/get/');
        settings = await response.json();
        updateSettingsUI();
    } catch (error) {
        console.error('Error loading settings:', error);
    }
}

async function updateSettings() {
    settings = {
        scrollConfig: {
            autoScroll: document.getElementById('autoScroll').checked,
            speed: parseFloat(document.getElementById('scrollSpeed').value)
        },
        sttConfig: {
            sttAfterBufferSize: parseInt(document.getElementById('sttAfterBuffer').value),
            sttBeforeBufferSize: parseInt(document.getElementById('sttBeforeBuffer').value),
            sttEnabled: document.getElementById('sttEnabled').checked
        },
        uiConfig: {
            currentStringHighlight: document.getElementById('currentStringHighlight').checked,
            mirrorText: document.getElementById('mirrorText').checked,
            textScale: parseInt(document.getElementById('textScale').value),
            theme: document.getElementById('theme').value
        }
    };

    try {
        await fetch('/settings/set/', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(settings)
        });
        updateSettingsUI();
    } catch (error) {
        console.error('Error updating settings:', error);
    }
}

// UI functions
function updateSettingsUI() {
    document.getElementById('autoScroll').checked = settings.scrollConfig.autoScroll;
    document.getElementById('scrollSpeed').value = settings.scrollConfig.speed;
    document.getElementById('sttEnabled').checked = settings.sttConfig.sttEnabled;
    document.getElementById('sttBeforeBuffer').value = settings.sttConfig.sttBeforeBufferSize;
    document.getElementById('sttAfterBuffer').value = settings.sttConfig.sttAfterBufferSize;
    document.getElementById('currentStringHighlight').checked = settings.uiConfig.currentStringHighlight;
    document.getElementById('mirrorText').checked = settings.uiConfig.mirrorText;
    document.getElementById('textScale').value = settings.uiConfig.textScale;
    document.getElementById('theme').value = settings.uiConfig.theme;
    document.body.className = settings.uiConfig.theme.toLowerCase();
}

function setupSliders() {
    document.querySelectorAll('input[type="range"]').forEach(slider => {
        const valueDisplay = document.createElement('span');
        valueDisplay.className = 'value-display';
        slider.parentElement.appendChild(valueDisplay);
        valueDisplay.textContent = slider.value;
        slider.addEventListener('input', () => {
            valueDisplay.textContent = slider.value;
        });
    });
}

function changeLanguage(lang) {
    currentLanguage = lang;
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        if (translations[lang][key]) {
            element.textContent = translations[lang][key];
        }
    });
}

// Modal functions
function openUploadModal() {
    uploadModal.style.display = 'block';
}

function closeUploadModal() {
    uploadModal.style.display = 'none';
}

async function uploadFile() {
    const file = document.getElementById('fileUpload').files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
        await fetch('/document/set/file/', { method: 'POST', body: formData });
        closeUploadModal();
        fetchDocument();
    } catch (error) {
        console.error('Error uploading file:', error);
    }
}

async function uploadYandexLink() {
    const link = document.getElementById('yandexLink').value;
    if (!link) return;

    try {
        await fetch('/document/set/yandex-disk/', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ yandexDiskLink: link })
        });
        closeUploadModal();
        fetchDocument();
    } catch (error) {
        console.error('Error uploading Yandex.Disk link:', error);
    }
}

async function uploadGoogleLink() {
    const link = document.getElementById('googleLink').value;
    if (!link) return;

    try {
        await fetch('/document/set/google-drive/', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ googleDriveLink: link })
        });
        closeUploadModal();
        fetchDocument();
    } catch (error) {
        console.error('Error uploading Google Drive link:', error);
    }
}

// Polling
function startPolling() {
    setInterval(() => {
        fetchDocument();
        fetchPosition();
        loadSettings();
    }, 100);
}

// Event listeners
window.onclick = event => {
    if (event.target === uploadModal) {
        closeUploadModal();
    }
}; 
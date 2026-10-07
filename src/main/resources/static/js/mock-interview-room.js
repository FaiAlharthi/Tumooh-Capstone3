(function () {
    const config = window.TUMOOH_MOCK_SESSION || {};
    const sessionId = config.sessionId;
    const feedbackBase = config.feedbackBasePath || '/mock-interview/feedback/';
    const duration = config.durationSeconds || 30;

    const timerEl = document.getElementById('timer');
    const videoEl = document.getElementById('webcam');
    const fallbackEl = document.getElementById('camera-fallback');
    const submittingOverlay = document.getElementById('submitting-overlay');

    let remaining = duration;
    let mediaStream = null;
    let audioContext = null;
    let analyser = null;
    let monitorInterval = null;
    let timerInterval = null;
    let redirectStarted = false;
    let sessionReady = false;

    let speechActiveSeconds = 0;
    let peakAudioLevel = 0;
    let transcriptParts = [];

    const SPEECH_LEVEL_THRESHOLD = 0.06;
    const SAMPLE_MS = 200;

    function formatTime(seconds) {
        const m = Math.floor(seconds / 60);
        const s = Math.max(0, seconds % 60);
        return m + ':' + String(s).padStart(2, '0');
    }

    function showSubmittingOverlay(message) {
        if (!submittingOverlay) {
            return;
        }
        submittingOverlay.classList.remove('hidden');
        if (message) {
            const msgEl = submittingOverlay.querySelector('[data-overlay-message]');
            if (msgEl) {
                msgEl.textContent = message;
            }
        }
    }

    function showCameraRequired(message) {
        if (fallbackEl) {
            fallbackEl.textContent = message;
            fallbackEl.classList.remove('hidden');
        }
        if (videoEl) {
            videoEl.classList.add('hidden');
        }
        if (timerEl) {
            timerEl.textContent = '—';
        }
    }

    function isVideoLive(stream) {
        const track = stream.getVideoTracks()[0];
        return track && track.readyState === 'live' && track.enabled;
    }

    function isAudioLive(stream) {
        const track = stream.getAudioTracks()[0];
        return track && track.readyState === 'live' && track.enabled;
    }

    function getQuestionsFromDom() {
        const list = document.getElementById('question-list');
        if (!list) {
            return [];
        }
        return Array.from(list.querySelectorAll('li')).map(function (li) {
            return li.textContent.trim();
        }).filter(Boolean);
    }

    function buildSessionNotes() {
        const transcript = transcriptParts.join(' ').trim();
        const spoke = speechActiveSeconds >= 1.5;
        const questions = getQuestionsFromDom();
        const questionsBlock = questions.map(function (q, i) {
            return (i + 1) + '. ' + q;
        }).join(' ');

        const cameraActive = mediaStream && isVideoLive(mediaStream);
        const microphoneActive = mediaStream && isAudioLive(mediaStream);

        const header = [
            'CAMERA_ACTIVE:' + (cameraActive ? 'true' : 'false'),
            'MICROPHONE_ACTIVE:' + (microphoneActive ? 'true' : 'false')
        ].join(' ');

        if (!spoke && !transcript) {
            return [
                header,
                'NO_SPEECH_DETECTED:true',
                'Questions shown: ' + questionsBlock,
                'The candidate remained silent for essentially the entire ' + duration + '-second session.',
                'Measured active speech time: ' + speechActiveSeconds.toFixed(1) + ' seconds.',
                'Peak microphone level (0-1): ' + peakAudioLevel.toFixed(3) + '.',
                'Transcript: (empty — no words captured).',
                'Do not assume engagement or answers; there was no verbal content to score against the questions.'
            ].join(' ');
        }

        return [
            header,
            'NO_SPEECH_DETECTED:false',
            'Questions shown: ' + questionsBlock,
            'Active speech time: approximately ' + speechActiveSeconds.toFixed(1) + ' seconds of ' + duration + '.',
            'Peak microphone level (0-1): ' + peakAudioLevel.toFixed(3) + '.',
            'Transcript: ' + transcript,
            'Evaluate how well the transcript answers each question.'
        ].join(' ');
    }

    async function redirectToFeedback() {
        if (redirectStarted || !sessionReady) {
            return;
        }
        redirectStarted = true;
        showSubmittingOverlay('Saving your session and generating AI feedback…');

        const notes = buildSessionNotes();
        stopMonitoring();

        try {
            const response = await fetch('/api/v1/mock-interviews/' + sessionId + '/telemetry', {
                method: 'POST',
                headers: { 'Content-Type': 'text/plain;charset=UTF-8' },
                body: notes,
                keepalive: true
            });
            if (!response.ok) {
                throw new Error('Telemetry save failed');
            }
        } catch (err) {
            redirectStarted = false;
            showSubmittingOverlay(null);
            if (submittingOverlay) {
                submittingOverlay.classList.add('hidden');
            }
            showCameraRequired('Could not save your session data. Check your connection and refresh to try again.');
            return;
        }

        window.location.assign(feedbackBase + sessionId);
    }

    function startTimer() {
        remaining = duration;
        timerEl.textContent = formatTime(remaining);
        timerInterval = setInterval(function () {
            remaining -= 1;
            timerEl.textContent = formatTime(remaining);
            if (remaining <= 10) {
                timerEl.classList.add('text-red-400');
            }
            if (remaining <= 0) {
                clearInterval(timerInterval);
                timerInterval = null;
                redirectToFeedback();
            }
        }, 1000);
    }

    function startAudioMonitoring(stream) {
        audioContext = new (window.AudioContext || window.webkitAudioContext)();
        analyser = audioContext.createAnalyser();
        analyser.fftSize = 256;
        const source = audioContext.createMediaStreamSource(stream);
        source.connect(analyser);

        const data = new Uint8Array(analyser.frequencyBinCount);

        monitorInterval = setInterval(function () {
            analyser.getByteFrequencyData(data);
            let sum = 0;
            for (let i = 0; i < data.length; i++) {
                sum += data[i];
            }
            const level = sum / (data.length * 255);
            if (level > peakAudioLevel) {
                peakAudioLevel = level;
            }
            if (level >= SPEECH_LEVEL_THRESHOLD) {
                speechActiveSeconds += SAMPLE_MS / 1000;
            }
        }, SAMPLE_MS);
    }

    function startSpeechRecognition() {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRecognition) {
            return;
        }
        try {
            const recognition = new SpeechRecognition();
            recognition.continuous = true;
            recognition.interimResults = true;
            recognition.lang = 'en-US';
            recognition.onresult = function (event) {
                for (let i = event.resultIndex; i < event.results.length; i++) {
                    if (event.results[i].isFinal) {
                        transcriptParts.push(event.results[i][0].transcript);
                    }
                }
            };
            recognition.start();
        } catch (e) {
            /* Web Speech API unavailable; mic levels still captured */
        }
    }

    function stopMonitoring() {
        if (timerInterval) {
            clearInterval(timerInterval);
            timerInterval = null;
        }
        if (monitorInterval) {
            clearInterval(monitorInterval);
            monitorInterval = null;
        }
        if (audioContext) {
            audioContext.close().catch(function () {});
            audioContext = null;
        }
        if (mediaStream) {
            mediaStream.getTracks().forEach(function (t) { t.stop(); });
            mediaStream = null;
        }
    }

    async function initCamera() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            showCameraRequired('Camera and microphone are required. Use a modern browser with HTTPS or localhost.');
            return;
        }

        try {
            mediaStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
            if (!isVideoLive(mediaStream) || !isAudioLive(mediaStream)) {
                throw new Error('Tracks not live');
            }
            videoEl.srcObject = mediaStream;
            await videoEl.play();
            sessionReady = true;
            startAudioMonitoring(mediaStream);
            startSpeechRecognition();
            startTimer();
        } catch (err) {
            stopMonitoring();
            showCameraRequired(
                'Camera and microphone access is required for mock interviews. '
                + 'Allow both permissions in your browser, then reload this page.'
            );
        }
    }

    initCamera();

    window.addEventListener('beforeunload', stopMonitoring);
})();

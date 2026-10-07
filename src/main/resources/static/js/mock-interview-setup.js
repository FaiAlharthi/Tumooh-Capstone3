(function () {
    const form = document.getElementById('mock-setup-form');
    if (!form) {
        return;
    }
    const errorEl = document.getElementById('setup-error');
    const submitBtn = document.getElementById('setup-submit');

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        if (errorEl) {
            errorEl.classList.add('hidden');
        }
        const userId = Number(document.getElementById('userId').value);
        const jobTitle = document.getElementById('jobTitle').value.trim();
        if (!jobTitle) {
            if (errorEl) {
                errorEl.textContent = 'Job title is required.';
                errorEl.classList.remove('hidden');
            }
            return;
        }
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Starting…';
        }
        try {
            const data = await window.TumoohApi.post('/mock-interviews/start', {
                userId: userId,
                jobTitle: jobTitle
            });
            window.location.assign('/mock-interview/room/' + data.id);
        } catch (err) {
            if (errorEl) {
                errorEl.textContent = err.message || 'Could not start session.';
                errorEl.classList.remove('hidden');
            }
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Enter interview room';
            }
        }
    });
})();

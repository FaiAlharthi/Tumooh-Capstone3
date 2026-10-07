/**
 * Nav profile dropdown — greeting + name in the corner of the main nav.
 * Reads window.TUMOOH_PROFILE.userId when a page provides it, otherwise defaults to 1
 * (same convention as the web controllers' DEFAULT_USER_ID).
 */
(function () {
    'use strict';

    const toggle = document.getElementById('nav-profile-toggle');
    const menu = document.getElementById('nav-profile-menu');
    const label = document.getElementById('nav-profile-label');
    const chevron = document.getElementById('nav-profile-chevron');
    const nameEl = document.getElementById('nav-profile-name');
    const emailEl = document.getElementById('nav-profile-email');
    const linkEl = document.getElementById('nav-profile-link');

    if (!toggle || !menu || !label) {
        return;
    }

    const API_PREFIX = '/api/v1';

    function greeting() {
        const hour = new Date().getHours();
        if (hour < 12) {
            return 'Good morning';
        }
        if (hour < 18) {
            return 'Good afternoon';
        }
        return 'Good evening';
    }

    async function fetchJson(path) {
        const response = await fetch(API_PREFIX + path);
        if (!response.ok) {
            throw new Error('Request failed');
        }
        return response.json();
    }

    async function loadIdentity() {
        const userId = (window.TUMOOH_PROFILE && window.TUMOOH_PROFILE.userId) || 1;

        let fullName = '';
        try {
            const profile = await fetchJson('/profiles/user/' + userId);
            fullName = profile && profile.fullName ? String(profile.fullName).trim() : '';
        } catch (err) {
            // No profile yet — fall back to the generic greeting
        }

        let email = '';
        try {
            const user = await fetchJson('/users/get/' + userId);
            email = user && user.email ? String(user.email) : '';
        } catch (err) {
            // Email is optional decoration
        }

        const firstName = fullName ? fullName.split(/\s+/)[0] : '';
        // textContent, not innerHTML — nothing here is parsed as markup
        label.textContent = firstName ? greeting() + ', ' + firstName : 'Hi there';

        if (nameEl) {
            nameEl.textContent = fullName || 'Your profile';
        }
        if (emailEl) {
            if (email) {
                emailEl.textContent = email;
            } else {
                emailEl.classList.add('hidden');
            }
        }
    }

    // Lets other scripts (e.g. profile.js after a save) refresh the greeting/name in place
    window.TumoohNavProfile = { refresh: loadIdentity };

    /* ---------------- dropdown open / close ---------------- */

    function isOpen() {
        return toggle.getAttribute('aria-expanded') === 'true';
    }

    function openMenu() {
        menu.classList.remove('hidden');
        toggle.setAttribute('aria-expanded', 'true');
        if (chevron) {
            chevron.classList.add('rotate-180');
        }
    }

    function closeMenu() {
        menu.classList.add('hidden');
        toggle.setAttribute('aria-expanded', 'false');
        if (chevron) {
            chevron.classList.remove('rotate-180');
        }
    }

    toggle.addEventListener('click', function (event) {
        event.stopPropagation();
        if (isOpen()) {
            closeMenu();
        } else {
            openMenu();
        }
    });

    document.addEventListener('click', function (event) {
        if (!isOpen()) {
            return;
        }
        if (menu.contains(event.target) || toggle.contains(event.target)) {
            return;
        }
        closeMenu();
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && isOpen()) {
            closeMenu();
            toggle.focus();
        }
    });

    if (linkEl) {
        linkEl.addEventListener('click', function () {
            closeMenu();
        });
    }

    // Wait for DOMContentLoaded so pages that set window.TUMOOH_PROFILE at the
    // end of <body> are taken into account before we resolve the userId.
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadIdentity);
    } else {
        loadIdentity();
    }
})();

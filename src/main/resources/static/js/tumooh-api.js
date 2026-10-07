window.TumoohApi = (function () {
    const API_PREFIX = '/api/v1';

    async function parseResponse(response) {
        const text = await response.text();
        let data = null;
        if (text) {
            try {
                data = JSON.parse(text);
            } catch (e) {
                data = text;
            }
        }
        if (!response.ok) {
            const message = (data && (data.message || data.error)) ? (data.message || data.error) : (typeof data === 'string' ? data : 'Request failed');
            throw new Error(message);
        }
        return data;
    }

    async function apiGet(path) {
        const response = await fetch(API_PREFIX + path);
        return parseResponse(response);
    }

    async function apiPost(path, body, contentType) {
        const headers = {};
        let payload = body;
        if (contentType === 'text/plain') {
            headers['Content-Type'] = 'text/plain;charset=UTF-8';
            payload = body;
        } else if (body !== undefined) {
            headers['Content-Type'] = 'application/json';
            payload = JSON.stringify(body);
        }
        const response = await fetch(API_PREFIX + path, {
            method: 'POST',
            headers: headers,
            body: payload
        });
        return parseResponse(response);
    }

    async function apiPatch(path, body) {
        const response = await fetch(API_PREFIX + path, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        return parseResponse(response);
    }

    return {
        get: apiGet,
        post: apiPost,
        patch: apiPatch
    };
})();

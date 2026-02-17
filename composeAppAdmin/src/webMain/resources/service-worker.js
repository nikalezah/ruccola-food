// Service Worker for Food Admin PWA
// Strategy:
// - Do NOT cache HTML/app shell or main JS with cache-first to avoid reload loops
// - Network-first for navigations, documents, scripts, and workers
// - Cache-first for static assets (icons, manifest, images)
// - Safe activation (skipWaiting + clients.claim) with cache versioning

const CACHE_NAME = 'food-admin-v3';
const STATIC_ASSETS = [
  'manifest.json',
  'favicon.png',
  'icon-192.png',
  'icon-512.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(STATIC_ASSETS)).catch(() => {})
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => Promise.all(keys.map((k) => (k !== CACHE_NAME ? caches.delete(k) : undefined))))
  );
  self.clients.claim();
});

function isNavOrAppCode(request) {
  const dest = request.destination;
  // Treat navigations, documents, scripts, and workers as app code
  return request.mode === 'navigate' || dest === 'document' || dest === 'script' || dest === 'worker';
}

self.addEventListener('fetch', (event) => {
  const { request } = event;

  // Only handle GET requests
  if (request.method !== 'GET') {
    return;
  }

  const url = new URL(request.url);

  // Never intercept cross-origin requests (e.g., API calls to backend)
  if (url.origin !== self.location.origin) {
    // event.respondWith(fetch(request));
    return;
  }

  // Avoid caching JSON/XHR requests; go network-first (no cache write)
  const accept = request.headers.get('accept') || '';
  const isJson = accept.includes('application/json') || accept.includes('text/event-stream');
  if (isJson) {
    event.respondWith(fetch(request).catch(() => caches.match(request)));
    return;
  }

  if (isNavOrAppCode(request)) {
    // Network-first for app code to avoid stale JS/HTML causing reloads
    event.respondWith(
      fetch(request)
        .then((networkResp) => {
          // Optionally update cache for offline fallback
          const respClone = networkResp.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(request, respClone)).catch(() => {});
          return networkResp;
        })
        .catch(() => caches.match(request))
    );
    return;
  }

  // Cache-first for static assets (images, icons, manifest, etc.)
  event.respondWith(
    caches.match(request).then((cached) => {
      if (cached) return cached;
      return fetch(request)
        .then((networkResp) => {
          // Only cache basic, successful responses
          if (networkResp && networkResp.status === 200 && (networkResp.type === 'basic' || networkResp.type === 'cors')) {
            const clone = networkResp.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(request, clone)).catch(() => {});
          }
          return networkResp;
        })
        .catch(() => cached);
    })
  );
});

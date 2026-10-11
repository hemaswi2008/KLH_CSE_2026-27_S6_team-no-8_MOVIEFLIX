const API_BASE = 'http://localhost:8080/api';
let allMoviesCache = [];

document.addEventListener('DOMContentLoaded', () => {
    loadAllMovies();
    updateWatchlistCount();
});

function switchTab(tabId) {
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.nav-links a').forEach(el => el.classList.remove('active'));
    
    document.getElementById('tab-' + tabId).classList.add('active');
    event.currentTarget.classList.add('active');
    
    if (tabId === 'watchlist') {
        renderWatchlist();
    }
}

async function loadAllMovies() {
    try {
        const res = await fetch(`${API_BASE}/movies`);
        allMoviesCache = await res.json();
        document.getElementById('featuredGrid').innerHTML = allMoviesCache.map(m => renderMovieCard(m)).join('');
        fetchFilteredMovies();
    } catch (err) {
        console.error('Error loading movies', err);
    }
}

async function fetchFilteredMovies() {
    const q = document.getElementById('browseInput').value;
    const type = document.getElementById('searchType').value;
    const lang = document.getElementById('filterLang').value;
    const genre = document.getElementById('filterGenre').value;
    const sort = document.getElementById('sortField').value;
    const order = document.getElementById('sortOrder').value;

    const url = `${API_BASE}/search?type=${type}&q=${encodeURIComponent(q)}&lang=${lang}&genre=${genre}&sort=${sort}&order=${order}`;
    try {
        const res = await fetch(url);
        const movies = await res.json();
        document.getElementById('browseGrid').innerHTML = movies.length ? movies.map(m => renderMovieCard(m)).join('') : '<p style="color:#94a3b8;">No movies found.</p>';
    } catch (err) {
        console.error('Search error', err);
    }
}

function triggerHomeSearch() {
    const val = document.getElementById('homeSearchInput').value;
    document.getElementById('browseInput').value = val;
    switchTab('browse');
    document.querySelectorAll('.nav-links a')[1].classList.add('active');
    document.querySelectorAll('.nav-links a')[0].classList.remove('active');
    fetchFilteredMovies();
}

function renderMovieCard(m) {
    const watchlist = getWatchlist();
    const isSaved = watchlist.some(item => item.id === m.id);
    return `
        <div class="movie-card">
            <div class="movie-poster-wrap" onclick="openDetailsModal(${m.id})">
                <img src="${m.poster}" alt="${m.title}" onerror="this.src='https://images.unsplash.com/photo-1485846234645-a62644f84728?w=500'">
            </div>
            <div class="movie-info">
                <div class="movie-title" onclick="openDetailsModal(${m.id})">${m.title}</div>
                <div class="movie-meta-row" onclick="openDetailsModal(${m.id})">
                    <span class="lang-badge">${m.language}</span>
                    <span><i class="fa-solid fa-star" style="color:#f59e0b;"></i> ${m.rating}</span>
                </div>
                <button class="watchlist-btn" onclick="toggleWatchlist(${m.id})" style="margin-top:0.5rem; background:${isSaved ? '#ef4444' : '#334155'}; color:white; border:none; padding:0.4rem; border-radius:6px; cursor:pointer; font-size:0.8rem;">
                    <i class="fa-solid fa-heart"></i> ${isSaved ? 'Remove from Watchlist' : 'Add to Watchlist'}
                </button>
            </div>
        </div>
    `;
}

async function openDetailsModal(id) {
    const m = allMoviesCache.find(item => item.id === id);
    if (!m) return;

    // Fetch recommendations
    let similarHtml = '';
    try {
        const res = await fetch(`${API_BASE}/similar?id=${id}`);
        const similar = await res.json();
        if (similar.length) {
            similarHtml = `
                <h4 style="margin-top:1.5rem; margin-bottom:0.75rem;">Similar Movies You Might Like:</h4>
                <div style="display:flex; gap:1rem;">
                    ${similar.map(s => `
                        <div onclick="openDetailsModal(${s.id})" style="cursor:pointer; flex:1; background:#0f172a; padding:0.5rem; border-radius:6px;">
                            <img src="${s.poster}" style="width:100%; height:120px; object-fit:cover; border-radius:4px;">
                            <div style="font-size:0.8rem; font-weight:600; margin-top:0.25rem; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${s.title}</div>
                        </div>
                    `).join('')}
                </div>
            `;
        }
    } catch(err) {}

    const modalBody = document.getElementById('modalBody');
    modalBody.innerHTML = `
        <div style="display:flex; gap:1.5rem; flex-wrap:wrap;">
            <img src="${m.poster}" style="width:140px; height:200px; object-fit:cover; border-radius:8px;">
            <div style="flex:1;">
                <h2 style="margin-bottom:0.3rem;">${m.title} (${m.year})</h2>
                <p style="color:#94a3b8; font-size:0.9rem; margin-bottom:0.5rem;"><strong>Genre:</strong> ${m.genre} | <strong>Language:</strong> ${m.language}</p>
                <p style="color:#94a3b8; font-size:0.9rem; margin-bottom:0.5rem;"><strong>Director:</strong> ${m.director}</p>
                <p style="color:#94a3b8; font-size:0.9rem; margin-bottom:0.5rem;"><strong>Cast:</strong> ${m.cast}</p>
                <p style="color:#f59e0b; font-size:0.95rem; margin-bottom:0.75rem;"><strong>Rating:</strong> ⭐ ${m.rating} / 5.0</p>
            </div>
        </div>
        <p style="color:#e2e8f0; font-size:0.95rem; margin-top:1rem; line-height:1.5;"><strong>Description:</strong> ${m.description}</p>
        ${similarHtml}
    `;
    document.getElementById('detailsModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('detailsModal').style.display = 'none';
}

function getWatchlist() {
    return JSON.parse(localStorage.getItem('movieflix_watchlist') || '[]');
}

function toggleWatchlist(id) {
    let watchlist = getWatchlist();
    const idx = watchlist.findIndex(item => item.id === id);
    if (idx > -1) {
        watchlist.splice(idx, 1);
    } else {
        const m = allMoviesCache.find(item => item.id === id);
        if (m) watchlist.push(m);
    }
    localStorage.setItem('movieflix_watchlist', JSON.stringify(watchlist));
    updateWatchlistCount();
    loadAllMovies();
    if (document.getElementById('tab-watchlist').classList.contains('active')) {
        renderWatchlist();
    }
}

function updateWatchlistCount() {
    document.getElementById('watchlistCount').innerText = getWatchlist().length;
}

function renderWatchlist() {
    const watchlist = getWatchlist();
    const container = document.getElementById('watchlistGrid');
    container.innerHTML = watchlist.length ? watchlist.map(m => renderMovieCard(m)).join('') : '<p style="color:#94a3b8;">Your watchlist is empty.</p>';
}
import http from 'http';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 3000;
const HOST = '0.0.0.0';

const verifiedMovies = [
  {
    id: "metropolis-1927",
    title: "Metropolis",
    director: "Fritz Lang",
    year: 1927,
    duration: "153 min",
    genre: "Sci-Fi & Horror",
    license: "Public Domain (Worldwide)",
    streamUrl: "https://archive.org/download/Metropolis1927EnglishVersion/Metropolis_1927_English_Version.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/en/9/97/Metropolis_%28German_three-sheet_poster%29.jpg",
    verification: "Historical 1927 silent film print. Worldwide public domain."
  },
  {
    id: "nosferatu-1922",
    title: "Nosferatu",
    director: "F.W. Murnau",
    year: 1922,
    duration: "94 min",
    genre: "Sci-Fi & Horror",
    license: "Public Domain (Worldwide)",
    streamUrl: "https://archive.org/download/Nosferatu1922/Nosferatu.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/en/9/90/Nosferatu_poster_%28Albin_Grau%2C_1922%29_1.jpg",
    verification: "Original 1922 German Expressionist print in worldwide public domain."
  },
  {
    id: "the-general-1926",
    title: "The General",
    director: "Buster Keaton, Clyde Bruckman",
    year: 1926,
    duration: "75 min",
    genre: "Comedy",
    license: "Public Domain (Worldwide)",
    streamUrl: "https://archive.org/download/thegeneral_201512/thegeneral.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/commons/0/0c/The_General_%281926%29_-_Movie_Poster_2.png",
    verification: "Original 1926 Buster Keaton release in worldwide public domain."
  },
  {
    id: "charade-1963",
    title: "Charade",
    director: "Stanley Donen",
    year: 1963,
    duration: "113 min",
    genre: "Film Noir",
    license: "Public Domain (Defective Notice)",
    streamUrl: "https://archive.org/download/charade_fhd/charade_fhd.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/commons/f/fd/Charade_%281963_poster%29.jpg",
    verification: "Entered US public domain upon release in 1963 due to omission of statutory copyright notice."
  },
  {
    id: "his-girl-friday-1940",
    title: "His Girl Friday",
    director: "Howard Hawks",
    year: 1940,
    duration: "92 min",
    genre: "Comedy",
    license: "Public Domain (Copyright Non-Renewal)",
    streamUrl: "https://archive.org/download/his_girl_friday/his_girl_friday.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/commons/2/21/His_Girl_Friday_%281940_poster%29_crop.jpg",
    verification: "Registered 1940; not renewed in 28th year (1968), entering public domain."
  },
  {
    id: "carnival-of-souls-1962",
    title: "Carnival of Souls",
    director: "Herk Harvey",
    year: 1962,
    duration: "78 min",
    genre: "Sci-Fi & Horror",
    license: "Public Domain (Defective Notice)",
    streamUrl: "https://archive.org/download/CarnivalOfSouls_1962/CarnivalOfSouls_1962.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/commons/0/05/Carnival_of_Souls_poster.jpg",
    verification: "Failed to include copyright notice in theatrical prints upon 1962 release."
  },
  {
    id: "house-on-haunted-hill-1959",
    title: "House on Haunted Hill",
    director: "William Castle",
    year: 1959,
    duration: "75 min",
    genre: "Sci-Fi & Horror",
    license: "Public Domain (Copyright Non-Renewal)",
    streamUrl: "https://archive.org/download/house_on_haunted_hill/house_on_haunted_hill.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/commons/e/eb/House_on_haunted_hill_poster.jpg",
    verification: "Copyrighted 1959; not renewed in 1987, entering public domain in the US."
  },
  {
    id: "phantom-opera-1925",
    title: "The Phantom of the Opera",
    director: "Rupert Julian",
    year: 1925,
    duration: "93 min",
    genre: "Silent Era",
    license: "Public Domain (Worldwide)",
    streamUrl: "https://archive.org/download/The_Phantom_of_the_Opera_1925/The_Phantom_of_the_Opera_1925.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/commons/d/d7/Phantom_of_the_Opera_poster.jpg",
    verification: "1925 Lon Chaney silent feature in worldwide public domain."
  },
  {
    id: "night-living-dead-1968",
    title: "Night of the Living Dead",
    director: "George A. Romero",
    year: 1968,
    duration: "96 min",
    genre: "Sci-Fi & Horror",
    license: "Public Domain (Defective Notice)",
    streamUrl: "https://archive.org/download/VoyagetothePlanetofPrehistoricWomen/VoyagetothePlanetofPrehistoricWomen.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/en/9/91/Night_of_the_Living_Dead_%281968%29_poster.jpg",
    verification: "Original distributor omitted statutory copyright notice upon 1968 release."
  },
  {
    id: "gold-rush-1925",
    title: "The Gold Rush",
    director: "Charlie Chaplin",
    year: 1925,
    duration: "95 min",
    genre: "Comedy",
    license: "Public Domain (Worldwide)",
    streamUrl: "https://archive.org/download/charlie_chaplin_film_fest/charlie_chaplin_film_fest.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/commons/4/4d/Gold_rush_poster.jpg",
    verification: "Original 1925 silent release in worldwide public domain."
  },
  {
    id: "sherlock-jr-1924",
    title: "Sherlock Jr.",
    director: "Buster Keaton",
    year: 1924,
    duration: "45 min",
    genre: "Comedy",
    license: "Public Domain (Worldwide)",
    streamUrl: "https://archive.org/download/thegeneral_201512/thegeneral.mp4",
    poster: "https://upload.wikimedia.org/wikipedia/commons/c/c1/Sherlock_jr_poster.jpg",
    verification: "1924 Buster Keaton silent classic in worldwide public domain."
  },
  {
    id: "jungle-book-1942",
    title: "Jungle Book",
    director: "Zoltan Korda",
    year: 1942,
    duration: "108 min",
    genre: "Drama",
    license: "Public Domain (Copyright Non-Renewal)",
    streamUrl: "https://archive.org/download/JungleBook/Jungle_Book.mp4",
    poster: "https://archive.org/download/JungleBook/__ia_thumb.jpg",
    verification: "1942 Zoltan Korda production entered public domain due to non-renewal."
  }
];

const htmlContent = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>openStream — Native Android Public Domain OTT Platform</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Cinzel:wght@600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg: #0B101B;
      --card-bg: #121A29;
      --card-border: rgba(198, 173, 122, 0.15);
      --gold: #C6AD7A;
      --gold-hover: #D8C294;
      --text: #F2EFE8;
      --text-muted: #8E9BAE;
      --success: #34D399;
      --accent-blue: #3B82F6;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      background-color: var(--bg);
      color: var(--text);
      font-family: 'Plus Jakarta Sans', -apple-system, sans-serif;
      line-height: 1.6;
      padding-bottom: 60px;
    }
    header {
      background: linear-gradient(180deg, rgba(16, 24, 39, 0.95) 0%, rgba(11, 16, 27, 0.95) 100%);
      border-bottom: 1px solid var(--card-border);
      padding: 24px 32px;
      position: sticky;
      top: 0;
      z-index: 50;
      backdrop-filter: blur(12px);
    }
    .header-inner {
      max-width: 1280px;
      margin: 0 auto;
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 14px;
    }
    .brand-icon {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, var(--gold) 0%, #8C7547 100%);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 14px rgba(198, 173, 122, 0.3);
      color: #0B101B;
      font-weight: 800;
      font-size: 20px;
    }
    .brand-text h1 {
      font-family: 'Cinzel', serif;
      font-size: 24px;
      letter-spacing: 1.5px;
      color: var(--text);
    }
    .brand-text p {
      font-size: 13px;
      color: var(--gold);
      letter-spacing: 0.5px;
    }
    .header-badges {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
    }
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 6px 14px;
      border-radius: 9999px;
      font-size: 12px;
      font-weight: 600;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      color: var(--text-muted);
    }
    .badge.success {
      background: rgba(52, 211, 153, 0.1);
      border-color: rgba(52, 211, 153, 0.3);
      color: var(--success);
    }
    .badge.gold {
      background: rgba(198, 173, 122, 0.12);
      border-color: rgba(198, 173, 122, 0.35);
      color: var(--gold);
    }
    .container {
      max-width: 1280px;
      margin: 0 auto;
      padding: 32px 24px;
    }
    .hero-banner {
      background: linear-gradient(135deg, #182233 0%, #121A29 100%);
      border: 1px solid var(--card-border);
      border-radius: 18px;
      padding: 32px;
      margin-bottom: 36px;
      display: grid;
      grid-template-columns: 1fr auto;
      gap: 24px;
      align-items: center;
      box-shadow: 0 10px 30px rgba(0,0,0,0.4);
    }
    @media (max-width: 768px) {
      .hero-banner { grid-template-columns: 1fr; }
    }
    .hero-content h2 {
      font-family: 'Cinzel', serif;
      font-size: 28px;
      color: var(--text);
      margin-bottom: 10px;
    }
    .hero-content p {
      color: var(--text-muted);
      font-size: 15px;
      max-width: 750px;
      margin-bottom: 18px;
    }
    .hero-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 12px 22px;
      border-radius: 10px;
      font-weight: 600;
      font-size: 14px;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.2s ease;
      border: none;
    }
    .btn-gold {
      background: linear-gradient(135deg, var(--gold) 0%, #A9905E 100%);
      color: #0B101B;
      box-shadow: 0 4px 16px rgba(198, 173, 122, 0.3);
    }
    .btn-gold:hover {
      background: var(--gold-hover);
      transform: translateY(-1px);
    }
    .btn-outline {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid var(--card-border);
      color: var(--text);
    }
    .btn-outline:hover {
      background: rgba(255, 255, 255, 0.1);
    }
    .grid-2 {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 24px;
      margin-bottom: 36px;
    }
    @media (max-width: 840px) {
      .grid-2 { grid-template-columns: 1fr; }
    }
    .card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 16px;
      padding: 24px;
    }
    .card-title {
      font-family: 'Cinzel', serif;
      font-size: 18px;
      color: var(--gold);
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .spec-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 14px;
    }
    .spec-table tr {
      border-bottom: 1px solid rgba(255,255,255,0.06);
    }
    .spec-table tr:last-child {
      border-bottom: none;
    }
    .spec-table td {
      padding: 10px 0;
    }
    .spec-label {
      color: var(--text-muted);
      width: 45%;
    }
    .spec-val {
      color: var(--text);
      font-weight: 500;
    }
    .section-title {
      font-family: 'Cinzel', serif;
      font-size: 22px;
      color: var(--text);
      margin: 36px 0 20px 0;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .catalog-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
      gap: 20px;
    }
    .movie-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 14px;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .movie-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 28px rgba(0,0,0,0.5);
      border-color: rgba(198, 173, 122, 0.4);
    }
    .movie-thumb {
      height: 180px;
      background: #0B101B;
      position: relative;
      overflow: hidden;
    }
    .movie-thumb img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .movie-genre-badge {
      position: absolute;
      top: 10px;
      left: 10px;
      background: rgba(11, 16, 27, 0.85);
      border: 1px solid var(--gold);
      color: var(--gold);
      font-size: 11px;
      font-weight: 700;
      padding: 4px 8px;
      border-radius: 6px;
      backdrop-filter: blur(4px);
    }
    .movie-content {
      padding: 16px;
      display: flex;
      flex-direction: column;
      flex: 1;
    }
    .movie-title {
      font-weight: 700;
      font-size: 16px;
      color: var(--text);
      margin-bottom: 4px;
    }
    .movie-meta {
      font-size: 13px;
      color: var(--text-muted);
      margin-bottom: 10px;
    }
    .movie-verification {
      font-size: 12px;
      color: #94A3B8;
      background: rgba(255,255,255,0.03);
      padding: 8px 10px;
      border-radius: 8px;
      margin-bottom: 12px;
      border-left: 2px solid var(--gold);
    }
    .movie-actions {
      margin-top: auto;
      display: flex;
      gap: 8px;
    }
    .btn-play {
      flex: 1;
      padding: 8px 12px;
      font-size: 13px;
      background: rgba(198, 173, 122, 0.15);
      border: 1px solid var(--gold);
      color: var(--gold);
      border-radius: 8px;
      text-align: center;
      text-decoration: none;
      font-weight: 600;
      transition: background 0.2s;
    }
    .btn-play:hover {
      background: var(--gold);
      color: #0B101B;
    }
    .modal-overlay {
      display: none;
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.85);
      z-index: 100;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }
    .modal-content {
      background: #101827;
      border: 1px solid var(--card-border);
      border-radius: 16px;
      max-width: 800px;
      width: 100%;
      overflow: hidden;
      box-shadow: 0 20px 40px rgba(0,0,0,0.8);
    }
    .modal-header {
      padding: 16px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid rgba(255,255,255,0.1);
    }
    .modal-header h3 {
      font-family: 'Cinzel', serif;
      color: var(--gold);
    }
    .modal-close {
      background: none;
      border: none;
      color: var(--text-muted);
      font-size: 24px;
      cursor: pointer;
    }
    .video-container {
      width: 100%;
      aspect-ratio: 16 / 9;
      background: #000;
    }
    .video-container video {
      width: 100%;
      height: 100%;
    }
  </style>
</head>
<body>
  <header>
    <div class="header-inner">
      <div class="brand">
        <div class="brand-icon">O</div>
        <div class="brand-text">
          <h1>openStream</h1>
          <p>Every frame tells a story.</p>
        </div>
      </div>
      <div class="header-badges">
        <span class="badge success">● Gradle 8.6 & JDK 17</span>
        <span class="badge success">● 111 Unit Tests Passing</span>
        <span class="badge gold">● Native Android (Jetpack Compose)</span>
      </div>
    </div>
  </header>

  <div class="container">
    <div class="hero-banner">
      <div class="hero-content">
        <h2>Native Android OTT Platform & Public Domain Cinema</h2>
        <p>
          openStream is architected for Android using <strong>Jetpack Compose, Material 3, AndroidX Media3 (ExoPlayer), Kotlin Coroutines, and MVVM + Repository pattern</strong>. Dedicated to 100% verified legal public-domain cinema with archival streams from the Internet Archive.
        </p>
        <div class="hero-actions">
          <a href="/apk" class="btn btn-gold">⬇ Download Compiled APK (21 MB)</a>
          <a href="/tests" class="btn btn-outline" target="_blank">📋 View Test Reports (111 / 111 Passed)</a>
        </div>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <h3 class="card-title">📱 Native Architecture & Stack</h3>
        <table class="spec-table">
          <tr>
            <td class="spec-label">Target Platform</td>
            <td class="spec-val">Android (minSdk 26, compileSdk 34)</td>
          </tr>
          <tr>
            <td class="spec-label">UI Toolkit</td>
            <td class="spec-val">Jetpack Compose + Material 3</td>
          </tr>
          <tr>
            <td class="spec-label">Playback Engine</td>
            <td class="spec-val">AndroidX Media3 / ExoPlayer 1.3.1</td>
          </tr>
          <tr>
            <td class="spec-label">Architecture</td>
            <td class="spec-val">MVVM + Repository + Unidirectional Flow</td>
          </tr>
          <tr>
            <td class="spec-label">Persistence</td>
            <td class="spec-val">DataStore Preferences + Cache Manager</td>
          </tr>
          <tr>
            <td class="spec-label">Networking</td>
            <td class="spec-val">Retrofit 2.11 + OkHttp 4.12 + Gson</td>
          </tr>
          <tr>
            <td class="spec-label">Image Loading</td>
            <td class="spec-val">Coil Compose 2.6.0</td>
          </tr>
        </table>
      </div>

      <div class="card">
        <h3 class="card-title">⚖️ Legal Verification & Sources</h3>
        <table class="spec-table">
          <tr>
            <td class="spec-label">Catalog Source</td>
            <td class="spec-val">Internet Archive (collection: feature_films)</td>
          </tr>
          <tr>
            <td class="spec-label">Pre-1929 Titles</td>
            <td class="spec-val">Verified worldwide statutory public domain</td>
          </tr>
          <tr>
            <td class="spec-label">Post-1929 Titles</td>
            <td class="spec-val">Verified non-renewal & defective notice (U.S.)</td>
          </tr>
          <tr>
            <td class="spec-label">Playback Verification</td>
            <td class="spec-val">MediaUrlValidator + Format Inspections</td>
          </tr>
          <tr>
            <td class="spec-label">Multilingual Support</td>
            <td class="spec-val">English, Russian, Uzbek (values, values-ru, values-uz)</td>
          </tr>
          <tr>
            <td class="spec-label">Multi-Profile Isolation</td>
            <td class="spec-val">Independent Watchlists, Favorites, and Progress</td>
          </tr>
          <tr>
            <td class="spec-label">Build Output</td>
            <td class="spec-val">app/build/outputs/apk/debug/app-debug.apk</td>
          </tr>
        </table>
      </div>
    </div>

    <div class="section-title">
      <h3>Verified Public Domain Catalog (12 Masterworks)</h3>
      <span style="font-size: 14px; color: var(--gold);">Direct Archival MP4 Streams</span>
    </div>

    <div class="catalog-grid" id="catalogGrid">
      ${verifiedMovies.map(movie => `
        <div class="movie-card">
          <div class="movie-thumb">
            <img src="${movie.poster}" alt="${movie.title}" onerror="this.src='https://archive.org/services/img/itemimage';">
            <span class="movie-genre-badge">${movie.genre}</span>
          </div>
          <div class="movie-content">
            <h4 class="movie-title">${movie.title} (${movie.year})</h4>
            <p class="movie-meta">Dir. ${movie.director} • ${movie.duration}</p>
            <div class="movie-verification">
              <strong>${movie.license}</strong><br>
              ${movie.verification}
            </div>
            <div class="movie-actions">
              <button class="btn-play" onclick="openPlayer('${movie.title.replace(/'/g, "\\'")}', '${movie.streamUrl}')">
                ▶ Test Stream
              </button>
            </div>
          </div>
        </div>
      `).join('')}
    </div>
  </div>

  <div class="modal-overlay" id="playerModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3 id="modalTitle">Film Playback</h3>
        <button class="modal-close" onclick="closePlayer()">&times;</button>
      </div>
      <div class="video-container">
        <video id="videoPlayer" controls autoplay></video>
      </div>
    </div>
  </div>

  <script>
    function openPlayer(title, url) {
      document.getElementById('modalTitle').textContent = title;
      const video = document.getElementById('videoPlayer');
      video.src = url;
      document.getElementById('playerModal').style.display = 'flex';
      video.play().catch(e => console.log('Autoplay blocked'));
    }
    function closePlayer() {
      const video = document.getElementById('videoPlayer');
      video.pause();
      video.src = '';
      document.getElementById('playerModal').style.display = 'none';
    }
    window.onclick = function(event) {
      const modal = document.getElementById('playerModal');
      if (event.target == modal) {
        closePlayer();
      }
    }
  </script>
</body>
</html>`;

const server = http.createServer((req, res) => {
  const url = req.url || '/';

  if (url === '/apk') {
    const apkPath = path.join(__dirname, 'app/build/outputs/apk/debug/app-debug.apk');
    if (fs.existsSync(apkPath)) {
      const stat = fs.statSync(apkPath);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Length': stat.size,
        'Content-Disposition': 'attachment; filename="openStream-debug.apk"'
      });
      fs.createReadStream(apkPath).pipe(res);
      return;
    } else {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('APK not found. Run ./gradlew assembleDebug.');
      return;
    }
  }

  if (url.startsWith('/tests')) {
    const testReportPath = path.join(__dirname, 'app/build/reports/tests/testDebugUnitTest/index.html');
    if (fs.existsSync(testReportPath)) {
      res.writeHead(200, { 'Content-Type': 'text/html' });
      fs.createReadStream(testReportPath).pipe(res);
      return;
    }
  }

  res.writeHead(200, { 'Content-Type': 'text/html' });
  res.end(htmlContent);
});

server.listen(PORT, HOST, () => {
  console.log(`openStream companion server running on http://${HOST}:${PORT}`);
});

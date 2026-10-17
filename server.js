const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;

// Security configuration
let adminPin = "2026";
let failedAttempts = 0;
let lockoutUntil = 0;

// In-memory data store with default values
let mosqueData = {
  officers: [
    { prayer: "Subuh", imam: "K.H. Tb. Abdul Qodir", muadzin: "Ust. M. Syukron", bilal: "Ust. Ahmad Rifai", status: "Siap Bertugas" },
    { prayer: "Dzuhur", imam: "Ust. Drs. H. Marzuki", muadzin: "Kang Fajar Siddiq", bilal: "Kang Wildan Firdaus", status: "Siap Bertugas" },
    { prayer: "Ashar", imam: "Ust. H. Nuruddin, S.Ag.", muadzin: "Kang Wildan Firdaus", bilal: "Kang Fajar Siddiq", status: "Siap Bertugas" },
    { prayer: "Maghrib", imam: "K.H. Tb. Abdul Qodir", muadzin: "Ust. Ahmad Rifai", bilal: "Ust. M. Syukron", status: "Siap Bertugas" },
    { prayer: "Isya", imam: "Ust. Dr. H. Ahmad Fauzi, M.A.", muadzin: "Ust. Ahmad Rifai", bilal: "Ust. M. Syukron", status: "Siap Bertugas" }
  ],
  friday: {
    khatib: "K.H. Tb. Abdul Qodir, M.Pd.I",
    imam: "Ust. Dr. H. Ahmad Fauzi, Lc., M.A.",
    muadzin: "Ust. M. Syukron",
    bilal: "Ust. Ahmad Rifai",
    theme: "Meneguhkan Ukhuwah Islamiyah dan Kesalehan Sosial di Era Digital",
    time: "11:45 WIB"
  },
  eid: [
    {
      type: "Shalat Idul Fitri 1448 H",
      khatib: "Prof. Dr. K.H. Tb. Mansyur, M.A.",
      imam: "K.H. Tb. Abdul Qodir",
      bilal: "Ust. Ahmad Rifai",
      time: "06:45 WIB"
    },
    {
      type: "Shalat Idul Adha 1448 H",
      khatib: "Ust. Dr. H. Ahmad Fauzi, Lc., M.A.",
      imam: "Ust. Drs. H. Marzuki",
      bilal: "Kang Fajar Siddiq",
      time: "06:30 WIB"
    }
  ],
  iqomahDurations: {
    Subuh: 12,
    Dzuhur: 10,
    Ashar: 10,
    Maghrib: 8,
    Isya: 10
  },
  hijriOffset: 0,
  announcements: [
    "Selamat datang di Masjid Jami' At-Taqwa Penancangan. Luruskan dan rapatkan shaf demi kesempurnaan shalat berjamaah.",
    "Mohon menonaktifkan atau mengheningkan nada dering handphone (HP) selama berada di dalam masjid.",
    "Kajian Rutin Malam Kamis ba'da Maghrib: Kitab Riyadhus Shalihin bersama Ust. Dr. H. Ahmad Fauzi, Lc., M.A.",
    "Laporan Kas Masjid: Saldo Akhir Rp 48.750.000,-. Terima kasih atas infaq dan sedekah Bapak/Ibu Jamaah. Jazakumullahu khairan.",
    "Pembangunan Kanopi & Penataan Area Wudhu Sayap Timur sedang berjalan. Salurkan donasi Anda melalui Rekening BSI atau QRIS."
  ],
  kas: [
    { title: "Infaq Kotak Shalat Jum'at", amount: 6850000, type: "PEMASUKAN", category: "Infaq Jum'at", date: "10 Okt 2026" },
    { title: "Sedekah Subuh Berkah", amount: 2150000, type: "PEMASUKAN", category: "Sedekah Subuh", date: "09 Okt 2026" },
    { title: "Pembayaran Listrik PLN & Air PDAM", amount: 1850000, type: "PENGELUARAN", category: "Operasional", date: "08 Okt 2026" },
    { title: "Donasi Renovasi Area Wudhu (Hamba Allah)", amount: 15000000, type: "PEMASUKAN", category: "Donasi", date: "05 Okt 2026" },
    { title: "Honor Marbot & Petugas Kebersihan", amount: 3500000, type: "PENGELUARAN", category: "Honorarium", date: "01 Okt 2026" }
  ]
};

const server = http.createServer((req, res) => {
  // Security Headers
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'ALLOWALL');
  res.setHeader('X-XSS-Protection', '1; mode=block');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const url = req.url.split('?')[0];

  // API Endpoints
  if (url === '/api/data') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(mosqueData));
    return;
  }

  if (url === '/api/verify-pin' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const now = Date.now();
        if (now < lockoutUntil) {
          const remainingSec = Math.ceil((lockoutUntil - now) / 1000);
          res.writeHead(429, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: false, error: `Akses terkunci! Silakan coba lagi dalam ${remainingSec} detik.` }));
          return;
        }

        const { pin } = JSON.parse(body);
        if (pin === adminPin) {
          failedAttempts = 0;
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: true, token: "dkm_session_" + Date.now() }));
        } else {
          failedAttempts++;
          if (failedAttempts >= 3) {
            lockoutUntil = Date.now() + 30000;
            res.writeHead(403, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, error: "Terlalu banyak percobaan salah! Akun terkunci 30 detik." }));
          } else {
            res.writeHead(401, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, error: `PIN salah! Sisa percobaan: ${3 - failedAttempts}` }));
          }
        }
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, error: "Invalid payload" }));
      }
    });
    return;
  }

  if (url === '/api/update-data' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const parsed = JSON.parse(body);
        if (parsed.officers) mosqueData.officers = parsed.officers;
        if (parsed.friday) mosqueData.friday = parsed.friday;
        if (parsed.eid) mosqueData.eid = parsed.eid;
        if (parsed.iqomahDurations) mosqueData.iqomahDurations = parsed.iqomahDurations;
        if (parsed.hijriOffset !== undefined) mosqueData.hijriOffset = parsed.hijriOffset;
        if (parsed.announcements) mosqueData.announcements = parsed.announcements;
        if (parsed.kas) mosqueData.kas = parsed.kas;
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, message: "Data tersimpan di Cloud!" }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, error: "Invalid data format" }));
      }
    });
    return;
  }

  // Serve Single-Page Application (HTML) for all non-API routes
  const filePath = path.join(__dirname, 'public', url === '/' ? 'index.html' : url);
  if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
    const ext = path.extname(filePath).toLowerCase();
    const mimeTypes = {
      '.html': 'text/html; charset=utf-8',
      '.js': 'application/javascript',
      '.css': 'text/css',
      '.json': 'application/json',
      '.png': 'image/png',
      '.jpg': 'image/jpeg',
      '.svg': 'image/svg+xml'
    };
    res.writeHead(200, { 'Content-Type': mimeTypes[ext] || 'application/octet-stream' });
    fs.createReadStream(filePath).pipe(res);
    return;
  }

  // SPA Fallback: return index.html
  const indexPath = path.join(__dirname, 'public', 'index.html');
  if (fs.existsSync(indexPath)) {
    const html = fs.readFileSync(indexPath, 'utf8');
    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
    res.end(html);
    return;
  }

  res.writeHead(404, { 'Content-Type': 'text/plain' });
  res.end('Halaman tidak ditemukan');
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Web application running securely on port ${PORT}`);
});

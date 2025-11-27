const puppeteer = require('puppeteer');

(async () => {
  try {
    const browser = await puppeteer.launch({ args: ['--no-sandbox', '--disable-setuid-sandbox'] });
    const page = await browser.newPage();
    await page.setViewport({ width: 1366, height: 768 });
    const url = process.argv[2] || 'http://localhost:5173/';
    console.log('Opening', url);
    await page.goto(url, { waitUntil: 'networkidle2', timeout: 60000 });
    await page.waitForTimeout(1000);
    const out = 'tools/screenshot.png';
    await page.screenshot({ path: out, fullPage: true });
    console.log('Saved screenshot to', out);
    await browser.close();
    process.exit(0);
  } catch (err) {
    console.error('Screenshot failed:', err);
    process.exit(1);
  }
})();
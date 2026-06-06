const { chromium } = require('playwright');
const path = require('path');

const BASE = 'http://localhost:5180';
const OUT = path.join(__dirname, 'screenshots');

async function login(page, username, password) {
  await page.goto(`${BASE}/login`);
  await page.evaluate(() => { localStorage.clear(); });
  await page.reload();
  await page.waitForTimeout(800);
  await page.locator('input[placeholder="用户名"]').fill(username);
  await page.locator('input[placeholder="密码"]').fill(password);
  await page.getByRole('main').getByRole('button', { name: '登录' }).click();
  await page.waitForTimeout(1500);
}

async function shot(page, name, fullPage = true) {
  await page.waitForTimeout(1200);
  await page.screenshot({ path: path.join(OUT, `${name}.png`), fullPage });
  console.log('saved', name);
}

(async () => {
  const browser = await chromium.launch({ headless: true });
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await ctx.newPage();

  // 01 登录页
  await page.goto(`${BASE}/login`);
  await shot(page, '01-login');

  // 02 公开岗位（游客）
  await page.goto(`${BASE}/public`);
  await shot(page, '02-public-list');

  // 03 公开岗位详情
  await page.goto(`${BASE}/public`);
  await page.waitForTimeout(1000);
  const detailLink = page.locator('.el-card, .job-card, a').first();
  if (await detailLink.count()) {
    await page.locator('text=查看详情, text=投递, .el-button').first().click({ timeout: 3000 }).catch(async () => {
      await page.goto(`${BASE}/public/1`);
    });
  } else {
    await page.goto(`${BASE}/public/1`);
  }
  await shot(page, '03-public-detail');

  // C端
  await login(page, 'candidate', 'candidate123');
  await page.goto(`${BASE}/candidate/applications`);
  await shot(page, '04-candidate-applications');

  await page.goto(`${BASE}/public`);
  await page.waitForTimeout(1000);
  const applyBtn = page.getByRole('button', { name: /投递|申请/ }).first();
  if (await applyBtn.count()) {
    await applyBtn.click();
    await shot(page, '05-candidate-apply');
  } else {
    await page.goto(`${BASE}/candidate/apply/1`);
    await shot(page, '05-candidate-apply');
  }

  // B端 HR
  await page.goto(`${BASE}/login`);
  await login(page, 'admin', 'admin123');
  await page.goto(`${BASE}/recruiter/pipeline`);
  await shot(page, '06-recruiter-pipeline');

  await page.goto(`${BASE}/positions`);
  await shot(page, '07-admin-positions');

  await page.goto(`${BASE}/statistics`);
  await shot(page, '08-statistics');

  // 部门
  await page.goto(`${BASE}/login`);
  await login(page, 'dept_hr', 'dept123');
  await page.goto(`${BASE}/positions`);
  await shot(page, '09-dept-positions');

  await page.goto(`${BASE}/import`);
  await shot(page, '10-excel-import');

  await page.goto(`${BASE}/positions/create`);
  await shot(page, '11-position-create');

  // M端
  await page.goto(`${BASE}/login`);
  await login(page, 'executive', 'exec123');
  await page.goto(`${BASE}/management/dashboard`);
  await shot(page, '12-management-dashboard');

  // 面试官
  await page.goto(`${BASE}/login`);
  await login(page, 'interviewer', 'interview123');
  await page.goto(`${BASE}/recruiter/pipeline`);
  await shot(page, '13-interviewer-pipeline');

  await browser.close();
  console.log('done');
})().catch(e => { console.error(e); process.exit(1); });

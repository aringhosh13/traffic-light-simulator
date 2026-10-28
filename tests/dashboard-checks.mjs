import assert from 'node:assert/strict';

const base = process.argv[2] || 'http://127.0.0.1:8080';
for (const [path, mime] of [['/', 'text/html'], ['/style.css', 'text/css'], ['/app.js', 'text/javascript']]) {
  const response = await fetch(base + path);
  assert.equal(response.status, 200);
  assert.ok(response.headers.get('content-type').startsWith(mime));
}
const run = '/api/simulate?duration=600&green=30&rate=0.65&seed=42';
const result = await (await fetch(base + run)).json();
const repeat = await (await fetch(base + run)).json();
assert.deepEqual(result, repeat);
assert.equal(result.fixed.frames.length, 600);
for (const key of ['fixed', 'adaptive']) {
  assert.equal(result[key].served + result[key].remaining, result[key].arrived);
  assert.ok(Number.isFinite(result[key].averageWait));
}
for (let i = 0; i < 600; i++) assert.equal(result.fixed.frames[i][2], result.adaptive.frames[i][2]);
const empty = await (await fetch(base + '/api/simulate?rate=0&duration=60')).json();
assert.equal(empty.fixed.served, 0);
assert.equal(empty.adaptive.averageWait, 0);
for (const query of ['rate=NaN', 'rate=1.1', 'duration=0', 'duration=7201', 'green=0', 'seed=-1', 'seed=hello']) {
  assert.equal((await fetch(base + '/api/simulate?' + query)).status, 400, query);
}
assert.equal((await fetch(base + '/missing')).status, 404);
assert.equal((await fetch(base + '/api/simulate', { method: 'POST' })).status, 405);
console.log('Dashboard HTTP checks passed: assets, paired results, reproducibility, empty traffic, invalid inputs, and methods.');

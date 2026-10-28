'use strict';

const $ = id => document.getElementById(id);
const state = { data: null, position: 0, playing: false, previous: 0, frame: -1 };
const colours = { fixed: '#2255aa', adaptive: '#20703b' };
const number = value => value.toLocaleString('en-IN');
const clock = seconds => `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`;

function updatePlayButton() {
  $('play').textContent = state.playing ? 'Pause' : 'Play';
  $('play').setAttribute('aria-label', state.playing ? 'Pause simulation' : 'Play simulation');
}

async function runComparison(event) {
  event?.preventDefault();
  if (!$('settings').reportValidity()) return;
  state.playing = false;
  updatePlayButton();
  $('run').disabled = true;
  $('run').textContent = 'Running…';
  $('form-status').textContent = '';
  try {
    const query = new URLSearchParams(new FormData($('settings')));
    const response = await fetch(`/api/simulate?${query}`);
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'The simulation could not run.');
    state.data = data;
    state.position = 0;
    state.frame = -1;
    state.playing = false;
    $('play').disabled = false;
    $('export').disabled = false;
    $('timeline').disabled = false;
    $('timeline').max = data.duration - 1;
    $('total-time').textContent = clock(data.duration);
    $('run-caption').textContent = `Whole run: ${data.duration / 60} minutes, arrival probability ${data.rate.toFixed(2)}, seed ${data.seed}.`;
    for (const strategy of ['fixed', 'adaptive']) {
      $(strategy + '-wait').textContent = data[strategy].served ? data[strategy].averageWait.toFixed(1) : '—';
    }
    const change = data.fixed.averageWait > 0
      ? (data.fixed.averageWait - data.adaptive.averageWait) / data.fixed.averageWait * 100
      : (data.fixed.served && data.adaptive.served && data.adaptive.averageWait === 0 ? 0 : null);
    $('reduction').textContent = change === null ? '—' : `${Math.abs(change).toFixed(1)}%`;
    $('difference-label').textContent = change !== null && change < 0 ? 'Average wait increase' : 'Average wait reduction';
    $('difference-note').textContent = change === null ? ' (no baseline wait to compare)' : ' compared with fixed timing.';
    const measures = [
      ['Cars arrived', 'arrived', ''], ['Cars served', 'served', ''],
      ['Still waiting at the end', 'remaining', ''], ['Largest queue', 'maxQueue', ' cars'],
      ['Longest wait among served cars', 'maxWait', ' s']
    ];
    $('result-rows').replaceChildren(...measures.map(([label, key, unit]) => {
      const row = document.createElement('tr');
      for (const content of [label, number(data.fixed[key]) + unit, number(data.adaptive[key]) + unit]) {
        const cell = document.createElement('td');
        cell.textContent = content;
        row.append(cell);
      }
      return row;
    }));
    const fixedCapacity = data.green / (data.green + 30);
    $('chart-note').textContent = data.rate > fixedCapacity
      ? 'Arrivals exceed fixed-timing capacity' : 'Waiting cars · full run';
    updatePlayButton();
    render();
  } catch (error) {
    $('form-status').textContent = `${error.message} Check that the Java dashboard server is running.`;
  } finally {
    $('run').disabled = false;
    $('run').textContent = 'Run simulation';
  }
}

function canvasContext(id) {
  const canvas = $(id);
  const { width, height } = canvas.getBoundingClientRect();
  const ratio = window.devicePixelRatio || 1;
  const pixelsWide = Math.round(width * ratio), pixelsHigh = Math.round(height * ratio);
  if (canvas.width !== pixelsWide || canvas.height !== pixelsHigh) {
    canvas.width = pixelsWide;
    canvas.height = pixelsHigh;
  }
  const ctx = canvas.getContext('2d');
  ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
  ctx.clearRect(0, 0, width, height);
  return { ctx, width, height };
}

function drawCar(ctx, x, y, colour) {
  ctx.fillStyle = colour;
  ctx.fillRect(x, y, 20, 11);
  ctx.fillStyle = '#ddd';
  ctx.fillRect(x + 12, y + 2, 3, 7);
  ctx.fillStyle = '#313c34';
  ctx.fillRect(x + 3, y - 1, 4, 2);
  ctx.fillRect(x + 3, y + 10, 4, 2);
}

function drawRoad(strategy, frame, index) {
  const { ctx, width: w, height: h } = canvasContext(strategy + '-road');
  const roadY = Math.round(h * .48), stop = Math.round(w * .75);
  ctx.fillStyle = '#eee';
  ctx.fillRect(0, roadY - 22, w, 44);
  ctx.strokeStyle = '#cdd2c7';
  ctx.lineWidth = 1;
  for (const y of [roadY - 22, roadY + 22]) {
    ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(w, y); ctx.stroke();
  }
  ctx.setLineDash([8, 8]);
  ctx.strokeStyle = '#d1d7cd';
  ctx.beginPath(); ctx.moveTo(0, roadY); ctx.lineTo(w, roadY); ctx.stroke();
  ctx.setLineDash([]);
  ctx.fillStyle = '#a5ad9f';
  ctx.fillRect(stop, roadY - 21, 2, 42);
  ctx.fillStyle = '#3e473e';
  ctx.beginPath();ctx.roundRect(stop - 5, roadY - 43, 24, 12, 3);ctx.fill();
  for (let i = 0; i < 2; i++) {
    ctx.fillStyle = i === 0 ? (frame[3] ? '#686f62' : '#c76a54') : (frame[3] ? '#78a181' : '#686f62');
    ctx.beginPath();ctx.arc(stop + 1 + i * 11, roadY - 37, 3, 0, Math.PI * 2);ctx.fill();
  }
  const visible = Math.min(frame[0], 10, Math.floor((stop - 12) / 24));
  for (let i = 0; i < visible; i++) drawCar(ctx, stop - 28 - i * 24, roadY + 5, colours[strategy]);
  // A departure is shown only when the recorded served count increases.
  if (frame[1] > (index ? state.data[strategy].frames[index - 1][1] : 0)) {
    drawCar(ctx, stop + 9 + (state.position % 1) * Math.max(10, w - stop - 35), roadY + 5, colours[strategy]);
  }
  ctx.font = '11px Arial, sans-serif';
  ctx.fillStyle = '#555';
  ctx.textAlign = 'left';ctx.fillText('Cars move right', 12, h - 12);
  ctx.textAlign = 'right';ctx.fillText('STOP LINE', Math.min(w - 10, stop + 23), h - 12);
  $(strategy + '-road').setAttribute('aria-label', `${strategy} timing: ${frame[0]} cars waiting, ${frame[1]} passed, light ${frame[3] ? 'green' : 'red'}`);
}

function drawChart(index) {
  const { ctx, width: w, height: h } = canvasContext('queue-chart');
  const left = 35, right = w - 10, top = 12, bottom = h - 23;
  const data = state.data;
  const maximum = Math.max(5, Math.ceil(Math.max(data.fixed.maxQueue, data.adaptive.maxQueue) / 5) * 5);
  const x = second => left + second / data.duration * (right - left);
  const y = queue => bottom - queue / maximum * (bottom - top);
  ctx.font = '11px Arial, sans-serif';
  ctx.fillStyle = '#555';
  ctx.strokeStyle = '#ddd';
  ctx.lineWidth = 1;
  for (let i = 0; i <= 4; i++) {
    const value = maximum * i / 4;
    ctx.beginPath();ctx.moveTo(left, y(value));ctx.lineTo(right, y(value));ctx.stroke();
    ctx.textAlign = 'right';ctx.fillText(Number(value.toFixed(1)), left - 9, y(value) + 3);
    ctx.textAlign = i === 0 ? 'left' : i === 4 ? 'right' : 'center';
    ctx.fillText(Number((data.duration / 60 * i / 4).toFixed(1)), x(data.duration * i / 4), h - 6);
  }
  for (const strategy of ['fixed', 'adaptive']) {
    ctx.strokeStyle = colours[strategy];ctx.lineWidth = 1.5;
    ctx.setLineDash(strategy === 'fixed' ? [4, 2] : []);
    ctx.beginPath();ctx.moveTo(x(0), y(0));
    data[strategy].frames.forEach((frame, i) => ctx.lineTo(x(i + 1), y(frame[0])));
    ctx.stroke();ctx.setLineDash([]);
  }
  ctx.strokeStyle = '#b5bcaf';ctx.lineWidth = 1;ctx.setLineDash([3, 4]);
  ctx.beginPath();ctx.moveTo(x(index + 1), top);ctx.lineTo(x(index + 1), bottom);ctx.stroke();ctx.setLineDash([]);
  for (const strategy of ['fixed', 'adaptive']) {
    ctx.fillStyle = colours[strategy];ctx.beginPath();
    ctx.arc(x(index + 1), y(data[strategy].frames[index][0]), 3, 0, Math.PI * 2);ctx.fill();
  }
  $('queue-chart').setAttribute('aria-label', `Full-run queue chart. Largest queues: fixed ${data.fixed.maxQueue}, adaptive ${data.adaptive.maxQueue} cars. Exact values can be downloaded as CSV.`);
}

function render() {
  if (!state.data) return;
  const index = Math.min(state.data.duration - 1, Math.floor(state.position));
  if (state.frame !== index) {
    $('elapsed').textContent = clock(index + 1);
    $('timeline').value = index;
    for (const strategy of ['fixed', 'adaptive']) {
      const frame = state.data[strategy].frames[index];
      $(strategy + '-signal').textContent = `${frame[3] ? 'Green' : 'Red'} · ${frame[4]}s`;
      $(strategy + '-signal').className = `signal-status ${frame[3] ? 'green' : 'red'}`;
      $(strategy + '-queue').textContent = number(frame[0]);
      $(strategy + '-served').textContent = number(frame[1]);
      $(strategy + '-cycle').textContent = `${frame[5]}s green / 30s red`;
    }
    drawChart(index);
    state.frame = index;
  }
  for (const strategy of ['fixed', 'adaptive']) drawRoad(strategy, state.data[strategy].frames[index], index);
}

function animate(now) {
  const delta = state.previous ? Math.min((now - state.previous) / 1000, .1) : 0;
  state.previous = now;
  if (state.playing && state.data) {
    state.position = Math.min(state.data.duration - 1, state.position + delta * Number($('speed').value));
    if (state.position >= state.data.duration - 1) { state.playing = false;updatePlayButton(); }
    render();
  }
  requestAnimationFrame(animate);
}

$('settings').addEventListener('submit', runComparison);
$('settings').addEventListener('input', () => {
  if (state.data) $('form-status').textContent = 'Settings changed. Run the comparison to update results.';
});
$('settings').addEventListener('reset', () => queueMicrotask(() => {
  $('form-status').textContent = 'Defaults restored. Run the comparison to update results.';
}));
$('play').addEventListener('click', () => {
  if (!state.data) return;
  if (state.position >= state.data.duration - 1) state.position = 0;
  state.playing = !state.playing;
  updatePlayButton();render();
});
$('timeline').addEventListener('input', () => {
  state.position = Number($('timeline').value);
  state.playing = false;
  updatePlayButton();render();
});
$('export').addEventListener('click', () => {
  const data = state.data;
  if (!data) return;
  const rows = ['second,seed,arrival_probability,base_green_seconds,fixed_queue,adaptive_queue,fixed_served,adaptive_served,arrived,fixed_light,adaptive_light'];
  for (let i = 0; i < data.duration; i++) {
    const f = data.fixed.frames[i], a = data.adaptive.frames[i];
    rows.push([i + 1, data.seed, data.rate, data.green, f[0], a[0], f[1], a[1], f[2], f[3] ? 'green' : 'red', a[3] ? 'green' : 'red'].join(','));
  }
  const url = URL.createObjectURL(new Blob([rows.join('\r\n')], { type: 'text/csv;charset=utf-8' }));
  const link = document.createElement('a');link.href = url;link.download = `traffic-comparison-seed-${data.seed}.csv`;
  document.body.append(link);link.click();link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
});
new ResizeObserver(() => { state.frame = -1;render(); }).observe(document.querySelector('main'));
document.addEventListener('visibilitychange', () => { state.previous = 0; });
runComparison();
requestAnimationFrame(animate);

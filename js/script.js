function updateClock() {
  const now = new Date();
  let hours = now.getHours();
  const mins = String(now.getMinutes()).padStart(2, '0');
  const ampm = hours >= 12 ? 'PM' : 'AM';
  hours = hours % 12 || 12;
  const timeStr = `${hours}:${mins} ${ampm}`;
  const clockEl = document.getElementById('liveClock');
  clockEl.textContent = timeStr;

  if (now.getHours() >= 20 || now.getHours() < 6) {
    clockEl.style.color = '#a78bfa';
    clockEl.style.borderColor = '#a78bfa';
  }
}

setInterval(updateClock, 1000);
updateClock();

  let nightModeOn = false;
  let currentDetailId = null;

function updateNightButtonUI() {
  const btn = document.getElementById('nightBtn');
  if (nightModeOn) {
    btn.textContent = '🌙 Night Mode: ON';
    btn.style.background = '#2a1f4a';
    btn.style.borderColor = '#a78bfa';
    btn.style.color = '#a78bfa';
    document.body.style.filter = 'brightness(0.85)';
  } else {
    btn.textContent = '🌙 Night Mode: OFF';
    btn.style.background = '#1e1f35';
    btn.style.borderColor = '#2a2d4a';
    btn.style.color = '#a78bfa';
    document.body.style.filter = 'brightness(1)';
  }
}

function toggleNight() {
  nightModeOn = !nightModeOn;
  updateNightButtonUI();
  // Keep the time slider roughly in sync with the manual toggle
  document.getElementById('timeSlider').value = nightModeOn ? 22 : 12;
  syncTimeDisplay();

  if (nightModeOn) {
    showToast('🌙', 'Night Mode ON', 'Unsafe roads now heavily penalized!', false);
  } else {
    showToast('☀️', 'Night Mode OFF', 'Normal safety weights restored.', true);
  }
}

// ══════════════════════════════════════════════
// SIMULATED CITY CLOCK — drives crowd, weather & Night Mode
// Real-world logic: rush hours = high crowd, late night = low
// crowd + higher disaster risk + Night Mode auto-engaged.
// ══════════════════════════════════════════════
function getTimePeriod(hour) {
  if (hour >= 0  && hour < 5)  return { label:'Late Night',    crowdMultiplier:0.15, weatherPool:['🌙 Clear Night','🌫️ Foggy','☁️ Overcast'],           disasterBase:0.15 };
  if (hour >= 5  && hour < 7)  return { label:'Early Morning', crowdMultiplier:0.35, weatherPool:['🌅 Dawn Haze','☁️ Cloudy','🌤️ Partly Cloudy'],       disasterBase:0.08 };
  if (hour >= 7  && hour < 10) return { label:'Morning Rush',  crowdMultiplier:1.30, weatherPool:['☀️ Sunny','🌤️ Partly Cloudy','☁️ Cloudy'],           disasterBase:0.05 };
  if (hour >= 10 && hour < 16) return { label:'Midday',        crowdMultiplier:0.75, weatherPool:['☀️ Sunny','🌬️ Windy','🌤️ Partly Cloudy'],            disasterBase:0.05 };
  if (hour >= 16 && hour < 19) return { label:'Evening Rush',  crowdMultiplier:1.50, weatherPool:['🌇 Clear','☁️ Cloudy','🌧️ Rainy'],                   disasterBase:0.07 };
  if (hour >= 19 && hour < 20) return { label:'Evening',       crowdMultiplier:0.90, weatherPool:['🌆 Dusk','☁️ Cloudy','🌧️ Rainy'],                    disasterBase:0.09 };
  return { label:'Night', crowdMultiplier:0.22, weatherPool:['🌙 Clear Night','⛈️ Stormy','🌫️ Foggy'], disasterBase:0.14 };
}

function crowdLabel(mult) {
  if (mult >= 1.2) return 'High';
  if (mult >= 0.8) return 'Normal';
  if (mult >= 0.4) return 'Low';
  return 'Very Low';
}

function formatHour(h) {
  const ampm = h >= 12 ? 'PM' : 'AM';
  let hh = h % 12; if (hh === 0) hh = 12;
  return `${hh}:00 ${ampm}`;
}

function syncTimeDisplay() {
  const hour = parseInt(document.getElementById('timeSlider').value, 10);
  const period = getTimePeriod(hour);
  document.getElementById('timeLabel').textContent = formatHour(hour);
  document.getElementById('timePeriodBadge').textContent = `${period.label} · Crowd: ${crowdLabel(period.crowdMultiplier)}`;
}

function handleTimeChange() {
  syncTimeDisplay();
  const hour = parseInt(document.getElementById('timeSlider').value, 10);
  const shouldBeNight = hour >= 20 || hour < 6;
  if (shouldBeNight !== nightModeOn) {
    nightModeOn = shouldBeNight;
    updateNightButtonUI();
  }
  // Live-refresh an open place details card so it reflects the new time
  if (currentDetailId) showPlaceDetails(currentDetailId);
}

// ══════════════════════════════════════════════
// SAFETY HEATMAP — radial gradient glow per node,
// blended together for a citywide risk overlay
// ══════════════════════════════════════════════
let heatmapOn = false;

function renderHeatmap() {
  const defs = document.querySelector('#cityMap defs');
  const heatG = document.getElementById('heatmapGroup');
  heatG.innerHTML = '';

  Object.values(nodes).forEach(n => {
    const color = n.safety >= 7 ? '#22d3a5' : n.safety >= 4 ? '#f5a623' : '#ef4444';
    const gradId = `heat-${n.id}`;

    const grad = document.createElementNS('http://www.w3.org/2000/svg', 'radialGradient');
    grad.setAttribute('id', gradId);
    grad.innerHTML = `
      <stop offset="0%" stop-color="${color}" stop-opacity="0.55"/>
      <stop offset="100%" stop-color="${color}" stop-opacity="0"/>
    `;
    defs.appendChild(grad);

    const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
    circle.setAttribute('cx', n.x);
    circle.setAttribute('cy', n.y);
    circle.setAttribute('r', n.type !== 'normal' ? 130 : 100);
    circle.setAttribute('fill', `url(#${gradId})`);
    circle.setAttribute('style', 'mix-blend-mode: screen;');
    heatG.appendChild(circle);
  });
}

function toggleHeatmap() {
  heatmapOn = !heatmapOn;
  const heatG = document.getElementById('heatmapGroup');
  const btn = document.getElementById('heatmapBtn');

  if (heatmapOn) {
    renderHeatmap();
    heatG.style.display = 'block';
    btn.textContent = '🔥 Safety Heatmap: ON';
    btn.style.background = '#2a1f1f';
    btn.style.borderColor = 'var(--warn)';
    btn.style.color = 'var(--warn)';
    showToast('🔥', 'Heatmap ON', 'Green = safe, red = high-risk zones.', true);
  } else {
    heatG.style.display = 'none';
    btn.textContent = '🔥 Safety Heatmap: OFF';
    btn.style.background = '#1e1f35';
    btn.style.borderColor = '#2a2d4a';
    btn.style.color = '#a78bfa';
  }
}

// ══════════════════════════════════════════════
// DATA — mirrors the Java project exactly
// ══════════════════════════════════════════════
const nodes = {
  A: { id:'A', name:'Home Area',           x:90,  y:280, safety:5,  type:'normal',   rating:4.1, baseCrowd:40  },
  B: { id:'B', name:'Market Street',       x:220, y:170, safety:4,  type:'normal',   rating:4.3, baseCrowd:220 },
  C: { id:'C', name:'Well-Lit Boulevard',  x:340, y:280, safety:9,  type:'normal',   rating:4.6, baseCrowd:150 },
  D: { id:'D', name:'Dark Back Alley',     x:210, y:390, safety:2,  type:'normal',   rating:2.4, baseCrowd:15  },
  E: { id:'E', name:'Police Station',      x:430, y:160, safety:10, type:'police',   rating:4.8, baseCrowd:60  },
  F: { id:'F', name:'Metro Station',       x:520, y:280, safety:8,  type:'metro',    rating:4.0, baseCrowd:280 },
  G: { id:'G', name:'General Hospital',   x:430, y:390, safety:9,  type:'hospital', rating:4.6, baseCrowd:130 },
  H: { id:'H', name:'Office District',    x:620, y:280, safety:7,  type:'normal',   rating:4.2, baseCrowd:190 },
  I: { id:'I', name:'Lonely Underpass',   x:320, y:430, safety:2,  type:'normal',   rating:2.1, baseCrowd:10  },
  J: { id:'J', name:'Shopping Mall',      x:520, y:430, safety:8,  type:'normal',   rating:4.5, baseCrowd:260 },
};

const edges = [
  { from:'A', to:'B', dist:1.5, light:5, crowd:6  },
  { from:'A', to:'D', dist:1.0, light:2, crowd:2  },
  { from:'A', to:'C', dist:2.0, light:9, crowd:8  },
  { from:'B', to:'E', dist:1.5, light:8, crowd:7  },
  { from:'B', to:'D', dist:0.8, light:3, crowd:3  },
  { from:'C', to:'E', dist:1.2, light:9, crowd:8  },
  { from:'C', to:'F', dist:1.5, light:8, crowd:9  },
  { from:'D', to:'I', dist:0.5, light:1, crowd:1  },
  { from:'E', to:'F', dist:1.0, light:9, crowd:8  },
  { from:'F', to:'H', dist:1.0, light:8, crowd:9  },
  { from:'F', to:'J', dist:1.2, light:8, crowd:9  },
  { from:'G', to:'J', dist:0.8, light:7, crowd:6  },
  { from:'H', to:'J', dist:0.5, light:7, crowd:8  },
  { from:'I', to:'G', dist:1.0, light:4, crowd:3  },
];

function safetyWeight(edge) {
  const dest = nodes[edge.to];
  const avg = (dest.safety + edge.light + edge.crowd) / 3;
  return edge.dist * (11 - avg);
}

function edgeSafetyClass(edge) {
  const avg = (nodes[edge.to].safety + edge.light + edge.crowd) / 3;
  if (avg >= 7) return 'safe';
  if (avg >= 4) return 'moderate';
  return 'unsafe';
}

function nodeColor(node) {
  if (node.type === 'police')   return '#60a5fa';
  if (node.type === 'hospital') return '#f472b6';
  if (node.type === 'metro')    return '#a78bfa';
  if (node.safety >= 7) return '#22d3a5';
  if (node.safety >= 4) return '#f5a623';
  return '#ef4444';
}

// ══════════════════════════════════════════════
// RENDER MAP
// ══════════════════════════════════════════════
function renderMap() {
  const edgesG = document.getElementById('edgesGroup');
  const nodesG = document.getElementById('nodesGroup');

  // Draw edges
  edges.forEach(e => {
    const n1 = nodes[e.from], n2 = nodes[e.to];
    const cls = edgeSafetyClass(e);
    const colors = { safe:'#22d3a5', moderate:'#f5a623', unsafe:'#ef4444' };
    // Forward
    const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line.setAttribute('x1', n1.x); line.setAttribute('y1', n1.y);
    line.setAttribute('x2', n2.x); line.setAttribute('y2', n2.y);
    line.setAttribute('stroke', colors[cls]);
    line.setAttribute('stroke-width', '2');
    line.setAttribute('stroke-opacity', '0.35');
    line.setAttribute('stroke-dasharray', cls === 'unsafe' ? '5,5' : 'none');
    line.setAttribute('id', `edge-${e.from}-${e.to}`);
    line.setAttribute('class', 'edge-line');
    edgesG.appendChild(line);
    // Reverse
    const line2 = line.cloneNode();
    line2.setAttribute('id', `edge-${e.to}-${e.from}`);
    edgesG.appendChild(line2);
  });

  // Draw nodes
  Object.values(nodes).forEach(n => {
    const g = document.createElementNS('http://www.w3.org/2000/svg','g');
    g.setAttribute('id', `node-${n.id}`);
    g.setAttribute('class','node-circle');
    g.setAttribute('cursor','pointer');
    g.addEventListener('click', () => showPlaceDetails(n.id));

    const circle = document.createElementNS('http://www.w3.org/2000/svg','circle');
    circle.setAttribute('cx', n.x); circle.setAttribute('cy', n.y);
    circle.setAttribute('r', n.type !== 'normal' ? 18 : 14);
    circle.setAttribute('fill', nodeColor(n));
    circle.setAttribute('fill-opacity', '0.2');
    circle.setAttribute('stroke', nodeColor(n));
    circle.setAttribute('stroke-width', '2');
    g.appendChild(circle);

    // Icon
    const icon = document.createElementNS('http://www.w3.org/2000/svg','text');
    icon.setAttribute('x', n.x); icon.setAttribute('y', n.y + 1);
    icon.setAttribute('text-anchor','middle'); icon.setAttribute('dominant-baseline','middle');
    icon.setAttribute('font-size', n.type !== 'normal' ? '14' : '11');
    icon.textContent = n.type === 'police' ? '👮' : n.type === 'hospital' ? '🏥' : n.type === 'metro' ? '🚇' : n.id;
    g.appendChild(icon);

    // Label
   const label = document.createElementNS('http://www.w3.org/2000/svg','text');
label.setAttribute('x', n.x); label.setAttribute('y', n.y + (n.type !== 'normal' ? 34 : 30));
label.setAttribute('text-anchor','middle');
label.setAttribute('font-size','13');
label.setAttribute('fill','#e8eaf0');
label.setAttribute('font-weight','600');
label.setAttribute('font-family','Manrope,sans-serif');
label.textContent = n.name;
    g.appendChild(label);

    nodesG.appendChild(g);
  });
}

// ══════════════════════════════════════════════
// DIJKSTRA
// ══════════════════════════════════════════════
function dijkstra(source, dest) {
  const dist = {}, prev = {}, visited = new Set();
  Object.keys(nodes).forEach(id => { dist[id] = Infinity; prev[id] = null; });
  dist[source] = 0;

  // Simple priority queue using sorted array (same logic as Java Min-Heap)
  const pq = [{ id: source, cost: 0 }];

  while (pq.length > 0) {
    pq.sort((a, b) => a.cost - b.cost);
    const { id: curr } = pq.shift();
    if (visited.has(curr)) continue;
    visited.add(curr);
    if (curr === dest) break;

    // All edges from curr (both directions)
    edges.filter(e => e.from === curr || e.to === curr).forEach(e => {
      const neighbor = e.from === curr ? e.to : e.from;
      if (visited.has(neighbor)) return;
      // Compute weight using neighbor as destination for safety score
      const edgeCopy = { from: curr, to: neighbor, dist: e.dist, light: e.light, crowd: e.crowd };
      const w = safetyWeight(edgeCopy);
      const newDist = dist[curr] + w;
      if (newDist < dist[neighbor]) {
        dist[neighbor] = newDist;
        prev[neighbor] = curr;
        pq.push({ id: neighbor, cost: newDist });
      }
    });
  }

  // Reconstruct path
  const path = [];
  let cur = dest;
  while (cur) { path.unshift(cur); cur = prev[cur]; }
  if (path[0] !== source) return { path: [], cost: Infinity };
  return { path, cost: dist[dest] };
}

/** Same Dijkstra, but optimizing for raw distance only — ignores safety entirely. Used by "Compare Safe vs Shortest". */
function dijkstraShortest(source, dest) {
  const dist = {}, prev = {}, visited = new Set();
  Object.keys(nodes).forEach(id => { dist[id] = Infinity; prev[id] = null; });
  dist[source] = 0;

  const pq = [{ id: source, cost: 0 }];

  while (pq.length > 0) {
    pq.sort((a, b) => a.cost - b.cost);
    const { id: curr } = pq.shift();
    if (visited.has(curr)) continue;
    visited.add(curr);
    if (curr === dest) break;

    edges.filter(e => e.from === curr || e.to === curr).forEach(e => {
      const neighbor = e.from === curr ? e.to : e.from;
      if (visited.has(neighbor)) return;
      const newDist = dist[curr] + e.dist; // raw distance, no safety weighting
      if (newDist < dist[neighbor]) {
        dist[neighbor] = newDist;
        prev[neighbor] = curr;
        pq.push({ id: neighbor, cost: newDist });
      }
    });
  }

  const path = [];
  let cur = dest;
  while (cur) { path.unshift(cur); cur = prev[cur]; }
  if (path[0] !== source) return { path: [], cost: Infinity };
  return { path, cost: dist[dest] };
}

// ══════════════════════════════════════════════
// BFS — Nearest Safe Place (police / hospital)
// Unweighted shortest path in hops, layer by layer —
// deliberately a different algorithm from Dijkstra above.
// ══════════════════════════════════════════════
function bfsNearestSafePlace(startId) {
  const queue = [startId];
  const visited = new Set([startId]);
  const parent = { [startId]: null };

  while (queue.length) {
    const curr = queue.shift();
    const n = nodes[curr];

    if (curr !== startId && (n.type === 'police' || n.type === 'hospital')) {
      const path = [];
      let cur = curr;
      while (cur !== null) { path.unshift(cur); cur = parent[cur]; }
      return { target: curr, path };
    }

    getNeighborIds(curr).forEach(nb => {
      if (!visited.has(nb)) {
        visited.add(nb);
        parent[nb] = curr;
        queue.push(nb);
      }
    });
  }
  return null;
}

// ══════════════════════════════════════════════
// UI ACTIONS
// ══════════════════════════════════════════════
let currentRoute = [];

/** Raw physical distance along a path (km) — separate metric from the safety-weighted cost */
function pathDistance(path) {
  let total = 0;
  for (let i = 0; i < path.length - 1; i++) {
    const a = path[i], b = path[i + 1];
    const e = edges.find(e => (e.from === a && e.to === b) || (e.from === b && e.to === a));
    if (e) total += e.dist;
  }
  return total;
}

/** All directly-connected neighbor ids for a node (used for "nearby" suggestions) */
function getNeighborIds(id) {
  const result = new Set();
  edges.forEach(e => {
    if (e.from === id) result.add(e.to);
    if (e.to === id) result.add(e.from);
  });
  return [...result];
}

// ══════════════════════════════════════════════
// TRAVEL OPTIONS — Walk / Auto / Bus / Metro
// Each mode has its own fare model and its own safety
// multiplier applied to the route's safety cost (being in a
// vehicle with a driver, or in a crowded public vehicle, changes
// personal exposure risk compared to walking the same road alone).
// ══════════════════════════════════════════════
const TRAVEL_MODES = {
  walk:  { label: 'Walk',            icon: '🚶', baseFare: 0,  perKm: 0,  safetyFactor: 1.00, requiresMetro: false, nightLimited: false,
           note: 'Free — but you\'re fully exposed to the route\'s risk, especially at night.' },
  auto:  { label: 'Auto / Rickshaw', icon: '🛺', baseFare: 25, perKm: 12, safetyFactor: 0.60, requiresMetro: false, nightLimited: false,
           note: 'A driver is present the whole way, which lowers personal exposure risk.' },
  bus:   { label: 'Public Bus',      icon: '🚌', baseFare: 10, perKm: 2,  safetyFactor: 0.75, requiresMetro: false, nightLimited: true,
           note: 'Cheapest option — but most local routes stop running after 10 PM.' },
  metro: { label: 'Metro / Train',   icon: '🚇', baseFare: 15, perKm: 3,  safetyFactor: 0.50, requiresMetro: true,  nightLimited: false,
           note: 'Safest option overall — only usable when the route passes an actual metro station.' },
};

/** Availability depends on whether the route touches a metro stop, and whether Night Mode (after 8 PM) has shut buses down */
function checkModeAvailability(modeKey, path) {
  const mode = TRAVEL_MODES[modeKey];
  if (mode.requiresMetro && !path.some(id => nodes[id].type === 'metro')) {
    return { available: false, reason: 'No metro station on this route' };
  }
  if (mode.nightLimited && nightModeOn) {
    return { available: false, reason: 'Not running — Night Mode is ON (after 8 PM)' };
  }
  return { available: true, reason: null };
}

/** Renders a cost / safety / availability comparison across all travel modes for the current route */
function renderTravelOptions(path, safetyCost, distance) {
  const card = document.getElementById('travelOptionsCard');
  card.style.display = 'block';

  const rowsHtml = Object.entries(TRAVEL_MODES).map(([key, mode]) => {
    const { available, reason } = checkModeAvailability(key, path);
    const fare = mode.baseFare + mode.perKm * distance;
    const adjSafety = safetyCost * mode.safetyFactor;

    return `
      <div class="travel-option-card${available ? '' : ' unavailable'}">
        <div class="travel-option-head">
          <span>${mode.icon} ${mode.label}</span>
          <span class="travel-avail ${available ? 'avail-yes' : 'avail-no'}">${available ? 'Available' : 'Unavailable'}</span>
        </div>
        <div class="metric-row"><span>Est. Fare</span><span class="metric-val" style="color:var(--warn)">${available ? '₹' + Math.round(fare) : '—'}</span></div>
        <div class="metric-row"><span>Adj. Safety Cost</span><span class="metric-val">${available ? adjSafety.toFixed(2) : '—'}</span></div>
        <div class="muted-note">${available ? mode.note : reason}</div>
      </div>`;
  }).join('');

  card.innerHTML = `<div class="panel-title">Travel Options</div>${rowsHtml}`;
}

/** Reset the map, then highlight every edge/node on the given combined path */
function highlightPath(path) {
  document.getElementById('compareOverlayGroup').innerHTML = '';

  document.querySelectorAll('.edge-line').forEach(l => {
    l.setAttribute('stroke-opacity', '0.35');
    l.setAttribute('stroke-width', '2');
  });
  document.querySelectorAll('.node-circle circle').forEach(c => {
    c.setAttribute('fill-opacity', '0.2');
    c.removeAttribute('filter');
  });

  for (let i = 0; i < path.length - 1; i++) {
    const a = path[i], b = path[i + 1];
    const el = document.getElementById(`edge-${a}-${b}`) || document.getElementById(`edge-${b}-${a}`);
    if (el) {
      el.setAttribute('stroke', '#7c6fef');
      el.setAttribute('stroke-width', '5');
      el.setAttribute('stroke-opacity', '1');
      el.setAttribute('stroke-dasharray', 'none');
    }
  }

  path.forEach(id => {
    const circle = document.querySelector(`#node-${id} circle`);
    if (circle) {
      circle.setAttribute('fill-opacity', '0.5');
      circle.setAttribute('filter', 'url(#glowStrong)');
    }
  });
}

function findRoute() {
  const src = document.getElementById('sourceSelect').value;
  const dst = document.getElementById('destSelect').value;

  if (src === dst) { showToast('⚠️', 'Same Location', 'Pick different start and end.', false); return; }

  const { path, cost } = dijkstra(src, dst);

  if (!path.length) {
    showToast('❌', 'No Route', 'No path found between these points.', false);
    return;
  }

  currentRoute = path;
  highlightPath(path);

  const distance = pathDistance(path);
  document.getElementById('routeResultTitle').textContent = '🛡️ Safest Path Found';
  renderRouteSteps(path, cost, distance);
  renderTravelOptions(path, cost, distance);
  logTripHistory(src, dst, path, cost, distance, 'safe');
  showToast('🛡️', 'Safest Route Found!', `${path.length} stops via ${path.join(' → ')}`, true);
}

function renderRouteSteps(path, cost, distance) {
  const result = document.getElementById('routeResult');
  const steps = document.getElementById('routeSteps');
  const scoreEl = document.getElementById('safetyScore');
  const distEl = document.getElementById('totalDistance');

  result.style.display = 'block';
  scoreEl.textContent = cost.toFixed(2);
  distEl.textContent = `${distance.toFixed(1)} km`;
  steps.innerHTML = '';

  path.forEach((id, idx) => {
    const n = nodes[id];
    const unsafe = n.safety <= 3;
    const div = document.createElement('div');
    div.className = `route-step${unsafe ? ' unsafe' : ''}`;
    div.innerHTML = `
      <div>
        <div class="step-dot" style="${unsafe ? 'background:var(--danger)' : ''}"></div>
        ${idx < path.length - 1 ? '<div class="step-line"></div>' : ''}
      </div>
      <div>
        <div style="font-weight:600;font-size:0.8rem">${n.name}</div>
        <div style="color:var(--muted);font-size:0.7rem;font-family:var(--mono)">Safety: ${n.safety}/10</div>
      </div>`;
    steps.appendChild(div);
  });
}

// ══════════════════════════════════════════════
// MULTI-STOP TRIP PLANNING
// Chains Start → each Trip Plan stop (in the order added) → End,
// running the safe-route Dijkstra between every consecutive pair.
// Each leg reports its OWN safety score and its OWN distance cost —
// they are different metrics and can disagree on which "cost" is lower.
// ══════════════════════════════════════════════
function planVisit() {
  const src = document.getElementById('sourceSelect').value;
  const dst = document.getElementById('destSelect').value;
  const middle = travelPlan.filter(id => id !== src && id !== dst);
  const sequence = [src, ...middle, dst];

  if (sequence.length < 2) { showToast('⚠️', 'Nothing to Plan', 'Pick a start and end first.', false); return; }

  const legs = [];
  let combinedPath = [];
  let totalSafety = 0;
  let totalDistance = 0;

  for (let i = 0; i < sequence.length - 1; i++) {
    const a = sequence[i], b = sequence[i + 1];
    if (a === b) continue;
    const { path, cost } = dijkstra(a, b);
    if (!path.length) {
      showToast('❌', 'Broken Trip', `No safe path between ${nodes[a].name} and ${nodes[b].name}.`, false);
      return;
    }
    const distance = pathDistance(path);
    legs.push({ from: a, to: b, path, safety: cost, distance });
    totalSafety += cost;
    totalDistance += distance;

    combinedPath = (combinedPath.length && combinedPath[combinedPath.length - 1] === path[0])
      ? combinedPath.concat(path.slice(1))
      : combinedPath.concat(path);
  }

  currentRoute = combinedPath;
  highlightPath(combinedPath);
  renderItinerary(legs, totalSafety, totalDistance);
  renderTravelOptions(combinedPath, totalSafety, totalDistance);
  suggestNearby(sequence);
  logTripHistory(src, dst, combinedPath, totalSafety, totalDistance, 'trip');

  showToast('🧭', 'Trip Planned!', `${legs.length} leg${legs.length > 1 ? 's' : ''} covering ${middle.length} stop${middle.length !== 1 ? 's' : ''}.`, true);
}

/** Renders a leg-by-leg guide: how to get from each stop to the next, with each leg's own safety score + distance */
function renderItinerary(legs, totalSafety, totalDistance) {
  const result = document.getElementById('routeResult');
  const steps = document.getElementById('routeSteps');
  const scoreEl = document.getElementById('safetyScore');
  const distEl = document.getElementById('totalDistance');

  result.style.display = 'block';
  document.getElementById('routeResultTitle').textContent = '🧭 Trip Itinerary';
  scoreEl.textContent = totalSafety.toFixed(2);
  distEl.textContent = `${totalDistance.toFixed(1)} km`;

  let html = '';
  legs.forEach((leg, li) => {
    html += `<div style="font-weight:800;font-size:0.75rem;color:var(--accent);margin:${li ? '10px' : '0'} 0 6px;">
      Leg ${li + 1}: ${nodes[leg.from].name} → ${nodes[leg.to].name}
    </div>`;

    leg.path.forEach((id, idx) => {
      const n = nodes[id];
      const unsafe = n.safety <= 3;
      html += `
        <div class="route-step${unsafe ? ' unsafe' : ''}">
          <div>
            <div class="step-dot" style="${unsafe ? 'background:var(--danger)' : ''}"></div>
            ${idx < leg.path.length - 1 ? '<div class="step-line"></div>' : ''}
          </div>
          <div>
            <div style="font-weight:600;font-size:0.8rem">${n.name}</div>
            <div style="color:var(--muted);font-size:0.7rem;font-family:var(--mono)">Safety: ${n.safety}/10</div>
          </div>
        </div>`;
    });

    html += `
      <div class="metric-row" style="margin-top:4px;">
        <span>Leg Safety Cost</span><span class="metric-val">${leg.safety.toFixed(2)}</span>
      </div>
      <div class="metric-row">
        <span>Leg Distance</span><span class="metric-val" style="color:var(--warn)">${leg.distance.toFixed(1)} km</span>
      </div>`;
  });

  steps.innerHTML = html;
}

/** Suggests places adjacent to the itinerary that aren't already part of it — an easy detour to add */
function suggestNearby(sequence) {
  const already = new Set([...sequence, ...travelPlan]);
  const candidates = new Map();

  sequence.forEach(id => {
    getNeighborIds(id).forEach(nid => {
      if (!already.has(nid)) candidates.set(nid, nodes[nid]);
    });
  });

  const box = document.getElementById('nearbySuggestions');
  const emptyNote = document.getElementById('nearbySuggestionsEmpty');
  const list = [...candidates.values()].sort((a, b) => b.safety - a.safety).slice(0, 4);

  if (!list.length) {
    box.style.display = 'none';
    emptyNote.style.display = 'block';
    emptyNote.textContent = 'No extra stops near your route.';
    return;
  }

  emptyNote.style.display = 'none';
  box.style.display = 'block';
  box.innerHTML = list.map(n => `
    <div class="plan-item">
      <span>${iconFor(n)} ${n.name} <span style="color:var(--muted);font-family:var(--mono);font-size:0.68rem;">(${n.safety}/10)</span></span>
      <span class="remove-x" style="color:var(--accent)" onclick="quickAddNearby('${n.id}')">+</span>
    </div>
  `).join('');
}

function quickAddNearby(id) {
  addToPlan(id);
  document.getElementById('nearbySuggestions').style.display = 'none';
  document.getElementById('nearbySuggestionsEmpty').style.display = 'block';
  document.getElementById('nearbySuggestionsEmpty').textContent = 'Tap "Plan My Visit" again to route through it.';
}

// ══════════════════════════════════════════════
// COMPARE: Safe Route vs Shortest Route
// Runs both algorithms, draws the safe route as normal,
// and overlays any shortest-route segments it avoided.
// ══════════════════════════════════════════════
function compareRoutes() {
  const src = document.getElementById('sourceSelect').value;
  const dst = document.getElementById('destSelect').value;

  if (src === dst) { showToast('⚠️', 'Same Location', 'Pick different start and end.', false); return; }

  const safe = dijkstra(src, dst);
  const shortest = dijkstraShortest(src, dst);

  if (!safe.path.length || !shortest.path.length) {
    showToast('❌', 'No Route', 'No path found between these points.', false);
    return;
  }

  currentRoute = safe.path;
  highlightPath(safe.path);
  drawComparisonOverlay(shortest.path, safe.path);

  const safeDist = pathDistance(safe.path);
  const shortDist = pathDistance(shortest.path);
  const same = safe.path.join('-') === shortest.path.join('-');

  renderCompareResult(safe, shortest, safeDist, shortDist, same);
  logTripHistory(src, dst, safe.path, safe.cost, safeDist, 'compare');

  showToast('⚖️', same ? 'Same Route' : 'Routes Differ!',
    same ? 'The shortest path is already the safest one here.' : 'The shortest route cuts through less-safe roads — see the dashed orange line.',
    true);
}

/** Draws dashed orange segments for any part of the shortest route NOT already shown by the safe route */
function drawComparisonOverlay(shortestPath, safePath) {
  const group = document.getElementById('compareOverlayGroup');
  group.innerHTML = '';

  const safeEdgeSet = new Set();
  for (let i = 0; i < safePath.length - 1; i++) {
    safeEdgeSet.add(safePath[i] + '-' + safePath[i + 1]);
    safeEdgeSet.add(safePath[i + 1] + '-' + safePath[i]);
  }

  for (let i = 0; i < shortestPath.length - 1; i++) {
    const a = shortestPath[i], b = shortestPath[i + 1];
    if (safeEdgeSet.has(a + '-' + b)) continue;

    const n1 = nodes[a], n2 = nodes[b];
    const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line.setAttribute('x1', n1.x); line.setAttribute('y1', n1.y);
    line.setAttribute('x2', n2.x); line.setAttribute('y2', n2.y);
    line.setAttribute('stroke', '#f5a623');
    line.setAttribute('stroke-width', '4');
    line.setAttribute('stroke-dasharray', '2,7');
    line.setAttribute('stroke-linecap', 'round');
    line.setAttribute('opacity', '0.9');
    group.appendChild(line);
  }
}

function renderCompareResult(safe, shortest, safeDist, shortDist, same) {
  const result = document.getElementById('routeResult');
  const steps = document.getElementById('routeSteps');
  const scoreEl = document.getElementById('safetyScore');
  const distEl = document.getElementById('totalDistance');

  result.style.display = 'block';
  document.getElementById('routeResultTitle').textContent = '⚖️ Safe vs Shortest';
  scoreEl.textContent = safe.cost.toFixed(2);
  distEl.textContent = `${safeDist.toFixed(1)} km`;

  let html = `<div style="font-weight:800;font-size:0.75rem;color:var(--accent);margin-bottom:6px;">🛡️ Safest Route</div>
    <div style="font-size:0.78rem;margin-bottom:6px;">${safe.path.map(id => nodes[id].name).join(' → ')}</div>
    <div class="metric-row"><span>Safety Cost</span><span class="metric-val">${safe.cost.toFixed(2)}</span></div>
    <div class="metric-row"><span>Distance</span><span class="metric-val" style="color:var(--warn)">${safeDist.toFixed(1)} km</span></div>

    <hr class="divider" style="margin:10px 0;">

    <div style="font-weight:800;font-size:0.75rem;color:var(--warn);margin-bottom:6px;">📏 Shortest Route (Distance-Only)</div>
    <div style="font-size:0.78rem;margin-bottom:6px;">${shortest.path.map(id => nodes[id].safety <= 3 ? `${nodes[id].name} ⚠️` : nodes[id].name).join(' → ')}</div>
    <div class="metric-row"><span>Safety Cost</span><span class="metric-val">${shortest.cost.toFixed(2)}</span></div>
    <div class="metric-row"><span>Distance</span><span class="metric-val" style="color:var(--warn)">${shortDist.toFixed(1)} km</span></div>

    <hr class="divider" style="margin:10px 0;">
    ${same
      ? `<div class="muted-note">ℹ️ Both routes are identical here — the shortest path is already the safest.</div>`
      : `<div class="muted-note" style="color:var(--danger)">⚠️ The dashed orange line on the map shows where the shortest route cuts through less-safe roads that our system avoids.</div>`}`;

  steps.innerHTML = html;
}

// ══════════════════════════════════════════════
// BFS ACTION: Nearest Safe Place button
// ══════════════════════════════════════════════
function findNearestSafePlace() {
  const src = document.getElementById('sourceSelect').value;
  const found = bfsNearestSafePlace(src);

  if (!found) {
    showToast('❌', 'No Safe Place Found', 'No police station or hospital reachable from here.', false);
    return;
  }

  currentRoute = found.path;
  highlightPath(found.path);

  const distance = pathDistance(found.path);
  const hops = found.path.length - 1;
  const target = nodes[found.target];

  document.getElementById('routeResultTitle').textContent = '🆘 Nearest Safe Place';
  const result = document.getElementById('routeResult');
  const steps = document.getElementById('routeSteps');
  const scoreEl = document.getElementById('safetyScore');
  const distEl = document.getElementById('totalDistance');

  result.style.display = 'block';
  scoreEl.textContent = `${hops} hop${hops !== 1 ? 's' : ''}`;
  distEl.textContent = `${distance.toFixed(1)} km`;

  steps.innerHTML = found.path.map((id, idx) => {
    const n = nodes[id];
    const unsafe = n.safety <= 3;
    const isTarget = idx === found.path.length - 1;
    return `
      <div class="route-step${unsafe ? ' unsafe' : ''}">
        <div>
          <div class="step-dot" style="${unsafe ? 'background:var(--danger)' : ''}"></div>
          ${idx < found.path.length - 1 ? '<div class="step-line"></div>' : ''}
        </div>
        <div>
          <div style="font-weight:600;font-size:0.8rem">${n.name}${isTarget ? ' ' + iconFor(n) : ''}</div>
          <div style="color:var(--muted);font-size:0.7rem;font-family:var(--mono)">Safety: ${n.safety}/10</div>
        </div>
      </div>`;
  }).join('');

  logTripHistory(src, found.target, found.path, 0, distance, 'bfs');
  showToast('🆘', 'Nearest Safe Place Found!', `${target.name} is ${hops} hop${hops !== 1 ? 's' : ''} away.`, true);
}

// ══════════════════════════════════════════════
// SEARCH HISTORY — LinkedList-style log (add-first / undo = remove-first)
// Separate from Trip Plan: this logs every route SEARCH, not a wishlist.
// ══════════════════════════════════════════════
let searchHistory = [];

function logTripHistory(fromId, toId, path, cost, distance, type) {
  searchHistory.unshift({
    from: fromId, to: toId,
    fromName: nodes[fromId].name, toName: nodes[toId].name,
    path, cost, distance, type,
    nightMode: nightModeOn,
    timestamp: new Date().toLocaleString('en-IN', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }),
  });
  renderHistory();
}

function undoLastHistory() {
  if (!searchHistory.length) { showToast('⚠️', 'No History', 'No searches to undo yet.', false); return; }
  const removed = searchHistory.shift();
  renderHistory();
  showToast('↩️', 'Removed From History', `${removed.fromName} → ${removed.toName} removed.`, true);
}

function iconForType(type) {
  return type === 'compare' ? '⚖️' : type === 'trip' ? '🧭' : type === 'bfs' ? '🆘' : '🛡️';
}

function renderHistory() {
  const list = document.getElementById('tripHistoryList');
  if (!searchHistory.length) {
    list.innerHTML = '<div class="muted-note">No searches yet — find a route to start your history.</div>';
    return;
  }
  list.innerHTML = searchHistory.slice(0, 8).map(e => {
    const metricLabel = e.type === 'bfs' ? 'Hops' : 'Safety Cost';
    const metricVal = e.type === 'bfs' ? (e.path.length - 1) : e.cost.toFixed(2);
    return `
      <div class="history-item">
        <div class="history-item-head">
          <span>${iconForType(e.type)} ${e.fromName} → ${e.toName}</span>
          <span class="history-time">${e.timestamp}</span>
        </div>
        <div class="metric-row"><span>${metricLabel}</span><span class="metric-val">${metricVal}</span></div>
        <div class="metric-row"><span>Distance</span><span class="metric-val" style="color:var(--warn)">${e.distance.toFixed(1)} km</span></div>
      </div>`;
  }).join('');
}

let sosActive = false;
function triggerSOS() {
  const btn = document.getElementById('sosBtn');
  sosActive = !sosActive;
  if (sosActive) {
    btn.classList.add('pulsing');
    btn.textContent = '[OK] SOS SENT — Tap to Cancel';
    showToast('🚨', 'SOS Alert Sent!', 'Notifying Mom, Friend Neha, Dad + Police + Hospital', false);
  } else {
    btn.classList.remove('pulsing');
    btn.textContent = '🚨 SOS Emergency Alert';
    showToast('[OK]', 'SOS Cancelled', 'Alert deactivated.', true);
  }
}

function showToast(icon, title, msg, safe) {
  const toast = document.getElementById('toast');
  document.getElementById('toastIcon').textContent = icon;
  document.getElementById('toastTitle').textContent = title;
  document.getElementById('toastMsg').textContent = msg;
  toast.className = `toast${safe ? '' : ' sos'} show`;
  setTimeout(() => { toast.className = `toast${safe ? '' : ' sos'}`; }, 6000);
}

// ══════════════════════════════════════════════
// CITY GUIDE FEATURES (merged from console project)
// Mirrors: WeatherAPI, CrowdSimulator, Trie search, TravelPlan
// ══════════════════════════════════════════════
function iconFor(n) {
  return n.type === 'police' ? '👮' : n.type === 'hospital' ? '🏥' : n.type === 'metro' ? '🚇' : '📍';
}

/** Simulates WeatherAPI.getWeather() + checkDisaster() + live crowd count — now driven by the simulated city clock */
function showPlaceDetails(id) {
  const n = nodes[id];
  if (!n) return;

  currentDetailId = id;

  const hour = parseInt(document.getElementById('timeSlider').value, 10);
  const period = getTimePeriod(hour);

  const weather = period.weatherPool[Math.floor(Math.random() * period.weatherPool.length)];
  const dangerMultiplier = n.safety <= 3 ? 1.6 : 1.0;
  const disasterAlert = Math.random() < (period.disasterBase * dangerMultiplier);
  const jitter = 0.85 + Math.random() * 0.3; // small variance so it's not perfectly robotic
  const crowdCount = Math.max(0, Math.round((n.baseCrowd || 100) * period.crowdMultiplier * jitter));

  const card = document.getElementById('placeDetailsCard');
  card.style.display = 'block';
  card.innerHTML = `
    <div class="panel-title" style="margin-bottom:12px;">Place Details</div>
    <div class="place-detail-header">
      <div class="place-icon" style="background:${nodeColor(n)}33; color:${nodeColor(n)};">${iconFor(n)}</div>
      <div>
        <div class="place-name" style="font-size:0.95rem">${n.name}</div>
        <div class="place-score">${n.type} · ⭐ ${n.rating.toFixed(1)}</div>
      </div>
    </div>
    <div class="metric-row"><span>Safety Score</span><span class="metric-val">${n.safety}/10</span></div>
    <div class="metric-row"><span>Time</span><span class="metric-val" style="color:var(--text)">${formatHour(hour)} · ${period.label}</span></div>
    <div class="metric-row"><span>Weather</span><span class="metric-val" style="color:var(--text)">${weather}</span></div>
    <div class="metric-row">
      <span>Disaster Alert</span>
      <span class="disaster-badge" style="color:${disasterAlert ? 'var(--danger)' : 'var(--safe)'}; border:1px solid ${disasterAlert ? 'var(--danger)' : 'var(--safe)'};">
        ${disasterAlert ? 'ACTIVE ⚠️' : 'CLEAR'}
      </span>
    </div>
    <div class="metric-row"><span>Live Crowd</span><span class="metric-val" style="color:var(--text)">${crowdCount} people</span></div>
    <button class="add-plan-btn" onclick="addToPlan('${n.id}')">+ Add to Trip Plan</button>
  `;

  pulseNode(id);
}

/** Brief highlight pulse when a place is focused via search or click */
function pulseNode(id) {
  const circle = document.querySelector(`#node-${id} circle`);
  if (!circle) return;
  circle.setAttribute('filter', 'url(#glowStrong)');
  setTimeout(() => {
    if (!currentRoute.includes(id)) circle.removeAttribute('filter');
  }, 900);
}

/** Prefix-style autocomplete over place names — mirrors the Trie search feature */
function handleSearchInput() {
  const raw = document.getElementById('searchInput').value.trim().toLowerCase();
  const box = document.getElementById('searchSuggestions');

  if (!raw) { box.style.display = 'none'; box.innerHTML = ''; return; }

  const matches = Object.values(nodes).filter(n =>
    n.name.toLowerCase().includes(raw) || n.type.toLowerCase().includes(raw)
  );

  if (!matches.length) {
    box.innerHTML = '<div class="suggestion-empty">No matching places.</div>';
    box.style.display = 'block';
    return;
  }

  box.innerHTML = matches.map(n => `
    <div class="suggestion-item" onclick="selectSearchResult('${n.id}')">
      <span>${iconFor(n)}</span><span>${n.name}</span>
    </div>
  `).join('');
  box.style.display = 'block';
}

function selectSearchResult(id) {
  const n = nodes[id];
  document.getElementById('searchInput').value = n.name;
  document.getElementById('searchSuggestions').style.display = 'none';
  showPlaceDetails(id);
}

// Close suggestions when clicking elsewhere
document.addEventListener('click', (e) => {
  const wrap = document.querySelector('.search-wrap');
  if (wrap && !wrap.contains(e.target)) {
    document.getElementById('searchSuggestions').style.display = 'none';
  }
});

/** Travel Plan — LinkedList-style ordered stops with Stack-based undo/redo */
let travelPlan = [];
let planUndoStack = [];
let planRedoStack = [];

function addToPlan(id) {
  travelPlan.push(id);
  planUndoStack.push({ type: 'ADD', id });
  planRedoStack = [];
  renderPlan();
  showToast('🧳', 'Added to Trip', `${nodes[id].name} added to your plan.`, true);
}

function removeFromPlan(id) {
  const idx = travelPlan.indexOf(id);
  if (idx === -1) return;
  travelPlan.splice(idx, 1);
  planUndoStack.push({ type: 'REMOVE', id, idx });
  planRedoStack = [];
  renderPlan();
}

function undoPlan() {
  if (!planUndoStack.length) { showToast('↩️', 'Nothing to Undo', 'No trip actions yet.', false); return; }
  const action = planUndoStack.pop();
  planRedoStack.push(action);
  if (action.type === 'ADD') {
    const idx = travelPlan.indexOf(action.id);
    if (idx > -1) travelPlan.splice(idx, 1);
  } else {
    travelPlan.splice(action.idx, 0, action.id);
  }
  renderPlan();
}

function redoPlan() {
  if (!planRedoStack.length) { showToast('↪️', 'Nothing to Redo', 'No undone actions.', false); return; }
  const action = planRedoStack.pop();
  planUndoStack.push(action);
  if (action.type === 'ADD') {
    travelPlan.push(action.id);
  } else {
    const idx = travelPlan.indexOf(action.id);
    if (idx > -1) travelPlan.splice(idx, 1);
  }
  renderPlan();
}

function renderPlan() {
  const list = document.getElementById('travelPlanList');
  if (!travelPlan.length) {
    list.innerHTML = '<div class="muted-note">No stops added yet — search a place and tap "+ Add to Trip Plan".</div>';
    return;
  }
  list.innerHTML = travelPlan.map(id => `
    <div class="plan-item">
      <span>${iconFor(nodes[id])} ${nodes[id].name}</span>
      <span class="remove-x" onclick="removeFromPlan('${id}')">✕</span>
    </div>
  `).join('');
}

// Init
renderMap();
// No auto-run — Safety Score, Distance, and Travel Options stay hidden
// until the user picks locations and clicks "Find Safest Route" or "Plan My Visit".

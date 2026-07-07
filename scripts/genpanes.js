// Generates the hand-curated leaded glass pane models from their pattern textures.
// Run from the repo root:  node scripts/genpanes.js
//
// - Front (south) & back (north) faces UV-sample the pattern texture 1:1 positionally,
//   south straight / north u-flipped so both sides read exactly as authored.
// - Glass pixels become per-region elements (tintindex = region, per LeadedGlassFrame.regionAt);
//   came pixels become untinted elements sampling the same texture.
// - Pane edges (up/down/west/east of the 1px border) use leaded_glass_pane_side
//   (a 2px vertical strip at columns 7-8), rotated 90° on up/down for 1:1 pixels.
// - Multipart patterns (grid, lattice) get a came model + one model per cell for the
//   blockstate, plus a combined inline ITEM model (items can't compose multiparts).
// Self-check: re-rasterizes each generated model and asserts pixel-exact agreement
// with the texture (class + sampled uv position).
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const ASSETS = path.join(__dirname, '..', 'common', 'src', 'main', 'resources', 'assets', 'theleadage');
const TEX_DIR = path.join(ASSETS, 'textures', 'block');
const OUT_DIR = path.join(ASSETS, 'models', 'block');
const ITEM_OUT_DIR = path.join(ASSETS, 'models', 'item');

// ---- minimal PNG decode (8-bit, non-interlaced; palette/gray/rgb/rgba) ----
function decode(file) {
  const buf = fs.readFileSync(file);
  let pos = 8, w, h, bitDepth, colorType, palette = null, trns = null;
  const idat = [];
  while (pos < buf.length) {
    const len = buf.readUInt32BE(pos);
    const type = buf.toString('ascii', pos + 4, pos + 8);
    const data = buf.slice(pos + 8, pos + 8 + len);
    if (type === 'IHDR') { w = data.readUInt32BE(0); h = data.readUInt32BE(4); bitDepth = data[8]; colorType = data[9]; }
    else if (type === 'PLTE') palette = data;
    else if (type === 'tRNS') trns = data;
    else if (type === 'IDAT') idat.push(data);
    pos += 12 + len;
  }
  const raw = zlib.inflateSync(Buffer.concat(idat));
  const channels = { 0: 1, 2: 3, 3: 1, 4: 2, 6: 4 }[colorType];
  const bpp = Math.ceil(bitDepth * channels / 8);
  const stride = Math.ceil(w * bitDepth * channels / 8);
  const img = Buffer.alloc(h * stride);
  let p = 0;
  for (let y = 0; y < h; y++) {
    const filter = raw[p++];
    for (let x = 0; x < stride; x++) {
      const cur = raw[p + x];
      const left = x >= bpp ? img[y * stride + x - bpp] : 0;
      const up = y > 0 ? img[(y - 1) * stride + x] : 0;
      const ul = (y > 0 && x >= bpp) ? img[(y - 1) * stride + x - bpp] : 0;
      let val;
      switch (filter) {
        case 0: val = cur; break;
        case 1: val = cur + left; break;
        case 2: val = cur + up; break;
        case 3: val = cur + ((left + up) >> 1); break;
        case 4: {
          const pp = left + up - ul, pa = Math.abs(pp - left), pb = Math.abs(pp - up), pc = Math.abs(pp - ul);
          val = cur + (pa <= pb && pa <= pc ? left : pb <= pc ? up : ul);
          break;
        }
      }
      img[y * stride + x] = val & 0xff;
    }
    p += stride;
  }
  const px = (x, y) => {
    if (bitDepth === 8) {
      const i = y * stride + x * channels;
      if (colorType === 6) return [img[i], img[i + 1], img[i + 2], img[i + 3]];
      if (colorType === 2) return [img[i], img[i + 1], img[i + 2], 255];
      if (colorType === 3) {
        const idx = img[i];
        return [palette[idx * 3], palette[idx * 3 + 1], palette[idx * 3 + 2], trns && idx < trns.length ? trns[idx] : 255];
      }
      if (colorType === 0) return [img[i], img[i], img[i], 255];
      if (colorType === 4) return [img[i], img[i], img[i], img[i + 1]];
    }
    if (bitDepth === 4 && colorType === 3) {
      const b = img[y * stride + (x >> 1)];
      const idx = x % 2 === 0 ? b >> 4 : b & 0xf;
      return [palette[idx * 3], palette[idx * 3 + 1], palette[idx * 3 + 2], trns && idx < trns.length ? trns[idx] : 255];
    }
    throw new Error('unsupported PNG: depth=' + bitDepth + ' colorType=' + colorType);
  };
  return { w, h, px };
}

// Glass pixels are pure white with partial alpha; everything else opaque = came.
function isGlass(rgba) {
  return rgba[0] === 255 && rgba[1] === 255 && rgba[2] === 255;
}

// ---- region rules, mirroring LeadedGlassFrame.regionAt (u: left->right, v: bottom->top, 0..16) ----
const REGIONS = {
  plain: () => 0,
  split_h: (u) => u < 8 ? 0 : 1,
  split_v: (u, v) => v > 8 ? 0 : 1,
  plus: (u, v) => (v > 8 ? 0 : 2) + (u < 8 ? 0 : 1),
  grid: (u, v) => {
    const col = u < 16 / 3 ? 0 : (u < 32 / 3 ? 1 : 2);
    const row = v > 32 / 3 ? 0 : (v > 16 / 3 ? 1 : 2);
    return row * 3 + col;
  },
  diagonal_a: (u, v) => v > u ? 0 : 1,
  diagonal_b: (u, v) => v > 16 - u ? 0 : 1,
  cross: (u, v) => {
    const aboveSlash = v > u, aboveBackslash = v > 16 - u;
    if (aboveSlash && aboveBackslash) return 0;
    if (!aboveSlash && aboveBackslash) return 1;
    if (!aboveSlash) return 2;
    return 3;
  },
  diamond: (u, v) => {
    if (Math.abs(u - 8) + Math.abs(v - 8) < 8) return 2; // centre rhombus
    return (v > 8 ? 0 : 3) + (u < 8 ? 0 : 1);            // corners TL, TR / BL, BR
  },
  bars_h: (u) => u < 16 / 3 ? 0 : (u < 32 / 3 ? 1 : 2),   // left | middle | right
  bars_v: (u, v) => v > 32 / 3 ? 0 : (v > 16 / 3 ? 1 : 2), // top / middle / bottom
  diagonal_bars_a: (u, v) => {                             // "/" cames at v-u = 7, 0, -7
    const d = v - u;
    return d > 7 ? 0 : d > 0 ? 1 : d > -7 ? 2 : 3;         // top-left ... bottom-right
  },
  diagonal_bars_b: (u, v) => {                             // "\" cames at u+v = 9, 16, 23
    const s = u + v;
    return s > 23 ? 0 : s > 16 ? 1 : s > 9 ? 2 : 3;        // top-right ... bottom-left
  },
  lattice: (u, v) => {
    // Diamond lattice: "/" lines at p = u-v in {-7, 0, 7}, "\" lines at q = u+v in {9, 16, 23}
    // (pixel-centre coordinates). Regions match LeadedGlassFrame.LATTICE.
    const p = u - v, q = u + v;
    if (p < -7) return q > 16 ? 0 : 2;   // top-left corner: top / left triangle
    if (p > 7) return q > 16 ? 9 : 11;   // bottom-right: right / bottom triangle
    if (q > 23) return p < 0 ? 1 : 4;    // top-right: top / right triangle
    if (q < 9) return p < 0 ? 7 : 10;    // bottom-left: left / bottom triangle
    if (p < 0) return q > 16 ? 3 : 5;    // rhombus: north / west
    return q > 16 ? 6 : 8;               // rhombus: east / south
  },
};

const GLASS_KEYS = {
  plain: () => 'glass',
  split_h: r => ['glass_left', 'glass_right'][r],
  split_v: r => ['glass_top', 'glass_bottom'][r],
  plus: r => 'glass_' + r,
  grid: () => 'glass',
  diagonal_a: r => 'glass_' + r,
  diagonal_b: r => 'glass_' + r,
  cross: r => 'glass_' + r,
  diamond: r => 'glass_' + r,
  bars_h: r => 'glass_' + r,
  bars_v: r => 'glass_' + r,
  diagonal_bars_a: r => 'glass_' + r,
  diagonal_bars_b: r => 'glass_' + r,
  lattice: () => 'glass',
};

// south face: straight u; north face: u-flipped — both sides then read as authored.
function frontBackFaces(x1, y1, x2, y2, texKey, tint) {
  const south = { uv: [x1, 16 - y2, x2, 16 - y1], texture: '#' + texKey };
  const north = { uv: [x2, 16 - y2, x1, 16 - y1], texture: '#' + texKey };
  if (tint !== null) { north.tintindex = tint; south.tintindex = tint; }
  return { north, south };
}

function el(x1, y1, x2, y2, texKey, tint, extraFaces) {
  return { from: [x1, y1, 7], to: [x2, y2, 9], faces: { ...frontBackFaces(x1, y1, x2, y2, texKey, tint), ...extraFaces } };
}

// The 1px came border, present in every pane model (grid_3 keeps it in the came model).
// Edge faces sample the pane_side strip (columns 7-8); up/down rotate 90° so the strip's
// vertical variation runs along the pane's width at 1:1 pixel density.
function borderElements() {
  const edgeStrip = (v1, v2) => ({ uv: [7, v1, 9, v2], texture: '#edge', rotation: 90 });
  return [
    el(0, 0, 1, 16, 'came', null, {
      west: { uv: [9, 0, 7, 16], texture: '#edge' },
      up: edgeStrip(0, 1), down: edgeStrip(0, 1),
    }),
    el(15, 0, 16, 16, 'came', null, {
      east: { uv: [7, 0, 9, 16], texture: '#edge' },
      up: edgeStrip(15, 16), down: edgeStrip(15, 16),
    }),
    el(1, 0, 15, 1, 'came', null, { down: edgeStrip(1, 15) }),
    el(1, 15, 15, 16, 'came', null, { up: edgeStrip(1, 15) }),
  ];
}

// Decompose the texture interior (1..15) into elements: per-row runs of came / glass-region,
// with identical consecutive rows merged into one band. include: 'all' | 'came' | cellIndex.
function interiorElements(tex, pattern, include) {
  const rows = [];
  for (let v = 1; v <= 14; v++) {
    const run = [];
    for (let x = 1; x <= 14; x++) {
      const glass = isGlass(tex.px(x, v));
      // Region from the pixel center in model space (u right, v up).
      const region = glass ? REGIONS[pattern](x + 0.5, 16 - v - 0.5) : -1;
      const cls = glass ? 'g' + region : 'came';
      if (run.length && run[run.length - 1].cls === cls) run[run.length - 1].x2 = x + 1;
      else run.push({ cls, x1: x, x2: x + 1 });
    }
    rows.push({ v, run });
  }
  const elements = [];
  let i = 0;
  while (i < rows.length) {
    let j = i;
    const sig = JSON.stringify(rows[i].run);
    while (j + 1 < rows.length && JSON.stringify(rows[j + 1].run) === sig) j++;
    const vTop = rows[i].v, vBottom = rows[j].v; // texture rows (top-down)
    const y1 = 15 - vBottom, y2 = 16 - vTop;     // model y (bottom-up)
    for (const r of rows[i].run) {
      const came = r.cls === 'came';
      const region = came ? -1 : Number(r.cls.slice(1));
      if (include === 'came' && !came) continue;
      if (typeof include === 'number' && (came || region !== include)) continue;
      const texKey = came ? 'came' : GLASS_KEYS[pattern](region);
      elements.push(el(r.x1, y1, r.x2, y2, texKey, came ? null : region));
    }
    i = j + 1;
  }
  return elements;
}

function model(patternTexture, textureKeys, elements) {
  const textures = { particle: 'theleadage:block/' + patternTexture };
  for (const key of textureKeys) {
    textures[key] = key === 'edge' ? 'theleadage:block/leaded_glass_pane_side' : 'theleadage:block/' + patternTexture;
  }
  return {
    parent: 'block/block',
    ambientocclusion: false,
    render_type: 'minecraft:translucent',
    textures,
    elements,
  };
}

// ---- self-check: rasterize the model's south faces and compare with the texture ----
function verify(name, tex, m, { border = true, cellsOf = null } = {}) {
  // coverage[x][v] = texKey drawn there (from south-face uv, which must be positional).
  const cover = Array.from({ length: 16 }, () => Array(16).fill(null));
  for (const e of m.elements) {
    const [x1, y1] = e.from, [x2, y2] = e.to;
    const s = e.faces.south;
    const expectUv = [x1, 16 - y2, x2, 16 - y1];
    if (JSON.stringify(s.uv) !== JSON.stringify(expectUv)) throw new Error(`${name}: south uv not positional at ${JSON.stringify(e.from)}: ${JSON.stringify(s.uv)}`);
    const n = e.faces.north;
    if (JSON.stringify(n.uv) !== JSON.stringify([x2, 16 - y2, x1, 16 - y1])) throw new Error(`${name}: north uv not flipped at ${JSON.stringify(e.from)}`);
    for (let x = x1; x < x2; x++) for (let y = y1; y < y2; y++) {
      const v = 15 - y;
      if (cover[x][v]) throw new Error(`${name}: overlap at ${x},${v}`);
      cover[x][v] = s.texture.slice(1) + (s.tintindex !== undefined ? ':' + s.tintindex : '');
    }
  }
  for (let x = 0; x < 16; x++) for (let v = 0; v < 16; v++) {
    const onBorder = x === 0 || x === 15 || v === 0 || v === 15;
    const glass = isGlass(tex.px(x, v));
    const c = cover[x][v];
    if (onBorder) {
      if (border && c !== 'came') throw new Error(`${name}: border pixel ${x},${v} covered by ${c}`);
      if (!border && c !== null) throw new Error(`${name}: unexpected border coverage at ${x},${v}`);
      continue;
    }
    const region = glass ? REGIONS[m._pattern](x + 0.5, 16 - v - 0.5) : -1;
    let expected;
    if (cellsOf !== null) expected = glass && region === cellsOf ? `glass:${region}` : null;
    else if (m._came) expected = glass ? null : 'came';
    else expected = glass ? `${GLASS_KEYS[m._pattern](region)}:${region}` : 'came';
    if (c !== expected) throw new Error(`${name}: pixel ${x},${v} expected ${expected}, got ${c}`);
  }
}

function write(name, m) {
  const clean = { ...m };
  delete clean._pattern; delete clean._came;
  fs.writeFileSync(path.join(OUT_DIR, name + '.json'), JSON.stringify(clean, null, 2) + '\n');
  console.log('wrote', name, '(' + m.elements.length + ' elements)');
}

const FULL_PANES = [
  ['leaded_glass_pane_plain', 'plain', 'white_leaded_glass', ['glass']],
  ['leaded_glass_pane_split_h', 'split_h', 'white_leaded_glass_split_h', ['glass_left', 'glass_right']],
  ['leaded_glass_pane_split_v', 'split_v', 'white_leaded_glass_split_v', ['glass_top', 'glass_bottom']],
  ['leaded_glass_pane_plus', 'plus', 'white_leaded_glass_plus', ['glass_0', 'glass_1', 'glass_2', 'glass_3']],
  ['leaded_glass_pane_cross', 'cross', 'white_leaded_glass_cross', ['glass_0', 'glass_1', 'glass_2', 'glass_3']],
  ['leaded_glass_pane_diagonal_a', 'diagonal_a', 'white_leaded_glass_diagonal_a', ['glass_0', 'glass_1']],
  ['leaded_glass_pane_diagonal_b', 'diagonal_b', 'white_leaded_glass_diagonal_b', ['glass_0', 'glass_1']],
  ['leaded_glass_pane_diamond', 'diamond', 'white_leaded_glass_diamond', ['glass_0', 'glass_1', 'glass_2', 'glass_3', 'glass_4']],
  ['leaded_glass_pane_bars_h', 'bars_h', 'white_leaded_glass_bars_h', ['glass_0', 'glass_1', 'glass_2']],
  ['leaded_glass_pane_bars_v', 'bars_v', 'white_leaded_glass_bars_v', ['glass_0', 'glass_1', 'glass_2']],
  ['leaded_glass_pane_diagonal_bars_a', 'diagonal_bars_a', 'white_leaded_glass_diagonal_bars_a', ['glass_0', 'glass_1', 'glass_2', 'glass_3']],
  ['leaded_glass_pane_diagonal_bars_b', 'diagonal_bars_b', 'white_leaded_glass_diagonal_bars_b', ['glass_0', 'glass_1', 'glass_2', 'glass_3']],
];

for (const [name, pattern, texture, glassKeys] of FULL_PANES) {
  const tex = decode(path.join(TEX_DIR, texture + '.png'));
  const m = model(texture, [...glassKeys, 'came', 'edge'], [...borderElements(), ...interiorElements(tex, pattern, 'all')]);
  m._pattern = pattern;
  verify(name, tex, m);
  write(name, m);
}

// Multipart panes (too many regions for combined models): the came frame (border + lattice)
// and one model per glass cell — the blockstate multipart assembles them per clear_N state.
const MULTIPART_PANES = [
  ['leaded_glass_pane_grid', 'grid', 'white_leaded_glass_grid', 9],
  ['leaded_glass_pane_lattice', 'lattice', 'white_leaded_glass_lattice', 12],
];

// The display transforms shared by every pane item model.
const ITEM_DISPLAY = {
  gui: { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [1, 1, 1] },
  ground: { rotation: [0, 0, 0], translation: [0, 2, 0], scale: [0.5, 0.5, 0.5] },
  head: { rotation: [0, 180, 0], translation: [0, 13, 7], scale: [1, 1, 1] },
  thirdperson_righthand: { rotation: [0, 0, 0], translation: [0, 3, 1], scale: [0.55, 0.55, 0.55] },
  thirdperson_lefthand: { rotation: [0, 0, 0], translation: [0, 3, 1], scale: [0.55, 0.55, 0.55] },
  firstperson_righthand: { rotation: [0, -90, 25], translation: [1.13, 3.2, 1.13], scale: [0.68, 0.68, 0.68] },
  firstperson_lefthand: { rotation: [0, -90, 25], translation: [1.13, 3.2, 1.13], scale: [0.68, 0.68, 0.68] },
  fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [1, 1, 1] },
};

for (const [name, pattern, texture, cells] of MULTIPART_PANES) {
  const tex = decode(path.join(TEX_DIR, texture + '.png'));
  const came = model(texture, ['came', 'edge'], [...borderElements(), ...interiorElements(tex, pattern, 'came')]);
  came._pattern = pattern; came._came = true;
  verify(name + '_came', tex, came);
  write(name + '_came', came);
  for (let cell = 0; cell < cells; cell++) {
    const m = model(texture, ['glass'], interiorElements(tex, pattern, cell));
    m._pattern = pattern;
    verify(name + '_cell_' + cell, tex, m, { border: false, cellsOf: cell });
    write(name + '_cell_' + cell, m);
  }
  // Item model: items can't compose a multipart, so it inlines the full pane (came + all
  // cells, tinted per region) in one model, plus the shared pane display transforms. The
  // all-clear icon (theleadage:clear predicate) is the same model re-textured to the clear
  // pattern texture — the tintable white texture reads wrong for an uncoloured pane.
  const item = model(texture, ['glass', 'came', 'edge'],
      [...borderElements(), ...interiorElements(tex, pattern, 'all')]);
  item._pattern = pattern;
  verify('item/' + name, tex, item);
  item.gui_light = 'front';
  item.display = ITEM_DISPLAY;
  item.overrides = [{ predicate: { 'theleadage:clear': 1 }, model: 'theleadage:item/' + name + '_clear' }];
  const { _pattern, _came, ...clean } = item;
  fs.writeFileSync(path.join(ITEM_OUT_DIR, name + '.json'), JSON.stringify(clean, null, 2) + '\n');
  console.log('wrote item/' + name, '(' + item.elements.length + ' elements)');
  const clearTexture = texture.replace('white_', '');
  const clear = { ...clean };
  delete clear.overrides;
  clear.textures = Object.fromEntries(Object.entries(clean.textures)
      .map(([key, value]) => [key, key === 'edge' ? value : 'theleadage:block/' + clearTexture]));
  fs.writeFileSync(path.join(ITEM_OUT_DIR, name + '_clear.json'), JSON.stringify(clear, null, 2) + '\n');
  console.log('wrote item/' + name + '_clear');
}

// The variant-pattern pane item models: each parents its combined block model and applies the
// shared front-facing display. These CANNOT come from datagen: NeoForge's transforms builder
// silently drops a transform equal to the identity, and the GUI entry must be an explicit
// identity to shadow block/block's isometric view — omitted, the icon renders as an angled
// block instead of the flat 16x16 pane face.
//
// Overrides (an item override list is last-match-wins, so the orientation+clear combo sits
// after its parts): theleadage:clear = 1 when every region is uncoloured → the clear-texture
// icon; split/diagonal/bars additionally pick their second orientation.
const VARIANT_PANE_ITEMS = [
  ['leaded_glass_pane', 'leaded_glass_pane_plain', [
    [{ 'theleadage:clear': 1 }, 'leaded_glass_pane_clear'],
  ]],
  ['leaded_glass_pane_clear', 'leaded_glass_pane_plain_clear', null],
  ['leaded_glass_pane_split', 'leaded_glass_pane_split_h', [
    [{ 'theleadage:clear': 1 }, 'leaded_glass_pane_split_clear'],
    [{ 'theleadage:split_v': 1 }, 'leaded_glass_pane_split_v'],
    [{ 'theleadage:split_v': 1, 'theleadage:clear': 1 }, 'leaded_glass_pane_split_v_clear'],
  ]],
  ['leaded_glass_pane_split_clear', 'leaded_glass_pane_split_h_clear_both', null],
  ['leaded_glass_pane_split_v', 'leaded_glass_pane_split_v', null],
  ['leaded_glass_pane_split_v_clear', 'leaded_glass_pane_split_v_clear_both', null],
  ['leaded_glass_pane_plus', 'leaded_glass_pane_plus', [
    [{ 'theleadage:clear': 1 }, 'leaded_glass_pane_plus_clear'],
  ]],
  ['leaded_glass_pane_plus_clear', 'leaded_glass_pane_plus_clear_0123', null],
  ['leaded_glass_pane_diagonal', 'leaded_glass_pane_diagonal_a', [
    [{ 'theleadage:clear': 1 }, 'leaded_glass_pane_diagonal_clear'],
    [{ 'theleadage:diagonal_b': 1 }, 'leaded_glass_pane_diagonal_b'],
    [{ 'theleadage:diagonal_b': 1, 'theleadage:clear': 1 }, 'leaded_glass_pane_diagonal_b_clear'],
  ]],
  ['leaded_glass_pane_diagonal_clear', 'leaded_glass_pane_diagonal_a_clear_01', null],
  ['leaded_glass_pane_diagonal_b', 'leaded_glass_pane_diagonal_b', null],
  ['leaded_glass_pane_diagonal_b_clear', 'leaded_glass_pane_diagonal_b_clear_01', null],
  ['leaded_glass_pane_cross', 'leaded_glass_pane_cross', [
    [{ 'theleadage:clear': 1 }, 'leaded_glass_pane_cross_clear'],
  ]],
  ['leaded_glass_pane_cross_clear', 'leaded_glass_pane_cross_clear_0123', null],
  ['leaded_glass_pane_diamond', 'leaded_glass_pane_diamond', [
    [{ 'theleadage:clear': 1 }, 'leaded_glass_pane_diamond_clear'],
  ]],
  ['leaded_glass_pane_diamond_clear', 'leaded_glass_pane_diamond_clear_01234', null],
  ['leaded_glass_pane_bars', 'leaded_glass_pane_bars_h', [
    [{ 'theleadage:clear': 1 }, 'leaded_glass_pane_bars_clear'],
    [{ 'theleadage:bars_v': 1 }, 'leaded_glass_pane_bars_v'],
    [{ 'theleadage:bars_v': 1, 'theleadage:clear': 1 }, 'leaded_glass_pane_bars_v_clear'],
  ]],
  ['leaded_glass_pane_bars_clear', 'leaded_glass_pane_bars_h_clear_012', null],
  ['leaded_glass_pane_bars_v', 'leaded_glass_pane_bars_v', null],
  ['leaded_glass_pane_bars_v_clear', 'leaded_glass_pane_bars_v_clear_012', null],
  ['leaded_glass_pane_diagonal_bars', 'leaded_glass_pane_diagonal_bars_a', [
    [{ 'theleadage:clear': 1 }, 'leaded_glass_pane_diagonal_bars_clear'],
    [{ 'theleadage:diagonal_bars_b': 1 }, 'leaded_glass_pane_diagonal_bars_b'],
    [{ 'theleadage:diagonal_bars_b': 1, 'theleadage:clear': 1 }, 'leaded_glass_pane_diagonal_bars_b_clear'],
  ]],
  ['leaded_glass_pane_diagonal_bars_clear', 'leaded_glass_pane_diagonal_bars_a_clear_0123', null],
  ['leaded_glass_pane_diagonal_bars_b', 'leaded_glass_pane_diagonal_bars_b', null],
  ['leaded_glass_pane_diagonal_bars_b_clear', 'leaded_glass_pane_diagonal_bars_b_clear_0123', null],
];

for (const [name, blockModel, overrides] of VARIANT_PANE_ITEMS) {
  const item = {
    parent: 'theleadage:block/' + blockModel,
    gui_light: 'front',
    render_type: 'minecraft:translucent',
    display: ITEM_DISPLAY,
  };
  if (overrides) {
    item.overrides = overrides.map(([predicate, model]) => ({ predicate, model: 'theleadage:item/' + model }));
  }
  fs.writeFileSync(path.join(ITEM_OUT_DIR, name + '.json'), JSON.stringify(item, null, 2) + '\n');
  console.log('wrote item/' + name);
}

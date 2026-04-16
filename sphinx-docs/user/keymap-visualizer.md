---
myst:
  html_meta:
    "keywords": "keymap, keyboard, visualizer, shortcuts, interactive"
---

# Keymap Visualizer

Hover over any shortcut in the list to highlight the keys on the keyboard. Switch between macOS and Windows/Linux layouts, or filter shortcuts by name.

:::{note}
The visualizer runs entirely in your browser. No data is sent anywhere.
:::

```{raw} html
<div class="keymap-container" id="keymap-visualizer">
  <div class="keymap-keyboard">
    <svg viewBox="0 0 780 260" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="QWERTY keyboard layout">
      <!-- Row 1: Function keys (F1..F12) -->
      <g id="row-function">
        <rect x="10" y="10" width="40" height="30" rx="4" data-key="esc"/>
        <text x="30" y="30" text-anchor="middle">esc</text>
        <rect x="60" y="10" width="40" height="30" rx="4" data-key="f1"/>
        <text x="80" y="30" text-anchor="middle">F1</text>
        <rect x="110" y="10" width="40" height="30" rx="4" data-key="f2"/>
        <text x="130" y="30" text-anchor="middle">F2</text>
        <rect x="160" y="10" width="40" height="30" rx="4" data-key="f3"/>
        <text x="180" y="30" text-anchor="middle">F3</text>
        <rect x="210" y="10" width="40" height="30" rx="4" data-key="f4"/>
        <text x="230" y="30" text-anchor="middle">F4</text>
        <rect x="260" y="10" width="40" height="30" rx="4" data-key="f5"/>
        <text x="280" y="30" text-anchor="middle">F5</text>
        <rect x="310" y="10" width="40" height="30" rx="4" data-key="f6"/>
        <text x="330" y="30" text-anchor="middle">F6</text>
        <rect x="360" y="10" width="40" height="30" rx="4" data-key="f7"/>
        <text x="380" y="30" text-anchor="middle">F7</text>
        <rect x="410" y="10" width="40" height="30" rx="4" data-key="f8"/>
        <text x="430" y="30" text-anchor="middle">F8</text>
        <rect x="460" y="10" width="40" height="30" rx="4" data-key="f9"/>
        <text x="480" y="30" text-anchor="middle">F9</text>
        <rect x="510" y="10" width="40" height="30" rx="4" data-key="f10"/>
        <text x="530" y="30" text-anchor="middle">F10</text>
        <rect x="560" y="10" width="40" height="30" rx="4" data-key="f11"/>
        <text x="580" y="30" text-anchor="middle">F11</text>
        <rect x="610" y="10" width="40" height="30" rx="4" data-key="f12"/>
        <text x="630" y="30" text-anchor="middle">F12</text>
      </g>

      <!-- Row 2: Number row -->
      <g id="row-numbers">
        <rect x="10" y="50" width="40" height="40" rx="4" data-key="`"/>
        <text x="30" y="75" text-anchor="middle">`</text>
        <rect x="60" y="50" width="40" height="40" rx="4" data-key="1"/>
        <text x="80" y="75" text-anchor="middle">1</text>
        <rect x="110" y="50" width="40" height="40" rx="4" data-key="2"/>
        <text x="130" y="75" text-anchor="middle">2</text>
        <rect x="160" y="50" width="40" height="40" rx="4" data-key="3"/>
        <text x="180" y="75" text-anchor="middle">3</text>
        <rect x="210" y="50" width="40" height="40" rx="4" data-key="4"/>
        <text x="230" y="75" text-anchor="middle">4</text>
        <rect x="260" y="50" width="40" height="40" rx="4" data-key="5"/>
        <text x="280" y="75" text-anchor="middle">5</text>
        <rect x="310" y="50" width="40" height="40" rx="4" data-key="6"/>
        <text x="330" y="75" text-anchor="middle">6</text>
        <rect x="360" y="50" width="40" height="40" rx="4" data-key="7"/>
        <text x="380" y="75" text-anchor="middle">7</text>
        <rect x="410" y="50" width="40" height="40" rx="4" data-key="8"/>
        <text x="430" y="75" text-anchor="middle">8</text>
        <rect x="460" y="50" width="40" height="40" rx="4" data-key="9"/>
        <text x="480" y="75" text-anchor="middle">9</text>
        <rect x="510" y="50" width="40" height="40" rx="4" data-key="0"/>
        <text x="530" y="75" text-anchor="middle">0</text>
        <rect x="560" y="50" width="40" height="40" rx="4" data-key="-"/>
        <text x="580" y="75" text-anchor="middle">-</text>
        <rect x="610" y="50" width="40" height="40" rx="4" data-key="="/>
        <text x="630" y="75" text-anchor="middle">=</text>
        <rect x="660" y="50" width="70" height="40" rx="4" data-key="backspace"/>
        <text x="695" y="75" text-anchor="middle">⌫</text>
      </g>

      <!-- Row 3: QWERTY -->
      <g id="row-qwerty">
        <rect x="10" y="100" width="55" height="40" rx="4" data-key="tab"/>
        <text x="37" y="125" text-anchor="middle">tab</text>
        <rect x="75" y="100" width="40" height="40" rx="4" data-key="q"/>
        <text x="95" y="125" text-anchor="middle">Q</text>
        <rect x="125" y="100" width="40" height="40" rx="4" data-key="w"/>
        <text x="145" y="125" text-anchor="middle">W</text>
        <rect x="175" y="100" width="40" height="40" rx="4" data-key="e"/>
        <text x="195" y="125" text-anchor="middle">E</text>
        <rect x="225" y="100" width="40" height="40" rx="4" data-key="r"/>
        <text x="245" y="125" text-anchor="middle">R</text>
        <rect x="275" y="100" width="40" height="40" rx="4" data-key="t"/>
        <text x="295" y="125" text-anchor="middle">T</text>
        <rect x="325" y="100" width="40" height="40" rx="4" data-key="y"/>
        <text x="345" y="125" text-anchor="middle">Y</text>
        <rect x="375" y="100" width="40" height="40" rx="4" data-key="u"/>
        <text x="395" y="125" text-anchor="middle">U</text>
        <rect x="425" y="100" width="40" height="40" rx="4" data-key="i"/>
        <text x="445" y="125" text-anchor="middle">I</text>
        <rect x="475" y="100" width="40" height="40" rx="4" data-key="o"/>
        <text x="495" y="125" text-anchor="middle">O</text>
        <rect x="525" y="100" width="40" height="40" rx="4" data-key="p"/>
        <text x="545" y="125" text-anchor="middle">P</text>
        <rect x="575" y="100" width="40" height="40" rx="4" data-key="["/>
        <text x="595" y="125" text-anchor="middle">[</text>
        <rect x="625" y="100" width="40" height="40" rx="4" data-key="]"/>
        <text x="645" y="125" text-anchor="middle">]</text>
        <rect x="675" y="100" width="55" height="40" rx="4" data-key="\"/>
        <text x="702" y="125" text-anchor="middle">\</text>
      </g>

      <!-- Row 4: ASDF -->
      <g id="row-home">
        <rect x="10" y="150" width="70" height="40" rx="4" data-key="capslock"/>
        <text x="45" y="175" text-anchor="middle">caps</text>
        <rect x="90" y="150" width="40" height="40" rx="4" data-key="a"/>
        <text x="110" y="175" text-anchor="middle">A</text>
        <rect x="140" y="150" width="40" height="40" rx="4" data-key="s"/>
        <text x="160" y="175" text-anchor="middle">S</text>
        <rect x="190" y="150" width="40" height="40" rx="4" data-key="d"/>
        <text x="210" y="175" text-anchor="middle">D</text>
        <rect x="240" y="150" width="40" height="40" rx="4" data-key="f"/>
        <text x="260" y="175" text-anchor="middle">F</text>
        <rect x="290" y="150" width="40" height="40" rx="4" data-key="g"/>
        <text x="310" y="175" text-anchor="middle">G</text>
        <rect x="340" y="150" width="40" height="40" rx="4" data-key="h"/>
        <text x="360" y="175" text-anchor="middle">H</text>
        <rect x="390" y="150" width="40" height="40" rx="4" data-key="j"/>
        <text x="410" y="175" text-anchor="middle">J</text>
        <rect x="440" y="150" width="40" height="40" rx="4" data-key="k"/>
        <text x="460" y="175" text-anchor="middle">K</text>
        <rect x="490" y="150" width="40" height="40" rx="4" data-key="l"/>
        <text x="510" y="175" text-anchor="middle">L</text>
        <rect x="540" y="150" width="40" height="40" rx="4" data-key=";"/>
        <text x="560" y="175" text-anchor="middle">;</text>
        <rect x="590" y="150" width="40" height="40" rx="4" data-key="'"/>
        <text x="610" y="175" text-anchor="middle">'</text>
        <rect x="640" y="150" width="90" height="40" rx="4" data-key="enter"/>
        <text x="685" y="175" text-anchor="middle">enter</text>
      </g>

      <!-- Row 5: ZXCV -->
      <g id="row-shift">
        <rect x="10" y="200" width="90" height="40" rx="4" data-key="shift"/>
        <text x="55" y="225" text-anchor="middle">shift</text>
        <rect x="110" y="200" width="40" height="40" rx="4" data-key="z"/>
        <text x="130" y="225" text-anchor="middle">Z</text>
        <rect x="160" y="200" width="40" height="40" rx="4" data-key="x"/>
        <text x="180" y="225" text-anchor="middle">X</text>
        <rect x="210" y="200" width="40" height="40" rx="4" data-key="c"/>
        <text x="230" y="225" text-anchor="middle">C</text>
        <rect x="260" y="200" width="40" height="40" rx="4" data-key="v"/>
        <text x="280" y="225" text-anchor="middle">V</text>
        <rect x="310" y="200" width="40" height="40" rx="4" data-key="b"/>
        <text x="330" y="225" text-anchor="middle">B</text>
        <rect x="360" y="200" width="40" height="40" rx="4" data-key="n"/>
        <text x="380" y="225" text-anchor="middle">N</text>
        <rect x="410" y="200" width="40" height="40" rx="4" data-key="m"/>
        <text x="430" y="225" text-anchor="middle">M</text>
        <rect x="460" y="200" width="40" height="40" rx="4" data-key=","/>
        <text x="480" y="225" text-anchor="middle">,</text>
        <rect x="510" y="200" width="40" height="40" rx="4" data-key="."/>
        <text x="530" y="225" text-anchor="middle">.</text>
        <rect x="560" y="200" width="40" height="40" rx="4" data-key="/"/>
        <text x="580" y="225" text-anchor="middle">/</text>
        <rect x="610" y="200" width="120" height="40" rx="4" data-key="shift"/>
        <text x="670" y="225" text-anchor="middle">shift</text>
      </g>

      <!-- Bottom row: modifiers + space -->
      <g id="row-bottom">
        <rect x="10" y="50" width="0" height="0"/>
        <rect x="100" y="250" width="55" height="40" rx="4" data-key="ctrl" transform="translate(-90,-50)"/>
        <text x="37" y="245" text-anchor="middle">ctrl</text>
        <rect x="70" y="195" width="0" height="0"/>
        <rect x="75" y="245" width="55" height="40" rx="4" data-key="alt"/>
        <text x="102" y="270" text-anchor="middle">alt</text>
        <rect x="140" y="245" width="55" height="40" rx="4" data-key="meta"/>
        <text x="167" y="270" text-anchor="middle">cmd</text>
        <rect x="205" y="245" width="260" height="40" rx="4" data-key="space"/>
        <text x="335" y="270" text-anchor="middle">space</text>
        <rect x="475" y="245" width="55" height="40" rx="4" data-key="meta"/>
        <text x="502" y="270" text-anchor="middle">cmd</text>
        <rect x="540" y="245" width="55" height="40" rx="4" data-key="alt"/>
        <text x="567" y="270" text-anchor="middle">alt</text>
        <rect x="605" y="245" width="55" height="40" rx="4" data-key="ctrl"/>
        <text x="632" y="270" text-anchor="middle">ctrl</text>
        <rect x="670" y="245" width="60" height="40" rx="4" data-key="menu"/>
        <text x="700" y="270" text-anchor="middle">fn</text>
      </g>
    </svg>
  </div>

  <div class="keymap-sidebar">
    <div class="keymap-controls">
      <select id="keymap-platform" aria-label="Platform">
        <option value="mac">macOS</option>
        <option value="pc">Windows / Linux</option>
      </select>
      <input id="keymap-filter" type="search" placeholder="Filter shortcuts..." aria-label="Filter shortcuts"/>
    </div>
    <ul id="keymap-shortcuts" class="keymap-list" role="list"></ul>
  </div>
</div>
```

## How to use

- **Hover** over any shortcut in the list to see which keys it uses.
- **Switch platform** between macOS and Windows/Linux using the dropdown.
- **Filter** by shortcut name, category, or key combination (e.g., type "ctrl+alt" to see all Ctrl+Alt shortcuts).

## See also

- [Keyboard Shortcuts](keyboard-shortcuts.md) — Complete shortcut reference
- [Quick Reference Card](cheatsheet.md) — Printable cheat sheet

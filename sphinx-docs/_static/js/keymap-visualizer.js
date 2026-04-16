(function () {
  "use strict";

  // Shortcut database: each entry is a JetBrains action with its key chord.
  // Platform: "mac" uses Cmd/Option; "pc" uses Ctrl/Alt.
  // Modifier keys are normalized to lowercase single tokens.
  const SHORTCUTS = [
    { name: "Go to Definition", mac: "cmd+b", pc: "ctrl+b", category: "navigation" },
    { name: "Go to Implementation", mac: "cmd+alt+b", pc: "ctrl+alt+b", category: "navigation" },
    { name: "Find Usages", mac: "alt+f7", pc: "alt+f7", category: "navigation" },
    { name: "Go to Symbol", mac: "cmd+alt+o", pc: "ctrl+alt+o", category: "navigation" },
    { name: "Search Everywhere", mac: "shift+shift", pc: "shift+shift", category: "navigation" },
    { name: "Switch .res/.resi", mac: "alt+o", pc: "alt+o", category: "navigation" },
    { name: "Goto Super", mac: "cmd+u", pc: "ctrl+u", category: "navigation" },
    { name: "Go to Test", mac: "cmd+shift+t", pc: "ctrl+shift+t", category: "navigation" },
    { name: "Call Hierarchy", mac: "cmd+alt+h", pc: "ctrl+alt+h", category: "navigation" },
    { name: "External Docs", mac: "shift+f1", pc: "shift+f1", category: "navigation" },
    { name: "Structure View", mac: "cmd+7", pc: "alt+7", category: "navigation" },

    { name: "Format Code", mac: "cmd+alt+l", pc: "ctrl+alt+l", category: "editing" },
    { name: "Intentions / Quick Fix", mac: "alt+enter", pc: "alt+enter", category: "editing" },
    { name: "Surround With", mac: "cmd+alt+t", pc: "ctrl+alt+t", category: "editing" },
    { name: "Toggle Comment", mac: "cmd+/", pc: "ctrl+/", category: "editing" },
    { name: "Unwrap / Remove", mac: "cmd+shift+backspace", pc: "ctrl+shift+backspace", category: "editing" },
    { name: "Move Statement Up", mac: "alt+shift+up", pc: "alt+shift+up", category: "editing" },
    { name: "Move Statement Down", mac: "alt+shift+down", pc: "alt+shift+down", category: "editing" },
    { name: "Smart Enter", mac: "shift+enter", pc: "shift+enter", category: "editing" },
    { name: "Optimize Imports", mac: "cmd+alt+o", pc: "ctrl+alt+o", category: "editing" },

    { name: "Rename", mac: "shift+f6", pc: "shift+f6", category: "refactor" },
    { name: "Extract Variable", mac: "cmd+alt+v", pc: "ctrl+alt+v", category: "refactor" },
    { name: "Extract Function", mac: "cmd+alt+m", pc: "ctrl+alt+m", category: "refactor" },
    { name: "Inline", mac: "cmd+alt+n", pc: "ctrl+alt+n", category: "refactor" },
    { name: "Change Signature", mac: "cmd+f6", pc: "ctrl+f6", category: "refactor" },

    { name: "Trigger Completion", mac: "cmd+space", pc: "ctrl+space", category: "completion" },
    { name: "Parameter Info", mac: "cmd+p", pc: "ctrl+p", category: "completion" },
    { name: "Expression Type", mac: "cmd+shift+p", pc: "ctrl+shift+p", category: "completion" },
    { name: "Generate", mac: "cmd+n", pc: "alt+insert", category: "completion" },

    { name: "Problems Panel", mac: "cmd+6", pc: "alt+6", category: "run" },
    { name: "Run Anything", mac: "ctrl+ctrl", pc: "ctrl+ctrl", category: "run" },
  ];

  const CATEGORY_COLORS = {
    navigation: "#3a7ab8",
    editing: "#4a8c3f",
    refactor: "#b86e3a",
    completion: "#8c6a3f",
    run: "#7a5a8e",
  };

  // Key aliases. When computing which keys to highlight for a shortcut,
  // normalize these synonyms to the canonical id used in the SVG data-key
  // attribute.
  const KEY_ALIAS = {
    cmd: "meta",
    command: "meta",
    ctrl: "ctrl",
    control: "ctrl",
    alt: "alt",
    option: "alt",
    opt: "alt",
    shift: "shift",
    enter: "enter",
    return: "enter",
    backspace: "backspace",
    space: "space",
    up: "up",
    down: "down",
    left: "left",
    right: "right",
  };

  function normalizeKey(token) {
    const lower = token.toLowerCase();
    if (KEY_ALIAS[lower]) return KEY_ALIAS[lower];
    return lower;
  }

  function parseChord(chord) {
    return chord.split("+").map(normalizeKey);
  }

  function detectPlatform() {
    return /Mac|iPhone|iPad/.test(navigator.platform) ? "mac" : "pc";
  }

  function init() {
    const container = document.getElementById("keymap-visualizer");
    if (!container) return;

    const svg = container.querySelector("svg");
    const list = document.getElementById("keymap-shortcuts");
    const platformToggle = document.getElementById("keymap-platform");
    const filterInput = document.getElementById("keymap-filter");

    let platform = detectPlatform();
    if (platformToggle) {
      platformToggle.value = platform;
      platformToggle.addEventListener("change", () => {
        platform = platformToggle.value;
        render();
      });
    }

    let filter = "";
    if (filterInput) {
      filterInput.addEventListener("input", () => {
        filter = filterInput.value.trim().toLowerCase();
        render();
      });
    }

    function render() {
      // Reset key highlights
      svg.querySelectorAll("[data-key]").forEach((el) => {
        el.classList.remove("keymap-active");
        el.removeAttribute("data-fill");
      });

      // Rebuild shortcut list
      list.innerHTML = "";
      const filtered = SHORTCUTS.filter((s) => {
        if (!filter) return true;
        return (
          s.name.toLowerCase().includes(filter) ||
          s.category.toLowerCase().includes(filter) ||
          s[platform].toLowerCase().includes(filter)
        );
      });

      filtered.forEach((shortcut) => {
        const chord = shortcut[platform];
        const keys = parseChord(chord);
        const item = document.createElement("li");
        item.className = "keymap-item";
        item.dataset.category = shortcut.category;
        item.innerHTML = `
          <span class="keymap-name">${shortcut.name}</span>
          <code class="keymap-keys">${chord.replace(/\+/g, " + ")}</code>
        `;
        item.addEventListener("mouseenter", () => highlightKeys(keys, shortcut.category));
        item.addEventListener("mouseleave", () => clearHighlight());
        item.addEventListener("focus", () => highlightKeys(keys, shortcut.category));
        item.addEventListener("blur", () => clearHighlight());
        item.tabIndex = 0;
        list.appendChild(item);
      });

      if (filtered.length === 0) {
        const empty = document.createElement("li");
        empty.className = "keymap-empty";
        empty.textContent = "No shortcuts match your filter.";
        list.appendChild(empty);
      }
    }

    function highlightKeys(keys, category) {
      const color = CATEGORY_COLORS[category] || "#3a7ab8";
      keys.forEach((key) => {
        const el = svg.querySelector(`[data-key="${key}"]`);
        if (el) {
          el.classList.add("keymap-active");
          el.style.setProperty("--keymap-highlight", color);
        }
      });
    }

    function clearHighlight() {
      svg.querySelectorAll("[data-key]").forEach((el) => {
        el.classList.remove("keymap-active");
        el.style.removeProperty("--keymap-highlight");
      });
    }

    render();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();

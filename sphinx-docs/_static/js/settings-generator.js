(function () {
  "use strict";

  // Schema of plugin settings exposed in the UI. Each entry corresponds
  // to a field in RescriptProjectSettings / the LSP initialization options.
  const SETTINGS = [
    {
      id: "lspPath",
      label: "Custom LSP path",
      type: "text",
      placeholder: "node_modules/.bin/rescript-language-server",
      default: "",
      description: "Absolute or project-relative path to a custom language server binary. Leave blank to auto-detect.",
    },
    {
      id: "nodePath",
      label: "Node.js path",
      type: "text",
      placeholder: "node",
      default: "",
      description: "Path to the Node.js executable used to launch the language server. Leave blank to use PATH.",
    },
    {
      id: "codeLens",
      label: "Enable Code Lens",
      type: "boolean",
      default: true,
      description: "Display type annotations above function definitions via the CodeVision API.",
    },
    {
      id: "signatureHelp",
      label: "Enable Signature Help",
      type: "boolean",
      default: true,
      description: "Show parameter information while typing function arguments.",
    },
    {
      id: "inlayHints",
      label: "Enable Inlay Hints",
      type: "boolean",
      default: true,
      description: "Display inferred types next to let bindings and function parameters.",
    },
    {
      id: "compileStatus",
      label: "Show compilation status",
      type: "boolean",
      default: true,
      description: "Receive rescript/compilationStatus notifications for the status bar widget.",
    },
    {
      id: "incrementalTypechecking",
      label: "Incremental type checking",
      type: "boolean",
      default: false,
      description: "Enable the language server's incremental type checking (experimental).",
    },
    {
      id: "cache",
      label: "Enable LSP cache",
      type: "boolean",
      default: true,
      description: "Allow the language server to cache analysis results between sessions.",
    },
    {
      id: "formatCheck",
      label: "Format check inspection",
      type: "boolean",
      default: false,
      description: "Flag unformatted code as an inspection issue with a Quick Fix to format.",
    },
    {
      id: "reanalyzeServerMode",
      label: "Reanalyze server mode",
      type: "boolean",
      default: true,
      description: "Keep a reanalyze daemon running for fast dead-code analysis (requires ReScript ≥ 12.1).",
    },
    {
      id: "errorLens",
      label: "Error Lens inline",
      type: "boolean",
      default: true,
      description: "Render diagnostics inline at the end of the line.",
    },
  ];

  function buildForm(container) {
    const form = document.createElement("form");
    form.className = "settings-form";
    SETTINGS.forEach((setting) => {
      const field = document.createElement("div");
      field.className = "settings-field";

      const label = document.createElement("label");
      label.htmlFor = `setting-${setting.id}`;
      label.textContent = setting.label;

      let input;
      if (setting.type === "boolean") {
        input = document.createElement("input");
        input.type = "checkbox";
        input.checked = setting.default;
        label.prepend(input);
        field.appendChild(label);
      } else {
        field.appendChild(label);
        input = document.createElement("input");
        input.type = "text";
        input.value = setting.default;
        if (setting.placeholder) input.placeholder = setting.placeholder;
        field.appendChild(input);
      }
      input.id = `setting-${setting.id}`;
      input.dataset.settingId = setting.id;
      input.dataset.settingType = setting.type;

      const desc = document.createElement("p");
      desc.className = "settings-description";
      desc.textContent = setting.description;
      field.appendChild(desc);

      form.appendChild(field);
    });
    container.appendChild(form);
    return form;
  }

  function collectValues(form) {
    const result = {};
    SETTINGS.forEach((setting) => {
      const input = form.querySelector(`[data-setting-id="${setting.id}"]`);
      if (!input) return;
      let value;
      if (setting.type === "boolean") {
        value = input.checked;
      } else {
        value = input.value.trim();
        if (!value) return; // Skip empty strings
      }
      result[setting.id] = value;
    });
    return result;
  }

  function updateOutput(outputEl, values) {
    outputEl.value = JSON.stringify(values, null, 2);
  }

  function copyToClipboard(text, statusEl) {
    if (!navigator.clipboard) {
      statusEl.textContent = "Clipboard API unavailable. Please copy manually.";
      return;
    }
    navigator.clipboard
      .writeText(text)
      .then(() => {
        statusEl.textContent = "Copied!";
        setTimeout(() => (statusEl.textContent = ""), 2000);
      })
      .catch(() => {
        statusEl.textContent = "Copy failed. Please copy manually.";
      });
  }

  function downloadJson(text, filename) {
    const blob = new Blob([text], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  function init() {
    const container = document.getElementById("settings-generator");
    if (!container) return;

    const formContainer = container.querySelector(".settings-form-container");
    const output = container.querySelector(".settings-output");
    const copyBtn = container.querySelector(".settings-copy");
    const downloadBtn = container.querySelector(".settings-download");
    const resetBtn = container.querySelector(".settings-reset");
    const statusEl = container.querySelector(".settings-status");

    const form = buildForm(formContainer);

    function refresh() {
      updateOutput(output, collectValues(form));
    }

    form.addEventListener("input", refresh);
    form.addEventListener("change", refresh);

    copyBtn.addEventListener("click", () => copyToClipboard(output.value, statusEl));
    downloadBtn.addEventListener("click", () =>
      downloadJson(output.value, "rescript-plugin-settings.json"),
    );
    resetBtn.addEventListener("click", () => {
      SETTINGS.forEach((setting) => {
        const input = form.querySelector(`[data-setting-id="${setting.id}"]`);
        if (!input) return;
        if (setting.type === "boolean") {
          input.checked = setting.default;
        } else {
          input.value = setting.default;
        }
      });
      refresh();
    });

    refresh();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();

// Tauri main process. Three `#[tauri::command]` functions exposed to the
// renderer:
//   - `greet`               plain RPC, returns String
//   - `get_info`            returns a struct serialised as JSON
//   - `count_with_progress` low-level streaming demo using `tauri::ipc::Channel`
//
// The Builder also registers six `tauri-plugin-*` crates so the renderer
// can drive native dialogs / fs / clipboard / notifications / logging /
// OS info through the matching `@rescript-tauri/plugin-*` bindings.

use std::thread;
use std::time::Duration;

use serde::Serialize;
use tauri::ipc::Channel;
use tauri_plugin_log::{Target, TargetKind};

#[derive(Serialize)]
struct AppInfo {
    name: String,
    version: String,
    platform: String,
    arch: String,
}

#[derive(Clone, Serialize)]
#[serde(tag = "kind", rename_all = "camelCase")]
enum ProgressEvent {
    Started { total: u32 },
    Tick { progress: u32 },
    Finished { total: u32 },
}

#[tauri::command]
fn greet(name: &str) -> String {
    format!("Hello, {}! — from rescript-tauri", name)
}

#[tauri::command]
fn get_info() -> AppInfo {
    AppInfo {
        name: env!("CARGO_PKG_NAME").to_string(),
        version: env!("CARGO_PKG_VERSION").to_string(),
        platform: std::env::consts::OS.to_string(),
        arch: std::env::consts::ARCH.to_string(),
    }
}

// Streams `Tick` events over `on_event` until it has counted to `target`.
// The work runs on a background OS thread so the IPC reply can return
// immediately — `Channel` is the only way the Rust side keeps talking
// to the renderer after the original `invoke` Promise has resolved.
#[tauri::command]
fn count_with_progress(on_event: Channel<ProgressEvent>, target: u32, step_ms: u64) {
    thread::spawn(move || {
        let _ = on_event.send(ProgressEvent::Started { total: target });
        for i in 1..=target {
            thread::sleep(Duration::from_millis(step_ms));
            let _ = on_event.send(ProgressEvent::Tick { progress: i });
        }
        let _ = on_event.send(ProgressEvent::Finished { total: target });
    });
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_os::init())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_clipboard_manager::init())
        .plugin(tauri_plugin_notification::init())
        .plugin(
            tauri_plugin_log::Builder::new()
                .targets([
                    Target::new(TargetKind::Stdout),
                    Target::new(TargetKind::Webview),
                ])
                .build(),
        )
        .invoke_handler(tauri::generate_handler![greet, get_info, count_with_progress])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}

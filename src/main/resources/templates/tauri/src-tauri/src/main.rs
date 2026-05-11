// Tauri main process. Two `#[tauri::command]` functions exposed to the
// renderer: `greet` returns a plain string; `get_info` returns a small
// struct so the ReScript side has something non-trivial to validate
// through `Validation.parseInfo`.

use serde::Serialize;

#[derive(Serialize)]
struct AppInfo {
    name: String,
    version: String,
    platform: String,
    arch: String,
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

fn main() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![greet, get_info])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}

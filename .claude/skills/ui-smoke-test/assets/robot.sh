#!/bin/bash
# Remote-Robot helper for the ui-smoke-test skill.
#
# Wraps the curl + JSON byte-array decoding + JS-execute boilerplate used
# to drive the sandbox IDE launched by `./gradlew runIdeForUiTests`.
#
# Env:
#   ROBOT_PORT  Remote-Robot port (default 8082)
#   OUT         output dir for screenshots / temp scripts (default /tmp/ide_smoke)
#
# Subcommands:
#   robot.sh wait [seconds]        Wait until the robot-server answers (default 600s)
#   robot.sh js <file> [edt]       Run a JS file in the IDE; edt=true|false (default true).
#                                  Prints the server log/message line.
#   robot.sh jsinline "<script>"   Same, but the script is given inline (runs on EDT).
#   robot.sh shot <name>           Full-screen screenshot -> $OUT/<name>.png
#   robot.sh frame <name>          Screenshot of the IdeFrameImpl window only -> $OUT/<name>.png
#
# All JS runs with a `log` object available; use `log.info(...)` to surface
# values back to the caller (printed from the server response).
set -euo pipefail

PORT="${ROBOT_PORT:-8082}"
OUT="${OUT:-/tmp/ide_smoke}"
BASE="http://127.0.0.1:${PORT}"
mkdir -p "$OUT"

_post_js() {  # <script-file> <runInEdt>
  local file="$1" edt="$2"
  node -e 'const fs=require("fs");console.log(JSON.stringify({script:fs.readFileSync(process.argv[1],"utf8"),runInEdt:process.argv[2]==="true"}))' \
    "$file" "$edt" > "$OUT/_req.json"
  curl -s -m 30 -X POST "$BASE/js/execute" -H 'Content-Type: application/json' -d @"$OUT/_req.json"
}

case "${1:-}" in
  wait)
    secs="${2:-600}"
    for ((i=0; i<secs; i+=5)); do
      if curl -s -m 2 "$BASE/" -o /dev/null 2>/dev/null; then echo "robot-server UP after ${i}s"; exit 0; fi
      sleep 5
    done
    echo "robot-server did NOT come up within ${secs}s" >&2; exit 1
    ;;

  js)
    _post_js "$2" "${3:-true}" | grep -o '"log[^}]*\|"message[^}]*' || true
    ;;

  jsinline)
    printf '%s' "$2" > "$OUT/_inline.js"
    _post_js "$OUT/_inline.js" "true" | grep -o '"log[^}]*\|"message[^}]*' || true
    ;;

  shot)
    curl -s -m 20 "$BASE/screenshot" -o "$OUT/_shot.json"
    node -e 'const fs=require("fs");const j=JSON.parse(fs.readFileSync(process.argv[1],"utf8"));fs.writeFileSync(process.argv[2],Buffer.from(j.bytes.map(b=>b&0xff)));' \
      "$OUT/_shot.json" "$OUT/$2.png"
    echo "$OUT/$2.png"
    ;;

  frame)
    cat > "$OUT/_frame.js" <<'EOF'
var frame = null;
var ws = java.awt.Window.getWindows();
for (var i = 0; i < ws.length; i++) {
  if (ws[i].isShowing() && ws[i].getClass().getSimpleName().indexOf("IdeFrameImpl") >= 0) frame = ws[i];
}
if (frame == null) { log.info("NO IDE FRAME"); } else {
  var img = new java.awt.image.BufferedImage(frame.getWidth(), frame.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
  frame.paint(img.getGraphics());
  javax.imageio.ImageIO.write(img, "png", new java.io.File("__OUT__/__NAME__.png"));
  log.info("frame saved " + frame.getWidth() + "x" + frame.getHeight());
}
EOF
    sed -i '' "s|__OUT__|$OUT|; s|__NAME__|$2|" "$OUT/_frame.js" 2>/dev/null || sed -i "s|__OUT__|$OUT|; s|__NAME__|$2|" "$OUT/_frame.js"
    _post_js "$OUT/_frame.js" "true" | grep -o '"log[^}]*' || true
    echo "$OUT/$2.png"
    ;;

  *)
    echo "usage: robot.sh {wait [s]|js <file> [edt]|jsinline <script>|shot <name>|frame <name>}" >&2
    exit 2
    ;;
esac

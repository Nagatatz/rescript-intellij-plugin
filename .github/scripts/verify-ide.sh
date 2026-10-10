#!/usr/bin/env bash
# Pass workflow inputs as data, never as shell source or positional Gradle tasks.
set -euo pipefail

version="${VERIFICATION_IDE_VERSION:-}"
product="${VERIFICATION_IDE_TYPE:-IU}"

if [[ -n "$version" && ! "$version" =~ ^[0-9]+(\.[0-9]+){1,3}(-EAP-CANDIDATE)?$ ]]; then
  echo "::error::IDE version must be a numeric release or build" >&2
  exit 2
fi
case "$product" in
  IU|WS|PY|PS) ;;
  *)
    echo "::error::Unsupported verification product" >&2
    exit 2
    ;;
esac

arguments=(verifyPlugin --info -PverificationMaxHeap=2g --no-parallel --max-workers=1)
if [[ -n "$version" ]]; then
  arguments+=("-PverificationIde=$version" "-PverificationIdeType=$product")
fi
exec ./gradlew "${arguments[@]}"

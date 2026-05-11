/* Reason → ReScript Migration Pilot fixture.
 *
 * Open the "ReScript Migration Pilot" tool window. Both this file
 * and its `.rei` interface should appear in the file list. Check
 * them and press "Convert". The rescript CLI should rewrite them to
 * `LegacyReason.res` / `LegacyReason.resi` in place. */

type point = {
  x: int,
  y: int,
};

let origin = {x: 0, y: 0};

let translate = (p, dx, dy) => {x: p.x + dx, y: p.y + dy};

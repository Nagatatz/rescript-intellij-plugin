#!/usr/bin/env python3
"""Summarize measured coverage populations, declared behavior fixtures and PIT XML.

Only observed XML contributes test counts. An API contract, a missing fixture,
and a compiler test excluded from unit coverage are never labeled behavior success.
The script uses the standard library and requires no installs or IDE process.
"""

import argparse
from collections import Counter
import json
import fnmatch
import os
from pathlib import Path
from html.parser import HTMLParser
import re
import xml.etree.ElementTree as ET


def percentage(numerator, denominator):
    """Keep zero populations explicit instead of turning them into 100%."""
    return f"{100 * numerator / denominator:.2f}%" if denominator else "n/a (0 denominator)"


def coverage(path):
    """Read the root LINE counter and actual classes/source files from Kover XML."""
    root = ET.parse(path).getroot()
    counter = root.find("./counter[@type='LINE']")
    if counter is None:
        raise ValueError(f"Missing root LINE counter: {path}")
    covered, missed = (int(counter.attrib[name]) for name in ("covered", "missed"))
    if min(covered, missed) < 0:
        raise ValueError(f"Negative coverage counter: {path}")
    classes = {node.attrib["name"].replace("/", ".") for node in root.findall("./package/class")}
    sources = {(p.attrib["name"], s.attrib["name"]) for p in root.findall("./package")
               for s in p.findall("./sourcefile")}
    return {"covered": covered, "missed": missed, "total": covered + missed,
            "classes": classes, "sources": sources}


def exclusions(path):
    """Reject blanket/duplicate exclusions and retain a reason for each exact class."""
    rows = {}
    for number, line in enumerate(Path(path).read_text().splitlines(), 1):
        if not line or line.startswith("#"):
            continue
        fields = line.split("\t")
        if len(fields) != 3 or not all(fields) or any(c in fields[0] for c in "*?"):
            raise ValueError(f"Invalid class exclusion at {path}:{number}")
        name, category, reason = fields
        if name in rows:
            raise ValueError(f"Duplicate class exclusion: {name}")
        rows[name] = {"category": category, "reason": reason}
    return rows


def case_counts(suite):
    """Count observed test cases; errors/failures and skips do not count as passes."""
    counts = Counter(passed=0, failed=0, skipped=0)
    cases = suite.findall(".//testcase")
    for case in cases:
        if case.find("failure") is not None or case.find("error") is not None:
            counts["failed"] += 1
        elif case.find("skipped") is not None:
            counts["skipped"] += 1
        else:
            counts["passed"] += 1
    expected = {"tests": len(cases), "failures": counts["failed"], "skipped": counts["skipped"]}
    if "tests" in suite.attrib and int(suite.attrib["tests"]) != expected["tests"]:
        raise ValueError("JUnit suite total disagrees with observed testcase entries")
    if "failures" in suite.attrib and "errors" in suite.attrib:
        if int(suite.attrib["failures"]) + int(suite.attrib["errors"]) != counts["failed"]:
            raise ValueError("JUnit failure/error totals disagree with testcase outcomes")
    if "skipped" in suite.attrib and int(suite.attrib["skipped"]) != counts["skipped"]:
        raise ValueError("JUnit skipped total disagrees with testcase outcomes")
    return counts


def fixtures(inventory, result_dirs, repository, results_after=None):
    """Use the explicit evidence inventory, never test-name or reflection heuristics."""
    results = {}
    for task, directory in result_dirs.items():
        for path in Path(directory).glob("TEST-*.xml"):
            if results_after is not None and path.stat().st_mtime < results_after:
                raise ValueError(f"JUnit XML predates this execution boundary: {path}")
            root = ET.parse(path).getroot()
            if root.tag not in {"testsuite", "testsuites"}:
                raise ValueError(f"Unknown JUnit root: {root.tag}")
            suites = [root] if root.tag == "testsuite" else root.findall("testsuite")
            for suite in suites:
                key = task, suite.attrib.get("name", "")
                if key in results:
                    raise ValueError(f"Duplicate JUnit suite: {key}")
                results[key] = case_counts(suite)
    entries = json.loads(Path(inventory).read_text())
    rows, classified = [], set()
    for entry in entries:
        key = entry["task"], entry["class"]
        if key in classified:
            raise ValueError(f"Duplicate fixture inventory entry: {key}")
        classified.add(key)
        present = (Path(repository) / entry["source"]).is_file()
        observed = results.get(key)
        if observed is not None and not present:
            raise ValueError(f"JUnit result without its declared fixture source: {key}")
        status = "observed" if observed is not None else "not run" if present else "not in this stack"
        rows.append({**entry, "status": status, "counts": observed or Counter()})
    unclassified = Counter(passed=0, failed=0, skipped=0)
    for key, counts in results.items():
        if key not in classified:
            unclassified.update(counts)
    return rows, unclassified


def coverage_summary(full_path, managed_path, exclusion_path, inventory, result_dirs, repository, results_after=None):
    """Keep total production and managed populations distinct and validate their relationship."""
    full, managed = coverage(full_path), coverage(managed_path)
    if not managed["classes"] <= full["classes"] or not managed["sources"] <= full["sources"]:
        raise ValueError("Managed report is not a subset of the Full population")
    if managed["covered"] > full["covered"] or managed["missed"] > full["missed"]:
        raise ValueError("Coverage reports do not describe the same unit execution")
    reasons = exclusions(exclusion_path)
    removed = full["classes"] - managed["classes"]
    unknown = {name for name in removed if name.split("$", 1)[0] not in reasons}
    if unknown:
        raise ValueError(f"Unexplained excluded classes: {sorted(unknown)}")
    rows, other = fixtures(inventory, result_dirs, repository, results_after)
    main = Path(repository) / "src/main"
    source_count = sum(1 for suffix in ("*.kt", "*.java") for _ in main.rglob(suffix))
    text = ["## Quality populations — unit execution only", "",
            "Both reports use the same `test` data. CLI/template integration and UI tasks are excluded from instrumentation.",
            "No explicit class/package filters apply to Full; Kover's intrinsic executable-bytecode/source mapping still applies.",
            "", "| Population | Covered / executable lines | Line coverage | JVM classes in XML | Sources in XML |",
            "|---|---:|---:|---:|---:|"]
    for name, data in (("Full (no exclusions)", full), ("Managed (reasoned class exclusions; 87% ratchet)", managed)):
        text.append(f"| {name} | {data['covered']} / {data['total']} | {percentage(data['covered'], data['total'])} | "
                    f"{len(data['classes'])} | {len(data['sources'])} |")
    text += ["", f"Source inventory: {source_count} main Kotlin/Java files (includes the generated lexer when present). "
             "This is a different denominator from executable sources represented in XML.",
             f"Excluded difference: {len(removed)} JVM classes, {full['total'] - managed['total']} executable lines, "
             f"{len(full['sources'] - managed['sources'])} sources absent from Managed.",
             "", "### Explicit behavior evidence", "",
             "Counts are observed JUnit cases, not program counts or semantic guarantees. A helper assertion does not prove compilation.",
             "| Issue / fixture | Evidence kind | Task | Status | Passed | Failed/error | Skipped |",
             "|---|---|---|---|---:|---:|---:|"]
    for row in rows:
        c = row["counts"]
        text.append(f"| {row['issue']} / `{row['class'].split('.')[-1]}` | {row['kind']} | `{row['task']}` | "
                    f"{row['status']} | {c.get('passed', 0)} | {c.get('failed', 0)} | {c.get('skipped', 0)} |")
    text += ["", f"Other/unclassified observed cases: passed {other['passed']}, failed/error {other['failed']}, skipped {other['skipped']}. "
             "These are not automatically labeled behavior tests.", "", "### Excluded classes and reasons", "",
             "Only exact outer names and their `$` nested classes are excluded. New classes are included by default.",
             "", "| Class | Reason category | Rationale |", "|---|---|---|"]
    for name in sorted({name.split("$", 1)[0] for name in removed}):
        row = reasons[name]
        text.append(f"| `{name}` | {row['category']} | {row['reason']} |")
    return "\n".join(text) + "\n"


class PitTable(HTMLParser):
    """Read only PIT's first project-summary table, including its exact ratios."""

    def __init__(self):
        super().__init__()
        self.headers, self.cells = [], []
        self.table, self.finished, self.cell = False, False, None

    def handle_starttag(self, tag, attrs):
        if tag == "table" and not self.finished:
            self.table = True
        if self.table and tag in {"th", "td"}:
            self.cell = (tag, [])

    def handle_data(self, data):
        if self.cell is not None:
            self.cell[1].append(data)

    def handle_endtag(self, tag):
        if self.cell is not None and tag == self.cell[0]:
            values = self.headers if tag == "th" else self.cells
            values.append("".join(self.cell[1]).strip())
            self.cell = None
        if tag == "table" and self.table:
            self.table, self.finished = False, True


def pit_line_coverage(path, class_count, detected, generated, tested):
    """Read line coverage from actual PIT HTML and cross-check its XML populations."""
    table = PitTable()
    table.feed(Path(path).read_text())
    if not table.finished or table.headers != ["Number of Classes", "Line Coverage", "Mutation Coverage", "Test Strength"] or len(table.cells) != 4:
        raise ValueError("Missing/incomplete PIT project summary table")
    if int(table.cells[0]) != class_count:
        raise ValueError("PIT HTML/XML class populations disagree")
    ratios = []
    for cell in table.cells[1:]:
        ratio = re.search(r"(?:^|\s)(\d+)\s*/\s*(\d+)\s*$", cell)
        if ratio is None:
            raise ValueError("Missing PIT HTML coverage denominator")
        covered, total = map(int, ratio.groups())
        if covered > total:
            raise ValueError("Invalid PIT HTML coverage counter")
        ratios.append((covered, total))
    if ratios[1:] != [(detected, generated), (detected, tested)]:
        raise ValueError("PIT HTML/XML mutation populations disagree")
    return ratios[0]


def pit_summary(path, configuration, html_path=None, results_after=None):
    """Report actual mutations without treating uncovered errors or zero mutations as success."""
    config = json.loads(Path(configuration).read_text())
    if results_after is not None:
        for result in (path, html_path):
            if result is not None and Path(result).stat().st_mtime < results_after:
                raise ValueError(f"PIT report predates this execution boundary: {result}")
    root = ET.parse(path).getroot()
    mutations = root.findall("mutation")
    if not mutations:
        raise ValueError("PIT generated zero mutations")
    allowed = {"KILLED", "SURVIVED", "NO_COVERAGE", "TIMED_OUT", "NON_VIABLE", "MEMORY_ERROR", "RUN_ERROR", "EQUIVALENT"}
    counts = Counter(m.attrib["status"] for m in mutations)
    if set(counts) - allowed:
        raise ValueError(f"Unknown PIT statuses: {sorted(set(counts) - allowed)}")
    detected = sum(1 for m in mutations if m.attrib.get("detected") == "true")
    generated = len(mutations)
    tested = generated - counts["NO_COVERAGE"]
    for mutation in mutations:
        expected = mutation.attrib["status"] not in {"SURVIVED", "NO_COVERAGE"}
        if mutation.attrib.get("detected") != str(expected).lower():
            raise ValueError("PIT detected flag contradicts its completed status")
    classes = sorted({m.findtext("mutatedClass") or "<unknown>" for m in mutations})
    if any(not any(fnmatch.fnmatchcase(name, target) for target in config["classes"]) for name in classes):
        raise ValueError("PIT XML contains a class outside the declared targets")
    text = ["## PIT — declared pure JVM target only", "",
            "Configured targets: " + ", ".join(f"`{v}`" for v in config["classes"]),
            "Configured tests: " + ", ".join(f"`{v}`" for v in config["tests"]),
            "IDE/Document/compiler fixtures are not PIT targets; this score does not measure their semantic strength.",
            "Standard JVM bytecode mutators are used; no Kotlin-specific mutation plugin is configured.",
            "", f"Actual mutated classes: {len(classes)}; generated mutations: {len(mutations)}; "
            f"score denominator: {generated}; mutations with coverage: {tested}.",
            f"Detected / generated (PIT mutation score): {detected}/{generated} ({percentage(detected, generated)}).",
            f"Detected / covered mutations (PIT test strength): {detected}/{tested} ({percentage(detected, tested)}).",
            "PIT treats timeouts, non-viable/error outcomes and equivalent mutations as detected. "
            "Status counts below distinguish these from assertion-killed mutations.",
            f"Assertion kill ratio: {counts['KILLED']}/{generated} ({percentage(counts['KILLED'], generated)}).",
            "", "| Status | Count |", "|---|---:|"]
    text += [f"| {status} | {counts[status]} |" for status in sorted(allowed)]
    text += ["", "Actual classes: " + ", ".join(f"`{name}`" for name in classes)]
    if html_path is not None:
        covered, total = pit_line_coverage(html_path, len(classes), detected, generated, tested)
        text += ["", f"PIT line coverage (actual HTML summary): {covered}/{total} ({percentage(covered, total)}). "
                 "This is a separate population, never inferred from mutation counts."]
    else:
        text += ["", "PIT line coverage unavailable: HTML was not supplied; mutation XML cannot establish its denominator."]
    return "\n".join(text) + "\n"


def behavior_summary(inventory, result_dirs, repository, results_after=None):
    """Describe only the observed behavior evidence of this job, without coverage claims."""
    rows, other = fixtures(inventory, result_dirs, repository, results_after)
    text = ["## Operation fixture evidence for this job", "",
            "Missing/not-run/skipped cases are not successes. Counts are JUnit cases, not compiled program counts.",
            "| Issue / fixture | Evidence kind | Task | Status | Passed | Failed/error | Skipped |",
            "|---|---|---|---|---:|---:|---:|"]
    for row in rows:
        c = row["counts"]
        text.append(f"| {row['issue']} / `{row['class'].split('.')[-1]}` | {row['kind']} | `{row['task']}` | "
                    f"{row['status']} | {c.get('passed', 0)} | {c.get('failed', 0)} | {c.get('skipped', 0)} |")
    text += ["", f"Other/unclassified: {dict(other)}; not automatically labeled behavior tests."]
    return "\n".join(text) + "\n"


def emit(text, output):
    """Write a review artifact and optionally append the same content to the CI summary."""
    path = Path(output)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text)
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a") as stream:
            stream.write(text)
    print(text.splitlines()[0])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)
    cov = sub.add_parser("coverage")
    cov.add_argument("--full", required=True)
    cov.add_argument("--managed", required=True)
    cov.add_argument("--exclusions", default="config/quality/coverage-exclusions.tsv")
    cov.add_argument("--inventory", default="config/quality/fixtures.json")
    cov.add_argument("--repository", default=".")
    cov.add_argument("--unit-results", default="build/test-results/test")
    cov.add_argument("--cli-results", default="build/test-results/integrationIdeTest")
    cov.add_argument("--results-after", type=float, default=os.environ.get("QUALITY_RESULTS_AFTER"))
    cov.add_argument("--output", default="build/reports/quality/coverage.md")
    behavior = sub.add_parser("behavior")
    behavior.add_argument("--inventory", default="config/quality/fixtures.json")
    behavior.add_argument("--repository", default=".")
    behavior.add_argument("--unit-results", default="build/test-results/test")
    behavior.add_argument("--cli-results", default="build/test-results/integrationIdeTest")
    behavior.add_argument("--results-after", type=float, default=os.environ.get("QUALITY_RESULTS_AFTER"))
    behavior.add_argument("--output", default="build/reports/quality/behavior.md")
    pit = sub.add_parser("pit")
    pit.add_argument("--xml", default="build/reports/pitest/mutations.xml")
    pit.add_argument("--html", default="build/reports/pitest/index.html")
    pit.add_argument("--results-after", type=float, default=os.environ.get("QUALITY_RESULTS_AFTER"))
    pit.add_argument("--configuration", default="config/quality/pit-targets.json")
    pit.add_argument("--output", default="build/reports/quality/pit.md")
    args = parser.parse_args()
    try:
        if args.command == "coverage":
            text = coverage_summary(args.full, args.managed, args.exclusions, args.inventory,
                                    {"test": args.unit_results, "integrationIdeTest": args.cli_results}, args.repository, args.results_after)
        elif args.command == "behavior":
            text = behavior_summary(args.inventory,
                                    {"test": args.unit_results, "integrationIdeTest": args.cli_results}, args.repository, args.results_after)
        else:
            text = pit_summary(args.xml, args.configuration, args.html, args.results_after)
        emit(text, args.output)
    except (OSError, ValueError, KeyError, ET.ParseError) as error:
        emit(f"## Quality report unavailable\n\n{type(error).__name__}: {error}\n", args.output)
        raise SystemExit(1) from error


if __name__ == "__main__":
    main()

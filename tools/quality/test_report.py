"""Regression cases for differing populations and honest test/mutation evidence."""
import json
import os
import subprocess
import sys
from pathlib import Path
import tempfile
import unittest
import report


class QualityReportTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)

    def write(self, name, text):
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)
        return path

    def xml(self, name, covered, missed, classes):
        nodes = ''.join(f'<class name="example/{c}"/><sourcefile name="{c}.kt"/>' for c in classes)
        return self.write(name, f'<report><package name="example">{nodes}</package>'
                          f'<counter type="LINE" covered="{covered}" missed="{missed}"/></report>')

    def test_different_denominators_are_retained_and_excluded_lines_are_visible(self):
        full = self.xml('full.xml', 60, 40, ['Logic', 'Dialog'])
        managed = self.xml('managed.xml', 60, 10, ['Logic'])
        exclusions = self.write('exclude.tsv', 'example.Dialog\tide-dialog\tDialog lifecycle\n')
        inventory = self.write('fixtures.json', '[]')
        text = report.coverage_summary(full, managed, exclusions, inventory, {}, self.root)
        self.assertIn('60 / 100 | 60.00%', text)
        self.assertIn('60 / 70 | 85.71%', text)
        self.assertIn('30 executable lines', text)
        self.assertIn('Dialog lifecycle', text)

    def test_unexplained_exclusions_and_incompatible_populations_fail(self):
        full = self.xml('full.xml', 1, 10, ['Logic', 'Dialog'])
        managed = self.xml('managed.xml', 1, 1, ['Logic'])
        empty = self.write('exclude.tsv', '')
        inventory = self.write('fixtures.json', '[]')
        with self.assertRaisesRegex(ValueError, 'Unexplained'):
            report.coverage_summary(full, managed, empty, inventory, {}, self.root)
        other = self.xml('other.xml', 1, 1, ['Unknown'])
        with self.assertRaisesRegex(ValueError, 'subset'):
            report.coverage_summary(full, other, empty, inventory, {}, self.root)

    def test_negative_or_missing_counters_fail_and_zero_is_not_perfect_coverage(self):
        with self.assertRaises(ValueError):
            report.coverage(self.xml('negative.xml', -1, 2, []))
        with self.assertRaises(ValueError):
            report.coverage(self.write('missing.xml', '<report/>'))
        self.assertEqual('n/a (0 denominator)', report.percentage(0, 0))

    def test_exclusions_are_exact_and_duplicates_or_blanket_filters_fail(self):
        for contents in ('example.*\tide\tReason\n',
                         'example.A\tide\tReason\nexample.A\tide\tReason\n',
                         'example.A\tide\t\n'):
            with self.assertRaises(ValueError):
                report.exclusions(self.write('excluded.tsv', contents))

    def test_missing_not_run_skipped_failed_and_api_evidence_are_distinct(self):
        entries = []
        for name, kind in [('Missing', 'Document/undo'), ('NotRun', 'CLI'), ('Api', 'API existence')]:
            entries.append(dict(issue='#104', kind=kind, task='test', source=f'{name}.kt', **{'class':name}))
        self.write('NotRun.kt', '// declared but unexecuted')
        self.write('Api.kt', '// reflection contract')
        inventory = self.write('fixtures.json', json.dumps(entries))
        self.write('results/TEST-Api.xml', '<testsuite name="Api"><testcase name="present"/>'
                   '<testcase name="skipped"><skipped/></testcase>'
                   '<testcase name="broken"><error/></testcase></testsuite>')
        self.write('results/TEST-Other.xml', '<testsuite name="Other"><testcase name="unclassified"/></testsuite>')
        rows, other = report.fixtures(inventory, {'test':self.root/'results'}, self.root)
        self.assertEqual(['not in this stack', 'not run', 'observed'], [r['status'] for r in rows])
        self.assertEqual({'passed':1, 'failed':1, 'skipped':1}, dict(rows[2]['counts']))
        self.assertEqual(1, other['passed'])
        self.assertEqual('API existence', rows[2]['kind'])

    def test_stale_result_without_declared_source_is_rejected(self):
        inventory = self.write('fixtures.json', json.dumps([
            dict(issue='#100', kind='CLI', task='test', source='Absent.kt', **{'class':'Absent'})]))
        self.write('results/TEST-Absent.xml', '<testsuite name="Absent"><testcase name="old"/></testsuite>')
        with self.assertRaisesRegex(ValueError, 'without'):
            report.fixtures(inventory, {'test':self.root/'results'}, self.root)

    def test_incomplete_junit_counter_totals_do_not_become_false_passes(self):
        with self.assertRaisesRegex(ValueError, "total"):
            report.case_counts(report.ET.fromstring('<testsuite tests="3"><testcase/></testsuite>'))
        with self.assertRaisesRegex(ValueError, "failure"):
            report.case_counts(report.ET.fromstring('<testsuite failures="1" errors="0"><testcase/></testsuite>'))
        with self.assertRaisesRegex(ValueError, "skipped"):
            report.case_counts(report.ET.fromstring('<testsuite skipped="1"><testcase/></testsuite>'))

    def test_same_source_old_xml_requires_an_execution_boundary_to_reject(self):
        self.write('Present.kt', '// source still exists')
        inventory = self.write('fixtures.json', json.dumps([
            dict(issue='#104', kind='Document', task='test', source='Present.kt', **{'class':'Present'})]))
        xml = self.write('results/TEST-Present.xml', '<testsuite name="Present"><testcase/></testsuite>')
        os.utime(xml, (100, 100))
        rows, _ = report.fixtures(inventory, {'test':self.root/'results'}, self.root)
        self.assertEqual('observed', rows[0]['status'])
        with self.assertRaisesRegex(ValueError, 'execution boundary'):
            report.fixtures(inventory, {'test':self.root/'results'}, self.root, results_after=200)

    def test_missing_and_malformed_coverage_or_pit_xml_cli_fail_without_success_summary(self):
        script = Path(report.__file__).resolve()
        for command in ('coverage', 'pit'):
            for contents in (None, '<broken'):
                xml = self.root/'input.xml'
                xml.unlink(missing_ok=True)
                if contents is not None: xml.write_text(contents)
                output = self.root/'summary.md'
                args = [sys.executable, str(script), command, '--output', str(output)]
                args += ['--full', str(xml), '--managed', str(xml)] if command=='coverage' else ['--xml', str(xml), '--configuration', str(self.targets())]
                env = dict(os.environ); env.pop('GITHUB_STEP_SUMMARY', None)
                result = subprocess.run(args, text=True, capture_output=True, env=env)
                self.assertEqual(1, result.returncode)
                self.assertIn('Quality report unavailable', output.read_text())
                self.assertNotIn('mutation score', output.read_text())
                self.assertNotIn('Full (no exclusions)', output.read_text())

    def pit(self, statuses):
        detected = {'KILLED', 'TIMED_OUT', 'NON_VIABLE', 'MEMORY_ERROR', 'RUN_ERROR', 'EQUIVALENT'}
        return self.write('mutations.xml', '<mutations>'+''.join(
            f'<mutation status="{status}" detected="{str(status in detected).lower()}">'
            '<mutatedClass>example.Logic</mutatedClass></mutation>' for status in statuses)+'</mutations>')

    def targets(self):
        return self.write('pit.json', json.dumps({'classes':['example.Logic*'], 'tests':['example.LogicTest*']}))

    def test_pit_score_denominators_follow_all_generated_and_covered_mutations(self):
        text = report.pit_summary(self.pit(['KILLED', 'SURVIVED', 'NO_COVERAGE', 'TIMED_OUT', 'NON_VIABLE']), self.targets())
        self.assertIn('score): 3/5 (60.00%)', text)
        self.assertIn('strength): 3/4 (75.00%)', text)
        self.assertIn('Assertion kill ratio: 1/5 (20.00%)', text)
        self.assertIn('| NO_COVERAGE | 1 |', text)
        self.assertIn('not PIT targets', text)

    def test_zero_incomplete_unknown_and_out_of_scope_mutations_fail(self):
        for statuses in ([], ['STARTED'], ['UNKNOWN']):
            with self.assertRaises(ValueError):
                report.pit_summary(self.pit(statuses), self.targets())
        bad = self.write('bad.xml', '<mutations><mutation status="KILLED" detected="true">'
                         '<mutatedClass>unconfigured.Class</mutatedClass></mutation></mutations>')
        with self.assertRaisesRegex(ValueError, 'outside'):
            report.pit_summary(bad, self.targets())

    def pit_html(self, line='7/9', mutation='1/2', strength='1/2', classes='1'):
        return self.write('index.html', '<table><tr>' + ''.join(
            f'<th>{h}</th>' for h in ('Number of Classes', 'Line Coverage', 'Mutation Coverage', 'Test Strength'))
            + '</tr><tr>' + f'<td>{classes}</td>' + ''.join(
                f'<td>99% <div class="coverage_legend">{ratio}</div></td>' for ratio in (line, mutation, strength))
            + '</tr></table>')

    def test_pit_line_coverage_uses_html_not_mutation_denominator(self):
        text = report.pit_summary(self.pit(['KILLED', 'SURVIVED']), self.targets(), self.pit_html())
        self.assertIn('actual HTML summary): 7/9 (77.78%)', text)
        self.assertIn('score): 1/2 (50.00%)', text)
        self.assertIn('no Kotlin-specific', text)
        self.assertIn('line coverage unavailable', report.pit_summary(self.pit(['KILLED']), self.targets()))

    def test_missing_incomplete_or_inconsistent_pit_html_fails(self):
        with self.assertRaises(OSError):
            report.pit_summary(self.pit(['KILLED', 'SURVIVED']), self.targets(), self.root/'absent.html')
        with self.assertRaises(ValueError):
            report.pit_line_coverage(self.write('bad.html', '<table><td>1</td>'), 1, 1, 2, 2)
        for values in ({'classes':'2'}, {'line':'10/9'}, {'line':'-1/9'},
                       {'mutation':'2/2'}, {'strength':'1/3'}):
            html = self.pit_html(**values)
            with self.assertRaises(ValueError):
                report.pit_line_coverage(html, 1, 1, 2, 2)

    def test_old_pit_xml_or_html_is_rejected_at_execution_boundary(self):
        for old in ('xml', 'html'):
            xml, html = self.pit(['KILLED', 'SURVIVED']), self.pit_html()
            os.utime(xml if old == 'xml' else html, (100, 100))
            with self.assertRaisesRegex(ValueError, 'execution boundary'):
                report.pit_summary(xml, self.targets(), html, results_after=200)


if __name__ == '__main__':
    unittest.main()

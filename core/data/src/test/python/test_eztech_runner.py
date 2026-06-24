import pathlib
import sys
import unittest


PYTHON_SOURCE = pathlib.Path(__file__).parents[2] / "main" / "python"
sys.path.insert(0, str(PYTHON_SOURCE))

from eztech_runner import run_code


class EzTechRunnerTest(unittest.TestCase):
    def test_captures_stdout_and_stdin(self):
        stdout, stderr, exit_code = run_code(
            'name = input("Name: ")\nprint(f"Hello {name}")',
            "Ada\n",
        )

        self.assertEqual("Name: Hello Ada\n", stdout)
        self.assertEqual("", stderr)
        self.assertEqual(0, exit_code)

    def test_captures_traceback(self):
        stdout, stderr, exit_code = run_code("print(1 / 0)")

        self.assertEqual("", stdout)
        self.assertIn("ZeroDivisionError", stderr)
        self.assertEqual(1, exit_code)

    def test_stops_long_running_python_code(self):
        stdout, stderr, exit_code = run_code(
            "while True:\n    pass",
            timeout_seconds=0.01,
        )

        self.assertEqual("", stdout)
        self.assertIn("Execution exceeded", stderr)
        self.assertEqual(1, exit_code)

    def test_blocks_system_modules(self):
        stdout, stderr, exit_code = run_code("import os\nprint(os.getcwd())")

        self.assertEqual("", stdout)
        self.assertIn("Module 'os' is not available", stderr)
        self.assertEqual(1, exit_code)

    def test_limits_console_output(self):
        stdout, stderr, exit_code = run_code("print('x' * 100_001)")

        self.assertEqual(100_000, len(stdout))
        self.assertIn("OutputLimitExceeded", stderr)
        self.assertEqual(1, exit_code)


if __name__ == "__main__":
    unittest.main()

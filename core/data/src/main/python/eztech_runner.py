import builtins
import contextlib
import io
import linecache
import sys
import time
import traceback
import tracemalloc


SOURCE_NAME = "<eztech>"
MAX_OUTPUT_CHARACTERS = 100_000
MAX_MEMORY_BYTES = 64 * 1024 * 1024
BLOCKED_MODULES = frozenset(
    {
        "builtins",
        "ctypes",
        "importlib",
        "multiprocessing",
        "os",
        "pathlib",
        "shutil",
        "signal",
        "socket",
        "subprocess",
        "sys",
        "threading",
    }
)


class OutputLimitExceeded(RuntimeError):
    pass


class LimitedStringIO(io.StringIO):
    def write(self, value):
        remaining = MAX_OUTPUT_CHARACTERS - self.tell()
        if remaining <= 0:
            raise OutputLimitExceeded(
                f"Output exceeded {MAX_OUTPUT_CHARACTERS:,} characters."
            )
        if len(value) > remaining:
            super().write(value[:remaining])
            raise OutputLimitExceeded(
                f"Output exceeded {MAX_OUTPUT_CHARACTERS:,} characters."
            )
        return super().write(value)


def restricted_import(name, globals=None, locals=None, fromlist=(), level=0):
    root_module = name.partition(".")[0]
    if root_module in BLOCKED_MODULES:
        raise ImportError(f"Module '{root_module}' is not available in EzTech IDE.")
    return builtins.__import__(name, globals, locals, fromlist, level)


def blocked_open(*args, **kwargs):
    del args, kwargs
    raise PermissionError("File access is not available in EzTech IDE.")


def run_code(code, stdin="", timeout_seconds=10.0):
    stdout = LimitedStringIO()
    stderr = LimitedStringIO()
    exit_code = 0
    diagnostic = ""
    previous_stdin = sys.stdin
    previous_trace = sys.gettrace()
    deadline = time.monotonic() + max(float(timeout_seconds), 0.0)

    source_lines = code.splitlines(keepends=True)
    linecache.cache[SOURCE_NAME] = (len(code), None, source_lines, SOURCE_NAME)

    def enforce_timeout(frame, event, arg):
        del frame, arg
        if event == "line" and time.monotonic() >= deadline:
            raise TimeoutError(
                f"Execution exceeded the {timeout_seconds:g} second time limit."
            )
        if event == "line" and tracemalloc.get_traced_memory()[1] >= MAX_MEMORY_BYTES:
            raise MemoryError(
                f"Execution exceeded the {MAX_MEMORY_BYTES // (1024 * 1024)} MB memory limit."
            )
        return enforce_timeout

    safe_builtins = vars(builtins).copy()
    safe_builtins["__import__"] = restricted_import
    safe_builtins["open"] = blocked_open
    namespace = {
        "__name__": "__main__",
        "__file__": SOURCE_NAME,
        "__builtins__": safe_builtins,
    }

    try:
        tracemalloc.start()
        sys.stdin = io.StringIO(stdin)
        with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
            try:
                sys.settrace(enforce_timeout)
                compiled_code = compile(code, SOURCE_NAME, "exec")
                exec(compiled_code, namespace, namespace)
            except SystemExit as exception:
                if exception.code is None:
                    exit_code = 0
                elif isinstance(exception.code, int):
                    exit_code = exception.code
                else:
                    print(exception.code, file=stderr)
                    exit_code = 1
            except OutputLimitExceeded as exception:
                diagnostic = f"\n{type(exception).__name__}: {exception}\n"
                exit_code = 1
            except BaseException:
                try:
                    traceback.print_exc(file=stderr)
                except OutputLimitExceeded as exception:
                    diagnostic = f"\n{type(exception).__name__}: {exception}\n"
                exit_code = 1
    finally:
        sys.settrace(previous_trace)
        sys.stdin = previous_stdin
        linecache.cache.pop(SOURCE_NAME, None)
        tracemalloc.stop()

    return stdout.getvalue(), stderr.getvalue() + diagnostic, exit_code

import os
import sqlite3
from contextlib import contextmanager

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
DB_PATH = os.environ.get('MARKET_DB_PATH', os.path.join(ROOT_DIR, 'market.db'))
SCHEMA_PATH = os.path.join(ROOT_DIR, 'db', 'schema.sql')
SEED_PATH = os.path.join(ROOT_DIR, 'db', 'seed.sql')


def get_connection() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH, check_same_thread=False, isolation_level=None)
    conn.row_factory = sqlite3.Row
    conn.execute('PRAGMA foreign_keys = ON;')
    return conn


def exec_script(conn: sqlite3.Connection, path: str):
    with open(path, 'r', encoding='utf-8') as f:
        conn.executescript(f.read())


def init_db(seed: bool = False):
    conn = get_connection()
    try:
        exec_script(conn, SCHEMA_PATH)
        if seed:
            exec_script(conn, SEED_PATH)
    finally:
        conn.close()


@contextmanager
def transaction():
    conn = get_connection()
    try:
        conn.execute('BEGIN IMMEDIATE;')
        yield conn
        conn.execute('COMMIT;')
    except Exception:
        conn.execute('ROLLBACK;')
        raise
    finally:
        conn.close()

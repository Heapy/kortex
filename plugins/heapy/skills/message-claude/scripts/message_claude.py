#!/usr/bin/env python3
"""List Claude Code sessions or send one plain-text message to an exact target."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import socket
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional


def config_dir(override: Optional[str]) -> Path:
    configured = override or os.environ.get("CLAUDE_CONFIG_DIR") or "~/.claude"
    return Path(configured).expanduser()


def registered_sessions(root: Path) -> List[Dict[str, Any]]:
    sessions: List[Dict[str, Any]] = []
    for registry in sorted((root / "sessions").glob("*.json")):
        try:
            with registry.open() as source:
                candidate = json.load(source)
        except (OSError, json.JSONDecodeError):
            continue
        if isinstance(candidate, dict) and candidate.get("messagingSocketPath"):
            sessions.append(candidate)
    return sessions


def print_sessions(sessions: List[Dict[str, Any]]) -> None:
    print("NAME\tSTATUS\tPID\tCWD")
    for session in sessions:
        print(
            f"{session.get('name', '')}\t{session.get('status', '')}\t"
            f"{session.get('pid', '')}\t{session.get('cwd', '')}"
        )


def select_target(
    sessions: List[Dict[str, Any]], name: str, cwd: Optional[str], pid: Optional[int]
) -> Dict[str, Any]:
    expected_cwd = os.path.realpath(cwd) if cwd else None
    matches = [
        session
        for session in sessions
        if session.get("name") == name
        and (pid is None or session.get("pid") == pid)
        and (
            expected_cwd is None
            or os.path.realpath(str(session.get("cwd", ""))) == expected_cwd
        )
    ]
    if len(matches) != 1:
        details = ", ".join(
            f"pid={session.get('pid')} cwd={session.get('cwd')}" for session in matches
        )
        suffix = f": {details}" if details else ""
        raise ValueError(f"expected one target named {name!r}, found {len(matches)}{suffix}")
    return matches[0]


def read_peer_token(root: Path, target: Dict[str, Any]) -> str:
    pid = target.get("pid")
    socket_path = target.get("messagingSocketPath")
    if not isinstance(pid, int) or not isinstance(socket_path, str):
        raise ValueError("target registry entry has no valid pid or socket path")
    digest = hashlib.sha256(socket_path.encode()).hexdigest()
    key_file = root / "sessions" / f"{pid}.{digest}.key"
    try:
        with key_file.open() as source:
            token = json.load(source)["peerToken"]
    except (OSError, KeyError, TypeError, json.JSONDecodeError) as error:
        raise ValueError(f"peerToken not found for target pid={target.get('pid')}") from error
    if not isinstance(token, str) or not token:
        raise ValueError(f"peerToken is invalid for target pid={target.get('pid')}")
    return token


def send_message(root: Path, target: Dict[str, Any], message: str) -> int:
    if not message:
        raise ValueError("message is empty")
    token = read_peer_token(root, target)
    lines = [
        json.dumps({"type": "auth", "token": token}, separators=(",", ":")),
        json.dumps(
            {"type": "user", "message": {"role": "user", "content": message}},
            ensure_ascii=False,
            separators=(",", ":"),
        ),
    ]
    payload = ("\n".join(lines) + "\n").encode()
    socket_path = str(target["messagingSocketPath"])
    try:
        with socket.socket(socket.AF_UNIX, socket.SOCK_STREAM) as client:
            client.connect(socket_path)
            client.sendall(payload)
            client.shutdown(socket.SHUT_WR)
    except OSError as error:
        detail = error.strerror or str(error)
        raise OSError(error.errno, f"{detail}: {socket_path}") from error
    return len(payload)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--config-dir", help="Claude config directory (default: environment or ~/.claude)"
    )
    parser.add_argument("--list", action="store_true", dest="list_sessions")
    parser.add_argument("--name", help="exact session name")
    parser.add_argument("--cwd", help="target working directory when names repeat")
    parser.add_argument("--pid", type=int, help="target PID when names repeat")
    parser.add_argument("--message", help="message text; omit to read stdin")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    root = config_dir(args.config_dir)
    sessions = registered_sessions(root)
    if args.list_sessions:
        print_sessions(sessions)
        return 0
    if not args.name:
        raise ValueError("--name is required unless --list is used")
    if args.message is None and sys.stdin.isatty():
        raise ValueError("--message is required when stdin is a terminal")
    message = args.message if args.message is not None else sys.stdin.read()
    target = select_target(sessions, args.name, args.cwd, args.pid)
    payload_size = send_message(root, target, message)
    print(f"sent target={target['name']} pid={target['pid']} bytes={payload_size}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError) as error:
        print(f"error: {error}", file=sys.stderr)
        raise SystemExit(1) from error

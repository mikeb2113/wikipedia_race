#!/usr/bin/env bash
set -euo pipefail

BASE_PORT=8080
INCREMENT=1

port=$BASE_PORT

# Function: returns 0 if port is in use, 1 if free
port_in_use() {
    # netstat version (works on many systems)
    netstat -tuln 2>/dev/null | awk '{print $4}' | grep -q ":$1\$"
}

while port_in_use "$port"; do
    port=$((port + INCREMENT))
done

echo "$port"
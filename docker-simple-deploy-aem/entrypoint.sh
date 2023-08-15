#!/bin/sh
set -e
service ssh start
/bin/bash start
tail -f ../logs/error.log
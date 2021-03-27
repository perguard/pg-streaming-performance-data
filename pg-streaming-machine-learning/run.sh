#!/bin/sh
set -x

export SIMPLE_SETTINGS=settings

faust -A pg_streaming_machine_learning.app worker -l INFO

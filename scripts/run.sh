#!/usr/bin/env bash

# Get number of CPUs available to the container
cpus=$(nproc)

optional_jvm_args=""

# If running on AMD EPYC CPUs, use these JVM arguments
cpu_info=$(grep -Eo "AMD EPYC.*$" /proc/cpuinfo)
if [[ -n "$cpu_info" ]]; then
  optional_jvm_args+=" -XX:+UseUnalignedLoadStores"
  optional_jvm_args+=" -XX:+UseXMMForArrayCopy"
  optional_jvm_args+=" -XX:+UseXMMForObjInit"
  optional_jvm_args+=" -XX:+UseFPUForSpilling"
  optional_jvm_args+=" -XX:-UseFastStosb"
fi

# Get the amount of free RAM available to the container
memory=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes)
memory=$((memory / 1024)) # Convert  B to KB
memory=$((memory / 1024)) # Convert KB to MB
memory=$((memory -  500)) # Save some overhead for container/OS
memory=$((memory / 1024)) # Convert MB to GB

# Check minimum memory requirements
if ((memory < 2)); then
  echo "Memory limit ($memory) was set too small. Minimum 2g."
  exit 1
fi

# Set JVM Object alignment
alignment=8
if ((memory >= 128)); then
  alignment=32
elif ((memory >= 64)); then
  alignment=32
  optional_jvm_args+=" -XX:+UseCompressedOops"
elif ((memory < 64)); then
  alignment=16
  optional_jvm_args+=" -XX:+UseCompressedOops"
else
  alignment=8
  optional_jvm_args+=" -XX:+UseCompressedOops"
fi

memory="$memory"g
echo "JVM will use $cpus platform thread(s)"
echo "JVM will use $cpus garbage collector thread(s)"
echo "JVM will use $memory in heap space"
echo "JVM will use $alignment-byte object alignment"

if [ -n "$optional_jvm_args" ]; then
  echo "JVM will use the following optional arguments: $optional_jvm_args"
fi

exec java \
  -server \
  $optional_jvm_args \
  -XX:ObjectAlignmentInBytes=$alignment \
  -Xms$memory \
  -Xmx$memory \
  -XX:ParallelGCThreads="$cpus" \
  --class-path /powerset/powerset-test-1.0-SNAPSHOT.jar \
  com.thetinkeringtypist.powerset.Main
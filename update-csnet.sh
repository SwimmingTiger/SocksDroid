#!/bin/bash
cd "$(dirname "$(realpath "$0")")"
basedir="$PWD"

# 下载 csnet 二进制
download() {
    cd "$basedir/app/src/main/jniLibs/$1"
    rm libcsnet.so 2>/dev/null
    while ! wget -c -O libcsnet.so "https://aite.xyz/product/csnet/client/csnet_client_android_$2"; do
        sleep 1
    done
}

download arm64-v8a   arm64
download armeabi-v7a arm
download x86         386
download x86_64      amd64

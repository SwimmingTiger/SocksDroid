#!/bin/bash
cd "$(dirname "$(realpath "$0")")"
basedir="$PWD/app/src/main/jniLibs"

# 下载 csnet 二进制
download() {
    cd "$basedir/$1"
    rm libcsnet.so 2>/dev/null
    while ! wget -c -O libcsnet.so "https://aite.xyz/product/csnet/client/csnet_client_android_$2"; do
        sleep 1
    done
    md5sum libcsnet.so
}

# 显示md5
md5() {
    md5sum "$basedir/$1/libcsnet.so"
}

if [ "$1" != "md5" ]; then
    download x86         386
    download x86_64      amd64
    download armeabi-v7a arm
    download arm64-v8a   arm64
fi

md5 x86
md5 x86_64
md5 armeabi-v7a
md5 arm64-v8a

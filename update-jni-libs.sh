#!/bin/bash
cd "$(dirname "$(realpath "$0")")"

# 从Android Studio的NDK构建目录复制可执行文件到源代码目录
copy() {
    lib="$1"
    arch="$2"

    src="$PWD/app/build/intermediates/ndkBuild/release/obj/local/$arch/$lib"
    dst="$PWD/app/src/main/jniLibs/$arch/lib$lib.so"

    if ! [ -f "$src" ]; then
        echo "Cannot find $src"
        echo "Please do a release build."
        echo "You can finish this with generate a signed APK with Android Studio."
        exit
    fi

    cp "$src" "$dst"
}

copy pdnsd armeabi-v7a
copy pdnsd arm64-v8a
copy pdnsd x86
copy pdnsd x86_64

copy tun2socks armeabi-v7a
copy tun2socks arm64-v8a
copy tun2socks x86
copy tun2socks x86_64

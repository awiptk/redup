#!/bin/bash
set -e

ANDROID_JAR="${ANDROID_JAR:-$HOME/android-sdk/platforms/android-30/android.jar}"
PKG="com.awiselow.redup"
OUT_DIR="output"
DEX_DIR="dex"
RES_DIR="res"
SRC_DIR="src"

rm -rf $OUT_DIR $DEX_DIR
mkdir -p $OUT_DIR $DEX_DIR

# Resource
aapt2 compile $RES_DIR/layout/activity_main.xml -o $OUT_DIR/layout_flat
aapt2 compile $RES_DIR/drawable/ic_launcher.xml -o $OUT_DIR/drawable_flat
aapt2 compile $RES_DIR/values/strings.xml -o $OUT_DIR/strings_flat
aapt2 compile $RES_DIR/values/themes.xml -o $OUT_DIR/themes_flat

aapt2 link $OUT_DIR/*_flat \
    --manifest AndroidManifest.xml \
    -I $ANDROID_JAR \
    -o $OUT_DIR/app.apk \
    --java $OUT_DIR

# Java
javac -d $DEX_DIR -cp $ANDROID_JAR -sourcepath $SRC_DIR \
    $SRC_DIR/$PKG/MainActivity.java \
    $SRC_DIR/$PKG/DimService.java \
    $SRC_DIR/$PKG/DimTileService.java \
    $OUT_DIR/$PKG/R.java

# DEX
dx --dex --output=$OUT_DIR/classes.dex $DEX_DIR

# Pack
unzip -o $OUT_DIR/app.apk -d $OUT_DIR/unpacked
cp $OUT_DIR/classes.dex $OUT_DIR/unpacked/
cd $OUT_DIR/unpacked && zip -r ../unsigned.apk ./* && cd ../../..

# Align
zipalign 4 $OUT_DIR/unsigned.apk $OUT_DIR/aligned.apk

echo "✅ Build selesai, siap ditandatangani."

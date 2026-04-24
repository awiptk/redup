#!/bin/bash
set -e

# Konfigurasi
ANDROID_JAR="${ANDROID_JAR:-$HOME/android-sdk/platforms/android-30/android.jar}"
PKG="com.awiselow.redup"
OUT_DIR="output"
DEX_DIR="dex"
RES_DIR="res"
SRC_DIR="src"

# Bersihkan
rm -rf $OUT_DIR $DEX_DIR
mkdir -p $OUT_DIR $DEX_DIR

# 1. Kompilasi resource dengan aapt2
aapt2 compile $RES_DIR/layout/activity_main.xml -o $OUT_DIR/layout_flat
aapt2 compile $RES_DIR/drawable/ic_launcher.xml -o $OUT_DIR/drawable_flat
aapt2 compile $RES_DIR/values/strings.xml -o $OUT_DIR/strings_flat
aapt2 compile $RES_DIR/values/themes.xml -o $OUT_DIR/themes_flat

aapt2 link $OUT_DIR/*_flat \
    --manifest AndroidManifest.xml \
    -I $ANDROID_JAR \
    -o $OUT_DIR/app.apk \
    --java $OUT_DIR

# 2. Kompilasi Java
javac -d $DEX_DIR \
    -cp $ANDROID_JAR \
    -sourcepath $SRC_DIR \
    $SRC_DIR/$PKG/MainActivity.java \
    $SRC_DIR/$PKG/DimService.java \
    $SRC_DIR/$PKG/DimTileService.java \
    $OUT_DIR/$PKG/R.java

# 3. Konversi ke DEX
dx --dex --output=$OUT_DIR/classes.dex $DEX_DIR

# 4. Bongkar APK mentah dari aapt2, masukkan dex
unzip -o $OUT_DIR/app.apk -d $OUT_DIR/unpacked
cp $OUT_DIR/classes.dex $OUT_DIR/unpacked/

# 5. Paket ulang APK (unsigned)
cd $OUT_DIR/unpacked
zip -r ../unsigned.apk ./*
cd ../..

# 6. Zipalign (tanpa tanda tangan dulu)
zipalign 4 $OUT_DIR/unsigned.apk $OUT_DIR/aligned.apk

echo "✅ APK siap ditandatangani di GitHub Actions"

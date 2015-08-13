#!/bin/bash

ANDROID_SOURCE_PATH=~/android-4.3_r3.1
grep -P -r "implements (.+, )*Parcelable" --include=*.java $ANDROID_SOURCE_PATH > all_parcelables
grep -P -r "implements (.+, )+Parcelable" --include=*.java $ANDROID_SOURCE_PATH >> all_parcelables
python get_parcelables_packages.py > parcelables_with_pkg

#!/bin/bash

# gets all classes that extends or implements given class
# example usage: ./all_extending_class.sh IGpsStatusListener

ANDROID_SOURCE_PATH=~/android-4.3_r3.1
grep -P -r "(extends|implements) ([^ ]*, )*$1" --include=*.java $ANDROID_SOURCE_PATH

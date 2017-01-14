#!/usr/bin/env bash

GROOVY_LIBS=$(find /opt/groovy/lib -name '*.jar' -printf '%p:')
CLASSES_DIR=$(realpath out/production/VokabelTrainer)

java -classpath out/vokabeltrainer.jar:${CLASSES_DIR}:${GROOVY_LIBS} VokabeltrainerKt

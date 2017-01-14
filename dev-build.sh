#!/usr/bin/env bash

GROOVY_LIBS=$(find /opt/groovy/lib -name '*.jar' -printf '%p:')
CLASSES_DIR=$(realpath out/production/VokabelTrainer)

groovyc -d ${CLASSES_DIR} vokabeltrainer.groovy

kotlinc -include-runtime -classpath ${CLASSES_DIR}:${GROOVY_LIBS} -d out/vokabeltrainer.jar vokabeltrainer.kt

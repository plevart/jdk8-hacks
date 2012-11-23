#!/bin/bash

if [ -f .mavenrc.override ]; then
    echo "picking up .mavenrc.override"
    . .mavenrc.override
fi

if [ "$JAVA_HOME" == "" ]; then
    echo "Please specify JAVA_HOME to point to JDK8 image"
    exit 1
fi

if [ -f "$JAVA_HOME/bin/sparcv9/java" ]; then
    JAVA_BIN="$JAVA_HOME/bin/sparcv9/java"
else
    JAVA_BIN="$JAVA_HOME/bin/java"
fi

if [ "$CP" == "" ]; then
    CP=target/classes
fi

OPTS="-Xmx4G -cp $CP"

echo ""
echo "Executing: $JAVA_BIN $OPTS $*"
echo ""
$JAVA_BIN $OPTS $*

OPTS="-Xbootclasspath/p:$CP $OPTS"

echo ""
echo "Executing: $JAVA_BIN $OPTS $*"
echo ""
$JAVA_BIN $OPTS $*

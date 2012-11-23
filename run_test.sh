#!/bin/bash

if [ -f .mavenrc.override ]; then
    echo "picking up .mavenrc.override"
    . .mavenrc.override
fi

if [ "$JAVA_HOME" == "" ]; then
    echo "Please specify JAVA_HOME to point to JDK8 image"
    exit 1
fi

OPTS="-Xmx4G -cp target/classes"

echo ""
echo "Executing: $JAVA_HOME/bin/java $OPTS $*"
echo ""
$JAVA_HOME/bin/java $OPTS $*

OPTS="-Xbootclasspath/p:target/classes $OPTS"

echo ""
echo "Executing: $JAVA_HOME/bin/java $OPTS $*"
echo ""
$JAVA_HOME/bin/java $OPTS $*

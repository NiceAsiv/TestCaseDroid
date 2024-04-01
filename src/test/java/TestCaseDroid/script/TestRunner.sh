#!/bin/bash

# Define variables
CLASSPATH="/path/to/classes"
TEST_CLASS="my.package.MyTest"
JUNIT_JAR="/path/to/junit-platform-console-standalone.jar"

# Construct the command
COMMAND="java -jar $JUNIT_JAR -cp $CLASSPATH --select-class $TEST_CLASS"

# Run the command and print the output
echo "Running command: $COMMAND"
$COMMAND
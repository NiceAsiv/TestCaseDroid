#!/bin/bash

#check if the number of arguments is valid
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Please enter the project path and the test class name"
    echo "Example: /home/user/Project/src/test/java com.example.TestClass"
    exit 1
fi
PROJECT_PATH=$1
TEST_CLASS_NAME=$2

# Optional test case arguments
TEST_CASE_ARGS=${*:3}

printf "Project path: %s\n" "$PROJECT_PATH"
printf "Test class name: %s\n" "$TEST_CLASS_NAME"

#check if the project path is valid
if [ ! -d "$PROJECT_PATH" ]; then
    echo "Invalid project path"
    exit 1
fi

#check if maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed"
    exit 1
fi

# Run the test case using Maven
if [ -n "$TEST_CASE_ARGS" ]; then
    printf "Test case arguments: %s\n" "$TEST_CASE_ARGS"
    OUTPUT=$(mvn -f "$PROJECT_PATH" test -Dtest="$TEST_CLASS_NAME" -Dexec.args="$TEST_CASE_ARGS")
else
    OUTPUT=$(mvn -f "$PROJECT_PATH" test -Dtest="$TEST_CLASS_NAME")
fi

#check if the output is empty
if [ -z "$OUTPUT" ]; then
    echo "Test case not found"
    exit 1
fi


#Split the output into lines
IFS=$'\n' read -rd '' -a LINES <<< "$OUTPUT"


TOTAL_TESTS=0
FAILED_TESTS=0
ERROR_TESTS=0
SKIPPED_TESTS=0


# Parse the test results line by line
for LINE in "${LINES[@]}"; do
    if [[ $LINE == *"Tests run"* ]]; then
        #split the line into words by ","
        IFS=',' read -rd '' -a WORDS <<< "$LINE"
        for WORD in "${WORDS[@]}"; do
            #extract the number of tests
            if [[ $WORD == *"Tests run"* ]]; then
                TOTAL_TESTS=$(echo "$WORD" | grep -o '[0-9]\+')
            fi
            #extract the number of failed tests
            if [[ $WORD == *"Failures"* ]]; then
                FAILED_TESTS=$(echo "$WORD" | grep -o '[0-9]\+')
            fi
            #extract the number of error tests
            if [[ $WORD == *"Errors"* ]]; then
                ERROR_TESTS=$(echo "$WORD" | grep -o '[0-9]\+')
            fi
            #extract the number of skipped tests
            if [[ $WORD == *"Skipped"* ]]; then
                SKIPPED_TESTS=$(echo "$WORD" | grep -o '[0-9]\+')
            fi
        done
    #Error message
    elif [[ $LINE == *"BUILD FAILURE"* ]]; then
        echo "Build failure"
        exit 1
    fi
done


# Print the test results
echo "Total tests: $TOTAL_TESTS"
echo "Failed tests: $FAILED_TESTS"
echo "Error tests: $ERROR_TESTS"
echo "Skipped tests: $SKIPPED_TESTS"
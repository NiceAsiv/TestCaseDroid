import subprocess
import sys
import os

# Check if the arguments are empty
if len(sys.argv) < 3:
    print("Please enter the project path and the test class name")
    print("Example: /home/user/Project/src/test/java com.example.TestClass")
    sys.exit(1)

project_path = sys.argv[1]
test_class_name = sys.argv[2]

# Optional test case arguments
test_case_args = sys.argv[3:]

print(f"Project path: {project_path}")
print(f"Test class name: {test_class_name}")

# Check if the project path is valid
if not os.path.isdir(project_path):
    print("Invalid project path")
    sys.exit(1)

command =[]
# Run the test case using Maven
if len(test_case_args) < 1:
    command = ["mvn", "-f", project_path, "test", "-Dtest=" + test_class_name]
else:
    command = ["mvn", "-f", project_path, "test", "-Dtest=" + test_class_name, "-Dexec.args=" + " ".join(test_case_args)]
output = subprocess.check_output(command, text=True)

# Check if the output is empty
if not output:
    print("Test case not found")
    sys.exit(1)

# Initialize the test results
total_tests = 0
failed_tests = 0
error_tests = 0
skipped_tests = 0

# Parse the test results line by line
for line in output.splitlines():
    if "Tests run" in line:
        parts = line.split(",")
        for part in parts:
            if "Tests run" in part:
                total_tests = int(part.split(":")[1].strip())
            elif "Failures" in part:
                failed_tests = int(part.split(":")[1].strip())
            elif "Errors" in part:
                error_tests = int(part.split(":")[1].strip())
            elif "Skipped" in part:
                skipped_tests = int(part.split(":")[1].strip())

# Print the test results
print(f"Total tests: {total_tests}")
print(f"Failed tests: {failed_tests}")
print(f"Error tests: {error_tests}")
print(f"Skipped tests: {skipped_tests}")
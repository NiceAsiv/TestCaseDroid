import subprocess


def run_java_test(classpath, test_class, junit_jar):
    # Construct the command
    command = ["java", "-jar", junit_jar, "-cp", classpath, "--select-class", test_class]

    # Run the command and capture the output
    result = subprocess.run(command, capture_output=True, text=True)

    # Print the output
    print(result.stdout)


# Example usage
run_java_test("/path/to/classes", "my.package.MyTest", "/path/to/junit-platform-console-standalone.jar")

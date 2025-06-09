# Qubership Testing Platform Macros Core Library

## Purpose
Macros Core Library is designed to evaluate macros and used currently in the Qubership Testing Platform Datasets Service.

## Functionality

Macros Core Library supports Global Macros (available-from-the-box) and can evaluate custom macros written in JavaScript Language.
Global Macros are:
- DATE - Returns current date in specified format in some timezone (optional argument)
- GET_SEC - Returns seconds of the specified date in given format in some timezone (optional argument)
- GET_MIN - Returns minutes of the specified date in given format in some timezone (optional argument)
- GET_HOUR - Returns hours of the specified date in given format in some timezone (optional argument)
- GET_DAY - Returns day of the specified date in given format in some timezone (optional argument)
- GET_MONTH - Returns month of the specified date in given format in some timezone (optional argument)
- GET_YEAR - Returns year of the specified date in given format in some timezone (optional argument)
- MOVE_DATE - Shifts the current date according the parameter, e.g. MOVE_DATE("-1d+1h") - minus 1 day and plus 1 hour
- RAND - Returns a random integer in the range from 0 to (10 in the specified power) - 1 (including)
- RANDBETWEEN - Returns the next pseudorandom number between the first argument value (inclusive) and the second argument value (inclusive)
- RAND_UUID - Returns random UUID
  - UUID - shortcut name, does the same
- RANDOM - Returns random upper case characters
- CHARS - Returns random lower case characters
- SHIFT_SEC - Returns new date, after correcting seconds of the specified date to the shift value (can be negative)
- SHIFT_MIN - Returns new date, after correcting minutes of the specified date to the shift value (can be negative)
- SHIFT_HOUR - Returns new date, after correcting hour of the specified date to the shift value (can be negative)
- SHIFT_DAY - Returns new date, after correcting day of the specified date to the shift value (can be negative)
- SHIFT_MONTH - Returns new date, after correcting month of the specified date to the shift value (can be negative)
- SHIFT_YEAR - Returns new date, after correcting year of the specified date to the shift value (can be negative)
- ENV_VARIABLE - Returns value of variable from Test Environment by name of variable
- RES_VARIABLE - Returns value of variable from TestCase Context by name of variable
- EXECUTION_REQUEST_ID - Returns ID of the Execution Request
- EXECUTION_REQUEST_NAME - Returns Name of the Execution Request
- EXECUTION_REQUEST_NUMBER - Returns Number of the Execution Request
- EXECUTION_REQUEST_SHORT_NAME - Returns Short Name of the Execution Request
- SDS_DATA_SET - Returns the name of Service Data Set which is default for TestCase or is used during TestRun execution
- SDS_DATA_SET_LIST - Returns the name of Service Data Set List which is default for TestCase or is used during TestRun execution
- SDS_FULL_NAME - Returns the full name of Service Data Set
- SDS_VISIBILITY_AREA - Returns the name of Service Visibility Area which Data Set is default for TestCase or is used during TestRun execution
- TEST_CASE_NAME - Returns name of the Test Case
- TEST_CASE_SHORT_NAME - Returns short name of the Test Case
- TEST_ENV_NAME - Returns name of the Environment used in the test
- TEST_RUN_ID - Returns ID of the Test Run
- TEST_RUN_KEY - Returns Key of the Test Run
- TEST_RUN_NAME - Returns Name of the Test Run
- TEST_RUN_NUMBER - Returns Number of the Test Run
- TEST_RUN_SHORT_NAME - Returns Short Name of the Test Run
- CONTEXT - Returns context variable value for the specified variable name
- DATA_SET_SERVICE_VALUE - Returns data set service value for specified variable path

## Local build

In IntelliJ IDEA, one can select 'github' Profile in Maven Settings menu on the right, then expand Lifecycle dropdown of qubership-atp-macros-core module, then select 'clean' and 'install' options and click 'Run Maven Build' green arrow button on the top.

Or, one can execute the command:
```bash
mvn -P github clean install
```

## How to add dependency into a service
```xml
    <!-- Change version number if necessary -->
    <dependency>
        <groupId>org.qubership.atp</groupId>
        <artifactId>atp-macros-core</artifactId>
        <version>1.0.26-SNAPSHOT</version>
    </dependency>
```


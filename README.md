# Qubership Testing Platform Macros Core Library

## Purpose
Macros Core Library is designed to evaluate macros and used currently in the Qubership Testing Platform Datasets Service.

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


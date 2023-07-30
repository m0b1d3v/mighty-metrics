# ADR: Logging

## Status

Accepted.

## Context

Program output can be very helpful in auditing and monitoring events.
Most important of all is capturing any stack traces from thrown exceptions.
There must be a process in place for how to log this information.

## Decision

[Logback](https://logback.qos.ch/) will be the library used for logging.
There is not much thought process involved in using this one over any other logging library.
Two requirements are met by this library and not much else matters:
- Logging output is always done to console in JSON format
- Key-value information pairs can be added to logging statements in JSON format

By outputting to console in JSON format we can leverage Unix processes for log persistence.
We can then audit those logs for events using tools like `jq`.

## Consequences

Few, if any.

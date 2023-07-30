# ADR: Build tool

## Status

Accepted.

## Context

Most programming languages have tooling available to dictate everything needed to build a program.
Third-party dependency management is a crucial aspect of this for a more productive and enjoyable experience.

## Decision

[Gradle](https://gradle.org/) will be our build tool and dependency manager.

I don't much care about the technical differences between Gradle and Maven.
Gradle files being much less verbose than XML makes me appreciate it more though.
Other build tools like Ant and SBT exist but are not very common.

## Consequences

Maven is more widely used by Java developers.
It can take some research to figure out how to replicate processes from Maven to Gradle.

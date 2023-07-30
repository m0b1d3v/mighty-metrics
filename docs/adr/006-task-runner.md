# ADR: Task runner

## Status

Accepted.

## Context

Every project has tasks that must be run hundreds or thousands of times throughout its life.
Examples would be building, testing, analysis, running, etc.
Good task running processes save developers time and effort from inefficient duplicated processes.

## Decision

Common tasks will be listed in the project root [Makefile](/Makefile).
[Make](https://www.gnu.org/software/make/manual/make.html) can stitch together these tasks in many different ways.

## Consequences

Some developers may not be familiar with Make.
Make can also cause some headaches with proper formatting or escaping intricate commands.

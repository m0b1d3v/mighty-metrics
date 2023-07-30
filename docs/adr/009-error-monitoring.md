# ADR:Exception monitoring

## Status

Accepted.

## Context

When things go wrong they need to be known about as soon as possible.

## Decision

[Sentry](https://sentry.io) is a great error and exception monitor.
It can take almost any programming environment and has a generous free tier.

## Consequences

Care should be taken not to run Sentry in development environments to keep the history clean.
Email alerts may also not be checked in a timely manner.

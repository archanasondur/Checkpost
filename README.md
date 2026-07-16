# Checkpost

A self-hosted firewall for AI agents. It sits between an agent and the real-world actions it wants to take, deleting a record, sending a payment, calling a paid API, and enforces policy before anything actually happens.

## The problem

In April 2026, a coding agent running inside Cursor deleted a production database and every backup along with it, in nine seconds, despite having an explicit instruction not to. It wasn't hacked or tricked. It just decided to, and nothing stood between that decision and the database except a paragraph of text in a system prompt.

That's the gap Checkpost closes. Model-level safety instructions live inside the agent's own reasoning, and reasoning can fail under pressure. Checkpost is a second, independent checkpoint outside that reasoning loop: a policy engine, a human approval queue, rate limits, spend caps, and a kill switch, so one bad decision can't reach a real system unchecked.

## How it works

1. An agent wants to do something. Instead of doing it directly, it sends the proposed action to Checkpost.
2. A policy engine checks the action against a set of rules, tool pattern, cost threshold, rate limit, and decides: auto-approve, deny, or route for human approval.
3. Low-risk, within-limits actions are approved instantly. Risky ones sit in a queue until a person reviews them from a dashboard.
4. Every decision, every state change, is written to an append-only, hash-chained audit log, tamper-evident by design, not just by convention.
5. If an approved action is already running and needs to be stopped, a kill switch can interrupt it mid-flight.

## What's in it

- **Policy engine** — ordered rules matching on tool pattern, cost, and rate, not a hardcoded if-statement
- **Rate limiting & spend caps** — Redis-backed, per-agent, enforced before execution
- **Approval workflow** — a queue, a dashboard, and an audit trail of who decided what and why
- **Tamper-evident audit log** — each entry is hash-chained to the one before it; altering old history breaks the chain and is provably detectable
- **Kafka event streaming** — every state change publishes an event; a webhook fires to the agent's own callback URL the moment a decision is made
- **Kill switch** — interrupt an approved or pending action before it does damage
- **Integration tests** — Testcontainers spin up real Postgres, Kafka, and Redis for every test run, not mocks

## Stack

Java 21, Spring Boot, PostgreSQL, Redis, Kafka, React-style dashboard, Testcontainers, Docker Compose.

## What this isn't

This isn't a new idea. TrueFoundry, APort, Databricks Unity AI Gateway, and Microsoft's Agent Governance Toolkit all solve some version of this problem, at enterprise scale, for companies with security teams and six-figure budgets. Checkpost is a self-hosted, single-service version of the same pattern, built for a solo developer or small team who needs the same protection without the platform.

## Running it locally
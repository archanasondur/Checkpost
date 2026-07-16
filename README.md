Checkpost

A self-hosted reverse proxy for AI agent actions. Agents propose actions before executing them; Checkpost evaluates policy, enforces rate and spend limits, routes risky actions to a human approval queue, and writes every decision to a tamper-evident audit trail.

Not a chat gateway. A gate for what an agent is about to do.

Why this exists

In April 2026, a coding agent running inside Cursor on Claude Opus 4.6 was given access to a staging environment and, within nine seconds, deleted the entire production database for PocketOS along with every backup, despite an explicit system-prompt instruction never to run destructive commands without user request. The agent had not been compromised or manipulated. It simply decided, in its own reasoning, to do it anyway.

Gartner found that fewer than 15% of enterprise AI agent deployments in 2025 included systematic exception routing for high-risk actions. Model-level alignment (system prompts, refusal training) lives inside the model's own reasoning and can fail under task pressure, as it did here. Checkpost is a governance layer that sits outside that reasoning loop, so a bad decision inside an agent can't reach a real system without an independent check.

What it does


An agent wants to take an action (delete a record, send a payment, call a paid API).
Instead of doing it directly, it submits the intended action to Checkpost.
A policy engine evaluates the action against ordered rules (tool pattern, condition, action) and checks Redis-backed rate limits and daily spend caps.
Low-risk, within-limits actions are auto-approved instantly.
Higher-risk actions are queued for human approval in a dashboard.
Every state transition is written to a hash-chained, tamper-evident audit log.
Terminal decisions (approved/denied/killed) publish to Kafka and, if a callback URL was registered, trigger a webhook back to the agent.
An in-flight or pending action can be interrupted at any time via a kill switch.


Architecture

Agent → POST /v1/actions → Policy Engine → Rate Limiter (Redis) → Spend Cap (Postgres)
                                    ↓
                    AUTO_APPROVED  or  PENDING_APPROVAL
                                    ↓
                    Human decision (dashboard) → APPROVED / DENIED
                                    ↓
                    Kafka (action-state-changes) → Webhook delivery
                                    ↓
                    Hash-chained AuditLog (every transition, tamper-evident)

State machine

SUBMITTED → POLICY_EVALUATING → (APPROVED | PENDING_APPROVAL → APPROVED/DENIED/TIMED_OUT) → EXECUTING → (SUCCEEDED | FAILED | KILLED)

Core features


Policy engine — ordered rules matching on tool pattern, an optional cost condition, and an action (allow / require approval / deny). First match wins.
Rate limiting — Redis-backed, per-agent, per-minute counters.
Spend caps — per-agent daily spend tracked in Postgres, enforced before an action is approved.
Approval workflow — a queue dashboard for human sign-off on risky actions, with decision reasons recorded.
Kill switch — interrupts an action from PENDING_APPROVAL, APPROVED, or EXECUTING.
Idempotency keys — duplicate submissions of the same logical action return the original request instead of creating a new one.
Hash-chained audit trail — every audit entry stores a SHA-256 hash of the entry before it. Tampering with any past row is detectable via /v1/audit/verify.
Kafka event streaming — state changes publish to an action-state-changes topic; consumers (webhook delivery, and any future consumer) subscribe independently.
Webhook callbacks — agents register a callback URL at submission time and are notified on terminal decisions instead of polling.
Integration tests — real Postgres, Kafka, and Redis containers via Testcontainers, not mocks.


Tech stack

Java 21, Spring Boot 4.1, PostgreSQL, Redis, Apache Kafka, Spring Security, Spring Data JPA, Docker Compose, Testcontainers, a static HTML/JS dashboard.

API surface

EndpointPurposePOST /v1/actionsSubmit a proposed action (tool, payload, estimatedCost, optional idempotencyKey, optional callbackUrl)GET /v1/actions/{id}Poll an action's statusPOST /v1/actions/{id}/approveApprove a pending actionPOST /v1/actions/{id}/denyDeny a pending action, with a reasonPOST /v1/actions/{id}/killInterrupt an in-flight or pending actionPOST /v1/policiesCreate a policy ruleGET /v1/audit?actionRequestId=Query the audit trailGET /v1/audit/verifyVerify the integrity of the entire audit chainGET /v1/agents/{id}/spendCurrent spend against an agent's daily cap

Running it locally

bashdocker compose up -d          # Postgres + Redis + Kafka
./mvnw spring-boot:run

Dashboard: http://localhost:8080/
Health check: curl localhost:8080/actuator/health

Running the tests

bash./mvnw clean test -Dtest=CheckpostIntegrationTest

Spins up real Postgres, Kafka, and Redis containers via Testcontainers and tears them down automatically.

Honest scope

This is a solo learning and portfolio project, not a production system, and not a novel idea. It's inspired by production AI governance gateways — TrueFoundry's MCP Gateway, APort, Microsoft's Agent Governance Toolkit, Databricks Unity AI Gateway — scoped down to something a single developer can self-host without an enterprise platform.

Explicitly out of scope: multi-tenant billing, SSO, compliance certification tooling, an LLM-based risk classifier (the policy engine is rule-based by design, easier to reason about and test), and a custom agent framework (the firewall is the project; any agent that calls it is just a demo of the integration).

Spend tracking is recorded at submission time, not confirmed execution, since real tool execution isn't wired up. Exactly-once delivery guarantees are not implemented; idempotency keys cover the realistic retry case.
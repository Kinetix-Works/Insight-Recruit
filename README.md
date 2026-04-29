# Insight-Recruit
InsightRecruit: A context-aware AI talent intelligence platform designed to eliminate the "resume black hole." Built with Java/Spring Boot and React, it utilizes asynchronous LLM processing to provide transparent candidate rankings and actionable interview insights for high-volume recruitment teams.



🚀 InsightRecruit: AI-Driven Talent Intelligence
InsightRecruit is not just a resume screener; it is a Decision Velocity engine. It transforms raw resume data into transparent, actionable rankings, enabling recruiters to identify the top 5% of talent in minutes.

🛠️ Tech Stack
Backend: Java 21, Spring Boot 3.x (Stateless REST APIs).
Frontend: React (SPA) with an Explanation Dashboard.
Database: PostgreSQL with Row-Level Security (RLS) for multi-tenancy.
Infrastructure: Redis/RabbitMQ for Asynchronous AI Worker Pattern.
Migration: Flyway/Liquibase for versioned schema evolution.

✨ Core Features (MVP)
Intelligent Ranker: Semantic scoring based on skill matches and Risk Flags.
Explanation Dashboard: Transparent justifications for every score to drive human-in-the-loop decisions.
Bulk Processor: Secure upload and parsing of multiple PDFs/Docx files.
Feedback Loop: Recruiter-led "corrections" to the AI to power future model fine-tuning.

🛡️ Compliance & Security
GDPR/DPDP Ready: Automated 90-day data retention/deletion policies.
Multi-Tenancy: Strict data isolation enforced at the database level.
Audit Logging: Comprehensive tracking of all PII access for legal compliance.

🚀 Getting Started
Local Environment: Spin up infrastructure using docker-compose up.
Database Migration: Run ./mvnw flyway:migrate to initialize the RLS-enabled schema.
Branching Strategy: We follow a main (prod), develop (staging), and feature/* strategy.

# First Commit 

# LinkedIn System Architecture – Microservices Clone

A scalable, event-driven LinkedIn clone backend microservices architecture built using **Spring Boot**, **Apache Kafka**, **Elasticsearch**, **Redis**, and **AWS S3**.

---

## Project Overview

This project implements a full-featured, distributed system simulating LinkedIn's core infrastructure. It handles real-time feed fan-out, high-speed full-text searching across millions of indexed documents, asynchronous real-time notifications, connection request flows, and media management under a secure API Gateway environment.

---

## High-Level System Architecture

```
                       ┌──────────────────────┐
                       │     API Gateway      │
                       │  (JWT Auth & Rate    │
                       │     Limiting)        │
                       └──────────┬───────────┘
                                  │
         ┌────────────────────────┼────────────────────────┐
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│  User Service   │      │  Post Service   │      │  Feed Service   │
│ (Users & Rel.)  │      │ (Posts/Media)   │      │ (Fan-out Engine)│
└────────┬────────┘      └────────┬────────┘      └─────────────────┘
         │                        │                        
         │   ┌────────────────────┴────────────────────┐   
         │   │            Apache Kafka                 │   
         │   │       (Event-Driven Engine)             │   
         └───┴────────────────────┬────────────────────┴
                                  │
                 ┌────────────────┴────────────────┐
                 ▼                                 ▼
       ┌──────────────────┐               ┌──────────────────┐
       │  Search Service  │               │ Notification Svc │
       │ (Elasticsearch)  │               │   (Stateless)    │
       └──────────────────┘               └──────────────────┘

```

---

## Technology Stack & Usage

| Technology | Purpose & Description |
| --- | --- |
| **Java & Spring Boot** | Core backend framework for developing isolated microservices. |
| **Spring Security & JWT** | Centralized authentication, state-less session control, and access management. |
| **Spring Cloud API Gateway** | Single entry-point handling routing, rate limiting (DoS protection), and token verification. |
| **Apache Kafka** | Distributed event-streaming platform for event-driven asynchronous messaging across microservices. |
| **Elasticsearch** | High-performance search engine powering full-text queries for profiles, posts, and skills in milliseconds. |
| **Redis** | In-memory key-value data structure store used for caching and managing real-time feeds via fan-out caching. |
| **AWS S3** | Cloud storage for user media assets such as profile pictures, cover images, and post attachments. |
| **Docker** | Containerization solution used to pull and run third-party middleware services (Kafka, Elasticsearch, Redis, Databases). |

---

## Microservices Breakdown

### 1. **API Gateway Service**

* **Role**: Centralized routing and security entry-point.
* **Features**:
  * Centralized JWT authentication validation.
  *  Rate limiting to prevent abuse and denial-of-service (DoS) attacks.



### 2. **User Service**

* **Role**: User identity, profile management, and network connections.
* **Features**:
  * User registration, authentication, and JWT token issuance.
  * User connections (send/accept/reject connection requests).
  * Profile and skills management.
  * Triggers Kafka event `user.created` to notify Search Service for immediate indexing.



### 3. **Post Service**

* **Role**: Content creation engine.
* **Features**:
  * Create, like, and comment on posts.
  * Direct media upload integration with **AWS S3**.
  * Publishes Kafka events such as `post.created`.



### 4. **Feed Service**

* **Role**: Real-time personalized feed generator.
* **Features**:
  * Consumes `post.created` Kafka events.
  * Implements **Fan-out on Write** pattern using **Redis** to update connections' feeds instantly.
  * In-memory cache design ensuring high-read availability without hitting primary relational databases.



### 5. **Search Service**

* **Role**: Discovery and query engine.
* **Features**:
  * Event-driven indexing of user profiles and posts powered by **Elasticsearch**.
  * Full-text search support by names, skills (e.g., *Java*, *Kafka*), job titles, and posts.



### 6. **Notification Service**

* **Role**: Real-time event alert engine.
* **Features**:
  * Stateless service consuming connection and engagement events (likes, comments, connection approvals).
  * Immediate dispatch of alerts to affected users.



---

## Key Architectural Patterns

1. **Event-Driven Architecture (EDA)**: Decoupled communication across services via Apache Kafka topics.
2. **Fan-Out on Write**: When a post is created, it is asynchronously distributed directly to all active connections' Redis feeds for low-latency retrieval.
3. **CQRS (Command Query Responsibility Segregation)**: Write operations are handled by domain services (User, Post) while complex search-read queries are redirected to Elasticsearch.

---

## Prerequisites & Local Setup

### Prerequisites

* **Java SDK** (v17+ recommended)
* **Maven**
* **Docker**
* **AWS S3 Account Credentials**

### Docker Infrastructure Setup

Start required middleware services using Docker:

```bash
# Pull and start containers for dependencies
docker run -d --name redis -p 6379:6379 redis
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:8.x
# Start Kafka and PostgresSQL/MySQL containers accordingly

```

### Running the Services

1. Clone the repository:
```bash
git clone https://github.com/luizgdsmdev/search-engine-SpringBoot-Elasticsearch-Kafka-Redis.git
cd search-engine-SpringBoot-Elasticsearch-Kafka-Redis

```


2. Build all services using Maven:
```bash
mvn clean install

```


3. Run each microservice (API Gateway, User Service, Post Service, Feed Service, Search Service, Notification Service).

---

## API Endpoints Overview

| Service | Method | Endpoint | Description |
| --- | --- | --- | --- |
| **Gateway/User** | `POST` | `/api/v1/auth/register` | Register a new user profile. |
| **Gateway/User** | `POST` | `/api/v1/auth/login` | Authenticate and obtain JWT. |
| **Post Service** | `POST` | `/api/v1/posts` | Create a post (with S3 media attachment). |
| **Post Service** | `POST` | `/api/v1/posts/{id}/like` | Like a post. |
| **Search Service** | `GET` | `/api/v1/search/people?q={query}` | Search profiles by keywords/skills. |
| **Feed Service** | `GET` | `/api/v1/feed` | Retrieve personalized user feed from Redis. |
|  |  |  |  |
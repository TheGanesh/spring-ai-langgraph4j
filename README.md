# LangGraph4J + Spring AI — Complete Learning Project

A comprehensive project demonstrating **all core and advanced LangGraph4J concepts** with Spring AI integration.

## Quick Start

```bash
export OPENAI_API_KEY=sk-your-key-here
mvn clean install -DskipTests
mvn spring-boot:run
```

## Prerequisites

- **Java 17+**, **Maven 3.8+**, **OpenAI API Key** (or Ollama for local LLM)

---

## 🟢 Hello World — Core Concepts (State, Nodes, Edges)

```
POST /greet  { "name": "Ganesh" }
```

Graph: `START → greeting → sentiment → (conditional) → celebrate/encourage → END`

```bash
curl -X POST http://localhost:8080/greet \
  -H "Content-Type: application/json" \
  -d '{"name": "Ganesh"}'
```

---

## 🔧 Concept 1: Tool Calling

LLM decides when to call external tools (weather, calculator) and uses results to answer.

```
POST /tools/ask  { "query": "What's the weather in Tokyo?" }
POST /tools/ask  { "query": "What is 42 * 17?" }
POST /tools/ask  { "query": "Tell me a joke" }          ← no tool needed
```

Graph: `START → agent → (needs_tool?) → tool_executor → agent (loop) → END`

```bash
curl -X POST http://localhost:8080/tools/ask \
  -H "Content-Type: application/json" \
  -d '{"query": "What is the weather in London?"}'
```

---

## 👤 Concept 2: Human-in-the-Loop

Graph pauses for human approval before publishing content.

**Step 1 — Submit (graph pauses):**
```bash
curl -X POST http://localhost:8080/human-loop/submit \
  -H "Content-Type: application/json" \
  -d '{"topic": "AI in healthcare"}'
# Returns: { "threadId": "abc-123", "draft": "...", "status": "AWAITING_REVIEW" }
```

**Step 2 — Approve/Reject (graph resumes):**
```bash
curl -X POST http://localhost:8080/human-loop/review \
  -H "Content-Type: application/json" \
  -d '{"threadId": "abc-123", "approved": true, "feedback": "Looks great!"}'
```

Graph: `START → generate_draft → [INTERRUPT] → human_review → final_response → END`

---

## 💾 Concept 3: Persistence / Checkpointing

Multi-turn conversation with memory — same threadId remembers context.

```bash
# Turn 1
curl -X POST http://localhost:8080/checkpoint/chat \
  -H "Content-Type: application/json" \
  -d '{"threadId": "my-thread", "message": "My name is Ganesh"}'

# Turn 2 — AI remembers!
curl -X POST http://localhost:8080/checkpoint/chat \
  -H "Content-Type: application/json" \
  -d '{"threadId": "my-thread", "message": "What is my name?"}'

# Different thread — AI does NOT know
curl -X POST http://localhost:8080/checkpoint/chat \
  -H "Content-Type: application/json" \
  -d '{"threadId": "other-thread", "message": "What is my name?"}'
```

Graph: `START → build_context → respond → END` (with MemorySaver)

---

## 🧩 Concept 4: Subgraphs

A document analysis pipeline with a nested summarization subgraph.

```bash
curl -X POST http://localhost:8080/subgraph/analyze \
  -H "Content-Type: application/json" \
  -d '{"document": "Artificial intelligence is transforming healthcare. Machine learning algorithms can now detect diseases from medical images with accuracy rivaling human doctors. Natural language processing helps extract insights from clinical notes. Robotic surgery systems are becoming more precise. AI-powered drug discovery is accelerating the development of new treatments."}'
```

```
OUTER:  START → classify → [SUBGRAPH: summarize] → format → END
INNER:  START → extract_key_points → generate_summary → END
```

---

## 📡 Concept 5: Streaming (SSE)

Real-time streaming of graph progress via Server-Sent Events.

```bash
curl -N http://localhost:8080/streaming/write?topic=Artificial+Intelligence
```

Each node's completion is streamed as an SSE event:
```
event: create_outline    data: { outline... }
event: write_content     data: { content... }
event: polish            data: { polished final... }
event: complete          data: { "status": "done" }
```

Graph: `START → create_outline → write_content → polish → END`

---

## 🤖 Concept 6: Multi-Agent

Supervisor pattern — multiple AI agents collaborate to complete a task.

```bash
curl -X POST http://localhost:8080/multi-agent/run \
  -H "Content-Type: application/json" \
  -d '{"task": "Write about the future of renewable energy"}'
```

```
START → supervisor → (researcher | writer | FINISH)
              ↑              |            |
              └──────────────┴────────────┘
```

Response includes an `agentLog` showing the collaboration flow.

---

## Project Structure

```
src/main/java/com/example/langgraph/
├── LangGraphApplication.java                    # Spring Boot entry point
│
├── greeting/                                    # Hello World — Core Concepts
│   ├── GreetingState.java                       #   State definition
│   ├── GreetingGraphConfig.java                 #   Graph wiring
│   ├── GreetingController.java                  #   REST API
│   ├── GreetRequest.java                        #   Request DTO
│   ├── GreetResponse.java                       #   Response DTO
│   └── nodes/                                   #   Node implementations
│       ├── GreetingNode.java
│       ├── SentimentNode.java
│       └── ResponseNode.java
│
├── toolcalling/                                 # Concept 1: Tool Calling
│   ├── ToolCallingState.java                    #   State with messages
│   ├── ToolCallingGraphConfig.java              #   Agent + tool executor loop
│   ├── ToolCallingController.java               #   REST API
│   ├── nodes/                                   #   Node implementations
│   │   ├── AgentNode.java
│   │   └── ToolExecutorNode.java
│   └── tools/                                   #   Tool definitions
│       ├── WeatherTool.java                     #     Mock weather API
│       └── CalculatorTool.java                  #     Calculator tool
│
├── humanloop/                                   # Concept 2: Human-in-the-Loop
│   ├── HumanLoopState.java                      #   State with approval field
│   ├── HumanLoopGraphConfig.java                #   Graph with interruptBefore
│   ├── HumanLoopController.java                 #   Submit + review endpoints
│   └── nodes/                                   #   Node implementations
│       ├── GenerateDraftNode.java
│       ├── HumanReviewNode.java
│       └── FinalResponseNode.java
│
├── checkpoint/                                  # Concept 3: Checkpointing
│   ├── CheckpointState.java                     #   State with conversation history
│   ├── CheckpointGraphConfig.java               #   Graph with MemorySaver
│   ├── CheckpointController.java                #   Multi-turn chat endpoint
│   └── nodes/                                   #   Node implementations
│       ├── BuildContextNode.java
│       └── RespondNode.java
│
├── subgraph/                                    # Concept 4: Subgraphs
│   ├── SubGraphState.java                       #   Shared state for both graphs
│   ├── SubGraphConfig.java                      #   Outer graph + inner subgraph
│   ├── SubGraphController.java                  #   Document analysis endpoint
│   └── nodes/                                   #   Node implementations
│       ├── ClassifyDocumentNode.java
│       ├── ExtractKeyPointsNode.java
│       ├── GenerateSummaryNode.java
│       └── FormatOutputNode.java
│
├── streaming/                                   # Concept 5: Streaming
│   ├── StreamingState.java                      #   State with step tracking
│   ├── StreamingGraphConfig.java                #   3-step content pipeline
│   ├── StreamingController.java                 #   SSE endpoint
│   └── nodes/                                   #   Node implementations
│       ├── CreateOutlineNode.java
│       ├── WriteContentNode.java
│       └── PolishNode.java
│
├── multiagent/                                  # Concept 6: Multi-Agent
│   ├── MultiAgentState.java                     #   State with agent routing
│   ├── MultiAgentGraphConfig.java               #   Supervisor + worker agents
│   ├── MultiAgentController.java                #   Task execution endpoint
│   └── nodes/                                   #   Node implementations
│       ├── SupervisorAgentNode.java
│       ├── ResearcherAgentNode.java
│       └── WriterAgentNode.java
│
└── studio/                                      # LangGraph Studio Integration
    ├── LangGraphStudioConfig.java               #   Studio graph registration
    └── StudioWebConfig.java                     #   Web/CORS configuration
```

## Key LangGraph4J APIs Used

| Concept | Key API |
|---------|---------|
| **State** | `AgentState`, `Channels.appender()` |
| **Nodes** | `node_async()`, `AsyncNodeAction` |
| **Edges** | `addEdge()`, `addConditionalEdges()`, `edge_async()` |
| **Tool Calling** | Conditional loop: agent → tool_executor → agent |
| **Human-in-the-Loop** | `CompileConfig.interruptBefore()`, `updateState()` |
| **Checkpointing** | `MemorySaver`, `RunnableConfig.threadId()` |
| **Subgraphs** | `addSubgraph(name, StateGraph)` |
| **Streaming** | `graph.stream()` + Spring `SseEmitter` |
| **Multi-Agent** | Supervisor conditional routing + worker loops |

## Using Ollama (Local LLM) Instead of OpenAI

1. In `pom.xml`, swap `spring-ai-openai-spring-boot-starter` with `spring-ai-ollama-spring-boot-starter`
2. In `application.yml`, replace the `spring.ai` section:
   ```yaml
   spring:
     ai:
       ollama:
         base-url: http://localhost:11434
         chat:
           options:
             model: llama3
   ```
3. Run: `ollama run llama3`

## Dependency Versions

> **Note:** If build fails, check for latest versions:
> - [LangGraph4J on Maven Central](https://central.sonatype.com/search?q=langgraph4j)
> - [Spring AI Releases](https://docs.spring.io/spring-ai/reference/)
> - Update version properties in `pom.xml` accordingly.

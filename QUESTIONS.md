# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**

This question addresses the "Design-First" vs. "Code-First" approaches to API development.

**Approach 1: OpenAPI with Code Generation (Design-First, used for `Warehouse`)**

*   **Pros:**
    *   **Strong Contract:** The YAML file is the single source of truth, ensuring the server, clients, and documentation are always in sync. This is excellent for reducing integration bugs.
    *   **Parallel Development:** Once the spec is agreed upon, frontend and backend teams can work independently using generated mocks and clients.
    *   **Clear Separation:** It forces a clear distinction between the API design and its implementation, often leading to better-designed, consumer-focused APIs.
    *   **Rich Tooling:** A vast ecosystem exists for generating interactive documentation (Swagger UI), client SDKs, and server stubs.

*   **Cons:**
    *   **Complexity:** Adds a code-generation step to the build process, which can be complex to configure and may slow down local development.
    *   **Verbosity:** Writing and maintaining large OpenAPI YAML files can be cumbersome and has a learning curve.
    *   **Rigidity:** Can be less flexible when you need to add framework-specific annotations or behavior not easily described in the spec.

**Approach 2: Hand-Coded Endpoints (Code-First, used for `Product` and `Store`)**

*   **Pros:**
    *   **Simplicity & Speed:** It's faster for prototyping and for simple or internal APIs. There's no extra build step or tooling required.
    *   **Full Control:** Developers have complete control over the implementation, annotations, and can easily leverage all framework features.

*   **Cons:**
    *   **Documentation Drift:** The documentation is generated from code annotations, and it's easy for developers to forget to update them, leading to inaccurate API docs.
    *   **No Formal Contract:** The code is the contract, which makes it harder for external teams to integrate without inspecting the source code.

**Which would I choose and why?**

For a production-grade system, especially one with multiple teams or public consumers, **I would choose the OpenAPI (Design-First) approach for all core business APIs (`Warehouse`, `Store`, `Product`)**.

The benefits of a strong, version-controlled contract that prevents integration bugs and enables parallel workstreams far outweigh the initial setup cost. It establishes a professional, scalable workflow. The code-first approach is acceptable for internal-only, non-critical endpoints or rapid prototyping, but for the main business domain, the rigor of a design-first approach is superior for long-term maintainability.


---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
A pragmatic testing strategy prioritizes tests that provide the most confidence for the least cost. I would follow the "Testing Pyramid" model.

**1. Unit Tests (Highest Priority):**
*   **Focus:** The **domain layer**, specifically the **Use Cases** (`CreateWarehouseUseCase`, `ArchiveWarehouseUseCase`, etc.). This is where the core business logic and rules reside.
*   **Implementation:** Use JUnit 5 and Mockito (`@InjectMock`) to mock all external dependencies (like repositories). This makes tests extremely fast and isolates the logic being tested. Use **Parameterized Tests** (`@ParameterizedTest`) to efficiently cover many validation scenarios (e.g., invalid capacity, duplicate codes).
*   **Why:** They are fast, stable, and give the highest ROI by precisely verifying complex business rules.

**2. Integration Tests (Medium Priority):**
*   **Focus:** The interaction between application layers, especially with the database and other external systems. This is critical for this assignment.
*   **Implementation:**
    *   **Repository/Persistence Tests:** Verify that Panache queries, optimistic locking, and transaction management work correctly.
    *   **API/Use Case Tests:** Use `@QuarkusTest` to run the full application and **Testcontainers** (`WarehouseTestcontainersIT`) to provide a real PostgreSQL database. This ensures queries are tested against a production-like environment.
    *   **Concurrency Tests:** The `WarehouseConcurrencyIT` is **essential**. Tests simulating race conditions are the only way to prove that the optimistic locking strategy is correctly implemented.
*   **Why:** They provide confidence that components work together correctly and that persistence logic is sound.

**3. End-to-End (E2E) Tests (Lowest Priority for this assignment):**
*   **Focus:** Testing the entire deployed application via its public interface.
*   **Why Low Priority:** Given the time constraints, E2E tests are too slow and complex. The confidence from a solid suite of unit and integration tests is sufficient.

**Ensuring Effective Coverage Over Time:**
*   **Automate in CI/CD:** All tests must run in a CI pipeline on every commit. A failing test must block merges/deployments.
*   **Focus on Logic, Not Lines:** Don't just chase 100% line coverage. Ensure every business rule, validation, and critical path (both success and error paths) is tested.
*   **Bug-Driven Testing:** When a bug is found, write a failing test that reproduces it *before* fixing the bug. This prevents regressions.
*   **Mandatory Test Reviews:** Code reviews must include a thorough review of the accompanying tests to ensure they are meaningful and cover edge cases.


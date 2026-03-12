# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
```txt

```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Both approaches, API-first (using OpenAPI to generate code) and code-first (hand-coding endpoints), have distinct advantages and trade-offs.

**API-First (e.g., `Warehouse` API):**
*   **Pros:**
    *   **Contract as Single Source of Truth:** The OpenAPI spec is the definitive contract, preventing ambiguity between frontend, backend, and other consumers.
    *   **Parallel Development:** Teams can work in parallel. Once the spec is agreed upon, frontend teams can build against mock servers generated from the spec while the backend is being implemented.
    *   **Rich Tooling Ecosystem:** A vast range of tools exists for generating documentation (like Swagger UI), client SDKs, and server stubs, accelerating development.
    *   **Enforced Consistency:** It forces developers to think about the API design upfront, leading to more consistent and well-structured APIs.
*   **Cons:**
    *   **Increased Complexity:** It adds a build step and can make the development loop slightly slower.
    *   **Verbosity and Rigidity:** Writing and maintaining YAML can be cumbersome. The generated code might be rigid or contain boilerplate that is hard to customize.

**Code-First (e.g., `Product` and `Store` APIs):**
*   **Pros:**
    *   **Simplicity and Speed:** It's often faster for prototyping and for small, simple APIs. Developers can work directly in Java without context-switching to YAML.
    *   **Full Control:** Developers have complete control over the implementation, annotations, and endpoint structure without being constrained by a code generator.
    *   **Spec Generation:** Modern frameworks like Quarkus can automatically generate an OpenAPI spec from the code, providing documentation without manual effort.
*   **Cons:**
    *   **Risk of Contract Drift:** The generated documentation can easily fall out of sync with the actual business contract if not carefully managed. The code is the truth, but the documented spec might lag behind.
    *   **Reactive Design:** API design can become an afterthought, potentially leading to inconsistencies across the system.

**Which would I choose and why?**
For a project of this nature, intended for a production environment with multiple services and potentially different teams, I would strongly advocate for the **API-first approach**. The benefits of a clear, enforceable contract, parallel development, and a strong tooling ecosystem far outweigh the initial setup complexity. It establishes a discipline that pays dividends in maintainability and scalability. The `Warehouse` API is a good example of this, as it's a core entity that other parts of the system will likely depend on.
```

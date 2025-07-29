# Pancake Ordering System

## Overview
This project models a virtual pancake kitchen. We can create an order, customize pancakes, and have them prepared and delivered. The system is built around clean architecture and thread-safe principles.

## Features
- Create, cancel, and complete orders
- Add and assemble pancakes ingredient by ingredient
- Validate ingredient combinations (e.g., incompatibility rules)
- Validate maximum ingredient limit per pancake
- Validate address fields (building and room)
- Enforce order state rules (only CREATED orders can be modified)
- Prepare and deliver completed orders
- Remove orders and pancakes after cancellation or delivery
- View audit log of all operations
- List ready-for-delivery orders

## Architecture
The system is composed of several service classes, each with a specific responsibility:

- Order service – manages order lifecycle: create, cancel, complete
- Pancake assembly service – builds pancakes step by step
- Chef service – prepares completed orders
- Delivery service – handles delivery and cleanup
- Order log service – records all system actions for audit and testing
- Order lookup service – provides safe read-only access to orders
- Order validation service – ensures orders are in a valid state before modification
- Ingredient validation service – checks ingredient compatibility and limits
- Location validation service – validates building and room input

Each service is injected with its dependencies and operates on immutable or thread-safe collections.

Domain objects like Pancake and Order are immutable.  
Pancakes are built step-by-step using the Builder pattern.  
Orders are created through a Factory Method pattern.

Custom exceptions handle invalid states and inputs.

## Architectural Decisions
The following decisions were made to ensure maintainability, performance, and safety in concurrent environments:

- The Pancake.Builder class uses the builder pattern and allows ingredients to be added one by one in a thread-safe manner using synchronization.
- Orders are created through a factory method (OrderFactory.create(...)), encapsulating construction logic and ensuring consistent initialization.
- All services that work with shared data structures (like draftPancakes, completedPancakes, preparedOrders) use concurrent collections, synchronization, and defensive copies to ensure safe access.
- A separate interface (OrderLookupService) is used to decouple services and avoid circular dependencies, following the Dependency Inversion Principle.
- Logging is delegated to a dedicated OrderLogService, making it easy to track important actions without mixing concerns.
- The system uses Optional with orElseThrow(...) and custom exception types to make error handling explicit and meaningful.
- Data transfer objects and models are immutable. All lists returned are unmodifiable copies to avoid accidental mutation.
- Ingredient validation logic is handled in its own service, which can easily be extended or replaced.

- The application logic was guided and validated through a comprehensive `PancakeFlowIntegrationTest`,
  which acted as a high-level safety net throughout development.
  This allowed us to follow a behavior-first approach similar to Test-Driven Development (TDD) at the integration level.  
  Given the constraint of using pure Java without any external libraries like Mockito, this approach provided the most pragmatic and reliable way to verify system correctness.


These decisions help make the system easier to test, extend, and run safely in a multi-threaded environment.

## Design Principles
- Services are thread-safe using concurrent collections, synchronization, and defensive copies.
  All shared data is either immutable or returned as unmodifiable copies to prevent race conditions.
- Domain objects are immutable and shared safely across threads
- Internal domain objects like `Order` and `Pancake` are never exposed through public APIs. Instead, the system uses `OrderView` and `PancakeView` interfaces to safely represent read-only projections for external consumers.
- All logic follows SOLID principles
- Code is modular and easy to extend

## Example Usage Flow
The following flow is fully tested in `PancakeFlowIntegrationTest`:

- Create a new order (building + room)
- Start a new pancake for that order
- Add ingredients one by one
- Finalize the pancake
- Complete the order
- Chef prepares the order
- Delivery service delivers it and cleans up


## Error Handling
The system defines clear exception types for:

- Not found cases (e.g. `OrderNotFoundException`, `PancakeNotFoundException`)
- Invalid order states (`InvalidOrderStateException`, `OrderNotReadyForDeliveryException`)
- Invalid pancake operations (`InvalidPancakeOperationException`)
- Invalid location input (`InvalidAddressException`)
- Unsafe storage injection (`OrderStoreNotThreadSafeException`)


## Technologies Used
- Java 17
- PlantUML (for architecture diagrams, stored in the `docs/` folder)
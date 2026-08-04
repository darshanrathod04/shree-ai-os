ADR-ID-002
Identity Type Enumeration

Status: 🟢 APPROVED

Applies to: Identity Kernel

Affected Sprint: EIO-ID-102

Problem

Current model:

public final class Identity {

    private final IdentityId id;
    private final String name;
    private final String type;
}

Although documentation restricts values to:

HUMAN
AGENT
PLUGIN
DEVICE
SERVICE
ORGANIZATION

the implementation still accepts any string.

Examples:

"Human"

"human"

"AI"

"robot"

"anything"

This violates the Platform Language philosophy.

Decision

Introduce

IdentityType

as an enum.

Example

public enum IdentityType {

    HUMAN,
    AGENT,
    ORGANIZATION,
    DEVICE,
    SERVICE,
    PLUGIN

}
Identity becomes
public final class Identity {

    private final IdentityId id;

    private final String name;

    private final IdentityType type;

    private final Instant createdAt;

}
CreateIdentityRequest

Replace

String type

with

IdentityType type
Benefits

✔ Compile-time safety

✔ No invalid values

✔ Cleaner SDK

✔ Better autocomplete

✔ Easier switch statements

✔ Better serialization

✔ Consistent Platform Language

Architecture Principle

Platform Language must encode business concepts as strongly typed value objects or enums whenever the valid value set is finite.

Primitive strings SHALL NOT represent finite platform concepts.
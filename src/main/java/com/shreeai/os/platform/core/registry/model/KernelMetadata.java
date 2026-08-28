package com.shreeai.os.platform.core.registry.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * <b>KernelMetadata</b>
 *
 * <p>Represents descriptive information about a Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides human-readable descriptive information for registered kernels.</li>
 *   <li>Enables platform observability through name, description, author, tags, and category.</li>
 *   <li>Records the creation timestamp to support lifecycle tracking.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> The kernel name SHALL NOT be null or blank.
 * All other fields may be empty but SHALL NOT be null.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005</p>
 *
 * @see RegisteredKernel
 */
public final class KernelMetadata {

    private final String name;
    private final String description;
    private final String author;
    private final Set<String> tags;
    private final String category;
    private final Instant createdTimestamp;

    /**
     * Constructs a new {@code KernelMetadata} with the given descriptive information.
     *
     * @param name             the kernel name (must not be null or blank)
     * @param description      the kernel description (must not be null)
     * @param author           the kernel author (must not be null)
     * @param tags             a set of tags categorizing the kernel (must not be null)
     * @param category         the kernel category (must not be null)
     * @param createdTimestamp the timestamp when this metadata was created (must not be null)
     * @throws IllegalArgumentException if {@code name} is null or blank, or any other parameter is null
     */
    public KernelMetadata(
            String name,
            String description,
            String author,
            Set<String> tags,
            String category,
            Instant createdTimestamp) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Kernel name must not be null or blank");
        }
        Objects.requireNonNull(description, "Description must not be null");
        Objects.requireNonNull(author, "Author must not be null");
        Objects.requireNonNull(tags, "Tags must not be null");
        Objects.requireNonNull(category, "Category must not be null");
        Objects.requireNonNull(createdTimestamp, "Created timestamp must not be null");

        this.name = name;
        this.description = description;
        this.author = author;
        this.tags = Set.copyOf(tags);
        this.category = category;
        this.createdTimestamp = createdTimestamp;
    }

    /**
     * Returns the kernel name.
     *
     * @return the kernel name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the kernel description.
     *
     * @return the kernel description
     */
    public String description() {
        return description;
    }

    /**
     * Returns the kernel author.
     *
     * @return the kernel author
     */
    public String author() {
        return author;
    }

    /**
     * Returns an unmodifiable set of tags categorizing the kernel.
     *
     * @return an unmodifiable set of tags
     */
    public Set<String> tags() {
        return tags;
    }

    /**
     * Returns the kernel category.
     *
     * @return the kernel category
     */
    public String category() {
        return category;
    }

    /**
     * Returns the timestamp when this metadata was created.
     *
     * @return the creation timestamp
     */
    public Instant createdTimestamp() {
        return createdTimestamp;
    }

    /**
     * Compares this {@code KernelMetadata} to the specified object for equality.
     *
     * @param o the object to compare to
     * @return {@code true} if the given object is a {@code KernelMetadata} with the same values
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KernelMetadata that = (KernelMetadata) o;
        return name.equals(that.name)
                && description.equals(that.description)
                && author.equals(that.author)
                && tags.equals(that.tags)
                && category.equals(that.category)
                && createdTimestamp.equals(that.createdTimestamp);
    }

    /**
     * Returns the hash code for this {@code KernelMetadata}.
     *
     * @return the hash code based on all fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, description, author, tags, category, createdTimestamp);
    }

    /**
     * Returns a string representation of this {@code KernelMetadata}.
     *
     * @return a string containing the kernel name and category
     */
    @Override
    public String toString() {
        return "KernelMetadata{"
                + "name='" + name + '\''
                + ", category='" + category + '\''
                + ", createdTimestamp=" + createdTimestamp
                + '}';
    }
}
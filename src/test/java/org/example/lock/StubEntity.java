package org.example.lock;

import java.util.Objects;

public class StubEntity {
    private final String text;
    private final int value;

    public StubEntity(final String text, final int value) {
        this.text = text;
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StubEntity that = (StubEntity) o;
        return value == that.value && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, value);
    }
}

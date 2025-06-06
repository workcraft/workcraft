package org.workcraft.types;

public class Triple<R, S, T> {
    private final R first;
    private final S second;
    private final T third;

    public Triple(R first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public R getFirst() {
        return first;
    }
    public S getSecond() {
        return second;
    }
    public T getThird() {
        return third;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        result = prime * result + ((third == null) ? 0 : third.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
        if (first == null) {
            if (other.first != null) {
                return false;
            }
        } else if (!first.equals(other.first)) {
            return false;
        }
        if (second == null) {
            if (other.second != null) {
                return false;
            }
        } else if (!second.equals(other.second)) {
            return false;
        }
        if (third == null) {
            if (other.third != null) {
                return false;
            }
        } else if (!third.equals(other.third)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "<" + first.toString() + ", " + second.toString() + ", " + third.toString() + ">";
    }

    public static <R, S, T> Triple<R, S, T> of(R first, S second, T third) {
        return new Triple<>(first, second, third);
    }
}

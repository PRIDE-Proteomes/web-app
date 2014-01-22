package uk.ac.ebi.pride.proteomes.web.client.utils;

/**
 * Provides a lightweight implementation for a pair.
 * Note that it's fields can be changed if the classes aren't final.
 * (I'd do a clone in the constructor, but GWT doesn't emulate it)
 * @param <A>
 * @param <B>
 */
public final class Pair<A, B> {
    private final A a;
    private final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || o.getClass() != this.getClass())
            return false;

        Pair other = (Pair) o;
        if(other.getA() == null || other.getB() == null)
            return false;

        return other.getA().equals(getA()) && other.getB().equals(getB());
    }

    @Override
    public int hashCode() {
        return getA().hashCode() ^ getB().hashCode();
    }
}

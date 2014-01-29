package uk.ac.ebi.pride.proteomes.web.client.utils;

/**
 * Provides a lightweight implementation for a pair.
 * Note that it's fields can be changed if the classes aren't final.
 * (I'd do a clone in the constructor, but GWT doesn't emulate it)
 * @param <A>
 * @param <B>
 * @param <C>
 */
public final class Triplet<A,B,C> {
    private final A a;
    private final B b;
    private final C c;

    public Triplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || o.getClass() != this.getClass())
            return false;

        Triplet other = (Triplet) o;
        if(other.getA() == null || other.getB() == null || other.getC() == null)
            return false;

        return other.getA().equals(getA()) && other.getB().equals(getB()) && other.getC().equals(getC());
    }

    @Override
    public int hashCode() {
        return getA().hashCode() ^ getB().hashCode() ^ getC().hashCode();
    }
}

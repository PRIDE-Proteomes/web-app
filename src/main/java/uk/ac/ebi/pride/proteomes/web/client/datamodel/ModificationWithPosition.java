package uk.ac.ebi.pride.proteomes.web.client.datamodel;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalModificationPositionException;

import java.util.Objects;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:07
 */
public class ModificationWithPosition implements ModifiedLocation {

    private static class EmptyModificationWithPosition extends ModificationWithPosition {
        EmptyModificationWithPosition() {
        }

        @Override
        public int getPosition() {
            return 0;
        }

        @Override
        public String getModification() {
            return "";
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }

    private static volatile EmptyModificationWithPosition emptyModificationWithPosition = new EmptyModificationWithPosition();
    private int position;
    private String modification;

    private ModificationWithPosition() {}


    public ModificationWithPosition(String modification, int postion) throws IllegalModificationPositionException  {
        if (postion < 0 || (!(this instanceof EmptyModificationWithPosition) && postion <= 0 && (modification == null || modification.equals("")))) {
            throw new IllegalModificationPositionException();
        }

        this.position = postion;
        this.modification = modification;
    }

    public static ModificationWithPosition tokenize(String modificationId) throws
            IllegalModificationPositionException,
            NumberFormatException,
            IndexOutOfBoundsException {
        String[] startEnd = modificationId.split("-");
        return new ModificationWithPosition(startEnd[1], Integer.parseInt(startEnd[0]));
    }

    public static EmptyModificationWithPosition emptyModificationWithPosition() {
        return emptyModificationWithPosition;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public String getModification() {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return modification + "-" + Integer.toString(position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModificationWithPosition)) return false;
        ModificationWithPosition that = (ModificationWithPosition) o;
        return position == that.position &&
                Objects.equals(modification, that.modification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, modification);
    }
}

package uk.ac.ebi.pride.proteomes.web.client;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 03/12/13
 *         Time: 14:41
 */
public class UserAction {
    private static volatile UserAction emptyAction = new UserAction(Type
            .none, "None, this is most probably the cause of a bug");
    private final String name;
    private final Type type;

    public enum Type {
        group, protein, region, peptide, modification, modificationWithPos, tissue, peptiform, none
    }

    public UserAction(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type.toString();
    }

    public String getName() {
        return name;
    }

    public static UserAction emptyAction() {
        return emptyAction;
    }
}

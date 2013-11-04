package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 14:15
 */
public class ProteinUpdateEvent extends
        GwtEvent<ProteinUpdateEvent.ProteinUpdateHandler> {

public interface ProteinUpdateHandler extends EventHandler {
    public void onProteinUpdateEvent(ProteinUpdateEvent event);
}

private static final GwtEvent.Type<ProteinUpdateHandler> TYPE = new GwtEvent.Type<ProteinUpdateHandler>();

private List<Protein> ProteinList;

    public ProteinUpdateEvent(List<Protein> Proteins, HasHandlers source) {
        super();
        ProteinList = Proteins;
        setSource(source);
    }

    public static void fire(HasHandlers source, List<Protein> Proteins) {
        ProteinUpdateEvent eventInstance = new ProteinUpdateEvent(Proteins, source);
        source.fireEvent(eventInstance);
    }

    public List<Protein> getProteins() {
        return ProteinList;
    }

    public static GwtEvent.Type<ProteinUpdateHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<ProteinUpdateHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ProteinUpdateHandler handler) {
        handler.onProteinUpdateEvent(this);
    }
}

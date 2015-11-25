package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:13
 */
public class PeptiformUpdateEvent extends GwtEvent<PeptiformUpdateEvent.Handler> {
    public interface Handler extends EventHandler {
        public void onPeptiformUpdateEvent(PeptiformUpdateEvent event);
    }

    private static final Type<Handler> TYPE = new Type<>();

    private final List<Peptide> peptiforms;

    private PeptiformUpdateEvent(List<Peptide> peptiforms, HasHandlers source) {
        super();
        this.peptiforms = peptiforms;
        setSource(source);
    }

    public static void fire(HasHandlers source, List<Peptide> peptiforms) {
        PeptiformUpdateEvent eventInstance = new PeptiformUpdateEvent(peptiforms, source);
        source.fireEvent(eventInstance);
    }

    public List<Peptide> getPeptiforms() {
        return peptiforms;
    }

    public static Type<Handler> getType() {
        return TYPE;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onPeptiformUpdateEvent(this);
    }
}
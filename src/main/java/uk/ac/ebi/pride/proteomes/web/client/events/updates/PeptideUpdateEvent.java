package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithPeptiforms;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 14:15
 */
public class PeptideUpdateEvent extends GwtEvent<PeptideUpdateEvent.Handler> {
    public interface Handler extends EventHandler {
        void onPeptideUpdateEvent(PeptideUpdateEvent event);
    }

    private static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<>();

    private final List<PeptideWithPeptiforms> peptiformses;

    private PeptideUpdateEvent(List<PeptideWithPeptiforms> peptides, HasHandlers source) {
        super();
        peptiformses = peptides;
        setSource(source);
    }

    public static void fire(HasHandlers source, List<PeptideWithPeptiforms> peptiforms) {
        PeptideUpdateEvent eventInstance = new PeptideUpdateEvent(peptiforms, source);
        source.fireEvent(eventInstance);
    }

    /**
     *
     * @return the peptiforms of the peptides that got selected
     */
    public List<PeptideWithPeptiforms> getPeptides() {
        return peptiformses;
    }

    public static GwtEvent.Type<Handler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onPeptideUpdateEvent(this);
    }
}

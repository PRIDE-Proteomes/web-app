package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideList;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 14:15
 */
public class PeptideUpdateEvent extends GwtEvent<PeptideUpdateEvent.Handler> {
    public interface Handler extends EventHandler {
        public void onPeptideUpdateEvent(PeptideUpdateEvent event);
    }

    private static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<Handler>();

    private List<PeptideList> peptideVariances;

    public PeptideUpdateEvent(List<PeptideList> peptides, HasHandlers source) {
        super();
        peptideVariances = peptides;
        setSource(source);
    }

    public static void fire(HasHandlers source, List<PeptideList> variances) {
        PeptideUpdateEvent eventInstance = new PeptideUpdateEvent(variances, source);
        source.fireEvent(eventInstance);
    }

    /**
     *
     * @return the variances of the peptides that got selected
     */
    public List<PeptideList> getPeptides() {
        return peptideVariances;
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

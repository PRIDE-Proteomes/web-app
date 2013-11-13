package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 14:15
 */
public class PeptideUpdateEvent extends
        GwtEvent<PeptideUpdateEvent.PeptideUpdateHandler> {

    public interface PeptideUpdateHandler extends EventHandler {
        public void onPeptideUpdateEvent(PeptideUpdateEvent event);
    }

    private static final GwtEvent.Type<PeptideUpdateHandler> TYPE = new GwtEvent.Type<PeptideUpdateHandler>();

    private List<Peptide> peptideList;
    private List<PeptideMatch> matchList;

    public PeptideUpdateEvent(List<PeptideMatch> matches,
                              List<Peptide> peptides,
                              HasHandlers source) {
        super();
        matchList = matches;
        peptideList = peptides;
        setSource(source);
    }

    public static void fire(HasHandlers source, List<PeptideMatch> matches) {
        PeptideUpdateEvent eventInstance = new PeptideUpdateEvent
                (matches, new ArrayList<Peptide>(), source);
        source.fireEvent(eventInstance);
    }

    public static void fire(HasHandlers source, List<PeptideMatch> matches,
                            List<Peptide> peptides) {
        PeptideUpdateEvent eventInstance = new PeptideUpdateEvent
                (matches, peptides, source);
        source.fireEvent(eventInstance);
    }

    /**
     *
     * @return the peptide matches in the protein(s) that just got updated
     */
    public List<PeptideMatch> getPeptides() {
        return matchList;
    }

    /**
     *
     * @return the variances of the peptides that got selected
     */
    public List<Peptide> getVariances() {
        return peptideList;
    }

    public static GwtEvent.Type<PeptideUpdateHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<PeptideUpdateHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PeptideUpdateHandler handler) {
        handler.onPeptideUpdateEvent(this);
    }
}

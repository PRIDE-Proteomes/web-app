package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.*;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 08/11/13
 *         Time: 14:43
 */
public class CoveragePresenter implements Presenter,
                                          GroupUpdateEvent.GroupUpdateHandler,
                                          ProteinUpdateEvent.ProteinUpdateHandler,
                                          ProteinRequestEvent.ProteinRequestHandler,
                                          RegionUpdateEvent.RegionUpdateHandler,
                                          PeptideUpdateEvent.PeptideUpdateHandler,
                                          ModificationUpdateEvent.ModificationUpdateHandler,
                                          CoverageUiHandler
{
    private Protein currentProtein;

    public interface View extends uk.ac.ebi.pride.proteomes.web.client.modules.View {
        public void updateProtein(ProteinAdapter protein);
        public void updateRegionSelection(int start, int end);
        public void resetRegionSelection();
        public void updatePeptideSelection(PeptideAdapter peptide);
        public void resetPeptideSelection();
        public void updateModificationHighlight(ModificationAdapter mod);
        public void resetModificationHighlight();
        public void displayLoadingMessage();
    }

    private final EventBus eventBus;
    private final View view;
    private boolean groups = true;

    public CoveragePresenter(EventBus eventBus, View view) {
        this.eventBus = eventBus;
        this.view = view;

        eventBus.addHandler(GroupUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        view.bindToContainer(container);
    }

    @Override
    public void onGroupUpdateEvent(GroupUpdateEvent event) {
        if(event.getGroups().size() > 0) {
            groups = true;
            view.asWidget().setVisible(false);
        }
        else {
            groups = false;
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        if(!groups && event.getProteins().size() > 0) {
            currentProtein = event.getProteins().get(0);
            view.updateProtein(new ProteinAdapter(currentProtein));
            view.setVisible(true);
        }
        else if(!groups) {
            // what should we do here? put a blank view?
        }
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        view.displayLoadingMessage();
    }

    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {
        if(event.getModifications().length > 0) {
            view.updateModificationHighlight(
                    new ModificationAdapter(event.getModifications()[0]));
        }
        else {
            view.resetModificationHighlight();
        }
    }

    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        if(event.getPeptides().size() > 0) {
            view.updatePeptideSelection(new PeptideAdapter(event.getPeptides
                    ().get(0)));
        }
        else {
            view.resetPeptideSelection();
        }
    }

    @Override
    public void onRegionUpdateEvent(RegionUpdateEvent event) {
        if(event.getRegions().size() > 0) {
            Region region = event.getRegions().get(0);
            view.updateRegionSelection(region.getStart(), region.getEnd());
        }
        else {
            view.resetRegionSelection();
        }
    }
}

package uk.ac.ebi.pride.proteomes.web.client.modules.sequence;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.modules.ViewWithUiHandlers;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.ModuleContainerFactory;
import uk.ac.ebi.pride.widgets.client.disclosure.client.ModuleContainer;
import uk.ac.ebi.pride.widgets.client.sequence.client.SequenceViewer;
import uk.ac.ebi.pride.widgets.client.sequence.events.ProteinRegionHighlightedEvent;
import uk.ac.ebi.pride.widgets.client.sequence.events.ProteinRegionSelectionEvent;
import uk.ac.ebi.pride.widgets.client.sequence.handlers.ProteinRegionHighlightedHandler;
import uk.ac.ebi.pride.widgets.client.sequence.handlers.ProteinRegionSelectedHandler;
import uk.ac.ebi.pride.widgets.client.sequence.type.Pride;
import uk.ac.ebi.pride.widgets.client.sequence.type.SequenceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 27/11/13
 *         Time: 10:58
 */
public class SequenceView extends ViewWithUiHandlers<SequenceUiHandler>
                          implements SequencePresenter.ThisView,
                                     ProteinRegionSelectedHandler,
                                     ProteinRegionHighlightedHandler {

    private HTMLPanel panel;
    private ModuleContainer outerBox;
    private SequenceViewer sequence;
    private Map<ProteinAdapter, SequenceViewer> viewersCache;

    public SequenceView() {
        viewersCache = new HashMap<ProteinAdapter, SequenceViewer>();

        outerBox = ModuleContainerFactory.getModuleContainer("Protein " +
                                                             "Sequence");
        panel = new HTMLPanel("");
        outerBox.setWidth("100%");
        outerBox.setContent(panel);
        outerBox.setOpen(true);
    }

    @Override
    public void updateProtein(ProteinAdapter protein) {
        if(!viewersCache.containsKey(protein)) {
            SequenceType type = new Pride();
            SequenceViewer viewer = new SequenceViewer(type, protein);
            bindViewer(viewer);
            viewersCache.put(protein, viewer);
        }
        sequence = viewersCache.get(protein);
        panel.clear();
        panel.add(sequence);
        outerBox.setContent(panel);
    }

    @Override
    public void updateRegionSelection(int start, int end) {
        sequence.selectRegion(start, end);
    }

    @Override
    public void resetRegionSelection() {
        sequence.resetSelection();
    }

    @Override
    public void updatePeptideSelection(List<PeptideAdapter> peptideSelection) {
        for(PeptideAdapter peptide : peptideSelection) {
            sequence.setVisiblePeptide(peptide);
        }
    }

    @Override
    public void resetPeptideSelection() {
        sequence.resetVisiblePeptides();
    }

    @Override
    public void updateModificationHighlight(ModificationAdapter mod) {
        sequence.filterModification(mod);
    }

    @Override
    public void resetModificationHighlight() {
        sequence.resetModification();
    }

    @Override
    public void displayLoadingMessage() {
        outerBox.setContent(ModuleContainer.getLoadingPanel());
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        container.setWidget(outerBox);
    }

    @Override
    public void setVisible(boolean visible) {
        asWidget().setVisible(visible);
    }

    @Override
    public Widget asWidget() {
        return outerBox;
    }

    @Override
    public void onProteinRegionHighlighted(ProteinRegionHighlightedEvent e) {
        for(SequenceUiHandler handler : getUiHandlers()) {
            handler.onRegionHighlighted(e);
        }
    }

    @Override
    public void onProteinRegionSelectionChanged(ProteinRegionSelectionEvent e) {
        for(SequenceUiHandler handler : getUiHandlers()) {
            handler.onRegionSelected(e);
        }
    }


    private void bindViewer(SequenceViewer viewer) {
        viewer.addProteinRegionSelectedHandler(this);
        viewer.addProteinRegionHighlightedHandler(this);
    }
}

package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.modules.ViewWithUiHandlers;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.ModuleContainerFactory;
import uk.ac.ebi.pride.widgets.client.disclosure.client.ModuleContainer;
import uk.ac.ebi.pride.widgets.client.protein.client.ProteinViewer;
import uk.ac.ebi.pride.widgets.client.protein.events.*;
import uk.ac.ebi.pride.widgets.client.protein.handlers.*;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 12:10
 */
public class CoverageView extends ViewWithUiHandlers<CoverageUiHandler>
                          implements CoveragePresenter.ThisView,
                                     ProteinAreaSelectedHandler,
                                     ProteinAreaHighlightedHandler,
                                     ProteinRegionSelectedHandler,
                                     ProteinRegionHighlightedHandler,
                                     PeptideSelectedHandler,
                                     ModificationSelectedHandler {
    private HTMLPanel panel;
    private ModuleContainer outerBox;
    private ProteinViewer coverage;
    private Map<ProteinAdapter, ProteinViewer> viewersCache;

    public CoverageView() {
        viewersCache = new HashMap<ProteinAdapter, ProteinViewer>();

        outerBox = ModuleContainerFactory.getModuleContainer("Protein " +
                                                             "Coverage");
        panel = new HTMLPanel("");
        outerBox.setWidth("100%");
        outerBox.setContent(panel);
        outerBox.setOpen(true);
    }

    @Override
    public void updateProtein(ProteinAdapter protein) {
        if(!viewersCache.containsKey(protein)) {
            ProteinViewer viewer = new ProteinViewer(1020, 90, protein);
            bindViewer(viewer);
            viewersCache.put(protein, viewer);
        }
        coverage = viewersCache.get(protein);
        panel.clear();
        panel.add(coverage);
        outerBox.setContent(panel);
    }

    @Override
    public void updateRegionSelection(int start, int end) {
        coverage.setSelectedArea(start, end);
    }

    @Override
    public void resetRegionSelection() {
        coverage.resetRegionSelection();
        coverage.resetSelectedArea();
        coverage.resetModificationSelection();
    }

    @Override
    public void updatePeptideSelection(List<PeptideAdapter> peptideSelection) {
        coverage.setSelectedPeptide(peptideSelection.get(0));
    }

    @Override
    public void resetPeptideSelection() {
        coverage.resetPeptideSelection();
    }

    @Override
    public void updateModificationHighlight(ModificationAdapter mod) {
        coverage.setHighlightedModifications(mod);
    }

    @Override
    public void updateModificationHighlight(int start, int end) {
        coverage.selectModificationsBetween(start, end);
    }

    @Override
    public void resetModificationHighlight() {
        coverage.resetModificationHighlight();
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

    /**
     * This function gets called whenever the user is doing a
     * selection by dragging the mouse over the coverage.
     * @param e event containing the region that got selected.
     */
    @Override
    public void onProteinAreaSelected(ProteinAreaSelectedEvent e) {
        for(CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionDragSelected(e);
        }
    }

    @Override
    public void onProteinRegionSelectionChanged(ProteinRegionSelectionEvent e) {
        for(CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionClickSelected(e);
        }
    }

    @Override
    public void onProteinAreaHighlighted(ProteinAreaHighlightEvent e) {
        for(CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionDragHighlighted(e);
        }
    }

    @Override
    public void onProteinRegionHighlighted(ProteinRegionHighlightEvent e) {
        for(CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionClickHighlighted(e);
        }
    }

    @Override
    public void onPeptideSelected(PeptideSelectedEvent e) {
        for(CoverageUiHandler handler : getUiHandlers()) {
            handler.onPeptideSelected(e);
        }
    }

    @Override
    public void onModificationSelected(ModificationSelectedEvent e) {
        for(CoverageUiHandler handler : getUiHandlers()) {
            handler.onModificationSelected(e);
        }
    }

    private void bindViewer(ProteinViewer viewer) {
        viewer.addProteinAreaSelectedHandler(this);
        viewer.addProteinAreaHighlightedHandler(this);

        viewer.addProteinRegionSelectedHandler(this);
        viewer.addProteinRegionHighlightedHandler(this);

        viewer.addPeptideSelectedHandler(this);

        viewer.addModificationSelectedHandler(this);
    }
}

package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
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
public class CoverageView implements CoveragePresenter.View, ProteinAreaSelectedHandler, ProteinAreaHighlightedHandler, ProteinRegionHighlightedHandler, PeptideSelectedHandler, ModificationSelectedHandler, ModificationHighlightedHandler {
    private HTMLPanel panel;
    private ModuleContainer outerBox;
    private ProteinViewer coverage;
    private Map<ProteinAdapter, ProteinViewer> viewersCache;

    public CoverageView() {
        viewersCache = new HashMap<ProteinAdapter, ProteinViewer>();

        panel = new HTMLPanel("");
        outerBox = ModuleContainerFactory.getModuleContainer("Protein " +
                                                             "Coverage");
        outerBox.setWidth("100%");
        outerBox.setContent(panel);
        outerBox.setOpen(true);
    }

    @Override
    public void updateProtein(ProteinAdapter protein) {
        if(!viewersCache.containsKey(protein)) {
            ProteinViewer viewer = new ProteinViewer(950, 90, protein);
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resetRegionSelection() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updatePeptideSelection(List<PeptideAdapter> peptideSelection) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resetPeptideSelection() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateModificationHighlight(ModificationAdapter mod) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resetModificationHighlight() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void displayLoadingMessage() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addUiHandler(Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection getUiHandlers() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
    public void onProteinAreaSelected(ProteinAreaSelectedEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onProteinAreaHighlighted(ProteinAreaHighlightEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onProteinRegionHighlighted(ProteinRegionHighlightEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onPeptideSelected(PeptideSelectedEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onModificationSelected(ModificationSelectedEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onModificationHighlighted(ModificationHighlightedEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private void bindViewer(ProteinViewer viewer) {
        viewer.addProteinAreaSelectedHandler(this);
        viewer.addProteinAreaHighlightedHandler(this);
        viewer.addProteinRegionHighlightedHandler(this);

        viewer.addPeptideSelectedHandler(this);

        viewer.addModificationSelectedHandler(this);
        viewer.addModificationHighlightedHandler(this);
    }
}

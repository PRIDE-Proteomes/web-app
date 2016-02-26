package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.modules.ViewWithUiHandlers;
import uk.ac.ebi.pride.proteomes.web.client.modules.legend.LegendView;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.ModuleContainerFactory;
import uk.ac.ebi.pride.widgets.client.common.handler.PeptideHandler;
import uk.ac.ebi.pride.widgets.client.disclosure.client.ModuleContainer;
import uk.ac.ebi.pride.widgets.client.feature.client.FeatureViewer;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureAreaHighlightEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureAreaSelectionEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureRegionHighlightEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureRegionSelectionEvent;
import uk.ac.ebi.pride.widgets.client.feature.handlers.FeatureAreaHighlightedHandler;
import uk.ac.ebi.pride.widgets.client.feature.handlers.FeatureAreaSelectedHandler;
import uk.ac.ebi.pride.widgets.client.feature.handlers.FeatureRegionHighlightedHandler;
import uk.ac.ebi.pride.widgets.client.feature.handlers.FeatureRegionSelectedHandler;
import uk.ac.ebi.pride.widgets.client.protein.client.ProteinViewer;
import uk.ac.ebi.pride.widgets.client.protein.events.*;
import uk.ac.ebi.pride.widgets.client.protein.handlers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        ModificationSelectedHandler,
        FeatureAreaHighlightedHandler,
        FeatureAreaSelectedHandler,
        FeatureRegionHighlightedHandler,
        FeatureRegionSelectedHandler

{

    Logger logger = Logger.getLogger(CoverageView.class.getName());

    public static final int WIDTH = 982; //Same with than sequence viewer to align the widgets
    public static final int PW_HEIGHT = 90;
    public static final int FW_HEIGHT = 20; //Optimize to 20

    private HTMLPanel panel;
    private ModuleContainer outerBox;
    private ProteinViewer coverageViewer;
    private FeatureViewer featureViewer;

    private Map<ProteinAdapter, ProteinViewer> coverageVwCache;
    private Map<ProteinAdapter, FeatureViewer> featureVwCache;


    public CoverageView() {
        coverageVwCache = new HashMap<>();
        featureVwCache = new HashMap<>();

        outerBox = ModuleContainerFactory.getModuleContainer("Protein " + "Coverage");
        panel = new HTMLPanel("");
        outerBox.setWidth("100%");
        outerBox.setContent(panel);
        outerBox.setOpen(true);

        //The duration of the initial animation of the coverageViewer widget
        ProteinViewer.setAnimationDuration(1000);
        FeatureViewer.setAnimationDuration(1000);
    }

    @Override
    public void updateProtein(ProteinAdapter protein) {
        if (!coverageVwCache.containsKey(protein) || !featureVwCache.containsKey(protein)) {
            ProteinViewer proteinViewer = new ProteinViewer(WIDTH, PW_HEIGHT, protein);
            FeatureViewer featureViewer = new FeatureViewer(WIDTH, FW_HEIGHT, protein);

            bindCoverageViewer(proteinViewer);
            bindFeatureViewer(featureViewer);

            coverageVwCache.put(protein, proteinViewer);
            featureVwCache.put(protein, featureViewer);

        }
        coverageViewer = coverageVwCache.get(protein);
        featureViewer = featureVwCache.get(protein);

        panel.clear();
        panel.add(coverageViewer);

        //We add the viewer if we have features to display
        if(protein.getFeatures()!= null && !protein.getFeatures().isEmpty()) {
            panel.add(featureViewer);
        }
        panel.add(new LegendView());

        outerBox.setContent(panel);
    }

    @Override
    public void updateRegionSelection(int start, int end) {
        coverageViewer.setSelectedArea(start, end);
        featureViewer.setSelectedArea(start, end);
    }

    @Override
    public void resetRegionSelection() {
        coverageViewer.resetRegionSelection();
        coverageViewer.resetSelectedArea();
        coverageViewer.resetModificationSelection();

        featureViewer.resetRegionSelection();
        featureViewer.resetSelectedArea();
    }

    @Override
    public void updatePeptideSelection(List<PeptideAdapter> peptideSelection) {
        coverageViewer.setSelectedPeptide(peptideSelection.get(0));
    }

    @Override
    public void resetPeptideSelection() {
        coverageViewer.resetPeptideSelection();
    }

    @Override
    public void updateModificationHighlight(ModificationAdapter mod) {
        coverageViewer.setHighlightedModifications(mod);
    }

    @Override
    public void updateModificationHighlight(int start, int end) {
        coverageViewer.highlightModificationsBetween(start, end);
    }

    @Override
    public void resetModificationHighlight() {
        coverageViewer.resetModificationHighlight();
    }

    @Override
    public void resetModWithPosSelection() {
        coverageViewer.resetModificationSelection();
    }

    @Override
    public void updateModWithPosSelection(Integer position) {
        coverageViewer.selectModificationsBetween(position, position);
    }

    @Override
    public void updatePeptideHighlight(List<PeptideAdapter> peptides) {
        coverageViewer.setHighlightedPeptides(new ArrayList<PeptideHandler>(peptides));
    }

    @Override
    public void resetPeptideHighlight() {
        coverageViewer.resetPeptideHighlight();
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
     * selection by dragging the mouse over the coverageViewer.
     *
     * @param e event containing the region that got selected.
     */
    //Protein
    @Override
    public void onProteinAreaSelected(ProteinAreaSelectedEvent e) {
        logger.log(Level.INFO, "ProteinAreaSelectedEvent " + e);

        for (CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionDragSelected(e);
        }
    }

    @Override
    public void onProteinRegionSelectionChanged(ProteinRegionSelectionEvent e) {
        logger.log(Level.INFO, "ProteinRegionSelectionEvent " + e);

        for (CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionClickSelected(e);
        }
    }


    @Override
    public void onFeatureRegionSelectionChanged(FeatureRegionSelectionEvent e) {
        logger.log(Level.INFO, "FeatureRegionSelectionEvent " + e);

        for (CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionClickSelected(e);
        }
    }

    @Override
    public void onProteinAreaHighlighted(ProteinAreaHighlightEvent e) {
        logger.log(Level.INFO, "ProteinAreaHighlightEvent " + e);

        for (CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionDragHighlighted(e);
        }
    }

    @Override
    public void onProteinRegionHighlighted(ProteinRegionHighlightEvent e) {
        logger.log(Level.INFO, "ProteinRegionHighlightEvent " + e);

        for (CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionClickHighlighted(e);
        }
    }

    //Peptides
    @Override
    public void onPeptideSelected(PeptideSelectedEvent e) {
        logger.log(Level.INFO, "PeptideSelectedEvent " + e);

        for (CoverageUiHandler handler : getUiHandlers()) {
            handler.onPeptideSelected(e);
        }
    }

    //Modifications with position (triangles)
    @Override
    public void onModificationSelected(ModificationSelectedEvent e) {
        logger.log(Level.INFO, "ModificationSelectedEvent " + e);

        for (CoverageUiHandler handler : getUiHandlers()) {
            handler.onModificationSelected(e);
        }
    }

    //Feature
    @Override
    public void onFeatureRegionHighlighted(FeatureRegionHighlightEvent e) {
        logger.log(Level.INFO, "FeatureRegionHighlightEvent " + e);

        for (CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionClickHighlighted(e);
        }

    }

    @Override
    public void onFeatureAreaHighlighted(FeatureAreaHighlightEvent e) {
        logger.log(Level.INFO, "FeatureAreaHighlightEvent " + e);

        for (CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionDragHighlighted(e);
        }
    }

    @Override
    public void onFeatureAreaSelected(FeatureAreaSelectionEvent e) {
        logger.log(Level.INFO, "FeatureAreaSelectionEvent " + e);

        for (CoverageUiHandler handler : getUiHandlers()) {
            handler.onRegionDragSelected(e);
        }
    }


    private void bindCoverageViewer(ProteinViewer viewer) {
        viewer.addProteinAreaSelectedHandler(this);
        viewer.addProteinAreaHighlightedHandler(this);

        viewer.addProteinRegionSelectedHandler(this);
        viewer.addProteinRegionHighlightedHandler(this);

        viewer.addPeptideSelectedHandler(this);
        viewer.addModificationSelectedHandler(this);
    }

    private void bindFeatureViewer(FeatureViewer viewer) {
        viewer.addFeatureRegionSelectedHandler(this);
        viewer.addFeatureRegionHighlightedHandler(this);

        viewer.addFeatureAreaSelectedHandler(this);
        viewer.addFeatureAreaHighlightedHandler(this);

    }

}

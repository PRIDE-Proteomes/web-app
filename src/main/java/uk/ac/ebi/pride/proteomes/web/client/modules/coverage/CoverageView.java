package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;

import java.util.Collection;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 12:10
 */
public class CoverageView implements CoveragePresenter.View {

    public CoverageView() {

    }

    @Override
    public void updateProtein(ProteinAdapter protein) {
        //To change body of implemented methods use File | Settings | File Templates.
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
    public void updatePeptideSelection(PeptideAdapter peptide) {
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

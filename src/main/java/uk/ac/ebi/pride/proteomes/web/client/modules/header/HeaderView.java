package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import uk.ac.ebi.pride.proteomes.web.client.style.Resources;
import uk.ac.ebi.pride.proteomes.web.client.utils.Pair;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.HyperlinkFactory;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.ModuleContainerFactory;
import uk.ac.ebi.pride.widgets.client.disclosure.client.ModuleContainer;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 07/11/13
 *         Time: 12:08
 */
public class HeaderView implements HeaderPresenter.ThisView {

    private static HeaderUiBinder ourUiBinder = GWT.create(HeaderUiBinder.class);
    @UiField
    Anchor title;
    @UiField
    Anchor geneGroupLink;
    @UiField
    InlineLabel uniquePeptideToProteinCount;
    @UiField
    InlineLabel uniquePeptideToGeneCount;
    @UiField
    InlineLabel nonUniquePeptidesCount;
    @UiField
    Label altId;
//    @UiField
//    Label name;
    @UiField
    Label species;
    @UiField
    Label geneSymbol;
    @UiField
    Label proteinEvidence;
    @UiField
    Label genes;
    @UiField
    Label description;
    @UiField
    FlowPanel attributes;
    @UiField
    HTMLPanel panel;
    @UiField
    HTMLPanel summaryPanel;
    @UiField
    HTMLPanel groupSummaryPanel;
    @UiField
    HTMLPanel proteinSummaryPanel;
    @UiField
    HTMLPanel uniqueToProteinBox;
    @UiField
    HTMLPanel uniqueToGeneBox;
    @UiField
    HTMLPanel nonUniqueBox;

    private HTMLPanel root;
    private ModuleContainer outerBox;

    public HeaderView() {

        Resources.INSTANCE.style().ensureInjected();
        root = ourUiBinder.createAndBindUi(this);
        //Disclosure panel
        outerBox = ModuleContainerFactory.getModuleContainer("Summary");
        outerBox.setWidth("100%");
        outerBox.setContent(panel);
        outerBox.setOpen(true);
        outerBox.setVisible(true);
        root.add(outerBox);

    }

    @Override
    public void updateTitle(String title, String accession, String link) {
        panel.clear();
        panel.add(summaryPanel);

        if (link != null) {
            this.title.setHref(link + accession);
            this.title.setTitle("Go to UniProtKB");
        }
        this.title.setText(title);
        outerBox.setContent(panel);
    }

    @Override
    public void updateGeneGroupLink(String geneGroupId) {
        if (geneGroupId != null && !geneGroupId.isEmpty()) {
            geneGroupLink.setHref("#group=" + geneGroupId);
            geneGroupLink.setText("Gene");
            geneGroupLink.setTitle("Go To Gene of the Protein");
            uniqueToGeneBox.setVisible(true);
        } else {
            geneGroupLink.setVisible(false);
            uniqueToGeneBox.setVisible(false);
        }
    }

    @Override
    public void updateUniquePeptideToProteinCount(int count) {
        if (count != 0) {
            uniquePeptideToProteinCount.setText(String.valueOf(count));
            uniqueToProteinBox.setVisible(true);
        } else {
            uniqueToProteinBox.setVisible(false);
        }
    }

    @Override
    public void updateUniquePeptideToGeneCount(int count) {
        if (count != 0) {
            uniquePeptideToGeneCount.setText(String.valueOf(count));
            uniqueToGeneBox.setVisible(true);
        } else {
            uniqueToGeneBox.setVisible(false);
        }
    }

    @Override
    public void updateNonUniquePeptidesCount(int count) {
        if (count != 0) {
            nonUniquePeptidesCount.setText(String.valueOf(count));
            nonUniqueBox.setVisible(true);
        } else {
            nonUniqueBox.setVisible(false);
        }
    }

//    @Override
//    public void updateName(String name) {
//        this.name.setText(name);
//    }

    @Override
    public void updateGeneSymbol(String geneSymbol) {
        this.geneSymbol.setText(geneSymbol);
    }

    @Override
    public void updateAlternativeName(String alternativeName) {
        this.altId.setText(alternativeName);
    }

    @Override
    public void updateSpecies(String species) {
        this.species.setText(species);
    }

    @Override
    public void updateProteinEvidence(String proteinEvidence) {
        this.proteinEvidence.setText(proteinEvidence);
    }

    @Override
    public void updateDescription(String description) {
        this.description.setText(description);
    }

    @Override
    public void updateProperties(List<Pair<String, String>> links) {
        clearProperties();
        summaryPanel.clear();
        summaryPanel.add(groupSummaryPanel);

        for (Pair<String, String> link : links) {
            attributes.add(HyperlinkFactory.getInlineHyperLink(link.getA(), link.getB()));
            if (link != links.get(links.size() - 1)) {
                attributes.add(new InlineLabel(", "));
            }
        }
    }

    @Override
    public void clearProperties() {
        attributes.clear();
        summaryPanel.clear();
        summaryPanel.add(proteinSummaryPanel);
    }

    @Override
    public void clearTitle() {
        title.setText("");
        title.setHref("");
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        container.setWidget(root);
    }

    @Override
    public Widget asWidget() {
        return root;
    }

    @Override
    public void setVisible(boolean visible) {
        asWidget().setVisible(visible);
    }

    @Override
    public void displayLoadingMessage() {
        outerBox.setContent(ModuleContainer.getLoadingPanel());
    }

    @UiTemplate("HeaderView.ui.xml")
    interface HeaderUiBinder extends UiBinder<HTMLPanel, HeaderView> {
    }
}

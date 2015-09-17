package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import uk.ac.ebi.pride.proteomes.web.client.resources.Resources;
import uk.ac.ebi.pride.proteomes.web.client.utils.Pair;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.HyperlinkFactory;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 07/11/13
 *         Time: 12:08
 */
public class HeaderView implements HeaderPresenter.ThisView {

    @UiTemplate("HeaderView.ui.xml")
    interface HeaderUiBinder extends UiBinder<FlowPanel, HeaderView> {
    }

    private static HeaderUiBinder ourUiBinder = GWT.create(HeaderUiBinder.class);

    @UiField
    Anchor title;
    @UiField
    Anchor upGroupLink;
    @UiField
    Anchor geneGroupLink;

    @UiField
    InlineLabel uniquePeptideCount;

    @UiField
    Label altId;
    @UiField
    Label name;
    @UiField
    Label species;
    @UiField
    Label geneSymbol;
    @UiField
    Label proteinEvidence;

    @UiField
    FlowPanel attributes;

    private FlowPanel root;

    public HeaderView() {
        root = ourUiBinder.createAndBindUi(this);
        Resources.INSTANCE.style().ensureInjected();
    }

    @Override
    public void updateTitle(String title, String accession, String link) {
        if (link != null) {
            this.title.setHref(link + accession);
        }
        this.title.setText(title);
        this.title.setTitle("Go to UniProt");
    }

    @Override
    public void updateUpGroupLink(String upGroupId) {
        if (upGroupId.contains("-")) {
            upGroupId = upGroupId.substring(0, upGroupId.indexOf("-"));
        }
        this.upGroupLink.setHref("#group=" + upGroupId);
        this.upGroupLink.setText("Go to UniProt entry group");
    }

    @Override
    public void updateGeneGroupLink(String geneGroupId) {
        if (geneGroupId != null && !geneGroupId.isEmpty()) {
            this.geneGroupLink.setHref("#group=" + geneGroupId);
            this.geneGroupLink.setText("Go to gene group");
        } else
            this.geneGroupLink.setVisible(false);
    }

    @Override
    public void updateUniquePeptideCount(int count) {
        this.uniquePeptideCount.setText(String.valueOf(count));
    }

    @Override
    public void updateDescription(HeaderPresenter.Description description) {
        this.altId.setText(description.altId);
        this.name.setText(description.name);
        this.species.setText(description.species);
        this.geneSymbol.setText(description.geneSymbol);
        this.proteinEvidence.setText(description.proteinEvidence);

    }

    @Override
    public void updateProperties(List<Pair<String, String>> links) {
        clearProperties();
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
}

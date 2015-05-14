package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
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
    interface HeaderUiBinder extends UiBinder<HTMLPanel, HeaderView> {
    }

    private static HeaderUiBinder ourUiBinder = GWT.create(HeaderUiBinder.class);

//    @UiField
//    VerticalPanel vPanel;

    @UiField
    HTMLPanel panel;

    @UiField
    Anchor title;
    @UiField
    Anchor groupLink;

//    @UiField
//    Label description;
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

    private HTMLPanel root;

    public HeaderView() {
        root = ourUiBinder.createAndBindUi(this);
        title.setText("");
    }
    @Override
    public void updateTitle(String title) {
        this.title.setHref("http://www.uniprot.org/uniprot/"+title);
        this.title.setText(title);
//        this.title.setTitle("tooltip");
    }

    @Override
    public void updateGroupLink(String groupLink) {
        if (groupLink.contains("-")) {
            groupLink = groupLink.substring(0, groupLink.indexOf("-"));
        }
        this.groupLink.setHref("#group="+groupLink);
        this.groupLink.setText("Go to UniProt entry group");
    }

    @Override
    public void updateDescription(String description) {
//        this.description.setText(description);
        String patternStr = "([A-Z_0-9]+)+\\s+(.+)\\s+OS=(.+)\\s+GN=([A-Z_]+)(\\sPE=([1-5]).*)?";
        RegExp regExp = RegExp.compile(patternStr);
        MatchResult matcher = regExp.exec(description);
        boolean matchFound = (matcher != null);

        this.altId.setText(description);// just in case the parsing fails
        if (matchFound) {
            String match = matcher.getGroup(1);
            if (match != null && !match.isEmpty()) {
                this.altId.setText("Protein ID: "+match);
                match = matcher.getGroup(2);
                if (match != null && !match.isEmpty()) {
                    this.name.setText("Protein Name: "+match);
                    match = matcher.getGroup(3);
                    if (match != null && !match.isEmpty()) {
                        this.species.setText("Species Name: "+match);
                        match = matcher.getGroup(4);
                        if (match != null && !match.isEmpty()) {
                            this.geneSymbol.setText("Gene symbol (UniProt): "+match);
                            match = matcher.getGroup(6);
                            if (match != null && !match.isEmpty()) {
                                this.proteinEvidence.setText("UniProt Evidence level: "+match);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateProperties(List<Pair<String, String>> links) {
        clearProperties();
        for(Pair<String, String> link : links) {
            attributes.add(HyperlinkFactory.getInlineHyperLink(link.getA(), link.getB()));
            if(link != links.get(links.size() - 1)) {
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
        return panel;
    }

    @Override
    public void setVisible(boolean visible) {
        asWidget().setVisible(visible);
    }
}

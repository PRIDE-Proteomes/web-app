package uk.ac.ebi.pride.proteomes.web.client.datamodel.factory;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InvalidJSONException;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:44
 */
public abstract class ModelFactory {
    private interface MyFactory extends AutoBeanFactory {
        AutoBean<Group> group();
        AutoBean<Protein> protein();
        AutoBean<PeptideMatch> peptideMatch();
        AutoBean<PeptideList> peptideList();
        AutoBean<Peptide> peptide();
        AutoBean<ModifiedLocation> modifiedLocation();
    }

    public static<T> T getModelObject(Class<T> tClass, String json) throws
            InvalidJSONException {
        MyFactory factory = GWT.create(MyFactory.class);
        try {
            AutoBean<T> bean = AutoBeanCodex.decode(factory, tClass, json);
            return bean.as();
        }
        catch (Throwable e){
            throw new InvalidJSONException("Error parsing json object for " +
                                            tClass + ": " + json, e);
        }
    }
}

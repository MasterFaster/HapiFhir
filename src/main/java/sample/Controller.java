package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    private FhirContext ctx;
    private IGenericClient client;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        System.out.println("HEllo world");
        ctx = new FhirContext().forDstu3();
        client = ctx.newRestfulGenericClient("http://localhost:8080/baseDstu3/");
        Bundle bundle = client.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
        ArrayList<Patient> patients = new ArrayList<Patient>();
        while(true) {
            List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
            for (Bundle.BundleEntryComponent entry : entries) {
                if (entry.getResource() instanceof Patient) {
                    Patient patient = (Patient) entry.getResource();
                    System.out.println(patient.getName().get(0).getFamily());
                    patients.add(patient);
                }
            }
            if(bundle.getLink(Bundle.LINK_NEXT) != null) {
                bundle = client.loadPage().next(bundle).execute();
            }else{
                break;
            }
        }

    }
}

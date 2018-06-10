package sample;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Master Faster on 11.06.2018.
 */
public class PatientDetailsController implements Initializable{

    private Patient patient;
    private IGenericClient client;
    @FXML
    private Label familyNameLabel;
    @FXML
    private Label nameLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String patientFamilyNames = "";
        String patientNames = "";
        for(HumanName humanName : patient.getName()){
            if(!patientFamilyNames.contains(humanName.getFamily())) {
                patientFamilyNames += humanName.getFamily() + ", ";
            }
            for(StringType stringType : humanName.getGiven()){
                if(!patientNames.contains(stringType.getValueNotNull())) {
                    patientNames += stringType.getValueNotNull() + ", ";
                }
            }
        }
        patientFamilyNames.substring(0, patientFamilyNames.length()-2);
        familyNameLabel.setText(patientFamilyNames);
        patientNames.substring(0, patientNames.length()-2);
        nameLabel.setText(patientNames);
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public IGenericClient getClient() {
        return client;
    }

    public void setClient(IGenericClient client) {
        this.client = client;
    }

    public List<MedicationRequest> getMedicationRequests(String patientID){
        Bundle bundle = getClient().search().forResource(MedicationRequest.class)
                .where(new ReferenceClientParam("subject").hasId(patientID)).returnBundle(Bundle.class)
                .execute();
        ArrayList<MedicationRequest> medicationRequests = new ArrayList<>();
        while(true) {
            List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
            for (Bundle.BundleEntryComponent entry : entries) {
                if (entry.getResource() instanceof MedicationRequest) {
                    MedicationRequest medicationRequest = (MedicationRequest) entry.getResource();


                    //Println
                    try {
                        System.out.println(medicationRequest.getMedicationCodeableConcept().getText());
                    } catch (FHIRException e){
                        e.printStackTrace();
                    }


                    medicationRequests.add(medicationRequest);
                }
            }
            if (bundle.getLink(Bundle.LINK_NEXT) != null) {
                bundle = getClient().loadPage().next(bundle).execute();
            } else {
                break;
            }
        }
        return medicationRequests;
    }

    public List<Observation> getObservations(String patientID){
        ArrayList<Observation> observations = new ArrayList<>();
        Bundle bundle = getClient().search().forResource(Observation.class)
                .where(new ReferenceClientParam("patient").hasId(patientID)).returnBundle(Bundle.class)
                .execute();
        while(true) {
            List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
            for (Bundle.BundleEntryComponent entry : entries) {
                if (entry.getResource() instanceof Observation) {
                    Observation observation = (Observation) entry.getResource();
                    System.out.println(observation.getCode().getText());
                    observations.add(observation);
                }
            }
            if (bundle.getLink(Bundle.LINK_NEXT) != null) {
                bundle = getClient().loadPage().next(bundle).execute();
            } else {
                break;
            }
        }
        return observations;
    }


}

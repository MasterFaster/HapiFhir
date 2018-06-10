package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    private FhirContext ctx;
    private IGenericClient client;

    @FXML
    private TextField nameTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        System.out.println("HEllo world");
        ctx = new FhirContext().forDstu3();
        client = ctx.newRestfulGenericClient("http://localhost:8080/baseDstu3/");
    }

    @FXML
    public void getPatient(){
        String name = nameTextField.getText();
        Bundle bundle = client.search().forResource(Patient.class)
                .where(new StringClientParam("name").matches().value(name))
                .returnBundle(Bundle.class).execute();
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
            if (bundle.getLink(Bundle.LINK_NEXT) != null) {
                bundle = client.loadPage().next(bundle).execute();
            } else {
                break;
            }
        }
        if (patients.size()==1){
            System.out.println("\nObservations:");
            String identifier = patients.get(0).getId().split("/")[5];
            System.out.println(identifier);
            bundle = client.search().forResource(Observation.class)
                    .where(new ReferenceClientParam("patient").hasId(identifier)).returnBundle(Bundle.class)
                    .execute();
            while(true) {
                List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
                for (Bundle.BundleEntryComponent entry : entries) {
                    if (entry.getResource() instanceof Observation) {
                        Observation observation = (Observation) entry.getResource();
                        System.out.println(observation.getCode().getText());
                    }
                }
                if (bundle.getLink(Bundle.LINK_NEXT) != null) {
                    bundle = client.loadPage().next(bundle).execute();
                } else {
                    break;
                }
            }
        }
    }

    @FXML
    public void listPatients(){
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
            if (bundle.getLink(Bundle.LINK_NEXT) != null) {
                bundle = client.loadPage().next(bundle).execute();
            } else {
                break;
            }
        }
    }


    public List<MedicationRequest> getMedicationRequests(String patientID){
        Bundle bundle = client.search().forResource(MedicationRequest.class)
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
                bundle = client.loadPage().next(bundle).execute();
            } else {
                break;
            }
        }
        return medicationRequests;
    }

    public List<Observation> getObservations(String patientID){
        ArrayList<Observation> observations = new ArrayList<>();
        Bundle bundle = client.search().forResource(Observation.class)
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
                bundle = client.loadPage().next(bundle).execute();
            } else {
                break;
            }
        }
        return observations;
    }

}

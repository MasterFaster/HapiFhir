package sample;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class ObservationMedicamentController implements Initializable {

    @FXML
    private ListView eventsListView;

    private IGenericClient client;
    private Patient patient;
    private List<MedicationRequest> medicationRequests;
    private List<Observation> observations;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String patientID = patient.getId().split("/")[5];
        medicationRequests = getMedicationRequests(patientID);
        observations = getObservations(patientID);
        ObservableList<String> patientsIntroductions = FXCollections.observableArrayList();
        HashMap<Integer, Pair<List, Integer>>  viewToListsMapper = new HashMap<>();

        eventsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(eventsListView.getSelectionModel().getSelectedItem() != null) {
                    /*
                    System.out.println(eventsListView.getSelectionModel().getSelectedIndex());
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("patientDetails.fxml"));
                    PatientDetailsController patientDetailsController = new PatientDetailsController();
                    patientDetailsController.setPatient(patient);
                    patientDetailsController.setClient(client);
                    fxmlLoader.setController(patientDetailsController);
                    try{
                        Parent root = (Parent)fxmlLoader.load();
                        Stage stage = new Stage();
                        stage.setTitle("Patient Details");
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.initStyle(StageStyle.DECORATED);
                        stage.setResizable(false);
                        stage.setScene(new Scene(root, 450, 90));
                        stage.showAndWait();
                    }catch(Exception ex){
                        System.out.println(ex);
                    }
                    */
                    System.out.println(eventsListView.getSelectionModel().getSelectedIndex());
                }
            }
        });



        Collections.sort(observations, new Comparator<Observation>() {
            @Override
            public int compare(Observation o1, Observation o2) {
                return o2.getIssued().compareTo(o1.getIssued());
            }
        });

        Collections.sort(medicationRequests, new Comparator<MedicationRequest>() {
            @Override
            public int compare(MedicationRequest o1, MedicationRequest o2) {
                return o2.getAuthoredOn().compareTo(o1.getAuthoredOn());
            }
        });
        SimpleDateFormat dateFormatNoTime = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateFormatTime = new SimpleDateFormat("dd-MM-yyyy HH:MM:SS");
        int medicationsIndex = 0;
        int observationsIndex = 0;
        while (patientsIntroductions.size()<observations.size()+medicationRequests.size()){
            if (medicationsIndex == medicationRequests.size()){
                patientsIntroductions.add("Observation " +
                        dateFormatTime.format(observations.get(observationsIndex).getIssued())+ ":\t" +
                        observations.get(observationsIndex).getCode().getText());
                viewToListsMapper.put(eventsListView.getItems().size(), new Pair<>(observations, observationsIndex));
                observationsIndex++;
            } else if (observationsIndex == observations.size()){
                try{
                    patientsIntroductions.add("Medication Request " +
                            dateFormatNoTime.format(medicationRequests.get(medicationsIndex).getAuthoredOn()) + ":\t" +
                            medicationRequests.get(medicationsIndex).getMedicationCodeableConcept().getText());
                    viewToListsMapper.put(eventsListView.getItems().size(),
                            new Pair<>(medicationRequests, medicationsIndex));
                    medicationsIndex++;
                } catch (FHIRException ex){
                    ex.printStackTrace();
                }
            } else if(medicationRequests.get(medicationsIndex).getAuthoredOn()
                    .after(observations.get(observationsIndex).getIssued())){
                try{
                    patientsIntroductions.add("Medication Request " +
                            dateFormatNoTime.format(medicationRequests.get(medicationsIndex).getAuthoredOn()) + ":\t" +
                            medicationRequests.get(medicationsIndex).getMedicationCodeableConcept().getText());
                    viewToListsMapper.put(eventsListView.getItems().size(),
                            new Pair<>(medicationRequests, medicationsIndex));
                    medicationsIndex++;
                } catch (FHIRException ex){
                    ex.printStackTrace();
                }
            } else {
                patientsIntroductions.add("Observation " +
                        dateFormatTime.format(observations.get(observationsIndex).getIssued()) + ":\t" +
                        observations.get(observationsIndex).getCode().getText());
                viewToListsMapper.put(eventsListView.getItems().size(), new Pair<>(observations, observationsIndex));
                observationsIndex++;
            }
        }
        eventsListView.setItems(patientsIntroductions);
    }

    public void setClient(IGenericClient client) {
        this.client = client;
    }

    public IGenericClient getClient() {

        return client;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
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

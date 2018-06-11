package sample;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
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

import javax.xml.crypto.Data;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.*;
import java.util.function.Predicate;

public class ObservationMedicamentController implements Initializable {

    @FXML
    private ListView eventsListView;

    private IGenericClient client;
    private Patient patient;
    private List<MedicationRequest> medicationRequests;
    private List<Observation> observations;
    @FXML private ChoiceBox periodChoiceBox;
    private HashMap<Integer, Pair<List, Integer>>  viewToListsMapper = new HashMap<>();
    private String patientID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientID = patient.getId().split("/")[5];

        periodChoiceBox.getItems().addAll(FXCollections
                .observableArrayList("All", "Last year", "Last month", "Last week"));
        periodChoiceBox.getSelectionModel().selectFirst();
        periodChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                showTimeline(newValue.intValue());
            }
        });

        eventsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(eventsListView.getSelectionModel().getSelectedItem() != null) {
                    System.out.println(eventsListView.getSelectionModel().getSelectedIndex());
                    List key = viewToListsMapper.get(eventsListView.getSelectionModel().getSelectedIndex()).getKey();
                    Integer index = viewToListsMapper.get(eventsListView.getSelectionModel().getSelectedIndex())
                            .getValue();
                    if (key == observations){
                        String code = observations.get(index).getCode().getCodingFirstRep().getCode();
                        System.out.println("Observation code: " + code);
                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("observationsChart.fxml"));
                        ObservationsChartController observationsChartController = new ObservationsChartController();
                        observationsChartController.setPatient(patientID);
                        observationsChartController.setClient(client);
                        observationsChartController.setObservationCode(code);
                        fxmlLoader.setController(observationsChartController);
                        try{
                            Parent root = (Parent)fxmlLoader.load();
                            Stage stage = new Stage();
                            stage.setTitle("Observations chart");
                            stage.initModality(Modality.APPLICATION_MODAL);
                            stage.initStyle(StageStyle.DECORATED);
                            stage.setResizable(false);
                            stage.setScene(new Scene(root, 450, 450));
                            stage.showAndWait();
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                    } else {
                        System.out.println("Medicament request " + index);
                    }
                }
            }
        });
        showTimeline(0);
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

    private void showTimeline(int period){
        medicationRequests = getMedicationRequests(patientID);
        observations = getObservations(patientID);
        ObservableList<String> patientsIntroductions = FXCollections.observableArrayList();
        if (period == 1){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -1); // to get previous year add -1
            Date yearAgo = cal.getTime();
            medicationRequests.removeIf(p -> p.getAuthoredOn().before(yearAgo));
            observations.removeIf(p -> p.getIssued().before(yearAgo));
        } else if (period == 2){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1); // to get previous year add -1
            Date monthAgo = cal.getTime();
            medicationRequests.removeIf(p -> p.getAuthoredOn().before(monthAgo));
            observations.removeIf(p -> p.getIssued().before(monthAgo));
        } else if (period == 3){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -7); // to get previous year add -1
            Date yearAgo = cal.getTime();
            medicationRequests.removeIf(p -> p.getAuthoredOn().before(yearAgo));
            observations.removeIf(p -> p.getIssued().before(yearAgo));
        }
        SimpleDateFormat dateFormatNoTime = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateFormatTime = new SimpleDateFormat("dd-MM-yyyy HH:MM:SS");
        int medicationsIndex = 0;
        int observationsIndex = 0;
        while (patientsIntroductions.size()<observations.size()+medicationRequests.size()){
            if (medicationsIndex == medicationRequests.size()){
                String observationText;
                if (observations.get(observationsIndex).getCode().getText() != null){
                    observationText = observations.get(observationsIndex).getCode().getText();
                } else {
                    observationText = observations.get(observationsIndex).getCode().getCoding().get(0).getDisplay();
                }
                patientsIntroductions.add("Observation " +
                        dateFormatTime.format(observations.get(observationsIndex).getIssued())+ ":\t" +
                        observationText);
                viewToListsMapper.put(patientsIntroductions.size()-1, new Pair<>(observations, observationsIndex));
                observationsIndex++;
            } else if (observationsIndex == observations.size()){
                try{
                    patientsIntroductions.add("Medication Request " +
                            dateFormatNoTime.format(medicationRequests.get(medicationsIndex).getAuthoredOn()) + ":\t" +
                            medicationRequests.get(medicationsIndex).getMedicationCodeableConcept().getText());
                    viewToListsMapper.put(patientsIntroductions.size()-1,
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
                    viewToListsMapper.put(patientsIntroductions.size()-1,
                            new Pair<>(medicationRequests, medicationsIndex));
                    medicationsIndex++;
                } catch (FHIRException ex){
                    ex.printStackTrace();
                }
            } else {
                String observationText;
                if (observations.get(observationsIndex).getCode().getText() != null){
                    observationText = observations.get(observationsIndex).getCode().getText();
                } else {
                    observationText = observations.get(observationsIndex).getCode().getCoding().get(0).getDisplay();
                }
                patientsIntroductions.add("Observation " +
                        dateFormatTime.format(observations.get(observationsIndex).getIssued()) + ":\t" +
                        observationText);
                viewToListsMapper.put(patientsIntroductions.size()-1, new Pair<>(observations, observationsIndex));
                observationsIndex++;
            }
        }
        eventsListView.setItems(patientsIntroductions);
    }


    private List<MedicationRequest> getMedicationRequests(String patientID){
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
        Collections.sort(medicationRequests, new Comparator<MedicationRequest>() {
            @Override
            public int compare(MedicationRequest o1, MedicationRequest o2) {
                return o2.getAuthoredOn().compareTo(o1.getAuthoredOn());
            }
        });
        return medicationRequests;
    }

    private List<Observation> getObservations(String patientID){
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
        Collections.sort(observations, new Comparator<Observation>() {
            @Override
            public int compare(Observation o1, Observation o2) {
                return o2.getIssued().compareTo(o1.getIssued());
            }
        });
        return observations;
    }
}

package sample;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;

import java.awt.event.MouseEvent;
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
    @FXML private Label familyNameLabel;
    @FXML private Label nameLabel;
    @FXML private Label genderLabel;
    @FXML private Label addressLabel;
    @FXML private Button timelineButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String patientFamilyNames = "";
        String patientNames = "";
        for(HumanName humanName : patient.getName()){
            if(!patientFamilyNames.contains(humanName.getFamily())) {
                patientFamilyNames += humanName.getFamily() + "(" + humanName.getUse() + ")" + ", ";
            }
            for(StringType stringType : humanName.getGiven()){
                if(!patientNames.contains(stringType.getValueNotNull())) {
                    patientNames += stringType.getValueNotNull() + ", ";
                }
            }
        }
        patientFamilyNames = patientFamilyNames.substring(0, patientFamilyNames.length()-2);
        familyNameLabel.setText(patientFamilyNames);
        patientNames = patientNames.substring(0, patientNames.length()-2);
        nameLabel.setText(patientNames);
        genderLabel.setText(patient.getGender().getDefinition());
        Tooltip addressTooltip = new Tooltip("");
//        addressTooltip.setX(addressLabel.getLayoutY());
//        addressTooltip.setY(addressLabel.getLayoutX());
        StringBuilder addresses = new StringBuilder();
        for(Address address : patient.getAddress()){
            StringBuilder addressAsString = new StringBuilder();
            for(StringType line : address.getLine()){
                addressAsString.append(line.getValueNotNull() + ", ");
            }
            addressAsString.append(address.getPostalCode() + ", ");
            addressAsString.append(address.getCity() + ", ");
            addressAsString.append(address.getCountry());
            addresses.append(addressAsString + "\n");
        }
        addressTooltip.setText(addresses.toString());
//        addressLabel.setTooltip(addressTooltip);
        Point2D p = addressLabel.localToScene(0.0,0.0);
        addressLabel.setOnMouseEntered(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                addressTooltip.show(addressLabel, p.getX() + addressLabel.getScene().getX() + addressLabel.getScene().getWindow().getX() +
                                addressLabel.getLayoutX()
                        , p.getY() + addressLabel.getScene().getY() + addressLabel.getScene().getWindow().getY() + addressLabel.getLayoutY());
            }
        });
        addressLabel.setOnMouseExited(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                addressTooltip.hide();
            }
        });

        timelineButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("observationMedicament.fxml"));
                ObservationMedicamentController observationMedicamentController = new ObservationMedicamentController();
                observationMedicamentController.setPatient(patient);
                observationMedicamentController.setClient(client);
                fxmlLoader.setController(observationMedicamentController);
                try{
                    Parent root = (Parent)fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setTitle("Timeline");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initStyle(StageStyle.DECORATED);
                    stage.setResizable(false);
                    stage.setScene(new Scene(root, 450, 450));
                    stage.showAndWait();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
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

package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.dstu3.model.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    private FhirContext ctx;
    private IGenericClient client;
    private ArrayList<Patient> patients;
    @FXML
    public ListView patientsListView;
    /**
     * Search patients with family name from familyNameTextField
     */
    @FXML
    public TextField familyNameTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        System.out.println("HEllo world");
        ctx = new FhirContext().forDstu3();
        client = ctx.newRestfulGenericClient("http://localhost:8080/baseDstu3/");
        patientsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(patientsListView.getSelectionModel().getSelectedItem() != null) {
                    System.out.println(patientsListView.getSelectionModel().getSelectedIndex());
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("patientDetails.fxml"));
                    PatientDetailsController patientDetailsController = new PatientDetailsController();
                    patientDetailsController.setPatient(patients.get(patientsListView.getSelectionModel().getSelectedIndex()));
                    patientDetailsController.setClient(client);
                    fxmlLoader.setController(patientDetailsController);
                    try{
                        Parent root = (Parent)fxmlLoader.load();
                        Stage stage = new Stage();
                        stage.setTitle("Patient Details");
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.initStyle(StageStyle.DECORATED);
                        stage.setResizable(false);
                        stage.setScene(new Scene(root, 450, 450));
                        stage.showAndWait();
                    }catch(Exception ex){
                        System.out.println(ex);
                    }
                }
            }
        });
    }

    /**
     * When button "List Patients" is clicked
     * Downloads all patients from database
     */
    @FXML
    public void listPatients(){
        Bundle bundle = client.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
        if(familyNameTextField.getText() != ""){
            bundle = client.search().forResource(Patient.class)
                    .where(new StringClientParam("family").matches().value(familyNameTextField.getText()))
                    .returnBundle(Bundle.class)
                    .execute();
        }
        patients = new ArrayList<Patient>();
        ObservableList<String> patientsIntroductions = FXCollections.observableArrayList();
        while(true) {
            List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
            for (Bundle.BundleEntryComponent entry : entries) {
                if (entry.getResource() instanceof Patient) {
                    Patient patient = (Patient) entry.getResource();
                    System.out.println(patient.getName().get(0).getFamily());
                    patients.add(patient);
                    String patientIntroduction = "";
                    for(HumanName humanName : patient.getName()){
                        if(!patientIntroduction.contains(humanName.getFamily())) {
                            patientIntroduction += humanName.getFamily() + " ";
                        }
                        for(StringType stringType : humanName.getGiven()){
                            if(!patientIntroduction.contains(stringType.getValueNotNull())) {
                                patientIntroduction += stringType.getValueNotNull() + " ";
                            }
                        }
                    }
                    patientsIntroductions.add(patientIntroduction);
                }
            }
            if (bundle.getLink(Bundle.LINK_NEXT) != null) {
                bundle = client.loadPage().next(bundle).execute();
            } else {
                break;
            }
        }

        patientsListView.setItems(patientsIntroductions);
    }

}

package sample;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        genderLabel.setText(patient.getGender().getDefinition());
        setFamilyNameLabel();
        setAddressLabel();
    }

    private void setFamilyNameLabel(){
        String patientFamilyNames = "";
        String patientFamilyName = "";
        String patientNames = "";
        for(HumanName humanName : patient.getName()){
            if(humanName.getUse() == HumanName.NameUse.OFFICIAL)
                patientFamilyName = humanName.getFamily();
            if(!patientFamilyNames.contains(humanName.getFamily())) {
                patientFamilyNames += humanName.getFamily() + "(" + humanName.getUse() + ")" + "\n";
            }
            for(StringType stringType : humanName.getGiven()){
                if(!patientNames.contains(stringType.getValueNotNull())) {
                    patientNames += stringType.getValueNotNull() + ", ";
                }
            }
        }
        patientFamilyNames = patientFamilyNames.substring(0, patientFamilyNames.length()-1);
        if(!patientFamilyName.equals(""))
            familyNameLabel.setText(patientFamilyName);
        else
            familyNameLabel.setText(patientFamilyNames);
        Tooltip familyNameTooltip = new Tooltip(patientFamilyNames);
        familyNameLabel.setOnMouseEntered(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                double x = event.getSceneX() + familyNameLabel.getScene().getWindow().getX()+30;
                double y = event.getSceneY() + familyNameLabel.getScene().getWindow().getY() + 10;
                familyNameTooltip.show(familyNameLabel, x, y);
            }
        });

        familyNameLabel.setOnMouseExited(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                familyNameTooltip.hide();
            }
        });
        patientNames = patientNames.substring(0, patientNames.length()-2);
        nameLabel.setText(patientNames);
    }

    private void setAddressLabel(){
        Tooltip addressTooltip = new Tooltip("");
        StringBuilder addressesForLabel = new StringBuilder();
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
            addressesForLabel.append(address.getCountry() + ", ");
        }
        addressLabel.setText(addressesForLabel.substring(0, addressesForLabel.length()-2));
        addressTooltip.setText(addresses.toString());
        addressLabel.setOnMouseEntered(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                double x = event.getSceneX() + addressLabel.getScene().getWindow().getX()+30;
                double y = event.getSceneY() + addressLabel.getScene().getWindow().getY() + 10;
                addressTooltip.show(addressLabel, x, y);
            }
        });

        addressLabel.setOnMouseExited(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                addressTooltip.hide();
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


}

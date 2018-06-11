package sample;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class ObservationsChartController implements Initializable {

    private String patientID;
    private String observationCode;
    @FXML LineChart<Number, Number> observationsChart;

    public IGenericClient getClient() {
        return client;
    }

    private IGenericClient client;

    public void setPatient(String patient) {
        this.patientID = patient;
    }

    public void setObservationCode(String observationCode) {
        this.observationCode = observationCode;
    }

    public void setClient(IGenericClient client) {
        this.client = client;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<Observation> observations = new ArrayList<>();
        XYChart.Series series = new XYChart.Series<Number, Number>();
        Number index = 0;
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();


        Bundle bundle = client.search().forResource(Observation.class)
                .where(new ReferenceClientParam("patient").hasId(patientID))
                .where(new TokenClientParam("code").exactly().code(observationCode)).returnBundle(Bundle.class)
                .execute();
        while(true) {
            List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
            for (Bundle.BundleEntryComponent entry : entries) {
                if (entry.getResource() instanceof Observation) {
                    Observation observation = (Observation) entry.getResource();
                    try{
                        System.out.println(observation.getValueQuantity().getValue());
                        observations.add(observation);
                        series.getData().add(new XYChart.Data(observation.getIssued().toString(),
                                observation.getValueQuantity().getValue()));
                        index = index.intValue() + 1;
                    } catch (FHIRException ex){
                        ex.printStackTrace();
                    }
                }
            }
            if (bundle.getLink(Bundle.LINK_NEXT) != null) {
                bundle = getClient().loadPage().next(bundle).execute();
            } else {
                break;
            }
        }
        observationsChart.setTitle(observations.get(0).getCode().getText());
        observationsChart.getData().add(series);
    }
}

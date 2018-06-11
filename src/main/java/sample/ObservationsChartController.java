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
import java.text.SimpleDateFormat;
import java.util.*;

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
                    observations.add(observation);
                }
            }
            if (bundle.getLink(Bundle.LINK_NEXT) != null) {
                bundle = getClient().loadPage().next(bundle).execute();
            } else {
                break;
            }
        }
        Collections.sort(observations, Comparator.comparing(Observation::getIssued));
        SimpleDateFormat dateFormatTime = new SimpleDateFormat("dd-MM-yyyy HH:MM:SS");
        System.out.println(observationCode);
        if (!observationCode.contains("55284-4")){
            XYChart.Series series = new XYChart.Series<Number, Number>();

            for (Observation o :
                    observations) {
                try{
                    series.getData().add(new XYChart.Data(dateFormatTime.format(o.getIssued()),
                            o.getValueQuantity().getValue()));
                } catch (FHIRException ex){
                    ex.printStackTrace();
                }
            }
            series.setName(observations.get(0).getCode().getText());
            observationsChart.getData().add(series);
        } else {
            XYChart.Series series = new XYChart.Series<Number, Number>();
            XYChart.Series series2 = new XYChart.Series<Number, Number>();

            for (Observation o :
                    observations) {
                try{
                    series.getData().add(new XYChart.Data(dateFormatTime.format(o.getIssued()),
                            o.getComponent().get(0).getValueQuantity().getValue()));
                    series2.getData().add(new XYChart.Data(dateFormatTime.format(o.getIssued()),
                            o.getComponent().get(1).getValueQuantity().getValue()));
                } catch (FHIRException ex){
                    ex.printStackTrace();
                }
            }
            series.setName(observations.get(0).getComponent().get(0).getCode().getText());
            series2.setName(observations.get(0).getComponent().get(1).getCode().getText());
            observationsChart.getData().add(series);
            observationsChart.getData().add(series2);
        }
        observationsChart.setTitle(observations.get(0).getCode().getText());
    }
}

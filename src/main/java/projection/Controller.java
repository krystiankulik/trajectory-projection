package projection;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;


public class Controller implements Initializable {


    @FXML
    private BorderPane borderPane;

    @FXML
    private Button simulateButton;

    @FXML
    private Button clearButton;

    @FXML
    private TextField velocityTextField;

    @FXML
    private TextField gravityAccTextField;

    @FXML
    private TextField angleTextField;

    @FXML
    private TextField dragTextField;

    @FXML
    private CheckBox includeDragCheckBox;

    private LineChart lineChart;

    private Double velocity;
    private Double angle;
    private Double gravityAcc;
    private Double dragCoefficient;
    private Boolean includeDrag = true;

    private double equationComponent;

    private XYChart.Series<Number, Number> currentSeries;

    private ConcurrentLinkedQueue<XYChart.Data> points = new ConcurrentLinkedQueue<>();

    private ExecutorService executor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setDefaultParameters();
        validateInput();
        final NumberAxis xAxis = new NumberAxis(Messages.getString("projection.chart.axis.x"), 0, 100, 10);
        final NumberAxis yAxis = new NumberAxis(Messages.getString("projection.chart.axis.y"), 0, 100, 10);

        lineChart = new LineChart(xAxis, yAxis);
        lineChart.setAnimated(false);
        lineChart.setTitle(Messages.getString("projection.chart.title"));

        executor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });

        borderPane.setCenter(lineChart);
        updateChart();

    }

    private void setDefaultParameters() {
        velocity = 30.0;
        angle = 45.0;
        gravityAcc = 9.81;
        dragCoefficient = 0.5;
        includeDrag = true;

        velocityTextField.setText(String.valueOf(velocity));
        gravityAccTextField.setText(String.valueOf(gravityAcc));
        angleTextField.setText(String.valueOf(angle));
        dragTextField.setText(String.valueOf(dragCoefficient));
        includeDragCheckBox.setSelected(includeDrag);

    }

    private void validateInput() {
        Validator.validateTextFieldInput(velocityTextField);
        Validator.validateTextFieldInput(gravityAccTextField);
        Validator.validateTextFieldInput(angleTextField);
        Validator.validateTextFieldInput(dragTextField);
    }


    public double calculateXVacuum(double time) {
        double angleRec = Math.toRadians(angle);
        return velocity * time * Math.cos(angleRec);
    }

    public double calculateYVacuum(double time) {
        double angleRec = Math.toRadians(angle);
        return velocity * time * Math.sin(angleRec) - gravityAcc * Math.pow(time, 2) / 2;

    }

    private double getEquationComponent(double time) {
        return 1 - Math.pow(Math.E, -dragCoefficient * time);
    }


    public double calculateYDrag(double time) {
        double angleRad = Math.toRadians(angle);
        return (velocity / dragCoefficient) * equationComponent * Math.sin(angleRad)
                + (gravityAcc / Math.pow(dragCoefficient, 2)) * (-dragCoefficient * time + equationComponent);
    }


    public double calculateXDrag(double time) {
        double angleRad = Math.toRadians(angle);
        return (velocity / dragCoefficient) * equationComponent * Math.cos(angleRad);
    }



    private void setCurrentSeries(XYChart.Series currentSeries) {
        this.currentSeries = currentSeries;
    }

    @FXML
    public void simulate() {
        velocity = Double.valueOf(velocityTextField.getText());
        angle = Double.valueOf(angleTextField.getText());
        gravityAcc = Double.valueOf(gravityAccTextField.getText());
        dragCoefficient = Double.valueOf(dragTextField.getText());
        includeDrag = includeDragCheckBox.isSelected();

        if (includeDrag) {
            drawChart(this::calculateXDrag, this::calculateYDrag);
        } else {
            drawChart(this::calculateXVacuum, this::calculateYVacuum);
        }
    }

    @FXML
    public void clear() {
        lineChart.getData().clear();
    }

    public void drawChart(UnaryOperator<Double> calculateX, UnaryOperator<Double> calculateY) {

        XYChart.Series series = new XYChart.Series();
        if (includeDrag) {
            series.setName(Messages.getString(
                    "projection.chart.series.title", velocity, angle, gravityAcc, dragCoefficient));
        } else {
            series.setName(Messages.getString("projection.chart.series.title",
                    velocity, angle, gravityAcc, Messages.getString("projection.general.off")));
        }

        lineChart.getData().add(series);

        setCurrentSeries(series);
        simulateButton.setDisable(true);
        clearButton.setDisable(true);
        executor.execute(new AddPosition(0, calculateX, calculateY));
    }



    private void addPointsToSeries() {

        if (!points.isEmpty()) {
            currentSeries.getData().add(points.remove());
            int seriesSize = currentSeries.getData().size();
            if (currentSeries.getData().size() > 1) {
                (currentSeries.getData().get(seriesSize - 2)).getNode().setVisible(false);
                (currentSeries.getData().get(seriesSize - 1).getNode()).setVisible(true);
            }

        }
    }

    public void loadEnglishLocale() {
        Localizing.loadView(new Locale("en", "US"), borderPane);
    }

    public void loadPolishLocale() {
        Localizing.loadView(new Locale("pl", "PL"), borderPane);
    }

    public void updateChart() {
        new AnimationTimer() {
            @Override
            public void handle(long l) {
                addPointsToSeries();
            }
        }.start();
    }

    private class AddPosition implements Runnable {

        private double time;
        private UnaryOperator<Double> calculateX;
        private UnaryOperator<Double> calculateY;


        public AddPosition(double time, UnaryOperator<Double> calculateX, UnaryOperator<Double> calculateY) {
            this.time = time;
            this.calculateX = calculateX;
            this.calculateY = calculateY;
        }

        @Override
        public void run() {
            try {
                equationComponent = getEquationComponent(time);
                points.add(new XYChart.Data(calculateX.apply(time), calculateY.apply(time)));
                Thread.sleep(30);
                if (calculateY.apply(time) >= 0) {
                    executor.execute(new AddPosition(time + 0.03, calculateX, calculateY));
                } else {
                    simulateButton.setDisable(false);
                    clearButton.setDisable(false);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }




}

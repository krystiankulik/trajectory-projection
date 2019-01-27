package projection;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

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
    private XYChart.Series<Number, Number> currentSeries;
    private ConcurrentLinkedQueue<XYChart.Data> points = new ConcurrentLinkedQueue<>();
    private ExecutorService executor;
    private Projector projector;
    private boolean stopSimulation;

    private final NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        projector = new Projector();
        setDefaultParameters();
        validateInput();

        xAxis.setLabel(Messages.getString("projection.chart.axis.x"));
        yAxis.setLabel(Messages.getString("projection.chart.axis.y"));

        resetAxis(xAxis);
        resetAxis(yAxis);

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

    private void resetAxis(NumberAxis axis) {
        axis.setAutoRanging(false);
        axis.setLowerBound(0);
        axis.setUpperBound(100);
        axis.setTickUnit(10);
    }

    private void setDefaultParameters() {

        velocityTextField.setText(String.valueOf(projector.getVelocity()));
        gravityAccTextField.setText(String.valueOf(projector.getGravityAcc()));
        angleTextField.setText(String.valueOf(projector.getAngle()));
        dragTextField.setText(String.valueOf(projector.getDragCoefficient()));
        includeDragCheckBox.setSelected(projector.getIncludeDrag());
    }

    private void validateInput() {
        Validator.validateTextFieldInput(velocityTextField);
        Validator.validateTextFieldInput(gravityAccTextField);
        Validator.validateTextFieldInput(angleTextField);
        Validator.validateTextFieldInput(dragTextField);
    }

    private void setCurrentSeries(XYChart.Series<Number, Number> currentSeries) {
        this.currentSeries = currentSeries;
    }

    @FXML
    public void simulate() {
        projector.setVelocity(Double.valueOf(velocityTextField.getText()));
        projector.setAngle(Double.valueOf(angleTextField.getText()));
        projector.setGravityAcc(Double.valueOf(gravityAccTextField.getText()));
        projector.setDragCoefficient(Double.valueOf(dragTextField.getText()));
        projector.setIncludeDrag(includeDragCheckBox.isSelected());

        drawChart(projector.calculateX(), projector.calculateY());


    }

    @FXML
    public void clear() {
        stopSimulation = true;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        lineChart.getData().clear();
        resetAxis(xAxis);
        resetAxis(yAxis);
    }

    public void drawChart(UnaryOperator<Double> calculateX, UnaryOperator<Double> calculateY) {

        XYChart.Series series = new XYChart.Series();
        series.setName(projector.getSeriesDescription());


        lineChart.getData().add(series);

        setCurrentSeries(series);
        simulateButton.setDisable(true);
        stopSimulation = false;
        executor.execute(new AddPosition(0, calculateX, calculateY));
    }


    private void addPointsToSeries() {

        if (stopSimulation) {
            points.clear();
        }

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
                double positionX = calculateX.apply(time);
                double positionY = calculateY.apply(time);
                if (positionX > xAxis.getUpperBound()) {
                    xAxis.setUpperBound(xAxis.getUpperBound() * 2);
                    xAxis.setTickUnit(xAxis.getTickUnit() * 2);
                }

                if (positionY > yAxis.getUpperBound()) {
                    yAxis.setUpperBound(yAxis.getUpperBound() * 2);
                    yAxis.setTickUnit(yAxis.getTickUnit() * 2);
                }
                points.add(new XYChart.Data(positionX, positionY));
                Thread.sleep(30);
                if (positionY >= 0 && !stopSimulation) {
                    executor.execute(new AddPosition(time + 0.03, calculateX, calculateY));
                } else {
                    simulateButton.setDisable(false);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}

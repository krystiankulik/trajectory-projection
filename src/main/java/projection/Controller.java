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
    private XYChart.Series<Number, Number> currentSeries;
    private ConcurrentLinkedQueue<XYChart.Data> points = new ConcurrentLinkedQueue<>();
    private ExecutorService executor;
    private Projector projector;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        projector = new Projector();
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
        projector.setVelocity( Double.valueOf(velocityTextField.getText()));
        projector.setAngle(Double.valueOf(angleTextField.getText()));
        projector.setGravityAcc(Double.valueOf(gravityAccTextField.getText()));
        projector.setDragCoefficient(Double.valueOf(dragTextField.getText()));
        projector.setIncludeDrag(includeDragCheckBox.isSelected());

        drawChart(projector.calculateX(), projector.calculateY());


    }

    @FXML
    public void clear() {
        lineChart.getData().clear();
    }

    public void drawChart(UnaryOperator<Double> calculateX, UnaryOperator<Double> calculateY) {

        XYChart.Series series = new XYChart.Series();
            series.setName(projector.getSeriesDescription());


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

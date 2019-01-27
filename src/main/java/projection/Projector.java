package projection;

import java.util.function.UnaryOperator;

public class Projector {

    private Double velocity;
    private Double angle;
    private Double gravityAcc;
    private Double dragCoefficient;
    private Boolean includeDrag;

    public Projector() {
        this(30.0, 45.0, 9.81, 0.3, true);
    }

    public Projector(Double velocity, Double angle, Double gravityAcc, Double dragCoefficient, Boolean includeDrag) {
        this.velocity = velocity;
        this.angle = angle;
        this.gravityAcc = gravityAcc;
        this.dragCoefficient = dragCoefficient;
        this.includeDrag = includeDrag;

    }


    public double calculateXVacuum(double time) {
        double angleRec = Math.toRadians(angle);
        return velocity * time * Math.cos(angleRec);
    }

    public double calculateYVacuum(double time) {
        double angleRec = Math.toRadians(angle);
        return velocity * time * Math.sin(angleRec) - gravityAcc * Math.pow(time, 2) / 2;

    }


    public double calculateYDrag(double time) {
        double angleRad = Math.toRadians(angle);
        return (velocity / dragCoefficient) * (1 - Math.pow(Math.E, -dragCoefficient * time)) * Math.sin(angleRad)
                + (gravityAcc / Math.pow(dragCoefficient, 2)) * (-dragCoefficient * time + (1 - Math.pow(Math.E, -dragCoefficient * time)));
    }


    public double calculateXDrag(double time) {
        double angleRad = Math.toRadians(angle);
        return (velocity / dragCoefficient) * (1 - Math.pow(Math.E, -dragCoefficient * time)) * Math.cos(angleRad);
    }

    public String getSeriesDescription() {
        if (includeDrag) {
            return Messages.getString(
                    "projection.chart.series.title", velocity, angle, gravityAcc, dragCoefficient);
        } else {
            return Messages.getString("projection.chart.series.title",
                    velocity, angle, gravityAcc, Messages.getString("projection.general.off"));
        }
    }

    public UnaryOperator<Double> calculateX() {
        if (includeDrag) {
            return this::calculateXDrag;
        } else {
            return this::calculateXVacuum;
        }
    }

    public UnaryOperator<Double> calculateY() {
        if (includeDrag) {
            return this::calculateYDrag;
        } else {
            return this::calculateYVacuum;
        }
    }

    public Double getVelocity() {
        return velocity;
    }

    public void setVelocity(Double velocity) {
        this.velocity = velocity;
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }

    public Double getGravityAcc() {
        return gravityAcc;
    }

    public void setGravityAcc(Double gravityAcc) {
        this.gravityAcc = gravityAcc;
    }

    public Double getDragCoefficient() {
        return dragCoefficient;
    }

    public void setDragCoefficient(Double dragCoefficient) {
        this.dragCoefficient = dragCoefficient;
    }

    public Boolean getIncludeDrag() {
        return includeDrag;
    }

    public void setIncludeDrag(Boolean includeDrag) {
        this.includeDrag = includeDrag;
    }

}

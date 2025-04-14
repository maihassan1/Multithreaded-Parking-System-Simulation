import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
class ParkingLot {
    private final Semaphore spots = new Semaphore(4);
    private int served = 0;
    public synchronized void incrementCarsServed() {
        served++;
    }
    public int getCarsServed() {
        return served;
    }

    public Semaphore getParkingSpots() {
        return spots;
    }
}
class Car extends Thread {
    private final int carId;
    private final String gate;
    private final int arrivalTime;
    private final int parkingDuration;
    private final ParkingLot parkingLot;
    public Car(int carId, String gate, int arrivalTime, int parkingDuration, ParkingLot parkingLot) {
        this.carId = carId;
        this.gate = gate;
        this.arrivalTime = arrivalTime;
        this.parkingDuration = parkingDuration;
        this.parkingLot = parkingLot;
    }
    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(arrivalTime);
            System.out.println("Car " + carId + " from " + gate + " arrived at time " + arrivalTime);
            while (!parkingLot.getParkingSpots().tryAcquire()) {
                System.out.println("Car " + carId + " from " + gate + " waiting for a spot.");
                long waitTimeMicros = (long) (Math.random() * (1000000 - 1) + 1);
                if (waitTimeMicros < 1000) {
                    long start = System.nanoTime();
                    while ((System.nanoTime() - start) < waitTimeMicros * 1000) {
                    }
                } else {
                    TimeUnit.MICROSECONDS.sleep(waitTimeMicros);
                }
            }
            System.out.println("Car " + carId + " from " + gate + " parked. (Parking Status: " +
                    (4 - parkingLot.getParkingSpots().availablePermits()) + " spots occupied)");
            parkingLot.incrementCarsServed();
            TimeUnit.SECONDS.sleep(parkingDuration);
            System.out.println("Car " + carId + " from " + gate + " leaving after " + parkingDuration + " units of time.");
            parkingLot.getParkingSpots().release();
            System.out.println("Car " + carId + " from " + gate + " left. (Parking Status: " +
                    (4 - parkingLot.getParkingSpots().availablePermits()) + " spots occupied)");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
public class ParkingSimulation {
    public static void main(String[] args) {
        ParkingLot parkingLot = new ParkingLot();
        List<Car> cars = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\DELL\\IdeaProjects\\ParkingSimulation\\src\\car_data.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(", ");
                String gate = parts[0];
                int carId = Integer.parseInt(parts[1].split(" ")[1]);
                int arrivalTime = Integer.parseInt(parts[2].split(" ")[1]);
                int parkingDuration = Integer.parseInt(parts[3].split(" ")[1]);
                Car car = new Car(carId, gate, arrivalTime, parkingDuration, parkingLot);
                cars.add(car);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Car car : cars) {
            car.start();
        }
        for (Car car : cars) {
            try {
                car.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("\nTotal Cars Served: " + parkingLot.getCarsServed());
        System.out.println("Current Cars in Parking: " + (4 - parkingLot.getParkingSpots().availablePermits()));
    }
}

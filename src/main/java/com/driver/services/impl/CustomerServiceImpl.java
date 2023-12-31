package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
       customerRepository2.deleteById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
        List<Driver> driverList = driverRepository2.findAll();
		Driver driver = null;

		for(Driver d : driverList){
			if(d.getCab().isAvailable()){
				driver=d;
				break;
			}
		}
		if(driver==null){
			throw new Exception("No cab availabvle");
		}


        // create a booking
		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setTripStatus(TripStatus.CONFIRMED);


		TripBooking booking = tripBookingRepository2.save(tripBooking); // savewd booking
		booking.setDriver(driver);
		driver.getTripBookingList().add(booking);

		Customer customer = customerRepository2.findById(customerId).get();
		customer.getTripBookingList().add(booking);
		booking.setCustomer(customer);

driverRepository2.save(driver);
customerRepository2.save(customer);
return booking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
tripBooking.setTripStatus(TripStatus.CANCELED);
tripBooking.setBill(0);

Driver driver = tripBooking.getDriver();
Cab cab = driver.getCab();
cab.setAvailable(true);
driverRepository2.save(driver);
tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
        TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setTripStatus(TripStatus.COMPLETED);

		Driver driver = tripBooking.getDriver();
		Cab cab = driver.getCab();
		cab.setAvailable(true);

		int totalBill = tripBooking.getDistanceInKm() * cab.getPerKmRate();
		tripBooking.setBill(totalBill);

		driverRepository2.save(driver);
		tripBookingRepository2.save(tripBooking);
	}
}

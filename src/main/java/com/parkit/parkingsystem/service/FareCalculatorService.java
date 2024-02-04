package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import static java.lang.Math.round;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	public void calculateFare(Ticket ticket) {
		calculateFare(ticket, false);
	}

	public void calculateFare(Ticket ticket, boolean discount) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		long inHour = ticket.getInTime().getTime();
		long outHour = ticket.getOutTime().getTime();

		long millisDuration = outHour - inHour;
		float duration = (millisDuration / 3600000) + (millisDuration % 3600000 / 60000 / 60.f);

		if (duration <= 0.50) {
			ticket.setPrice(0);
		} else {
			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
				break;
			}
			case BIKE: {
				ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}
			ticket.setPrice((double) round(ticket.getPrice() * 1000) / 1000);

		}

	}
}
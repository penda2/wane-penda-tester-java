package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class ParkingServiceTest {

	@InjectMocks
	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@Mock
	private static ParkingSpotDAO parkingSpotDAO;

	@Mock
	private static TicketDAO ticketDAO;

	@Mock
	private static Ticket ticket;

	@Mock
	private static ParkingSpot parkingSpot;

	@BeforeEach
	private void setUpPerTest() {
		try {
			parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			ticket = new Ticket();
			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");
			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	// test the method call where everything goes as expected
	@Test
	public void testProcessIncomingVehicle() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		parkingService.processIncomingVehicle();
		verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
	}

	// Test process exiting vehicle which mocks the call to the getNbTicket() method
	@Test
	public void processExitingVehicleTest() throws Exception {
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
		parkingService.processExitingVehicle();
		ticketDAO.getNbTicket("ABCDEF");
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
		verify(ticketDAO, Mockito.times(2)).getNbTicket("ABCDEF");
	}
	
	//test the method in case updateTicket() returns false
	@Test
	public void processExitingVehicleTestUnableUpdate() throws Exception {
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
		parkingService.processExitingVehicle();
		verify(ticketDAO, Mockito.times(1)).updateTicket(ticket);
	}
	
	//test of the method with the result of obtaining a spot whose ID is 1 and which is available 
	@Test
	public void testGetNextParkingNumberIfAvailable() {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		parkingService.processIncomingVehicle();
		verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
	}

	// return getNextParkingNumberIfAvailable() method as null if no spot available
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() throws Exception {
		int unavailableSpot = -1;
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(unavailableSpot);
		parkingService.processIncomingVehicle();
		verify(inputReaderUtil, Mockito.times(0)).readVehicleRegistrationNumber();
	}
	
	//return getNextParkingNumberIfAvailable() method as null if vehicle type entered is incorrect
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgum() {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(3);
		parkingService.processIncomingVehicle();
		verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
		verify(inputReaderUtil, times(1)).readSelection();
	}
}

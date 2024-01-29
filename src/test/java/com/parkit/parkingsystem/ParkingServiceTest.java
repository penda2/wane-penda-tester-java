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

	private static Ticket ticket;
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

	@Test
	public void testProcessIncomingVehicle() throws Exception {
		// given
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		// when
		parkingService.processIncomingVehicle();
		// then
		verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));

	}

	@Test
	public void processExitingVehicleTest() throws Exception {
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
		parkingService.processExitingVehicle();
		ticketDAO.getNbTicket();
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
		verify(ticketDAO, Mockito.times(2)).getNbTicket();
	}
	/*
	 * //test Sortie du véhicule si mise à jour impossible
	 * 
	 * @Test public void processExitingVehicleTestUnableUpdate() {
	 * parkingService.processExitingVehicle(); verify(ticketDAO,
	 * Mockito.times(1)).updateTicket(any(Ticket.class)); 
	 * }
	 * 
	 * @Test public void testGetNextParkingNumberIfAvailable() {
	 * when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
	 * parkingService.getNextParkingNumberIfAvailable();
	 * verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
	 }
	 * @Test public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){ 
	 * parkingService.getNextParkingNumberIfAvailable(); }
	 * 
	 * @Test public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgum(){
	 * parkingService.getNextParkingNumberIfAvailable(); }
	 */
}

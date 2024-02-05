package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.sql.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@Mock
	private static ParkingService parkingService;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	}

	@AfterAll
	private static void tearDown() {

	}

	// check that a ticket is actualy saved in DB and Parking table is updated with availability
	@Test
	public void testParkingACar() {
		parkingService.processIncomingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		assertNotNull(ticket);
		assertEquals("ABCDEF", ticket.getVehicleRegNumber());
		assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
	}

	// test that the fare generated and out time are populated correctly in the database
	@Test
	public void testParkingLotExit() {
		testParkingACar();
		parkingService.processExitingVehicle();
		assertNotNull(ticketDAO.getTicket("ABCDEF").getPrice());
		assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
	}

	//calculation test of the price of a ticket for a recurring vehicle benefiting from a 5% discount
	@Test
	public void testParkingLotExitRecurringUser() {
		parkingService.processIncomingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		ticket.setOutTime(new Date(System.currentTimeMillis()));
		ticketDAO.updateTicket(ticket);
		parkingService.RecurringUser("ABCDEF");
		parkingService.processExitingVehicle();
		assertEquals(ticket.getPrice() * 0.95, ticketDAO.getTicket("ABCDEF").getPrice());
	}
}
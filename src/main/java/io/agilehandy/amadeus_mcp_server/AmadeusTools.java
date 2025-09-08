package io.agilehandy.amadeus_mcp_server;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.referencedata.Locations;
import com.amadeus.resources.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Description;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AmadeusTools {

    private final Amadeus amadeus;;

    public AmadeusTools(Amadeus amadeus) {
        this.amadeus = amadeus;
    }

    @Tool(
            name="Flights Search",
            description="To search flights from origin to destination locations on departure and return dates for number of adults"
    )
    public String flights(String originLocationCode, String destinationLocationCode,
                          @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate departureDate,
                          @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate returnDate,
                          Integer numberOfAdults, Integer maxNumberOfTravelers) throws ResponseException, JsonProcessingException {
        Map<String, LocalDate> dates = this.checkAndCorrectDates(Map.of("departure date", departureDate, "return date", returnDate));
        Integer adults = numberOfAdults == null? 1:numberOfAdults;
        Integer max = maxNumberOfTravelers == null? 1:maxNumberOfTravelers;
        Params params = Params.with("originLocationCode", originLocationCode)
                .and("destinationLocationCode", destinationLocationCode)
                .and("departureDate", dates.get("departure date"))
                .and("returnDate", dates.get("return date"))
                .and("adults", adults)
                .and("max", max);
        FlightOfferSearch[] flightOfferSearches = amadeus.shopping.flightOffersSearch.get(params);
        return Arrays.stream(flightOfferSearches).map(FlightOfferSearch::toString)
                .collect(Collectors.joining("\n"));
    }

    @Tool(
            name="Hotels Search",
            description="To search hotels based on a city code"
    )
    public String hotels(String cityCode) throws ResponseException, JsonProcessingException {
        Params params = Params.with("cityCode", cityCode);
        Hotel[] hotels = amadeus.referenceData.locations.hotels.byCity.get(params);
        return Arrays.stream(hotels).map(Hotel::toString)
                .collect(Collectors.joining("\n"));
    }

    @Tool(
            name="List Cities",
            description="To list all cities based on a keyword"
    )
    public String cities(String keyword) throws ResponseException {
        Location[] locations = amadeus.referenceData.locations.get(Params
                .with("keyword", keyword)
                .and("subType", Locations.CITY));
        return Arrays.stream(locations).map(Location::toString)
                .collect(Collectors.joining("\n"));

    }

    @Tool(
            name="List Airports",
            description="To list all airports based on a keyword"
    )
    public String airports(String keyword) throws ResponseException {
        Location[] locations = amadeus.referenceData.locations.get(Params
                .with("keyword", keyword)
                .and("subType", Locations.AIRPORT));
        return Arrays.stream(locations).map(Location::toString)
                .collect(Collectors.joining("\n"));
    }

    @Tool(
            name="Airline Lookup",
            description="To lookup airline by code"
    )
    public String airlineCodeLookup(String airlineCode) throws ResponseException {
        Airline[] airlineCodes = amadeus.referenceData.airlines.get(Params
                .with("airlineCodes", airlineCode));
        return Arrays.stream(airlineCodes).map(Airline::toString)
                .collect(Collectors.joining("\n"));
    }

    private Map<String, LocalDate> checkAndCorrectDates(Map<String, LocalDate> dates) {
        LocalDate departureDate = dates.get("departure date");
        LocalDate returnDate = dates.get("return date");
        if (returnDate.isBefore(departureDate)) {
            return Map.of("departure date", returnDate, "return date", departureDate);
        }
        return dates;
    }

/** Maybe to be used at some point to convert dates
    public String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Example format
        return date.format(formatter);
    }
 */
}

package io.agilehandy.amadeus_mcp_server;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.referencedata.Locations;
import com.amadeus.resources.Airline;
import com.amadeus.resources.FlightOfferSearch;
import com.amadeus.resources.Hotel;
import com.amadeus.resources.Location;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class AmadeusTools {

    private final Amadeus amadeus;;

    public AmadeusTools(Amadeus amadeus) {
        this.amadeus = amadeus;
    }

    @Tool(
            name="search_flights",
            description="to search flights from origin to destination locations on departure and return dates for number of adults"
    )
    public List<String> flights(
            @ToolParam(description = "iataCode of the travel departure airport. If a city is given find the code of the main or nearby airport in that city")
            String originLocationCode,
            @ToolParam(description = "iataCode of the travel arrival airport. If a city is given find code of the main or nearby airport in that city")
            String destinationLocationCode,
            @ToolParam(description = "travel departure date in the format yyyy-MM-dd")
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate departureDate,
            @ToolParam(description = "travel return date in the format yyyy-MM-dd")
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate returnDate,
            @ToolParam(description = "number of traveling adults")
            Integer numberOfAdults,
            @ToolParam(description = "number of all traveling persons")
            Integer maxNumberOfTravelers) throws ResponseException, JsonProcessingException {
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
        return Arrays.stream(flightOfferSearches)
                .map(flight -> flight.getResponse().getBody()).toList();
    }

    @Tool(
            name="search_hotels",
            description="to search hotels based on a location iataCode"
    )
    public List<String> hotels(
            @ToolParam(description = """
                city iataCode. If the provided code is an airport iataCode then find the iatacode of the city 
                where the airport is located.
            """)
            String cityCode) throws ResponseException, JsonProcessingException {
        Params params = Params.with("cityCode", cityCode);
        Hotel[] hotels = amadeus.referenceData.locations.hotels.byCity.get(params);
        return Arrays.stream(hotels)
                .map(hotel -> hotel.getResponse().getBody()).toList();
    }

    //@Tool(
            //name="list_cities",
            //description="to list information about all cities based on a keyword"
    //)
    public Location[] cities( @ToolParam(description = "keyword to use for cities search") String keyword) throws ResponseException {
        Location[] locations = amadeus.referenceData.locations.get(Params
                .with("keyword", keyword)
                .and("subType", Locations.CITY));
        //return Arrays.stream(locations).map(Location::toString)
                //.collect(Collectors.joining("\n"));
        return locations;
    }

    //@Tool(
            //name="list_airports",
            //description="to list information about all airports based on a keyword"
    //)
    public Location[] airports(@ToolParam(description = "keyword to use for airports search") String keyword) throws ResponseException {
        Location[] locations = amadeus.referenceData.locations.get(Params
                .with("keyword", keyword)
                .and("subType", Locations.AIRPORT));
        //return Arrays.stream(locations).map(Location::toString)
                //.collect(Collectors.joining("\n"));
        return locations;
    }

    //@Tool(
            //name="lookup_airlines",
            //description="to lookup airline by iataCode"
    //)
    public Airline[] airlineCodeLookup(@ToolParam(description = "airline code") String airlineCode) throws ResponseException {
        Airline[] airlineCodes = amadeus.referenceData.airlines.get(Params
                .with("airlineCodes", airlineCode));
        //return Arrays.stream(airlineCodes).map(Airline::toString)
            //.collect(Collectors.joining("\n"));
        return airlineCodes;
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

package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import at.ac.tuwien.sepr.assignment.individual.service.TournamentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = TournamentEndpoint.BASE_PATH)
public class TournamentEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/tournaments";

  private final TournamentService service;

  public TournamentEndpoint(TournamentService service) {
    this.service = service;
  }

  @GetMapping
  public Stream<TournamentListDto> searchTournaments(TournamentSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH);
    LOG.debug("request parameters: {}", searchParameters);
    return service.search(searchParameters);
  }


  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }
}

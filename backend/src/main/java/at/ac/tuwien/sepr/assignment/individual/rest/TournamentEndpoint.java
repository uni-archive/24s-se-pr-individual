package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.TournamentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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


  @PostMapping
  public ResponseEntity<TournamentDetailDto> create(@RequestBody TournamentCreateDto toCreate) {
    LOG.info("POST " + BASE_PATH);
    LOG.debug("Body of request:\n{}", toCreate);
    try {
      TournamentDetailDto created = service.create(toCreate);
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (ValidationException e) {
      HttpStatus status = HttpStatus.BAD_REQUEST;
      logClientError(status, "Invalid horse data", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ConflictException e) {
      HttpStatus status = HttpStatus.CONFLICT;
      logClientError(status, "Horse already exists", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

//  @GetMapping("{id}/standings")
//  public TournamentStandingsDto getStandingsById(@PathVariable("id") long id) {
//    LOG.info("GET " + BASE_PATH + "/{}", id);
//    try {
//      LOG.debug("TournamentEndpoint.getStandingsById");
//      return service.getStandingsById(id);
//    } catch (NotFoundException e) {
//      HttpStatus status = HttpStatus.NOT_FOUND;
//      logClientError(status, "Tournament to get standings of not found", e);
//      throw new ResponseStatusException(status, e.getMessage(), e);
//    }
//  }

  @GetMapping("{id}")
  public TournamentDetailDto getById(@PathVariable("id") long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);
    try {
      LOG.debug("TournamentEndpoint.getById");
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Tournament to get standings of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }


  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }
}

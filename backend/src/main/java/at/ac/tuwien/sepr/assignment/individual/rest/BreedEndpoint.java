package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedDto;
import at.ac.tuwien.sepr.assignment.individual.dto.BreedSearchDto;
import at.ac.tuwien.sepr.assignment.individual.service.BreedService;
import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = BreedEndpoint.BASE_PATH)
public class BreedEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final String BASE_PATH = "/breeds";

  private final BreedService service;

  public BreedEndpoint(BreedService service) {
    this.service = service;
  }

  @GetMapping
  public Stream<BreedDto> search(BreedSearchDto searchParams) {
    LOG.info("GET " + BASE_PATH);
    LOG.debug("Request Params: {}", searchParams);
    return service.search(searchParams);
  }
}

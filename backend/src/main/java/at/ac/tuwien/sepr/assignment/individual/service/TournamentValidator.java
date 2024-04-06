package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsTreeDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component
public class TournamentValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void validateForStandingsUpdate(TournamentStandingsTreeDto tree) throws ValidationException {
    LOG.trace("validateForStandingsUpdate({})", tree);
  }

//  private List<String> validateHorseProperties(HorseDetailDto horse) {
//    List<String> validationErrors = new ArrayList<>();
//
//    if (horse.name() == null || horse.name().isBlank()) {
//      validationErrors.add("No name given");
//    }
//
//    if (horse.name().length() > 255) {
//      validationErrors.add("Name too long");
//    }
//
//    if (horse.sex() == null) {
//      validationErrors.add("Sex not given");
//    }
//
//    if (horse.dateOfBirth() == null) {
//      validationErrors.add("Date of birth not given");
//    }
//
//    if (horse.dateOfBirth() != null && horse.dateOfBirth().isAfter(java.time.LocalDate.now())) {
//      validationErrors.add("Date of birth must be in the past");
//    }
//
//    if (horse.height() <= 0) {
//      validationErrors.add("Height must be greater than 0");
//    }
//
//    if (horse.weight() <= 0) {
//      validationErrors.add("Weight must be greater than 0");
//    }
//
//    return validationErrors;
//  }

}

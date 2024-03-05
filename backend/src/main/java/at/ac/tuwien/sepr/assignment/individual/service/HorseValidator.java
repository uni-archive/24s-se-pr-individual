package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void validateForUpdate(HorseDetailDto horse) throws ValidationException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    if (horse.id() == null) {
      validationErrors.add("No ID given");
    }

    // TODO this is not complete…

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

    public void validateForCreate(HorseDetailDto horse) throws ValidationException {
        LOG.trace("validateForCreate({})", horse);
        List<String> validationErrors = new ArrayList<>();

        if (horse.id() != null) {
          validationErrors.add("ID given");
        }

        if (horse.name() == null || horse.name().isBlank()) {
          validationErrors.add("No name given");
        }

        if (horse.name().length() > 255) {
          validationErrors.add("Name too long");
        }

        if (horse.sex() == null) {
          validationErrors.add("Sex not given");
        }

        if (horse.dateOfBirth() == null) {
          validationErrors.add("Date of birth not given");
        }

        if (horse.dateOfBirth() != null && horse.dateOfBirth().isAfter(java.time.LocalDate.now())) {
          validationErrors.add("Date of birth must be in the past");
        }

        if (horse.height() <= 0) {
          validationErrors.add("Height must be greater than 0");
        }

        if (horse.weight() <= 0) {
          validationErrors.add("Weight must be greater than 0");
        }


        if (!validationErrors.isEmpty()) {
        throw new ValidationException("Validation of horse for create failed", validationErrors);
        }
    }

}

package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSelectionDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsTreeDto;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentTree;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class TournamentValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void validateForStandingsUpdate(TournamentStandingsTreeDto tree, int participantCount)
      throws ValidationException {
    LOG.trace("validateForStandingsUpdate({})", tree);
    var participantSet = new HashSet<Long>();
    var participantNullCount = new AtomicInteger(0);
    validateSubbranches(tree, null, participantSet, participantNullCount);

    if (participantSet.size() + participantNullCount.get() != participantCount) {
      throw new ValidationException("Updating of tree failed",
          List.of("Participants can only be entered once in the first round"));
    }

  }
  private void validateSubbranches(TournamentStandingsTreeDto current, Long previousHorseId, Set<Long> participantSet,
                                   AtomicInteger participantNullCount) throws ValidationException {
    LOG.trace("validateForStandingsUpdate({})", current);
    if (previousHorseId != null && current.thisParticipant() == null) {
      throw new ValidationException("Updating of tree failed", List.of("Tree contains missing placements"));
    }

    if (current.branches() == null || current.branches().isEmpty()) {
      if (current.thisParticipant() == null) {
        participantNullCount.incrementAndGet();
      } else {
        participantSet.add(current.thisParticipant().horseId());
      }
      return;
    }

    validateSubbranches(current.branches().get(0), current.thisParticipant() == null
        ? null : current.thisParticipant().horseId(), participantSet, participantNullCount);
    validateSubbranches(current.branches().get(1), current.thisParticipant() == null
        ? null : current.thisParticipant().horseId(), participantSet, participantNullCount);
  }

  public void validateTournament(TournamentCreateDto toCreate) throws ValidationException {
    LOG.trace("validateTournament({})", toCreate);
    List<String> validationErrors = new ArrayList<>();
    if (toCreate.name() == null || toCreate.name().isBlank()) {
      validationErrors.add("No name given");
    }

    if (toCreate.endDate() == null) {
      validationErrors.add("No end date given");
    }

    if (toCreate.startDate() == null) {
      validationErrors.add("No start date given");
    }

    if (toCreate.startDate() != null && toCreate.endDate() != null
        && ! toCreate.startDate().isBefore(toCreate.endDate())) {
      validationErrors.add("End date must be after start date");
    }

    if (toCreate.participants().stream().map(p -> p.id())
        .collect(Collectors.toSet()).size() != toCreate.participants().size()) {
      validationErrors.add("Participant list contains duplicates");
    }



    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of tournament for create failed", validationErrors);
    }
  }

  public void validateForGenerateFirstRound(TournamentStandingsTreeDto tree) throws ValidationException {
    LOG.trace("validateForGenerateFirstRound({})", tree);

    if (tree.thisParticipant() != null) {
      throw new ValidationException("Validation of tree for generating first round failed",
          List.of("Tree contains participants"));
    }

    if (tree.branches() == null || tree.branches().size() != 2) {
      return;
    }

    validateForGenerateFirstRound(tree.branches().get(0));
    validateForGenerateFirstRound(tree.branches().get(1));
  }

}

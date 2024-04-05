package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsTreeDto;
import at.ac.tuwien.sepr.assignment.individual.entity.BranchPosition;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentParticipant;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentTree;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TournamentMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Convert a tournament entity object to a {@link TournamentListDto}.
   *
   * @param tournament the tournament to convert
   * @return the converted {@link TournamentListDto}
   */
  public TournamentListDto entityToListDto(Tournament tournament) {
    LOG.trace("entityToListDto({})", tournament);
    if (tournament == null) {
      return null;
    }

    return new TournamentListDto(
        tournament.getId(),
        tournament.getName(),
        tournament.getStartDate(),
        tournament.getEndDate()
    );
  }

  /**
   * Convert a tournament entity object to a {@link TournamentDetailDto}.
   *
   * @param tournament the tournament to convert
   * @param participants the participants of the tournament
   * @return the converted {@link TournamentDetailDto}
   */
  public TournamentDetailDto entitiesToDetailDto(Tournament tournament, List<TournamentDetailParticipantDto> participants) {
    LOG.trace("entityToDto({})", tournament);
    if (tournament == null) {
      return null;
    }

    return new TournamentDetailDto(
        tournament.getId(),
        tournament.getName(),
        tournament.getStartDate(),
        tournament.getEndDate(),
        participants
    );
  }

  /**
   * Convert a tournament participant entity object to a {@link TournamentDetailParticipantDto}.
   * The given map of horses needs to contain the breed of {@code horse}.
   *
   * @param participant the participant to convert
   * @param horses a map of horses identified by their id, required for mapping participants
   * @return the converted {@link TournamentDetailParticipantDto}
   */
  public TournamentDetailParticipantDto participantToDetailDto(TournamentParticipant participant, Map<Long, HorseDetailDto> horses) {
    LOG.trace("participantToDetailDto({})", participant);
    if (participant == null) {
      return null;
    }

    var horse = horseFromMap(participant, horses);

    return new TournamentDetailParticipantDto(
        horse.id(),
        horse.name(),
        horse.dateOfBirth(),
        participant.getEntryNumber(),
        participant.getRoundReached()
    );
  }

  // todo javadocs
  private HorseDetailDto horseFromMap(TournamentParticipant participant, Map<Long, HorseDetailDto> map) {
    var horseId = participant.getHorseId();
    if (horseId == null) {
      return null;
    } else {
      return Optional.ofNullable(map.get(horseId))
          .orElseThrow(() -> new FatalException(
              "Saved participant with id " + participant.getId() + " refers to non-existing horse with id " + horseId));
    }
  }

  /**
   * Convert a {@link TournamentCreateDto} entity object to a {@link TournamentDetailDto}.
   *
   * @param createDto the tournament to convert
   * @return the converted {@link TournamentDetailDto}
   */
  public TournamentDetailDto createDtoToEntity(TournamentCreateDto createDto) {
    var participants = createDto.participants().stream().map(p -> new TournamentDetailParticipantDto(
        p.id(),
        p.name(),
        p.dateOfBirth(),
        createDto.participants().indexOf(p),
        0
    )).toList();

    return new TournamentDetailDto(
        createDto.id(),
        createDto.name(),
        createDto.startDate(),
        createDto.endDate(),
        participants
    );
  }

  /**
   * Convert a tournament participant entity object to a {@link TournamentDetailParticipantDto}.
   * The given map of horses needs to contain the breed of {@code horse}.
   *
   * @param participant the participant to convert
   * @param horses a map of horses identified by their id, required for mapping participants
   * @return the converted {@link TournamentDetailParticipantDto}
   */
  public TournamentStandingsTreeDto branchesToStandingsTree(Collection<TournamentTree> branches, Map<Long, TournamentDetailParticipantDto> participants) {
    LOG.trace("branchesToStandingsTree({}, {})", branches, participants);
    var branchesMap = branches.stream().collect(Collectors.toMap(TournamentTree::getId, Function.identity()));
    LOG.info("test: {} ", branches);
    var rootEntity = branches.stream().filter(t -> t.getParentId() == 0).findFirst().get();
    var rootDto = findBranches(null, rootEntity.getId(), branchesMap, participants, 4);

    return rootDto;
  }

  private TournamentStandingsTreeDto findBranches(TournamentStandingsTreeDto lastDto, Long currentId, Map<Long, TournamentTree> branches, Map<Long, TournamentDetailParticipantDto> participants, int remainingDepth) {
    if (remainingDepth == 0)
      return null;
    TournamentTree currentBranch = branches.get(currentId);

    TournamentDetailParticipantDto currentParticipant = null;
    List<TournamentStandingsTreeDto> currentBranches = null;

    if (currentBranch.getParticipantId() != null) {
      currentParticipant = participants.get(currentBranch.getParticipantId());
    }
    if (remainingDepth > 1) {
      currentBranches = new ArrayList<>();
    }

    var currentDto = new TournamentStandingsTreeDto(currentParticipant, currentBranches);

    switch (currentBranch.getBranchPosition()) {
      case UPPER -> lastDto.branches().add(0, currentDto);
      case LOWER -> lastDto.branches().add(1, currentDto);
    }

    branches.values().stream().filter(b -> Objects.equals(b.getParentId(), currentId))
        .forEach(b -> findBranches(currentDto, b.getId(), branches, participants, remainingDepth - 1));

    return currentDto;
  }
}

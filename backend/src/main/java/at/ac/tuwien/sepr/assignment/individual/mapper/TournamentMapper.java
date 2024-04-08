package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsTreeDto;
import at.ac.tuwien.sepr.assignment.individual.type.BranchPosition;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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

  /**
   * This method retrieves a horse from a map using the horseId from a participant.
   *
   * @param participant the participant whose horseId is to be used
   * @param map the map of horses
   * @return the HorseDetailDto object if found, null otherwise
   */
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
   * This method converts a collection of TournamentTree entities into a TournamentStandingsTreeDto.
   *
   * @param branches the collection of TournamentTree entities
   * @param participants the map of TournamentDetailParticipantDto objects, identified by their id
   * @return the TournamentStandingsTreeDto object representing the tournament tree
   */
  public TournamentStandingsTreeDto branchesToStandingsTree(Collection<TournamentTree> branches, Map<Long, TournamentDetailParticipantDto> participants) {
    LOG.trace("branchesToStandingsTree({}, {})", branches, participants);
    var branchesMap = branches.stream().collect(Collectors.toMap(TournamentTree::getId, Function.identity()));
    var rootEntity = branches.stream().filter(t -> t.getBranchPosition().equals(BranchPosition.FINAL_WINNER)).findFirst().get();
    var rootDto = findBranches(null, rootEntity.getId(), branchesMap, participants, 4);

    return rootDto;
  }

  /**
   * This method recursively finds branches in a tournament tree.
   * It stops when the remaining depth is zero.
   *
   * @param lastDto the last TournamentStandingsTreeDto object
   * @param currentId the current id of the branch
   * @param branches the map of branches
   * @param participants the map of participants
   * @param remainingDepth the remaining depth of the tree
   * @return the current TournamentStandingsTreeDto object
   */
  private TournamentStandingsTreeDto findBranches(TournamentStandingsTreeDto lastDto, Long currentId,
                                                  Map<Long, TournamentTree> branches,
                                                  Map<Long, TournamentDetailParticipantDto> participants, int remainingDepth) {
    if (remainingDepth == 0) {
      return null;
    }
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
      default -> new AtomicInteger(1); // do not do nothing, idk why checkstyle wants me to add this;
    }

    branches.values().stream().filter(b -> Objects.equals(b.getParentId(), currentId))
        .forEach(b -> findBranches(currentDto, b.getId(), branches, participants, remainingDepth - 1));

    return currentDto;
  }

  /**
   * This method converts a tournament tree to branches.
   * It updates the participantId of each branch based on the horseId from the treeDto.
   *
   * @param tournamentId the id of the tournament
   * @param treeDto the TournamentStandingsTreeDto object
   * @param branches the collection of branches
   * @param participants the collection of participants
   * @return the updated collection of branches
   */
  public Collection<TournamentTree> tournamentTreeToBranches(Long tournamentId, TournamentStandingsTreeDto treeDto,
                                                             Collection<TournamentTree> branches, Collection<TournamentParticipant> participants) {
    var root = branches.stream().filter(b -> b.getBranchPosition().equals(BranchPosition.FINAL_WINNER)).findFirst().get();
    var participantMap = participants.stream().collect(Collectors.toMap(TournamentParticipant::getHorseId, Function.identity()));
    root.setParticipantId(treeDto.thisParticipant() == null ? null : participantMap.get(treeDto.thisParticipant().horseId()).getId());

    recursiveParticipantUpdate(treeDto.branches().get(0), BranchPosition.UPPER, branches, root.getId(), participantMap);
    recursiveParticipantUpdate(treeDto.branches().get(1), BranchPosition.LOWER, branches, root.getId(), participantMap);


    return branches;
  }

  private void recursiveParticipantUpdate(TournamentStandingsTreeDto treeDto, BranchPosition pos,
                                         Collection<TournamentTree> branches, Long previousId, Map<Long, TournamentParticipant> participantMap) {
    var current = branches.stream().filter(b -> Objects.equals(b.getParentId(), previousId) && b.getBranchPosition().equals(pos)).findFirst().get();
    current.setParticipantId(treeDto.thisParticipant() == null ? null : participantMap.get(treeDto.thisParticipant().horseId()).getId());

    if (treeDto.branches() == null || treeDto.branches().size() != 2) {
      return;
    }

    recursiveParticipantUpdate(treeDto.branches().get(0), BranchPosition.UPPER, branches, current.getId(), participantMap);
    recursiveParticipantUpdate(treeDto.branches().get(1), BranchPosition.LOWER, branches, current.getId(), participantMap);
  }

  /**
   * This method determines the rounds reached for each participant in a tournament.
   * It returns a map of participants and the rounds they reached.
   *
   * @param participants the collection of participants
   * @param tree the TournamentStandingsTreeDto object
   * @return the map of participants and the rounds they reached
   */
  public Map<TournamentParticipant, Integer> determineRoundsReachedForParticipants(Collection<TournamentParticipant> participants,
                                                                                   TournamentStandingsTreeDto tree) {
    Map<TournamentParticipant, Integer> out = new HashMap<>();
    Map<Long, TournamentParticipant> remainingParticipants = participants.stream()
        .collect(Collectors.toMap(TournamentParticipant::getHorseId, Function.identity()));
    recursiveDetermineRoundsReached(tree, 3, remainingParticipants, out);
    return out;
  }

  private void recursiveDetermineRoundsReached(TournamentStandingsTreeDto tree, int points, Map<Long, TournamentParticipant> remainingParticipants,
                                               Map<TournamentParticipant, Integer> out) {
    if (points == 0) {
      return;
    }

    if (tree.thisParticipant() != null && remainingParticipants.containsKey(tree.thisParticipant().horseId())) {
      var participant = remainingParticipants.get(tree.thisParticipant().horseId());
      out.put(participant, points);
      remainingParticipants.remove(participant.getHorseId());
    }

    points--;

    recursiveDetermineRoundsReached(tree.branches().get(0), points, remainingParticipants, out);
    recursiveDetermineRoundsReached(tree.branches().get(1), points, remainingParticipants, out);
  }

}

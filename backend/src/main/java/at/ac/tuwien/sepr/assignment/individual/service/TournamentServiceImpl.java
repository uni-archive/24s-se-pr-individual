package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsTreeDto;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentParticipant;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentTree;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.TournamentMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.TournamentDao;
import at.ac.tuwien.sepr.assignment.individual.type.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TournamentServiceImpl implements TournamentService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final TournamentDao dao;
  private final TournamentMapper mapper;
  private final TournamentValidator validator;
  private final HorseService horseService;

  public TournamentServiceImpl(TournamentDao dao, TournamentMapper mapper, HorseService horseService, TournamentValidator validator) {
    this.dao = dao;
    this.mapper = mapper;
    this.horseService = horseService;
    this.validator = validator;
  }

  @Override
  public Stream<TournamentListDto> search(TournamentSearchDto searchParameters) {
    var tournaments = dao.search(searchParameters);

    return tournaments.stream()
        .map(mapper::entityToListDto);
  }

  @Override
  public TournamentDetailDto create(TournamentCreateDto toCreate) throws ValidationException, ConflictException, NotFoundException {
    LOG.info("Creating tournament with name {}", toCreate.name());
    validator.validateTournament(toCreate);
    TournamentDetailDto tournamentDetail = mapper.createDtoToEntity(toCreate);
    var tournament = dao.create(tournamentDetail);
    var participantEntities = dao.getParticipantsByTournamentId(tournament.getId());
    var horseMap = horseService.findHorsesByIds(
            participantEntities.stream().map(TournamentParticipant::getHorseId).collect(Collectors.toSet()))
        .collect(Collectors.toMap(HorseDetailDto::id, Function.identity()));
    var participants = participantEntities.stream().map(p -> mapper.participantToDetailDto(p, horseMap)).collect(Collectors.toList());
    return mapper.entitiesToDetailDto(tournament, participants);
  }

  @Override
  public TournamentStandingsDto getStandingsById(long id) throws NotFoundException {
    LOG.info("Getting tournament standings with tournament id {}", id);
    var tournament = dao.getById(id);
    var branches = dao.getBranchesByTournamentId(id);
    var participantEntities = dao.getParticipantsByTournamentId(id);
    var participantEntityMap = participantEntities.stream().collect(Collectors.toMap(TournamentParticipant::getHorseId, Function.identity()));
    var horseMap = horseService.findHorsesByIds(
            participantEntities.stream().map(TournamentParticipant::getHorseId).collect(Collectors.toSet()))
        .collect(Collectors.toMap(HorseDetailDto::id, Function.identity()));
    var participants = participantEntities.stream().map(p -> mapper.participantToDetailDto(p, horseMap)).collect(Collectors.toList());

    var standingsTree = mapper.branchesToStandingsTree(branches, participants.stream().collect(Collectors.toMap(p -> participantEntityMap.get(p.horseId()).getId(), Function.identity())));

    return new TournamentStandingsDto(
        id,
        tournament.getName(),
        participants,
        standingsTree
    );
  }

  @Override
  public TournamentDetailDto getById(long id) throws NotFoundException {
    LOG.info("Getting tournament with id {}", id);
    var tournament = dao.getById(id);
    var participantEntities = dao.getParticipantsByTournamentId(id);
    var horseMap = horseService.findHorsesByIds(
        participantEntities.stream().map(TournamentParticipant::getHorseId).collect(Collectors.toSet()))
        .collect(Collectors.toMap(HorseDetailDto::id, Function.identity()));
    var participants = participantEntities.stream().map(p -> mapper.participantToDetailDto(p, horseMap)).collect(Collectors.toList());
    return mapper.entitiesToDetailDto(tournament, participants);
  }


  @Override
  public TournamentStandingsTreeDto updateStandings(long tournamentId, TournamentStandingsTreeDto toUpdate) throws ValidationException, ConflictException, NotFoundException {
    LOG.trace("updateStandings({}, {})", tournamentId, toUpdate);
    validator.validateForStandingsUpdate(toUpdate, 8);

    var participantEntities = dao.getParticipantsByTournamentId(tournamentId);
    var participantEntityMap = participantEntities.stream().collect(Collectors.toMap(TournamentParticipant::getHorseId, Function.identity()));

    var roundsReachedPerParticipant = mapper.determineRoundsReachedForParticipants(participantEntities, toUpdate);
    for (var entry : roundsReachedPerParticipant.entrySet()) {
      participantEntityMap.get(entry.getKey().getHorseId()).setRoundReached(entry.getValue());
    }
    dao.updateParticipants(participantEntities);

    Collection<TournamentTree> branchesToUpdate = mapper.tournamentTreeToBranches(tournamentId, toUpdate, dao.getBranchesByTournamentId(tournamentId), participantEntities);
    dao.updateStandings(branchesToUpdate);
    var updatedBranches = dao.getBranchesByTournamentId(tournamentId);

    var horseMap = horseService.findHorsesByIds(
            participantEntities.stream().map(TournamentParticipant::getHorseId).collect(Collectors.toSet()))
        .collect(Collectors.toMap(HorseDetailDto::id, Function.identity()));
    var participants = participantEntities.stream().map(p -> mapper.participantToDetailDto(p, horseMap)).toList();



    var standingsTree = mapper.branchesToStandingsTree(updatedBranches, participants.stream().collect(Collectors.toMap(p -> participantEntityMap.get(p.horseId()).getId(), Function.identity())));



    return standingsTree;
  }

  @Override
  public TournamentStandingsTreeDto generateFirstRoundMatches(long tournamentId, TournamentStandingsTreeDto tree) throws ValidationException, ConflictException, NotFoundException {
    validator.validateForGenerateFirstRound(tree);
    var participants = dao.getParticipantsByTournamentId(tournamentId);
    var horseMap = horseService.findHorsesByIds(participants.stream().map(TournamentParticipant::getHorseId).collect(Collectors.toSet()))
        .collect(Collectors.toMap(HorseDetailDto::id, Function.identity()));
    var allParticipationsOfHorses = dao.getParticipationsForHorseIds(participants.stream().map(TournamentParticipant::getHorseId).collect(Collectors.toList()));
    var horsePoints = allParticipationsOfHorses.stream().map(p -> new Pair<Long, Integer>(p.getHorseId(), switch(p.getRoundReached()) {
      case 3 -> 5;
      case 2 -> 3;
      case 1 -> 1;
      default -> 0;
    })).collect(Collectors.groupingBy(Pair::getFirst, Collectors.summingInt(Pair::getSecond)));

    var participantsSorted = participants.stream().sorted(new Comparator<TournamentParticipant>() {
      @Override
      public int compare(TournamentParticipant o1, TournamentParticipant o2) {
        var score1 = horsePoints.get(o1.getHorseId());
        var score2 = horsePoints.get(o2.getHorseId());

        if (!Objects.equals(score1, score2)){
          return -score1.compareTo(score2);
        }
        return -horseMap.get(o1.getHorseId()).name().compareTo(horseMap.get(o1.getHorseId()).name());
      }
    }).toList();
    var firstRoundBranches = dao.getFirstRoundBranchesByTournamentId(tournamentId).stream().sorted(Comparator.comparing(TournamentTree::getFirstRoundIndex)).toList();
    for (int i = 0; i < 8 / 2; i++) {
      firstRoundBranches.get(i * 2).setParticipantId(participantsSorted.get(i).getId());
      firstRoundBranches.get(i * 2 + 1).setParticipantId(participantsSorted.get(participantsSorted.size() - i - 1).getId());
    }
    dao.updateStandings(firstRoundBranches);

    var branches = dao.getBranchesByTournamentId(tournamentId);
    var participantEntityMap = participants.stream().collect(Collectors.toMap(TournamentParticipant::getHorseId, Function.identity()));
    var participantsDao = participants.stream().map(p -> mapper.participantToDetailDto(p, horseMap)).toList();

    var standingsTree = mapper.branchesToStandingsTree(branches, participantsDao.stream().collect(Collectors.toMap(p -> participantEntityMap.get(p.horseId()).getId(), Function.identity())));

    return standingsTree;
  }
}

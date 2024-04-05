package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsDto;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentParticipant;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.TournamentMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.TournamentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TournamentServiceImpl implements TournamentService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final TournamentDao dao;
  private final TournamentMapper mapper;
  //private final TournamentValidator validator;
  private final HorseService horseService;

  public TournamentServiceImpl(TournamentDao dao, TournamentMapper mapper, HorseService horseService) {
    this.dao = dao;
    this.mapper = mapper;
    this.horseService = horseService;
  }

  @Override
  public Stream<TournamentListDto> search(TournamentSearchDto searchParameters) {
    var tournaments = dao.search(searchParameters);

    return tournaments.stream()
        .map(mapper::entityToListDto);
  }

  @Override
  public TournamentDetailDto create(TournamentCreateDto toCreate) throws ValidationException, ConflictException {
    LOG.info("Creating tournament with name {}", toCreate.name());
    TournamentDetailDto tournament = mapper.createDtoToEntity(toCreate);
    var created = dao.create(tournament);
    return null; // todo CHANGE THIS
  }

//  @Override
//  public TournamentStandingsDto getStandingsById(long id) throws NotFoundException {
//    LOG.info("Getting tournament with id {}", id);
//    var tournamentStandings = dao.getStandingsById(id);
//    return tournamentStandings;
//  }

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

}

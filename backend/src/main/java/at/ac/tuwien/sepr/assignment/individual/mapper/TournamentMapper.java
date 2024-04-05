package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentParticipant;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
}

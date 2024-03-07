package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailParticipantDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

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

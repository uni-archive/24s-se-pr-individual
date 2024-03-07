package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;
import java.util.List;

public record TournamentDetailDto(
    Long id,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    List<TournamentDetailParticipantDto> participants
) {
  public TournamentDetailDto withId(long newId) {
    return new TournamentDetailDto(
        newId,
        name,
        startDate,
        endDate,
        participants);
  }
}

package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;
import java.util.List;

public record TournamentCreateDto(
    Long id,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    List<HorseSelectionDto> participants
) {
  public TournamentCreateDto withId(long newId) {
    return new TournamentCreateDto(
        newId,
        name,
        startDate,
        endDate,
        participants);
  }
}

package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

public record TournamentDetailParticipantDto(
    long horseId,
    String name,
    LocalDate dateOfBirth,
    int entryNumber,
    int roundReached
) {
  public TournamentDetailParticipantDto withId(long newId) {
    return new TournamentDetailParticipantDto(
        newId,
        name,
        dateOfBirth,
        entryNumber,
        roundReached);
  }
}

package at.ac.tuwien.sepr.assignment.individual.entity;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * Represents a horse in the persistent data store.
 */
public class TournamentParticipant {
  private Long id;
  private Long tournamentId;
  private Long horseId;
  private int entryNumber;
  private int roundReached;

  public Long getId() {
    return id;
  }

  public TournamentParticipant setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getTournamentId() {
    return tournamentId;
  }

  public TournamentParticipant setTournamentId(Long tournamentId) {
    this.tournamentId = tournamentId;
    return this;
  }

  public Long getHorseId() {
    return horseId;
  }

  public TournamentParticipant setHorseId(Long horseId) {
    this.horseId = horseId;
    return this;
  }

  public int getEntryNumber() {
    return entryNumber;
  }

  public TournamentParticipant setEntryNumber(int entryNumber) {
    this.entryNumber = entryNumber;
    return this;
  }

  public int getRoundReached() {
    return roundReached;
  }

  public TournamentParticipant setRoundReached(int roundReached) {
    this.roundReached = roundReached;
    return this;
  }

  @Override
  public String toString() {
    return "TournamentParticipant{"
        + "id=" + id
        + ", tournamentId=" + tournamentId
        + ", horseId=" + horseId
        + ", entryNumber=" + entryNumber
        + ", roundReached=" + roundReached
        + '}';
  }
}

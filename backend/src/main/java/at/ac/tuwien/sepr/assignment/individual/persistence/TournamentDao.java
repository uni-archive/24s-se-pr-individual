package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Tournament;
import at.ac.tuwien.sepr.assignment.individual.entity.TournamentParticipant;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;

import java.util.Collection;

/**
 * Data Access Object for tournaments.
 * Implements access functionality to the application's persistent data store regarding tournaments.
 */
public interface TournamentDao {

  /**
   * Get the tournaments that match the given search parameters.
   * Parameters that are {@code null} are ignored.
   * The name is considered a match, if the given parameter is a substring of the field in tournament.
   *
   * @param searchParameters the parameters to use in searching.
   * @return the tournaments where all given parameters match.
   */
  Collection<Tournament> search(TournamentSearchDto searchParameters);

  /**
   * Create a new tournament in the persistent data store.
   *
   * @param toCreate the tournament to create.
   * @return the created tournament.
   */
  Tournament create(TournamentDetailDto toCreate) throws ConflictException;

  /**
   * Get the details of a tournament from the persistent data store.
   *
   * @param id the id of the tournament to get.
   * @return the details of the tournament.
   */
  Tournament getById(long id) throws NotFoundException;

  /**
   * Get the participants of a tournament from the persistent data store.
   *
   * @param id the id of the tournament to get.
   * @return the participants of the tournament.
   */
  Collection<TournamentParticipant> getParticipantsByTournamentId(long id);

//
//  /**
//   * Get the standings of a tournament from the persistent data store.
//   *
//   * @param id the id of the tournament to get.
//   * @return the standings of the tournament.
//   */
//  TournamentStandingsDto getStandingsById(long id) throws NotFoundException;
}
